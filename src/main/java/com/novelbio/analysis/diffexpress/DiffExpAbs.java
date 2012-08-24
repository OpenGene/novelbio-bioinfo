package com.novelbio.analysis.diffexpress;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.apache.velocity.app.event.ReferenceInsertionEventHandler.referenceInsertExecutor;

import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.DateTime;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.geneanno.SepSign;
import com.novelbio.generalConf.NovelBioConst;
import com.novelbio.generalConf.TitleFormatNBC;

public abstract class DiffExpAbs {
	public static final int LIMMA = 10;
	public static final int DESEQ = 20;
	public static final int DEGSEQ = 30;
	
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
	
	protected abstract void setFileNameRawdata();
	
	protected void setRworkspace() {
		workSpace = NovelBioConst.getRworkspaceTmp();
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
	public ArrayList<String> getResultFileName() {
		calculateResult();
		return ArrayOperate.getArrayListKey(mapOutFileName2Compare);
	}
	/** 计算差异 */
	private void calculateResult() {
		if (calculate) {
			return;
		}
		calculate = true;
		writeToGeneFile();
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
		txtWrite.ExcelWrite(getAnalysisGeneInfo(), "\t", 1, 1);
	}
	/**
	 * 将选定的基因ID和具体值写入文本
	 * @return
	 */
	private ArrayList<String[]> getAnalysisGeneInfo() {
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
	protected abstract void generateScript();
	
	protected String getWorkSpace(String content) {
		String RworkSpace = content.split(SepSign.SEP_ID)[1];
		RworkSpace = RworkSpace.replace("{$workspace}", workSpace);
		return RworkSpace;
	}
	protected String getFileName(String content) {
		String fileRawdata = content.split(SepSign.SEP_ID)[1];
		fileRawdata = fileRawdata.replace("{$filename}", fileNameRawdata);
		return fileRawdata;
	}
	/**
	 * 调用Rrunning并写入Cmd的名字,
	 * 例如：
	 * Rrunning("DEseq")
	 */
	protected abstract void run();
	protected void Rrunning(String cmdName) {
		String cmd = NovelBioConst.R_SCRIPT + outScript;
		CmdOperate cmdOperate = new CmdOperate(cmd, cmdName);
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
		HashMap<String, Integer> mapMethod2ID = new HashMap<String, Integer>();
		mapMethod2ID.put("Limma--Microarray", LIMMA);
		mapMethod2ID.put("DESeq--Counts", DESEQ);
		mapMethod2ID.put("DEGseq--RNAseq", DEGSEQ);
		return mapMethod2ID;
	}
	
	public static DiffExpAbs createDiffExp(int DiffExpID) {
		if (DiffExpID == LIMMA) {
			return new DiffExpLimma();
		}
		else if (DiffExpID == DESEQ) {
			return new DiffExpDESeq();
		}
		else if (DiffExpID == DEGSEQ) {
			return new DiffExpDEGseq();
		}
		else {
			return null;
		}
	}
}
