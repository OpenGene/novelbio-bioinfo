package com.novelbio.analysis.tools;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashSet;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.math.MathException;
import org.apache.commons.math.stat.StatUtils;
import org.apache.commons.math.stat.inference.TestUtils;
import org.jfree.xml.factory.objects.ArrayObjectDescription;

import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.fileOperate.FileOperate;
/**
 * 解析罗氏的甲基化芯片Gff文件
 * @author zong0jie
 *
 */
public class MeDIParrayGFF {

	public static void main(String[] args) {
//		Ttest();
//		getConstentProb();
		String inFile = "/media/winE/NBC/Project/Methylation_PH_120110/长征医院-彭浒/GFF Files（长征医院彭浒 QQ52901159）/Scaled log2-ratio Data/Ams/out456vs23789_Filtered.txt";
		String outFile = FileOperate.changeFileSuffix(inFile, "_OneProbe", "txt");
		copeFinalFile(inFile, outFile, 1, 3, 4);
	}
	
	
	/**
	 * @param args
	 */
	public static void getConstentProb() {
		String Gff1 = "/media/winE/NBC/Project/Methylation_PH_120110/长征医院-彭浒/GFF Files（长征医院彭浒 QQ52901159）/Scaled log2-ratio Data/Ams/out8vs9.txt";
		
		try {
			getCombProbe(Gff1, FileOperate.changeFileSuffix(Gff1, "_Filtered", null), "\t", 3, 4, 14, 2, 0.5, -1, 0.05, 3);
			
		} catch (Exception e) {
						e.printStackTrace();
		}
		
		
//		formatPath("/media/winE/NBC/Project/Methylation_PH_120110/长征医院-彭浒/GFF Files（长征医院彭浒 QQ52901159）/Scaled log2-ratio Data");
	}
	
