package com.novelbio.analysis.annotation.pathway.kegg.prepare;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.domain.geneanno.AgeneUniID;
import com.novelbio.database.domain.geneanno.NCBIID;
import com.novelbio.database.domain.geneanno.UniProtID;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.service.servgeneanno.ManageNCBIUniID;



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
	public static String[] getAccID(String accIDFile,int rowStartNum,int colNum) throws Exception {
		colNum--;
		if (colNum < 0) {
			colNum = 0;
		}
		TxtReadandWrite txtReadandWrite = new TxtReadandWrite(accIDFile);
		List<String> lsAccID = new ArrayList<String>();
		for (String string : txtReadandWrite.readlines()) {
			String[] ss = string.split("\t");
			lsAccID.add(ss[colNum]);
		}
		txtReadandWrite.close();
		return lsAccID.toArray(new String[0]);
	}
	/**
	 * 读取指定excel2003文本，获得geneID信息数组, 如果类似XM_002121.1类型，那么将.1去除
	 * @param rowStartNum 从第几行开始读
	 * @param colNum geneID在第几列
	 * @param accIDFile 文本名
	 * @return
	 * @throws Exception
	 */
	public static String[] getAccID(int rowStartNum,int colNum,String accIDFile) throws Exception {
		ExcelOperate excelOperate = new ExcelOperate(accIDFile);
		ArrayList<String[]> lsGeneID = excelOperate.readLsExcel(rowStartNum, colNum, -1, colNum);
		excelOperate.close();
		String[] geneID2=new String[lsGeneID.size()];
		for (int i = 0; i < lsGeneID.size(); i++) {
			geneID2[i]=GeneID.removeDot(lsGeneID.get(i)[0]);
		}
		return geneID2;
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
		ManageNCBIUniID servNCBIID = ManageNCBIUniID.getInstance();
		Hashtable<String, String> hashAcc2Gen = new Hashtable<String, String>();
		String geneID[] =KGprepare.getAccID(accIDFile, rowStartNum, colNum);
 
		for (int i = 0; i < geneID.length; i++) {
			///////////////////如果类似XM_002121.1类型，那么将.1去除////////////////////////////////////
			AgeneUniID ageneUniID = AgeneUniID.creatAgeneUniID(GeneID.IDTYPE_GENEID);
			String accID = GeneID.removeDot(geneID[i]);
			ageneUniID.setAccID(accID); 
			ageneUniID.setTaxID(taxID);
			ArrayList<? extends AgeneUniID> lsNcbiids=null;//rvNCBIID.queryLsAgeneUniID(ageneUniID);
			//先查找NCBIID表
			if (lsNcbiids != null && lsNcbiids.size() > 0) {
				String tmpGeneID = lsNcbiids.get(0).getGenUniID();
				if (Sep) {
					hashAcc2Gen.put(accID,tmpGeneID);
				} else {
					hashAcc2Gen.put(tmpGeneID,tmpGeneID);
				}
			} else {
				ageneUniID = AgeneUniID.creatAgeneUniID(GeneID.IDTYPE_UNIID);
				ageneUniID.setAccID(accID); 
				ageneUniID.setTaxID(taxID);
				ArrayList<? extends AgeneUniID> lsUniProtIDs = null;//servNCBIID.queryLsAgeneUniID(ageneUniID);
				if (lsUniProtIDs != null && lsUniProtIDs.size() > 0) {
					String tmpGeneID = lsUniProtIDs.get(0).getGenUniID();
					if (Sep) {
						hashAcc2Gen.put(accID,tmpGeneID);
					}
					else {
						hashAcc2Gen.put(tmpGeneID,tmpGeneID);
					}
				}
			}
			
			//没找到的话，查找UniProtID表
		
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
