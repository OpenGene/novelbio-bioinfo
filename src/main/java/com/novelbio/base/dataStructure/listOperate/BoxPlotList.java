package com.novelbio.base.dataStructure.listOperate;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.plot.BoxStyle;
import com.novelbio.base.plot.PlotBox;
import com.novelbio.base.plot.PlotBox.BoxInfo;

public class BoxPlotList {
	/** key����Сд */
	LinkedHashMap<String, HistList> mapName2HistList = new LinkedHashMap<String, HistList>();
	
	PlotBox plotBox;
	/** ���box���������߾����������ж೤�������趨 setAxisY �� */
	double margen = 0.05;
	public void addHistList(HistList histList) {
		mapName2HistList.put(histList.getName().toLowerCase(), histList);
	}
	public HistList getHistList(String histListName) {
		return mapName2HistList.get(histListName.toLowerCase());
	}
	
	public void setPlotBox(PlotBox plotBox) {
		this.plotBox = plotBox;
	}
	
	public PlotBox getPlotBox(BoxStyle boxStyle) {
		if (plotBox == null) {
			plotBox = new PlotBox();
		}
		
		plotBox.setBoxPlot(getLsBoxInfo(), boxStyle);
		plotBox.setAxisX(0, mapName2HistList.size() + 1);
		double len = getMax() - getMin();
		plotBox.setAxisY(getMin() - margen*len, getMax() + margen*len);
		return plotBox;
	}
	
	private double getMin() {
		ArrayList<Double> lsMin = new ArrayList<Double>();
		for (HistList histList : mapName2HistList.values()) {
			lsMin.add(histList.getBoxInfo().getInfoBoxDefMin());
		}
		return MathComput.min(lsMin);
	}
	
	private double getMax() {
		ArrayList<Double> lsMax = new ArrayList<Double>();
		for (HistList histList : mapName2HistList.values()) {
			lsMax.add(histList.getBoxInfo().getInfoBoxDefMax());
		}
		return MathComput.max(lsMax);
	}
	
	public ArrayList<BoxInfo> getLsBoxInfo() {
		ArrayList<BoxInfo> lsBoxInfos = new ArrayList<BoxInfo>();
		for (HistList histList : mapName2HistList.values()) {
			lsBoxInfos.add(histList.getBoxInfo());
		}
		return lsBoxInfos;
	}
	
}
