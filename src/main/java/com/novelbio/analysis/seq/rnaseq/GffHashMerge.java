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
/** 重建转录本 */
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
	/** key小写 */
	HashMap<String, ArrayList<GffGeneCluster>> mapChrID2LsGffCluster = new HashMap<String, ArrayList<GffGeneCluster>>();
	
	GffHashGene gffHashGeneResult = new GffHashGene();
	/**统计转录本信息时用到 */
	Species species;
	/**新的转录本如果长度小于1000，并且没有内含子，就有可能是假基因，就删除 */
	int minGeneLen = 1000;
	
	boolean calculate = false;
	
	public void setGffHashGeneRef(GffHashGene gffHashGeneRef) {
		this.gffHashGeneRef = gffHashGeneRef;
		lsGffHashGenes.add(0, gffHashGeneRef);
		calculate = false;
	}
	/**
	 * 添加Gff信息
	 * @param gffHashGene
	 */
	public void addGffHashGene(GffHashGene gffHashGene) {
		lsGffHashGenes.add(gffHashGene);
		calculate = false;
	}
	public void setSpecies(Species species) {
		this.species = species;
	}
	/** 内部包含有重复的iso */
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
	 * 将Gff信息装入mapChrID2LsGffGeneCluster
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
	 * 将某一条染色体的所有gffhashgene的信息按照划分的区域装入mapChrID2LsGffGeneCluster
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
				//起点大于终点说明在该位置区间里面，本GffHashGene中没有找到基因
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
	 * 	看gffCodGene上，头部基因的indexNum和尾部基因的indexNum
	 * @param start 是否看
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
	 * 将指定范围内的gffGene装入cluster中
	 * @param gffHashGeneBed
	 * @param chrID
	 * @param startLoc 从0开始
	 * @param endLoc 从0开始
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
				//长度大于指定长度，或者最长转录本含有内含子，就可以认为不是假基因
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
