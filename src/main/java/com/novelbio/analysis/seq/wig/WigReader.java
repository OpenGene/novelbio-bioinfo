package com.novelbio.analysis.seq.wig;

import java.io.IOException;

import org.broad.igv.bbfile.BBFileReader;
import org.broad.igv.bbfile.BigWigIterator;
import org.broad.igv.bbfile.WigItem;

public class WigReader {
	public static void main(String[] args) throws IOException {
		BBFileReader bbFileReader = new BBFileReader("/media/winE/NBC/Project/Project_MaHong/huangqiyue/out3.bw");
		BigWigIterator bi = bbFileReader.get.getBigWigIterator();
		WigItem wi;
		int i = 0;
		while ((wi = bi.next()) != null) {
			if (i++ > 20) {
				break;
			}
			System.out.println(wi.getChromosome() + "\t" + wi.getStartBase()  + "\t" + wi.getEndBase() + "\t" + wi.getWigValue());
		}
	}
}
