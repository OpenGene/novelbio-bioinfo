package com.novelBio.chIPSeq.motif;


import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.novelBio.base.dataOperate.ExcelTxtRead;
import com.novelBio.base.dataOperate.TxtReadandWrite;
import com.novelBio.base.genome.GffChrUnion;

 

public class GetSeq 
{

	
	/**
	 * @param Gffclass ����ȡ��gffhash���࣬Ŀǰֻ���� "TIGR","CG","UCSC","Peak","Repeat"�⼸��
	 * @param gfffilename gff�ļ�
	 * @param chrPah chr�ļ���
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
	public static void getPeakSeq(String Gffclass, String gfffilename,String chrPah,int peaklength,int condition,String txtFilepeakFile,String sep,int[] columnID,int rowStart,int rowEnd, String txtresultfilename) throws Exception 
	{
		String[][] LOCIDInfo=ExcelTxtRead.readtxtExcel(txtFilepeakFile, sep, columnID, rowStart, rowEnd);
		GffChrUnion getPeakseq=new GffChrUnion();
		
		TxtReadandWrite txtresult=new TxtReadandWrite();
		txtresult.setParameter(txtresultfilename, true,false);
		txtresult.writefile("����peak��������ȡ��:" + peaklength+"bp");
		txtresult.writefile("\r\n");
		
		getPeakseq.loadChr(chrPah);
		if (condition==0) {
			getPeakseq.loadGff(Gffclass, gfffilename);
			txtresult.writefile("����Ϊ����peak���ڻ�����ȡ������");
			txtresult.writefile("\r\n");
		}
		else if (condition==1) {
			getPeakseq.loadGff(Gffclass, gfffilename);
			txtresult.writefile("����Ϊ����");
			txtresult.writefile("\r\n");
		}
		else if (condition==2) {
			getPeakseq.loadGff(Gffclass, gfffilename);
			txtresult.writefile("����Ϊ����");
			txtresult.writefile("\r\n");
		}
		
		ArrayList<String> lsresult=	getPeakSeq( Gffclass,  gfffilename, chrPah, peaklength, condition, txtFilepeakFile, sep,columnID,rowStart, rowEnd,true);
		txtresult.writefile(lsresult);
	}

	
	/**
	 * @param Gffclass ����ȡ��gffhash���࣬Ŀǰֻ���� "TIGR","CG","UCSC","Peak","Repeat"�⼸��
	 * @param gfffilename gff�ļ�
	 * @param chrPah chr�ļ���
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
	public static ArrayList<String> getPeakSeq(String Gffclass, String gfffilename,String chrPah,int peaklength,int condition,String txtFilepeakFile,
			String sep,int[] columnID,int rowStart,int rowEnd,boolean chrInfo) throws Exception 
	{
		String[][] LOCIDInfo=ExcelTxtRead.readtxtExcel(txtFilepeakFile, sep, columnID, rowStart, rowEnd);
		GffChrUnion getPeakseq=new GffChrUnion();
 
		getPeakseq.loadChr(chrPah);
		if (gfffilename != null && !gfffilename.trim().equals("")) {
			getPeakseq.loadGff(Gffclass, gfffilename);
		}
		
		ArrayList<String> lsresult=new ArrayList<String>();
		//String filter2="[AGC]A[GCA]A[ACG][AG][ACG][AG]|[AGC][AG][GCA]A[ACG][AG][ACG]A|[AGC]A[GCA]A[ACG][AG][ACG][AG]";
		//String filter3="[AGC]A[GCA]A[ACG][AG][ACG]A";
	//	 Pattern pattern =Pattern.compile(filter2, Pattern.CASE_INSENSITIVE); 
		 //   Matcher matcher;//matcher.groupCount() ���ش�ƥ����ģʽ�еĲ���������
	
		         
		for (int i = 0; i < LOCIDInfo.length; i++) {
			String peakSeq=getPeakseq.getPeakSeq(LOCIDInfo[i][0], Integer.parseInt(LOCIDInfo[i][1]), peaklength, condition); //.getCDGPeakSeq(LOCIDInfo[i][0],"C", Integer.parseInt(LOCIDInfo[i][1]), peaklength, condition);
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
