package com.novelbio.analysis.tools;

import java.io.IOException;

import com.novelbio.analysis.seq.sam.SamToBam;
import com.novelbio.analysis.seq.sam.SamToBam.SamToBamOutFile;
import com.novelbio.base.fileOperate.FileOperate;

public class SamAddUniqueFlag {

	public static void main(String[] args) throws IOException {
//	 	String inFile = args[0];
//	 	String outFile = args[1];
	 	
		String inFile = "/run/media/novelbio/A/tmp/A_hisat.sam";
	 	String outFile = "/run/media/novelbio/A/tmp/A_hisat.addFlag.bam";
		SamToBam samToBamSort = new SamToBam();
		samToBamSort.setIsPairend(true);
	 	//TODO 写一个配置文件来保存测试文件的路径
		samToBamSort.setInStream(FileOperate.getInputStream(inFile));
	 	samToBamSort.readInputStream();
	 	SamToBamOutFile samWriteSort = new SamToBamOutFile();
	 	samWriteSort.setNeedSort(false);
	 	samWriteSort.setOutFileName(outFile);
	 	samToBamSort.setSamWriteTo(samWriteSort);
	 	samToBamSort.writeToOs();
		
//		String samFileTxt = "/run/media/novelbio/A/tmp/test/mock_S_a.bowtie2_addFlag_sorted.bam";
//		SamFile samFile = new SamFile(samFileTxt);
//		samFile.indexMake();
//		for (SamRecord samRecord : samFile.readLinesOverlap("IWGSC_CSS_1BL_scaff_3829327", 3356, 3375)) {
//			System.out.println(samRecord.toString());
//		}
		
	}
}
