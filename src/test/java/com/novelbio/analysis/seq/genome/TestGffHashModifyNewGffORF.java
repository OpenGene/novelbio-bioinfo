package com.novelbio.analysis.seq.genome;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class TestGffHashModifyNewGffORF {
	
	@Test
	public void testGetNoDuplicateName() {
		Set<String> setIsoName = new HashSet<>();
		String baseName = "geneName1";
		setIsoName.add(baseName);
		setIsoName.add(baseName + "-1");
		setIsoName.add(baseName + "-2");
		
		String geneName = GffHashModifyNewGffORF.getNoDuplicateName(setIsoName, baseName);
		Assert.assertEquals(baseName + "-3", geneName);
		//===========================
		
		setIsoName = new HashSet<>();
		baseName = "geneName1-123";
		setIsoName.add(baseName);
		setIsoName.add(baseName + "-1");
		setIsoName.add(baseName + "-2");
		
		geneName = GffHashModifyNewGffORF.getNoDuplicateName(setIsoName, baseName);
		Assert.assertEquals(baseName + "-3", geneName);
		//===========================
		
		setIsoName = new HashSet<>();
		baseName = "geneName1-123";
		String thisName = baseName + "-3";
		setIsoName.add(baseName);
		setIsoName.add(baseName + "-1");
		setIsoName.add(baseName + "-2");
		
		geneName = GffHashModifyNewGffORF.getNoDuplicateName(setIsoName, thisName);
		Assert.assertEquals(thisName, geneName);
		
		setIsoName = new HashSet<>();
		baseName = "geneName1-123";
		thisName = baseName + "-3";
		setIsoName.add(baseName);
		setIsoName.add(baseName + "-1");
		setIsoName.add(baseName + "-3");
		
		geneName = GffHashModifyNewGffORF.getNoDuplicateName(setIsoName, thisName);
		Assert.assertEquals(baseName+"-2", geneName);
	}

}
