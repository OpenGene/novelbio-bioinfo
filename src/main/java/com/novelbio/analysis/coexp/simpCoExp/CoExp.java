
package com.novelbio.analysis.coexp.simpCoExp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math.MathException;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.log4j.Logger;

import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.generalConf.TitleFormatNBC;

public class CoExp {
	List<String[]> lsRawData;
	double pvalueCutoff = 0.01;
	/** 最多这么多对，超过了就会删除 */
	int maxPairNum = 1500000;
	/** 从第二行开始读 */
	int readFirstLine = 2;
	
	public void setPvalueCutoff(double pvalueCutoff) {
		this.pvalueCutoff = pvalueCutoff;
	}
	
	public void readTxtExcel(String inFile, int[] columnID) {
		if (columnID == null) {
			lsRawData = ExcelTxtRead.readLsExcelTxt(inFile, readFirstLine);
		} else {
			lsRawData = ExcelTxtRead.readLsExcelTxt(inFile, columnID, readFirstLine, -1);
		}
	}
	
	public void writeToExcel(String outFile) {		
		TxtReadandWrite txtOut = new TxtReadandWrite(outFile, true);
		for (String[] strings : calculateExp()) {
			txtOut.writefileln(strings);
		}
		txtOut.close();
	}
	
	public List<String[]> calculateExp() {
		List<CoexpGenInfo> lsCoexpGenInfos = initialData();
		List<CoexPair> lsCoexPairs = calCoExp(lsCoexpGenInfos);
		sortAndCutLsCoexpPair(lsCoexPairs);
		List<String[]> lsResult = new ArrayList<>();
		for (CoexPair coexPair : lsCoexPairs) {
			String[] tmp = coexPair.toStringArray();
			if (tmp != null) {
				lsResult.add(tmp);
			}
		}
		if (lsCoexPairs.get(0).getPvalue() >= 0) {
			lsResult.add(0, CoexPair.getTitleCoexp());
		} else {
			lsResult.add(0, CoexPair.getTitle());
		}
		return lsResult;
	}
	
	/** 给定原始数据，将需要的列挑选出来，并调用R获得简单共表达结果 */
	private List<CoexpGenInfo> initialData() {
		List<CoexpGenInfo> lsCoexpInfo = new ArrayList<CoexpGenInfo>();
		//将rawData注释上，没有symbol和description的通通去除，结果保存在lsRawData中
		for (String[] rowdata : lsRawData) {
			double[] exp = new double[rowdata.length - 1]; //获得每一行的表达值
			for (int j = 0; j < exp.length; j++) {
				exp[j] = Double.parseDouble(rowdata[j+1]);
			}
			CoexpGenInfo coexpGenInfo = new CoexpGenInfo(rowdata[0], 0, exp);
			lsCoexpInfo.add(coexpGenInfo);				
		}
		return lsCoexpInfo;
	}
	
	/**
	 * 获得pearson算好的内容，第一列为基因，第二列为基因，第三列：pearson值，第四列 pvalue，第五列 fdr，注意后续处理要去除其中的引号
	 * @param lsCoexpGenInfos
	 */
	private List<CoexPair> calCoExp(List<CoexpGenInfo> lsCoexpGenInfos) {
		List<CoexPair> lsCoexPairs = new ArrayList<>();
		PearsonCalculate pearsonCalculate = new PearsonCalculate(lsCoexpGenInfos);
		for (int i = 0; i < lsCoexpGenInfos.size() -1 ; i++) {
			for (int j = i+1 ; j < lsCoexpGenInfos.size(); j++) {
				CoexPair coexPair = pearsonCalculate.getCoexpPair(lsCoexpGenInfos.get(i), lsCoexpGenInfos.get(j));
				if (coexPair.getPvalue() > pvalueCutoff || coexPair.getPvalue().equals(Double.NaN)) {
					continue;
				}
				lsCoexPairs.add(coexPair);
			}
		}
		CoexPair.setLsFdr(lsCoexPairs);
		return lsCoexPairs;
	}
	
	/** 对结果进行排序，以及删除过多的值 */
	private void sortAndCutLsCoexpPair(List<CoexPair> lscCoexPairs) {
		Collections.sort(lscCoexPairs, new Comparator<CoexPair>() {
			@Override
			public int compare(CoexPair o1, CoexPair o2) {
				return o1.getPvalue().compareTo(o2.getPvalue());
			}
		});
		if (lscCoexPairs.size() > maxPairNum) {
			lscCoexPairs = lscCoexPairs.subList(0, maxPairNum);
		}
	}
	
}


/**
 * 仅比较两个对象的accID是否一致
 */
class CoexpGenInfo {
	String accID;
	int taxID;
	GeneID geneID;
	double[] expValue;
	
