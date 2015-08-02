package com.novelbio.analysis.seq.sam;

import htsjdk.samtools.SAMSequenceDictionary;

import java.io.IOException;

import junit.framework.TestCase;

import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.analysis.seq.sam.SamToBam.SamToBamOutFile;
import com.novelbio.base.fileOperate.FileOperate;

public class TestSamToBamSort extends TestCase {
	
	public void testAddMulti() {
		String inFile = "src/test/resources/test_file/sam/test.sam";
		String outFile = "/home/novelbio/git/NBCplatform/src/test/resources/test_file/sam/test_sort.bam";
		
		
		SamToBam samToBamSort = new SamToBam();
		samToBamSort.setIsPairend(true);
		//TODO 写一个配置文件来保存测试文件的路径
		try {
			samToBamSort.setInStream(FileOperate.getInputStream(inFile));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		samToBamSort.readInputStream();

		SamToBamOutFile samWriteSort = new SamToBamOutFile();
		
		samWriteSort.setNeedSort(true);
		samWriteSort.setOutFileName(outFile);
		samToBamSort.setSamWriteTo(samWriteSort);
		
		samToBamSort.writeToOs();
	
		SamFile samFile = new SamFile(outFile);
		assertEquals(true, SamFile.isSorted(samFile));
		
		for (SamRecord samRecord : samFile.readLines()) {
			if (samRecord.getName().equals("HWI-D00175:261:C6L59ANXX:7:1101:10478:67311")) {
				assertEquals(Integer.valueOf(2), samRecord.getMappingNum());
				assertEquals(false, samRecord.isUniqueMapping());
			}
		}
		
//		String indexFile = samFile.indexMake();
//		for (SamRecord samRecord : samFile.readLinesOverlap("chrc", 2000, 140659)) {
//			System.out.println(samRecord);
//		}
		FileOperate.DeleteFileFolder(outFile);
//		FileOperate.DeleteFileFolder(indexFile);
	}
	
	public void testNotAddMulti() {
		String inFile = "src/test/resources/test_file/sam/test.sam";
		String outFile = "src/test/resources/test_file/sam/test_sort_NotAddMulti.sam";
		
		SamFile samFileIn = new SamFile(inFile);
		assertEquals("chr4", samFileIn.getMapChrID2Length().keySet().iterator().next());
		SamToBamSort samToBamSort = new SamToBamSort(outFile, samFileIn);
		samToBamSort.setAddMultiHitFlag(false);
		samToBamSort.setNeedSort(true);
		samToBamSort.setSamSequenceDictionary(getSeqDict());
		samToBamSort.convert();
	
		SamFile samFile = samToBamSort.getSamFileBam();
		assertEquals("chr1", samFile.getMapChrID2Length().keySet().iterator().next());
		assertEquals(true, SamFile.isSorted(samFile));
		
		for (SamRecord samRecord : samFile.readLines()) {
			if (samRecord.getName().equals("HWI-D00175:261:C6L59ANXX:7:1101:10478:67311")) {
				assertEquals(Integer.valueOf(1), samRecord.getMappingNum());
				assertEquals(true, samRecord.isUniqueMapping());
			}
		}
		
		FileOperate.DeleteFileFolder(outFile);
	}
	
	public void testSortAndReorder() {
		String inFile = "src/test/resources/test_file/sam/test.sam";
		String outFile = "src/test/resources/test_file/sam/test_sort_reorder.sam";
		
		SamFile samFileIn = new SamFile(inFile);
		assertEquals("chr4", samFileIn.getMapChrID2Length().keySet().iterator().next());
		SamToBamSort samToBamSort = new SamToBamSort(outFile, samFileIn);
		samToBamSort.setAddMultiHitFlag(true);
		samToBamSort.setNeedSort(true);
		samToBamSort.setSamSequenceDictionary(getSeqDict());
		samToBamSort.convert();
	
		SamFile samFile = samToBamSort.getSamFileBam();
		assertEquals("chr1", samFile.getMapChrID2Length().keySet().iterator().next());
		assertEquals(true, SamFile.isSorted(samFile));
		
		int i = 0;
		for (SamRecord samRecord : samFile.readLines()) {
			if (i++ < 2) {
				assertEquals("chr3", samRecord.getRefID());
			} else {
				assertEquals("chrc", samRecord.getRefID());
			}
		}
		
		FileOperate.DeleteFileFolder(outFile);
	}
	
	private SAMSequenceDictionary getSeqDict() {
		SeqHash seqHash = new SeqHash("/hdfs:/nbCloud/public/nbcplatform/genome/species/3702/tair10/ChromFa/chrAll.fa");
		seqHash.close();
		return seqHash.getDictionary();
	}
}
