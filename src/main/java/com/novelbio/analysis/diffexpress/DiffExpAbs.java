package com.novelbio.analysis.diffexpress;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.novelbio.base.PathDetail;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.geneanno.SepSign;
import com.novelbio.generalConf.NovelBioConst;

public abstract class DiffExpAbs {
	public static final int LIMMA = 10;
	public static final int DESEQ = 20;
	public static final int DEGSEQ = 30;
	public static final int EDEGR = 40;
	public static final int TTest = 50;
	
	String workSpace;
	String fileNameRawdata = "";
	String outScript = "";
	
	ArrayList<String[]> lsGeneInfo = new ArrayList<String[]>();
	/**
	 * 一系列的表示基因分组的列，输入的时候就按照col进行了排序<br>
	 * 0: colNum, 实际number<br>
	 * 1: SampleGroupName
	 */
	ArrayList<String[]> lsSampleColumn2GroupName;
	/** 基因名
	 * 对应样本名
	 * 对应平均表达值
	 */
	HashMap<String, HashMap<String, Double>> mapGeneID_2_Sample2MeanValue;
	/**基因唯一ID，必须没有重复 */
	int colAccID = 0;
	/**
	 * 比较组与相应的输出文件名，可以输入一系列组
	 * map: condition to compare group <br>
	 * FileName <br>
	 * To<br>
	 * 0：treatment<br>
	 * 1：control
	 */
	HashMap<String, String[]> mapOutFileName2Compare = new LinkedHashMap<String, String[]>();
	
	boolean calculate = false;
	
	String rawScript = "";
	
	public DiffExpAbs() {
		setRworkspace();
		setOutScriptPath();
		setFileNameRawdata();
	}
	public void setRawScript(String rawScript) {
		this.rawScript = rawScript;
	}
	/**
	 * 一系列的表示基因分组的列<br>
	 * 0: colNum, 实际number<br>
	 * 1: SampleGroupName
	 */
	public void setCol2Sample(ArrayList<String[]> lsSampleColumn2GroupName) {
		this.lsSampleColumn2GroupName = lsSampleColumn2GroupName;
		//按列进行排序
		Collections.sort(lsSampleColumn2GroupName, new Comparator<String[]>() {
			public int compare(String[] o1, String[] o2) {
				Integer col1 = Integer.parseInt(o1[0]);
				Integer col2 = Integer.parseInt(o2[0]);
				return col1.compareTo(col2);
			}
		});
		calculate = false;
	}
	/**
	 * 设定输出文件夹和比较组
	 * @param fileName
	 * @param comparePair <br>
	 * 0: treatment<br>
	 * 1: control
	 */
	public void addFileName2Compare(String fileName, String[] comparePair) {
		mapOutFileName2Compare.put(fileName, comparePair);
		calculate = false;
	}
	public void setGeneInfo(ArrayList<String[]> lsGeneInfo) {
		this.lsGeneInfo = lsGeneInfo;
		calculate = false;
	}
	/** 基因标记列，实际列 */
	public void setColID(int colID) {
		this.colAccID = colID - 1;
		calculate = false;
	}
	protected abstract void setOutScriptPath();
	/** 设定原始数据的文件名 */
	protected abstract void setFileNameRawdata();
	
	protected void setRworkspace() {
		workSpace = PathDetail.getRworkspaceTmp();
	}
	/** 仅供测试 */
	public String getOutScript() {
		generateScript();
		return outScript;
	}
	/** 仅供测试 */
	public String getFileNameRawdata() {
		return fileNameRawdata;
	}
	
