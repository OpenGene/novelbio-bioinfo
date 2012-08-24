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
	 * һϵ�еı�ʾ���������У������ʱ��Ͱ���col����������<br>
	 * 0: colNum, ʵ��number<br>
	 * 1: SampleGroupName
	 */
	ArrayList<String[]> lsSampleColumn2GroupName;
	/**����ΨһID������û���ظ� */
	int colAccID = 0;
	/**
	 * �Ƚ�������Ӧ������ļ�������������һϵ����
	 * map: condition to compare group <br>
	 * FileName <br>
	 * To<br>
	 * 0��treatment<br>
	 * 1��control
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
	 * һϵ�еı�ʾ����������<br>
	 * 0: colNum, ʵ��number<br>
	 * 1: SampleGroupName
	 */
	public void setCol2Sample(ArrayList<String[]> lsSampleColumn2GroupName) {
		this.lsSampleColumn2GroupName = lsSampleColumn2GroupName;
		//���н�������
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
	 * �趨����ļ��кͱȽ���
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
	/** �������У�ʵ���� */
	public void setColID(int colID) {
		this.colAccID = colID - 1;
		calculate = false;
	}
	protected abstract void setOutScriptPath();
	
	protected abstract void setFileNameRawdata();
	
	protected void setRworkspace() {
		workSpace = NovelBioConst.getRworkspaceTmp();
	}
	/** �������� */
	public String getOutScript() {
		generateScript();
		return outScript;
	}
	/** �������� */
	public String getFileNameRawdata() {
		return fileNameRawdata;
	}
	public ArrayList<String> getResultFileName() {
		calculateResult();
		return ArrayOperate.getArrayListKey(mapOutFileName2Compare);
	}
	/** ������� */
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
	 * ��������ļ������������Ҫ��txt��ʽд���ı�
	 */
	protected void writeToGeneFile() {
		TxtReadandWrite txtWrite = new TxtReadandWrite(fileNameRawdata, true);
		txtWrite.ExcelWrite(getAnalysisGeneInfo(), "\t", 1, 1);
	}
	/**
	 * ��ѡ���Ļ���ID�;���ֵд���ı�
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
	 * ����Rrunning��д��Cmd������,
	 * ���磺
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
	
	/** ɾ���м��ļ� */
	public void clean() {
		FileOperate.DeleteFileFolder(outScript);
		FileOperate.DeleteFileFolder(fileNameRawdata);
	}
	/**
	 * ����method����������ID���ձ�
	 * ID���Ǳ���ĳ���
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
