package com.novelbio.analysis.seq;

import java.io.BufferedReader;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.novelbio.base.dataOperate.TxtReadandWrite;
/**
 * 读取测序文件的类。
 * 测序文件有固定的格式，与Fasta格式不同。
 * fasta格式只要开头有>即可，而本类读取的测序文件每行都要有固定的含义，
 * 也就是每条序列都有固定的行数
 * @author zong0jie
 *
 */
public abstract class SeqComb {
	TxtReadandWrite txtSeqFile = new TxtReadandWrite();
	String seqFile = "";
	int block = 1;
	
	/**
	 * fastQ文件里面的序列数量
	 */
	int seqNum = -1;
	
	private static Logger logger = Logger.getLogger(SeqComb.class);  
	/**
	 * 
	 * @param seqFile
	 * @param block 每个序列占几行，譬如fastQ文件每个序列占4行
	 */
	public SeqComb(String seqFile, int block) {
		this.seqFile = seqFile;
		this.block = block;
	}
	public String getSeqFile() {
		return seqFile;
	}
	/**
	 * 获得序列的数量，不管双端单端，都只返回一端的测序数量，也就是fragment的数量
	 * 如果返回小于0，说明出错
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
	 * 梯度提取序列Gradient
	 * @param block 
	 * @param percent 百分比，从 0-100
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
	 * 过滤右侧接头序列的方法，用循环搜索，容许错配，但是不能够过虑含有gap的adaptor。
	 * 算法，假设右侧最多只有一整个接头。那么先将接头直接对到右侧对齐，然后循环的将接头对到reads上去。
	 * @param seqIn 输入序列 无所谓大小写
	 * @param seqAdaptor 接头 无所谓大小写
	 * @param mapNum 第一次接头左端mapping到序列的第几个碱基上，从1开始记数，-1说明没找到
	 * @param numMM 最多容错几个mismatch
	 * @param perMm 最多容错百分比
	 * @return 返回该tag的第一个碱基在序列上的位置，从0开始记数
	 * 也就是该adaptor前面有多少个碱基，可以直接用substring(0,return)来截取
	 * -1说明没有adaptor
	 */
	public static int trimAdaptorR(String seqIn, String seqAdaptor, int mapNum, int numMM, float perMm) {
		mapNum--;
		seqIn = seqIn.toUpperCase();
		seqAdaptor = seqAdaptor.toUpperCase();
		char[] chrIn = seqIn.toCharArray(); int lenIn = seqIn.length();
		char[] chrAdaptor = seqAdaptor.toCharArray(); int lenA = seqAdaptor.length();
//		从左到右搜索chrIn
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
	 * 过滤左侧接头序列的方法，用循环搜索，容许错配，但是不能够过虑含有gap的adaptor。
	 * 算法，假设右侧最多只有一整个接头。那么先将接头直接对到右侧对齐，然后循环的将接头对到reads上去。
	 * @param seqIn 输入序列 无所谓大小写
	 * @param seqAdaptor 接头 无所谓大小写
	 * @param mapNum 第一次接头左端mapping到序列的第几个碱基上，从1开始记数，-1说明没找到
	 * @param numMM 最多容错几个mismatch
	 * @param perMm 最多容错百分比
	 * @return 返回该tag的最一个碱基在序列上的位置，从1开始记数
	 * 也就是该adaptor前面有多少个碱基，可以直接用substring(return)来截取
	 * -1说明没有adaptor
	 */
	public static int trimAdaptorL(String seqIn, String seqAdaptor, int mapNum, int numMM, float perMm) {
		mapNum--;
		seqIn = seqIn.toUpperCase();
		seqAdaptor = seqAdaptor.toUpperCase();
		char[] chrIn = seqIn.toCharArray(); int lenIn = seqIn.length();
		char[] chrAdaptor = seqAdaptor.toCharArray(); int lenA = seqAdaptor.length();
//		从右到左搜索chrIn
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
	 * 过滤右侧polyA
	 * @param seqIn
	 * @param numMM 几个错配
	 * @return
	 * 返回该tag的第一个碱基在序列上的位置，从0开始记数
	 * 也就是该polyA前面有多少个碱基，可以直接用substring(0,return)来截取
	 */
	public static int trimPolyA(String seqIn, int numMM) {
		seqIn = seqIn.toUpperCase();
		char[] chrIn = seqIn.toCharArray(); int lenIn = seqIn.length();
		int numMismatch = 0; int numA = 0;
		int con = 0;//记录连续的非A的字符有几个
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
				return i+con;//把最后不是a的还的加回去
			}
		}
		System.out.println(seqIn);
		return -1;
	}
	
	
	
	
	
	
}
