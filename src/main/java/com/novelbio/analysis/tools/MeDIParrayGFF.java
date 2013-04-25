package com.novelbio.analysis.tools;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math.stat.StatUtils;
import org.apache.commons.math.stat.inference.TestUtils;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.fileOperate.FileOperate;
/**
 * 解析罗氏的甲基化芯片Gff文件
 * @author zong0jie
 *
 */
public class MeDIParrayGFF {
	public static final int BIG_TAIL = 200;
	public static final int SMALL_TAIL = 300;
	public static final int TWO_TAIL = 100;
	
	/** 从0开始 */
	int colChrID = 0;
	int colStart = 2;
	int colEnd = 3;
	
	List<String> lsMedipGffFile = new ArrayList<String>();
	List<String> lsMedipGffFileRunning;
	List<String> lsPrefix;
	int tTestType = TWO_TAIL;
	/** 是否计算了pvalue，如果不符合t检验的条件，就不会进行t检验 */
	boolean isPvalueCal = true;
	
	String outFile;
	
	int[] colSample1;
	int[] colSample2;
	
	double fcUp = 2;
	double fcDown = 0.5;
	double pvalue = 0.05;
	int probNum = 2;
	
	/** 从0开始 */
	int colRatio = -1;
	int colPvalue = -1;
	
	public static void main(String[] args) {
		String path = "/media/winF/NBC/Project/methy_QYY/rawdata/AMS/";
		
		MeDIParrayGFF meDIParrayGFF = new MeDIParrayGFF();
//		meDIParrayGFF.addRawGffRatioFile(path + "C4_ratio.gff");
//		meDIParrayGFF.addRawGffRatioFile(path + "FA53_ratio.gff");
//		meDIParrayGFF.addRawGffRatioFile(path + "T11_ratio.gff");
//		meDIParrayGFF.addRawGffRatioFile(path + "T33_ratio.gff");
//		meDIParrayGFF.addRawGffRatioFile(path + "T8_ratio.gff");
//		meDIParrayGFF.addRawGffRatioFile(path + "T9_ratio.gff");
//		meDIParrayGFF.preCope();
		
		meDIParrayGFF.lsMedipGffFileRunning = new ArrayList<String>();

		meDIParrayGFF.lsMedipGffFileRunning.add(path + "C4_ratio_Cope.gff.gff");
		meDIParrayGFF.lsMedipGffFileRunning.add(path + "FA53_ratio_Cope.gff.gff");
		meDIParrayGFF.lsMedipGffFileRunning.add(path + "T11_ratio_Cope.gff.gff");
		meDIParrayGFF.lsMedipGffFileRunning.add(path + "T33_ratio_Cope.gff.gff");
		meDIParrayGFF.lsMedipGffFileRunning.add(path + "T8_ratio_Cope.gff.gff");
		meDIParrayGFF.lsMedipGffFileRunning.add(path + "T9_ratio_Cope.gff.gff");
		meDIParrayGFF.setOutFile("/media/winF/NBC/Project/methy_QYY/rawdata/AMS/ams_P0.05.txt");
		meDIParrayGFF.setProbNum(3);
		meDIParrayGFF.setColTreat(new int[]{3, 4 ,5, 6});
		meDIParrayGFF.setColControl(new int[]{1, 2});
		meDIParrayGFF.calculateResult();
	}
	
	public void initial() {
		colChrID = 0;
		colStart = 2;
		colEnd = 3;
		
		lsMedipGffFile = new ArrayList<String>();
		lsMedipGffFileRunning = null;
		lsPrefix = null;
		tTestType = TWO_TAIL;
		/** 是否计算了pvalue，如果不符合t检验的条件，就不会进行t检验 */
		isPvalueCal = true;
		
		outFile = "";;
		
		colSample1 = null;
		colSample2 = null;
		
		fcUp = 2;
		fcDown = 0.5;
		pvalue = 0.05;
		probNum = 3;
		
		colRatio = -1;
		colPvalue = -1;
	}
	
