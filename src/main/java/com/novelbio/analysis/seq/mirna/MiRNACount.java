package com.novelbio.analysis.seq.mirna;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.analysis.seq.AlignRecord;
import com.novelbio.analysis.seq.AlignSeq;
import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqFastaHash;
import com.novelbio.analysis.seq.genome.gffOperate.ListDetailBin;
import com.novelbio.analysis.seq.rnaseq.RPKMcomput;
import com.novelbio.analysis.seq.rnaseq.RPKMcomput.EnumExpression;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.base.SepSign;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.dataStructure.listOperate.ListBin;
import com.novelbio.base.multithread.RunProcess;
import com.novelbio.database.model.species.Species;
import com.novelbio.generalConf.PathDetailNBC;
import com.novelbio.generalConf.TitleFormatNBC;

/**
 * 计算每个miRNA的表达，无法获得总表达值，只能获得每个表达值
 * @author zong0jie
 *
 */
public class MiRNACount extends RunProcess<MiRNACount.MiRNAcountProcess>{
	private static final Logger logger = Logger.getLogger(MiRNACount.class);
	
	/** 获得miRNA定位信息 */
	ListMiRNAInt listMiRNALocation;
	/** miRNA前体 */
	SeqFastaHash seqFastaHashPreMiRNA = null;
	/** miRNA成熟体 */
	SeqFastaHash seqFastaHashMatureMiRNA = null;
	/** 比对的bed文件 */
	AlignSeq alignSeqMiRNA = null;
	/** Mapping至前体但是没到成熟体的序列的后缀 */
	String flag_MapTo_PreMirna_NotTo_MatureMirna_Suffix = "_pre";

	/**
	 * 成熟体, 用于结果中<br>
	 * key: Pre_Mature
	 */
	Map<String, Double> mapMirMature2Value;
	/** 前体 */
	Map<String, Double> mapMiRNApre2Value;
	Map<String, String> mapMirPre2Seq = new HashMap<>();
	Map<String, String> mapMirMature2Seq = new HashMap<>();
	
	/** double[2] 0: allReadsNum 1: upQuartile的reads number */
	double countsPreAll = 0;
	/** double[2] 0: allReadsNum 1: upQuartile的reads number<br> */
	double countsMatureAll = 0;
		
	/**
	 * 如果出现相同的mirMature Name，但是mirPre Name不同的
	 * 用该ID来分隔两个mirPre Name<p>
	 * 如hsa-miR-548aj-3p有两个前体，hsa-mir-548aj-1/hsa-mir-548aj-2
	 */
	String sepMirPreID = "/";
	
	/**
	 * 设定miRNA的前体序列和成熟序列
	 * @param hairpairMirna
	 * @param matureMirna
	 */
	public void setMiRNAfile(String hairpairMirna, String matureMirna) {
		seqFastaHashMatureMiRNA = new SeqFastaHash(matureMirna);
		seqFastaHashPreMiRNA = new SeqFastaHash(hairpairMirna);
	}
	
	/** 设定自定义ListMiRNALocation */
	public void setListMiRNALocation(ListMiRNAInt listMiRNALocation) {
		this.listMiRNALocation = listMiRNALocation;
	}
	public List<String> getLsMirNameMature() {
		List<String> lsMirName = new ArrayList<>();
		for (ListDetailBin listDetailBin : listMiRNALocation.getGffDetailAll()) {
			lsMirName.add(listDetailBin.getNameSingle());
		}
		return lsMirName;
	}
	public Map<String, String> getMapPre2Seq() {
		Map<String, String> mapPre2Seq = new HashMap<>();
		for (ListDetailBin lsMiRNA : listMiRNALocation.getGffDetailAll()) {
			String parentName = lsMiRNA.getParent().getName();
			mapPre2Seq.put(parentName, getMiRNApreSeq(parentName));
		}
		return mapPre2Seq;
	}
	
