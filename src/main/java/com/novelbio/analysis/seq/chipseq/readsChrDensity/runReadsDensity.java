package com.novelbio.analysis.seq.chipseq.readsChrDensity;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.base.fileOperate.FileOperate;


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
		String parentFile="/media/winE/NBC/Project/Project_WZF_Lab/Denovo_WZF110622/s_3_fastq.txt/TGACT/";
		String resultFile = "/media/winE/NBC/Project/Project_WZF_Lab/Denovo_WZF110622/s_3_fastq.txt/TGACT";
		int invNum=1;
		int tagLength=30;//考虑自动获取
		try {
			
			String mapFFile=parentFile+"TGACTsort.bed";
			String mapRFile="";
			String prix = "TGACTplot";
			String sep="\t"; 
			
			ReadsDensity aaaDensity=new ReadsDensity();
			aaaDensity.maxresolution = 30000;
			aaaDensity.prepare(mapFFile,mapRFile,"/media/winE/Bioinformatics/GenomeData/Streptococcus_suis/98HAH33/BWAindex/NC_009443.fna", "",sep, 1, 2,3, invNum,tagLength);
//			aaaDensity.prepare(mapFFile,mapRFile,NovelBioConst.GENOME_PATH_UCSC_HG19_CHROM, "",sep, 1, 2,3, invNum,tagLength);
			aaaDensity.getAllChrDist();
			FileOperate.moveFoldFile(NovelBioConst.R_WORKSPACE_CHIP_CHRREADS,resultFile,prix,true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	}
}
