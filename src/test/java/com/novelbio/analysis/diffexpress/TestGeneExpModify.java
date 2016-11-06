package com.novelbio.analysis.diffexpress;

import java.util.Iterator;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

public class TestGeneExpModify {
	String avgFile = "/tmp/testgeneexp-avgfile.txt";
	String expFile = "/tmp/testgeneexp-expfile.txt";
	
	@After
	public void deleteFile() {
		FileOperate.deleteFileFolder(avgFile);
		FileOperate.deleteFileFolder(expFile);
	}
	
	@Test
	public void testGeneExpModify() {
		GeneExpModify geneExpModify = new GeneExpModify();
		List<String[]> lsSampleCol2Group = Lists.newArrayList(
				new String[]{"3", "sampleA"}, new String[]{"4", "sampleA"}
				,new String[]{"6", "sampleB"}, new String[]{"7", "sampleB"}
				,new String[]{"9", "sampleC"}, new String[]{"10", "sampleC"});
		geneExpModify.setCol2SampleFrom1(lsSampleCol2Group);
		geneExpModify.setMinSampleSepNum(6);
		geneExpModify.setMinSampleSumNum(20);
		geneExpModify.setColAccID(0);
		geneExpModify.setIsCount(true);
		geneExpModify.readGeneExpFile("src/test/resources/test_file/geneexp/geneexpfile.txt");
		geneExpModify.writeToGeneFile(expFile);
		geneExpModify.writeAvgInfo2File(avgFile);
		
		compareFiles("src/test/resources/test_file/geneexp/geneExp-result.txt", expFile);
		compareFiles("src/test/resources/test_file/geneexp/geneAvg-result.txt", avgFile);
	}
	
	private void compareFiles(String txt1, String txt2) {
		TxtReadandWrite txtRead1 = new TxtReadandWrite(txt1);
		TxtReadandWrite txtRead2 = new TxtReadandWrite(txt2);
		Iterator<String> it1 = txtRead1.readlines().iterator();
		Iterator<String> it2 = txtRead2.readlines().iterator();
		while (it1.hasNext()) {
			String[] ss1 = it1.next().split("\t");
			String[] ss2 = it2.next().split("\t");
			Assert.assertEquals(ss1.length, ss2.length);
			for (int i = 0; i < ss1.length; i++) {
				try {
					double value1 = Double.parseDouble(ss1[i]);
					double value2 = Double.parseDouble(ss2[i]);
					Assert.assertEquals(value1, value2, 0.005);
				} catch (Exception e) {
					Assert.assertEquals(ss1[i], ss2[i]);
				}
			}
			
		}
		txtRead1.close();
		txtRead2.close();
	}
}
