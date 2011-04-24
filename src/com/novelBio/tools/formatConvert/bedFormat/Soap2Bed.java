package com.novelBio.tools.formatConvert.bedFormat;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;

import com.novelBio.base.dataOperate.TxtReadandWrite;


public class Soap2Bed {
	
	/**
	 * 将soap转化为bed文件，只有当为pear-end的时候，并且需要将单双链分开的时候才用这个。
	 * 假设测双端45bp
	 * @param soapFile 
	 * @param 双端测序长度
	 * @param outPut1 输出#/1序列的坐标，一行起点45bp，一行终点45bp
	 * @param outCombFile1 输出#/1序列的合并，一行起点终点共fragment长度，用于后面画图
	 * @param outPut2 输出#/2序列的坐标，一行起点45bp，一行终点45bp
	 * @param outCombFile2 输出#/2序列的合并，一行起点终点共fragment长度，用于后面画图
	 * @param outError 输出错误信息，也就是两个 #/1或两个#/2连在一起的情况
	 * @throws Exception
	 */
	public static void getBed2Macs(String soapFile,int bpLength,String outPut1, String outCombFile1, String outPut2,String outCombFile2,String outError) throws Exception {
		TxtReadandWrite txtSoap = new TxtReadandWrite();
		txtSoap.setParameter(soapFile, false, true);
		TxtReadandWrite txtOut1 = new TxtReadandWrite();
		txtOut1.setParameter(outPut1, true, false);
		TxtReadandWrite txtoutCombFile1 = new TxtReadandWrite();
		txtoutCombFile1.setParameter(outCombFile1, true, false);
		
		TxtReadandWrite txtOut2 = new TxtReadandWrite();
		txtOut2.setParameter(outPut2, true, false);
		TxtReadandWrite txtoutCombFile2 = new TxtReadandWrite();
		txtoutCombFile2.setParameter(outCombFile2, true, false);
		
		TxtReadandWrite txtOuterror = new TxtReadandWrite();
		txtOuterror.setParameter(outError, true, false);
		
 
		String content = "";
		BufferedReader readSoap = txtSoap.readfile();
		String tmpcontent=""; String tmp = "";String tmpPrespre = "";
		String[] tmpresPre =null;
		while ((content = readSoap.readLine()) != null) {
			String[] ss = content.split("\t");
			//soap文件的格式是 chrID 坐标 无论mapping到正负链，该坐标都是起点，都是要向后加上bpLength-1的，
			String tmpres = ss[7] + "\t"+ ss[8] +"\t"+ (Long.parseLong(ss[8])+bpLength-1)+"\t"+ ss[3]+"\t"+ss[9]+"\t"+ss[6];
			//tmpPrespre only save content while ss[0].split("#/")[1].equals("1")
			//只需要判断#/1即可，如果#/1为正，则mapping到正链上，否则mapping到负链上
			if (ss[0].split("#/")[1].equals("1")) {
				tmpcontent = content;
				tmpresPre = ss;
				tmpPrespre = tmpres;
				continue;
			}
			//只有当#/1和#/2的方向相反才是正确的测序结果。因为solexa测序的结果就是一正一负
			if ((ss[0].split("#/")[1].equals("2")&&ss[6].equals("-"))
					&& tmpresPre[0].split("#/")[1].equals("1")&&tmpresPre[6].equals("+")
			)
			{
				txtOut1.writefile(tmpPrespre+"\n"+tmpres+"\n");
				long startLoc = 0; long endLoc = 0; long tmpPre = Long.parseLong(tmpresPre[8]); long tmpSS = Long.parseLong(ss[8]);  
				if (tmpPre<tmpSS) {
					startLoc = tmpPre; endLoc = tmpSS + bpLength-1;
				}
				else {
					startLoc = tmpSS; endLoc = tmpPre + bpLength-1;
				}
				txtoutCombFile1.writefile(ss[7]+"\t"+startLoc+"\t"+endLoc+"\n");
			}
			else if ((ss[0].split("#/")[1].equals("2")&&ss[6].equals("+"))
					&& tmpresPre[0].split("#/")[1].equals("1")&&tmpresPre[6].equals("-")
			)
			{
				txtOut2.writefile(tmpPrespre+"\n"+tmpres+"\n");
				long startLoc = 0; long endLoc = 0; long tmpPre = Long.parseLong(tmpresPre[8]); long tmpSS = Long.parseLong(ss[8]);  
				if (tmpPre<tmpSS) {
					startLoc = tmpPre; endLoc = tmpSS + bpLength-1;
				}
				else {
					startLoc = tmpSS; endLoc = tmpPre + bpLength-1;
				}
				txtoutCombFile2.writefile(ss[7]+"\t"+startLoc+"\t"+endLoc+"\n");
			}
			else {
				txtOuterror.writefile(tmpcontent+"\n"+content+"\n");
			}
		}
	}
	/**
	 * 将soap转化为bed文件，用于SE，不区分正负链时候做
	 * @param soapFile
	 * @param bpLength 测序长度
	 * @param outPut1 macs的文件，仅仅是reads的结果
	 * @param outLong 用于后期分析的mapping文件，这个将reads向3‘端延生至350bp
	 * @throws Exception
	 */
	private static void getBed2MacsSE(String soapFile,int bpLength,String outPut1,String outLong) throws Exception {
		TxtReadandWrite txtSoap = new TxtReadandWrite();
		txtSoap.setParameter(soapFile, false, true);
		TxtReadandWrite txtOut1 = new TxtReadandWrite();
		txtOut1.setParameter(outPut1, true, false);
		TxtReadandWrite txtOutLong = new TxtReadandWrite();
		txtOutLong.setParameter(outLong, true, false);
		
		String content = "";
		BufferedReader readSoap = txtSoap.readfile();
		while ((content = readSoap.readLine()) != null) {
			String[] ss = content.split("\t");
			String tmpres = ss[7] + "\t"+ ss[8] +"\t"+ (Long.parseLong(ss[8])+bpLength-1)+"\t"+ ss[3]+"\t"+ss[9]+"\t"+ss[6];
			txtOut1.writefile(tmpres+"\n");
			if (ss[6].equals("+")) {
				String tmpres2 = ss[7] + "\t"+ ss[8] +"\t"+ (Long.parseLong(ss[8])+349)+"\t"+ ss[3]+"\t"+ss[9]+"\t"+ss[6];
				txtOutLong.writefile(tmpres2+"\n");
			}
			else {
				String tmpres2 = ss[7] + "\t"+ (Long.parseLong(ss[8])+bpLength-350) +"\t"+(Long.parseLong(ss[8])+bpLength-1) +"\t"+ ss[3]+"\t"+ss[9]+"\t"+ss[6];
				txtOutLong.writefile(tmpres2+"\n");
			}
		}
	}
	
