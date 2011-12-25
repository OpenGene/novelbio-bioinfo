package com.novelbio.analysis.tools.compare;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.broadinstitute.sting.jna.lsf.v7_0_6.LibBat.newDebugLog;

import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * ѡ�������
 * ÿ������ѡ��������Ҫѡ�����
 * �����Щ���еļ��кϲ���һ��table�У��ϲ���IDΪѡ�е�ID��
 * @author zong0jie
 */
public class CombineTab {
	
	public static void main(String[] args) {
		String parentFile = "/media/winE/NBC/Project/Project_Q_Lab/tophat/GO/";
		String file1 = parentFile + "1vs0gene_exp.diff.xls";
		String file2 = parentFile + "2vs0gene_exp.diff.xls";
		String file3 = parentFile + "3vs0gene_exp.diff.xls";

		CombineTab comb = new CombineTab();
		comb.setColDetai(file1, "1vs0", 2,3,4,5,6);
		comb.setColDetai(file2, "2vs0", 3,4,5,6);
		comb.setColDetai(file3, "3vs0", 3,4,5,6);

		comb.setColID(1);
		comb.exeToFile(parentFile + "Maize_All_nofilter.xls");
	}
	
	
	
	
	
	
	
	
	
	
	
	private static Logger logger = Logger.getLogger(CombineTab.class);
	/**
	 * ÿ������������--Ҳ����ÿ��tab���ļ���
	 */
	LinkedHashMap<String, String> hashCond = new LinkedHashMap<String, String>();
	
	int[] colID;
	public void setColID(int... colID)
	{
		for (int i = 0; i < colID.length; i++) {
			colID[i] = colID[i] - 1;
		}
		this.colID = colID;
	}
	/**
	 * ����ÿ���ļ����趨������Ҫ�����ļ��У�����flag��
	 */
	LinkedHashMap<String, int[]> hashCodColDetail = new LinkedHashMap<String, int[]>();
	/**
	 *  ���ÿ���ļ���, ����ÿ���ļ����趨����ID��
	 */
	public void setColDetai(String condTxt, String codName, int... colDetai) {
		for (int i = 0; i < colDetai.length; i++) {
			colDetai[i] = colDetai[i] - 1;
		}
		hashCodColDetail.put(condTxt, colDetai);
		hashCond.put(condTxt,codName);
	}
	/**
	 *  ���ÿ���ļ���, ����ÿ���ļ����趨����ID��
	 */
	@Deprecated
	public void setColDetai(String condTxt,int... colDetai) {
		for (int i = 0; i < colDetai.length; i++) {
			colDetai[i] = colDetai[i] - 1;
		}
		hashCodColDetail.put(condTxt, colDetai);
		hashCond.put(condTxt,FileOperate.getFileNameSep(condTxt)[0]);
	}
 
	/**
	 * ���ظ�������ID
	 */
	LinkedHashMap<String, String[]> hashID = new LinkedHashMap<String,String[]>();
	/**
	 * cod--hash-string-string[]
	 * colID--colID+colDetail
	 */
	HashMap<String, LinkedHashMap<String, String[]>> hashCod2LsInfo = new LinkedHashMap<String, LinkedHashMap<String,String[]>>();
	
	
	public void exeToFile(String txtOutFile)
	{
		String title[] = new String[0];
		for (Entry<String, String> entry : hashCond.entrySet()) {
			String string = entry.getKey();
			
			ArrayList<String[]> lsInfoCod = getLsInfo(string);
			if (title.length == 0) {
				title = new String[colID.length];
				for (int i = 0; i < title.length; i++) {
					title[i] = lsInfoCod.get(0)[i];
				}
			}
			String[] subTitle = new String[hashCodColDetail.get(string).length];
			for (int i = 0; i < subTitle.length; i++) {
				subTitle[i] = lsInfoCod.get(0)[i+colID.length]+"_"+entry.getValue();
			}
			title = ArrayOperate.combArray(title, subTitle, 0);
			setHashID(string, lsInfoCod.subList(1, lsInfoCod.size()));
		}
		ArrayList<String[]> lsResult = combInfo();
		lsResult.add(0,title);
		TxtReadandWrite txtOut = new TxtReadandWrite(txtOutFile, true);
		txtOut.ExcelWrite(lsResult, "\t", 1, 1);
	}
	
	
	/**
	 * ��ȡָ���ı�����Ϣ
	 * ����������
	 * @param cond
	 * @return
	 */
	private ArrayList<String[]> getLsInfo(String condTxt) {
		int[] colDetail = hashCodColDetail.get(condTxt);
		int[] colRead = new int[colID.length + colDetail.length];
		//�ϲ���
		for (int i = 0; i < colID.length; i++) {
			colRead[i] = colID[i] + 1;
		}
		for (int i = 0; i < colDetail.length; i++) {
			colRead[colID.length+i] = colDetail[i] + 1;
		}
		
		ArrayList<String[]> lsTmpInfo = ExcelTxtRead.readLsExcelTxt(condTxt, colRead, 1, -1, true);
		return lsTmpInfo;
	}
	
	/**
	 * �趨Ψһ�е���Ϣ��Ȼ�󽫾������Ϣװ������hash����
	 * @param lsTmpInfo �����list��Ϣ������flag��
	 * @param colIDLen ͷ������colID
	 * �Զ�ȥ���࣬������һ�γ��ֵ�ID
	 */
	private void setHashID(String cod, List<String[]> lsTmpInfo)
	{
		String sep = "@//@";
		//�����colID2colDetail��Ϣ
		LinkedHashMap<String, String[]> hashColID2Detail = new LinkedHashMap<String, String[]>();
		hashCod2LsInfo.put(cod, hashColID2Detail);
		if (colID.length > lsTmpInfo.get(0).length) {
			logger.error("������������������");
		}
		for (String[] strings : lsTmpInfo) {
			String colIDstr = ""; String[] colIDarray = new String[colID.length];
			//flag�е���Ϣ
			for (int i = 0; i < colID.length; i++) {
				colIDstr = colIDstr + sep + strings[i];
				colIDarray[i] = strings[i];
			}
			//ɾ��flag�е���Ϣ
			String[] tmpDetail = new String[strings.length - colID.length];
			for (int i = colID.length; i < strings.length; i++) {
				tmpDetail[i - colID.length] = strings[i];
			}
			//�Ѿ����˾�����
			if (hashColID2Detail.containsKey(colIDstr)) {
				continue;
			}
			hashColID2Detail.put(colIDstr, tmpDetail);
			//���ظ�������ID��Ϊȡ������׼��
			hashID.put(colIDstr,colIDarray);
		}
	}
	/**
	 * ���ȡ�����Ľ��
	 * @return
	 */
	private ArrayList<String[]> combInfo()
	{
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		for (String strColID : hashID.keySet()) {
			String[] tmpAllCodInfo = hashID.get(strColID);
			//ÿ��ID�����ж������ȫ������һ��
			for (String strCod : hashCond.keySet()) {
				LinkedHashMap<String, String[]> hashCol2ColDetail = hashCod2LsInfo.get(strCod);
				String[] tmpDetail = hashCol2ColDetail.get(strColID);
				//û�ҵ������ÿո��滻
				if (tmpDetail == null) {
					tmpDetail = new String[hashCodColDetail.get(strCod).length];
					for (int i = 0; i < tmpDetail.length; i++) {
						tmpDetail[i] = "";
					}
				}
				//�ϲ���
				tmpAllCodInfo = ArrayOperate.combArray(tmpAllCodInfo, tmpDetail, 0);
			}
			lsResult.add(tmpAllCodInfo);
		}
		return lsResult;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
