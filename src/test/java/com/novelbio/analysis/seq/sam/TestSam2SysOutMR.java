package com.novelbio.analysis.seq.sam;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Iterator;

import org.junit.Test;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

public class TestSam2SysOutMR {
	@Test
	public void testInStream() throws Exception {
		Sam2SysOutMR sam2SysOutMR = new Sam2SysOutMR();
		sam2SysOutMR.setIsPairend(true);
		sam2SysOutMR.setInStream(FileOperate.getInputStream("src/main/resources/Test/test.sam"));
		sam2SysOutMR.readInputStream();
		Iterable<String> it = sam2SysOutMR.readLines();
		Iterator<String> itor = it.iterator();
		
		assertEquals("@SQ	SN:chr1	LN:30427671", itor.next());
		assertEquals("@SQ	SN:chr2	LN:19698289", itor.next());
		assertEquals("@SQ	SN:chr3	LN:23459830", itor.next());
		assertEquals("@SQ	SN:chr4	LN:18585056", itor.next());
		assertEquals("@SQ	SN:chr5	LN:26975502", itor.next());
		assertEquals("@SQ	SN:chrc	LN:154478", itor.next());
		assertEquals("@SQ	SN:chrm	LN:366924", itor.next());
		assertEquals("@PG	ID:bwa	PN:bwa	VN:0.7.8-r455	CL:bwa mem -p -t 15 /media/nbfs/nbCloud/public/nbcplatform/genome/index/bwa/3702/tair10/Chr_Index/chrAll.fa /media/nbfs//nbCloud/public/AllProject/project_54d9c2eee4b05fbe04b74aa6/task_54d9e385e4b05fbe04b74abd/QualityControl_result/testout96_filtered33.fq", itor.next());
		assertEquals("mchrc_@_23104_@_HWI-D00175:261:C6L59ANXX:7:1101:1181:2181\t" + "HWI-D00175:261:C6L59ANXX:7:1101:1181:2181	83	chrc	23104	42	75S50M	=	22664	-490	AACGAGTCCAATTTGAAGTTGTTGATGTTTATATTGGTCAATCATAAAATAGAAATAAGAAAAGAATTTATATTTATTCCGATCAAACTTCTTCCCTATTAACCTGGAAGTTCTTCTGAGATACA	FGGGGGFEEGGFGGGGGGGGGGGGGGGGGGGGEGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGCCBCB	MD:Z:8T7C1T8T6A7C7	NH:i:1	HI:i:1	NM:i:6	AS:i:20	XS:i:0", itor.next());
		assertEquals("mchrc_@_22664_@_HWI-D00175:261:C6L59ANXX:7:1101:1181:2181\t" + "HWI-D00175:261:C6L59ANXX:7:1101:1181:2181	163	chrc	22664	60	125M	=	23104	490	CTAGGCCCTCCAATTTCTTAAGGGGTTTATCTAAAAGATTCGCGATATAACTAGGAAGACCTTTTAAATACCACACATGAGTCACGGGACATGCGAGTTTGATGTATCCCATTTGATATCTTCGT	BBCBCGGGGGGGGGGGGGGGGGCGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGDGGGGGFGGGGGGGGGGGFGGGGGGGGGGGGGGGGGEG	MD:Z:7T2T4C0T5A20A16G3C8T8T2A7T0C5T24	NH:i:1	HI:i:1	NM:i:14	AS:i:58	XS:i:0", itor.next());
		assertEquals("mchr3_@_14197997_@_HWI-D00175:261:C6L59ANXX:7:1101:8758:67282	HWI-D00175:261:C6L59ANXX:7:1101:8758:67282	99	chr3	14197997	0	125M	=	14198376	439	CTTTCGATGGTAGGATAGGGGCCTACCATGGTGGTGACGGGTGACGGAGAATTAGGGTTCGATTCCGGAGAGGGAGCCTGAGAAACGGCTACCACATCCAAGGAAGGCAGCAGGCGCGCAAATTA	CCCCCGEFGGGGGGGGGGGFGGGGGGGEGGGGGGGGFGDGGGGGGGGFGGGGGGGGGB>>CBFFFGGGECBFGGGG@GBFBGGD0FGGGGGGEGGG=DFGGGGGGGGGDGC@8EGGGGDGGGGBG	MD:Z:18T16A89	NH:i:1	HI:i:1	NM:i:2	AS:i:115	XS:i:115", itor.next());
	}
	
	@Test
	public void testInStreamNumber() throws Exception {
		Sam2SysOutMR sam2SysOutMR = new Sam2SysOutMR();
		sam2SysOutMR.setIsPairend(true);
		sam2SysOutMR.setInStream(FileOperate.getInputStream("src/main/resources/Test/test.sam"));
		sam2SysOutMR.readInputStream();
		int i = 0;
		for (String content : sam2SysOutMR.readLines()) {
			if (content.startsWith("@")) {
				continue;
			}
			i++;
		}
		
		assertEquals(16, i);
	}
}
