package com.novelbio.analysis.tools.formatConvert;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;

/**
 * 罗氏的NimbleScan的pair文件格式转化为gff文件，给MEDME读取用的
 * chromosome, probe ids, start and stop chromosomal positions, and score are expected in columns 1, 3, 4, 5 and 6 repectively.
 * @author zong0jie
 *
 */
public class Pair2Gff {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String parentFile = "/media/winE/NBC/Project/Microarray_WFL110423/王凤良 基因芯片/王凤良 基因芯片/chip result/数据图及原始数据/";

		try {
			String pairFile = parentFile  + "C_vs_N_532.pair";
			String gffFile = parentFile + "C_vs_N_532.gff";
			pair2gff(pairFile, gffFile);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			String pairFile = parentFile  + "C_vs_N_635.pair";
			String gffFile = parentFile + "C_vs_N_635.gff";
			pair2gff(pairFile, gffFile);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		String parentFile = "/media/winE/NBC/Project/Microarray_WFL110423/王凤良 基因芯片/王凤良 基因芯片/chip result/数据图及原始数据/MEDMEresultAbs/";
//
//		
//		try {
//			String gffPair = parentFile  + "C_vs_N_532.pair";
//			String gffRMA532 = parentFile + "C_vs_N_532RMS.gff";
//			String gffRMA635 = parentFile + "C_vs_N_635RMS.gff";
//			String excelResultFile = parentFile + "C_vs_N_Peak.xls";
//			String[] prix = new String[]{"532vs635","635vs532"};
//			
//			int contProbNum = 3;
//			double ratio = 2;
//			getMeDIP(gffPair, gffRMA532, gffRMA635, contProbNum, ratio, excelResultFile, prix);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	/**
	 * 罗氏的NimbleScan的pair文件格式转化为gff文件
	 * chromosome, probe ids, start and stop chromosomal positions, and score are expected in columns 1, 3, 4, 5 and 6 repectively.
	 * @param pairFile
	 * @param gffFile
	 * @throws Exception 
	 */
	public static void pair2gff(String pairFile, String gffFile) throws Exception {
		TxtReadandWrite txtPair = new TxtReadandWrite();
		txtPair.setParameter(pairFile, false, true);
		TxtReadandWrite txtGff = new TxtReadandWrite();
		txtGff.setParameter(gffFile, true, false);
		
		BufferedReader reader = txtPair.readfile();
		String content = "";
		reader.readLine(); reader.readLine();
		String ssOld = null; String ssNew = null;
		String ssOld2 = null; String ssNew2 = null; //保存chr10:97505105-97506105分割后的元素，因为甲基化的探针长度需要从下一行计算本行的长度，所以需要Old和New两个存储
		//Gff的格式，第二个MEDME不读取
		txtGff.writefile("chrID\tMATCH_INDEX\tprobID\tstart\tstop\tscore\n");
		while ((content = reader.readLine()) != null) {
			ssOld = ssNew;
			ssNew = content;
			if (ssOld == null) {
				continue;
			}
			String tmpEnd = "";
			String[] ssOldarray = ssOld.split("\t"); String[] ssNewarray = ssNew.split("\t");
			if (ssOldarray[1].equals("RANDOM") || ssOldarray[2].contains("random") )
				continue;
			
			ssOld2 = ssNew2;
			String[] ssOldarray2 = ssOldarray[2].split(":|-"); String[] ssNewarray2 = ssNewarray[2].split(":|-");//chr10:100017797-100018797
			if (ssOldarray2[1].equals(ssNewarray2[1])) {//如果是同一组探针，那么旧探针的结尾就是新探针起点-1
				tmpEnd = Long.parseLong(ssNewarray[4]) - 1 + "";
			}
			else {//如果不是同一组探针，那么旧探针的结尾就是总探针结尾
				tmpEnd = ssOldarray2[2];
			}
//			String tmpResult = ssOldarray2[0] + "\t" + ssOldarray[2] + "\t" + ssOldarray[3] + "\t" + ssOldarray[4] + "\t" + tmpEnd  +"\t"
//			+Math.log(Double.parseDouble(ssOldarray[9]))/Math.log(2) +"\n";
			String tmpResult = ssOldarray2[0] + "\t" + ssOldarray[2] + "\t" + ssOldarray[3] + "\t" + ssOldarray[4] + "\t" + tmpEnd  +"\t"+ssOldarray[9] +"\n";

			txtGff.writefile(tmpResult);
		}
		String tmpEnd = "";
		ssOld = ssNew;String[] ssOldarray = ssOld.split("\t"); String[] ssOldarray2 = ssOldarray[2].split(":|-"); 
		tmpEnd = ssOldarray2[2];
//		String tmpResult = ssOldarray2[0] + "\t" + ssOldarray[2] + "\t" + ssOldarray[3] + "\t" + ssOldarray[4] + "\t" + tmpEnd +"\t"
//		+ Math.log(Double.parseDouble(ssOldarray[9]))/Math.log(2) +"\n";
		String tmpResult = ssOldarray2[0] + "\t" + ssOldarray[2] + "\t" + ssOldarray[3] + "\t" + ssOldarray[4] + "\t" + tmpEnd +"\t"+ ssOldarray[9] +"\n";

		txtGff.writefile(tmpResult);
		txtGff.close();
		txtPair.close();
	}
	
	/**
	 * @throws Exception 
	 * @param gffPair 保存探针对应的坐标信息
	 * @param gffRMA532 不取log的甲基化芯片的RMA数值
	 * @param gffRMA635
	 * @param contProbNum 连续几根探针超过阈值，一般取3比较好
	 * @param ratio 阈值ratio，一般取2比较好
	 * @param excelResultFile excel的结果文件
	 * @param prix sting[2] 532和635的sheet名字
	 * @throws Exception
	 */
	public static void getMeDIP(String gffPair,String gffRMA532,String gffRMA635,int contProbNum, double ratio,String excelResultFile,String[] prix) throws Exception {
		ArrayList<ArrayList<String[]>> lsPeak = getMeDIP(gffPair, gffRMA532, gffRMA635, contProbNum, ratio);
		ExcelOperate excelOperate = new ExcelOperate();
		excelOperate.openExcel(excelResultFile);
		excelOperate.WriteExcel(prix[0], 1, 1, lsPeak.get(0), true);
		excelOperate.WriteExcel(prix[1], 1, 1, lsPeak.get(1), true);
	}
	
	/**
	 * @throws Exception 
	 * @param gffPair 保存探针对应的坐标信息
	 * @param gffRMA532 不取log的甲基化芯片的RMA数值
	 * @param gffRMA635
	 * @param contProbNum 连续几根探针超过阈值，一般取3比较好
	 * @param ratio 阈值ratio，一般取2比较好
	 * @throws Exception
	 * @return 两个ArrayList，第一个532，第二个635
	 */
	private static ArrayList<ArrayList<String[]>> getMeDIP(String gffPair,String gffRMA532,String gffRMA635,int contProbNum, double ratio) throws Exception {
		TxtReadandWrite txtGffPair = new TxtReadandWrite();
		txtGffPair.setParameter(gffPair, false, true);
		
		TxtReadandWrite txtGffRMA532 = new TxtReadandWrite();
		txtGffRMA532.setParameter(gffRMA532, false, true);
		
		TxtReadandWrite txtGffRMA635 = new TxtReadandWrite();
		txtGffRMA635.setParameter(gffRMA635, false, true);
		//保存探针ID到LOC的信息，为后面连续多个甲基化做准备
		//key:探针名，value:0：LOC   1：探针起点      2: 第几个探针，从1开始计算
		HashMap<String, String[]> hashProb2Loc = new HashMap<String, String[]>();
		
		BufferedReader readerGffPair = txtGffPair.readfile();
		String content = "";
		int i = 1;//计数器，计算第几个探针
		String lasLOChash = ""; //上一根探针的总坐标，计数用的，如果本探针和上一根探针的总坐标不同，计数器归1

		while ((content = readerGffPair.readLine()) != null) {
			if (content.trim().startsWith("#") || content.trim().startsWith("IMAGE_ID")) {
				continue;
			}
			String[] ss = content.split("\t");
			
			if (ss[1].equals("RANDOM") || ss[2].contains("random") )
				continue;
			
			if(!ss[2].equals(lasLOChash))//探针坐标不同
				i = 1;//计数器归1
			
			String[] tmpLOC = new String[3];
			tmpLOC[0] = ss[2];//:0：LOC
			tmpLOC[1] = ss[4];// 1：探针起点
			tmpLOC[2] = i +"";//2: 第几号探针，方便后期计数，连续三个探针认为是甲基化位点
			lasLOChash = ss[2];
			i++;
			hashProb2Loc.put(ss[3], tmpLOC);
			
		}
		BufferedReader readerGffRMA532 = txtGffRMA532.readfile();
		BufferedReader readerGffRMA635 = txtGffRMA635.readfile();
		String content532 = ""; String content635 = "";
		String content532New = ""; String content635New = "";
		String content532Old = ""; 
		int tmpContProbNum = 0; //上根探针是累计第几根探针
		String lastLoc = ""; //这批探针的LOC
		int tmpProbNum = -10; //上个探针是LOC的第几个
		double allScore532 = 0;//总共的score，用来计算多个连续探针的平均ratio
		double allScore635 = 0;//总共的score，用来计算多个连续探针的平均ratio
		double lastRatio532 = 0;//上一个532的ratio值，如果上个ratio大于2，而本ratio小于0.5，同样也不能计入统计的
		double allRatio532 = 0;//这个allRatio最后得到的平均数是ratio的平均数，不等于score之和相除，而是他们的几何平均数？？
		double lastRatio635 = 0;//上一个635的ratio值，如果上个ratio大于2，而本ratio小于0.5，同样也不能计入统计的
		double allRatio635 = 0;//这个allRatio最后得到的平均数是ratio的平均数，不等于score之和相除，而是他们的几何平均数？？
		long peakStart = 0;//Peak的起点，为第一个探针的起点
		long peakEnd = 0;//peak的终点，为最后一个探针的终点+60
		boolean flag = false; //是否需要总结上一组探针
		String[] LOCInfo = null; //本探针的具体信息 value:0：LOC   1：探针起点      2: 第几个探针，从1开始计算
		ArrayList<String[]> lsTmp532 = new ArrayList<String[]>();//结果存放
		ArrayList<String[]> lsTmp635 = new ArrayList<String[]>();
		readerGffRMA532.readLine();
		readerGffRMA635.readLine();
		while ((content532 = readerGffRMA532.readLine()) != null) {
			
			content635 = readerGffRMA635.readLine();
			content532Old = content532New; //记录上一行的信息
//			String content635Old = content635New; //记录上一行的信息
			content532New = content532;
			content635New = content635;
			
			
			String[] ss532 = content532New.split(" ");
			String[] ss635 = content635New.split(" ");
			
			LOCInfo = hashProb2Loc.get(ss532[2]);
			//本probe和上个probe属于同一组LOC，    他们的探针是紧挨着的，   他们的532和635的ratio在正负ratio之外 
			double tmpratio532 = Double.parseDouble(ss532[5])/Double.parseDouble(ss635[5]);
			double tmpratio635 = Double.parseDouble(ss635[5])/Double.parseDouble(ss532[5]);
			
			//并且上组探针没有总结过
			//开始总结上组探针的情况
			if (  flag  //需要做总结
					&&
					(  !LOCInfo[0].equals(lastLoc) 
					|| (Integer.parseInt(LOCInfo[2]) - tmpProbNum != 1) //如果本探针和上组探针不是连着的
					||(tmpratio532 < ratio && tmpratio635 < ratio)) //本探针没有超过阈值
					|| ( (lastRatio532 >= ratio && tmpratio635 >= ratio) || (lastRatio635 >= ratio && tmpratio532 >= ratio) ) //本探针超过阈值了，但是和上一个探针的阈值正好是反的
					)
			{
				//只有当连续探针数超过了设定的探针数才能开始做总结
				if (tmpContProbNum >= contProbNum) {
					double avgScore532 = allScore532/tmpContProbNum;
					double avgScore635 = allScore635/tmpContProbNum;
					double avgRatio532 = allRatio532/tmpContProbNum;
					double avgRatio635 = allRatio635/tmpContProbNum;
					peakEnd = peakEnd + 60;
					String[] tmpResult = new String[7];
					tmpResult[0] = content532Old.split(" ")[0];
					tmpResult[1] = peakStart + "";
					tmpResult[2] = peakEnd + "";
					tmpResult[3] = avgScore532 + "";
					tmpResult[4] = avgScore635 + "";
					tmpResult[6] = tmpContProbNum + ""; //连续探针数
					if (avgRatio532 >= ratio) {
						tmpResult[5] = avgRatio532 + "";
						lsTmp532.add(tmpResult);
					}
					else if (avgRatio635 >= ratio) {
						tmpResult[5] = avgRatio635 + "";
						lsTmp635.add(tmpResult);
					}
					flag = false;//已经总结过了
					//各种清零
					tmpContProbNum = 0;
					allScore532 = 0; allScore635 = 0; allRatio532 = 0; allRatio635 = 0;
				}
				tmpProbNum = -10;
				lastRatio532 = tmpratio532; lastRatio635 = tmpratio635;
			}
			
			if (  LOCInfo[0].equals(lastLoc) 
					&& (Integer.parseInt(LOCInfo[2]) - tmpProbNum == 1) //如果本探针和上组探针连着的
					&& ( (lastRatio532>=ratio && tmpratio532 >= ratio) || (lastRatio635>=ratio && tmpratio635 >= ratio) ) //本探针超过阈值了，并且和上一个探针的阈值相同
		    	)
			{
				peakEnd = Long.parseLong(LOCInfo[1]);
				allScore532 = allScore532 + Double.parseDouble(ss532[5]);
				allScore635 = allScore635 + Double.parseDouble(ss635[5]);
				allRatio532 = allRatio532 + tmpratio532;
				allRatio635 = allRatio635 + tmpratio635;
				tmpContProbNum ++;
				flag = true;
				tmpProbNum = Integer.parseInt(LOCInfo[2]);
				lastRatio532 = tmpratio532; lastRatio635 = tmpratio635;
			}
			//本组第一根超过阈值的探针
			else if (  
					(!LOCInfo[0].equals(lastLoc) 
					|| (Integer.parseInt(LOCInfo[2]) - tmpProbNum != 1) //如果本探针和上组探针不连
					||  ( (lastRatio532>=ratio && tmpratio635 >= ratio) || (lastRatio635>=ratio && tmpratio532 >= ratio))//本探针和上一个探针的ratio是反着的
					)
					&&(tmpratio532 >= ratio || tmpratio635 >= ratio)	//本探针超过阈值了
		        )
			{
				peakStart = Long.parseLong(LOCInfo[1]);
				peakEnd = Long.parseLong(LOCInfo[1]);
				allScore532 = Double.parseDouble(ss532[5]);
				allScore635 =  Double.parseDouble(ss635[5]);
				allRatio532 = allRatio532 + tmpratio532;
				allRatio635 = allRatio635 + tmpratio635;
				tmpContProbNum = 1;
				flag = true;
				tmpProbNum = Integer.parseInt(LOCInfo[2]);
				lastRatio532 = tmpratio532; lastRatio635 = tmpratio635;
				
			}
			lastLoc = LOCInfo[0];
		}
		//最后总结
		//只有当连续探针数超过了设定的探针数才能开始做总结
		if (tmpContProbNum >= contProbNum) {
			double avgScore532 = allScore532/tmpContProbNum;
			double avgScore635 = allScore635/tmpContProbNum;
			double avgRatio532 = allRatio532/tmpContProbNum;
			double avgRatio635 = allRatio635/tmpContProbNum;
			peakEnd = peakEnd + 60;
			String[] tmpResult = new String[7];
			tmpResult[0] = content532New.split(" ")[0];
			tmpResult[1] = peakStart + "";
			tmpResult[2] = peakEnd + "";
			tmpResult[3] = avgScore532 + "";
			tmpResult[4] = avgScore635 + "";
			tmpResult[6] = tmpContProbNum + ""; //连续探针数
			if (avgRatio532 >= ratio) {
				tmpResult[5] = avgRatio532 + "";
				lsTmp532.add(tmpResult);
			}
			else if (avgRatio635 >= ratio) {
				tmpResult[5] = avgRatio635 + "";
				lsTmp635.add(tmpResult);
			}
		}
		String[] title = new String[7];
		title[0] = "ChrID"; title[1] = "PeakStart"; title[2] = "PeakEnd";
		title[3] = "AvgScore532"; title[4] = "AvgScore635"; title[5] = "AvgRatio";
		title[6] = "ContinueProbNum";
		lsTmp532.add(0,title);
		lsTmp635.add(0,title);
		
		txtGffPair.close();
		txtGffRMA532.close();
		txtGffRMA635.close();
		
		ArrayList<ArrayList<String[]>> lsResult = new ArrayList<ArrayList<String[]>>();
		lsResult.add(lsTmp532);
		lsResult.add(lsTmp635);
		return lsResult;
	}
	
	
	
	
	
	
	
	
	
	
	
	
}
