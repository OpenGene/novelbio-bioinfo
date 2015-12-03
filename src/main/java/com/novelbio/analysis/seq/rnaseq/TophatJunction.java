package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.novelbio.analysis.seq.AlignRecord;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.analysis.seq.mapping.StrandSpecific;
import com.novelbio.analysis.seq.rnaseq.JunctionInfo.JunctionUnit;
import com.novelbio.analysis.seq.sam.AlignmentRecorder;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.analysis.seq.sam.SamRecord;
import com.novelbio.base.SepSign;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.listOperate.ListBin;
import com.novelbio.listOperate.ListCodAbs;
import com.novelbio.listOperate.ListCodAbsDu;
import com.novelbio.listOperate.ListHashSearch;

public class TophatJunction extends ListHashSearch<JunctionInfo, ListCodAbs<JunctionInfo>, 
ListCodAbsDu<JunctionInfo, ListCodAbs<JunctionInfo>>, ListBin<JunctionInfo>> implements AlignmentRecorder {
	private static final Logger logger = Logger.getLogger(TophatJunction.class);
	
	private int intronMinLen = 15;
	private int junctionMinAnchorLen = 5;

	Map<String, JunctionUnit> mapJunUnitKey2Unit = new HashMap<String, JunctionUnit>();
	ArrayListMultimap<String, JunctionUnit> mapJunSite2JunUnit = ArrayListMultimap.create();
	String condition;
	String subGroup;
	HashMultimap<String, String> mapCondition2Group = HashMultimap.create();
	Map<String, Map<String, double[]>> mapCondition_Group2JunNum = new HashMap<>();
	StrandSpecific strandSpecific = StrandSpecific.NONE;
	
	/** 针对链特异性进行了优化 */
	public TophatJunction() {
		mapChrID2ListGff = new LinkedHashMap<>();
		mapName2DetailAbs = new LinkedHashMap<>();
		mapName2DetailNum = new LinkedHashMap<>();
		lsNameAll = new ArrayList<>();
		lsNameNoRedundent = new ArrayList<>();
	}
	/** 设定最短的intron长度，默认为25,也就是说小于25bp（<25）的都认为是deletion，该reads不加入可变剪接考察 */
	public void setIntronMinLen(int intronMinLen) {
	    this.intronMinLen = intronMinLen;
    }
	/** 设定junction reads的接头最短长度，譬如reads的一头搭到了某个exon上，如果这个长度小于该指定长度,默认为5(<5)，则该reads不加入可变剪接考察 */
	public void setJunctionMinAnchorLen(int junctionMinAnchorLen) {
	    this.junctionMinAnchorLen = junctionMinAnchorLen;
    }
	/** 设定测序连特异性的方向 */
	public void setStrandSpecific(StrandSpecific strandSpecific) {
		this.strandSpecific = strandSpecific;
	}
	public void setCondition(String condition, String subgroup) {
		this.condition = condition;
		this.subGroup = subgroup;
		mapCondition2Group.put(condition, subgroup);
		Map<String, double[]> mapGroup2Num = mapCondition_Group2JunNum.get(condition);
		if (mapGroup2Num == null) {
			mapGroup2Num = new HashMap<>();
		}
		mapGroup2Num.put(subgroup, new double[1]);
		mapCondition_Group2JunNum.put(condition, mapGroup2Num);
	}
	
	/** 获得指定时期和group下的全体junction数量，考虑了非unique mapping */
	public long getJunAllNum(String condition, String group) {
		if (mapCondition_Group2JunNum.containsKey(condition) && mapCondition_Group2JunNum.get(condition).containsKey(group)) {
			return (long)mapCondition_Group2JunNum.get(condition).get(group)[0];
		}
		return 0;
	}
	
	/** 获得condition对group的对照表 */
	public HashMultimap<String, String> getMapCondition2Group() {
		return mapCondition2Group;
	}
	/**添加samBam的文件用来获得信息 */
	public void addAlignRecord(AlignRecord alignRecord) {
		Boolean cis5to3 = getCis5to3(alignRecord);
		ArrayList<Align> lsAlign = alignRecord.getAlignmentBlocks();
		if (lsAlign.size() <= 1) {
			return;
		}
		//消除intron的影响
		List<Align> lsAlignNew = new ArrayList<>();
		for (int i = 0; i < lsAlign.size(); i++) {
			Align align = lsAlign.get(i);
			if ((i== 0 || i == lsAlign.size() - 1) && align.getLength() < junctionMinAnchorLen) {
				continue;
            }
			if (lsAlignNew.size() == 0) {
				lsAlignNew.add(align);
			} else {
				Align alignlast = lsAlignNew.get(lsAlignNew.size() - 1);
				if (align.getStartAbs() - alignlast.getEndAbs() < intronMinLen) {
					alignlast.setEnd(align.getEndAbs());
				} else {
					lsAlignNew.add(align);
				}
			}
        }
		int size = lsAlignNew.size();
		if (size <= 1) {
			return;
		}
		String chrID = alignRecord.getRefID();
		List<JunctionUnit> lsJun = new ArrayList<>();
		double[] junNum = mapCondition_Group2JunNum.get(condition).get(subGroup);
		for (int i = 0; i < size - 1; i++) {
			Align alignThis = lsAlignNew.get(i);
			Align alignNext = lsAlignNew.get(i + 1);
			int junStart = alignThis.getEndAbs();
			int junEnd = alignNext.getStartAbs();
			JunctionUnit jun = new JunctionUnit(chrID, junStart, junEnd);
			jun.setConsiderStrand(strandSpecific != StrandSpecific.NONE);
			if (cis5to3 != null) {
				jun.setCis5to3(cis5to3);
			}
			
			jun.addReadsNum(condition, subGroup, (double)1/alignRecord.getMappedReadsWeight());
			lsJun.add(jun);
			junNum[0] += (double)1/alignRecord.getMappedReadsWeight();
		}
		addJunctionInfo(lsJun);
	}
	
	private Boolean getCis5to3(AlignRecord alignRecord) {
		if (strandSpecific == StrandSpecific.NONE || !(alignRecord instanceof SamRecord)) {
			return null;
		}
		SamRecord samRecord = (SamRecord)alignRecord;
		return samRecord.isCis5to3ConsiderStrand(strandSpecific);
	}
	
	/** 
	 * 添加一系列的junctionUnit，都是来源于同一条reads的
	 */
	private void addJunctionInfo(List<JunctionUnit> lsJun) {
		JunctionUnit junBefore = null, junThis = null,  junAfter = null;
		if (lsJun.size() == 1) {
			junThis = lsJun.get(0);
			addJun(junThis, junBefore, junAfter);
			return;
		}
		for (int i = 1; i < lsJun.size(); i++) {
			junAfter = lsJun.get(i);
			if (junThis == null) junThis = lsJun.get(i-1);			
			addJun(junThis, junBefore, junAfter);
			junBefore = junThis;
			junThis = junAfter;
		}
		addJun(junThis, junBefore, null);
	}
	
	private void addJun(JunctionUnit junThis, JunctionUnit junBefore, JunctionUnit junAfter) {
		String juncUnitKey = junThis.key();
		if (mapJunUnitKey2Unit.containsKey(juncUnitKey)) {
			JunctionUnit junThisExist = mapJunUnitKey2Unit.get(juncUnitKey);
			junThisExist.addReadsJuncUnit(junThis);
			//TODO 感觉有问题，就是before和after的引用是否正确，考虑从mapJunUnitKey2Unit中获取before和end的信息
			junThisExist.addJunBeforeAbs(junBefore); junThisExist.addJunAfterAbs(junAfter);
		} else {
			junThis.addJunBeforeAbs(junBefore); junThis.addJunAfterAbs(junAfter);
			JunctionInfo juncInfo = new JunctionInfo(strandSpecific != StrandSpecific.NONE, junThis);
			ListBin<JunctionInfo> lsJunctionInfos = mapChrID2ListGff.get(junThis.getRefID().toLowerCase());
			if (lsJunctionInfos == null) {
				lsJunctionInfos = new ListBin<>();
				mapChrID2ListGff.put(junThis.getRefID().toLowerCase(), lsJunctionInfos);
			}
			lsJunctionInfos.add(juncInfo);
			mapJunUnitKey2Unit.put(juncUnitKey, junThis);
			mapJunSite2JunUnit.put(junThis.getRefID().toLowerCase() + SepSign.SEP_ID + junThis.getStartAbs(), junThis);
			mapJunSite2JunUnit.put(junThis.getRefID().toLowerCase() + SepSign.SEP_ID + junThis.getEndAbs(), junThis);
		}
	}
	
	/**
	 * 给定坐标和位点，找出全体条件下locsite,以及总共有多少reads支持
	 * 0表示没有junction
	 * @param chrID
	 * @param locSite
	 * @return
	 */
	public double getJunctionSiteAll(boolean cis5to3, String chrID, int locSite) {
		double num = 0;
		List<JunctionUnit> lsJunctionUnits = mapJunSite2JunUnit.get(chrID + SepSign.SEP_ID + locSite);
		for (JunctionUnit junctionUnit : lsJunctionUnits) {
			if (strandSpecific != StrandSpecific.NONE && cis5to3 != junctionUnit.isCis5to3()) {
				continue;
			}
			num += junctionUnit.getReadsNumAll();
		}
		return num;
	}
	/**
	 * 给定坐标和位点，找出全体条件下locsite,以及总共有多少reads支持
	 * 0表示没有junction
	 * @param chrID
	 * @param locSite
	 * @return
	 */
	public double getJunctionSiteAll(boolean cis5to3, String chrID, int locStart, int locEnd) {
		int start = Math.min(locStart, locEnd), end = Math.max(locStart, locEnd);
		Boolean cis5to3Final = null;
		if (strandSpecific != StrandSpecific.NONE) {
			cis5to3Final = cis5to3;
		}
		JunctionUnit junctionUnit = mapJunUnitKey2Unit.get(JunctionUnit.getKey(cis5to3Final, chrID, start, end));
		if (junctionUnit == null) {
			return 0;
		} else {
			return junctionUnit.getReadsNumAll();
		}
	}
	
	/**
	 * 给定坐标和位点，找出全体条件下locsite,以及总共有多少reads支持
	 * 0表示没有junction
	 * @param chrID
	 * @param locSite
	 * @return
	 */
	public JunctionUnit getJunctionSiteAll(String key) {
		return mapJunUnitKey2Unit.get(key);
	}
	
	/**
	 * 给定坐标和位点，找出locsite,以及总共有多少reads支持
	 * 0表示没有junction
	 * @param chrID
	 * @param locSite
	 * @return
	 */
	public Map<String, Double> getJunctionSite(String condition, boolean cis5to3, String chrID, int locSite) {
		Map<String, Double> mapGroup2Value = new HashMap<>();
		List<JunctionUnit> lsJunctionUnits = mapJunSite2JunUnit.get(chrID + SepSign.SEP_ID + locSite);
		
		for (JunctionUnit junctionUnit : lsJunctionUnits) {
			if (strandSpecific != StrandSpecific.NONE && cis5to3 != junctionUnit.isCis5to3()) {
				continue;
			}
			Map<String, Double> mapGroup2ValueTmp = junctionUnit.getReadsNum(condition, mapCondition2Group.get(condition));
			if (mapGroup2ValueTmp.isEmpty()) {
				continue;
			}
			if (mapGroup2Value.isEmpty()) {
				mapGroup2Value = new HashMap<>(mapGroup2ValueTmp);
			} else {
				for (String group : mapGroup2ValueTmp.keySet()) {
					double value = mapGroup2Value.get(group);
					double valueTmp = mapGroup2ValueTmp.get(group);
					mapGroup2Value.put(group, value + valueTmp);
				}
			}
		}
		
		if (mapGroup2Value.isEmpty()) {
			for (String group : mapCondition2Group.get(condition)) {
				mapGroup2Value.put(group, 0.0);
			}
			return mapGroup2Value;
		}
		
		return mapGroup2Value;
	}
	
	/**
	 * 给定坐标和位点，找出locsite,以及总共有多少reads支持
	 * 0表示没有junction
	 * @param chrID
	 * @param locSite
	 * @return
	 */
	public List<JunctionUnit> getLsJunctionUnit(String condition, boolean cis5to3, String chrID, int locSite) {
		List<JunctionUnit> lsJunctionUnits = mapJunSite2JunUnit.get(chrID + SepSign.SEP_ID + locSite);
		List<JunctionUnit> lsJuncResult = new ArrayList<>();
		for (JunctionUnit junctionUnit : lsJunctionUnits) {
			if (strandSpecific != StrandSpecific.NONE && cis5to3 != junctionUnit.isCis5to3()) {
				continue;
			}
			lsJuncResult.add(junctionUnit);
		}
		return lsJuncResult;
	}
	
	/**
	 * 给定坐标和位点，找出locsite,以及总共有多少reads支持
	 * @param condition 时期
	 * @param cis5to3 方向
	 * @param chrID
	 * @param locStartSite
	 * @param locEndSite
	 * @return key: 该condition所对应的group <br>
	 * value: 具体的junction reads 数量
	 */
	public Map<String, Double> getJunctionSite(String condition, boolean cis5to3, String chrID, int locStartSite, int locEndSite) {
		Map<String, Double> mapGroup2Value = new HashMap<>();
		for (String group : mapCondition2Group.get(condition)) {
			mapGroup2Value.put(group, getJunctionSite(condition, group, cis5to3, chrID, locStartSite, locEndSite));
		}
		if (mapGroup2Value.isEmpty()) {
			for (String group : mapCondition2Group.get(condition)) {
				mapGroup2Value.put(group, 0.0);
			}
			return mapGroup2Value;
		}
		return mapGroup2Value;
	}
	
	/**
	 * 给定坐标和位点，找出locsite,以及总共有多少reads支持
	 * 0表示没有junction
	 * @param chrID
	 * @param locSite
	 * @return
	 */
	public int getJunctionSite(String condition, String group, String chrID, int locSite) {
		int num = 0;
		List<JunctionUnit> lsJunctionUnits = mapJunSite2JunUnit.get(chrID + SepSign.SEP_ID + locSite);
		for (JunctionUnit junctionUnit : lsJunctionUnits) {
			num += junctionUnit.getReadsNum(condition, group);
		}
		return num;
	}
	/**
	 * 给定坐标和位点，找出locsite
	 * @param chrID
	 * @param cis5to3 junction的方向，只有当StrandSpecific不为NONE时才会起作用
	 * @param locStartSite 无所谓前后，内部自动判断
	 * @param locEndSite
	 * @return
	 */
	public double getJunctionSite(String condition, String group, boolean cis5to3, String chrID, int locStartSite, int locEndSite) {
		int start = Math.min(locStartSite, locEndSite), end = Math.max(locStartSite, locEndSite);
		Boolean cis5to3Final = null;
		if (strandSpecific != StrandSpecific.NONE) {
			cis5to3Final = cis5to3;
		}
		JunctionUnit junctionUnit = mapJunUnitKey2Unit.get(JunctionUnit.getKey(cis5to3Final, chrID, start, end));
		if (junctionUnit == null) {
			return 0;
		} else {
			return junctionUnit.getReadsNum(condition, group);
		}
	}

	@Override
	public void summary() {
	}
	
	/** 读取完bam文件后必须调用该方法进行总结 */
	public void conclusion() {
		for (Entry<String, ListBin<JunctionInfo>> entry : mapChrID2ListGff.entrySet()) {
			String chrID = entry.getKey().toLowerCase();
			ListBin<JunctionInfo> listGff = entry.getValue();
			listGff.sort();
			ListBin<JunctionInfo> listGffNew = combineOverlapGene(listGff);
			mapChrID2ListGff.put(chrID, listGffNew);
			listGff = null;
		}
		
		try {
			setItemDistance();
			setOther();
			fillMapName2DetailNum();
			fillMapName2Detail();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private void fillMapName2DetailNum() {
		mapName2DetailNum = new LinkedHashMap<String, Integer>();
		for ( ListBin<JunctionInfo>  listAbs : mapChrID2ListGff.values()) {
			mapName2DetailNum.putAll(listAbs.getMapName2DetailAbsNum());
		}
	}
	
	private void fillMapName2Detail() {
		mapName2DetailAbs = new LinkedHashMap<String, JunctionInfo>();
		for (ListBin<JunctionInfo>  listAbs : mapChrID2ListGff.values()) {
			mapName2DetailAbs.putAll(listAbs.getMapName2DetailAbs());
		}
	}
	
	/** 这里读取的是sam，bam文件 */
	@Override
	protected void ReadGffarrayExcep(String gfffilename) throws Exception {
		SamFile samFile = new SamFile(gfffilename);
		for (SamRecord samRecord : samFile.readLines()) {
			addAlignRecord(samRecord);
		}
	}

	/**
	 * 合并重复的GffDetailGene
	 * @return
	 */
	private static ListBin<JunctionInfo> combineOverlapGene(ListBin<JunctionInfo> lsInput) {
		ListBin<JunctionInfo> listGffNew = new ListBin<>();
		JunctionInfo gffDetailGeneLast = null;
		//合并两个重叠的基因
		for (JunctionInfo gffDetailGene : lsInput) {
			if (gffDetailGeneLast != null && gffDetailGene.getRefID().equals(gffDetailGeneLast.getRefID())) {
				double[] regionLast = new double[]{gffDetailGeneLast.getStartAbs(), gffDetailGeneLast.getEndAbs()};
				double[] regionThis = new double[]{gffDetailGene.getStartAbs(), gffDetailGene.getEndAbs() };
				double[]  overlapInfo = ArrayOperate.cmpArray(regionLast, regionThis);
				if ((overlapInfo[2] > 0.2 || overlapInfo[3] > 0.2)) {
					gffDetailGeneLast.addJuncInfo(gffDetailGene);
					continue;
				}
			}
			listGffNew.add(gffDetailGene);
			gffDetailGeneLast = gffDetailGene;
		}
		return listGffNew;
	}
	@Override
	public Align getReadingRegion() {
		return null;
	}
}
