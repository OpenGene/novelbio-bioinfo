package com.novelbio.analysis.seq.genomeNew;

import java.util.ArrayList;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.AminoAcid;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffCodGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapInfo;

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
//		ArrayList<String[]> lsChrFile = FileOperate.getFoldFileName("/media/winE/Bioinformatics/GenomeData/checken/chromFa",
//				regx, "*");
		
//		GffChrSeq gffChrSeq = new GffChrSeq(NovelBioConst.GENOME_GFF_TYPE_UCSC, 
//				"/media/winE/Bioinformatics/GenomeData/checken/GeneLoc/chickenEnsemblGenes",
//				"/media/winE/Bioinformatics/GenomeData/checken/chromFa");
		
		GffChrSeq gffChrSeq = new GffChrSeq(NovelBioConst.GENOME_GFF_TYPE_UCSC, 
		NovelBioConst.GENOME_PATH_UCSC_HG19_GFF_REFSEQ,
		NovelBioConst.GENOME_PATH_UCSC_HG19_CHROM);
		MapInfo mapInfo = new MapInfo("chr1");
		mapInfo.setFlagLoc(67391831);
		gffChrSeq.getAAsnp(mapInfo);
		System.out.println(mapInfo.getStart() + "  " + mapInfo.getEnd());
		System.out.println(mapInfo.getNrSeq());
		System.out.println(mapInfo.getAaSeq());
		System.out.println(mapInfo.getTitle());
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
	 * 给定mapInfo，其中mapInfo的flagLoc为snp坐标位点，chrID为染色体位置
	 * 获得该snp所在的三个碱基，以及所对应的氨基酸
	 * startLoc为起点碱基坐标，endLoc为终点碱基坐标
	 * 默认搜索最长转录本
	 * 将具体的序列信息填充mapInfo
	 * 返回该snp的定位信息
	 */
	public GffCodGene getAAsnp(MapInfo mapInfo) {
		GffCodGene gffCodeGene = gffHashGene.searchLocation(mapInfo.getChrID(), mapInfo.getFlagSite());
		if (gffCodeGene.isInsideLoc()) {
			//先找最长转录本，看snp是否在该转录本的exon中，不在的话，找其他所有转录本,看是否在基因的表达区中
			GffGeneIsoInfo gffGeneIsoInfo = gffCodeGene.getGffDetailThis() .getLongestSplit();
			if (gffGeneIsoInfo.getCodLoc() != GffGeneIsoInfo.COD_LOC_EXON
					|| gffGeneIsoInfo.getCod2ATGmRNA() < 0 
					|| gffGeneIsoInfo.getCod2UAG() > 0 ) {
				for (GffGeneIsoInfo gffGeneIsoInfo2 : gffCodeGene.getGffDetailThis().getLsCodSplit()) {
					if (gffGeneIsoInfo2.getCodLoc() == GffGeneIsoInfo.COD_LOC_EXON 
							&& gffGeneIsoInfo2.getCod2ATGmRNA() >= 0 
							&& gffGeneIsoInfo2.getCod2UAG() <= 0)  {
						gffGeneIsoInfo = gffGeneIsoInfo2;
						break;
					}
				}
			}
			//找到了
			if (gffGeneIsoInfo.getCodLoc() == GffGeneIsoInfo.COD_LOC_EXON) {
				int startLen = gffGeneIsoInfo.getCod2ATGmRNA();
				int endLen = gffGeneIsoInfo.getCod2UAG();
				// 确定在外显子中
				if (startLen >= 0 && endLen <= 0) {
					int LocStart = gffGeneIsoInfo.getLocDistmRNASite(mapInfo.getFlagSite(), -startLen%3);
					int LocEnd = gffGeneIsoInfo.getLocDistmRNASite(mapInfo.getFlagSite(), 2 - startLen%3);
					ArrayList<int[]> lsTmp = gffGeneIsoInfo.getRangeIso(LocStart, LocEnd);
					String NR = seqHash.getSeq(mapInfo.getChrID(), lsTmp, false);
					
//					System.out.println(seqHash.getSeq(mapInfo.getChrID(),  gffGeneIsoInfo.getRangeIso(LocStart-4, LocEnd+4), false));
					mapInfo.setNrSeq(NR);
					mapInfo.setAaSeq(AminoAcid.convertDNA2AA(NR, false));
					mapInfo.setStartLoc(LocStart);
					mapInfo.setEndLoc(LocEnd);
					mapInfo.setTitle(gffGeneIsoInfo.getIsoName());
				}
			}
		}
		return gffCodeGene;
	}
	
}
