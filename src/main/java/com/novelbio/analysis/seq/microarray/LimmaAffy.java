package com.novelbio.analysis.seq.microarray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.jasper.tagplugins.jstl.core.ForEach;

import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;

public class LimmaAffy {
	public static final String NORM_RMA = "RMA";
	public static final String NORM_GCRMA = "GCRMA";
	/**
	 * �����ݽ���log2�任
	 */
	public static final String DATA_CONVERT_LOG2 = "log2";
	/**
	 * �������ݽ��б任
	 */
	public static final String DATA_CONVERT_NONE = "none";
	ArrayList<String> lsRawData = new ArrayList<String>();
	/**
	 * ���Դ�Сд
	 * string[2];
	 * 0���ڼ�����Ϣ��������intege
	 * 1���������ڱ��������
	 */
	ArrayList<String[]> lsGroupInfo = new ArrayList<String[]>();
	/**
	 * ���Դ�Сд
	 * ������group1vsgroup2
	 * 0��group1
	 * 1��group2
	 * group�����lsGroupInfo�е�stringһ��
	 */
	ArrayList<String[]> lsCompInfo = new ArrayList<String[]>();
	/**
	 * ��׼�����ݵ��ļ�·��
	 */
	String normData = "";
	/**
	 * ��׼������
	 */
	String NormType = "";
	/**
	 * ������ɵĽű���ֱ��д��txt�ͺ�
	 */
	ArrayList<String> lsScript = null;
	/**
	 * Ĭ�ϲ����б任
	 */
	String dataConvertType = DATA_CONVERT_NONE;
	/**
	 * �Ա�׼�������ݽ��к��ֱ任
	 * Ĭ�ϲ����б任
	 * ���Բ���DATA_CONVERT_LOG2�ȷ���
	 */
	public void setDataConvertType(String dataConvertType) {
		this.dataConvertType = dataConvertType;
	}
	public void setRawData(String rawDataFile)
	{
		lsRawData.add(rawDataFile);
	}
	public void setNormData(String normDataFile)
	{
		this.normData = normDataFile;
	}
	/**
	 * ���Դ�Сд
	 * ������group1vsgroup2
	 * 0��group1
	 * 1��group2
	 * group�����lsGroupInfo�е�stringһ��
	 */
	public void setLsCompInfo(ArrayList<String[]> lsCompInfo) {
		this.lsCompInfo = lsCompInfo;
	}
	/**
	 * ���Դ�Сд
	 * string[2];
	 * 0���ڼ�����Ϣ��������intege
	 * 1���������ڱ��������
	 */
	public void setLsGroupInfo(ArrayList<String[]> lsGroupInfo) {
		this.lsGroupInfo = lsGroupInfo;
	}
	/**
	 * �Ƿ���Ҫ��׼�����ݣ�ע��ֻ��ԭʼ�ļ����ڲ��ܱ�׼������
	 */
	boolean booNormRawData = false;
	/**
	 * ѡ���׼�������ͣ���NORM_RMA����ѡ��
	 * @param normType
	 */
	public void setNormType(String normType) {
		this.NormType = normType;
	}
	
	/**
	 * ������׼����R�ű�
	 * @return
	 */
	private ArrayList<String> generateScriptNorm() {
		lsScript = new ArrayList<String>();
		lsScript.add("library(affy)");
		lsScript.add("library(gcrma)");
		lsScript.add(scriptReadRawDataCel());
		return lsScript;
	}
	/**
	 * ������ѡ��������R�ű�
	 * @return
	 */
	private ArrayList<String> generateScriptDifGen() {
		lsScript = new ArrayList<String>();
		lsScript.add("library(limma)");
		lsScript.add(scriptReadNormData());
		return lsScript;
	}
	/**
	 * ���ɶ�ȡcel�ļ���script
	 * @return
	 */
	private String scriptReadRawDataCel()
	{
		String script = "data = ReadAffy(";
		for (String string : lsRawData) {
			script = script + "\"" + string + "\", ";
		}
		script = script + ")\r\n";
		if (NormType.equals(LimmaAffy.NORM_RMA)) {
			script = script + "esetOld = rma(data)\r\n";
		}
		else if (NormType.equals(LimmaAffy.NORM_GCRMA)) {
			script = script + "esetOld = gcrma(data)\r\n";
		}
		return script;
	}
	/**
	 * ������ѡ��������script
	 * @return
	 */
	private String scriptDifGeneFind()
	{
		String script = "";
		if (dataConvertType.equals(DATA_CONVERT_LOG2)) {
			script = "eset = log2(eset)";
		}
		for (String[] strings : lsGroupInfo) {
			
		}
		script = script + "design = model.matrix(~ -1+factor (c(rep(1,15),rep(2,41))))";
		
		return null;
	}
	/**
	 * �����ƱȽϾ���
	 * @return
	 */
	private String getDesign()
	{
		//�����н�������
		Collections.sort(lsGroupInfo, new Comparator<String[]>() {
			public int compare(String[] o1, String[] o2) {
				Integer m1 = Integer.parseInt(o1[0]);
				Integer m2 = Integer.parseInt(o2[0]);
				return m1.compareTo(m2);
			}
		});
		//ȷ�����ʵ�����--Ʃ��WT��������groupID֮��Ĺ�ϵ
		HashMap<String, Integer> hashName2GroupID = new HashMap<String, Integer>();
		int tmpGroupID = 1;
		for (String[] strings : lsGroupInfo) {
			if (hashName2GroupID.containsKey(strings[1].toLowerCase())) {
				continue;
			}
			hashName2GroupID.put(strings[1].toLowerCase(), tmpGroupID);
			tmpGroupID ++;
		}
		for (String[] strings : lsGroupInfo) {
			if (strings[0]) {
				
			}
			
		}
		
		return null;
	}
	/**
	 * �������ȶ�lsGroupInfo��������
	 * ��ָ������������������к󣬽���Щ����д�������ļ���д��txt�ı�
	 * @return
	 */
	private void setTmpNormDataTxtFile(int colNameID, String txtFileName)
	{
		int[] columnID = new int[lsGroupInfo.size()+1];
		columnID[0] = colNameID;
		//�����Ҫ���������
		for (int i = 0; i < lsGroupInfo.size(); i++)
		{
			String[] strings = lsGroupInfo.get(i);
			columnID[i+1] = Integer.parseInt(strings[0]);
		}		
		ArrayList<String[]> lsTmpNormData = ExcelTxtRead.readLsExcelTxt(getNormFile(), columnID, 1, -1);
		TxtReadandWrite txtWrite = new TxtReadandWrite(txtFileName, true);
		txtWrite.ExcelWrite(lsTmpNormData, "\t", 1, 1);
	}
	/**
	 * ����׼������д���ı���script
	 */
	private String scriptWriteNormData()
	{
		String script = "write.exprs(esetOld, file=\"" + getNormFile()+ "\")";
		return script;
	}
	/**
	 * ��ȡ��׼�����ݵĽű�
	 * @return
	 */
	private String scriptReadNormData() {
		String script = "eset=read.table(file=\""+getNormFile()+"\",he=T,sep=\"\\t\",row.names=1)";
		return script;
	}
	
	private ArrayList<String[]> getNormData()
	{
		ArrayList<String[]> lsNormData = ExcelTxtRead.readLsExcelTxt(getNormFile(), 1);
		return lsNormData;
	}
	
	
	/**
	 * ��ñ�׼���õ��ļ�����
	 * @return
	 */
	private String getNormFile() {
		//TODO
		return null;
	}
}
