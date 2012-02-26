package com.novelbio.analysis.project.cdg;

import java.util.ArrayList;

import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.FastQ;
import com.novelbio.analysis.seq.genomeNew.GffChrMap;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.generalConf.NovelBioConst;

public class StatisticMap {
	
	GffChrMap gffChrMap = new GffChrMap(NovelBioConst.GENOME_GFF_TYPE_UCSC, NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ,
			NovelBioConst.GENOME_PATH_UCSC_MM9_CHROM, null,5);
	
	
	public static void main(String[] args) {
		getStatisticMapping();
 	}
	
	public void getStatic(String bedFile, String outFile)
	{
		gffChrMap.setMapReads(bedFile, 10);
		gffChrMap.loadMapReads();
		ArrayList<String[]> lsInfo = gffChrMap.getChrLenInfo();
		TxtReadandWrite txtOut = new TxtReadandWrite(outFile, true);
		txtOut.ExcelWrite(lsInfo, "\t", 1, 1);
		txtOut.close();
	}
	
	
	public static void getStatisticMapping() {
		String txtOutFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/mappingQualityK4"; String[] content1 = null; String filteredFile = "";
		TxtReadandWrite txtOut = new TxtReadandWrite(txtOutFile, true);
		String parentFileMap = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/rawdata/";
		String parentFileBed = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/mapping/";
		
//		content1 = getStaticMapping("K27_2K", parentFileMap + "2K.clean.fq.gz", filteredFile, parentFileBed + "2Kextend_sort.bed");
//		txtOut.writefileln(content1);
//		content1 = getStaticMapping("K27_2W", parentFileMap + "2W.clean.fq.gz", filteredFile, parentFileBed + "2Wextend_sort.bed");
//		txtOut.writefileln(content1);
//		content1 = getStaticMapping("K27_FX2", parentFileMap + "FX2.clean.fq.gz", filteredFile,  parentFileBed + "FX2extend_sort.bed");
//		txtOut.writefileln(content1);
//		content1 = getStaticMapping("K27_FHE", parentFileMap + "FHE.clean.fq.gz", filteredFile,  parentFileBed + "FHE.clean.fq_ExtendSort.bed");
//		txtOut.writefileln(content1);
//		content1 = getStaticMapping("K27_KE", parentFileMap + "KE.clean.fq.gz", filteredFile, parentFileBed + "KEextend_sort.bed");
//		txtOut.writefileln(content1);
//		content1 = getStaticMapping("K27_WE", parentFileMap + "WE.clean.fq.gz", filteredFile,  parentFileBed + "WEextend_sort.bed");
//		txtOut.writefileln(content1);
//		content1 = getStaticMapping("K27_4K", parentFileMap + "HSZ_K-4.clean.fq.gz", filteredFile, parentFileBed + "4Kextend_sort.bed");
//		txtOut.writefileln(content1);
//		content1 = getStaticMapping("K27_4W", parentFileMap + "HSZ_W-4.clean.fq.gz", filteredFile,  parentFileBed + "4Wextend_sort.bed");
//		txtOut.writefileln(content1);
//		
		
		parentFileMap = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110225/rawData/";
		parentFileBed = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110225/mapping/";
		content1 = getStaticMapping("K4_K0", parentFileMap + "K0.fq", filteredFile, parentFileBed + "k0_extend_sort.bed");
		txtOut.writefileln(content1);
		content1 = getStaticMapping("K4_K4", parentFileMap + "K4.fq", filteredFile, parentFileBed + "k4_extend_sort.bed");
		txtOut.writefileln(content1);
		content1 = getStaticMapping("K4_W0", parentFileMap + "W0.fq", filteredFile,  parentFileBed + "W0_extend_sort.bed");
		txtOut.writefileln(content1);
		content1 = getStaticMapping("K4_W4", parentFileMap + "W4.fq", filteredFile, parentFileBed + "W4_extend_sort.bed");
		txtOut.writefileln(content1);
		
		
		
		
		
		txtOut.close();
		
		
		
		
	}
	
	
	private static String[] getStaticMapping(String title, String fQ, String filteredFile, String bedFile)
	{
		FastQ fastQ = new FastQ(fQ, FastQ.QUALITY_LOW);
//		fastQ.setCompressType(TxtReadandWrite.GZIP, TxtReadandWrite.TXT);
//		FastQ fastQFilter = new FastQ(filteredFile, FastQ.QUALITY_LOW);
		BedSeq bedSeq = new BedSeq(bedFile);
		String[] result = new String[3];
		result[0] = title;
		result[1] = fastQ.getSeqNum() + "";
//		result[2] = fastQFilter.getSeqFile() + "";
		result[2] = bedSeq.getSeqNum() + "";
		return result;
	}
	
}
