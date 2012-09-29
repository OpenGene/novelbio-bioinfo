package com.novelbio.analysis.seq.genome.mappingOperate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

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
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.Equations;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.dataStructure.listOperate.ListCodAbs;
import com.novelbio.base.multithread.RunProcess;
import com.novelbio.database.model.modgeneid.GeneID;

/**
 * �������ڴ����Ƶı�
 * T: ����running����������м���Ϣ��MapReadsProcessInfo������
 * ���������̣߳���Ҫ���²��� <br>
 * 1. ��ѭ�������� suspendCheck()  �������߳�<br>
 * 2. ��ѭ���м�� flagRun ����ֹѭ��<br>
 * 3: ��ѭ�������� setRunInfo() ��������ȡ����ʱ���ֵ���Ϣ
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
	
	 HashMap<String, ChrMapReadsInfo> mapChrID2ReadsInfo = new HashMap<String, ChrMapReadsInfo>();
 
	 /**ÿ������λ����������趨Ϊ1�����㷨��仯��Ȼ���ܾ�ȷ*/
	 int invNum = 10;
	 
	 int tagLength = 300;//��ReadMapFile������ֵ
	 /** ������Ϣ,���ֶ�ΪСд */
	 HashMap<String, Long> mapChrID2Len = new HashMap<String, Long>();
	 
	 Equations FormulatToCorrectReads;
	 
	 AlignSeq alignSeqReader;
	 
	 int summeryType = SUM_TYPE_MEAN;
	 
	 /**
	  * @param invNum ÿ������λ����������趨Ϊ1�����㷨��仯��Ȼ���ܾ�ȷ
	  * @param mapFile mapping�Ľ���ļ���һ��Ϊbed��ʽ
	  */
	 public MapReadsAbs() {}
	 /**ÿ������λ����������趨Ϊ1�����㷨��仯��Ȼ���ܾ�ȷ*/
	 public void setInvNum(int invNum) {
		this.invNum = invNum;
	}
	 public void setBedSeq(String bedSeqFile) {
		 alignSeqReader = new BedSeq(bedSeqFile);
	}
	 public void setAlignSeqReader(AlignSeq alignSeqReader) {
		 this.alignSeqReader = alignSeqReader;
	}
	 /**�����ĵ�������ȵ�һ��Ⱦɫ��ѹ��Ϊ�̵�ÿ��inv��Լ10-20bp�����У���ôѹ������ѡ��Ϊ20bp�е���ֵ����λ����ƽ����<br>
	  * SUM_TYPE_MEDIAN��SUM_TYPE_MEAN
	  *  
	  */
	 public void setSummeryType(int summeryType) {
		this.summeryType = summeryType;
	}
	 /** ��species�������趨
	  * key���Сд
	  *  */
	 public void setMapChrID2Len(HashMap<String, Long> mapChrID2Len) {
		 this.mapChrID2Len = mapChrID2Len;
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
	 /**
	  * �趨peak��bed�ļ�����һ��ΪchrID���ڶ���Ϊ��㣬������Ϊ�յ㣬
	  * ����ȥ��peak��ÿ��Ⱦɫ���bg���
	  * @param peakBedFile
	  * @param firstlinels1
	  * @return ls-0��chrID 1��bg
	  * ���е�һλ��chrAll����Ϣ
	  */
	 public ArrayList<String[]> getChIPBG(String peakBedFile, int firstlinels1) {
		 ArrayList<String[]> lsResult = new ArrayList<String[]>();
		 ListHashBin gffHashPeak = new ListHashBin(true, 1, 2, 3, firstlinels1);
		 gffHashPeak.ReadGffarray(peakBedFile);
		 
		 double allReads = 0; int numAll = 0; double max = 0;
		 ArrayList<Integer> lsMidAll = new ArrayList<Integer>();
		 for (Entry<String, ChrMapReadsInfo> entry : mapChrID2ReadsInfo.entrySet()) {
			String chrID = entry.getKey();
			double allReadsChr = 0; int numChr = 0; double maxChr = 0;
			ArrayList<Integer> lsMidChr = new ArrayList<Integer>();
			int[] info = entry.getValue().getSumChrBpReads();
			for (int i = 0; i < info.length; i++) {
				if (info[i] == 0) { 
					continue;
				}
				ListCodAbs<ListDetailBin> gffcodPeak = gffHashPeak.searchLocation(chrID, i*invNum);
				if (gffcodPeak != null && gffcodPeak.isInsideLoc()) {
					continue;
				}
				if (maxChr < info[i]) {
					maxChr = info[i];
				}
				if (lsMidChr.size() < 50000) {
					lsMidChr.add(info[i]);
				}
				allReadsChr = allReadsChr + info[i];
				numChr ++;
			}
			if (numChr != 0) {
				double med75 = MathComput.median(lsMidChr, 75);
				lsResult.add(new String[]{chrID, (double)allReadsChr/numChr+"", maxChr+"",  med75 + ""});
			}
			if (max < maxChr) {
				max = maxChr;
			}
			lsMidAll.addAll(lsMidChr);
			allReads = allReads + allReadsChr;
			numAll = numAll + numChr;
		 }
		 double med75All = MathComput.median(lsMidAll, 75);
		 lsResult.add(0, new String[]{"chrAll", (double)allReads/numAll + "", max + "", med75All + ""});
		 return lsResult;
	 }
	/**
	 * �趨˫��readsTagƴ�����󳤶ȵĹ���ֵ��Ŀǰsolexa˫���������ȴ����300bp������̫��ȷ
	 * Ĭ��300
	 * ����Ƿ�����getReadsDensity����reads�ܶȵĶ���
	 * @param readsTagLength
	 */
	public  void setTagLength(int thisTagLength) {
		tagLength=thisTagLength;
	}
	/**
	 * ÿ������λȡ��,����趨Ϊ1�����㷨��仯��Ȼ���ܾ�ȷ
	 * @return
	 */
	public int getBinNum() {
		return invNum;
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
	 * ������׼������equations����
	 * �������귶Χ�����ظ���������Сֵ
	 * @param chrID
	 * @param startLoc
	 * @param endLoc
	 * @return double -1��ʾ����
	 */
	public double regionMin(String chrID, int startLoc, int endLoc) {
		double[] info = getRengeInfo(invNum, chrID, startLoc, endLoc, 0);
		if (info == null) {
			return -1;
		}
		return new Min().evaluate(info);
	}
	
	/**
	 * ������׼������equations����<br>
	 * �������귶Χ�����ظ����������ֵ
	 * @param chrID
	 * @param startLoc
	 * @param endLoc
	 * @return double -1��ʾ����
	 */
	public double regionMax(String chrID, int startLoc, int endLoc) {
		double[] info = getRengeInfo(invNum, chrID, startLoc, endLoc, 0);
		if (info == null) {
			return -1;
		}
		return new Max().evaluate(info);
	}
	/**
	 * ������׼������equations����
	 * �������귶Χ�����ظ�������ƽ��ֵ
	 * @param chrID
	 * @param startLoc
	 * @param endLoc
	 * @return double -1��ʾ����
	 */
	public double getRegionMean(String chrID, int startLoc, int endLoc) {
		double[] info = getRengeInfo(invNum, chrID, startLoc, endLoc, 0);
		if (info == null) {
			return -1;
		}
		return new Mean().evaluate(info);
	}
	/**
	 * ���invNum��Ϊ1������ܲ���ȷ
	 * �������귶Χ�������������ж���0����
	 * @param chrID
	 * @param startLoc ����ν�ĸ���ǰ�����������1��ʼ
	 * @param endLoc
	 * @return arrayList[]:0����ľ�����������
	 *  null��ʾ����
	 */
	public ArrayList<int[]> region0Info(String chrID, int startLocT, int endLocT) {
		int startLoc = Math.min(startLocT, endLocT);
		int endLoc = Math.max(startLocT, endLocT);
		startLoc--; endLoc--;
		int[] invNumReads = mapChrID2ReadsInfo.get(chrID.toLowerCase()).getSumChrBpReads();
		if (startLoc < 0 || endLoc >= invNumReads.length) {
			logger.error("Խ���ˣ�"+ chrID + " " + startLoc + " " + endLoc);
			return null;
		}
		ArrayList<int[]> lsResult = new ArrayList<int[]>();
		
		boolean flag0 = false;
		int[] region = null;
		for (int i = startLoc; i < endLoc; i++) {
			if (invNumReads[i] == 0 && !flag0) {
				region = new int[2];
				region[0] = i+1;
				region[1] = i + 1;
				lsResult.add(region);
				flag0 = true;
			}
			else if (invNumReads[i] == 0 && flag0) {
				region[1] = i+1;
			}
			else if (invNumReads[i] != 0) {
				flag0 = false;
			}
		}
		return lsResult;
	}
	/**
	 * ������׼������equations����
	 * �������귶Χ�����ظ������ڱ�׼��
	 * @param chrID
	 * @param startLoc
	 * @param endLoc
	 * @return double  -1��ʾ����
	 */
	public double regionSD(String chrID, int startLoc, int endLoc) {
		double[] info = getRengeInfo(invNum, chrID, startLoc, endLoc, 0);
		if (info == null) {
			return -1;
		}
		return new StandardDeviation().evaluate(info);
	}
	/**
	 * ������׼������equations����
	 * �������귶Χ�����ظ������ڱ�׼��
	 * @param chrID Ⱦɫ����
	 * @param lsLoc һ��ת¼����exon list
	 * @return -1��ʾ����
	 */
	public double regionSD(String chrID, List<ExonInfo> lsLoc) {
		double[] info = getRegionInfo(chrID, lsLoc);
		if (info == null) {
			return -1;
		}
		return new StandardDeviation().evaluate(getRegionInfo(chrID, lsLoc));
	}
	/**
	 * ������׼������equations����
	 * �������귶Χ�����ظ�������ƽ��ֵ
	 * @param chrID
	 * @param lsLoc һ��ת¼����exon list
	 * @return  -1��ʾ����
	 */
	public double regionMean(String chrID, List<ExonInfo> lsLoc) {
		double[] info = getRegionInfo(chrID, lsLoc);
		if (info == null) {
			return -1;
		}
		return new Mean().evaluate(info);
	}

	/**
	 * ������׼������equations����
	 * �������귶Χ�����ظ������ڵ���Ϣ��ȡ��Ϊ��Ȩƽ��
	 * @param chrID
	 * @param lsLoc һ��ת¼����exon list
	 * @return null��ʾ����
	 */
	public double[] getRegionInfo(String chrID, List<ExonInfo> lsLoc) {
		return getRegionInfo(chrID, lsLoc, -1 , 0);
	}
	/**
	 *  ����mRNA�ļ��㣬������׼������equations����
	 * �����������䣬��Ҫ���ֵĿ��������ظö�������reads�����顣�����Ⱦɫ����mappingʱ�򲻴��ڣ��򷵻�null
	 * ��λ�������˵����ڵ� ��ȡinvNum���䣬Ȼ������µ�invNum����
	 * @param chrID
	 * @param lsLoc ֱ������gffIso����
	 * @param binNum �ֳɼ��ݣ����С��0���򷵻��Լ��ķ���
	 * @param type  0����Ȩƽ�� 1��ȡ���ֵ��2����Ȩ����ƽ��--Ҳ���ǼӺ�
	 * @return
	 */
	public double[] getRegionInfo(String chrID, List<ExonInfo> lsLoc, int binNum, int type) {
		ArrayList<double[]> lstmp = new ArrayList<double[]>();
		for (ExonInfo is : lsLoc) {
			double[] info = getRengeInfo(invNum, chrID, is.getStartAbs(), is.getEndAbs(), type);
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
	/**
	 * ���ÿ��MapInfo
	 * ������׼������equations����
	 * @param lsmapInfo
	 * @param thisInvNum  ÿ��������������bp�������ڵ���invNum�������invNum�ı��� ���invNum ==1 && thisInvNum == 1�������ܾ�ȷ
	 * @param type 0����Ȩƽ�� 1��ȡ���ֵ��2����Ȩ����ƽ��--Ҳ���ǼӺ�
	 */
	public void getRegionLs(List<MapInfo> lsmapInfo, int thisInvNum, int type) {
		for (MapInfo mapInfo : lsmapInfo) {
			double[] Info = getRengeInfo(thisInvNum, mapInfo.getRefID(), mapInfo.getStartAbs(), mapInfo.getEndAbs(), type);
			mapInfo.setDouble(Info);
		}
	}
	/**
	 * ���ÿ��MapInfo�����û���ҵ���Ⱦɫ��λ�㣬�����null

	 * ������׼������equations����
	 * @param lsmapInfo
	 * @param thisInvNum  ÿ��������������bp�������ڵ���invNum�������invNum�ı��� ���invNum ==1 && thisInvNum == 1�������ܾ�ȷ
	 * @param type 0����Ȩƽ�� 1��ȡ���ֵ��2����Ȩ����ƽ��--Ҳ���ǼӺ�
	 */
	public void getRegion(MapInfo mapInfo, int thisInvNum, int type) {
		double[] Info = getRengeInfo(thisInvNum, mapInfo.getRefID(), mapInfo.getStartAbs(), mapInfo.getEndAbs(), type);
		mapInfo.setDouble(Info);
	}
	/**
	 * ������׼��
	 * ��MapInfo�е�double�������Ӧ��reads��Ϣ
	 * @param binNum ���ָ��������Ŀ
	 * @param lsmapInfo
	 * @param type 0����Ȩƽ�� 1��ȡ���ֵ��2����Ȩ����ƽ��--Ҳ���ǼӺ�
	 */
	public void getRegionLs(int binNum, List<MapInfo> lsmapInfo, int type) {
		for (int i = 0; i < lsmapInfo.size(); i++) {
			MapInfo mapInfo = lsmapInfo.get(i);
			double[] Info = getRengeInfo(mapInfo.getRefID(), mapInfo.getStartAbs(), mapInfo.getEndAbs(), binNum, type);
			if (Info == null) {
				lsmapInfo.remove(i); i--;
				logger.error("����δ֪ID��"+mapInfo.getName() + " "+mapInfo.getRefID() + " " + mapInfo.getStartAbs() + " "+ mapInfo.getEndAbs());
				continue;
			}
			mapInfo.setDouble(Info);
		}
	}
	/**
	 * ������׼��
	 * ��MapInfo�е�double�������Ӧ��reads��Ϣ
	 * @param binNum ���ָ��������Ŀ
	 * @param lsmapInfo
	 * @param type 0����Ȩƽ�� 1��ȡ���ֵ��2����Ȩ����ƽ��--Ҳ���ǼӺ�
	 */
	public void getRegion(int binNum, MapInfo mapInfo, int type) {
		double[] Info = getRengeInfo(mapInfo.getRefID(), mapInfo.getStartAbs(), mapInfo.getEndAbs(), binNum, type);
		if (Info == null) {
			logger.error("����δ֪ID��"+mapInfo.getName() + " "+mapInfo.getRefID() + " " + mapInfo.getStartAbs() + " "+ mapInfo.getEndAbs());
		}
		mapInfo.setDouble(Info);
	}
	/**
	 * ������׼������equations����
	 * �����������䣬��ÿ�������bp�������ظö�������reads������
	 * ��λ�������˵����ڵ� ��ȡinvNum���䣬Ȼ������µ�invNum���䣬�����Ⱦɫ����mappingʱ�򲻴��ڣ��򷵻�null
	 * @param thisInvNum ÿ��������������bp�������ڵ���invNum�������invNum�ı���
	 * ���invNum ==1 && thisInvNum == 1�������ܾ�ȷ
	 * @param chrID һ��ҪСд
	 * @param startNum ������꣬Ϊʵ����㣬���startNum<=0 ����endNum<=0���򷵻�ȫ����Ϣ
	 * @param endNum �յ����꣬Ϊʵ���յ�
	 * ���(endNum - startNum + 1) / thisInvNum >0.7����binNum����Ϊ1
	 * @param type 0����Ȩƽ�� 1��ȡ���ֵ��2����Ȩ����ƽ��--Ҳ���ǼӺ�
	 * @return ���û���ҵ���Ⱦɫ��λ�㣬�򷵻�null
	 */
	public double[] getRengeInfo(int thisInvNum,String chrID,int startNum,int endNum,int type) {
		double[] result;
		////////////////////////����Ҫ�ָ���////////////////////////////////////////
		if (invNum == 1 && thisInvNum == 1) {
			result = getRengeInfoInv1(chrID, startNum, endNum);
		} else {
			result = getRengeInfoNorm(chrID, thisInvNum, startNum, endNum, type);
		}
		return result;
	}
	/** ���Ϊ1�ľ�ȷ�汾��������׼������equations����
	 * @param chrID Ⱦɫ��ID
	 * @param startNum
	 * @param endNum
	 *  */
	private double[] getRengeInfoInv1(String chrID, int startNum, int endNum) {
		ChrMapReadsInfo chrMapReadsInfo = mapChrID2ReadsInfo.get(chrID.toLowerCase());
		if (chrMapReadsInfo == null) {
			logger.info("û�и�Ⱦɫ�壺 " + chrID);
			return null;
		}
		int[] startEnd = correctStartEnd(chrID, startNum, endNum);
		double[] result = new double[startEnd[1] - startEnd[0] + 1];
		
		int[] invNumReads = chrMapReadsInfo.getSumChrBpReads();
		
		int k = 0;
		for (int i = startEnd[0]; i <= startEnd[1]; i++) {
			result[k] = invNumReads[i];
			k++;
		}
		//��׼��
		normDouble(result);
		result = equationsCorrect(result);
		return result;
	}
	/** ����İ汾��������׼������equations����
	 * @param chrID Ⱦɫ��ID
	 * @param thisInvNum ÿ��������������bp�������ڵ���invNum�������invNum�ı���
	 * @param startNum
	 * @param endNum
	 * @param type 0����Ȩƽ�� 1��ȡ���ֵ��2����Ȩ����ƽ��--Ҳ���ǼӺ�
	 *  */
	private double[] getRengeInfoNorm(String chrID, int thisInvNum, int startNum, int endNum, int type) {
		ChrMapReadsInfo chrMapReadsInfo = mapChrID2ReadsInfo.get(chrID.toLowerCase());
		if (chrMapReadsInfo == null) {
			logger.error("û�и�Ⱦɫ�壺" + chrID);
			return null;
		}
		int[] startEndLoc = correctStartEnd(chrID, startNum, endNum);
		double binNum = (double)(startEndLoc[1] - startEndLoc[0] + 1) / thisInvNum;
		int binNumFinal = 0;
		if (binNum - (int)binNum >= 0.7) {
			binNumFinal = (int)binNum + 1;
		} else {
			binNumFinal = (int)binNum;
		}
		//�ڲ�������׼����
		double[] tmp = getRengeInfo( chrID, startNum, endNum, binNumFinal,type);
		
		return tmp;
	}
	
	/**
	 * ������׼������equations����
	 * �����������䣬��Ҫ���ֵĿ��������ظö�������reads�����顣�����Ⱦɫ����mappingʱ�򲻴��ڣ��򷵻�null
	 * ��λ�������˵����ڵ� ��ȡinvNum���䣬Ȼ������µ�invNum����
	 * @param chrID һ��ҪСд
	 * @param startNum ������꣬Ϊʵ����� ���startNum<=0 ����endNum<=0���򷵻�ȫ����Ϣ
	 * @param endNum �յ����꣬Ϊʵ���յ�
	 * @param binNum ���ָ��������Ŀ
	 * @param type 0����Ȩƽ�� 1��ȡ���ֵ��2����Ȩ����ƽ��--Ҳ���ǼӺ�
	 * @return ���û���ҵ���Ⱦɫ��λ�㣬�򷵻�null
	 */
	public double[] getRengeInfo(String chrID,int startNum,int endNum,int binNum,int type) {
		ChrMapReadsInfo chrMapReadsInfo = mapChrID2ReadsInfo.get(chrID.toLowerCase());
		if (chrMapReadsInfo == null) {
			logger.error("û�и�Ⱦɫ�壺" + chrID);
			return null;
		}
		int[] startEnd = correctStartEnd(chrID, startNum, endNum);
		if (startEnd == null) {
			return null;
		}
		int[] invNumReads = chrMapReadsInfo.getSumChrBpReads();
		if (invNumReads == null) {
			return null;
		}
		try {
			return getRengeInfoExp(invNumReads, startEnd[0], startEnd[1], binNum, type);
		} catch (Exception e) {
			return null;
		}
	}
	/**
	 * У�������start �� end
	 * @param chrID
	 * @param startNum
	 * @param endNum
	 * @return null ��ʾû��ͨ��У��
	 */
	private int[] correctStartEnd(String chrID, int startNum, int endNum) {
		if (startNum <=0) {
			startNum = 1; 
		}
		if (endNum <= 0 || endNum > (int)getChrLen(chrID) ) {
			endNum = (int)getChrLen(chrID);
		}
		if (startNum > endNum) {
			logger.error("��㲻�ܱ��յ��: "+chrID+" "+startNum+" "+endNum);
			return null;
		}
		startNum --; endNum --;
		return new int[]{startNum, endNum};
	}
	/**
	 * @param invNumReads ĳ��Ⱦɫ�������reads�ѵ����
	 * @param startNum
	 * @param endNum
	 * @param binNum
	 * @param type
	 * @return
	 */
	private double[] getRengeInfoExp(int[] invNumReads, int startNum,int endNum,int binNum,int type) {
		int leftNum = 0;//��invNumReads�е�ʵ�����
		int rightNum = 0;//��invNumReads�е�ʵ���յ�

		leftNum = startNum/invNum;
		double leftBias = (double)startNum/invNum-leftNum;//����߷ָ������ľ����ֵ
		double rightBias = 0;
		if (endNum%invNum==0) {
			rightNum = endNum/invNum - 1;//javaС��ת��int Ϊֱ��ȥ��С����
		} else  {
			rightNum = endNum/invNum;
			rightBias = rightNum + 1 - (double)endNum/invNum;//���ұ߷ָ����յ�ľ����ֵ
		}
		//////////////////////////////////////////////////////////////////////////////////////////////////////
		double[] tmpRegReads=new double[rightNum - leftNum + 1];
		int k=0;
		for (int i = leftNum; i <= rightNum; i++) {
			if (i >= invNumReads.length || k >= tmpRegReads.length) {
				break;
			}
			if (i < 0) {
				continue;
			}
			tmpRegReads[k] = invNumReads[i];
			k++;
		}
		normDouble(tmpRegReads);
		double[] tmp = null;
		try {
			tmp =  MathComput.mySpline(tmpRegReads, binNum,leftBias,rightBias,type);
		} catch (Exception e) {
			return null;
		}
		tmp = equationsCorrect(tmp);
		return tmp;
	}
	
	
	/**
	 * ������Ĺ�ʽ��������
	 * @param input
	 * @return
	 */
	private double[] equationsCorrect(double[] input) {
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
	/**
	 * ��ȡ��ԭʼ������Ҫ������׼���������
	 * ���������б�׼��
	 * �����doubleֱ���޸ģ������ء�<br>
	 * @param doubleInfo ��ȡ�õ���ԭʼvalue
	 * @return 
	 */
	protected abstract void normDouble(double[] readsInfo);
	/**
	 * ����������Ϣ�����Ƚϵı�ֵ��Ҳ���Ǿ�ֵ���������mapInfo��weight��
	 * �ڲ���׼��
	 * @param mapReads ��һ��mapReads��Ϣ
	 * @param mapReads2 �ڶ���mapReads��Ϣ
	 * @param mapInfo
	 */
	public static void CmpMapReg(MapReads mapReads, MapReads mapReads2, MapInfo mapInfo) {
		double value1 = mapReads.getRegionMean(mapInfo.getRefID(), mapInfo.getStartAbs(), mapInfo.getEndAbs());
		double value2 = mapReads2.getRegionMean(mapInfo.getRefID(), mapInfo.getStartAbs(), mapInfo.getEndAbs());
		mapInfo.setScore(value1/value2);
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

class ChrMapReadsInfo {
	String chrID;
	int invNum = 10;
	int type;
	long chrLength;
	
	/** ����ܽ����Ϣ */
	int[] SumChrBpReads;
	/** ����Ⱦɫ���ϵ�reads���� */
	long readsAllNum;
	/** ����Ⱦɫ���ϵ�reads�Ķѵ���֮�� */
	long readsAllPipNum;
	Equations FormulatToCorrectReads; 
	public ChrMapReadsInfo(String chrID, long chrLen, int invNumm, int sumType, Equations FormulatToCorrectReads) {
		this.chrID = chrID;
		this.chrLength = chrLen;
		this.invNum = invNumm;
		this.type = sumType;
		this.FormulatToCorrectReads = FormulatToCorrectReads;
	}
	
	public String getChrID() {
		return chrID;
	}
	public long getReadsChrNum() {
		return readsAllNum;
	}
	public long getReadsPipNum() {
		return readsAllPipNum;
	}
	public int[] getSumChrBpReads() {
		return SumChrBpReads;
	}
	
	/**
	 * ��ν�������˵ÿ��invNum��bp�Ͱ���invNumbp��ÿ��bp��Reads������ȡƽ������λ���������chrBpReads��
	 * ����chrBpReads����chrBpReads�����ֵ����invNum����ŵ�SumChrBpReads����
	 * ��Ϊ�����ô��ݣ������޸���SumChrBpReads���������
	 * @param chrBpReads ÿ�������reads�ۼ�ֵ
	 * @param invNum ����
	 * @param type ȡֵ���ͣ���λ����ƽ��ֵ��0��λ����1��ֵ ������Ĭ����λ��
	 * @param SumChrBpReads ��ÿ�������ڵ�
	 */
	protected void sumChrBp(int[] chrBpReads) {
		// //////////SumChrBpReads�趨//////////////////////////////////
		// ������Ǻܾ�ȷ�����һλ���ܲ�׼������ʵ��Ӧ��������ν��,Ϊ���㣬0λ��¼�ܳ��ȡ�����ʵ��bp����ʵ�ʳ���
		int SumLength = chrBpReads.length / invNum + 1;// ��֤���������������Ҫ��SumChrBpReads��һ��
		SumChrBpReads = new int[SumLength];// ֱ�Ӵ�0��ʼ��¼��1�����ڶ���invNum,Ҳ��ʵ����ͬ
		
		if (invNum == 1) {
			for (int i = 0; i < SumLength - 2; i++) {
				SumChrBpReads[i] = chrBpReads[i+1];
				readsAllPipNum = readsAllPipNum + chrBpReads[i+1];
			}
			return;
		 }
		 for (int i = 0; i < SumLength - 2; i++) {
			 int[] tmpSumReads=new int[invNum];//���ܵ�chrBpReads���ÿһ����ȡ����
			 int sumStart=i*invNum + 1; int k=0;//k������tmpSumReads���±꣬ʵ���±���У�����-1
			 for (int j = sumStart; j < sumStart + invNum; j++) {
				 tmpSumReads[k] = chrBpReads[j];
				 readsAllPipNum = readsAllPipNum + chrBpReads[j];
				 k++;
			 }
			 samplingSite(i, tmpSumReads);
		 }
	}
	/**
	 * 
	 * �ܽ�double���͵�chrBpReads��double�Ŀ��Կ��ǽ���unique mapping��reads���м��ִ���
	 * ����chrBpReads����chrBpReads�����ֵ����invNum����ŵ�SumChrBpReads����
	 * ��Ϊ�����ô��ݣ������޸���SumChrBpReads���������
	 * @param chrBpReads ÿ�������reads�ۼ�ֵ
	 * @param invNum ����
	 * @param type ȡֵ���ͣ���λ����ƽ��ֵ��0��λ����1��ֵ ������Ĭ����λ��
	 * @param SumChrBpReads ��ÿ�������ڵ�
	 */
	protected void sumChrBp(double[] chrBpReads, long[] chrReadsPipNum) {
		int SumLength = chrBpReads.length / invNum + 1;// ��֤���������������Ҫ��SumChrBpReads��һ��
		SumChrBpReads = new int[SumLength];// ֱ�Ӵ�0��ʼ��¼��1�����ڶ���invNum,Ҳ��ʵ����ͬ
		
		if (invNum == 1) {
			for (int i = 0; i < SumLength - 2; i++) {
				SumChrBpReads[i] = (int) Math.round(chrBpReads[i+1]);
				chrReadsPipNum[0] = chrReadsPipNum[0] + (int) Math.round(chrBpReads[i+1]);
			}
			return;
		}
		 for (int i = 0; i < SumLength - 2; i++) {
			 int[] tmpSumReads=new int[invNum];//���ܵ�chrBpReads���ÿһ����ȡ����
			 int sumStart=i*invNum + 1; int k=0;//k������tmpSumReads���±꣬ʵ���±���У�����-1
			 for (int j = sumStart; j < sumStart + invNum; j++)  {
				 tmpSumReads[k] = (int) Math.round(chrBpReads[j]);
				 chrReadsPipNum[0] = chrReadsPipNum[0] + (int) Math.round(chrBpReads[j]);
				 k++;
			 }
			 samplingSite(i, tmpSumReads);
		 }
	}
	
	private void samplingSite(int siteNum, int[] tmpSumReads) {
		 if (type == MapReadsAbs.SUM_TYPE_MEDIAN) //ÿ��һ������ȡ��������ÿ��10bpȡ����ȡ��λ��
			 SumChrBpReads[siteNum] = (int) MathComput.median(tmpSumReads);
		 else if (type == MapReadsAbs.SUM_TYPE_MEAN) 
			 SumChrBpReads[siteNum] = (int) MathComput.mean(tmpSumReads);
		 else //Ĭ��ȡ��λ��
			 SumChrBpReads[siteNum] = (int) MathComput.median(tmpSumReads);
	}

}