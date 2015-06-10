package com.novelbio.analysis.diffexpress;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileHadoop;
import com.novelbio.generalConf.TitleFormatNBC;

import freemarker.template.Template;
@Component
@Scope("prototype")
public class DiffExpLimma extends DiffExpAbs{
	private static final Logger logger = Logger.getLogger(DiffExpLimma.class);
	/** 比较矩阵设计好后需要将每个ID对应到试验名上 <br>
	 * 譬如：<br>
	 * design = model.matrix(~ -1+factor (c(1,1,2,2,3,3))) <br>
	 * colnames(design) = c("Patient","Treat","Norm")<br>
	 * 1 = Patient<br>
	 * 2 = Treat<br>
	 * 3 = Norm<br>
	 * */
	HashMap<Integer, String> mapID2Sample = new HashMap<Integer, String>();

	@Override
	protected void setOutScriptPath() {
		outScript = workSpace + "Limma_" + DateUtil.getDateAndRandom() + ".R";
	}

	@Override
	protected void setFileNameRawdata() {
		fileNameRawdata = workSpace + "LimmaGeneInfo_" + DateUtil.getDateAndRandom() + ".txt";
	}
	
	/**
	 * 将输入的文件重整理成所需要的txt格式写入文本
	 */
	protected void writeToGeneFile() {
		lsGeneInfo = removeDuplicate();
		TxtReadandWrite txtWrite = new TxtReadandWrite(fileNameRawdata, true);
		List<String[]> lsAnalysisGeneInfo = getAnalysisGeneInfo();
		String[] title = lsAnalysisGeneInfo.get(0);
		txtWrite.writefileln(title);
		// 在这里取log
		for (int i = 0; i < lsAnalysisGeneInfo.size(); i++) {
			String[] info = lsAnalysisGeneInfo.get(i);
			for (int j = 1; j < info.length; j++) {
				if (info[j].equalsIgnoreCase("na") || info[j].equalsIgnoreCase("nan") || info[j].equals("null") || info[j].equals("none")) {
					info[j] = 0 + "";
				} else {
					try {
						double value = Double.parseDouble(info[j]);
						if (!isLogValue()) {
							info[j] = getLogValue(value) + "";
						}
					} catch (Exception e) {
						info[j] = 0 + "";
					}
				}
			}

			double sum = 0;
			boolean biggerThanMinSampleSep = false;
			for (int m = 1; m < info.length; m++) {
				double num = Double.parseDouble(info[m]);
				if (num > minSampleSepNum) {
					biggerThanMinSampleSep = true;
				}
				sum += num;
			}
			if ((minSampleSumNum < 0 || sum > minSampleSumNum)
					&& (minSampleSepNum < 0 || biggerThanMinSampleSep)) {
				txtWrite.writefileln(info);
			}
		}

		txtWrite.close();
	}
	
	@Override
	protected String generateScript() {
		Map<String,Object> mapData = new HashMap<String, Object>();
		mapData.put("workspace", getWorkSpace());
		mapData.put("filename", getFileName());
		mapData.put("islog2", true);
		mapData.put("design", getDesignMatrixAndFillMapID2Sample());
		mapData.put("SampleName", getSampleName());
		mapData.put("PairedInfo", getContrastMatrix());
		mapData.put("pair2filename", getMapCoef2FileName());
		String scriptContent = null;
		try {
			Template template = freeMarkerConfiguration.getTemplate("/R/diffgene/limma.ftl");
			StringWriter sw = new StringWriter();
			TxtReadandWrite txtReadandWrite = new TxtReadandWrite(outScript, true);
			// 处理并把结果输出到字符串中
			template.process(mapData, sw);
			scriptContent = sw.toString();
			txtReadandWrite.writefile(scriptContent);
			txtReadandWrite.close();
		} catch (Exception e) {
			throw new ExceptionFreemarker("limma rendering error:" + outScript, e);
		}
		return scriptContent;
	}

	/**
	 * 设计矩阵，并填充MapID2Sample<br>
	 * 之前 :
	 * ${design}<br>
	 * 之后：
	 * 1,1,2,2,3,3
	 * @param content
	 * @return
	 */
	private String getDesignMatrixAndFillMapID2Sample() {
		String result = "1";
		mapID2Sample.clear();
		
		HashMap<String, Integer> mapSample2ID = new HashMap<String, Integer>();
		String[] sample = lsSampleColumn2GroupName.get(0);
		mapSample2ID.put(sample[1], 1);
		mapID2Sample.put(1, sample[1]);
		int designNum = 2;//1已经加入hashmap了
		
		for (int i = 1; i < lsSampleColumn2GroupName.size(); i++) {
			sample = lsSampleColumn2GroupName.get(i);
			//发现新的名字，就将design的ID加一
			if (!mapSample2ID.containsKey(sample[1])) {
				mapSample2ID.put(sample[1], designNum);
				mapID2Sample.put(designNum, sample[1]);
				designNum++;
			}
			result = result + ", " + mapSample2ID.get(sample[1]);
		}
		return result;
	}
	/**
	 * 添加样本名字<br>
	 * 之前 :
	 * colnames(design) = c(${SampleName})<br>
	 * 之后 :
	 * colnames(design) = c("Patient","Treat","Norm")<br>
	 * @param content
	 * @return
	 */
	private String getSampleName() {
		String Result = CmdOperate.addQuot(mapID2Sample.get(1));
		for (int i = 2; i <= mapID2Sample.size(); i++) {
			Result = Result + ", " + CmdOperate.addQuot(mapID2Sample.get(i));
		}
		return Result;
	}
	/**
	 * 比较矩阵<br>
	 * 之前 :<br>
	 * contrast.matrix = makeContrasts( ${PairedInfo},levels=design)<br>
	 * 之后：<br>
	 * contrast.matrix = makeContrasts( PatientvsNorm = Patient - Norm,TreatvsNorm = Treat - Norm,levels=design)<br>
	 * @param content
	 * @return
	 */
	private String getContrastMatrix() {
		ArrayList<String> lsFileName = ArrayOperate.getArrayListKey(mapOutFileName2Compare);
		String result = "";
		
		for (String fileName : lsFileName) {
			String[] pair = mapOutFileName2Compare.get(fileName);
			result = result + getCompare(pair) + ", ";
		}
		return result;
	}
	/**
	 * 产生一个 PatientvsNorm = Patient - Norm
	 * @return
	 */
	private String getCompare(String[] pair) {
		pair[0] = pair[0].replace(" ", "");
		pair[1] = pair[1].replace(" ", "");
		return getCoef(pair) + " = " + pair[0] + " - " + pair[1];
	}