	/**
	 * 设定gffRatio文件
	 * @param gffRatioFile
	 */
	public void addRawGffRatioFile(String gffRatioFile) {
		lsMedipGffFile.add(gffRatioFile);
	}
	
	public void setOutFile(String outFile) {
		this.outFile = outFile;
	}
	/** 连续几根探针甲基化就算差异，默认是3 */
	public void setProbNum(int probNum) {
		this.probNum = probNum;
	}
	public void setColTreat(int[] colSample1) {
		for (int i = 0; i < colSample1.length; i++) {
			colSample1[i] = 4 + colSample1[i];
		}
		this.colSample1 = colSample1;
	}
	public void setColControl(int[] colSample2) {
		for (int i = 0; i < colSample2.length; i++) {
			colSample2[i] = 4 + colSample2[i];
		}
		this.colSample2 = colSample2;
	}
	
	/** 将文件进行预处理 */
	protected void preCope() {
		lsMedipGffFileRunning = new ArrayList<String>();
		for (String gffFile : lsMedipGffFile) {
			String copedFileName = FileOperate.changeFileSuffix(gffFile, "_Cope", null);
			try {
				format(gffFile, copedFileName);
				lsMedipGffFileRunning.add(copedFileName);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 将Gff文件整理为MEME可识别的文件
	 * @param ratiogff
	 * @param outFile
	 * @throws Exception
	 */
	private static void format(String ratiogff, String outFile) throws Exception {
		TxtReadandWrite txtGff = new TxtReadandWrite(ratiogff, false);
		TxtReadandWrite txtOut = new TxtReadandWrite(outFile, true);
		txtOut.writefileln("chrID\tMATCH_INDEX\tprobID\tstart\tstop\tscore");
		HashSet<String> hashUniq = new HashSet<String>();//去冗余用
		for (String content : txtGff.readlines()) {
			if (content.startsWith("#")) {
				continue;
			}
			String[] ss = content.split("\t");
			String result = ss[0] + "\t" + ss[1] + "\t" + ss[8].split(";")[1].split("=")[1] + "\t" + ss[3] + "\t" + ss[4] + "\t" + ss[5];
			if (hashUniq.contains(result.split("\t")[2])) {
				continue;
			}
			hashUniq.add(result.split("\t")[2]);
			txtOut.writefileln(result);
		}
		txtOut.close();
		txtGff.close();
	}
	
	protected void MEDME() {
		// TODO 调用R处理lsMedipGffFileRunning中的东西
	}
	
	protected void calculateResult() {
		ArrayList<ArrayList<String>> lsCombine = combAMSfile();
		calculatePvalueT(lsCombine);
		lsCombine.add(0, getLsTitle());
		TxtReadandWrite txtOutAll = new TxtReadandWrite(FileOperate.changeFileSuffix(outFile, "_All", "txt"), true);
		txtOutAll.writefileln(lsCombine);
		txtOutAll.close();
		
		ArrayList<String[]> lsResult = getCombProbe(lsCombine);
		lsResult.add(0, getLsTitle().toArray(new String[0]));
		TxtReadandWrite txtOutFilter = new TxtReadandWrite(FileOperate.changeFileSuffix(outFile, "_Filter", "txt"), true);
		txtOutFilter.ExcelWrite(lsResult);
		txtOutFilter.close();
		
		ArrayList<String[]> lsFilterCombine = copeFinal(lsResult);
		TxtReadandWrite txtOutCombine = new TxtReadandWrite(FileOperate.changeFileSuffix(outFile, "_Combine", "txt"), true);
		txtOutCombine.ExcelWrite(lsFilterCombine);
		txtOutCombine.close();
	}
	
	/**
	 * 合并文件
	 */
	protected ArrayList<ArrayList<String>> combAMSfile() {
		Map<String, String> mapPrix2Txt = new LinkedHashMap<String, String>();
		ArrayList<ArrayList<String>> lsResult = new ArrayList<ArrayList<String>>();
		lsPrefix = new ArrayList<String>();
		for (String fileName : lsMedipGffFileRunning) {
			String prix = FileOperate.getFileNameSep(fileName)[0];
			mapPrix2Txt.put(prix, fileName);
			lsPrefix.add(prix);
		}

		int i = 0;
		for (String prefix : mapPrix2Txt.keySet()) {
			TxtReadandWrite txtRead = new TxtReadandWrite(mapPrix2Txt.get(prefix), false);
			int linNum = 0;
			for (String info : txtRead.readlines(2)) {
				String[] strings = info.split(" ");
				ArrayList<String> lsDetail;
				if (i == 0) {
					lsDetail = new ArrayList<String>();
					lsResult.add(lsDetail);
					for (int j = 0; j < strings.length; j++) {
						if (j == 1) {
							continue;
						} else if (j >= 6) {
							break;
						}
						lsDetail.add(strings[j]);
					
					}
				} else {
					lsDetail = lsResult.get(linNum);
					lsDetail.add(strings[5]);
				}
				linNum++;
			}
			txtRead.close();
			i++;
		}
		return lsResult;
	}
	
	private ArrayList<String> getLsTitle() {
		ArrayList<String> lsTitle = new ArrayList<String>();
		lsTitle.add("Seqname"); lsTitle.add("Feature");
		lsTitle.add("Start"); lsTitle.add("End");
		lsTitle.addAll(lsPrefix);
		if (isPvalueCal) {
			lsTitle.add("Pvalue");
			colPvalue = lsTitle.size() - 1;
		}
		lsTitle.add("Ratio");
		colRatio = lsTitle.size() - 1;
		lsTitle.add("TreatMedian");
		lsTitle.add("ColMedian");
		return lsTitle;
	}
	
	/**
	 * 尾部添加4列，1:sample1 median        2:sample2 median              3:sample1/sample2            4:pvalue
	 * 指定列数，获得t检验的pvalue
	 * @param txtFile 文件
	 * @param rowStartNum 第几行开始
	 * @param colSample1 样本1是哪几列 实际列
	 * @param colSample2 样本2是哪几列 实际列
	 * @param Value 100：常规双尾T检验 200：big 300：small
	 */
	private void calculatePvalueT(ArrayList<ArrayList<String>> lsTmpResult) {
		if (colSample1.length < 2 || colSample2.length < 2) {
			isPvalueCal = false;
		}
		for (ArrayList<String> lsStrings : lsTmpResult) {
			double[] sample1 = new double[colSample1.length];
			double[] sample2 = new double[colSample2.length];
			for (int j = 0; j < sample1.length; j++) {
				sample1[j] = Double.parseDouble(lsStrings.get(colSample1[j] - 1));
			}
			for (int j = 0; j < sample2.length; j++) {
				sample2[j] = Double.parseDouble(lsStrings.get(colSample2[j] - 1));
			}
			if (lsStrings.get(2).equals("10375235")) {
				System.out.println("stop");
			}
			addPvalue(lsStrings, sample1, sample2);
			double medS1 = MathComput.median(sample1);
			double medS2 = MathComput.median(sample2);
			lsStrings.add(medS1/medS2 + "");
			lsStrings.add(medS2 + "");
			lsStrings.add(medS1 + "");
		}
	}
	
	private void addPvalue(ArrayList<String> lsTmpResult, double[] sample1, double[] sample2) {
		if (!isPvalueCal) {
			return;
		}
		double mean1 = StatUtils.mean(sample1);
		double mean2 = StatUtils.mean(sample2);
		double pvalue = 100;
		if (tTestType == BIG_TAIL && mean1 <= mean2) {
			pvalue = 0.9;
		}
		else if (tTestType == SMALL_TAIL && mean1 >= mean2) {
			pvalue = 0.9;
		} else {
			try { pvalue = TestUtils.tTest(sample1, sample2); } catch (Exception e) { }
		}
		if (tTestType != TWO_TAIL) {
			pvalue = pvalue/2;
		}
		lsTmpResult.add(pvalue + "");
	}
	
	/**
	 * 连续三根探针值大于2则认为甲基化
	 * @param lsInput
	 * @param outFile
	 * @return
	 * @throws Exception
	 */
	private ArrayList<String[]> getCombProbe(ArrayList<ArrayList<String>> lsInput) {
//		txtOut.writefileln("chrID\tMATCH_INDEX\tprobID\tstart\tstop\tscore");
		ArrayList<String[]> lsTmpOut = new ArrayList<String[]>();
		ArrayList<String[]> lsFinal = new ArrayList<String[]>();
		int lastEnd = 0;
		int i = 0;
		for (ArrayList<String> lsTmpResult : lsInput) {
			if (i == 0) {
				i++;
				continue;
			}
			if ((Double.parseDouble(lsTmpResult.get(colRatio)) > fcDown && Double.parseDouble(lsTmpResult.get(colRatio)) < fcUp ||
					(isPvalueCal && (colPvalue >= 0 && Double.parseDouble(lsTmpResult.get(colPvalue)) > pvalue))
					)//当出现不满足探针时
					||
					(int)Double.parseDouble(lsTmpResult.get(colStart)) - lastEnd > 2000//不连续探针
					||//连续探针的强度不一致，一会儿上调一会儿下调
					(Double.parseDouble(lsTmpResult.get(colRatio)) >= fcUp 
							&& lsTmpOut.size() > 0 && Double.parseDouble(lsTmpOut.get(lsTmpOut.size()-1)[colRatio]) <= fcDown)
					||
					(Double.parseDouble(lsTmpResult.get(colRatio)) <= fcDown 
							&& lsTmpOut.size() > 0 &&  Double.parseDouble(lsTmpOut.get(lsTmpOut.size()-1)[colRatio]) >= fcUp)
					) {
						if (lsTmpOut.size() >= probNum) {
							for (String[] strings : lsTmpOut) {
								lsFinal.add(strings);
							}
						}
						lsTmpOut = new ArrayList<String[]>();
					}
					
					if ((Double.parseDouble(lsTmpResult.get(colRatio)) <= fcDown || Double.parseDouble(lsTmpResult.get(colRatio)) >= fcUp)
					&&  (!isPvalueCal || (colPvalue >= 0 && Double.parseDouble(lsTmpResult.get(colPvalue)) <= pvalue))
					){
						lsTmpOut.add(lsTmpResult.toArray(new String[0]));
					}
					lastEnd = (int)Double.parseDouble(lsTmpResult.get(colStart));
		}
		return lsFinal;
	}
	/**
	 * 给定输入文件，就是挑选出的三个探针连在一起的甲基化芯片分析结果
	 * 去除冗余，将三个探针连在一起的只保留最中间的一条探针
	 * @param inFile
	 * @param outFile
	 */
	private ArrayList<String[]> copeFinal(ArrayList<String[]> lsInput) {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		//加上title
		lsResult.add(lsInput.get(0));
		ArrayList<String[]> lsTmp = new ArrayList<String[]>();
		for (int i = 1; i < lsInput.size(); i++) {
			String[] tmp = lsInput.get(i);
			//如果本探针和上一个探针只相差2000bp
			if (lsTmp.size() == 0) {
				lsTmp.add(tmp);
			}
			else if (tmp[colChrID].equals(lsTmp.get(lsTmp.size() - 1)[colChrID]) && Double.parseDouble(tmp[colStart]) - Double.parseDouble(lsTmp.get(lsTmp.size() - 1)[colEnd]) < 2000) {
				lsTmp.add(tmp);
			}
			else {
				lsResult.add(getMedProb(lsTmp));
				lsTmp.clear();
				lsTmp.add(tmp);
			}
		}
		//在结束的时候最后总结一下
		if (lsTmp.size() > 0) {
			lsResult.add(getMedProb(lsTmp));
		}
	
		return lsResult;
	}
	
	private static String[] getMedProb(ArrayList<String[]> lsProbs) {
		int i = lsProbs.size()/2;
		return lsProbs.get(i);
	}
	


}
