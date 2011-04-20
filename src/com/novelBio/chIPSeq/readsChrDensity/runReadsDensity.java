package com.novelBio.chIPSeq.readsChrDensity;

import com.novelBio.base.fileOperate.FileOperate;
import com.novelBio.generalConf.NovelBioConst;


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
		String parentFile="/media/winE/NBC/Project/ChIPSeq_CDG110330/mapping/";
		String resultFile = "/media/winE/NBC/Project/ChIPSeq_CDG110330/result/readsInChr/";
		int invNum=10;
		int tagLength=300;//�����Զ���ȡ
		try {
			
			String mapFFile=parentFile+"FT5sort.bed";
			String mapRFile="";
			String prix = "FT5";
			String sep="\t"; 
			
			ReadsDensity aaaDensity=new ReadsDensity();
			aaaDensity.maxresolution = 10000;
			aaaDensity.prepare(mapFFile,mapRFile,NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM, sep, 1, 2,3, invNum,tagLength);
//			aaaDensity.prepare(mapFFile,mapRFile,chrFilePath,rworkSpaceString, sep, 0, 1,2, invNum,tagLength);
			aaaDensity.getAllChrDist();
			FileOperate.moveFoldFile(NovelBioConst.R_WORKSPACE_CHIP_CHRREADS,resultFile,prix);
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	}
}
