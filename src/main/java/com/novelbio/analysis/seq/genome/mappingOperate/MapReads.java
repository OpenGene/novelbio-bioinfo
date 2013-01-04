package com.novelbio.analysis.seq.genome.mappingOperate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.AlignRecord;
import com.novelbio.analysis.seq.AlignSeq;
import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.ListDetailBin;
import com.novelbio.analysis.seq.genome.gffOperate.ListHashBin;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.analysis.seq.sam.AlignmentRecorder;
import com.novelbio.base.dataStructure.Equations;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.dataStructure.listOperate.ListAbs;
import com.novelbio.base.dataStructure.listOperate.ListCodAbs;
import com.novelbio.database.model.species.Species;
/**
 * �����mapping����Ѿ�����ã�����Ⱦɫ���Ѿ��ֿ��á�
 * �������ڴ����Ƶı�
 * 
 * @author zong0jie
 * 
 */
public class MapReads extends MapReadsAbs implements AlignmentRecorder {
	private static Logger logger = Logger.getLogger(MapReads.class);

	 boolean uniqReads = false;
	 int startCod = -1;

	 /** ��ѡȡĳ�������reads */
	 Boolean FilteredStrand = null;
	 Species species;
	 
	 long readsSize = 0;

	 AlignSeq alignSeqReader;
	 
	 HashMap<String, ChrMapReadsInfo> mapChrID2ReadsInfo = new HashMap<String, ChrMapReadsInfo>();
	 int tagLength = 300;//��ReadMapFile������ֵ
	 /** ����������������reads */
	 MapReadsAddAlignRecord mapReadsAddAlignRecord;

	 int summeryType = SUM_TYPE_MEAN;
	 
	 /**ÿ������λ����������趨Ϊ1�����㷨��仯��Ȼ���ܾ�ȷ*/
	 int invNum = 10;
	 /** ��Ϊ�����С��������double�Ƚ�ռ�ڴ棬���Ծͽ����ݳ���fold��Ȼ�����������ͺ� */
	 int fold = 1000;
	 /**���samBam���ļ����������Ϣ
	  * ע�������֮ǰҪ��ִ��{@link #prepareAlignRecord(AlignRecord)}
	  */
	 public void addAlignRecord(AlignRecord alignRecord) {
		 mapReadsAddAlignRecord.addAlignRecord(alignRecord);
	 }
	 /**
	  * ÿ������λȡ��,����趨Ϊ1�����㷨��仯��Ȼ���ܾ�ȷ
	  * @return
	  */
	 public int getBinNum() {
		 return invNum;
	 }
		
	 /**
	  * �����ĵ�������ȵ�һ��Ⱦɫ��ѹ��Ϊ�̵�ÿ��inv��Լ10-20bp�����У���ôѹ������ѡ��Ϊ20bp�е���ֵ����λ����ƽ����<br>
	  * SUM_TYPE_MEDIAN��SUM_TYPE_MEAN
	  */
	 public void setSummeryType(int summeryType) {
		this.summeryType = summeryType;
	}
	 
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

	 /** �ܹ��ж���reads������mapping�������ReadMapFile���ܵõ��� */
	public long getAllReadsNum() {
		if (allReadsNum > 0) {
			return allReadsNum;
		}
		for (ChrMapReadsInfo chrMapReadsInfo : mapChrID2ReadsInfo.values()) {
			allReadsNum = allReadsNum + chrMapReadsInfo.getReadsChrNum();
		}
		return allReadsNum;
	}
	
	/**
	 * @param uniqReads ��reads mapping��ͬһ��λ��ʱ���Ƿ������һ��reads Ĭ��false
	 * @param startCod ����㿪ʼ��ȡ��reads�ļ���bp�������õ� С��0��ʾȫ����ȡ ����reads���ȵ�����Ըò�����Ĭ��-1
	 * @param booUniqueMapping �ظ���reads�Ƿ�ֻѡ��һ�� Ĭ��Ϊtrue
	 * @param FilteredStrand �Ƿ��ѡȡĳһ�����reads��null������ Ĭ��Ϊnull
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
	 * �趨˫��readsTagƴ�����󳤶ȵĹ���ֵ��Ŀǰsolexa˫���������ȴ����300bp������̫��ȷ
	 * Ĭ��300
	 * ����Ƿ�����getReadsDensity����reads�ܶȵĶ���
	 * @param readsTagLength
	 */
	public  void setTagLength(int thisTagLength) {
		tagLength=thisTagLength;
	}
	
