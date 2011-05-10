package com.novelbio.analysis.annotation.pathway.kegg.prepare;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.DAO.FriceDAO.DaoFSNCBIID;
import com.novelbio.database.DAO.FriceDAO.DaoFSUniProtID;
import com.novelbio.database.entity.friceDB.NCBIID;
import com.novelbio.database.entity.friceDB.UniProtID;



/**
 * KEGG的准备工作，用这个来读取基因的信息等
 * @author zong0jie
 *
 */
public class KGprepare {
	
	
	/**
	 * 读取指定txt文本，获得geneID信息数组, 如果类似XM_002121.1类型，那么将.1去除
	 * @param accIDFile 文本名
	 * @param rowStartNum 从第几行开始读
	 * @param colNum geneID在第几列
	 * @return
	 * @throws Exception
	 */
	public static String[] getAccID(String accIDFile,int rowStartNum,int colNum) throws Exception 
	{
		TxtReadandWrite txtReadandWrite = new TxtReadandWrite();
		txtReadandWrite.setParameter(accIDFile, false, true);
		String[][] geneID=txtReadandWrite.ExcelRead("\t", rowStartNum, colNum,txtReadandWrite.ExcelRows(),colNum);
		String[] geneID2=new String[geneID.length];
		for (int i = 0; i < geneID.length; i++) {
			geneID2[i]=removeDot(geneID[i][0]);
		}
		return geneID2;
	}
	/**
	 * 读取指定excel2003文本，获得geneID信息数组, 如果类似XM_002121.1类型，那么将.1去除
	 * @param accIDFile 文本名
	 * @param rowStartNum 从第几行开始读
	 * @param colNum geneID在第几列
	 * @return
	 * @throws Exception
	 */
	public static String[] getAccID(int rowStartNum,int colNum,String accIDFile) throws Exception 
	{
		ExcelOperate excelOperate = new ExcelOperate();
		excelOperate.openExcel(accIDFile);
		String[][] geneID = excelOperate.ReadExcel(rowStartNum, colNum, excelOperate.getRowCount(), colNum);
		String[] geneID2=new String[geneID.length];
		for (int i = 0; i < geneID.length; i++) {
			geneID2[i]=removeDot(geneID[i][0]);
		}
		return geneID2;
	}
	/**
	 *  首先除去空格
	 * 如果类似XM_002121.1类型，那么将.1去除
	 * @param accID
	 * @return accID without .1
	 */
	public static String removeDot(String accID)
	{
		String tmpGeneID = accID.trim();
		int dotIndex = tmpGeneID.lastIndexOf(".");
		//如果类似XM_002121.1类型
		if (dotIndex>0 && tmpGeneID.length() - dotIndex == 2) {
			tmpGeneID = tmpGeneID.substring(0,dotIndex);
		}
		return tmpGeneID;
	}
	/**
	 * 读取gene信息并返回GeneID信息，有geneID的返回geneID，没有的返回UniProtID, 如果类似XM_002121.1类型，那么将.1去除
	 * @param accIDFile 
	 * @param rowStartNum
	 * @param colNum
	 * @param taxID
	 * @param Sep 是否分开指向相同基因的探针，True：分开 ，False：合并
	 * @return 返回不重复的ArrayList-String[2]：Sep=true: 0:accID -1: GeneID/UniProtID  Sep=false: 0:GeneID/UniProtID -1: GeneID/UniProtID 
	 * @throws Exception
	 */
	public ArrayList<String[]> getLsAcc2GenID(String accIDFile,int rowStartNum,int colNum,int taxID,boolean Sep) throws Exception 
	{
		Hashtable<String, String> hashAcc2Gen = new Hashtable<String, String>();
		String geneID[] =KGprepare.getAccID(accIDFile, rowStartNum, colNum);
 
		for (int i = 0; i < geneID.length; i++) {
			///////////////////如果类似XM_002121.1类型，那么将.1去除////////////////////////////////////
			NCBIID ncbiid=new NCBIID();UniProtID uniProtID=new UniProtID();
			String accID = removeDot(geneID[i]);
			ncbiid.setAccID(accID);ncbiid.setTaxID(taxID);
			uniProtID.setAccID(accID);uniProtID.setTaxID(taxID);
			ArrayList<NCBIID> lsNcbiids=DaoFSNCBIID.queryLsNCBIID(ncbiid);
			//先查找NCBIID表
			if (lsNcbiids != null && lsNcbiids.size() > 0) 
			{
				String tmpGeneID = lsNcbiids.get(0).getGeneId() + "" ;
				if (Sep) {
					hashAcc2Gen.put(accID,tmpGeneID);
				}
				else {
					hashAcc2Gen.put(tmpGeneID,tmpGeneID);
				}
				continue;
			}
			//没找到的话，查找UniProtID表
			ArrayList<UniProtID> lsUniProtIDs=DaoFSUniProtID.queryLsUniProtID(uniProtID);
			if (lsUniProtIDs != null && lsUniProtIDs.size() > 0) {
				String tmpGeneID = lsUniProtIDs.get(0).getUniID();
				if (Sep) {
					hashAcc2Gen.put(accID,tmpGeneID);
				}
				else {
					hashAcc2Gen.put(tmpGeneID,tmpGeneID);
				}
			}
		}
		ArrayList<String[]> lsAcc2GenID=new ArrayList<String[]>();
		
		
		Enumeration keys=hashAcc2Gen.keys();
		//这个是结果文件，保存了如下结果：
		//GOID,GOterm,该GOID的差异基因Num，总差异基因Num，该GOID的总基因Num，总基因的Num,
		while(keys.hasMoreElements())
		{
		    String[] tmpAcc2GenID=new String[2];
		    tmpAcc2GenID[0] =(String)keys.nextElement();
		    tmpAcc2GenID[1] = (String) hashAcc2Gen.get(tmpAcc2GenID[0]);
		    lsAcc2GenID.add(tmpAcc2GenID);
		}
		return lsAcc2GenID;
	}
	
	
}
