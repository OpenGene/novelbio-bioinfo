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
public class MapReads {
	Equations equations;
	public void setEquations(Equations equations) {
		this.equations = equations;
		//Ĭ���趨��������reads����СֵΪ0������У��С��0�Ķ���Ϊ0
		equations.setMin(0);
	}
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
	/**
	 * ����׼��
	 */
	public static final int NORMALIZATION_NO = 64;
	/**
	 * ��������ÿ��Ⱦɫ���еĻ�������-invNum���������reads��Ŀ
	 * chrID(Сд)--int[]
	 * ֱ�Ӵ�0��ʼ��¼��1����ڶ���invNum,Ҳ��ʵ����ͬ
	 */
	 Hashtable<String, int[]> hashChrBpReads=new Hashtable<String, int[]>();
	/**
	 * ��������mapping�ļ��г��ֹ���ÿ��chr �ĳ���
	 */
	 ArrayList<String[]> lsChrLength=new ArrayList<String[]>();
	 int invNum=10;
	 int tagLength=300;//��ReadMapFile������ֵ
	 String sep = "\t";
	 /**
	  * ������Ϣ,���ֶ�ΪСд
	  */
	 HashMap<String, Long> hashChrLen = new HashMap<String, Long>();
	 /**
	  * ÿ�����е�reads������long[]ֻ��0��Ч��ֻ��Ϊ�˵�ַ����
	  */
	 HashMap<String, long[]> hashChrReadsNum = new HashMap<String, long[]>();
	 /**
	  * ÿ�����еĶѵ�bp������long[]ֻ��0��Ч��ֻ��Ϊ�˵�ַ����
	  */
	 HashMap<String, long[]> hashChrPipNum = new HashMap<String, long[]>();
	 /**
	  * ÿ�����е�ƽ���ѵ��߶ȣ�int[]ֻ��0��Ч��ֻ��Ϊ�˵�ַ����
	  */
	 HashMap<String, Double> hashChrPipMean = new HashMap<String, Double>();
	 /**
	  * �����mapping�ļ�
	  */
	 String mapFile = "";
	 /**
	  * ChrID���ڵ���
	  */
	 int colChrID = 0;
	 /**
	  * ������ڵ���
	  */
	 int colStartNum = 1;
	 /**
	  * �յ����ڵ���
	  */
	 int colEndNum = 2;
	 /**
	  * ������,bed�ļ�һ���ڵ�����
	  */
	 int colCis5To3 = 5;
	 /**
	  * ����Ƿ�Ϊ������
	  * �����bed��1
	  */
	 int startRegion = 1;
	 /**
	  * �յ��Ƿ�Ϊ������
	  */
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
	 int NormalType = NORMALIZATION_ALL_READS;
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
	 public void setSplit( int colSplit, int splitStart)
	 {
		 colSplit--; splitStart--;
		 this.colSplit = colSplit;
		 this.splitStart = splitStart;
	 }
	 /**
	  * �ܹ��ж���reads������mapping�������ReadMapFile���ܵõ���
	  */
	 long allReadsNum = 0;
	 /**
	  * �ܹ��ж���reads������mapping�������ReadMapFile���ܵõ���
	  */
	 public long getAllReadsNum() {
		return allReadsNum;
	 }
	 /**
	  * �趨peak��bed�ļ�����һ��ΪchrID���ڶ���Ϊ��㣬������Ϊ�յ㣬
	  * ����ȥ��peak��ÿ��Ⱦɫ���bg���
	  * @param peakBedFile
	  * @param firstlinels1
	  * @return ls-0��chrID 1��bg
	  * ���е�һλ��chrAll����Ϣ
	  */
	 public ArrayList<String[]> getChIPBG(String peakBedFile, int firstlinels1)
	 {
		 ArrayList<String[]> lsResult = new ArrayList<String[]>();
		 ListHashBin gffHashPeak = new ListHashBin(true, 1, 2, 3, firstlinels1);
		 gffHashPeak.ReadGffarray(peakBedFile);
		 
		 double allReads = 0; int numAll = 0; double max = 0;
		 ArrayList<Integer> lsMidAll = new ArrayList<Integer>();
		 for (Entry<String, int[]> entry : hashChrBpReads.entrySet()) {
			String chrID = entry.getKey();
			double allReadsChr = 0; int numChr = 0; double maxChr = 0;
			ArrayList<Integer> lsMidChr = new ArrayList<Integer>();
			int[] info = entry.getValue();
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
	 
	 
	 static boolean booPrintChrID = true;
	 public static void setBooPrintChrID(boolean booPrintChrID) {
		 MapReads.booPrintChrID = booPrintChrID;
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
	 * <b>һ�㲻���޸�</b>
	 * mapFile ������ʲô�ָ������зָ��ģ�Ĭ����"\t"
	 */
	public void setSep(String sep) {
		this.sep = sep;
	}
	 /**
	  * ����Ƿ�Ϊ������,
	  * �����bed��1��bed�ļ������޸�
	  */
	public void setstartRegion(int startRegion) {
		this.startRegion = startRegion;
	}
//	/**
//	 * �Ƿ��м����У����û����С��0, ��bamת����bed�ļ��в��е��У���Ҫ��RNA-Seq��ʹ��
//	 * Ϊ��11��
//	 */
//	public void setColSplit(int colSplit) {
//		this.colSplit = colSplit;
//	}
	/**
	 * �趨�����ļ���ChrID�� ��㣬�յ��������<b>����ǳ����bed�ļ�����ô��������޸�</b>
	 * @param colChrID ChrID���ڵ���
	 * @param colStartNum ������ڵ�
	 * @param colEndNum �յ����ڵ���
	 */
	public void setColNum(int colChrID,int colStartNum,int colEndNum, int colCis5To3)
	{
		colChrID--; colStartNum--;colEndNum--;colCis5To3--;
		this.colChrID = colChrID;
		this.colStartNum = colStartNum;
		this.colEndNum = colEndNum;
		this.colCis5To3 = colCis5To3;
	}
	boolean uniqReads = false;
	int startCod = -1;
	/**
	 * ���mapping��������
	 */
	int colUnique = BedRecord.COL_MAPNUM + 1;
	boolean booUniqueMapping = true;
	Boolean cis5to3 = null;
	/**
	 * @param uniqReads ��reads mapping��ͬһ��λ��ʱ���Ƿ������һ��reads
	 * @param startCod ����㿪ʼ��ȡ��reads�ļ���bp�������õ� С��0��ʾȫ����ȡ ����reads���ȵ�����Ըò���
	 * @param colUnique Unique��reads����һ�� novelbio�ı���ڵ����У���1��ʼ����
	 * @param booUniqueMapping �ظ���reads�Ƿ�ֻѡ��һ��
	 * @param cis5to3 �Ƿ��ѡȡĳһ�����reads��null������
	 */
	public void setFilter(boolean uniqReads, int startCod, int colUnique, boolean booUniqueMapping, Boolean cis5to3)
	{
		this.uniqReads = uniqReads;
		this.startCod = startCod;
		this.colUnique = colUnique;
		this.booUniqueMapping = booUniqueMapping;
		this.cis5to3 = cis5to3;
	}
	/**
	 * @param invNum ÿ������λ����������趨Ϊ1�����㷨��仯��Ȼ���ܾ�ȷ
	 * @param mapFile mapping�Ľ���ļ���һ��Ϊbed��ʽ
	 */
	public MapReads(int invNum, String mapFile) 
	{
		this.invNum = invNum;
		this.mapFile = mapFile;
	}
	String chrLenFile = "";
	/**
	 * �趨Ⱦɫ�峤���ļ�
	 * @param chrLenFile
	 */
	public void setChrLenFile(String chrLenFile) {
		this.chrLenFile = chrLenFile;
		readChrLenFile(chrLenFile);
	}
	/**
	 * @param chrLenFile �����ļ���ָ��ÿ��Ⱦɫ��ĳ���<br>
	 * �ļ���ʽΪ�� chrID \t chrLen   �� chr1 \t  23456
	 * @param invNum ÿ������λ����
	 * @param mapFile mapping�Ľ���ļ���һ��Ϊbed��ʽ
	 */
	public MapReads(String chrLenFile,int invNum, String mapFile) 
	{
		hashChrLen = new HashMap<String, Long>();
		this.invNum = invNum;
		readChrLenFile(chrLenFile);
		this.mapFile = mapFile;
	}
	/**
	 * ÿ������λȡ��,����趨Ϊ1�����㷨��仯��Ȼ���ܾ�ȷ
	 * @return
	 */
	public int getBinNum() {
		return invNum;
	}
	/**
	 * ��mapping�ļ��л��ÿ��Ⱦɫ��ĳ��ȣ�
	 * Ҫ��mapping�ļ������Ź���Ȼ����ÿ��chr���reads���೤
	 * @throws Exception 
	 */
	private void setHashChrLen() throws Exception
	{
		if (hashChrLen.size() > 0) {
			return;
		}
		if (readChrLenFile(chrLenFile)) {
			return;
		}
		
		
		TxtReadandWrite txtMap = new TxtReadandWrite(mapFile, false);
		BufferedReader readerMap = txtMap.readfile();
		String content = ""; String chrID = ""; 
		String[] preSs = null;
		while ((content = readerMap.readLine()) != null) {
			String[] ss = content.split("\t");
			if (!ss[colChrID].equals(chrID)) {
				if (preSs != null) {
					hashChrLen.put(chrID.toLowerCase(), Long.parseLong(preSs[colEndNum]));
				}
				chrID = ss[colChrID];
			}
			preSs = ss;
		}
		hashChrLen.put(preSs[colChrID].toLowerCase(), Long.parseLong(preSs[colEndNum]));
	}
	
	private boolean readChrLenFile(String chrLenFile)
	{
		if (FileOperate.isFileExist(chrLenFile)) {
			try {
				TxtReadandWrite txtChrLen = new TxtReadandWrite(chrLenFile, false);
				ArrayList<String> lsChrLen = txtChrLen.readfileLs();
				for (String string : lsChrLen) {
					String[] ss = string.split("\t");
					hashChrLen.put(ss[0].toLowerCase(), Long.parseLong(ss[1]));
				}
				return true;
			} catch (Exception e) {
				logger.error("no chrLenFile file");
				e.printStackTrace();
				return false;
			}
		}
		return false;
	}
	
	public long ReadMapFile()
	{
		try {
			return ReadMapFileExp();
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	/**
	 * ������Ϊmacs��bed�ļ�ʱ���Զ�<b>����chrm��Ŀ</b><br>
	 * ����chr��Ŀ��Сд
	 * ��ȡMapping�ļ���������Ӧ��һά�������飬��󱣴���һ����ϣ���С�ע�⣬mapping�ļ��е�chrID��chrLengthFile�е�chrIDҪһ�£���������
	 * @param uniqReads ��reads mapping��ͬһ��λ��ʱ���Ƿ������һ��reads
	 * @param startCod ����㿪ʼ��ȡ��reads�ļ���bp�������õ� С��0��ʾȫ����ȡ ����reads���ȵ�����Ըò���
	 * @param colUnique Unique��reads����һ��
	 * @param booUniqueMapping �ظ���reads�Ƿ�ֻѡ��һ��
	 * @param cis5to3 �Ƿ��ѡȡĳһ�����reads��null������
	 * @return ��������mapping��reads����
	 * @throws Exception
	 */
	private long ReadMapFileExp() throws Exception 
	{
		setHashChrLen();
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
		int[] tmpOld = new int[2]; int count = 0;
		for (String content : txtmap.readlines()) {
			String[] tmp = content.split(sep);
			if (!tmp[colChrID].trim().toLowerCase().equals(lastChr)) // �������µ�chrID����ʼ�����ϵ�chrBpReads,Ȼ���½�chrBpReads�����װ���ϣ��
			{
				tmpOld = new int[2];//���� tmpOld
				if (!lastChr.equals("") && flag){ // ǰ���Ѿ�����һ��chrBpReads����ô��ʼ�ܽ����chrBpReads
					sumChrBp(chrBpReads, 1, SumChrBpReads, readsPipNum);
				}
				lastChr = tmp[colChrID].trim().toLowerCase();// ʵ�������³��ֵ�ChrID
				// ////////////////�ͷ��ڴ棬�о���������е��ã������ڴ浽1.2g�����˺󽵵�990m///////////////////////////
				if (booPrintChrID) {
//					if (count%200 == 0) {
						System.out.println(lastChr);
//					}
				}
//				chrBpReads = null;// �����ܲ����ͷŵ��ڴ�
//				System.gc();// ��ʽ����gc
				int chrLength = 0;
				// ///////chrBpReads�趨/////////////////////////
				try {
					chrLength =  hashChrLen.get(lastChr.toLowerCase()).intValue();
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
				hashChrBpReads.put(lastChr, SumChrBpReads);// ���³��ֵ�chrID���½���SumChrBpReadsװ��hash��
				readsChrNum = new long[1];
				hashChrReadsNum.put(lastChr, readsChrNum);
				readsPipNum = new long[1];
				hashChrPipNum.put(lastChr, readsPipNum);
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
				tmpOld = addLoc(tmp, uniqReads, tmpOld, startCod, cis5to3, chrBpReads,readsChrNum);
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
		  for (Entry<String, long[]> entry : hashChrReadsNum.entrySet()) {
			allReadsNum = allReadsNum + entry.getValue()[0];
			hashChrPipMean.put(entry.getKey(), (double)hashChrPipNum.get(entry.getKey())[0]/hashChrLen.get(entry.getKey()));
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
	 * ����һ�����е�������Ϣ���Լ�������Ҫ�ۼӵ���������
	 * ��������������ۼӵ�Ŀ��������ȥ
	 * @param chrLoc ����λ�㣬0Ϊ���곤�ȣ�1��ʼΪ�������꣬����chrLoc[123] ����ʵ��123λ������
	 * @param lsAddLoc ��ϵ���������Ϊint[2] ��list��Ʃ�� 100-250��280-300�����ӣ�ע���ṩ�����궼�Ǳ����䣬������λ���˶�Ҫ����
	 */
	protected void addChrLoc(int[] chrLoc, ArrayList<int[]> lsAddLoc)
	{
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
	 * Chr1	5242	5444	A80W3KABXX:8:44:8581:122767#GGCTACAT/2	255	-	5242	5444	255,0,0	2	30,20,40	0,120,160
	 * @param start ������� 5242���������������
	 * @param end �յ����� 5444���������������
	 * @param split �ָ���� 30,20,40
	 * @param splitStart ÿ���ָ������ 0,35,68
	 * @return ����һ��start��end
	 * ���ݼ����������
	 */
	protected ArrayList<int[]> getStartEndLoc(int start, int end, String split, String splitStart) {
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
	protected ArrayList<int[]> setStartCod(ArrayList<int[]> lsStartEnd, int StartCodLen, boolean cis5to3) {
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
	 * ����chrBpReads����chrBpReads�����ֵ����invNum����ŵ�SumChrBpReads����
	 * ��Ϊ�����ô��ݣ������޸���SumChrBpReads���������
	 * @param chrBpReads ÿ�������reads�ۼ�ֵ
	 * @param invNum ����
	 * @param type ȡֵ���ͣ���λ����ƽ��ֵ��0��λ����1��ֵ ������Ĭ����λ��
	 * @param SumChrBpReads ��ÿ�������ڵ�
	 */
	protected void sumChrBp(int[] chrBpReads,int type,int[] SumChrBpReads, long[] chrReadsPipNum) 
	{
		 int SumLength = chrBpReads.length/invNum - 1;//��֤�����������ΪjavaĬ�ϳ���ֱ�Ӻ���С����������������
		 if (invNum == 1) {
			for (int i = 0; i < SumLength; i++) {
				SumChrBpReads[i] = chrBpReads[i+1];
				chrReadsPipNum[0] = chrReadsPipNum[0] + chrBpReads[i+1];
			}
			return;
		 }
		 for (int i = 0; i < SumLength; i++)
		 {
			 int[] tmpSumReads=new int[invNum];//���ܵ�chrBpReads���ÿһ����ȡ����
			 int sumStart=i*invNum + 1; int k=0;//k������tmpSumReads���±꣬ʵ���±���У�����-1
			 for (int j = sumStart; j < sumStart + invNum; j++) 
			 {
				 tmpSumReads[k] = chrBpReads[j];
				 chrReadsPipNum[0] = chrReadsPipNum[0] + chrBpReads[j];
				 k++;
			 }
			 if (type==0) //ÿ��һ������ȡ��������ÿ��10bpȡ����ȡ��λ��
				 SumChrBpReads[i]=(int) MathComput.median(tmpSumReads);
			 else if (type==1) 
				 SumChrBpReads[i]=(int) MathComput.mean(tmpSumReads);
			 else //Ĭ��ȡ��λ��
				 SumChrBpReads[i]=(int) MathComput.median(tmpSumReads);
		 }
	}
	/**
	 * ���ǽ���unique mapping��reads���м��ִ���
	 * ����chrBpReads����chrBpReads�����ֵ����invNum����ŵ�SumChrBpReads����
	 * ��Ϊ�����ô��ݣ������޸���SumChrBpReads���������
	 * @param chrBpReads ÿ�������reads�ۼ�ֵ
	 * @param invNum ����
	 * @param type ȡֵ���ͣ���λ����ƽ��ֵ��0��λ����1��ֵ ������Ĭ����λ��
	 * @param SumChrBpReads ��ÿ�������ڵ�
	 */
	protected void sumChrBp(double[] chrBpReads,int type,int[] SumChrBpReads, long[] chrReadsPipNum) 
	{
		 int SumLength = chrBpReads.length/invNum - 1;//��֤�����������ΪjavaĬ�ϳ���ֱ�Ӻ���С����������������
		 if (invNum == 1) {
			for (int i = 0; i < SumLength; i++) {
				SumChrBpReads[i] = (int) Math.round(chrBpReads[i+1]);
				chrReadsPipNum[0] = chrReadsPipNum[0] + (int) Math.round(chrBpReads[i+1]);
			}
			return;
		}
		 for (int i = 0; i < SumLength; i++)
		 {
			 int[] tmpSumReads=new int[invNum];//���ܵ�chrBpReads���ÿһ����ȡ����
			 int sumStart=i*invNum + 1; int k=0;//k������tmpSumReads���±꣬ʵ���±���У�����-1
			 for (int j = sumStart; j < sumStart + invNum; j++) 
			 {
				 tmpSumReads[k] = (int) Math.round(chrBpReads[j]);
				 chrReadsPipNum[0] = chrReadsPipNum[0] + (int) Math.round(chrBpReads[j]);
				 k++;
			 }
			 if (type==0) //ÿ��һ������ȡ��������ÿ��10bpȡ����ȡ��λ��
				 SumChrBpReads[i]=(int) MathComput.median(tmpSumReads);
			 else if (type==1) 
				 SumChrBpReads[i]=(int) MathComput.mean(tmpSumReads);
			 else //Ĭ��ȡ��λ��
				 SumChrBpReads[i]=(int) MathComput.median(tmpSumReads);
		 }
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
	public  double[] getRengeInfo(int thisInvNum,String chrID,int startNum,int endNum,int type)
	{	
		if (startNum <=0 && endNum <=0) {
			startNum = 1; endNum = (int)getChrLen(chrID);
		}
		if (startNum > endNum) {
			logger.error("��㲻�ܱ��յ��: "+chrID+" "+startNum+" "+endNum);
		}
		////////////////////////����Ҫ�ָ���////////////////////////////////////////
		if (invNum == 1 && thisInvNum == 1) {
			double[] result = new double[endNum - startNum + 1];
			startNum--; endNum--;
			int[] invNumReads = hashChrBpReads.get(chrID.toLowerCase());
			if (invNumReads == null) {
				logger.info("û�и�Ⱦɫ�壺 " + chrID);
				return null;
			}
			int k = 0;
			for (int i = startNum; i <= endNum; i++) {
				result[k] = invNumReads[i];
				k++;
			}
			//��׼��
			normDouble(result, NormalType);
			if (equations != null) {
				result = equations.getYinfo(result);
			}
			return result;
		}
		///////////////////////////////////////////////////////////////////////////////
		double binNum = (double)(endNum - startNum + 1) / thisInvNum;
		int binNumFinal = 0;
		if (binNum - (int)binNum >= 0.7) {
			binNumFinal = (int)binNum + 1;
		}
		else {
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
	public  double[] getRengeInfo(String chrID,int startNum,int endNum,int binNum,int type) 
	{
		if (startNum <=0 && endNum <=0) {
			startNum = 1; endNum = (int)getChrLen(chrID);
		}
		
		if (startNum > endNum) {
			logger.error("��㲻�ܱ��յ��: "+chrID+" "+startNum+" "+endNum);
		}
		
		int[] invNumReads = hashChrBpReads.get(chrID.toLowerCase());
		if (invNumReads == null) {
			return null;
		}
		startNum--; endNum--;
		////////////////ȷ��Ҫ��ȡ�������˵���Ҷ˵�/////////////////////////////////
		int leftNum = 0;//��invNumReads�е�ʵ�����
		int rightNum = 0;//��invNumReads�е�ʵ���յ�

		leftNum = startNum/invNum;
		double leftBias = (double)startNum/invNum-leftNum;//����߷ָ������ľ����ֵ
		double rightBias = 0;
		if (endNum%invNum==0) 
			rightNum = endNum/invNum-1;//ǰ����javaС��ת��intֱͨͨ��ȥ��С����
		else 
		{
			rightNum = endNum/invNum;//ǰ����javaС��ת��intֱͨͨ��ȥ��С����
			rightBias = rightNum + 1 - (double)endNum/invNum;//���ұ߷ָ����յ�ľ����ֵ
		}
		//////////////////////////////////////////////////////////////////////////////////////////////////////
		double[] tmpRegReads=new double[rightNum - leftNum + 1];
		int k=0;
		try {
			for (int i = leftNum; i <= rightNum; i++) {
				if (i >= invNumReads.length) {
					break;
				}
				if (i < 0) {
					continue;
				}
				tmpRegReads[k] = invNumReads[i];
				k++;
			}
		} catch (Exception e) {
			logger.error("�±�Խ��"+e.toString());
		}
		normDouble(tmpRegReads, NormalType);
		double[] tmp = null;
		try {
			tmp =  MathComput.mySpline(tmpRegReads, binNum,leftBias,rightBias,type);
		} catch (Exception e) {
//			tmp =  MathComput.mySpline(tmpRegReads, binNum,leftBias,rightBias,type);
			return null;
		}
		if (equations != null) {
			tmp = equations.getYinfo(tmp);
		}
		return tmp;
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
	public  double[] getReadsDensity(String chrID,int startLoc,int endLoc,int binNum ) 
	{
		//���Ƚ�reads��׼��Ϊһ��400-500bp��Ĵ�飬ÿһ������Ӧ���Ǹ���������tags���������������������������ֵ
		//Ȼ�����ڴ������ͳ�ƣ�
		//��Ź�����һ�£������Ͽ����һ��tag��1.5����ʱ�������ȽϺ���
		int tagBinLength=(int)(tagLength*1.5);
		double[] tmpReadsNum= getRengeInfo(tagBinLength, chrID, startLoc, endLoc,1);
//		normDouble(tmpReadsNum, NormalType);
	/**	for (int i = 0; i < tmpReadsNum.length; i++) {
			if(tmpReadsNum[i]>1)
				System.out.println(tmpReadsNum[i]);
		}
		*/
		if (tmpReadsNum==null) {
			return null;
		}
		
		double[] resultTagDensityNum=MathComput.mySpline(tmpReadsNum, binNum, 0, 0, 2);
		return resultTagDensityNum;
	}
	/**
	 * ���Mapping�ļ���������chr�ĳ���
	 * @param refID
	 * @return int[]
	 * 0: ���chr����
	 * 1: �chr����
	 */
	public int[] getLimChrLength()
	{
		int[] result=new int[2];
		result[0]=Integer.parseInt(lsChrLength.get(0)[1]);
		result[1]=Integer.parseInt(lsChrLength.get(lsChrLength.size()-1)[1]);
		return result;
	}
	
	/**
	 *  ����mRNA�ļ��㣬������׼������equations����
	 * �����������䣬��Ҫ���ֵĿ��������ظö�������reads�����顣�����Ⱦɫ����mappingʱ�򲻴��ڣ��򷵻�null
	 * ��λ�������˵����ڵ� ��ȡinvNum���䣬Ȼ������µ�invNum����
	 * @param chrID һ��ҪСд
	 * @param binNum ���ָ��������Ŀ<b>��binNumΪ-1ʱ���������ܽᣬֱ�ӷ���invNumͳ�ƵĽ�������صĽ�����ȳ�</b>
	 * @param type 0����Ȩƽ�� 1��ȡ���ֵ��2����Ȩ����ƽ��--Ҳ���ǼӺ�
	 * @param lsIsoform �������ת¼�������<b>����int[0]&lt;int[1]������int[0]&gt;int[1]</b>
	 * @return ���صĶ������򣬽������Ҫ������������������������
	 * ���û�ҵ�ת¼������chr1_random֮��ID�ģ��Ǿ�����
	 */
	public double[] getRengeInfo(String chrID,int binNum,int type,ArrayList<int[]> lsIsoform) {
		if (lsIsoform == null || lsIsoform.size() == 0) {
			return null;
		}
		boolean cis5to3 = true;
		ArrayList<double[]> lsExonInfo = new ArrayList<double[]>();
		/**
		 * ���lsIsoform��int[0]<int[1]˵������
		 * ���lsIsoform��int[1]<int[0]˵������
		 */
		for (int[] is : lsIsoform) {
			if (is[0] < is[1]) {
				break;
			}
			else if (is[0] > is[1]) {
				cis5to3 = false;
				break;
			}
			else {
				logger.error("ת¼���������ӵ������յ�����һ��"+is[0]+" "+is[1]);
			}
		}
		
		if (cis5to3) {
			for (int[] is : lsIsoform) {
				double[] isoInfo = getRengeInfo(invNum, chrID, is[0], is[1], type);
				if (isoInfo != null) {
					lsExonInfo.add(isoInfo);
				}
			}
		}
		else {
			for (int i = lsIsoform.size() - 1; i >= 0; i--) {
				double[] isoInfo = getRengeInfo(invNum, chrID, lsIsoform.get(i)[1], lsIsoform.get(i)[0], type);
				if (isoInfo != null) {
					lsExonInfo.add(isoInfo);
				}
			}
		}
		int num = 0;
		for (double[] ds : lsExonInfo) {
			if (ds == null) {
				logger.error("�޷����reads��Ϣ��Ⱦɫ��Ϊ: " + chrID);
				return null;
			}
			num = num + ds.length;
		}
		double[] result = new double[num];
		int i = 0;
		for (double[] ds : lsExonInfo) {
			for (double d : ds) {
				result[i] = d;
				i++;
			}
		}
		if (binNum <= 0) {
			return result;
		}
		double[] resultTagDensityNum=MathComput.mySpline(result, binNum, 0, 0, 0);
		return resultTagDensityNum;
	}
	
	/**
	 * �����doubleֱ���޸ģ������ء�<br>
	 * ���õ��Ľ����Ҫ���ֵ
	 * ����double���飬����reads�������б�׼��,reads�����ɶ�ȡ��mapping�ļ��Զ����<br>
	 * ����ȳ���1millionȻ���ٳ���ÿ��double��ֵ<br>
	 * @param doubleInfo
	 * @param NormType ����ѡ��MapReads��NORMALIZATION��,����������У����޸�
	 * @return 
	 */
	public void normDouble(double[] doubleInfo, int NormalType) {
		if (doubleInfo == null) {
			return;
		}
		if (NormalType == NORMALIZATION_NO) 
		{
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
	
	/**
	 * ������׼��
	 * �������귶Χ�����ظ���������Сֵ
	 * @param chrID
	 * @param startLoc
	 * @param endLoc
	 * @return double
	 */
	public double regionMin(String chrID, int startLoc, int endLoc) {
		double[] info = getRengeInfo(invNum, chrID, startLoc, endLoc, 0);
		return new Min().evaluate(info);
	}
	
	/**
	 * �������귶Χ�����ظ����������ֵ
	 * @param chrID
	 * @param startLoc
	 * @param endLoc
	 * @return double
	 */
	public double regionMax(String chrID, int startLoc, int endLoc) {
		double[] info = getRengeInfo(invNum, chrID, startLoc, endLoc, 0);
		return new Max().evaluate(info);
	}
	/**
	 * ������׼��
	 * �������귶Χ�����ظ�������ƽ��ֵ
	 * @param chrID
	 * @param startLoc
	 * @param endLoc
	 * @return double
	 */
	public double regionMean(String chrID, int startLoc, int endLoc)
	{
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
	 * 
	 */
	public ArrayList<int[]> region0Info(String chrID, int startLocT, int endLocT)
	{
		int startLoc = Math.min(startLocT, endLocT);
		int endLoc = Math.max(startLocT, endLocT);
		startLoc--; endLoc--;
		int[] invNumReads = hashChrBpReads.get(chrID.toLowerCase());
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
	 * �������귶Χ�����ظ������ڱ�׼��
	 * @param chrID
	 * @param startLoc
	 * @param endLoc
	 * @return double
	 */
	public double regionSD(String chrID, int startLoc, int endLoc) {
		double[] info = getRengeInfo(invNum, chrID, startLoc, endLoc, 0);
		return new StandardDeviation().evaluate(info);
	}
	/**
	 * 
	 * �������귶Χ�����ظ������ڱ�׼��
	 * @param chrID Ⱦɫ����
	 * @param lsLoc һ��ת¼����exon list
	 * @return
	 */
	public double regionSD(String chrID, List<int[]> lsLoc) {
		return new StandardDeviation().evaluate(getRegionInfo(chrID, lsLoc));
	}
	/**
	 * �������귶Χ�����ظ�������ƽ��ֵ
	 * @param chrID
	 * @param lsLoc һ��ת¼����exon list
	 * @return
	 */
	public double regionMean(String chrID, List<int[]> lsLoc)
	{
		return new Mean().evaluate(getRegionInfo(chrID, lsLoc));
	}
	/**
	 * �������귶Χ�����ظ������ڵ���Ϣ
	 * @param chrID
	 * @param lsLoc һ��ת¼����exon list
	 * @return
	 */
	public double[] getRegionInfo(String chrID, List<int[]> lsLoc) {
		ArrayList<double[]> lstmp = new ArrayList<double[]>();
		for (int[] is : lsLoc) {
			int min = Math.min(is[0], is[1]);
			int max = Math.max(is[0], is[1]);
			double[] info = getRengeInfo(invNum, chrID, min,max, 0);
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
			double[] Info = getRengeInfo(thisInvNum, mapInfo.getRefID(), mapInfo.getStart(), mapInfo.getEnd(), type);
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
		double[] Info = getRengeInfo(thisInvNum, mapInfo.getRefID(), mapInfo.getStart(), mapInfo.getEnd(), type);
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
			double[] Info = getRengeInfo(mapInfo.getRefID(), mapInfo.getStart(), mapInfo.getEnd(), binNum, type);
			if (Info == null) {
				lsmapInfo.remove(i); i--;
				logger.error("����δ֪ID��"+mapInfo.getName() + " "+mapInfo.getRefID() + " " + mapInfo.getStart() + " "+ mapInfo.getEnd());
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
			double[] Info = getRengeInfo(mapInfo.getRefID(), mapInfo.getStart(), mapInfo.getEnd(), binNum, type);
			if (Info == null) {
				logger.error("����δ֪ID��"+mapInfo.getName() + " "+mapInfo.getRefID() + " " + mapInfo.getStart() + " "+ mapInfo.getEnd());
			}
			mapInfo.setDouble(Info);
	}
	/**
	 * ������õ���ʵ��ĳ��Ⱦɫ��ĳ���
	 */
	public long getChrLen(String chrID) {
		return hashChrLen.get(chrID.toLowerCase());
	}
	/**
	 * ������õ���ʵ��ĳ��Ⱦɫ����������reads��Ŀ
	 */
	public long getChrReadsNum(String chrID) {
		return hashChrReadsNum.get(chrID.toLowerCase())[0];
	}
	/**
	 * ������õ���ʵ��ĳ��Ⱦɫ��ĳ���
	 */
	public long getChrReadsPipNum(String chrID) {
		return hashChrPipNum.get(chrID.toLowerCase())[0];
	}
	/**
	 * ������õ���ʵ��ĳ��Ⱦɫ��߶ȵ�ƽ��ֵ
	 */
	public double getChrReadsPipMean(String chrID) {
		return hashChrPipMean.get(chrID.toLowerCase());
	}
	
	/**
	 * ��������chrID��list
	 * @return
	 */
	public ArrayList<String> getChrIDLs() {
		return ArrayOperate.getArrayListKey(hashChrLen);
	}
	
	/**
	 * ����������Ϣ�����Ƚϵı�ֵ��Ҳ���Ǿ�ֵ���������mapInfo��weight��
	 * �ڲ���׼��
	 * @param mapReads ��һ��mapReads��Ϣ
	 * @param mapReads2 �ڶ���mapReads��Ϣ
	 * @param mapInfo
	 */
	public static void CmpMapReg(MapReads mapReads, MapReads mapReads2, MapInfo mapInfo)
	{
		double value1 = mapReads.regionMean(mapInfo.getRefID(), mapInfo.getStart(), mapInfo.getEnd());
		double value2 = mapReads2.regionMean(mapInfo.getRefID(), mapInfo.getStart(), mapInfo.getEnd());
		mapInfo.setScore(value1/value2);
	}
	
}
