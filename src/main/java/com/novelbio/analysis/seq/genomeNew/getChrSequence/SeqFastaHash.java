package com.novelbio.analysis.seq.genomeNew.getChrSequence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.chipseq.repeatMask.repeatRun;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;




/**
 * ������������ȡfasta�ı�������Hash��key-������-Сд��value-������Ϣ
 * ���������еĿո�ȫ����Ϊ�»���
 * һ�������һ��fasta�ļ�
 * ���ߣ��ڽ� 20090617
 */

public class SeqFastaHash extends SeqHashAbs {
	private static Logger logger = Logger.getLogger(SeqFastaHash.class);  

	
	public ArrayList<String> getLsSeqName() {
		return lsSeqName;
	}
	/**
	 * ��������Ϣ�����ϣ������<br>
	 * ��ϣ��ļ���������������������ı��Сд���ΪСд<br>
	 * ��ϣ���ֵ�����У������޿ո�<br>
	 */
	public HashMap<String,SeqFasta> hashSeq;
	
	/**
	 * ���������ư�˳�����list
	 */
	public ArrayList<String> lsSeqName;
	
	boolean append;
	public void setInfo(boolean append) {
		this.append = append;
	}

	/**
	 * ��ȡ�����ļ��������б�����Seqhash��ϣ��<br/>
	 * ��ȡ��Ϻ�����<br/>
	 * һ��listSeqName����������List<br/>
	 * һ��Seqhash��������--����HashTable<br/>
	 * ͬʱ����������һ��ͬ���Ĺ�ϣ��
	 * @param chrFile
	 * @param CaseChange �������Ƿ�Ҫ�ı��Сд,true����ΪСд��false���Ĵ�Сд
	 * @param regx ��Ҫ��ȡ��fasta��ʽ��������������ʽ��""Ϊȫ�����֡����ûץ������ȫ��������Ϊ������
	 * @param append ������ͬ�������еĴ���true����������������У����ڵڶ������ֺ����"<"��Ϊ���
	 * false����������������У����ó�������ȥ�滻�̵�����
	 * @return
	 * @throws Exception 
	 */
	protected void setChrFile() throws Exception
	{
		Pattern pattern = Pattern.compile(regx, Pattern.CASE_INSENSITIVE); // flags
		Matcher matcher;// matcher.groupCount() ���ش�ƥ����ģʽ�еĲ���������
		hashSeq = new HashMap<String, SeqFasta>();// ��list�����洢Ⱦɫ��
		TxtReadandWrite txtSeqFile = new TxtReadandWrite(chrFile,false);
		StringBuilder SeqStringBuilder = new StringBuilder();
		String content = "";
		BufferedReader reader = txtSeqFile.readfile();// open gff file
		SeqFasta Seq = null;
		lsSeqName = new ArrayList<String>();
		while ((content = reader.readLine()) != null) {
			if (content.trim().startsWith(">"))// ������һ������ʱ��������������
			{
				if (Seq != null) {
					putSeqFastaInHash(Seq, SeqStringBuilder.toString(), append);
					SeqStringBuilder = new StringBuilder();// ���
				}
				Seq = new SeqFasta();
				String tmpSeqName = "";
				// //////////////�Ƿ�ı��������ֵĴ�Сд//////////////////////////////////////////////
				if (CaseChange)
					tmpSeqName = content.trim().substring(1).trim()
							.toLowerCase().replace(" ", "_");// substring(1)��ȥ��>���ţ�Ȼ��ͳͳ�ĳ�Сд
				else
					tmpSeqName = content.trim().substring(1).trim().replace(" ", "_");// substring(1)��ȥ��>���ţ������Сд
				// ///////////////��������ʽץȡ�������е��ض��ַ�////////////////////////////////////////////////
				if (regx.trim().equals("")) {
					Seq.setSeqName(tmpSeqName);
				} else {
					matcher = pattern.matcher(tmpSeqName);
					if (matcher.find()) {
						Seq.setSeqName(matcher.group());
					} else {
						System.out.println("û�ҵ������е��ض����ƣ���ȫ�ƴ��� " + tmpSeqName);
						Seq.setSeqName(tmpSeqName);
					}
				}
				continue;
			}
			SeqStringBuilder.append(content.replace(" ", ""));
		}
		// /////////�뿪ѭ��������һ���ܽ�/////////////////////
		putSeqFastaInHash(Seq, SeqStringBuilder.toString(), append);
	}

