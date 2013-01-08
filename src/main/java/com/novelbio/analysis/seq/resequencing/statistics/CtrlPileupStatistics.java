package com.novelbio.analysis.seq.resequencing.statistics;

import java.awt.Color;

import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.base.dataStructure.listOperate.HistList;
import com.novelbio.base.plot.BarStyle;
import com.novelbio.base.plot.BoxStyle;
import com.novelbio.base.plot.DotStyle;
import com.novelbio.base.plot.PlotBox;
import com.novelbio.base.plot.PlotScatter;

//TODO 没有做清空StatisticUnit的工作
public class CtrlPileupStatistics {
	public static void main(String[] args) {
//		CtrlPileupStatistics ctrlPileupStatistics = new CtrlPileupStatistics();
//		ctrlPileupStatistics.setCoverageBin(100, 1, 1000);
//		ctrlPileupStatistics.setGffChrAbs(new GffChrAbs(39947));
//		ctrlPileupStatistics.setMaxGapSize(0);
//		ctrlPileupStatistics.setPileupFile("/media/winE/NBC/Project/PGM/CombineAlignments_CA_yuli-all_yuli1-10_001_sorted_pileup.gz");
//		ctrlPileupStatistics.startStatistics();
//		ctrlPileupStatistics.plot("/media/winE/NBC/Project/PGM/plot/PGM");
		
		GffChrAbs gffChrAbs = new GffChrAbs(39947);
		String parentPath = "/media/winF/NBC/Project/Project_ZDB_Lab/QXL/Project_ZDB/pileup/";
		
		CtrlPileupStatistics ctrlPileupStatistics = new CtrlPileupStatistics();
//		ctrlPileupStatistics.setCoverageBin(100, 1, 1000);
//		ctrlPileupStatistics.setGffChrAbs(gffChrAbs);
//		ctrlPileupStatistics.setMaxGapSize(0);
//		ctrlPileupStatistics.setPileupFile(parentPath+"Q60-1_sorted_realign_removeDuplicate_pileup.gz");
//		ctrlPileupStatistics.startStatistics();
//		ctrlPileupStatistics.plot(parentPath, "Q60-1");
		
//		ctrlPileupStatistics = new CtrlPileupStatistics();
//		ctrlPileupStatistics.setCoverageBin(100, 1, 1000);
//		ctrlPileupStatistics.setGffChrAbs(gffChrAbs);
//		ctrlPileupStatistics.setMaxGapSize(0);
//		ctrlPileupStatistics.setPileupFile(parentPath+"TF142-3_sorted_realign_removeDuplicate_pileup.gz");
//		ctrlPileupStatistics.startStatistics();
//		ctrlPileupStatistics.plot(parentPath, "TF142-3");
		
		ctrlPileupStatistics = new CtrlPileupStatistics();
		ctrlPileupStatistics.setCoverageBin(100, 1, 1000);
		ctrlPileupStatistics.setGffChrAbs(gffChrAbs);
		ctrlPileupStatistics.setMaxGapSize(0);
		ctrlPileupStatistics.setPileupFile(parentPath+"TF75-4_sorted_realign_removeDuplicate_pileup.gz");
		ctrlPileupStatistics.startStatistics();
		ctrlPileupStatistics.plot(parentPath, "TF75-4");
		
		ctrlPileupStatistics = new CtrlPileupStatistics();
		ctrlPileupStatistics.setCoverageBin(100, 1, 1000);
		ctrlPileupStatistics.setGffChrAbs(gffChrAbs);
		ctrlPileupStatistics.setMaxGapSize(0);
		ctrlPileupStatistics.setPileupFile(parentPath+"TF182-1_sorted_realign_removeDuplicate_pileup.gz");
		ctrlPileupStatistics.startStatistics();
		ctrlPileupStatistics.plot(parentPath, "TF182-1");
		
		ctrlPileupStatistics = new CtrlPileupStatistics();
		ctrlPileupStatistics.setCoverageBin(100, 1, 1000);
		ctrlPileupStatistics.setGffChrAbs(gffChrAbs);
		ctrlPileupStatistics.setMaxGapSize(0);
		ctrlPileupStatistics.setPileupFile(parentPath+"TF57-1_sorted_realign_removeDuplicate_pileup.gz");
		ctrlPileupStatistics.startStatistics();
		ctrlPileupStatistics.plot(parentPath, "TF57-1");
		
		ctrlPileupStatistics = new CtrlPileupStatistics();
		ctrlPileupStatistics.setCoverageBin(100, 1, 1000);
		ctrlPileupStatistics.setGffChrAbs(gffChrAbs);
		ctrlPileupStatistics.setMaxGapSize(0);
		ctrlPileupStatistics.setPileupFile(parentPath+"TF81-2_sorted_realign_removeDuplicate_pileup.gz");
		ctrlPileupStatistics.startStatistics();
		ctrlPileupStatistics.plot(parentPath, "TF81-2");
	
	}

