package com.novelbio.test.analysis.diffexpress;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.novelbio.analysis.diffexpress.DiffExpDESeq;
import com.novelbio.base.PathDetail;
import com.novelbio.base.dataOperate.TxtReadandWrite;

import junit.framework.TestCase;

public class TestDiffExpDESeq extends TestCase {
	DiffExpDESeq deSeq;
	TxtReadandWrite txtScript;
	@Before
	public void setUp() {
		deSeq = new DiffExpDESeq();
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
		deSeq.setCol2Sample(lsSampleColumn2GroupName);
		deSeq.setColID(1);
		deSeq.addFileName2Compare(PathDetail.getProjectPath() + "AvsB.xls", new String[]{"A","B"});
		deSeq.addFileName2Compare(PathDetail.getProjectPath() + "AvsC.xls", new String[]{"A","C"});
		deSeq.addFileName2Compare(PathDetail.getProjectPath() + "CvsB.xls", new String[]{"C","B"});
		String DEseqScript = deSeq.getOutScript();
		txtScript = new TxtReadandWrite(DEseqScript, false);
		return txtScript.readfileLs();
	}
	private void assertScriptDuplicate(ArrayList<String> lsScript) {
		assertEquals("filePath = \"" + PathDetail.getProjectPath() + "Tmp/\"", lsScript.get(0));
		assertEquals("fileName = \"" + deSeq.getFileNameRawdata() + "\"", lsScript.get(1));
		assertEquals("setwd(filePath)", lsScript.get(2));
		assertEquals("library(DESeq)", lsScript.get(3));
		assertEquals("data = read.table(fileName, he=T, sep=\"\\t\", row.names=1)", lsScript.get(4));	
		
		assertEquals("conds = factor( c(\"A\", \"A\", \"B\", \"B\", \"C\", \"C\") )", lsScript.get(5));
		
		assertEquals("cds = newCountDataSet( data, conds )", lsScript.get(6));
		assertEquals("cds = estimateSizeFactors(cds)", lsScript.get(7));
		if (deSeq.isRepeatExp()) {
			assertEquals("cds =  estimateDispersions(cds)", lsScript.get(8));
		}
		else {
			assertEquals("cds = estimateDispersions( cds, method=\"blind\", sharingMode=\"fit-only\" )", lsScript.get(8));
		}
		assertEquals(true, deSeq.isRepeatExp());

		assertEquals("res = nbinomTest( cds, \"B\", \"A\" )", lsScript.get(9));
		assertEquals("write.table( res, file=\""+ PathDetail.getProjectPath() + "AvsB.xls" +"\",sep=\"\\t\",row.names=F  )", lsScript.get(10));
		
		assertEquals("res = nbinomTest( cds, \"C\", \"A\" )", lsScript.get(11));
		assertEquals("write.table( res, file=\""+ PathDetail.getProjectPath() + "AvsC.xls" +"\",sep=\"\\t\",row.names=F  )", lsScript.get(12));
		
		assertEquals("res = nbinomTest( cds, \"B\", \"C\" )", lsScript.get(13));
		assertEquals("write.table( res, file=\""+ PathDetail.getProjectPath() + "CvsB.xls" +"\",sep=\"\\t\",row.names=F  )", lsScript.get(14));
	}
	
	private ArrayList<String> setNoDuplicate() {
		ArrayList<String[]> lsSampleColumn2GroupName = new ArrayList<String[]>();
		lsSampleColumn2GroupName.add(new String[] {"2","A"});
		lsSampleColumn2GroupName.add(new String[] {"3","B"});
		lsSampleColumn2GroupName.add(new String[] {"4","C"});
		lsSampleColumn2GroupName.add(new String[] {"5","E"});
		lsSampleColumn2GroupName.add(new String[] {"6","F"});
		lsSampleColumn2GroupName.add(new String[] {"7","G"});
		deSeq.setCol2Sample(lsSampleColumn2GroupName);
		deSeq.setColID(1);
		deSeq.addFileName2Compare(PathDetail.getProjectPath() + "AvsB.xls", new String[]{"A","B"});
		deSeq.addFileName2Compare(PathDetail.getProjectPath() + "AvsC.xls", new String[]{"A","C"});
		deSeq.addFileName2Compare(PathDetail.getProjectPath() + "CvsB.xls", new String[]{"C","B"});
		String DEseqScript = deSeq.getOutScript();
		txtScript = new TxtReadandWrite(DEseqScript, false);
		return txtScript.readfileLs();
	}
	private void assertScriptNoDuplicate(ArrayList<String> lsScript) {
		assertEquals("filePath = \"" + PathDetail.getProjectPath() + "Tmp/\"", lsScript.get(0));
		assertEquals("fileName = \"" + deSeq.getFileNameRawdata() + "\"", lsScript.get(1));
		assertEquals("setwd(filePath)", lsScript.get(2));
		assertEquals("library(DESeq)", lsScript.get(3));
		assertEquals("data = read.table(fileName, he=T, sep=\"\\t\", row.names=1)", lsScript.get(4));	
		
		assertEquals("conds = factor( c(\"A\", \"B\", \"C\", \"E\", \"F\", \"G\") )", lsScript.get(5));
		
		assertEquals("cds = newCountDataSet( data, conds )", lsScript.get(6));
		assertEquals("cds = estimateSizeFactors(cds)", lsScript.get(7));
		if (deSeq.isRepeatExp()) {
			assertEquals("cds =  estimateDispersions(cds)", lsScript.get(8));
		}
		else {
			assertEquals("cds = estimateDispersions( cds, method=\"blind\", sharingMode=\"fit-only\" )", lsScript.get(8));
		}
		assertEquals(true, deSeq.isRepeatExp());

		assertEquals("res = nbinomTest( cds, \"B\", \"A\" )", lsScript.get(9));
		assertEquals("write.table( res, file=\""+ PathDetail.getProjectPath() + "AvsB.xls" +"\",sep=\"\\t\",row.names=F  )", lsScript.get(10));
		
		assertEquals("res = nbinomTest( cds, \"C\", \"A\" )", lsScript.get(11));
		assertEquals("write.table( res, file=\""+ PathDetail.getProjectPath() + "AvsC.xls" +"\",sep=\"\\t\",row.names=F  )", lsScript.get(12));
		
		assertEquals("res = nbinomTest( cds, \"B\", \"C\" )", lsScript.get(13));
		assertEquals("write.table( res, file=\""+ PathDetail.getProjectPath() + "CvsB.xls" +"\",sep=\"\\t\",row.names=F  )", lsScript.get(14));
	}
	@After
	public void tearDown() {
		deSeq.clean();
	}
}
