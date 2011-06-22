package com.novelbio.analysis.seq.chipseq.motif;


import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.novelbio.analysis.seq.chipseq.prepare.GenomeBasePrepare;
import com.novelbio.analysis.seq.genome.GffChrUnion;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;

public class GetSeq extends GenomeBasePrepare
{

	
	/**
	 * 使用前先用prepare准备，需要Gffclass,gfffilename和chrPah
	 * @param peaklength peak左右两端长度
	 * @param condition 
	 * 0:按照peak在gff里的情况提取，也就是基因内按基因方向，基因外正向
	 * 1: 通通提取正向
	 * 2: 通通提取反向
	 * @param txtFilepeakFile peak信息，以文本形式保存
	 * @param sep 文本里面的分割符，一般为"\t"
	 * @param columnID 读取哪几列，用int[]保存，列可以有间隔，现在读取这两列，0：chrID，1:peakSummit
	 * @param rowStart 从第几列读取
	 * @param rowEnd 读到第几列 如果rowEnd=-1，则一直读到文件结尾
	 * @param txtresultfilename
	 * @throws Exception
	 */
	public static void getPeakSeq(int peaklength,int condition,String txtFilepeakFile,String sep,int[] columnID,int rowStart,int rowEnd, String txtresultfilename) throws Exception 
	{
		String[][] LOCIDInfo=ExcelTxtRead.readtxtExcel(txtFilepeakFile, sep, columnID, rowStart, rowEnd);
		
		TxtReadandWrite txtresult=new TxtReadandWrite();
		txtresult.setParameter(txtresultfilename, true,false);
		txtresult.writefile("本次peak左右两端取了:" + peaklength+"bp");
		txtresult.writefile("\r\n");
		
		if (condition==0) {
			txtresult.writefile("方向为按照peak所在基因方向取的序列");
			txtresult.writefile("\r\n");
		}
		else if (condition==1) {
			txtresult.writefile("方向为正向");
			txtresult.writefile("\r\n");
		}
		else if (condition==2) {
			txtresult.writefile("方向为反向");
			txtresult.writefile("\r\n");
		}
		ArrayList<String> lsresult=	getPeakSeq(LOCIDInfo, peaklength, condition, true);
		txtresult.writefile(lsresult);
	}

	
	/**
	 * 使用前先用prepare准备，需要Gffclass,gfffilename和chrPah
	 * @param peaklength peak左右两端长度
	 * @param condition 
	 * 0:按照peak在gff里的情况提取，也就是基因内按基因方向，基因外正向
	 * 1: 通通提取正向
	 * 2: 通通提取反向
	 * @param txtFilepeakFile peak信息，以文本形式保存
	 * @param sep 文本里面的分割符，一般为"\t"
	 * @param columnID 读取哪几列，用int[]保存，列可以有间隔，现在读取这两列，0：chrID，1:peakSummit
	 * @param rowStart 从第几列读取
	 * @param rowEnd 读到第几列 如果rowEnd=-1，则一直读到sheet1文件结尾
	 * @param chrInfo 是否记录chrID信息，false不记录chrID的信息
	 * @throws Exception
	 * @return  ArrayList-String 每个peak的序列
	 */
	public static ArrayList<String> getPeakSeq(int peaklength,int condition,String txtFilepeakFile,
			String sep,int[] columnID,int rowStart,int rowEnd,boolean chrInfo) throws Exception 
	{
		String[][] LOCIDInfo=ExcelTxtRead.readtxtExcel(txtFilepeakFile, sep, columnID, rowStart, rowEnd); 
		return getPeakSeq(LOCIDInfo, peaklength, condition, chrInfo);
	}
	/**
	 * 指定染色体坐标点，提取该染色体坐标左右peaklength的序列
	 * 使用前先用prepare准备，需要Gffclass,gfffilename和chrPah
	 * @param LOCIDInfo 染色体坐标信息，string-2：0：chrID，1：具体坐标
	 * @param peaklength peak左右两端长度
	 * @param condition 
	 * 0:按照peak在gff里的情况提取，也就是基因内按基因方向，基因外正向
	 * 1: 通通提取正向
	 * 2: 通通提取反向
	 * @param chrInfo 是否记录chrID信息，false不记录chrID的信息
	 * @throws Exception
	 * @return  ArrayList-String 每个peak的序列
	 */
	public static ArrayList<String> getPeakSeq(String[][] LOCIDInfo,int peaklength,int condition,boolean chrInfo) throws Exception 
	{		
		ArrayList<String> lsresult=new ArrayList<String>();
		//String filter2="[AGC]A[GCA]A[ACG][AG][ACG][AG]|[AGC][AG][GCA]A[ACG][AG][ACG]A|[AGC]A[GCA]A[ACG][AG][ACG][AG]";
		//String filter3="[AGC]A[GCA]A[ACG][AG][ACG]A";
	//	 Pattern pattern =Pattern.compile(filter2, Pattern.CASE_INSENSITIVE); 
		 //   Matcher matcher;//matcher.groupCount() 返回此匹配器模式中的捕获组数。
	
		         
		for (int i = 0; i < LOCIDInfo.length; i++) {
			String peakSeq=gffLocatCod.getPeakSeq(LOCIDInfo[i][0], Integer.parseInt(LOCIDInfo[i][1]), peaklength, condition); //.getCDGPeakSeq(LOCIDInfo[i][0],"C", Integer.parseInt(LOCIDInfo[i][1]), peaklength, condition);
			if (peakSeq.equals("")) {
				continue;
			}
			if (chrInfo) {
				lsresult.add(">"+LOCIDInfo[i][0]+"_"+LOCIDInfo[i][1]);
			}
			lsresult.add(peakSeq);
			
			/**
			//////////////////判断是否特定motif，motif中是否含c///////////////////////////////////////////////////////////////////////////////
			if (peakSeq.contains("c")||peakSeq.contains("C")) {
			    matcher = pattern.matcher(peakSeq);     
			    if (matcher.find()) {
			    	String ss=matcher.group();
			
			    	if (ss.contains("c")||peakSeq.contains("C")) {
			    	/////////////////////////////////////////////////////////////////////////////////////
			    		lsresult.add(">"+LOCIDInfo[i][0]+"_"+LOCIDInfo[i][1]);
						lsresult.add(peakSeq);			    
					}
			    		
				}
			}
			
			*/
		}
		return lsresult;
	}
}