	public Map<String, String> getMapMature2Seq() {
		Map<String, String> mapMature2Seq = new HashMap<>();
		for (ListDetailBin lsMiRNA : listMiRNALocation.getGffDetailAll()) {
			String matureName = lsMiRNA.getNameSingle();
			String parentName = lsMiRNA.getParent().getName();
			mapMature2Seq.put(matureName, getMiRNAmatureSeq(matureName, parentName));
		}
		return mapMature2Seq;
	}
	public List<String> getLsMirNamePre() {
		Set<String> setMirName = new HashSet<>();
		for (ListDetailBin listDetailBin : listMiRNALocation.getGffDetailAll()) {
			setMirName.add(listDetailBin.getParent().getName());
		}
		return new ArrayList<>(setMirName);
	}
	/**
	 * 成熟体名称到前体名称的对照表
	 * 	//如果出现了两个相同的matureRNA，但是它们的前体名字不一样
			//则格式调整为 mir-163-5p   mir-163-1/mir-163-2
	 * @return
	 */
	public Map<String, String> getMapMature2Pre() {
		Map<String, String> mapMature2Pre = new HashMap<>();
		for (ListDetailBin lsMiRNA : listMiRNALocation.getGffDetailAll()) {
			String matureName = lsMiRNA.getNameSingle();
			String parentName = lsMiRNA.getParent().getName();
			if (mapMature2Pre.containsKey(matureName)) {
				String parentNameOld = mapMature2Pre.get(matureName);
				boolean addParentName = true;
				for (String name : parentNameOld.split(sepMirPreID)) {
					if (name.equals(parentName)) {
						addParentName = false;
						break;
					}
				}
				if (addParentName) {
					parentName = parentNameOld + sepMirPreID + parentName;
				} else {
					parentName = parentNameOld;
				}
			}
			mapMature2Pre.put(matureName, parentName);
		}
		return mapMature2Pre;
	}

	/**
	 * 输出文件前缀，就是miRNA的计数
	 * @param outFilePrefix
	 */
	public void writeResultToOut(String outFilePrefix) {
		String outMirValue = outFilePrefix + "MirValue";
		String outMirMatureValue = outFilePrefix + "MirMatureValue";
		
		System.out.println(outMirValue);
		System.out.println(outMirValue);
		
		TxtReadandWrite txtMirValue = new TxtReadandWrite(outMirValue, true);
		TxtReadandWrite txtMirMatureValue = new TxtReadandWrite(outMirMatureValue, true);
		for (Entry<String, Double> miRNApre2Value : mapMiRNApre2Value.entrySet()) {
			txtMirValue.writefileln(miRNApre2Value.getKey() + "\t" + miRNApre2Value.getValue().intValue() + "\t" + seqFastaHashPreMiRNA.getSeqFasta(miRNApre2Value.getKey().split(sepMirPreID)[0] ));
		}
		for (String miRNAName : mapMirPre_Mature2Value.keySet()) {
			String[] ss = miRNAName.split(SepSign.SEP_ID);
			String miRNApre = ss[0], miRNAmature = ss[1];
			Double value = mapMirPre_Mature2Value.get(miRNAName);
		
			String mirMatureSeq = getMiRNAmatureSeq(miRNApre, miRNAmature);
			if (value == null || mirMatureSeq == null) {
				continue;
			}
			txtMirMatureValue.writefileln(miRNAmature + "\t" + miRNApre + "\t" + mirMatureSeq + "\t" + value.intValue());
		}
		txtMirValue.close();
		txtMirMatureValue.close();
	}
	
	/** 初始化表<p>
	 * 其中mapMirMatureName2Pre表<br>：
	 * 如果出现了两个相同的matureRNA，但是它们的前体名字不一样<br>
	 * 则格式调整为 mir-163-5p  --> mir-163-1/mir-163-2
	 */
	private void initialMap() {
		mapMiRNApre2Value = new LinkedHashMap<String, Double>();
		mapMirMature2Value = new LinkedHashMap<String, Double>();
	}
	/**
	 * 给定miRNA成熟体名字，从前体中获得序列
	 * @param mirID miRNA前体名字
	 * @param matureID miRNA成熟体名字
	 * @return
	 */
	private String getMiRNAmatureSeq(String mirID, String matureID) {
		if (seqFastaHashMatureMiRNA.getSeqFasta(matureID) != null) {
			return seqFastaHashMatureMiRNA.getSeqFasta(matureID).toString();
		}
		ListDetailBin listDetailBin = listMiRNALocation.searchLOC(matureID);
		if (listDetailBin == null) {
			ListBin<ListDetailBin > lsInfo = listMiRNALocation.getMapChrID2LsGff().get(matureID);
			if (lsInfo != null) {
				listDetailBin = lsInfo.get(0);
			} else {
				if (!matureID.endsWith(flag_MapTo_PreMirna_NotTo_MatureMirna_Suffix)) {
					logger.error("出现未知miRNA的成熟体：" + mirID + " "  + matureID);
				}
				return null;
			}
		}
		SeqFasta seqFasta = seqFastaHashPreMiRNA.getSeq(mirID.split(sepMirPreID)[0].toLowerCase(), listDetailBin.getStartAbs(), listDetailBin.getEndAbs());
		if (listDetailBin.getStartAbs() > 40) {
			return seqFasta.reservecom().toString();
		}
		return seqFasta.toString();
	}
	/**
	 * 给定miRNA成熟体名字，从前体中获得序列
	 * @param mirID miRNA前体名字
	 * @param matureID miRNA成熟体名字
	 * @return
	 */
	private String getMiRNApreSeq(String matureID) {
		return seqFastaHashPreMiRNA.getSeq(matureID).toString();
	}
	/** 设定需要计算表达值的bed文件 */
	public void setAlignFile(AlignSeq alignSeq) {
		alignSeqMiRNA =alignSeq;
	}
	
