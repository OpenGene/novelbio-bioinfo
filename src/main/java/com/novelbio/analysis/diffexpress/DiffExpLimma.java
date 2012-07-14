package com.novelbio.analysis.diffexpress;

import java.util.ArrayList;
import java.util.HashMap;

import com.novelbio.base.dataOperate.DateTime;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.geneanno.SepSign;
import com.novelbio.generalConf.NovelBioTitleItem;

public class DiffExpLimma extends DiffExpAbs{
	String rawScript = "/media/winE/Bioinformatics/R/Protocol/Microarray/limmaSimpleJava.txt";
	boolean log2Transform = false;
	/** �ȽϾ�����ƺú���Ҫ��ÿ��ID��Ӧ���������� <br>
	 * Ʃ�磺<br>
	 * design = model.matrix(~ -1+factor (c(1,1,2,2,3,3))) <br>
	 * colnames(design) = c("Patient","Treat","Norm")<br>
	 * 1 = Patient<br>
	 * 2 = Treat<br>
	 * 3 = Norm<br>
	 * */
	HashMap<Integer, String> mapID2Sample = new HashMap<Integer, String>();
	
	public static void main(String[] args) {
		ArrayList<String[]> lsInfo = ExcelTxtRead.readLsExcelTxt("/media/winF/NBC/Project/Project_XSQ_Lab/miRNA/novelbio/miRNA����Deseq.txt", 1);
		DiffExpLimma deSeq = new DiffExpLimma();
		ArrayList<String[]> lsSampleColumn2GroupName = new ArrayList<String[]>();
		lsSampleColumn2GroupName.add(new String[] {"2","A"});
		lsSampleColumn2GroupName.add(new String[] {"3","A"});
		lsSampleColumn2GroupName.add(new String[] {"4","B"});
		lsSampleColumn2GroupName.add(new String[] {"5","B"});
		lsSampleColumn2GroupName.add(new String[] {"6","C"});
		lsSampleColumn2GroupName.add(new String[] {"7","C"});
		deSeq.setCol2Sample(lsSampleColumn2GroupName);
		deSeq.setColID(1);
		deSeq.addFileName2Compare(FileOperate.getProjectPath() + "Tmp/AvsB.xls", new String[]{"A","B"});
		deSeq.addFileName2Compare(FileOperate.getProjectPath() + "Tmp/AvsC.xls", new String[]{"A","C"});
		deSeq.addFileName2Compare(FileOperate.getProjectPath() + "Tmp/CvsB.xls", new String[]{"C","B"});
		deSeq.setGeneInfo(lsInfo);
		deSeq.getResultFileName();
	}
	
	@Override
	protected void setOutScriptPath() {
		outScript = workSpace + "Limma_" + DateTime.getDateAndRandom() + ".R";		
	}

