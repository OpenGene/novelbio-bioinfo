package com.novelbio.analysis.annotation.functiontest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.velocity.runtime.parser.node.PutExecutor;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.FisherTest;
import com.novelbio.base.dataStructure.StatisticsTest;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.modgeneid.GeneID;

public abstract class AbstFunTest implements FunTestInt{
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
	ArrayList<GeneID2LsItem> lsTest = null;
	/** genUniID item,item��ʽ */
	ArrayList<GeneID2LsItem> lsBGGeneID2Items = null;
	String BGfile = "";
	int BGnum = 0;
	/**
	 * gene2CopedID�Ķ��ձ����accID��Ӧͬһ��geneID��ʱ��������hash������
	 * ��;������elimFisher��ʱ������õ�һϵ�е�geneID����ÿ��geneID���ܶ�Ӧ�˶��accID
	 * ��ʱ�����geneID��Ϊkey����accID����value��list�С�
	 * ���Ǻܿ���value�����copedID����ͬ��accID����ʱ��Ϊ�˱���������������½���һ��hashAcc2CopedID
	 * ר������ȥ����
	 */
	ArrayListMultimap<String, GeneID> mapGeneUniID2LsGeneID = ArrayListMultimap.create();
	ArrayList<StatisticTestResult> lsTestResult = new ArrayList<StatisticTestResult>();
	
	/**
	 * Gene2GO����Gene2Path����Ϣ
	 * ÿ�������µ�LsCopedTest���������
	 */
	ArrayList<String[]> lsGene2GOPath = null;
	
	StatisticsTest statisticsTest;

	public AbstFunTest(ArrayList<GeneID> lsCopedIDsTest, ArrayList<GeneID> lsCopedIDsBG, boolean blast) {
		this.lsCopedIDsTest = lsCopedIDsTest;
		this.lsCopedIDsBG = lsCopedIDsBG;
		this.blast = blast;
	}
	
	public AbstFunTest() {}
	
	public void setStatisticsTest(StatisticsTest statisticsTest) {
		this.statisticsTest = statisticsTest;
	}
	
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
	 * ����ܵ�һʱ���趨
	 * ��ȡgenUniID item,item��ʽ�ı�
	 * @param fileName
	 */
	public void setLsBGItem(String fileName) {
		//���Test
		lsTestResult = new ArrayList<StatisticTestResult>();
		lsBGGeneID2Items = new ArrayList<GeneID2LsItem>();
		if (!FileOperate.isFileExist(fileName)) {
			logger.error("no FIle exist: "+ fileName);
		}
		
		ArrayList<String[]> lsTmpGeneID2LsItem = ExcelTxtRead.readLsExcelTxt(fileName, new int[]{1,2}, 1, -1, true);
		lsBGGeneID2Items = readFromBGfile(lsTmpGeneID2LsItem);
		BGnum = lsBGGeneID2Items.size();
	}
	/**
	 * �������geneID item,item list
	 * ����
	 * @param lsTmpGeneID2LsItem
	 * @return
	 */
	protected abstract ArrayList<GeneID2LsItem> readFromBGfile(ArrayList<String[]> lsTmpGeneID2LsItem);
	
	/**
	 * ��һʱ���趨
	 * ��ȡ�����ļ���ָ����ȡĳһ��
	 * @param fileName
	 */
	public void setLsBGAccID(String fileName, int colNum) {
		lsTestResult = new ArrayList<StatisticTestResult>();
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
		BGnum = lsBGGeneID2Items.size();
	}
	/**
	 * ��һʱ���趨
	 * ��ȡ�����ļ���ָ����ȡĳһ��
	 * @param showMessage
	 */
	public void setLsBGCopedID(ArrayList<GeneID> lsBGaccID) {
		lsTestResult = new ArrayList<StatisticTestResult>();
		for (GeneID copedID : lsBGaccID) {
			copedID.setBlastInfo(blastEvalue, blastTaxID);
		}
		this.lsCopedIDsBG = lsBGaccID;
		lsBGGeneID2Items = convert2Item(lsCopedIDsBG);
		BGnum = lsBGGeneID2Items.size();
	}
	/**
	 * Ҫ�ȶ�ȡAccID�ļ�
	 * @return
	 */
	protected ArrayList<GeneID2LsItem> getLsBG() {
		return lsBGGeneID2Items;
	}
	
