package com.novelbio.test;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.HashSet;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.resequencing.SnpAnnotation;
import com.novelbio.analysis.seq.rnaseq.GffHashMerge;
import com.novelbio.analysis.seq.rnaseq.TranscriptomStatistics;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.listOperate.HistBin;
import com.novelbio.base.dataStructure.listOperate.HistList;
import com.novelbio.base.plot.BarStyle;
import com.novelbio.base.plot.DotStyle;
import com.novelbio.base.plot.PlotScatter;
import com.novelbio.database.model.species.Species;
import com.novelbio.generalConf.NovelBioConst;


public class mytest {

	private static Logger logger = Logger.getLogger(mytest.class);
	
	public static void main(String[] args) {
		HistList histList = HistList.creatHistList(true);
		histList.setStartBin("", 0, 1);
		for (int i = 2; i < 10; i++) {
			histList.addHistBin("",i);
		}
		
		histList.addNum(5, 50);
		histList.addNum(6, 55);
		histList.addNum(7, 34);
		histList.addNum(8, 28);
		histList.addNum(9, 10);
		BarStyle dotStyle = new BarStyle();
		dotStyle.setColor(DotStyle.getGridentColorBrighter(Color.red));
		dotStyle.setColorEdge(DotStyle.getGridentColorBrighterTrans(Color.red));
//		dotStyle.setBasicStroke(new BasicStroke(5f));
		
		PlotScatter plotScatter = histList.getPlotHistBar(dotStyle, 20);
		plotScatter.setBg(Color.white);
		plotScatter.saveToFile("/home/zong0jie/Desktop/test/aaa.png", 1000, 1000);
	}
	
	private static void HG18() {
		SnpAnnotation snpAnnotation = new SnpAnnotation();
		GffChrAbs gffChrAbs = new GffChrAbs();
		gffChrAbs.setChrFile("/media/winE/Bioinformatics/genome/rice/tigr6.0/all.con", null);
		gffChrAbs.setGffFile(39947, NovelBioConst.GENOME_GFF_TYPE_TIGR, "/media/winE/Bioinformatics/genome/rice/tigr6.0/all.gff3");
		

		snpAnnotation.setGffChrAbs(gffChrAbs);
		snpAnnotation.addTxtSnpFile("/home/zong0jie/×ÀÃæ/geneID.txt", "/home/zong0jie/×ÀÃæ/geneID_Anno");
		snpAnnotation.setCol(1, 2, 3, 4);
		snpAnnotation.run();
//		
	}
}