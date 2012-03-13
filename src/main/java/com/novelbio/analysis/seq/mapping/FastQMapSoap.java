package com.novelbio.analysis.seq.mapping;

import java.io.BufferedReader;
import java.util.ArrayList;

import org.apache.commons.math.stat.descriptive.moment.ThirdMoment;
import org.apache.log4j.Logger;
import org.apache.taglibs.standard.lang.jstl.NullLiteral;

import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.FastQ;
import com.novelbio.analysis.seq.chipseq.preprocess.MapPeak;
import com.novelbio.analysis.tools.formatConvert.bedFormat.Soap2Bed;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.generalConf.NovelBioConst;

public class FastQMapSoap extends FastQMapAbs{

	
	private static Logger logger = Logger.getLogger(FastQMapSoap.class);  
	String exeIndexPath = "";

	int mismatch = 2;
	/**
	 * 设定mismatch
	 * 默认为2
	 * snp设定为5
	 */
	public void setMisMatch(int mismatch) {
		this.mismatch = mismatch;
	}
	
	/**
	 * 双端只做unique mapping
	 * @param seqFile1
	 * @param seqFile2 没有就写null
	 * @param FastQFormateOffset
	 * @param QUALITY 质量 有三档高中低 QUALITY_HIGH等
	 * @param outFile mapping结果文件全路径
	 * @param SoapExePath soap程序的全路径
	 * @param IndexFile
	 */
	public FastQMapSoap(String seqFile1, String seqFile2,
			int FastQFormateOffset, int QUALITY,String outFile, boolean uniqMapping) {
		super(seqFile1, seqFile2, FastQFormateOffset, QUALITY);
		this.uniqMapping = uniqMapping;
		this.outFileName = outFile;
	}

	/**
	 * @param seqFile1
	 * @param FastQFormateOffset
	 * @param QUALITY 质量 有三档高中低 QUALITY_HIGH等
	 * @param outFilePath 结果文件路径
	 * @param SoapExePath soap程序的路径
	 * @param IndexFile
	 */
	public FastQMapSoap(String seqFile1,
			int FastQFormateOffset, int QUALITY,String outFilePath,boolean uniqMapping) {
		super(seqFile1, null, FastQFormateOffset, QUALITY);
		this.uniqMapping = uniqMapping;
		this.outFileName = outFilePath;
	}
	
	/**
	 * @param seqFile1
	 * @param FastQFormateOffset
	 * @param QUALITY 质量 有三档高中低 QUALITY_HIGH等
	 * @param outFilePath 结果文件路径
	 * @param SoapExePath soap程序的路径
	 * @param IndexFile
	 */
	public FastQMapSoap(String seqFile1
			, int QUALITY,String outFilePath,boolean uniqMapping) {
		super(seqFile1, QUALITY);
		this.outFileName = outFilePath;
		this.uniqMapping = uniqMapping;
	}
	
	/**
	 * @param fastQ
	 * @param outFileName 结果文件名
	 * @param uniqMapping 是否uniqmapping，单端才有的参数
	 */
	protected FastQMapSoap(FastQ fastQ, String outFileName, boolean uniqMapping ) 
	{
		 this(fastQ.getFileName(), fastQ.getSeqFile2(),fastQ.getOffset(), fastQ.getQuality(), outFileName, uniqMapping);
	}
	
	
	/**
	 * 将本seqFile进行mapping分析，做mapping之前先要进行过滤处理，mapping后直接转化成bed文件返回
	 * @throws Exception
	 */
	public void mapReads()  {
		 IndexMake();
		// soap -a TGACT.fastq -b TGACT2.fastq -D /NC_009443.fna.index -o soapMapping -2 soapMappingNotPair -m 20 -x 500
		String cmd = "";
		cmd = ExePath + "soap -a "+getFileName();
		cmd = cmd + " -D " + chrFile + ".index "; 
		cmd = cmd + " -o " +outFileName; 
		cmd = cmd +  " -r 2 ";
		cmd = cmd +  " -v "+mismatch+" -p 4 ";
		if (isPairEnd()) {
			cmd = cmd + " -b " + getSeqFile2();
			cmd = cmd+ " -2 "+ outFileName+"_NotPair "+" -m "+minInsert+" -x "+maxInsert;
		}
		cmd = cmd+ " -u  "+ outFileName+"_NoMapping ";
		System.out.println(cmd);
		CmdOperate cmdOperate = new CmdOperate(cmd);
		cmdOperate.doInBackground("soapMapping");
	}
	
