package com.novelbio.analysis.seq.chipseq.readsChrDensity;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.base.fileOperate.FileOperate;


public class runReadsDensity 
{

	/**
	 * ����macs�Ľ��bed�ļ���������Ҫ��������
	 * ���򷽷�
	 * sort -k1,1 -k2,2n test #��һ�����һ����ֹ���򣬵ڶ�����ڶ�����ֹ����������
	 * @param args
	 */
	public static void main(String[] args) {
		///**mouse
		String parentFile="/media/winE/NBC/Project/Project_HY_Lab/TSC2_WT/mappingSoap/sort/";
		String resultFile = "/media/winE/NBC/Project/Project_HY_Lab/TSC2_WT/mappingSoap/sort/Mid";
		int invNum=10;
		int tagLength=30;//�����Զ���ȡ
		try {
			
			String mapFFile=parentFile+"mid_Treat_macs_Sort.bed";
			String mapRFile="";
			String prix = "mid";
			String sep="\t"; 
			
			ReadsDensity aaaDensity=new ReadsDensity();
			aaaDensity.maxresolution = 10000;
			aaaDensity.prepare(mapFFile,mapRFile,NovelBioConst.GENOME_PATH_UCSC_HG19_CHROM, sep, 1, 2,3, invNum,tagLength);
//			aaaDensity.prepare(mapFFile,mapRFile,chrFilePath,rworkSpaceString, sep, 0, 1,2, invNum,tagLength);
			aaaDensity.getAllChrDist();
			FileOperate.moveFoldFile(NovelBioConst.R_WORKSPACE_CHIP_CHRREADS,resultFile,prix,true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	}
}
