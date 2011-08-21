package com.novelbio.analysis.seq.mapping;

import java.io.BufferedReader;
import java.util.ArrayList;

import org.apache.commons.math.stat.descriptive.moment.ThirdMoment;
import org.apache.log4j.Logger;
import org.apache.taglibs.standard.lang.jstl.NullLiteral;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.FastQ;
import com.novelbio.analysis.seq.chipseq.preprocess.MapPeak;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

public class FastQSoapMap extends Mapping{
	/**
	 * ����ļ�·��
	 */
	String outFileName = "";
	/**
	 * soap�����·��
	 */
	String SoapExePath = "";
	/**
	 * �����ļ�
	 */
	String IndexFile = "";
	
	/**
	 * �Ƿ��mapping unique����
	 */
	boolean uniqMapping = true;
	
	
	private static Logger logger = Logger.getLogger(FastQSoapMap.class);  
	
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
	public FastQSoapMap filterReads(String fileFilterOut) 
	{
		FastQ fastQ = null;
		try {
			fastQ = super.filterReads(fileFilterOut);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("filter reads error:" + e.toString());
			return null;
		}
		FastQSoapMap fastQSoapMap= new FastQSoapMap(fastQ.getSeqFile(), fastQ.getSeqFile2(), getOffset(), getQuality(), outFileName, SoapExePath,IndexFile);
		return fastQSoapMap;
	}

	
	
	/**
	 * ˫��ֻ��unique mapping
	 * @param seqFile1
	 * @param seqFile2 û�о�дnull
	 * @param FastQFormateOffset
	 * @param QUALITY ���� ���������е� QUALITY_HIGH��
	 * @param outFilePath ����ļ�·��
	 * @param SoapExePath soap�����ȫ·��
	 * @param IndexFile
	 */
	public FastQSoapMap(String seqFile1, String seqFile2,
			int FastQFormateOffset, int QUALITY,String outFilePath,String SoapExePath, String IndexFile) {
		super(seqFile1, seqFile2, FastQFormateOffset, QUALITY);
		if (seqFile2==null || seqFile2.trim().equals("")) {
			uniqMapping = true;
		}
		this.outFileName = outFilePath;
		this.IndexFile = IndexFile;
		this.SoapExePath = SoapExePath;
	}

	/**
	 * @param seqFile1
	 * @param FastQFormateOffset
	 * @param QUALITY ���� ���������е� QUALITY_HIGH��
	 * @param outFilePath ����ļ�·��
	 * @param SoapExePath soap�����·��
	 * @param IndexFile
	 */
	public FastQSoapMap(String seqFile1,
			int FastQFormateOffset, int QUALITY,String outFilePath,String SoapExePath, String IndexFile,boolean uniqMapping) {
		super(seqFile1, null, FastQFormateOffset, QUALITY);
		this.uniqMapping = uniqMapping;
		this.outFileName = outFilePath;
		this.IndexFile = IndexFile;
		this.SoapExePath = SoapExePath;
	}
	
	/**
	 * @param seqFile1
	 * @param FastQFormateOffset
	 * @param QUALITY ���� ���������е� QUALITY_HIGH��
	 * @param outFilePath ����ļ�·��
	 * @param SoapExePath soap�����·��
	 * @param IndexFile
	 */
	public FastQSoapMap(String seqFile1
			, int QUALITY,String outFilePath,String SoapExePath, String IndexFile,boolean uniqMapping) {
		super(seqFile1, QUALITY);
		this.outFileName = outFilePath;
		this.uniqMapping = uniqMapping;
		this.IndexFile = IndexFile;
		this.SoapExePath = SoapExePath;
	}
	
//	/**
//	 * @param FastQFormateOffset
//	 * @param QUALITY ���� ���������е� QUALITY_HIGH��
//	 * @param outFilePath ����ļ�·��
//	 * @param SoapExePath soap�����·��
//	 * @param IndexFile
//	 */
//	public FastQSoapMap(String outFilePath) {
//		super(null, QUALITY_MIDIAN);
//		this.outFileName = outFilePath;
//	}
//	
	
	/**
	 * ����seqFile����mapping��������mapping֮ǰ��Ҫ���й��˴���mapping��ֱ��ת����bed�ļ�����
	 * @param fileName �����ļ���
	 * ʵ���� fileName+"_Treat_SoapMap";
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
	 * ��soap����mapping
	 * @param soapPath soap�ĳ���·��
	 * @param inputFile1 ������������������˭�Ķ����ԣ�����������ʿ�Ŀǰֻ���solexa�����˱�д
	 * @param inputFile2 ˫�˵Ļ���������һ�������ļ���û�еĻ�������null����""
	 * @param indexFile genome�ļ�������
	 * @param outFile3 ����ļ�
	 * @param minInsert ��С����Ƭ�Σ�20����
	 * @param maxInsert ������Ƭ�Σ�400����
	 * @throws Exception
	 */
	private void mapSoap(int minInsert, int maxInsert) throws Exception {
		String cmd = "";
		cmd = SoapExePath + " -a "+getSeqFile();
		cmd = cmd + " -D " +this.IndexFile; 
		cmd = cmd + " -o " +outFileName; 
		if (uniqMapping) {
			cmd = cmd +  " -r 0 ";
		}
		else {
			cmd = cmd +  " -r 2 ";
		}
		cmd = cmd +  " -v 2 -p 7 ";
		if (getBooPairEnd()) {
			cmd = cmd + " -b " + getSeqFile2();
			cmd = cmd+ " -2 "+ outFileName+"_SEout "+" -m "+minInsert+" -x "+maxInsert;
		}
		System.out.println(cmd);
		CmdOperate cmdOperate = new CmdOperate(cmd);
		cmdOperate.doInBackground();
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
	public ArrayList<BedSeq> copeSope2Bed(String outPut,String outComb,String outError) throws Exception {
		if (getBooPairEnd()) {
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
	 * @param outLong ���ں��ڷ�����mapping�ļ��������reads��3����������350bp
	 * @throws Exception
	 * @return ����ArrayList-BedSeq ��һ���ǵ��˵�bed�ļ����ڶ����Ǳ䳤��bed�ļ���������ͼ
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
		
		TxtReadandWrite txtSoap = new TxtReadandWrite();
		txtSoap.setParameter(outFileName, false, true);
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
	
	
	
}
