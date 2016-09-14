package com.novelbio.analysis.tools;

import java.io.IOException;
import java.util.ArrayList;

import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.analysis.seq.sam.SamRecord;
import com.novelbio.analysis.seq.sam.SamToBam;
import com.novelbio.analysis.seq.sam.SamToBam.SamToBamOutFile;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

public class SamAddUniqueFlag {

	public static void main(String[] args) throws IOException {
//	 	String inFile = args[0];
//	 	String outFile = args[1];
/**	 	
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
*/		
//		String samFileTxt = "/run/media/novelbio/A/bianlianle/project/software_test/hisat2/LTJ_indexWithoutSplicefile_mapWithSpliceFile.result_UniqMapFlag_sorted.bam";
		
		String samFileTxt = "/run/media/novelbio/A/bianlianle/project/software_test/hisat2/LTJ_indexWithsplicefile_mapWithoutSpliceFile.result.bam";
		String juncationSam = "/run/media/novelbio/A/bianlianle/project/software_test/hisat2/junction_with_out.sam";
		TxtReadandWrite junctionReadandWrite = new TxtReadandWrite(juncationSam,true) ;
		SamFile samFile = new SamFile(samFileTxt);
		samFile.indexMake();
		
		ArrayList<Align> alignBlockAligns = new ArrayList<>();
		int i=0;
		for (SamRecord samRecord : samFile.readLines()) {
			alignBlockAligns = samRecord.getAlignmentBlocks();
			if ( samRecord.isUniqueMapping() && samRecord.isJunctionCovered()) {
				junctionReadandWrite.writefileln(samRecord.toString());
//				System.out.println(samRecord.toString());
//				if (i++>2) {
//					break;
//				}
			}
			
			
		}
		junctionReadandWrite.close();
	}
}
