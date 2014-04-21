package com.novelbio.analysis.seq.wig;

import java.io.IOException;

import org.broad.igv.bbfile.BBFileReader;
import org.broad.igv.bbfile.BigWigIterator;
import org.broad.igv.bbfile.WigItem;
import org.broad.igv.bbfile.ZoomDataRecord;
import org.broad.igv.bbfile.ZoomLevelIterator;

public class WigReader {
	public static void main(String[] args) throws IOException {
//		BBFileReader bbFileReader = new BBFileReader("/media/winE/NBC/Project/Project_MaHong/huangqiyue/out3.bw");
		BBFileReader bbFileReader = new BBFileReader("/media/hdfs/nbCloud/public/customerData/Projects/DN14001/bam/IonXpress_013.bam.bw");

		BigWigIterator bi = bbFileReader.getBigWigIterator();
		WigItem wi;
		int i = 0;
		while ((wi = bi.next()) != null) {
			if (i++ > 20) {
				break;
			}
			System.out.println(wi.getChromosome() + "\t" + wi.getStartBase()  + "\t" + wi.getEndBase() + "\t" + wi.getWigValue());
		}
		System.out.println();
		System.out.println(bbFileReader.getBBFileHeader().getZoomLevels());
		System.out.println();
		ZoomLevelIterator zi = bbFileReader.getZoomLevelIterator(10);
		ZoomDataRecord zz = null;
		i = 0;
		while ((zz = zi.next()) != null) {
			if (i++ > 20) {
				break;
			}
			System.out.println(zz.getChromName() + "\t" + zz.getChromStart()  + "\t" + zz.getChromEnd() + "\t" + zz.getSumData());
		}
	}
}
