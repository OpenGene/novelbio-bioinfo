package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;

import com.novelbio.base.dataStructure.listOperate.ListCodAbs;

/**
 * UCSC konwn gene�Ļ���������Ϣ
 * @author zong0jie
 *
 */
public class GffCodGene extends ListCodAbs<GffDetailGene>
{
	/**
	 * ��ԭʼ��ListCodAbs���ɱ���
	 * @param lsSuper
	 */
	public GffCodGene(ListCodAbs<GffDetailGene> lsSuper) {
		super(lsSuper);
	}
	
	/**
	 * ���cod��exon�����iso��Ϣ��û���򷵻�null�����Ȳ����ת¼������Ϣ
	 * ���ص�һ���ҵ���iso��Ϣ
	 */
	public GffGeneIsoInfo getCodInExonIso() {
		GffGeneIsoInfo gffGeneIsoInfoTmp = null;
		GffGeneIsoInfo gffGeneIsoInfo;
		if (isInsideLoc()) {
			gffGeneIsoInfo = getCodInExon(getGffDetailThis(), super.getCoord());
			if (  gffGeneIsoInfo != null ) {
				if (gffGeneIsoInfo.ismRNA()) {
					return gffGeneIsoInfo;
				}
				else {
					gffGeneIsoInfoTmp = gffGeneIsoInfo;
				}
			}
		}
		if (isInsideUp()) {
			gffGeneIsoInfo = getCodInExon(getGffDetailUp(), super.getCoord());
			if (  gffGeneIsoInfo != null && gffGeneIsoInfo.ismRNA()) {
				return gffGeneIsoInfo;
			}
		}
		if (isInsideDown()) {
			gffGeneIsoInfo = getCodInExon(getGffDetailDown(), super.getCoord());
			if (  gffGeneIsoInfo != null && gffGeneIsoInfo.ismRNA()) {
				return gffGeneIsoInfo;
			}
		}
		
		
		if (gffGeneIsoInfoTmp != null) {
			return gffGeneIsoInfoTmp;
		}
		return null;
	}
	
	/**
	 * ����cod���������е�ת¼��
	 * ע�ⲻһ����exon�У�����������Ҫ�����ж�
	 * @param gffDetailGene
	 * @return
	 */
	private GffGeneIsoInfo getCodInExon(GffDetailGene gffDetailGene, int coord)
	{
		//�����ת¼������snp�Ƿ��ڸ�ת¼����exon�У����ڵĻ�������������ת¼��,���Ƿ��ڻ���ı������
		GffGeneIsoInfo gffGeneIsoInfo = gffDetailGene.getLongestSplit();
		if (!gffGeneIsoInfo.ismRNA() || gffGeneIsoInfo.getCodLoc(coord) != GffGeneIsoInfo.COD_LOC_EXON
				|| gffGeneIsoInfo.getCod2ATGmRNA(coord) < 0 
				|| gffGeneIsoInfo.getCod2UAG(coord) > 0 ) {
			for (GffGeneIsoInfo gffGeneIsoInfo2 : gffDetailGene.getLsCodSplit()) {
				if (gffGeneIsoInfo2.getCodLoc(coord) == GffGeneIsoInfo.COD_LOC_EXON 
						&& gffGeneIsoInfo2.getCod2ATGmRNA(coord) >= 0 
						&& gffGeneIsoInfo2.getCod2UAG(coord) <= 0)  {
					gffGeneIsoInfo = gffGeneIsoInfo2;
					break;
				}
			}
		}
		//�ҵ���
		if (gffGeneIsoInfo.getCodLoc(coord) == GffGeneIsoInfo.COD_LOC_EXON) {
			return gffGeneIsoInfo;
		}
		return null;
	}

	public static GffCodGene convert2Class(ListCodAbs<GffDetailGene> lsSuper)
	{
		return new GffCodGene(lsSuper);
	}
	
}
