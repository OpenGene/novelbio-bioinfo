package com.novelbio.analysis.gwas;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.novelbio.base.PathDetail;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

public class TestPlinkMapAddBase {
	String reference = "src/test/resources/test_file/reference/testTrinity.fa";
	String contigName = "Contig6";
	
	@Test
	public void testAddAnno() {
		String Infile = PathDetail.getTmpPathWithSep() + "plinkMap.map";
		String InfilePlinkped = PathDetail.getTmpPathWithSep() + "plinkMap.ped";

		String outFile = Infile + ".ref";

		TxtReadandWrite txtWrite = new TxtReadandWrite(Infile, true);
		txtWrite.writefileln("Contig6\tnovelbio\t0\t146");//T
		txtWrite.writefileln("Contig6\tnovelbio\t0\t147");//G
		txtWrite.writefileln("Contig6\tnovelbio\t0\t148");//A
		txtWrite.writefileln("Contig6\tnovelbio\t0\t179");//T
		txtWrite.writefileln("Contig6\tnovelbio\t0\t180");//T
		txtWrite.writefileln("Contig6\tnovelbio\t0\t181");//G
		txtWrite.writefileln("Contig6\tnovelbio\t0\t182");//A
		txtWrite.close();
		
		txtWrite = new TxtReadandWrite(InfilePlinkped, true);
		txtWrite.writefileln("s1 s1 0 0 0 -9 G G G G A A T T T T A A A A\n");
		txtWrite.writefileln("s2 s2 0 0 0 -9 T G A A T A T A T T A A A A\n");
		txtWrite.writefileln("s3 s3 0 0 0 -9 G T G A A A T T C T A A A A\n");
		txtWrite.writefileln("s4 s4 0 0 0 -9 G G G G A A T T C C A A A A\n");
		txtWrite.writefileln("s5 s5 0 0 0 -9 G G G G A A T T C T A A A A\n");
		txtWrite.close();
		PlinkPedReader.createPlinkPedIndex(InfilePlinkped);

		PlinkMapAddBase plinkMapAddBase = new PlinkMapAddBase(reference);
		plinkMapAddBase.AddAnno(Infile, InfilePlinkped, outFile);
		
		TxtReadandWrite txtRead = new TxtReadandWrite(outFile);
		List<String> lsAddRef = txtRead.readfileLs();
		Assert.assertEquals("Contig6\tnovelbio\t0\t146\tT\tG", lsAddRef.get(0));
		Assert.assertEquals("Contig6\tnovelbio\t0\t147\tG\tA", lsAddRef.get(1));
		Assert.assertEquals("Contig6\tnovelbio\t0\t148\tA\tT", lsAddRef.get(2));
		Assert.assertEquals("Contig6\tnovelbio\t0\t179\tT\tA", lsAddRef.get(3));
		Assert.assertEquals("Contig6\tnovelbio\t0\t180\tT\tC", lsAddRef.get(4));
		Assert.assertEquals("Contig6\tnovelbio\t0\t181\tG\tA", lsAddRef.get(5));
		Assert.assertEquals("Contig6\tnovelbio\t0\t182\tA", lsAddRef.get(6));
		txtRead.close();
		
		FileOperate.deleteFileFolder(Infile);
		FileOperate.deleteFileFolder(InfilePlinkped);
		FileOperate.deleteFileFolder(outFile);
	}
	
	@Test
	public void testAlleleShortEncode() {
		AlleleShort alleleShort = AlleleShort.decode(1);
		Assert.assertEquals(false, alleleShort.isHaveAlt());
		Assert.assertEquals("A", alleleShort.getRef());
		Assert.assertEquals("", alleleShort.getAlt());
			
		alleleShort = AlleleShort.decode(12);
		Assert.assertEquals(true, alleleShort.isHaveAlt());
		Assert.assertEquals("T", alleleShort.getRef());
		Assert.assertEquals("A", alleleShort.getAlt());
	}
}