	/**
	 * 先filterReads，得到过滤后的FastQ文件后，再mapping，
	 * 指定阈值，将fastQ文件进行过滤处理并产生新文件，那么本类的文件也会替换成新的文件
	 * @param Qvalue_Num 二维数组 每一行代表一个Qvalue 以及最多出现的个数
	 * int[0][0] = 13  int[0][1] = 7 :表示质量低于Q13的个数小于7个
	 * @param fileFilterOut 结果文件后缀，如果指定的fastQ有两个文件，那么最后输出两个fileFilterOut<br>
	 * 分别为fileFilterOut_1和fileFilterOut_2
	 * @return 返回已经过滤好的FastQSoapMap，其实里面也就是换了两个FastQ文件而已，mapping结果文件不变。
	 * 所以不需要指定新的mapping文件
	 * 出错返回null
	 */
	protected FastQMapSoap createFastQMap(FastQ fastQ) 
	{
		FastQMapSoap fastQSoapMap= new FastQMapSoap(fastQ.getFileName(), fastQ.getSeqFile2(), fastQ.getOffset(), fastQ.getQuality(), outFileName, uniqMapping);
		return fastQSoapMap;
	}
	
	/**
	 * 强制返回单端的bed文件，用于给macs找peak用
	 * @return
	 */
	public BedSeq getBedFileSE(String bedFile)  {
		if (!FileOperate.isFileExist(outFileName)) {
			mapReads();
		}
		TxtReadandWrite txtSoap = new TxtReadandWrite(outFileName, false);
		TxtReadandWrite txtOut1 = new TxtReadandWrite(bedFile, true);
		BedSeq bedSeqOut1 = null;
		String content = "";
		try {
			BufferedReader readSoap = txtSoap.readfile();
			while ((content = readSoap.readLine()) != null) {
				if (content.trim().equals("")) {
					continue;
				}
				String[] ss = content.split("\t");
				String tmpres = "";
				if (uniqMapping && Integer.parseInt(ss[3]) > 1) {
					continue;
				}
				else {
					tmpres = ss[7] + "\t"+ (Long.parseLong(ss[8])-1) +"\t"+ (Long.parseLong(ss[8])+ss[1].length()-1)+"\t"+ ss[12]+"\t"+ss[11]+"\t"+ss[6] +"\t" + ss[3];
				}
				txtOut1.writefileln(tmpres);
			}
			txtOut1.close();
			txtSoap.close();
			bedSeqOut1 = new BedSeq(bedFile);
		} catch (Exception e) {
			logger.error("BedFile Error:" + bedFile);
		}
		return bedSeqOut1;
	}
	