	@Override
	protected void running() {
		initialMap();
		countMiRNA();
	}
	

	
	/**
	 * 无所谓排不排序
	 *读取bed文件，然后在mirDat中查找信息，并确定数量
	 * @param outTxt
	 */
	private void countMiRNA() {
		int countLoop = 0;
		for (AlignRecord alignRecord : alignSeqMiRNA.readLines()) {
			if (!alignRecord.isMapped()) {
				continue;
			}
			copeRecordAndFillMap(alignRecord);
			
			suspendCheck();
			if (flagStop) break;
			countLoop++;
//			if (countLoop % 1000 == 0) {
//				MiRNAcountProcess miRNAcountProcess = new MiRNAcountProcess();
//				miRNAcountProcess.setReadsNum(countLoop);
//				if (runGetInfo != null) {
//					runGetInfo.setRunningInfo(miRNAcountProcess);
//				}
//			}
		}
		setMapMirPre2Mature_Value();
		summaryMature();
		summaryPre();
	}
	/** 一行一行处理
	 * 并填充hashmap
	 *  */
	private void copeRecordAndFillMap(AlignRecord alignRecord) {
		if (alignRecord.getRefID() == null) {
			System.out.println();
		}
		double value = (double)1/alignRecord.getMappedReadsWeight();
		addMiRNACountPre(alignRecord.getRefID(), value);
		
		String subName = listMiRNALocation.searchMirName(alignRecord.getRefID(), alignRecord.getStartAbs(), alignRecord.getEndAbs());
		//找不到名字的就不写如miRNA成熟体列表
		if (subName != null) {
			addMiRNACountPre2Mature(alignRecord.getRefID(), subName, value);
		}
	}
	/**
	 * 给定miRNA的名字，和值，累加起来
	 * @param miRNAname
	 * @param thisMiRNAcount 本次需要累计的miRNAcount，因为一条reads可能mapping至多个miRNA，那么每个miRNA的数量即为1/count
	 */
	private void addMiRNACountPre(String miRNAname, double thisMiRNAcount) {
		if (mapMiRNApre2Value.containsKey(miRNAname)) {
			double tmpValue = mapMiRNApre2Value.get(miRNAname);
			mapMiRNApre2Value.put(miRNAname, thisMiRNAcount + tmpValue);
		} else {
			mapMiRNApre2Value.put(miRNAname, thisMiRNAcount);
		}
	}
	/**
	 * 给定miRNA的名字，和值，累加起来
	 * @param miRNAname miRNA的名字
	 * @param miRNADetailname miRNA成熟体的名字
	 * @param thisMiRNAcount 本次需要累计的miRNAcount，因为一条reads可能mapping至多个miRNA，那么每个miRNA的数量即为1/count
	 */
	private void addMiRNACountPre2Mature(String miRNAname,String miRNADetailname, double thisMiRNAcount) {
		if (mapMirPre2LsMature_Value.containsKey(miRNAname)) {
			//获得具体成熟miRNA的信息
			List<String[]> lsTmpResult = mapMirPre2LsMature_Value.get(miRNAname);
			for (String[] strings : lsTmpResult) {
				if (strings[0].equals(miRNADetailname)) {
					//累加表达数值，加完就跳出
					strings[1] = (Double.parseDouble(strings[1]) + thisMiRNAcount) + "";
					return;
				}
			}
			//如果没有跳出说明是第一次找到该miRNA
			lsTmpResult.add(new String[]{miRNADetailname, thisMiRNAcount+""});
		} else {
			mapMirPre2LsMature_Value.put(miRNAname, new String[]{miRNADetailname, thisMiRNAcount + ""});
		}
	}
	