	public void setLsTestAccID(ArrayList<String> lsCopedID) {
		lsCopedIDsTest = new ArrayList<GeneID>();		
		for (String string : lsCopedID) {
			GeneID copedID = new GeneID(string, taxID, false);
			lsCopedIDsTest.add(copedID);
		}
		initial();
	}
	
	public void setLsTestGeneID(ArrayList<GeneID> lsCopedIDs) {
		this.lsCopedIDsTest = lsCopedIDs;
		initial();
	}
	
	private void initial() {
		lsGene2GOPath = null;
		fillCopedIDInfo(lsCopedIDsTest);
		lsTest = getLsTestFromLsBG(lsCopedIDsTest);
		lsTestResult = new ArrayList<StatisticTestResult>();
	}
	/**
	 * �趨hashgene2CopedID������һ��geneID���Ӧ���accID������
	 * @param lsCopedIDs
	 */
	private void fillCopedIDInfo(ArrayList<GeneID> lsCopedIDs) {
		//////////////  �� �� ��  ////////////////////////
		HashSet<String> setAccID = new HashSet<String>();
		mapGeneUniID2LsGeneID.clear();
		////////////////////////////////////////////
		for (GeneID geneID : lsCopedIDs) {
			//ȥ���࣬accID��ͬȥ��
			if (setAccID.contains(geneID.getAccID())) {
				continue;
			}
			setAccID.add(geneID.getAccID());
			mapGeneUniID2LsGeneID.put(geneID.getGenUniID(), geneID);
		}
	}
	/**
	 * ��copedID��geneUniID�Ȳ���lsBG���Ҳ����ٴ�ͷ����
	 * Ŀ�����Ż�����
	 * ���lsTest����һЩ�µ�gene��Ҳ�����lsBGGeneID2Items��
	 * @param lsTest
	 * @return
	 */
	private ArrayList<GeneID2LsItem> getLsTestFromLsBG(ArrayList<GeneID> lsTest) {
		//ȥ�����õ�
		HashSet<GeneID> setGeneIDs = new HashSet<GeneID>();
		for (GeneID geneID : lsTest) {
			if (blast) {
				geneID.setBlastInfo(blastEvalue, blastTaxID);
			}
			setGeneIDs.add(geneID);
		}
		
		//���û��lsBG���Ͳ������ݿ⣬�������lsBG
		if (lsBGGeneID2Items == null || lsBGGeneID2Items.size() < 1) {
			return convert2Item(setGeneIDs);
		}
		
		HashMap<String, GeneID2LsItem>  mapBGGeneID2Items = new HashMap<String, GeneID2LsItem>();
		for (GeneID2LsItem geneID2LsGO : lsBGGeneID2Items) {
			mapBGGeneID2Items.put(geneID2LsGO.getGeneUniID(), geneID2LsGO);
		}
		ArrayList<GeneID2LsItem> lsout = new ArrayList<GeneID2LsItem>();
		
		//�����lsTest��������ڱ������Ҳ�����Ӧ����Ϣ���򱣴�����list
		ArrayList<GeneID> lsInputNotFindGene = new ArrayList<GeneID>();
		for (GeneID copedID : setGeneIDs) {
			GeneID2LsItem tmpresult = mapBGGeneID2Items.get(copedID.getGenUniID());
			if (tmpresult == null) {
				lsInputNotFindGene.add(copedID);
				continue;
			}
			lsout.add(tmpresult);
		}
		if (lsInputNotFindGene.size() > 0) {
			ArrayList<GeneID2LsItem> lsnew = convert2Item(lsInputNotFindGene);
			lsout.addAll(lsnew);
			lsBGGeneID2Items.addAll(lsnew);
		}
		return lsout;
	}
	
