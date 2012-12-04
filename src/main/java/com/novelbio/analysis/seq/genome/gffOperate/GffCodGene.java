package com.novelbio.analysis.seq.genome.gffOperate;

import com.novelbio.base.dataStructure.listOperate.ListCodAbs;

/**
 * UCSC konwn gene�Ļ���������Ϣ
 * @author zong0jie
 *
 */
public class GffCodGene extends ListCodAbs<GffDetailGene> {
	/**
	 * ��ԭʼ��ListCodAbs���ɱ���
	 * @param lsSuper
	 */
	public GffCodGene(String chrID, int Coordinate) {
		super(chrID, Coordinate);
	}
	/**
	 * ���cod��exon�����iso��Ϣ��û���򷵻�null�����Ȳ����ת¼������Ϣ
	 * ���ص�һ���ҵ���iso��Ϣ
	 */
	public GffGeneIsoInfo getCodInCDSIso() {
		GffGeneIsoInfo gffGeneIsoInfoTmp = null;
		GffGeneIsoInfo gffGeneIsoInfo;
		if (isInsideLoc()) {
			gffGeneIsoInfo = getCodInCDS(getGffDetailThis(), super.getCoord());
			if (  gffGeneIsoInfo != null ) {
				if (gffGeneIsoInfo.ismRNA() && 
					gffGeneIsoInfo.getCodLocUTRCDS(super.getCoord()) == GffGeneIsoInfo.COD_LOCUTR_CDS
					) {
					return gffGeneIsoInfo;
				}
				else {
					gffGeneIsoInfoTmp = gffGeneIsoInfo;
				}
			}
		}
		if (isInsideUp()) {
			gffGeneIsoInfo = getCodInCDS(getGffDetailUp(), super.getCoord());
			if (  gffGeneIsoInfo != null && gffGeneIsoInfo.ismRNA() 
					&& gffGeneIsoInfo.getCodLocUTRCDS(super.getCoord()) == GffGeneIsoInfo.COD_LOCUTR_CDS) {
				return gffGeneIsoInfo;
			}
		}
		if (isInsideDown()) {
			gffGeneIsoInfo = getCodInCDS(getGffDetailDown(), super.getCoord());
			if (  gffGeneIsoInfo != null && gffGeneIsoInfo.ismRNA()
					&& gffGeneIsoInfo.getCodLocUTRCDS(super.getCoord()) == GffGeneIsoInfo.COD_LOCUTR_CDS) {
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
	private GffGeneIsoInfo getCodInCDS(GffDetailGene gffDetailGene, int coord) {
		//�����ת¼������snp�Ƿ��ڸ�ת¼����exon�У����ڵĻ�������������ת¼��,���Ƿ��ڻ���ı������
		GffGeneIsoInfo gffGeneIsoInfo = gffDetailGene.getLongestSplitMrna();
		//����ת¼������CDS����ֱ�ӷ���
		if (gffGeneIsoInfo.getCodLocUTRCDS(coord) == GffGeneIsoInfo.COD_LOCUTR_CDS) {
			return gffGeneIsoInfo;
		}
		//����ת¼����exon�У�������CDS������������ת¼�����ҵ���CDS�м��
		else if (gffGeneIsoInfo.getCodLoc(coord) == GffGeneIsoInfo.COD_LOC_EXON) {
			for (GffGeneIsoInfo gffGeneIsoInfo2 : gffDetailGene.getLsCodSplit()) {
				if (gffGeneIsoInfo2.ismRNA() && gffGeneIsoInfo2.getCodLocUTRCDS(coord) == GffGeneIsoInfo.COD_LOCUTR_CDS)  {
					gffGeneIsoInfo = gffGeneIsoInfo2;
					break;
				}
			}
		}
		//����ת¼������exon�У���������ת¼���������CDS�о�ֱ�ӷ��أ��������CDS�У��ͷ�����Exon�е�
		else {
			for (GffGeneIsoInfo gffGeneIsoInfo2 : gffDetailGene.getLsCodSplit()) {
				if (gffGeneIsoInfo2.ismRNA() && gffGeneIsoInfo2.getCodLocUTRCDS(coord) == GffGeneIsoInfo.COD_LOCUTR_CDS)  {
					gffGeneIsoInfo = gffGeneIsoInfo2;
					break;
				} else if (gffGeneIsoInfo2.ismRNA() && gffGeneIsoInfo2.getCodLoc(coord) == GffGeneIsoInfo.COD_LOC_EXON) {
					gffGeneIsoInfo = gffGeneIsoInfo2;
				}
			}
		}
		//�ҵ���
		if (gffGeneIsoInfo.getCodLoc(coord) == GffGeneIsoInfo.COD_LOC_EXON) {
			return gffGeneIsoInfo;
		}
		return null;
	}
}
