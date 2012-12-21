package com.novelbio.base.dataStructure.listOperate;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.plot.BarStyle;
import com.novelbio.base.plot.DotStyle;
import com.novelbio.base.plot.PlotScatter;
import com.novelbio.base.plot.PlotBox.BoxInfo;

public abstract class HistList extends ListAbsSearch<HistBin, ListCodAbs<HistBin>, ListCodAbsDu<HistBin,ListCodAbs<HistBin>>> {
	private static final Logger logger = Logger.getLogger(HistList.class);
	private static final long serialVersionUID = 1481673037539688125L;
	
	/** �ܹ��������� */
	long allNum = 0;
	HistBinType histBinType = HistBinType.LopenRclose;
	
	PlotScatter plotScatter;
	
	/**
	 * Ĭ�������ұ�
	 * @param histBinType
	 */
	public void setHistBinType(HistBinType histBinType) {
		this.histBinType = histBinType;
	}
	
	@Override
	protected ListCodAbs<HistBin> creatGffCod(String listName, int Coordinate) {
		ListCodAbs<HistBin> lsAbs = new ListCodAbs<HistBin>(listName, Coordinate);
		return lsAbs;
	}

	@Override
	protected ListCodAbsDu<HistBin, ListCodAbs<HistBin>> creatGffCodDu(
			ListCodAbs<HistBin> gffCod1, ListCodAbs<HistBin> gffCod2) {
		ListCodAbsDu<HistBin, ListCodAbs<HistBin>> lsResult= new ListCodAbsDu<HistBin, ListCodAbs<HistBin>>(gffCod1, gffCod2);
		return lsResult;
	}
	
	/**
	 * ��õ�ÿһ����Ϣ����ʵ�ʵĶ�û��clone
	 * ����PeakNum���͵���Chr��list��Ϣ ���ظ�PeakNum������LOCID���;���λ��
	 * ����clone�ķ��������Ϣ
	 * û�ҵ��ͷ���null
	 */
	@Deprecated
	public ListCodAbs<HistBin> searchLocation(int Coordinate) {
		return super.searchLocation(Coordinate);
	}
	@Deprecated
	public boolean add(HistBin e) {
		// TODO Auto-generated method stub
		return super.add(e);
	}
	/**
	 * ����˫�����ѯ�Ľ�����ڲ��Զ��ж� cod1 �� cod2�Ĵ�С
	 * ���cod1 ��cod2 ��һ��С��0����ô���겻���ڣ��򷵻�null
	 * @param chrID �ڲ��Զ�Сд
	 * @param cod1 �������0
	 * @param cod2 �������0
	 * @return
	 */
	@Deprecated
	public ListCodAbsDu<HistBin, ListCodAbs<HistBin>> searchLocationDu(int cod1, int cod2) {
		return super.searchLocationDu(cod1, cod2);
	}
	
	public void setPlotScatter(PlotScatter plotScatter) {
		this.plotScatter = plotScatter;
	}
	
	/**
	 * �Զ�����histlist��bin��ÿ��interval����һλ�����־���interval
	 * @param histList
	 * @param binNum bin�ĸ���
	 * @param interval ���
	 * @param maxSize ���ֵ��������һλbin��û�����ֵ��������һ��bin�ͺ����ֵ�ϲ�
	 */
	public void setBinAndInterval(int binNum, int interval,int maxSize) {
		clear();
		setStartBin(interval, interval + "", 0, interval);
		int binNext = interval*2;
		for (int i = 1; i < binNum; i++) {
			addHistBin(binNext, binNext + "", binNext);
			binNext = binNext + interval;
		}
		if (binNext < maxSize) {
			addHistBin(binNext, binNext + "", maxSize);
		}
	}
	
	/**
	 * �������
	 * @param number ��bin���������ֵ��null�����յ������ƽ��ֵ
	 * @param name
	 * @param start
	 * @param end
	 */
	public void setStartBin(Integer number, String name, int start, int end) {
		setStartBin(number.doubleValue(), name, start, end);
	}
	/**
	 * �������
	 * @param number ��bin���������ֵ��null�����յ������ƽ��ֵ
	 * @param name
	 * @param start
	 * @param end
	 */
	public void setStartBin(Double number, String name, int start, int end) {
		HistBin histBinThis = new HistBin(number);
		histBinThis.setStartCis(start);
		histBinThis.setEndCis(end);
		histBinThis.addItemName(name);
		add(histBinThis);
	}

