package com.novelbio.analysis.annotation.functiontest;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.novelbio.analysis.annotation.GO.GoFisher;
import com.novelbio.analysis.annotation.copeID.CopedID;
import com.novelbio.analysis.annotation.copeID.FisherTest;
import com.novelbio.analysis.annotation.copeID.ItemInfo;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;

public abstract class TestAbs implements ItemInfo{

	private static final Logger logger = Logger.getLogger(GoFisher.class);
	public static final String TEST_GO = "go";
	public static final String TEST_KEGGPATH = "KEGGpathway";
	
	public TestAbs(ArrayList<CopedID> lsCopedIDsTest, ArrayList<CopedID> lsCopedIDsBG, boolean blast)
	{
		this.lsCopedIDsTest = lsCopedIDsTest;
		this.lsCopedIDsBG = lsCopedIDsBG;
		this.blast = blast;
	}
	
	public TestAbs(boolean blast)
	{
		this.blast = blast;
	}

	int taxID = 0;
	boolean blast = false;

	ArrayList<CopedID> lsCopedIDsTest = null;
	ArrayList<CopedID> lsCopedIDsBG = null;
	/**
	 * genUniID item,item格式
	 */
	ArrayList<String[]> lsTest = null;
	/**
	 * genUniID item,item格式
	 */
	ArrayList<String[]> lsBG = null;
	
	String BGfile = "";
	/**
	 * AccID2CopedID的对照表
	 */
	HashMap<String, CopedID> hashAcc2CopedID = new HashMap<String, CopedID>();
	/**
	 * gene2CopedID的对照表，多个accID对应同一个geneID的时候就用这个hash来处理
	 */
	HashMap<String, ArrayList<CopedID>> hashgene2CopedID = new HashMap<String, ArrayList<CopedID>>();
	
	public void setTest(ArrayList<String> lsCopedID, int taxID) {
		lsCopedIDsTest = new ArrayList<CopedID>();
		lsAnno = null;
		for (String string : lsCopedID) {
			CopedID copedID = new CopedID(string, taxID, false);
			lsCopedIDsTest.add(copedID);
		}
		fillCopedIDInfo();
		lsTest = convert2Item(lsCopedIDsTest);
	}
	
	public void setLsCopedID(ArrayList<CopedID> lsCopedIDs) {
		this.lsCopedIDsTest = lsCopedIDs;
		lsAnno = null;
		lsTest = convert2Item(lsCopedIDsTest);
		fillCopedIDInfo();
	}
	
	public void setLsBG(String fileName) {
		if (!FileOperate.isFileExist(fileName)) {
			logger.error("no FIle exist: "+ fileName);
		}
		TxtReadandWrite txtReadBG = new TxtReadandWrite(fileName, false);
		lsBG = txtReadBG.ExcelRead("\t", 1, 1, txtReadBG.ExcelRows(), 2, 1);
	}
	
	
	
	
	/**
	 * 必须先设置lsTestCopedID，因为要从中读取taxID
	 * @param fileName
	 */
	public void setLsBGAccID(String fileName) {
		if (!FileOperate.isFileExist(fileName)) {
			logger.error("no FIle exist: "+ fileName);
		}
		String[][] accID = null;
		try {
			accID = ExcelTxtRead.readExcel(fileName, new int[]{1}, 1, -1);
		} catch (Exception e) {
			try {
				accID = ExcelTxtRead.readtxtExcel(fileName, "\t",new int[]{1}, 1, -1);
			} catch (Exception e2) {
				logger.error("BG accID file is not correct: "+ fileName);
			}
		}
		int taxID = lsCopedIDsTest.get(0).getTaxID();
		for (String[] strings : accID) {
			lsCopedIDsBG.add(new CopedID(strings[0], taxID, false));
		}
	}
	/**
	 * 每次设置新的LsCopedTest后必须重置
	 */
	ArrayList<String[]> lsAnno = null;
	/**
	 * 待修正
	 * 返回Gene2ItemPvalue
	 * @param Type
	 * @return
	 */
	public ArrayList<String[]> Item2GenePvalue() {
		ArrayList<String[]> lsTestResult = null;
		try {
			lsTestResult = getTestResult();
		} catch (Exception e) {
			logger.error("error");
		}
		ArrayList<String[]> lsAnno = getGene2Item();
		ArrayList<String[]> lsResult = null;
		if (blast) {
			 lsResult = ArrayOperate.combArrayListHash(lsTestResult, lsAnno, 0, 6);
		}
		else {
			 lsResult = ArrayOperate.combArrayListHash(lsTestResult, lsAnno, 0, 3);
		}
		return lsResult;
	}
	
