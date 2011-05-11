package com.novelbio.analysis.tools.formatConvert.bedFormat;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.analysis.tools.formatConvert.FastQ;
import com.novelbio.base.dataOperate.TxtReadandWrite;

 /**
 * 本方法内部含有ascII的质量控制
 * @author zong0jie
 */
public class Soap2Bed {

/**
	 * 将soap转化为bed文件，只有当为pear-end的时候，并且需要将单双链分开的时候才用这个。
	 * 本方法内部含有ascII的质量控制
	 * 假设测双端45bp
	 * @param soapFile 
	 * @param outPut1 输出#/1序列的坐标，一行起点45bp，一行终点45bp
	 * @param outCombFile1 输出#/1序列的合并，一行起点终点共fragment长度，用于后面画图
	 * @param outPut2 输出#/2序列的坐标，一行起点45bp，一行终点45bp
	 * @param outCombFile2 输出#/2序列的合并，一行起点终点共fragment长度，用于后面画图
	 * @param outError 输出错误信息，也就是两个 #/1或两个#/2连在一起的情况
	 * @throws Exception
	 */
	public static void getBed2Macs(String fastQ,String soapFile,String outPut1, String outCombFile1, String outPut2,String outCombFile2,String outError) throws Exception {
		if (fastQ.trim().toLowerCase().equals("sanger")) {
			fastQ = NovelBioConst.FASTQ_SANGER;
		}
		else if (fastQ.trim().toLowerCase().equals("illumina")) {
			fastQ = NovelBioConst.FASTQ_ILLUMINA;
		}
		FastQ.setFastQoffset(fastQ);
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
		//获得测序长度
		String[] string = txtSoap.readFirstLines(1).get(0).split("\t");
		int bpLength = string[1].trim().length();
               //soap文件的格式是 chrID 坐标 无论mapping到正负链，该坐标都是起点，都是要向后加上bpLength-1的，
		String content = "";
		BufferedReader readSoap = txtSoap.readfile();
		String tmpcontent=""; String tmp = "";String tmpPrespre = "";
		String[] tmpresPre =null;
		while ((content = readSoap.readLine()) != null) {
			if (content.trim().equals("")) {
				continue;
			}
			String[] ss = content.split("\t");
			//soap文件的格式是 chrID 坐标 无论mapping到正负链，该坐标都是起点，都是要向后加上bpLength-1的，
			String tmpres = ss[7] + "\t"+ ss[8] +"\t"+ (Long.parseLong(ss[8])+bpLength-1)+"\t"+ ss[3]+"\t"+ss[9]+"\t"+ss[6];
			//tmpPrespre only save content while ss[0].split("#/")[1].equals("1")
			//只需要判断#/1即可，如果#/1为正，则mapping到正链上，否则mapping到负链上
			if (ss[0].trim().endsWith("1")) {
				tmpcontent = content;
				tmpresPre = ss;
				tmpPrespre = tmpres;
				continue;
			}
			//只有当#/1和#/2的方向相反才是正确的测序结果。因为solexa测序的结果就是一正一负		
			if ((ss[0].trim().endsWith("2")&&ss[6].equals("-"))
					&& tmpresPre[0].trim().endsWith("1")&&tmpresPre[6].equals("+")
			)
			{
				////////////////////////////////////////////////序列质量////////////////////////////////////////////////
				int[] bpQ1 = FastQ.copeFastQ(ss[2], 10,13);
				int[] bpQ2 = FastQ.copeFastQ(tmpresPre[2], 10,13);
				if ((bpQ1[0] > bpLength/10 || bpQ1[1] > bpLength/5) && (bpQ2[0] > bpLength/10 || bpQ2[1] > bpLength/5)  ) {
					continue;
				}
////////////////////////////////////////////////////////////////////////////////////////////////
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
			else if ((ss[0].trim().endsWith("2")&&ss[6].equals("+"))
					&& tmpresPre[0].trim().endsWith("1")&&tmpresPre[6].equals("-")
			)
			{
				/////////////////////////////////////////////////序列质量////////////////////////////////////////////////
				int[] bpQ1 = FastQ.copeFastQ(ss[2], 10,13);
				int[] bpQ2 = FastQ.copeFastQ(tmpresPre[2], 10,13);
				if ((bpQ1[0] > bpLength/10 || bpQ1[1] > bpLength/5) && (bpQ2[0] > bpLength/10 || bpQ2[1] > bpLength/5)  ) {
					continue;
				}
////////////////////////////////////////////////////////////////////////////////////////////////
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
		txtOut1.close();
		txtOut2.close();
		txtoutCombFile1.close();
		txtoutCombFile2.close();
		txtOuterror.close();
		txtSoap.close();
		
	}
	
	

	/**
	* 将soap转化为bed文件，用于SE，不区分正负链时候做
	* 本方法内部含有ascII的质量控制
	* @param SE true: 单端   false: 双端
	 * @param soapFile 
	 * @param bpLength 测序长度
	 * @param outPut macs的文件，仅仅是reads的结果
	 * @param outComb 用于后期分析的mapping文件，单端将reads向3‘端延生至350bp，双端则是双端长度合并
	 * @param error 双端才有的，单端随便设
	 * @throws Exception 
	 */
	public static void copeSope2Bed(String fastQ,boolean SE, String soapFile,String outPut,String outComb,String error) throws Exception {
		if (fastQ.trim().toLowerCase().equals("sanger")) {
			fastQ = NovelBioConst.FASTQ_SANGER;
		}
		else if (fastQ.trim().toLowerCase().equals("illumina")) {
			fastQ = NovelBioConst.FASTQ_ILLUMINA;
		}
		FastQ.setFastQoffset(fastQ);
		if (SE) {
			getBed2MacsSE(soapFile, outPut, outComb);
		}
		else {
			getBed2MacsPE(soapFile, outPut, outComb, error);
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
	private static void getBed2MacsSE(String soapFile,String outPut1,String outLong) throws Exception {
		TxtReadandWrite txtSoap = new TxtReadandWrite();
		txtSoap.setParameter(soapFile, false, true);
		TxtReadandWrite txtOut1 = new TxtReadandWrite();
		txtOut1.setParameter(outPut1, true, false);
		TxtReadandWrite txtOutLong = new TxtReadandWrite();
		txtOutLong.setParameter(outLong, true, false);
		
		String[] string = txtSoap.readFirstLines(1).get(0).split("\t");
		int bpLength = string[1].trim().length();
		
		
		String content = "";
		BufferedReader readSoap = txtSoap.readfile();
		while ((content = readSoap.readLine()) != null) {
			if (content.trim().equals("")) {
				continue;
			}
			String[] ss = content.split("\t");
			String tmpres = ss[7] + "\t"+ ss[8] +"\t"+ (Long.parseLong(ss[8])+bpLength-1)+"\t"+ ss[3]+"\t"+ss[9]+"\t"+ss[6];
			txtOut1.writefile(tmpres+"\n");
			if (ss[6].equals("+")) {
//////////////////////////////////////////////////序列质量////////////////////////////////////////////////
				int[] bpQ1 = FastQ.copeFastQ(ss[2], 10,13);
				if (bpQ1[0] > bpLength/10 || bpQ1[1] > bpLength/5  ) {
					continue;
				}
////////////////////////////////////////////////////////////////////////////////////////////////			
				String tmpres2 = ss[7] + "\t"+ ss[8] +"\t"+ (Long.parseLong(ss[8])+349)+"\t"+ ss[3]+"\t"+ss[9]+"\t"+ss[6];
				txtOutLong.writefile(tmpres2+"\n");
			}
			else {
//////////////////////////////////////////////////序列质量////////////////////////////////////////////////
				int[] bpQ1 = FastQ.copeFastQ(ss[2], 10,13);
				if (bpQ1[0] > bpLength/10 || bpQ1[1] > bpLength/5  ) {
					continue;
				}
////////////////////////////////////////////////////////////////////////////////////////////////
				String tmpres2 = ss[7] + "\t"+ (Long.parseLong(ss[8])+bpLength-350) +"\t"+(Long.parseLong(ss[8])+bpLength-1) +"\t"+ ss[3]+"\t"+ss[9]+"\t"+ss[6];
				txtOutLong.writefile(tmpres2+"\n");
			}
		}
		txtOut1.close();
		txtOutLong.close();
		txtSoap.close();
	}
	
	/**
	 * 将soap转化为bed文件，用于PE，不区分正负链时候做
	 * @param soapFile
	 * @param bpLength 测序长度
	 * @param outPut1 用于后期分析的mapping文件，这个将双端长度合并
	 * @param outCombine 
	 * @throws Exception
	 */
	private static void getBed2MacsPE(String soapFile,String outPut1,String outCombine,String outError) throws Exception {
		TxtReadandWrite txtSoap = new TxtReadandWrite();
		txtSoap.setParameter(soapFile, false, true);
		TxtReadandWrite txtOut1 = new TxtReadandWrite();
		txtOut1.setParameter(outPut1, true, false);
		TxtReadandWrite txtOutComb = new TxtReadandWrite();
		txtOutComb.setParameter(outCombine, true, false);
		
		TxtReadandWrite txtOuterror = new TxtReadandWrite();
		txtOuterror.setParameter(outError, true, false);
		
		String[] string = txtSoap.readFirstLines(1).get(0).split("\t");
		int bpLength = string[1].trim().length();
		
		
		
		String content = "";
		BufferedReader readSoap = txtSoap.readfile();
		String tmpcontent=""; String tmp = "";String tmpPrespre = "";
		String[] tmpresPre =null;
		while ((content = readSoap.readLine()) != null) {
			if (content.trim().equals("")) {
				continue;
			}
			String[] ss = content.split("\t");
			//soap文件的格式是 chrID 坐标 无论mapping到正负链，该坐标都是起点，都是要向后加上bpLength-1的，
			String tmpres = ss[7] + "\t"+ ss[8] +"\t"+ (Long.parseLong(ss[8])+bpLength-1)+"\t"+ ss[3]+"\t"+ss[9]+"\t"+ss[6];
			//tmpPrespre only save content while ss[0].split("#/")[1].equals("1")
			//只需要判断#/1即可，如果#/1为正，则mapping到正链上，否则mapping到负链上
			if (ss[0].trim().endsWith("1")) {
				tmpcontent = content;
				tmpresPre = ss;
				tmpPrespre = tmpres;
				continue;
			}
			//只有当#/1和#/2的方向相反才是正确的测序结果。因为solexa测序的结果就是一正一负
			if ((ss[0].trim().endsWith("2")&&ss[6].equals("-"))
					&& tmpresPre[0].trim().endsWith("1")&&tmpresPre[6].equals("+")
			)
			{
			/////////////////////////////////////////////////序列质量////////////////////////////////////////////////
				int[] bpQ1 = FastQ.copeFastQ(ss[2], 10,13);
				int[] bpQ2 = FastQ.copeFastQ(tmpresPre[2], 10,13);
				if ((bpQ1[0] > bpLength/10 || bpQ1[1] > bpLength/5) && (bpQ2[0] > bpLength/10 || bpQ2[1] > bpLength/5)  ) {
					continue;
				}
////////////////////////////////////////////////////////////////////////////////////////////////
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
			else if ((ss[0].trim().endsWith("2")&&ss[6].equals("+"))
					&& tmpresPre[0].trim().endsWith("1")&&tmpresPre[6].equals("-")
			)
			{
				/////////////////////////////////////////////////序列质量////////////////////////////////////////////////
				int[] bpQ1 = FastQ.copeFastQ(ss[2], 10,13);
				int[] bpQ2 = FastQ.copeFastQ(tmpresPre[2], 10,13);
				if ((bpQ1[0] > bpLength/10 || bpQ1[1] > bpLength/5) && (bpQ2[0] > bpLength/10 || bpQ2[1] > bpLength/5)  ) {
					continue;
				}
////////////////////////////////////////////////////////////////////////////////////////////////
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
		txtOut1.close();
		txtOutComb.close();
		txtSoap.close();
		txtOuterror.close();
	}
	
	/**
	 * 给定solexa用soap的mapping结果，获得序列的fastQ行
	 * @return
	 * @throws Exception 
	 */
	public static ArrayList<String> getSoapFastQStr(String soapFile) throws Exception {
		TxtReadandWrite txtSoap = new TxtReadandWrite();
		txtSoap.setParameter(soapFile, false, true);
		ArrayList<String> lsSoap= txtSoap.readFirstLines(1000);
		ArrayList<String> lsFastQ = new ArrayList<String>();
		for (String string : lsSoap) {
			lsFastQ.add(string.split("\t")[2]);
		}
		return lsFastQ;
	}
}
