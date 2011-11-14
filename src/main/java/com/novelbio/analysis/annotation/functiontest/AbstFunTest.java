package com.novelbio.analysis.annotation.functiontest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.log4j.Logger;

import com.novelbio.analysis.annotation.GO.GoFisher;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.modcopeid.CopedID;

public abstract class AbstFunTest implements ItemInfo, FunTestInt{

	private static final Logger logger = Logger.getLogger(GoFisher.class);
	public static final String TEST_GO = "go";
	public static final String TEST_KEGGPATH = "KEGGpathway";
	
	public AbstFunTest(ArrayList<CopedID> lsCopedIDsTest, ArrayList<CopedID> lsCopedIDsBG, boolean blast)
	{
		this.lsCopedIDsTest = lsCopedIDsTest;
		this.lsCopedIDsBG = lsCopedIDsBG;
		this.blast = blast;
	}
	
	public AbstFunTest(boolean blast, double evalue, int... blastTaxID)
	{
		this.blast = blast;
		this.blastTaxID = blastTaxID;
		this.blastEvalue = evalue;
	}
	
	int taxID = 0;
	boolean blast = false;
	int[] blastTaxID = null;
	double blastEvalue = 1e-10;
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
	 * gene2CopedID的对照表，多个accID对应同一个geneID的时候就用这个hash来处理
	 * 用途，当做elimFisher的时候，最后会得到一系列的geneID，而每个geneID可能对应了多个accID
	 * 这时候就用geneID作为key，将accID放入value的list中。
	 * 但是很可能value里面的copedID有相同的accID，这时候为了避免这种情况，我新建了一个hashAcc2CopedID
	 * 专门用于去冗余
	 */
	HashMap<String, ArrayList<CopedID>> hashgene2CopedID = new HashMap<String, ArrayList<CopedID>>();
	
	/**
	 * 设定物种
	 * @param taxID
	 */
	public void setTaxID(int taxID) {
		this.taxID = taxID;
	}
	/**
	 * 获得当前物种
	 * @return
	 */
	public int getTaxID() {
		return taxID;
	}
	/**
	 * 用copedID的geneUniID先查找lsBG，找不到再从头查找
	 * @param lsTest
	 * @return
	 */
	private ArrayList<String[]> getLsTestFromLsBG(ArrayList<CopedID> lsTest)
	{
		//去冗余用的
		HashSet<CopedID> hashCopedIDs = new HashSet<CopedID>();
		for (CopedID copedID : lsTest) {
			hashCopedIDs.add(copedID);
		}
		if (blast) {
			for (CopedID copedID : hashCopedIDs) {
				copedID.setBlastLsInfo(blastEvalue, blastTaxID);
			}
		}
		
		if (lsBG == null || lsBG.size() < 1) {
			return convert2Item(hashCopedIDs);
		}
		HashMap<String, String>  hashBG = new HashMap<String, String>();
		for (String[] strings : lsBG) {
			hashBG.put(strings[0], strings[1]);
		}
		ArrayList<String[]> lsout = new ArrayList<String[]>();
		ArrayList<CopedID> lsNo = new ArrayList<CopedID>();
		for (CopedID copedID : hashCopedIDs) {
			String tmpresult = hashBG.get(copedID.getGenUniID());
			if (tmpresult == null) {
				lsNo.add(copedID);
				continue;
			}
			String[] result = new String[]{copedID.getGenUniID(), tmpresult};
			lsout.add(result);
		}
		if (lsNo.size() > 0) {
			ArrayList<String[]> lsnew = convert2Item(lsNo);
			lsout.addAll(lsnew);
			lsBG.addAll(lsnew);
		}
		return lsout;
	}
	/**
	 * 要先读取AccID文件
	 * @return
	 */
	public ArrayList<String[]> getLsBG() {
		return lsBG;
	}
	public void setLsTestAccID(ArrayList<String> lsCopedID) {
		lsCopedIDsTest = new ArrayList<CopedID>();
		lsTestResult = new ArrayList<String[]>();
		lsAnno = null;
		
		for (String string : lsCopedID) {
			CopedID copedID = new CopedID(string, taxID, false);
			if (blast) {
				copedID.setBlastLsInfo(blastEvalue, blastTaxID);
			}
			lsCopedIDsTest.add(copedID);
		}
		fillCopedIDInfo(lsCopedIDsTest);
		lsTest = getLsTestFromLsBG( lsCopedIDsTest);
	}
	
