package com.novelbio.analysis.seq.genome.mappingOperate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math.stat.descriptive.rank.Max;
import org.apache.commons.math.stat.descriptive.rank.Min;
import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.AlignSeq;
import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.ListDetailBin;
import com.novelbio.analysis.seq.genome.gffOperate.ListHashBin;
import com.novelbio.analysis.seq.sam.AlignmentRecorder;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.Equations;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.dataStructure.listOperate.ListCodAbs;
import com.novelbio.base.multithread.RunProcess;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.model.species.Species;

/**
 * �������ڴ����Ƶı�
 * T: ����running����������м���Ϣ��MapReadsProcessInfo������
 * ���������̣߳���Ҫ���²��� <br>
 * 1. ��ѭ������� suspendCheck()  �������߳�<br>
 * 2. ��ѭ���м�� flagRun ����ֹѭ��<br>
 * 3: ��ѭ������� setRunInfo() ��������ȡ����ʱ���ֵ���Ϣ
 * @author zong0jie
 *
 * @author zong0jie
 * 
 */
public abstract class MapReadsAbs extends RunProcess<MapReadsAbs.MapReadsProcessInfo> {
	private static Logger logger = Logger.getLogger(MapReadsAbs.class);
	/**�����ĵ�������ȵ�һ��Ⱦɫ��ѹ��Ϊ�̵�ÿ��inv��Լ10-20bp�����У���ôѹ������ѡ��Ϊ20bp�е���ֵ����λ�� */
	public static final int SUM_TYPE_MEDIAN = 2;
	/**�����ĵ�������ȵ�һ��Ⱦɫ��ѹ��Ϊ�̵�ÿ��inv��Լ10-20bp�����У���ôѹ������ѡ��Ϊ20bp�е���ֵ��ƽ���� */
	public static final int SUM_TYPE_MEAN = 3;
	
	/** ��ÿ��double[]���/double.length Ҳ���ǽ�ÿ������Ը�gene��ƽ��������� */
	public static final int NORMALIZATION_PER_GENE = 128;
	/** ��ÿ��double[]*1million/AllReadsNum Ҳ���ǽ�ÿ������Բ������ */
	public static final int NORMALIZATION_ALL_READS = 256;
	/** ����׼�� */
	public static final int NORMALIZATION_NO = 64;

	/** ���ڽ���ı�׼������ */
	int NormalType = NORMALIZATION_ALL_READS;

	 /** ������Ϣ,���ֶ�ΪСд */
	 HashMap<String, Long> mapChrID2Len = new HashMap<String, Long>();
	 
	 Equations FormulatToCorrectReads;

	 /**
	  * key��chrID����Сд
	  * value�� Ⱦɫ�������Ϣ�������Ҫֻ��tss��ֻ��exon�ȱ��
	  */
	 Map<String, List<? extends Alignment>> mapChrID2LsAlignmentFilter;
	 
	 /**
	  * @param invNum ÿ������λ����������趨Ϊ1�����㷨��仯��Ȼ���ܾ�ȷ
	  * @param mapFile mapping�Ľ���ļ���һ��Ϊbed��ʽ
	  */
	 public MapReadsAbs() {}

	 public void setSpecies(Species species) {
		 mapChrID2Len = species.getMapChromInfo();
	 }
	 /**
	  * �趨����������Ʃ������뿴ȫ��������tss�ķֲ�����ô�ͽ�tss������װ����ls�м�
	  * @param lsAlignments
	  */
	 public void setMapChrID2LsAlignments(Map<String, List<? extends Alignment>> mapChrID2LsAlignmentFilter) {
		 this.mapChrID2LsAlignmentFilter = mapChrID2LsAlignmentFilter;
	 }

	 /** ��species�������趨
	  * key���Сд
	  *  */
	 public void setMapChrID2Len(HashMap<String, Long> mapChrID2Len) {
		 this.mapChrID2Len = mapChrID2Len;
	 }
	 public HashMap<String, Long> getMapChrID2Len() {
		return mapChrID2Len;
	}
	 /**
	  * ��������chrID��list
	  * @return
	  */
	 public ArrayList<String> getChrIDLs() {
		 return ArrayOperate.getArrayListKey(mapChrID2Len);
	 }
	 /**
	  * ����У��reads���ķ��̣�Ĭ���趨��������reads����СֵΪ0������У��С��0�Ķ���Ϊ0
	  * @param FormulatToCorrectReads
	  */
	 public void setFormulatToCorrectReads(Equations FormulatToCorrectReads) {
		 this.FormulatToCorrectReads = FormulatToCorrectReads;
		 //Ĭ���趨��������reads����СֵΪ0������У��С��0�Ķ���Ϊ0
		 FormulatToCorrectReads.setMin(0);
	 }

