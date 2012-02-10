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
	 * 对数据进行log2变换
	 */
	public static final String DATA_CONVERT_LOG2 = "log2";
	/**
	 * 不对数据进行变换
	 */
	public static final String DATA_CONVERT_NONE = "none";
	ArrayList<String> lsRawData = new ArrayList<String>();
	/**
	 * 忽略大小写
	 * string[2];
	 * 0：第几列信息，必须是intege
	 * 1：该列属于编组的名称
	 */
	ArrayList<String[]> lsGroupInfo = new ArrayList<String[]>();
	/**
	 * 忽略大小写
	 * 内容是group1vsgroup2
	 * 0：group1
	 * 1：group2
	 * group必须和lsGroupInfo中的string一致
	 */
	ArrayList<String[]> lsCompInfo = new ArrayList<String[]>();
	/**
	 * 标准化数据的文件路径
	 */
	String normData = "";
	/**
	 * 标准化方法
	 */
	String NormType = "";
	/**
	 * 最后生成的脚本，直接写入txt就好
	 */
	ArrayList<String> lsScript = null;
	/**
	 * 默认不进行变换
	 */
	String dataConvertType = DATA_CONVERT_NONE;
	/**
	 * 对标准化的数据进行何种变换
	 * 默认不进行变换
	 * 可以采用DATA_CONVERT_LOG2等方法
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
	 * 忽略大小写
	 * 内容是group1vsgroup2
	 * 0：group1
	 * 1：group2
	 * group必须和lsGroupInfo中的string一致
	 */
	public void setLsCompInfo(ArrayList<String[]> lsCompInfo) {
		this.lsCompInfo = lsCompInfo;
	}
	/**
	 * 忽略大小写
	 * string[2];
	 * 0：第几列信息，必须是intege
	 * 1：该列属于编组的名称
	 */
	public void setLsGroupInfo(ArrayList<String[]> lsGroupInfo) {
		this.lsGroupInfo = lsGroupInfo;
	}
	/**
	 * 是否需要标准化数据，注意只有原始文件存在才能标准化数据
	 */
	boolean booNormRawData = false;
	/**
	 * 选择标准化的类型，在NORM_RMA等中选择
	 * @param normType
	 */
	public void setNormType(String normType) {
		this.NormType = normType;
	}
	
	/**
	 * 产生标准化的R脚本
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
	 * 产生挑选差异基因的R脚本
	 * @return
	 */
	private ArrayList<String> generateScriptDifGen() {
		lsScript = new ArrayList<String>();
		lsScript.add("library(limma)");
		lsScript.add(scriptReadNormData());
		return lsScript;
	}
	/**
	 * 生成读取cel文件的script
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
	 * 生成挑选差异基因的script
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
	 * 获得设计比较矩阵
	 * @return
	 */
	private String getDesign()
	{
		//按照列进行排序
		Collections.sort(lsGroupInfo, new Comparator<String[]>() {
			public int compare(String[] o1, String[] o2) {
				Integer m1 = Integer.parseInt(o1[0]);
				Integer m2 = Integer.parseInt(o2[0]);
				return m1.compareTo(m2);
			}
		});
		//确定起的实验分组--譬如WT，与具体的groupID之间的关系
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
	 * 必须首先对lsGroupInfo进行排序
	 * 当指定试验的列数、基因列后，将这些内容写入具体的文件，写入txt文本
	 * @return
	 */
	private void setTmpNormDataTxtFile(int colNameID, String txtFileName)
	{
		int[] columnID = new int[lsGroupInfo.size()+1];
		columnID[0] = colNameID;
		//获得需要计算的列数
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
	 * 将标准化数据写入文本的script
	 */
	private String scriptWriteNormData()
	{
		String script = "write.exprs(esetOld, file=\"" + getNormFile()+ "\")";
		return script;
	}
	/**
	 * 读取标准化数据的脚本
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
	 * 获得标准化好的文件名称
	 * @return
	 */
	private String getNormFile() {
		//TODO
		return null;
	}
}
