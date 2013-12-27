package com.novelbio.analysis.diffexpress;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.HashMultimap;
import com.novelbio.analysis.IntCmdSoft;
import com.novelbio.base.PathDetail;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.cmd.ExceptionCmd;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.fileOperate.FileHadoop;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.service.SpringFactory;
import com.novelbio.generalConf.TitleFormatNBC;

import freemarker.template.Configuration;

public abstract class DiffExpAbs implements DiffExpInt, IntCmdSoft {
	
	// 满足条件的差异基因的最少数量
//	public static final int QUANUM = 1000;
	public static final double THRESHOLD1 = 0.01;
	public static final double THRESHOLD2 = 0.05;
	//火山图的长宽
	public static final int PLOT_WIDTH = 1024;
	public static final int PLOT_HEIGTH = 1024;
	
	//TODO这个值要改，需要在数据库里获取，前台表单填写到数据库中
	public static final String sequencingType = "表达谱芯片";
	
	EnumDifGene enumDifGene;
	
	Configuration freeMarkerConfiguration = (Configuration)SpringFactory.getFactory().getBean("freemarkNBC");
	String workSpace;
	String fileNameRawdata = "";
	String outScript = "";
	
	TitleFormatNBC titleFormatNBC;
	
	double logFCcutoff;
	
	double pValueOrFDRcutoff;
	
	List<String[]> lsGeneInfo = new ArrayList<String[]>();
	/**
	 * 一系列的表示基因分组的列，输入的时候就按照col进行了排序<br>
	 * 0: colNum, 实际number<br>
	 * 1: SampleGroupName
	 */
	List<String[]> lsSampleColumn2GroupName;
	/** 基因名
	 * 对应样本名
	 * 对应平均表达值
	 */
	Map<String, Map<String, Double>> mapGeneID_2_Sample2MeanValue;
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
	
	List<String> lsOutFile = new ArrayList<>();
	boolean calculate = false;
	
	boolean logTheValue = false;
	
	/** 是否提高算法的敏感度 */
	boolean isSensitive = false;
	
	String scriptContent;
	
	public DiffExpAbs() {
		setRworkspace();
		setOutScriptPath();
		setFileNameRawdata();
	}
	