	public void running() {
		try {
			ReadMapFileExp();
		} catch (Exception e) {
			e.printStackTrace();
 		}
 	}
	/**
	 * ������Ϊmacs��bed�ļ�ʱ������ҪȻ��<b>����chrm��Ŀ</b><br>
	 * ����chr��Ŀ��Сд
	 * ��ȡMapping�ļ���������Ӧ��һά�������飬��󱣴���һ����ϣ���С�ע�⣬mapping�ļ��е�chrID��chrLengthFile�е�chrIDҪһ�£���������
	 * @return ��������mapping��reads����
	 * @throws Exception
	 */
	protected abstract void ReadMapFileExp() throws Exception;

	/**
	 * ���ÿ��MapInfo�����û���ҵ���Ⱦɫ��λ�㣬�����null

	 * ������׼������equations����
	 * @param lsmapInfo
	 * @param thisInvNum  ÿ��������������bp�������ڵ���invNum�������invNum�ı��� ���invNum ==1 && thisInvNum == 1�������ܾ�ȷ
	 * @param type 0����Ȩƽ�� 1��ȡ���ֵ��2����Ȩ����ƽ��--Ҳ���ǼӺ�
	 */
	public void getRange(MapInfo mapInfo, int thisInvNum, int type) {
		double[] Info = getRangeInfo(thisInvNum, mapInfo.getRefID(), mapInfo.getStartAbs(), mapInfo.getEndAbs(), type);
		mapInfo.setDouble(Info);
	}
	/**
	 * ������׼��
	 * ��MapInfo�е�double�������Ӧ��reads��Ϣ
	 * @param binNum ���ָ��������Ŀ
	 * @param lsmapInfo
	 * @param type 0����Ȩƽ�� 1��ȡ���ֵ��2����Ȩ����ƽ��--Ҳ���ǼӺ�
	 */
	public void getRange(int binNum, MapInfo mapInfo, int type) {
		double[] Info = getRangeInfo(mapInfo.getRefID(), mapInfo.getStartAbs(), mapInfo.getEndAbs(), binNum, type);
		if (Info == null) {
			logger.error("����δ֪ID��"+mapInfo.getName() + " "+mapInfo.getRefID() + " " + mapInfo.getStartAbs() + " "+ mapInfo.getEndAbs());
		}
		mapInfo.setDouble(Info);
	}
	/**
	 * ���ÿ��MapInfo��ֱ���趨�������Ƿ���
	 * ������׼������equations����
	 * @param lsmapInfo
	 * @param thisInvNum  ÿ��������������bp�������ڵ���invNum�������invNum�ı��� ���invNum ==1 && thisInvNum == 1�������ܾ�ȷ
	 * @param type 0����Ȩƽ�� 1��ȡ���ֵ��2����Ȩ����ƽ��--Ҳ���ǼӺ�
	 */
	public void getRangeLs(List<MapInfo> lsmapInfo, int thisInvNum, int type) {
		for (MapInfo mapInfo : lsmapInfo) {
			double[] Info = getRangeInfo(thisInvNum, mapInfo.getRefID(), mapInfo.getStartAbs(), mapInfo.getEndAbs(), type);
			mapInfo.setDouble(Info);
		}
	}
	/**
	 * ������׼��
	 * ��MapInfo�е�double�������Ӧ��reads��Ϣ��ֱ���趨�������Ƿ���
	 * @param binNum ���ָ��������Ŀ
	 * @param lsmapInfo
	 * @param type 0����Ȩƽ�� 1��ȡ���ֵ��2����Ȩ����ƽ��--Ҳ���ǼӺ�
	 */
	public void getRangeLs(int binNum, List<MapInfo> lsmapInfo, int type) {
		for (int i = 0; i < lsmapInfo.size(); i++) {
			MapInfo mapInfo = lsmapInfo.get(i);
			double[] Info = getRangeInfo(mapInfo.getRefID(), mapInfo.getStartAbs(), mapInfo.getEndAbs(), binNum, type);
			if (Info == null) {
				lsmapInfo.remove(i); i--;
				logger.error("����δ֪ID��"+mapInfo.getName() + " "+mapInfo.getRefID() + " " + mapInfo.getStartAbs() + " "+ mapInfo.getEndAbs());
				continue;
			}
			mapInfo.setDouble(Info);
		}
	}
	/**
	 * ������׼������equations������<b>ע�ⷵ�ص�ֵһֱ���ǰ��������С���󣬲�����ݷ�����ı䷽��</b>
	 * �������귶Χ�����ظ������ڵ���Ϣ��ȡ��Ϊ��Ȩƽ��
	 * @param chrID
	 * @param lsLoc һ��ת¼����exon list
	 * @return null��ʾ����
	 */
	public double[] getRangeInfo(String chrID, List<? extends Alignment> lsLoc) {
		return getRangeInfo(chrID, lsLoc, -1 , 0);
	}
	/**
	 *  ����mRNA�ļ��㣬������׼������equations����
	 * �����������䣬��Ҫ���ֵĿ��������ظö�������reads�����顣�����Ⱦɫ����mappingʱ�򲻴��ڣ��򷵻�null
	 * ��λ�������˵����ڵ� ��ȡinvNum���䣬Ȼ������µ�invNum����
	 * @param chrID
	 * @param lsLoc ֱ������gffIso����
	 * @param binNum �ֳɼ��ݣ����С��0���򲻽��кϲ���ֱ�ӷ����Լ��ķ���
	 * @param type  0����Ȩƽ�� 1��ȡ���ֵ��2����Ȩ����ƽ��--Ҳ���ǼӺ�
	 * @return
	 */
	private double[] getRangeInfo(String chrID, List<? extends Alignment> lsLoc, int binNum, int type) {
		ArrayList<double[]> lstmp = new ArrayList<double[]>();
		if (lsLoc.size() > 1 && !lsLoc.get(0).isCis5to3()) {
			lsLoc = sortLsLoc(lsLoc);
		}
	
		for (Alignment is : lsLoc) {
			double[] info = getRangeInfo(0, chrID, is.getStartAbs(), is.getEndAbs(), type);
			if (info == null) {
				return null;
			}
			lstmp.add(info);
		}
		int len = 0;
		for (double[] ds : lstmp) {
			len = len + ds.length;
		}
		//�������ճ��ȵ�double
		double[] finalReads = new double[len];
		int index = 0;
		for (double[] ds : lstmp) {
			for (double d : ds) {
				finalReads[index] = d;
				index ++ ;
			}
		}
		if (binNum > 0) {
			finalReads =MathComput.mySpline(finalReads, binNum, 0, 0, 0);
		}
		return finalReads;
	}
	/** �������loc��С�������򣬵��ǲ����ı������loc */
	private ArrayList<Alignment> sortLsLoc(List<? extends Alignment> lsLoc) {
		ArrayList<Alignment> lsLocNew = new ArrayList<Alignment>();
		for (Alignment alignment : lsLoc) {
			lsLocNew.add(alignment);
		}
		Collections.sort(lsLocNew, new Comparator<Alignment>() {
			public int compare(Alignment o1, Alignment o2) {
				Integer o1Int = o1.getStartAbs();
				Integer o2Int = o2.getStartAbs();
				return o1Int.compareTo(o2Int);
			}
		});
		return lsLocNew;
	}
	/**
	 * ������׼������equations����
	 * �����������䣬Ĭ��ÿ�������bp��ΪinvNum�����ظö�������reads������
	 * �����Ⱦɫ����mappingʱ�򲻴��ڣ��򷵻�null
	 * ���invNum ==1 && thisInvNum == 1�������ܾ�ȷ
	 * @param chrID һ��ҪСд
	 * @param startNum ������꣬Ϊʵ����㣬���startNum<=0 ����endNum<=0���򷵻�ȫ����Ϣ
	 * @param endNum �յ����꣬Ϊʵ���յ�
	 * ���(endNum - startNum + 1) / thisInvNum >0.7����binNum����Ϊ1
	 * @param type 0����Ȩƽ�� 1��ȡ���ֵ��2����Ȩ����ƽ��--Ҳ���ǼӺ�
	 * @return ���û���ҵ���Ⱦɫ��λ�㣬�򷵻�null
	 */
	public double[] getRangeInfo(String chrID,int startNum,int endNum,int type) {
		return getRangeInfo(0, chrID, startNum, endNum, type);
	}
	/**
	 * ������׼������equations����
	 * �����������䣬��ÿ�������bp�������ظö�������reads������
	 * ��λ�������˵����ڵ� ��ȡinvNum���䣬Ȼ������µ�invNum���䣬�����Ⱦɫ����mappingʱ�򲻴��ڣ��򷵻�null
	 * @param thisInvNum ÿ��������������bp�������ڵ���invNum�������invNum�ı���<br>
	 * ���thisInvNum <= 0����thisInvNum = invNum<br>
	 * ���invNum ==1 && thisInvNum == 1�������ܾ�ȷ
	 * @param chrID һ��ҪСд
	 * @param startNum ������꣬Ϊʵ����㣬���startNum<=0 ����endNum<=0���򷵻�ȫ����Ϣ
	 * @param endNum �յ����꣬Ϊʵ���յ�
	 * ���(endNum - startNum + 1) / thisInvNum >0.7����binNum����Ϊ1
	 * @param type 0����Ȩƽ�� 1��ȡ���ֵ��2����Ȩ����ƽ��--Ҳ���ǼӺ�
	 * @return ���û���ҵ���Ⱦɫ��λ�㣬�򷵻�null
	 */
	public abstract double[] getRangeInfo(int thisInvNum,String chrID,int startNum,int endNum,int type);
	protected abstract double[] getRangeInfo(String chrID, int startNum, int endNum, int binNum, int type);
	
