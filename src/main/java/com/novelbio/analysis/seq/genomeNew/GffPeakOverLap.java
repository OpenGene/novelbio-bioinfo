package com.novelbio.analysis.seq.genomeNew;

import java.util.ArrayList;
import java.util.HashMap;

import com.novelbio.analysis.seq.genomeNew.gffOperate.GffCodPeakDU;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailPeak;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashBin;

 

public class GffPeakOverLap 
{
	/**
	 * 几个待用类
	 */
	GffHashBin gffHashPeakMinus;
	GffHashBin gffHashPeakPlus;
	
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
	HashMap<String, GffDetailPeak> gffMinusLocHash;
	/**
	 * 正链上的PeakHash
	 */
	HashMap<String, GffDetailPeak> gffPlusLocHash;
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
	
		gffHashPeakPlus = new GffHashBin(true, colChr, peakStart, peakEnd, rowNum);
		gffHashPeakPlus.ReadGffarray(filePlus);
		gffHashPeakMinus = new GffHashBin(true, colChr, peakStart, peakEnd, rowNum);
		gffHashPeakMinus.ReadGffarray(fileMinus);
		
		lspeakMinusID = gffHashPeakMinus.getLOCIDList();
		lspeakPlusID = gffHashPeakPlus.getLOCIDList();
		
		lspeakMinusCope = gffHashPeakMinus.getLOCChrHashIDList();
		lspeakPlusCope = gffHashPeakPlus.getLOCChrHashIDList();
		
		
		gffMinusLocHash = gffHashPeakMinus.getLocHashtable();
		gffPlusLocHash = gffHashPeakPlus.getLocHashtable();
		
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
			GffDetailPeak tmpPlusDetail=gffPlusLocHash.get(lspeakPlusID.get(i));
			int tmplength=tmpPlusDetail.getEndAbs() - tmpPlusDetail.getStartAbs();
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
			GffDetailPeak tmpMinusDetail=gffMinusLocHash.get(lspeakMinusID.get(i));
			int tmplength=tmpMinusDetail.getEndAbs() - tmpMinusDetail.getStartAbs();
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
				GffDetailPeak gffMinusPeakDetial = gffMinusLocHash.get(tmpPeakID);
				String ChrID = gffMinusPeakDetial.getParentName();
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
				GffDetailPeak gffMinusPeakDetial = gffMinusLocHash.get(tmpPeakID);
				String ChrID = gffMinusPeakDetial.getParentName();
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
				GffDetailPeak gffPlusPeakDetial=gffPlusLocHash.get(tmpPeakID);
				String ChrID=gffPlusPeakDetial.getParentName();
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
				GffDetailPeak gffPlusPeakDetial=gffPlusLocHash.get(tmpPeakID);
				String ChrID=gffPlusPeakDetial.getParentName();
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
	 * @param parentName
	 * @param start
	 * @param end
	 */
	private int twoSiteLocation(GffDetailPeak gffMPeakDetial,GffHashBin gffHashplusPeak) 
	{
		String ChrID=gffMPeakDetial.getParentName();
		int start=gffMPeakDetial.getStartAbs();
		int end=gffMPeakDetial.getEndAbs();
		
		GffCodPeakDU lsSearchReslut = gffHashplusPeak.searchLocation(ChrID, start, end);//searchLocation(ChrID, start, end, gffHashplusPeak);
		if (lsSearchReslut == null) {
			return 0;
		}
		double startOverlapLength = lsSearchReslut.getOpLeftInCod();
		if (startOverlapLength == 100) {
			return lsSearchReslut.getOpLeftBp();
		}

		int tmpOverlapLength=0;
		tmpOverlapLength= lsSearchReslut.getOpLeftBp() + lsSearchReslut.getOpRightBp();
		if (lsSearchReslut.getLsGffDetailMid() != null) {
			for (GffDetailPeak gffDetailPeak : lsSearchReslut.getLsGffDetailMid()) {
				tmpOverlapLength = tmpOverlapLength + (int)gffDetailPeak.getLen();
			}
		}
	
		
		if (tmpOverlapLength < 0) {
			System.out.println("两个peak的交集为负数");
			tmpOverlapLength = 0;
		}
		return tmpOverlapLength;
	}
}
