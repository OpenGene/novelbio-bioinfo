package com.novelbio.analysis.seq.genomeNew;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFasta;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqHash;
import com.novelbio.analysis.seq.genomeNew.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene.GeneStructure;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.SiteInfo;
import com.novelbio.base.dataOperate.TxtReadandWrite;

public class GffChrSeq {
	private static Logger logger = Logger.getLogger(GffChrSeq.class);
	GffChrAbs gffChrAbs = null;
	
	GeneStructure geneStructure;
	/** true,提取该转录本，false，提取该gene下的最长转录本 */
	boolean absIso;
	/** 是否提取内含子 */
	boolean getIntron;
	/** 提取全基因组序列的时候，是每个LOC提取一条序列还是提取全部 */
	boolean getAllIso;
	
	public GffChrSeq() {}
	
	public GffChrSeq(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	/** 提取单个基因的时候<br>
	 * true：提取该基因对应的转录本<br>
	 * false 提取该基因所在基因的最长转录本<br>
	 * @param absIso
	 */
	public void setAbsIso(boolean absIso) {
		this.absIso = absIso;
	}
	/** 提取全基因组序列的时候，是每个LOC提取一条序列还是提取全部 */
	public void setGetAllIso(boolean getAllIso) {
		this.getAllIso = getAllIso;
	}
	/**
	 * 提取基因的时候遇到内含子，是提取出来还是跳过去
	 * @param getIntron
	 */
	public void setGetIntron(boolean getIntron) {
		this.getIntron = getIntron;
	}
	/**
	 * 将GffChrAbs导入，其中gffChrAbs务必初始化chrSeq和gffhashgene这两项
	 * @param gffChrAbs
	 */
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	/**
	 * 给定坐标，获得该坐标所对应的序列
	 * @return
	 */
	public void getSeq(SiteInfo mapInfo) {
		gffChrAbs.getSeqHash().getSeq(mapInfo);
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
	
	public void setGeneStructure(GeneStructure geneStructure) {
		this.geneStructure = geneStructure;
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
		GffGeneIsoInfo gffGeneIsoInfo = null;
		if (absIso)
			gffGeneIsoInfo = gffChrAbs.getGffHashGene().searchISO(IsoName);
		else
			gffGeneIsoInfo = gffChrAbs.getGffHashGene().searchLOC(IsoName).getLongestSplit();
		
		SeqFasta seqFasta = gffChrAbs.getSeqHash().getSeq(gffGeneIsoInfo.getChrID(), startExon, endExon, gffGeneIsoInfo, getIntron);
		seqFasta.setName(IsoName);
		return seqFasta;
	}
	public SeqFasta getSeq(String IsoName) {
		GffGeneIsoInfo gffGeneIsoInfo = null;
		if (absIso)
			gffGeneIsoInfo = gffChrAbs.getGffHashGene().searchISO(IsoName);
		else
			gffGeneIsoInfo = gffChrAbs.getGffHashGene().searchLOC(IsoName).getLongestSplit();
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
			getSiteRange(gffGeneIsoInfo, gffGeneIsoInfo.getTSSsite(),gffChrAbs.tss[0], gffChrAbs.tss[1]);
		}
		else if (geneStructure.equals(GeneStructure.TES)) {
			getSiteRange(gffGeneIsoInfo, gffGeneIsoInfo.getTSSsite(),gffChrAbs.tes[0], gffChrAbs.tes[1]);
		}
		SeqFasta seqFastaResult = gffChrAbs.getSeqHash().getSeq(gffGeneIsoInfo.getChrID(), lsExonInfos, getIntron);
		seqFastaResult.setName(gffGeneIsoInfo.getName());
		return seqFastaResult;
	}
	/**
	 * 提取某个位点的周边序列，根据方向返回合适的序列
	 * 用来提取Tss和Tes周边序列的
	 * @param cis5to3 方向
	 * @param site 位点
	 * @param upBp 该位点上游，考虑正反向
	 * @param downBp 该位点下游，考虑正反向
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
		SeqFasta seq = gffChrAbs.getSeqHash().getSeq(gffGeneIsoInfo.isCis5to3(), gffGeneIsoInfo.getChrID(), start, end);
		if (seq == null) {
			logger.error("没有提取到序列：" + " "+ gffGeneIsoInfo.getChrID() + " " + start + " " + end);
			return null;
		}
		seq.setName(gffGeneIsoInfo.getName());
		return seq;
	}
	/**
	 * 提取全基因组的promoter附近的序列
	 * @param upBp tss上游多少bp，负数，如果正数就在下游
	 * @param downBp tss下游多少bp，正数，如果负数就在上游
	 * @return
	 */
	public ArrayList<SeqFasta> getGenomeWideSeq() {
		ArrayList<String> lsID = gffChrAbs.getGffHashGene().getLsNameNoRedundent();
		ArrayList<SeqFasta> lsResult = new ArrayList<SeqFasta>();
		GffDetailGene gffDetailGene = null;
		for (String geneID : lsID) {
			gffDetailGene = gffChrAbs.getGffHashGene().searchLOC(geneID);
			if (getAllIso) {
				lsResult.addAll(getGeneSeqAllIso(gffDetailGene));
			}
			else {
				lsResult.add(getGeneSeqLongestIso(gffDetailGene));
			}
		}
		return lsResult;
	}
	private LinkedList<SeqFasta> getGeneSeqAllIso(GffDetailGene gffDetailGene) {
		LinkedList<SeqFasta> lsResult = new LinkedList<SeqFasta>();
		for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
			SeqFasta seqFasta = getSeq(gffGeneIsoInfo);
			if (seqFasta == null || seqFasta.getLength() < 3) {
				continue;
			}
			lsResult.add(seqFasta);
		}
		return lsResult;
	}
	private SeqFasta getGeneSeqLongestIso(GffDetailGene gffDetailGene) {
		GffGeneIsoInfo gffGeneIsoInfo = gffDetailGene.getLongestSplit();
		return getSeq(gffGeneIsoInfo);
	}
	/**
	 * 用指定motif搜索全基因组基因的promoter区域，返回得到的motif
	 * 并写入文本
	 * @param outTxtFile
	 * @param regex
	 * @param upBp
	 * @param downBp
	 */
	public void motifPromoterScan(String outTxtFile, String regex, int upBp, int downBp) {
		ArrayList<SeqFasta> lsPromoterSeq = getGenomePromoterSeq(upBp, downBp);
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		lsResult.add(SeqFasta.getMotifScanTitle());
		for (SeqFasta seqFasta : lsPromoterSeq) {
			lsResult.addAll(seqFasta.getMotifScanResult(regex));
		}
		TxtReadandWrite txtMotifOut = new TxtReadandWrite(outTxtFile, true);
		txtMotifOut.ExcelWrite(lsResult, "\t", 1, 1);
	}
	/**
	 * 获得某个物种的全部cds序列，从refseq中提取更加精确
	 * 每个基因只选取其中一条序列
	 * 按照GffGeneIsoInfo转录本给定的情况，自动提取相对于基因转录方向的序列
	 * @return
	 */
	public ArrayList<SeqFasta> getSeqCDSAll() {
		ArrayList<String> lsID = gffChrAbs.getGffHashGene().getLsNameNoRedundent();
		ArrayList<SeqFasta> lsResult = new ArrayList<SeqFasta>();
		GffGeneIsoInfo gffGeneIsoInfo = null;
		for (String geneID : lsID) {
			gffGeneIsoInfo = gffChrAbs.getGffHashGene().searchISO(geneID);
			ArrayList<ExonInfo> lsCDS = gffGeneIsoInfo.getIsoInfoCDS();
			if (lsCDS.size() > 0) {
				SeqFasta seq = gffChrAbs.getSeqHash().getSeq(gffGeneIsoInfo.getChrID(), lsCDS, false);
				if (seq == null || seq.getLength() < 3) {
					continue;
				}
				seq.setName(geneID);
				lsResult.add(seq);
			}
		}
		return lsResult;
	}
	//TODO 可以新建一个类将这些5UTR，3UTR，Promoter等全部装进去
	/**
	 * 获得某个物种的全部cds，也就是从ATG到UAG的每个ISO序列，从refseq中提取更加精确
	 * @return
	 */
	public ArrayList<SeqFasta> getSeqCDSAllIso() {
		ArrayList<String> lsID = gffChrAbs.getGffHashGene().getLsNameAll();
		ArrayList<SeqFasta> lsResult = new ArrayList<SeqFasta>();
		GffDetailGene gffDetailGene = null;
		for (String geneID : lsID) {
			gffDetailGene = gffChrAbs.getGffHashGene().searchLOC(geneID);
			gffDetailGene.removeDupliIso();
			for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
				ArrayList<ExonInfo> lsCDS = gffGeneIsoInfo.getIsoInfoCDS();
				if (lsCDS.size() > 0) {
					SeqFasta seq = gffChrAbs.getSeqHash().getSeq(gffGeneIsoInfo.getChrID(), lsCDS, false);
					if (seq == null || seq.getLength() < 3) {
						continue;
					}
					seq.setName(gffGeneIsoInfo.getName());
					lsResult.add(seq);
				}
			}
		}
		return lsResult;
	}
	/**
	 * 获得某个物种的全部3UTR序列，为了预测novel miRNA靶基因
	 */
	public ArrayList<SeqFasta> getSeq3UTRAll() {
		ArrayList<String> lsID = gffChrAbs.getGffHashGene().getLsNameNoRedundent();
		ArrayList<SeqFasta> lsResult = new ArrayList<SeqFasta>();
		GffDetailGene gffDetailGene = null;
		for (String geneID : lsID) {
			gffDetailGene = gffChrAbs.getGffHashGene().searchLOC(geneID);
			gffDetailGene.removeDupliIso();
			for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
				ArrayList<ExonInfo> lsCDS = gffGeneIsoInfo.getUTR3seq();
				if (lsCDS.size() > 0) {
					SeqFasta seq = gffChrAbs.getSeqHash().getSeq(gffGeneIsoInfo.getChrID(), lsCDS, false);
					if (seq == null || seq.getLength() < 3) {
						continue;
					}
					seq.setName(gffGeneIsoInfo.getName());
					lsResult.add(seq);
				}
			}
		}
		return lsResult;
	}
	/**
	 * 获得某个物种的全部RNA全长序列的每个ISO序列，从refseq中提取更加精确
	 * 按照GffGeneIsoInfo转录本给定的情况，自动提取相对于基因转录方向的序列
	 * @param IsoName 转录本的名字
	 * @param FilteredStrand 正反向，在提出的正向转录本的基础上，是否需要反向互补
	 * @param startExon 具体某个exon
	 * @param endExon 具体某个Intron
	 * @param absIso 是否是该转录本，false则选择该基因名下的最长转录本
	 * @param getIntron
	 * @return
	 */
	public ArrayList<SeqFasta> getSeqAllIso() {
		ArrayList<SeqFasta> lsResult = new ArrayList<SeqFasta>();
		for (GffDetailGene gffDetailGene : gffChrAbs.getGffHashGene().getLocHashtable().values()) {
			for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
				SeqFasta seq = gffChrAbs.getSeqHash().getSeq(gffGeneIsoInfo.getChrID(), gffGeneIsoInfo, false);
				if (seq == null || seq.getLength() < 3) {
					continue;
				}
				seq.setName(gffGeneIsoInfo.getName());
				lsResult.add(seq);
			}
		}
		return lsResult;
	}
	/**
	 * 获得某个物种的全部RNA全长序列的每个gene的最长序列，从refseq中提取更加精确
	 * 按照GffGeneIsoInfo转录本给定的情况，自动提取相对于基因转录方向的序列
	 * @param IsoName 转录本的名字
	 * @param FilteredStrand 正反向，在提出的正向转录本的基础上，是否需要反向互补
	 * @param startExon 具体某个exon
	 * @param endExon 具体某个Intron
	 * @param absIso 是否是该转录本，false则选择该基因名下的最长转录本
	 * @param getIntron
	 * @return
	 */
	public ArrayList<SeqFasta> getSeqAll() {
		ArrayList<SeqFasta> lsResult = new ArrayList<SeqFasta>();
		for (GffDetailGene gffDetailGene : gffChrAbs.getGffHashGene().getLocHashtable().values()) {
			GffGeneIsoInfo gffGeneIsoInfo = gffDetailGene.getLongestSplit();
				SeqFasta seq = gffChrAbs.getSeqHash().getSeq(gffGeneIsoInfo.getChrID(), gffGeneIsoInfo, false);
				if (seq == null || seq.getLength() < 3) {
					continue;
				}
				seq.setName(gffGeneIsoInfo.getName());
				lsResult.add(seq);
		}
		return lsResult;
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
				SeqFasta seq = gffChrAbs.getSeqHash().getSeq(gffGeneIsoInfo.getChrID(), gffGeneIsoInfo, false);
				seq.setName(gffGeneIsoInfo.getName());
				txtFasta.writefileln(seq.toStringNRfasta());
			}
		}
		txtFasta.close();
	}
}
