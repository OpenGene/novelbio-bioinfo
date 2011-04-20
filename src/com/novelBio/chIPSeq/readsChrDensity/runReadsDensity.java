package com.novelBio.chIPSeq.readsChrDensity;

import com.novelBio.base.fileOperate.FileOperate;
import com.novelBio.generalConf.NovelBioConst;


public class runReadsDensity 
{

	/**
	 * 对于macs的结果bed文件，首先需要进行排序
	 * 排序方法
	 * sort -k1,1 -k2,2n test #第一列起第一列终止排序，第二列起第二列终止按数字排序
	 * @param args
	 */
	public static void main(String[] args) {
		///**mouse
		String parentFile="/media/winE/NBC/Project/ChIPSeq_CDG110330/mapping/";
		String resultFile = "/media/winE/NBC/Project/ChIPSeq_CDG110330/result/readsInChr/";
		int invNum=10;
		int tagLength=300;//考虑自动获取
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
