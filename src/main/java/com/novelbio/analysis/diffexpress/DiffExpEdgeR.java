package com.novelbio.analysis.diffexpress;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import com.novelbio.base.PathDetail;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.DateTime;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.geneanno.SepSign;
import com.novelbio.generalConf.TitleFormatNBC;

public class DiffExpEdgeR extends DiffExpAbs {
	public static void main(String[] args) {
		String rawScript = "/media/winD/fedora/rscript/edgeRJava.txt";
		
		ArrayList<String[]> lsGeneInfo = ExcelTxtRead.readLsExcelTxt("/media/winF/NBC/Project/RNA-Seq_CR_20111201/crNew.txt", 1);
		ArrayList<String[]> lsSampleColumn2GroupName = new ArrayList<String[]>();
		lsSampleColumn2GroupName.add(new String[]{"3", "Ns6d"});
		lsSampleColumn2GroupName.add(new String[]{"4", "E6d"});
		lsSampleColumn2GroupName.add(new String[]{"5", "E6d"});
		
		DiffExpEdgeR diffExpEdgeR = new DiffExpEdgeR();
		diffExpEdgeR.setCol2Sample(lsSampleColumn2GroupName);
		diffExpEdgeR.setColID(1);
		diffExpEdgeR.setGeneInfo(lsGeneInfo);
		diffExpEdgeR.setRawScript(rawScript);
		diffExpEdgeR.addFileName2Compare("/media/winD/fedora/rscript/out1", new String[]{"Ns6d", "E6d"});
		diffExpEdgeR.getResultFileName();
	}
	
	/**
	 * lslsGeneInfo��ÿһ����������Ӧ�ı�׼����ֵ
	 * ���Ȼ��reads���������� m����reads��Ϊ mNum��
	 * Ȼ��ÿ������k��reads�� kNum
	 * ��׼����ϵ��  normalizeNum = kNum/mNum
	 * ��׼������Ϊ ���� k��ÿ�������reads�� geneCountNumModify = geneCountNum * kNum/mNum
	 * ��Ϊ geneCountNumModify = geneCountNum * normalizeNum
	 */
	HashMap<Integer, Double> mapColNum2NormalizeNum = new HashMap<Integer, Double>();
	
	public DiffExpEdgeR() {
		rawScript = PathDetail.getRworkspace() + "edgeRJava.txt";
	}
	/**
	 * ���ÿ����������Ҫ���Ե�����ϵ��
	 * @return
	 */
	protected void setNormalizeCoef() {
		//��������ȫ��������
		ArrayList<Integer> lsColNum = new ArrayList<Integer>();
		
		double maxSampleCount = 0;
		//���������reads��������������װ��hash����
		for (int i = 0; i < lsSampleColumn2GroupName.size(); i++) {
			String colSample = lsSampleColumn2GroupName.get(i)[0];
			int colNum = Integer.parseInt(colSample) - 1;//ʵ��������Ҫ��ȥ1
			lsColNum.add(colNum);
			ArrayList<Double> lsValue = new ArrayList<Double>();
			for (String[] strings : lsGeneInfo) {
				try {
					double tmpValue = Double.parseDouble(strings[colNum]);
					lsValue.add(tmpValue);
				} catch (Exception e) { }
			}
			double thisAllCounts = MathComput.sum(lsValue);
			mapColNum2NormalizeNum.put(colNum, (double) thisAllCounts);
			if (thisAllCounts > maxSampleCount) {
				maxSampleCount = thisAllCounts;
			}
		}
		
		//����ϵ��
		for (Integer integer : lsColNum) {
			double tmpAllCounts = mapColNum2NormalizeNum.get(integer);
			mapColNum2NormalizeNum.put(integer, maxSampleCount/tmpAllCounts);
		}
		
	}
	
