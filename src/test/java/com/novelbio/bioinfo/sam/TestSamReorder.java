package com.novelbio.bioinfo.sam;

import static org.junit.Assert.assertEquals;
import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.SAMSequenceRecord;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.bioinfo.fasta.SeqHash;
import com.novelbio.bioinfo.sam.SamFile;
import com.novelbio.bioinfo.sam.SamRecord;
import com.novelbio.bioinfo.sam.SamReorder;
import com.novelbio.bioinfo.sam.SamToBam;
import com.novelbio.bioinfo.sam.SamToBamSort;
import com.novelbio.bioinfo.sam.SamToBam.SamToBamOutFile;

public class TestSamReorder {
	String inFile = "src/test/resources/test_file/sam/test.sam";

//	@Test
	public void testSamReorder() {
		String outFile = "src/test/resources/test_file/sam/test_sort_reorder.sam";
		
		SamFile samFileIn = new SamFile(inFile);
		Assert.assertEquals("chr4", samFileIn.getMapChrID2Length().keySet().iterator().next());
		SamToBamSort samToBamSort = new SamToBamSort(outFile, samFileIn);
		samToBamSort.setAddMultiHitFlag(false);
		samToBamSort.setNeedSort(false);
		samToBamSort.setSamSequenceDictionary(getSeqDict(true));
		samToBamSort.convertAndFinish();
	
		SamFile samFileOut = samToBamSort.getSamFileBam();
		Assert.assertEquals("chr1", samFileOut.getMapChrID2Length().keySet().iterator().next());
		Assert.assertEquals(false, SamFile.isSorted(samFileOut));
		
		Iterator<SamRecord> itSamIn = samFileIn.readLines().iterator();
		for (SamRecord samRecordOut : samFileOut.readLines()) {
			SamRecord samRecordIn = itSamIn.next();
			Assert.assertEquals(samRecordOut.toString(), samRecordIn.toString());
		}
		
		FileOperate.deleteFileFolder(outFile);
	}
	
	/**
	 * @param isChr true：返回chr1,chr2，false 返回 1,2
	 * @return
	 */
	private SAMSequenceDictionary getSeqDict(boolean isChr) {
		SAMSequenceDictionary dict = SeqHash.getDictionaryFromFai("src/test/resources/test_file/reference/arabidopsis/chrAll.fa.fai");
		if (!isChr) {
			SAMSequenceDictionary dictWithoutChr = new SAMSequenceDictionary();
			for (SAMSequenceRecord record : dict.getSequences()) {
				SAMSequenceRecord recordNew = new SAMSequenceRecord(record.getSequenceName().replace("chr", ""), record.getSequenceLength());
				dictWithoutChr.addSequence(recordNew);
			}
			dict = dictWithoutChr;
		}
		return dict;
	}

//	@Test
	public void testSamChangeChrIdBam() throws IOException {
		String outFileName = runReorderAndGetResult(true);
		SamFile samFileIn = new SamFile(inFile);
		SamFile samFileOut = new SamFile(outFileName);
		Map<String, Long> mapChrId2Len = samFileOut.getMapChrID2Length();
		assertEquals(7, mapChrId2Len.size());
		assertEquals(true, mapChrId2Len.containsKey("3"));
		assertEquals(false, mapChrId2Len.containsKey("chr3"));
		Iterator<SamRecord> itorOut = samFileOut.readLines().iterator();
		Iterator<SamRecord> itorIn = samFileIn.readLines().iterator();
		while (itorIn.hasNext()) {
			SamRecord samIn = itorIn.next();
			SamRecord samOut = itorOut.next();
			assertEquals(samOut.getStartAbs(), samIn.getStartAbs());
			assertEquals(samIn.getSeqFasta().toString(), samOut.getSeqFasta().toString());
			
			assertEquals(samIn.getChrId().replace("chr", ""), samOut.getChrId());
		}

		FileOperate.deleteFileFolder(outFileName);
	}
	
	/** 去除染色体名字中的chr，并重新输出为sam文件 */
	private String runReorderAndGetResult(boolean isBam) {
		SamFile samFile = new SamFile(inFile);
		Map<String, Long> mapChrId2Len = samFile.getMapChrID2Length();
		assertEquals(7, mapChrId2Len.size());
		assertEquals(false, mapChrId2Len.containsKey("3"));
		assertEquals(true, mapChrId2Len.containsKey("chr3"));
		
		SamToBam samToBam = new SamToBam();
		samToBam.setIsPairend(true);
		//TODO 考虑写一个配置文件来保存测试文件的路径
		try {
			samToBam.setInStream(FileOperate.getInputStream(inFile));
		} catch (Exception e) {
		}
		
		SamReorder samReorder = new SamReorder();
		Map<String, String> mapChrIdOld2New = new HashMap<>();
		for (String chrId : mapChrId2Len.keySet()) {
			mapChrIdOld2New.put(chrId, chrId.replace("chr", ""));
		}
		samReorder.setMapChrIdOld2New(mapChrIdOld2New);
		samToBam.setSamReorder(samReorder);
		
		SamToBamOutFile samwritFile = new SamToBamOutFile();
		String outputTest = "src/test/resources/test_file/sam/test_cope";
		outputTest += isBam? ".bam" : ".sam";
		
		samwritFile.setOutFileName(outputTest);
		samToBam.setSamWriteTo(samwritFile);
		
		samToBam.readInputStream();
		samToBam.writeToOs();
		samToBam.finish();
		return outputTest;
	}
	
	@Test
	public void testReorderAndChangeChrIdSamReorder() {
		SamFile samFileIn = new SamFile(inFile);
		String fileOut = FileOperate.changeFileSuffix(inFile, "_reorder", null);
		FileOperate.deleteFileFolder(fileOut);
		Map<String, Long> mapChrId2Len = samFileIn.getMapChrID2Length();
		
		SamReorder samReorder = new SamReorder();
		Map<String, String> mapChrIdOld2New = new HashMap<>();
		for (String chrId : mapChrId2Len.keySet()) {
			mapChrIdOld2New.put(chrId, chrId.replace("chr", ""));
		}
		samReorder.setMapChrIdOld2New(mapChrIdOld2New);
		//获得拟南芥的标准排序 */
		samReorder.setSamSequenceDictionary(getSeqDict(false));
		samReorder.setSamFileHeader(samFileIn.getHeader());
		samReorder.reorder();
		
		SamFile samFileOut = samReorder.reorderSam(samFileIn, fileOut);
		List<SAMSequenceRecord> lsChrIds = samFileOut.getHeader().getSequenceDictionary().getSequences();
		int i = 0;
		for (SAMSequenceRecord samSequenceRecord : lsChrIds) {
			if (i++ > 5) break;
			samSequenceRecord.getSequenceName().equals(i);
		}
		Iterator<SamRecord> itorOut = samFileOut.readLines().iterator();
		Iterator<SamRecord> itorIn = samFileIn.readLines().iterator();
		while (itorIn.hasNext()) {
			SamRecord samIn = itorIn.next();
			SamRecord samOut = itorOut.next();
			assertEquals(samOut.getStartAbs(), samIn.getStartAbs());
			assertEquals(samIn.getSeqFasta().toString(), samOut.getSeqFasta().toString());
			assertEquals(samIn.isMapped(), samOut.isMapped());

			if (samIn.isMapped()) {
				assertEquals(samIn.getChrId().replace("chr", ""), samOut.getChrId());
			}
		}
		
		FileOperate.deleteFileFolder(samFileOut.getFileName());
	}
}
