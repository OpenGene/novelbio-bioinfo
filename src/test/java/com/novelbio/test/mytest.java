package com.novelbio.test;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.fastq.FastQRecordFilter;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReads;
import com.novelbio.analysis.seq.resequencing.SnpAnnotation;
import com.novelbio.analysis.seq.rnaseq.GffHashMerge;
import com.novelbio.analysis.seq.rnaseq.TranscriptomStatistics;
import com.novelbio.base.dataOperate.HttpFetch;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.listOperate.BoxPlotList;
import com.novelbio.base.dataStructure.listOperate.HistBin;
import com.novelbio.base.dataStructure.listOperate.HistList;
import com.novelbio.base.plot.BarStyle;
import com.novelbio.base.plot.BoxStyle;
import com.novelbio.base.plot.DotStyle;
import com.novelbio.base.plot.PlotBox;
import com.novelbio.base.plot.PlotScatter;
import com.novelbio.database.domain.geneanno.AGene2Go;
import com.novelbio.database.domain.geneanno.Go2Term;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.model.species.Species;
import com.novelbio.generalConf.NovelBioConst;

import de.erichseifert.gral.plots.BoxPlot;


public class mytest {

	private static Logger logger = Logger.getLogger(mytest.class);
	
	public static void main(String[] args) {
		GeneID geneID = new GeneID("F1T0I5", 9606);
		ArrayList<AGene2Go> ls = geneID.getGene2GO(Go2Term.GO_ALL);
		for (AGene2Go aGene2Go : ls) {
			System.out.println(aGene2Go.getGOID());
		}
		
	}
	
	private void plotHist() {
		HistList histList = HistList.creatHistList(true);
		histList.setStartBin(1, "", 0, 1);
		for (int i = 2; i < 10; i++) {
			histList.addHistBin(i, "", i);
		}
		
		histList.addNum(5, 50);
		histList.addNum(6, 55);
		histList.addNum(7, 34);
		histList.addNum(8, 28);
		histList.addNum(9, 10);
		
		BarStyle dotStyle = new BarStyle();
		dotStyle.setColor(DotStyle.getGridentColorBrighter(Color.gray));
		dotStyle.setColorEdge(DotStyle.getGridentColorBrighterTrans(Color.blue));
//		dotStyle.setBasicStroke(new BasicStroke(5f));
		
		PlotScatter plotScatter = histList.getPlotHistBar(dotStyle);
		plotScatter.setBg(Color.white);
		plotScatter.saveToFile("/home/zong0jie/Desktop/test/aaa3.png", 1000, 1000);
	}
	
	private static void HG18() {
		SnpAnnotation snpAnnotation = new SnpAnnotation();
		GffChrAbs gffChrAbs = new GffChrAbs();
		gffChrAbs.setChrFile("/media/winE/Bioinformatics/genome/rice/tigr6.0/all.con", null);
		gffChrAbs.setGffFile(39947, NovelBioConst.GENOME_GFF_TYPE_TIGR, "/media/winE/Bioinformatics/genome/rice/tigr6.0/all.gff3");
		

		snpAnnotation.setGffChrAbs(gffChrAbs);
		snpAnnotation.addTxtSnpFile("/home/zong0jie/����/geneID.txt", "/home/zong0jie/����/geneID_Anno");
		snpAnnotation.setCol(1, 2, 3, 4);
		snpAnnotation.run();
//		
	}
}