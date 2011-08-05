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
	TxtReadandWrite txtSeqFile = new TxtReadandWrite();
	String seqFile = "";
	int block = 1;
	
	/**
	 * fastQ�ļ��������������
	 */
	int seqNum = -1;
	
	private static Logger logger = Logger.getLogger(SeqComb.class);  
	/**
	 * 
	 * @param seqFile
	 * @param block ÿ������ռ���У�Ʃ��fastQ�ļ�ÿ������ռ4��
	 */
	public SeqComb(String seqFile, int block) {
		this.seqFile = seqFile;
		this.block = block;
	}
	public String getSeqFile() {
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
		txtSeqFile.setParameter(seqFile, false, true);
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
		txtSeqFile.setParameter(seqFile, false, true);
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
	 * �����Ҳ��ͷ���еķ�������ѭ��������������䣬���ǲ��ܹ����Ǻ���gap��adaptor��
	 * �㷨�������Ҳ����ֻ��һ������ͷ����ô�Ƚ���ͷֱ�ӶԵ��Ҳ���룬Ȼ��ѭ���Ľ���ͷ�Ե�reads��ȥ��
	 * @param seqIn �������� ����ν��Сд
	 * @param seqAdaptor ��ͷ ����ν��Сд
	 * @param mapNum ��һ�ν�ͷ���mapping�����еĵڼ�������ϣ���1��ʼ������-1˵��û�ҵ�
	 * @param numMM ����ݴ���mismatch
	 * @param perMm ����ݴ�ٷֱ�
	 * @return ���ظ�tag�ĵ�һ������������ϵ�λ�ã���0��ʼ����
	 * Ҳ���Ǹ�adaptorǰ���ж��ٸ����������ֱ����substring(0,return)����ȡ
	 * -1˵��û��adaptor
	 */
	public static int trimAdaptorR(String seqIn, String seqAdaptor, int mapNum, int numMM, float perMm) {
		mapNum--;
		seqIn = seqIn.toUpperCase();
		seqAdaptor = seqAdaptor.toUpperCase();
		char[] chrIn = seqIn.toCharArray(); int lenIn = seqIn.length();
		char[] chrAdaptor = seqAdaptor.toCharArray(); int lenA = seqAdaptor.length();
//		����������chrIn
		for (int i = mapNum; i < lenIn; i++) {
			int pm = 0; //perfect match
			int mm = 0; //mismatch
			for (int j = 0; j < lenA; j++) {
				if (i+j >= lenIn)
					break;
				if (chrIn[i+j] == chrAdaptor[j]) {
					pm++;
				}
				else {
					mm++;
					if (mm > numMM)
						break;
				}
			}
			int lenAdaptor = pm + mm;
//			float per = ((float)mm/lenAdaptor);
			if (mm <= numMM && ((float)mm/lenAdaptor) <= perMm && lenAdaptor > 4) {
				return i;
			}
		}
		logger.error("haven't find adaptor: "+seqIn+" "+seqAdaptor);
		return -1;
	}
	
	
	/**
	 * ��������ͷ���еķ�������ѭ��������������䣬���ǲ��ܹ����Ǻ���gap��adaptor��
	 * �㷨�������Ҳ����ֻ��һ������ͷ����ô�Ƚ���ͷֱ�ӶԵ��Ҳ���룬Ȼ��ѭ���Ľ���ͷ�Ե�reads��ȥ��
	 * @param seqIn �������� ����ν��Сд
	 * @param seqAdaptor ��ͷ ����ν��Сд
	 * @param mapNum ��һ�ν�ͷ���mapping�����еĵڼ�������ϣ���1��ʼ������-1˵��û�ҵ�
	 * @param numMM ����ݴ���mismatch
	 * @param perMm ����ݴ�ٷֱ�
	 * @return ���ظ�tag����һ������������ϵ�λ�ã���1��ʼ����
	 * Ҳ���Ǹ�adaptorǰ���ж��ٸ����������ֱ����substring(return)����ȡ
	 * -1˵��û��adaptor
	 */
	public static int trimAdaptorL(String seqIn, String seqAdaptor, int mapNum, int numMM, float perMm) {
		mapNum--;
		seqIn = seqIn.toUpperCase();
		seqAdaptor = seqAdaptor.toUpperCase();
		char[] chrIn = seqIn.toCharArray(); int lenIn = seqIn.length();
		char[] chrAdaptor = seqAdaptor.toCharArray(); int lenA = seqAdaptor.length();
//		���ҵ�������chrIn
		for (int i = mapNum; i >= 0 ; i--) {
			int pm = 0; //perfect match
			int mm = 0; //mismatch
			for (int j = chrAdaptor.length-1; j >= 0; j--) {
				if (i+j-lenA+1 < 0)
					break;
				if (chrIn[i+j-lenA+1] == chrAdaptor[j]) {
					pm++;
				}
				else {
					mm++;
					if (mm > numMM)
						break;
				}
			}
			int lenAdaptor = pm + mm;
//			float per = ((float)mm/lenAdaptor);
			if (mm <= numMM && ((float)mm/lenAdaptor) <= perMm && lenAdaptor > 4) {
				return i+1;
			}
		}
		logger.error("haven't find adaptor: "+seqIn+" "+seqAdaptor);
		return -1;
	}
	
	
	/**
	 * �����Ҳ�polyA
	 * @param seqIn
	 * @param numMM ��������
	 * @return
	 * ���ظ�tag�ĵ�һ������������ϵ�λ�ã���0��ʼ����
	 * Ҳ���Ǹ�polyAǰ���ж��ٸ����������ֱ����substring(0,return)����ȡ
	 */
	public static int trimPolyA(String seqIn, int numMM) {
		seqIn = seqIn.toUpperCase();
		char[] chrIn = seqIn.toCharArray(); int lenIn = seqIn.length();
		int numMismatch = 0; int numA = 0;
		int con = 0;//��¼�����ķ�A���ַ��м���
		for (int i = lenIn-1; i >= 0; i--) {
			numA++;
			if (chrIn[i] != 'A') {
				numMismatch++;
				con++;
			}
			else {
				con = 0;
			}
			if (numMismatch > numMM) {
				return i+con;//�������a�Ļ��ļӻ�ȥ
			}
		}
		System.out.println(seqIn);
		return -1;
	}
	
	
	
	
	
	
}
