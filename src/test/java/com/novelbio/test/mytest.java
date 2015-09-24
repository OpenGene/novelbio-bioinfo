package com.novelbio.test;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.stat.inference.TestUtils;
import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.analysis.seq.mirna.NovelMiRNADeep;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;


public class mytest {
	private static final Logger logger = Logger.getLogger(mytest.class);
	static boolean is;

	public static void main(String[] args) throws Exception {
//		TxtReadandWrite txtRead = new TxtReadandWrite("/hdfs:/nbCloud/public/nbcplatform/genome/species/9925/ncbi/ChromFa/chi_ref_CHIR_1.0_all_modify.mfa");
//		TxtReadandWrite txtWrite = new TxtReadandWrite("/hdfs:/nbCloud/public/nbcplatform/genome/species/9925/ncbi/ChromFa/chi_ref_CHIR_1.0_all.mfa", true);
//		for (String content : txtRead.readlines()) {
//			content = content.trim();
//			if (content.equals("")) {
//				continue;
//			}
//			txtWrite.writefileln(content);
//		}
//		txtRead.close();
//		txtWrite.close();
		logger.error("test");
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
