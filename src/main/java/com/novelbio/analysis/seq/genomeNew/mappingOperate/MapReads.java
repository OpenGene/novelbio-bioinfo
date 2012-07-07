package com.novelbio.analysis.seq.genomeNew.mappingOperate;


import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math.stat.descriptive.rank.Max;
import org.apache.commons.math.stat.descriptive.rank.Min;
import org.apache.log4j.Logger;
import org.junit.experimental.max.MaxCore;

import com.novelbio.analysis.seq.BedRecord;
import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.genomeNew.gffOperate.ListHashBin;
import com.novelbio.analysis.seq.genomeNew.gffOperate.ListDetailBin;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.Equations;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.dataStructure.listOperate.ListCodAbs;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * �������ڴ����Ƶı�
 * 
 * @author zong0jie
 * 
 */
public class MapReads extends MapReadsAbs{
	private static Logger logger = Logger.getLogger(MapReads.class);
	
	/**
	 * ��ÿ��double[]���/double.length
	 * Ҳ���ǽ�ÿ������Ը�gene��ƽ���������
	 */
	public static final int NORMALIZATION_PER_GENE = 128;
	/**
	 * ��ÿ��double[]*1million/AllReadsNum
	 * Ҳ���ǽ�ÿ������Բ������
	 */
	public static final int NORMALIZATION_ALL_READS = 256;
	/** ����׼�� */
	public static final int NORMALIZATION_NO = 64;
	
	 /** ÿ�����е�reads������long[]ֻ��0��Ч��ֻ��Ϊ�˵�ַ���� */
	 HashMap<String, long[]> mapChrID2ReadsNum = new HashMap<String, long[]>();
	 /** ÿ�����еĶѵ�bp������long[]ֻ��0��Ч��ֻ��Ϊ�˵�ַ���� */
	 HashMap<String, long[]> mapChrID2PipNum = new HashMap<String, long[]>();
	 /** ÿ�����е�ƽ���ѵ��߶ȣ�int[]ֻ��0��Ч��ֻ��Ϊ�˵�ַ���� */
	 HashMap<String, Double> mapChrID2PipMean = new HashMap<String, Double>();
	 
	 /** ChrID���ڵ��� */
	 int colChrID = 0;
	 /** ������ڵ��� */
	 int colStartNum = 1;
	 /** �յ����ڵ��� */
	 int colEndNum = 2;
	 /** ������,bed�ļ�һ���ڵ����� */
	 int colCis5To3 = 5;
	 /**
	  * ����Ƿ�Ϊ������
	  * �����bed��1
	  */
	 int startRegion = 1;
	 /** �յ��Ƿ�Ϊ������ */
	 int endRegion = 0;
	 /**
	  * �Ƿ��м����У����û����С��0, ��bamת����bed�ļ��в��е��У���Ҫ��RNA-Seq��ʹ��
	  * Ϊ��11��
	  */
	 int colSplit = -1;
	 /**
	  * �����е����ȣ���0,34,68�����û����С��0, ��bamת����bed�ļ��в��е��У���Ҫ��RNA-Seq��ʹ��
	  * Ϊ��12��
	  */
	 int splitStart = -1;
	 /** ���ڽ���ı�׼������ */
	 int NormalType = NORMALIZATION_ALL_READS;
	 /** �ܹ��ж���reads������mapping�������ReadMapFile���ܵõ���*/
	 long allReadsNum = 0;
	 boolean uniqReads = false;
	 int startCod = -1;
	 /**
	  * ���mapping��������
	  */
	 int colUnique = BedRecord.COL_MAPNUM + 1;
	 boolean booUniqueMapping = true;
	 /** ��ѡȡĳ�������reads */
	 Boolean FilteredStrand = null;

	
	 /** �ܹ��ж���reads������mapping�������ReadMapFile���ܵõ��� */
	public long getAllReadsNum() {
		return allReadsNum;
	}
	/**
	 * @param uniqReads ��reads mapping��ͬһ��λ��ʱ���Ƿ������һ��reads
	 * @param startCod ����㿪ʼ��ȡ��reads�ļ���bp�������õ� С��0��ʾȫ����ȡ ����reads���ȵ�����Ըò���
	 * @param colUnique  Unique��reads����һ�� novelbio�ı���ڵ����У���1��ʼ����
	 * @param booUniqueMapping �ظ���reads�Ƿ�ֻѡ��һ��
	 * @param cis5to3 �Ƿ��ѡȡĳһ�����reads��null������
	 */
	public void setFilter(boolean uniqReads, int startCod, int colUnique, boolean booUniqueMapping, Boolean FilteredStrand) {
		this.uniqReads = uniqReads;
		this.startCod = startCod;
		this.colUnique = colUnique;
		this.booUniqueMapping = booUniqueMapping;
		this.FilteredStrand = FilteredStrand;
	}

