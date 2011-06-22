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
	 * ʹ��ǰ����prepare׼������ҪGffclass,gfffilename��chrPah
	 * @param peaklength peak�������˳���
	 * @param condition 
	 * 0:����peak��gff��������ȡ��Ҳ���ǻ����ڰ������򣬻���������
	 * 1: ͨͨ��ȡ����
	 * 2: ͨͨ��ȡ����
	 * @param txtFilepeakFile peak��Ϣ�����ı���ʽ����
	 * @param sep �ı�����ķָ����һ��Ϊ"\t"
	 * @param columnID ��ȡ�ļ��У���int[]���棬�п����м�������ڶ�ȡ�����У�0��chrID��1:peakSummit
	 * @param rowStart �ӵڼ��ж�ȡ
	 * @param rowEnd �����ڼ��� ���rowEnd=-1����һֱ�����ļ���β
	 * @param txtresultfilename
	 * @throws Exception
	 */
	public static void getPeakSeq(int peaklength,int condition,String txtFilepeakFile,String sep,int[] columnID,int rowStart,int rowEnd, String txtresultfilename) throws Exception 
	{
		String[][] LOCIDInfo=ExcelTxtRead.readtxtExcel(txtFilepeakFile, sep, columnID, rowStart, rowEnd);
		
		TxtReadandWrite txtresult=new TxtReadandWrite();
		txtresult.setParameter(txtresultfilename, true,false);
		txtresult.writefile("����peak��������ȡ��:" + peaklength+"bp");
		txtresult.writefile("\r\n");
		
		if (condition==0) {
			txtresult.writefile("����Ϊ����peak���ڻ�����ȡ������");
			txtresult.writefile("\r\n");
		}
		else if (condition==1) {
			txtresult.writefile("����Ϊ����");
			txtresult.writefile("\r\n");
		}
		else if (condition==2) {
			txtresult.writefile("����Ϊ����");
			txtresult.writefile("\r\n");
		}
		ArrayList<String> lsresult=	getPeakSeq(LOCIDInfo, peaklength, condition, true);
		txtresult.writefile(lsresult);
	}

	
	/**
	 * ʹ��ǰ����prepare׼������ҪGffclass,gfffilename��chrPah
	 * @param peaklength peak�������˳���
	 * @param condition 
	 * 0:����peak��gff��������ȡ��Ҳ���ǻ����ڰ������򣬻���������
	 * 1: ͨͨ��ȡ����
	 * 2: ͨͨ��ȡ����
	 * @param txtFilepeakFile peak��Ϣ�����ı���ʽ����
	 * @param sep �ı�����ķָ����һ��Ϊ"\t"
	 * @param columnID ��ȡ�ļ��У���int[]���棬�п����м�������ڶ�ȡ�����У�0��chrID��1:peakSummit
	 * @param rowStart �ӵڼ��ж�ȡ
	 * @param rowEnd �����ڼ��� ���rowEnd=-1����һֱ����sheet1�ļ���β
	 * @param chrInfo �Ƿ��¼chrID��Ϣ��false����¼chrID����Ϣ
	 * @throws Exception
	 * @return  ArrayList-String ÿ��peak������
	 */
	public static ArrayList<String> getPeakSeq(int peaklength,int condition,String txtFilepeakFile,
			String sep,int[] columnID,int rowStart,int rowEnd,boolean chrInfo) throws Exception 
	{
		String[][] LOCIDInfo=ExcelTxtRead.readtxtExcel(txtFilepeakFile, sep, columnID, rowStart, rowEnd); 
		return getPeakSeq(LOCIDInfo, peaklength, condition, chrInfo);
	}
	/**
	 * ָ��Ⱦɫ������㣬��ȡ��Ⱦɫ����������peaklength������
	 * ʹ��ǰ����prepare׼������ҪGffclass,gfffilename��chrPah
	 * @param LOCIDInfo Ⱦɫ��������Ϣ��string-2��0��chrID��1����������
	 * @param peaklength peak�������˳���
	 * @param condition 
	 * 0:����peak��gff��������ȡ��Ҳ���ǻ����ڰ������򣬻���������
	 * 1: ͨͨ��ȡ����
	 * 2: ͨͨ��ȡ����
	 * @param chrInfo �Ƿ��¼chrID��Ϣ��false����¼chrID����Ϣ
	 * @throws Exception
	 * @return  ArrayList-String ÿ��peak������
	 */
	public static ArrayList<String> getPeakSeq(String[][] LOCIDInfo,int peaklength,int condition,boolean chrInfo) throws Exception 
	{		
		ArrayList<String> lsresult=new ArrayList<String>();
		//String filter2="[AGC]A[GCA]A[ACG][AG][ACG][AG]|[AGC][AG][GCA]A[ACG][AG][ACG]A|[AGC]A[GCA]A[ACG][AG][ACG][AG]";
		//String filter3="[AGC]A[GCA]A[ACG][AG][ACG]A";
	//	 Pattern pattern =Pattern.compile(filter2, Pattern.CASE_INSENSITIVE); 
		 //   Matcher matcher;//matcher.groupCount() ���ش�ƥ����ģʽ�еĲ���������
	
		         
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
			//////////////////�ж��Ƿ��ض�motif��motif���Ƿ�c///////////////////////////////////////////////////////////////////////////////
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