	/**
	 * 返回bed文件，如果是双端就返回双端的bed文件
	 * 如果是单端就返回延长的bed文件，默认延长至extendTo bp
	 * @return
	 * 出错则返回null
	 */
	public BedSeq getBedFile(String outPut1) {
		if (isPairEnd()) {
			try {
				return getBedFilePE(outFileName, outPut1);
			} catch (Exception e) {
				logger.error("convertBedFile error:"+ outFileName + " "+ outPut1);
				e.printStackTrace();
				return null;
			}
		}
		else {
			BedSeq bedSeq = getBedFileSE(outPut1+"raw");
			return bedSeq.extend(extendTo, outPut1);
		}
	}
	/**
	 * 返回bed文件，返回双端的bed文件
	 * @return
	 */
	private BedSeq getBedFilePE(String soapFile,String outPut1) throws Exception {
		if (!FileOperate.isFileExist(outFileName)) {
			mapReads();
		}
		TxtReadandWrite txtSoap = new TxtReadandWrite(outFileName, false);
		TxtReadandWrite txtOutComb = new TxtReadandWrite(outPut1, true);
		
		String content = "";
		BufferedReader readSoap = txtSoap.readfile();
		String tmpcontent=""; String tmpPrespre = "";
		String[] tmpresPre =null;
		while ((content = readSoap.readLine()) != null) {
			if (content.trim().equals("")) {
				continue;
			}
			String[] ss = content.split("\t");
			//soap文件的格式是 chrID 坐标 无论mapping到正负链，该坐标都是起点，都是要向后加上bpLength-1的，
			//只需要判断#/1即可，如果#/1为正，则mapping到正链上，否则mapping到负链上
			if (ss[0].trim().endsWith("1")) {
				tmpcontent = content;
				tmpresPre = ss; //上一条的ss，上一条序列用 "\t" 分割
				continue;
			}
			//只有当#/1和#/2的方向相反才是正确的测序结果。因为solexa测序的结果就是一正一负
			if ((ss[0].trim().endsWith("2")&&ss[6].equals("-"))
					&& tmpresPre[0].trim().endsWith("1")&&tmpresPre[6].equals("+")
			)
			{
				long startLoc = 0; long endLoc = 0; long tmpPre = Long.parseLong(tmpresPre[8]) - 1; long tmpSS = Long.parseLong(ss[8]) - 1;  
				if (tmpPre < tmpSS) {
					startLoc = tmpPre; endLoc = tmpSS + ss[1].length();
				}
				else {
					logger.error("正向mapping，第2条序列在第1条reads前： "+tmpcontent+"\r\n"+content);
					startLoc = tmpSS; endLoc = tmpPre + tmpresPre[1].length();
				}
				txtOutComb.writefileln(tmpresPre[7]+"\t"+startLoc+"\t"+endLoc + "\t" + tmpresPre[12] + "\t" + tmpresPre[11] +"\t+\t" +tmpresPre[3]);
			}
			else if ((ss[0].trim().endsWith("2")&&ss[6].equals("+"))
					&& tmpresPre[0].trim().endsWith("1")&&tmpresPre[6].equals("-")
			)
			{
				long startLoc = 0; long endLoc = 0; long tmpPre = Long.parseLong(tmpresPre[8]) - 1; long tmpSS = Long.parseLong(ss[8]) - 1;  
				if (tmpPre<tmpSS) {
					logger.error("反向mapping，第1条序列在第2条reads前： "+tmpcontent+"\r\n"+content);
					startLoc = tmpPre; endLoc = tmpSS + ss[1].length();
				}
				else {
					startLoc = tmpSS; endLoc = tmpPre + tmpresPre[1].length();
				}
				txtOutComb.writefileln(tmpresPre[7]+"\t"+startLoc+"\t"+endLoc + "\t" + tmpresPre[12] + "\t" + tmpresPre[11] +"\t-\t" +tmpresPre[3]);
			}
		}
		txtOutComb.close();
		txtSoap.close();
		return new BedSeq(outPut1);
	}
	
	
	
	
	
	
	
	
	
	
	
