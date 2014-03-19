package com.novelbio.analysis.seq.mirna;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.AlignRecord;
import com.novelbio.analysis.seq.AlignSeq;
import com.novelbio.analysis.seq.GeneExpTable;
import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqFastaHash;
import com.novelbio.analysis.seq.genome.gffOperate.MiRNAList;
import com.novelbio.analysis.seq.genome.gffOperate.MirMature;
import com.novelbio.analysis.seq.genome.gffOperate.MirPre;
import com.novelbio.analysis.seq.sam.SamMapRate;
import com.novelbio.base.multithread.RunProcess;
import com.novelbio.database.model.species.Species;
import com.novelbio.generalConf.TitleFormatNBC;

/**
 * 计算每个miRNA的表达，无法获得总表达值，只能获得每个表达值
 * @author zong0jie
 *
 */
public class MiRNACount extends RunProcess<MiRNACount.MiRNAcountProcess>{
	private static final Logger logger = Logger.getLogger(MiRNACount.class);
	static String flag_MapTo_PreMirna_NotTo_MatureMirna_Suffix = "_pre";

	/** 获得miRNA定位信息 */
	MiRNAList listMiRNALocation;
	/** miRNA前体 */
	SeqFastaHash seqFastaHashPreMiRNA = null;
	/** miRNA成熟体 */
	SeqFastaHash seqFastaHashMatureMiRNA = null;
	/** 比对的bed文件 */
	AlignSeq alignSeqMiRNA = null;
	/** Mapping至前体但是没到成熟体的序列的后缀 */
	
	/**
	 * 成熟体, 用于结果中<br>
	 * key: Pre_Mature
	 */
	Map<String, Double> mapMirMature2Value;
	/** 前体 */
	Map<String, Double> mapMiRNApre2Value;

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
	
	SamMapRate samMapRate;
	/**
	 * 设定miRNA的前体序列和成熟序列
	 * @param hairpairMirna
	 * @param matureMirna
	 */
	public void setMiRNAfile(String hairpairMirna, String matureMirna) {
		seqFastaHashPreMiRNA = new SeqFastaHash(hairpairMirna);
		seqFastaHashMatureMiRNA = new SeqFastaHash(matureMirna);
	}
	/** 用来统计注释上的novel mirna数量的 */
	public void setSamMapRate(SamMapRate samMapRate) {
		this.samMapRate = samMapRate;
	}
	/**
	 * 通过设定物种来设定
	 * {@link #setListMiRNALocation(ListMiRNAInt)}
	 * {@link #setMiRNAfile(String, String)}
	 * 这两个方法
	 * @param species
	 * @param rnadatFile
	 */
	public void setSpecies(Species species, String rnadatFile) {
		ListMiRNAdat listMiRNAdate = new ListMiRNAdat();
		listMiRNAdate.setSpecies(species);
		listMiRNAdate.ReadGffarray(rnadatFile);
		listMiRNALocation = listMiRNAdate;
		if (species.getMiRNAmatureFile() == null) {
			String msg = "no miRNA file exist in species:" + species.getCommonName() + " " + species.getNameLatin();
			throw new RuntimeException(msg.trim());
		}
		setMiRNAfile(species.getMiRNAhairpinFile(), species.getMiRNAmatureFile());
	}
	
	/** 设定自定义ListMiRNALocation */
	public void setListMiRNALocation(MiRNAList listMiRNALocation) {
		this.listMiRNALocation = listMiRNALocation;
	}
	public List<String> getLsMirNameMature() {
		List<String> lsMirName = new ArrayList<>();
		for (MirMature listDetailBin : listMiRNALocation.getGffDetailAll()) {
			lsMirName.add(listDetailBin.getNameSingle());
		}
		return lsMirName;
	}
	public Map<String, String> getMapPre2Seq() {
		Map<String, String> mapPre2Seq = new HashMap<>();
		for (MirMature lsMiRNA : listMiRNALocation.getGffDetailAll()) {
			String parentName = lsMiRNA.getParent().getName();
			mapPre2Seq.put(parentName, getMiRNApreSeq(parentName));
		}
		return mapPre2Seq;
	}
	