	@Override
	protected void calculateResult() {
		setNormalizeCoef();
		super.calculateResult();
	}
	/**
	 * ��� 
	 * ����ʱ��--��ʱ����ƽ��ֵ
	 * ��map
	 * �ڲ���ÿ��ֵ�϶�����������ϵ��
	 * @param info
	 * @return
	 */
	protected HashMap<String, Double> mapTime2AvgValue(String[] info) {
		HashMap<String, ArrayList<Double>> mapTime2LsValue = new HashMap<String, ArrayList<Double>>();
		
		for (int i = 0; i < lsSampleColumn2GroupName.size(); i++) {
			int colNum = Integer.parseInt(lsSampleColumn2GroupName.get(i)[0]) - 1;
			//ÿ��ֵ����������ϵ��
			double value = Double.parseDouble(info[colNum]) * mapColNum2NormalizeNum.get(colNum);
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
	
	
	@Override
	protected void setOutScriptPath() {
		outScript = workSpace + "EdgeR_" + DateTime.getDateAndRandom() + ".R";
		
	}

	@Override
	protected void setFileNameRawdata() {
		fileNameRawdata = workSpace + "EdgeRGeneInfo_" + DateTime.getDateAndRandom() + ".txt";
	}

	@Override
	protected void run() {
		Rrunning("EdgeR");
	}
	
	
	@Override
	protected void generateScript() {
		TxtReadandWrite txtReadScript = new TxtReadandWrite(rawScript, false);
		TxtReadandWrite txtOutScript = new TxtReadandWrite(outScript, true);
		for (String content : txtReadScript.readlines()) {
			if (content.startsWith("#workspace"))
				txtOutScript.writefileln(getWorkSpace(content));
			else if (content.startsWith("#filename"))
				txtOutScript.writefileln(getFileName(content));
			else if (content.startsWith("#compare_group"))
				txtOutScript.writefileln(getGroupInfo(content));
			else if (content.startsWith("#DuplicateExpEstimate"))
				txtOutScript.writefileln(duplicateEstimate(content));
			else if (content.startsWith("#DuplicateExpResult")) {
				String[] readFileAndCol = getResultScript(content);
				for (String string : readFileAndCol) {
					txtOutScript.writefileln(string);
				}
			}
			else {
				txtOutScript.writefileln(content);
			}
		}
		txtOutScript.close();
	}
	/**
	 * group = factor( c("Patient", "Patient","Treat","Treat")) )
	 * @param content
	 * @return
	 */
	private String getGroupInfo(String content) {
		String result = CmdOperate.addQuot(lsSampleColumn2GroupName.get(0)[1]);
		for (int i = 1; i < lsSampleColumn2GroupName.size(); i++) {
			String[] sampleCol2GroupName = lsSampleColumn2GroupName.get(i);
			result = result + ", " + CmdOperate.addQuot(sampleCol2GroupName[1]);
		}
		String designScript = content.split(SepSign.SEP_ID)[1];
		designScript = designScript.replace("{$Group}", result);
		return designScript;
	}
	/**
	 * group = factor( c("Patient", "Patient","Treat","Treat")) )
	 * @param content
	 * @return
	 */
	private String duplicateEstimate(String content) {
		String estimate = "";
		if (isHaveReplicate()) {
			estimate = content.split(SepSign.SEP_ID)[1];
		}		
		return estimate;
	}
	
	/** �Ƿ����ظ� */
	private boolean isHaveReplicate() {
		boolean haveReplicate = false;
		HashSet<String> setGroupName = new HashSet<String>();
		for (String[] sampleCol2GroupName : lsSampleColumn2GroupName) {
			if (setGroupName.contains(sampleCol2GroupName[1])) {
				haveReplicate = true;
				break;
			}
			setGroupName.add(sampleCol2GroupName[1]);
		}
		return haveReplicate;
	}
	 
	/**
	 * ����������ֺͱȽ�
	 * result = exactTest(m, pair=c({$Compare})); 
	 * result2=cbind(resultFinal,fdr=p.adjust(resultFinal[,3])); 
	 * write.table(result2, file="{$OutFileName}",sep="\t")
	 * @param content
	 * @return
	 */
	private String[] getResultScript(String content) {
		String writeToFileScript = "";
		if (isHaveReplicate()) {
			writeToFileScript = content.split(SepSign.SEP_ID)[1];
		} else {
			writeToFileScript = content.split(SepSign.SEP_ID)[3];
		}
		
		ArrayList<String> lsFileName = ArrayOperate.getArrayListKey(mapOutFileName2Compare);
		String[] result = new String[lsFileName.size()];
		for (int i = 0; i < lsFileName.size(); i++) {
			String outFileName = lsFileName.get(i);
			String[] pair = mapOutFileName2Compare.get(outFileName);
			String compare = CmdOperate.addQuot(pair[1]) + "," + CmdOperate.addQuot(pair[0]);
			result[i] = writeToFileScript.replace("{$Compare}", compare).replace("{$OutFileName}", outFileName.replace("\\", "/"));
		}
		
		return result;
	}
 
	
	@Override
	protected void modifySingleResultFile(String outFileName, String treatName, String controlName) {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		ArrayList<String[]> lsDifGene = ExcelTxtRead.readLsExcelTxt(outFileName, 1);
		String[] title = new String[]{TitleFormatNBC.AccID.toString(), treatName, controlName, TitleFormatNBC.Log2FC.toString(), TitleFormatNBC.Pvalue.toString(), TitleFormatNBC.FDR.toString()};
		lsResult.add(title);

		ArrayList<int[]> lsIndelItem = new ArrayList<int[]>();
		lsIndelItem.add(new int[]{1, 2});//"treat" and control
		lsIndelItem.add(new int[]{2, -1});//"LogCMP"

		for (int i = 1; i < lsDifGene.size(); i++) {
			String[] tmpResult = ArrayOperate.indelElement(lsDifGene.get(i), lsIndelItem, "");
			String geneID = tmpResult[0].replace("\"", "");
			tmpResult[1] = mapGeneID_2_Sample2MeanValue.get(geneID).get(treatName) + "";
			tmpResult[2] = mapGeneID_2_Sample2MeanValue.get(geneID).get(controlName) + "";
			if (!tmpResult[2].equals("0")) {
				setLogFC(tmpResult);
			}
			for (int j = 0; j < tmpResult.length; j++) {
				tmpResult[j] = tmpResult[j].replace("\"", "");
			}
			lsResult.add(tmpResult);
		}
		FileOperate.DeleteFileFolder(outFileName);
		//��ֹR��û��������ȥ��ȡ
		try { Thread.sleep(50); } catch (Exception e) { }
		
		TxtReadandWrite txtOutFinal = new TxtReadandWrite(outFileName, true);
		txtOutFinal.ExcelWrite(lsResult);
	}
	
	private void setLogFC(String[] resultInfo) {
		if (!resultInfo[2].equals("0")) {
			try {
				double treat = Double.parseDouble(resultInfo[1]);
				double control = Double.parseDouble(resultInfo[2]);
				resultInfo[3] = Math.log(treat/control)/Math.log(2) + "";
			} catch (Exception e) { }
		}
	}

}
