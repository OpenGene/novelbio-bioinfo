package com.novelbio.analysis.seq.chipseq.preprocess;

import java.io.BufferedReader;

import com.novelbio.analysis.tools.formatConvert.bedFormat.Soap2Bed;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.cmd.cmdOperate2;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * mapping和peak Calling 的方法包
 * @author zong0jie
 *
 */
public class MapPeak {
	/**
	 * 
	 * 用soap进行mapping
	 * @param soapPath soap的程序路径
	 * @param inputFile1 输入测序结果，好像随便谁的都可以，不过后面的质控目前只针对solexa进行了编写
	 * @param inputFile2 双端的话，输入另一个测序文件，没有的话就输入null
	 * @param indexFile genome文件的索引
	 * @param outFile3 输出文件
	 * @param minInsert 最小插入片段，20好了
	 * @param maxInsert 最大插入片段，400好了
	 * @throws Exception
	 * @return 返回reads的总数，也就是测序量，<b>双端的话不乘以2</b>
	 */
	public static long mapSoap(String soapPath, String inputFile1,String inputFile2,String indexFile,String outFile3,int minInsert, int maxInsert) throws Exception {
		TxtReadandWrite txtInput = new TxtReadandWrite();
		txtInput.setParameter(inputFile1, false, true);
		BufferedReader readInput = txtInput.readfile();
		String content = "";
		long readsNum = 0;
		while ((content = readInput.readLine()) != null ){
				readsNum++;
		}
		//每四行一个fastQ条目
		readsNum = readsNum/4;
		String cmd = "";
		cmd = soapPath + " -a "+inputFile1;
		cmd = cmd + " -D " +indexFile; 
		cmd = cmd + " -o " +outFile3; 
		cmd = cmd +  " -r 0 -v 2 -p 7 ";
		if (inputFile2 != null && !inputFile2.trim().equals("")) {
			cmd = cmd + " -b " + inputFile2;
			cmd = cmd+ " -2 "+ outFile3+"_SEout "+" -m "+minInsert+" -x "+maxInsert;
			
		}
		System.out.println(cmd);
		CmdOperate cmdOperate = new CmdOperate(cmd);
		cmdOperate.doInBackground();
//		//双端加倍
//		if (inputFile2 != null) {
//			readsNum = readsNum*2;
//		}
		return readsNum;
	}
	/**
	 * 指定bed文件，以及需要排序的列数，产生排序结果
	 * @param bedFile
	 * @param arg
	 * @throws Exception
	 */
	public static void sortBedFile(String path,String bedFile,int chrID, String sortBedFile,int...arg) throws Exception {
		//sort -k1,1 -k2,2n -k3,3n FT5.bed > FT5sort.bed #第一列起第一列终止排序，第二列起第二列终止按数字排序,第三列起第三列终止按数字排序
		String cmd = "sort";
		if (chrID != 0) {
			cmd = cmd + " -k"+chrID+","+chrID+" ";
		}
		for (int i : arg) {
			cmd = cmd + " -k"+i+","+i+"n ";
		}
		cmd = cmd + bedFile + " > " + sortBedFile;
		
		TxtReadandWrite txtcmd = new TxtReadandWrite();
		txtcmd.setParameter(path+"/.sort.sh", true, false);
		txtcmd.writefile(cmd);
		String cmd2 = "sh "+path+"/.sort.sh";
		
		CmdOperate cmdOperate = new CmdOperate(cmd2);
		cmdOperate.doInBackground();
		FileOperate.delFile(path+"/.sort.sh");
	}
	/**
	 * 本方法内部含有ascII的质量控制
	 * 将soap转化为bed文件，只有当为pear-end的时候，并且需要将单双链分开的时候才用这个。
	 * 假设测双端45bp
	 * @param SE 单端还是双端
	 * @param soapFile 
	 * @param outPut1 输出#/1序列的坐标，一行起点45bp，一行终点45bp
	 * @param outCombFile1 输出#/1序列的合并，一行起点终点共fragment长度，用于后面画图
	 * @param outPut2 输出#/2序列的坐标，一行起点45bp，一行终点45bp，如果不区分正负链，本项为null
	 * @param outCombFile2 输出#/2序列的合并，一行起点终点共fragment长度，用于后面画图，如果不区分正负链，本项为null
	 * @param outError 输出错误信息，也就是两个 #/1或两个#/2连在一起的情况
	 * @throws Exception
	 */
	public static void copeMapSolexa(String fastQ,boolean SE, boolean sepChain, String soapFile,String outPut1, String outCombFile1, String outPut2,String outCombFile2,String outError) throws Exception {
		if (!SE && sepChain) {
			Soap2Bed.getBed2Macs(fastQ,soapFile, outPut1, outCombFile1, outPut2, outCombFile2, outError);
		}
		else if (SE && sepChain) {
			System.out.println("MapPeak.copeMapSolexa 没有相应的代码");
		}
		else {
			Soap2Bed.copeSope2Bed(fastQ,SE, soapFile, outPut1, outCombFile1, outError);
		}
	}
	
	/**
	 * 
	 * 用macs的方法进行peak calling
	 * @param bedTreat 实验
	 * @param bedCol 对照
	 * @param species 物种，用于effective genome size，有hs，mm，dm，ce，os
	 * @param outFile 目标文件夹，不用加"/"
	 * @throws Exception 
	 */
	public static void peakCalMacs(String thisPath, String bedTreat,String bedCol,String species, String outFilePath ,String prix) throws Exception 
	{
		String effge = "";
		String col = "";
		String name = "";
		if (species.equals("os")) {
			effge = " -g 2.6e8 ";
		}
		else {
			effge = " -g "+ species + " ";
		}
		if (bedCol != null && !bedCol.trim().equals("")) {
			col = " -c " + bedCol + " ";
		}
		if (prix !=null && !prix.trim().equals("")) {
			name = " -n "+prix;
		}
		String cmd = "macs14 -t "+bedTreat +col+name + effge + "-w";
		TxtReadandWrite txtCmd = new TxtReadandWrite();
		txtCmd.setParameter(outFilePath+"/macs.sh", true, false);
		txtCmd.writefile(cmd);
		CmdOperate cmdOperate = new CmdOperate("sh "+outFilePath+"/macs.sh");
		cmdOperate.doInBackground();
		FileOperate.moveFile(thisPath+"/"+prix+"_peaks.xls", outFilePath,true);
		FileOperate.moveFile(thisPath+"/"+prix+"_peaks.bed", outFilePath+"/TmpPeakInfo/",true);
		FileOperate.moveFile(thisPath+"/"+prix+"_negative_peaks.xls", outFilePath+"/TmpPeakInfo/",true);
		FileOperate.moveFile(thisPath+"/"+prix+"_model.r", outFilePath+"/TmpPeakInfo/",true);
		FileOperate.moveFile(thisPath+"/"+prix+"_diag.xls", outFilePath+"/TmpPeakInfo/",true);
		FileOperate.moveFile(thisPath+"/"+prix+"_summits.bed", outFilePath+"/TmpPeakInfo/",true);
		FileOperate.moveFolder(thisPath+"/"+prix+"_MACS_wiggle", outFilePath+"/TmpPeakInfo/",true);
		FileOperate.delFile(outFilePath+"/macs.sh");
	}
	
	
	
}
