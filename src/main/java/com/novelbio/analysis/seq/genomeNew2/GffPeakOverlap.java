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
	 * 几个待用类
	 */
	GffHashPeak gffHashPeakMinus=new GffHashPeak();
	GffHashPeak gffHashPeakPlus=new GffHashPeak();
	
	GffsearchPeak gffsearchPeak=new GffsearchPeak();
	ArrayList<String> lspeakMinusID;//负链上的peakID
	ArrayList<String> lspeakPlusID;//正链上的peakID
	/**
	 * 将重复peak合并后负链上的Peak
	 */
	ArrayList<String> lspeakMinusCope;
	/**
	 * 将重复peak合并后正链上的Peak
	 */
	ArrayList<String> lspeakPlusCope;
	
	/**
	 * 负链上的PeakHash
	 */
	Hashtable<String, GffDetail> gffMinusLocHash;
	/**
	 * 正链上的PeakHash
	 */
	Hashtable<String, GffDetail> gffPlusLocHash;
	/**
	 * 用之前两个peak文件都要按照chr进行排序，不过chr内部不用排序，也就是chr内部坐标不要求一致
	 * 读取两个peak文件，输入两个文件名，默认两个文件中peak信息所在的列一样<br>
	 * 所有计数都从1开始计数
	 * @param filePlus 正向peak txt文件
	 * @param fileMinus 反向peak txt文件
	 * @param colChr ChrID所在的列
	 * @param peakStart peak起点所在的列
	 * @param peakEnd peak终点所在的列
	 * @param rowNum 从第几行读起
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
	 * 获得Plus所有peak的长度
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
	 * 获得Plus合并后所有peak的个数
	 * @return
	 */
	public int getPlusNum() 
	{
		return lspeakPlusCope.size();
	}
	
	/**
	 * 获得Minus合并后所有peak的个数
	 * @return
	 */
	public int getMinusNum() 
	{
		return lspeakMinusCope.size();
	}
	
	/**
	 * 获得Minus所有peak的长度
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
	 * 获得Overlap的长度
	 * 是用minus的起点和终点在plus中找
	 * @return int[2]
	 * 0: 负交正两个之间交集bp<br>
	 * 1: 负交正的peak数<br>
	 * 2: 正交负两个之间交集bp<br>
	 * 3: 正交负的peak数
	 */
	public int[] getOverlapInfo() {
		int overlapNumM2P = 0;
		int overlapLengthM2P=0;
		ArrayList<String[]> lsOverlap=new ArrayList<String[]>();
		lsOverlap=compareMinus2Plus(true);//将peakOverlap都考虑进去计算交集
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
		lsOverlap2=comparePlus2Minus(true);//将peakOverlap都考虑进去计算交集
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
	 * 是用minus的起点和终点在plus中查找的
	 * 返回ArrayList-String[]，里面依次保存每个minus peak的两端与plus peak 的overlap情况
	 * 0:ChrID <br>
	 * 1:PeakID <br>
	 * 2:overlapLength <br>
	 * @param peakOverlap true时将重叠peak合并来计算两条链上的peak交集
	 *  false：A链上的peak不考虑重叠，与B链上已经和并重叠的peak计算两条链上的peak交集
	 */
	public ArrayList<String[]> compareMinus2Plus(boolean peakOverlap) 
	{
		ArrayList<String[]> result=new ArrayList<String[]>();//存储最后结果
		
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
	 * 是用plus的起点和终点在minus中查找的
	 * 返回ArrayList-String[]，里面依次保存每个plus peak的两端与minus peak 的overlap情况
	 * 0:ChrID <br>
	 * 1:PeakID <br>
	 * 2:overlapLength <br>
	 * @param peakOverlap true时将重叠peak合并来计算两条链上的peak交集
	 *  false：A链上的peak不考虑重叠，与B链上已经和并重叠的peak计算两条链上的peak交集
	 */
	public ArrayList<String[]> comparePlus2Minus(boolean peakOverlap) 
	{
		ArrayList<String[]> result=new ArrayList<String[]>();//存储最后结果
		
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
	 * 查找peak OverLap的情况，返回Integer<br>
	 * 为gffMPeakDetial查找gffHashplusPeak后获得的交集情况<br>
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
		 * 以前编的，现在来检查以前编的效果如何
		//两个点落在一个peak内
		if(gffstartCodDetail.insideLOC&&gffendCodDetail.insideLOC&&gffstartCodDetail.LOCID[0].equals(gffendCodDetail.LOCID[0]))
		{
			return gffMinusPeakDetial.numberend-gffMinusPeakDetial.numberstart;
		}
		//两个点不落在一个peak内
		
		if (gffstartCodDetail.insideLOC) {
			tmpOverlapLength=tmpOverlapLength+gffstartCodDetail.distancetoLOCEnd[0];//第一个坐标到peak终点的距离
		}
		if (gffendCodDetail.insideLOC) {
			tmpOverlapLength=tmpOverlapLength+gffendCodDetail.distancetoLOCStart[0];//第二个坐标到peak起点的距离
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
			System.out.println("两个peak的交集为负数");
			tmpOverlapLength = 0;
		}
		return tmpOverlapLength;
	}
}
