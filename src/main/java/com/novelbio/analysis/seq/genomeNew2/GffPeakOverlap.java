package com.novelbio.analysis.seq.genomeNew2;

import java.util.ArrayList;
import java.util.Hashtable;

import com.novelbio.analysis.seq.genome.gffOperate.GffCodInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetail;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashPeak;
import com.novelbio.analysis.seq.genome.gffOperate.GffsearchPeak;

 

public class GffPeakOverlap 
{
	/**
	 * ����������
	 */
	GffHashPeak gffHashPeakMinus=new GffHashPeak();
	GffHashPeak gffHashPeakPlus=new GffHashPeak();
	
	GffsearchPeak gffsearchPeak=new GffsearchPeak();
	ArrayList<String> lspeakMinusID;//�����ϵ�peakID
	ArrayList<String> lspeakPlusID;//�����ϵ�peakID
	/**
	 * ���ظ�peak�ϲ������ϵ�Peak
	 */
	ArrayList<String> lspeakMinusCope;
	/**
	 * ���ظ�peak�ϲ��������ϵ�Peak
	 */
	ArrayList<String> lspeakPlusCope;
	
	/**
	 * �����ϵ�PeakHash
	 */
	Hashtable<String, GffDetail> gffMinusLocHash;
	/**
	 * �����ϵ�PeakHash
	 */
	Hashtable<String, GffDetail> gffPlusLocHash;
	/**
	 * ��֮ǰ����peak�ļ���Ҫ����chr�������򣬲���chr�ڲ���������Ҳ����chr�ڲ����겻Ҫ��һ��
	 * ��ȡ����peak�ļ������������ļ�����Ĭ�������ļ���peak��Ϣ���ڵ���һ��<br>
	 * ���м�������1��ʼ����
	 * @param filePlus ����peak txt�ļ�
	 * @param fileMinus ����peak txt�ļ�
	 * @param colChr ChrID���ڵ���
	 * @param peakStart peak������ڵ���
	 * @param peakEnd peak�յ����ڵ���
	 * @param rowNum �ӵڼ��ж���
	 * @throws Exception 
	 */
	public void readPeakFile(String filePlus,String fileMinus,int colChr,int peakStart,int peakEnd,int rowNum) throws Exception 
	{
		gffHashPeakMinus.ReadGffarray(fileMinus, true, colChr, peakStart, peakEnd, rowNum);
		gffHashPeakPlus.ReadGffarray(filePlus, true, colChr, peakStart, peakEnd, rowNum);
		
		lspeakMinusID=gffHashPeakMinus.getLOCIDList();
		lspeakPlusID=gffHashPeakPlus.getLOCIDList();
		
		lspeakMinusCope=gffHashPeakMinus.getLOCChrHashIDList();
		lspeakPlusCope=gffHashPeakPlus.getLOCChrHashIDList();
		
		
		gffMinusLocHash=gffHashPeakMinus.getLocHashtable();
		gffPlusLocHash=gffHashPeakPlus.getLocHashtable();
		
	}
	
	/**
	 * ���Plus����peak�ĳ���
	 * @return
	 */
	public int getPlusallLength() 
	{
		int plusAllLength=0;
		int plusNum=lspeakPlusID.size();
		
		for (int i = 0; i < plusNum; i++) 
		{
			GffDetail tmpPlusDetail=gffPlusLocHash.get(lspeakPlusID.get(i));
			int tmplength=tmpPlusDetail.numberend-tmpPlusDetail.numberstart;
			plusAllLength=plusAllLength+tmplength;
		}
		return plusAllLength;
	}
	/**
	 * ���Plus�ϲ�������peak�ĸ���
	 * @return
	 */
	public int getPlusNum() 
	{
		return lspeakPlusCope.size();
	}
	
	/**
	 * ���Minus�ϲ�������peak�ĸ���
	 * @return
	 */
	public int getMinusNum() 
	{
		return lspeakMinusCope.size();
	}
	
	/**
	 * ���Minus����peak�ĳ���
	 * @return
	 */
	public int getMinusallLength() 
	{
		int minusAllLength=0;
		int minusNum=lspeakMinusID.size();
		
		for (int i = 0; i < minusNum; i++) 
		{
			GffDetail tmpMinusDetail=gffMinusLocHash.get(lspeakMinusID.get(i));
			int tmplength=tmpMinusDetail.numberend-tmpMinusDetail.numberstart;
			minusAllLength=minusAllLength+tmplength;
		}
		return minusAllLength;
	}