	/**
	 * 返回最后的结果，ElimGO需要覆盖该方法
	 * 对结果排个序
	 * @return 结果没加标题<br>
	 * arrayList-string[6] 
	 * 0:itemID <br>
	 * 1到n:item信息 <br>  
	 * n+1:difGene <br>
	 * n+2:AllDifGene<br>
	 * n+3:GeneInGoID <br>
	 * n+4:AllGene <br>
	 * n+5:Pvalue<br>
	 * n+6:FDR <br>
	 * n+7:enrichment n+8:(-log2P) <br>
	 * @throws Exception 
	 */
	public ArrayList<String[]> getTestResult() throws Exception
	{
		ArrayList<String[]> lsTestResult = null;
			lsTestResult = FisherTest.getFisherResult(lsTest, lsBG, this);
		return lsTestResult;
	}
	/**
	 * 将List-CopedID转化为
	 * geneID goID,goID,goID的样式
	 * 并按照genUniID去冗余
	 */
	protected abstract ArrayList<String[]> convert2Item(ArrayList<CopedID> lsCopedIDs);
	
	
	private void fillCopedIDInfo()
	{
		for (CopedID copedID : lsCopedIDsTest) {
			//去冗余，accID相同去掉
			if (hashAcc2CopedID.containsKey(copedID.getAccID())) {
				continue;
			}
			hashAcc2CopedID.put(copedID.getAccID(), copedID);
			if (hashgene2CopedID.containsKey(copedID.getGenUniID())) {
				hashgene2CopedID.get(copedID.getGenUniID()).add(copedID);
			}
			else {
				ArrayList<CopedID> lstmp = new ArrayList<CopedID>();
				lstmp.add(copedID);
				hashgene2CopedID.put(copedID.getGenUniID(), lstmp);
			}
		}
	}
	/**
	 * 根据不同的Test有不同的情况
	 * 一般如下
	 * Go富集分析的gene2Go表格<br>
	 * blast：<br>
	 * 			title2[0]="QueryID";title2[1]="QuerySymbol";title2[2]="Description";title2[3]="Evalue";title2[4]="subjectSymbol";<br>
			title2[5]="Description";title2[6]="PathID";title2[7]="PathTerm";<br>
			不blast：<br>
						title2[0]="QueryID";title2[1]="QuerySymbol";title2[2]="Description";title2[3]="PathID";<br>
			title2[4]="PathTerm";<br>
	 * @return
	 */
	public ArrayList<String[]> getGene2Item() {
		if (lsAnno == null) {
			lsAnno = setGene2Item();
		}
		return lsAnno;
	}
	/**
	 * 根据不同的Test有不同的情况，填充lsAnno
	 * 一般如下
	 * Go富集分析的gene2Go表格<br>
	 * blast：<br>
	 * 			title2[0]="QueryID";title2[1]="QuerySymbol";title2[2]="Description";title2[3]="Evalue";title2[4]="subjectSymbol";<br>
			title2[5]="Description";title2[6]="PathID";title2[7]="PathTerm";<br>
			不blast：<br>
						title2[0]="QueryID";title2[1]="QuerySymbol";title2[2]="Description";title2[3]="PathID";<br>
			title2[4]="PathTerm";<br>
	 * @return
	 */
	protected abstract ArrayList<String[]> setGene2Item();
}
