package com.novelbio.test.analysis.seq.resequencing.statistics;

import java.awt.Color;

import com.novelbio.analysis.seq.resequencing.statistics.StatisticsGenome;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.listOperate.HistList;
import com.novelbio.base.plot.BarStyle;
import com.novelbio.base.plot.PlotScatter;
import com.novelbio.database.model.species.Species;

import junit.framework.TestCase;

public class TestStatisticsGenome extends TestCase {
	StatisticsGenome statisticsGenome;
	
	@Override
	protected void setUp() throws Exception {
		statisticsGenome = new StatisticsGenome();
		Species species = new Species(9606);
		statisticsGenome.setSpecies(species);

		statisticsGenome.setGapMaxNum(30);
		statisticsGenome.setSnpDetectThreshold(10);
		statisticsGenome.setReadsNum(2);
		statisticsGenome.setSameNumSet(3);
		statisticsGenome.setATBoxPlotList();
		
		statisticsGenome.setBoxPlotList();
		
		HistList InserthistList = statisticsGenome.getInsertHistList();
		statisticsGenome.setHistListStyle(InserthistList, 15, 1, 1500);

		HistList delHistList = statisticsGenome.getDeletionHistList();
		statisticsGenome.setHistListStyle(delHistList, 15, 1, 1500);

		HistList cGNumHistList = statisticsGenome.getCG2NumHistList();
		statisticsGenome.setHistListStyle(cGNumHistList, 15, 1, 1500);

		HistList aTNumHistList = statisticsGenome.getAT2NumHistList();
		statisticsGenome.setHistListStyle(aTNumHistList, 15, 1, 1500);
		
		HistList insertNumHistList = statisticsGenome.getInsertNumHistList();
		statisticsGenome.setHistListStyle(insertNumHistList, 15, 1, 1500);
		
		HistList delNumHistList = statisticsGenome.getDelectionNumHistList();
		statisticsGenome.setHistListStyle(delNumHistList, 15, 1, 1500);

		statisticsGenome.readAndRecord("src/main/resources/Test/testPileUpFile.txt");
		BarStyle dotStyle = new BarStyle();
		dotStyle.setColor(Color.blue);
		dotStyle.setColorEdge(Color.black);

		PlotScatter insertNumPlotScatter = insertNumHistList.getPlotHistBar(dotStyle);
		insertNumPlotScatter.setBg(Color.white);
		insertNumPlotScatter.saveToFile("/home/ywd/draw/inertNum.png", 1000, 1000);
		
		PlotScatter delNumPlotScatter = delNumHistList.getPlotHistBar(dotStyle);
		delNumPlotScatter.setBg(Color.white);
		delNumPlotScatter.saveToFile("/home/ywd/draw/DelNum.png", 1000, 1000);
		
		
		PlotScatter insertPlotScatter = InserthistList.getPlotHistBar(dotStyle);
		insertPlotScatter.setBg(Color.white);
		insertPlotScatter.saveToFile("/home/ywd/draw/inert.png", 1000, 1000);

		PlotScatter deletionPlotScatter = delHistList.getPlotHistBar(dotStyle);
		deletionPlotScatter.setBg(Color.white);
		deletionPlotScatter.saveToFile("/home/ywd/draw/delect.png", 1000, 1000);

		PlotScatter plotScatterCGnum = cGNumHistList.getPlotHistBar(dotStyle);
		plotScatterCGnum.setBg(Color.white);
		plotScatterCGnum.saveToFile("/home/ywd/draw/CG2num.png", 1000, 1000);

		PlotScatter plotScatterATnum = aTNumHistList.getPlotHistBar(dotStyle);
		plotScatterATnum.setBg(Color.white);
		plotScatterATnum.saveToFile("/home/ywd/draw/AT2num.png", 1000, 1000);

		statisticsGenome.drawATBox("/home/ywd/draw/ATBox.png");
		statisticsGenome.drawCGBox("/home/ywd/draw/CGBox.png");

		HistList histListReadsCover = statisticsGenome.getHistListReadsCover();
		statisticsGenome.setHistListStyle(histListReadsCover, 50, 1, 15000);

		statisticsGenome.getAllReadsCover2Num();
		statisticsGenome.addNumReadsCoverHistList();
		PlotScatter plotScatterReadsNum = histListReadsCover.getPlotHistBar(dotStyle);
		plotScatterReadsNum.setBg(Color.white);
		plotScatterReadsNum.saveToFile("/home/ywd/draw/allReads2num.png", 1000,1000);

		HistList histListReadsStack = statisticsGenome.getHistListReadsStack();
		statisticsGenome.setHistListStyle(histListReadsStack, 50, 5, 15000);
		statisticsGenome.addNumStack();
		PlotScatter plotScatterReadsStack = histListReadsStack.getPlotHistBar(dotStyle);
		plotScatterReadsStack.setBg(Color.white);
		plotScatterReadsStack.saveToFile("/home/ywd/draw/allReads2numStack.png", 1000,1000);
		
		
	}
	
	@Override
	protected void tearDown() throws Exception {
		
	}
	
	public void testInfo() {
		HistList histList = statisticsGenome.getDelectionNumHistList();
		assertEquals(3, histList.get(6).getCountNumber());
		
	}
}