	/**
	 * 写入结果<br>
	 * 之前 :<br>
	 * write.table(topTable(fit2.eBayes, coef="{$Pair}", adjust="fdr", sort.by="B", number=50000),  file="{$OutFileName}", row.names=F, sep="\t")
	 * 之后：<br>
	 * write.table(topTable(fit2.eBayes, coef="AvsB", adjust="fdr", sort.by="B", number=50000),  file="AvsB.xls", row.names=F, sep="\t")
	 * contrast.matrix = makeContrasts( PatientvsNorm = Patient - Norm,TreatvsNorm = Treat - Norm,levels=design)<br>
	 * @param content
	 * @return
	 */
	private Map<String, String> getMapCoef2FileName() {
		Map<String, String> mapCoef2FileName = new HashMap<String, String>();
		for (String outFileName : mapOutFileName2Compare.keySet()) {
			String[] pair = mapOutFileName2Compare.get(outFileName);
			mapCoef2FileName.put(getCoef(pair), FileHadoop.convertToLocalPath(outFileName));
		}
		return mapCoef2FileName;
	}
	/** 给定一对信息，返回该对信息所产生的前缀 */
	private String getCoef(String[] pair) {
		return pair[0] +"_vs_" +pair[1];
	}
	@Override
	protected void run() {
		Rrunning("Limma");
	}
	
	static String logfc = "logFC";
	static String pValue = "P.Value";
	static String fdr = "adj.P.Val";
	static String Bvalue = "B";
	@Override
	protected List<String[]> modifySingleResultFile(String outFileName, String treatName, String controlName) {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		ArrayList<String[]> lsDifGene = ExcelTxtRead.readLsExcelTxt(outFileName, 1);
		String[] title = new String[]{TitleFormatNBC.AccID.toString(), treatName + "_logValue", controlName + "_logValue", TitleFormatNBC.Log2FC.toString(), TitleFormatNBC.Pvalue.toString(), TitleFormatNBC.FDR.toString(), "Bvalue"};
		lsResult.add(title);

		ArrayList<int[]> lsIndelItem = new ArrayList<int[]>();
		String[] titleOld = lsDifGene.get(0);
		String[] firstLine = lsDifGene.get(1);
		//第一列可能为数字列
		int accId = 0;
		if (firstLine.length == 8 || (firstLine.length == 7 && titleOld[0].replace("\"", "").equalsIgnoreCase("id"))) {
			accId = 1;
		}
		Map<String, Integer> mapColName2Num = new HashMap<>();
		for (int i = 0; i < titleOld.length; i++) {
			String tmpTitle = titleOld[i];
			tmpTitle = tmpTitle.replace("\"", "");
			if (tmpTitle.toLowerCase().equals(logfc.toLowerCase())) {
				mapColName2Num.put(logfc, i);
			} else if (tmpTitle.toLowerCase().equals(pValue.toLowerCase())) {
				mapColName2Num.put(pValue, i);
			} else if (tmpTitle.toLowerCase().equals(fdr.toLowerCase())) {
				mapColName2Num.put(fdr, i);
			} else if (tmpTitle.toLowerCase().equals(Bvalue.toLowerCase())) {
				mapColName2Num.put(Bvalue, i);
			}
		}
		
		for (int i = 1; i < lsDifGene.size(); i++) {
			String[] ss = lsDifGene.get(i);
			List<String> lsTmpLine = new ArrayList<>();
			lsTmpLine.add(ss[accId].replace("\"", ""));
			
			double treatLogValue = 0; double ctrlLogValue = 0;
			Map<String, Double> mapSample2Value = mapGeneID_2_Sample2MeanValue.get(ss[accId].replace("\"", ""));
			if (mapSample2Value == null) {
				continue;
			}
			treatLogValue = mapSample2Value.get(treatName);
			ctrlLogValue = mapSample2Value.get(controlName);
		
			if (!isLogValue()) {
				treatLogValue = getLogValue(treatLogValue);
				ctrlLogValue = getLogValue(ctrlLogValue);
			}
			lsTmpLine.add(treatLogValue + "");
			lsTmpLine.add(ctrlLogValue + "");
			lsTmpLine.add((treatLogValue - ctrlLogValue) + "");
			lsTmpLine.add(ss[mapColName2Num.get(pValue) + accId + 1] );
			lsTmpLine.add(ss[mapColName2Num.get(fdr) + accId + 1] );
			lsTmpLine.add(ss[mapColName2Num.get(Bvalue) + accId + 1] );
			lsResult.add(lsTmpLine.toArray(new String[0]));
		}
		return lsResult;
	}
	
	private double getLogValue(double value) {
		value = (value <= 1)? 0 : Math.log(value)/Math.log(2);
		return value;
	}
	
}