	public static void Ttest() {
		String excelTxtFile = "/media/winE/NBC/Project/Methylation_PH_120110/长征医院-彭浒/GFF Files（长征医院彭浒 QQ52901159）/Scaled log2-ratio Data/Ams/Sample_All_635_ratio_coped.gff.gff";
		String outFile1 = "/media/winE/NBC/Project/Methylation_PH_120110/长征医院-彭浒/GFF Files（长征医院彭浒 QQ52901159）/Scaled log2-ratio Data/Ams/out456vs23789.txt";
		String outFile2 = "/media/winE/NBC/Project/Methylation_PH_120110/长征医院-彭浒/GFF Files（长征医院彭浒 QQ52901159）/Scaled log2-ratio Data/Ams/out89vs234567.txt";

		int[] colSample1 = new int[]{10, 9, 8};
		int[] colSample2 = new int[]{5, 6, 7, 11, 12};
		getPvalueT(excelTxtFile, 2, colSample1, colSample2, 100, outFile1);
		
		colSample1 = new int[]{5, 6};
		colSample2 = new int[]{7, 8, 9, 10, 11, 12};
		getPvalueT(excelTxtFile, 2, colSample1, colSample2, 100, outFile2);
		
	}
	

	


/**
 * 将一个文件夹中所有的Gff文件整理为MEME可识别的文件
 * @param ratiogff
 * @param outFile
 * @throws Exception
 */
	public static void formatPath(String filePath)
	{
		ArrayList<String[]> lsFile = FileOperate.getFoldFileName(filePath, "*", "gff");
		for (String[] strings : lsFile) {
			String fileName = FileOperate.addSep(filePath) + strings[0] + "." + strings[1];
			String fileOut = FileOperate.changeFileSuffix(fileName, "_coped", null);
			try {
				format(fileName, fileOut);
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
	public static void format(String ratiogff, String outFile) throws Exception
	{
		TxtReadandWrite txtGff = new TxtReadandWrite(ratiogff, false);
		TxtReadandWrite txtOut = new TxtReadandWrite(outFile, true);
		BufferedReader reader = txtGff.readfile();
		String content = "";
		txtOut.writefileln("chrID\tMATCH_INDEX\tprobID\tstart\tstop\tscore");
		HashSet<String> hashUniq = new HashSet<String>();//去冗余用
		while ((content = reader.readLine()) != null) {
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
	}
	/**
	 * 
	 * 连续三根探针值大于2则认为甲基化
	 * @param resultGff
	 * @param outFile
	 * @param sep
	 * @param colStart 起点所在列
	 * @param colEnd 终点所在列
	 * @param colNum 第几行需要判断，实际列
	 * @param fcUp 上调阈值
	 * @param fcDown 下调阈值
	 * @param colPvalue 小于0表示不考虑colPvalue
	 * @param pvalue
	 * @param probNum 连续几根探针
	 * @throws Exception
	 */
	public static void getCombProbe(String resultGff, String outFile,String sep, int colStart, int colEnd, int colNum, double fcUp, double fcDown,int colPvalue, double pvalue, int probNum) throws Exception
	{
		colNum--;colStart--;colEnd--; colPvalue--;
		TxtReadandWrite txtGff = new TxtReadandWrite(resultGff, false);
		TxtReadandWrite txtOut = new TxtReadandWrite(outFile, true);
		BufferedReader reader = txtGff.readfile();
		String content = "";reader.readLine();//跳过第一行
		txtOut.writefileln("chrID\tMATCH_INDEX\tprobID\tstart\tstop\tscore");
		ArrayList<String[]> lsTmpResult = new ArrayList<String[]>(); //用来存储基因的信息
		int lastEnd = 0;
		while ((content = reader.readLine()) != null) {
			if (content.startsWith("#")) {
				continue;
			}
			String[] ss = content.split("\t");
			
			if ((Double.parseDouble(ss[colNum]) > fcDown && Double.parseDouble(ss[colNum]) < fcUp &&
			(colPvalue < 0 || (colPvalue >= 0 && Double.parseDouble(ss[colPvalue]) <= pvalue))
			)//当出现不满足探针时
			||		
			Integer.parseInt(ss[colStart]) - lastEnd > 2000 //不连续探针
			||//连续探针的强度不一致，一会儿上调一会儿下调
			(lsTmpResult.size() > 0 && Double.parseDouble(ss[colNum]) >= fcUp 
					&& Double.parseDouble(lsTmpResult.get(lsTmpResult.size()-1)[colNum]) <= fcDown 
					&& (colPvalue < 0 || (colPvalue >= 0 && Double.parseDouble(ss[colPvalue]) <= pvalue)))
			||
			(lsTmpResult.size() > 0 && Double.parseDouble(ss[colNum]) <= fcDown 
					&& Double.parseDouble(lsTmpResult.get(lsTmpResult.size()-1)[colNum]) >= fcUp 
					&& (colPvalue < 0 || (colPvalue >= 0 && Double.parseDouble(ss[colPvalue]) <= pvalue)))
			) {
				if (lsTmpResult.size() >= probNum) {
					for (String[] strings : lsTmpResult) {
						txtOut.ExcelWrite(strings, true, "\t");
					}
				}
				lsTmpResult = new ArrayList<String[]>();
			}
			
			if ((Double.parseDouble(ss[colNum]) <= fcDown || Double.parseDouble(ss[colNum]) >= fcUp)
			&&  (colPvalue < 0 || (colPvalue >= 0 && Double.parseDouble(ss[colPvalue]) <= pvalue))
			){
				lsTmpResult.add(ss);
			}
			lastEnd = (int)Double.parseDouble(ss[colStart]);
		}
		txtGff.close();
		txtOut.close();
	}
	/**
	 * 给定输入文件，就是挑选出的三个探针连在一起的甲基化芯片分析结果
	 * 去除冗余，将三个探针连在一起的只保留最中间的一条探针
	 * @param inFile
	 * @param outFile
	 */
	public static void copeFinalFile(String inFile, String outFile, int colChrID, int colStart, int colEnd)
	{
		colChrID--; colStart--; colEnd--;
		ArrayList<String[]> lsInfo = ExcelTxtRead.readLsExcelTxt(inFile, 1);
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		//加上title
		lsResult.add(lsInfo.get(0));
		ArrayList<String[]> lsTmp = new ArrayList<String[]>();
		for (int i = 1; i < lsInfo.size(); i++) {
			String[] tmp = lsInfo.get(i);
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
		lsResult.add(getMedProb(lsTmp));
		TxtReadandWrite txtOut = new TxtReadandWrite(outFile, true);
		txtOut.ExcelWrite(lsResult, "\t", 1, 1);
	}
	
	private static String[] getMedProb(ArrayList<String[]> lsProbs)
	{
		int i = lsProbs.size()/2;
		return lsProbs.get(i);
	}
	
	/**
	 * 尾部添加4列，1:sample1 median        2:sample2 median              3:sample1/sample2            4:pvalue
	 * 指定列数，获得t检验的pvalue
	 * @param excelTxtFile 文件
	 * @param colSample1 样本1是哪几列
	 * @param colSample2 样本2是哪几列
	 * @param Value 100：常规双尾T检验 200：big 300：small
	 */
	public static void getPvalueT(String excelTxtFile, int rowStartNum, int[] colSample1, int[] colSample2, int Value, String outFile)
	{
		rowStartNum--;
		ArrayList<String[]> lsInfo = ExcelTxtRead.readLsExcelTxtFile(excelTxtFile, 1, 1, -1, -1);
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		for (int i = 0; i < rowStartNum; i++) {
			lsResult.add(lsInfo.get(i));
		}
		for (int i = rowStartNum; i < lsInfo.size(); i++) {
			String[] result = ArrayOperate.copyArray(lsInfo.get(i), lsInfo.get(i).length + 4);
			double[] sample1 = new double[colSample1.length];
			double[] sample2 = new double[colSample2.length];
			for (int j = 0; j < sample1.length; j++) {
				sample1[j] = Double.parseDouble(lsInfo.get(i)[colSample1[j] - 1]);
			}
			for (int j = 0; j < sample2.length; j++) {
				sample2[j] = Double.parseDouble(lsInfo.get(i)[colSample2[j] - 1]);
			}
			double mean1 = StatUtils.mean(sample1);
			double mean2 = StatUtils.mean(sample2);
			double pvalue = 100;
			if (Value == 200 && mean1 <= mean2) {
				pvalue = 0.9;
			}
			else if (Value == 300 && mean1 >= mean2) {
				pvalue = 0.9;
			}
			else {
				try {
					pvalue = TestUtils.tTest(sample1, sample2);
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (MathException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			if (Value != 100) {
				pvalue = pvalue/2;
			}
			double medS1 = MathComput.median(sample1);
			double medS2 = MathComput.median(sample2);
			result[result.length - 1] = pvalue + "";
			result[result.length - 2] = medS1/medS2 + "";
			result[result.length - 3] = medS2 + "";
			result[result.length - 4] = medS1 + "";
			lsResult.add(result);
		}
		TxtReadandWrite txtOut = new TxtReadandWrite(outFile, true);
		txtOut.ExcelWrite(lsResult, "\t", 1, 1);
	}
	
	
	
	
 
	
	
	
	
}






