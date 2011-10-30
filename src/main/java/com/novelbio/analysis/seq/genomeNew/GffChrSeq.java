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
	 * ��������������ø�ת¼������Ϣ
	 * ����GffGeneIsoInfoת¼��������������Զ���ȡ����ڻ���ת¼���������
	 * @param IsoName ת¼��������
	 * @param absIso �Ƿ��Ǹ�ת¼����false��ѡ��û������µ��ת¼��
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
	 * ��������������ø�ת¼������Ϣ
	 * ����ת¼���ķ������Ǵӻ������5����3����ȡ�� ������Ҫ�˹��趨cisseq
	 * @param cisseq ��������Ƿ������У�����ڻ�������˵��
	 * @param IsoName ת¼��������
	 * @param absIso �Ƿ��Ǹ�ת¼����false��ѡ��û������µ��ת¼��
	 * @param getIntron �Ƿ����ں���
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
	 * ����mapInfo������mapInfo��flagLocΪsnp����λ�㣬chrIDΪȾɫ��λ��
	 * ��ø�snp���ڵ�����������Լ�����Ӧ�İ�����
	 * startLocΪ��������꣬endLocΪ�յ�������
	 * Ĭ�������ת¼��
	 * �������������Ϣ���mapInfo
	 * ���ظ�snp�Ķ�λ��Ϣ
	 */
	public GffCodGene getAAsnp(MapInfo mapInfo) {
		GffCodGene gffCodeGene = gffHashGene.searchLocation(mapInfo.getChrID(), mapInfo.getFlagSite());
		if (gffCodeGene.isInsideLoc()) {
			//�����ת¼������snp�Ƿ��ڸ�ת¼����exon�У����ڵĻ�������������ת¼��,���Ƿ��ڻ���ı������
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
			//�ҵ���
			if (gffGeneIsoInfo.getCodLoc() == GffGeneIsoInfo.COD_LOC_EXON) {
				int startLen = gffGeneIsoInfo.getCod2ATGmRNA();
				int endLen = gffGeneIsoInfo.getCod2UAG();
				// ȷ������������
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