	/**
	 *  ���û��ͬ�����У�ֱ��װ��hash��
	 *  ������ͬ�������еĴ���true����������������У����ڵڶ������ֺ����"<"��Ϊ���
	 *  ����������"<"ֱ��hash��û����������Ϊֹ��Ȼ��װ��hash��
	 * @param seqFasta
	 * @param seq
	 * @param append
	 */
	private void putSeqFastaInHash(SeqFasta seqFasta, String seq, boolean append) {
		seqFasta.setSeq(seq);
		SeqFasta tmpSeq = hashSeq.get(seqFasta.getSeqName());// ���Ƿ���ͬ�������г���
		// ���û��ͬ�����У�ֱ��װ��hash��
		if (tmpSeq == null) {
			hashSeq.put(seqFasta.getSeqName(), seqFasta);
			lsSeqName.add(seqFasta.getSeqName());
			hashChrLength.put(seqFasta.getSeqName(), (long) seq.length());
		} else {// ������ͬ�������еĴ���true����������������У����ڵڶ������ֺ����"<"��Ϊ���
			if (append)
			 { //����������"<"ֱ��hash��û����������Ϊֹ��Ȼ��װ��hash��
				 while (hashSeq.containsKey(seqFasta.getSeqName()))
				 {
					 seqFasta.setSeqName(seqFasta.getSeqName()+"<");
				 }
				 hashSeq.put(seqFasta.getSeqName(), seqFasta);
				 lsSeqName.add(seqFasta.getSeqName());
				 hashChrLength.put(seqFasta.getSeqName(), (long) seq.length());
			 }
			 else 
			 {
				if (tmpSeq.getSeq().length()<seqFasta.getSeq().length()) 
				{
					hashSeq.put(seqFasta.getSeqName(), seqFasta);
					hashChrLength.put(seqFasta.getSeqName(), (long) seq.length());
					//��Ϊ�Ѿ�����ͬ�������У����� lsSeqName �в���Ҫ����µ�����
				}
			}
		 }
	}
	/**
	 * ����������Ϣ��������,������
	 * ��������
	 * @param SeqID ��������
	 * @param chr ���в���֮�������������ڹ�ϣ���в��Ҿ���ĳ������
	 * @param cisseq���������򣬵������о���true
	 * ���û�������򷵻�null
	 */
	public String getSeqAll(String SeqID,boolean cisseq) 
	{
		if (hashSeq.containsKey(SeqID)) {
			if (cisseq) {
				return hashSeq.get(SeqID).getSeq().toLowerCase();
			} else {
				return hashSeq.get(SeqID).getSeqRC();
			}
		}
	   return null;
	}
	
	/**
	 * ����������
	 * �����������꣬�����յ�
	 * ��������
	 */
	protected String getSeqInfo(String seqID, long startlocation, long endlocation) throws IOException 
	{ 
		SeqFasta targetChr=hashSeq.get(seqID);
		if (targetChr == null) {
			logger.error("û�и����� " +seqID);
			return "û�и����� " +seqID;
		}
		return targetChr.getsequence((int)startlocation, (int)endlocation);
	}
	/**
	 * ����������
	 * �����������꣬�����յ�
	 * ��������
	 */
	public SeqFasta getSeqFasta(String seqID) 
	{ 
		return hashSeq.get(seqID);
	}
	/**
	 * ����ȫ������
	 */
	public ArrayList<SeqFasta>  getSeqFastaAll()
	{
		ArrayList<SeqFasta> lsresult = new ArrayList<SeqFasta>();
		for (SeqFasta seqFasta : hashSeq.values()) {
			lsresult.add(seqFasta);
		}
		return lsresult;
	}
	
