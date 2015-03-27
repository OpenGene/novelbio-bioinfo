package com.novelbio.analysis.seq.fasta;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.novelbio.base.dataOperate.TxtReadandWrite;

import junit.framework.TestCase;


public class TestSeqFastaReader {
	String parentPath = "/media/nbfs/nbCloud/testJava/NBCplatform/testSeqFasta/";
	@Test
	public void testCreatSeqFasta() {
		List<String> lsSeqFasta = new ArrayList<>();
		lsSeqFasta.add(">fastaTest");
		lsSeqFasta.add("fasrterse234rfds");
		lsSeqFasta.add("nny\t\nadtdc");
		lsSeqFasta.add("");
		lsSeqFasta.add("fse aser  ser   ");
		lsSeqFasta.add("");
		
		SeqFasta seqFasta = SeqFastaReader.creatSeqFasta(lsSeqFasta);
		
		assertEquals("fastaTest", seqFasta.getSeqName());
		assertEquals("fasrterserfdsnnyadtdcfseaserser", seqFasta.toString());
	}
	
	@Test
	public void testReadFastaNum() {	
		int seqNumThis = 0;
		SeqFastaReader seqFastaReader = new SeqFastaReader(parentPath + "Sequence.fa");
		for (SeqFasta seqFasta : seqFastaReader.readlines()) {
			seqNumThis++;
		}
		assertEquals(getSeqNum(), seqNumThis);
	}
	
	@Test
	public void testReadFastaFirstFasta() {
		SeqFasta seqFastaFirst = getFirstSeqFasta();
		SeqFastaReader seqFastaReader = new SeqFastaReader(parentPath + "Sequence.fa");
		SeqFasta seqFastaFirstThis = seqFastaReader.readlines().iterator().next();
		assertEquals(seqFastaFirst.getSeqName(), seqFastaFirstThis.getSeqName());
		assertEquals(seqFastaFirst.toString(), seqFastaFirstThis.toString());
	}
	
	@Test
	public void testReadFastaLastFasta() {
		int seqNum = getSeqNum();
		SeqFasta seqFastaLast = getLastSeqFasta(seqNum);
		
		SeqFastaReader seqFastaReader = new SeqFastaReader(parentPath + "Sequence.fa");
		SeqFasta seqFastaLastThis = null;
		for (SeqFasta seqFasta : seqFastaReader.readlines()) {
			seqFastaLastThis = seqFasta;
		}
		assertEquals(seqFastaLast.getSeqName(), seqFastaLastThis.getSeqName());
		assertEquals(seqFastaLast.toString(), seqFastaLastThis.toString());
	}
	
	private int getSeqNum() {
		TxtReadandWrite txtRead = new TxtReadandWrite(parentPath + "Sequence.fa");
		int seqNumReal = 0;
		for (String content : txtRead.readlines()) {
			if (content.startsWith(">")) {
				seqNumReal++;
			}
		}
		txtRead.close();
		return seqNumReal;
	}
	
	private SeqFasta getLastSeqFasta(int seqNum) {
		SeqFasta seqFasta = new SeqFasta();
		int num = 0; boolean isLast = false;
		TxtReadandWrite txtRead = new TxtReadandWrite(parentPath + "Sequence.fa");
		for (String content : txtRead.readlines()) {
			if (content.startsWith(">")) {
				num++;
				if (num == seqNum) {
					isLast = true;
					seqFasta.setName(content.replaceFirst(">", "").trim());
					continue;
				}
			}
			
			if (isLast) {
				seqFasta.appendSeq(content);
			}
		}
		txtRead.close();
		seqFasta.appendFinish();
		return seqFasta;
	}
	
	private SeqFasta getFirstSeqFasta() {
		SeqFasta seqFasta = null;
		TxtReadandWrite txtRead = new TxtReadandWrite(parentPath + "Sequence.fa");
		for (String content : txtRead.readlines()) {
			if (content.startsWith(">")) {
				if (seqFasta == null) {
					seqFasta = new SeqFasta();
					seqFasta.setName(content.replaceFirst(">", "").trim());
					continue;
				} else {
					break;
				}
			}
			seqFasta.appendSeq(content);
		}
		seqFasta.appendFinish();
		txtRead.close();
		return seqFasta;
	}
}
