package com.novelbio.analysis.seq.genomeNew;

import java.util.ArrayList;
import java.util.HashSet;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFasta;
import com.novelbio.analysis.seq.genomeNew.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapInfo;
import com.novelbio.base.dataOperate.TxtReadandWrite;

public class GffChrSeq {
	private static Logger logger = Logger.getLogger(GffChrSeq.class);
	GffChrAbs gffChrAbs = null;
	
	public GffChrSeq() {}
	
	public GffChrSeq(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	/**
	 * 将GffChrAbs导入，其中gffChrAbs务必初始化chrSeq和gffhashgene这两项
	 * @param gffChrAbs
	 */
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	/**
	 * 给定基因名，获得该转录本的信息
	 * 按照GffGeneIsoInfo转录本给定的情况，自动提取相对于基因转录方向的序列
	 * @param IsoName 转录本的名字
	 * @param absIso 是否是该转录本，false则选择该基因名下的最长转录本
	 * @return
	 */
	public SeqFasta getSeq(String IsoName, boolean absIso,boolean getIntron) {
		GffGeneIsoInfo gffGeneIsoInfo = null;
		if (absIso)
			gffGeneIsoInfo = gffChrAbs.getGffHashGene().searchISO(IsoName);
		else
			gffGeneIsoInfo = gffChrAbs.getGffHashGene().searchLOC(IsoName).getLongestSplit();
		
		return gffChrAbs.getSeqHash().getSeq(gffGeneIsoInfo.getChrID(), gffGeneIsoInfo, getIntron);
	}
	/**
	 * 给定坐标，获得该坐标所对应的序列
	 * @return
	 */
	public void getSeq(MapInfo mapInfo) {
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
	
	/**
	 * 给定基因名，获得该转录本的信息
	 * 按照GffGeneIsoInfo转录本给定的情况，自动提取相对于基因转录方向的序列
	 * @param IsoName 转录本的名字
	 * @param cis5to3 正反向，在提出的正向转录本的基础上，是否需要反向互补。
	 * @param startExon 具体某个exon
	 * @param endExon 具体某个Intron
	 * @param absIso 是否是该转录本，false则选择该基因名下的最长转录本
	 * @param getIntron
	 * @return
	 */
	public SeqFasta getSeq(String IsoName, boolean cis5to3,int startExon, int endExon, boolean absIso,boolean getIntron) {
		GffGeneIsoInfo gffGeneIsoInfo = null;
		if (absIso)
			gffGeneIsoInfo = gffChrAbs.getGffHashGene().searchISO(IsoName);
		else
			gffGeneIsoInfo = gffChrAbs.getGffHashGene().searchLOC(IsoName).getLongestSplit();
		
		SeqFasta seqFasta = gffChrAbs.getSeqHash().getSeq(gffGeneIsoInfo.getChrID(), cis5to3, startExon, endExon, gffGeneIsoInfo, getIntron);
		seqFasta.setName(IsoName);
		return seqFasta;
	}
	
	
	
	/**
	 * 给定基因名，获得该转录本的信息
	 * 不管转录本的方向，总是从基因组的5‘向3’提取。 方向需要人工设定cisseq
	 * @param cisseq 获得正向还是反向序列，相对于基因组来说的
	 * @param IsoName 转录本的名字
	 * @param absIso 是否是该转录本，false则选择该基因名下的最长转录本
	 * @param getIntron 是否获得内含子
	 * @return
	 */
	public SeqFasta getSeq(boolean cisseq, String IsoName, boolean absIso,boolean getIntron) {
		GffGeneIsoInfo gffGeneIsoInfo = null;
		if (absIso)
			gffGeneIsoInfo = gffChrAbs.getGffHashGene().searchISO(IsoName);
		else
			gffGeneIsoInfo = gffChrAbs.getGffHashGene().searchLOC(IsoName).getLongestSplit();
		
		SeqFasta seqFasta = gffChrAbs.getSeqHash().getSeq(cisseq, gffGeneIsoInfo.getChrID(), gffGeneIsoInfo, getIntron);
		seqFasta.setName(IsoName);
		return seqFasta;
	}

	/**
	 * 给定基因名，获得该转录本的信息
	 * 按照GffGeneIsoInfo转录本给定的情况，自动提取相对于基因转录方向的序列
	 * @param IsoName 转录本的名字
	 * @param cis5to3 正反向，在提出的正向转录本的基础上，是否需要反向互补。
	 * @param startExon 具体某个exon
	 * @param endExon 具体某个Intron
	 * @param absIso 是否是该转录本，false则选择该基因名下的最长转录本
	 * @param getIntron
	 * @return
	 */
	public SeqFasta getSeqCDS(String IsoName, boolean cis5to3, boolean absIso,boolean getIntron) {
		GffGeneIsoInfo gffGeneIsoInfo = null;
		if (absIso)
			gffGeneIsoInfo = gffChrAbs.getGffHashGene().searchISO(IsoName);
		else
			gffGeneIsoInfo = gffChrAbs.getGffHashGene().searchLOC(IsoName).getLongestSplit();
		
		SeqFasta seqFasta = gffChrAbs.getSeqHash().getSeq(gffGeneIsoInfo.getChrID(), cis5to3, 0, 0, gffGeneIsoInfo.getIsoInfoCDS(), getIntron);
		seqFasta.setName(IsoName);
		return seqFasta;
	}
	
	/**
	 * 给定基因名，获得该转录本的信息
	 * 按照GffGeneIsoInfo转录本给定的情况，自动提取相对于基因转录方向的序列
	 * @param IsoName 转录本的名字
	 * @param cis5to3 正反向，在提出的正向转录本的基础上，是否需要反向互补
	 * @param startExon 具体某个exon
	 * @param endExon 具体某个Intron
	 * @param absIso 是否是该转录本，false则选择该基因名下的最长转录本
	 * @param getIntron
	 * @return
	 */
	public String getSeqProtein(String IsoName, boolean cis5to3, boolean absIso,boolean getIntron) {
		GffGeneIsoInfo gffGeneIsoInfo = null;
		if (absIso)
			gffGeneIsoInfo = gffChrAbs.getGffHashGene().searchISO(IsoName);
		else
			gffGeneIsoInfo = gffChrAbs.getGffHashGene().searchLOC(IsoName).getLongestSplit();
		
		SeqFasta seq = gffChrAbs.getSeqHash().getSeq(gffGeneIsoInfo.getChrID(), cis5to3, 0, 0, gffGeneIsoInfo.getIsoInfoCDS(), getIntron);
		seq.setName(IsoName);
		return seq.toStringAA();
	}
	/**
	 * 提取基因promoter附近的序列
	 * @param IsoName 基因名
	 * @param upBp tss上游多少bp，负数，如果正数就在下游
	 * @param downBp tss下游多少bp，正数，如果负数就在上游
	 * @return
	 */
	public SeqFasta getPromoter(String IsoName, int upBp, int downBp) {
		GffGeneIsoInfo gffGeneIsoInfo = null;
		gffGeneIsoInfo = gffChrAbs.getGffHashGene().searchISO(IsoName);
		int TssSite = gffGeneIsoInfo.getTSSsite();
		int startlocation = 0; int endlocation = 0;
		if (gffGeneIsoInfo.isCis5to3()) {
			startlocation = TssSite + upBp;
			endlocation = TssSite + downBp;
		}
		else {
			startlocation = TssSite - upBp;
			endlocation = TssSite - downBp;
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
	public ArrayList<SeqFasta> getGenomePromoterSeq(int upBp, int downBp) {
		ArrayList<SeqFasta> lsResult = new ArrayList<SeqFasta>();
		ArrayList<String> lsID = gffChrAbs.getGffHashGene().getLsNameNoRedundent();
		for (String geneID : lsID) {
			SeqFasta seqFasta = getPromoter(geneID, upBp, downBp);
			if (seqFasta == null) {
				logger.error("没有提取到序列"+geneID);
				continue;
			}
			lsResult.add(seqFasta);
		}
		return lsResult;
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
				ArrayList<ExonInfo> lsCDS = gffGeneIsoInfo.get3UTRseq();
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
