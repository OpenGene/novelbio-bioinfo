package com.novelbio.analysis.seq.wig;

import java.io.IOException;

import org.broad.igv.bbfile.BBFileReader;
import org.broad.igv.bbfile.BigWigIterator;
import org.broad.igv.bbfile.WigItem;
import org.broad.igv.bbfile.ZoomDataRecord;
import org.broad.igv.bbfile.ZoomLevelIterator;

import com.novelbio.analysis.seq.genome.mappingOperate.MapReadsAbs;
import com.novelbio.base.dataOperate.DateUtil;

public class WigReader extends MapReadsAbs {
	public static void main(String[] args) throws IOException {
//		BBFileReader bbFileReader = new BBFileReader("/media/winE/NBC/Project/Project_MaHong/huangqiyue/out3.bw");
		DateUtil dateUtil = new DateUtil();
		dateUtil.setStartTime();
		BBFileReader bbFileReader = new BBFileReader("/media/hdfs/nbCloud/public/customerData/Projects/DN14001/bam/IonXpress_013.bam.bw");
		int num1 = 0;
		int num2 = 0;
		for (int i = 0; i < 10000000; i +=1500) {
			BigWigIterator bi = bbFileReader.getBigWigIterator("chr20", i, "chr20", i+1000, false);
			num1++;
			if (!bi.hasNext()) {
				continue;
			}
			num2++;
			WigItem wi;
			int j = 0;
			while ((wi = bi.next()) != null) {
			}
		}
		System.out.println(dateUtil.getElapseTime());
		System.out.println(num1);
		System.out.println(num2);
//		BigWigIterator bi = bbFileReader.getBigWigIterator();
//		WigItem wi;
//		int i = 0;
//		while ((wi = bi.next()) != null) {
//			if (i++ > 20) {
//				break;
//			}
//			System.out.println(wi.getChromosome() + "\t" + wi.getStartBase()  + "\t" + wi.getEndBase() + "\t" + wi.getWigValue());
//		}
//		System.out.println();
//		System.out.println(bbFileReader.getBBFileHeader().getZoomLevels());
//		System.out.println();
//		ZoomLevelIterator zi = bbFileReader.getZoomLevelIterator(10);
//		ZoomDataRecord zz = null;
//		i = 0;
//		while ((zz = zi.next()) != null) {
//			if (i++ > 20) {
//				break;
//			}
//			System.out.println(zz.getChromName() + "\t" + zz.getChromStart()  + "\t" + zz.getChromEnd() + "\t" + zz.getSumData());
//		}
	}

	@Override
	protected void ReadMapFileExp() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double[] getRangeInfo(int thisInvNum, String chrID, int startNum,
			int endNum, int type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected double[] getRangeInfo(String chrID, int startNum, int endNum,
			int binNum, int type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected long getAllReadsNum() {
		// TODO Auto-generated method stub
		return 0;
	}
}
