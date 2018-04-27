package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.analysis.seq.AlignRecord;
import com.novelbio.analysis.seq.GeneExpTable;
import com.novelbio.analysis.seq.GeneExpTable.EnumAddAnnoType;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffOperate.GffCodGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffCodGeneDU;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGeneAbs;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.analysis.seq.mapping.StrandSpecific;
import com.novelbio.analysis.seq.sam.AlignSamReading;
import com.novelbio.analysis.seq.sam.AlignmentRecorder;
import com.novelbio.analysis.seq.sam.SamRecord;
import com.novelbio.base.ExceptionNullParam;
import com.novelbio.base.SepSign;
import com.novelbio.base.StringOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.modgeneid.GeneType;
import com.novelbio.generalConf.TitleFormatNBC;

/**
 * 输入的必须是排过序的sam文件
 * 根据tophat或者其他mapping至genome的bam结果，计算基因的rpkm
 * 如果是链特异性测序，那就需要考虑方向，可以设定{@link #setConsiderStrand}
 * @author zong0jie
 */
public class RPKMcomput implements AlignmentRecorder {
	private static final Logger logger = Logger.getLogger(RPKMcomput.class);
	private static int numForFragment = 200000;
	/** 默认不考虑方向 */
	StrandSpecific strandSpecific = StrandSpecific.NONE;
	boolean isPairend = false;
	boolean upQuartile = false;
	/** 是否仅计算 unique mapped reads */
	boolean isUniqueMapped = false;
	Map<String, String> mapGene2Type;
	
	GffHashGene gffHashGene;
	GeneExpTable geneExpTable = new GeneExpTable(TitleFormatNBC.GeneID);
	/** 不同类型RNA的表达量，譬如tRNA多少，rRNA多少，ncRNA多少等 */
	GeneExpTable rnaTypeTable = new GeneExpTable(TitleFormatNBC.RNAType);
	/** 双端测序用来配对 */
	Map<String, SamRecord> mapKey2SamRecord = new LinkedHashMap<>((int)(numForFragment*1.5));
	
	boolean isSorted = true;
	
	int parNum = 0;
	
	/** 计数器，获得当前样本的总体 reads数， 用来算rpkm的 */
	double currentReadsNum = 0;
	
	/** 是否计算FPKM, pairend计算FPKM */
	public boolean isCalculateFPKM() {
		return isPairend;
	}
	
	public void setSorted(boolean isSorted) {
		this.isSorted = isSorted;
	}
	
	/** 是否仅计算 unique mapped reads，注意如果上层，也就是{@link AlignSamReading} 中设定了unique mapped reads，
	 * 则这里设定为false也没有用，依然只统计unique mapped reads。
	 * 只有当{@link AlignSamReading} 中设定为 全体 reads，这里才起作用。
	 */
	public void setUniqueMapped(boolean isUniqueMapped) {
		this.isUniqueMapped = isUniqueMapped;
	}
	
