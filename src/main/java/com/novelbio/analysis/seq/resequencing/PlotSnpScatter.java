package com.novelbio.analysis.seq.resequencing;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.plot.DotStyle;
import com.novelbio.base.plot.PlotScatter;

public class PlotSnpScatter {
	public static void main(String[] args) {
		String excelTxt = "/home/zong0jie/×ÀÃæ/9522snpvsZDBQ60-1_filtered.xls";
		PlotSnpScatter plotSnpScatter = new PlotSnpScatter();
		plotSnpScatter.readInfo(excelTxt, 1, 2, 9);
		plotSnpScatter.plot("/media/winF/NBC/Project/Project_ZDB_Lab/QXL/Project_ZDB/plot/QXL");
	}
	//12 52 29
	ArrayList<String[]> lsInfo;
	ArrayListMultimap<String, SnpAxis> mapChrID2SnpAxis = ArrayListMultimap.create();
	DotStyle dotStyle;
	
	public PlotSnpScatter() {
		dotStyle = new DotStyle();
		dotStyle.setColor(Color.blue);
		dotStyle.setStyle(DotStyle.STYLE_CYCLE);
		dotStyle.setSize(DotStyle.SIZE_B);
		dotStyle.setValueVisible(true);
		
	}
	
	public void readInfo(String excelTxt, int colChrID, int colLocation, int colRatio) {
		lsInfo = ExcelTxtRead.readLsExcelTxt(excelTxt, new int[]{colChrID, colLocation, colRatio}, 2, -1);
		arrange();
	}
	
	
	private void arrange() {
		for (String[] strings : lsInfo) {
			SnpAxis snpAxis = new SnpAxis(strings[1], strings[2]);
			mapChrID2SnpAxis.put(strings[0], snpAxis);
		}
		
		for (String chrID : mapChrID2SnpAxis.keySet()) {
			List<SnpAxis> lsSnpAxis = mapChrID2SnpAxis.get(chrID);
			Collections.sort(lsSnpAxis);
		}
	}
	
	public void plot(String saveTo) {
		for (String chrID : mapChrID2SnpAxis.keySet()) {
			ArrayList<double[]> lsXY = new ArrayList<double[]>();
			List<SnpAxis> lsSnpAxis = mapChrID2SnpAxis.get(chrID);
			for (SnpAxis snpAxis : lsSnpAxis) {
				if (snpAxis.isOK()) {
					lsXY.add(snpAxis.ToAxis());
				}
			}
			if (lsXY.size() == 0) {
				continue;
			}
			PlotScatter plotScatter = new PlotScatter(PlotScatter.PLOT_TYPE_SCATTERPLOT);
			plotScatter.addXY(lsXY, dotStyle);
			plotScatter.setBg(Color.white);
			plotScatter.setInsets(PlotScatter.INSETS_SIZE_SM);
			plotScatter.setTitle(chrID, null);
			plotScatter.setTitleX("ChrLength", null, 0);
			plotScatter.setTitleY("Ratio", null, 0);
			plotScatter.setAxisX(0, lsXY.get(lsXY.size() - 1)[0]);
			plotScatter.setAxisY(0, 1.2);
			plotScatter.saveToFile(saveTo + chrID, 1000, 1000);
		}
	}
}

class SnpAxis implements Comparable<SnpAxis> {
	double location;
	double ratio;
	
	public SnpAxis(double chrLoc, double ratio) {
		this.location = chrLoc;
		this.ratio = ratio;
	}
	
	public SnpAxis(String chrLoc, String ratio) {
		this.location = Double.parseDouble(chrLoc);
		this.ratio = Double.parseDouble(ratio);
	}
	
	@Override
	public int compareTo(SnpAxis o) {
		Double location1 = location;
		Double location2 = o.location;
		return location1.compareTo(location2);
	}
	
	public boolean isOK() {
		if (ratio > 0.5) {
			return true;
		}
		return false;
	}
	
	public double[] ToAxis() {
		return new double[]{location, ratio};
	}
}