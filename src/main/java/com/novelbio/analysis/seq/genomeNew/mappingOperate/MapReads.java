package com.novelbio.analysis.seq.genomeNew.mappingOperate;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.BedRecord;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.database.model.species.Species;
/**
 * �����mapping����Ѿ�����ã�����Ⱦɫ���Ѿ��ֿ��á�
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
 
	 

	 /** ���ڽ���ı�׼������ */
	 int NormalType = NORMALIZATION_ALL_READS;
	 boolean uniqReads = false;
	 int startCod = -1;
	 boolean booUniqueMapping = true;
	 /** ��ѡȡĳ�������reads */
	 Boolean FilteredStrand = null;
	 Species species;
	 
	 long readsSize = 0;
	 
	 public void setSpecies(Species species) {
		mapChrID2Len = species.getMapChromInfo();
	}
	
	 /** �ܹ��ж���reads������mapping�������ReadMapFile���ܵõ��� */
	public long getAllReadsNum() {
		long allReadsNum = 0;
		for (ChrMapReadsInfo chrMapReadsInfo : mapChrID2ReadsInfo.values()) {
			allReadsNum = allReadsNum + chrMapReadsInfo.getReadsChrNum();
		}
		return allReadsNum;
	}
	/**
	 * @param uniqReads ��reads mapping��ͬһ��λ��ʱ���Ƿ������һ��reads
	 * @param startCod ����㿪ʼ��ȡ��reads�ļ���bp�������õ� С��0��ʾȫ����ȡ ����reads���ȵ�����Ըò���
	 * @param colUnique  Unique��reads����һ�� novelbio�ı���ڵ����У���1��ʼ����
	 * @param booUniqueMapping �ظ���reads�Ƿ�ֻѡ��һ��
	 * @param cis5to3 �Ƿ��ѡȡĳһ�����reads��null������
	 */
	public void setFilter(boolean uniqReads, int startCod, boolean booUniqueMapping, Boolean FilteredStrand) {
		this.uniqReads = uniqReads;
		this.startCod = startCod;
		this.booUniqueMapping = booUniqueMapping;
		this.FilteredStrand = FilteredStrand;
	}
	/**
	 * ������õ���ʵ��ĳ��Ⱦɫ����������reads��Ŀ
	 */
	public long getChrReadsNum(String chrID) {
		return mapChrID2ReadsInfo.get(chrID.toLowerCase()).getReadsChrNum();
	}
	/**
	 * ������õ���ʵ��ĳ��Ⱦɫ��ĸ߶��ܺ�
	 */
	public long getChrReadsPipNum(String chrID) {
		return mapChrID2ReadsInfo.get(chrID.toLowerCase()).getReadsPipNum();
	}
	/**
	 * ������õ���ʵ��ĳ��Ⱦɫ��߶ȵ�ƽ��ֵ
	 */
	public double getChrReadsPipMean(String chrID) {
		ChrMapReadsInfo chrMapReadsInfo = mapChrID2ReadsInfo.get(chrID.toLowerCase());
		return chrMapReadsInfo.getReadsPipNum()/chrMapReadsInfo.chrLength;
	}
	 /**
	  * �趨��׼��������������ʱ�趨����һ��Ҫ�ڶ�ȡ�ļ�ǰ
	  * Ĭ����NORMALIZATION_ALL_READS
	  * @param normalType
	  */
	 public void setNormalType(int normalType) {
		NormalType = normalType;
	}
	 
	private void setChrLenFromReadBed() {
		if (mapChrID2Len.size() > 0)
			return;
		
		String chrID = ""; BedRecord lastBedRecord = null;
		for (BedRecord bedRecord : bedSeq.readlines()) {
			if (!bedRecord.getRefID().equals(chrID)) {
				if (lastBedRecord != null) {
					mapChrID2Len.put(chrID.toLowerCase(), (long)lastBedRecord.getEndAbs());
				}
				chrID = bedRecord.getRefID();
			}
			lastBedRecord = bedRecord;
		}
		mapChrID2Len.put(lastBedRecord.getRefID().toLowerCase(), (long)lastBedRecord.getEndAbs());
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
	 * @throws Exception
	 */
	protected void ReadMapFileExp() throws Exception {
		setChrLenFromReadBed();
		BedRecord bedRecordFirst = bedSeq.readFirstLine();
		if (startCod > 0 && bedRecordFirst.isCis5to3() == null) {
			logger.error("�����趨startCod����Ϊû���趨������");
			return;
		}
		
		int[] chrBpReads = null;//����ÿ��bp��reads�ۼ���
		String lastChr="";
		boolean flag = true;// ��û�и�Ⱦɫ��ʱ���Ϊfalse�����������и�Ⱦɫ���ϵ�����
		ChrMapReadsInfo chrMapReadsInfo = null;
		int[] tmpOld = new int[2];//���� tmpOld
		
		int readsNum = 0;
		
		for (BedRecord bedRecord : bedSeq.readlines()) {
			readsNum++;
			
			String tmpChrID = bedRecord.getRefID().toLowerCase();
			if (!tmpChrID.equals(lastChr)) {
				tmpOld = new int[2];//���� tmpOld
				
				if (!lastChr.equals("") && flag) { // ǰ���Ѿ�����һ��chrBpReads����ô��ʼ�ܽ����chrBpReads
					chrMapReadsInfo.sumChrBp(chrBpReads);
				}
				lastChr = tmpChrID;// ʵ�������³��ֵ�ChrID
				logger.info(lastChr);
				
				Long chrLength = mapChrID2Len.get(lastChr.toLowerCase());
				flag = true;
				if (chrLength == null) {
					logger.error("����δ֪chrID "+lastChr);
					flag = false; continue;
				}

				chrBpReads = new int[(int) (chrLength + 1)];// ͬ��Ϊ���㣬0λ��¼�ܳ��ȡ�����ʵ��bp����ʵ�ʳ���
				chrBpReads[0] = chrLength.intValue();
				chrMapReadsInfo = new ChrMapReadsInfo(lastChr, getChrLen(lastChr), invNum, summeryType, FormulatToCorrectReads);
				mapChrID2ReadsInfo.put(lastChr, chrMapReadsInfo);
			}
			if (flag == false) //û�и�Ⱦɫ��������
				continue;
			tmpOld = addLoc(bedRecord, uniqReads, tmpOld, startCod, FilteredStrand, chrBpReads,chrMapReadsInfo);
			
			suspendCheck();
			if (flagStop) {
				break;
			}
			readsSize = readsSize + bedRecord.getRawStringInfo().getBytes().length;
			if (readsNum%1000 == 0) {
				MapReadsProcessInfo mapReadsProcessInfo = new MapReadsProcessInfo(readsSize);
				setRunInfo(mapReadsProcessInfo);
			}
		}
		if (flag) {
			chrMapReadsInfo.sumChrBp(chrBpReads);
		}		
		return;
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
	protected int[] addLoc(BedRecord bedRecord,boolean uniqReads,int[] tmpOld,int startCod, Boolean cis5to3, int[] chrBpReads, ChrMapReadsInfo chrMapReadsInfo) {
		boolean cis5to3This = bedRecord.isCis5to3();
		if ((cis5to3 != null && bedRecord.isCis5to3() != cis5to3)
				|| (booUniqueMapping && bedRecord.getMappingNum() > 1)
				) {
			return tmpOld;
		}
		
		int[] tmpStartEnd = new int[2];
		tmpStartEnd[0] = bedRecord.getStartAbs();
		tmpStartEnd[1] = bedRecord.getEndAbs();

		//�����reads����һ��reads��ͬ������Ϊ����������������
		if (uniqReads && tmpStartEnd[0] == tmpOld[0] && tmpStartEnd[1] == tmpOld[1] ) {
			return tmpOld;
		}

		ArrayList<? extends Alignment> lsadd = null;
		//���û�пɱ����
		lsadd = bedRecord.getAlignmentBlocks();
		lsadd = setStartCod(lsadd, startCod, cis5to3This);

		addChrLoc(chrBpReads, lsadd);
		chrMapReadsInfo.readsAllNum = chrMapReadsInfo.readsAllNum + 1;
		return tmpStartEnd;
	}
	/**
	 * �����������ȡ��Ӧ��������󷵻���Ҫ�ۼӵ�ArrayList<int[]>
	 * @param lsStartEnd
	 * @param cis5to3
	 * @return ���cis5to3 = True����ô���Ž�ȡstartCod���ȵ�����
	 * ���cis5to3 = False����ô���Ž�ȡstartCod���ȵ�����
	 */
	private ArrayList<? extends Alignment> setStartCod(ArrayList<? extends Alignment> lsStartEnd, int StartCodLen, boolean cis5to3) {
		if (StartCodLen <= 0) {
			return lsStartEnd;
		}
		ArrayList<Align> lsResult = new ArrayList<Align>();
		
		if (cis5to3) {
			for (int i = 0; i < lsStartEnd.size(); i++) {
				Alignment alignment = lsStartEnd.get(i);
				if (StartCodLen - lsStartEnd.get(i).Length() > 0) {
					Align align = new Align(alignment.getStartAbs(), alignment.getEndAbs());
					align.setCis5to3(alignment.isCis5to3());
					lsResult.add(align);
					StartCodLen = StartCodLen - alignment.Length();
				}
				else {
					Align lastAlign = new Align(alignment.getStartAbs(), alignment.getStartAbs() + StartCodLen - 1);
					lsResult.add(lastAlign);
					break;
				}
			}
		}
		else {
			for (int i = lsStartEnd.size() - 1; i >= 0; i--) {
				Alignment alignment = lsStartEnd.get(i);
				if (StartCodLen - alignment.Length() > 0) {
					Align align = new Align(alignment.getStartAbs(), alignment.getEndAbs());
					align.setCis5to3(alignment.isCis5to3());
					
					lsResult.add(0,align);
					StartCodLen = StartCodLen - alignment.Length();
				}
				else {
					Align align = new Align(alignment.getEndAbs() - StartCodLen + 1, alignment.getEndAbs());
					align.setCis5to3(alignment.isCis5to3());
					lsResult.add(0,align);
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
	private void addChrLoc(int[] chrLoc, ArrayList<? extends Alignment> lsAddLoc) {
		for (Alignment is : lsAddLoc) {
			for (int i = is.getStartAbs(); i <=is.getEndAbs(); i++) {
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
			return;
		}
		else if (NormalType == NORMALIZATION_ALL_READS) {
			for (int i = 0; i < doubleInfo.length; i++) {
				doubleInfo[i] = doubleInfo[i]*1000000/getAllReadsNum();
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


