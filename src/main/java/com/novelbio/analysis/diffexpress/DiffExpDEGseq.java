package com.novelbio.analysis.diffexpress;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.novelbio.base.PathDetail;
import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileHadoop;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.generalConf.TitleFormatNBC;

import freemarker.template.Template;

/**
 * 调用DEGseq算法，适用于RPKM的试验，譬如mRNAseq
 * @author zong0jie
 */
@Component
@Scope("prototype")
public class DiffExpDEGseq extends DiffExpAbs {
	private static final Logger logger = Logger.getLogger(DiffExpDEGseq.class);
	String outPutSuffix = "_Path";
	
	String outTmp = FileOperate.addSep(PathDetail.getTmpPathRandom());
	/** key: 输出文件的临时路径
	 * value: 输出文件的实际路径
	 */
	Map<String, String> mapTmp2Out = new HashMap<>();
	
	
	protected String generateScript() {
		Map<String,Object> mapData = new HashMap<String, Object>();
		mapData.put("workspace", getWorkSpace());
		mapData.put("filename", getFileName());
		mapData.put("mapSample2LsCol", getMapReadFileAndColumn());
		mapData.put("lsOutFileInfo", getLsCompareAndOutput());
		String scriptContent = null;
		try {
			Template template = freeMarkerConfiguration.getTemplate("/R/diffgene/DEGseq.ftl");
			StringWriter sw = new StringWriter();
			TxtReadandWrite txtReadandWrite = new TxtReadandWrite(outScript, true);
			// 处理并把结果输出到字符串中
			template.process(mapData, sw);
			scriptContent = sw.toString();
			txtReadandWrite.writefile(scriptContent);
			txtReadandWrite.close();
		} catch (Exception e) {
			throw new ExceptionFreemarker("DEGSeq rendering error:" + outScript, e);
		}
		return scriptContent;
	}

	private Map<String, String[]> getMapReadFileAndColumn() {
		Map<String, String[]> mapSampleName2ColInfo = new LinkedHashMap<String, String[]>();
		HashMap<String, ArrayList<Integer>> mapSample2LsCol = getMapSample2LsCol();
		for (Entry<String, ArrayList<Integer>> entry : mapSample2LsCol.entrySet()) {
			String[] colInfo = new String[2];
			colInfo[0] = (colAccID+1) + "";
			colInfo[1] = getRformatSampleVector(entry.getValue()) + "";
			mapSampleName2ColInfo.put( entry.getKey(), colInfo);
		}
		return mapSampleName2ColInfo;
	}

	/**
	 * 根据输入的col2sampleName，返回Map对照表
	 * @return
	 */
	private HashMap<String, ArrayList<Integer>> getMapSample2LsCol() {
		HashMap<String, ArrayList<Integer>> mapSample2LsCol = new LinkedHashMap<String, ArrayList<Integer>>();
		int i = 2;
		for (String[] strings : lsSampleColumn2GroupName) {
			strings[1] = strings[1].replace("\\", "/");
			ArrayList<Integer> lsColNum = null;
			if (mapSample2LsCol.containsKey(strings[1])) {
				lsColNum = mapSample2LsCol.get(strings[1]);
			} else {
				lsColNum = new ArrayList<Integer>();
				mapSample2LsCol.put(strings[1], lsColNum);
			}
			lsColNum.add(i++);
		}
		return mapSample2LsCol;
	}

	/**
	 * 给定样本col的list，返回样本所在的列1，2，4，5这个样子的文本
	 * 
	 * @param lsSample
	 * @return
	 */
	private String getRformatSampleVector(List<Integer> lsSample) {
		String result = lsSample.get(0) + "";
		for (int i = 1; i < lsSample.size(); i++) {
			result = result + ", " + lsSample.get(i);
		}
		return result;
	}
	
