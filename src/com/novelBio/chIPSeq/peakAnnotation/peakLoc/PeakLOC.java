package com.novelBio.chIPSeq.peakAnnotation.peakLoc;


import java.util.ArrayList;
import java.util.List;

import com.novelBio.base.dataOperate.ExcelOperate;
import com.novelBio.base.dataOperate.ExcelTxtRead;
import com.novelBio.base.dataOperate.TxtReadandWrite;
import com.novelBio.base.dataStructure.ArrayOperate;
import com.novelBio.base.dataStructure.MathComput;
import com.novelBio.base.genome.GffLocatCod;
import com.novelBio.base.genome.gffOperate.GffHashUCSCgene;
import com.novelBio.chIPSeq.prepare.GenomeBasePrepare;


 
/**
 * ��peak��λ�������ϣ���ȷ���������ӻ��ں���
 * @author zong0jie
 *
 */
public class PeakLOC extends GenomeBasePrepare{
	
	/**
	 * peak��λ����д���ı��ļ���ֻ���2k�����Լ�50bp�ڵ�λ��
	 * @param gfffilename gff�ļ�
	 * @param txtFilepeakFile peak�ļ�,ע���һ����title������Ҫ�е�
	 * @param sep �ָ���
	 * @param columnID ��ȡpeak�ļ����ļ��У���Ҫָ��ChrID��summitλ��
	 * @param rowStart �ӵڼ��ж���
	 * @param rowEnd �����ڼ���Ϊֹ
	 * @param txtresultfilename д���ļ���·�����ļ���
	 * @throws Exception
	 */
	public static void locatDetail(String txtFilepeakFile,String sep,int[] columnID,int rowStart,int rowEnd,String excelFile) throws Exception 
	{
		String[][] LOCIDInfo=ExcelTxtRead.readtxtExcel(txtFilepeakFile, sep, columnID, rowStart, rowEnd);
		gffLocatCod.setUpstreamTSSbp(5000);
		gffLocatCod.setDownStreamTssbp(5000);
		gffLocatCod.setGeneEnd3UTR(5000);
		
		ArrayList<String[]>LOCDetail=gffLocatCod.peakAnnotationEN(LOCIDInfo);		
		/**
		 * ������ά����,�����ÿ��peakLOC���ڵĻ������UCSCknown gene�Լ�refseq ��������ݣ�
			��һά��ChrID
			�ڶ�ά������
			���ArrayList-String[8]
		0: ChrID
		1: ����
		2: �ڻ����ڣ���������
		3: �ڻ����ڵľ�����Ϣ
		4: �ڻ���䲢�Ҿ����ϸ�����ܽ����ϸ�������
		5: �ϸ������򣬵��ϸ��������/�յ�ľ���
		6: �ڻ���䲢�Ҿ����¸�����ܽ����¸�������
		7: �¸������򣬵��¸��������/�յ�ľ��� 
		 */
		
		TxtReadandWrite txtReadandWrite=new TxtReadandWrite();
		txtReadandWrite.setParameter(txtFilepeakFile, false, true);
		String[][] all = txtReadandWrite.ExcelRead("\t", 1, 1, txtReadandWrite.ExcelRows(), txtReadandWrite.ExcelColumns(2, "\t"));
		
		
		String[] content=new String[8];
		content[0]="";content[1]="";
		content[2]="insideGene_GeneName";
		content[3]="DetailInfo";content[4]="UpStream_GeneName";
		content[5]="Distance to UpStream Gene";content[6]="DownStream_GeneName";
		content[7]="Distance to DownSteam Gene";
		LOCDetail.add(0, content);
		String[][] locDetailStr=new String[LOCDetail.size()][LOCDetail.get(1).length-2];
		for (int i = 0; i < locDetailStr.length; i++) {
			for (int j = 2; j <LOCDetail.get(1).length; j++) {
				locDetailStr[i][j-2]=LOCDetail.get(i)[j];
			}
		}
		String[][] result=ArrayOperate.combStrArray(all, locDetailStr, -1);
		txtReadandWrite.setParameter(excelFile, true, false);
		txtReadandWrite.ExcelWrite(result, "\t");
	}
	
	
	/**
	 * 
	 * ɸѡ�����ʵ�peak
	 * 
	 * @param txtFile peak�ļ�,ע���һ����title������Ҫ�е�
	 * @param sep �ָ���
	 * @param colChrID chrID�ڵڼ��У�ʵ����
	 * @param colSummit summitλ���ڵڼ��У�ʵ����
	 * @param rowStart �ӵڼ��ж���ע�Ȿ��Ҫ����title
	 * @param filterTss �Ƿ����tssɸѡ��null�����У�������У���ô������int[2],0��tss���ζ���bp  1��tss���ζ���bp����Ϊ���� <b>ֻ�е�filterGeneBodyΪfalseʱ��tss���βŻᷢ������</b>
	 * @param filterGenEnd �Ƿ����geneEndɸѡ��null�����У�������У���ô������int[2],0��geneEnd���ζ���bp  1��geneEnd���ζ���bp����Ϊ����<b>ֻ�е�filterGeneBodyΪfalseʱ��geneEnd���βŻᷢ������</b>
	 * @param filterGeneBody �Ƿ���geneBody��true��������geneBody�Ļ���ȫ��ɸѡ������false��������geneBody��ɸѡ<br>
	 * <b>��������ֻ�е�filterGeneBodyΪfalseʱ���ܷ�������</b>
	 * @param filter5UTR �Ƿ���5UTR��
	 * @param filter3UTR �Ƿ���3UTR��
	 * @param filterExon �Ƿ�����������
	 * @param filterIntron �Ƿ����ں�����
	 * @param excelResultFile �����ļ�
	 * 0-n:�����loc��Ϣ<br>
	 * n+1: ������<br>
	 * n+2: ������Ϣ<br>
	 */
	public static void filterPeak(String txtFile,String sep,int colChrID,int colSummit,int rowStart,
			int[] filterTss, int[] filterGenEnd, boolean filterGeneBody, boolean filter5UTR, boolean filter3UTR, boolean filterExon,boolean filterIntron,
			String excelResultFile) throws Exception 
	{
		TxtReadandWrite txtPeakFile = new TxtReadandWrite(); txtPeakFile.setParameter(txtFile, false, true);
		ArrayList<String[]> LOCIDInfo = txtPeakFile.ExcelRead(sep, rowStart, 1, txtPeakFile.ExcelRows(), txtPeakFile.ExcelColumns(rowStart, sep), -1);
		String[] title = LOCIDInfo.get(0);
		String[] titleNew = new String[title.length + 2];
		for (int i = 0; i < title.length; i++) {
			titleNew[i] = title[i];
		}
		titleNew[title.length] = "AccID";
		titleNew[title.length+1] = "Location";
		List<String[]> lsQuery = LOCIDInfo.subList(1, LOCIDInfo.size()-1);
		ArrayList<String[]> LOCDetail=gffLocatCod.peakAnnoFilter(lsQuery, colChrID, colSummit, filterTss, filterGenEnd, filterGeneBody, filter5UTR, filter3UTR, filterExon, filterIntron);
		LOCDetail.add(0, titleNew);
		ExcelOperate excelResult = new ExcelOperate();
		excelResult.openExcel(excelResultFile);
		excelResult.WriteExcel(true, 1, 1, LOCDetail);
	}
	
	
	
	
	/**
	 * peak��λ����д���ı��ļ�
	 * @param gfffilename gff�ļ�
	 * @param txtFilepeakFile peak�ļ�
	 * @param sep �ָ���
	 * @param columnID ��ȡpeak�ļ����ļ��У���Ҫָ��ChrID��summitλ��
	 * @param rowStart �ӵڼ��ж���
	 * @param rowEnd �����ڼ���Ϊֹ
	 * @param txtresultfilename д���ļ���·�����ļ���
	 * @throws Exception
	 */
	public static void locatstatistic(String txtFilepeakFile,String sep,int[] columnID,int rowStart,int rowEnd,String txtresultfilename) throws Exception 
	{
		String[][] LOCIDInfo=ExcelTxtRead.readtxtExcel(txtFilepeakFile, sep, columnID, rowStart, rowEnd);
		
		ArrayList<String[]> LOCDetail=gffLocatCod.peakAnnotationDetail(LOCIDInfo);
		
		/**
		 * ������ά����,�����ÿ��peakLOC���ڵĻ������UCSCknown gene�Լ�refseq ��������ݣ�
			��һά��ChrID
			�ڶ�ά������
			���ArrayList-String[10]
			1: ����
			2: �����ڻ��ǻ����
			3: �ڻ�����: �ں��ӻ���������
			4: �ں������ں������������
			5: �ں������ں����յ�������
			6: �����������������������
			7: ���������������յ�������
			8: 5UTR��TSS�����������׼���������ж���Ҫ�ͳµ¹�����
			9: 5UTR��ATG�������
			10: 3UTR��������������
			11: 3UTR������β�������
			12: peak��TSS����
			13: peak��ATG����
			14: ���peak�ڻ���䣬peak������յ�ľ���
		 */
		
		TxtReadandWrite result=new TxtReadandWrite();
		result.setParameter(txtresultfilename, true,false);
		String content="chrID"+"\t"+"����"+"\t"+"�����ڻ��ǻ����"+"\t"+"�ں��ӻ���������"+"\t"+"�ں������ں������������"+"\t"+"�ں������ں����յ�������"+"\t"+
		"�����������������������"+"\t"+"���������������յ�������"+"\t"+"�Ƿ�5UTR"+"\t"+"5UTR��TSS����"+"\t"+
		"5UTR��ATG�������"+"\t"+"�Ƿ�3UTR"+"\t"+"3UTR��������������"+"\t"+"3UTR������β�������"+"\t"+"peak��TSS����"+"\t"+"peak��ATG����"+"\t"
		+"���peak�ڻ���䣬peak������յ�ľ���"+"\r\n";
		result.writefile(content);
		result.ExcelWrite(LOCDetail, sep, 1, 1);
	}
	
	
	/**
	 * ���Intron/Exon��ͳ����Ϣ����������״ͼ��
	 * ֱ�ӽ����صĽ��д���ı��ļ�
	 * @return
	 * @throws Exception 
	 */
	public static String[][] getPeakStaticInfo(String txtFilepeakFile,String sep,int[] columnID,int rowStart,int rowEnd) throws Exception 
	{
		String[][] LOCIDInfo=ExcelTxtRead.readtxtExcel(txtFilepeakFile, sep, columnID, rowStart, rowEnd);
		String[][]  gffPeakstaticInfo=gffLocatCod.peakStatistic(LOCIDInfo);
		ArrayList<Long>  gffstaticInfo=gffLocatCod.getGeneStructureLength();
		String[] item=new String[gffPeakstaticInfo.length];
		long[] peakInfo=new long[gffPeakstaticInfo.length];
		long[] background=new long[gffPeakstaticInfo.length];
		long chrLength=gffLocatCod.getChrLength("");
		for (int i = 0; i < gffPeakstaticInfo.length; i++) {
			item[i]=gffPeakstaticInfo[i][0];
			peakInfo[i]=Integer.parseInt(gffPeakstaticInfo[i][1]);
			background[i]=gffstaticInfo.get(i);
		}
		background[5]=chrLength-background[0]-background[1]-background[2]-background[3]-background[4];
		return MathComput.batStatistic(peakInfo, background, item, "PeakInfo", "GenomeBackGround");
	}
	
	
	
	
	
	
	
	
	/**
	 * ���ȫ������Intron/Exon����Ϣ
	 * @return
	 * @throws Exception 
	 */
	public static ArrayList<String[]> getStaticInfo() throws Exception 
	{
		ArrayList<Long>  gffstaticInfo=gffLocatCod.getGeneStructureLength();
		ArrayList<String[]> lsresult=new ArrayList<String[]>();
			String[] tmpresult0=new String[2];
			tmpresult0[0]="allGeneLength"; tmpresult0[1]=gffstaticInfo.get(0)+"";
			lsresult.add(tmpresult0);
			String[] tmpresult1=new String[2];
			tmpresult1[0]="allIntronLength"; tmpresult1[1]=gffstaticInfo.get(1)+"";
			lsresult.add(tmpresult1);
			String[] tmpresult2=new String[2];
			tmpresult2[0]="allExonLength"; tmpresult2[1]=gffstaticInfo.get(2)+"";
			lsresult.add(tmpresult2);
			String[] tmpresult3=new String[2];
			tmpresult3[0]="all5UTRLength"; tmpresult3[1]=gffstaticInfo.get(3)+"";
			lsresult.add(tmpresult3);
			String[] tmpresult4=new String[2];
			tmpresult4[0]="all3UTRLength"; tmpresult4[1]=gffstaticInfo.get(4)+"";
			lsresult.add(tmpresult4);
			String[] tmpresult5=new String[2];
			tmpresult5[0]="allup2kLength"; tmpresult5[1]=gffstaticInfo.get(5)+"";
			lsresult.add(tmpresult5);
			return lsresult;
	}

}
