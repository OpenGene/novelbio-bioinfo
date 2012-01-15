package com.novelbio.analysis.tools.arrayTools;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashSet;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.fileOperate.FileOperate;
/**
 * 解析罗氏的甲基化芯片Gff文件
 * @author zong0jie
 *
 */
public class MeDIParrayGFF {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String parentPath = "/media/winE/NBC/Project/Methylation_PH_120110/长征医院-彭浒/GFF Files（长征医院彭浒 QQ52901159）/Scaled log2-ratio Data/Ams/";
//		String ratiogff = parentPath + "DJY_635_ratio.gff";
//		String outFile = parentPath + FileOperate.getFileNameSep(ratiogff)[0]+"_out."+ FileOperate.getFileNameSep(ratiogff)[1];
		String resultGff = parentPath + "Sample1_635_ratio_coped.gff.gff";
		String outFile = parentPath + "Sample1_635_ratio_coped_final.gff";
		try {
//			format(ratiogff, outFile);
			getCombProbe(resultGff, outFile, "\t", 3, 4, 8, 2, 0.5, 3);
			
			
		} catch (Exception e) {
						e.printStackTrace();
		}
		
		
		formatPath("/media/winE/NBC/Project/Methylation_PH_120110/长征医院-彭浒/GFF Files（长征医院彭浒 QQ52901159）/Scaled log2-ratio Data");
	}

	
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
	 * 连续三根探针值大于2则认为甲基化
	 * @param resultGff
	 * @param outFile
	 * @param sep
	 * @param colStart 起点所在列
	 * @param colEnd 终点所在列
	 * @param colNum 第几行需要判断，实际列
	 * @param fcUp 上调阈值
	 * @param fcDown 下调阈值
	 * @param probNum 连续几根探针
	 * @throws Exception
	 */
	public static void getCombProbe(String resultGff, String outFile,String sep, int colStart, int colEnd, int colNum, double fcUp, double fcDown, int probNum) throws Exception
	{
		colNum--;colStart--;colEnd--;
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
			
			if ((Double.parseDouble(ss[colNum]) > fcDown && Double.parseDouble(ss[colNum]) < fcUp)//当出现不满足探针时
			||		
			Integer.parseInt(ss[colStart]) - lastEnd > 2000 //不连续探针
			||//连续探针的强度不一致，一会儿上调一会儿下调
			(lsTmpResult.size() > 0 && Double.parseDouble(ss[colNum]) >= fcUp && Double.parseDouble(lsTmpResult.get(lsTmpResult.size()-1)[colNum]) <= fcDown)
			||
			(lsTmpResult.size() > 0 && Double.parseDouble(ss[colNum]) <= fcDown && Double.parseDouble(lsTmpResult.get(lsTmpResult.size()-1)[colNum]) >= fcUp)
					
			) {
				if (lsTmpResult.size() >= probNum) {
					for (String[] strings : lsTmpResult) {
						txtOut.ExcelWrite(strings, true, "\t");
					}
				}
				lsTmpResult = new ArrayList<String[]>();
			}
			
			if (Double.parseDouble(ss[colNum]) <= fcDown || Double.parseDouble(ss[colNum]) >= fcUp) {
				lsTmpResult.add(ss);
			}
			lastEnd = (int)Double.parseDouble(ss[colStart]);
		}
		txtGff.close();
		txtOut.close();
	}
	
	
	
	
	
	
	
	
	
	
	
//	/**
//	 * 
//	 * 处理已有的探针情况
//	 * @param lsTmpResult
//	 * @param colStart 从0开始计数
//	 * @param colEnd 从0开始计数
//	 * @param colNum 从0开始计数
//	 * @param probNum
//	 */
//	private void copeProbeInfo(ArrayList<String[]> lsTmpResult,  int colStart, int colEnd,int colNum, int probNum) {
//		String[] tmpResult = new String[3];
//		if (lsTmpResult.size() < probNum) {
//			return null;
//		}
//		tmpResult[0] = lsTmpResult.get(0)[colStart];
//		tmpResult[1] =  lsTmpResult.get(lsTmpResult.size() - 1)[colEnd];
//		//计算中位数
//		double[] tmpValue = new double[lsTmpResult.size()];
//		for (int i = 0; i < tmpValue.length; i++) {
//			tmpValue[i] = Double.parseDouble(lsTmpResult.get(i)[colNum]);
//		}
//		tmpResult[2] = MathComput.median(tmpValue) + "";
//	}
//	
	
	
	
	
}






