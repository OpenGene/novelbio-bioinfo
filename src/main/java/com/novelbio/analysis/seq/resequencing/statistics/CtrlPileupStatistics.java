package com.novelbio.analysis.seq.resequencing.statistics;

import java.awt.Color;

import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.base.dataStructure.listOperate.HistList;
import com.novelbio.base.plot.BarStyle;
import com.novelbio.base.plot.BoxStyle;
import com.novelbio.base.plot.DotStyle;
import com.novelbio.base.plot.PlotBox;
import com.novelbio.base.plot.PlotScatter;

public class CtrlPileupStatistics {
	public static void main(String[] args) {
		CtrlPileupStatistics ctrlPileupStatistics = new CtrlPileupStatistics();
		ctrlPileupStatistics.setCoverageBin(100, 1, 1000);
		ctrlPileupStatistics.setGffChrAbs(new GffChrAbs(39947));
		ctrlPileupStatistics.setMaxGapSize(200000);
		ctrlPileupStatistics.setPileupFile("/media/winE/NBC/Project/PGM/CombineAlignments_CA_yuli-all_yuli1-10_001_sorted_pileup.gz");
		ctrlPileupStatistics.startStatistics();
		ctrlPileupStatistics.plot("/media/winE/NBC/Project/PGM/plot/PGM");
	}

	StatisticsGenome statisticsGenome = new StatisticsGenome();
	//����ATͳ��
	StatisticsContinueATorCGCoverge statisticsContinueATcoverge = new StatisticsContinueATorCGCoverge(true);
	StatisticsContinueATorCGdestribution statisticsContinueATdestribution = new StatisticsContinueATorCGdestribution(true);
	//����CGͳ��
	StatisticsContinueATorCGCoverge statisticsContinueCGcoverge = new StatisticsContinueATorCGCoverge(false);
	StatisticsContinueATorCGdestribution statisticsContinueCGdestribution = new StatisticsContinueATorCGdestribution(false);
	//���Ƕ�ͳ��
	StatisticsCoverage statisticsCoverage = new StatisticsCoverage();
	//indel�������ͱ��ʵ�ͳ��
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
	
	/** �Ƿ��ͳ��exon capure������ */
	public void setExonOnly(boolean isExonOnly) {
		statisticsGenome.setExonOnly(isExonOnly);
	}
	
	/**
	 * <b>�����趨</b>
	 * ���ֶ�������ÿ���������interval
	 * @param binNum
	 * @param interval ����2�Ļ�������ÿ��2��coverageͳ��һ�£���˼����2reads���ǣ�4reads���ǵ�����
	 * @param maxCoverage  ���ֵ��������һλbin��û�����ֵ��������һ��bin�ͺ����ֵ�ϲ������Կ����������趨
	 */
	public void setCoverageBin(int binNum, int interval, int maxCoverage) {
		statisticsCoverage.setBinNum(binNum, interval, maxCoverage);
	}
	
	private void setStatisticUnits() {
		int ATorCGInterval = 2;
		int ATorCGcontinueMax = 50;
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
	
	public void startStatistics() {
		setStatisticUnits();
		addStatisticUnits();
		statisticsGenome.readAndRecord();
	}
	
	public void plot(String outPathAndPrefix) {
		try {
			plotExp(outPathAndPrefix);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	private void plotExp(String outPathAndPrefix) throws InterruptedException {
		if (!outPathAndPrefix.endsWith("/") && !outPathAndPrefix.endsWith("\\")) {
			outPathAndPrefix = outPathAndPrefix + "_";
		}
		System.out.println(1);
		BoxStyle boxStyle = new BoxStyle();
		boxStyle.setBasicStroke(0.3f);
		boxStyle.setColor(Color.blue);
		boxStyle.setColorBoxCenter(Color.red);
		boxStyle.setColorBoxEdge(Color.BLACK);
		boxStyle.setColorBoxWhisker(Color.black);
		boxStyle.setSize(DotStyle.SIZE_M);
		System.out.println(2);
		BarStyle barStyle = new BarStyle();
		barStyle.setColor(Color.blue);
		HistList histListCoverage = statisticsCoverage.getResultHistList();
		PlotScatter plotScatterCoverage = histListCoverage.getPlotHistBar(barStyle.clone());
		plotScatterCoverage.setBg(Color.white);
		plotScatterCoverage.saveToFile(outPathAndPrefix + "Coverage", 2000, 1000);
		Thread.sleep(100);
		System.out.println(7);

		
		DotStyle dotStyle = new DotStyle();
		dotStyle.setSize(DotStyle.SIZE_M);
		dotStyle.setStyle(DotStyle.STYLE_LINE);
		dotStyle.setColor(Color.blue);
		PlotScatter plotScatterIntegralCoverage = histListCoverage.getIntegralPlot(false, dotStyle.clone());
		Thread.sleep(100);
		System.out.println(8);
		
		plotScatterIntegralCoverage.setBg(Color.white);
		plotScatterIntegralCoverage.saveToFile(outPathAndPrefix + "Integral_Coverage", 2000, 1000);
		Thread.sleep(100);
		System.out.println(9);
		
		HistList histListIndelProp = statisticsIndelProp.getHistList();
		PlotScatter plotScatterIndelProp = histListIndelProp.getPlotHistBar(barStyle.clone());
		plotScatterIndelProp.setBg(Color.white);
		plotScatterIndelProp.saveToFile(outPathAndPrefix + "Indel_Prop", 2000, 1000);
		
		
	
		HistList histListATdestribution = statisticsContinueATdestribution.getHistList();
		PlotScatter plotScatterAT = histListATdestribution.getPlotHistBar(barStyle.clone());
		plotScatterAT.setBg(Color.white);
		plotScatterAT.saveToFile(outPathAndPrefix + "AT_Destribution", 2000, 1000);
		Thread.sleep(100);
		System.out.println(5);
		
		HistList histListCGdestribution = statisticsContinueCGdestribution.getHistList();
		PlotScatter plotScatterCG = histListCGdestribution.getPlotHistBar(barStyle.clone());
		plotScatterCG.setBg(Color.white);
		plotScatterCG.saveToFile(outPathAndPrefix + "CG_Destribution", 2000, 1000);
		Thread.sleep(100);
		System.out.println(6);
		

		
		PlotBox plotBoxAT = statisticsContinueATcoverge.getBoxPlotList().getPlotBox(boxStyle.clone());
		plotBoxAT.setBg(Color.white);
		plotBoxAT.saveToFile(outPathAndPrefix + "AT_coverage", 2000, 1000);
		Thread.sleep(100);
		
		
		PlotBox plotBoxCG = statisticsContinueCGcoverge.getBoxPlotList().getPlotBox(boxStyle.clone());
		plotBoxCG.setBg(Color.white);
		plotBoxCG.saveToFile(outPathAndPrefix + "CG_coverage", 2000, 1000);
		Thread.sleep(100);
		System.out.println(4);

	}
}
