package com.novelbio.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.math.stat.inference.ChiSquareTestImpl;
import org.apache.commons.math3.distribution.FDistribution;
import org.apache.commons.math3.stat.inference.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import smile.stat.hypothesis.ChiSqTest;
import smile.stat.hypothesis.FTest;
import smile.math.special.Beta;
import smile.math.special.Gamma;

import com.novelbio.analysis.seq.fasta.ChrDensity;
import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.fastq.FastQRecord;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReads;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReads.ChrMapReadsInfo;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.analysis.seq.mapping.IndexMappingMaker;
import com.novelbio.analysis.seq.mapping.IndexMappingMaker.IndexMapSplice;
import com.novelbio.analysis.seq.sam.AlignSamReading;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;
import com.novelbio.database.model.species.Species;
import com.novelbio.listOperate.HistBin;
import com.novelbio.listOperate.HistList;


public class mytest {
	private static final Logger logger = LoggerFactory.getLogger(mytest.class);
	static boolean is;

	public static void main(String[] args) throws Exception {
//		double[] aa = new double[]{123l,144l,105l};
//		double[] bb = new double[]{93l,104l,135l};
//		
//		int[] aai = new int[]{123,144,105};
//		int[] bbi = new int[]{93,104,135};
//		
//		FTest fTest = FTest.test(aa, bb);
//		ChiSqTest chiSqTest = ChiSqTest.test(aai, bbi);
//		
//		System.out.println(chiSqTest.chisq);
//		System.out.println(chiSquareDataSetsComparison(aai, bbi));
//		int df1 = 2;
//		int df2 = 2;
//		double p = 2.0 * Beta.regularizedIncompleteBetaFunction(0.5 * df2, 0.5 * df1, df2 / (df2 + df1 * fTest.f));
//		if (p > 1.0) {
//            p = 2.0 - p;
//        }
//		System.out.println(fTest.pvalue);
		
//		System.out.println(chiSquareDataSetsComparison(new int[]{24,16}, new int[]{35,33}));
//		System.out.println(chiSquareDataSetsComparison(new int[]{24,35}, new int[]{16,33}));
//		System.out.println(chiSquareDataSetsComparison(new int[]{24,16}, new int[]{35,33}));

//		int[] a = new int[]{24,35, 22};
//		int[] b = new int[]{16,33, 31};
//		
//		long[] ad = new long[]{24,35, 22};
//		long[] bd = new long[]{16,33, 31};
//		
//		ChiSqTest chiSqTest = ChiSqTest.test(a, b);
//		System.out.println(chiSqTest.chisq);
//		System.out.println(chiSqTest.pvalue);
//		System.out.println();
//		
//		System.out.println(TestUtils.chiSquareTestDataSetsComparison(ad, bd));
		
		
//		int[] a = new int[]{24,16};
//		int[] b = new int[]{35,33};
//		
//		ChiSqTest chiSqTest = ChiSqTest.test(a, b);
//		System.out.println(chiSqTest.chisq);
//		System.out.println(chiSqTest.pvalue);
//		System.out.println();
//		int[] a = new int[]{24,35};
//		int[] b = new int[]{16,33};
//		
//		ChiSqTest chiSqTest = ChiSqTest.test(a, b);
//		System.out.println(chiSqTest.chisq);
//		System.out.println(chiSqTest.pvalue);
//		System.out.println();
//		
//		
//		
//		long[] al = new long[]{24,35};
//		long[] bl = new long[]{16,33};
////		
////		
//		ChiSquareTestImpl chiSquareTestImpl = new ChiSquareTestImpl();
//		long[][] aaa = new long[2][2];
//		aaa[0][0]=24; aaa[0][1] = 16;
//		aaa[1][0] = 35; aaa[1][1] = 33;
//		System.out.println();
//		System.err.println(chiSquareTestImpl.chiSquare(aaa));
//		System.err.println(chiSquareTestImpl.chiSquareTest(aaa));
//		System.err.println(chiSquareTestImpl.chiSquareDataSetsComparison(aaa[0], aaa[1]));
//		System.err.println(TestUtils.chiSquareDataSetsComparison(aaa[0], aaa[1]));
//		System.err.println(org.apache.commons.math.stat.inference.TestUtils.chiSquareDataSetsComparison(aaa[0], aaa[1]));
		
//		double p = Gamma.regularizedUpperIncompleteGamma(0.5 * 1, 0.5 * chiSquareTestImpl.chiSquare(aaa));
//		System.out.println(p);
		double f= 0.2863;
		int dfIn = 4;
		int dfCvT = 3;
		double p = 2.0 * Beta.regularizedIncompleteBetaFunction(0.5 * dfIn, 0.5 * dfCvT, dfIn / (dfIn + dfCvT * f));
		if (p > 1.0) {
		p = 2.0 - p;
    }
		System.out.println(p);
		System.out.println();
		double[] x = new double[]{24,35,32,26,31};
		double[] y = new double[]{35,21,19,34};
		FTest fTest = FTest.test(x, y);
		System.out.println(fTest.f);
		System.out.println(fTest.pvalue);
		System.out.println(fTest.df1);
		System.out.println(fTest.df2);
		
		
		FDistribution fDistribution = new FDistribution(4, 3);
		System.out.println(1-fDistribution.cumulativeProbability(3.493431855500827));
//		p2 = org.apache.commons.math3.special.Beta.regularizedBeta(f, dfCvT ,dfIn);
//		System.out.println(p2);
//		dfIn = 6;
//		dfCvT = 3;
//		p1 = 2.0 * Beta.regularizedIncompleteBetaFunction(0.5 * dfIn, 0.5 * dfCvT, dfIn / (dfIn + dfCvT * f));
//		p2 = 2.0 * org.apache.commons.math3.special.Beta.regularizedBeta(dfIn / (dfIn + dfCvT * f), 0.5 * dfIn, 0.5 * dfCvT );
//		System.out.println(p1);
//		System.out.println(p2);
//		FTest
//		p = 2.0 * Beta.inverseRegularizedIncompleteBetaFunction(0.5 * dfIn, 0.5 * dfCvT, dfIn / (dfIn + dfCvT * f));
//		if (p > 1.0) {
//			p = 2.0 - p;
//        }
//		System.out.println(p);
//		
//		p = 2.0 * Beta.regularizedIncompleteBetaFunction(0.5 * dfIn, 0.5 * dfCvT, dfIn / (dfIn + dfCvT * f));
//		if (p > 1.0) {
//			p = 2.0 - p;
//        }
//		System.out.println(p);
//		ChiSqTest chiSqTest = ChiSqTest.test(a, b);
//		System.out.println(chiSqTest.chisq);
//		System.out.println(chiSqTest.pvalue);
//		System.out.println();
//		
//		System.out.println(TestUtils.chiSquareTestDataSetsComparison(al, bl));
	}
	
	
	
