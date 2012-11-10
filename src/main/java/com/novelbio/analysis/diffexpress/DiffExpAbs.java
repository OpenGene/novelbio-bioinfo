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
	 * һϵ�еı�ʾ���������У������ʱ��Ͱ���col����������<br>
	 * 0: colNum, ʵ��number<br>
	 * 1: SampleGroupName
	 */
	ArrayList<String[]> lsSampleColumn2GroupName;
	/** ������
	 * ��Ӧ������
	 * ��Ӧƽ�����ֵ
	 */
	HashMap<String, HashMap<String, Double>> mapGeneID_2_Sample2MeanValue;
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
	/** �趨ԭʼ���ݵ��ļ��� */
	protected abstract void setFileNameRawdata();
	
	protected void setRworkspace() {
		workSpace = PathDetail.getRworkspaceTmp();
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
	
	/** �����Ƿ�Ϊlog����ֵ
	 * ��Ҫ����limma����ʵ�����ж����ı��ֵ�Ƿ����40
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
	/** ������� */
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
	 * ��������ļ������������Ҫ��txt��ʽд���ı�
	 */
	protected void writeToGeneFile() {
		TxtReadandWrite txtWrite = new TxtReadandWrite(fileNameRawdata, true);
		txtWrite.ExcelWrite(getAnalysisGeneInfo());
	}
	/**
	 * ���ѡ���Ļ���ID�;���ֵ
	 * ����ʽ���������lsSampleColumn2GroupName�������򣬲�������
	 * @return
	 * 0�� geneID
	 * 1-n��value
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
	 * ��� 
	 * ����ʱ��--��ʱ����ƽ��ֵ
	 * ��map
	 * @param info
	 * @return
	 */
	protected HashMap<String, Double> mapTime2AvgValue(String[] info) {
		HashMap<String, ArrayList<Double>> mapTime2LsValue = new HashMap<String, ArrayList<Double>>();
		
		for (int i = 0; i < lsSampleColumn2GroupName.size(); i++) {
			int colNum = Integer.parseInt(lsSampleColumn2GroupName.get(i)[0]) - 1;
			double value = Double.parseDouble(info[colNum]);
			String timeInfo = lsSampleColumn2GroupName.get(i)[1];//ʱ��
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
	 * �趨����ʱ�ڵ�����ֵ����Ϣ��
	 * û�и�timeInfo�Ͳ������µ�list���еĻ��ͻ��ԭ����list 
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
	 * ����Rrunning��д��Cmd������,
	 * ���磺
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