	public Map<String, String> getMapMature2Seq() {
		Map<String, String> mapMature2Seq = new HashMap<>();
		for (MirMature lsMiRNA : listMiRNALocation.getGffDetailAll()) {
			String matureName = lsMiRNA.getNameSingle();
			String parentName = lsMiRNA.getParent().getName();
			mapMature2Seq.put(matureName, getMiRNAmatureSeq(parentName, matureName));
		}
		return mapMature2Seq;
	}
	public List<String> getLsMirNamePre() {
		Set<String> setMirName = new HashSet<>();
		for (MirMature listDetailBin : listMiRNALocation.getGffDetailAll()) {
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
		for (MirMature lsMiRNA : listMiRNALocation.getGffDetailAll()) {
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
	 * 给定miRNA成熟体名字，从前体中获得序列
	 * @param mirID miRNA前体名字
	 * @param matureID miRNA成熟体名字
	 * @return
	 */
	private String getMiRNAmatureSeq(String mirID, String matureID) {
		if (seqFastaHashMatureMiRNA.getSeqFasta(matureID) != null) {
			return seqFastaHashMatureMiRNA.getSeqFasta(matureID).toString();
		}
		MirMature mirMature = listMiRNALocation.searchLOC(matureID);
		if (mirMature == null) {
			MirPre mirPre = listMiRNALocation.getMapChrID2LsGff().get(matureID);
			if (mirPre != null) {
				mirMature = mirPre.get(0);
			} else {
				if (!matureID.endsWith(flag_MapTo_PreMirna_NotTo_MatureMirna_Suffix)) {
					logger.error("出现未知miRNA的成熟体：" + mirID + " "  + matureID);
				}
				return null;
			}
		}
		SeqFasta seqFasta = seqFastaHashPreMiRNA.getSeq(mirID.split(sepMirPreID)[0].toLowerCase(), mirMature.getStartAbs(), mirMature.getEndAbs());
		if (mirMature.getStartAbs() > 40) {
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
		summary();
	}
	/** 一行一行处理
	 * 并填充hashmap
	 *  */
	private void copeRecordAndFillMap(AlignRecord alignRecord) {
		double value = (double)1/alignRecord.getMappedReadsWeight();
		addMiRNACountPre(alignRecord.getRefID(), value);
		
		String subName = listMiRNALocation.searchMirName(alignRecord.getRefID(), alignRecord.getStartAbs(), alignRecord.getEndAbs());
		//找不到名字的就不写如miRNA成熟体列表
		if (subName != null) {
			addMiRNACountMature(subName, value);
		}
		if (samMapRate != null) {
			samMapRate.addMapInfoNovelMiRNA(MiRNAnovelAnnotaion.getSepSymbol(), subName, alignRecord.getMappedReadsWeight());
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
	private void addMiRNACountMature(String miRmatureName, double thisMiRNAcount) {
		if (mapMirMature2Value.containsKey(miRmatureName)) {
			double tmpValue = mapMirMature2Value.get(miRmatureName);
			mapMirMature2Value.put(miRmatureName, thisMiRNAcount + tmpValue);
		} else {
			mapMirMature2Value.put(miRmatureName, thisMiRNAcount);
		}
	}
	
	private void summary() {
		countsMatureAll = 0;
		countsPreAll = 0;
		for (String mirMatureName : mapMirMature2Value.keySet()) {
			Double counts = mapMirMature2Value.get(mirMatureName);
			if (counts != null) {
				countsMatureAll += counts;
			}
		}
		for (String mirPreName : mapMiRNApre2Value.keySet()) {
			Double counts = mapMiRNApre2Value.get(mirPreName);
			if (counts != null) {
				countsPreAll += counts;
			}
		}
	}
		
	/**
	 * @return
	 * key: mirMatureName
	 */
	public Map<String, Double> getMapMirMature2Value() {
		return mapMirMature2Value;
	}
	public Map<String, Double> getMapMiRNApre2Value() {
		return mapMiRNApre2Value;
	}
	/** allreads
	 * @return
	 */
	public double getCountPreAll() {
		return countsPreAll;
	}
	/** allreads
	 * @return
	 */
	public double getCountMatureAll() {
		return countsMatureAll;
	}
	
	/**
	 * 设定Title, LsGeneName, Annotation
	 * @param expMirPre
	 * @param expMirMature
	 */
	public void setExpTable(GeneExpTable expMirPre, GeneExpTable expMirMature) {
		expMirPre.addLsGeneName(getLsMirNamePre());
		expMirPre.addAnnotation(getMapPre2Seq());
		expMirPre.addLsTitle(MiRNACount.getLsTitleAnnoPre());
		
		expMirMature.addLsGeneName(getLsMirNameMature());
		expMirMature.addAnnotation(getMapMature2Pre());
		expMirMature.addAnnotation(getMapMature2Seq());
		expMirMature.addLsTitle(MiRNACount.getLsTitleAnnoMature());
	}
	/** 设定Title, Annotation */
	public void setExpTableWithoutLsGeneName(GeneExpTable expMirPre, GeneExpTable expMirMature) {
		expMirPre.addAnnotation(getMapPre2Seq());
		expMirPre.addLsTitle(MiRNACount.getLsTitleAnnoPre());
		
		expMirMature.addAnnotation(getMapMature2Pre());
		expMirMature.addAnnotation(getMapMature2Seq());
		expMirMature.addLsTitle(MiRNACount.getLsTitleAnnoMature());
	}
	/** MiRNACount的输出，给GUI使用 */
	public static class MiRNAcountProcess {
		long readsNum;
		public void setReadsNum(long readsNum) {
			this.readsNum = readsNum;
		}
		public long getReadsNum() {
			return readsNum;
		}
	}
	
	/** mirPre的anno标题 */
	public static List<String> getLsTitleAnnoPre() {
		List<String> lsTitleAnno = new ArrayList<>();
		lsTitleAnno.add(TitleFormatNBC.mirPreSequence.toString());
		return lsTitleAnno;
	}
	/** mirMature的anno标题<br>
	 * 0: mirPreName<br>
	 * 1: mirSeq<br>
	 */
	public static List<String> getLsTitleAnnoMature() {
		List<String> lsTitleAnno = new ArrayList<>();
		lsTitleAnno.add(TitleFormatNBC.miRNApreName.toString());
		lsTitleAnno.add(TitleFormatNBC.mirSequence.toString());
		return lsTitleAnno;
	}
}