	/**
	 * ������Ĺ�ʽ��������
	 * @param input
	 * @return
	 */
	protected double[] equationsCorrect(double[] input) {
		double[] result = null;
		if (FormulatToCorrectReads != null) {
			result = FormulatToCorrectReads.getYinfo(input);
		} else {
			result = input;
		}
		return result;
	}
	 /**
	  * ������õ���ʵ��ĳ��Ⱦɫ��ĳ���
	  */
	 protected long getChrLen(String chrID) {
		 return mapChrID2Len.get(chrID.toLowerCase());
	 }
	 
	 /** �ܹ��ж���reads������mapping�������ReadMapFile���ܵõ��� */
	protected abstract long getAllReadsNum();
	
	/**
	 * ��������start �� end�Ƿ���ָ�����䷶Χ�ڣ�
	 * @param mapChrID2Length keyΪСд
	 * @param chrID ����Ĵ�Сд����ν
	 * @param startNum С��0������Ϊ0
	 * @param endNum С��0������Ϊ���Χ
	 * @return
	 */
	public static int[] correctStartEnd(Map<String, ? extends Number> mapChrID2Length, String chrID, int startNum, int endNum) {
		if (startNum <=0) {
			startNum = 1; 
		}
		
		if (!mapChrID2Length.containsKey(chrID.toLowerCase())) {
			logger.error("�����ڸ�Ⱦɫ�壺" + chrID);
			return null;
		}
		if (endNum <= 0 || endNum > mapChrID2Length.get(chrID.toLowerCase()).intValue() ) {
			endNum = mapChrID2Length.get(chrID.toLowerCase()).intValue();
		}
		if (startNum > endNum) {
			logger.error("��㲻�ܱ��յ��: "+chrID+" "+startNum+" "+endNum);
			return null;
		}
		startNum --; endNum --;
		return new int[]{startNum, endNum};
	}
	/**
	 * ����������Ϣ�����Ƚϵı�ֵ��Ҳ���Ǿ�ֵ���������mapInfo��weight��
	 * �ڲ���׼��
	 * @param mapReads ��һ��mapReads��Ϣ
	 * @param mapReads2 �ڶ���mapReads��Ϣ
	 * @param mapInfo
	 */
	public static void CmpMapReg(MapReads mapReads, MapReads mapReads2, MapInfo mapInfo) {
		double[] info1 = mapReads.getRangeInfo(mapInfo.getRefID(), mapInfo.getStartAbs(), mapInfo.getEndAbs(), 0);
		double[] info2 = mapReads.getRangeInfo(mapInfo.getRefID(), mapInfo.getStartAbs(), mapInfo.getEndAbs(), 0);
		
		double value1 = getMean(info1);
		double value2 = getMean(info2);
		
		mapInfo.setScore(value1/value2);
	}
	
