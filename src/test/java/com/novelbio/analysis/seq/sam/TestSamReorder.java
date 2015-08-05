package com.novelbio.analysis.seq.sam;

import static org.junit.Assert.assertEquals;
import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.SAMSequenceRecord;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.analysis.seq.sam.SamToBam.SamToBamOutFile;
import com.novelbio.analysis.seq.sam.SamToBam.SamToBamOutMR;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

public class TestSamReorder {
	@Test
	public void testSamReorder() {
		String inFile = "src/test/resources/test_file/sam/test.sam";
		String outFile = "src/test/resources/test_file/sam/test_sort_reorder.sam";
		
		SamFile samFileIn = new SamFile(inFile);
		Assert.assertEquals("chr4", samFileIn.getMapChrID2Length().keySet().iterator().next());
		SamToBamSort samToBamSort = new SamToBamSort(outFile, samFileIn);
		samToBamSort.setAddMultiHitFlag(true);
		samToBamSort.setNeedSort(true);
		samToBamSort.setSamSequenceDictionary(getSeqDict(true));
		samToBamSort.convert();
	
		SamFile samFile = samToBamSort.getSamFileBam();
		Assert.assertEquals("chr1", samFile.getMapChrID2Length().keySet().iterator().next());
		Assert.assertEquals(true, SamFile.isSorted(samFile));
		
		int i = 0;
		for (SamRecord samRecord : samFile.readLines()) {
			if (i++ < 2) {
				Assert.assertEquals("chr3", samRecord.getRefID());
			} else {
				Assert.assertEquals("chrc", samRecord.getRefID());
			}
		}
		
		FileOperate.DeleteFileFolder(outFile);
	}
	
