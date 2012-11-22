package com.novelbio.base.dataStructure.listOperate;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.novelbio.database.updatedb.database.Arabidopsis;

public abstract class HistList extends ListAbsSearch<HistBin, ListCodAbs<HistBin>, ListCodAbsDu<HistBin,ListCodAbs<HistBin>>> {
	private static final Logger logger = Logger.getLogger(HistList.class);
	private static final long serialVersionUID = 1481673037539688125L;
	
	/** 总共多少数字 */
	long allNum = 0;
	HistBinType histBinType = HistBinType.LopenRclose;
		
	/**
	 * 默认是左开右闭
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
	 * 获得的每一个信息都是实际的而没有clone
	 * 输入PeakNum，和单条Chr的list信息 返回该PeakNum的所在LOCID，和具体位置
	 * 采用clone的方法获得信息
	 * 没找到就返回null
	 */
	@Deprecated
	public ListCodAbs<HistBin> searchLocation(int Coordinate) {
		return super.searchLocation(Coordinate);
	}
	
	/**
	 * 返回双坐标查询的结果，内部自动判断 cod1 和 cod2的大小
	 * 如果cod1 和cod2 有一个小于0，那么坐标不存在，则返回null
	 * @param chrID 内部自动小写
	 * @param cod1 必须大于0
	 * @param cod2 必须大于0
	 * @return
	 */
	@Deprecated
	public ListCodAbsDu<HistBin, ListCodAbs<HistBin>> searchLocationDu(int cod1, int cod2) {
		return super.searchLocationDu(cod1, cod2);
	}
	
	/**
	 * 添加hist区间，必须是紧挨着设定，
	 * 意思本区间为上一个num和本num之间
	 */
	public void addHistBin(int thisNum) {
		HistBin histBinLast = get(size() - 1);
		histBinLast.getEndCis();
		HistBin histBinThis = new HistBin();
		histBinThis.setStartCis(histBinLast.getEndCis());
		histBinThis.setEndCis(thisNum);
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
	
	/**
	 * 从大到小依次返回
	 * 返回<br>
	 * 上99，95，75<br>
	 * 中50<br>
	 * 下25, 5, 1<br>
	 * @return
	 */
	public ArrayList<Long> getLsPercentInfo() {
		ArrayList<Long> lsResult = new ArrayList<Long>();
		lsResult.add(getPercentInfo(99).getCountNumber());
		lsResult.add(getPercentInfo(95).getCountNumber());
		lsResult.add(getPercentInfo(75).getCountNumber());
		lsResult.add(getPercentInfo(50).getCountNumber());
		lsResult.add(getPercentInfo(25).getCountNumber());
		lsResult.add(getPercentInfo(5).getCountNumber());
		lsResult.add(getPercentInfo(1).getCountNumber());
		return lsResult;
	}
	/** 指定percentage乘以100
	 * 返回该比例所对应的值
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
		//全找了一遍没找到么说明数字太大了那就返回最后一位的HistBin吧
		return get(size() - 1);		
	}
	
	/**
	 * @param cisList true 从小到大排序的list。 false 从大到小排序的list
	 * @return
	 */
	public static HistList creatHistList(boolean cisList){
		if (cisList) {
			return new HistListCis();
		} else {
			return new HistListTrans();
		}
	}
	
	public static enum HistBinType {
		LcloseRopen, LopenRclose
	}
	
}

class HistListCis extends HistList {
	private static final Logger logger = Logger.getLogger(HistListCis.class);
	private static final long serialVersionUID = -4966352009491903291L;
	
	/**
	 * 查找 coordinate，根据 HistBinType 返回相应的histbin
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
			if (coordinate >= histThis.getStartCis() && coordinate < histThis.getEndCis()) {
				resultBin = histThis;
			} else if (coordinate < histThis.getStartCis() && coordinate >= histLast.getEndCis()) {
				resultBin = histLast;
			} else if (coordinate >= histThis.getEndCis() && coordinate <= histNext.getStartCis()) {
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

class HistListTrans extends HistList {
	private static final Logger logger = Logger.getLogger(HistListTrans.class);
	private static final long serialVersionUID = -5310222125261004172L;

	/**
	 * 查找 coordinate，根据 HistBinType 返回相应的histbin
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