	private static double getMean(double[] info) {
		if (info == null) {
			return -1;
		}
		return new Mean().evaluate(info);
	}
	
	/**
	 * ��ȡ��ԭʼ������Ҫ������׼���������
	 * ���������б�׼��
	 * �����doubleֱ���޸ģ������ء�<br>
	 * ���õ��Ľ����Ҫ���ֵ
	 * ����double���飬����reads�������б�׼��,reads�����ɶ�ȡ��mapping�ļ��Զ����<br>
	 * ����ȳ���1millionȻ���ٳ���ÿ��double��ֵ<br>
	 * @param doubleInfo ��ȡ�õ���ԭʼvalue
	 * @return 
	 */
	public static void normDouble(int NormalType, double[] doubleInfo, long allReadsNum) {
		if (doubleInfo == null) {
			return;
		}
		if (NormalType == NORMALIZATION_NO) {
			return;
		}
		else if (NormalType == NORMALIZATION_ALL_READS) {
			for (int i = 0; i < doubleInfo.length; i++) {
				doubleInfo[i] = doubleInfo[i]*1000000/allReadsNum;
			}
		}
		else if (NormalType == NORMALIZATION_PER_GENE) {
			double avgSite = MathComput.mean(doubleInfo);
			if (avgSite != 0) {
				for (int i = 0; i < doubleInfo.length; i++) {
					doubleInfo[i] = doubleInfo[i]/avgSite;
				}
			}
		}
	}
	
	public static class MapReadsProcessInfo {
		long readsize;
		public MapReadsProcessInfo(long readsize) {
			this.readsize = readsize;
		}
		public long getReadsize() {
			return readsize;
		}
	}
}