	/**
	 * 并不初始化，需要调用 {@link #initialGeneInfo()} 来初始化
	 * @param accID
	 * @param taxID
	 * @param expValue
	 */
	public CoexpGenInfo(String accID, int taxID,double[] expValue) {
		this.accID = accID;
		this.taxID = taxID;
		this.expValue = expValue;
	}
	
	protected void initialGeneInfo() {
		geneID = new GeneID(accID, taxID);
	}
	
	public GeneID getGeneID() {
		return geneID;
	}
	
	protected String getGeneName() {
		if (geneID == null) {
			return accID;
		}
		return geneID.getSymbol();
	}
	
	/**
	 * 获得表达值
	 * @return
	 */
	public double[] getExpValue() {
		return expValue;
	}
	
	public int hashCode() {
		double sum = MathComput.sum(expValue);
		return accID.hashCode() + (int)sum*1000;
	}
	
	/**
	 * 仅比较两个对象的accID是否一致
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		
		CoexpGenInfo otherObj = (CoexpGenInfo)obj;
		return accID.equals(otherObj.accID);
	}
}

class PearsonCalculate {
	private static final Logger logger = Logger.getLogger(PearsonCalculate.class);
	Map<CoexpGenInfo, Integer> mapGenInfos = null;
	double[][] exp = null;
	double[][] corInfo = null;
	double[][] corPvalue = null;
	
	public PearsonCalculate(List<CoexpGenInfo> lsGenInfos) {
		mapGenInfos = new HashMap<>();
		int num = 0;
		for (CoexpGenInfo coexpGenInfo : lsGenInfos) {
			mapGenInfos.put(coexpGenInfo, num);
			num++;
		}
		
		exp = new double[lsGenInfos.get(0).getExpValue().length][lsGenInfos.size()];
		for (int i = 0; i < lsGenInfos.size(); i++) {
			for (int j = 0; j < lsGenInfos.get(0).getExpValue().length ; j++) {
				exp[j][i] = lsGenInfos.get(i).getExpValue()[j];
			}
		}
		PearsonsCorrelation pearson = new PearsonsCorrelation(exp);
		corInfo = pearson.getCorrelationMatrix().getData();
		try {
			corPvalue = pearson.getCorrelationPValues().getData();
		} catch (Exception e) {
			logger.error("pearson 系数计算错误" + e.toString());
			e.printStackTrace();
		}
	}
	
	/**
	 * @param coexpGenInfo1
	 * @param coexpGenInfo2
	 * @return
	 */
	public CoexPair getCoexpPair(CoexpGenInfo coexpGenInfo1, CoexpGenInfo coexpGenInfo2) {
		int i = mapGenInfos.get(coexpGenInfo1);
		int j = mapGenInfos.get(coexpGenInfo2);
		double corValue = corInfo[i][j];
		double pvalue = corPvalue[i][j];
		
		CoexPair coexPair = new CoexPair();
		coexPair.setPvalue(pvalue);
		coexPair.setCorValue(corValue);
		coexPair.setCoexpPair(coexpGenInfo1, coexpGenInfo2);
		return coexPair;
	}
}

/**
 * 首先用setFirst()设定coexp的情况和数据矩阵
 * @author zong0jie
 *
 */
class CoexPair {
	private static final Logger logger = Logger.getLogger(CoexPair.class);
	CoexpGenInfo coexpGenInfo1;
	CoexpGenInfo coexpGenInfo2;
	double corValue = -1;
	double Pvalue = -1;
	double fdr = -1;
	boolean isGetSymbol;
	CoexPair() {}
	/** 第一列为基因，第二列为基因，第三列：pearson值，第四列 pvalue，第五列 fdr */
	CoexPair(int taxID, String[] info, boolean initial) {
		coexpGenInfo1 = new CoexpGenInfo(info[0], taxID, null);
		coexpGenInfo2 = new CoexpGenInfo(info[1], taxID, null);
		if (initial) {
			coexpGenInfo1.initialGeneInfo();
			coexpGenInfo2.initialGeneInfo();
		}
		corValue = Double.parseDouble(info[2]);
		Pvalue = Double.parseDouble(info[3]);
		fdr = Double.parseDouble(info[4]);
	}
	public void setPvalue(double pvalue) {
		Pvalue = pvalue;
	}
	public void setCorValue(double corValue) {
		this.corValue = corValue;
	}
	/** 是否读取数据库查找symbol */
	public void setGetSymbol(boolean isGetSymbol) {
		this.isGetSymbol = isGetSymbol;
	}
	public void setCoexpPair(CoexpGenInfo coexpGenInfo1, CoexpGenInfo coexpGenInfo2) {
		this.coexpGenInfo1 = coexpGenInfo1;
		this.coexpGenInfo2 = coexpGenInfo2;
	}

	public CoexpGenInfo getCoexpGenInfo1() {
		return coexpGenInfo1;
	}
	public CoexpGenInfo getCoexpGenInfo2() {
		return coexpGenInfo2;
	}
	
