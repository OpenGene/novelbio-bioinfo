package com.novelbio.analysis.seq.sam;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffOperate.GffCodGeneDU;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGeneInf;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.analysis.seq.mapping.MapLibrary;
import com.novelbio.analysis.seq.mapping.StrandSpecific;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.database.model.species.Species;


/**
 * 给定bam文件，获得该bam的测序信息，如是单端还是双端，是否为链特异性测序<br>
 * @author zong0jie
 */
public class BamReadsInfo {
	private static final Logger logger = Logger.getLogger(BamReadsInfo.class);
	public static void main(String[] args) {
		BamReadsInfo bamReadsInfo = new BamReadsInfo();
		bamReadsInfo.setSamFile(new SamFile("/media/hdfs/nbCloud/public/AllProject/project_531d6744e4b054275b734e67/task_53210ddde4b0183c9804832b/RNASeqMap_result/CQ9522-2mm-1A_tophat_sorted.bam"));
		Species species = new Species(39947);
		species.setVersion("tigr7");
		GffChrAbs gffHashGene = new GffChrAbs(species);
		bamReadsInfo.setGffHashGene(gffHashGene.getGffHashGene());
		try {
			bamReadsInfo.calculate();
		} catch (Exception e) {
	        // TODO: handle exception
        }
		System.out.println(bamReadsInfo.getMapLibrary().toString());
		System.out.println(bamReadsInfo.getStrandSpecific().toString());
	}
	SamFile samFile;
	GffHashGeneInf gffHashGene;
	/** 连特异性形式 */
	StrandSpecific strandSpecific;
	/** 建库方法 */
	MapLibrary mapLibrary;
	
	/** 与基因同向的reads数量 */
	double cisNum;
	/** 与基因反向的reads数量 */
	double transNum;
	
	/** 不能输入流形式的samFile */
	public void setSamFile(SamFile samFile) {
		assert samFile != null;
		this.samFile = samFile;
	}
	
	public void setGffHashGene(GffHashGeneInf gffHashGene) {
		this.gffHashGene = gffHashGene;
	}
	
	public void calculate() throws ExceptionSamStrandError {
		mapLibrary = null;
		cisNum = 0;
		transNum = 0;
		
		setLibrary();
		if (gffHashGene != null) {
			calculateStrandSpecific();
		}
	}
	
	public StrandSpecific getStrandSpecific() {
		return strandSpecific;
	}
	public MapLibrary getMapLibrary() {
		return mapLibrary;
	}
	
	public double getCisNum() {
		return cisNum;
	}
	public double getTransNum() {
		return transNum;
	}
	
	/** mapLibrary "\t" strandSpecific "\t" cisNum "\t" transNum */
	public String toString() {
		return mapLibrary + "\t" + strandSpecific + "\t" + cisNum + "\t" + transNum;
	}
	
	public static String[] getTitle() {
		return new String[]{"Library", "StrandInfo", "CisReadsNum", "TransReadsNum"};
	}
	/**
	 * 双端数据是否获得连在一起的bed文件
	 * 如果输入是单端数据，则将序列延长返回bed文件
	 * 注意：如果是双端文件，<b>不能预先排序</b>
	 * @param getPairedBed
	 */
	protected void setLibrary() {
		List<Integer> lsInsertLen = new ArrayList<>();
		boolean isPairend = false;
		
		int countAll = 1000;
		int countMax = 100000;
		int countLines = 0;
		
		for (SamRecord samRecord : samFile.readLines()) {
			countLines++;
			if ((!isPairend && countLines > countAll) //单端并且读了1000条reads 
					|| (isPairend && lsInsertLen.size() > 1000) //双端并且读了1000条双端reads
					|| countLines > countMax) { //总共读了不超过10000条reads
				break;
			}
			if (samRecord.isHavePairEnd()) {
				isPairend = true;
				if (samRecord.getRefID().equals(samRecord.getMateRefID())) {
					lsInsertLen.add(Math.abs(samRecord.getMateAlignmentStart() - samRecord.getStartAbs()));
				}
			}
		}
		samFile.close();
		if (!isPairend) {
			mapLibrary = MapLibrary.SingleEnd;
		} else {
			double lenUp75 = MathComput.median(lsInsertLen, 75);
			if (lsInsertLen.size() < 200) {
				mapLibrary = MapLibrary.Unknown;
			} else if (lenUp75 < 800) {
					mapLibrary = MapLibrary.PairEnd;
			} else {
				mapLibrary = MapLibrary.MatePair;
			}
		}
	}
	
	protected void calculateStrandSpecific() throws ExceptionSamStrandError {
		int countsMax = 10000000;
		int counts = 0;
		int countsMapped = 0;//mapping上的reads数量
		for (SamRecord samRecord : samFile.readLines()) {
			if (!samRecord.isFirstRead()) {//只看第一条reads
				continue;
			}
			if (counts++ > countsMax || (cisNum + transNum) > 500000) {
				break;
			}
			if (!samRecord.isMapped()) {
				continue;
			}
			Align align = samRecord.getAlignmentBlocks().iterator().next();
			GffCodGeneDU gffCodGeneDu = gffHashGene.searchLocation(align.getRefID(), align.getStartAbs(), align.getEndAbs());
			if (gffCodGeneDu == null) {
				continue;
			}
			countsMapped++;
			Boolean isSame = isSameToGeneIso(samRecord.isCis5to3(), gffCodGeneDu);
			if (isSame != null) {//不考虑重叠基因
				if (isSame) {
					cisNum++;
				} else {
					transNum++;
				}
			}
		}
		if (countsMapped < counts/100) {
			throw new ExceptionSamStrandError(samFile.getFileName() + " Mapped Rate Too Low");
		}
		samFile.close();
		logger.info("cisReadsNum/transReadsNum" + cisNum/transNum);
		if (cisNum/transNum > 6) {
			strandSpecific = StrandSpecific.FIRST_READ_TRANSCRIPTION_STRAND;
		} else if (transNum/cisNum > 6) {
			strandSpecific = StrandSpecific.SECOND_READ_TRANSCRIPTION_STRAND;
		} else if ( transNum/cisNum <= 6 && cisNum/transNum <= 6 && (transNum/cisNum >= 4 || cisNum/transNum >= 4)) {
			strandSpecific = StrandSpecific.UNKNOWN;
		} else {
			strandSpecific = StrandSpecific.NONE;
		}
	}

	/**
	 * 返回落到的基因Iso
	 * 没有落在基因内部就返回null
	 * @param readsCis5to3 考虑了方向
	 * @param gffCodGene
	 * @return null表示有重叠基因
	 */
	private Boolean isSameToGeneIso(boolean readsCis5to3, GffCodGeneDU gffCodGeneDu) {
		Boolean gffReadsStrand = null;
		gffCodGeneDu.setGeneBody(false);
		gffCodGeneDu.setExon(true);
		Set<GffDetailGene> setGffGene = gffCodGeneDu.getCoveredOverlapGffGene();
		for (GffDetailGene gffDetailGene : setGffGene) {
			for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
				if (gffReadsStrand == null) {
					gffReadsStrand = gffGeneIsoInfo.isCis5to3();
				}
				if (gffReadsStrand != null && gffReadsStrand != gffGeneIsoInfo.isCis5to3()) {
					return null;
				}
			}
		}
		if (gffReadsStrand == null) {
			return null;
		}
		return readsCis5to3 == gffReadsStrand;
	}
}
