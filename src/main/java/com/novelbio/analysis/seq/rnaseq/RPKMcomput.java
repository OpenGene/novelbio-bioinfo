package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.analysis.seq.AlignRecord;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffOperate.GffCodGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffCodGeneDU;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGeneAbs;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.analysis.seq.mapping.StrandSpecific;
import com.novelbio.analysis.seq.sam.AlignmentRecorder;
import com.novelbio.analysis.seq.sam.SamRecord;
import com.novelbio.base.SepSign;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.database.model.modgeneid.GeneType;
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
	boolean calculateFPKM = true;
	boolean upQuartile = false;
	
	GffHashGene gffHashGene;
	/**
	 * key：基因名<br>
	 * value：<br>
	 * key:condiiton<br>
	 *         value: 0 counts<br>
	 */
	Map<String, Map<String, double[]>> mapGeneName2Cond2ReadsCounts = new HashMap<String, Map<String,double[]>>();
	/**
	 * key: geneName
	 * value: geneLength
	 */
	Map<String, Integer> mapGeneName2Length = new TreeMap<String, Integer>();
	/**
	 * key: geneName
	 * value: geneType
	 */
	Map<String, GeneType> mapGeneName2Type = new HashMap<String, GeneType>();
	
	/** 双端测序用来配对 */
	HashMap<String, SamRecord> mapKey2SamRecord = new HashMap<String, SamRecord>((int)(numForFragment*1.5));
	/** 样本时期 和 样本reads num信息<br>
	 * key: 样本时期<br>
	 *  value: double[2] 0: allReadsNum 1: upQuartile的reads number<br>
	 *  用来算rpkm
	 */
	Map<String, double[]> mapCond2CountsNum = new LinkedHashMap<String, double[]>();
	/** 设定当前condition */
	String currentCondition;
	
	int parNum = 0;
	
	/** 计数器，获得当前样本的总体 reads数， 用来算rpkm的 */
	double currentReadsNum = 0;
	
	/** 是否计算FPKM，同时有FPKM和pairend才算是FPKM */
	public boolean isCalculateFPKM() {
		return isPairend && calculateFPKM;
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
		ArrayList<GffDetailGene> lsGffDetailGene = gffHashGene.getGffDetailAll();
		for (GffDetailGene gffDetailGene : lsGffDetailGene) {
			for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
				String geneName = gffGeneIsoInfo.getParentGeneName();
				int isoLength = gffGeneIsoInfo.getLenExon(0);
				//获得一个基因中最长转录本的名字
				if (!mapGeneName2Length.containsKey(geneName) || mapGeneName2Length.get(geneName) < isoLength) {
					mapGeneName2Length.put(geneName, isoLength);
					mapGeneName2Type.put(geneName, gffGeneIsoInfo.getGeneType());
				}
			}
		}
	}
	
	/** 设定是否为双端测序，同时会清空双端的hash表 */
	public void  setIsPairend(boolean isPairend) {
		this.isPairend = isPairend;
		mapKey2SamRecord.clear();
	}
	
	/** 是否考虑reads方向，只有链特异性测序才使用 */
	public void setConsiderStrand(StrandSpecific strandSpecific) {
		this.strandSpecific = strandSpecific;
	}
	/** 双端数据是否计算FPKM，单端设置该参数无效 */
	public void setCalculateFPKM(boolean calculateFPKM) {
		this.calculateFPKM = calculateFPKM;
		if (!calculateFPKM) {
			mapKey2SamRecord.clear();
		}
	}
	/**
	 * 设定样本名，同时清空currentReadsNum这个计数器
	 * 区分大小写
	 * @param currentCondition
	 */
	public void setCurrentCondition(String currentCondition) {
		this.currentCondition = currentCondition;
		this.currentReadsNum = 0;
	}
	
	@Override
	public void addAlignRecord(AlignRecord alignRecord) {
		if (!alignRecord.isMapped()) return;
		List<SamRecord> lSamRecords = null;
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
		
		boolean cis5to3 = lsSamRecords.get(0).isCis5to3();
		if (strandSpecific == StrandSpecific.SECOND_READ_TRANSCRIPTION_STRAND) {
			cis5to3 = !cis5to3;
		}
		
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
		if ((int)currentReadsNum % 50000 == 0) {
			removeSetOverDue();
		}
	}
	
	/** 挑选出配对的samRecord，如果不满足配对条件，则返回单条SamRecord */
	private List<SamRecord> isSelectedReads(AlignRecord alignRecord) {
		if (alignRecord instanceof SamRecord != true) return new ArrayList<SamRecord>();
		
		List<SamRecord> lsResult = new ArrayList<SamRecord>();
		SamRecord samRecord = (SamRecord)alignRecord;
		//是否计算RPKM
		if (!isPairend || !calculateFPKM) {
			lsResult.add(samRecord);
			return lsResult;
		}
		//两个reads是否挨着
		if (!samRecord.isHavePairEnd() || !samRecord.isMateMapped() 
				|| !samRecord.getRefID().equals(samRecord.getMateRefID())
				|| Math.abs(samRecord.getStartAbs() - samRecord.getMateAlignmentStart()) > 50000000
				) {
			lsResult.add(samRecord);
			return lsResult;
		}
		
		String key = samRecord.getName() + SepSign.SEP_ID + samRecord.getMateAlignmentStart();
		if (mapKey2SamRecord.containsKey(key)) {
			SamRecord samRecord2 = mapKey2SamRecord.remove(key);
			lsResult.add(samRecord2);
			lsResult.add(samRecord);
			parNum++;
			return lsResult;
		}
		//意思在map中没有找到他的mate，而靠前的reads一般都要进入map的
		if (samRecord.getStartAbs() > samRecord.getMateAlignmentStart()) {
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
		mapKey2SamRecord.put(samRecord.getName() + SepSign.SEP_ID + samRecord.getStartAbs(), samRecord);
		return new ArrayList<SamRecord>();
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
		if (isPairend && calculateFPKM && mapKey2SamRecord.size() > 100) {
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
				if (start == end || Math.abs(Math.abs(start) - Math.abs(end)) <= 2) {
					setGeneName.add(gffGeneIsoInfo.getParentGeneName());
					continue;
				}
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
				
				if (strandSpecific == StrandSpecific.NONE || (readsCis5to3 == gffGeneIsoInfo.isCis5to3())) {
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
		Set<GffDetailGene> setGffGene = gffCodGeneDu.getCoveredGffGene();
		for (GffDetailGene gffDetailGene : setGffGene) {
			for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
				if (strandSpecific == StrandSpecific.NONE || (readsCis5to3 == gffGeneIsoInfo.isCis5to3())) {
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
		Map<String, double[]>mapCond2ReadsCounts = null;
		if (mapGeneName2Cond2ReadsCounts.containsKey(geneName)) {
			mapCond2ReadsCounts = mapGeneName2Cond2ReadsCounts.get(geneName);
		} else {
			mapCond2ReadsCounts = new LinkedHashMap<String, double[]>();
			mapGeneName2Cond2ReadsCounts.put(geneName, mapCond2ReadsCounts);
		}
		
		double[] countsNum = null;
		if (mapCond2ReadsCounts.containsKey(currentCondition)) {
			countsNum = mapCond2ReadsCounts.get(currentCondition);
		} else {
			countsNum = new double[1];
			mapCond2ReadsCounts.put(currentCondition, countsNum);
		}
		countsNum[0] += (double)1/mapNum;
	}
	
	@Override
	public void summary() {
		if (isPairend && calculateFPKM) {
			for (SamRecord samRecord : mapKey2SamRecord.values()) {
				List<SamRecord> lsSamRecords = new ArrayList<SamRecord>();
				lsSamRecords.add(samRecord);
				addAlignRecord(lsSamRecords);
			}
		}
		double[] currentReadsNumInfo = new double[2];
		currentReadsNumInfo[0] = currentReadsNum;
		List<Double> lsReadsInfo = new ArrayList<Double>();
		for (String geneName : mapGeneName2Length.keySet()) {
			Map<String, double[]> mapCond2Counts = mapGeneName2Cond2ReadsCounts.get(geneName);
			if (mapCond2Counts == null) {
				lsReadsInfo.add(0.0);
			} else {
				double[] readsCounts = mapCond2Counts.get(currentCondition);
				if (readsCounts == null) {
					lsReadsInfo.add(0.0);
				} else {
					lsReadsInfo.add(readsCounts[0]);
				}
			}
		}
		currentReadsNumInfo[1] = MathComput.median(lsReadsInfo, 75);
		mapCond2CountsNum.put(currentCondition, currentReadsNumInfo);
	}
	
	/**
	 * @param enumExpression
	 * @param colGeneName
	 * @param lsConditions 按照指定的condition
	 * @param mapGeneName2Type 每个基因所对应的类型，如果存在则会装到最后的结果中，null表示没这个东西
	 * @param mapGeneName2Cond2ReadsCounts
	 * key：基因名<br>
	 * value：<br>
	 * key:condiiton<br>
	 *         value: 0 counts<br>
	 * @param mapCond2CountsNum
	 * 样本时期 和 样本reads num信息<br>
	 * key: 样本时期<br>
	 *  value: double[2] 0: allReadsNum 1: upQuartile的reads number<br>
	 *  用来算rpkm
	 *  @param mapGeneName2Length
	 *  
	 *  @return 返回按照 lsConditions顺序的基因表达list
	 */
	public static List<String[]> getLsCond2CountsNum(EnumExpression enumExpression, Collection<String> colGeneName,
			List<String> lsConditions,
			Map<String, ? extends Object> mapGeneName2Type,
			Map<String, Map<String, double[]>> mapGeneName2Cond2ReadsCounts,
			Map<String, double[]> mapCond2CountsNum, Map<String, Integer> mapGeneName2Length) {
		List<String[]> lsResult = new ArrayList<>();
		for (String geneName : colGeneName) {
			List<String> lsTmpResult = new ArrayList<String>();
			lsTmpResult.add(geneName);
			if (mapGeneName2Type != null) {
				lsTmpResult.add(mapGeneName2Type.get(geneName).toString());
			}
			
			Map<String, double[]> mapCond2Counts = mapGeneName2Cond2ReadsCounts.get(geneName);
			for (String conditions : lsConditions) {
				if (mapCond2Counts == null) {
					lsTmpResult.add(0 + "");
					continue;
				}
				double[] readsCounts = mapCond2Counts.get(conditions);
				if (readsCounts == null) {
					lsTmpResult.add(0 + "");
				} else {
					double[] allReads2UQreads = mapCond2CountsNum.get(conditions);
					lsTmpResult.add(getValue(enumExpression, readsCounts[0], allReads2UQreads[0], 
							allReads2UQreads[1], mapGeneName2Length.get(geneName)) + "");
				}
			}
			lsResult.add(lsTmpResult.toArray(new String[0]));
		}
		return lsResult;
	}
	
	/** 计算单个基因的表达值 */
	public static double getValue(EnumExpression enumExpression, double readsCount, double allReadsNum, double upQuerterNum, int geneLen) {
		if (enumExpression == EnumExpression.Counts) {
			return readsCount;
		} else if (enumExpression == EnumExpression.TPM) {
			return readsCount*1000000/allReadsNum;
		} else if (enumExpression == EnumExpression.RPKM) {
			return readsCount*1000000*1000/allReadsNum/geneLen;
		} else if (enumExpression == EnumExpression.UQRPKM) {
			return readsCount*100*1000/upQuerterNum/geneLen;
		} else if (enumExpression == EnumExpression.UQPM) {
			return readsCount*10/upQuerterNum;
		}
		return 0;
	}
	
	private List<String> getLsConditions() {
		return ArrayOperate.getArrayListKey(mapCond2CountsNum);
	}
	
	/**
	 * 返回表达值
	 * @param enumExpression
	 * @param condition 指定的时期，null表示返回全体时期
	 * @return
	 */
	private List<String[]> getLsExp(EnumExpression enumExpression, String condition) {
		List<String[]> lsResult = new ArrayList<String[]>();
		List<String> lsConditions = null;
		if (condition == null) {
			lsConditions = getLsConditions();
		} else {
			lsConditions = new ArrayList<>();
			lsConditions.add(condition);
		}
			
		lsResult = getLsCond2CountsNum(enumExpression, mapGeneName2Length.keySet(),
				lsConditions, mapGeneName2Type, mapGeneName2Cond2ReadsCounts, mapCond2CountsNum, mapGeneName2Length);
		lsConditions.add(0, TitleFormatNBC.GeneName.toString());
		lsConditions.add(1, TitleFormatNBC.GeneType.toString());
		lsResult.add(lsConditions.toArray(new String[0]));
		
		return lsResult;
	}
	
	
	/** 返回计算得到的rpm值 */
	public List<String[]> getLsTPMs() {
		return getLsExp(EnumExpression.TPM, null);
	}
	/** 返回counts数量，可以拿来给DEseq继续做标准化 */
	public List<String[]> getLsCounts() {
		return getLsExp(EnumExpression.Counts, null);
	}	
	/**
	 * 返回计算得到的rpkm值
	 * 其中allreadscount的单位是百万
	 * exonlength的单位是kb
	 */
	public List<String[]> getLsRPKMs() {
		return getLsExp(EnumExpression.RPKM, null);
	}
	/**
	 * 返回用Upper Quartile计算得到的rpkm值
	 * 其中Upper Quartile的单位是1/100
	 * exonlength的单位是kb
	 */
	public List<String[]> getLsUQRPKMs() {
		return getLsExp(EnumExpression.UQRPKM, null);
	}
	
	/** 返回当前时期的rpm值 */
	public List<String[]> getLsTPMsCurrent() {
		return getLsExp(EnumExpression.TPM, currentCondition);
	}
	/** 返回counts数量，可以拿来给DEseq继续做标准化 */
	public List<String[]> getLsCountsCurrent() {
		return getLsExp(EnumExpression.Counts, currentCondition);
	}
	
	/**
	 * 返回计算得到的rpkm值
	 * 其中allreadscount的单位是百万
	 * exonlength的单位是kb
	 */
	public List<String[]> getLsRPKMsCurrent() {
		return getLsExp(EnumExpression.RPKM, currentCondition);
	}
	
	/**
	 * 返回用Upper Quartile计算得到的rpkm值
	 * 其中Upper Quartile的单位是1/100
	 * exonlength的单位是kb
	 */
	public List<String[]> getLsUQRPKMsCurrent() {
		return getLsExp(EnumExpression.UQRPKM, currentCondition);
	}
	/** 输入文件前缀，把所有结果写入该文件为前缀的文本中 */
	public void writeToFile(String fileNamePrefix) {
		TxtReadandWrite txtWriteTpm = new TxtReadandWrite(fileNamePrefix + "_TPM", true);
		String suffix = "_RawCounts";
		if (isPairend && calculateFPKM) {
			suffix = "_RawFragments";
		}
		TxtReadandWrite txtWriteCounts = new TxtReadandWrite(fileNamePrefix + suffix, true);
		suffix = "_RPKM";
		if (isPairend && calculateFPKM) {
			suffix = "_FPKM";
		}
		TxtReadandWrite txtWriteRPKM = new TxtReadandWrite(fileNamePrefix + suffix, true);
		txtWriteCounts.ExcelWrite(getLsCounts());
		txtWriteRPKM.ExcelWrite(getLsRPKMs());
		txtWriteTpm.ExcelWrite(getLsTPMs());
		txtWriteCounts.close();
		txtWriteRPKM.close();
		txtWriteTpm.close();
	}
	
	/** 输入文件前缀，把所有结果写入该文件为前缀的文本中 */
	public void writeToFileCurrent(String fileNamePrefix) {
		TxtReadandWrite txtWriteTpm = new TxtReadandWrite(fileNamePrefix + currentCondition + "_TPM", true);
		String suffix = "_RawCounts";
		if (isPairend && calculateFPKM) {
			suffix = "_RawFragments";
		}
		TxtReadandWrite txtWriteCounts = new TxtReadandWrite(fileNamePrefix + currentCondition + suffix, true);
		suffix = "_RPKM";
		if (isPairend && calculateFPKM) {
			suffix = "_FPKM";
		}
		TxtReadandWrite txtWriteRPKM = new TxtReadandWrite(fileNamePrefix + currentCondition + suffix, true);
		txtWriteCounts.ExcelWrite(getLsCountsCurrent());
		txtWriteRPKM.ExcelWrite(getLsRPKMsCurrent());
		txtWriteTpm.ExcelWrite(getLsTPMsCurrent());
		txtWriteCounts.close();
		txtWriteRPKM.close();
		txtWriteTpm.close();
	}

	@Override
	public Align getReadingRegion() {
		return null;
	}
	
	public static enum EnumExpression {
		TPM, RPKM, UQRPKM, UQPM, Counts
	}
}
