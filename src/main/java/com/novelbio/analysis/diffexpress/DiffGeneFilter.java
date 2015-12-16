package com.novelbio.analysis.diffexpress;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.generalConf.TitleFormatNBC;

/**
 * 用来筛选差异基因,
 * 需要指定pvalue、fdr、logfc
 * @author zong0jie
 */
 public class DiffGeneFilter {
	 /**给出图中的比例*/
	 public static final double PRE = 0.999;
		
	// 满足条件的差异基因数量,默认是1000，在构造方法里面设定，数量的10/1
	public static  int QUANUM = 1000;
	/** 75%的比较结果都要超过这个值 */
	public static final double MIN_PROP_NUM = 0.75;
	
	public static final double THRESHOLD1 = 0.01;
	public static final double THRESHOLD2 = 0.05;
	
	/** 上下调的阈值 */
	public double upfc = 1;
	public double downfc = -1;
	
	/** 输入的文件全名 */
	String excelFileName;
	/** 需要输出的文件前缀 */
	String excelPrefix;
	
	List<Double> lsFDR = new ArrayList<Double>();
	List<Double> lsPvalue = new ArrayList<Double>();
	List<Double> lsLogFC = new ArrayList<Double>();
	/** 具体的值，第一行是标题 */
	List<String[]> lslsInfo;
	/** 差异基因的值 */
	List<String[]> lslsDifGene;
	
	/** 画图的时候用pvalue还是fdr做阈值 */
	TitleFormatNBC titlePvalueFDR = TitleFormatNBC.FDR;
	/** pvalue或fdr的阈值，是0.05还是0.01 */
	double pvalueFDRthreshold = THRESHOLD2;	
	/** pValue的列号, 从0开始计算 */
	int pvalueCol = -1;
	/** LogFC的列号, 从0开始计算*/
	int logfcCol = -1;
	/** FDR的列号, 从0开始计算 */
	int fdrCol = -1;
	/** 上下调基因的数量，注意，必须在调用{@link #getDifGeneNum()}后才能使用 */
	int[] upDownNum = new int[2];
	
	public DiffGeneFilter(String excelName, String excelPrefix) {
		this.excelFileName = excelName;
		this.excelPrefix = excelPrefix;
		lslsInfo = ExcelTxtRead.readLsExcelTxt(excelName, 0);
		QUANUM = lslsInfo.size()/10;
		String[] lsTitle = lslsInfo.get(0);
		fdrCol = findColNum(lsTitle, TitleFormatNBC.FDR.toString());
		pvalueCol = findColNum(lsTitle, TitleFormatNBC.Pvalue.toString());
		logfcCol = findColNum(lsTitle, TitleFormatNBC.Log2FC.toString());
		
		List<String[]> lsInfoWithoutTitle = lslsInfo.subList(1, lslsInfo.size());
		lsFDR = readListListCol(lsInfoWithoutTitle, fdrCol, 0,1);
		if (pvalueCol > 0) {
			lsPvalue = readListListCol(lsInfoWithoutTitle, pvalueCol, 0, 1);
			List<Double>  lsPvalueOut_0 = readListListColOut_0(lsInfoWithoutTitle,pvalueCol);
			Collections.sort(lsPvalueOut_0);
		}
		lsLogFC = readListListCol(lsInfoWithoutTitle, logfcCol, 20, 0);
	}
	
	public double getPvalueFDRthreshold() {
		return pvalueFDRthreshold;
	}
	
	public TitleFormatNBC getTitlePvalueFDR() {
		return titlePvalueFDR;
	}
	
	public double getUpfc() {
		return upfc;
	}
	/** 
	 * 设定用pvalue还是fdr卡，以及卡的阈值
	 * @param titlePvalueFdr
	 * @param threshold 小于等于
	 */
	public void setThreshold(TitleFormatNBC titlePvalueFdr, double threshold) {
		this.titlePvalueFDR = titlePvalueFdr;
		this.pvalueFDRthreshold = threshold;
	}
	/** 设定差异倍数 */
	public void setLogfcCol(double upfc, double downfc) {
		this.upfc = upfc;
		this.downfc = downfc;
	}
	
	/**
	 *  根据title名字获取的列号
	 * @param lsTitle
	 * @param colName
	 * @return 返回从0开始的计数
	 */
	private int findColNum(String[] lsTitle, String colName) {
		int colNum = -1;
		for (int i = 0; i < lsTitle.length; i++) {
			if (lsTitle[i].equalsIgnoreCase(colName)) {
				colNum = i;
			}
		}
		return colNum;
	}

	/**
	 * 读取excel的某一列
	 * @param lsls 注意输入的lsls不包含title
	 * @param colNum 从0开始的计数
	 * @param inf inf的数值
	 * @param naValue 如果是NA，那么用什么值来填充
	 * @return
	 */
	public static List<Double> readListListCol(List<String[]> lsls, int colNum, double inf, double naValue) {
		if (colNum < 0) {
			return new ArrayList<Double>();
		}
		List<Double> lsCol = new ArrayList<Double>();
		for (String[] list : lsls) {
			String str = list[colNum];
			double value;
			//TODO 尚未考虑NA等情况
			if (str.trim().toLowerCase().startsWith("inf")) {
				value = inf;
			} else if (str.trim().toLowerCase().startsWith("-inf")) {
				value = -inf;
			} else if (str.trim().toLowerCase().startsWith("na")) {
				value = naValue;
			} else {
				try {
					value = Double.parseDouble(str);
				} catch (Exception e) {
					value = naValue;
				}
			}
			lsCol.add(value);
		}
		return lsCol;
	}
	/**
	 * 读取excel的某一列,遇到NA不计入
	 * @param lsls 注意输入的lsls不包含title
	 * @param colNum 从0开始的计数
	 * @return
	 */
	public static List<Double> readListListColOut_0(List<String[]> lsls, int colNum) {
		if (colNum < 0) {
			return new ArrayList<Double>();
		}
		List<Double> lsCol = new ArrayList<Double>();
		for (String[] ss: lsls) {
			String str = ss[colNum];
			double value;
			//TODO 尚未考虑NA等情况
			try {
				value = Double.parseDouble(str);
			} catch (Exception e) {
				continue;
			}
			lsCol.add(value);
		}
		return lsCol;
	}

	/** 根据阈值，获得差异基因的个数 */
	public int getDifGeneNum() {
		return getLsDifGene().size() - 1;
	}
	/**  上下调基因的数量，注意，必须在调用{@link #getDifGeneNum()}后才能使用 */
	public int getUpGeneNum() {
		return upDownNum[0];
	}
	/**  上下调基因的数量，注意，必须在调用{@link #getDifGeneNum()}后才能使用 */
	public int getDownGeneNum() {
		return upDownNum[1];
	}
	
	/**
	 * 写差异基因进入文本
	 * 具体文本名可以通过 {@link #getDifGeneFileName()} 获得
	 * @param difResultInfo
	 * @param excelFileName
	 * @return
	 */
	public String writeDifGene() {
		String outFile = getDifGeneFileName(excelFileName, excelPrefix);
		List<String[]> lsResult = getLsDifGene();
		
		FileOperate.DeleteFileFolder(outFile);
		ExcelOperate excelOperate = new ExcelOperate(outFile);
		excelOperate.writeExcel(lsResult);
		excelOperate.close();
		
		String txtFile = FileOperate.changeFileSuffix(outFile, "", "txt");
		TxtReadandWrite txtOutFinal = new TxtReadandWrite(txtFile, true);
		for (String[] ss : lsResult) {
			txtOutFinal.writefileln(ss);
		}
		txtOutFinal.close();
		return outFile;
	}
	
	/** 获得筛选差异后的文件名 */
	public static String getDifGeneFileName(String excelFileName, String excelPrefix) {
		String fileName = FileOperate.getPathName(excelFileName) + excelPrefix;
		return FileOperate.changeFileSuffix(fileName, ".diff", "xls");
	}
	
	/**
	 * 根据阈值，获得差异基因的表，可以直接写入excel中
	 * 第一行是title
	 */
	public List<String[]> getLsDifGene() {
		if (lslsDifGene != null && lslsDifGene.size() > 0) {
			return lslsDifGene;
		}
		
		lslsDifGene = new ArrayList<String[]>();
		if (pvalueCol < 0 && titlePvalueFDR != TitleFormatNBC.FDR) {
			titlePvalueFDR = TitleFormatNBC.FDR;
		}
		
		for (int i = 0; i < lsFDR.size(); i++) {
			double pvalue = (pvalueCol > 0)? lsPvalue.get(i) : lsFDR.get(i);
			double logfc = lsLogFC.get(i);
			if (titlePvalueFDR == TitleFormatNBC.FDR) {
				pvalue = lsFDR.get(i);
			}
			
			if (pvalue <= pvalueFDRthreshold && (logfc >= upfc || logfc <= downfc)) {
				//因为lslsInfo的第一行是title，所以要加上1行以保持一致
				List<String> lsValue = ArrayOperate.converArray2List(lslsInfo.get(i+1));
				if (logfc >= upfc) {
					upDownNum[0]++;
					lsValue.add(getLastCol()+1, "up");
				} else {
					upDownNum[1]++;
					lsValue.add(getLastCol()+1, "down");
				}
				lslsDifGene.add(lsValue.toArray(new String[0]));
			}
		}
		
		//先按照fdr排序，一样的话按照pvalue排序，再一样按照foldchange排序
		Collections.sort(lslsDifGene, new Comparator<String[]>() {
			public int compare(String[] o1, String[] o2) {
				Double pvalue1= 0.0, pvalue2 = 0.0, fdr1 = 0.0, fdr2 = 0.0, logfc1 = 0.0, logfc2 = 0.0;
				if (pvalueCol >= 0) {
					pvalue1 = Double.parseDouble(o1[pvalueCol]);
					pvalue2 = Double.parseDouble(o2[pvalueCol]);
				}
				if (fdrCol >= 0) {
					fdr1 = Double.parseDouble(o1[fdrCol]);
					fdr2 = Double.parseDouble(o2[fdrCol]);
				}
				if (logfcCol >= 0) {
					logfc1 = Double.parseDouble(o1[logfcCol]);
					logfc2 = Double.parseDouble(o2[logfcCol]);
				}
				int result = fdr1.compareTo(fdr2);
				if (result == 0) {
					result = pvalue1.compareTo(pvalue2);
				}
				if (result == 0) {
					result = -logfc1.compareTo(logfc2);
				}
				return result;
			}
		});
		List<String> lsTitle = ArrayOperate.converArray2List(lslsInfo.get(0));
		lsTitle.add(getLastCol()+1, TitleFormatNBC.Style.toString());
		lslsDifGene.add(0, lsTitle.toArray(new String[0]));
		return lslsDifGene;
	}
	
	/** 获得pvalue或fdr或logfc，这三列中最靠后的一列，会把up，down的信息加在这一列的后面。从0开始计算 */
	private int getLastCol() {
		return Math.max(pvalueCol, Math.max(fdrCol, logfcCol));
	}
	
	/** 画的火山图的路径 */
	public String getVolcanoFileName() {
		return FileOperate.changeFileSuffix(excelFileName, "_volcano", "jpg");
	}
	
	/**
	 * 自动卡一个阈值 如果FDR小于0.01的有1000个，那么阈值就是FDR=0.01
	 * 否则如果FDR小于0.05的有1000个，那么阈值就是FDR=0.05
	 * 否则考虑pValue，如果P-value小于0.01的有大于1000个，那么阈值是pvalue0.01， 在不行阈值选择pvalue=0.05
	 * 
	 * @param lslspValue 输入的pvalue
	 * @param lslsFDR 输入的fdr
	 * @return 返回卡定的阈值 string[2] 0是pvalue还是fdr<br>
	 * 1：具体的阈值
	 */
	public static String[] setThreshold(Collection<DiffGeneFilter> lslspValue, double upfc, double downfc) {
		int thresholdNum = (int)(lslspValue.size() * MIN_PROP_NUM + 1);
		/** 超过1000个基因的样本数量 */
		int pvalueUpThresh1 = 0, pvalueUpThresh2 = 0, fdrUpThresh1 = 0, fdrUpThresh2 = 0;
		for (DiffGeneFilter difResultInfo : lslspValue) {
			int FDR1Num = 0, FDR2Num = 0, pValue1Num = 0, pValue2Num = 0;
			for (int i = 0; i < difResultInfo.lsFDR.size(); i++) {
				double fdr = difResultInfo.lsFDR.get(i);
				double fc = difResultInfo.lsLogFC.get(i);
				if (fc >= downfc && fc <= upfc ) continue;
				
				if (fdr < THRESHOLD1) FDR1Num++;
				if (fdr < THRESHOLD2) FDR2Num++;
			}
			for (int i = 0; i < difResultInfo.lsPvalue.size(); i++) {
				double pvalue = difResultInfo.lsPvalue.get(i);
				double fc = difResultInfo.lsLogFC.get(i);
				if (fc >= downfc && fc <= upfc ) continue;
				
				if (pvalue < THRESHOLD1) pValue1Num++;
				if (pvalue < THRESHOLD2)pValue2Num++;
			}
			
			if (FDR1Num >= QUANUM) fdrUpThresh1++;
			if (FDR2Num >= QUANUM) fdrUpThresh2++;
			if (pValue1Num >= QUANUM) pvalueUpThresh1++;
			if (pValue2Num >= QUANUM) pvalueUpThresh2++;
		}
		TitleFormatNBC title = TitleFormatNBC.Pvalue;
		double threshold = THRESHOLD2;
		if (fdrUpThresh1 >= thresholdNum) {
			title = TitleFormatNBC.FDR;
			threshold = THRESHOLD1;
		} else {
			title = TitleFormatNBC.FDR;
			threshold = THRESHOLD2;
		}
		
//		if (fdrUpThresh1 >= thresholdNum) {
//			title = TitleFormatNBC.FDR;
//			threshold = THRESHOLD1;
//		} else if (fdrUpThresh2 >= thresholdNum) {
//			title = TitleFormatNBC.FDR;
//			threshold = THRESHOLD2;
//		} else if (pvalueUpThresh1 >= thresholdNum) {
//			title = TitleFormatNBC.Pvalue;
//			threshold = THRESHOLD1;
//		} else if (pvalueUpThresh2 >= thresholdNum) {
//			title = TitleFormatNBC.Pvalue;
//			threshold = THRESHOLD2;
//		}
		
		for (DiffGeneFilter difResultInfo : lslspValue) {
			difResultInfo.setThreshold(title, threshold);
		}
		return new String[]{title.toString(), threshold + ""};
	}
}