	/**
	 * 将soap转化为bed文件，用于PE，不区分正负链时候做
	 * @param soapFile
	 * @param bpLength 测序长度
	 * @param outPut1 用于后期分析的mapping文件，这个将双端长度合并
	 * @param outCombine 
	 * @throws Exception
	 */
	private static void getBed2MacsPE(String soapFile,int bpLength,String outPut1,String outCombine,String outError) throws Exception {
		TxtReadandWrite txtSoap = new TxtReadandWrite();
		txtSoap.setParameter(soapFile, false, true);
		TxtReadandWrite txtOut1 = new TxtReadandWrite();
		txtOut1.setParameter(outPut1, true, false);
		TxtReadandWrite txtOutComb = new TxtReadandWrite();
		txtOutComb.setParameter(outCombine, true, false);
		
		TxtReadandWrite txtOuterror = new TxtReadandWrite();
		txtOuterror.setParameter(outError, true, false);
		
		String content = "";
		BufferedReader readSoap = txtSoap.readfile();
		String tmpcontent=""; String tmp = "";String tmpPrespre = "";
		String[] tmpresPre =null;
		while ((content = readSoap.readLine()) != null) {

			String[] ss = content.split("\t");
			//soap文件的格式是 chrID 坐标 无论mapping到正负链，该坐标都是起点，都是要向后加上bpLength-1的，
			String tmpres = ss[7] + "\t"+ ss[8] +"\t"+ (Long.parseLong(ss[8])+bpLength-1)+"\t"+ ss[3]+"\t"+ss[9]+"\t"+ss[6];
			//tmpPrespre only save content while ss[0].split("#/")[1].equals("1")
			//只需要判断#/1即可，如果#/1为正，则mapping到正链上，否则mapping到负链上
			if (ss[0].split("#/")[1].equals("1")) {
				tmpcontent = content;
				tmpresPre = ss;
				tmpPrespre = tmpres;
				continue;
			}
			//只有当#/1和#/2的方向相反才是正确的测序结果。因为solexa测序的结果就是一正一负
			if ((ss[0].split("#/")[1].equals("2")&&ss[6].equals("-"))
					&& tmpresPre[0].split("#/")[1].equals("1")&&tmpresPre[6].equals("+")
			)
			{
				txtOut1.writefile(tmpPrespre+"\n"+tmpres+"\n");
				long startLoc = 0; long endLoc = 0; long tmpPre = Long.parseLong(tmpresPre[8]); long tmpSS = Long.parseLong(ss[8]);  
				if (tmpPre<tmpSS) {
					startLoc = tmpPre; endLoc = tmpSS + bpLength-1;
				}
				else {
					startLoc = tmpSS; endLoc = tmpPre + bpLength-1;
				}
				txtOutComb.writefile(ss[7]+"\t"+startLoc+"\t"+endLoc+"\n");
			}
			else if ((ss[0].split("#/")[1].equals("2")&&ss[6].equals("+"))
					&& tmpresPre[0].split("#/")[1].equals("1")&&tmpresPre[6].equals("-")
			)
			{
				txtOut1.writefile(tmpPrespre+"\n"+tmpres+"\n");
				long startLoc = 0; long endLoc = 0; long tmpPre = Long.parseLong(tmpresPre[8]); long tmpSS = Long.parseLong(ss[8]);  
				if (tmpPre<tmpSS) {
					startLoc = tmpPre; endLoc = tmpSS + bpLength-1;
				}
				else {
					startLoc = tmpSS; endLoc = tmpPre + bpLength-1;
				}
				txtOutComb.writefile(ss[7]+"\t"+startLoc+"\t"+endLoc+"\n");
			}
			else {
				txtOuterror.writefile(tmpcontent+"\n"+content+"\n");
			}
		}
	}
	