	/**
	 * @param isChr true：返回chr1,chr2，false 返回 1,2
	 * @return
	 */
	private SAMSequenceDictionary getSeqDict(boolean isChr) {
		SeqHash seqHash = new SeqHash("/hdfs:/nbCloud/public/nbcplatform/genome/species/3702/tair10/ChromFa/chrAll.fa");
		seqHash.close();
		SAMSequenceDictionary dict = seqHash.getDictionary();
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

	@Test
	public void testSamChangeChrIdBam() throws IOException {
		String outFileName = runReorderAndGetResult(true);

		SamFile samFile = new SamFile(outFileName);
		Map<String, Long> mapChrId2Len = samFile.getMapChrID2Length();
		assertEquals(7, mapChrId2Len.size());
		assertEquals(true, mapChrId2Len.containsKey("3"));
		assertEquals(false, mapChrId2Len.containsKey("chr3"));
		Iterator<SamRecord> itor = samFile.readLines().iterator();

		assertEquals("HWI-D00175:261:C6L59ANXX:7:1101:1181:2181	83	chrc	23104	42	75S50M	=	22664	-490	AACGAGTCCAATTTGAAGTTGTTGATGTTTATATTGGTCAATCATAAAATAGAAATAAGAAAAGAATTTATATTTATTCCGATCAAACTTCTTCCCTATTAACCTGGAAGTTCTTCTGAGATACA	FGGGGGFEEGGFGGGGGGGGGGGGGGGGGGGGEGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGCCBCB	MD:Z:8T7C1T8T6A7C7	NH:i:1	HI:i:1	NM:i:6	AS:i:20	XS:i:0", itor.next().toString());
		assertEquals("HWI-D00175:261:C6L59ANXX:7:1101:1181:2181	163	chrc	22664	60	125M	=	23104	490	CTAGGCCCTCCAATTTCTTAAGGGGTTTATCTAAAAGATTCGCGATATAACTAGGAAGACCTTTTAAATACCACACATGAGTCACGGGACATGCGAGTTTGATGTATCCCATTTGATATCTTCGT	BBCBCGGGGGGGGGGGGGGGGGCGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGDGGGGGFGGGGGGGGGGGFGGGGGGGGGGGGGGGGGEG	MD:Z:7T2T4C0T5A20A16G3C8T8T2A7T0C5T24	NH:i:1	HI:i:1	NM:i:14	AS:i:58	XS:i:0", itor.next().toString());
		assertEquals("HWI-D00175:261:C6L59ANXX:7:1101:8758:67282	99	3	14197997	0	125M	=	14198376	439	CTTTCGATGGTAGGATAGGGGCCTACCATGGTGGTGACGGGTGACGGAGAATTAGGGTTCGATTCCGGAGAGGGAGCCTGAGAAACGGCTACCACATCCAAGGAAGGCAGCAGGCGCGCAAATTA	CCCCCGEFGGGGGGGGGGGFGGGGGGGEGGGGGGGGFGDGGGGGGGGFGGGGGGGGGB>>CBFFFGGGECBFGGGG@GBFBGGD0FGGGGGGEGGG=DFGGGGGGGGGDGC@8EGGGGDGGGGBG	MD:Z:18T16A89	NH:i:1	HI:i:1	NM:i:2	AS:i:115	XS:i:115", itor.next().toString());

		FileOperate.DeleteFileFolder(outFileName);
	}
	
	@Test
	public void testSamChangeChrIdSam() throws IOException {
		String outFileName = runReorderAndGetResult(false);
		TxtReadandWrite txtRead = new TxtReadandWrite(outFileName);
		List<String> lsRecord = txtRead.readfileLs();
		txtRead.close();
		Iterator<String> itor = lsRecord.iterator();

		assertEquals("@HD	VN:1.5	SO:unsorted", itor.next());
		assertEquals("@SQ	SN:4	LN:18585056", itor.next());
		assertEquals("@SQ	SN:2	LN:19698289", itor.next());
		assertEquals("@SQ	SN:1	LN:30427671", itor.next());
		assertEquals("@SQ	SN:3	LN:23459830", itor.next());
		assertEquals("@SQ	SN:5	LN:26975502", itor.next());
		assertEquals("@SQ	SN:chrc	LN:154478", itor.next());
		assertEquals("@SQ	SN:chrm	LN:366924", itor.next());
		assertEquals("@PG	ID:bwa	PN:bwa	VN:0.7.8-r455	CL:bwa mem -p -t 15 /media/nbfs/nbCloud/public/nbcplatform/genome/index/bwa/3702/tair10/Chr_Index/chrAll.fa /media/nbfs//nbCloud/public/AllProject/project_54d9c2eee4b05fbe04b74aa6/task_54d9e385e4b05fbe04b74abd/QualityControl_result/testout96_filtered33.fq", itor.next());
		assertEquals("HWI-D00175:261:C6L59ANXX:7:1101:1181:2181	83	chrc	23104	42	75S50M	=	22664	-490	AACGAGTCCAATTTGAAGTTGTTGATGTTTATATTGGTCAATCATAAAATAGAAATAAGAAAAGAATTTATATTTATTCCGATCAAACTTCTTCCCTATTAACCTGGAAGTTCTTCTGAGATACA	FGGGGGFEEGGFGGGGGGGGGGGGGGGGGGGGEGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGCCBCB	MD:Z:8T7C1T8T6A7C7	NH:i:1	HI:i:1	NM:i:6	AS:i:20	XS:i:0", itor.next());
		assertEquals("HWI-D00175:261:C6L59ANXX:7:1101:1181:2181	163	chrc	22664	60	125M	=	23104	490	CTAGGCCCTCCAATTTCTTAAGGGGTTTATCTAAAAGATTCGCGATATAACTAGGAAGACCTTTTAAATACCACACATGAGTCACGGGACATGCGAGTTTGATGTATCCCATTTGATATCTTCGT	BBCBCGGGGGGGGGGGGGGGGGCGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGDGGGGGFGGGGGGGGGGGFGGGGGGGGGGGGGGGGGEG	MD:Z:7T2T4C0T5A20A16G3C8T8T2A7T0C5T24	NH:i:1	HI:i:1	NM:i:14	AS:i:58	XS:i:0", itor.next());
		assertEquals("HWI-D00175:261:C6L59ANXX:7:1101:8758:67282	99	3	14197997	0	125M	=	14198376	439	CTTTCGATGGTAGGATAGGGGCCTACCATGGTGGTGACGGGTGACGGAGAATTAGGGTTCGATTCCGGAGAGGGAGCCTGAGAAACGGCTACCACATCCAAGGAAGGCAGCAGGCGCGCAAATTA	CCCCCGEFGGGGGGGGGGGFGGGGGGGEGGGGGGGGFGDGGGGGGGGFGGGGGGGGGB>>CBFFFGGGECBFGGGG@GBFBGGD0FGGGGGGEGGG=DFGGGGGGGGGDGC@8EGGGGDGGGGBG	MD:Z:18T16A89	NH:i:1	HI:i:1	NM:i:2	AS:i:115	XS:i:115", itor.next());
		
		//测试record数量
		int i = 0;
		for (String string : lsRecord) {
			if (string.startsWith("@")) {
				continue;
			}
			i++;
		}
		assertEquals(17, i);
		FileOperate.DeleteFileFolder(outFileName);
	}
	
	private String runReorderAndGetResult(boolean isBam) {
		SamFile samFile = new SamFile("src/test/resources/test_file/sam/test.sam");
		Map<String, Long> mapChrId2Len = samFile.getMapChrID2Length();
		assertEquals(7, mapChrId2Len.size());
		assertEquals(false, mapChrId2Len.containsKey("3"));
		assertEquals(true, mapChrId2Len.containsKey("chr3"));
		
		SamToBam samToBam = new SamToBam();
		samToBam.setIsPairend(true);
		//TODO 写一个配置文件来保存测试文件的路径
		try {
			samToBam.setInStream(FileOperate.getInputStream("src/test/resources/test_file/sam/test.sam"));
		} catch (Exception e) {
		}
		
		SamReorder samReorder = new SamReorder();
		Map<String, String> mapChrIdOld2New = new HashMap<>();
		mapChrIdOld2New.put("chr1", "1");
		mapChrIdOld2New.put("chr2", "2");
		mapChrIdOld2New.put("chr3", "3");
		mapChrIdOld2New.put("chr4", "4");
		mapChrIdOld2New.put("chr5", "5");
		samReorder.setMapChrIdOld2New(mapChrIdOld2New);
		samToBam.setSamReorder(samReorder);
		
		SamToBamOutFile samwritFile = new SamToBamOutFile();
		String outputTest = "src/test/resources/test_file/sam/test_cope";
		outputTest += isBam? ".bam" : ".sam";
		
		samwritFile.setOutFileName(outputTest);
		samToBam.setSamWriteTo(samwritFile);
		
		samToBam.readInputStream();
		samToBam.writeToOs();
		return outputTest;
	}
	
	@Test
	public void testReorderAndChangeChrId() {
		SamFile samFile = new SamFile("src/test/resources/test_file/sam/test.sam");
		Map<String, Long> mapChrId2Len = samFile.getMapChrID2Length();
		assertEquals(7, mapChrId2Len.size());
		assertEquals(false, mapChrId2Len.containsKey("3"));
		assertEquals(true, mapChrId2Len.containsKey("chr3"));
		
		SamToBam samToBam = new SamToBam();
		samToBam.setIsPairend(true);
		//TODO 写一个配置文件来保存测试文件的路径
		try {
			samToBam.setInStream(FileOperate.getInputStream("src/test/resources/test_file/sam/test.sam"));
		} catch (Exception e) {
		}
		
		SamReorder samReorder = new SamReorder();
		Map<String, String> mapChrIdOld2New = new HashMap<>();
		mapChrIdOld2New.put("chr1", "1");
		mapChrIdOld2New.put("chr2", "2");
		mapChrIdOld2New.put("chr3", "3");
		mapChrIdOld2New.put("chr4", "4");
		mapChrIdOld2New.put("chr5", "5");
		mapChrIdOld2New.put("chrc", "c");
		mapChrIdOld2New.put("chrm", "m");
		samReorder.setMapChrIdOld2New(mapChrIdOld2New);
		samReorder.setSamSequenceDictionary(getSeqDict(false));

		samToBam.setSamReorder(samReorder);
		
		SamToBamOutFile samwritFile = new SamToBamOutFile();
		String outputTest = "src/test/resources/test_file/sam/test_cope.sam";
		
		samwritFile.setOutFileName(outputTest);
		samToBam.setSamWriteTo(samwritFile);
		
		samToBam.readInputStream();
		samToBam.writeToOs();
		
		TxtReadandWrite txtRead = new TxtReadandWrite(outputTest);
		List<String> lsRecord = txtRead.readfileLs();
		txtRead.close();
		Iterator<String> itor = lsRecord.iterator();

		assertEquals("@HD	VN:1.5	SO:unsorted", itor.next());
		assertEquals("@SQ	SN:1	LN:30427671", itor.next());
		assertEquals("@SQ	SN:2	LN:19698289", itor.next());
		assertEquals("@SQ	SN:3	LN:23459830", itor.next());
		assertEquals("@SQ	SN:4	LN:18585056", itor.next());
		assertEquals("@SQ	SN:5	LN:26975502", itor.next());
		assertEquals("@SQ	SN:c	LN:154478", itor.next());
		assertEquals("@SQ	SN:m	LN:366924", itor.next());
		assertEquals("@PG	ID:bwa	PN:bwa	VN:0.7.8-r455	CL:bwa mem -p -t 15 /media/nbfs/nbCloud/public/nbcplatform/genome/index/bwa/3702/tair10/Chr_Index/chrAll.fa /media/nbfs//nbCloud/public/AllProject/project_54d9c2eee4b05fbe04b74aa6/task_54d9e385e4b05fbe04b74abd/QualityControl_result/testout96_filtered33.fq", itor.next());
		assertEquals("HWI-D00175:261:C6L59ANXX:7:1101:1181:2181	83	c	23104	42	75S50M	=	22664	-490	AACGAGTCCAATTTGAAGTTGTTGATGTTTATATTGGTCAATCATAAAATAGAAATAAGAAAAGAATTTATATTTATTCCGATCAAACTTCTTCCCTATTAACCTGGAAGTTCTTCTGAGATACA	FGGGGGFEEGGFGGGGGGGGGGGGGGGGGGGGEGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGCCBCB	MD:Z:8T7C1T8T6A7C7	NH:i:1	HI:i:1	NM:i:6	AS:i:20	XS:i:0", itor.next());
		assertEquals("HWI-D00175:261:C6L59ANXX:7:1101:1181:2181	163	c	22664	60	125M	=	23104	490	CTAGGCCCTCCAATTTCTTAAGGGGTTTATCTAAAAGATTCGCGATATAACTAGGAAGACCTTTTAAATACCACACATGAGTCACGGGACATGCGAGTTTGATGTATCCCATTTGATATCTTCGT	BBCBCGGGGGGGGGGGGGGGGGCGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGDGGGGGFGGGGGGGGGGGFGGGGGGGGGGGGGGGGGEG	MD:Z:7T2T4C0T5A20A16G3C8T8T2A7T0C5T24	NH:i:1	HI:i:1	NM:i:14	AS:i:58	XS:i:0", itor.next());
		assertEquals("HWI-D00175:261:C6L59ANXX:7:1101:8758:67282	99	3	14197997	0	125M	=	14198376	439	CTTTCGATGGTAGGATAGGGGCCTACCATGGTGGTGACGGGTGACGGAGAATTAGGGTTCGATTCCGGAGAGGGAGCCTGAGAAACGGCTACCACATCCAAGGAAGGCAGCAGGCGCGCAAATTA	CCCCCGEFGGGGGGGGGGGFGGGGGGGEGGGGGGGGFGDGGGGGGGGFGGGGGGGGGB>>CBFFFGGGECBFGGGG@GBFBGGD0FGGGGGGEGGG=DFGGGGGGGGGDGC@8EGGGGDGGGGBG	MD:Z:18T16A89	NH:i:1	HI:i:1	NM:i:2	AS:i:115	XS:i:115", itor.next());
		
		//测试record数量
		int i = 0;
		for (String string : lsRecord) {
			if (string.startsWith("@")) {
				continue;
			}
			i++;
		}
		assertEquals(17, i);
		FileOperate.DeleteFileFolder(outputTest);
	}
	
	@Test
	public void testReorderAndChangeChrIdSamReorder() {
		SamFile samFile = new SamFile("src/test/resources/test_file/sam/test.sam");
		Map<String, Long> mapChrId2Len = samFile.getMapChrID2Length();
		assertEquals(7, mapChrId2Len.size());
		assertEquals(false, mapChrId2Len.containsKey("3"));
		assertEquals(true, mapChrId2Len.containsKey("chr3"));
		
		
		SamReorder samReorder = new SamReorder();
		Map<String, String> mapChrIdOld2New = new HashMap<>();
		mapChrIdOld2New.put("chr1", "1");
		mapChrIdOld2New.put("chr2", "2");
		mapChrIdOld2New.put("chr3", "3");
		mapChrIdOld2New.put("chr4", "4");
		mapChrIdOld2New.put("chr5", "5");
		mapChrIdOld2New.put("chrc", "c");
		mapChrIdOld2New.put("chrm", "m");
		samReorder.setMapChrIdOld2New(mapChrIdOld2New);
		samReorder.setSamSequenceDictionary(getSeqDict(false));
		samReorder.setSamFileHeader(samFile.getHeader());
		samReorder.reorder();
		SamFile samFileReorder = samReorder.reorderSam(samFile);
		TxtReadandWrite txtRead = new TxtReadandWrite(samFileReorder.getFileName());
		
		List<String> lsRecord = txtRead.readfileLs();
		txtRead.close();
		Iterator<String> itor = lsRecord.iterator();

		assertEquals("@HD	VN:1.5	SO:unsorted", itor.next());
		assertEquals("@SQ	SN:1	LN:30427671", itor.next());
		assertEquals("@SQ	SN:2	LN:19698289", itor.next());
		assertEquals("@SQ	SN:3	LN:23459830", itor.next());
		assertEquals("@SQ	SN:4	LN:18585056", itor.next());
		assertEquals("@SQ	SN:5	LN:26975502", itor.next());
		assertEquals("@SQ	SN:c	LN:154478", itor.next());
		assertEquals("@SQ	SN:m	LN:366924", itor.next());
		assertEquals("@PG	ID:bwa	PN:bwa	VN:0.7.8-r455	CL:bwa mem -p -t 15 /media/nbfs/nbCloud/public/nbcplatform/genome/index/bwa/3702/tair10/Chr_Index/chrAll.fa /media/nbfs//nbCloud/public/AllProject/project_54d9c2eee4b05fbe04b74aa6/task_54d9e385e4b05fbe04b74abd/QualityControl_result/testout96_filtered33.fq", itor.next());
		assertEquals("HWI-D00175:261:C6L59ANXX:7:1101:1181:2181	83	c	23104	42	75S50M	=	22664	-490	AACGAGTCCAATTTGAAGTTGTTGATGTTTATATTGGTCAATCATAAAATAGAAATAAGAAAAGAATTTATATTTATTCCGATCAAACTTCTTCCCTATTAACCTGGAAGTTCTTCTGAGATACA	FGGGGGFEEGGFGGGGGGGGGGGGGGGGGGGGEGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGCCBCB	MD:Z:8T7C1T8T6A7C7	NM:i:6	AS:i:20	XS:i:0", itor.next());
		assertEquals("HWI-D00175:261:C6L59ANXX:7:1101:1181:2181	163	c	22664	60	125M	=	23104	490	CTAGGCCCTCCAATTTCTTAAGGGGTTTATCTAAAAGATTCGCGATATAACTAGGAAGACCTTTTAAATACCACACATGAGTCACGGGACATGCGAGTTTGATGTATCCCATTTGATATCTTCGT	BBCBCGGGGGGGGGGGGGGGGGCGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGDGGGGGFGGGGGGGGGGGFGGGGGGGGGGGGGGGGGEG	MD:Z:7T2T4C0T5A20A16G3C8T8T2A7T0C5T24	NM:i:14	AS:i:58	XS:i:0", itor.next());
		assertEquals("HWI-D00175:261:C6L59ANXX:7:1101:8758:67282	99	3	14197997	0	125M	=	14198376	439	CTTTCGATGGTAGGATAGGGGCCTACCATGGTGGTGACGGGTGACGGAGAATTAGGGTTCGATTCCGGAGAGGGAGCCTGAGAAACGGCTACCACATCCAAGGAAGGCAGCAGGCGCGCAAATTA	CCCCCGEFGGGGGGGGGGGFGGGGGGGEGGGGGGGGFGDGGGGGGGGFGGGGGGGGGB>>CBFFFGGGECBFGGGG@GBFBGGD0FGGGGGGEGGG=DFGGGGGGGGGDGC@8EGGGGDGGGGBG	MD:Z:18T16A89	NM:i:2	AS:i:115	XS:i:115", itor.next());
		
		//测试record数量
		int i = 0;
		for (String string : lsRecord) {
			if (string.startsWith("@")) {
				continue;
			}
			i++;
		}
		assertEquals(17, i);
		FileOperate.DeleteFileFolder(samFileReorder.getFileName());
	}
}