	/**
	 * @param invNum
	 *            ÿ������λ����������趨Ϊ1�����㷨��仯��Ȼ���ܾ�ȷ
	 * @param mapFile
	 *            mapping�Ľ���ļ���һ��Ϊbed��ʽ
	 */
	public MapReads(int invNum, String mapFile) {
		super(invNum, mapFile);
	}
	/**
	 * @param chrLenFile �����ļ���ָ��ÿ��Ⱦɫ��ĳ���<br>
	 * �ļ���ʽΪ�� chrID \t chrLen   �� chr1 \t  23456
	 * @param invNum ÿ������λ����
	 * @param mapFile mapping�Ľ���ļ���һ��Ϊbed��ʽ
	 */
	public MapReads(String chrLenFile,int invNum, String mapFile) {
		super(chrLenFile, invNum, mapFile);
	}
	/**
	 * ������õ���ʵ��ĳ��Ⱦɫ����������reads��Ŀ
	 */
	public long getChrReadsNum(String chrID) {
		return mapChrID2ReadsNum.get(chrID.toLowerCase())[0];
	}
	/**
	 * ������õ���ʵ��ĳ��Ⱦɫ��ĳ���
	 */
	public long getChrReadsPipNum(String chrID) {
		return mapChrID2PipNum.get(chrID.toLowerCase())[0];
	}
	/**
	 * ������õ���ʵ��ĳ��Ⱦɫ��߶ȵ�ƽ��ֵ
	 */
	public double getChrReadsPipMean(String chrID) {
		return mapChrID2PipMean.get(chrID.toLowerCase());
	}
	 /**
	  * �趨��׼��������������ʱ�趨����һ��Ҫ�ڶ�ȡ�ļ�ǰ
	  * Ĭ����NORMALIZATION_ALL_READS
	  * @param normalType
	  */
	 public void setNormalType(int normalType) {
		NormalType = normalType;
	}
	 /**
	  * <b>RNA-Seqʹ��</b><br>
	  * ����λ���е��趨
	  * @param colSplit �Ƿ��м����У����û����С��0, ��bamת����bed�ļ��в��е��У���Ҫ��RNA-Seq��ʹ�á�����еĻ���һ��Ϊ11��
	  * @param splitStart �����е����ȣ���0,34,68�����û����С��0, ��bamת����bed�ļ��в��е��У���Ҫ��RNA-Seq��ʹ�á�����еĻ���һ��Ϊ12��
	  */
	 public void setSplit( int colSplit, int splitStart)  {
		 colSplit--; splitStart--;
		 this.colSplit = colSplit;
		 this.splitStart = splitStart;
	 }
	 /**
	  * ����Ƿ�Ϊ������,
	  * �����bed��1��bed�ļ������޸�
	  */
	public void setstartRegion(int startRegion) {
		this.startRegion = startRegion;
	}
	/**
	 * �趨�����ļ���ChrID�� ��㣬�յ��������<b>����ǳ����bed�ļ�����ô��������޸�</b>
	 * @param colChrID ChrID���ڵ���
	 * @param colStartNum ������ڵ�
	 * @param colEndNum �յ����ڵ���
	 */
	public void setColNum(int colChrID,int colStartNum,int colEndNum, int colCis5To3) {
		colChrID--; colStartNum--;colEndNum--;colCis5To3--;
		this.colChrID = colChrID;
		this.colStartNum = colStartNum;
		this.colEndNum = colEndNum;
		this.colCis5To3 = colCis5To3;
	}