	/**
	 * 测试一下
	 * 计算测序结果的coverage情况，以及reads在每条染色体上的分布情况，结果必须是排过序的
	 * 并且为bed文件，格式必须如下:<br>
	 * chrID \t loc1 \t loc2 \n<br>
	 * 并且loc1 < loc2<br>
	 * @param bedFile 必须是排过序的bed文件，格式必须如下:<br>
	 * chrID \t loc1 \t loc2 \n<br>
	 * 并且loc1 < loc2<br>
	 * @param chrLength 染色体长度的文件，计算得到的
	 * @param calFragLen 是否计算Fragment的长度分布
	 * @param 当calFragLen为true时才会生成，是fragment的分布文件，用R读取
	 */
	private static ArrayList<String> calCover(String bedFile,String chrLength,boolean calFragLen, String FragmentFile) throws Exception {
		TxtReadandWrite txtbed = new TxtReadandWrite();
		txtbed.setParameter(bedFile, false, true);
		TxtReadandWrite txtFragmentFile = new TxtReadandWrite();
		txtFragmentFile.setParameter(FragmentFile, false, true);
		
		HashMap<String, Integer> hashChrReadsNum = new HashMap<String, Integer>();
		int totalMappedReads = 0;
		int chrMappedReads = 0;
		int tmpLocStart = 0; int tmpLocEnd = 0;//用来计算coverage
		String content = ""; String chrID = "";
		BufferedReader readBed = txtbed.readfile();
		long coverage = 0;
		while ((content = readBed.readLine()) != null) {
			String[] ss = content.split("\t");
			int Locstart = Integer.parseInt(ss[1]); int Locend = Integer.parseInt(ss[2]); 
			
			
			if (!ss[0].trim().equals(chrID)) {
				chrID = ss[0].trim();
				chrMappedReads = 0;
				if (chrID.equals("")) {//跳过最开始的chrID
					continue;
				}
				hashChrReadsNum.put(chrID, chrMappedReads);
				tmpLocStart = 0; tmpLocEnd = 0;//用来计算coverage
			}
			totalMappedReads ++; chrMappedReads++;
			if (calFragLen) //如果要计算fragment的分布，那么就将fragment的长度记录在txt文本中，最后调用R来计算长度分布
			{
				txtFragmentFile.writefile(Locend - Locstart+""+"\n");
			}
			if (Locend <= tmpLocEnd) {
				continue;
			}
			else if (Locstart <= tmpLocEnd && Locend> tmpLocEnd ) {
				coverage = coverage + Locend - tmpLocEnd;
				tmpLocEnd = Locend;
			}
			else if (Locstart > tmpLocEnd ) {
				coverage = coverage + Locend - Locstart + 1;
				tmpLocEnd = Locend;
			}
		}
	}
	
	
	
	/**
	 * 梯度提取序列Gradient
	 * @param inFile
	 * @param percent �?��?��?�?�?0�?0�?0...100
	 * @param outFile
	 * @throws Exception
	 */
	public static void getGradTxt(String inFile,int[] percent,String outFile) throws Exception {
		TxtReadandWrite txtIn = new TxtReadandWrite();
		txtIn.setParameter(inFile, false, true);
		//�??大�??��???00以�??
		for (int i = 0; i < percent.length; i++) {
			if (percent[i]>100) {
				percent[i] = 100;
			}
		}
		ArrayList<TxtReadandWrite> lstxtWrite = new ArrayList<TxtReadandWrite>();
		for (int i = 0; i < percent.length; i++) {
			TxtReadandWrite txtWrite = new TxtReadandWrite();
			txtWrite.setParameter(outFile+percent[i], true, false);
			lstxtWrite.add(txtWrite);
		}
		int rowAllNum = txtIn.ExcelRows();
		BufferedReader reader = txtIn.readfile();
		String content = "";
		int rowNum = 0;
		while ((content = reader.readLine()) != null) {
			for (int i = 0; i < percent.length; i++) {
				 int tmpNum =percent[i]*rowAllNum;
				if (rowNum<tmpNum/100) {
					lstxtWrite.get(i).writefile(content+"\n");
				}
			}
			rowNum++;
		}
		for (TxtReadandWrite txtReadandWrite : lstxtWrite) {
			txtReadandWrite.close();
		}
		txtIn.close();
	}
	
	
	
}