	public void setLsTest(ArrayList<CopedID> lsCopedIDs) {
		this.lsCopedIDsTest = lsCopedIDs;
		lsAnno = null;
		fillCopedIDInfo(lsCopedIDsTest);
		lsTest = getLsTestFromLsBG(lsCopedIDsTest);
		lsTestResult = new ArrayList<String[]>();
	}
	/**
	 * 最好能第一时间设定
	 * 读取genUniID item,item格式的表
	 * @param fileName
	 */
	public void setLsBGItem(String fileName) {
		if (!FileOperate.isFileExist(fileName)) {
			logger.error("no FIle exist: "+ fileName);
		}
		TxtReadandWrite txtReadBG = new TxtReadandWrite(fileName, false);
		lsBG = txtReadBG.ExcelRead("\t", 1, 1, txtReadBG.ExcelRows(), 2, 1);
	}
	
	/**
	 * 最好能第一时间设定
	 * 读取背景文件，指定读取某一列
	 * @param fileName
	 */
	public void setLsBGAccID(String fileName, int colNum) {
		if (lsCopedIDsBG == null) {
			lsCopedIDsBG = new ArrayList<CopedID>();
		}
		lsCopedIDsBG.clear();
		
		if (!FileOperate.isFileExist(fileName)) {
			logger.error("no FIle exist: "+ fileName);
		}
		ArrayList<String[]> accID = null;
		try {
			accID =  ExcelTxtRead.readLsExcelTxt(fileName, new int[]{colNum}, 1, -1);
		} catch (Exception e) {
			logger.error("BG accID file is not correct: "+ fileName);
		}
		for (String[] strings : accID) {
			CopedID copedID = new CopedID(strings[0], taxID, false);
			copedID.setBlastLsInfo(blastEvalue, blastTaxID);
			lsCopedIDsBG.add(copedID);
		}
		lsBG = convert2Item(lsCopedIDsBG);
	}
	/**
	 * 最好能第一时间设定
	 * 读取背景文件，指定读取某一列
	 * @param fileName
	 */
	public void setLsBGCopedID(ArrayList<CopedID> lsBGaccID) {
		for (CopedID copedID : lsBGaccID) {
			copedID.setBlastLsInfo(blastEvalue, blastTaxID);
		}
		this.lsCopedIDsBG = lsBGaccID;
		lsBG = convert2Item(lsCopedIDsBG);
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
	 * 
	 */
	public ArrayList<String[]> getGene2ItemPvalue() {
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
	ArrayList<String[]> lsTestResult = new ArrayList<String[]>();
	/**
	 * booRun 新跑一次 返回最后的结果，ElimGO需要覆盖该方法 对结果排个序
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
	public ArrayList<String[]> getTestResult()
	{
		if (lsTestResult != null && lsTestResult.size() > 0) {
			return lsTestResult;
		}
		try {
			ArrayList<String[]> lstest = new ArrayList<String[]>();
			for (String[] strings : lsTest) {
				if (strings[1] == null || strings[1].trim().equals("")) {
					continue;
				}
				lstest.add(strings);
			}
			ArrayList<String[]> lsbg = new ArrayList<String[]>();
			for (String[] strings : lsBG) {
				if (strings[1] == null || strings[1].trim().equals("")) {
					continue;
				}
				lsbg.add(strings);
			}
			
			lsTestResult = FisherTest.getFisherResult(lstest, lsbg, this);
		} catch (Exception e) {
			logger.error("error: ");
		}
			
			
		return lsTestResult;
	}
	/**
	 * 将List-CopedID转化为
	 * geneID goID,goID,goID的样式
	 * 并按照genUniID去冗余
	 */
	protected abstract ArrayList<String[]> convert2Item(Collection<CopedID> lsCopedIDs);
	
	/**
	 * 设定hashgene2CopedID，就是一个geneID会对应多个accID的这种
	 * @param lsCopedIDs
	 */
	private void fillCopedIDInfo(ArrayList<CopedID> lsCopedIDs)
	{
		//////////////  先 清 空  ////////////////////////
		HashSet<String> hashAccID = new HashSet<String>();
		hashgene2CopedID.clear();
		////////////////////////////////////////////
		for (CopedID copedID : lsCopedIDs) {
			//去冗余，accID相同去掉
			if (hashAccID.contains(copedID.getAccID())) {
				continue;
			}
			hashAccID.add(copedID.getAccID());
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
	/**
	 * 目前只能设定GO的type
	 */
	public abstract void setDetailType(String GOtype);
	
	/**
	 * 保存本LsBG的信息
	 * @param txtBGItem
	 */
	public void saveLsBGItem(String txtBGItem) {
		TxtReadandWrite txtOut = new TxtReadandWrite(txtBGItem, true);
		txtOut.ExcelWrite(lsBG, "\t", 1, 1);
		txtOut.close();
	}
	
}
