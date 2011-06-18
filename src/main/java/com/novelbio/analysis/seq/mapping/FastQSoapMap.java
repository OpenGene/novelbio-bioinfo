package com.novelbio.analysis.seq.mapping;

import java.io.BufferedReader;
import java.util.ArrayList;

import org.apache.commons.math.stat.descriptive.moment.ThirdMoment;
import org.apache.log4j.Logger;
import org.apache.taglibs.standard.lang.jstl.NullLiteral;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.analysis.seq.chipseq.preprocess.MapPeak;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

public class FastQSoapMap extends FastQ implements Mapping{
	/**
	 * 结果文件路径
	 */
	String outFileName = "";
	/**
	 * soap程序的路径
	 */
	String SoapExePath = "";
	/**
	 * 索引文件
	 */
	String IndexFile = "";
		
	private static Logger logger = Logger.getLogger(FastQSoapMap.class);  
	
	/**
	 * 先filterReads，得到过滤后的FastQ文件后，再mapping，
	 * 指定阈值，将fastQ文件进行过滤处理并产生新文件，那么本类的文件也会替换成新的文件
	 * @param Qvalue_Num 二维数组 每一行代表一个Qvalue 以及最多出现的个数
	 * int[0][0] = 13  int[0][1] = 7 :表示质量低于Q13的个数小于7个
	 * @param fileFilterOut 结果文件后缀，如果指定的fastQ有两个文件，那么最后输出两个fileFilterOut<br>
	 * 分别为fileFilterOut_1和fileFilterOut_2
	 * @return 返回已经过滤好的FastQSoapMap，其实里面也就是换了两个FastQ文件而已，mapping结果文件不变。
	 * 所以不需要指定新的mapping文件
	 * @throws Exception 
	 */
	public FastQSoapMap filterReads(String fileFilterOut) 
	{
		FastQ fastQ = null;
		try {
			fastQ = super.filterReads(fileFilterOut);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("filter reads error:" + e.toString());
		}
		FastQSoapMap fastQSoapMap= new FastQSoapMap(fastQ.seqFile, fastQ.seqFile2, offset, quality, outFileName, SoapExePath,IndexFile);
		return fastQSoapMap;
	}

	
	
	/**
	 * @param seqFile1
	 * @param seqFile2 没有就写null
	 * @param FastQFormateOffset
	 * @param QUALITY 质量 有三档高中低 QUALITY_HIGH等
	 * @param outFilePath 结果文件路径
	 * @param SoapExePath soap程序的路径
	 * @param IndexFile
	 */
	public FastQSoapMap(String seqFile1, String seqFile2,
			int FastQFormateOffset, int QUALITY,String outFilePath,String SoapExePath, String IndexFile) {
		super(seqFile1, seqFile2, FastQFormateOffset, QUALITY);
		this.outFileName = outFilePath;
		this.IndexFile = IndexFile;
	}

	/**
	 * @param seqFile1
	 * @param FastQFormateOffset
	 * @param QUALITY 质量 有三档高中低 QUALITY_HIGH等
	 * @param outFilePath 结果文件路径
	 * @param SoapExePath soap程序的路径
	 * @param IndexFile
	 */
	public FastQSoapMap(String seqFile1,
			int FastQFormateOffset, int QUALITY,String outFilePath,String SoapExePath, String IndexFile) {
		super(seqFile1, null, FastQFormateOffset, QUALITY);
		this.outFileName = outFilePath;
		this.IndexFile = IndexFile;
	}
	
	/**
	 * @param seqFile1
	 * @param FastQFormateOffset
	 * @param QUALITY 质量 有三档高中低 QUALITY_HIGH等
	 * @param outFilePath 结果文件路径
	 * @param SoapExePath soap程序的路径
	 * @param IndexFile
	 */
	public FastQSoapMap(String seqFile1
			, int QUALITY,String outFilePath,String SoapExePath, String IndexFile) {
		super(seqFile1, QUALITY);
		this.outFileName = outFilePath;
		this.IndexFile = IndexFile;
	}
	