	/**
	 * ��List-CopedIDת��Ϊ
	 * geneID goID,goID,goID����ʽ
	 * ������genUniIDȥ����
	 */
	protected abstract ArrayList<GeneID2LsItem> convert2Item(Collection<GeneID> lsCopedIDs);
	
	/**
	 * ������
	 * ����Gene2ItemPvalue
	 * @param Type
	 * @return
	 * ���ݲ�ͬ��StatisticTestGene2Item�����в�ͬ�����
	 */
	public ArrayList<StatisticTestGene2Item> getGene2ItemPvalue() {
		ArrayList<StatisticTestGene2Item> lsTestResult = new ArrayList<StatisticTestGene2Item>();
		Map<String, StatisticTestResult> mapItem2StatictResult = getMapItemID2StatisticsResult();
		for (GeneID geneID : lsCopedIDsTest) {
			StatisticTestGene2Item statisticTestGene2Item = creatStatisticTestGene2Item();
			statisticTestGene2Item.setGeneID(geneID);
			statisticTestGene2Item.setStatisticTestResult(mapItem2StatictResult);
			lsTestResult.add(statisticTestGene2Item);
		}
		return lsTestResult;
	}
	
	protected abstract StatisticTestGene2Item creatStatisticTestGene2Item();

	/**
	 * �� getTestResult() �Ľ��װ��hash��
	 * @return
	 */
	private HashMap<String, StatisticTestResult> getMapItemID2StatisticsResult() {
		ArrayList<StatisticTestResult> lStatisticTestResults = getTestResult();
		//keyΪСд��item�ͼ�������map
		HashMap<String, StatisticTestResult> mapItem2StatisticsResult = new HashMap<String, StatisticTestResult>();
		for (StatisticTestResult statisticTestResult : lStatisticTestResults) {
			mapItem2StatisticsResult.put(statisticTestResult.getItemName().toLowerCase(), statisticTestResult);
		}
		return mapItem2StatisticsResult;
	}
	/**
	 * booRun ����һ�� �������Ľ����ElimGO��Ҫ���Ǹ÷��� �Խ���Ÿ���
	 * �������Ľ����ElimGO��Ҫ���Ǹ÷���
	 * @throws Exception 
	 * û�оͷ���null
	 */
	public ArrayList<StatisticTestResult> getTestResult() {
		if (statisticsTest == null) {
			statisticsTest = new FisherTest();
		}
		if (lsTestResult != null && lsTestResult.size() > 10) {
			return lsTestResult;
		}
		ArrayList<GeneID2LsItem> lstest = new ArrayList<GeneID2LsItem>();
		for (GeneID2LsItem geneID2LsGO : lsTest) {
			if (!geneID2LsGO.isValidate()) {
				continue;
			}
			lstest.add(geneID2LsGO);
		}
		if (lstest.size() == 0) {
			return null;
		}
		ArrayList<GeneID2LsItem> lsbg = new ArrayList<GeneID2LsItem>();
		for (GeneID2LsItem geneID2LsGO : lsBGGeneID2Items) {
			if (!geneID2LsGO.isValidate()) {
				continue;
			}
			lsbg.add(geneID2LsGO);
		}
		lsTestResult = GeneID2LsItem.getFisherResult(statisticsTest, lstest, lsbg,BGnum);
		for (StatisticTestResult statisticTestResult : lsTestResult) {
			statisticTestResult.setItemTerm(getItemTerm(statisticTestResult.getItemName()));
		}
		return lsTestResult;
	}
	/**
	 * ����ָ����Item��ע��
	 * Ʃ��GOterm��kegg term��
	 * @param item
	 * @return
	 */
	protected abstract String getItemTerm(String item);
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
		for (GeneID2LsItem geneID2LsGO : lsBGGeneID2Items) {
			txtOut.writefileln(geneID2LsGO.toString());
		}
		txtOut.close();
	}
	
	/**
	 * ֻ������GO����
	 * @param goType
	 */
	public void setGoType(String goType) { }
	
}