	@Override
	protected void setFileNameRawdata() {
		fileNameRawdata = workSpace + "LimmaGeneInfo_" + DateTime.getDateDetail() + ".txt";
	}
	public boolean isLog2Transform() {
		return log2Transform;
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
			else if (content.startsWith("#IsLog"))
				txtOutScript.writefileln(isLog2TransForm(content));
			else if (content.startsWith("#DesignMatrix"))
				txtOutScript.writefileln(getDesignMatrixAndFillMapID2Sample(content));
			else if (content.startsWith("#SampleName"))
				txtOutScript.writefileln(getSampleName(content));
			else if (content.startsWith("#ContrastMatrix"))
				txtOutScript.writefileln(getContrastMatrix(content));
			else if (content.startsWith("#WriteToFile")) {
				String[] readFileAndCol = getWriteToFile(content);
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
	 * ���Ƿ���Ҫlogת��
	 * @param content
	 * @return
	 */
	private String isLog2TransForm(String content) {
		String logScript = content.split(SepSign.SEP_ID)[1];
		if (log2Transform) {
			return logScript;
		}
		return "";
	}
	/**
	 * ��ƾ��󣬲����MapID2Sample<br>
	 * ֮ǰ :
	 * design = model.matrix(~ -1+factor (c({$design})))<br>
	 * ֮��
	 * design = model.matrix(~ -1+factor (c(1,1,2,2,3,3)))
	 * @param content
	 * @return
	 */
	private String getDesignMatrixAndFillMapID2Sample(String content) {
		String result = "1";
		mapID2Sample.clear();
		
		HashMap<String, Integer> mapSample2ID = new HashMap<String, Integer>();
		String[] sample = lsSampleColumn2GroupName.get(0);
		mapSample2ID.put(sample[1], 1);
		mapID2Sample.put(1, sample[1]);
		int designNum = 2;//1�Ѿ�����hashmap��
		
		for (int i = 1; i < lsSampleColumn2GroupName.size(); i++) {
			sample = lsSampleColumn2GroupName.get(i);
			//�����µ����֣��ͽ�design��ID��һ
			if (!mapSample2ID.containsKey(sample[1])) {
				mapSample2ID.put(sample[1], designNum);
				mapID2Sample.put(designNum, sample[1]);
				designNum++;
			}
			result = result + ", " + mapSample2ID.get(sample[1]);
		}
		
		String designScript = content.split(SepSign.SEP_ID)[1];
		designScript = designScript.replace("{$design}", result);
		return designScript;
	}
	/**
	 * ������������<br>
	 * ֮ǰ :
	 * colnames(design) = c({$SampleName})<br>
	 * ֮�� :
	 * colnames(design) = c("Patient","Treat","Norm")<br>
	 * @param content
	 * @return
	 */
	private String getSampleName(String content) {
		String Result = "\"" + mapID2Sample.get(1) + "\"";
		for (int i = 2; i <= mapID2Sample.size(); i++) {
			Result = Result + ", \"" + mapID2Sample.get(i) + "\"";
		}
		
		String SampleScript = content.split(SepSign.SEP_ID)[1];
		SampleScript = SampleScript.replace("{$SampleName}", Result);
		return SampleScript;
	}
	/**
	 * �ȽϾ���<br>
	 * ֮ǰ :<br>
	 * contrast.matrix = makeContrasts( {$PairedInfo},levels=design)<br>
	 * ֮��<br>
	 * contrast.matrix = makeContrasts( PatientvsNorm = Patient - Norm,TreatvsNorm = Treat - Norm,levels=design)<br>
	 * @param content
	 * @return
	 */
	private String getContrastMatrix(String content) {
		ArrayList<String> lsFileName = ArrayOperate.getArrayListKey(mapOutFileName2Compare);
		String result = "";
		
		for (String fileName : lsFileName) {
			String[] pair = mapOutFileName2Compare.get(fileName);
			result = result + getCompare(pair) + ", ";
		}
		
		String CompareScript = content.split(SepSign.SEP_ID)[1];
		CompareScript = CompareScript.replace("{$PairedInfo}", result);
		return CompareScript;
	}
	/**
	 * ����һ�� PatientvsNorm = Patient - Norm
	 * @return
	 */
	private String getCompare(String[] pair) {
		pair[0] = pair[0].replace(" ", "");
		pair[1] = pair[1].replace(" ", "");
		return getCoef(pair) + " = " + pair[0] + " - " + pair[1];
	}
	/** ����һ����Ϣ�����ظö���Ϣ��������ǰ׺ */
	private String getCoef(String[] pair) {
		return pair[0] +"_vs_" +pair[1];
	}
	/**
	 * д����<br>
	 * ֮ǰ :<br>
	 * write.table(topTable(fit2.eBayes, coef="{$Pair}", adjust="fdr", sort.by="B", number=50000),  file="{$OutFileName}", row.names=F, sep="\t")
	 * ֮��<br>
	 * write.table(topTable(fit2.eBayes, coef="AvsB", adjust="fdr", sort.by="B", number=50000),  file="AvsB.xls", row.names=F, sep="\t")
	 * contrast.matrix = makeContrasts( PatientvsNorm = Patient - Norm,TreatvsNorm = Treat - Norm,levels=design)<br>
	 * @param content
	 * @return
	 */
	private String[] getWriteToFile(String content) {
		String writeToFileScript = content.split(SepSign.SEP_ID)[1];
		ArrayList<String> lsFileName = ArrayOperate.getArrayListKey(mapOutFileName2Compare);
		String[] result = new String[lsFileName.size()];
		for (int i = 0; i < lsFileName.size(); i++) {
			String outFileName = lsFileName.get(i);
			String[] pair = mapOutFileName2Compare.get(outFileName);
			String coef = getCoef(pair);
			result[i] = writeToFileScript.replace("{$Pair}", coef).replace("{$OutFileName}", outFileName);
		}
		return result;
	}
	@Override
	protected void run() {
		Rrunning("Limma");
	}

	@Override
	protected void modifySingleResultFile(String outFileName, String treatName, String controlName) {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		ArrayList<String[]> lsDifGene = ExcelTxtRead.readLsExcelTxt(outFileName, 1);
		String[] title = new String[]{NovelBioTitleItem.AccID.toString(), NovelBioTitleItem.Log2FC.toString(), NovelBioTitleItem.Pvalue.toString(), NovelBioTitleItem.FDR.toString(), "Bvalue"};
		lsResult.add(title);

		ArrayList<int[]> lsIndelItem = new ArrayList<int[]>();
		lsIndelItem.add(new int[]{2, -1});//"AveExpr"
		lsIndelItem.add(new int[]{3, -1});//"t"
		for (int i = 1; i < lsDifGene.size(); i++) {
			String[] tmpResult = ArrayOperate.indelElement(lsDifGene.get(i), lsIndelItem, "");
			for (int j = 0; j < tmpResult.length; j++) {
				tmpResult[j] = tmpResult[j].replace("\"", "");
			}
			lsResult.add(tmpResult);
		}
		FileOperate.DeleteFileFolder(outFileName);
		//��ֹR��û��������ȥ��ȡ
		try { Thread.sleep(50); } catch (Exception e) { }
		
		TxtReadandWrite txtOutFinal = new TxtReadandWrite(outFileName, true);
		txtOutFinal.ExcelWrite(lsResult, "\t", 1, 1);
	}

}