	protected boolean isUniqueMapping() {
		return booUniqueMapping;
	}
	 
	private void setChrLenFromReadBed() {
		if (mapChrID2Len.size() > 0)
			return;
		
		String chrID = ""; AlignRecord lastAlignRecord = null;
		for (AlignRecord alignRecord : alignSeqReader.readLines()) {
			if (!alignRecord.getRefID().equals(chrID)) {
				if (lastAlignRecord != null) {
					mapChrID2Len.put(chrID.toLowerCase(), (long)lastAlignRecord.getEndAbs());
				}
				chrID = alignRecord.getRefID();
			}
			lastAlignRecord = alignRecord;
		}
		mapChrID2Len.put(lastAlignRecord.getRefID().toLowerCase(), (long)lastAlignRecord.getEndAbs());
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
	 * ������׼������equations����
	 * ����Ⱦɫ�壬�������յ㣬���ظ�Ⱦɫ����tag���ܶȷֲ��������Ⱦɫ����mappingʱ�򲻴��ڣ��򷵻�null
	 * @param chrID Сд
	 * @param startLoc ������꣬Ϊʵ����� ���startNum<=0 ����endNum<=0���򷵻�ȫ����Ϣ
	 * @param endLoc 
	 * @param binNum ���ָ�Ŀ���
	 * @return
	 */
	public  double[] getReadsDensity(String chrID, int startLoc, int endLoc, int binNum ) {
		int tagBinLength=(int)(tagLength*1.5);
		double[] tmpReadsNum = getRangeInfo(tagBinLength, chrID, startLoc, endLoc,1);
		if (tmpReadsNum==null) {
			return null;
		}
		double[] resultTagDensityNum=MathComput.mySpline(tmpReadsNum, binNum, 0, 0, 2);
		return resultTagDensityNum;
	}
	
	/**
	 * ������Ҫ���������װ��ArrayList-ExonInfo���棬���ؽ���������Щ����Ļ�����ֲ��ܶ�ͼ<br>
	 * ������Ǳߵ�����ǿ����ġ�������뿴ȫ��������tss����ļ׻����ֲ������exon����ļ׻����ֲ������<br>
	 * ����˼·����һ�����ȵ�slide window����������Ȼ�󿴸�λ�����м׻����Ļ���ı�������<br>
	 * ��ô�ҵ��������ǳ���tss������������ļ׻���ȫ���趨Ϊ0��Ҳ���ǽ�����ָ��lsExonInfos�ڵļ׻�����Ȼ������߳��沽��<br>
	 * @param lsExonInfos
	 * @param chrID
	 * @param startLoc
	 * @param endLoc
	 * @param binNum
	 * @return
	 */
	public  double[] getReadsDensity(ListAbs<ExonInfo> lsExonInfos, String chrID, int startLoc, int endLoc, int binNum ) {
		//���Ƚ�reads��׼��Ϊһ��400-500bp��Ĵ�飬ÿһ������Ӧ���Ǹ���������tags���������������������������ֵ
		//Ȼ�����ڴ������ͳ�ƣ�
		//��Ź�����һ�£������Ͽ����һ��tag��1.5����ʱ�������ȽϺ���
		int tagBinLength=(int)(tagLength*1.5);
		double[] tmpReadsNum = getRangeInfo(tagBinLength, chrID, startLoc, endLoc,1);
		if (tmpReadsNum==null) {
			return null;
		}
		double[] resultTagDensityNum=MathComput.mySpline(tmpReadsNum, binNum, 0, 0, 2);
		return resultTagDensityNum;
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
	public double[] getRangeInfo(int thisInvNum,String chrID,int startNum,int endNum,int type) {
		double[] result = null;
		if (!mapChrID2ReadsInfo.containsKey(chrID.toLowerCase())) {
			logger.error("û�и�Ⱦɫ�壺" + chrID);
			return result;
		}
		////////////////////////����Ҫ�ָ���////////////////////////////////////////
		if (thisInvNum <= 0) {
			thisInvNum = invNum;
		}
		if (invNum == 1 && thisInvNum == 1) {
			result = getRangeInfoInv1(chrID, startNum, endNum);
		} else {
			result = getRangeInfoNorm(chrID, thisInvNum, startNum, endNum, type);
		}
		return result;
	}
	/**
	 * ���Ϊ1�ľ�ȷ�汾��������׼������equations����
	 * @param chrID Ⱦɫ��ID
	 * @param startNum ʵ����㣬��1��ʼ����
	 * @param endNum ʵ���յ㣬��1��ʼ����
	 */
	private double[] getRangeInfoInv1(String chrID, int startNum, int endNum) {
		ChrMapReadsInfo chrMapReadsInfo = mapChrID2ReadsInfo.get(chrID.toLowerCase());
		if (chrMapReadsInfo == null) {
			logger.info("û�и�Ⱦɫ�壺 " + chrID);
			return null;
		}
		int[] startEnd = correctStartEnd(mapChrID2Len, chrID, startNum, endNum);
		if (startEnd == null) {
			return null;
		}
		startEnd[0] = startEnd[0] - 1;
		startEnd[1] = startEnd[1] - 1;
		double[] result = new double[startEnd[1] - startEnd[0] + 1];
		
		int[] invNumReads = chrMapReadsInfo.getSumChrBpReads();
		if (mapChrID2LsAlignmentFilter != null && mapChrID2LsAlignmentFilter.containsKey(chrID.toLowerCase())) {
			List<? extends Alignment> lsAlignments = mapChrID2LsAlignmentFilter.get(chrID.toLowerCase());
			invNumReads = cleanInfoNotInAlignment(lsAlignments, invNumReads, 1);
		}
		int k = 0;
		for (int i = startEnd[0]; i <= startEnd[1]; i++) {
			result[k] = invNumReads[i];
			k++;
		}
		//��׼��
		normDouble(NormalType, result, getAllReadsNum());
		result = equationsCorrect(result);
		return result;
	}
	/** ����İ汾��������׼������equations����
	 * @param lsAlignments �Ƿ������lsAlignments��Χ�ڵ���Ϣ
	 * @param chrID Ⱦɫ��ID
	 * @param thisInvNum ÿ��������������bp�������ڵ���invNum�������invNum�ı���
	 * @param startNum
	 * @param endNum
	 * @param type 0����Ȩƽ�� 1��ȡ���ֵ��2����Ȩ����ƽ��--Ҳ���ǼӺ�
	 *  */
	private double[] getRangeInfoNorm(String chrID, int thisInvNum, int startNum, int endNum, int type) {
		int[] startEndLoc = correctStartEnd(mapChrID2Len, chrID, startNum, endNum);
		if (startEndLoc == null) {
			return null;
		}
		double binNum = (double)(startEndLoc[1] - startEndLoc[0] + 1) / thisInvNum;
		int binNumFinal = 0;
		if (binNum - (int)binNum >= 0.7) {
			binNumFinal = (int)binNum + 1;
		} else {
			binNumFinal = (int)binNum;
		}
		if (binNumFinal == 0) {
			binNumFinal = 1;
		}
		//�ڲ�������׼����
		double[] tmp = getRangeInfo(chrID, startEndLoc[0], startEndLoc[1], binNumFinal, type);
		return tmp;
	}
	/**
	 * ������׼������equations����
	 * �����������䣬��Ҫ���ֵĿ��������ظö�������reads�����顣�����Ⱦɫ����mappingʱ�򲻴��ڣ��򷵻�null
	 * ��λ�������˵����ڵ� ��ȡinvNum���䣬Ȼ������µ�invNum����
	 * @param lsAlignments ��������ָ�������ڵ���ֵȫ����գ������linkedlist
	 * @param chrID һ��ҪСд
	 * @param startNum ������꣬Ϊʵ����� ���startNum<=0 ����endNum<=0���򷵻�ȫ����Ϣ
	 * @param endNum �յ����꣬Ϊʵ���յ�
	 * @param binNum ���ָ��������Ŀ
	 * @param type 0����Ȩƽ�� 1��ȡ���ֵ��2����Ȩ����ƽ��--Ҳ���ǼӺ�
	 * @return ���û���ҵ���Ⱦɫ��λ�㣬�򷵻�null
	 * @return
	 */
	protected double[] getRangeInfo(String chrID, int startNum, int endNum, int binNum, int type) {
		if (!mapChrID2ReadsInfo.containsKey(chrID.toLowerCase())) {
			logger.error("û�и�Ⱦɫ�壺" + chrID);
			return null;
		}
		int[] startEnd = correctStartEnd(mapChrID2Len, chrID, startNum, endNum);
		if (startEnd == null) {
			return null;
		}
		ChrMapReadsInfo chrMapReadsInfo = mapChrID2ReadsInfo.get(chrID.toLowerCase());
		int[] invNumReads = chrMapReadsInfo.getSumChrBpReads();
		if (invNumReads == null) {
			return null;
		}
		if (mapChrID2LsAlignmentFilter != null && mapChrID2LsAlignmentFilter.containsKey(chrID.toLowerCase())) {
			List<? extends Alignment> lsAlignments = mapChrID2LsAlignmentFilter.get(chrID.toLowerCase());
			invNumReads = cleanInfoNotInAlignment(lsAlignments, invNumReads, invNum);
		}
		
		try {
			return getRengeInfoExp(invNumReads, startEnd[0], startEnd[1], binNum, type);
		} catch (Exception e) {
			return null;
		}
	}
	
	//TODO check
	/**
	 * ����list���Σ���ȫ���������Ϣ����û�б�list���θ��ǵ�����Ϣȫ��ɾ��
	 * @param lsAlignments �����alignment��ʵ����Ŀ
	 * @param invNumReads ��0��ʼ������ÿ����Ԫ��ʾһ��invNum�����Լ�����ʱ��Ҫ����1
	 * @param binNum
	 * @return
	 */
	private static int[] cleanInfoNotInAlignment(List<? extends Alignment> lsAlignments, int[] invNumReads, int invNum) {
		Queue<Alignment> lsAlignmentThis = new LinkedList<Alignment>();
		for (Alignment alignment : lsAlignments) {
			lsAlignmentThis.add(alignment);
		}
		int[] result = new int[invNumReads.length];
		int i = 0;
		Alignment alignment = lsAlignmentThis.poll();
		while (!lsAlignmentThis.isEmpty() && i < invNumReads.length) {
			if((i+1) * invNum < alignment.getStartAbs()) {
				i++;
			} else if ((i+1) * invNum > alignment.getEndAbs()) {
				alignment = lsAlignmentThis.poll();
			} else {
				result[i] = invNumReads[i];
				i++;
			}
		}
		return result;
	}

	/**
	 * @param invNumReads ĳ��Ⱦɫ�������reads�ѵ����
	 * @param startNum ʵ��num
	 * @param endNum ʵ��num
	 * @param binNum
	 * @param type
	 * @return
	 */
	protected double[] getRengeInfoExp(int[] invNumReads, int startNum,int endNum,int binNum,int type) {
		startNum--; endNum--;
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
		normDouble(NormalType, tmpRegReads, getAllReadsNum());
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
	 * ������Ϊmacs��bed�ļ�ʱ���Զ�<b>����chrm��Ŀ</b><br>
	 * ����chr��Ŀ��Сд
	 * ��ȡMapping�ļ���������Ӧ��һά�������飬��󱣴���һ����ϣ���С�ע�⣬mapping�ļ��е�chrID��chrLengthFile�е�chrIDҪһ�£���������
	 * @throws Exception
	 */
	protected void ReadMapFileExp() throws Exception {
		allReadsNum = 0;
		setChrLenFromReadBed();
		AlignRecord alignRecordFirst = alignSeqReader.readFirstLine();
		if (!prepareAlignRecord(alignRecordFirst)) {
			return;
		}
		
		for (AlignRecord alignRecord : alignSeqReader.readLines()) {
			allReadsNum++;
			mapReadsAddAlignRecord.addAlignRecord(alignRecord);
			
			suspendCheck();
			if (flagStop) {
				break;
			}
			readsSize = readsSize + alignRecord.getRawStringInfo().getBytes().length;
			if (allReadsNum%1000 == 0) {
				MapReadsProcessInfo mapReadsProcessInfo = new MapReadsProcessInfo(readsSize);
				setRunInfo(mapReadsProcessInfo);
			}
		}
		mapReadsAddAlignRecord.summary();
	}
	
	/**
	 * ׼�����reads��Ϣ����Ҫ�ǳ�ʼ��mapReadsAddAlignRecord
	 * ��������ж�һ��startCod�Ƿ�����
	 * @param alignRecordFirst
	 * @return �����setFilter�ж��� startCod > 0 ����readsû�з���
	 * �򷵻�false
	 */
	public boolean prepareAlignRecord(AlignRecord alignRecordFirst) {
		mapReadsAddAlignRecord = new MapReadsAddAlignRecord(this, fold);
		if (startCod > 0 && alignRecordFirst.isCis5to3() == null) {
			logger.error("�����趨startCod����Ϊû���趨������");
			return false;
		}
		return true;
	}
	@Override
	public void summary() {
		mapReadsAddAlignRecord.summary();
	}

}

/**
 * ��������þ��ǽ�reads����Ϣװ��MapReads��mapChrID2ReadsInfo��ȥ��
 * �����ģ������������Է���һ�ζ�ȡbam�ļ���Ȼ�����ö�����
 * @author zong0jie
 *
 */
class MapReadsAddAlignRecord {
	private static final Logger logger = Logger.getLogger(MapReadsAddAlignRecord.class);
	MapReads mapReads;
	int[] chrBpReads = null;//����ÿ��bp��reads�ۼ���
	String lastChr="";
	boolean flag = true;// ��û�и�Ⱦɫ��ʱ���Ϊfalse�����������и�Ⱦɫ���ϵ�����
	ChrMapReadsInfo chrMapReadsInfo = null;
	int[] tmpOld = new int[2];//���� tmpOld
	int fold;
	public MapReadsAddAlignRecord(MapReads mapReads, int fold) {
		this.mapReads = mapReads;
		this.fold = fold;
	}
	
	public void addAlignRecord(AlignRecord alignRecord) {
		String tmpChrID = alignRecord.getRefID().toLowerCase();
		if (!tmpChrID.equals(lastChr)) {
			tmpOld = new int[2];//���� tmpOld
			summary();
			lastChr = tmpChrID;// ʵ�������³��ֵ�ChrID
			logger.error(lastChr);
			
			Long chrLength = mapReads.mapChrID2Len.get(lastChr.toLowerCase());
			flag = true;
			if (chrLength == null) {
				logger.error("����δ֪chrID "+lastChr);
				flag = false; return;
			}

			chrBpReads = new int[(int) (chrLength + 1)];// ͬ��Ϊ���㣬0λ��¼�ܳ��ȡ�����ʵ��bp����ʵ�ʳ���
			chrBpReads[0] = chrLength.intValue();
			chrMapReadsInfo = new ChrMapReadsInfo(lastChr, mapReads);
			mapReads.mapChrID2ReadsInfo.put(lastChr, chrMapReadsInfo);
		}
		//û�и�Ⱦɫ��������
		if (flag == false) return;
		tmpOld = addLoc(alignRecord, tmpOld, chrBpReads, chrMapReadsInfo);
		chrMapReadsInfo.addReadsAllNum(1);
	}
	
	public void summary() {
		if (!lastChr.equals("") && flag) {
			chrMapReadsInfo.sumChrBp(chrBpReads, fold);
			chrBpReads = null;
		}
	}
	/**
	 * ����Ӻ͵Ĵ�����
	 * ����һ����Ϣ�����������ݼӵ���Ӧ��������
	 * @param alignRecord reads��Ϣ
	 * @param tmpOld ��һ�������յ㣬�����ж��Ƿ�����ͬһλ�����
	 * @param chrBpReads ������Ҫ���ӵ�Ⱦɫ����Ϣ
	 * @param chrMapReadsInfo ��¼�ܹ�mapping��reads������Ϊ���ܹ�������ȥ���������鷽ʽ
	 * @return
	 * ��λ�����Ϣ��������һ���ж��Ƿ���ͬһλ��
	 */
	protected int[] addLoc(AlignRecord alignRecord, int[] tmpOld, int[] chrBpReads, ChrMapReadsInfo chrMapReadsInfo) {
		boolean cis5to3This = alignRecord.isCis5to3();
		if ((mapReads.FilteredStrand != null && alignRecord.isCis5to3() != mapReads.FilteredStrand)
				|| (mapReads.isUniqueMapping() && alignRecord.getMappingNum() > 1)
				) {
			return tmpOld;
		}
		
		int[] tmpStartEnd = new int[2];
		tmpStartEnd[0] = alignRecord.getStartAbs();
		tmpStartEnd[1] = alignRecord.getEndAbs();

		//�����reads����һ��reads��ͬ������Ϊ����������������
		if (mapReads.uniqReads && tmpStartEnd[0] == tmpOld[0] && tmpStartEnd[1] == tmpOld[1] ) {
			return tmpOld;
		}
		
		ArrayList<? extends Alignment> lsadd = null;
		//���û�пɱ����
		lsadd = alignRecord.getAlignmentBlocks();
		lsadd = setStartCod(lsadd, mapReads.startCod, cis5to3This);
		int addNum = (int) ((double)1*fold/alignRecord.getMappingNum());
		addChrLoc(chrBpReads, lsadd, addNum);
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
					Align align = new Align(alignment.getRefID(), alignment.getStartAbs(), alignment.getEndAbs());
					align.setCis5to3(alignment.isCis5to3());
					lsResult.add(align);
					StartCodLen = StartCodLen - alignment.Length();
				}
				else {
					Align lastAlign = new Align(alignment.getRefID(), alignment.getStartAbs(), alignment.getStartAbs() + StartCodLen - 1);
					lsResult.add(lastAlign);
					break;
				}
			}
		}
		else {
			for (int i = lsStartEnd.size() - 1; i >= 0; i--) {
				Alignment alignment = lsStartEnd.get(i);
				if (StartCodLen - alignment.Length() > 0) {
					Align align = new Align(alignment.getRefID(), alignment.getStartAbs(), alignment.getEndAbs());
					align.setCis5to3(alignment.isCis5to3());
					
					lsResult.add(0,align);
					StartCodLen = StartCodLen - alignment.Length();
				}
				else {
					Align align = new Align(alignment.getRefID(), alignment.getEndAbs() - StartCodLen + 1, alignment.getEndAbs());
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
	private void addChrLoc(int[] chrLoc, ArrayList<? extends Alignment> lsAddLoc, int addNum) {
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
				chrLoc[i] = chrLoc[i] + addNum;
			}
		}
	}
}


/**
 * ����Ⱦɫ����Ϣ
 * @author zong0jie
 */
class ChrMapReadsInfo {
	String chrID;
	int invNum = 10;
	int type;
	long chrLength;
	
	/** ֱ�Ӵ�0��ʼ��¼��1����ڶ���invNum,Ҳ��ʵ����ͬ */
	int[] SumChrBpReads;
	/** ����Ⱦɫ���ϵ�reads���� */
	long readsAllNum;
	/** ����Ⱦɫ���ϵ�reads�Ķѵ���֮�� */
	long readsAllPipNum;
	/** ����У������ */
	Equations FormulatToCorrectReads; 
	
	/**
	 * @param chrID
	 * @param mapReadsAbs
	 */
	public ChrMapReadsInfo(String chrID, MapReads mapReads) {
		this.chrID = chrID;
		this.chrLength = mapReads.getChrLen(chrID);
		this.invNum = mapReads.invNum;
		this.type = mapReads.summeryType;
		this.FormulatToCorrectReads = mapReads.FormulatToCorrectReads;
	}
	
	public String getChrID() {
		return chrID;
	}
	public long getReadsChrNum() {
		return readsAllNum;
	}
	public void addReadsAllNum(long readsAllNum) {
		this.readsAllNum = this.readsAllNum + readsAllNum;
	}
	public long getReadsPipNum() {
		return readsAllPipNum;
	}
	/**
	 * ֱ�Ӵ�0��ʼ��¼��1����ڶ���invNum,Ҳ��ʵ����ͬ
	 * @return
	 */
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
	protected void sumChrBp(int[] chrBpReads, int fold) {
		// //////////SumChrBpReads�趨//////////////////////////////////
		// ������Ǻܾ�ȷ�����һλ���ܲ�׼������ʵ��Ӧ��������ν��,Ϊ���㣬0λ��¼�ܳ��ȡ�����ʵ��bp����ʵ�ʳ���
		int SumLength = chrBpReads.length / invNum + 1;// ��֤���������������Ҫ��SumChrBpReads��һ��
		SumChrBpReads = new int[SumLength];// ֱ�Ӵ�0��ʼ��¼��1����ڶ���invNum,Ҳ��ʵ����ͬ
		
		if (invNum == 1) {
			for (int i = 0; i < SumLength - 2; i++) {
				SumChrBpReads[i] = chrBpReads[i+1]/fold;
				readsAllPipNum = readsAllPipNum + chrBpReads[i+1]/fold;
			}
			return;
		 }
		 for (int i = 0; i < SumLength - 2; i++) {
			 int[] tmpSumReads=new int[invNum];//���ܵ�chrBpReads���ÿһ����ȡ����
			 int sumStart=i*invNum + 1; int k=0;//k������tmpSumReads���±꣬ʵ���±���У�����-1
			 for (int j = sumStart; j < sumStart + invNum; j++) {
				 int thisNum = chrBpReads[j]/fold;
				 tmpSumReads[k] = thisNum;
				 readsAllPipNum = readsAllPipNum + thisNum;
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
