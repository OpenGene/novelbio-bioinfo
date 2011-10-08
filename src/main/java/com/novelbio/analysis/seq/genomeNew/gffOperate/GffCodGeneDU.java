package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.novelbio.analysis.annotation.copeID.CopedID;

public class GffCodGeneDU extends GffCodAbsDu<GffDetailGene, GffCodGene>{

	public GffCodGeneDU(ArrayList<GffDetailGene> lsgffDetail,
			GffCodGene gffCod1, GffCodGene gffCod2) {
		super(lsgffDetail, gffCod1, gffCod2);
	}
	
	/**
	 * ���������˵��У��漰��Tss�Ļ���ȫ����ȡ����
	 * @return
	 */
	public Set<GffDetailGene> getTSSGene() {
		/**
		 * �����صĻ���
		 */
		Set<GffDetailGene> setGffDetailGenes = new HashSet<GffDetailGene>();
		//��ǰ�������ת¼����Χ��
		if (gffCod1 != null)
		{
			//��һ�������й�ϵ
			if (isUpTss(gffCod1.getGffDetailUp())) {
				setGffDetailGenes.add(gffCod1.getGffDetailUp());
			}
			//�������ϵ
			if (isUpTss(gffCod1.getGffDetailThis())) {
				setGffDetailGenes.add(gffCod1.getGffDetailThis());
			}
		}
		
		for (GffDetailGene gffDetailGene : lsgffDetailsMid) {
			setGffDetailGenes.add(gffDetailGene);
		}
		
		if (gffCod2 != null)
		{
			//��һ�������й�ϵ
			if (isDownTss(gffCod2.getGffDetailThis()))
			{
				setGffDetailGenes.add(gffCod2.getGffDetailThis());
			}
			//�������ϵ
			if (isDownTss(gffCod2.getGffDetailDown())) {
				setGffDetailGenes.add(gffCod2.getGffDetailDown());
			}
		}
		return setGffDetailGenes;
	}
	
	private boolean isUpTss(GffDetailGene gffDetailGene) {
		if (gffDetailGene == null || !gffDetailGene.isCodInGenExtend()) {
			return false;
		}
		if (gffDetailGene.getLongestSplit().isCis5to3() && gffDetailGene.getLongestSplit().isCodInIsoTss()
		||
		(!gffDetailGene.getLongestSplit().isCis5to3() && gffDetailGene.getLongestSplit().isCodInIsoExtend())
		)
		{
			return true;
		}
		return false;
	}
	
	private boolean isDownTss(GffDetailGene gffDetailGene) {
		if (gffDetailGene == null || !gffDetailGene.isCodInGenExtend()) {
			return false;
		}
		if (!gffDetailGene.getLongestSplit().isCis5to3() && gffDetailGene.getLongestSplit().isCodInIsoTss()
		||
		(gffDetailGene.getLongestSplit().isCis5to3() && gffDetailGene.getLongestSplit().isCodInIsoExtend())
		)
		{
			return true;
		}
		return false;
	}
	/**
	 * ���������˵��У��漰��Tes�Ļ���ȫ����ȡ����
	 * @return
	 */
	public Set<GffDetailGene> getTESGene() {
		/**
		 * �����صĻ���
		 */
		Set<GffDetailGene> setGffDetailGenes = new HashSet<GffDetailGene>();
		//��ǰ�������ת¼����Χ��
		if (gffCod1 != null)
		{
			//��һ�������й�ϵ
			if (isUpTes(gffCod1.getGffDetailUp())) {
				setGffDetailGenes.add(gffCod1.getGffDetailUp());
			}
			//�������ϵ
			if (isUpTes(gffCod1.getGffDetailThis())) {
				setGffDetailGenes.add(gffCod1.getGffDetailThis());
			}
		}
		
		for (GffDetailGene gffDetailGene : lsgffDetailsMid) {
			setGffDetailGenes.add(gffDetailGene);
		}
		
		if (gffCod2 != null)
		{
			//��һ�������й�ϵ
			if (isDownTes(gffCod2.getGffDetailThis())) {
				setGffDetailGenes.add(gffCod2.getGffDetailThis());
			}
			//�������ϵ
			if (isDownTes(gffCod2.getGffDetailDown())) {
				setGffDetailGenes.add(gffCod2.getGffDetailDown());
			}
		}
		return setGffDetailGenes;
	}
	
	private boolean isUpTes(GffDetailGene gffDetailGene) {
		if (gffDetailGene == null || !gffDetailGene.isCodInGenExtend()) {
			return false;
		}
		if (gffDetailGene.getLongestSplit().isCis5to3() && gffDetailGene.getLongestSplit().isCodInIsoExtend()
		||
		(!gffDetailGene.getLongestSplit().isCis5to3() && gffDetailGene.getLongestSplit().isCodInIsoGenEnd() )
		)
		{
			return true;
		}
		return false;
	}
	
	private boolean isDownTes(GffDetailGene gffDetailGene) {
		if (gffDetailGene == null || !gffDetailGene.isCodInGenExtend()) {
			return false;
		}
		if (!gffDetailGene.getLongestSplit().isCis5to3() && gffDetailGene.getLongestSplit().isCodInIsoExtend()
		||
		(gffDetailGene.getLongestSplit().isCis5to3() && gffDetailGene.getLongestSplit().isCodInIsoGenEnd())
		)
		{
			return true;
		}
		return false;
	}
	/**
	 * �������и��ǵ��Ļ����copedID
	 * @return
	 */
	public ArrayList<CopedID> getAllCoveredGenes() {
		ArrayList<GffDetailGene> lsGenes = getAllGffDetail();
		//����ȥ�����
		HashSet<CopedID> hashCopedID = new HashSet<CopedID>();
		ArrayList<CopedID> lsCopedIDs = new ArrayList<CopedID>();
		for (GffDetailGene gffDetailGene : lsGenes) {
			for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
				//���Ƿ����������ڸû����ڲ�
				if (gffGeneIsoInfo.getCodLoc() != GffGeneIsoInfo.COD_LOC_OUT) {
					CopedID copedID = new CopedID(gffGeneIsoInfo.getIsoName(), gffDetailGene.getTaxID(), false);
					if (hashCopedID.contains(copedID)) {
						continue;
					}
					hashCopedID.add(copedID);
					lsCopedIDs.add(copedID);
				}
			}
		}
		return lsCopedIDs;
	}
	
}
