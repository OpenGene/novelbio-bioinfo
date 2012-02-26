package com.novelbio.analysis.seq.genomeNew;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genomeNew.getChrSequence.AminoAcid;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFasta;
import com.novelbio.analysis.seq.genomeNew.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffCodGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genomeNew.listOperate.ListAbs;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapInfo;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapInfoSnpIndel;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.generalConf.NovelBioConst;

public class GffChrSeq extends GffChrAbs{
	private static Logger logger = Logger.getLogger(GffChrSeq.class);
	public GffChrSeq(String gffType, String gffFile, String chrFile, String regx) {
		super(gffType, gffFile, chrFile, regx, null, 0);
		loadChrFile();
	}
	
	public GffChrSeq(String gffType, String gffFile, String chrFile) {
		this(gffType, gffFile, chrFile, null);
		loadChrFile();
	}

	public static void main(String[] args) {
//		GffChrSeq gffChrSeq = new GffChrSeq(NovelBioConst.GENOME_GFF_TYPE_UCSC,  
//				NovelBioConst.GENOME_PATH_UCSC_HG19_GFF_REFSEQ, NovelBioConst.GENOME_PATH_UCSC_HG19_CHROM);
		
		GffChrSeq gffChrSeq = new GffChrSeq(NovelBioConst.GENOME_GFF_TYPE_UCSC,  
				NovelBioConst.GENOME_PATH_UCSC_HG19_GFF_REFSEQ, NovelBioConst.GENOME_PATH_UCSC_HG19_CHROM);

	}
	
	/**
	 * 给定基因名，获得该转录本的信息
	 * 按照GffGeneIsoInfo转录本给定的情况，自动提取相对于基因转录方向的序列
	 * @param IsoName 转录本的名字
	 * @param absIso 是否是该转录本，false则选择该基因名下的最长转录本
	 * @return
	 */
	public SeqFasta getSeq(String IsoName, boolean absIso,boolean getIntron)
	{
		GffGeneIsoInfo gffGeneIsoInfo = null;
		if (absIso)
			gffGeneIsoInfo = gffHashGene.searchISO(IsoName);
		else
			gffGeneIsoInfo = gffHashGene.searchLOC(IsoName).getLongestSplit();
		
		return seqHash.getSeq(gffGeneIsoInfo.getChrID(), gffGeneIsoInfo, getIntron);
	}
	
	/**
	 * 给定坐标，提取序列
	 * @param IsoName
	 * @param absIso
	 * @param getIntron
	 * @return
	 */
	public SeqFasta getSeq(boolean cis5to3,String chrID, int startLoc, int endLoc)
	{
		return seqHash.getSeq(chrID, (long)startLoc, (long)endLoc);
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
	public SeqFasta getSeq(String IsoName, boolean cis5to3,int startExon, int endExon, boolean absIso,boolean getIntron)
	{
		GffGeneIsoInfo gffGeneIsoInfo = null;
		if (absIso)
			gffGeneIsoInfo = gffHashGene.searchISO(IsoName);
		else
			gffGeneIsoInfo = gffHashGene.searchLOC(IsoName).getLongestSplit();
		
		SeqFasta seqFasta = seqHash.getSeq(gffGeneIsoInfo.getChrID(), cis5to3, startExon, endExon, gffGeneIsoInfo, getIntron);
		seqFasta.setSeqName(IsoName);
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
	public SeqFasta getSeq(boolean cisseq, String IsoName, boolean absIso,boolean getIntron)
	{
		GffGeneIsoInfo gffGeneIsoInfo = null;
		if (absIso)
			gffGeneIsoInfo = gffHashGene.searchISO(IsoName);
		else
			gffGeneIsoInfo = gffHashGene.searchLOC(IsoName).getLongestSplit();
		
		SeqFasta seqFasta = seqHash.getSeq(cisseq, gffGeneIsoInfo.getChrID(), gffGeneIsoInfo, getIntron);
		seqFasta.setSeqName(IsoName);
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
	public SeqFasta getSeqCDS(String IsoName, boolean cis5to3, boolean absIso,boolean getIntron)
	{
		GffGeneIsoInfo gffGeneIsoInfo = null;
		if (absIso)
			gffGeneIsoInfo = gffHashGene.searchISO(IsoName);
		else
			gffGeneIsoInfo = gffHashGene.searchLOC(IsoName).getLongestSplit();
		
		SeqFasta seqFasta = seqHash.getSeq(gffGeneIsoInfo.getChrID(), cis5to3, 0, 0, gffGeneIsoInfo.getIsoInfoCDS(), getIntron);
		seqFasta.setSeqName(IsoName);
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
	public String getSeqProtein(String IsoName, boolean cis5to3, boolean absIso,boolean getIntron)
	{
		GffGeneIsoInfo gffGeneIsoInfo = null;
		if (absIso)
			gffGeneIsoInfo = gffHashGene.searchISO(IsoName);
		else
			gffGeneIsoInfo = gffHashGene.searchLOC(IsoName).getLongestSplit();
		
		SeqFasta seq = seqHash.getSeq(gffGeneIsoInfo.getChrID(), cis5to3, 0, 0, gffGeneIsoInfo.getIsoInfoCDS(), getIntron);
		seq.setSeqName(IsoName);
		return seq.toStringAA(true, 0);
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
		gffGeneIsoInfo = gffHashGene.searchISO(IsoName);
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
		SeqFasta seq = seqHash.getSeq(gffGeneIsoInfo.isCis5to3(), gffGeneIsoInfo.getChrID(), start, end);
		if (seq == null) {
			logger.error("没有提取到序列：" + " "+ gffGeneIsoInfo.getChrID() + " " + start + " " + end);
			return null;
		}
		seq.setSeqName(gffGeneIsoInfo.getIsoName());
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
		ArrayList<String> lsID = gffHashGene.getLOCChrHashIDList();
		for (String string : lsID) {
			String geneID = string.split(ListAbs.SEP)[0];
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
	 * 按照GffGeneIsoInfo转录本给定的情况，自动提取相对于基因转录方向的序列
	 * @param IsoName 转录本的名字
	 * @param cis5to3 正反向，在提出的正向转录本的基础上，是否需要反向互补
	 * @param startExon 具体某个exon
	 * @param endExon 具体某个Intron
	 * @param absIso 是否是该转录本，false则选择该基因名下的最长转录本
	 * @param getIntron
	 * @return
	 */
	public ArrayList<SeqFasta> getSeqCDSAll()
	{
		ArrayList<String> lsID = gffHashGene.getLOCChrHashIDList();
		ArrayList<SeqFasta> lsResult = new ArrayList<SeqFasta>();
		GffGeneIsoInfo gffGeneIsoInfo = null;
		for (String string : lsID) {
			gffGeneIsoInfo = gffHashGene.searchISO(string.split(ListAbs.SEP)[0]);
			ArrayList<ExonInfo> lsCDS = gffGeneIsoInfo.getIsoInfoCDS();
			if (lsCDS.size() > 0) {
				SeqFasta seq = seqHash.getSeq(gffGeneIsoInfo.getChrID(), lsCDS, false);
				if (seq == null || seq.length() < 3) {
					continue;
				}
				seq.setSeqName(string.split(ListAbs.SEP)[0]);
				lsResult.add(seq);
			}
		}
		return lsResult;
		
	}
}
