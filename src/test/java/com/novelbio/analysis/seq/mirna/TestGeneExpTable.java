package com.novelbio.analysis.seq.mirna;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import com.novelbio.analysis.seq.GeneExpTable;
import com.novelbio.analysis.seq.rnaseq.RPKMcomput.EnumExpression;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.generalConf.TitleFormatNBC;

public class TestGeneExpTable {
	GeneExpTable geneExpTable = new GeneExpTable(TitleFormatNBC.GeneID);
	String geneExpTestFile = "";
	int sampleNum = 4;
	@Test
	public void testRun() {
		try { initial(); } catch (IOException e) { }
		readGeneExp1("s1", 3);
		readGeneExp1("s2", 4);
		readGeneExp1("s3", 5);
		readGeneExp1("s4", 6);
		
		try { initial2(); } catch (Exception e) { }
		
		readGeneExp2("s1", 3);
		readGeneExp2("s2", 4);
		readGeneExp2("s3", 5);
		readGeneExp2("s4", 6);
		printInfo("", false, EnumExpression.RPKM);
		
	}
	
	public void initial() throws IOException {
		ClassPathResource resource = new ClassPathResource("geneExpTestFile", TestGeneExpTable.class);
		geneExpTestFile = resource.getFile().getAbsolutePath();
		TxtReadandWrite txtRead = new TxtReadandWrite(geneExpTestFile);
		Map<String, Integer> mapGene2Len = new HashMap<>();
		List<String> lsGeneName = new ArrayList<>();
		int i = 0;
		for (String content : txtRead.readlines(2)) {
			i++;
			String ss[] = content.split("\t");
			if (i > 4 || ss[0].equals("Sum")) {
				break;
			}
			lsGeneName.add(ss[0]);
			mapGene2Len.put(ss[0], Integer.parseInt(ss[1]));
		}
		geneExpTable.addLsGeneName(lsGeneName);
		geneExpTable.setMapGene2Len(mapGene2Len);
		txtRead.close();
	}
	
	public void initial2() throws IOException {
		TxtReadandWrite txtRead = new TxtReadandWrite(geneExpTestFile);
		Map<String, Integer> mapGene2Len = new HashMap<>();
		List<String> lsGeneName = new ArrayList<>();
		int i = 0;
		for (String content : txtRead.readlines(2)) {
			i++;
			if (i <= 4 ) {
				continue;
			}
			String ss[] = content.split("\t");
			if (ss[0].equals("Sum")) {
				break;
			}
			lsGeneName.add(ss[0]);
			mapGene2Len.put(ss[0], Integer.parseInt(ss[1]));
		}
		geneExpTable.addLsGeneName(lsGeneName);
		geneExpTable.addMapGene2Len(mapGene2Len);
		txtRead.close();
	}
	
	/**
	 * @param sampleName
	 * @param colNum 实际列
	 */
	public void readGeneExp1(String sampleName, int colNum) {
		colNum--;
		TxtReadandWrite txtRead = new TxtReadandWrite(geneExpTestFile);
		geneExpTable.setCurrentCondition(sampleName);
		int i = 0;
		for (String content : txtRead.readlines(2)) {
			i++;
			String[] ss = content.split("\t");
			if (i > 4 || ss[0].equals("Sum")) {
				geneExpTable.addAllReads(Long.parseLong(ss[colNum]));
				break;
			}
			geneExpTable.addGeneExp(ss[0], Double.parseDouble(ss[colNum]));
		}

		txtRead.close();
	}
	/**
	 * @param sampleName
	 * @param colNum 实际列
	 */
	public void readGeneExp2(String sampleName, int colNum) {
		colNum--;
		TxtReadandWrite txtRead = new TxtReadandWrite(geneExpTestFile);
		geneExpTable.setCurrentCondition(sampleName);
		int i = 0;
		for (String content : txtRead.readlines(2)) {
			i++;
			if (i <= 4) {
				continue;
			}
			String[] ss = content.split("\t");
			if (ss[0].equals("Sum")) {
				geneExpTable.addAllReads(Long.parseLong(ss[colNum]));
				break;
			}
			geneExpTable.addGeneExp(ss[0], Double.parseDouble(ss[colNum]));
		}
		printInfo(sampleName, true, EnumExpression.Counts);
		printInfo(sampleName, true, EnumExpression.TPM);
		printInfo(sampleName, true, EnumExpression.UQPM);
		printInfo(sampleName, true, EnumExpression.RPKM);
		printInfo(sampleName, true, EnumExpression.UQRPKM);
		
		txtRead.close();
	}
	
	private void printInfo(String sampleName, boolean current, EnumExpression enumExpression) {
		if (current) {
			System.out.println(sampleName + " " + enumExpression.toString());
		} else {
			System.out.println("All " + enumExpression.toString());
		}
		List<String[]> ls = current ? geneExpTable.getLsCountsNum(enumExpression) :
			geneExpTable.getLsAllCountsNum(enumExpression);
		for (String[] strings : ls) {
			String combine = ArrayOperate.cmbString(strings, "\t");
			System.out.println(combine);
		}
		System.out.println();
		System.out.println();
	}
	
	
}