	/**
	 * �ڴ�֮ǰ�������趨���{@link #setStartBin}
	 * ���hist���䣬�����ǽ������趨��
	 * ��˼������Ϊ��һ��num�ͱ�num֮��
	 * @param number ��bin���������ֵ��null�����յ������ƽ��ֵ
	 * @param name ��д�Ļ������ø���������������
	 * @param thisNum
	 */
	public void addHistBin(Integer number, String name, int thisNum) {
		addHistBin(number.doubleValue(), name, thisNum);
	}
	/**
	 * �ڴ�֮ǰ�������趨���{@link #setStartBin}
	 * ���hist���䣬�����ǽ������趨��
	 * ��˼������Ϊ��һ��num�ͱ�num֮��
	 * @param number ��bin���������ֵ��null�����յ������ƽ��ֵ
	 * @param name
	 * @param thisNum
	 */
	public void addHistBin(Double number, String name, int thisNum) {
		HistBin histBinLast = get(size() - 1);
		histBinLast.getEndCis();
		HistBin histBinThis = new HistBin(number);
		histBinThis.addItemName(name);
		histBinThis.setStartCis(histBinLast.getEndCis());
		histBinThis.setEndCis(thisNum);
		add(histBinThis);
	}
	
	/**
	 * ���� coordinate������ HistBinType ������Ӧ��histbin
	 * @param coordinate
	 * @return
	 */
	public abstract HistBin searchHistBin(int coordinate);
	/**
	 * ����number������Ӧ��hist����1
	 * @param coordinate
	 */
	public void addNum(int coordinate) {
		addNum(coordinate, 1);
	}
	/**
	 * ����number������Ӧ��hist����addNumber������
	 * @param coordinate
	 */
	public void addNum(int coordinate, int addNumber) {
		HistBin histBin = searchHistBin(coordinate);
		histBin.addNumber(addNumber);
		allNum = allNum + addNumber;
	}
	
	/**
	 * ����BoxInfo<br>
	 * @return
	 */
	public BoxInfo getBoxInfo() {
		BoxInfo boxInfo = new BoxInfo(getName());
		boxInfo.setInfo25And75(getPercentInfo(25).getThisNumber(), getPercentInfo(75).getThisNumber());
		boxInfo.setInfoMedian(getPercentInfo(50).getThisNumber());
		boxInfo.setInfoMinAndMax(getPercentInfo(1).getThisNumber(), getPercentInfo(99).getThisNumber());
		boxInfo.setInfo5And95(getPercentInfo(5).getThisNumber(), getPercentInfo(95).getThisNumber());
		return boxInfo;
	}
	/** ָ��percentage����100
	 * ���ظñ�������Ӧ��ֵ
	 */
	private HistBin getPercentInfo(int percentage) {
		long thisNumThreshold = (long) ((double)percentage/100 * allNum);
		long thisNum = 0;
		
		for (HistBin histBin : this) {
			thisNum = thisNum + histBin.getCountNumber();
			if (thisNum >= thisNumThreshold) {
				return histBin;
			}
		}
		//ȫ����һ��û�ҵ�ô˵������̫�����Ǿͷ������һλ��HistBin��
		return get(size() - 1);		
	}
	
