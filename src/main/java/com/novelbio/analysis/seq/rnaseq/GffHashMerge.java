package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.HashMap;

import com.novelbio.analysis.seq.fasta.SeqFastaHash;
import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.analysis.seq.genome.gffOperate.GffCodGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genome.gffOperate.ListGff;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.listOperate.ListAbs;
import com.novelbio.database.model.species.Species;
import com.novelbio.generalConf.NovelBioConst;
/** �ؽ�ת¼�� */
public class GffHashMerge {
	public static void main(String[] args) {
//		mouse();
//		checken();
	}
	public static void mouse() {
		String gffhashGeneCuf = "/media/winF/NBC/Project/Project_FY/FYmouse20111122/tophata15m1/novelbioTranscriptome/transcripts.gtf";
		String gffFinal = "/media/winF/NBC/Project/Project_FY/FYmouse20111122/tophata15m1/novelbioTranscriptome/finalTranscript.gtf";
		String gffFinalStatistics = "/media/winF/NBC/Project/Project_FY/FYmouse20111122/tophata15m1/novelbioTranscriptome/transcriptomeStatistics.txt";
		Species species = new Species(10090);
		GffHashMerge gffHashMerge = new GffHashMerge();
		gffHashMerge.setSpecies(species);
		gffHashMerge.setGffHashGeneRef(new GffHashGene(species.getGffFileType(), species.getGffFile()));
		gffHashMerge.addGffHashGene(new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF, gffhashGeneCuf));
		GffHashGene gffHashGene = gffHashMerge.getGffHashGeneModifyResult();
		gffHashGene.removeDuplicateIso();
		gffHashGene.writeToGTF(gffFinal, "novelbio");