	/** 返回是否为log过的值
	 * 主要用于limma，其实就是判断最大的表达值是否大于40
	 *  */
	public boolean isLogValue() {
		ArrayList<Double> lsValue = new ArrayList<Double>();
		for (String[] strings : lsGeneInfo) {
			int colNum = Integer.parseInt(lsSampleColumn2GroupName.get(0)[0]) - 1;
			try {
				double tmpValue = Double.parseDouble(strings[colNum]);
				lsValue.add(tmpValue);
			} catch (Exception e) { }
		}
		double result = MathComput.median(lsValue, 98);
		if (result < 40) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public ArrayList<String> getResultFileName() {
		for (String fileName : mapOutFileName2Compare.keySet()) {
			FileOperate.DeleteFileFolder(fileName);
		}
		calculateResult();
		return ArrayOperate.getArrayListKey(mapOutFileName2Compare);
	}
	/** 计算差异 */
	protected void calculateResult() {
		if (calculate) {
			return;
		}
		calculate = true;
		writeToGeneFile();
		setMapSample_2_time2value();
		generateScript();
		run();
		modifyResult();
		clean();
	}
	/**
	 * 将输入的文件重整理成所需要的txt格式写入文本
	 */
	protected void writeToGeneFile() {
		TxtReadandWrite txtWrite = new TxtReadandWrite(fileNameRawdata, true);
		txtWrite.ExcelWrite(getAnalysisGeneInfo());
	}
	/**
	 * 获得选定的基因ID和具体值
	 * 排序方式按照输入的lsSampleColumn2GroupName进行排序，不做调整
	 * @return
	 * 0： geneID
	 * 1-n：value
	 */
	protected  ArrayList<String[]> getAnalysisGeneInfo() {
		ArrayList<String[]> lsResultGeneInfo = new ArrayList<String[]>();
		for (String[] strings : lsGeneInfo) {
			String[] tmpResult = new String[lsSampleColumn2GroupName.size() + 1];
			tmpResult[0] = strings[colAccID];
			for (int i = 0; i < lsSampleColumn2GroupName.size(); i++) {
				int colNum = Integer.parseInt(lsSampleColumn2GroupName.get(i)[0]) - 1;
				tmpResult[i + 1] = strings[colNum];
			}
			lsResultGeneInfo.add(tmpResult);
		}
		return lsResultGeneInfo;
	}
	private void setMapSample_2_time2value() {
		mapGeneID_2_Sample2MeanValue = new HashMap<String, HashMap<String,Double>>();
		for (String[] geneID2Info : lsGeneInfo) {
			String geneName = geneID2Info[colAccID];
			try {
				HashMap<String, Double> mapTime2value = mapTime2AvgValue(geneID2Info);
				mapGeneID_2_Sample2MeanValue.put(geneName, mapTime2value);
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}

	/**
	 * 获得 
	 * 样本时期--该时期内平均值
	 * 的map
	 * @param info
	 * @return
	 */
	protected HashMap<String, Double> mapTime2AvgValue(String[] info) {
		HashMap<String, ArrayList<Double>> mapTime2LsValue = new HashMap<String, ArrayList<Double>>();
		
		for (int i = 0; i < lsSampleColumn2GroupName.size(); i++) {
			int colNum = Integer.parseInt(lsSampleColumn2GroupName.get(i)[0]) - 1;
			double value = Double.parseDouble(info[colNum]);
			String timeInfo = lsSampleColumn2GroupName.get(i)[1];//时期
			ArrayList<Double> lsValue = add_and_get_LsValue(timeInfo, mapTime2LsValue);
			lsValue.add(value);
		}
		HashMap<String, Double> mapTime2AvgValue = new HashMap<String, Double>();
		for (Entry<String, ArrayList<Double>> entry : mapTime2LsValue.entrySet()) {
			Double avgValue = MathComput.mean(entry.getValue());
			mapTime2AvgValue.put(entry.getKey(), avgValue);
		}
		return mapTime2AvgValue;
	}
	/** 
	 * 设定样本时期到具体值的信息。
	 * 没有该timeInfo就产生个新的list，有的话就获得原来的list 
	 */
	protected ArrayList<Double> add_and_get_LsValue(String timeInfo, HashMap<String, ArrayList<Double>> mapTime2value) {
		ArrayList<Double> lsValue = mapTime2value.get(timeInfo);
		if (lsValue == null) {
			lsValue = new ArrayList<Double>();
			mapTime2value.put(timeInfo, lsValue);
		}
		return lsValue;
	}
	
	protected abstract void generateScript();
	
	protected String getWorkSpace(String content) {
		String RworkSpace = content.split(SepSign.SEP_ID)[1];
		RworkSpace = RworkSpace.replace("{$workspace}", workSpace.replace("\\", "/"));
		return RworkSpace;
	}
	protected String getFileName(String content) {
		String fileRawdata = content.split(SepSign.SEP_ID)[1];
		fileRawdata = fileRawdata.replace("{$filename}", fileNameRawdata.replace("\\", "/"));
		return fileRawdata;
	}
	/**
	 * 调用Rrunning并写入Cmd的名字,
	 * 例如：
	 * Rrunning("DEseq")
	 */
	protected abstract void run();
	protected void Rrunning(String cmdName) {
		String cmd = PathDetail.getRscript() + outScript.replace("\\", "/");
		CmdOperate cmdOperate = new CmdOperate(cmd);
		cmdOperate.run();
	}
	
	private void modifyResult() {
		for (Entry<String, String[]> entry : mapOutFileName2Compare.entrySet()) {
			String fileName = entry.getKey();
			String[] groupPaire = entry.getValue();
			modifySingleResultFile(fileName, groupPaire[0], groupPaire[1]);
		}
	}
	protected abstract void modifySingleResultFile(String outFileName, String treatName, String controlName);
	
	/** 删除中间文件 */
	public void clean() {
		FileOperate.DeleteFileFolder(outScript);
		FileOperate.DeleteFileFolder(fileNameRawdata);
	}
	/**
	 * 返回method的文字与其ID对照表
	 * ID就是本类的常量
	 */
	public static HashMap<String, Integer> getMapMethod2ID() {
		HashMap<String, Integer> mapMethod2ID = new LinkedHashMap<String, Integer>();
		mapMethod2ID.put("Limma--Microarray", LIMMA);
		mapMethod2ID.put("DEGseq--RPKM/Counts(recommand)", DEGSEQ);
		mapMethod2ID.put("EdegR--Counts(Needs Replication)", EDEGR);
		mapMethod2ID.put("DESeq--Counts(Needs Replication)", DESEQ);
		mapMethod2ID.put("Ttest", TTest);
		return mapMethod2ID;
	}
	
	public static DiffExpAbs createDiffExp(int DiffExpID) {
		if (DiffExpID == LIMMA) {
			return new DiffExpLimma();
		} else if (DiffExpID == DESEQ) {
			return new DiffExpDESeq();
		} else if (DiffExpID == DEGSEQ) {
			return new DiffExpDEGseq();
		} else if (DiffExpID == TTest) {
			return new DiffExpTtest();
		} else if (DiffExpID == EDEGR) {
			return new DiffExpEdgeR();
		} else {
			return null;
		}
	}
}