	private void setChrLenFromReadBed() {
		if (mapChrID2Len.size() > 0)
			return;
		
		TxtReadandWrite txtMap = new TxtReadandWrite(mapFile, false);
		String chrID = ""; String[] lastSs = null;
		for (String content : txtMap.readlines()) {
			String[] ss = content.split("\t");
			if (!ss[colChrID].equals(chrID)) {
				if (lastSs != null) {
					mapChrID2Len.put(chrID.toLowerCase(), Long.parseLong(lastSs[colEndNum]));
				}
				chrID = ss[colChrID];
			}
			lastSs = ss;
		}
		mapChrID2Len.put(lastSs[colChrID].toLowerCase(), Long.parseLong(lastSs[colEndNum]));
	}
	/**
	 * ������׼������equations����
	 * ����Ⱦɫ�壬�������յ㣬���ظ�Ⱦɫ����tag���ܶȷֲ��������Ⱦɫ����mappingʱ�򲻴��ڣ��򷵻�null
	 * @param chrID Сд
	 * @param startLoc ������꣬Ϊʵ����� ���startNum<=0 ����endNum<=0���򷵻�ȫ����Ϣ
	 * @param endLoc 
	 * @param binNum ���ָ�Ŀ���
	 * @return
	 */
	public  double[] getReadsDensity(String chrID,int startLoc,int endLoc,int binNum ) {
		//���Ƚ�reads��׼��Ϊһ��400-500bp��Ĵ�飬ÿһ������Ӧ���Ǹ���������tags���������������������������ֵ
		//Ȼ�����ڴ������ͳ�ƣ�
		//��Ź�����һ�£������Ͽ����һ��tag��1.5����ʱ�������ȽϺ���
		int tagBinLength=(int)(tagLength*1.5);
		double[] tmpReadsNum = getRengeInfo(tagBinLength, chrID, startLoc, endLoc,1);
		if (tmpReadsNum==null) {
			return null;
		}
		double[] resultTagDensityNum=MathComput.mySpline(tmpReadsNum, binNum, 0, 0, 2);
		return resultTagDensityNum;
	}
	/**
	 * ������Ϊmacs��bed�ļ�ʱ���Զ�<b>����chrm��Ŀ</b><br>
	 * ����chr��Ŀ��Сд
	 * ��ȡMapping�ļ���������Ӧ��һά�������飬��󱣴���һ����ϣ���С�ע�⣬mapping�ļ��е�chrID��chrLengthFile�е�chrIDҪһ�£���������
	 * @param uniqReads ��reads mapping��ͬһ��λ��ʱ���Ƿ������һ��reads
	 * @param startCod ����㿪ʼ��ȡ��reads�ļ���bp�������õ� С��0��ʾȫ����ȡ ����reads���ȵ�����Ըò���
	 * @param colUnique Unique��reads����һ��
	 * @param booUniqueMapping �ظ���reads�Ƿ�ֻѡ��һ��
	 * @param FilteredStrand �Ƿ��ѡȡĳһ�����reads��null������
	 * @return ��������mapping��reads����
	 * @throws Exception
	 */
	protected long ReadMapFileExp() throws Exception {
		setChrLenFromReadBed();
		colUnique--;
		if (startCod > 0 && colCis5To3 < 0) {
			logger.error("�����趨startCod����Ϊû���趨������");
			return -1;
		}
//		��һ��startRegion�Ƿ�������
 		//��ν�������˵ÿ��invNum��bp�Ͱ���invNumbp��ÿ��bp��Reads������ȡƽ������λ���������chrBpReads��
		/////////////////////////////////////////���ÿ��Ⱦɫ��ĳ��Ȳ�������hashChrLength��////////////////////////////////////////////////////
		int[] chrBpReads=null;//����ÿ��bp��reads�ۼ���
		int[] SumChrBpReads=null;//ֱ�Ӵ�0��ʼ��¼��1����ڶ���invNum,Ҳ��ʵ����ͬ
		/////////////////���ļ���׼������///////////////////////////////////////////////////
		TxtReadandWrite txtmap=new TxtReadandWrite(mapFile,false);
		String lastChr="";
		long[] readsChrNum = new long[1];
		long[] readsPipNum = new long[1];
		////////////////////////////////////////////////////////////////////////////////////////////////
		//�ȼ���mapping����Ѿ�����ã�����Ⱦɫ���Ѿ��ֿ��á�
		boolean flag = true;// ��û�и�Ⱦɫ��ʱ���Ϊfalse�����������и�Ⱦɫ���ϵ�����
		int[] tmpOld = new int[2];
		for (String content : txtmap.readlines()) {
			String[] tmp = content.split(sep);
			if (!tmp[colChrID].trim().toLowerCase().equals(lastChr)) // �������µ�chrID����ʼ�����ϵ�chrBpReads,Ȼ���½�chrBpReads�����װ���ϣ��
			{
				tmpOld = new int[2];//���� tmpOld
				if (!lastChr.equals("") && flag){ // ǰ���Ѿ�����һ��chrBpReads����ô��ʼ�ܽ����chrBpReads
					sumChrBp(chrBpReads, 1, SumChrBpReads, readsPipNum);
				}
				lastChr = tmp[colChrID].trim().toLowerCase();// ʵ�������³��ֵ�ChrID
				if (booPrintChrID) {
					System.out.println(lastChr);
				}
				int chrLength = 0;
				try {
					chrLength =  mapChrID2Len.get(lastChr.toLowerCase()).intValue();
					flag = true;
				} catch (Exception e) {
					logger.error("����δ֪chrID "+lastChr);
					flag = false; continue;
				}

				chrBpReads = new int[chrLength + 1];// ͬ��Ϊ���㣬0λ��¼�ܳ��ȡ�����ʵ��bp����ʵ�ʳ���
				chrBpReads[0] = (int) chrLength;
				// //////////SumChrBpReads�趨//////////////////////////////////
				// ������Ǻܾ�ȷ�����һλ���ܲ�׼������ʵ��Ӧ��������ν��,Ϊ���㣬0λ��¼�ܳ��ȡ�����ʵ��bp����ʵ�ʳ���
				int SumLength = chrBpReads.length / invNum + 1;// ��֤���������������Ҫ��SumChrBpReads��һ��
				SumChrBpReads = new int[SumLength];// ֱ�Ӵ�0��ʼ��¼��1����ڶ���invNum,Ҳ��ʵ����ͬ
				// //////////���³��ֵ�chrװ���ϣ��////////////////////////////////
				mapChr2BpReads.put(lastChr, SumChrBpReads);// ���³��ֵ�chrID���½���SumChrBpReadsװ��hash��
				readsChrNum = new long[1];
				mapChrID2ReadsNum.put(lastChr, readsChrNum);
				readsPipNum = new long[1];
				mapChrID2PipNum.put(lastChr, readsPipNum);
				// ///////////��ÿһ�����г���װ��lsChrLength///////////////////
				String[] tmpChrLen = new String[2];
				tmpChrLen[0] = lastChr;
				tmpChrLen[1] = chrLength + "";
				lsChrLength.add(tmpChrLen);
			}
			////////////////////����λ��Ӻ�chrBpReads////////////////////////////////
			if (flag == false) //û�иû���������
				continue;
			//TODO ���� uniqe mapping ��������Ҫ�޸ģ���Ϊ��һ tmp[colUnique]���治�������أ�
			if (!booUniqueMapping || colUnique < 0 || tmp.length <= colUnique || Integer.parseInt(tmp[colUnique]) <= 1) {
				tmpOld = addLoc(tmp, uniqReads, tmpOld, startCod, FilteredStrand, chrBpReads,readsChrNum);
			}
		}
		///////////////////ѭ��������Ҫ�����һ�ε��������ܽ�////////////////////////////////////
		if (flag) {
			sumChrBp(chrBpReads, 1, SumChrBpReads, readsPipNum);
		}
		 ////////////////////////////��lsChrLength����chrLen��С�����������/////////////////////////////////////////////////////////////////////////////
		  Collections.sort(lsChrLength,new Comparator<String[]>(){
	            public int compare(String[] arg0, String[] arg1)
	            {
	               if( Integer.parseInt(arg0[1])<Integer.parseInt(arg1[1]))
	            	   return -1;
	            else if (Integer.parseInt(arg0[1])==Integer.parseInt(arg1[1])) 
					return 0;
	             else 
					return 1;
	            }
	        });
		  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		  allReadsNum = 0;
		  for (Entry<String, long[]> entry : mapChrID2ReadsNum.entrySet()) {
			allReadsNum = allReadsNum + entry.getValue()[0];
			mapChrID2PipMean.put(entry.getKey(), (double)mapChrID2PipNum.get(entry.getKey())[0]/mapChrID2Len.get(entry.getKey()));
		  }
		  return allReadsNum;
	}
	/**
	 * ����Ӻ͵Ĵ�����
	 * ����һ����Ϣ�����������ݼӵ���Ӧ��������
	 * @param tmp ���зָ�����Ϣ
	 * @param uniqReads ͬһλ����Ӻ��Ƿ��ȡ
	 * @param tmpOld ��һ�������յ㣬�����ж��Ƿ�����ͬһλ�����
	 * @param startCod ֻ��ȡǰ��һ�εĳ���
	 * @param cis5to3 �Ƿ�ֻѡȡĳһ����������У�Ҳ����������������лᱻ���ˣ����������
	 * null��ʾ�����з������
	 * @param chrBpReads ������Ҫ���ӵ�Ⱦɫ����Ϣ
	 * @param readsNum ��¼�ܹ�mapping��reads������Ϊ���ܹ�������ȥ���������鷽ʽ
	 * @return
	 * ��λ�����Ϣ��������һ���ж��Ƿ���ͬһλ��
	 */
	protected int[] addLoc(String[] tmp,boolean uniqReads,int[] tmpOld,int startCod, Boolean cis5to3, int[] chrBpReads, long[] readsNum) {
		boolean cis5to3This = true;
		if (colCis5To3 >= 0 && tmp.length > colCis5To3) {
			cis5to3This = tmp[colCis5To3].trim().equals("+");
		}
		if (cis5to3 != null && cis5to3This != cis5to3.booleanValue()) {
			return tmpOld;
		}
		
		int[] tmpStartEnd = new int[2];
		tmpStartEnd[0] = Integer.parseInt(tmp[colStartNum]) + startRegion;//��reads �����
		tmpStartEnd[1] = Integer.parseInt(tmp[colEndNum]) + endRegion;//��reads���յ�

		//�����reads����һ��reads��ͬ������Ϊ����������������
		if (uniqReads && tmpStartEnd[0] == tmpOld[0] && tmpStartEnd[1] == tmpOld[1] ) {
			return tmpOld;
		}

		ArrayList<int[]> lsadd = null;
		//���û�пɱ����
		if (colSplit >= 0 && splitStart >=0) {
			lsadd = getStartEndLoc(tmpStartEnd[0], tmpStartEnd[1], tmp[colSplit], tmp[splitStart]);
			lsadd = setStartCod(lsadd, startCod, cis5to3This);
		}
		else {
			lsadd = getStartEndLoc(tmpStartEnd[0], tmpStartEnd[1], null,null);
			lsadd = setStartCod(lsadd, startCod, cis5to3This);
		}
		addChrLoc(chrBpReads, lsadd);
		readsNum[0]++;
		return tmpStartEnd;
	}
	/**
	 * Chr1	5242	5444	A80W3KABXX:8:44:8581:122767#GGCTACAT/2	255	-	5242	5444	255,0,0	2	30,20,40	0,120,160
	 * @param start ������� 5242���������������
	 * @param end �յ����� 5444���������������
	 * @param split �ָ���� 30,20,40
	 * @param splitStart ÿ���ָ������ 0,35,68
	 * @return ����һ��start��end
	 * ���ݼ����������
	 */
	private ArrayList<int[]> getStartEndLoc(int start, int end, String split, String splitStart) {
		ArrayList<int[]> lsStartEnd = new ArrayList<int[]>();
		if (split == null || split.equals("") || !split.contains(",")) {
			int[] startend = new int[2];
			startend[0] = start;
			startend[1] = end;
			lsStartEnd.add(startend);
			return lsStartEnd;
		}
		String[] splitLen = split.trim().split(",");
		String[] splitLoc = splitStart.trim().split(",");
		for (int i = 0; i < splitLen.length; i++) {
			int[] startend = new int[2];
			startend[0] = start + Integer.parseInt(splitLoc[i]);
			startend[1] = startend[0] + Integer.parseInt(splitLen[i]) - 1;
			lsStartEnd.add(startend);
		}
		return lsStartEnd;
	}
	/**
	 * �����������ȡ��Ӧ��������󷵻���Ҫ�ۼӵ�ArrayList<int[]>
	 * @param lsStartEnd
	 * @param cis5to3
	 * @return ���cis5to3 = True����ô���Ž�ȡstartCod���ȵ�����
	 * ���cis5to3 = False����ô���Ž�ȡstartCod���ȵ�����
	 */
	private ArrayList<int[]> setStartCod(ArrayList<int[]> lsStartEnd, int StartCodLen, boolean cis5to3) {
		if (StartCodLen <= 0) {
			return lsStartEnd;
		}
		ArrayList<int[]> lsResult = new ArrayList<int[]>();
		if (cis5to3) {
			for (int i = 0; i < lsStartEnd.size(); i++) {
				if (StartCodLen - (lsStartEnd.get(i)[1] - lsStartEnd.get(i)[0] +1) > 0) {
					lsResult.add(lsStartEnd.get(i));
					StartCodLen = StartCodLen - (lsStartEnd.get(i)[1] - lsStartEnd.get(i)[0] +1);
				}
				else {
					int[] last = new int[2];
					last[0] = lsStartEnd.get(i)[0];
					last[1] = lsStartEnd.get(i)[0] + StartCodLen - 1;
					lsResult.add(last);
					break;
				}
			}
		}
		else {
			for (int i = lsStartEnd.size() - 1; i >= 0; i--) {
				if (StartCodLen - (lsStartEnd.get(i)[1] - lsStartEnd.get(i)[0] +1) > 0) {
					lsResult.add(0,lsStartEnd.get(i));
					StartCodLen = StartCodLen - (lsStartEnd.get(i)[1] - lsStartEnd.get(i)[0] + 1);
				}
				else {
					int[] last = new int[2];
					last[1] = lsStartEnd.get(i)[1];
					last[0] = last[1] - StartCodLen + 1;
					lsResult.add(0,last);
				}
			}
		}
		return lsResult;
	}
	/**
	 * ����һ�����е�������Ϣ���Լ�������Ҫ�ۼӵ���������
	 * ��������������ۼӵ�Ŀ��������ȥ
	 * @param chrLoc ����λ�㣬0Ϊ���곤�ȣ�1��ʼΪ�������꣬����chrLoc[123] ����ʵ��123λ������
	 * @param lsAddLoc ��ϵ���������Ϊint[2] ��list��Ʃ�� 100-250��280-300�����ӣ�ע���ṩ�����궼�Ǳ����䣬������λ���˶�Ҫ����
	 */
	private void addChrLoc(int[] chrLoc, ArrayList<int[]> lsAddLoc) {
		for (int[] is : lsAddLoc) {
			for (int i = is[0]; i <= is[1]; i++) {
				if (i >= chrLoc.length) {
					logger.info("������Χ��"+ i);
					break;
				}
				if (i < 0) {
					logger.info("������Χ��"+ i);
					continue;
				}
				chrLoc[i]++;
			}
		}
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
	public void normDouble(double[] doubleInfo) {
		if (doubleInfo == null) {
			return;
		}
		if (NormalType == NORMALIZATION_NO) {
			//����ɶҲ����
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
}
