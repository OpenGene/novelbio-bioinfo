package com.novelbio.analysis.seq.genome;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene.GeneStructure;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.mappingOperate.SiteSeqInfo;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.multithread.RunProcess;
import com.novelbio.database.model.modgeneid.GeneType;
import com.novelbio.database.model.species.Species;
/**
 * 在GffChrAbs中设定Tss和Tes的范围
 * setGetSeqIso 和 setGetSeqSite，谁后设定就提取谁
 * @author zong0jie
 *
 */
public class GffChrSeq extends RunProcess<GffChrSeq.GffChrSeqProcessInfo>{
	private static Logger logger = Logger.getLogger(GffChrSeq.class);
	GffChrAbs gffChrAbs = new GffChrAbs();
	
	GeneStructure geneStructure = GeneStructure.ALLLENGTH;
	/** 是否提取内含子 */
	boolean getIntron;
	/** 提取全基因组序列的时候，是每个LOC提取一条序列还是提取全部 */
	boolean getAllIso;
	/** 是否提取氨基酸 */
	boolean getAAseq = false;
	/** 是否仅提取mRNA序列 */
	boolean getOnlyMRNA = false;
	
	boolean getGenomWide = false;
	
	/** 是提取位点还是提取基因 */
	boolean booGetIsoSeq = false;
	LinkedHashSet<GffGeneIsoInfo> setIsoToGetSeq = new LinkedHashSet<GffGeneIsoInfo>();
	ArrayList<SiteSeqInfo> lsSiteInfos = new ArrayList<SiteSeqInfo>();
	
	/** 默认存入文件，否则返回一个listSeqFasta */
	boolean saveToFile = true;
	ArrayList<SeqFasta> lsResult = new ArrayList<SeqFasta>();
	TxtReadandWrite txtOutFile;
	String outFile = "";
	
	int[] tssRange;
	int[] tesRange; 
	
	public GffChrSeq() {}
	
