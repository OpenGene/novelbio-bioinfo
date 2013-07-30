package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.analysis.seq.AlignRecord;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.analysis.seq.rnaseq.JunctionInfo.JunctionUnit;
import com.novelbio.analysis.seq.sam.AlignmentRecorder;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.analysis.seq.sam.SamRecord;
import com.novelbio.base.SepSign;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.listOperate.ListBin;
import com.novelbio.base.dataStructure.listOperate.ListCodAbs;
import com.novelbio.base.dataStructure.listOperate.ListCodAbsDu;
import com.novelbio.base.dataStructure.listOperate.ListHashSearch;

public class TophatJunction extends ListHashSearch<JunctionInfo, ListCodAbs<JunctionInfo>, 
ListCodAbsDu<JunctionInfo, ListCodAbs<JunctionInfo>>, ListBin<JunctionInfo>> implements AlignmentRecorder {
	private static final Logger logger = Logger.getLogger(TophatJunction.class);
	
	private static int intronMinLen = 25;
	
	ArrayListMultimap<String, String> mapCond2JuncFile = ArrayListMultimap.create();
	Map<String, JunctionUnit> mapJunUnitKey2Unit = new HashMap<String, JunctionUnit>();
	ArrayListMultimap<String, JunctionUnit> mapJunSite2JunUnit = ArrayListMultimap.create();
	String condition;
	
	public TophatJunction() {
		mapChrID2ListGff = new LinkedHashMap<>();
		mapName2DetailAbs = new LinkedHashMap<>();
		mapName2DetailNum = new LinkedHashMap<>();
		lsNameAll = new ArrayList<>();
		lsNameNoRedundent = new ArrayList<>();
	}
	public void setCondition(String condition) {
		this.condition = condition;
	}
	
	/**添加samBam的文件用来获得信息 */
	public void addAlignRecord(AlignRecord alignRecord) {
		ArrayList<Align> lsAlign = alignRecord.getAlignmentBlocks();
		if (lsAlign.size() <= 1) {
			return;
		}
		//消除intron的影响
		List<Align> lsAlignNew = new ArrayList<>();
		for (Align align : lsAlign) {
			if (lsAlignNew.size() == 0) {
				lsAlignNew.add(align);
			} else {
				Align alignlast = lsAlignNew.get(lsAlignNew.size() - 1);
				if (align.getStartAbs() - alignlast.getEndAbs() <= intronMinLen) {
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
		for (int i = 0; i < size - 1; i++) {
			Align alignThis = lsAlignNew.get(i);
			Align alignNext = lsAlignNew.get(i + 1);
			int junStart = alignThis.getEndAbs();
			int junEnd = alignNext.getStartAbs();
			JunctionUnit jun = new JunctionUnit(chrID, junStart, junEnd);
			jun.addReadsNum1(condition);
			lsJun.add(jun);
		}
		addJunctionInfo(lsJun);
	}
//
//	/**
//	 * 读取junction文件，文件中每个剪接位点只能出现一次\
//	 * @param condition
//	 * @param junctionFile
//	 */
//	@Deprecated
//	public void setJunFile(String condition, String junctionFile) {
//		mapCond2JuncFile.put(condition, junctionFile);
//	}
//	@Deprecated
//	public void readJuncFile() {
//		for (String condition : mapCond2JuncFile.keySet()) {
//			List<String> lsFileName = mapCond2JuncFile.get(condition);
//			for (String junctionFile : lsFileName) {
//				readJuncFile(junctionFile);
//			}
//		}
//	}
//	
//	/**
//	 * 读取之前先设定{@link #setCondition(String)}
//	 * 读取junction文件，文件中每个剪接位点只能出现一次\
//	 * @param condition
//	 * @param junctionFile
//	 */
//	@Deprecated
//	private void readJuncFile(String junctionFile) {
//		TxtReadandWrite txtReadandWrite = new TxtReadandWrite(junctionFile);
//		for (String string : txtReadandWrite.readfileLs()) {
//			if (string.startsWith("track")) {
//				continue;
//			}
//			String[] ss = string.split("\t");
//			String chrID = ss[0];
//			
//			//junction位点都设定在exon上
//			int junct1 = Integer.parseInt(ss[1]) + Integer.parseInt(ss[10].split(",")[0]);
//			int junct2 = Integer.parseInt(ss[2]) - Integer.parseInt(ss[10].split(",")[1]) + 1;
//			int junctionNum = Integer.parseInt(ss[4]);
//			addJunctionInfo(chrID, junct1, junct2, junctionNum);
//		}
//		txtReadandWrite.close();
//	}
	
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
		String juncUnitKey = JunctionUnit.getKey(junThis.getRefID(), junThis.getStartAbs(), junThis.getEndAbs());
		if (mapJunUnitKey2Unit.containsKey(juncUnitKey)) {
			JunctionUnit junThisExist = mapJunUnitKey2Unit.get(juncUnitKey);
			junThisExist.addReadsNum(junThis);
			//TODO 感觉有问题，就是before和after的引用是否正确
			junThisExist.addJunBeforeAbs(junBefore);
			junThisExist.addJunAfterAbs(junAfter);
		} else {
			junThis.addJunBeforeAbs(junBefore); junThis.addJunAfterAbs(junAfter);
			JunctionInfo juncInfo = new JunctionInfo(junThis);
			ListBin<JunctionInfo> lsJunctionInfos = mapChrID2ListGff.get(junThis.getRefID());
			if (lsJunctionInfos == null) {
				lsJunctionInfos = new ListBin<>();
				mapChrID2ListGff.put(junThis.getRefID(), lsJunctionInfos);
			}
			lsJunctionInfos.add(juncInfo);
			mapJunUnitKey2Unit.put(junThis.key(), junThis);
			mapJunSite2JunUnit.put(junThis.getRefID().toLowerCase() + SepSign.SEP_ID + junThis.getStartAbs(), junThis);
			mapJunSite2JunUnit.put(junThis.getRefID().toLowerCase() + SepSign.SEP_ID + junThis.getEndAbs(), junThis);
		}
	}
	/** 
	 * 添加单个剪接位点reads
	 * @param chrID 染色体
	 * @param junctionStart 剪接起点
	 * @param junctionEnd 剪接终点
	 * @param junctionNum 剪接reads的数量
	 */
	private void addJunctionInfo(String chrID, int junctionStart, int junctionEnd, int junctionNum) {
		chrID = chrID.trim().toLowerCase();
		int junctionStartmin = Math.min(junctionStart, junctionEnd);
		int junctionEndmax = Math.max(junctionStart, junctionEnd);
		String juncUnitKey = JunctionUnit.getKey(chrID, junctionStartmin, junctionEndmax);
		if (mapJunUnitKey2Unit.containsKey(juncUnitKey)) {
			mapJunUnitKey2Unit.get(juncUnitKey).addReadsNum(condition, junctionNum);
		} else {
			JunctionUnit juncUnit = new JunctionUnit(chrID, junctionStartmin, junctionEndmax);
			JunctionInfo juncInfo = new JunctionInfo(juncUnit);
			ListBin<JunctionInfo> lsJunctionInfos = mapChrID2ListGff.get(chrID);
			lsJunctionInfos.add(juncInfo);
			mapJunUnitKey2Unit.put(juncUnit.key(), juncUnit);
			mapJunSite2JunUnit.put(chrID + SepSign.SEP_ID + juncUnit.getStartAbs(), juncUnit);
			mapJunSite2JunUnit.put(chrID + SepSign.SEP_ID + juncUnit.getEndAbs(), juncUnit);
		}
	}
	
	/**
	 * 给定坐标和位点，找出全体条件下locsite,以及总共有多少reads支持
	 * 0表示没有junction
	 * @param chrID
	 * @param locSite
	 * @return
	 */
	public int getJunctionSite(String chrID, int locSite) {
		int num = 0;
		List<JunctionUnit> lsJunctionUnits = mapJunSite2JunUnit.get(chrID + SepSign.SEP_ID + locSite);
		for (JunctionUnit junctionUnit : lsJunctionUnits) {
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
	public int getJunctionSite(String chrID, int locStart, int locEnd) {
		int start = Math.min(locStart, locEnd), end = Math.max(locStart, locEnd);
		JunctionUnit junctionUnit = mapJunUnitKey2Unit.get(JunctionUnit.getKey(chrID, start, end));
		if (junctionUnit == null) {
			return 0;
		} else {
			return junctionUnit.getReadsNumAll();
		}
	}
	/**
	 * 给定坐标和位点，找出locsite,以及总共有多少reads支持
	 * 0表示没有junction
	 * @param chrID
	 * @param locSite
	 * @return
	 */
	public int getJunctionSite(String condition, String chrID, int locSite) {
		int num = 0;
		List<JunctionUnit> lsJunctionUnits = mapJunSite2JunUnit.get(chrID + SepSign.SEP_ID + locSite);
		for (JunctionUnit junctionUnit : lsJunctionUnits) {
			num += junctionUnit.getReadsNum(condition);
		}
		return num;
	}
	/**
	 * 给定坐标和位点，找出locsite
	 * @param chrID
	 * @param locStartSite 无所谓前后，内部自动判断
	 * @param locEndSite
	 * @return
	 */
	public int getJunctionSite(String condition, String chrID, int locStartSite, int locEndSite) {
		int start = Math.min(locStartSite, locEndSite), end = Math.max(locStartSite, locEndSite);
		JunctionUnit junctionUnit = mapJunUnitKey2Unit.get(JunctionUnit.getKey(chrID, start, end));
		if (junctionUnit == null) {
			return 0;
		} else {
			return junctionUnit.getReadsNum(condition);
		}
	}

	@Override
	public void summary() {
	}
	
	/** 读取完bam文件后必须调用该方法进行总结 */
	public void conclusion() {
		for (Entry<String, ListBin<JunctionInfo>> entry : mapChrID2ListGff.entrySet()) {
			String chrID = entry.getKey();
			ListBin<JunctionInfo> listGff = entry.getValue();
			ListBin<JunctionInfo> listGffNew = combineOverlapGene(listGff);
			mapChrID2ListGff.put(chrID, listGffNew);
			listGff = null;
		}
		
		try {
			setItemDistance();
			setOther();
			getMapName2DetailNum();
			getMapName2Detail();
		} catch (Exception e) {
			e.printStackTrace();
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
}
