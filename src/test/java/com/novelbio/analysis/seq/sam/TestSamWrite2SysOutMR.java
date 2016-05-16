package com.novelbio.analysis.seq.sam;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import com.novelbio.analysis.seq.sam.SamToBam.SamToBamOutMR;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

public class TestSamWrite2SysOutMR {
	
	@Test
	public void testInStreamNumber() throws Exception {
		SamToBam sam2bam = new SamToBam();
		sam2bam.setIsPairend(true);
		//TODO 写一个配置文件来保存测试文件的路径
		sam2bam.setInStream(FileOperate.getInputStream("src/test/resources/test_file/sam/test.sam"));
		sam2bam.readInputStream();
		SamToBamOutMR samWrite2SysOutMR = new SamToBamOutMR();
		
		String outputTest = "src/test/resources/test_file/sam/test_cope.sam";
		samWrite2SysOutMR.setOutputStream(FileOperate.getOutputStream(outputTest));
		sam2bam.setSamWriteTo(samWrite2SysOutMR);
		
		sam2bam.writeToOs();
		
		List<String> lsRecord = TxtReadandWrite.readfileLs(outputTest);
		Iterator<String> itor = lsRecord.iterator();

		assertEquals("@HD	VN:1.4", itor.next());
		assertEquals("@SQ	SN:chr4	LN:18585056", itor.next());
		assertEquals("@SQ	SN:chr2	LN:19698289", itor.next());
		assertEquals("@SQ	SN:chr1	LN:30427671", itor.next());
		assertEquals("@SQ	SN:chr3	LN:23459830", itor.next());
		assertEquals("@SQ	SN:chr5	LN:26975502", itor.next());
		assertEquals("@SQ	SN:chrc	LN:154478", itor.next());
		assertEquals("@SQ	SN:chrm	LN:366924", itor.next());
		assertEquals("@PG	ID:bwa	PN:bwa	VN:0.7.8-r455	CL:bwa mem -p -t 15 /media/nbfs/nbCloud/public/nbcplatform/genome/index/bwa/3702/tair10/Chr_Index/chrAll.fa /media/nbfs//nbCloud/public/AllProject/project_54d9c2eee4b05fbe04b74aa6/task_54d9e385e4b05fbe04b74abd/QualityControl_result/testout96_filtered33.fq", itor.next());
		assertEquals("m0000006_@_chrc_@_000000000023104_@_HWI-D00175:261:C6L59ANXX:7:1101:1181:2181\t" + "HWI-D00175:261:C6L59ANXX:7:1101:1181:2181	83	chrc	23104	42	75S50M	=	22664	-490	AACGAGTCCAATTTGAAGTTGTTGATGTTTATATTGGTCAATCATAAAATAGAAATAAGAAAAGAATTTATATTTATTCCGATCAAACTTCTTCCCTATTAACCTGGAAGTTCTTCTGAGATACA	FGGGGGFEEGGFGGGGGGGGGGGGGGGGGGGGEGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGCCBCB	MD:Z:8T7C1T8T6A7C7	NH:i:1	HI:i:1	NM:i:6	AS:i:20	XS:i:0", itor.next());
		assertEquals("m0000006_@_chrc_@_000000000022664_@_HWI-D00175:261:C6L59ANXX:7:1101:1181:2181\t" + "HWI-D00175:261:C6L59ANXX:7:1101:1181:2181	163	chrc	22664	60	125M	=	23104	490	CTAGGCCCTCCAATTTCTTAAGGGGTTTATCTAAAAGATTCGCGATATAACTAGGAAGACCTTTTAAATACCACACATGAGTCACGGGACATGCGAGTTTGATGTATCCCATTTGATATCTTCGT	BBCBCGGGGGGGGGGGGGGGGGCGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGDGGGGGFGGGGGGGGGGGFGGGGGGGGGGGGGGGGGEG	MD:Z:7T2T4C0T5A20A16G3C8T8T2A7T0C5T24	NH:i:1	HI:i:1	NM:i:14	AS:i:58	XS:i:0", itor.next());
		assertEquals("m0000004_@_chr3_@_000000014197997_@_HWI-D00175:261:C6L59ANXX:7:1101:8758:67282	HWI-D00175:261:C6L59ANXX:7:1101:8758:67282	99	chr3	14197997	0	125M	=	14198376	439	CTTTCGATGGTAGGATAGGGGCCTACCATGGTGGTGACGGGTGACGGAGAATTAGGGTTCGATTCCGGAGAGGGAGCCTGAGAAACGGCTACCACATCCAAGGAAGGCAGCAGGCGCGCAAATTA	CCCCCGEFGGGGGGGGGGGFGGGGGGGEGGGGGGGGFGDGGGGGGGGFGGGGGGGGGB>>CBFFFGGGECBFGGGG@GBFBGGD0FGGGGGGEGGG=DFGGGGGGGGGDGC@8EGGGGDGGGGBG	MD:Z:18T16A89	NH:i:1	HI:i:1	NM:i:2	AS:i:115	XS:i:115", itor.next());
		
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
}