	/**
	 * �Ƚ����������Ƿ�һ�£�������һ�µļ����
	 * ��ͷ��ʼ�Ƚϣ������пո�
	 */
	public static int compare2Seq(String seq1, String seq2) {
		char[] chrSeq1 = seq1.trim().toLowerCase().toCharArray();
		char[] chrSeq2 = seq2.trim().toLowerCase().toCharArray();
		int result = 0;
		int i = Math.min(chrSeq1.length, chrSeq2.length);
		for (int j = 0; j < i; j++) {
			if (chrSeq1[j] != chrSeq2[j]) {
				result ++ ;
			}
		}
		result = result + Math.max(chrSeq1.length, chrSeq2.length) - i;
		return result;
	}
	
	/**
	 * ��ָ�����ȵ�����д���ı�����Ҫ������lastz����,��׺��ͨͨ��Ϊ.fasta
	 * @param filePath д���ļ�·��
	 * @param prix �ļ�ǰ׺
	 * @param len seq�ĳ�������
	 * int[2] :0�����ޣ�С��0��ʾû������
	 * 1�����ޣ�С��0��ʾû������
	 * ���ޱ�����ڵ������ޣ��������С�����ޣ��򱨴�
	 * @param sepFile �Ƿ��Ϊ��ͬ�ļ�����
	 * @param writelen
	 */
	public void writeFileSep(String filePath, String prix, int[] len, boolean sepFile, int writelen)
	{
		filePath = FileOperate.addSep(filePath);
		TxtReadandWrite txtResultSeqName = new TxtReadandWrite(filePath + prix + "seqName.txt", true);
		TxtReadandWrite txtReadandWrite = null;
		if (!sepFile) {
			txtReadandWrite = new TxtReadandWrite(filePath + prix + ".fasta", true);
			txtResultSeqName.writefileln(txtReadandWrite.getFileName());
		}

		for (Entry<String, SeqFasta> entry : hashSeq.entrySet()) {
			String seqName = entry.getKey();
			SeqFasta seqFasta = entry.getValue();
			if (testSeqLen(seqFasta.getSeq().length(), len))//������Ŀ�귶Χ��
			{
				if (sepFile) {
					
					TxtReadandWrite txtReadandWrite2 = new TxtReadandWrite(filePath + prix + seqFasta.getSeqName().replace(" ", "_")+".fasta", true);
					txtReadandWrite2.writefileln(">"+seqFasta.getSeqName().trim().replace(" ", "_"));
					txtReadandWrite2.writefilePerLine(seqFasta.getSeq(), writelen);
					txtResultSeqName.writefileln(txtReadandWrite2.getFileName());
					txtReadandWrite2.close();
				}
				else {
					txtReadandWrite.writefileln(">"+seqFasta.getSeqName().trim().replace(" ", "_"));
					txtReadandWrite.writefilePerLine(seqFasta.getSeq(), writelen);
					txtReadandWrite.writefileln("");
				}
			}
		}
		if (!sepFile) {
			txtReadandWrite.close();
		}
		
		txtResultSeqName.close();
	}
	
	/**
	 * �ж�����ĳ����Ƿ���Ŀ�������ڣ�������
	 * @param seqlen 
	 * @param len
	 * 	int[2] :0�����ޣ�С��0��ʾû������
	 * 1�����ޣ�С��0��ʾû������
	 * ���ޱ�����ڵ������ޣ��������С�����ޣ��򱨴�
	 * @return
	 */
	public static boolean testSeqLen(int seqlen, int[] len) {
		
		if (len[1] > 0 && len[1] < len[0]) {
			logger.error("Ҫ��������еĳ������޲���С������");
		}
		
		if (len[0] <= 0) { //������
			if (len[1] <= 0)  //������
				return true;
			else { //������
				if (seqlen <= len[1])  //����С�ڵ�������
					return true;
				else
					// ���ȴ�������
					return false;
			}
		}
		else // ������
		{
			if (seqlen < len[0]) //����С������
				return false;
			else {  //���ȴ�������
				if (len[1] > 0) { //������
					if (seqlen <= len[1]) //����С������
						return true;
					else
						return false;
				}
				else {
					return true;
				}
			}
		}
	}
		
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}

