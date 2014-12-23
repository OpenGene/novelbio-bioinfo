package com.novelbio.analysis.seq.sam;

import org.junit.Before;

import com.novelbio.base.fileOperate.FileOperate;

import junit.framework.TestCase;

public class TestBamFilterUnique extends TestCase {
	String testFileName = "/home/novelbio/NBCsource/test/sam/testUnique.bam";
	String unique = FileOperate.changeFileSuffix(testFileName, "_unique", null);
	String uniqueSort = FileOperate.changeFileSuffix(unique, "_sorted", null);
	
	@Before
	public void before() {
		BamFilterUnique bamFilterUnique = new BamFilterUnique();
		bamFilterUnique.setSamFile(new SamFile(testFileName));
		bamFilterUnique.setOutFile(unique);
		bamFilterUnique.filterUniqueReads();
		
		SamFile samFile = new SamFile(testFileName);
		samFile.sort(uniqueSort, true);
	}
	
	public void testIsUnique() {
		before();
		assertEquals(false, BamFilterUnique.isUniqueMapped(new SamFile(testFileName)));
		assertEquals(true, BamFilterUnique.isUniqueMapped(new SamFile(unique)));
		assertEquals(true, BamFilterUnique.isUniqueMapped(new SamFile(uniqueSort)));
	}
}
