package com.novelbio.test.analysis.diffexpress;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.novelbio.analysis.diffexpress.DiffExpDEGseq;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

import de.erichseifert.gral.data.comparators.Descending;

import junit.framework.TestCase;

public class TestDiffExpDEGseq extends TestCase {
	DiffExpDEGseq degSeq;
	TxtReadandWrite txtScript;
	@Before
	public void setUp() {
		degSeq = new DiffExpDEGseq();
	}
	
	
	@Test
	public void testScript() {
		ArrayList<String> lsScript = setDuplicate();
		assertScriptDuplicate(lsScript);
		
		lsScript = setNoDuplicate();
		assertScriptNoDuplicate(lsScript);
	}
	private ArrayList<String> setDuplicate() {
		ArrayList<String[]> lsSampleColumn2GroupName = new ArrayList<String[]>();
		lsSampleColumn2GroupName.add(new String[] {"2","A"});
		lsSampleColumn2GroupName.add(new String[] {"3","A"});
		lsSampleColumn2GroupName.add(new String[] {"4","B"});
		lsSampleColumn2GroupName.add(new String[] {"5","B"});
		lsSampleColumn2GroupName.add(new String[] {"6","C"});
		lsSampleColumn2GroupName.add(new String[] {"7","C"});
		degSeq.setCol2Sample(lsSampleColumn2GroupName);
		degSeq.setColID(1);
		degSeq.addFileName2Compare(FileOperate.getProjectPath() + "AvsB.xls", new String[]{"A","B"});
		degSeq.addFileName2Compare(FileOperate.getProjectPath() + "AvsC.xls", new String[]{"A","C"});
		degSeq.addFileName2Compare(FileOperate.getProjectPath() + "CvsB.xls", new String[]{"C","B"});
		String DEseqScript = degSeq.getOutScript();
		txtScript = new TxtReadandWrite(DEseqScript, false);
		return txtScript.readfileLs();
	}
	private void assertScriptDuplicate(ArrayList<String> lsScript) {
		assertEquals("filePath = \"" + FileOperate.getProjectPath() + "Tmp/\"", lsScript.get(0));
		assertEquals("fileName = \"" + degSeq.getFileNameRawdata() + "\"", lsScript.get(1));
		assertEquals("setwd(filePath)", lsScript.get(2));
		assertEquals("library(DEGseq)", lsScript.get(3));
		assertEquals("A = readGeneExp(fileName, header=T, sep='\\t', geneCol=1, valCol=c(2, 3))", lsScript.get(4));	
		assertEquals("B = readGeneExp(fileName, header=T, sep='\\t', geneCol=1, valCol=c(4, 5))", lsScript.get(5));	
		assertEquals("C = readGeneExp(fileName, header=T, sep='\\t', geneCol=1, valCol=c(6, 7))", lsScript.get(6));	

		assertEquals("DEGexp(geneExpMatrix1 = A, geneCol1 = 1, " +
				"expCol1 = c(2, 3), groupLabel1 = 'A', " +
				"geneExpMatrix2 = B, geneCol2 = 1, expCol2 = c(2, 3), " +
				"groupLabel2 = 'B', method = 'MARS', outputDir='"+FileOperate.getProjectPath() + "AvsB.xls_Path"+"')", lsScript.get(7));
		
		assertEquals("DEGexp(geneExpMatrix1 = A, geneCol1 = 1, " +
				"expCol1 = c(2, 3), groupLabel1 = 'A', " +
				"geneExpMatrix2 = C, geneCol2 = 1, expCol2 = c(2, 3), " +
				"groupLabel2 = 'C', method = 'MARS', outputDir='"+FileOperate.getProjectPath() + "AvsC.xls_Path"+"')", lsScript.get(8));
		
		assertEquals("DEGexp(geneExpMatrix1 = C, geneCol1 = 1, " +
				"expCol1 = c(2, 3), groupLabel1 = 'C', " +
				"geneExpMatrix2 = B, geneCol2 = 1, expCol2 = c(2, 3), " +
				"groupLabel2 = 'B', method = 'MARS', outputDir='"+FileOperate.getProjectPath() + "CvsB.xls_Path"+"')", lsScript.get(9));
	}
	
	private ArrayList<String> setNoDuplicate() {
		ArrayList<String[]> lsSampleColumn2GroupName = new ArrayList<String[]>();
		lsSampleColumn2GroupName.add(new String[] {"2","A"});
		lsSampleColumn2GroupName.add(new String[] {"3","B"});
		lsSampleColumn2GroupName.add(new String[] {"4","C"});
		degSeq.setCol2Sample(lsSampleColumn2GroupName);
		degSeq.setColID(1);
		degSeq.addFileName2Compare(FileOperate.getProjectPath() + "AvsB.xls", new String[]{"A","B"});
		degSeq.addFileName2Compare(FileOperate.getProjectPath() + "AvsC.xls", new String[]{"A","C"});
		degSeq.addFileName2Compare(FileOperate.getProjectPath() + "CvsB.xls", new String[]{"C","B"});
		String DEseqScript = degSeq.getOutScript();
		txtScript = new TxtReadandWrite(DEseqScript, false);
		return txtScript.readfileLs();
	}
	private void assertScriptNoDuplicate(ArrayList<String> lsScript) {
		assertEquals("filePath = \"" + FileOperate.getProjectPath() + "Tmp/\"", lsScript.get(0));
		assertEquals("fileName = \"" + degSeq.getFileNameRawdata() + "\"", lsScript.get(1));
		assertEquals("setwd(filePath)", lsScript.get(2));
		assertEquals("library(DEGseq)", lsScript.get(3));
		assertEquals("A = readGeneExp(fileName, header=T, sep='\\t', geneCol=1, valCol=c(2))", lsScript.get(4));	
		assertEquals("B = readGeneExp(fileName, header=T, sep='\\t', geneCol=1, valCol=c(3))", lsScript.get(5));	
		assertEquals("C = readGeneExp(fileName, header=T, sep='\\t', geneCol=1, valCol=c(4))", lsScript.get(6));	

		assertEquals("DEGexp(geneExpMatrix1 = A, geneCol1 = 1, " +
				"expCol1 = c(2), groupLabel1 = 'A', " +
				"geneExpMatrix2 = B, geneCol2 = 1, expCol2 = c(2), " +
				"groupLabel2 = 'B', method = 'MARS', outputDir='"+FileOperate.getProjectPath() + "AvsB.xls_Path"+"')", lsScript.get(7));
		
		assertEquals("DEGexp(geneExpMatrix1 = A, geneCol1 = 1, " +
				"expCol1 = c(2), groupLabel1 = 'A', " +
				"geneExpMatrix2 = C, geneCol2 = 1, expCol2 = c(2), " +
				"groupLabel2 = 'C', method = 'MARS', outputDir='"+FileOperate.getProjectPath() + "AvsC.xls_Path"+"')", lsScript.get(8));
		
		assertEquals("DEGexp(geneExpMatrix1 = C, geneCol1 = 1, " +
				"expCol1 = c(2), groupLabel1 = 'C', " +
				"geneExpMatrix2 = B, geneCol2 = 1, expCol2 = c(2), " +
				"groupLabel2 = 'B', method = 'MARS', outputDir='"+FileOperate.getProjectPath() + "CvsB.xls_Path"+"')", lsScript.get(9));
	}
	@After
	public void tearDown() {
		degSeq.clean();
	}
}
