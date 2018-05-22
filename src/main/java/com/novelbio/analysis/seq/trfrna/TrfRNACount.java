package com.novelbio.analysis.seq.trfrna;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.AlignRecord;
import com.novelbio.analysis.seq.GeneExpTable;
import com.novelbio.analysis.seq.genome.gffoperate.trfrna.TrfMature;
import com.novelbio.analysis.seq.genome.gffoperate.trfrna.TrfRNAList;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.analysis.seq.sam.AlignmentRecorder;
import com.novelbio.analysis.seq.sam.SamRecord;
import com.novelbio.generalconf.TitleFormatNBC;

/**
 * 计算每个trfRNA的表达，无法获得总表达值，只能获得每个表达值
 * 这个是从miRNA那里复制来并仅做了简单修改。如果再遇到类似需求，就需要写一个通用模块
 * @author zong0jie
 *
 */
public class TrfRNACount implements AlignmentRecorder {
	private static final Logger logger = Logger.getLogger(TrfRNACount.class);
	static String flag_MapTo_PreMirna_NotTo_MatureMirna_Suffix = "_pre";

	/** 获得miRNA定位信息 */
	protected TrfRNAList trfRNAList;

	/** Mapping至前体但是没到成熟体的序列的后缀 */
	
	GeneExpTable trfMatureExp;
	GeneExpTable trfPreExp;
	
	/** 如果一条reads比对到多个位置，是
	 * 1. 每个位置都加上 1/n 
	 * 2. 把第一个位置加上1
	 * 默认是 1 
	 */
	boolean isReadMultiMappedReadsOnce = false;
	
	/** 如果一条reads比对到多个位置，是
	 * 1. 每个位置都加上 1/n 
	 * 2. 把第一个位置加上1
	 * 默认是 1 
	 */
	public void setReadMultiMappedReadsOnce(boolean isReadMultiMappedReadsOnce) {
		this.isReadMultiMappedReadsOnce = isReadMultiMappedReadsOnce;
	}
	/**
	 * 通过设定物种来设定
	 * {@link #setListMiRNALocation(ListMiRNAInt)}
	 * {@link #setMiRNAfile(String, String)}
	 * 这两个方法
	 * @param species
	 * @param rnadatFile
	 */
	public void setSpecies(String speciesName, String trfFile) {
		trfRNAList = new TrfRNAList();
		trfRNAList.setSpeciesName(speciesName);
		trfRNAList.ReadGffarray(trfFile);
	}
	
	private List<String> getLsTrfNameMature() {
		List<String> lsMirName = new ArrayList<>();
		for (TrfMature listDetailBin : trfRNAList.getGffDetailAll()) {
			lsMirName.add(listDetailBin.getNameSingle());
		}
		return lsMirName;
	}
	
	private Map<String, String> getMapPre2Seq() {
		Map<String, String> mapPre2Seq = new HashMap<>();
		for (TrfMature trfMature : trfRNAList.getGffDetailAll()) {
			String parentName = trfMature.getParent().getName();
			mapPre2Seq.put(parentName, trfMature.getParent().getSeq().toString());
		}
		return mapPre2Seq;
	}
	
	private Map<String, String> getMapMature2Seq() {
		Map<String, String> mapMature2Seq = new HashMap<>();
		for (TrfMature trfMature : trfRNAList.getGffDetailAll()) {
			String matureName = trfMature.getNameSingle();
			mapMature2Seq.put(matureName, trfMature.getSeq().toString());
		}
		return mapMature2Seq;
	}
	private List<String> getLsMirNamePre() {
		Set<String> setMirName = new HashSet<>();
		for (TrfMature trfMature : trfRNAList.getGffDetailAll()) {
			setMirName.add(trfMature.getParent().getName());
		}
		return new ArrayList<>(setMirName);
	}
	
	/** 一行一行处理
	 * 并填充hashmap
	 *  */
	public void addAlignRecord(AlignRecord alignRecord) {
		if (!alignRecord.isMapped()) return;
		
		SamRecord samRecord = (SamRecord)alignRecord;
		
		if (isReadMultiMappedReadsOnce && samRecord.getMappedReadsWeight() > 1 && samRecord.getMapIndexNum() != 1) {
			return;
		}
		
		double value = isReadMultiMappedReadsOnce? 1 : (double)1/samRecord.getMappedReadsWeight();
		
		trfPreExp.addGeneExp(samRecord.getRefID(), value);

		String subName = trfRNAList.searchMirNameMid(samRecord.getRefID(), samRecord.getStartAbs(), samRecord.getEndAbs());
		//找不到名字的就不写如miRNA成熟体列表
		if (subName != null) {
			trfMatureExp.addGeneExp(subName, value);
		}
	}
	
	public void summary() {
		trfPreExp.setAllreadsPerConditon();
		trfMatureExp.setAllreadsPerConditon();
	}
	
	/**
	 * 设定Title, LsGeneName, Annotation
	 * @param expTrfPre
	 * @param expTrfMature
	 */
	public void setExpTable(GeneExpTable expTrfPre, GeneExpTable expTrfMature) {
		expTrfPre.addLsGeneName(getLsMirNamePre());
		expTrfPre.addAnnotation(getMapPre2Seq());
		expTrfPre.addLsTitle(TrfRNACount.getLsTitleAnnoPre());
		
		expTrfMature.addLsGeneName(getLsTrfNameMature());
		expTrfMature.addAnnotation(getMapPre2Seq());
		expTrfMature.addAnnotation(getMapMature2Seq());
		expTrfMature.addLsTitle(TrfRNACount.getLsTitleAnnoMature());
		
		this.trfMatureExp = expTrfMature;
		this.trfPreExp = expTrfPre;
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
		lsTitleAnno.add(TitleFormatNBC.trfPreSequence.toString());
		return lsTitleAnno;
	}
	/** mirMature的anno标题<br>
	 * 0: mirPreName<br>
	 * 1: mirSeq<br>
	 */
	public static List<String> getLsTitleAnnoMature() {
		List<String> lsTitleAnno = new ArrayList<>();
		lsTitleAnno.add(TitleFormatNBC.trfRNApreName.toString());
		lsTitleAnno.add(TitleFormatNBC.trfSequence.toString());
		return lsTitleAnno;
	}
	@Override
	public Align getReadingRegion() {
		return null;
	}
}