	public void setGffHashGene(GffHashGene gffHashGene) {
		this.gffHashGene = gffHashGene;
		initial();
	}
	public void setGffChrAbs(GffHashGeneAbs gffHashGeneAbs) {
		this.gffHashGene = new GffHashGene(gffHashGeneAbs);
		initial();
	}
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffHashGene = gffChrAbs.getGffHashGene();
		initial();
	}
	
	private void initial() {
		List<GffDetailGene> lsGffDetailGene = gffHashGene.getLsGffDetailGenes();
		Map<String, Integer> mapGene2Len = new HashMap<>();
		mapGene2Type = new HashMap<>();
		for (GffDetailGene gffDetailGene : lsGffDetailGene) {
			for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
				String geneName = gffGeneIsoInfo.getParentGeneName();
				int isoLength = gffGeneIsoInfo.getLenExon(0);
				//获得一个基因中最长转录本的名字
				if (!mapGene2Len.containsKey(geneName) || mapGene2Len.get(geneName) < isoLength) {
					mapGene2Len.put(geneName, isoLength);
				}
				if (!mapGene2Type.containsKey(geneName) || !mapGene2Type.get(geneName).equals(GeneType.mRNA.toString())) {
					mapGene2Type.put(geneName, gffGeneIsoInfo.getGeneType().toString());
				}
			}
		}
		geneExpTable.addLsGeneName(mapGene2Len.keySet());
		geneExpTable.setMapGene2Len(mapGene2Len);
		geneExpTable.addAnnotation(mapGene2Type);
		Set<String> setGeneType = new TreeSet<>();
		for (String geneType : mapGene2Type.values()) {
			setGeneType.add(geneType);
		}
		rnaTypeTable.addLsGeneName(setGeneType);
		List<String> lsTitleAnno = new ArrayList<>();
		lsTitleAnno.add(TitleFormatNBC.GeneType.toString());
		geneExpTable.addLsTitle(lsTitleAnno);
	}
	
	/** 设定是否为双端测序，同时会清空双端的hash表 */
	public void  setIsPairend(boolean isPairend) {
		this.isPairend = isPairend;
		mapKey2SamRecord.clear();
	}
	
	/** 是否考虑reads方向，只有链特异性测序才使用 */
	public void setConsiderStrand(StrandSpecific strandSpecific) {
		if (strandSpecific == null) {
			throw new ExceptionNullParam("No Param StrandSpecific");
		}
		this.strandSpecific = strandSpecific;
	}

	/**
	 * 设定样本名，同时清空currentReadsNum这个计数器
	 * 区分大小写
	 * @param currentCondition
	 */
	public void setAndAddCurrentCondition(String currentCondition) {
		geneExpTable.setCurrentCondition(currentCondition);
		rnaTypeTable.setCurrentCondition(currentCondition);
		this.currentReadsNum = 0;
	}
	
	@Override
	public void addAlignRecord(AlignRecord alignRecord) {
		if (!alignRecord.isMapped() || (isUniqueMapped && !alignRecord.isUniqueMapping())) return;
		List<SamRecord> lSamRecords = null;
		
		if (alignRecord.getName().contains("ST-E00276:159:H37Y7ALXX:8:2221:9983:21807")) {
			logger.info("ssssssssss");
		}
		if (mapKey2SamRecord.size() > 1000000) {
			for (SamRecord samRecord : mapKey2SamRecord.values()) {
				if (samRecord.isFirstRead()) {
					List<SamRecord> lsSamRecords = new ArrayList<>();
					lsSamRecords.add(samRecord);
					addAlignRecord(lsSamRecords);
				}
			}
			mapKey2SamRecord.clear();
		}
		
		try {
			lSamRecords = isSelectedReads(alignRecord);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("error:" + alignRecord.toString());
		}
		addAlignRecord(lSamRecords);
	}

	private void addAlignRecord(List<SamRecord> lsSamRecords) {
		if (lsSamRecords == null || lsSamRecords.size() == 0) {
			return;
		}
		
		SamRecord samRecord = lsSamRecords.get(0);
		//标记reads的正反向，将链特异性的reads转换成正向
		Boolean cis5to3 = samRecord.isCis5to3ConsiderStrand(strandSpecific);
		if (cis5to3 == null) cis5to3 = true;
		
		//TODO 待改进，如何能够更好的区分iso的表达
		List<List<Align>> lslsAligns = new ArrayList<>();
		lslsAligns.add(lsSamRecords.get(0).getAlignmentBlocks());
		if (lsSamRecords.size() > 1) {
			lslsAligns.add(lsSamRecords.get(1).getAlignmentBlocks());
		}
		Set<String> setGeneName = getSetGeneName(lslsAligns, cis5to3);
		if (setGeneName.size() == 0) {
			return;
		}
		for (String geneName : setGeneName) {
			//同时要考虑mapping至多个位置，以及两个不同的gene Overlap
			int weight =lsSamRecords.get(0).getMappedReadsWeight();
			if (lsSamRecords.size() > 1) {
				weight = Math.min(weight, lsSamRecords.get(1).getMappedReadsWeight());
			}
			addInMapGeneName2Cond2ReadsCounts(geneName, lsSamRecords.get(0).getMappedReadsWeight()*setGeneName.size());
		}
		currentReadsNum += (double)1/lsSamRecords.get(0).getMappedReadsWeight();
	}
	
	/** 挑选出配对的samRecord，如果不满足配对条件，则返回单条SamRecord */
	private List<SamRecord> isSelectedReads(AlignRecord alignRecord) {
		if (alignRecord instanceof SamRecord != true) return new ArrayList<SamRecord>();
		
		List<SamRecord> lsResult = new ArrayList<SamRecord>();
		SamRecord samRecord = (SamRecord)alignRecord;
		//是否计算RPKM
		if (!isPairend) {
			lsResult.add(samRecord);
			return lsResult;
		}
		//两个reads是否挨着
		if (!samRecord.isHavePairEnd() || !samRecord.isMateMapped() 
				|| !samRecord.getRefID().equals(samRecord.getMateRefID())
				|| Math.abs(samRecord.getStartAbs() - samRecord.getMateAlignmentStart()) > 5000000
				) {
			lsResult.add(samRecord);
			return lsResult;
		}
		
		String key = getSeqName(samRecord.getName()) + SepSign.SEP_ID + samRecord.getMateAlignmentStart();
		if (mapKey2SamRecord.containsKey(key)) {
			SamRecord samRecord2 = mapKey2SamRecord.remove(key);
			lsResult.add(samRecord2);
			lsResult.add(samRecord);
			parNum++;
			return lsResult;
		}
		//意思在map中没有找到他的mate，而靠前的reads一般都要进入map的
		if (isSorted && samRecord.getStartAbs() > samRecord.getMateAlignmentStart()) {
			lsResult.add(samRecord);
			return lsResult;
		}
		//判断两条reads是否在同一个基因内，或者说离的比较近
		GffCodGene gffCodGeneStart = gffHashGene.searchLocation(samRecord.getRefID(), samRecord.getStartAbs());
		GffCodGene gffCodGeneEnd = gffHashGene.searchLocation(samRecord.getRefID(), samRecord.getMateAlignmentStart());
		if (gffCodGeneStart == null || gffCodGeneEnd == null) {
			return new ArrayList<SamRecord>();
		}
		
		Set<String> setDetailGenes = getGffDetailKeySetStart(gffCodGeneStart);
		if (!setDetailGenes.contains(getGffDetailKey(gffCodGeneEnd.getGffDetailUp()))
				&&
				(!gffCodGeneEnd.isInsideLoc() || setDetailGenes.contains(getGffDetailKey(gffCodGeneEnd.getGffDetailThis())))
				&&
				!setDetailGenes.contains(getGffDetailKey(gffCodGeneEnd.getGffDetailDown()))
				) {
			lsResult.add(samRecord);
			return lsResult;
		}
		/////////////////////////////////
		mapKey2SamRecord.put(getSeqName(samRecord.getName()) + SepSign.SEP_ID + samRecord.getStartAbs(), samRecord);
		return new ArrayList<SamRecord>();
	}
	
	/** 给定seqName，过滤掉一些可能会造成错误的信息，如：
	 * HWI-ST507:97:C0AUBACXX:7:1101:1352:2093:1:1:0:ACTTGA/1
	 * HWI-ST507:97:C0AUBACXX:7:1101:1352:2093:2:1:0:ACTTGA/2
	 *	修改为   HWI-ST507:97:C0AUBACXX:7:1101:1352:2093:ACTTGA/1 和
	 *              HWI-ST507:97:C0AUBACXX:7:1101:1352:2093:ACTTGA/2
	 * @param seqName
	 * @return
	 */
	private String getSeqName(String seqName) {
		String[] ss = seqName.split(":");
		boolean remove = false;
		int num = ss.length;
		for (int i = ss.length - 1; i >= 0; i--) {
			if (i == ss.length - 1) {
				continue;
			}
			if (ss[i].length() > 1) {
				remove = true;
			}
			if (remove) {
				num = i + 1;
				break;
			}
		}
		
		String[] finalStr = new String[num];
		for (int i = 0; i < finalStr.length; i++) {
			finalStr[i] = ss[i];
		}
		String result = ArrayOperate.cmbString(finalStr, ":");
		return result;
	}
	
	private Set<String> getGffDetailKeySetStart(GffCodGene gffCodGeneStart) {
		Set<String> setKey = new HashSet<String>();
		if (gffCodGeneStart.getGffDetailUp() != null) {
			setKey.add(getGffDetailKey(gffCodGeneStart.getGffDetailUp()));
		}
		if (gffCodGeneStart.isInsideLoc()) {
			GffDetailGene gffDetailGene = gffCodGeneStart.getGffDetailThis();
			setKey.add(getGffDetailKey(gffDetailGene));
		}
		if (gffCodGeneStart.getGffDetailDown() != null) {
			setKey.add(getGffDetailKey(gffCodGeneStart.getGffDetailDown()));
		}
		return setKey;
	}
	
	private String getGffDetailKey(GffDetailGene gffDetailGene) {
		if (gffDetailGene == null) {
			return null;
		}
		return gffDetailGene.getRefID() + "_" +gffDetailGene.getStartAbs() + "_" + gffDetailGene.getEndAbs();
	}
	
	/**
	 * 移除setSamReads中过时的reads
	 */
	private void removeSetOverDue() {
		if (isPairend && mapKey2SamRecord.size() > 10000) {
			logger.debug(mapKey2SamRecord.size() + "\t" +parNum);
		}
//		if (isPairend && setSamReads.size() <= numForFragment) return;
//		List<String> lsReadsInfo = new ArrayList<String>();
//		int i = 0, max = setSamReads.size() - numForFragment;
//		for (String samRecord : setSamReads) {
//			lsReadsInfo.add(samRecord);
//			i++;
//			if (i >= max) break;
//		}
//		for (String string : lsReadsInfo) {
//			setSamReads.remove(string);
//			logger
//		}
	}
	private Set<String> getSetGeneName(List<List<Align>> lslsAligns, boolean readsCis5to3) {
		Set<String> setGeneName = new HashSet<>();
		Set<String> setGeneNameTmp = new HashSet<>();
		for (List<Align> lsAligns : lslsAligns) {
			if (setGeneName.size() == 0) {
				setGeneName = getSetGeneNameSingleReads(lsAligns, readsCis5to3);
			} else {
				Set<String> setGeneNameThis = getSetGeneNameSingleReads(lsAligns, readsCis5to3);
				if (setGeneNameThis.size() == 0) {
					continue;
				}
				setGeneNameTmp = new HashSet<>();
				for (String string : setGeneName) {
					if (setGeneNameThis.contains(string)) {
						setGeneNameTmp.add(string);
					}
				}
				setGeneName = setGeneNameTmp;
				setGeneNameTmp = setGeneNameThis;
			}
		}
		if (setGeneName.size() == 0) {
			setGeneName = setGeneNameTmp;
		}
		return setGeneName;
	}
	
	/** 根据序列比对情况，获得该序列到底mapping到哪个基因上 */
	private Set<String> getSetGeneNameSingleReads(List<Align> lsAligns, boolean readsCis5to3) {
		Set<GffGeneIsoInfo> setIso = new HashSet<>();
		for (Align align : lsAligns) {
			if (!gffHashGene.isContainChrID(align.getRefID())) continue;
			
			GffCodGene gffCodGene = gffHashGene.searchLocation(align.getRefID(), align.getMidSite());
			if (gffCodGene == null) continue;
			Set<GffGeneIsoInfo> setIsoCod = getSetGeneIso(readsCis5to3, gffCodGene);
			if (setIsoCod.size() == 0) {
				GffCodGeneDU gffCodGeneDu = gffHashGene.searchLocation(align.getRefID(), align.getStartAbs(), align.getEndAbs());
				setIsoCod = getSetGeneIso(readsCis5to3, gffCodGeneDu);
			}
			setIso.addAll(setIsoCod);
		}
		Set<String> setGeneName = new HashSet<>();
		
		if (lsAligns.size() == 1) {
			for (GffGeneIsoInfo gffGeneIsoInfo : setIso) {
				Align align = lsAligns.get(0);
				if (align.getStartAbs() > gffGeneIsoInfo.getEndAbs() || align.getEndAbs() < gffGeneIsoInfo.getStartAbs()) continue;
				if (align.getStartAbs() <= gffGeneIsoInfo.getStartAbs() && align.getEndAbs() >= gffGeneIsoInfo.getEndAbs()) {
					setGeneName.add(gffGeneIsoInfo.getParentGeneName());
					continue;
				}
				int start = gffGeneIsoInfo.getNumCodInEle(align.getStartAbs()+3);
				int end = gffGeneIsoInfo.getNumCodInEle(align.getEndAbs() - 3);
				//reads是否覆盖了小于两个exon
				if (start == end || Math.abs(Math.abs(start) - Math.abs(end)) <= 2) {
					setGeneName.add(gffGeneIsoInfo.getParentGeneName());
					continue;
				}
				//reads 是否覆盖了基因的开头或结尾
				if (gffGeneIsoInfo.isCis5to3()) {
					if (start == 0 && Math.abs(end) <= 2
						|| end == 0 && Math.abs(start) >= gffGeneIsoInfo.size()-1 ) 
					{
						setGeneName.add(gffGeneIsoInfo.getParentGeneName());
					}
				} else {
					if ( start == 0 && Math.abs(end) >= gffGeneIsoInfo.size()-1
					|| end == 0 && Math.abs(start) <= 2 ) 
					{
						setGeneName.add(gffGeneIsoInfo.getParentGeneName());
					}
				}
			}
			if (setGeneName.size() == 0) {
				for (GffGeneIsoInfo gffGeneIsoInfo : setIso) {
					setGeneName.add(gffGeneIsoInfo.getParentGeneName());
				}
			}
		} else {
			ArrayListMultimap<Integer, GffGeneIsoInfo> mapNotMatchNum2Iso = ArrayListMultimap.create();
			for (GffGeneIsoInfo gffGeneIsoInfo : setIso) {
				int notMatch = 0;
				for (int i = 0; i < lsAligns.size()-1; i++) {
					int beforeEnd = lsAligns.get(i).getEndAbs();//第一个exon的结尾
					int nextStart = lsAligns.get(i + 1).getStartAbs();//第二个exon的开头
					int beforeNum = gffGeneIsoInfo.getNumCodInEle(beforeEnd);
					int nextNum = gffGeneIsoInfo.getNumCodInEle(nextStart);
					if (beforeNum == 0 || nextNum == 0) {
						notMatch++;
						continue;
					}
					int beforeNum2Edge = 0, nextNum2Edge = 0;
					if (gffGeneIsoInfo.isCis5to3()) {
						beforeNum2Edge = gffGeneIsoInfo.getCod2ExInEnd(beforeEnd);
						nextNum2Edge = gffGeneIsoInfo.getCod2ExInStart(nextStart);
					} else {
						beforeNum2Edge = gffGeneIsoInfo.getCod2ExInEnd(nextStart);
						nextNum2Edge = gffGeneIsoInfo.getCod2ExInStart(beforeEnd);
					}
					if (beforeNum2Edge >= 5 || nextNum2Edge >= 5 || Math.abs((Math.abs(beforeNum) - Math.abs(nextNum))) > 1) {
						notMatch++;
					}
				}
				if (notMatch == 0) {
					setGeneName.add(gffGeneIsoInfo.getParentGeneName());
				} else {
					mapNotMatchNum2Iso.put(notMatch, gffGeneIsoInfo);
				}
			}
			if (setGeneName.size() == 0) {
				int notMatchMin = 10000;
				for (Integer num : mapNotMatchNum2Iso.keySet()) {
					if (num < notMatchMin) {
						notMatchMin = num;
					}
				}
				for (GffGeneIsoInfo iso : mapNotMatchNum2Iso.get(notMatchMin)) {
					setGeneName.add(iso.getParentGeneName());
				}
			}
		}
		return setGeneName;
	}
	
	/**
	 * 返回落到的基因Iso
	 * 没有落在基因内部就返回null
	 * @param readsCis5to3 考虑了方向
	 * @param gffCodGene
	 * @return
	 */
	private Set<GffGeneIsoInfo> getSetGeneIso(boolean readsCis5to3, GffCodGene gffCodGene) {
		//判定是否落在exon里面，落在intron里面不算
		if (!gffCodGene.isInsideLoc()) {
			return new HashSet<>();
		}
		
		GffDetailGene gffDetailGene = gffCodGene.getGffDetailThis();
		Set<GffGeneIsoInfo> setIso = new HashSet<>();
		for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
			if (gffGeneIsoInfo.getCodLoc(gffCodGene.getCoord()) == GffGeneIsoInfo.COD_LOC_EXON) {
				
				if (strandSpecific == null || strandSpecific == StrandSpecific.NONE || (readsCis5to3 == gffGeneIsoInfo.isCis5to3())) {
					setIso.add(gffGeneIsoInfo);
				}
			}
		}
		return setIso;
	}
	
	/**
	 * 返回落到的基因Iso
	 * 没有落在基因内部就返回null
	 * @param readsCis5to3 考虑了方向
	 * @param gffCodGene
	 * @return
	 */
	private Set<GffGeneIsoInfo> getSetGeneIso(boolean readsCis5to3, GffCodGeneDU gffCodGeneDu) {
		Set<GffGeneIsoInfo> setIso = new HashSet<>();
		gffCodGeneDu.setGeneBody(false);
		gffCodGeneDu.setExon(true);
		Set<GffDetailGene> setGffGene = gffCodGeneDu.getCoveredOverlapGffGene();
		for (GffDetailGene gffDetailGene : setGffGene) {
			for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
				if (strandSpecific == null || strandSpecific == StrandSpecific.NONE || (readsCis5to3 == gffGeneIsoInfo.isCis5to3())) {
					setIso.add(gffGeneIsoInfo);
				}
			}
		}
		return setIso;
	}
	
	/**
	 * @param geneName 基因名
	 * @param mapNum 如果是非unique mapping，那么mapping到几个位置上
	 */
	private void addInMapGeneName2Cond2ReadsCounts(String geneName, int mapNum) {
		double value = (double)1/mapNum;
		geneExpTable.addGeneExp(geneName, value);
		String geneType = mapGene2Type.get(geneName);
		rnaTypeTable.addGeneExp(geneType, value);
	}
	
	@Override
	public void summary() {
		if (isPairend) {
			for (SamRecord samRecord : mapKey2SamRecord.values()) {
				List<SamRecord> lsSamRecords = new ArrayList<SamRecord>();
				lsSamRecords.add(samRecord);
				addAlignRecord(lsSamRecords);
			}
		}
		geneExpTable.addAllReads((long)currentReadsNum);
	}
	
	/** 返回输入的值，不做修改或计算 */
	public List<String[]> getLsRawValue() {
		return geneExpTable.getLsAllCountsNum(EnumExpression.RawValue);
	}
	
	/** 返回计算得到的rpm值 */
	public List<String[]> getLsTPMs() {
		return geneExpTable.getLsAllCountsNum(EnumExpression.TPM);
	}
	/** 返回counts数量，可以拿来给DEseq继续做标准化 */
	public List<String[]> getLsCounts() {
		return geneExpTable.getLsAllCountsNum(EnumExpression.Counts);
	}	
	/**
	 * 返回计算得到的rpkm值
	 * 其中allreadscount的单位是百万
	 * exonlength的单位是kb
	 */
	public List<String[]> getLsRPKMs() {
		return geneExpTable.getLsAllCountsNum(EnumExpression.RPKM);
	}
	/**
	 * 返回用Upper Quartile计算得到的rpkm值
	 * 其中Upper Quartile的单位是1/100
	 * exonlength的单位是kb
	 */
	public List<String[]> getLsUQPMs() {
		return geneExpTable.getLsAllCountsNum(EnumExpression.UQ);
	}
	/** 返回ncRNA的情况，只有NCBI的模式物种，本项目才有意义 */
	public List<String[]> getLsNCrnaStatistics() {
		return rnaTypeTable.getLsAllCountsNum(EnumExpression.Counts);
	}
	
	/** 返回当前时期的rpm值 */
	public List<String[]> getLsTPMsCurrent() {
		return geneExpTable.getLsCountsNum(EnumExpression.TPM);
	}
	/** 返回counts数量，可以拿来给DEseq继续做标准化 */
	public List<String[]> getLsCountsCurrent() {
		return geneExpTable.getLsCountsNum(EnumExpression.Counts);
	}
	/** 返回counts数量，可以拿来给DEseq继续做标准化 */
	public List<String[]> getLsNCrnaStatisticsCurrent() {
		return rnaTypeTable.getLsCountsNum(EnumExpression.Counts);
	}
	/**
	 * 返回计算得到的rpkm值
	 * 其中allreadscount的单位是百万
	 * exonlength的单位是kb
	 */
	public List<String[]> getLsRPKMsCurrent() {
		return geneExpTable.getLsCountsNum(EnumExpression.RPKM);
	}
	/**
	 * 返回用Upper Quartile计算得到的rpkm值
	 * 其中Upper Quartile的单位是1/100
	 * exonlength的单位是kb
	 */
	public List<String[]> getLsUQPMsCurrent() {
		return geneExpTable.getLsCountsNum(EnumExpression.UQ);
	}
	/** 输入文件前缀，把所有结果写入该文件为前缀的文本中 */
	public void writeToFile(String resultExpPrefix, boolean isCountNCrna) {
		String outTPM = getFileCountsNameAll(resultExpPrefix, isCalculateFPKM(), EnumExpression.TPM);
		String outRPKM =  getFileCountsNameAll(resultExpPrefix, isCalculateFPKM(), EnumExpression.RPKM);
		String outUQ = getFileCountsNameAll(resultExpPrefix, isCalculateFPKM(), EnumExpression.UQ);
		String outCounts = getFileCountsNameAll(resultExpPrefix, isCalculateFPKM(), EnumExpression.Counts);
		String outNcRNA = null;
		if (isCountNCrna) {
			outNcRNA =  getFileCountsNameNcRNA(resultExpPrefix);
		}
		
		
		List<String[]> lsTpm = getLsTPMs();
		List<String[]> lsRpkm = getLsRPKMs();
		List<String[]> lsUQ = getLsUQPMs();
		List<String[]> lsCounts2 = getLsCounts();
		if (isCountNCrna) {
			List<String[]> lsNCRNA = getLsNCrnaStatistics();
			TxtReadandWrite txtNCRNA = new TxtReadandWrite(outNcRNA, true);
			txtNCRNA.ExcelWrite(lsNCRNA);
			txtNCRNA.close();
		}
		TxtReadandWrite txtWriteRpm = new TxtReadandWrite(outTPM, true);
		txtWriteRpm.ExcelWrite(lsTpm);
		TxtReadandWrite txtWriteRpkm = new TxtReadandWrite(outRPKM, true);
		txtWriteRpkm.ExcelWrite(lsRpkm);
		TxtReadandWrite txtWriteUQ = new TxtReadandWrite(outUQ, true);
		txtWriteUQ.ExcelWrite(lsUQ);
		TxtReadandWrite txtWriteCounts = new TxtReadandWrite(outCounts, true);
		txtWriteCounts.ExcelWrite(lsCounts2);
		txtWriteCounts.close();
		txtWriteRpkm.close();
		txtWriteUQ.close();
		txtWriteRpm.close();
	}
	
	/** 输入文件前缀，把所有结果写入该文件为前缀的文本中 */
	public void writeToFileCurrent(String outPathPrefix, boolean isCountNCrna) {
		outPathPrefix = FileOperate.getPathName(outPathPrefix) + "tmp/";
		FileOperate.createFolders(outPathPrefix);
		String fileNamePrefix = outPathPrefix + geneExpTable.getCurrentCondition();

		String outTPM = getFileCountsName(fileNamePrefix, isCalculateFPKM(), EnumExpression.TPM);
		String outRPKM =  getFileCountsName(fileNamePrefix, isCalculateFPKM(), EnumExpression.RPKM);
		String outUQ = getFileCountsName(fileNamePrefix, isCalculateFPKM(), EnumExpression.UQ);
		String outCounts = getFileCountsName(fileNamePrefix, isCalculateFPKM(), EnumExpression.Counts);
		String readsNumFile = getFileCountsNum(fileNamePrefix);
		String outNcRNA = null;
		if (isCountNCrna) {
			outNcRNA =  getFileCountsNameNcRNA(fileNamePrefix);
		}
		
		List<String[]> lsCounts = getLsCountsCurrent();
		List<String[]> lsTpm = getLsTPMsCurrent();
		List<String[]> lsRpkm = getLsRPKMsCurrent();
		List<String[]> lsUQ = getLsUQPMsCurrent();
		if (isCountNCrna) {
			List<String[]> lsNCrna = getLsNCrnaStatisticsCurrent();
			TxtReadandWrite txtWriteNCrna = new TxtReadandWrite(outNcRNA, true);
			txtWriteNCrna.ExcelWrite(lsNCrna);
			txtWriteNCrna.close();
		}
		
		TxtReadandWrite txtWriteRpm = new TxtReadandWrite(outTPM, true);
		txtWriteRpm.ExcelWrite(lsTpm);
		TxtReadandWrite txtWriteRpkm = new TxtReadandWrite(outRPKM, true);
		txtWriteRpkm.ExcelWrite(lsRpkm);
		TxtReadandWrite txtWriteUQRpkm = new TxtReadandWrite(outUQ, true);
		txtWriteUQRpkm.ExcelWrite(lsUQ);
		TxtReadandWrite txtWriteCounts = new TxtReadandWrite(outCounts, true);
		txtWriteCounts.ExcelWrite(lsCounts);
		txtWriteCounts.close();
		txtWriteRpkm.close();
		txtWriteUQRpkm.close();
		txtWriteRpm.close();
		
		TxtReadandWrite txtWriteAllReads = new TxtReadandWrite(readsNumFile, true);
		txtWriteAllReads.writefileln(Double.valueOf(currentReadsNum).longValue() + "");
		txtWriteAllReads.close();
	}
	
	public boolean isExistTmpResultAndReadExp(String outPathPrefix, boolean isCountNCrna) {
		outPathPrefix = FileOperate.getPathName(outPathPrefix) + "tmp/";
		String fileNamePrefix = outPathPrefix + geneExpTable.getCurrentCondition();
		String outTPM = getFileCountsName(fileNamePrefix, isCalculateFPKM(), EnumExpression.TPM);
		String outRPKM =  getFileCountsName(fileNamePrefix, false, EnumExpression.RPKM);
		String outFPKM =  getFileCountsName(fileNamePrefix, true, EnumExpression.RPKM);

		String outUQ = getFileCountsName(fileNamePrefix, isCalculateFPKM(), EnumExpression.UQ);
		String outCounts = getFileCountsName(fileNamePrefix, isCalculateFPKM(), EnumExpression.Counts);
		String readsNumFile = getFileCountsNum(fileNamePrefix);
		String outNcRNA = null;
		if (isCountNCrna) {
			outNcRNA =  getFileCountsNameNcRNA(fileNamePrefix);
		}
		

		boolean isExist = false;
		if (FileOperate.isFileExistAndBigThanSize(outTPM, 0)
				&& (FileOperate.isFileExistAndBigThan0(outRPKM) || FileOperate.isFileExistAndBigThan0(outFPKM))
				&& FileOperate.isFileExistAndBigThanSize(outUQ, 0)
				&& FileOperate.isFileExistAndBigThanSize(outCounts, 0)
				&& FileOperate.isFileExistAndBigThanSize(readsNumFile, 0)
				) {
			isExist = true;
		}
		
		if (isExist && isCountNCrna && !FileOperate.isFileExistAndBigThanSize(outNcRNA, 0)) {
			isExist = false;
			outNcRNA = null;
		}
		
		if (isExist) {
			readExpInfo(outCounts, readsNumFile, outNcRNA);
		}
	
		return isExist;
	}
	

	private void readExpInfo(String allCountsFile, String readsNumFile, String allRNAtypeFile) {
		geneExpTable.read(allCountsFile, EnumAddAnnoType.notAdd);
		TxtReadandWrite txtRead = new TxtReadandWrite(readsNumFile);
		String allCounts = txtRead.readFirstLine();
		txtRead.close();
		long allCountsL = (long) Double.parseDouble(allCounts);;
		geneExpTable.setAllReads(allCountsL);
		if (!StringOperate.isRealNull(allRNAtypeFile)) {
			rnaTypeTable.read(allRNAtypeFile, EnumAddAnnoType.notAdd);
		}
	}
	
	@Override
	public Align getReadingRegion() {
		return null;
	}
	
	/** 获得单个样本的文件名 */
	public static String getFileCountsName(String outAndPrefix, boolean isFPKM, EnumExpression expressType) {
		String suffix = "." + expressType.toString().toLowerCase();
		if (expressType == EnumExpression.RPKM && isFPKM) {
			suffix = ".FPKM".toLowerCase();
		}
		return outAndPrefix + suffix + ".exp.txt";
	}
	/** 获得总样本的文件名 */
	public static String getFileCountsNameNcRNA(String outAndPrefix) {
		return outAndPrefix + ".ncRNA.Statistics.exp.txt";
	}
	/** 获得总样本的文件名 */
	public static String getFileCountsNum(String outAndPrefix) {
		return outAndPrefix + ".readsnum.txt";
	}
	/** 获得总样本的文件名 */
	public static String getFileCountsNameAll(String outPathPrefix, boolean isFPKM, EnumExpression expressType) {
		String suffix = "." + expressType.toString().toLowerCase();
		if (expressType == EnumExpression.RPKM && isFPKM) {
			suffix = ".FPKM".toLowerCase();
		}
		suffix = "all" + suffix;
		
		if (!outPathPrefix.endsWith("/") && !outPathPrefix.endsWith("\\")) {
			suffix = "." + suffix;
		}
		return outPathPrefix + suffix + ".exp.txt";
	}
	/** 获得总样本的文件名 */
	public static String getFileCountsNameNcRNAAll(String outPathPrefix) {
		String suffix = "all.ncRNA.Statistics.exp.txt";
		if (!outPathPrefix.endsWith("/") && !outPathPrefix.endsWith("\\")) {
			suffix = "." + suffix;
		}
		return outPathPrefix + suffix;
	}
	
	public static enum EnumExpression {
		RawValue, TPM, RPKM, UQRPKM, UQ, Counts, 
		/** 某个item占总测序量的比例 */
		Ratio
	}
}