	/** 设定是哪一种算法 */
	private void setEnumDifGene(EnumDifGene enumDifGene) {
		this.enumDifGene = enumDifGene;
	}
	/** 设定是否需要进行log，仅在limma中使用 */
	public void setLogValue(boolean isLog2Value) {
		this.logTheValue = isLog2Value;
	}
	/**
	 * 是否提高差异基因筛选的敏感度，意思就是挑选出更多的差异基因
	 * @param isSensitive
	 */
	public void setSensitive(boolean isSensitive) {
		this.isSensitive = isSensitive;
	}
	/**
	 * 一系列的表示基因分组的列<br>
	 * 0: colNum, 实际number<br>
	 * 1: SampleGroupName
	 */
	public void setCol2Sample(List<String[]> lsSampleColumn2GroupName) {
		//按列进行排序
		Collections.sort(lsSampleColumn2GroupName, new Comparator<String[]>() {
			public int compare(String[] o1, String[] o2) {
				Integer col1 = Integer.parseInt(o1[0]);
				Integer col2 = Integer.parseInt(o2[0]);
				return col1.compareTo(col2);
			}
		});
		this.lsSampleColumn2GroupName = lsSampleColumn2GroupName;
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
	public void setGeneInfo(List<String[]> lsGeneInfo) {
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
	protected boolean isLogValue() {
		return !logTheValue;
//		ArrayList<Double> lsValue = new ArrayList<Double>();
//		for (String[] strings : lsGeneInfo) {
//			int colNum = Integer.parseInt(lsSampleColumn2GroupName.get(0)[0]) - 1;
//			try {
//				double tmpValue = Double.parseDouble(strings[colNum]);
//				lsValue.add(tmpValue);
//			} catch (Exception e) { }
//		}
//		double result = MathComput.median(lsValue, 98);
//		if (result < 40) {
//			return true;
//		}
//		else {
//			return false;
//		}
	}
	
	/**
	 * <b>调用{@link #calculateResult()} 后才能调用</b><br>
	 * 返回文件名，以及对应的比较<br>
	 * key：文件全名<br>
	 * value：对应的比较。譬如 String[]{Treat, Control}
	 * @return
	 */
	public Map<String, String[]> getMapOutFileName2Compare() {
		return mapOutFileName2Compare;
	}
	
	/**
	 * 调用{@link #calculateResult()} 后才能调用
	 */
	public List<String> getResultFileName() {
		return lsOutFile;
	}
	/** 计算差异 */
	public void calculateResult() {
		if (calculate) {
			return;
		}
		calculate = true;
		//清空文件
		for (String fileName : mapOutFileName2Compare.keySet()) {
			FileOperate.DeleteFileFolder(fileName);
		}
		writeToGeneFile();
		setMapSample_2_time2value();
		scriptContent = generateScript();
		run();
		modifyResult();
//		clean();
		lsOutFile = plotDifParams();
	}
	/**
	 * 将输入的文件重整理成所需要的txt格式写入文本
	 */
	protected void writeToGeneFile() {
		TxtReadandWrite txtWrite = new TxtReadandWrite(fileNameRawdata, true);
		List<String[]> lsAnalysisGeneInfo = getAnalysisGeneInfo();
		String[] title = lsAnalysisGeneInfo.get(0);
		lsAnalysisGeneInfo = removeDuplicate(lsAnalysisGeneInfo.subList(1, lsAnalysisGeneInfo.size()));
		lsAnalysisGeneInfo.add(0, title);
		txtWrite.ExcelWrite(lsAnalysisGeneInfo);
		txtWrite.close();
	}
	/**
	 * 获得选定的基因ID和具体值
	 * 排序方式按照输入的lsSampleColumn2GroupName进行排序，不做调整
	 * @return
	 * 0： geneID
	 * 1-n：value
	 */
	protected List<String[]> getAnalysisGeneInfo() {
		ArrayList<String[]> lsResultGeneInfo = new ArrayList<String[]>();
		for (int m = 0; m < lsGeneInfo.size(); m++) {
			String[] strings = lsGeneInfo.get(m);
			
			String[] tmpResult = new String[lsSampleColumn2GroupName.size() + 1];
			tmpResult[0] = strings[colAccID];
			for (int i = 0; i < lsSampleColumn2GroupName.size(); i++) {
				int colNum = Integer.parseInt(lsSampleColumn2GroupName.get(i)[0]) - 1;
				//title
				if (m == 0) {
					tmpResult[i + 1] = strings[colNum];
					continue;
				}
				try {
					tmpResult[i + 1] = Double.parseDouble(strings[colNum].trim()) + "";
				} catch (Exception e) {
					tmpResult[i + 1] = 0 + "";
				}
			}
			lsResultGeneInfo.add(tmpResult);
		}
		return lsResultGeneInfo;
	}
	
	protected List<String[]> removeDuplicate(List<String[]> lsGeneInfo) {
		ArrayList<Integer> lsColID = new ArrayList<Integer>();
		for (int i = 2; i <= lsGeneInfo.size(); i++) {
			lsColID.add(i);
		}
		return MathComput.getMedian(lsGeneInfo, 1, lsColID);
	}
	
	private void setMapSample_2_time2value() {
		mapGeneID_2_Sample2MeanValue = new HashMap<>();
		for (String[] geneID2Info : lsGeneInfo) {
			String geneName = geneID2Info[colAccID];
			try {
				Map<String, Double> mapTime2value = mapTime2AvgValue(geneID2Info);
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
	protected Map<String, Double> mapTime2AvgValue(String[] info) {
		Map<String, List<Double>> mapTime2LsValue = new HashMap<>();
		
		for (int i = 0; i < lsSampleColumn2GroupName.size(); i++) {
			int colNum = Integer.parseInt(lsSampleColumn2GroupName.get(i)[0]) - 1;
			double value = Double.parseDouble(info[colNum]);
			String timeInfo = lsSampleColumn2GroupName.get(i)[1];//时期
			List<Double> lsValue = add_and_get_LsValue(timeInfo, mapTime2LsValue);
			lsValue.add(value);
		}
		HashMap<String, Double> mapTime2AvgValue = new HashMap<String, Double>();
		for (Entry<String, List<Double>> entry : mapTime2LsValue.entrySet()) {
			Double avgValue = MathComput.mean(entry.getValue());
			mapTime2AvgValue.put(entry.getKey(), avgValue);
		}
		return mapTime2AvgValue;
	}
	/** 
	 * 设定样本时期到具体值的信息。
	 * 没有该timeInfo就产生个新的list，有的话就获得原来的list 
	 */
	protected List<Double> add_and_get_LsValue(String timeInfo, Map<String, List<Double>> mapTime2value) {
		List<Double> lsValue = mapTime2value.get(timeInfo);
		if (lsValue == null) {
			lsValue = new ArrayList<Double>();
			mapTime2value.put(timeInfo, lsValue);
		}
		return lsValue;
	}
	
	/**
	 * 产生脚本文件，并返回脚本内容
	 * @return
	 */
	protected abstract String generateScript();
	
	protected String getWorkSpace() {
		return workSpace.replace("\\", "/");
	}
	
	/** 仅用于产生script中，会将hdfs的文件名转化为本地路径 */
	protected String getFileName() {
		return FileHadoop.convertToLocalPath(fileNameRawdata.replace("\\", "/"));
	}
	/**
	 * 调用Rrunning并写入Cmd的名字,
	 * 例如：
	 * Rrunning("DEseq")
	 */
	protected abstract void run();
	
	protected void Rrunning(String cmdName) {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add(PathDetail.getRscript());
		lsCmd.add(outScript.replace("\\", "/"));
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		cmdOperate.setGetLsErrOut();
		cmdOperate.run();
		if (!cmdOperate.isFinishedNormal()) {
			throw new ExceptionCmd(enumDifGene.toString() + " error:\n" + cmdOperate.getErrOut());
		}
		try { Thread.sleep(2000); } catch (Exception e) {}
	}
	
	/** 仅供AOP拦截使用，外界不要调用
	 * 拦截在其完成之后
	 */
	public void modifyResult() {
		for (Entry<String, String[]> entry : mapOutFileName2Compare.entrySet()) {
			String fileName = entry.getKey();
			String[] groupPaire = entry.getValue();
			List<String[]> lsResult = modifySingleResultFile(fileName, groupPaire[0], groupPaire[1]);
//			FileOperate.DeleteFileFolder(outFileName + outPutSuffix);
			//防止R还没输出结果就去读取
			FileOperate.DeleteFileFolder(fileName);
			//防止R还没输出结果就去读取
			try { Thread.sleep(50); } catch (Exception e) { }
			
//			TxtReadandWrite txtOutFinal = new TxtReadandWrite(fileName, true);
//			ExcelOperate excelOperate = new ExcelOperate(fileName);
//			excelOperate.setNBCExcel(true);
//			excelOperate.WriteExcel(1, 1, lsResult);
//			excelOperate.Close();
			
			fileName = FileOperate.changeFileSuffix(fileName, "", "xls");
			TxtReadandWrite txtOutFinal = new TxtReadandWrite(fileName, true);
			txtOutFinal.ExcelWrite(lsResult);
			txtOutFinal.close();
		}
	}
	protected abstract List<String[]> modifySingleResultFile(String outFileName, String treatName, String controlName);
	
	/** 删除中间文件 */
	public void clean() {
//		FileOperate.DeleteFileFolder(outScript);
		FileOperate.DeleteFileFolder(fileNameRawdata);
	}
	
	/**
	 * 筛选满足条件的差异基因并画图
	 * @return
	 */
	public List<String> plotDifParams() {
		ArrayList<String> lsOutFile = new ArrayList<>(); 
		Map<String, String[]> mapExcelName2Compare = getMapOutFileName2Compare();
		Map<String, DiffGeneVocalno> mapExcelName2DifResultInfo= new LinkedHashMap<String, DiffGeneVocalno>();
		
		for (String excelName : mapExcelName2Compare.keySet()) {
			mapExcelName2DifResultInfo.put(excelName, new DiffGeneVocalno(excelName, mapExcelName2Compare.get(excelName)));
		}
//		String[] threshold = DiffGeneVocalno.setThreshold(mapExcelName2DifResultInfo.values());
		//画图，出差异基因的表格
		for (String excelFileName : mapExcelName2DifResultInfo.keySet()) {
			DiffGeneVocalno difResultInfo = mapExcelName2DifResultInfo.get(excelFileName);
			String outFile = difResultInfo.writeDifGene();
			lsOutFile.add(outFile);
			titleFormatNBC= difResultInfo.getTitlePvalueFDR();
			logFCcutoff = DiffGeneVocalno.getUpfc();
			pValueOrFDRcutoff = difResultInfo.getPvalueFDRthreshold();
		}
		return lsOutFile;
	}

	public double getLogFC() {
		return logFCcutoff;
	}
	
	public double getpValueOrFDR() {
		return pValueOrFDRcutoff;
	}
	
	public TitleFormatNBC getTitleFormatNBC() {
		return titleFormatNBC;
	}
	
	/**
	 * 返回method的文字与其ID对照表
	 * ID就是本类的常量
	 */
	public static HashMap<String, EnumDifGene> getMapMethod2ID() {
		HashMap<String, EnumDifGene> mapMethod2ID = new LinkedHashMap<>();
		mapMethod2ID.put("Limma--Microarray", EnumDifGene.Limma);
		mapMethod2ID.put("DEGseq--RPKM(No Rep)", EnumDifGene.DEGSeq);
		mapMethod2ID.put("DESeq--Counts(Needs Rep)", EnumDifGene.DESeq);
		mapMethod2ID.put("EBSeq--Counts", EnumDifGene.EBSeq);
		mapMethod2ID.put("EdegR--Counts(Needs Rep)", EnumDifGene.EdgeR);
		mapMethod2ID.put("Ttest", EnumDifGene.Ttest);
		return mapMethod2ID;
	}
	
	public static DiffExpInt createDiffExp(EnumDifGene DiffExpID) {
		DiffExpInt diffExpInt = null;
		if (DiffExpID == EnumDifGene.Limma) {
			diffExpInt = (DiffExpInt)SpringFactory.getFactory().getBean("diffExpLimma");
		} else if (DiffExpID == EnumDifGene.DESeq) {
			diffExpInt = (DiffExpInt)SpringFactory.getFactory().getBean("diffExpDESeq");
		} else if (DiffExpID == EnumDifGene.DEGSeq) {
			diffExpInt = (DiffExpInt)SpringFactory.getFactory().getBean("diffExpDEGseq");
		} else if (DiffExpID == EnumDifGene.Ttest) {
			diffExpInt = (DiffExpInt)SpringFactory.getFactory().getBean("diffExpTtest");
		} else if (DiffExpID == EnumDifGene.EdgeR) {
			diffExpInt = (DiffExpInt)SpringFactory.getFactory().getBean("diffExpEdgeR");
		} else if (DiffExpID == EnumDifGene.EBSeq) {
			diffExpInt = (DiffExpInt)SpringFactory.getFactory().getBean("diffExpEBSeq");
		}
		((DiffExpAbs)diffExpInt).setEnumDifGene(DiffExpID);
		return diffExpInt;
	}

	/** 务必在run完之后调用 */
	public List<String> getCmdExeStr() {
		List<String> lsScript = new ArrayList<>();
		for (String string : scriptContent.split("\n")) {
			lsScript.add(string);
		}
	
		return lsScript;
	}
	
	/** 必须在设定完输入文件名后才能使用 */
	public HashMultimap<String, String> getPredictMapPrefix2Result() {
		HashMultimap<String, String> mapPrefix2File = HashMultimap.create();
		for (String outFileName : mapOutFileName2Compare.keySet()) {
			String[] compare = mapOutFileName2Compare.get(outFileName);
			mapPrefix2File.put(compare[0] + " vs " + compare[1], outFileName);
			String difGene = DiffGeneVocalno.getDifGeneFileName(outFileName);
			mapPrefix2File.put("difgene " + compare[0] + " vs " + compare[1], difGene);
		}
		return mapPrefix2File;
	}
}