	/**
	 * ���Overlap�ĳ���
	 * ����minus�������յ���plus����
	 * @return int[2]
	 * 0: ����������֮�佻��bp<br>
	 * 1: ��������peak��<br>
	 * 2: ����������֮�佻��bp<br>
	 * 3: ��������peak��
	 */
	public int[] getOverlapInfo() {
		int overlapNumM2P = 0;
		int overlapLengthM2P=0;
		ArrayList<String[]> lsOverlap=new ArrayList<String[]>();
		lsOverlap=compareMinus2Plus(true);//��peakOverlap�����ǽ�ȥ���㽻��
		for (int i = 0; i < lsOverlap.size(); i++) {
			int tmpOverlap = Integer.parseInt(lsOverlap.get(i)[2]);
			overlapLengthM2P=overlapLengthM2P+tmpOverlap;
			if (tmpOverlap > 0) {
				overlapNumM2P++;
			}
		}
		
		int overlapNumP2M = 0;
		int overlapLengthP2M =0;
		ArrayList<String[]> lsOverlap2=new ArrayList<String[]>();
		lsOverlap2=comparePlus2Minus(true);//��peakOverlap�����ǽ�ȥ���㽻��
		for (int i = 0; i < lsOverlap2.size(); i++) {
			int tmpOverlap = Integer.parseInt(lsOverlap2.get(i)[2]);
			overlapLengthP2M = overlapLengthP2M+tmpOverlap;
			if (tmpOverlap > 0) {
				overlapNumP2M++;
			}
		}
		
		int[] result = new int[4];
		result[0] = overlapLengthM2P;
		result[1] = overlapNumM2P;
		result[2] = overlapLengthP2M;
		result[3] = overlapNumP2M;
		return result;
	}
	/**
	 * ����minus�������յ���plus�в��ҵ�
	 * ����ArrayList-String[]���������α���ÿ��minus peak��������plus peak ��overlap���
	 * 0:ChrID <br>
	 * 1:PeakID <br>
	 * 2:overlapLength <br>
	 * @param peakOverlap trueʱ���ص�peak�ϲ��������������ϵ�peak����
	 *  false��A���ϵ�peak�������ص�����B�����Ѿ��Ͳ��ص���peak�����������ϵ�peak����
	 */
	public ArrayList<String[]> compareMinus2Plus(boolean peakOverlap) 
	{
		ArrayList<String[]> result=new ArrayList<String[]>();//�洢�����
		
		if (peakOverlap) {
			int peakMinusNum=lspeakMinusCope.size();
			System.out.println(peakMinusNum);
			for (int i = 0; i < peakMinusNum; i++) 
			{
				String tmpResult[ ] = new String[3];
				String tmpPeakID = lspeakMinusCope.get(i).split("/")[0];
				GffDetail gffMinusPeakDetial = gffMinusLocHash.get(tmpPeakID);
				String ChrID = gffMinusPeakDetial.ChrID;
				int overlapLength = twoSiteLocation(gffMinusPeakDetial,gffHashPeakPlus);
				tmpResult[0] = ChrID;
				tmpResult[1] = tmpPeakID;
				tmpResult[2] = overlapLength+"";
				result.add(tmpResult);
			}
		}
		else {
			int peakMinusNum = lspeakMinusID.size();
			System.out.println(peakMinusNum);
			for (int i = 0; i < peakMinusNum; i++) 
			{
				String tmpResult[] = new String[3];
				String tmpPeakID = lspeakMinusID.get(i);
				GffDetail gffMinusPeakDetial = gffMinusLocHash.get(tmpPeakID);
				String ChrID = gffMinusPeakDetial.ChrID;
				int overlapLength = twoSiteLocation(gffMinusPeakDetial,gffHashPeakPlus);
				tmpResult[0] = ChrID;
				tmpResult[1] = tmpPeakID;
				tmpResult[2] = overlapLength+"";
				result.add(tmpResult);
			}
		}
		return result;
	}
	
	
	/**
	 * ����plus�������յ���minus�в��ҵ�
	 * ����ArrayList-String[]���������α���ÿ��plus peak��������minus peak ��overlap���
	 * 0:ChrID <br>
	 * 1:PeakID <br>
	 * 2:overlapLength <br>
	 * @param peakOverlap trueʱ���ص�peak�ϲ��������������ϵ�peak����
	 *  false��A���ϵ�peak�������ص�����B�����Ѿ��Ͳ��ص���peak�����������ϵ�peak����
	 */
	public ArrayList<String[]> comparePlus2Minus(boolean peakOverlap) 
	{
		ArrayList<String[]> result=new ArrayList<String[]>();//�洢�����
		
		if (peakOverlap) {
			int peakPlusNum=lspeakPlusCope.size();
			System.out.println(peakPlusNum);
			for (int i = 0; i < peakPlusNum; i++) 
			{
				String tmpResult[]=new String[3];
				String tmpPeakID=lspeakPlusCope.get(i).split("/")[0];
				GffDetail gffPlusPeakDetial=gffPlusLocHash.get(tmpPeakID);
				String ChrID=gffPlusPeakDetial.ChrID;
				int overlapLength=twoSiteLocation(gffPlusPeakDetial,gffHashPeakMinus);
				tmpResult[0]=ChrID;
				tmpResult[1]=tmpPeakID;
				tmpResult[2]=overlapLength+"";
				result.add(tmpResult);
			}
		}
		else {
			int peakPlusNum=lspeakPlusID.size();
			System.out.println(peakPlusNum);
			for (int i = 0; i < peakPlusNum; i++) 
			{
				String tmpResult[]=new String[3];
				String tmpPeakID=lspeakPlusID.get(i);
				GffDetail gffPlusPeakDetial=gffPlusLocHash.get(tmpPeakID);
				String ChrID=gffPlusPeakDetial.ChrID;
				int overlapLength=twoSiteLocation(gffPlusPeakDetial,gffHashPeakMinus);
				tmpResult[0]=ChrID;
				tmpResult[1]=tmpPeakID;
				tmpResult[2]=overlapLength+"";
				result.add(tmpResult);
			}
		}
		return result;
	}
	
	
	/**
	 * ����peak OverLap�����������Integer<br>
	 * ΪgffMPeakDetial����gffHashplusPeak���õĽ������<br>
	 * @param ChrID
	 * @param start
	 * @param end
	 */
	private int twoSiteLocation(GffDetail gffMPeakDetial,GffHashPeak gffHashplusPeak) 
	{
		String ChrID=gffMPeakDetial.ChrID;
		int start=gffMPeakDetial.numberstart;
		int end=gffMPeakDetial.numberend;
		
		ArrayList<Object> lsSearchReslut=gffsearchPeak.searchLocation(ChrID, start, end, gffHashplusPeak);
		Object[] gffstartCodInfor=(Object[])lsSearchReslut.get(0);
		double[] startOverlapLength=(double[])gffstartCodInfor[1];
		
		Object[] gffendCodInfor=(Object[])lsSearchReslut.get(1);
		double[] endOverlapLength=(double[])gffendCodInfor[1];
		
		if (startOverlapLength[1]==100) {
			return (int)startOverlapLength[2];
		}
		
		/**
		 * ��ǰ��ģ������������ǰ���Ч�����
		//����������һ��peak��
		if(gffstartCodDetail.insideLOC&&gffendCodDetail.insideLOC&&gffstartCodDetail.LOCID[0].equals(gffendCodDetail.LOCID[0]))
		{
			return gffMinusPeakDetial.numberend-gffMinusPeakDetial.numberstart;
		}
		//�����㲻����һ��peak��
		
		if (gffstartCodDetail.insideLOC) {
			tmpOverlapLength=tmpOverlapLength+gffstartCodDetail.distancetoLOCEnd[0];//��һ�����굽peak�յ�ľ���
		}
		if (gffendCodDetail.insideLOC) {
			tmpOverlapLength=tmpOverlapLength+gffendCodDetail.distancetoLOCStart[0];//�ڶ������굽peak���ľ���
		}
		*/
		int tmpOverlapLength=0;
		tmpOverlapLength=(int)startOverlapLength[2]+(int)endOverlapLength[2];
		for (int i = 2; i < lsSearchReslut.size(); i++) 
		{
			GffDetail tmpDetail=(GffDetail)lsSearchReslut.get(i);
			tmpOverlapLength=tmpOverlapLength+(tmpDetail.numberend-tmpDetail.numberstart);
		}
		if (tmpOverlapLength < 0) {
			System.out.println("����peak�Ľ���Ϊ����");
			tmpOverlapLength = 0;
		}
		return tmpOverlapLength;
	}
}