	public double getCorValue() {
		return corValue;
	}
	public Double getPvalue() {
		return Pvalue;
	}
	public void setFdr(double fdr) {
		this.fdr = fdr;
	}
	public String toString() {
		if (Pvalue < 0) {
			return null;
		}
		List<String> lsResult = new ArrayList<>();
		lsResult.add(coexpGenInfo1.getGeneID().getAccID());
		lsResult.add(coexpGenInfo1.getGeneID().getSymbol());
		lsResult.add(coexpGenInfo1.getGeneID().getDescription());
		lsResult.add(coexpGenInfo2.getGeneID().getAccID());
		lsResult.add(coexpGenInfo2.getGeneID().getSymbol());
		lsResult.add(coexpGenInfo2.getGeneID().getDescription());
		addCorInfo(lsResult);
		String[] result = lsResult.toArray(new String[0]);
		return ArrayOperate.cmbString(result, "\t");
	}
	public String[] toStringArrayAnno() {
		if (Pvalue < 0) {
			return null;
		}
		List<String> lsResult = new ArrayList<>();
		lsResult.add(coexpGenInfo1.getGeneID().getAccID());
		lsResult.add(coexpGenInfo1.getGeneID().getSymbol());
		lsResult.add(coexpGenInfo1.getGeneID().getDescription());
		lsResult.add(coexpGenInfo2.getGeneID().getAccID());
		lsResult.add(coexpGenInfo2.getGeneID().getSymbol());
		lsResult.add(coexpGenInfo2.getGeneID().getDescription());
		addCorInfo(lsResult);
		return lsResult.toArray(new String[0]);
	}
	public String[] toStringArray() {
		if (Pvalue < 0) {
			return null;
		}
		List<String> lsResult = new ArrayList<>();
		lsResult.add(coexpGenInfo1.getGeneName());
		lsResult.add(coexpGenInfo2.getGeneName());
		addCorInfo(lsResult);
		return lsResult.toArray(new String[0]);
	}
	
	private void addCorInfo(List<String> lsResult) {
		lsResult.add(corValue + "");
		lsResult.add(Pvalue + "");
		lsResult.add(fdr + "");
		String style = corValue > 0? "Positive" : "Negative";
		lsResult.add(style);
	}
	
	public static void setLsFdr(List<CoexPair> lsCoexPairs) {
		List<Double> lsPvalue = new ArrayList<Double>();
		for (CoexPair coexPair : lsCoexPairs) {
			lsPvalue.add(coexPair.getPvalue());
		}
		List<Double> lsfdr = MathComput.pvalue2Fdr(lsPvalue);
		for (int i = 0; i < lsfdr.size(); i++) {
			double fdrTmp = lsfdr.get(i);
			CoexPair coexPair = lsCoexPairs.get(i);
			coexPair.setFdr(fdrTmp);
		}
	}
	
	public static String[] getTitleAnnoCoexp() {
		List<String> lsTitle = new ArrayList<>();
		lsTitle.add(TitleFormatNBC.AccID.toString());
		lsTitle.add(TitleFormatNBC.Symbol.toString());
		lsTitle.add(TitleFormatNBC.Description.toString());
		lsTitle.add(TitleFormatNBC.AccID.toString());
		lsTitle.add(TitleFormatNBC.Symbol.toString());
		lsTitle.add(TitleFormatNBC.Description.toString());
		lsTitle.add("pearson");
		lsTitle.add(TitleFormatNBC.Pvalue.toString());
		lsTitle.add(TitleFormatNBC.FDR.toString());
		lsTitle.add("style");
		return lsTitle.toArray(new String[0]);
	}
	public static String[] getTitleCoexp() {
		List<String> lsTitle = new ArrayList<>();
		lsTitle.add(TitleFormatNBC.AccID.toString());
		lsTitle.add(TitleFormatNBC.AccID.toString());
		lsTitle.add("pearson");
		lsTitle.add(TitleFormatNBC.Pvalue.toString());
		lsTitle.add(TitleFormatNBC.FDR.toString());
		lsTitle.add("style");
		return lsTitle.toArray(new String[0]);
	}
	
	public static String[] getTitleAnno() {
		List<String> lsTitle = new ArrayList<>();
		lsTitle.add(TitleFormatNBC.AccID.toString());
		lsTitle.add(TitleFormatNBC.Symbol.toString());
		lsTitle.add(TitleFormatNBC.Description.toString());
		lsTitle.add(TitleFormatNBC.AccID.toString());
		lsTitle.add(TitleFormatNBC.Symbol.toString());
		lsTitle.add(TitleFormatNBC.Description.toString());
		return lsTitle.toArray(new String[0]);
	}
	public static String[] getTitle() {
		List<String> lsTitle = new ArrayList<>();
		lsTitle.add(TitleFormatNBC.AccID.toString());
		lsTitle.add(TitleFormatNBC.AccID.toString());
		return lsTitle.toArray(new String[0]);
	}
}
