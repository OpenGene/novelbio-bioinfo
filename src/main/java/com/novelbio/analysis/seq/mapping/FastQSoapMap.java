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
	 * @param seqFile2 û�о�дnull
	 * @param FastQFormateOffset
	 * @param QUALITY ���� ���������е� QUALITY_HIGH��
	 * @param outFilePath ����ļ�·��
	 * @param SoapExePath soap�����·��
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
	 * @param QUALITY ���� ���������е� QUALITY_HIGH��
	 * @param outFilePath ����ļ�·��
	 * @param SoapExePath soap�����·��
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
	 * @param QUALITY ���� ���������е� QUALITY_HIGH��
	 * @param outFilePath ����ļ�·��
	 * @param SoapExePath soap�����·��
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
	 * @param QUALITY ���� ���������е� QUALITY_HIGH��
	 * @param outFilePath ����ļ�·��
	 * @param SoapExePath soap�����·��
	 * @param IndexFile
	 */
	public FastQSoapMap(String outFilePath) {
		super(null, QUALITY_MIDIAN);
		this.outFileName = outFilePath;
	}
	
	
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
	* ��soapת��Ϊbed�ļ�������SE��������������ʱ����
	* �������ڲ�����ascII����������
	* @param SE true: ����   false: ˫��
	 * @param soapFile 
	 * @param bpLength ���򳤶�
	 * @param outPut macs���ļ���������reads�Ľ��
	 * @param outComb ���ں��ڷ�����mapping�ļ������˽�reads��3����������350bp��˫������˫�˳��Ⱥϲ�
	 * @param error ˫�˲��еģ����������
	 * @throws Exception 
	 * @return ���ˣ�����ArrayList-BedSeq ��һ���ǵ��˵�bed�ļ����ڶ����Ǳ䳤��bed�ļ���������ͼ<br>
	 * ˫�ˣ�����ArrayList-BedSeq ��һ���ǵ��˵�bed�ļ����ڶ����Ǳ䳤��bed�ļ���������ͼ���������ǳ�����Ϣ

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
		String tmpcontent=""; String tmp = "";String tmpPrespre = "";
		String[] tmpresPre =null;
		while ((content = readSoap.readLine()) != null) {
			if (content.trim().equals("")) {
				continue;
			}
			String[] ss = content.split("\t");
			//soap�ļ��ĸ�ʽ�� chrID ���� ����mapping���������������궼����㣬����Ҫ������bpLength-1�ģ�
			String tmpres = ss[7] + "\t"+ ss[8] +"\t"+ (Long.parseLong(ss[8])+ss[1].length()-1)+"\t"+ ss[3]+"\t"+ss[9]+"\t"+ss[6];
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
