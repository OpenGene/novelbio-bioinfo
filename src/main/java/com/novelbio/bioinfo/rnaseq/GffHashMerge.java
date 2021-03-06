package com.novelbio.bioinfo.rnaseq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.bioinfo.base.binarysearch.ListAbs;
import com.novelbio.bioinfo.fasta.SeqHash;
import com.novelbio.bioinfo.gff.ExonClusterOperator;
import com.novelbio.bioinfo.gff.GffCodGene;
import com.novelbio.bioinfo.gff.GffGene;
import com.novelbio.bioinfo.gff.GffHashGene;
import com.novelbio.bioinfo.gff.GffType;
import com.novelbio.bioinfo.gff.ListGff;
import com.novelbio.database.domain.species.Species;
/**
 * merge转录本的，不过现在就用其统计模块把<br>
 * merge工作交给cuffmerge来做
 * @author zong0jie
 *
 */
@Deprecated
public class GffHashMerge {
	private static final Logger logger = Logger.getLogger(GffHashMerge.class);
	
	public static void main(String[] args) {
//		mouse();
	}
	public static void mouse() {
		String gffhashGeneCuf = "/media/hdfs/nbCloud/staff/hongyanyan/wanglinsurui/reconstructure/ReconstructTranscriptome_result/tmpMerge/merged.gtf";
		String gffFinal = "/media/hdfs/nbCloud/staff/hongyanyan/wanglinsurui/reconstructure/ReconstructTranscriptome_result/finalTranscriptNew.gtf";
		String gffFinalStatistics = "/media/winF/NBC/Project/Project_FY/FYmouse20111122/tophata15m1/novelbioTranscriptome/transcriptomeStatisticsNew.txt";
		Species species = new Species(9925);
		GffHashMerge gffHashMerge = new GffHashMerge();
		gffHashMerge.setGffHashGeneRef(new GffHashGene(species.getGffType(), species.getGffFile()));
		gffHashMerge.addGffHashGene(new GffHashGene(GffType.GTF, gffhashGeneCuf));
		GffHashGene gffHashGene = gffHashMerge.getGffHashGeneModifyResult();
		gffHashGene.writeToGTF(gffFinal, "novelbio");

		gffHashMerge = new GffHashMerge();
		gffHashMerge.setGffHashGeneRef(new GffHashGene(species.getGffType(), species.getGffFile()));
		gffHashMerge.addGffHashGene(new GffHashGene(GffType.GTF, gffFinal));

		TranscriptomStatistics transcriptomStatistics = gffHashMerge.getStatisticsCompareGff();
		TxtReadandWrite txtOut = new TxtReadandWrite(gffFinalStatistics, true);

		txtOut.ExcelWrite(transcriptomStatistics.getStatisticsResult());
		txtOut.close();
	}

	ArrayList<GffHashGene> lsGffHashGenes = new ArrayList<GffHashGene>();
	/** key小写 */
	HashMap<String, ArrayList<GffGeneCluster>> mapChrID2LsGffCluster = new HashMap<String, ArrayList<GffGeneCluster>>();
	
	GffHashGene gffHashGeneResult = new GffHashGene();
	/**统计转录本信息时用到 */
	SeqHash seqHash;
	/**新的转录本如果长度小于本长度，并且没有内含子，就有可能是假基因，就删除 */
	int minGeneLen = 200;
	
	boolean calculate = false;
	
	public void setGffHashGeneRef(GffHashGene gffHashGeneRef) {
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
	public void setSeqHash(SeqHash seqHash) {
		this.seqHash = seqHash;
	}
	/** 内部包含有重复的iso
	 * 还是用cuffmerge做吧别多此一举麻烦
	 *  */
	@Deprecated 
	public GffHashGene getGffHashGeneModifyResult() {
		if (!calculate) {
			fillGffHashGeneResult();
			modifyGff();
		}
		return gffHashGeneResult;
	}
	public TranscriptomStatistics getStatisticsCompareGff() {
		fillGffHashGeneResult();
//		modifyGff();
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
			List<int[]> lsGeneBound = ExonClusterOperator.getCombSep(null, lsGffAll, true);
			ArrayList<GffGeneCluster> lsGffCluster = getListGeneCluster(chrID, lsGeneBound, lsGffHashGenes);
			mapChrID2LsGffCluster.put(chrID.toLowerCase(), lsGffCluster);
		}
	}
	
