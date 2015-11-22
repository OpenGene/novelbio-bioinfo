package com.novelbio.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.stat.inference.TestUtils;
import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.fastq.FastQRecord;
import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.analysis.seq.mirna.MiRNACount;
import com.novelbio.analysis.seq.sam.AlignSamReading;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.base.SepSign;
import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.geneanno.SpeciesFile;
import com.novelbio.database.model.species.Species;
import com.novelbio.generalConf.PathDetailNBC;


public class mytest {
	private static final Logger logger = Logger.getLogger(mytest.class);
	static boolean is;

	public static void main(String[] args) throws Exception {
		FastQ fastQ = new FastQ("/media/nbfs/nbCloud/public/AllProject/project_54ffd6d0e4b0b3b73a8cd400/task_54ffe000e4b081224e6dbc54/QualityControl_result/Col_filtered_1.fq.gz");
		FastQ fastQ2 = new FastQ("/media/nbfs/nbCloud/public/AllProject/project_54ffd6d0e4b0b3b73a8cd400/task_54ffe000e4b081224e6dbc54/QualityControl_result/Col_filtered_2.fq.gz");

		FastQ fastQWrite = new FastQ("/home/novelbio/git/NBCplatform/src/test/resources/test_file/fastq/PE/arabidopsis_rna_1.fq.gz", true);
		FastQ fastQWrite2 = new FastQ("/home/novelbio/git/NBCplatform/src/test/resources/test_file/fastq/PE/arabidopsis_rna_2.fq.gz", true);
		
		int i = 0;
		for (FastQRecord[] fqs : fastQ.readlinesPE(fastQ2)) {
			if (i++ > 100000) {
				break;
			}
			fastQWrite.writeFastQRecord(fqs[0]);
			fastQWrite2.writeFastQRecord(fqs[1]);
		}
		
		fastQ.close();
		fastQ2.close();
		fastQWrite.close();
		fastQWrite2.close();
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
