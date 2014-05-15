package com.novelbio.analysis.seq.genome;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene.GeneStructure;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.base.ExceptionNullParam;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.multithread.RunProcess;
import com.novelbio.database.model.modgeneid.GeneType;
/**
 * 在GffChrAbs中设定Tss和Tes的范围
 * setGetSeqIso 和 setGetSeqSite，谁后设定就提取谁
 * @author zong0jie
 *
 */
public class GffChrSeq extends RunProcess<GffChrSeq.GffChrSeqProcessInfo>{
	private static final Logger logger = Logger.getLogger(GffChrSeq.class);
	GffChrAbs gffChrAbs;
	
	GeneStructure geneStructure = GeneStructure.ALLLENGTH;
	/** 是否提取内含子 */
	boolean getIntron = false;
	/** 提取全基因组序列的时候，是每个LOC提取一条序列还是提取全部 */
	boolean getAllIso = false;
	/** 是否提取氨基酸 */
	boolean getAAseq = false;
	/** 是否仅提取mRNA序列 */
	boolean getOnlyMRNA = false;
	/** 同名序列是否提取多次，默认不提取同名序列 */
//	boolean isGetReplicateIso = false;
	boolean getGenomWide = false;
//	/** 是否提取iso的名字，默认是true，false表示提取基因的名字，也就是iso的parent name */
//	boolean isGetIsoName = true;
	/** 是提取位点还是提取基因 */
	boolean booGetIsoSeq = false;
	LinkedHashMap<String, GffGeneIsoInfo> mapName2Iso = new LinkedHashMap<>();
	ArrayList<Align> lsSiteInfos = new ArrayList<Align>();
	
	/** 默认存入文件，否则返回一个listSeqFasta */
	boolean saveToFile = true;
	ArrayList<SeqFasta> lsResult = new ArrayList<SeqFasta>();
	TxtReadandWrite txtOutFile;
	String outFile = "";
	
	int[] tssAtgRange;
	int[] tesUagRange; 
	
	public GffChrSeq() {}
	
	public GffChrSeq(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	public void setTssAtgRange(int[] tssRange) {
		this.tssAtgRange = tssRange;
	}
	public void setTesUagRange(int[] tesRange) {
		this.tesUagRange = tesRange;
	}
	/** 
	 * <b>false时{@link #setGetAAseq(boolean)}失效，返回的都是nr序列</b><p>
	 * 默认是ture，表示存入output文件
	 * 否则结果保存在lsResult中
	 * 如果要保存入文件，还需设定{@link #setOutPutFile(String)}
	 * 
	 */
	public void setIsSaveToFile(boolean saveToFile) {
		this.saveToFile = saveToFile;
	}
	/** 存入的output文件名
	 * 如果要保存入文件，还需设定{@link #setIsSaveToFile(boolean)}
	 */
	public void setOutPutFile(String outPutFile) {
		this.outFile = outPutFile;
	}
	
	/** 提取全基因组序列的时候，是每个Gene提取一条Iso还是提取全部Iso <br>
	 * true：提取该基因对应的转录本<br>
	 * false 提取该基因所在基因的最长转录本<br>
	 * 默认false
	 */
	public void setGetAllIso(boolean getAllIso) {
		this.getAllIso = getAllIso;
	}
	/** 是否仅提取mRNA，也就是有编码的RNA */
	public void setIsGetOnlyMRNA(boolean getOnlyMRNA) {
		this.getOnlyMRNA = getOnlyMRNA;
	}
	/**
	 * 提取基因的时候遇到内含子，是提取出来还是跳过去
	 * @param getIntron 默认false
	 */
	public void setGetIntron(boolean getIntron) {
		this.getIntron = getIntron;
	}
	public void setGetAAseq(boolean getAAseq) {
		this.getAAseq = getAAseq;
	}
	/**
	 * 将GffChrAbs导入，其中gffChrAbs务必初始化chrSeq和gffhashgene这两项
	 * @param gffChrAbs
	 */
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}