	/**
	 * @param FastQFormateOffset
	 * @param QUALITY 质量 有三档高中低 QUALITY_HIGH等
	 * @param outFilePath 结果文件路径
	 * @param SoapExePath soap程序的路径
	 * @param IndexFile
	 */
	public FastQSoapMap(String outFilePath) {
		super(null, QUALITY_MIDIAN);
		this.outFileName = outFilePath;
	}
	
	
	/**
	 * 将本seqFile进行mapping分析，做mapping之前先要进行过滤处理，mapping后直接转化成bed文件返回
	 * @param fileName 最后的文件名
	 * 实验组 fileName+"_Treat_SoapMap";
	 */
	@Override
	public BedSeq mapReads(){
		try {
			mapSoap( 20, 500);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("mapping error");
		}
		return null;
	}

	/**
	 * 
	 * 用soap进行mapping
	 * @param soapPath soap的程序路径
	 * @param inputFile1 输入测序结果，好像随便谁的都可以，不过后面的质控目前只针对solexa进行了编写
	 * @param inputFile2 双端的话，输入另一个测序文件，没有的话就输入null或者""
	 * @param indexFile genome文件的索引
	 * @param outFile3 输出文件
	 * @param minInsert 最小插入片段，20好了
	 * @param maxInsert 最大插入片段，400好了
	 * @throws Exception
	 */
	private void mapSoap(int minInsert, int maxInsert) throws Exception {
		String cmd = "";
		cmd = SoapExePath + " -a "+seqFile;
		cmd = cmd + " -D " +this.IndexFile; 
		cmd = cmd + " -o " +outFileName; 
		cmd = cmd +  " -r 0 -v 2 -p 7 ";
		if (booPairEnd) {
			cmd = cmd + " -b " + seqFile2;
			cmd = cmd+ " -2 "+ outFileName+"_SEout "+" -m "+minInsert+" -x "+maxInsert;
		}
		System.out.println(cmd);
		CmdOperate cmdOperate = new CmdOperate(cmd);
		cmdOperate.doInBackground();
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
	 * @return 单端：返回ArrayList-BedSeq 第一个是单端的bed文件，第二个是变长的bed文件，用于作图<br>
	 * 双端：返回ArrayList-BedSeq 第一个是单端的bed文件，第二个是变长的bed文件，用于作图，第三个是出错信息

	 */
	public ArrayList<BedSeq> copeSope2Bed(String outPut,String outComb,String error) throws Exception {
		if (booPairEnd) {
			return getBed2MacsPE(outFileName, outPut, outComb, error);
		}
		else {
			return getBed2MacsSE(outFileName, outPut, outComb);
		}
	}
	
	
	
	/**
	 * 将soap转化为bed文件，用于SE，不区分正负链时候做
	 * @param soapFile
	 * @param bpLength 测序长度
	 * @param outPut1 macs的文件，仅仅是reads的结果
	 * @param outLong 用于后期分析的mapping文件，这个将reads向3‘端延生至350bp
	 * @throws Exception
	 * @return 返回ArrayList-BedSeq 第一个是单端的bed文件，第二个是变长的bed文件，用于作图
	 */
	private ArrayList<BedSeq> getBed2MacsSE(String soapFile,String outPut1,String outLong) throws Exception {
		TxtReadandWrite txtSoap = new TxtReadandWrite();
		txtSoap.setParameter(soapFile, false, true);
		TxtReadandWrite txtOut1 = new TxtReadandWrite();
		txtOut1.setParameter(outPut1, true, false);
		TxtReadandWrite txtOutLong = new TxtReadandWrite();
		txtOutLong.setParameter(outLong, true, false);
		
		String content = "";
		BufferedReader readSoap = txtSoap.readfile();
		while ((content = readSoap.readLine()) != null) {
			if (content.trim().equals("")) {
				continue;
			}
			String[] ss = content.split("\t");
			
			String tmpres = ss[7] + "\t"+ ss[8] +"\t"+ (Long.parseLong(ss[8])+ss[1].length()-1)+"\t"+ ss[3]+"\t"+ss[9]+"\t"+ss[6];
			txtOut1.writefile(tmpres+"\n");
			if (ss[6].equals("+")) {		
				String tmpres2 = ss[7] + "\t"+ ss[8] +"\t"+ (Long.parseLong(ss[8])+349)+"\t"+ ss[3]+"\t"+ss[9]+"\t"+ss[6];
				txtOutLong.writefile(tmpres2+"\n");
			}
			else {
				String tmpres2 = ss[7] + "\t"+ (Long.parseLong(ss[8])+ss[1].length()-350) +"\t"+(Long.parseLong(ss[8])+ss[1].length()-1) +"\t"+ ss[3]+"\t"+ss[9]+"\t"+ss[6];
				txtOutLong.writefile(tmpres2+"\n");
			}
		}
		txtOut1.close();
		txtOutLong.close();
		txtSoap.close();
		BedSeq bedSeqOut1 = new BedSeq(outPut1);
		BedSeq bedSeqOutLong = new BedSeq(outLong);
		ArrayList<BedSeq> lsBedSeqs = new ArrayList<BedSeq>();
		lsBedSeqs.add(bedSeqOut1);
		lsBedSeqs.add(bedSeqOutLong);
		return lsBedSeqs;
	}
	
	/**
	 * 将soap转化为bed文件，用于PE，不区分正负链时候做
	 * @param soapFile
	 * @param bpLength 测序长度
	 * @param outPut1 用于后期分析的mapping文件，这个将双端长度合并
	 * @param outCombine 
	 * @throws Exception
	 * @return 返回ArrayList-BedSeq 第一个是单端的bed文件，第二个是变长的bed文件，用于作图，第三个是出错信息
	 */
	private ArrayList<BedSeq> getBed2MacsPE(String soapFile,String outPut1,String outCombine,String outError) throws Exception {
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
			if (content.trim().equals("")) {
				continue;
			}
			String[] ss = content.split("\t");
			//soap文件的格式是 chrID 坐标 无论mapping到正负链，该坐标都是起点，都是要向后加上bpLength-1的，
			String tmpres = ss[7] + "\t"+ ss[8] +"\t"+ (Long.parseLong(ss[8])+ss[1].length()-1)+"\t"+ ss[3]+"\t"+ss[9]+"\t"+ss[6];
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
				txtOut1.writefile(tmpPrespre+"\n"+tmpres+"\n");
				long startLoc = 0; long endLoc = 0; long tmpPre = Long.parseLong(tmpresPre[8]); long tmpSS = Long.parseLong(ss[8]);  
				if (tmpPre<tmpSS) {
					startLoc = tmpPre; endLoc = tmpSS + ss[1].length()-1;
				}
				else {
					startLoc = tmpSS; endLoc = tmpPre + tmpresPre[1].length() - 1;
				}
				txtOutComb.writefile(ss[7]+"\t"+startLoc+"\t"+endLoc+"\n");
			}
			else if ((ss[0].trim().endsWith("2")&&ss[6].equals("+"))
					&& tmpresPre[0].trim().endsWith("1")&&tmpresPre[6].equals("-")
			)
			{
				txtOut1.writefile(tmpPrespre+"\n"+tmpres+"\n");
				long startLoc = 0; long endLoc = 0; long tmpPre = Long.parseLong(tmpresPre[8]); long tmpSS = Long.parseLong(ss[8]);  
				if (tmpPre<tmpSS) {
					startLoc = tmpPre; endLoc = tmpSS + ss[1].length()-1;
				}
				else {
					startLoc = tmpSS; endLoc = tmpPre + tmpresPre[1].length() - 1;
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
		ArrayList<BedSeq> lsBedSeqs = new ArrayList<BedSeq>();
		lsBedSeqs.add(new BedSeq(outPut1));
		lsBedSeqs.add(new BedSeq(outCombine));
		lsBedSeqs.add(new BedSeq(outError));
		return lsBedSeqs;
		
		
	}
	
	
	
	
	
}
