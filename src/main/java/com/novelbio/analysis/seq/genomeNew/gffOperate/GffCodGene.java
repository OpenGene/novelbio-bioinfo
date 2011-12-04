package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;

import com.novelbio.analysis.seq.genomeNew.getChrSequence.AminoAcid;
 



/**
 * UCSC konwn gene�Ļ���������Ϣ
 * @author zong0jie
 *
 */
public class GffCodGene extends GffCodAbs<GffDetailGene>
{
	protected GffCodGene(String chrID, int Coordinate) {
		super(chrID, Coordinate);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * ���cod��exon�����iso��Ϣ��û���򷵻�null�����Ȳ����ת¼������Ϣ
	 * ���ص�һ���ҵ���iso��Ϣ
	 */
	public GffGeneIsoInfo getCodInExonIso() {
		GffGeneIsoInfo gffGeneIsoInfo;
		if (isInsideLoc()) {
			gffGeneIsoInfo = getCodInExon(getGffDetailThis());
			if (  gffGeneIsoInfo != null) {
				return gffGeneIsoInfo;
			}
		}
		if (isInsideUp()) {
			gffGeneIsoInfo = getCodInExon(getGffDetailUp());
			if (  gffGeneIsoInfo != null) {
				return gffGeneIsoInfo;
			}
		}
		if (isInsideDown()) {
			gffGeneIsoInfo = getCodInExon(getGffDetailDown());
			if (  gffGeneIsoInfo != null) {
				return gffGeneIsoInfo;
			}
		}
		return null;
	}
	
	
	private GffGeneIsoInfo getCodInExon(GffDetailGene gffDetailGene)
	{
		//�����ת¼������snp�Ƿ��ڸ�ת¼����exon�У����ڵĻ�������������ת¼��,���Ƿ��ڻ���ı������
		GffGeneIsoInfo gffGeneIsoInfo = gffDetailGene.getLongestSplit();
		if (gffGeneIsoInfo.getCodLoc() != GffGeneIsoInfo.COD_LOC_EXON
				|| gffGeneIsoInfo.getCod2ATGmRNA() < 0 
				|| gffGeneIsoInfo.getCod2UAG() > 0 ) {
			for (GffGeneIsoInfo gffGeneIsoInfo2 : gffDetailGene.getLsCodSplit()) {
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
				return gffGeneIsoInfo;
			}
		}
		return null;
	}
	/**
	 * ���ظ�gffDetailGene�ľ�����Ϣ
	 * string[3]
	 * @param gffDetailGene
	 */
	private static void copeGffDetailGene(GffDetailGene gffDetailGene) {
		if (gffDetailGene.isCodInGenExtend()) {
			ArrayList<GffGeneIsoInfo> lsGffGeneIsoInfo = gffDetailGene.getLsCodSplit();
			for (GffGeneIsoInfo gffGeneIsoInfo : lsGffGeneIsoInfo) {
				if (gffGeneIsoInfo.isCodInIsoExtend()) {
					gffGeneIsoInfo.getCodLocUTR();
				}
			}
		}
	}
	
	
}
