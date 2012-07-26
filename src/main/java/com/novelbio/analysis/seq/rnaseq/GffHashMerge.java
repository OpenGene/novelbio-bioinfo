package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;

import com.novelbio.analysis.seq.genomeNew.gffOperate.GffCodGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.ListGff;
import com.novelbio.base.dataStructure.listOperate.ListAbs;
import com.novelbio.generalConf.NovelBioConst;

public class GffHashMerge {
	public static void main(String[] args) {
		String gffHashGeneRef = "/media/winF/NBC/Project/Project_FY/chicken/chicken_ensembl_Gtf";
		String gffhashGeneCuf = "/media/winF/NBC/Project/Project_FY/chicken/Result/cufflinkAll/cufflink/transcripts.gtf";
		GffHashMerge gffHashMerge = new GffHashMerge();
		
		gffHashMerge.setGffHashGeneRef(new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF, gffHashGeneRef));
		gffHashMerge.addGffHashGene(new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF, gffhashGeneCuf));

		GffHashGene gffHashGeneResult = gffHashMerge.getGffHashGeneResult();
		gffHashGeneResult.removeDuplicateIso();
		gffHashGeneResult.writeToGTF("/media/winF/NBC/Project/Project_FY/chicken/Result/cufflinkAll/cufflink/test2.gtf", "novelbio");
		
	}
	GffHashGene gffHashGeneRef = new GffHashGene();
	ArrayList<GffHashGene> lsGffHashGenes = new ArrayList<GffHashGene>();
	GffHashGene gffHashGeneResult = new GffHashGene();
	boolean calculate = false;
	
	public void setGffHashGeneRef(GffHashGene gffHashGeneRef) {
		this.gffHashGeneRef = gffHashGeneRef;
		lsGffHashGenes.add(0, gffHashGeneRef);
		calculate = false;
	}
	/**
	 * ���Gff��Ϣ
	 * @param gffHashGene
	 */
	public void addGffHashGene(GffHashGene gffHashGene) {
		lsGffHashGenes.add(gffHashGene);
		calculate = false;
	}
	public GffHashGene getGffHashGeneResult() {
		if (!calculate) {
			fillGffHashGeneResult();
		}
		return gffHashGeneResult;
	}
	/**
	 * ��Gff��Ϣװ��mapChrID2LsGffGeneCluster
	 */
	public void fillGffHashGeneResult() {
		if (calculate) {
			return;
		}
		calculate = true;
		
		ArrayList<String> lsChrID = lsGffHashGenes.get(0).getLsChrID();
		for (String chrID : lsChrID) {
			ArrayList<ListGff> lsGffAll = new ArrayList<ListGff>();
			for (GffHashGene gffHashGene : lsGffHashGenes) {
				ListGff listGff = gffHashGene.getMapChrID2LsGff().get(chrID);
				if (listGff == null) {
					continue;
				}
				lsGffAll.add(listGff);
			}
			ArrayList<int[]> lsGeneBound = ListAbs.getCombSep(null, lsGffAll);
			ArrayList<GffGeneCluster> lsGff = getListGeneCluster(chrID, lsGeneBound, lsGffHashGenes);
			addChrIDlist(lsGff);
		}
	}
	/**
	 * ��ĳһ��Ⱦɫ�������gffhashgene����Ϣ���ջ��ֵ�����װ��mapChrID2LsGffGeneCluster
	 * @param chrID
	 * @param lsGeneBount
	 * @param lsGffHashGenes
	 */
	private ArrayList<GffGeneCluster> getListGeneCluster(String chrID, ArrayList<int[]> lsGeneBound, ArrayList<GffHashGene> lsGffHashGenes) {
		ArrayList<GffGeneCluster> lsGffGeneClusters = new ArrayList<GffGeneCluster>();
		for (int[] geneBound : lsGeneBound) {
			GffGeneCluster gffGeneCluster = new GffGeneCluster();
			for (int i = 0; i < lsGffHashGenes.size(); i++) {
				GffHashGene gffHashGene = lsGffHashGenes.get(i);
				GffCodGene gffCodGeneStart = gffHashGene.searchLocation(chrID, geneBound[0]);
				int startID = getGffGeneNum(true, gffCodGeneStart);
				
				GffCodGene gffCodGeneEnd = gffHashGene.searchLocation(chrID, geneBound[1]);
				int endID = getGffGeneNum(false, gffCodGeneEnd);
				//�������յ�˵���ڸ�λ���������棬��GffHashGene��û���ҵ�����
				if (startID > endID || startID < 0 || endID < 0) {
					if (i == 0) {
						gffGeneCluster.setIsContainsRef(false);
					}
					continue;
				}
				addGffGene_Into_GffCluster(gffGeneCluster, gffHashGene, chrID, startID, endID);
			}
			lsGffGeneClusters.add(gffGeneCluster);
		}
		return lsGffGeneClusters;
	}
	
	private int getGffGeneNum(boolean start, GffCodGene gffCodGene) {
		if (gffCodGene == null) {
			return -1;
		}
		if (gffCodGene.isInsideLoc()) {
			return gffCodGene.getItemNumThis();
		}
		//ͷ����λ��Ϳ�����һ��ID��β����λ��Ϳ�ǰ��һ��ID
		if (start) {
			return gffCodGene.getItemNumDown();
		}
		else {
			return gffCodGene.getItemNumUp();
		}
	}
	/**
	 * ��ָ����Χ�ڵ�gffGeneװ��cluster��
	 * @param gffHashGeneBed
	 * @param chrID
	 * @param startLoc ��0��ʼ
	 * @param endLoc ��0��ʼ
	 */
	private void addGffGene_Into_GffCluster(GffGeneCluster gffGeneCluster, GffHashGene gffHashGene, String chrID, int startID, int endID) {
		ListGff lsGff = gffHashGene.getMapChrID2LsGff().get(chrID);
		ArrayList<GffDetailGene> lsGffSubGene = new ArrayList<GffDetailGene>();
		for (int i = startID; i <= endID; i++) {
			lsGffSubGene.add(lsGff.get(i));
		}
		if (lsGffSubGene.size() == 0) {
			return;
		}
		gffGeneCluster.addLsGffDetailGene(gffHashGene.getGffFilename(), lsGffSubGene);
	}
	
	private void addChrIDlist( ArrayList<GffGeneCluster> lsGeneCluster) {
		ListGff listGff = new ListGff();
		for (GffGeneCluster gffGeneCluster : lsGeneCluster) {
			ArrayList<GffDetailGene> lsGene = gffGeneCluster.getCombinedGffGene();
			listGff.addAll(lsGene);
		}
		gffHashGeneResult.addListGff(listGff);
	}
}
