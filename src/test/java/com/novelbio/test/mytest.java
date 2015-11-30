package com.novelbio.test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.stat.inference.TestUtils;
import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.fastq.FastQRecord;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.analysis.seq.mapping.MapIndexMaker;
import com.novelbio.analysis.seq.mapping.MapIndexMaker.IndexMapSplice;
import com.novelbio.analysis.seq.rnaseq.RPKMcomput;
import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;
import com.novelbio.database.model.modgeneid.GeneType;
import com.novelbio.database.model.species.Species;


public class mytest {
	private static final Logger logger = Logger.getLogger(mytest.class);
	static boolean is;

	public static void main(String[] args) throws Exception {
//		Species species = new Species();
//		species = new Species();
//		species.setTaxID(10090);
//		species.setVersion("mm10_GRCm38");
//		makeIndexTophat(species);
//		
//		species.setTaxID(9823);
//		species.setVersion("Sscrofa10.2");
//		makeIndexTophat(species);
//		
//		species.setTaxID(9606);
//		species.setVersion("GRCh38");
//		makeIndexTophat(species);
//		
//		species = new Species();
//		species.setTaxID(9606);
//		species.setVersion("hg19_GRCh37");
//		makeIndexTophat(species);
//		
//		species = new Species();
//		species.setTaxID(39947);
//		species.setVersion("tigr7");
//		makeIndexTophat(species);
//		
//		species = new Species();
//		species.setTaxID(3702);
//		species.setVersion("tair9");
//		makeIndexTophat(species);

		
//		Object obj = JSONObject.parse("unable to ping registry endpoint https://192.168.0.172:5001/v0/");
//		System.out.println(obj.toString());
//		while (true) {
//			System.out.println(DateUtil.getDateDetail() + " sysout");
//			System.err.println(DateUtil.getDateDetail() + " err");
//			Thread.sleep(1000);
//		}
		GffHashGene gffHashGene = new GffHashGene("/home/novelbio/下载/Triticum_aestivum.IWGSC1.0_popseq.28.integration.v2.gtf");
		
		
		ArrayList<GffDetailGene> lsGffDetailGene = gffHashGene.getGffDetailAll();
		Map<String, Integer> mapGene2Len = new HashMap<>();
		for (GffDetailGene gffDetailGene : lsGffDetailGene) {
			for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
				String geneName = gffGeneIsoInfo.getParentGeneName();
				int isoLength = gffGeneIsoInfo.getLenExon(0);
				//获得一个基因中最长转录本的名字
				if (!mapGene2Len.containsKey(geneName) || mapGene2Len.get(geneName) < isoLength) {
					mapGene2Len.put(geneName, isoLength);
				}
			}
		}
		
		TxtReadandWrite txtWrite = new TxtReadandWrite("/home/novelbio/下载/genename", true);
		for (String geneName : mapGene2Len.keySet()) {
			txtWrite.writefileln(geneName);
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