	/** 待提取基因的哪一个部分 */
	public void setGeneStructure(GeneStructure geneStructure) {
		if (geneStructure == null) {
			throw new ExceptionNullParam("No Param GeneStructre");
		}
		this.geneStructure = geneStructure;
	}
	/**
	 * 输入名字提取序列，内部会去除重复基因
	 * @param lsIsoName
	 */
	public void setGetSeqIso(List<String> lsIsoName) {
		mapName2Iso.clear();
		for (String string : lsIsoName) {
			List<GffGeneIsoInfo> lsGffGeneIsoInfo = getIso(string);
			if (lsGffGeneIsoInfo.size() == 0) {
				logger.warn("cannot find gene: " + string);
				continue;
			}
			for (GffGeneIsoInfo gffGeneIsoInfo : lsGffGeneIsoInfo) {
				if (getOnlyMRNA && !gffGeneIsoInfo.ismRNA()) {
					continue;
				}
				if (gffGeneIsoInfo != null) {
					if (getAllIso) {
						mapName2Iso.put(gffGeneIsoInfo.getName(), gffGeneIsoInfo);
					} else {
						mapName2Iso.put(string, gffGeneIsoInfo);
					}
				}
			}
		}
		booGetIsoSeq = true;
	}
	
	/**
	 * 输入名字提取序列，如果不同的转录本对应相同的基因，那么仅提取一个基因的序列
	 * @param lsIsoName
	 */
	public void setGetSeqIsoRemoveSamGene(List<String> lsIsoName) {
		mapName2Iso.clear();
		LinkedHashMap<String, GffGeneIsoInfo> mapGene2Iso = new LinkedHashMap<>();
		for (String string : lsIsoName) {
			List<GffGeneIsoInfo> lsGffGeneIsoInfo = getIso(string);
			if (lsGffGeneIsoInfo.size() == 0) {
				logger.warn("cannot find gene: " + string);
				continue;
			}
			for (GffGeneIsoInfo gffGeneIsoInfo : lsGffGeneIsoInfo) {
				if (getOnlyMRNA && !gffGeneIsoInfo.ismRNA()) {
					continue;
				}
				if (gffGeneIsoInfo != null) {
					String geneName = gffGeneIsoInfo.getParentGeneName();
					if (!mapGene2Iso.containsKey(geneName) || mapGene2Iso.get(geneName).getLenExon(0) < gffGeneIsoInfo.getLenExon(0)) {
						mapGene2Iso.put(geneName, gffGeneIsoInfo);
					}
				}
			}
		}
		
		mapName2Iso = mapGene2Iso;
		booGetIsoSeq = true;
	}
	public void setGetSeqGenomWide() {
		getGenomWide = true;
	}

