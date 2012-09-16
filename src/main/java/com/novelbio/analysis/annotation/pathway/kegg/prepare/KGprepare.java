package com.novelbio.analysis.annotation.pathway.kegg.prepare;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.domain.geneanno.NCBIID;
import com.novelbio.database.domain.geneanno.UniProtID;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.service.servgeneanno.ServNCBIID;
import com.novelbio.database.service.servgeneanno.ServUniProtID;



/**
 * KEGG��׼�����������������ȡ�������Ϣ��
 * @author zong0jie
 *
 */
public class KGprepare {
	
	
	/**
	 * ��ȡָ��txt�ı������geneID��Ϣ����, �������XM_002121.1���ͣ���ô��.1ȥ��
	 * @param accIDFile �ı���
	 * @param rowStartNum �ӵڼ��п�ʼ��
	 * @param colNum geneID�ڵڼ���
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
			geneID2[i] = GeneID.removeDot(geneID[i][0]);
		}
		return geneID2;
	}
	/**
	 * 
	 * ��ȡָ��excel2003�ı������geneID��Ϣ����, �������XM_002121.1���ͣ���ô��.1ȥ��
	 * @param rowStartNum �ӵڼ��п�ʼ��
	 * @param colNum geneID�ڵڼ���
	 * @param accIDFile �ı���
	 * @return
	 * @throws Exception
	 */
	public static String[] getAccID(int rowStartNum,int colNum,String accIDFile) throws Exception {
		ExcelOperate excelOperate = new ExcelOperate();
		excelOperate.openExcel(accIDFile);
		ArrayList<String[]> lsGeneID = excelOperate.ReadLsExcel(rowStartNum, colNum, excelOperate.getRowCount(), colNum);
		String[] geneID2=new String[lsGeneID.size()];
		for (int i = 0; i < lsGeneID.size(); i++) {
			geneID2[i]=GeneID.removeDot(lsGeneID.get(i)[0]);
		}
		return geneID2;
	}
	/**
	 * ��ȡgene��Ϣ������GeneID��Ϣ����geneID�ķ���geneID��û�еķ���UniProtID, �������XM_002121.1���ͣ���ô��.1ȥ��
	 * @param accIDFile 
	 * @param rowStartNum
	 * @param colNum
	 * @param taxID
	 * @param Sep �Ƿ�ֿ�ָ����ͬ�����̽�룬True���ֿ� ��False���ϲ�
	 * @return ���ز��ظ���ArrayList-String[2]��Sep=true: 0:accID -1: GeneID/UniProtID  Sep=false: 0:GeneID/UniProtID -1: GeneID/UniProtID 
	 * @throws Exception
	 */
	public ArrayList<String[]> getLsAcc2GenID(String accIDFile,int rowStartNum,int colNum,int taxID,boolean Sep) throws Exception 
	{
		ServNCBIID servNCBIID = new ServNCBIID();
		ServUniProtID servUniProtID = new ServUniProtID();
		Hashtable<String, String> hashAcc2Gen = new Hashtable<String, String>();
		String geneID[] =KGprepare.getAccID(accIDFile, rowStartNum, colNum);
 
		for (int i = 0; i < geneID.length; i++) {
			///////////////////�������XM_002121.1���ͣ���ô��.1ȥ��////////////////////////////////////
			NCBIID ncbiid=new NCBIID();UniProtID uniProtID=new UniProtID();
			String accID = GeneID.removeDot(geneID[i]);
			ncbiid.setAccID(accID);ncbiid.setTaxID(taxID);
			uniProtID.setAccID(accID);uniProtID.setTaxID(taxID);
			ArrayList<NCBIID> lsNcbiids=servNCBIID.queryLsNCBIID(ncbiid);
			//�Ȳ���NCBIID��
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
			//û�ҵ��Ļ�������UniProtID��
			ArrayList<UniProtID> lsUniProtIDs=servUniProtID.queryLsUniProtID(uniProtID);
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
		//����ǽ���ļ������������½����
		//GOID,GOterm,��GOID�Ĳ������Num���ܲ������Num����GOID���ܻ���Num���ܻ����Num,
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
