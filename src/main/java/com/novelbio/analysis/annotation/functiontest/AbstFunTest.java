package com.novelbio.analysis.annotation.functiontest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;

import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.modgeneid.GeneID;

public abstract class AbstFunTest implements ItemInfo, FunTestInt{

	private static final Logger logger = Logger.getLogger(AbstFunTest.class);
	public static final String TEST_GO = "go";
	public static final String TEST_KEGGPATH = "KEGGpathway";
	
	int taxID = 0;
	boolean blast = false;
	int[] blastTaxID = null;
	double blastEvalue = 1e-10;
	
	ArrayList<GeneID> lsCopedIDsTest = null;
	ArrayList<GeneID> lsCopedIDsBG = null;
	/** genUniID item,item格式  */
	ArrayList<String[]> lsTest = null;
	/** genUniID item,item格式 */
	ArrayList<String[]> lsBGGeneID2Items = null;
	String BGfile = "";
	/**
	 * gene2CopedID的对照表，多个accID对应同一个geneID的时候就用这个hash来处理
	 * 用途，当做elimFisher的时候，最后会得到一系列的geneID，而每个geneID可能对应了多个accID
	 * 这时候就用geneID作为key，将accID放入value的list中。
	 * 但是很可能value里面的copedID有相同的accID，这时候为了避免这种情况，我新建了一个hashAcc2CopedID
	 * 专门用于去冗余
	 */
	HashMap<String, ArrayList<GeneID>> mapAccID2LsGeneID = new HashMap<String, ArrayList<GeneID>>();
	ArrayList<String[]> lsTestResult = new ArrayList<String[]>();
	
	/**
	 * Gene2GO或者Gene2Path的信息
	 * 每次设置新的LsCopedTest后必须重置
	 */
	ArrayList<String[]> lsGene2GOPath = null;

	public AbstFunTest(ArrayList<GeneID> lsCopedIDsTest, ArrayList<GeneID> lsCopedIDsBG, boolean blast) {
		this.lsCopedIDsTest = lsCopedIDsTest;
		this.lsCopedIDsBG = lsCopedIDsBG;
		this.blast = blast;
	}
	
	public AbstFunTest() {}
	
	public void setBlast(boolean blast, double evalue, int... blastTaxID) {
		this.blast = blast;
		this.blastTaxID = blastTaxID;
		this.blastEvalue = evalue;
	}
	
	public void setBlastTaxID(int... taxID) {
		this.blastTaxID = taxID;
	}

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
	 * 如果lsTest中有一些新的gene，也添加入lsBGGeneID2Items中
	 * @param lsTest
	 * @return
	 */
	private ArrayList<String[]> getLsTestFromLsBG(ArrayList<GeneID> lsTest) {
		//去冗余用的
		HashSet<GeneID> setGeneIDs = new HashSet<GeneID>();
		for (GeneID geneID : lsTest) {
			setGeneIDs.add(geneID);
		}
		if (blast) {
			for (GeneID copedID : setGeneIDs) {
				copedID.setBlastInfo(blastEvalue, blastTaxID);
			}
		}
		//如果没有lsBG，就查找数据库，否则查找lsBG
		if (lsBGGeneID2Items == null || lsBGGeneID2Items.size() < 1) {
			return convert2Item(setGeneIDs);
		}
		
		HashMap<String, String>  mapBGGeneID2Items = new HashMap<String, String>();
		for (String[] strings : lsBGGeneID2Items) {
			mapBGGeneID2Items.put(strings[0], strings[1]);
		}
		ArrayList<String[]> lsout = new ArrayList<String[]>();
		
		//输入的lsTest基因，如果在背景中找不到对应的信息，则保存进入该list
		ArrayList<GeneID> lsInputNotFindGene = new ArrayList<GeneID>();
		for (GeneID copedID : setGeneIDs) {
			String tmpresult = mapBGGeneID2Items.get(copedID.getGenUniID());
			if (tmpresult == null) {
				lsInputNotFindGene.add(copedID);
				continue;
			}
			String[] result = new String[]{copedID.getGenUniID(), tmpresult};
			lsout.add(result);
		}
		if (lsInputNotFindGene.size() > 0) {
			ArrayList<String[]> lsnew = convert2Item(lsInputNotFindGene);
			lsout.addAll(lsnew);
			lsBGGeneID2Items.addAll(lsnew);
		}
		return lsout;
	}

	
	public void setLsTestAccID(ArrayList<String> lsCopedID) {
		lsCopedIDsTest = new ArrayList<GeneID>();
		lsTestResult = new ArrayList<String[]>();
		lsGene2GOPath = null;
		
		for (String string : lsCopedID) {
			GeneID copedID = new GeneID(string, taxID, false);
			if (blast) {
				copedID.setBlastInfo(blastEvalue, blastTaxID);
			}
			lsCopedIDsTest.add(copedID);
		}
		fillCopedIDInfo(lsCopedIDsTest);
		lsTest = getLsTestFromLsBG( lsCopedIDsTest);
	}
	
	public void setLsTestGeneID(ArrayList<GeneID> lsCopedIDs) {
		this.lsCopedIDsTest = lsCopedIDs;
		lsGene2GOPath = null;
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
		lsTestResult = new ArrayList<String[]>();
		if (!FileOperate.isFileExist(fileName)) {
			logger.error("no FIle exist: "+ fileName);
		}
		lsBGGeneID2Items = ExcelTxtRead.readLsExcelTxt(fileName, new int[]{1,2}, 1, -1, true);
	}
	