	/**
	 * ����ͳ�ƻ�ֱ��ͼ
	 * @param dotStyle
	 * @param fontSize �����С
	 * @return
	 */
	public PlotScatter getPlotHistBar(BarStyle dotStyle) {
		double[] Ycount = getYnumber(0);
		double[] Xrange = getX();
		String[] xName = getRangeX();
		HashMap<Double, String> mapX2Name = new HashMap<Double, String>();
		for (int i = 0; i < xName.length; i++) {
			HistBin histBin = get(i);
			if (histBin.getNameSingle() == null || histBin.getNameSingle().trim().equals("")) {
				mapX2Name.put(Xrange[i], xName[i]);
			} else {
				mapX2Name.put(Xrange[i], histBin.getNameSingle());
			}
		}
		if (plotScatter == null) {
			plotScatter = new PlotScatter(PlotScatter.PLOT_TYPE_BARPLOT);
		}
		double minY = MathComput.min(Ycount);
		double maxY = MathComput.max(Ycount);

		if (dotStyle.getBarWidth() == 0 && Xrange.length > 1) {
			dotStyle.setBarAndStrokeWidth(Xrange[1] - Xrange[0]);
		}
		
		plotScatter.setAxisX(Xrange[0] - 1, Xrange[Xrange.length - 1] + 1);
		plotScatter.setAxisY(minY, maxY * 1.2);
		plotScatter.addXY(Xrange, Ycount, dotStyle);
//		plotScatter.setAxisTicksXMap(mapX2Name);
		return plotScatter;
	}
	/**
	 * ����x����ֵ����0��ʼ
	 * @return
	 */
	private double[] getX() {
		double[] lengthX = new double[size()];
		for (int j = 0; j < lengthX.length; j++) {
			lengthX[j] = j;
		}
		return lengthX;
	}
	/**
	 * ����y����ֵ��ע���ʼ��HistBin����Ϊ�ȷ֣���������
	 * @binNum �ָ�ķ�����С�ڵ���0��ʾ�ָ�Ϊhistlist�ķ���
	 * @return
	 */
	private double[] getYnumber(int binNum) {
		if (binNum <= 0) {
			binNum = size();
		}
		
		double[] numberY = new double[size()];
		int i = 0;
		for (HistBin histBin : this) {
			numberY[i] = histBin.getCountNumber();;
			i++;
		}
		
		if (binNum != size()) {
			numberY = MathComput.mySpline(numberY, binNum, 0, 0, 0);
		}
		
		return numberY;
	}
	/**
	 * ����x�����������
	 * @return
	 */
	private String[] getRangeX() {
		String[] rangeX = new String[size()];
		int i = 0;
		for (HistBin histBin : this) {
			rangeX[i] = histBin.getStartCis() + "_" + histBin.getEndCis();
			i++;
		}
		return rangeX;
	}
	
	public PlotScatter getIntegralPlot(boolean cis, DotStyle dotStyle) {
		ArrayList<double[]> lsXY = getIntegral(cis);
		PlotScatter plotScatter = null;
		if (dotStyle.getStyle() == DotStyle.STYLE_BAR || dotStyle.getStyle() == DotStyle.STYLE_BOX) {
			plotScatter = new PlotScatter(PlotScatter.PLOT_TYPE_BARPLOT);
		} else {
			plotScatter = new PlotScatter(PlotScatter.PLOT_TYPE_SCATTERPLOT);
		}
		
		plotScatter.addXY(lsXY, dotStyle);
		plotScatter.setAxisX(get(0).getStartAbs(), get(size() - 1).getStartAbs());
		plotScatter.setAxisY(0, 1);
		return plotScatter;
	}
	
	/**
	 * ����ͼ
	 * @param cis true����ǰ���󣬾�����ǰ����10%��Խ����Խ��
	 * false���Ӻ���ǰ��������ǰ����100%��Խ����Խ��
	 */
	public ArrayList<double[]> getIntegral(boolean cis) {
		ArrayList<double[]> lsXY = new ArrayList<double[]>();
		double thisNum = 0;
		double[] x = new double[size()];
		double[] y = new double[size()];
		if (cis) {
			for (int count = 0; count < size(); count++) {
				HistBin histBin = get(count);
				thisNum = thisNum + histBin.getCountNumber();
				x[count] = histBin.getThisNumber();
				y[count] = thisNum/allNum;
			}
		} else {
			for (int count = size() - 1; count >= 0; count--) {
				HistBin histBin = get(count);
				thisNum = thisNum + histBin.getCountNumber();
				x[count] = histBin.getThisNumber();
				y[count] = thisNum/allNum;
			}
		}
		for (int i = 0; i < x.length; i++) {
			double[] xy = new double[2];
			xy[0] = x[i];
			xy[1] = y[i];
			lsXY.add(xy);
		}
 		return lsXY;
	}

