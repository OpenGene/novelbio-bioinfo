package com.novelbio.analysis.seq.fasta;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.fastq.FastQRecord;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.PatternOperate;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * ������������ȡfasta�ı�������Hash��key-������-Сд��value-������Ϣ
 * ���������еĿո�ȫ����Ϊ�»���
 * һ�������һ��fasta�ļ�
 * ���ߣ��ڽ� 20090617
 */

public class SeqFastaHash extends SeqHashAbs {
	private static Logger logger = Logger.getLogger(SeqFastaHash.class);  
	Boolean TOLOWCASE = null;
	/**
	 * ��������Ϣ�����ϣ������<br>
	 * ��ϣ��ļ���������������������ı��Сд���ΪСд<br>
	 * ��ϣ���ֵ�����У������޿ո�<br>
	 */
	public HashMap<String,SeqFasta> hashSeq;
	
	boolean append = false;
	/**
	 * @param chrFile
	 * @param regx ��������������ʽ��null���趨
	 * @param CaseChange �Ƿ���������ΪСд��Ĭ��Ϊtrue
	 * @param append ������ͬ�������еĴ���true����������������У����ڵڶ������ֺ����"<"��Ϊ���
	 * false����������������У����ó�������ȥ�滻�̵����У�Ĭ��Ϊfalse
	 */
	public SeqFastaHash(String chrFile) {
		super(chrFile, "", true);
		setFile();
	}
	/**
	 * @param chrFile
	 * @param regx ��������������ʽ��null���趨
	 * @param CaseChange �Ƿ���������ΪСд��Ĭ��Ϊtrue
	 * @param append ������ͬ�������еĴ���true����������������У����ڵڶ������ֺ����"<"��Ϊ���
	 * false����������������У����ó�������ȥ�滻�̵����У�Ĭ��Ϊfalse
	 */
	public SeqFastaHash(String chrFile, String regx, boolean CaseChange,
			boolean append) {
		super(chrFile, regx, CaseChange);
		this.append = append;
		setFile();
	}
	/**
	 * @param chrFile
	 * @param regx ��������������ʽ��null���趨
	 * @param CaseChange �Ƿ���������ΪСд��Ĭ��Ϊtrue
	 * @param append ������ͬ�������еĴ���true����������������У����ڵڶ������ֺ����"<"��Ϊ���
	 * @param TOLOWCASE  �Ƿ�����ת��ΪСд True��Сд��False����д��null���� Ĭ��Ϊnull
	 * false����������������У����ó�������ȥ�滻�̵����У�Ĭ��Ϊfalse
	 */
	public SeqFastaHash(String chrFile, String regx, boolean CaseChange,
			boolean append,Boolean TOLOWCASE) {
		super(chrFile, regx, CaseChange);
		this.append = append;
		this.TOLOWCASE = TOLOWCASE;
		setFile();
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
	protected void setChrFile() throws Exception {
		Pattern pattern = null;
		if (regx == null) {
			pattern = Pattern.compile("", Pattern.CASE_INSENSITIVE); // flags
		}
		else {
			pattern = Pattern.compile(regx, Pattern.CASE_INSENSITIVE); // flags
		}
		
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
				tmpSeqName = content.trim().substring(1).trim();
				tmpSeqName = getChrIDisLowCase(tmpSeqName);
				
				// ///////////////��������ʽץȡ�������е��ض��ַ�////////////////////////////////////////////////
				if (regx == null || regx.trim().equals("")) {
					Seq.setName(tmpSeqName);
				} else {
					matcher = pattern.matcher(tmpSeqName);
					if (matcher.find()) {
						Seq.setName(matcher.group());
					} else {
						System.out.println("û�ҵ������е��ض����ƣ���ȫ�ƴ��� " + tmpSeqName);
						Seq.setName(tmpSeqName);
					}
				}
				continue;
			}
			SeqStringBuilder.append(content.replace(" ", ""));
		}
		// /////////�뿪ѭ��������һ���ܽ�/////////////////////
		putSeqFastaInHash(Seq, SeqStringBuilder.toString(), append);
		txtSeqFile.close();
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
		if (TOLOWCASE != null) {
			seq = (TOLOWCASE == true ? seq.toLowerCase() : seq.toUpperCase());
		}
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
				 while (hashSeq.containsKey(seqFasta.getSeqName())) {
					 seqFasta.setName(seqFasta.getSeqName()+"<");
				 }
				 hashSeq.put(seqFasta.getSeqName(), seqFasta);
				 lsSeqName.add(seqFasta.getSeqName());
				 hashChrLength.put(seqFasta.getSeqName(), (long) seq.length());
			 }
			 else {
				if (tmpSeq.Length()<seqFasta.Length()) 
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
	public String getSeqAll(String SeqID,boolean cisseq) {
		SeqID = getChrIDisLowCase(SeqID);
		if (hashSeq.containsKey(SeqID)) {
			if (cisseq) {
				return hashSeq.get(SeqID).toString();
			} else {
				return hashSeq.get(SeqID).reservecom().toString();
			}
		}
	   return null;
	}
	/**
	 * ����������
	 * �����������꣬�����յ�
	 * ��������
	 */
	protected SeqFasta getSeqInfo(String seqID, long startlocation, long endlocation) throws IOException {
		SeqFasta targetChr=hashSeq.get(seqID);
		if (targetChr == null) {
			logger.error("û�и����� " +seqID);
			return null;
		}
		return targetChr.getSubSeq((int)startlocation, (int)endlocation, true);
	}
	/**
	 * �������������Զ�ת��ΪСд
	 * �����������꣬�����յ�
	 * ��������
	 */
	public SeqFasta getSeqFasta(String seqID) {
		seqID = getChrIDisLowCase(seqID);
		SeqFasta seqFasta = hashSeq.get(seqID);
		if (seqFasta == null) {
			logger.error("û�и�ID��" + seqID);
			return null;
		}
		seqFasta.setDNA(isDNAseq);
		return seqFasta;
	}
	/**
	 * ����ȫ������
	 */
	public ArrayList<SeqFasta>  getSeqFastaAll() {
		ArrayList<SeqFasta> lsresult = new ArrayList<SeqFasta>();
		for (SeqFasta seqFasta : hashSeq.values()) {
			seqFasta.setDNA(isDNAseq);
			lsresult.add(seqFasta);
		}
		return lsresult;
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
	public void writeFileSep(String filePath, String prix, int[] len, boolean sepFile, int writelen) {
		filePath = FileOperate.addSep(filePath);
		TxtReadandWrite txtResultSeqName = new TxtReadandWrite(filePath + prix + "seqName.txt", true);
		TxtReadandWrite txtReadandWrite = null;
		if (!sepFile) {
			txtReadandWrite = new TxtReadandWrite(filePath + prix + ".fasta", true);
			txtResultSeqName.writefileln(txtReadandWrite.getFileName());
		}
		for (Entry<String, SeqFasta> entry : hashSeq.entrySet()) {
			SeqFasta seqFasta = entry.getValue();
			if (SeqHash.testSeqLen(seqFasta.toString().length(), len)) {//������Ŀ�귶Χ��
				if (sepFile) {
					TxtReadandWrite txtReadandWrite2 = new TxtReadandWrite(filePath + prix + seqFasta.getSeqName().replace(" ", "_")+".fasta", true);
					txtReadandWrite2.writefileln(">"+seqFasta.getSeqName().trim().replace(" ", "_"));
					txtReadandWrite2.writefilePerLine(seqFasta.toString(), writelen);
					txtResultSeqName.writefileln(txtReadandWrite2.getFileName());
					txtReadandWrite2.close();
				}
				else {
					txtReadandWrite.writefileln(">"+seqFasta.getSeqName().trim().replace(" ", "_"));
					txtReadandWrite.writefilePerLine(seqFasta.toString(), writelen);
					txtReadandWrite.writefileln("");
				}
			}
		}
		if (!sepFile) {
			txtReadandWrite.close();
		}
		txtResultSeqName.close();
	}
	
	public void writeToFile(String seqOut) {
		ArrayList<SeqFasta> lsFasta = getSeqFastaAll();
		TxtReadandWrite txtOut = new TxtReadandWrite(seqOut, true);
		for (SeqFasta seqFasta : lsFasta) {
			txtOut.writefileln(seqFasta.toStringNRfasta(50));
		}
		txtOut.close();
	}
	/**
	 * ��<b>������</b>���и�������ʽ������д���ļ�<br>
	 * ����д��������ʽ
	 * @param regx
	 * @param seqOut
	 */
	public void writeToFile(String regx, String seqOut) {
		PatternOperate patternOperate = new PatternOperate(regx, false);
		ArrayList<SeqFasta> lsFasta = getSeqFastaAll();
		TxtReadandWrite txtOut = new TxtReadandWrite(seqOut, true);
		for (SeqFasta seqFasta : lsFasta) {
			ArrayList<String> lsName = patternOperate.getPat(seqFasta.getSeqName());
			if (lsName != null && lsName.size() > 0) {
				txtOut.writefileln(seqFasta.toStringNRfasta());
			}
		}
		txtOut.close();
	}
	/**
	 * ��<b>������</b>���и�������ʽ������д���ļ�<br>
	 * ����д��������ʽ
	 * ����������Ϊ��hsa-mir-101-1 MI0000103 Homo sapiens miR-101-1 stem-loop <br>
	 * regSearch = Homo sapiens<br>
	 * regWrite = hsa-mir-101-1<br>
	 * ���ͻ���hsa-mir-101-1<br>
	 * @param regxSearch �ø�������ʽ����������
	 * @param regxWrite �ҵ���������������Ϊ��������ʽץ������Ϣ
	 * @param seqOut
	 */
	public void writeToFile(String regxSearch, String regxWrite, String seqOut) {
		PatternOperate patSearch = new PatternOperate(regxSearch, false);
		PatternOperate patWrite = new PatternOperate(regxWrite, false);

		ArrayList<SeqFasta> lsFasta = getSeqFastaAll();
		TxtReadandWrite txtOut = new TxtReadandWrite(seqOut, true);
		for (SeqFasta seqFasta : lsFasta) {
			ArrayList<String> lsName = patSearch.getPat(seqFasta.getSeqName());
			if (lsName != null && lsName.size() > 0) {
				SeqFasta seqFastaNew = seqFasta.clone();
				String name = patWrite.getPatFirst(seqFasta.getSeqName());
				if (name != null && name.equals("")) {
					seqFastaNew.setName(name);
				}
				txtOut.writefileln(seqFasta.toStringNRfasta());
			}
		}
		txtOut.close();
	}
	/**
	 * ��<b>��������sep�ָ�</b>Ȼ�󽫵ڼ�λ������д���ļ�<br>
	 * ����д��������ʽ
	 * @param sep �ָ��� 
	 * @param num �ڼ�λ���ı�
	 */
	public void writeToFile(String sep, int num, String seqOut) {
		num--;
		ArrayList<SeqFasta> lsFasta = getSeqFastaAll();
		TxtReadandWrite txtOut = new TxtReadandWrite(seqOut, true);
		for (SeqFasta seqFasta : lsFasta) {
			SeqFasta seqFastaOut = seqFasta.clone();
			String name = seqFasta.getSeqName().split(sep)[num];
			seqFastaOut.setName(name);
			seqFastaOut.setDNA(true);
			txtOut.writefileln(seqFastaOut.toStringNRfasta());
		}
		txtOut.close();
	}
	@Override
	public Iterable<Character> readBase(String refID) {
		refID = getChrIDisLowCase(refID);
		SeqFasta seqFasta = hashSeq.get(refID);
		return seqFasta.readBase();
	}
}

