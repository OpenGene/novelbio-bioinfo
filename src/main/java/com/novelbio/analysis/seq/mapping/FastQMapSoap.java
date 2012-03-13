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
	 * �趨mismatch
	 * Ĭ��Ϊ2
	 * snp�趨Ϊ5
	 */
	public void setMisMatch(int mismatch) {
		this.mismatch = mismatch;
	}
	
	/**
	 * ˫��ֻ��unique mapping
	 * @param seqFile1
	 * @param seqFile2 û�о�дnull
	 * @param FastQFormateOffset
	 * @param QUALITY ���� ���������е� QUALITY_HIGH��
	 * @param outFile mapping����ļ�ȫ·��
	 * @param SoapExePath soap�����ȫ·��
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
	 * @param QUALITY ���� ���������е� QUALITY_HIGH��
	 * @param outFilePath ����ļ�·��
	 * @param SoapExePath soap�����·��
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
	 * @param QUALITY ���� ���������е� QUALITY_HIGH��
	 * @param outFilePath ����ļ�·��
	 * @param SoapExePath soap�����·��
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
	 * @param outFileName ����ļ���
	 * @param uniqMapping �Ƿ�uniqmapping�����˲��еĲ���
	 */
	protected FastQMapSoap(FastQ fastQ, String outFileName, boolean uniqMapping ) 
	{
		 this(fastQ.getFileName(), fastQ.getSeqFile2(),fastQ.getOffset(), fastQ.getQuality(), outFileName, uniqMapping);
	}
	
	
	/**
	 * ����seqFile����mapping��������mapping֮ǰ��Ҫ���й��˴���mapping��ֱ��ת����bed�ļ�����
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
	 * ��filterReads���õ����˺��FastQ�ļ�����mapping��
	 * ָ����ֵ����fastQ�ļ����й��˴����������ļ�����ô������ļ�Ҳ���滻���µ��ļ�
	 * @param Qvalue_Num ��ά���� ÿһ�д���һ��Qvalue �Լ������ֵĸ���
	 * int[0][0] = 13  int[0][1] = 7 :��ʾ��������Q13�ĸ���С��7��
	 * @param fileFilterOut ����ļ���׺�����ָ����fastQ�������ļ�����ô����������fileFilterOut<br>
	 * �ֱ�ΪfileFilterOut_1��fileFilterOut_2
	 * @return �����Ѿ����˺õ�FastQSoapMap����ʵ����Ҳ���ǻ�������FastQ�ļ����ѣ�mapping����ļ����䡣
	 * ���Բ���Ҫָ���µ�mapping�ļ�
	 * ������null
	 */
	protected FastQMapSoap createFastQMap(FastQ fastQ) 
	{
		FastQMapSoap fastQSoapMap= new FastQMapSoap(fastQ.getFileName(), fastQ.getSeqFile2(), fastQ.getOffset(), fastQ.getQuality(), outFileName, uniqMapping);
		return fastQSoapMap;
	}
	
	/**
	 * ǿ�Ʒ��ص��˵�bed�ļ������ڸ�macs��peak��
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
	 * ����bed�ļ��������˫�˾ͷ���˫�˵�bed�ļ�
	 * ����ǵ��˾ͷ����ӳ���bed�ļ���Ĭ���ӳ���extendTo bp
	 * @return
	 * �����򷵻�null
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
	 * ����bed�ļ�������˫�˵�bed�ļ�
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
			//soap�ļ��ĸ�ʽ�� chrID ���� ����mapping���������������궼����㣬����Ҫ������bpLength-1�ģ�
			//ֻ��Ҫ�ж�#/1���ɣ����#/1Ϊ������mapping�������ϣ�����mapping��������
			if (ss[0].trim().endsWith("1")) {
				tmpcontent = content;
				tmpresPre = ss; //��һ����ss����һ�������� "\t" �ָ�
				continue;
			}
			//ֻ�е�#/1��#/2�ķ����෴������ȷ�Ĳ���������Ϊsolexa����Ľ������һ��һ��
			if ((ss[0].trim().endsWith("2")&&ss[6].equals("-"))
					&& tmpresPre[0].trim().endsWith("1")&&tmpresPre[6].equals("+")
			)
			{
				long startLoc = 0; long endLoc = 0; long tmpPre = Long.parseLong(tmpresPre[8]) - 1; long tmpSS = Long.parseLong(ss[8]) - 1;  
				if (tmpPre < tmpSS) {
					startLoc = tmpPre; endLoc = tmpSS + ss[1].length();
				}
				else {
					logger.error("����mapping����2�������ڵ�1��readsǰ�� "+tmpcontent+"\r\n"+content);
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
					logger.error("����mapping����1�������ڵ�2��readsǰ�� "+tmpcontent+"\r\n"+content);
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
	* ��soapת��Ϊ��׼bed�ļ������Ϊ�����䣬������������ʱ����
	* �������ڲ�����ascII����������
	* @param SE true: ����   false: ˫��
	 * @param soapFile 
	 * @param bpLength ���򳤶�
	 * @param outPut macs���ļ���������reads�Ľ��
	 * @param outComb ���ں��ڷ�����mapping�ļ������˽�reads��3����������350bp��˫������˫�˳��Ⱥϲ�
	 * @param outError ˫�˲��е�,������������ļ������������
	 * @throws Exception 
	 * @return ���ˣ�����ArrayList-BedSeq ��һ���ǵ��˵�bed�ļ����ڶ����Ǳ䳤��bed�ļ���������ͼ<br>
	 * ˫�ˣ�����ArrayList-BedSeq ��һ���ǵ��˵�bed�ļ����ڶ����Ǳ䳤��bed�ļ���������ͼ���������ǳ�����Ϣ
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
	 * ��soapת��Ϊbed�ļ�������SE��������������ʱ����
	 * @param soapFile
	 * @param bpLength ���򳤶�
	 * @param outPut1 macs���ļ���������reads�Ľ��
	 * @param outLong ���ں��ڷ�����mapping�ļ��������reads��3����������250bp
	 * @throws Exception
	 * @return ����ArrayList-BedSeq ��һ���ǵ��˵�bed�ļ����ڶ����Ǳ䳤��bed�ļ���������ͼ
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
	 * ��soapת��Ϊbed�ļ�������PE��������������ʱ����
	 * @param soapFile
	 * @param bpLength ���򳤶�
	 * @param outPut1 ���ں��ڷ�����mapping�ļ��������˫�˳��Ⱥϲ�
	 * @param outCombine 
	 * @throws Exception
	 * @return ����ArrayList-BedSeq ��һ���ǵ��˵�bed�ļ����ڶ����Ǳ䳤��bed�ļ���������ͼ���������ǳ�����Ϣ
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
			//soap�ļ��ĸ�ʽ�� chrID ���� ����mapping���������������궼����㣬����Ҫ������bpLength-1�ģ�
			String tmpres = ss[7] + "\t"+ (Long.parseLong(ss[8])-1) +"\t"+ (Long.parseLong(ss[8])+ss[1].length()-1)+"\t"+ ss[3]+"\t"+ss[9]+"\t"+ss[6];
			//tmpPrespre only save content while ss[0].split("#/")[1].equals("1")
			//ֻ��Ҫ�ж�#/1���ɣ����#/1Ϊ������mapping�������ϣ�����mapping��������
			if (ss[0].trim().endsWith("1")) {
				tmpcontent = content;
				tmpresPre = ss; //��һ����ss����һ�������� "\t" �ָ�
				tmpPrespre = tmpres; //��һ������õ�����
				continue;
			}
			//ֻ�е�#/1��#/2�ķ����෴������ȷ�Ĳ���������Ϊsolexa����Ľ������һ��һ��
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
					logger.error("����mapping����2�������ڵ�1��readsǰ�� "+tmpres+"�ָ�"+content);
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
					logger.error("����mapping����1�������ڵ�2��readsǰ�� "+tmpres+"�ָ�"+content);
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
	 * ��soapת��Ϊbed�ļ���ֻ�е�Ϊpear-end��ʱ�򣬲�����Ҫ����˫���ֿ���ʱ����������
	 * �����˫��45bp
	 * @param soapFile 
	 * @param outPut1 ���#/1���е����꣬һ�����45bp��һ���յ�45bp
	 * @param outCombFile1 ���#/1���еĺϲ���һ������յ㹲fragment���ȣ����ں��滭ͼ
	 * @param outPut2 ���#/2���е����꣬һ�����45bp��һ���յ�45bp
	 * @param outCombFile2 ���#/2���еĺϲ���һ������յ㹲fragment���ȣ����ں��滭ͼ
	 * @param outError ���������Ϣ��Ҳ�������� #/1������#/2����һ������
	 * @throws Exception
	 */
	public void getBed2Macs(String outPut1, String outCombFile1, String outPut2,String outCombFile2,String outError) throws Exception {
		
		TxtReadandWrite txtSoap = new TxtReadandWrite(outFileName, false);
		TxtReadandWrite txtOut1 = new TxtReadandWrite(outPut1, true);
		TxtReadandWrite txtoutCombFile1 = new TxtReadandWrite(outCombFile1, true);
		
		TxtReadandWrite txtOut2 = new TxtReadandWrite(outPut2, true);
		TxtReadandWrite txtoutCombFile2 = new TxtReadandWrite(outCombFile2, true);
		
		TxtReadandWrite txtOuterror = new TxtReadandWrite(outError, true);
		//��ò��򳤶�
		String[] string = txtSoap.readFirstLines(1).get(0).split("\t");
		int bpLength = string[1].trim().length();
               //soap�ļ��ĸ�ʽ�� chrID ���� ����mapping���������������궼����㣬����Ҫ������bpLength-1�ģ�
		String content = "";
		BufferedReader readSoap = txtSoap.readfile();
		String tmpcontent=""; String tmp = "";String tmpPrespre = "";
		String[] tmpresPre =null;
		while ((content = readSoap.readLine()) != null) {
			if (content.trim().equals("")) {
				continue;
			}
			String[] ss = content.split("\t");
			//soap�ļ��ĸ�ʽ�� chrID ���� ����mapping���������������궼����㣬����Ҫ������bpLength-1�ģ�
			String tmpres = ss[7] + "\t"+ ss[8] +"\t"+ (Long.parseLong(ss[8])+bpLength-1)+"\t"+ ss[3]+"\t"+ss[9]+"\t"+ss[6];
			//tmpPrespre only save content while ss[0].split("#/")[1].equals("1")
			//ֻ��Ҫ�ж�#/1���ɣ����#/1Ϊ������mapping�������ϣ�����mapping��������
			if (ss[0].trim().endsWith("1")) {
				tmpcontent = content;
				tmpresPre = ss;
				tmpPrespre = tmpres;
				continue;
			}
			//ֻ�е�#/1��#/2�ķ����෴������ȷ�Ĳ���������Ϊsolexa����Ľ������һ��һ��		
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
		//һ����soap�ĵط��ͻ���2bwt-builder 
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