	/**
SampleTreat=0<br>
SampleTreatNum=1<br>
SampleTreatName=2<br>
SampleControl=3<br>
SampleControlNum=4<br>
SampleControlName=5<br>
OutDir=6
	 * @param content
	 * @return
	 */
	private List<String[]> getLsCompareAndOutput() {
		List<String[]> lsResult = new ArrayList<String[]>();
		HashMap<String, ArrayList<Integer>> mapSample2LsCol = getMapSample2LsCol();
		ArrayList<String> lsFileName = ArrayOperate.getArrayListKey(mapOutFileName2Compare);		
		for (String fileName : lsFileName) {
			String[] comparePair = mapOutFileName2Compare.get(fileName);
			String[] tmpResult = new String[7];
			tmpResult[0] = comparePair[0];//SampleTreat
			tmpResult[1] =  getSampleCol(mapSample2LsCol.get(comparePair[0]).size());//SampleTreatNum
			tmpResult[2] = comparePair[0];//SampleTreatName
			
			tmpResult[3] = comparePair[1];//SampleControl
			tmpResult[4] = getSampleCol(mapSample2LsCol.get(comparePair[1]).size());//SampleControlNum
			tmpResult[5] = comparePair[1];//SampleControlName
			
			String outPathReal = fileName.replace("\\", "/") + outPutSuffix;
			
			String parentPath = FileOperate.getParentPathNameWithSep(fileName);
			String tmpPath = outPathReal.replace(parentPath, outTmp);
			
			mapTmp2Out.put(tmpPath, outPathReal);
			FileOperate.createFolders(tmpPath);
			
			
			tmpResult[6] = FileHadoop.convertToLocalPath(tmpPath);//OutDir
			lsResult.add(tmpResult);
		}
		return lsResult;
	}
	/** 比较的列，直接返回类似1，2，3，4这个样子的文本即可 */
	private String getSampleCol(int sampleNum) {
		String result = "2";
		for (int i = 2; i <= sampleNum; i++) {
			result = result + ", " + (i+1);
		}
		return result;
	}
	
	@Override
	protected void setOutScriptPath() {
		outScript = workSpace + "DEGseq_" + DateUtil.getDateAndRandom() + ".R";
	}
	/** 输入文件写成txt文本 */
	@Override
	protected void setFileNameRawdata() {
		fileNameRawdata = workSpace + "DEGseqGeneInfo_" + DateUtil.getDateAndRandom() + ".txt";
	}
//	/**
//	 * 不需要提取专门的信息
//	 */
//	@Override
//	protected void writeToGeneFile() {
//		TxtReadandWrite txtWrite = new TxtReadandWrite(fileNameRawdata, true);
//		txtWrite.ExcelWrite(lsGeneInfo);
//		txtWrite.close();
//	}

	@Override
	protected void run() {
		running("DEGseq");
		for (String tmpFile : mapTmp2Out.keySet()) {
			String realFile = mapTmp2Out.get(tmpFile);
			FileOperate.moveFile(true, tmpFile, realFile);
		}
	}

	@Override
	protected List<String[]> modifySingleResultFile(String outFileName, String treatName, String controlName) {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		ArrayList<String[]> lsDifGene = ExcelTxtRead.readLsExcelTxt(FileOperate.addSep(outFileName + outPutSuffix) + "output_score.txt", 1);
		String[] title = new String[]{TitleFormatNBC.AccID.toString() , treatName, controlName, TitleFormatNBC.FoldChange.toString(),
				TitleFormatNBC.Log2FC.toString(), TitleFormatNBC.Pvalue.toString(), TitleFormatNBC.FDR.toString()};
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
			
			double fc = -1;
			try { fc = Double.parseDouble(tmpResult[3]); } catch (Exception e) { }
			
			if (!tmpResult[3].equals("NA") && !tmpResult[3].contains("Inf") && !tmpResult[3].equals("None") && fc != 0) {
				try {
					tmpResult[4] = Math.log(Double.parseDouble(tmpResult[3]))/ Math.log(2) + "";
				} catch (Exception e) {
				}
			} else if (fc == 0) {
				tmpResult[4] = -20 + "";
			} else if (tmpResult[3].toLowerCase().startsWith("inf")) {
				tmpResult[4] = 20 + "";
			} else if (tmpResult[3].equals("NA") || tmpResult[3].equals("None")) {
				tmpResult[4] = 0 + "";
			}
			lsResult.add(tmpResult);
		}
		return lsResult;
	}
	/** A除以B，中间处理了一些异常 */
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
			return "Infinity";
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
