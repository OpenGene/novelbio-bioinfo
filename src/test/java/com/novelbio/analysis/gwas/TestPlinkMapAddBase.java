package com.novelbio.analysis.gwas;

import java.util.List;

import org.junit.Test;

import com.novelbio.base.PathDetail;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

import junit.framework.Assert;

public class TestPlinkMapAddBase {
	String reference = "src/test/resources/test_file/reference/testTrinity.fa";
	String contigName = "Contig6";
	
	@Test
	public void testAddAnno() {
		String Infile = PathDetail.getTmpPathWithSep() + "plinkMap.map";
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
		
		PlinkMapAddBase plinkMapAddBase = new PlinkMapAddBase(reference);
		plinkMapAddBase.AddAnno(Infile, outFile);
		
		TxtReadandWrite txtRead = new TxtReadandWrite(outFile);
		List<String> lsAddRef = txtRead.readfileLs();
		Assert.assertEquals("Contig6\tnovelbio\t0\t146\tT", lsAddRef.get(0));
		Assert.assertEquals("Contig6\tnovelbio\t0\t147\tG", lsAddRef.get(1));
		Assert.assertEquals("Contig6\tnovelbio\t0\t148\tA", lsAddRef.get(2));
		Assert.assertEquals("Contig6\tnovelbio\t0\t179\tT", lsAddRef.get(3));
		Assert.assertEquals("Contig6\tnovelbio\t0\t180\tT", lsAddRef.get(4));
		Assert.assertEquals("Contig6\tnovelbio\t0\t181\tG", lsAddRef.get(5));
		Assert.assertEquals("Contig6\tnovelbio\t0\t182\tA", lsAddRef.get(6));
		txtRead.close();
		
		FileOperate.deleteFileFolder(Infile);
		FileOperate.deleteFileFolder(outFile);
	}
}
