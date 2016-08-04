package com.novelbio.test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.math3.stat.inference.TestUtils;
import org.apache.hadoop.util.Shell;
import org.apache.hadoop.util.Shell.ShellCommandExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import smile.math.special.Beta;
import smile.stat.hypothesis.ChiSqTest;

import com.google.common.collect.Lists;
import com.novelbio.analysis.seq.fasta.ChrDensity;
import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.fastq.FastQRecord;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffType;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReads;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReads.ChrMapReadsInfo;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.analysis.seq.mapping.IndexMappingMaker;
import com.novelbio.analysis.seq.mapping.IndexMappingMaker.IndexMapSplice;
import com.novelbio.analysis.seq.sam.AlignSamReading;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.base.StringOperate;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.PatternOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;
import com.novelbio.database.domain.kegg.KGIDgen2Keg;
import com.novelbio.database.domain.kegg.KGentry;
import com.novelbio.database.domain.kegg.KGpathway;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.model.species.Species;
import com.novelbio.database.model.species.SpeciesFileSepChr;
import com.novelbio.database.service.servkegg.ServKEntry;
import com.novelbio.database.service.servkegg.ServKIDgen2Keg;
import com.novelbio.database.service.servkegg.ServKPathway;
import com.novelbio.listOperate.HistBin;
import com.novelbio.listOperate.HistList;


public class mytest {
	private static final Logger logger = LoggerFactory.getLogger(mytest.class);
	static boolean is;

	public static void main(String[] args) throws Exception {
		String str = "chr_97483430_97561047_chr12_97561047_97483431_-77616_RMST";
		PatternOperate patternOperate = new PatternOperate("[a-z,A-Z]+_{0,1}\\d*_\\d+_\\d+_-{0,1}\\d+");
		List<String> lsChrName = patternOperate.getPat(str);
		for (String chrName : lsChrName) {
			System.out.println(chrName);
		}
		str = str.replaceFirst(lsChrName.get(0), "");
		System.out.println(str);
	}
	
	private static List<Integer> getLsIntegers(String colInfo) {
		if (StringOperate.isRealNull(colInfo)) {
			return Lists.newArrayList(0);
		}
		colInfo = colInfo.replace(",", " ").replace(";", " ");
		
		String[] ss = colInfo.split(" +");
		Set<Integer> setCols = new LinkedHashSet<>();
		for (String colTmp : ss) {
			if (StringOperate.isRealNull(colTmp)) {
				continue;
			}
			if (colTmp.contains("-")) {
				setCols.addAll(getLsSequenceNum(colTmp));
			} else {
				try {
					setCols.add(Integer.parseInt(colTmp));
				} catch (Exception e) {
					throw new RuntimeException("cannot contain " + colTmp);
				}
			}
		}
		return new ArrayList<>(setCols);
	}
	
	private static List<Integer> getLsSequenceNum(String colSeq) {
		String[] ss = colSeq.trim().split("-");
		if (ss.length > 2) {
			throw new RuntimeException("cannot contain " + colSeq);
		}
		int start = 0, end = 0;
		try {
			start = Integer.parseInt(ss[0]);
			end = Integer.parseInt(ss[1]);
		} catch (Exception e) {
			throw new RuntimeException("cannot contain " + colSeq);
		}
		
		List<Integer> lsCols = new ArrayList<>();
		
		if (start <= end) {
			for (int i = start; i <= end; i++) {
				lsCols.add(i);
			}
		} else {
			for (int i = start; i >= end; i--) {
				lsCols.add(i);
			}
		}
		return lsCols;
	}
	
	
	public static void getGeneFromPath() throws Exception {
		List<String> lsGeneName = new ArrayList<>();
//		List<KGpathway> lsKGpathways = ServKPathway.getInstance().findAll();
//		System.out.println();
		KGpathway kGpathway = ServKPathway.getInstance().findByPathName("path:mmu11651");
		List<KGentry> lsKGentry = ServKEntry.getInstance().findByPathName(kGpathway.getPathName());
		for (KGentry kGentry : lsKGentry) {
			
			KGIDgen2Keg kgiDgen2Keg = ServKIDgen2Keg.getInstance().findByKegId(kGentry.getEntryName());
			if (kgiDgen2Keg != null) {
				GeneID geneID = new GeneID(GeneID.IDTYPE_GENEID, kgiDgen2Keg.getGeneID()+"", kgiDgen2Keg.getTaxID());
				lsGeneName.add(geneID.getSymbol());
			}
		}
		for (String symbol : lsGeneName) {
			System.out.println(symbol);
		}
		
	}
	
	private static void runPvalue(int repeat) throws Exception {}
	
	private static int[] getIntContent(String content) {
		Random random = new Random();
		
		String[] ss = content.split("::");
		int a = Integer.parseInt(ss[0]) + random.nextInt(100)-50;
		if (a < 0) {
			a = 2;
		}
		int b = Integer.parseInt(ss[1]) + random.nextInt(100)-50;
		if (b < 0) {
			b = 2;
		}
		
		int[] result = new int[]{a, b};
		return result;
	}
	
	private static String getStrInt(List<int[]> lsCtrl) {
		String ss = "";
		for (int[] is : lsCtrl) {
			ss+= is[0] + ":" + is[1] + "\t";
		}
		return ss;
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