		gffHashMerge = new GffHashMerge();
		gffHashMerge.setSpecies(species);
		gffHashMerge.setGffHashGeneRef(new GffHashGene(species.getGffFileType(), species.getGffFile()));
		gffHashMerge.addGffHashGene(new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF, gffFinal));

		TranscriptomStatistics transcriptomStatistics = gffHashMerge.getStatisticsCompareGff();
		TxtReadandWrite txtOut = new TxtReadandWrite(gffFinalStatistics, true);

		txtOut.ExcelWrite(transcriptomStatistics.getStatisticsResult());
	}
	
	public static void checken() {
		String gffHashGeneRef = "/media/winF/NBC/Project/Project_FY/chicken/chicken_ensembl_Gtf";
		String gffhashGeneCuf = "/media/winF/NBC/Project/Project_FY/chicken/Result/cufflinkAll/cufflink/transcripts.gtf";
		String gffFinal = "/media/winF/NBC/Project/Project_FY/chicken/Result/cufflinkAll/cufflink/finalTranscript.gtf";
		String gffFinalStatistics = "/media/winF/NBC/Project/Project_FY/chicken/Result/cufflinkAll/cufflink/transcriptomeStatistics.txt";
		
		GffHashMerge gffHashMerge = new GffHashMerge();
		gffHashMerge.setSpecies(new Species(9013));
		gffHashMerge.setGffHashGeneRef(new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF, gffHashGeneRef));
		gffHashMerge.addGffHashGene(new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF, gffhashGeneCuf));
		
		GffHashGene gffHashGene = gffHashMerge.getGffHashGeneModifyResult();
		gffHashGene.removeDuplicateIso();
		gffHashGene.writeToGTF(gffFinal, "novelbio");
		
		gffHashMerge = new GffHashMerge();
		gffHashMerge.setSpecies(new Species(9013));
		gffHashMerge.setGffHashGeneRef(new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF, gffHashGeneRef));
		gffHashMerge.addGffHashGene(new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF, gffFinal));
		
		TranscriptomStatistics transcriptomStatistics = gffHashMerge.getStatisticsCompareGff();
		TxtReadandWrite txtOut = new TxtReadandWrite(gffFinalStatistics, true);
		txtOut.ExcelWrite(transcriptomStatistics.getStatisticsResult());
	}
	GffHashGene gffHashGeneRef = new GffHashGene();
	ArrayList<GffHashGene> lsGffHashGenes = new ArrayList<GffHashGene>();
	/** keyСд */
	HashMap<String, ArrayList<GffGeneCluster>> mapChrID2LsGffCluster = new HashMap<String, ArrayList<GffGeneCluster>>();
	
	GffHashGene gffHashGeneResult = new GffHashGene();
	/**ͳ��ת¼����Ϣʱ�õ� */
	Species species;
	/**�µ�ת¼���������С��1000������û���ں��ӣ����п����Ǽٻ��򣬾�ɾ�� */
	int minGeneLen = 1000;
	
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
	public void setSpecies(Species species) {
		this.species = species;
	}
	/** �ڲ��������ظ���iso */
	public GffHashGene getGffHashGeneModifyResult() {
		if (!calculate) {
			fillGffHashGeneResult();
			modifyGff();
		}
		return gffHashGeneResult;
	}
	public TranscriptomStatistics getStatisticsCompareGff() {
		fillGffHashGeneResult();
		return statisticTranscriptome();
	}
	/**
	 * ��Gff��Ϣװ��mapChrID2LsGffGeneCluster
	 */
	private void fillGffHashGeneResult() {
		if (calculate) {
			return;
		}
		calculate = true;
		
		ArrayList<String> lsChrID = lsGffHashGenes.get(0).getLsChrID();
		for (String chrID : lsChrID) {
			ArrayList<ListGff> lsGffAll = new ArrayList<ListGff>();
			for (GffHashGene gffHashGene : lsGffHashGenes) {
				ListGff listGff = gffHashGene.getMapChrID2LsGff().get(chrID.toLowerCase());
				if (listGff == null) {
					continue;
				}
				lsGffAll.add(listGff);
			}
			ArrayList<int[]> lsGeneBound = ListAbs.getCombSep(null, lsGffAll, true);
			ArrayList<GffGeneCluster> lsGff = getListGeneCluster(chrID, lsGeneBound, lsGffHashGenes);
			mapChrID2LsGffCluster.put(chrID.toLowerCase(), lsGff);
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
				int startID = getGffGeneIndexNum(true, gffCodGeneStart);
				
				GffCodGene gffCodGeneEnd = gffHashGene.searchLocation(chrID, geneBound[1]);
				int endID = getGffGeneIndexNum(false, gffCodGeneEnd);
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
	/**
	 * 	��gffCodGene�ϣ�ͷ�������indexNum��β�������indexNum
	 * @param start �Ƿ�
	 * @param gffCodGene
	 * @return
	 */
	private int getGffGeneIndexNum(boolean start, GffCodGene gffCodGene) {
		if (gffCodGene == null) {
			return -1;
		}
		
		if (gffCodGene.isInsideLoc()) {
			return gffCodGene.getItemNumThis();
		}
		
		if (start) {
			return gffCodGene.getItemNumDown();
		} else {
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
	
	private void modifyGff() {
		for (ArrayList<GffGeneCluster> listGffGeneClusters : mapChrID2LsGffCluster.values()) {
			modifyAndAddChrIDlist(listGffGeneClusters);
		}
	}
	
	private void modifyAndAddChrIDlist( ArrayList<GffGeneCluster> lsGeneCluster) {
		ListGff listGff = new ListGff();
		for (GffGeneCluster gffGeneCluster : lsGeneCluster) {
			ArrayList<GffDetailGene> lsGene = gffGeneCluster.getCombinedGffGene();
	
			boolean shortGene = true;
			for (GffDetailGene gffDetailGene : lsGene) {
				//���ȴ���ָ�����ȣ������ת¼�������ں��ӣ��Ϳ�����Ϊ���Ǽٻ���
				if (gffDetailGene.Length() > minGeneLen || gffDetailGene.getLongestSplit().size() > 1) {
					shortGene = false;
					break;
				}
			}
			if (gffGeneCluster.isContainsRef) {
				shortGene = false;
			}
			if (!shortGene) {
				listGff.addAll(lsGene);
			}
		}
		gffHashGeneResult.addListGff(listGff);
	}
	
	private TranscriptomStatistics statisticTranscriptome() {
		TranscriptomStatistics transcriptomStatistics = new TranscriptomStatistics();
		prepareStatistics(transcriptomStatistics);
		for (ArrayList<GffGeneCluster> listGffGeneClusters : mapChrID2LsGffCluster.values()) {
			statisticsLsGffGeneCluster(transcriptomStatistics, listGffGeneClusters);
		}
		return transcriptomStatistics;
	}
	private void prepareStatistics(TranscriptomStatistics transcriptomStatistics) {
		SeqHash seqFastaHash = new SeqHash(species.getChromFaPath(), species.getChromFaRegex());
		transcriptomStatistics.setSeqFastaHash(seqFastaHash);
	}
	private void statisticsLsGffGeneCluster(TranscriptomStatistics transcriptomStatistics, ArrayList<GffGeneCluster> lsGeneCluster) {
		for (GffGeneCluster gffGeneCluster : lsGeneCluster) {
			transcriptomStatistics.addGeneCluster(gffGeneCluster);
		}
	}
}
