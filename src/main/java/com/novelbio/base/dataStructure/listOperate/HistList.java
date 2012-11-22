package com.novelbio.base.dataStructure.listOperate;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.novelbio.database.updatedb.database.Arabidopsis;

public abstract class HistList extends ListAbsSearch<HistBin, ListCodAbs<HistBin>, ListCodAbsDu<HistBin,ListCodAbs<HistBin>>> {
	private static final Logger logger = Logger.getLogger(HistList.class);
	private static final long serialVersionUID = 1481673037539688125L;
	
	/** �ܹ��������� */
	long allNum = 0;
	HistBinType histBinType = HistBinType.LopenRclose;
		
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
	
	/**
	 * ���hist���䣬�����ǽ������趨��
	 * ��˼������Ϊ��һ��num�ͱ�num֮��
	 */
	public void addHistBin(int thisNum) {
		HistBin histBinLast = get(size() - 1);
		histBinLast.getEndCis();
		HistBin histBinThis = new HistBin();
		histBinThis.setStartCis(histBinLast.getEndCis());
		histBinThis.setEndCis(thisNum);
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
	 * �Ӵ�С���η���
	 * ����<br>
	 * ��99��95��75<br>
	 * ��50<br>
	 * ��25, 5, 1<br>
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
	 * @param cisList true ��С���������list�� false �Ӵ�С�����list
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

