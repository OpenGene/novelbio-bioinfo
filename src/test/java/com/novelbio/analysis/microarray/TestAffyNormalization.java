package com.novelbio.analysis.microarray;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.novelbio.base.PathDetail;
import com.novelbio.base.dataOperate.TxtReadandWrite;

public class TestAffyNormalization extends TestCase{

	AffyNormalization affyNormalization;
	TxtReadandWrite txtScript;
	@Before
	public void setUp() {
		affyNormalization = new AffyNormalization();
	}
	
	
	@Test
	public void testAffyScript() {
		List<String> lsScript = setCelFile();
		assertScriptAffy(lsScript);
	}
	private List<String> setCelFile() {
		ArrayList<String> lsCelFile = new ArrayList<String>();
		lsCelFile.add("/media/winF/NBC/Project/Microarray_YL_111012/CEL/090918-HG-U133_Plus_2-09135E_C08025.CEL");
		lsCelFile.add("/media/winF/NBC/Project/Microarray_YL_111012/CEL/090918-HG-U133_Plus_2-09135E_C08195.CEL");
		affyNormalization.setLsRawCelFile(lsCelFile);
		affyNormalization.setNormalizedType(AffyNormalization.NORM_RMA);
		affyNormalization.setArrayType(AffyNormalization.arrayType_exonAffy);
		affyNormalization.setOutFileName("/media/winE/Bioinformatics/R/Protocol/Microarray/allData.txt");
		String AffyNormScript = affyNormalization.getOutScript();
		return TxtReadandWrite.readfileLs(AffyNormScript);
	}
	private void assertScriptAffy(List<String> lsScript) {
		assertEquals("filePath = \"" + PathDetail.getProjectPath() + "Tmp/\"", lsScript.get(0));
		assertEquals("fileName = \"" + affyNormalization.getOutScript() + "\"", lsScript.get(1));
		assertEquals("setwd(filePath)", lsScript.get(2));
		assertEquals("library(affy)", lsScript.get(3));
		assertEquals("library(gcrma)", lsScript.get(4));
		assertEquals("Data = ReadAffy(" + "\"/test/1.cel\"" + ","  + "\"/test/2.cel\"" +  ")", lsScript.get(5));	
		assertEquals("B = readGeneExp(fileName, header=T, sep='\\t', geneCol=1, valCol=c(4, 5))", lsScript.get(5));	
		assertEquals("C = readGeneExp(fileName, header=T, sep='\\t', geneCol=1, valCol=c(6, 7))", lsScript.get(6));	

		assertEquals("DEGexp(geneExpMatrix1 = A, geneCol1 = 1, " +
				"expCol1 = c(2, 3), groupLabel1 = 'A', " +
				"geneExpMatrix2 = B, geneCol2 = 1, expCol2 = c(2, 3), " +
				"groupLabel2 = 'B', method = 'MARS', outputDir='"+PathDetail.getProjectPath() + "AvsB.xls_Path"+"')", lsScript.get(7));
		
		assertEquals("DEGexp(geneExpMatrix1 = A, geneCol1 = 1, " +
				"expCol1 = c(2, 3), groupLabel1 = 'A', " +
				"geneExpMatrix2 = C, geneCol2 = 1, expCol2 = c(2, 3), " +
				"groupLabel2 = 'C', method = 'MARS', outputDir='"+PathDetail.getProjectPath() + "AvsC.xls_Path"+"')", lsScript.get(8));
		
		assertEquals("DEGexp(geneExpMatrix1 = C, geneCol1 = 1, " +
				"expCol1 = c(2, 3), groupLabel1 = 'C', " +
				"geneExpMatrix2 = B, geneCol2 = 1, expCol2 = c(2, 3), " +
				"groupLabel2 = 'B', method = 'MARS', outputDir='"+PathDetail.getProjectPath() + "CvsB.xls_Path"+"')", lsScript.get(9));
	}

}
