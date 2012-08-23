package com.novelbio.analysis.diffexpress;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.novelbio.base.PathDetail;
import com.novelbio.base.dataOperate.DateTime;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.geneanno.SepSign;
import com.novelbio.generalConf.NovelBioConst;
import com.novelbio.generalConf.NovelBioTitleItem;

/**
 * ����DEGseq�㷨��������RPKM�����飬Ʃ��mRNAseq
 * @author zong0jie
 */
public class DiffExpDEGseq extends DiffExpAbs {
	String outPutSuffix = "_Path";
	public static void main(String[] args) {
		ArrayList<String[]> lsInfo = ExcelTxtRead.readLsExcelTxt("/media/winF/NBC/Project/Project_XSQ_Lab/miRNA/novelbio/miRNA����Deseq.txt", 1);
		DiffExpDEGseq deSeq = new DiffExpDEGseq();
		ArrayList<String[]> lsSampleColumn2GroupName = new ArrayList<String[]>();
		lsSampleColumn2GroupName.add(new String[] {"2","A"});
		lsSampleColumn2GroupName.add(new String[] {"3","B"});
		lsSampleColumn2GroupName.add(new String[] {"4","C"});
		lsSampleColumn2GroupName.add(new String[] {"5","D"});
		lsSampleColumn2GroupName.add(new String[] {"6","E"});
		lsSampleColumn2GroupName.add(new String[] {"7","F"});
		deSeq.setCol2Sample(lsSampleColumn2GroupName);
		deSeq.setColID(1);
		deSeq.addFileName2Compare(FileOperate.getProjectPath() + "AvsB.xls", new String[]{"A","B"});
		deSeq.addFileName2Compare(FileOperate.getProjectPath() + "AvsC.xls", new String[]{"A","C"});
		deSeq.addFileName2Compare(FileOperate.getProjectPath() + "CvsB.xls", new String[]{"C","B"});
		deSeq.setGeneInfo(lsInfo);
		deSeq.getResultFileName();
	}
	public DiffExpDEGseq() {
//		rawScript = "/media/winE/Bioinformatics/R/Protocol/DEGseqJava.txt";
		rawScript = NovelBioConst.getRworkspace() + "DEGseqJava.txt";

	}
	/** �������У�ʵ���У�����R���棬���Բ���Ҫ��1 */
	public void setColID(int colID) {
		this.colAccID = colID;
		calculate = false;
	}
	protected void generateScript() {
		TxtReadandWrite txtReadScript = new TxtReadandWrite(rawScript, false);
		TxtReadandWrite txtOutScript = new TxtReadandWrite(outScript, true);
		for (String content : txtReadScript.readlines()) {
			if (content.startsWith("#workspace"))
				txtOutScript.writefileln(getWorkSpace(content));
			else if (content.startsWith("#filename"))
				txtOutScript.writefileln(getFileName(content));
			else if (content.startsWith("#ReadFileAndColumn")) {
				String[] readFileAndCol = getReadFileAndColumn(content);
				for (String string : readFileAndCol) {
					txtOutScript.writefileln(string);
				}
			} 
			else if (content.startsWith("#Compare")) {
				String[] compareAndOut = getCompareAndOutput(content);
				for (String string : compareAndOut) {
					txtOutScript.writefileln(string);
				}
			} 
			else {
				txtOutScript.writefileln(content);
			}
		}
		txtOutScript.close();
	}

	private String[] getReadFileAndColumn(String content) {
		HashMap<String, ArrayList<Integer>> mapSample2LsCol = getMapSample2LsCol();
		String[] resultSample = new String[mapSample2LsCol.size()];
		int resultSampleNum = 0;

		String SampleScipt = content.split(SepSign.SEP_ID)[1];
		for (Entry<String, ArrayList<Integer>> entry : mapSample2LsCol.entrySet()) {
			String tmpScript = SampleScipt.replace("{$SampleName}", entry.getKey());
			tmpScript = tmpScript.replace("{$colGeneID}", colAccID + "");
			tmpScript = tmpScript.replace("{$colvalueID}", getRformatSampleVector(entry.getValue()));
			resultSample[resultSampleNum] = tmpScript;
			resultSampleNum++;
		}
		return resultSample;
	}

	/**
	 * ���������col2sampleName������Map���ձ�
	 * @return
	 */
	private HashMap<String, ArrayList<Integer>> getMapSample2LsCol() {
		HashMap<String, ArrayList<Integer>> mapSample2LsCol = new LinkedHashMap<String, ArrayList<Integer>>();
		for (String[] strings : lsSampleColumn2GroupName) {
			ArrayList<Integer> lsColNum = null;
			if (mapSample2LsCol.containsKey(strings[1])) {
				lsColNum = mapSample2LsCol.get(strings[1]);
			} else {
				lsColNum = new ArrayList<Integer>();
				mapSample2LsCol.put(strings[1], lsColNum);
			}
			lsColNum.add(Integer.parseInt(strings[0]));
		}
		return mapSample2LsCol;
	}