	/** 用mapMirPre2LsMature_Value的信息填充最后的mirMature表 */
	private void setMapMirPre2Mature_Value() {
		//初始化一个包含全体miRNAmature名字的map
		Map<String, Double> mapMirMature2Value = new HashMap<>();
		for (ListDetailBin miRNAMature : listMiRNALocation.getGffDetailAll()) {
			mapMirMature2Value.put(miRNAMature.getNameSingle(), 0.0);
		}
		//往里面装具体的表达值，如果有mirMature名字一样，不管前体名是否一样都加和在一起
		for (String mirPreName : mapMirPre2LsMature_Value.keySet()) {
			List<String[]> lsMirMature2Value = mapMirPre2LsMature_Value.get(mirPreName);
			for (String[] mirMature2Value : lsMirMature2Value) {
				if (getMiRNAmatureSeq(mirPreName, mirMature2Value[0]) == null) {
					continue;
				}
				double countNum = Double.parseDouble(mirMature2Value[1]);
				if (mapMirMature2Value.containsKey(mirMature2Value[0])) {
					double valueOld = mapMirMature2Value.get(mirMature2Value[0]);
					countNum += valueOld;
				}
				mapMirMature2Value.put(mirMature2Value[0], countNum);
			}
		}
		//最后装入map中
		for (String mirMatureName : mapMirMature2Value.keySet()) {
			mapMirPre_Mature2Value.put(mapMirMatureName2Pre.get(mirMatureName) + SepSign.SEP_ID + mirMatureName, mapMirMature2Value.get(mirMatureName));
		}
	}
	
	private void summaryMature() {
		List<Double> lsReadsInfo = new ArrayList<Double>();
		for (String geneName : mapMirPre_Mature2Value.keySet()) {
			Double counts = mapMirPre_Mature2Value.get(geneName);
			if (counts != null) {
				CountsNumMature[0] += counts;
				lsReadsInfo.add(counts);
			}
		}
		CountsNumMature[1] = MathComput.median(lsReadsInfo, 75);
	}
	private void summaryPre() {
		List<Double> lsReadsInfo = new ArrayList<Double>();
		for (String geneName : mapMiRNApre2Value.keySet()) {
			Double counts = mapMiRNApre2Value.get(geneName);
			if (counts != null) {
				CountsNumPre[0] += counts;
				lsReadsInfo.add(counts);
			}
		}
		CountsNumPre[1] = MathComput.median(lsReadsInfo, 75);
	}
	
	/**
	 * @return
	 * key: mirPreName + {@link SepSign#SEP_ID} + mirMatureName
	 */
	public Map<String, Double> getMapMirMature2Value() {
		return mapMirPre_Mature2Value;
	}
	public Map<String, Double> getMapMiRNApre2Value() {
		return mapMiRNApre2Value;
	}
	/** 0: allreads
	 * 1: UQreads
	 * @return
	 */
	public double[] getCountPre() {
		return CountsNumPre;
	}
	/** 0: allreads
	 * 1: UQreads
	 * @return
	 */
	public double[] getCountMature() {
		return CountsNumMature;
	}
	
	/** 将给定的几组miRNA的值合并起来 */
	public ArrayList<String[]> combMapMir2ValueCounts(Map<String, Map<String, Double>> mapPrefix2_mapMiRNA2Value, 
			Map<String, double[]> mapPrefix2CountsPre) {
		CombMapMirPre2Value combMapMirPre2Value = new CombMapMirPre2Value(seqFastaHashPreMiRNA, sepMirPreID, EnumExpression.Counts);
		combMapMirPre2Value.setMapPrefix2Counts(mapPrefix2CountsPre);
		return combMapMirPre2Value.combValue(mapPrefix2_mapMiRNA2Value);
	}
	/** 将给定的几组miRNA的值合并起来 */
	public ArrayList<String[]> combMapMir2MatureValueCounts(Map<String, Map<String, Double>> mapPrefix2_mapMiRNAMature2Value,
			Map<String, double[]> mapPrefix2CountsMature) {
		CombMapMirMature2Value combMapMirMature2Value = new CombMapMirMature2Value(this, EnumExpression.Counts);
		combMapMirMature2Value.setMapPrefix2Counts(mapPrefix2CountsMature);
		return combMapMirMature2Value.combValue(mapPrefix2_mapMiRNAMature2Value);
	}
	/** 将给定的几组miRNA的值合并起来 */
	public ArrayList<String[]> combMapMir2ValueUQPM(Map<String, Map<String, Double>> mapPrefix2_mapMiRNA2Value,
			Map<String, double[]> mapPrefix2CountsPre) {
		CombMapMirPre2Value combMapMirPre2Value = new CombMapMirPre2Value(seqFastaHashPreMiRNA, sepMirPreID, EnumExpression.UQPM);
		combMapMirPre2Value.setMapPrefix2Counts(mapPrefix2CountsPre);
		return combMapMirPre2Value.combValue(mapPrefix2_mapMiRNA2Value);
	}
	/** 将给定的几组miRNA的值合并起来 */
	public ArrayList<String[]> combMapMir2MatureValueUQPM(Map<String, Map<String, Double>> mapPrefix2_mapMiRNAMature2Value,
			Map<String, double[]> mapPrefix2CountsMature) {
		CombMapMirMature2Value combMapMirMature2Value = new CombMapMirMature2Value(this, EnumExpression.UQPM);
		combMapMirMature2Value.setMapPrefix2Counts(mapPrefix2CountsMature);
		return combMapMirMature2Value.combValue(mapPrefix2_mapMiRNAMature2Value);
	}
	public static class MiRNAcountProcess {
		long readsNum;
		public void setReadsNum(long readsNum) {
			this.readsNum = readsNum;
		}
		public long getReadsNum() {
			return readsNum;
		}
	}
}

