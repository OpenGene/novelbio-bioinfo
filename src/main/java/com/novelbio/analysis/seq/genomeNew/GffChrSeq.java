package com.novelbio.analysis.seq.genomeNew;

import java.util.ArrayList;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.AminoAcid;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFasta;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffCodGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapInfo;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapInfoSnpIndel;

public class GffChrSeq extends GffChrAbs{

	public GffChrSeq(String gffType, String gffFile, String chrFile, String regx) {
		super(gffType, gffFile, chrFile, regx, null, 0);
		loadChrFile();
	}
	
	public GffChrSeq(String gffType, String gffFile, String chrFile) {
		this(gffType, gffFile, chrFile, null);
		loadChrFile();
	}

	public static void main(String[] args) {
		GffChrSeq gffChrSeq = new GffChrSeq(NovelBioConst.GENOME_GFF_TYPE_UCSC,  
				NovelBioConst.GENOME_PATH_UCSC_HG19_GFF_REFSEQ, NovelBioConst.GENOME_PATH_UCSC_HG19_CHROM);
//		gffChrSeq.setGffFile(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF, "/media/winE/NBC/Project/Project_FY_Lab/Result/cufflinkAll/cufcompare/cmpAll.combined_cope.gtf");
		gffChrSeq.loadChrFile();
		String seq = gffChrSeq.getSeqCDS("NM_004195", true, true, false);
		SeqFasta seqFasta = new SeqFasta("NM_004195", seq);
		seqFasta.toAAseq(true, 0);
		System.out.println(seqFasta.toAAseq(true, 0));
	}
	
	/**
	 * 给定基因名，获得该转录本的信息
	 * 按照GffGeneIsoInfo转录本给定的情况，自动提取相对于基因转录方向的序列
	 * @param IsoName 转录本的名字
	 * @param absIso 是否是该转录本，false则选择该基因名下的最长转录本
	 * @return
	 */
	public String getSeq(String IsoName, boolean absIso,boolean getIntron)
	{
		GffGeneIsoInfo gffGeneIsoInfo = null;
		if (absIso)
			gffGeneIsoInfo = gffHashGene.searchISO(IsoName);
		else
			gffGeneIsoInfo = gffHashGene.searchLOC(IsoName).getLongestSplit();
		
		return seqHash.getSeq(gffGeneIsoInfo.getChrID(), gffGeneIsoInfo.getIsoInfo(), getIntron);
	}
	
	/**
	 * 给定坐标，提取序列
	 * @param IsoName
	 * @param absIso
	 * @param getIntron
	 * @return
	 */
	public String getSeq(boolean cis5to3,String chrID, int startLoc, int endLoc)
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
	public String getSeq(String IsoName, boolean cis5to3,int startExon, int endExon, boolean absIso,boolean getIntron)
	{
		GffGeneIsoInfo gffGeneIsoInfo = null;
		if (absIso)
			gffGeneIsoInfo = gffHashGene.searchISO(IsoName);
		else
			gffGeneIsoInfo = gffHashGene.searchLOC(IsoName).getLongestSplit();
		
		return seqHash.getSeq(gffGeneIsoInfo.getChrID(), cis5to3, startExon, endExon, gffGeneIsoInfo.getIsoInfo(), getIntron);
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
	public String getSeq(boolean cisseq, String IsoName, boolean absIso,boolean getIntron)
	{
		GffGeneIsoInfo gffGeneIsoInfo = null;
		if (absIso)
			gffGeneIsoInfo = gffHashGene.searchISO(IsoName);
		else
			gffGeneIsoInfo = gffHashGene.searchLOC(IsoName).getLongestSplit();
		
		return seqHash.getSeq(cisseq, gffGeneIsoInfo.getChrID(), gffGeneIsoInfo.getIsoInfo(), getIntron);
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
	public String getSeqCDS(String IsoName, boolean cis5to3, boolean absIso,boolean getIntron)
	{
		GffGeneIsoInfo gffGeneIsoInfo = null;
		if (absIso)
			gffGeneIsoInfo = gffHashGene.searchISO(IsoName);
		else
			gffGeneIsoInfo = gffHashGene.searchLOC(IsoName).getLongestSplit();
		
		return seqHash.getSeq(gffGeneIsoInfo.getChrID(), cis5to3, 0, 0, gffGeneIsoInfo.getIsoInfoCDS(), getIntron);
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
		
		return seqHash.getSeq(gffGeneIsoInfo.getChrID(), cis5to3, 0, 0, gffGeneIsoInfo.getIsoInfoCDS(), getIntron);
	}
	
	
	
}