	/**
	 * ��������col��list�������������ڵ���1��2��4��5������ӵ��ı�
	 * 
	 * @param lsSample
	 * @return
	 */
	private String getRformatSampleVector(ArrayList<Integer> lsSample) {
		String result = lsSample.get(0) + "";
		for (int i = 1; i < lsSample.size(); i++) {
			result = result + ", " + lsSample.get(i);
		}
		return result;
	}

	private String[] getCompareAndOutput(String content) {
		String[] result = new String[mapOutFileName2Compare.size()];
		
		HashMap<String, ArrayList<Integer>> mapSample2LsCol = getMapSample2LsCol();
		ArrayList<String> lsFileName = ArrayOperate.getArrayListKey(mapOutFileName2Compare);
		String sampleScript = content.split(SepSign.SEP_ID)[1];
		
		int compareNum = 0;
		for (String fileName : lsFileName) {
			String tmpScript = sampleScript.replace("{$OutDir}", fileName + outPutSuffix);
			String[] comparePair = mapOutFileName2Compare.get(fileName);
			tmpScript = tmpScript.replace("{$SampleTreat}", comparePair[0]).replace("{$SampleTreatName}", comparePair[0]);
			tmpScript = tmpScript.replace("{$SampleControl}", comparePair[1]).replace("{$SampleControlName}", comparePair[1]);
			tmpScript = tmpScript.replace("{$SampleTreatNum}", getSampleCol(mapSample2LsCol.get(comparePair[0]).size()));
			tmpScript = tmpScript.replace("{$SampleControlNum}", getSampleCol(mapSample2LsCol.get(comparePair[1]).size()));
			result[compareNum] = tmpScript;
			compareNum++;
		}
		return result;
	}
	/** �Ƚϵ��У�ֱ�ӷ�������1��2��3��4������ӵ��ı����� */
	private String getSampleCol(int sampleNum) {
		String result = "2";
		for (int i = 2; i <= sampleNum; i++) {
			result = result + ", " + (i+1);
		}
		return result;
	}
	
	@Override
	protected void setOutScriptPath() {
		outScript = workSpace + "DEGseq_" + DateTime.getDateAndRandom() + ".R";
	}
	/** �����ļ�д��txt�ı� */
	@Override
	protected void setFileNameRawdata() {
		fileNameRawdata = workSpace + "DEGseqGeneInfo_" + DateTime.getDateAndRandom() + ".txt";
	}
	/**
	 * ����Ҫ��ȡר�ŵ���Ϣ
	 */
	@Override
	protected void writeToGeneFile() {
		TxtReadandWrite txtWrite = new TxtReadandWrite(fileNameRawdata, true);
		txtWrite.ExcelWrite(lsGeneInfo, "\t", 1, 1);
	}

	@Override
	protected void run() {
		Rrunning("DEGseq");
	}

	@Override
	protected void modifySingleResultFile(String outFileName, String treatName, String controlName) {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		ArrayList<String[]> lsDifGene = ExcelTxtRead.readLsExcelTxt(FileOperate.addSep(outFileName + outPutSuffix) + "output_score.txt", 1);
		String[] title = new String[]{NovelBioTitleItem.AccID.toString() , treatName, controlName, NovelBioTitleItem.FoldChange.toString(),
				NovelBioTitleItem.Log2FC.toString(), NovelBioTitleItem.Pvalue.toString(), NovelBioTitleItem.FDR.toString()};
		lsResult.add(title);
		
		ArrayList<int[]> lsIndelItem = new ArrayList<int[]>();
		lsIndelItem.add(new int[]{5, -1});//"z-score"
		lsIndelItem.add(new int[]{8, -1});//"q-value(Storey et al. 2003)"
		lsIndelItem.add(new int[]{9, -1});//"Signature(p-value < 0.001)"
		for (int i = 1; i < lsDifGene.size(); i++) {
			String[] tmpResult = ArrayOperate.indelElement(lsDifGene.get(i), lsIndelItem, "");
			for (int j = 0; j < tmpResult.length; j++) {
				tmpResult[j] = tmpResult[j].replace("\"", "");
			}
			tmpResult[3] = division(tmpResult[1], tmpResult[2]);
			lsResult.add(tmpResult);
		}
		FileOperate.DeleteFileFolder(outFileName + outPutSuffix);
		//��ֹR��û��������ȥ��ȡ
		try { Thread.sleep(50); } catch (Exception e) { }
		
		TxtReadandWrite txtOutFinal = new TxtReadandWrite(outFileName, true);
		txtOutFinal.ExcelWrite(lsResult, "\t", 1, 1);
	}
	private String division(String A, String B) {
		if (A.equals("NA") && B.equals("NA")) {
			return "NA";
		}
		else if (A.equals("NA")) {
			return 0 + "";
		}
		else if (B.equals("NA")) {
			if (Integer.parseInt(A) == 0) {
				return 0 + "";
			}
			return "Inf";
		}
		try {
			double a = Double.parseDouble(A);
			double b = Double.parseDouble(B);
			return a/b +"";
		} catch (Exception e) {
			return "None";
		}
	}
}
