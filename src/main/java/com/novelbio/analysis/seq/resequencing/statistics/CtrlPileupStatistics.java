package com.novelbio.analysis.seq.resequencing.statistics;

import java.awt.Color;

import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.base.dataStructure.listOperate.HistList;
import com.novelbio.base.plot.BarStyle;
import com.novelbio.base.plot.BoxStyle;
import com.novelbio.base.plot.PlotBox;
import com.novelbio.base.plot.PlotScatter;

public class CtrlPileupStatistics {
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
	 * @param maxCoverageNum  ���ֵ��������һλbin��û�����ֵ��������һ��bin�ͺ����ֵ�ϲ������Կ����������趨
	 */
	public void setCoverageBin(int binNum, int interVal, int maxCoverage) {
		statisticsCoverage.setBinNum(binNum, interVal, maxCoverage);
	}
	
	private void setStatisticUnits() {
		int ATorCGInterval = 2;
		int ATorCGcontinueMax = 50;
		statisticsContinueATcoverge.setCgInterval(ATorCGInterval);
		statisticsContinueATcoverge.setMaxContinueATorCG(ATorCGcontinueMax);
		statisticsContinueCGcoverge.setCgInterval(ATorCGInterval);
		statisticsContinueCGdestribution.setMaxContinueATorCG(ATorCGcontinueMax);
		
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
		if (!outPathAndPrefix.endsWith("/") && !outPathAndPrefix.endsWith("\\")) {
			outPathAndPrefix = outPathAndPrefix + "_";
		}
		BoxStyle boxStyle = new BoxStyle();
		boxStyle.setBasicStroke(0.3f);
		boxStyle.setColor(Color.blue);
		boxStyle.setColorBoxCenter(Color.red);
		boxStyle.setColorBoxEdge(Color.BLACK);
		boxStyle.setColorBoxWhisker(Color.darkGray);
		PlotBox plotBoxAT = statisticsContinueATcoverge.getBoxPlotList().getPlotBox(boxStyle);
		plotBoxAT.saveToFile(outPathAndPrefix + "ATcoverage", 2000, 1000);
		
		PlotBox plotBoxCG = statisticsContinueCGcoverge.getBoxPlotList().getPlotBox(boxStyle);
		plotBoxCG.saveToFile(outPathAndPrefix + "CGcoverage", 2000, 1000);
		
		
		BarStyle barStyle = new BarStyle();
		barStyle.setColor(Color.blue);
		HistList histListATdestribution = statisticsContinueATdestribution.getHistList();
		PlotScatter plotScatter = histListATdestribution.getPlotHistBar(barStyle);
		plotScatter.saveToFile(outPathAndPrefix + "Coverage", 2000, 1000);
	}
}
