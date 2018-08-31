package com.novelbio.bioinfo.fastq;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.bioinfo.fastq.FastQ;
import com.novelbio.bioinfo.fastq.FastQRecord;
import com.novelbio.bioinfo.fastq.RemoveBlastContaminate;


public class TestRemoveBlastContaminate extends TestCase {
	String parentPath = "/home/novelbio/下载/";
	String blastTest = parentPath + "blastn_modify.txt";
	String fastqL = parentPath + "Test1.fq";
	String fastqR = parentPath + "Test2.fq";
	
	public void testGetFqExistNameAndWriteSeqNotExistInBlast() {
		RemoveBlastContaminate remove = new RemoveBlastContaminate();
		remove.setIsWriteFastq(true);
		remove.setBlastFile(blastTest);
		remove.setFastqLeft(fastqL);
		remove.setFastqRight(fastqR);
		
		remove.initial();
		
		String seq1 = "HWI-D00552:106:C6DDEANXX:1:1101:1700:2243";
		String seq2 = "HWI-D00552:106:C6DDEANXX:1:1101:1951:2167";
		String seq3 = "HWI-D00552:106:C6DDEANXX:1:1101:1753:2173";
		assertEquals(seq1, remove.getFqExistNameAndWriteSeqNotExistInBlast(seq1));
		assertEquals(seq2, remove.getFqExistNameAndWriteSeqNotExistInBlast(seq2));
		assertEquals(seq3, remove.getFqExistNameAndWriteSeqNotExistInBlast(seq3));
	}
	
	/** 测试总体功能 */
	public void testFunction() {
		RemoveBlastContaminate remove = new RemoveBlastContaminate();
		remove.setBlastFile(blastTest);
		remove.setFastqLeft(fastqL);
		remove.setFastqRight(fastqR);
		remove.initial();
		remove.runRemove();
		
		TxtReadandWrite txtBlast = new TxtReadandWrite(blastTest);
		Set<String> setBlast = new HashSet<>();
		for (String blast : txtBlast.readlines()) {
			setBlast.add(blast.split("\t")[0]);
		}
		txtBlast.close();
		FastQ fastQL = new FastQ(fastqL);
		
		int fqRecordNum = 0;
		for (FastQRecord fastQRecord : fastQL.readlines()) {
			if (setBlast.contains(fastQRecord.getName().split(" ")[0])) {
				continue;
			}
			fqRecordNum++;
		}
		fastQL.close();
		
		int realNum = 0;
		FastQ fastqReal = new FastQ(remove.getResultFqLeft().getReadFileName());
		for (FastQRecord fq : fastqReal.readlines()) {
			realNum++;
		}
		fastqReal.close();
		assertEquals(fqRecordNum, realNum);		
	}
	
}