	/**
	 * 差异可变剪接计算pvalue
	 * 实验数据
	 * 24 35
	 * 16 33
	 * 45 66
	 * 
	 * 34 31
	 * 25 18
	 * 56 44
	 * 
	 */
	private static void testPvalue() {
		List<int[]> lsTreat = new ArrayList<>();
		lsTreat.add(new int[]{24, 35});
		lsTreat.add(new int[]{16, 33});
		lsTreat.add(new int[]{45, 66});
		
		List<int[]> lsCtrl = new ArrayList<>();
		lsCtrl.add(new int[]{34, 31});
		lsCtrl.add(new int[]{25, 18});
		lsCtrl.add(new int[]{56, 44});
		
		System.out.println(getPvalue(lsTreat, lsCtrl));
	}
	
	
	private static double getPvalue(List<int[]> lsTreat2LsValue, List<int[]> lsCtrl2LsValue) {
		double chiCvT = 0, chiIn = 0;
		int[] ctrlFirst = null, treatFirst = null;
		int[] ctrlLast = null, treatLast = null;
		
		int dfCvT= lsTreat2LsValue.size();
		int dfIn = 0;
		for (int i = 0; i < lsTreat2LsValue.size(); i++) {
			int[] treatOne = lsTreat2LsValue.get(i);
			int[] ctrlOne = lsCtrl2LsValue.get(i);
			
			if (i == 0) {
				ctrlFirst = ctrlOne;
				treatFirst = treatOne;
			}
			if (ctrlLast != null) {
				dfIn += 2;
				chiIn += chiSquareDataSetsComparison(ctrlLast, ctrlOne);
				chiIn += chiSquareDataSetsComparison(treatLast, treatOne);
			}
			if (i == lsTreat2LsValue.size() - 1) {
				dfIn += 2;
				chiIn += chiSquareDataSetsComparison(ctrlFirst, ctrlOne);
				chiIn += chiSquareDataSetsComparison(treatFirst, treatOne);
			}
			
			chiCvT += chiSquareDataSetsComparison(treatOne, ctrlOne);
		}
		
		double f = (chiCvT/dfCvT) / (chiIn/dfIn);
		
		double p = 2.0 * Beta.regularizedIncompleteBetaFunction(0.5 * dfIn, 0.5 * dfCvT, dfIn / (dfIn + dfCvT * f));
		if (p > 1.0) {
			p = 2.0 - p;
        }
		
		return p;
	}
	
	
	protected static double chiSquareDataSetsComparison(int[] cond1, int[] cond2) {
		return chiSquareDataSetsComparison1(cond1, cond2);
	}
	
	protected static double chiSquareDataSetsComparison1(int[] cond1, int[] cond2) {
		long[] cond1Long = new long[cond1.length];
		long[] cond2Long = new long[cond2.length];
		for (int i = 0; i < cond1.length; i++) {
			cond1Long[i] = cond1[i];
		}
		for (int i = 0; i < cond2.length; i++) {
			cond2Long[i] = cond2[i];
		}
		try {
			double chisq = TestUtils.chiSquareDataSetsComparison(cond1Long, cond2Long);
			return chisq;
		} catch (Exception e) {
			return 1.0;
		}
	}
	
	protected static double chiSquareDataSetsComparison2(int[] cond1, int[] cond2) {
		ChiSqTest chiSqTest = ChiSqTest.test(cond1, cond2);
		return chiSqTest.chisq;
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
		IndexMapSplice maker = (IndexMapSplice)IndexMappingMaker.createIndexMaker(SoftWare.bwa_aln);
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