	/**
	 * @param name hist�����֣���ز����ظ�������hash����г�ͻ
	 * @param cisList true ��С���������list�� false �Ӵ�С�����list
	 * @return
	 */
	public static HistList creatHistList(String name, boolean cisList){
		if (cisList) {
			return new HistListCis(name);
		} else {
			return new HistListTrans(name);
		}
	}
	
	public static enum HistBinType {
		LcloseRopen, LopenRclose
	}
	
}

class HistListCis extends HistList {
	private static final Logger logger = Logger.getLogger(HistListCis.class);
	private static final long serialVersionUID = -4966352009491903291L;
	
	public HistListCis(String histName) {
		setName(histName);
	}
	
	/**
	 * ���� coordinate������ HistBinType ������Ӧ��histbin
	 * @param coordinate
	 * @return
	 */
	public HistBin searchHistBin(int coordinate) {
		ListCodAbs<HistBin> lsHistBin = searchLocation(coordinate);
		HistBin histThis = lsHistBin.getGffDetailThis();
		HistBin histLast = lsHistBin.getGffDetailUp();
		HistBin histNext = lsHistBin.getGffDetailDown();
		
		HistBin resultBin = histThis;
		
		if (histThis == null) {
			HistBin histbin = null;
			if (histLast != null) {
				histbin = histLast;
			} else if (histNext != null) {
				histbin = histNext;
			}
			return histbin;
		}
		
		if (histBinType == HistBinType.LcloseRopen) {
			if ((coordinate >= histThis.getStartCis() && coordinate < histThis.getEndCis())
					||
				(histLast == null && coordinate < histThis.getStartCis() )
					||
				(histNext == null && coordinate >= histThis.getEndCis() )
			) {
				resultBin = histThis;
			} else if (coordinate < histThis.getStartCis() && coordinate >= histLast.getEndCis()) {
				resultBin = histLast;
			} else if (coordinate >= histThis.getEndCis() && coordinate <= histNext.getStartCis()) {
				resultBin = histNext;
			}
		} else if (histBinType == HistBinType.LopenRclose) {
			if ((coordinate > histThis.getStartCis() && coordinate <= histThis.getEndCis())
				||
			(histLast == null && coordinate <= histThis.getStartCis())
				||
			(histNext == null && coordinate > histThis.getEndCis())	
					) {
				resultBin = histThis;
			} else if (coordinate <= histThis.getStartCis() && coordinate >= histLast.getEndCis()) {
				resultBin = histLast;
			} else if (coordinate > histThis.getEndCis() && coordinate <= histNext.getStartCis()) {
				resultBin = histNext;
			}
		}
		return resultBin;
	}

}

class HistListTrans extends HistList {
	private static final Logger logger = Logger.getLogger(HistListTrans.class);
	private static final long serialVersionUID = -5310222125261004172L;
	
	public HistListTrans(String name) {
		setName(name);
	}
	/**
	 * ���� coordinate������ HistBinType ������Ӧ��histbin
	 * @param coordinate
	 * @return
	 */
	public HistBin searchHistBin(int coordinate) {
		ListCodAbs<HistBin> lsHistBin = searchLocation(coordinate);
		HistBin histThis = lsHistBin.getGffDetailThis();
		HistBin histLast = lsHistBin.getGffDetailUp();
		HistBin histNext = lsHistBin.getGffDetailDown();
		
		HistBin resultBin = histThis;
		if (histBinType == HistBinType.LcloseRopen) {
			if (coordinate <= histThis.getStartCis() && coordinate > histThis.getEndCis()) {
				resultBin = histThis;
			} else if (coordinate > histThis.getStartCis() && coordinate <= histLast.getEndCis()) {
				resultBin = histLast;
			} else if (coordinate <= histThis.getEndCis() && coordinate >= histNext.getStartCis()) {
				resultBin = histNext;
			}
		} else if (histBinType == HistBinType.LopenRclose) {
			if (coordinate > histThis.getStartCis() && coordinate <= histThis.getEndCis()) {
				resultBin = histThis;
			} else if (coordinate <= histThis.getStartCis() && coordinate >= histLast.getEndCis()) {
				resultBin = histLast;
			} else if (coordinate > histThis.getEndCis() && coordinate <= histNext.getStartCis()) {
				resultBin = histNext;
			}
		}
		return resultBin;
	}
	
}