	/**
	 * 将某一条染色体的所有gffhashgene的信息按照划分的区域装入mapChrID2LsGffGeneCluster
	 * @param chrID
	 * @param lsGeneBount
	 * @param lsGffHashGenes
	 */
	private ArrayList<GffGeneCluster> getListGeneCluster(String chrID, List<int[]> lsGeneBound, ArrayList<GffHashGene> lsGffHashGenes) {
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
	 * @param start 如果该cod没有落在一个GffDetail基因中，那么当start为true，则返回该cod的下一个基因
	 * 否则返回该cod的上一个基因<br>
	 * 如 start 为true：-----*------gene---------
	 * 如 start 为false：-----------gene---*----
	 * @param gffCodGene
	 * @return
	 */
	private int getGffGeneIndexNum(boolean start, GffCodGene gffCodGene) {
		if (gffCodGene == null) {
			return -1;
		}
		
		if (gffCodGene.isInsideLoc()) {
			return gffCodGene.getIndexAlignThis();
		}
		
		if (start) {
			return gffCodGene.getIndexAlignDown();
		} else {
			return gffCodGene.getIndexAlignUp();
		}
	}
	/**
	 * 将指定范围内的gffGene装入cluster中
	 * @param gffHashGeneBed
	 * @param chrID
	 * @param startID 本GffDetailGene的序号，如果本GffDetailGene不存在，则返回上一个gffDetailGene的序号
	 * @param endID 
	 */
	private void addGffGene_Into_GffCluster(GffGeneCluster gffGeneCluster, GffHashGene gffHashGene, String chrID, int startID, int endID) {
		ListGff lsGff = gffHashGene.getMapChrID2LsGff().get(chrID);
		ArrayList<GffGene> lsGffSubGene = new ArrayList<GffGene>();
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
		HashSet<String> setChrID = new HashSet<String>(gffHashGeneResult.getMapChrID2LsGff().keySet());
		for (String chrID : setChrID) {
			ListGff listGff = gffHashGeneResult.getMapChrID2LsGff().get(chrID);
			listGff.sort();
			ListGff listGffNew = listGff.combineOverlapGene();
			gffHashGeneResult.getMapChrID2LsGff().put(chrID, listGffNew);
			//装入hash表
			for (GffGene gffDetailGene : listGffNew) {
				gffDetailGene.removeDupliIso();
			}
		}
	}
	
	private void modifyAndAddChrIDlist( ArrayList<GffGeneCluster> lsGeneCluster) {
		ListGff listGff = new ListGff();
		for (GffGeneCluster gffGeneCluster : lsGeneCluster) {
//			if (gffGeneCluster.getRefGffGene() != null && gffGeneCluster.getRefGffGene().size() > 0 && gffGeneCluster.getRefGffGene().get(0).getName().contains("PARK2")) {
//				logger.error("stop");
//			}
			List<GffGene> lsGene = gffGeneCluster.getCombinedGffGene();
	
			boolean shortGene = true;
			for (GffGene gffDetailGene : lsGene) {
				//长度大于指定长度，或者最长转录本含有内含子，就可以认为不是假基因
				if (gffDetailGene.getLength() > minGeneLen || gffDetailGene.getLongestSplitMrna().size() > 1) {
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
		transcriptomStatistics.setSeqFastaHash(seqHash);
	}
	private void statisticsLsGffGeneCluster(TranscriptomStatistics transcriptomStatistics, ArrayList<GffGeneCluster> lsGeneCluster) {
		for (GffGeneCluster gffGeneCluster : lsGeneCluster) {
			transcriptomStatistics.addGeneCluster(gffGeneCluster);
		}
	}
}
