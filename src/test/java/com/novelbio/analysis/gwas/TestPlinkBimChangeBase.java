package com.novelbio.analysis.gwas;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.novelbio.base.PathDetail;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

public class TestPlinkBimChangeBase {
	String reference = "src/test/resources/test_file/reference/testTrinity.fa";
	String contigName = "Contig6";
	
	@Test
	public void testAddAnno() {
		String plinkBim = PathDetail.getTmpPathWithSep() + "plinkBim.bim";
		String outFile = plinkBim + ".ref";

		TxtReadandWrite txtWrite = new TxtReadandWrite(plinkBim, true);
		txtWrite.writefileln("Contig6\tnovelbio\t0\t146\tG\tT");//T
		txtWrite.writefileln("Contig6\tnovelbio\t0\t147\tG\tA");//G
		txtWrite.writefileln("Contig6\tnovelbio\t0\t148\tT\tA");//A
		txtWrite.writefileln("Contig6\tnovelbio\t0\t179\tT\tA");//T
		txtWrite.writefileln("Contig6\tnovelbio\t0\t180\tC\tT");//T
		txtWrite.writefileln("Contig6\tnovelbio\t0\t181\tG\tA");//G
		txtWrite.writefileln("Contig6\tnovelbio\t0\t182\tA\tG");//A
		txtWrite.close();

		PlinkBimChangeBase plinkMapAddBase = new PlinkBimChangeBase(reference);
		plinkMapAddBase.addAnnoFromRef(plinkBim, outFile);
		
		TxtReadandWrite txtRead = new TxtReadandWrite(outFile);
		List<String> lsAddRef = txtRead.readfileLs();
		Assert.assertEquals("Contig6\tnovelbio\t0\t146\tT\tG\t1", lsAddRef.get(0));
		Assert.assertEquals("Contig6\tnovelbio\t0\t147\tG\tA\t-1", lsAddRef.get(1));
		Assert.assertEquals("Contig6\tnovelbio\t0\t148\tA\tT\t1", lsAddRef.get(2));
		Assert.assertEquals("Contig6\tnovelbio\t0\t179\tT\tA\t-1", lsAddRef.get(3));
		Assert.assertEquals("Contig6\tnovelbio\t0\t180\tT\tC\t1", lsAddRef.get(4));
		Assert.assertEquals("Contig6\tnovelbio\t0\t181\tG\tA\t-1", lsAddRef.get(5));
		Assert.assertEquals("Contig6\tnovelbio\t0\t182\tA\tG\t-1", lsAddRef.get(6));
		txtRead.close();
		
		FileOperate.deleteFileFolder(plinkBim);
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
