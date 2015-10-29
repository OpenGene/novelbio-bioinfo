package com.novelbio.analysis.diffexpress;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.novelbio.analysis.IntCmdSoft;
import com.novelbio.base.ExceptionNullParam;
import com.novelbio.base.PathDetail;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.cmd.ExceptionCmd;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.fileOperate.FileHadoop;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.service.SpringFactoryBioinfo;
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
	
	Configuration freeMarkerConfiguration = (Configuration)SpringFactoryBioinfo.getFactory().getBean("freemarkNBC");
	String workSpace;
	String fileNameRawdata = "";
	String outScript = "";
	
	TitleFormatNBC tilePvalueOrFdr;
	double logFCcutoff;
	double pValueOrFDRcutoff = 0.05;
	
	/** 做差异基因的时候各个样本表达量的值之和不能小于该数值 */
	double minSampleSumNum = 0;
	/** 做差异基因的时候每个样本表达量的值不能都小于该数值
	 * 意思如果为 2, 3, 2, 1.3, 3
	 * 则当值设定为3时，上述基因删除 
	 */
	double minSampleSepNum = 0;
	
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
	/**基因唯一ID，必须没有重复，从0开始计算 */
	int colAccID = 0;
	/**
	 * 比较组与相应的输出文件名，可以输入一系列组
	 * map: condition to compare group <br>
	 * FileName <br>
	 * To<br>
	 * 0：treatment<br>
	 * 1：control
	 */
	HashMap<String, String[]> mapOutFileName2Compare = new LinkedHashMap<>();
	
	HashMap<String, Integer[]> mapDifGeneGroup2DifGeneNum = new LinkedHashMap<>();
	
	Map<String, DiffGeneFilter> mapCompare2DifResultInfo= new LinkedHashMap<>();
	
	List<String> lsOutFile = new ArrayList<>();
	boolean calculate = false;
	
	boolean logTheValue = false;
	
	/** 是否提高算法的敏感度 */
	boolean isSensitive = false;
	
	/** 结果中是否添加注释 */
	boolean isAddAnno = false;
	/** 结果中是否添加原始信号值 */
	boolean isAddInputData = true;
	int taxId;
	
	String scriptContent;
	TitleFormatNBC titlePvalueFdr = TitleFormatNBC.FDR;
		
	public DiffExpAbs() {
		setRworkspace();
		setOutScriptPath();
		setFileNameRawdata();
	}
	
	/** 做差异基因的时候各个样本表达量的值之和不能小于该数值
	 * 譬如如果多个样本表达量都为0，那就不考虑了
	 * @param addAllLine 默认为0，意思就是多个样本表达量之和为0 就不考虑
	 * 是小于的关系，不包括等于
	 */
	public void setMinSampleSumNum(double minSampleSumNum) {
		this.minSampleSumNum = minSampleSumNum;
	}
	/** 做差异基因的时候每个样本表达量的值不能都小于该数值
	 * 意思如果为 2, 3, 2, 1.3, 3
	 * 则当值设定为3时，上述基因删除
	 * @param minSampleSepNum 是小于的关系，不包括等于
	 */
	public void setMinSampleSepNum(double minSampleSepNum) {
		this.minSampleSepNum = minSampleSepNum;
	}
	/** 设定是哪一种算法 */
	private void setEnumDifGene(EnumDifGene enumDifGene) {
		this.enumDifGene = enumDifGene;
	}
	/** 设定是否需要进行log，仅在limma中使用 */
	public void setLogValue(boolean isLog2Value) {
		this.logTheValue = isLog2Value;
	}
	/** 默认是正负1，表示卡两倍阈值 */
	public void setLogFCcutoff(double logFCcutoff) {
		this.logFCcutoff = logFCcutoff;
	}
	/** 
	 * 设定用pvalue还是fdr卡，以及卡的阈值
	 * @param titlePvalueFdr
	 * @param threshold
	 */
	public void setThreshold(TitleFormatNBC titlePvalueFdr, double threshold) {
		this.titlePvalueFdr = titlePvalueFdr;
		this.pValueOrFDRcutoff = threshold;
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
	
	/** 将输入的名字修改为指定的输出名字 */
	private String getAllDifFileName(String filename) {
		return FileOperate.changeFileSuffix(filename, "_alldiff", "xls");
	}
	
	/**
	 * 设定输出比较组总差异基因个数、上调以及下调差异基因个数
	 * @param compareGroup
	 * @param difGeneNumber <br>
	 * 0: all difference gene number<br>
	 * 1: up difference gene number<br>
	 * 2: down difference gene number<br>
	 */
	public void addDifGeneGroup2DifGeneNum(String compareGroup, Integer[] difGeneNumber) {
		mapDifGeneGroup2DifGeneNum.put(compareGroup, difGeneNumber);
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
	 * <b>调用{@link #plotDifParams()} 后才能调用</b><br>
	 * 返回比较组名，以及对应的总差异基因个数，上调差异基因个数以及下调差异基因个数<br>
	 * key：比较组名<br>
	 * value：是一个数值型数组，
	 * 0 总的差异基因个数
	 * 1 上调差异基因个数
	 * 2 下调差异基因个数 
	 * @return
	 */
	public Map<String, Integer[]> getMapDifGeneGroup2DifGeneNum() {
		for (String compare : mapCompare2DifResultInfo.keySet()) {
			DiffGeneFilter difResultInfo = mapCompare2DifResultInfo.get(compare);
			Integer[] arrDifGeneNum = new Integer[3]; 
			arrDifGeneNum[0] = difResultInfo.getDifGeneNum();
			arrDifGeneNum[1] = difResultInfo.getUpGeneNum();
			arrDifGeneNum[2] = difResultInfo.getDownGeneNum();
			mapDifGeneGroup2DifGeneNum.put(compare, arrDifGeneNum);
		}
		
		return mapDifGeneGroup2DifGeneNum;
	}
	
	/**
	 * 调用{@link #calculateResult()} 后才能调用
	 */
	public List<String> getResultFileName() {
		return lsOutFile;
	}
	
	public void generateGeneAndScript() {
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
	}
	
	public void runAndModifyResult() {
		run();
		modifyResult();
		lsOutFile = plotDifParams();
	}
	
	/** 计算差异
	 * 包含了{@link #generateGeneAndScript()}和{@link #runAndModifyResult()}
	 * 两个方法
	 */
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
		lsGeneInfo = removeDuplicate();
		
		TxtReadandWrite txtWrite = new TxtReadandWrite(fileNameRawdata, true);
		List<String[]> lsAnalysisGeneInfo = getAnalysisGeneInfo();
		String[] title = lsAnalysisGeneInfo.get(0);
		txtWrite.writefileln(title);
		
		int m = 0;
		for (String[] info : lsAnalysisGeneInfo) {
			if (m++ <= 0) continue; //跳过第一行
			info[0] = CmdOperate.addQuot(info[0].replace("\"", ""));
			double sum = 0;
			boolean biggerThanMinSampleSep = false;
			for (int i = 1; i < info.length; i++) {
				double num = Double.parseDouble(info[i]);
				if (num >= minSampleSepNum) {
					biggerThanMinSampleSep = true;
				}
				sum += num;
			}
			if ((minSampleSumNum < 0 || sum >= minSampleSumNum)
					&& (minSampleSepNum < 0 || biggerThanMinSampleSep) 
				) {
				txtWrite.writefileln(info);
			}
		}
		txtWrite.close();
	}
	
	/** 将lsGeneInfo去重复 */
	protected List<String[]> removeDuplicate() {
		ArrayList<Integer> lsColID = new ArrayList<Integer>();
		for (String[] col2Group : lsSampleColumn2GroupName) {
			lsColID.add(Integer.parseInt(col2Group[0]));
		}
		List<String[]> lsTmpResult = MathComput.getMedian(lsGeneInfo.subList(1, lsGeneInfo.size()), colAccID+1, lsColID);
		lsTmpResult.add(0, lsGeneInfo.get(0));
		return lsTmpResult;
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
	

	
	private void setMapSample_2_time2value() {
		mapGeneID_2_Sample2MeanValue = new HashMap<>();
		for (String[] geneID2Info : lsGeneInfo) {
			String geneName = geneID2Info[colAccID];
			try {
				Map<String, Double> mapTime2value = mapTime2AvgValue(geneID2Info);
				mapGeneID_2_Sample2MeanValue.put(geneName, mapTime2value);
			} catch (Exception e) {
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
			double value = 0;
			try {
				value = Double.parseDouble(info[colNum]);
			} catch (Exception e) {
				
			}
			
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
		cmdOperate.runWithExp(enumDifGene.toString() + " error:");
		try { Thread.sleep(2000); } catch (Exception e) {}
	}
	
	/** 仅供AOP拦截使用，外界不要调用
	 * 拦截在其完成之后
	 */
	public void modifyResult() {
		ArrayListMultimap<String, Integer> mapGroup2LsColId = ArrayListMultimap.create();
		for (String[] col2Group : lsSampleColumn2GroupName) {
			mapGroup2LsColId.put(col2Group[1], Integer.parseInt(col2Group[0]) - 1);
		}
		
		for (Entry<String, String[]> entry : mapOutFileName2Compare.entrySet()) {
			String fileName = entry.getKey();
			String[] groupPaire = entry.getValue();
			List<String[]> lsResult = modifySingleResultFile(fileName, groupPaire[0], groupPaire[1]);
//			FileOperate.DeleteFileFolder(outFileName + outPutSuffix);
			//防止R还没输出结果就去读取
			FileOperate.DeleteFileFolder(fileName);
			//防止R还没输出结果就去读取
			try { Thread.sleep(50); } catch (Exception e) { }
			
			fileName = FileOperate.changeFileSuffix(fileName, "_alldiff", "xls");
			TxtReadandWrite txtOutFinal = new TxtReadandWrite(fileName, true);
			lsResult = addInputDataAndAnno(groupPaire, lsResult, mapGroup2LsColId);
			txtOutFinal.ExcelWrite(lsResult);
			txtOutFinal.close();
		}
	}
	
	/** 添加表达值和注释信息
	 * @param groupPaire
	 * @param lsDifGeneResult 第一行是title
	 * @param mapGroup2LsColId group--colId的list，注意colid从0开始计算
	 * @return
	 */
	private List<String[]> addInputDataAndAnno(String[] groupPaire, List<String[]> lsDifGeneResult, ArrayListMultimap<String, Integer> mapGroup2LsColId) {
		if (!isAddAnno && !isAddInputData) return lsDifGeneResult;
		
		List<String[]> lsResult = new ArrayList<>();
		List<String> lsTitle = ArrayOperate.converArray2List(lsDifGeneResult.get(0));
		Map<String, String[]> mapGeneName2LsValue = new HashMap<>();
		for (String[] info : lsGeneInfo) {
			mapGeneName2LsValue.put(info[colAccID], info);
		}
		List<Integer> lsColTreat = null, lsColCtrl = null;
		if (isAddInputData) {
			lsColTreat = mapGroup2LsColId.get(groupPaire[0]);
			lsColCtrl = mapGroup2LsColId.get(groupPaire[1]);
			
			for (Integer colTreat : lsColTreat) {
				lsTitle.add(lsGeneInfo.get(0)[colTreat]);
			}
			for (Integer colCtrl : lsColCtrl) {
				lsTitle.add(lsGeneInfo.get(0)[colCtrl]);
			}
		}
		
		if (isAddAnno) {
			lsTitle.add(TitleFormatNBC.Description.toString());
		}
		lsResult.add(lsTitle.toArray(new String[0]));
		
		for (int i = 1; i < lsDifGeneResult.size(); i++) {
			String[] difgene = lsDifGeneResult.get(i);
			List<String> lsGene = ArrayOperate.converArray2List(difgene);
			if (isAddInputData) {
				String[] lsValues = mapGeneName2LsValue.get(lsGene.get(0));
				for (Integer colTreat : lsColTreat) {
					lsGene.add(lsValues[colTreat]);
				}
				for (Integer colCtrl : lsColCtrl) {
					lsGene.add(lsValues[colCtrl]);
				}
			}
			if (isAddAnno) {
				GeneID geneID = new GeneID(lsGene.get(0), taxId);
				lsGene.add(geneID.getDescription());
			}
			lsResult.add(lsGene.toArray(new String[0]));
		}
		return lsResult;
	}
	
	protected abstract List<String[]> modifySingleResultFile(String outFileName, String treatName, String controlName);
	
	/** 删除中间文件 */
	public void clean() {
		FileOperate.DeleteFileFolder(outScript);
		FileOperate.DeleteFileFolder(fileNameRawdata);
	}
	
	/** 将中间文件拷贝到指定文件夹，文件夹必须存在 */
	public void copyTmpFileToPath(String path) {
		FileOperate.createFolders(path);
		FileOperate.copyFile(outScript, FileOperate.addSep(path) + FileOperate.getFileName(outScript), true);
		FileOperate.copyFile(fileNameRawdata, FileOperate.addSep(path) + FileOperate.getFileName(fileNameRawdata), true);		
	}
	
	/**
	 * 筛选满足条件的差异基因并画图
	 * @return
	 */
	public List<String> plotDifParams() {
		ArrayList<String> lsOutFile = new ArrayList<>(); 
		mapCompare2DifResultInfo.clear();
		
		for (String excelName : mapOutFileName2Compare.keySet()) {
			String[] compare = mapOutFileName2Compare.get(excelName);
			String prefix = FileOperate.getFileNameSep(excelName)[0];
			String filename = getAllDifFileName(excelName);
			mapCompare2DifResultInfo.put(getCompare(compare), new DiffGeneFilter(filename, prefix));
		}
//		String[] threshold = DiffGeneVocalno.setThreshold(mapExcelName2DifResultInfo.values());
		//画图，出差异基因的表格
		for (String compare : mapCompare2DifResultInfo.keySet()) {
			DiffGeneFilter difResultInfo = mapCompare2DifResultInfo.get(compare);
			difResultInfo.setThreshold(titlePvalueFdr, pValueOrFDRcutoff);
			difResultInfo.setLogfcCol(Math.abs(logFCcutoff), -Math.abs(logFCcutoff));
			String outFile = difResultInfo.writeDifGene();
			lsOutFile.add(outFile);
			tilePvalueOrFdr= difResultInfo.getTitlePvalueFDR();
			logFCcutoff = difResultInfo.getUpfc();
			pValueOrFDRcutoff = difResultInfo.getPvalueFDRthreshold();
		}
		return lsOutFile;
	}

	private String getCompare(String[] compare) {
		return compare[0] + "vs" + compare[1];
	}
	
	public double getLogFC() {
		return logFCcutoff;
	}
	
	public double getpValueOrFDR() {
		return pValueOrFDRcutoff;
	}
	
	public TitleFormatNBC getTitleFormatNBC() {
		return tilePvalueOrFdr;
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
		mapMethod2ID.put("edgeR--Counts(Needs Rep)", EnumDifGene.EdgeR);
		mapMethod2ID.put("Ttest", EnumDifGene.Ttest);
		return mapMethod2ID;
	}
	
	public static DiffExpInt createDiffExp(EnumDifGene DiffExpID) {
		if (DiffExpID == null) {
			throw new ExceptionNullParam("No DiffExpID");
		}
		DiffExpInt diffExpInt = null;
		if (DiffExpID == EnumDifGene.Limma) {
			diffExpInt = (DiffExpInt)SpringFactoryBioinfo.getFactory().getBean("diffExpLimma");
		} else if (DiffExpID == EnumDifGene.DESeq) {
			diffExpInt = (DiffExpInt)SpringFactoryBioinfo.getFactory().getBean("diffExpDESeq");
		} else if (DiffExpID == EnumDifGene.DEGSeq) {
			diffExpInt = (DiffExpInt)SpringFactoryBioinfo.getFactory().getBean("diffExpDEGseq");
		} else if (DiffExpID == EnumDifGene.Ttest) {
			diffExpInt = (DiffExpInt)SpringFactoryBioinfo.getFactory().getBean("diffExpTtest");
		} else if (DiffExpID == EnumDifGene.EdgeR) {
			diffExpInt = (DiffExpInt)SpringFactoryBioinfo.getFactory().getBean("diffExpEdgeR");
		} else if (DiffExpID == EnumDifGene.EBSeq) {
			diffExpInt = (DiffExpInt)SpringFactoryBioinfo.getFactory().getBean("diffExpEBSeq");
		}
		((DiffExpAbs)diffExpInt).setEnumDifGene(DiffExpID);
		return diffExpInt;
	}

	/** 调用{@link #generateGeneAndScript()}后可以使用 */
	public List<String> getCmdExeStr() {
		List<String> lsScript = new ArrayList<>();
		if (scriptContent == null) {
			return lsScript;
		}
		for (String string : scriptContent.split("\n")) {
			lsScript.add(CmdOperate.makePathToRelative(string));
		}
	
		return lsScript;
	}

}