	public GffChrSeq(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	public void setTssRange(int[] tssRange) {
		this.tssRange = tssRange;
	}
	public void setTesRange(int[] tesRange) {
		this.tesRange = tesRange;
	}
	public void setSpecies(Species species) {
		gffChrAbs.setSpecies(species);
	}
	/** 默认是ture，表示存入output文件
	 * 否则结果保存在lsResult中
	 * 如果要保存入文件，还需设定{@link #setOutPutFile(String)}
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
	 * @param getIntron
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
		this.geneStructure = geneStructure;
	}
	/**
	 * 输入名字提取序列，内部会去除重复基因
	 * @param lsIsoName
	 */
	public void setGetSeqIso(List<String> lsIsoName) {
		setIsoToGetSeq.clear();
		for (String string : lsIsoName) {
			GffGeneIsoInfo gffGeneIsoInfo = getIso(string);
			if (gffGeneIsoInfo == null) {
				logger.error("没找到基因：" + string);
				continue;
			}
			if (getOnlyMRNA && !gffGeneIsoInfo.ismRNA()) {
				continue;
			}
			if (gffGeneIsoInfo != null) {
				setIsoToGetSeq.add(gffGeneIsoInfo);
			}
		}
		booGetIsoSeq = true;
	}
	public void setGetSeqGenomWide() {
		getGenomWide = true;
	}

	public int getNumOfQuerySeq() {
		if (getGenomWide) {
			getSeqIsoGenomWide();
		}
		if (booGetIsoSeq) {
			return setIsoToGetSeq.size();
		} else {
			return lsSiteInfos.size();
		}
	}
	private LinkedList<GffGeneIsoInfo> getGeneSeqAllIso(GffDetailGene gffDetailGene) {
		LinkedList<GffGeneIsoInfo> lsResult = new LinkedList<GffGeneIsoInfo>();
		for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
			lsResult.add(gffGeneIsoInfo);
		}
		return lsResult;
	}
	private LinkedList<GffGeneIsoInfo> getGeneSeqLongestIso(GffDetailGene gffDetailGene) {
		LinkedList<GffGeneIsoInfo> lsResult = new LinkedList<GffGeneIsoInfo>();
		GffGeneIsoInfo gffGeneIsoInfo = gffDetailGene.getLongestSplitMrna();
		lsResult.add(gffGeneIsoInfo);
		return lsResult;
	}
	/**
	 * 输入位点提取序列
	 * @param lsListGffName
	 */
	public void setGetSeqSite(ArrayList<SiteSeqInfo> lsSiteName) {
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
		if (booGetIsoSeq) {
			for (GffGeneIsoInfo gffGeneIsoInfo : setIsoToGetSeq) {
				SeqFasta seqFasta = getSeq(gffGeneIsoInfo);
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
			getSeqIsoGenomWide();
		}
		if (saveToFile)
			txtOutFile = new TxtReadandWrite(outFile, true);
		
		int num = 0;
		boolean isGetSeq = false;
		if (booGetIsoSeq) {
			for (GffGeneIsoInfo gffGeneIsoInfo : setIsoToGetSeq) {
				num++;
				SeqFasta seqFasta = getSeq(gffGeneIsoInfo);

				isGetSeq = isSeqFastaAndWriteToFile(seqFasta);
				
				suspendCheck();
				if (flagStop) {
					break;
				}
				setTmpInfo(isGetSeq, seqFasta, num);
			}
		}
		else {
			for (SiteSeqInfo siteInfo : lsSiteInfos) {
				num++;
				getSeq(siteInfo);
				SeqFasta seqFasta = siteInfo.getSeqFasta();
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
	private void getSeqIsoGenomWide() {
		setIsoToGetSeq.clear();
		ArrayList<String> lsID = gffChrAbs.getGffHashGene().getLsNameNoRedundent();
		GffDetailGene gffDetailGene = null;
		for (String geneID : lsID) {
			gffDetailGene = gffChrAbs.getGffHashGene().searchLOC(geneID);
			if (getOnlyMRNA && !gffDetailGene.isMRNA()) {
				continue;
			}
			if (getAllIso) {
				setIsoToGetSeq.addAll(getGeneSeqAllIso(gffDetailGene));
			} else {
				setIsoToGetSeq.addAll(getGeneSeqLongestIso(gffDetailGene));
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
	
	private GffGeneIsoInfo getIso(String IsoName) {
		if (getAllIso) {
			return gffChrAbs.getGffHashGene().searchISO(IsoName);
		} else {
			GffDetailGene gffDetailGene = gffChrAbs.getGffHashGene().searchLOC(IsoName);
			if (gffDetailGene != null) {
				return gffDetailGene.getLongestSplitMrna();
			}
		}
		return null;
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
	public void getSeq(SiteSeqInfo siteInfo) {
		gffChrAbs.getSeqHash().getSeq(siteInfo);
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
	 * 设定外显子范围，获得具体序列
	 * 按照GffGeneIsoInfo转录本给定的情况，自动提取相对于基因转录方向的序列
	 * @param IsoName 转录本的名字
	 * @param startExon 具体某个exon 起点
	 * @param endExon 具体某个Intron 终点
	 * @param absIso 是否是该转录本，false则选择该基因名下的最长转录本
	 * @param getIntron
	 * @return
	 */
	public SeqFasta getSeq(String IsoName, int startExon, int endExon, boolean getIntron) {
		GffGeneIsoInfo gffGeneIsoInfo = getIso(IsoName);
		SeqFasta seqFasta = gffChrAbs.getSeqHash().getSeq(gffGeneIsoInfo.isCis5to3(), gffGeneIsoInfo.getRefID(), startExon, endExon, gffGeneIsoInfo, getIntron);
		if (seqFasta == null) {
			return null;
		}
		seqFasta.setName(IsoName);
		return seqFasta;
	}
	public SeqFasta getSeq(String IsoName) {
		GffGeneIsoInfo gffGeneIsoInfo = getIso(IsoName);
		return getSeq(gffGeneIsoInfo);
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
		ArrayList<ExonInfo> lsExonInfos = null;
		if (geneStructure.equals(GeneStructure.ALLLENGTH) || geneStructure.equals(GeneStructure.EXON)) {
			lsExonInfos = gffGeneIsoInfo;
		}
		else if (geneStructure.equals(GeneStructure.CDS)) {
			lsExonInfos = gffGeneIsoInfo.getIsoInfoCDS();
		}
		else if (geneStructure.equals(GeneStructure.INTRON)) {
			lsExonInfos = gffGeneIsoInfo.getLsIntron();
		}
		else if (geneStructure.equals(GeneStructure.UTR3)) {
			lsExonInfos = gffGeneIsoInfo.getUTR3seq();
		}
		else if (geneStructure.equals(GeneStructure.UTR5)) {
			lsExonInfos = gffGeneIsoInfo.getUTR5seq();
		}
		else if (geneStructure.equals(GeneStructure.TSS)) {
			return getSiteRange(gffGeneIsoInfo, gffGeneIsoInfo.getTSSsite(),tssRange[0], tssRange[1]);
		}
		else if (geneStructure.equals(GeneStructure.TES)) {
			return getSiteRange(gffGeneIsoInfo, gffGeneIsoInfo.getTSSsite(),tesRange[0], tesRange[1]);
		}
		if (lsExonInfos.size() == 0) {
			return null;
		}
		GffGeneIsoInfo gffGeneIsoInfoSearch = GffGeneIsoInfo.createGffGeneIso("", "", GeneType.mRNA, gffGeneIsoInfo.isCis5to3());
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
		SeqFasta seq = gffChrAbs.getSeqHash().getSeq(gffGeneIsoInfo.isCis5to3(), gffGeneIsoInfo.getRefID(), start, end);
		if (seq == null) {
			logger.error("没有提取到序列：" + " "+ gffGeneIsoInfo.getRefID() + " " + start + " " + end);
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
		setIsoToGetSeq = new LinkedHashSet<GffGeneIsoInfo>();
		lsSiteInfos = new ArrayList<SiteSeqInfo>();
		
		/** 默认存入文件，否则返回一个listSeqFasta */
		saveToFile = true;
		lsResult = new ArrayList<SeqFasta>();
		txtOutFile = null;
		outFile = "";
		
		tssRange = null;
		tesRange = null;
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

