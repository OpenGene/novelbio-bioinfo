package com.novelbio.bioinfo.base.freqcount;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.novelbio.bioinfo.base.binarysearch.BinarySearch;
import com.novelbio.bioinfo.base.binarysearch.BsearchSite;

public abstract class HistList implements Iterable<HistBin> {
	List<HistBin> lsHistBins = new ArrayList<>();
	
	String name;
	
	/** 总共多少数字 */
	long allNum = 0;
	HistBinType histBinType = HistBinType.LopenRclose;
	
	
	protected abstract boolean isCis5To3();
	
	public String getName() {
		return name;
	}
	/**
	 * 默认是左开右闭
	 * @param histBinType
	 */
	public void setHistBinType(HistBinType histBinType) {
		this.histBinType = histBinType;
	}
	public List<HistBin> getLsHistBins() {
		return lsHistBins;
	}
	public void remove(int index) {
		lsHistBins.remove(index);
	}

	public void setName(String name) {
		this.name = name;
	}
	public int size() {
		return lsHistBins.size();
	}
	public HistBin get(int index) {
		return lsHistBins.get(index);
	}
	public boolean add(HistBin histBin) {
		return lsHistBins.add(histBin);
	}
	public void add(int index, HistBin histBin) {
		lsHistBins.add(index, histBin);
	}
	@Override
	public Iterator<HistBin> iterator() {
		return lsHistBins.iterator();
	}
	/**
	 * 自动设置histlist的bin，从0开始，每隔interval设置一位，名字就起interval
	 * @param histList
	 * @param binNum bin的个数
	 * @param interval 间隔
	 * @param maxSize 最大值，如果最后一位bin都没到最大值，接下来一个bin就和最大值合并
	 */
	public void setBinAndInterval(int binNum, int interval,int maxSize) {
		lsHistBins.clear();
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
	 * 自动设置histlist的bin，从0开始，每隔interval设置一位，名字就起interval
	 * @param histList
	 * @param binNum bin的个数
	 * @param interval 间隔
	 */
	public void setBinAndInterval(int binNum, int interval) {
		lsHistBins.clear();
		setStartBin(interval, interval + "", 0, interval);
		int binNext = interval*2;
		for (int i = 1; i < binNum; i++) {
			addHistBin(binNext, binNext + "", binNext);
			binNext = binNext + interval;
		}
	}
	/**
	 * 设置起点
	 * @param number 本bin所代表的数值，null就用终点和起点的平均值
	 * @param name
	 * @param start
	 * @param end
	 */
	public void setStartBin(Integer number, String name, int start, int end) {
		setStartBin(number.doubleValue(), name, start, end);
	}
	/**
	 * 设置起点
	 * @param number 本bin所代表的数值，null就用终点和起点的平均值
	 * @param name
	 * @param start
	 * @param end
	 */
	public void setStartBin(Double number, String name, int start, int end) {
		HistBin histBinThis = new HistBin(number);
		histBinThis.setCis5to3(isCis5To3());
		histBinThis.setStartCis(start);
		histBinThis.setEndCis(end);
		histBinThis.setName(name);
		lsHistBins.add(histBinThis);
	}

	/**
	 * 在此之前必须先设定起点{@link #setStartBin}
	 * 添加hist区间，必须是紧挨着设定，
	 * 意思本区间为上一个num和本num之间
	 * @param number 本bin所代表的数值，null就用终点和起点的平均值
	 * @param name 填写的话，就用该名字做坐标名字
	 * @param thisNum 本bin的终点
	 */
	public void addHistBin(Integer number, String name, int thisNum) {
		addHistBin(number.doubleValue(), name, thisNum);
	}
	/**
	 * 在此之前必须先设定起点{@link #setStartBin}
	 * 添加hist区间，必须是紧挨着设定，
	 * 意思本区间为上一个num和本num之间
	 * @param number 本bin所代表的数值，null就用终点和起点的平均值
	 * @param name
	 * @param thisNum 本bin的终点
	 */
	public void addHistBin(Double number, String name, int thisNum) {
		HistBin histBinLast = lsHistBins.get(lsHistBins.size() - 1);
		histBinLast.getEndCis();
		HistBin histBinThis = new HistBin(number);
		histBinThis.setCis5to3(isCis5To3());
		histBinThis.setName(name);
		histBinThis.setStartCis(histBinLast.getEndCis());
		histBinThis.setEndCis(thisNum);
		histBinThis.setParentName(name);
		lsHistBins.add(histBinThis);
	}
	
	/**
	 * 查找 coordinate，根据 HistBinType 返回相应的histbin
	 * @param coordinate
	 * @return
	 */
	public abstract HistBin searchHistBin(int coordinate);
	/**
	 * 给定number，把相应的hist加上1
	 * @param coordinate
	 */
	public void addNum(int coordinate) {
		addNum(coordinate, 1);
	}
	/**
	 * 给定number，把相应的hist加上addNumber的数量
	 * @param coordinate
	 */
	public void addNum(int coordinate, int addNumber) {
		HistBin histBin = searchHistBin(coordinate);
		histBin.addNumber(addNumber);
		allNum = allNum + addNumber;
	}
	
	/** 指定percentage乘以100
	 * 返回该比例所对应的值
	 */
	private HistBin getPercentInfo(int percentage) {
		long thisNumThreshold = (long) ((double)percentage/100 * allNum);
		long thisNum = 0;
		
		for (HistBin histBin : lsHistBins) {
			thisNum = thisNum + histBin.getCountNumber();
			if (thisNum >= thisNumThreshold) {
				return histBin;
			}
		}
		//全找了一遍没找到么说明数字太大了那就返回最后一位的HistBin吧
		return lsHistBins.get(lsHistBins.size() - 1);		
	}

	/**
	 * 积分图
	 * @param cis true：从前往后，就是最前面是10%，越往后越高
	 * false：从后往前，就是最前面是100%，越往后越低
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
	 * 积分值
	 * @param cis true：从前往后，就是最前面是10%，越往后越高
	 * false：从后往前，就是最前面是100%，越往后越低
	 * @reture 0: 求和 1:积分的prop
	 */
	public double[] getIntegral(int Num, boolean cis) {
		double thisNum = 0;
		double thisIntergralProp = 0;
		if (cis) {
			for (int count = 0; count < size(); count++) {
				HistBin histBin = get(count);
				//TODO 没有考虑左开右闭和左闭右开
				if (histBin.getStartAbs() >= Num) {
					break;
				}
				thisNum = thisNum + histBin.getCountNumber();
			}
			thisIntergralProp = thisNum/allNum;
		} else {
			for (int count = size() - 1; count >= 0; count--) {
				HistBin histBin = get(count);
				if (histBin.getEndAbs() < Num) {
					break;
				}
				thisNum = thisNum + histBin.getCountNumber();
				thisIntergralProp = thisNum/allNum;
			}
		}
 		return new double[]{thisNum, thisIntergralProp};
	}
	
	/**
	 * @param name hist的名字，务必不能重复，否则hash表会有冲突
	 * @param cisList true 从小到大排序的list。 false 从大到小排序的list
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
	 * 查找 coordinate，根据 HistBinType 返回相应的histbin
	 * @param coordinate
	 * @return
	 */
	public HistBin searchHistBin(int coordinate) {
		BinarySearch<HistBin> binarySearch = new BinarySearch<>(lsHistBins, true);
		BsearchSite<HistBin> lsHistBin = binarySearch.searchLocation(coordinate);
		HistBin histThis = lsHistBin.getAlignThis();
		HistBin histLast = lsHistBin.getAlignUp();
		HistBin histNext = lsHistBin.getAlignDown();
		
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

	@Override
	protected boolean isCis5To3() {
		return true;
	}

}

class HistListTrans extends HistList {
	private static final Logger logger = Logger.getLogger(HistListTrans.class);
	private static final long serialVersionUID = -5310222125261004172L;
	
	public HistListTrans(String name) {
		setName(name);
	}
	/**
	 * 查找 coordinate，根据 HistBinType 返回相应的histbin
	 * @param coordinate
	 * @return
	 */
	public HistBin searchHistBin(int coordinate) {
		BinarySearch<HistBin> binarySearch = new BinarySearch<>(lsHistBins, false);
		BsearchSite<HistBin> lsHistBin = binarySearch.searchLocation(coordinate);
		
		HistBin histThis = lsHistBin.getAlignThis();
		HistBin histLast = lsHistBin.getAlignUp();
		HistBin histNext = lsHistBin.getAlignDown();
		
		
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
	@Override
	protected boolean isCis5To3() {
		return true;
	}

}

