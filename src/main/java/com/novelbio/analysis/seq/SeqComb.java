package com.novelbio.analysis.seq;

import java.io.BufferedReader;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.novelbio.base.dataOperate.TxtReadandWrite;
/**
 * ��ȡ�����ļ����ࡣ
 * �����ļ��й̶��ĸ�ʽ����Fasta��ʽ��ͬ��
 * fasta��ʽֻҪ��ͷ��>���ɣ��������ȡ�Ĳ����ļ�ÿ�ж�Ҫ�й̶��ĺ��壬
 * Ҳ����ÿ�����ж��й̶�������
 * @author zong0jie
 *
 */
public abstract class SeqComb {
	protected TxtReadandWrite txtSeqFile;
	protected String seqFile = "";
	protected int block = 1;
	
	/**
	 * fastQ�ļ��������������
	 */
	private int seqNum = -1;
	private boolean readPattern = true;
	
	
	
	protected String compressInType = TxtReadandWrite.TXT;
	protected String compressOutType = TxtReadandWrite.TXT;
	/**
	 * ����һ��block���У�Ʃ��sam�ļ�����һ��˫��2��
	 * @param block
	 */
	public void setBlock(int block) {
		this.block = block;
	}
	
	/**
	 * 
	 * �趨�ļ�ѹ����ʽ
	 * ��TxtReadandWrite.TXT��
	 * @param cmpInType ��ȡ��ѹ����ʽ null��""��ʾ����
	 * @param cmpOutType д���ѹ����ʽ null��""��ʾ����
	 */
	public void setCompressType(String cmpInType, String cmpOutType) {
		if (cmpInType != null && !cmpInType.equals("")) {
			this.compressInType = cmpInType;
		}
		if (cmpOutType != null && !cmpOutType.equals("")) {
			this.compressOutType = cmpOutType;
		}
		if (readPattern) {
			txtSeqFile.setFiletype(compressInType);
		}
		else {
			txtSeqFile.setFiletype(compressOutType);
		}
	}
	/**
	 *  �����ѹ����ʽ
	 * @return
	 */
	public String getCompressInType() {
		return compressInType;
	}
	/**
	 *  �����ѹ����ʽ
	 * @return
	 */
	public String getCompressOutType() {
		return compressOutType;
	}
	