	public int getNumOfQuerySeq() {
		if (getGenomWide) {
			fillSetNameGenomWide();
		}
		if (booGetIsoSeq) {
			return mapName2Iso.size();
		} else {
			return lsSiteInfos.size();
		}
	}
	/** 获取该基因中的最长转录本<br>
	 * 因为基因的重叠关系，同一个GffDetailGene中可能会包含不止一个gene
	 * 所以要遍历每个GffDetailGene，然后具有相同ParentName的Iso组 仅获得一条iso
	 * 譬如一个GffDetailGene有正向表达的mRNA3条，反向ncRNA两条，那么总共返回2条iso
	 * 其中一条来源于正向表达的mRNA，一条来源于反响的ncRNA
	 * 
	 * @param gffDetailGene
	 * @return
	 */
	private Map<String, GffGeneIsoInfo> getGeneSeqLongestIso(GffDetailGene gffDetailGene) {
		Map<String, GffGeneIsoInfo> mapParentName2Iso = new HashMap<>();
		for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
			String parentName = gffGeneIsoInfo.getParentGeneName();
			if (!mapParentName2Iso.containsKey(parentName) || 
					mapParentName2Iso.get(parentName).getLen() < gffGeneIsoInfo.getLen()) {
				mapParentName2Iso.put(parentName, gffGeneIsoInfo);
			}
		}
		return mapParentName2Iso;
	}
	/**
	 * 输入位点提取序列
	 * @param lsListGffName
	 */
	public void setGetSeqSite(ArrayList<Align> lsSiteName) {
		lsSiteInfos = lsSiteName;
		booGetIsoSeq = false;
	}
	/** 如果不是保存在文件中，就可以通过这个来获得结果 */
	public ArrayList<SeqFasta> getLsResult() {
		return lsResult;
	}
	/**
	 * 用指定motif搜索指定基因的指定区域，返回得到的motif
	 * 并写入文本
	 * @param regex
	 * 	给定motif，在全基因组的指定序列上查找相应的正则表达式<br>
	 * 返回正向序列和反向序列查找的结果<br>
	 * List-string [4] <br>
	 * 0: seqName<br>
	 * 1: strand : + / -<br>
	 * 2: 具体的motif序列<br>
	 * 3: motif最后一个碱基与本序列终点的距离
	 */
	public ArrayList<String[]> motifPromoterScan(String regex) {
		ArrayList<String[]> lsMotifResult = new ArrayList<String[]>();
		Set<String> setGeneNameRemoveDuplicate = new HashSet<>();
		if (booGetIsoSeq) {
			for (String name : mapName2Iso.keySet()) {
				GffGeneIsoInfo iso = mapName2Iso.get(name);
				SeqFasta seqFasta = getSeq(iso);
				seqFasta.setName(name);
				if (seqFasta == null || seqFasta.Length() < 3) {
					continue;
				}
				lsMotifResult.addAll(seqFasta.getMotifScan().getMotifScanResult(regex));
			}
		}
		return lsMotifResult;
	}

	@Override
	protected void running() {
		getSeq();
	}
	/**
	 * 先设定读取的序列，然后批量读取并写入文本
	 * @return
	 */
	public void getSeq() {
		if (getGenomWide) {
			fillSetNameGenomWide();
		}
		if (saveToFile)
			txtOutFile = new TxtReadandWrite(outFile, true);
		
		int num = 0;
		boolean isGetSeq = false;
		if (booGetIsoSeq) {
			for (String geneName : mapName2Iso.keySet()) {
				GffGeneIsoInfo iso = mapName2Iso.get(geneName);
				num++;
				SeqFasta seqFasta = getSeq(iso);
				if (seqFasta == null) {
					continue;
				}
				seqFasta.setName(geneName);
				isGetSeq = isSeqFastaAndWriteToFile(seqFasta);
				
				suspendCheck();
				if (flagStop) {
					break;
				}
				setTmpInfo(isGetSeq, seqFasta, num);
			}
		}
		else {
			for (Align align : lsSiteInfos) {
				num++;
				SeqFasta seqFasta = getSeq(align);
				isGetSeq = isSeqFastaAndWriteToFile(seqFasta);
				
				suspendCheck();
				if (flagStop) {
					break;
				}
				setTmpInfo(isGetSeq, seqFasta, num);
			}
		}
		if (saveToFile)
			txtOutFile.close();
	}

	/**
	 * 输入名字提取序列，内部会去除重复基因
	 * @param lsListGffName
	 */
	private void fillSetNameGenomWide() {
		mapName2Iso.clear();
		for (GffDetailGene gffDetailGene : gffChrAbs.getGffHashGene().getGffDetailAll()) {
			if (getOnlyMRNA && !gffDetailGene.isMRNA()) {
				continue;
			}
			if (getAllIso) {
				for (GffGeneIsoInfo iso : gffDetailGene.getLsCodSplit()) {
					mapName2Iso.put(iso.getName(), iso);
				}
			} else {
				mapName2Iso.putAll(getGeneSeqLongestIso(gffDetailGene));
				
			}
		}
		
		booGetIsoSeq = true;
	}
	
	/** 设定中间参数 */
	private void setTmpInfo(boolean isGetSeq, SeqFasta seqFasta, int number) {
		if (!isGetSeq) {
			return;
		}
		GffChrSeqProcessInfo gffChrSeqProcessInfo = new GffChrSeqProcessInfo(number);
		if (getAAseq) {
			gffChrSeqProcessInfo.setSeqFasta(seqFasta.toStringAAfasta());
		}
		else {
			gffChrSeqProcessInfo.setSeqFasta(seqFasta.toStringNRfasta());
		}
		setRunInfo(gffChrSeqProcessInfo);
	}
	
	private List<GffGeneIsoInfo> getIso(String IsoName) {
		try {
			if (getAllIso) {
				return gffChrAbs.getGffHashGene().searchISO(IsoName).getParentGffGeneSame().getLsCodSplit();
			} else {
				List<GffGeneIsoInfo> lsGeneIsoInfos = new ArrayList<>();
				GffGeneIsoInfo gffGeneIsoInfo = gffChrAbs.getGffHashGene().searchISO(IsoName);
				if (gffGeneIsoInfo != null) {
					lsGeneIsoInfos.add(gffGeneIsoInfo);
				}
				return lsGeneIsoInfos;
			}
		} catch (Exception e) {
			return new ArrayList<>();
		}
	}
	
	private GffGeneIsoInfo getIsoOne(String IsoName) {
		GffGeneIsoInfo gffGeneIsoInfo = gffChrAbs.getGffHashGene().searchISO(IsoName);
		return gffGeneIsoInfo;
	}
	
	/** 判定序列是否存在并且够长，同时根据需要写入output */
	private boolean isSeqFastaAndWriteToFile(SeqFasta seqFasta) {
		if (seqFasta == null || seqFasta.Length() < 3) {
			return false;
		}
		if (saveToFile) {
			if (getAAseq) {
				txtOutFile.writefileln(seqFasta.toStringAAfasta());
			}
			else {
				txtOutFile.writefileln(seqFasta.toStringNRfasta());
			}
		}
		else {
			lsResult.add(seqFasta);
		}
		return true;
	}
	/**
	 * 给定坐标，获得该坐标所对应的序列,会根据输入的方向进行反向
	 * @return
	 */
	public SeqFasta getSeq(Align siteInfo) {
		return gffChrAbs.getSeqHash().getSeq(siteInfo);
	}
	/**
	 * 给定坐标，提取序列
	 * @param IsoName
	 * @param absIso
	 * @param getIntron
	 * @return
	 */
	public SeqFasta getSeq(boolean cis5to3,String chrID, int startLoc, int endLoc) {
		return gffChrAbs.getSeqHash().getSeq(chrID, (long)startLoc, (long)endLoc);
	}
	/**
	 * 一般只返回一个seqfasta，不过如果设定了提取全部iso的序列，则会返回多个seqfasta<br>
	 * 设定外显子范围，获得具体序列
	 * 按照GffGeneIsoInfo转录本给定的情况，自动提取相对于基因转录方向的序列
	 * @param IsoName 转录本的名字
	 * @param startExon 具体某个exon 起点
	 * @param endExon 具体某个Intron 终点
	 * @param absIso 是否是该转录本，false则选择该基因名下的最长转录本
	 * @param getIntron
	 * @return
	 */
	public List<SeqFasta> getSeq(String IsoName, int startExon, int endExon, boolean getIntron) {
		List<SeqFasta> lsSeqFastas = new ArrayList<>();
		List<GffGeneIsoInfo> lsGffGeneIsoInfo = getIso(IsoName);
		for (GffGeneIsoInfo gffGeneIsoInfo : lsGffGeneIsoInfo) {
			SeqFasta seqFasta = gffChrAbs.getSeqHash().getSeq(gffGeneIsoInfo.isCis5to3(), gffGeneIsoInfo.getRefIDlowcase(), startExon, endExon, gffGeneIsoInfo.getLsElement(), getIntron);
			if (seqFasta == null) {
				continue;
			}
			seqFasta.setName(IsoName);
			lsSeqFastas.add(seqFasta);
		}
		return lsSeqFastas;
	}
	
	/**只返回一个seqfasta */
	public SeqFasta getSeqIso(String IsoName) {
		GffGeneIsoInfo gffGeneIsoInfo = getIsoOne(IsoName);
		SeqFasta seqFasta = getSeq(gffGeneIsoInfo);
		return seqFasta;
	}
	/**
	 * 根据genestructure中定义的结构提取序列
	 * 如果genestructure设定的是tss，则按照gffchrabs中设定的tss提取序列
	 * @param IsoName
	 * @param absIso true,提取该转录本，false，提取该gene下的最长转录本
	 * @return
	 */
	public SeqFasta getSeq(GffGeneIsoInfo gffGeneIsoInfo) {
		if (gffGeneIsoInfo == null) {
			return null;
		}
		List<ExonInfo> lsExonInfos = null;
		if (geneStructure.equals(GeneStructure.ALLLENGTH) || geneStructure.equals(GeneStructure.EXON)) {
			lsExonInfos = gffGeneIsoInfo.getLsElement();
		} else if (geneStructure.equals(GeneStructure.CDS)) {
			lsExonInfos = gffGeneIsoInfo.getIsoInfoCDS();
		} else if (geneStructure.equals(GeneStructure.INTRON)) {
			lsExonInfos = gffGeneIsoInfo.getLsIntron();
		} else if (geneStructure.equals(GeneStructure.UTR3)) {
			lsExonInfos = gffGeneIsoInfo.getUTR3seq();
		} else if (geneStructure.equals(GeneStructure.UTR5)) {
			lsExonInfos = gffGeneIsoInfo.getUTR5seq();
		} else if (geneStructure.equals(GeneStructure.TSS)) {
			return getSiteRange(gffGeneIsoInfo, gffGeneIsoInfo.getTSSsite(),tssAtgRange[0], tssAtgRange[1]);
		} else if (geneStructure.equals(GeneStructure.TES)) {
			return getSiteRange(gffGeneIsoInfo, gffGeneIsoInfo.getTESsite(),tesUagRange[0], tesUagRange[1]);
		} else if (geneStructure.equals(GeneStructure.ATG)) {
			return getSiteRange(gffGeneIsoInfo, gffGeneIsoInfo.getATGsite(), tssAtgRange[0], tssAtgRange[1]);
		} else if (geneStructure.equals(GeneStructure.UAG)) {
			return getSiteRange(gffGeneIsoInfo, gffGeneIsoInfo.getUAGsite(), tesUagRange[0], tesUagRange[1]);
		}
		if (lsExonInfos.size() == 0) {
			return null;
		}
		GffGeneIsoInfo gffGeneIsoInfoSearch = GffGeneIsoInfo.createGffGeneIso("", "", gffGeneIsoInfo.getParentGffDetailGene(), GeneType.mRNA, gffGeneIsoInfo.isCis5to3());
		gffGeneIsoInfoSearch.addAll(lsExonInfos);
		SeqFasta seqFastaResult = gffChrAbs.getSeqHash().getSeq(gffGeneIsoInfoSearch, getIntron);
		if (seqFastaResult == null) {
			return null;
		}
		seqFastaResult.setName(gffGeneIsoInfo.getName());
		return seqFastaResult;
	}
	/**
	 * 提取某个位点的周边序列，根据方向返回合适的序列
	 * 用来提取Tss和Tes周边序列的
	 * @param cis5to3 方向
	 * @param site 位点
	 * @param upBp 该位点上游，考虑正反向 上游为负数
	 * @param downBp 该位点下游，考虑正反向 下游为正数
	 * @return
	 */
	private SeqFasta getSiteRange(GffGeneIsoInfo gffGeneIsoInfo, int site, int upBp, int downBp) {
		int startlocation = 0; int endlocation = 0;
		if (gffGeneIsoInfo.isCis5to3()) {
			startlocation = site + upBp;
			endlocation = site + downBp;
		}
		else {
			startlocation = site - upBp;
			endlocation = site - downBp;
		}
		int start = Math.min(startlocation, endlocation);
		int end = Math.max(startlocation, endlocation);
		SeqFasta seq = gffChrAbs.getSeqHash().getSeq(gffGeneIsoInfo.isCis5to3(), gffGeneIsoInfo.getRefIDlowcase(), start, end);
		if (seq == null) {
			logger.error("没有提取到序列：" + " "+ gffGeneIsoInfo.getRefIDlowcase() + " " + start + " " + end);
			return null;
		}
		seq.setName(gffGeneIsoInfo.getName());
		return seq;
	}
	/**
	 * 可以给rsem使用
	 * 内部自动close
	 * @param seqFastaTxt
	 * @return
	 */
	public void writeIsoFasta(String seqFastaTxt) {
		HashSet<String> setRemoveRedundent = new HashSet<String>();
		TxtReadandWrite txtFasta = new TxtReadandWrite(seqFastaTxt, true);
		ArrayList<GffDetailGene> lsGffDetailGenes = gffChrAbs.getGffHashGene().getGffDetailAll();
		for (GffDetailGene gffDetailGene : lsGffDetailGenes) {
			for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
				if (setRemoveRedundent.contains(gffGeneIsoInfo.getName())) {
					continue;
				}
				setRemoveRedundent.add(gffGeneIsoInfo.getName());
				SeqFasta seq = gffChrAbs.getSeqHash().getSeq(gffGeneIsoInfo, false);
				if (seq == null) {
					continue;
				}
				seq.setName(gffGeneIsoInfo.getName());
				txtFasta.writefileln(seq.toStringNRfasta());
			}
		}
		txtFasta.close();
	}
	
	public void reset() {
		geneStructure = GeneStructure.ALLLENGTH;
		/** 是否提取内含子 */
		getIntron = true;
		/** 提取全基因组序列的时候，是每个LOC提取一条序列还是提取全部 */
		getAllIso = true;
		/** 是否提取氨基酸 */
		getAAseq = false;
		/** 是否仅提取mRNA序列 */
		getOnlyMRNA = false;
		getGenomWide = false;
		/** 是提取位点还是提取基因 */
		booGetIsoSeq = false;
		mapName2Iso = new LinkedHashMap<>();
		lsSiteInfos = new ArrayList<>();
		
		/** 默认存入文件，否则返回一个listSeqFasta */
		saveToFile = true;
		lsResult = new ArrayList<SeqFasta>();
		txtOutFile = null;
		outFile = "";
		
		tssAtgRange = null;
		tesUagRange = null;
	}
	public static class GffChrSeqProcessInfo {
		int number;
		ArrayList<String> lsTmpInfo = new ArrayList<String>();
		public GffChrSeqProcessInfo(int number) {
			this.number = number;
		}
		public void setSeqFasta(String string) {
			lsTmpInfo.add(string);
		}
		public int getNumber() {
			return number;
		}
		public ArrayList<String> getLsTmpInfo() {
			return lsTmpInfo;
		}
	}

}

