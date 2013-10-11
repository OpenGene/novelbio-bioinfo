package com.novelbio.analysis.diffexpress;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.novelbio.analysis.diffexpress.DiffExpLimma;
import com.novelbio.base.PathDetail;
import com.novelbio.base.dataOperate.TxtReadandWrite;

public class TestDiffExpLimma extends TestCase{
	DiffExpLimma limma;
	TxtReadandWrite txtScript;
	@Before
	public void setUp() {
		limma = new DiffExpLimma();
	}
	
	@Test
	public void testScript() {
		ArrayList<String> lsScript = setDuplicate();
		assertScriptDuplicate(lsScript);
	}
	
	private ArrayList<String> setDuplicate() {
		ArrayList<String[]> lsSampleColumn2GroupName = new ArrayList<String[]>();
		lsSampleColumn2GroupName.add(new String[] {"2","A"});
		lsSampleColumn2GroupName.add(new String[] {"3","A"});
		lsSampleColumn2GroupName.add(new String[] {"4","B"});
		lsSampleColumn2GroupName.add(new String[] {"5","B"});
		lsSampleColumn2GroupName.add(new String[] {"6","C"});
		lsSampleColumn2GroupName.add(new String[] {"7","C"});
		limma.setCol2Sample(lsSampleColumn2GroupName);
		limma.setColID(1);
		limma.addFileName2Compare(PathDetail.getProjectPath() + "AvsB.xls", "DiffGene", new String[]{"A","B"});
		limma.addFileName2Compare(PathDetail.getProjectPath() + "AvsC.xls", "DiffGene", new String[]{"A","C"});
		limma.addFileName2Compare(PathDetail.getProjectPath() + "CvsB.xls", "DiffGene", new String[]{"C","B"});
		String DEseqScript = limma.getOutScript();
		txtScript = new TxtReadandWrite(DEseqScript, false);
		return txtScript.readfileLs();
	}
	private void assertScriptDuplicate(ArrayList<String> lsScript) {
		int i = 0;
		assertEquals("filePath = \"" + PathDetail.getProjectPath() + "Tmp/\"", lsScript.get(i++));
		assertEquals("fileName = \"" + limma.getFileNameRawdata() + "\"", lsScript.get(i++));
		assertEquals("setwd(filePath)", lsScript.get(i++));
		assertEquals("library(limma)", lsScript.get(i++));
		assertEquals("eset=read.table(file=fileName,he=T,sep=\"\\t\",row.names=1)", lsScript.get(i++));
		
		if (limma.isLogValue())
			assertEquals("eset = log2(eset))", lsScript.get(i++));
		else
			assertEquals("", lsScript.get(i++));
		
		assertEquals("design = model.matrix(~ -1+factor (c(1, 1, 2, 2, 3, 3)))", lsScript.get(i++));
		assertEquals("colnames(design) = c(\"A\", \"B\", \"C\")", lsScript.get(i++));

		assertEquals("contrast.matrix = makeContrasts( A_vs_B = A - B, A_vs_C = A - C, C_vs_B = C - B, levels=design)", lsScript.get(i++));
		
		assertEquals("fit = lmFit(eset, design)", lsScript.get(i++));
		assertEquals("fit2 = contrasts.fit(fit, contrast.matrix)", lsScript.get(i++));
		assertEquals("fit2.eBayes = eBayes(fit2)", lsScript.get(i++));
		
		assertEquals("write.table(topTable(fit2.eBayes, coef=\"A_vs_B\", adjust=\"fdr\", sort.by=\"B\", number=50000),  " +
				"file=\""+PathDetail.getProjectPath() + "AvsB.xls\", row.names=F, sep=\"\\t\")", lsScript.get(i++));
		assertEquals("write.table(topTable(fit2.eBayes, coef=\"A_vs_C\", adjust=\"fdr\", sort.by=\"B\", number=50000),  " +
				"file=\""+PathDetail.getProjectPath() + "AvsC.xls\", row.names=F, sep=\"\\t\")", lsScript.get(i++));
		assertEquals("write.table(topTable(fit2.eBayes, coef=\"C_vs_B\", adjust=\"fdr\", sort.by=\"B\", number=50000),  " +
				"file=\""+PathDetail.getProjectPath() + "CvsB.xls\", row.names=F, sep=\"\\t\")", lsScript.get(i++));
		
	}
	@After
	public void tearDown() {
		limma.clean();
	}

}