	/**
	 * 最好能第一时间设定
	 * 读取背景文件，指定读取某一列
	 * @param fileName
	 */
	public void setLsBGAccID(String fileName, int colNum) {
		lsTestResult = new ArrayList<String[]>();
		if (lsCopedIDsBG == null) {
			lsCopedIDsBG = new ArrayList<GeneID>();
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
			GeneID copedID = new GeneID(strings[0], taxID, false);
			if (blast) {
				copedID.setBlastInfo(blastEvalue, blastTaxID);
			}
			lsCopedIDsBG.add(copedID);
		}
		lsBGGeneID2Items = convert2Item(lsCopedIDsBG);
	}
	/**
	 * 最好能第一时间设定
	 * 读取背景文件，指定读取某一列
	 * @param showMessage
	 */
	public void setLsBGCopedID(ArrayList<GeneID> lsBGaccID) {
		lsTestResult = new ArrayList<String[]>();
		for (GeneID copedID : lsBGaccID) {
			copedID.setBlastInfo(blastEvalue, blastTaxID);
		}
		this.lsCopedIDsBG = lsBGaccID;
		lsBGGeneID2Items = convert2Item(lsCopedIDsBG);
	}
	/**
	 * 要先读取AccID文件
	 * @return
	 */
	protected ArrayList<String[]> getLsBG() {
		return lsBGGeneID2Items;
	}
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
			lsResult = combArrayListHash(lsTestResult, lsAnno, 0, 6);
		} else {
			lsResult = combArrayListHash(lsTestResult, lsAnno, 0, 3);
		}
		return lsResult;
	}
	/**
	 * 合并testResult表和anno表
	 * */
	private static ArrayList<String[]> combArrayListHash(List<String[]> lsTestResult ,List<String[]> lsAnno, int AcolNum, int BcolNum) {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		Hashtable<String, String[]> hashLsA = new Hashtable<String, String[]>();
		for (String[] strings : lsTestResult) {
			String tmpKey = strings[AcolNum];
			hashLsA.put(tmpKey.trim(), strings);
		}
		for (String[] strings : lsAnno) {
			String tmpKeyB = strings[BcolNum];
			String[] tmpA = hashLsA.get(tmpKeyB.trim());
			if (tmpA == null) {
				logger.error("no lsA element equals lsB: "+tmpKeyB);
				continue;
			}
			tmpA = ArrayOperate.deletElement(tmpA, new int[]{0, 1});
			String[] tmpResult = ArrayOperate.combArray(strings, tmpA, 0);
			lsResult.add(tmpResult);
		}
		return lsResult;
	}
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
	 * 没有就返回null
	 */
	public ArrayList<String[]> getTestResult() {
		if (lsTestResult != null && lsTestResult.size() > 10) {
			return lsTestResult;
		}
		return doTest();
	}
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
	 * 没有就返回null
	 */
	protected ArrayList<String[]> doTest() {
		try {
			ArrayList<String[]> lstest = new ArrayList<String[]>();
			for (String[] strings : lsTest) {
				if (strings[1] == null || strings[1].trim().equals("")) {
					continue;
				}
				lstest.add(strings);
			}
			if (lstest.size() == 0) {
				return null;
			}
			ArrayList<String[]> lsbg = new ArrayList<String[]>();
			for (String[] strings : lsBGGeneID2Items) {
				if (strings[1] == null || strings[1].trim().equals("")) {
					continue;
				}
				lsbg.add(strings);
			}
			lsTestResult = DoFisherTest.getFisherResult(lstest, lsbg, this);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("error: ");
		}
		return lsTestResult;
	}
	/**
	 * 将List-CopedID转化为
	 * geneID goID,goID,goID的样式
	 * 并按照genUniID去冗余
	 */
	protected abstract ArrayList<String[]> convert2Item(Collection<GeneID> lsCopedIDs);
	
	/**
	 * 设定hashgene2CopedID，就是一个geneID会对应多个accID的这种
	 * @param lsCopedIDs
	 */
	private void fillCopedIDInfo(ArrayList<GeneID> lsCopedIDs)
	{
		//////////////  先 清 空  ////////////////////////
		HashSet<String> hashAccID = new HashSet<String>();
		mapAccID2LsGeneID.clear();
		////////////////////////////////////////////
		for (GeneID copedID : lsCopedIDs) {
			//去冗余，accID相同去掉
			if (hashAccID.contains(copedID.getAccID())) {
				continue;
			}
			hashAccID.add(copedID.getAccID());
			if (mapAccID2LsGeneID.containsKey(copedID.getGenUniID())) {
				mapAccID2LsGeneID.get(copedID.getGenUniID()).add(copedID);
			}
			else {
				ArrayList<GeneID> lstmp = new ArrayList<GeneID>();
				lstmp.add(copedID);
				mapAccID2LsGeneID.put(copedID.getGenUniID(), lstmp);
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
		if (lsGene2GOPath == null) {
			lsGene2GOPath = setGene2Item();
		}
		return lsGene2GOPath;
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
		txtOut.ExcelWrite(lsBGGeneID2Items, 1, 1);
		txtOut.close();
	}
	/**
	 * 只能用于GO分析
	 * @param goType
	 */
	public void setGoType(String goType) {
	}
	/**
	 * 输入
	 * * blast：
blast * 0:symbol 1:description 2:evalue 3:subjectSpecies 4:symbol 5:description
不blast：
0:symbol 1:description
	 * @param tmpresultRaw
	 * @return
 * blast：
blast * 0:queryID  1:symbol 2:description 3:evalue 4:subjectSpecies 5:symbol 6:description
不blast：
 0:queryID  1:symbol 2:description
	 */
	protected static String[] copyAnno(String QueryID, String[] tmpresultRaw) {
		String[] tmpInfo = ArrayOperate.deletElement(tmpresultRaw, new int[]{2});
		String[] result = ArrayOperate.combArray(new String[]{QueryID}, tmpInfo, 0);
		return result;
	}
}