	private static Logger logger = Logger.getLogger(SeqComb.class);  
	/**
	 * 
	 * @param seqFile
	 * @param block ÿ������ռ���У�Ʃ��fastQ�ļ�ÿ������ռ4��
	 */
	public SeqComb(String seqFile, int block) {
		this.seqFile = seqFile;
		this.block = block;
		txtSeqFile = new TxtReadandWrite(compressInType, seqFile, false);
	}
	/**
	 * @param seqFile
	 * @param block ÿ������ռ���У�Ʃ��fastQ�ļ�ÿ������ռ4��
	 */
	public SeqComb(String seqFile, int block, boolean creatFile) {
		this.seqFile = seqFile;
		this.block = block;
		this.readPattern = !creatFile;
		if (creatFile == false) {
			txtSeqFile = new TxtReadandWrite(compressInType, seqFile, creatFile);		
		}
		else {
			txtSeqFile = new TxtReadandWrite(compressOutType, seqFile, creatFile);	
		}
	}
	/**
	 * �����ļ���
	 * @return
	 */
	public String getFileName() {
		return seqFile;
	}
	/**
	 * ������е�����������˫�˵��ˣ���ֻ����һ�˵Ĳ���������Ҳ����fragment������
	 * �������С��0��˵������
	 * @throws Exception 
	 */
	public int getSeqNum(){
		if (seqNum >= 0) {
			return seqNum;
		}
		txtSeqFile.reSetInfo();
		int readsNum = 0;
		try {
			readsNum =  txtSeqFile.ExcelRows()/block;
			txtSeqFile.close();
		} catch (Exception e) {
			logger.error(seqFile + " may not exist " + e.toString());
			return -1;
		}
		seqNum = readsNum;
		return seqNum;
	}
	/**
	 * �ݶ���ȡ����Gradient
	 * @param block 
	 * @param percent �ٷֱȣ��� 0-100
	 * @param outFile
	 * @throws Exception
	 */
	public void getGradTxt( int[] percent,String outFile) throws Exception {
		txtSeqFile.reSetInfo();
		for (int i = 0; i < percent.length; i++) {
			if (percent[i]>100) {
				percent[i] = 100;
			}
		}
		ArrayList<TxtReadandWrite> lstxtWrite = new ArrayList<TxtReadandWrite>();
		for (int i = 0; i < percent.length; i++) {
			TxtReadandWrite txtWrite = new TxtReadandWrite();
			txtWrite.setParameter(compressOutType, outFile+percent[i], true, false);
			lstxtWrite.add(txtWrite);
		}
		int rowAllNum = txtSeqFile.ExcelRows();
		BufferedReader reader = txtSeqFile.readfile();
		String content = "";
		int rowNum = 0;
		while ((content = reader.readLine()) != null) {
			for (int i = 0; i < percent.length; i++) {
				 int tmpNum =percent[i]*(rowAllNum/block)*block;
				if (rowNum<tmpNum/100) {
					lstxtWrite.get(i).writefile(content+"\n");
				}
			}
			rowNum++;
		}
		for (TxtReadandWrite txtReadandWrite : lstxtWrite) {
			txtReadandWrite.close();
		}
		txtSeqFile.close();
	}
	
	
	/**
	 * ע���������µ�adaptor�޷�����
	 * �����Ҳ��ͷ���еķ�������ѭ��������������䣬���ǲ��ܹ����Ǻ���gap��adaptor��
	 * �㷨�������Ҳ����ֻ��һ������ͷ����ô�Ƚ���ͷֱ�ӶԵ��Ҳ���룬Ȼ��ѭ���Ľ���ͷ�Ե�reads��ȥ��
	 * @param seqIn �������� ����ν��Сд
	 * @param seqAdaptor ��ͷ ����ν��Сд ��ͷ����ֻдһ����
	 * @param mapNum ��һ�ν�ͷ���mapping�����еĵڼ�������ϣ���1��ʼ������-1˵��û�ҵ� �����趨Ϊ��seqIn.length() +1- seqAdaptor.length()
	 * @param numMM ����ݴ���mismatch 2���ȽϺ�
	 * @param conNum ����ݴ���������mismatch��1���ȽϺ�
	 * @param perMm ����ݴ�ٷֱ� �趨Ϊ30�ɣ��������adaptor̫��
	 * @return ���ظ�tag�ĵ�һ������������ϵ�λ�ã���0��ʼ����
	 * Ҳ���Ǹ�adaptorǰ���ж��ٸ����������ֱ����substring(0,return)����ȡ
	 * -1˵��û��adaptor
	 */
	public static int trimAdaptorR(String seqIn, String seqAdaptor, int mapNum, int numMM, int conNum, float perMm) {
		if (seqAdaptor.equals("")) {
			return seqIn.length();
		}
		mapNum--;
		if (mapNum < 0) {
			mapNum =0;
		}
		seqIn = seqIn.toUpperCase();
		seqAdaptor = seqAdaptor.toUpperCase();
		char[] chrIn = seqIn.toCharArray(); int lenIn = seqIn.length();
		char[] chrAdaptor = seqAdaptor.toCharArray(); int lenA = seqAdaptor.length();
		int con = 0;//��¼�����ķ�ƥ����ַ��м���
//		����������chrIn
		for (int i = mapNum; i < lenIn; i++) {
			int pm = 0; //perfect match
			int mm = 0; //mismatch
			for (int j = 0; j < lenA; j++) {
				if (i+j >= lenIn)
					break;
				if (chrIn[i+j] == chrAdaptor[j] || chrIn[i+j] == 'N') {
					pm++;
					con = 0;
				}
				else {
					con ++ ;
					mm++;
					if (mm > numMM || con > conNum)
						break;
				}
			}
			int lenAdaptor = pm + mm;
//			float per = ((float)mm/lenAdaptor);
			if (mm <= numMM && ((float)mm/lenAdaptor) <= perMm && lenAdaptor > 4) {
				return i;
			}
		}
		logger.info("haven't find adaptor: "+seqIn+" "+seqAdaptor);
		return seqIn.length();
	}
	
	
	/**
	 * ע���������µ�adaptor�޷�����
	 * ��������ͷ���еķ�������ѭ��������������䣬���ǲ��ܹ����Ǻ���gap��adaptor��
	 * �㷨������������ֻ��һ������ͷ����ô�Ƚ���ͷֱ�ӶԵ������룬Ȼ��ѭ���Ľ���ͷ�Ե�reads��ȥ��
	 * @param seqIn �������� ����ν��Сд
	 * @param seqAdaptor ��ͷ ����ν��Сд
	 * @param mapNum ��һ�ν�ͷ�Ҷ�mapping�����еĵڼ�������ϣ���1��ʼ������-1˵��û�ҵ� �����趨Ϊ��adaptorLeft.length()
	 * @param numMM ����ݴ���mismatch 1���ȽϺ�
	 * @param conNum ����ݴ���������mismatch��1���ȽϺ�
	 * @param perMm ����ݴ�ٷֱ�,100���ƣ��趨Ϊ30�ɣ��������adaptor̫��
	 * @return ���ظ�tag����һ������������ϵ�λ�ã���1��ʼ����
	 * Ҳ���Ǹ�adaptorǰ���ж��ٸ����������ֱ����substring(return)����ȡ
	 * -1˵��û��adaptor
	 */
	public static int trimAdaptorL(String seqIn, String seqAdaptor, int mapNum, int conNum, int numMM, float perMm) {
		if (seqAdaptor.equals("")) {
			return 0;
		}
		mapNum--;
		seqIn = seqIn.toUpperCase();
		seqAdaptor = seqAdaptor.toUpperCase();
		char[] chrIn = seqIn.toCharArray(); //int lenIn = seqIn.length();
		char[] chrAdaptor = seqAdaptor.toCharArray(); int lenA = seqAdaptor.length();
		int con = 0;//��¼�����ķ�ƥ����ַ��м���
//		���ҵ�������chrIn
		for (int i = mapNum; i >= 0 ; i--) {
			int pm = 0; //perfect match
			int mm = 0; //mismatch
			for (int j = chrAdaptor.length-1; j >= 0; j--) {
				if (i+j-lenA+1 < 0)
					break;
				if (chrIn[i+j-lenA+1] == chrAdaptor[j] || chrIn[i+j-lenA+1] == 'N') {
					pm++; con = 0;
				}
				else {
					con ++ ;
					mm++;
					if (mm > numMM || con > conNum)
						break;
				}
			}
			int lenAdaptor = pm + mm;
//			float per = ((float)mm/lenAdaptor);
			if (mm <= numMM && ((float)mm/lenAdaptor) <= perMm/100 && lenAdaptor > 4) {
				return i+1;
			}
		}
		logger.info("haven't find adaptor: "+seqIn+" "+seqAdaptor);
		return 0;
	}
	
	
	/**
	 * �����Ҳ�polyA����ΪAAANNNAAANANAAʱ������N��������
	 * @param seqIn
	 * @param numMM �������� һ��Ϊ1
	 * @return
	 * ���ظ�Seq�ĵ�һ��A�������ϵ�λ�ã���0��ʼ����
	 * ���û��A������ֵ == Seq.length()
	 * Ҳ���Ǹ�polyAǰ���ж��ٸ����������ֱ����substring(0,return)����ȡ
	 */
	public static int trimPolyA(String seqIn, int numMM, int maxConteniunNoneA) {
		seqIn = seqIn.toUpperCase();
		char[] chrIn = seqIn.toCharArray(); int lenIn = seqIn.length();
		int numMismatch = 0;
		int con = 0;//��¼�����ķ�A���ַ��м���
		for (int i = lenIn-1; i >= 0; i--) {
			if (chrIn[i] != 'A' && chrIn[i] != 'N') {
				numMismatch++;
				con++;
			}
			else {
				con = 0;
			}
			if (numMismatch > numMM || con > maxConteniunNoneA) {
				return i+con;//�������a�Ļ��ļӻ�ȥ
			}
		}
//		System.out.println(seqIn);
		return 0;
	}
	
	/**
	 * �������polyT����ΪTTTNNNTTTNTNTTʱ������N��������
	 * @param seqIn
	 * @param numMM �������� һ��Ϊ1
	 * @return
	 * ���ظ�tag�����һ������������ϵ�λ�ã���1��ʼ����
	 * Ҳ���Ǹ�polyT�ж��ٸ����������ֱ����substring(return)����ȡ
	 */
	public static int trimPolyT(String seqIn, int numMM, int maxConteniunNoneT) {
		seqIn = seqIn.toUpperCase();
		char[] chrIn = seqIn.toCharArray(); int lenIn = seqIn.length();
		int numMismatch = 0;
		int con = 0;//��¼�����ķ�A���ַ��м���
		for (int i = 0; i < lenIn; i++) {
			if (chrIn[i] != 'T' && chrIn[i] != 'N') {
				numMismatch++;
				con++;
			}
			else {
				con = 0;
			}
			if (numMismatch > numMM || con > maxConteniunNoneT) {
				return i-con+1;//�������a�Ļ��ļӻ�ȥ
			}
		}
//		System.out.println(seqIn);
		return lenIn;
		
	}
	
	
	
	
}
