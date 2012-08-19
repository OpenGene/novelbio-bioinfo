package com.novelbio.analysis.annotation.functiontest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

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
	/** genUniID item,item��ʽ  */
	ArrayList<String[]> lsTest = null;
	/** genUniID item,item��ʽ */
	ArrayList<String[]> lsBG = null;
	String BGfile = "";
	/**
	 * gene2CopedID�Ķ��ձ����accID��Ӧͬһ��geneID��ʱ��������hash������
	 * ��;������elimFisher��ʱ������õ�һϵ�е�geneID����ÿ��geneID���ܶ�Ӧ�˶��accID
	 * ��ʱ�����geneID��Ϊkey����accID����value��list�С�
	 * ���Ǻܿ���value�����copedID����ͬ��accID����ʱ��Ϊ�˱���������������½���һ��hashAcc2CopedID
	 * ר������ȥ����
	 */
	HashMap<String, ArrayList<GeneID>> hashgene2CopedID = new HashMap<String, ArrayList<GeneID>>();
	
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
	 * �趨����
	 * @param taxID
	 */
	public void setTaxID(int taxID) {
		this.taxID = taxID;
	}
	/**
	 * ��õ�ǰ����
	 * @return
	 */
	public int getTaxID() {
		return taxID;
	}
	/**
	 * ��copedID��geneUniID�Ȳ���lsBG���Ҳ����ٴ�ͷ����
	 * @param lsTest
	 * @return
	 */
	private ArrayList<String[]> getLsTestFromLsBG(ArrayList<GeneID> lsTest) {
		//ȥ�����õ�
		HashSet<GeneID> hashCopedIDs = new HashSet<GeneID>();
		for (GeneID copedID : lsTest) {
			hashCopedIDs.add(copedID);
		}
		if (blast) {
			for (GeneID copedID : hashCopedIDs) {
				copedID.setBlastInfo(blastEvalue, blastTaxID);
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
		ArrayList<GeneID> lsNo = new ArrayList<GeneID>();
		for (GeneID copedID : hashCopedIDs) {
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
	 * Ҫ�ȶ�ȡAccID�ļ�
	 * @return
	 */
	public ArrayList<String[]> getLsBG() {
		return lsBG;
	}
	public void setLsTestAccID(ArrayList<String> lsCopedID) {
		lsCopedIDsTest = new ArrayList<GeneID>();
		lsTestResult = new ArrayList<String[]>();
		lsAnno = null;
		
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
	
	public void setLsTest(ArrayList<GeneID> lsCopedIDs) {
		this.lsCopedIDsTest = lsCopedIDs;
		lsAnno = null;
		fillCopedIDInfo(lsCopedIDsTest);
		lsTest = getLsTestFromLsBG(lsCopedIDsTest);
		lsTestResult = new ArrayList<String[]>();
	}
	/**
	 * ����ܵ�һʱ���趨
	 * ��ȡgenUniID item,item��ʽ�ı�
	 * @param fileName
	 */
	public void setLsBGItem(String fileName) {
		lsTestResult = new ArrayList<String[]>();
		if (!FileOperate.isFileExist(fileName)) {
			logger.error("no FIle exist: "+ fileName);
		}
		lsBG = ExcelTxtRead.readLsExcelTxt(fileName, new int[]{1,2}, 1, -1, true);
//		TxtReadandWrite txtReadBG = new TxtReadandWrite(fileName, false);
//		lsBG = txtReadBG.ExcelRead("\t", 1, 1, txtReadBG.ExcelRows(), 2, 1);
	}
	
	/**
	 * ����ܵ�һʱ���趨
	 * ��ȡ�����ļ���ָ����ȡĳһ��
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
		lsBG = convert2Item(lsCopedIDsBG);
	}
	/**
	 * ����ܵ�һʱ���趨
	 * ��ȡ�����ļ���ָ����ȡĳһ��
	 * @param fileName
	 */
	public void setLsBGCopedID(ArrayList<GeneID> lsBGaccID) {
		lsTestResult = new ArrayList<String[]>();
		for (GeneID copedID : lsBGaccID) {
			copedID.setBlastInfo(blastEvalue, blastTaxID);
		}
		this.lsCopedIDsBG = lsBGaccID;
		lsBG = convert2Item(lsCopedIDsBG);
	}
	/**
	 * ÿ�������µ�LsCopedTest���������
	 */
	ArrayList<String[]> lsAnno = null;
	/**
	 * ������
	 * ����Gene2ItemPvalue
	 * @param Type
	 * @return
	 * ���ݲ�ͬ��Test�в�ͬ�����
	 * һ������
	 * Go����������gene2Go���<br>
	 * blast��<br>
	 * 			title2[0]="QueryID";title2[1]="QuerySymbol";title2[2]="Description";title2[3]="Evalue";title2[4]="subjectSymbol";<br>
			title2[5]="Description";title2[6]="PathID";title2[7]="PathTerm";<br>
			��blast��<br>
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
	 * booRun ����һ�� �������Ľ����ElimGO��Ҫ���Ǹ÷��� �Խ���Ÿ���
	 * �������Ľ����ElimGO��Ҫ���Ǹ÷���
	 * �Խ���Ÿ���
	 * @return ���û�ӱ���<br>
	 * arrayList-string[6] 
	 * 0:itemID <br>
	 * 1��n:item��Ϣ <br>  
	 * n+1:difGene <br>
	 * n+2:AllDifGene<br>
	 * n+3:GeneInGoID <br>
	 * n+4:AllGene <br>
	 * n+5:Pvalue<br>
	 * n+6:FDR <br>
	 * n+7:enrichment n+8:(-log2P) <br>
	 * @throws Exception 
	 * û�оͷ���null
	 */
	public ArrayList<String[]> getTestResult() {
		if (lsTestResult != null && lsTestResult.size() > 10) {
			return lsTestResult;
		}
		return doTest();
	}
	/**
	 * booRun ����һ�� �������Ľ����ElimGO��Ҫ���Ǹ÷��� �Խ���Ÿ���
	 * �������Ľ����ElimGO��Ҫ���Ǹ÷���
	 * �Խ���Ÿ���
	 * @return ���û�ӱ���<br>
	 * arrayList-string[6] 
	 * 0:itemID <br>
	 * 1��n:item��Ϣ <br>  
	 * n+1:difGene <br>
	 * n+2:AllDifGene<br>
	 * n+3:GeneInGoID <br>
	 * n+4:AllGene <br>
	 * n+5:Pvalue<br>
	 * n+6:FDR <br>
	 * n+7:enrichment n+8:(-log2P) <br>
	 * @throws Exception 
	 * û�оͷ���null
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
			for (String[] strings : lsBG) {
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
	 * ��List-CopedIDת��Ϊ
	 * geneID goID,goID,goID����ʽ
	 * ������genUniIDȥ����
	 */
	protected abstract ArrayList<String[]> convert2Item(Collection<GeneID> lsCopedIDs);
	
	/**
	 * �趨hashgene2CopedID������һ��geneID���Ӧ���accID������
	 * @param lsCopedIDs
	 */
	private void fillCopedIDInfo(ArrayList<GeneID> lsCopedIDs)
	{
		//////////////  �� �� ��  ////////////////////////
		HashSet<String> hashAccID = new HashSet<String>();
		hashgene2CopedID.clear();
		////////////////////////////////////////////
		for (GeneID copedID : lsCopedIDs) {
			//ȥ���࣬accID��ͬȥ��
			if (hashAccID.contains(copedID.getAccID())) {
				continue;
			}
			hashAccID.add(copedID.getAccID());
			if (hashgene2CopedID.containsKey(copedID.getGenUniID())) {
				hashgene2CopedID.get(copedID.getGenUniID()).add(copedID);
			}
			else {
				ArrayList<GeneID> lstmp = new ArrayList<GeneID>();
				lstmp.add(copedID);
				hashgene2CopedID.put(copedID.getGenUniID(), lstmp);
			}
		}
	}
	/**
	 * ���ݲ�ͬ��Test�в�ͬ�����
	 * һ������
	 * Go����������gene2Go���<br>
	 * blast��<br>
	 * 			title2[0]="QueryID";title2[1]="QuerySymbol";title2[2]="Description";title2[3]="Evalue";title2[4]="subjectSymbol";<br>
			title2[5]="Description";title2[6]="PathID";title2[7]="PathTerm";<br>
			��blast��<br>
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
	 * ���ݲ�ͬ��Test�в�ͬ����������lsAnno
	 * һ������
	 * Go����������gene2Go���<br>
	 * blast��<br>
	 * 			title2[0]="QueryID";title2[1]="QuerySymbol";title2[2]="Description";title2[3]="Evalue";title2[4]="subjectSymbol";<br>
			title2[5]="Description";title2[6]="PathID";title2[7]="PathTerm";<br>
			��blast��<br>
						title2[0]="QueryID";title2[1]="QuerySymbol";title2[2]="Description";title2[3]="PathID";<br>
			title2[4]="PathTerm";<br>
	 * @return
	 */
	protected abstract ArrayList<String[]> setGene2Item();
	/**
	 * Ŀǰֻ���趨GO��type
	 */
	public abstract void setDetailType(String GOtype);
	
	/**
	 * ���汾LsBG����Ϣ
	 * @param txtBGItem
	 */
	public void saveLsBGItem(String txtBGItem) {
		TxtReadandWrite txtOut = new TxtReadandWrite(txtBGItem, true);
		txtOut.ExcelWrite(lsBG, "\t", 1, 1);
		txtOut.close();
	}
	/**
	 * ֻ������GO����
	 * @param goType
	 */
	public void setGoType(String goType) {
	}
	/**
	 * ����
	 * * blast��
blast * 0:symbol 1:description 2:evalue 3:subjectSpecies 4:symbol 5:description
��blast��
0:symbol 1:description
	 * @param tmpresultRaw
	 * @return
 * blast��
blast * 0:queryID  1:symbol 2:description 3:evalue 4:subjectSpecies 5:symbol 6:description
��blast��
 0:queryID  1:symbol 2:description
	 */
	protected static String[] copyAnno(String QueryID, String[] tmpresultRaw) {
		String[] tmpInfo = ArrayOperate.deletElement(tmpresultRaw, new int[]{2});
		String[] result = ArrayOperate.combArray(new String[]{QueryID}, tmpInfo, 0);
		return result;
	}
}