	StatisticsGenome statisticsGenome = new StatisticsGenome();
	//连续AT统计
	StatisticsContinueATorCGCoverge statisticsContinueATcoverge = new StatisticsContinueATorCGCoverge(true);
	StatisticsContinueATorCGdestribution statisticsContinueATdestribution = new StatisticsContinueATorCGdestribution(true);
	//连续CG统计
	StatisticsContinueATorCGCoverge statisticsContinueCGcoverge = new StatisticsContinueATorCGCoverge(false);
	StatisticsContinueATorCGdestribution statisticsContinueCGdestribution = new StatisticsContinueATorCGdestribution(false);
	//覆盖度统计
	StatisticsCoverage statisticsCoverage = new StatisticsCoverage();
	//indel的数量和比率的统计
	StatisticsIndelProp statisticsIndelProp = new StatisticsIndelProp();
	
	public void setMaxGapSize(int gapMaxNum) {
		statisticsGenome.setGapMaxNum(gapMaxNum);
	}
	
	public void setPileupFile(String pileUpFile) {
		statisticsGenome.setPileupFile(pileUpFile);
	}
	
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		statisticsGenome.setGffChrAbs(gffChrAbs);
	}
	
	/** 是否仅统计exon capure的数据 */
	public void setExonOnly(boolean isExonOnly) {
		statisticsGenome.setExonOnly(isExonOnly);
	}
	
	/**
	 * <b>必须设定</b>
	 * 划分多少区域，每个区域多少interval
	 * @param binNum
	 * @param interval 等于2的话，就是每隔2个coverage统计一下，意思就是2reads覆盖，4reads覆盖的数量
	 * @param maxCoverage  最大值，如果最后一位bin都没到最大值，接下来一个bin就和最大值合并，所以可以往高里设定
	 */
	public void setCoverageBin(int binNum, int interval, int maxCoverage) {
		statisticsCoverage.setBinNum(binNum, interval, maxCoverage);
	}
	

	
	public void startStatistics() {
		setStatisticUnits();
		addStatisticUnits();
		statisticsGenome.readAndRecord();
	}
	private void setStatisticUnits() {
		int ATorCGInterval = 2;
		int ATorCGcontinueMax = 50;
		
		//清空
//		statisticsContinueATcoverge = new StatisticsContinueATorCGCoverge(true);
//		statisticsContinueATdestribution = new StatisticsContinueATorCGdestribution(true);
//		statisticsContinueCGcoverge = new StatisticsContinueATorCGCoverge(false);
//		statisticsContinueCGdestribution = new StatisticsContinueATorCGdestribution(false);
//		statisticsCoverage = new StatisticsCoverage();
//		statisticsIndelProp = new StatisticsIndelProp();
		
		//设置
		statisticsContinueATcoverge.setCgInterval(ATorCGInterval);
		statisticsContinueATcoverge.setMaxContinueATorCG(ATorCGcontinueMax);
		statisticsContinueATcoverge.setBoxPlotList();

		statisticsContinueCGcoverge.setCgInterval(ATorCGInterval);
		statisticsContinueCGcoverge.setMaxContinueATorCG(ATorCGcontinueMax);
		statisticsContinueCGcoverge.setBoxPlotList();

		statisticsContinueATdestribution.setBinNum(50, 1, 100);
		statisticsContinueCGdestribution.setMaxContinueATorCG(ATorCGcontinueMax);
		
		statisticsContinueATdestribution.setMaxContinueATorCG(ATorCGcontinueMax);
		statisticsContinueCGdestribution.setBinNum(50, 1, 100);
				
		statisticsIndelProp.setBinNum(100);
	}
	
	private void addStatisticUnits() {
		statisticsGenome.clearStatisticUnits();
		
		statisticsGenome.addStatisticUnits(statisticsContinueATcoverge);
		statisticsGenome.addStatisticUnits(statisticsContinueATdestribution);
		
		statisticsGenome.addStatisticUnits(statisticsContinueCGcoverge);
		statisticsGenome.addStatisticUnits(statisticsContinueCGdestribution);
		
		statisticsGenome.addStatisticUnits(statisticsCoverage);
		statisticsGenome.addStatisticUnits(statisticsIndelProp);
	}
	
	public void plot(String outPathAndPrefix, String suffix) {
		try {
			plotExp(outPathAndPrefix, suffix);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	private void plotExp(String outPathAndPrefix, String suffix) throws InterruptedException {
		if (!outPathAndPrefix.endsWith("/") && !outPathAndPrefix.endsWith("\\")) {
			outPathAndPrefix = outPathAndPrefix + "_";
		}
		if (suffix != null && suffix.trim().equals("")) {
			suffix = "_" + suffix.trim();
		}
		System.out.println(1);
		BoxStyle boxStyle = new BoxStyle();
		boxStyle.setBasicStroke(2f);
		boxStyle.setColor(DotStyle.getGridentColorBrighter(Color.BLUE));
		boxStyle.setColorBoxCenter(Color.red);
		boxStyle.setColorBoxEdge(Color.BLACK);
		boxStyle.setColorBoxWhisker(Color.BLACK);
		boxStyle.setSize(DotStyle.SIZE_M);
		
		System.out.println(2);
		BarStyle barStyle = new BarStyle();
		barStyle.setColor(DotStyle.getGridentColorBrighter(Color.BLUE));
		HistList histListCoverage = statisticsCoverage.getResultHistList();
		PlotScatter plotScatterCoverage = histListCoverage.getPlotHistBar(barStyle.clone());
		plotScatterCoverage.setBg(Color.white);
		plotScatterCoverage.saveToFile(outPathAndPrefix + "Coverage" + suffix, 2000, 1000);
		Thread.sleep(100);
		System.out.println(7);

		
		DotStyle dotStyle = new DotStyle();
		dotStyle.setSize(DotStyle.SIZE_M);
		dotStyle.setStyle(DotStyle.STYLE_LINE);
		dotStyle.setColor(DotStyle.getGridentColorBrighter(Color.BLUE));
		PlotScatter plotScatterIntegralCoverage = histListCoverage.getIntegralPlot(false, dotStyle.clone());
		Thread.sleep(100);
		System.out.println(8);
		
		plotScatterIntegralCoverage.setBg(Color.white);
		plotScatterIntegralCoverage.saveToFile(outPathAndPrefix + "Integral_Coverage" + suffix, 2000, 1000);
		Thread.sleep(100);
		System.out.println(9);
		
		HistList histListIndelProp = statisticsIndelProp.getHistList();
		PlotScatter plotScatterIndelProp = histListIndelProp.getPlotHistBar(barStyle.clone());
		plotScatterIndelProp.setBg(Color.white);
		plotScatterIndelProp.saveToFile(outPathAndPrefix + "Indel_Prop" + suffix, 2000, 1000);
		
		
	
		HistList histListATdestribution = statisticsContinueATdestribution.getHistList();
		PlotScatter plotScatterAT = histListATdestribution.getPlotHistBar(barStyle.clone());
		plotScatterAT.setBg(Color.white);
		plotScatterAT.saveToFile(outPathAndPrefix + "AT_Destribution" + suffix, 2000, 1000);
		Thread.sleep(100);
		System.out.println(5);
		
		HistList histListCGdestribution = statisticsContinueCGdestribution.getHistList();
		PlotScatter plotScatterCG = histListCGdestribution.getPlotHistBar(barStyle.clone());
		plotScatterCG.setBg(Color.white);
		plotScatterCG.saveToFile(outPathAndPrefix + "CG_Destribution" + suffix, 2000, 1000);
		Thread.sleep(100);
		System.out.println(6);
		
		//TODO boxplot有问题
		try {
			PlotBox plotBoxAT = statisticsContinueATcoverge.getBoxPlotList().getPlotBox(boxStyle.clone());
			plotBoxAT.setBg(Color.white);
			plotBoxAT.saveToFile(outPathAndPrefix + "AT_coverage" + suffix, 2000, 1000);
			Thread.sleep(100);
			
			
			PlotBox plotBoxCG = statisticsContinueCGcoverge.getBoxPlotList().getPlotBox(boxStyle.clone());
			plotBoxCG.setBg(Color.white);
			plotBoxCG.saveToFile(outPathAndPrefix + "CG_coverage" + suffix, 2000, 1000);
			Thread.sleep(100);
			System.out.println(4);
		} catch (Exception e) {
			// TODO: handle exception
		}


	}
}
