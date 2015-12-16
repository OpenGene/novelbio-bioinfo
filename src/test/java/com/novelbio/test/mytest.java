package com.novelbio.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.inference.TestUtils;
import org.apache.log4j.Logger;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapreduce.GroupBy;
import org.springframework.data.mongodb.core.mapreduce.GroupByResults;

import com.hg.doc.gr;
import com.novelbio.analysis.seq.fasta.ChrDensity;
import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.fastq.FastQRecord;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReads;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReads.ChrMapReadsInfo;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.analysis.seq.mapping.MapIndexMaker;
import com.novelbio.analysis.seq.mapping.MapIndexMaker.IndexMapSplice;
import com.novelbio.analysis.seq.sam.AlignSamReading;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.domain.geneanno.TaxInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;
import com.novelbio.database.model.species.Species;
import com.novelbio.database.service.SpringFactoryBioinfo;
import com.novelbio.listOperate.HistBin;
import com.novelbio.listOperate.HistList;


public class mytest {
	private static final Logger logger = Logger.getLogger(mytest.class);
	static boolean is;

	public static void main(String[] args) throws Exception {
//		List<String> lsResult = TxtReadandWrite.readReverse("/hdfs:/nbCloud/public/software/WebApp/testReadEnd.txt", 4);
//		for (String string : lsResult) {
//			System.out.println(string);
//		}
		
		
//		TxtReadandWrite txtWrite = new TxtReadandWrite("/media/winE/tsetserfs.txt", true);
//		txtWrite.writefileln("fse");
//		txtWrite.writefile("台湾铯夫人三");
//		txtWrite.close();
		
		List<String> lsResult = TxtReadandWrite.readReverse("/media/winE/tsetserfs.txt", 1);
		for (String string : lsResult) {
			System.out.println(string);
		}
	}
	
	private static void test() {
		Species species = new Species(39947);
		species.setVersion("tigr7");
		Map<String, Long> mapChr2Len = SeqHash.getMapChrId2Len(species.getChromSeq() + ".fai");
		ChrDensity chrDensity = new ChrDensity(mapChr2Len, 1000000);
		TxtReadandWrite txtRead = new TxtReadandWrite("/media/winE/resources/fanwei/combine/JP69.bed");
		for (String content : txtRead.readlines()) {
			String[] ss = content.split("\t");
			int start = Integer.parseInt(ss[1]);
			int end = Integer.parseInt(ss[2]);
			if (Math.abs(start - end) < 50) {
				continue;
			}
			chrDensity.addSite(ss[0], Integer.parseInt(ss[1]));
		}
		
		TxtReadandWrite txtWrite = new TxtReadandWrite("/media/winE/resources/fanwei/combine/JP69_count.txt", true);
		for (HistList histList : chrDensity.getMapChr2His().values()) {
			for (HistBin histBin : histList) {
				txtWrite.writefileln(histList.getName() + "\t" + (int)histBin.getThisNumber() + "\t" + histBin.getCountNumber());
			}
		}
		
		txtRead.close();
		txtWrite.close();
	}
	
	private static void getCoveredRegion() {
		TxtReadandWrite txtWrite = new TxtReadandWrite("/media/winE/resources/fanwei/combine/JP69.bed", true);
		SamFile samFile = new SamFile("/media/winE/resources/fanwei/combine/JP69_sorted.bam");
		MapReads mapReads = new MapReads();
		int invNum = 2;
		mapReads.setInvNum(invNum);
		mapReads.prepareAlignRecord(samFile.readFirstLine());
		mapReads.setMapChrID2Len(samFile.getMapChrID2Length());

		AlignSamReading alignSamReading = new AlignSamReading(samFile);
		alignSamReading.addAlignmentRecorder(mapReads);
		alignSamReading.run();
		
		for (String chrId : mapReads.getChrIDLs()) {
			ChrMapReadsInfo mapReadsInfo = mapReads.getChrMapReadsInfo(chrId);
			
			int num = 0;
			int start = 0;
			
			int[] sumChrBps = mapReadsInfo.getSumChrBpReads();
			for (int i = 0; i < sumChrBps.length; i++) {
				int loc = i*invNum;
				int pileUp = sumChrBps[i];
				double pileUpD = (double)pileUp/mapReads.fold;
				if (pileUpD > 10) {
					if (num == 0) {
						start = loc;
					}
					num++;
				} else {
					if (num*invNum > 70) {
						txtWrite.writefileln(chrId + "\t" + start + "\t" + (start+ num*invNum));
					}
					num = 0;				
				}
			}
		}

		txtWrite.close();
	}
	
	private static void makeIndexTophat(Species species) {
		GffChrAbs gffChrAbs = new GffChrAbs(species);
		IndexMapSplice maker = (IndexMapSplice)MapIndexMaker.createIndexMaker(SoftWare.bwa_aln);
		maker.setLock(false);
		maker.setChrIndex(species.getIndexChr(SoftWare.bowtie));
		maker.IndexMake();
		gffChrAbs.close();
		System.out.println("finish " + species.getCommonName());
	}
	
	
	private static void run(String group, String name) {
		int i = 0;

		try {
			System.out.println("start run " + group + "\t" + name);

			String parentPath = "/run/media/novelbio/4256740f-b44a-4500-89ab-61ce6448aeb6/F15FTSECKW0321_HUMcxvR/Raw/" + group + "/";
			FastQ fastQ = new FastQ(parentPath + name + "_1.fq.gz");
			FastQ fastQ2 = new FastQ(parentPath + name + "_2.fq.gz");
			
			for (FastQRecord[] fQRecords : fastQ.readlinesPE(fastQ2)) {
				i++;
				if (i%5000000 == 0) {
					System.out.println(i);
				}
			}
			System.out.println(group + "\t" + name + " is ok, linenum " + i);
			fastQ.close();
			fastQ2.close();
		} catch (Exception e) {
			System.out.println(group + "\t" + name + " is error, linenum " + i);
		}

	}
	
	protected static double chiSquareTestDataSetsComparison(int[] cond1, int[] cond2) {
		long[] cond1Long = new long[cond1.length];
		long[] cond2Long = new long[cond2.length];
		for (int i = 0; i < cond1.length; i++) {
			cond1Long[i] = cond1[i] + 1;
		}
		for (int i = 0; i < cond2.length; i++) {
			cond2Long[i] = cond2[i] + 1;
		}
		try {
			return TestUtils.chiSquareTestDataSetsComparison(cond1Long, cond2Long);
		} catch (Exception e) {
			return 1.0;
		}
	}
	private static List<Align> mergeNearbyAlign(List<Align> lsAligns) {
		List<Align> lsAlignsResult = new ArrayList<>();
		Align alignLast = null;
		for (Align align : lsAligns) {
			if (alignLast == null) {
				alignLast = align;
				continue;
			}
			int distance = align.getStartAbs() - alignLast.getEndAbs();
			if (distance < 0) {
				throw new RuntimeException("distance less than 0");
			} else if (distance < 20) {
				System.out.println("merge");
				align.setStart(alignLast.getStartAbs());
			} else {
				lsAlignsResult.add(alignLast);
			}
			alignLast = align;
		}
		lsAlignsResult.add(alignLast);
		return lsAlignsResult;
    }
	
	
}
