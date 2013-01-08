package com.novelbio.analysis.tools;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.math.MathException;
import org.apache.commons.math.stat.StatUtils;
import org.apache.commons.math.stat.inference.TestUtils;
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
//		String parent = "/media/winF/NBC/Project/MeDIP_Array_ZW/GFF/Ams/";
//		String pathIn = parent + "ZWout.txt";
//		String pathOut = FileOperate.changeFileSuffix(pathIn, "_Ttest", null);
//		Ttest();
		
////		combAMSfile(parent, pathIn);
//		getPvalueT(pathIn, 2, new int[]{11,12,13,5,6,7}, new int[]{14,15,16,8,9,10}, 100, pathOut);
////		String out = FileOperate.changeFileSuffix(pathOut, "_Filtered", null);
//		try {
//			getConstentProb();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
//		combAMSfile("/media/winF/NBC/Project/Methy_ZGJ_20121112/GFF Files/Scaled log2-ratio Data/ams", "/media/winF/NBC/Project/Methy_ZGJ_20121112/GFF Files/Scaled log2-ratio Data/ams/amsAll");
		getConstentProb();
	}
	
	
	/**
	 * 获得连续探针
	 * @param args
	 */
	public static void getConstentProb() {
		String Gff1 = "/media/winF/NBC/Project/Methy_ZGJ_20121112/GFF Files/Scaled log2-ratio Data/ams/amsPNDvsPOSITIV";
		try {
			getCombProbe(Gff1, FileOperate.changeFileSuffix(Gff1, "_Filtered", null), "\t", 3, 4, 11, 2, 0.5, 12, 0.05, 3);
		} catch (Exception e) {
						e.printStackTrace();
		}
//		formatPath("/media/winE/NBC/Project/Methylation_PH_120110/长征医院-彭浒/GFF Files（长征医院彭浒 QQ52901159）/Scaled log2-ratio Data");
	}
	/**
	 * 对探针做t检验
	 */
	public static void Ttest() {
		String excelTxtFile = "/media/winF/NBC/Project/Methy_ZGJ_20121112/GFF Files/Scaled log2-ratio Data/ams/amsAll";
		String outFile1 = "/media/winF/NBC/Project/Methy_ZGJ_20121112/GFF Files/Scaled log2-ratio Data/ams/amsPNDvsPOSITIV";

		int[] colSample1 = new int[]{5, 6};
		int[] colSample2 = new int[]{7, 8};
		try {
			getPvalueT(excelTxtFile, 2, colSample1, colSample2, 100, outFile1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
/**
 * 将一个文件夹中所有的Gff文件整理为MEME可识别的文件
 * @param ratiogff
 * @param mirandaResultOut
 * @throws Exception
 */
	public static void formatPath(String filePath) {
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
	public static void format(String ratiogff, String outFile) throws Exception {
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
	}
	//TODO 可能会有问题，就是文件是用" "来分割
	public static void combAMSfile(String filePath, String pathOut) {
		ArrayList<String[]> lsFileName = FileOperate.getFoldFileName(filePath, "*", "gff");
		//每个文件一个hash表
		HashMap<String, String> hashPrix2Txt = new HashMap<String, String>();
		ArrayList<String> lsPrix = new ArrayList<String>();
		for (String[] fileName : lsFileName) {
			String filePathGff = filePath + "/" + fileName[0] + "." + fileName[1];
			String prix = fileName[0].split("_")[0];
			hashPrix2Txt.put(prix, filePathGff);
			lsPrix.add(prix);
		}
		String[] title = new String[4 + lsPrix.size()];
		title[0] = "seqname"; title[1] = "feature"; title[2] = "start"; title[3] = "end";
		for (int i = 0; i < lsPrix.size(); i++) {
			title[i+4] = lsPrix.get(i);
		}
//		txtOut.writefileln(title);
		
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		for (int i = 0; i < lsPrix.size(); i++) {
			String string = lsPrix.get(i);
			String file = hashPrix2Txt.get(string);
			//第一个文件需要将前几列都读入，其他的就只要读入比值即可
			if (i == 0) {
				TxtReadandWrite txtRead = new TxtReadandWrite(file, false);
				for (String info : txtRead.readlines(2)) {
					String[] strings = info.split(" ");
					String[] content = new String[title.length];
					int m = 0;
					for (int j = 0; j < strings.length; j++) {
						if (j == 1) {
							continue;
						}
						content[m] = strings[j];
						m++;
					}
					lsResult.add(content);
				}
				txtRead.close();
			}
			else {
				TxtReadandWrite txtRead = new TxtReadandWrite(file, false);
				int j = 0;
				for (String info : txtRead.readlines(2)) {
					String[] strings = info.split(" ");
					//i一定大于1，这时候从第5列开始写入文件
					lsResult.get(j)[i + 4] = strings[5];
					j++;
				}
				txtRead.close();
			}
		}
		lsResult.add(0, title);
		
		TxtReadandWrite txtOut = new TxtReadandWrite(pathOut, true);
		txtOut.ExcelWrite(lsResult);
	}
	
	/**
	 * 连续三根探针值大于2则认为甲基化
	 * @param resultGff
	 * @param outFile
	 * @param sep
	 * @param colStart 起点所在列
	 * @param colEnd 终点所在列
	 * @param colNum 第几列需要判断，实际列
	 * @param fcUp 上调阈值
	 * @param fcDown 下调阈值
	 * @param colPvalue 小于0表示不考虑colPvalue
	 * @param pvalue
	 * @param probNum 连续几根探针
	 * @throws Exception
	 */
	public static void getCombProbe(String resultGff, String outFile,String sep, int colStart, int colEnd, int colNum, double fcUp, double fcDown,int colPvalue, double pvalue, int probNum) throws Exception {
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
			(int)Double.parseDouble(ss[colStart]) - lastEnd > 2000//不连续探针
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
						txtOut.ExcelWrite(strings, true);
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
	public static void copeFinalFile(String inFile, String outFile, int colChrID, int colStart, int colEnd) {
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
		txtOut.ExcelWrite(lsResult);
	}
	
	private static String[] getMedProb(ArrayList<String[]> lsProbs)
	{
		int i = lsProbs.size()/2;
		return lsProbs.get(i);
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
	public static void getPvalueT(String txtFile, int rowStartNum, int[] colSample1, int[] colSample2, int Value, String outFile) {
		TxtReadandWrite txtInfo = new TxtReadandWrite(txtFile, false);
		TxtReadandWrite txtOut = new TxtReadandWrite(outFile, true);
		ArrayList<String> lsTitle = txtInfo.readFirstLines(rowStartNum-1);
		for (String string : lsTitle) {
			txtOut.writefileln(string);
		}
		for (String string : txtInfo.readlines(rowStartNum)) {
			String[] ss = string.split("\t");
			String[] result = ArrayOperate.copyArray(ss, ss.length + 4);
			double[] sample1 = new double[colSample1.length];
			double[] sample2 = new double[colSample2.length];
			if (string.startsWith("chrID")) {
				continue;
			}
			for (int j = 0; j < sample1.length; j++) {
				try {
					sample1[j] = Double.parseDouble(ss[colSample1[j] - 1]);
				} catch (Exception e) {
					System.out.println(j + " " + string);
				}
			}
			for (int j = 0; j < sample2.length; j++) {
				sample2[j] = Double.parseDouble(ss[colSample2[j] - 1]);
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
					e.printStackTrace();
				} catch (MathException e) {
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
			txtOut.writefileln(result);
		}
		txtOut.close();
	}
	
	
	
	
 
	
	
	
	
}






