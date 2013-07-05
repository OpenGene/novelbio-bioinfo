package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.novelbio.analysis.seq.AlignRecord;
import com.novelbio.analysis.seq.genome.gffOperate.GffCodGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffCodGeneDU;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailCG;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGeneAbs;
import com.novelbio.analysis.seq.genome.gffOperate.ListGff;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.analysis.seq.sam.AlignmentRecorder;
import com.novelbio.base.SepSign;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.listOperate.ListAbsSearch;
import com.novelbio.base.dataStructure.listOperate.ListBin;
import com.novelbio.base.dataStructure.listOperate.ListCodAbs;
import com.novelbio.base.dataStructure.listOperate.ListCodAbsDu;
import com.novelbio.base.dataStructure.listOperate.ListHashSearch;
import com.novelbio.base.multithread.RunProcess;

public class TophatJunctionNew extends ListHashSearch<JunctionInfo, ListCodAbs<JunctionInfo>, 
ListCodAbsDu<JunctionInfo, ListCodAbs<JunctionInfo>>, ListBin<JunctionInfo>> implements AlignmentRecorder {
	ArrayListMultimap<String, String> mapCond2JuncFile = ArrayListMultimap.create();
	Map<String, JunctionUnit> mapJunUnitKey2Unit = new HashMap<String, JunctionUnit>();
	ArrayListMultimap<String, JunctionUnit> mapJunSite2JunUnit = ArrayListMultimap.create();
	String condition;
	
	public void setCondition(String condition) {
		this.condition = condition;
	}
	
	/**添加samBam的文件用来获得信息 */
	public void addAlignRecord(AlignRecord alignRecord) {
		ArrayList<Align> lsAlign = alignRecord.getAlignmentBlocks();
		if (lsAlign.size() <= 1) {
			return;
		}
		int size = lsAlign.size();
		String chrID = alignRecord.getRefID();
		for (int i = 0; i < size - 1; i++) {
			Align alignThis = lsAlign.get(i);
			Align alignNext = lsAlign.get(i + 1);
			int junStart = alignThis.getEndAbs();
			int junEnd = alignNext.getStartAbs();
			addJunctionInfo(chrID, junStart, junEnd, 1);
		}
	}

	/**
	 * 读取junction文件，文件中每个剪接位点只能出现一次\
	 * @param condition
	 * @param junctionFile
	 */
	public void setJunFile(String condition, String junctionFile) {
		mapCond2JuncFile.put(condition, junctionFile);
	}
	
	public void readJuncFile() {
		for (String condition : mapCond2JuncFile.keySet()) {
			List<String> lsFileName = mapCond2JuncFile.get(condition);
			for (String junctionFile : lsFileName) {
				readJuncFile(junctionFile);
			}
		}
	}
	
	/**
	 * 读取之前先设定{@link #setCondition(String)}
	 * 读取junction文件，文件中每个剪接位点只能出现一次\
	 * @param condition
	 * @param junctionFile
	 */
	private void readJuncFile(String junctionFile) {
		TxtReadandWrite txtReadandWrite = new TxtReadandWrite(junctionFile);
		for (String string : txtReadandWrite.readfileLs()) {
			if (string.startsWith("track")) {
				continue;
			}
			String[] ss = string.split("\t");
			String chrID = ss[0];
			
			//junction位点都设定在exon上
			int junct1 = Integer.parseInt(ss[1]) + Integer.parseInt(ss[10].split(",")[0]);
			int junct2 = Integer.parseInt(ss[2]) - Integer.parseInt(ss[10].split(",")[1]) + 1;
			int junctionNum = Integer.parseInt(ss[4]);
			addJunctionInfo(chrID, junct1, junct2, junctionNum);
		}
		txtReadandWrite.close();
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
			mapJunSite2JunUnit.put(chrID + SepSign.SEP_ID + juncUnit.getStartAbs(), juncUnit);
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
		setCondition(condition);
		String junc = chrID.toLowerCase() + SepSign.SEP_INFO_SAMEDB + locSite;
		Integer num = mapJuncOne2AllNum.get(junc);
		if (num == null) {
			num = 0;
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
		setCondition(condition);
		chrID = chrID.toLowerCase();
		int locS = Math.min(locStartSite, locEndSite);
		int locE = Math.max(locStartSite, locEndSite);
		String key = chrID + SepSign.SEP_INFO_SAMEDB + locS + SepSign.SEP_INFO +chrID + SepSign.SEP_INFO_SAMEDB + locE;
		int resultNum = 0;
		if (mapJuncPair2ReadsNum.containsKey(key)) {
			resultNum = mapJuncPair2ReadsNum.get(key);
		}
		return resultNum;
	}

	@Override
	public void summary() {
		//NOTHING TO DO
	}
	@Override
	protected void ReadGffarrayExcep(String gfffilename) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