	/**
	* 将soap转化为标准bed文件，左端为开区间，不区分正负链时候做
	* 本方法内部含有ascII的质量控制
	* @param SE true: 单端   false: 双端
	 * @param soapFile 
	 * @param bpLength 测序长度
	 * @param outPut macs的文件，仅仅是reads的结果
	 * @param outComb 用于后期分析的mapping文件，单端将reads向3‘端延生至350bp，双端则是双端长度合并
	 * @param outError 双端才有的,错误序列输出文件，单端随便设
	 * @throws Exception 
	 * @return 单端：返回ArrayList-BedSeq 第一个是单端的bed文件，第二个是变长的bed文件，用于作图<br>
	 * 双端：返回ArrayList-BedSeq 第一个是单端的bed文件，第二个是变长的bed文件，用于作图，第三个是出错信息
	 */
	@Deprecated
	public ArrayList<BedSeq> copeSope2Bed(String outPut,String outComb,String outError) throws Exception {
		if (isPairEnd()) {
			return getBed2MacsPE(outFileName, outPut, outComb, outError);
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
	 * @param outLong 用于后期分析的mapping文件，这个将reads向3‘端延生至250bp
	 * @throws Exception
	 * @return 返回ArrayList-BedSeq 第一个是单端的bed文件，第二个是变长的bed文件，用于作图
	 */
	@Deprecated
	private ArrayList<BedSeq> getBed2MacsSE(String soapFile,String outPut1,String outLong) throws Exception {
		TxtReadandWrite txtSoap = new TxtReadandWrite(soapFile, false);
		TxtReadandWrite txtOut1 = new TxtReadandWrite(outPut1, true);
		TxtReadandWrite txtOutLong = new TxtReadandWrite(outLong, true);
		
		String content = "";
		BufferedReader readSoap = txtSoap.readfile();
		while ((content = readSoap.readLine()) != null) {
			if (content.trim().equals("")) {
				continue;
			}
			String[] ss = content.split("\t");
			
			String tmpres = ss[7] + "\t"+ (Long.parseLong(ss[8])-1) +"\t"+ (Long.parseLong(ss[8])+ss[1].length()-1)+"\t"+ ss[3]+"\t"+ss[9]+"\t"+ss[6];
			txtOut1.writefile(tmpres+"\n");
			if (ss[6].equals("+")) {
				String tmpres2 = ss[7] + "\t"+ (Long.parseLong(ss[8])-1) +"\t"+ (Long.parseLong(ss[8])+249)+"\t"+ ss[3]+"\t"+ss[9]+"\t"+ss[6];
				txtOutLong.writefile(tmpres2+"\n");
			}
			else {
				String tmpres2 = ss[7] + "\t"+ (Long.parseLong(ss[8])+ss[1].length()-251) +"\t"+(Long.parseLong(ss[8])+ss[1].length()-1) +"\t"+ ss[3]+"\t"+ss[9]+"\t"+ss[6];
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
	@Deprecated
	private ArrayList<BedSeq> getBed2MacsPE(String soapFile,String outPut1,String outCombine,String outError) throws Exception {
		TxtReadandWrite txtSoap = new TxtReadandWrite(soapFile, false);
		TxtReadandWrite txtOut1 = new TxtReadandWrite(outPut1, true);
		TxtReadandWrite txtOutComb = new TxtReadandWrite(outCombine, true);
		
		TxtReadandWrite txtOuterror = new TxtReadandWrite(outError, true);
		
		String content = "";
		BufferedReader readSoap = txtSoap.readfile();
		String tmpcontent=""; String tmpPrespre = "";
		String[] tmpresPre =null;
		while ((content = readSoap.readLine()) != null) {
			if (content.trim().equals("")) {
				continue;
			}
			String[] ss = content.split("\t");
			//soap文件的格式是 chrID 坐标 无论mapping到正负链，该坐标都是起点，都是要向后加上bpLength-1的，
			String tmpres = ss[7] + "\t"+ (Long.parseLong(ss[8])-1) +"\t"+ (Long.parseLong(ss[8])+ss[1].length()-1)+"\t"+ ss[3]+"\t"+ss[9]+"\t"+ss[6];
			//tmpPrespre only save content while ss[0].split("#/")[1].equals("1")
			//只需要判断#/1即可，如果#/1为正，则mapping到正链上，否则mapping到负链上
			if (ss[0].trim().endsWith("1")) {
				tmpcontent = content;
				tmpresPre = ss; //上一条的ss，上一条序列用 "\t" 分割
				tmpPrespre = tmpres; //上一条整理好的序列
				continue;
			}
			//只有当#/1和#/2的方向相反才是正确的测序结果。因为solexa测序的结果就是一正一负
			if ((ss[0].trim().endsWith("2")&&ss[6].equals("-"))
					&& tmpresPre[0].trim().endsWith("1")&&tmpresPre[6].equals("+")
			)
			{
				txtOut1.writefile(tmpPrespre+"\n"+tmpres+"\n");
				long startLoc = 0; long endLoc = 0; long tmpPre = Long.parseLong(tmpresPre[8]) - 1; long tmpSS = Long.parseLong(ss[8]) - 1;  
				if (tmpPre<tmpSS) {
					startLoc = tmpPre; endLoc = tmpSS + ss[1].length();
				}
				else {
					logger.error("正向mapping，第2条序列在第1条reads前： "+tmpres+"分割"+content);
					startLoc = tmpSS; endLoc = tmpPre + tmpresPre[1].length();
				}
				txtOutComb.writefile(ss[7]+"\t"+startLoc+"\t"+endLoc+"\t+"+"\n");
			}
			else if ((ss[0].trim().endsWith("2")&&ss[6].equals("+"))
					&& tmpresPre[0].trim().endsWith("1")&&tmpresPre[6].equals("-")
			)
			{
				txtOut1.writefile(tmpPrespre+"\n"+tmpres+"\n");
				long startLoc = 0; long endLoc = 0; long tmpPre = Long.parseLong(tmpresPre[8]) - 1; long tmpSS = Long.parseLong(ss[8]) - 1;  
				if (tmpPre<tmpSS) {
					logger.error("反向mapping，第1条序列在第2条reads前： "+tmpres+"分割"+content);
					startLoc = tmpPre; endLoc = tmpSS + ss[1].length();
				}
				else {
					startLoc = tmpSS; endLoc = tmpPre + tmpresPre[1].length();
				}
				txtOutComb.writefile(ss[7]+"\t"+startLoc+"\t"+endLoc+"\t-"+"\n");
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
	
	
	/**
	 * 将soap转化为bed文件，只有当为pear-end的时候，并且需要将单双链分开的时候才用这个。
	 * 假设测双端45bp
	 * @param soapFile 
	 * @param outPut1 输出#/1序列的坐标，一行起点45bp，一行终点45bp
	 * @param outCombFile1 输出#/1序列的合并，一行起点终点共fragment长度，用于后面画图
	 * @param outPut2 输出#/2序列的坐标，一行起点45bp，一行终点45bp
	 * @param outCombFile2 输出#/2序列的合并，一行起点终点共fragment长度，用于后面画图
	 * @param outError 输出错误信息，也就是两个 #/1或两个#/2连在一起的情况
	 * @throws Exception
	 */
	public void getBed2Macs(String outPut1, String outCombFile1, String outPut2,String outCombFile2,String outError) throws Exception {
		
		TxtReadandWrite txtSoap = new TxtReadandWrite(outFileName, false);
		TxtReadandWrite txtOut1 = new TxtReadandWrite(outPut1, true);
		TxtReadandWrite txtoutCombFile1 = new TxtReadandWrite(outCombFile1, true);
		
		TxtReadandWrite txtOut2 = new TxtReadandWrite(outPut2, true);
		TxtReadandWrite txtoutCombFile2 = new TxtReadandWrite(outCombFile2, true);
		
		TxtReadandWrite txtOuterror = new TxtReadandWrite(outError, true);
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



	@Override
	protected void IndexMake() {
		if (FileOperate.isFileExist(chrFile+".index.bwt")) {
			return;
		}
		//一般有soap的地方就会有2bwt-builder 
		exeIndexPath = FileOperate.getParentPathName(ExePath) + "2bwt-builder ";
		String cmd = exeIndexPath + chrFile;
		System.out.println(cmd);
		CmdOperate cmdOperate = new CmdOperate(cmd);
		cmdOperate.doInBackground("soapIndex");
	}

	@Override
	public void setMapQ(int mapQ) {
		// TODO Auto-generated method stub
		
	}

	
	
}