class CombMapMirPre2Value extends MirCombMapGetValueAbs {
	SeqFastaHash seqFastaHashPreMiRNA;
	String sepID;
	Map<String, double[]> mapPrefix2Counts;
	EnumExpression enumExpression;
	/**
	 * @param miRNACount
	 * @param sepID 用于分割多个连在一起的miRNApreName的分隔符
	 */
	CombMapMirPre2Value(SeqFastaHash seqFastaHashPreMiRNA, String sepID, EnumExpression enumExpression) {
		this.seqFastaHashPreMiRNA = seqFastaHashPreMiRNA;
		this.sepID = sepID;
		this.enumExpression = enumExpression;
	}
	public void setMapPrefix2Counts(Map<String, double[]> mapPrefix2Counts) {
		this.mapPrefix2Counts = mapPrefix2Counts;
	}
	@Override
	protected String[] getTitleIDAndInfo() {	/** 返回涉及到的所有miRNA的名字 */
		String[] titleStart = new String[2];
		titleStart[0] = TitleFormatNBC.miRNApreName.toString();
		titleStart[1] = TitleFormatNBC.mirPreSequence.toString();
		return titleStart;
	}

	@Override
	protected void fillMataInfo(String id, ArrayList<String> lsTmpResult) {
		lsTmpResult.add(id);
		lsTmpResult.add(seqFastaHashPreMiRNA.getSeqFasta(id.split(sepID)[0]).toString());
	}

	@Override
	protected Number getExpValue(String condition, Double readsCount) {
		if (enumExpression == enumExpression.Counts) {
			return readsCount.intValue();
		} else {
			double[] count = mapPrefix2Counts.get(condition);
			double value = RPKMcomput.getValue(enumExpression, readsCount, count[0], count[1], 0);
			return value;
		}
	}
}

class CombMapMirMature2Value extends MirCombMapGetValueAbs {
	MiRNACount miRNACount;
	Map<String, double[]> mapPrefix2Counts;
	EnumExpression enumExpression;
	CombMapMirMature2Value(MiRNACount miRNACount, EnumExpression enumExpression) {
		this.miRNACount = miRNACount;
		this.enumExpression = enumExpression;
	}
	public void setMapPrefix2Counts(Map<String, double[]> mapPrefix2Counts) {
		this.mapPrefix2Counts = mapPrefix2Counts;
	}
	
	@Override
	protected String[] getTitleIDAndInfo() {	/** 返回涉及到的所有miRNA的名字 */
		String[] titleStart = new String[3];
		titleStart[0] = TitleFormatNBC.miRNAName.toString();
		titleStart[1] = TitleFormatNBC.miRNApreName.toString();
		titleStart[2] = TitleFormatNBC.mirSequence.toString();
		return titleStart;
	}

	@Override
	protected void fillMataInfo(String id, ArrayList<String> lsTmpResult) {
		String[] seqName = id.split(SepSign.SEP_ID);
		lsTmpResult.add(seqName[1]);//先放mature miRNA
		lsTmpResult.add(seqName[0]);//再放pre miRNA
		lsTmpResult.add(miRNACount.getMiRNAmatureSeq(seqName[0], seqName[1]));
	}
	@Override
	protected Number getExpValue(String condition, Double readsCount) {
		if (enumExpression == enumExpression.Counts) {
			return readsCount.intValue();
		} else {
			double[] count = mapPrefix2Counts.get(condition);
			double value = RPKMcomput.getValue(enumExpression, readsCount, count[0], count[1], 0);
			return value;
		}
	}
}
