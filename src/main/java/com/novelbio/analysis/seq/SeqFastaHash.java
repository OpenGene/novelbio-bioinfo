package com.novelbio.analysis.seq;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.novelbio.base.dataOperate.TxtReadandWrite;




/**
 * ������������ȡfasta�ı�������Hash��key-������-Сд��value-������Ϣ
 * һ�������һ��fasta�ļ�
 * ���ߣ��ڽ� 20090617
 */

public class SeqFastaHash {
	
	private static Logger logger = Logger.getLogger(SeqFastaHash.class);  
	
	/**
	 * ��������Ϣ�����ϣ������
	 * ��ϣ��ļ���������������������ı��Сд���ΪСд
	 * ��ϣ���ֵ�����У������޿ո�
	 */
	public Hashtable<String,SeqFasta> hashSeq;
	
	/**
	 * ���������ư�˳�����list
	 */
	public ArrayList<String> lsSeqName;
	
	/**
	 * ��ȡ�����ļ��������б�����Seqhash��ϣ��<br/>
	 * ��ȡ��Ϻ�����<br/>
	 * һ��listSeqName����������List<br/>
	 * һ��Seqhash��������--����HashTable<br/>
	 * ͬʱ����������һ��ͬ���Ĺ�ϣ��
	 * @param seqfilename
	 * @param CaseChange �������Ƿ�Ҫ�ı��Сд,true����ΪСд��false���Ĵ�Сд
	 * @param regx ��Ҫ��ȡ��fasta��ʽ��������������ʽ��""Ϊȫ�����֡����ûץ������ȫ��������Ϊ������
	 * @param append ������ͬ�������еĴ���true����������������У����ڵڶ������ֺ����"<"��Ϊ���
	 * false����������������У����ó�������ȥ�滻�̵�����
	 * @return
	 * @throws Exception 
	 */
	public Hashtable<String,SeqFasta> readfile(String seqfilename,boolean CaseChange, String regx,boolean append) throws Exception
 {
		Pattern pattern = Pattern.compile(regx, Pattern.CASE_INSENSITIVE); // flags
		Matcher matcher;// matcher.groupCount() ���ش�ƥ����ģʽ�еĲ���������
		hashSeq = new Hashtable<String, SeqFasta>();// ��list�����洢Ⱦɫ��
		TxtReadandWrite txtSeqFile = new TxtReadandWrite();
		txtSeqFile.setParameter(seqfilename, false, true);
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
							.toLowerCase();// substring(1)��ȥ��>���ţ�Ȼ��ͳͳ�ĳ�Сд
				else
					tmpSeqName = content.trim().substring(1).trim();// substring(1)��ȥ��>���ţ������Сд
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
		Seq.setSeq(SeqStringBuilder.toString());
		putSeqFastaInHash(Seq, SeqStringBuilder.toString(), append);
		return hashSeq;
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
		} else {// ������ͬ�������еĴ���true����������������У����ڵڶ������ֺ����"<"��Ϊ���
			if (append)
			 { //����������"<"ֱ��hash��û����������Ϊֹ��Ȼ��װ��hash��
				 while (hashSeq.containsKey(seqFasta.getSeqName()))
				 {
					 seqFasta.setSeqName(seqFasta.getSeqName()+"<");
				 }
				 hashSeq.put(seqFasta.getSeqName(), seqFasta);
				 lsSeqName.add(seqFasta.getSeqName());
			 }
			 else 
			 {
				if (tmpSeq.getSeq().length()<seqFasta.getSeq().length()) 
				{
					hashSeq.put(seqFasta.getSeqName(), seqFasta);
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
	 */
	public String getsequence(String SeqID,boolean cisseq) 
	{
	    if (cisseq)
	    {
	    	return hashSeq.get(SeqID).getSeq();
	    }
	    else 
	    {
		  return hashSeq.get(SeqID).getSeqRC();	
		}
	}
	
	/**
	 * ��������������Ϣ��������-���������յ�
	 * ��������
	 * @param hashSeq ���еĹ�ϣ����Ϊ�������ƣ�ֵΪ��������
	 * @param chr ���в���֮�������������ڹ�ϣ���в��Ҿ���ĳ������
	 * @param startlocation �������
	 * @param endlocation �����յ�
	 * @param cisseq���������򣬵������о���true
	 */
	public String getsequence(String SeqID, int startlocation, int endlocation,boolean cisseq) 
	{
		SeqFasta targetChr=hashSeq.get(SeqID);
		if (targetChr == null) {
			logger.error("û�и����� " +SeqID);
			return "û�и����� " +SeqID;
		}
		return targetChr.getsequence(startlocation, endlocation, cisseq);
	}

	/**
	 * ����������
	 * �����������꣬�����յ�
	 * ��������
	 */
	public String getsequence(String seqID, int startlocation, int endlocation) 
	{ 
		SeqFasta targetChr=hashSeq.get(seqID);
		if (targetChr == null) {
			logger.error("û�и����� " +seqID);
			return "û�и����� " +seqID;
		}
		return targetChr.getsequence(startlocation, endlocation);
	}
}

/**
 * ����ר������װfasta�ļ��ľ�����Ϣ���ĳ���
 * ������Seqû�й�ϵ
 */
class SeqFasta {
	private String SeqName;
	private String SeqSequence;
	private static Logger logger = Logger.getLogger(SeqFasta.class);  
	protected SeqFasta() {
		SetCompMap();
	}
	/**
	 * ��û������hash��
	 */
	private HashMap<Character,Character> getCompMap() {
		SetCompMap();
		return compmap;
	}
	
	/**
	 * �趨������
	 */
	public void setSeqName(String SeqName) {
		 this.SeqName = SeqName;
	}
	/**
	 * ���������
	 */
	public String getSeqName() {
		return SeqName;
	}
	
	/**
	 * �趨����
	 */
	protected void setSeq(String Seq) {
		 this.SeqSequence = Seq;
	}
	
	/**
	 * ��þ�������
	 */
	public String getSeq() {
		return SeqSequence;
	}
	/**
	 * ��þ������еķ��򻥲�����
	 */
	public String getSeqRC() {
		return reservecomplement(getSeq());
	}
	/**
	 * ���򻥲���ϣ��
	 */
	private static HashMap<Character, Character> compmap = null;// ��������ϣ��

	/**
	 * ��������չ�ϣ��ֵ Ŀǰ��A-T�� G-C��N-N �Ķ�Ӧ��ϵ�������˴�Сд�Ķ�Ӧ�� ��������Ҫ����µ�
	 */
	private static void SetCompMap() {
		if (compmap != null) {
			return;
		}
		compmap = new HashMap<Character, Character>();// ��������ϣ��
		compmap.put(Character.valueOf('A'), Character.valueOf('T'));
		compmap.put(Character.valueOf('a'), Character.valueOf('T'));
		compmap.put(Character.valueOf('T'), Character.valueOf('A'));
		compmap.put(Character.valueOf('t'), Character.valueOf('A'));
		compmap.put(Character.valueOf('G'), Character.valueOf('C'));
		compmap.put(Character.valueOf('g'), Character.valueOf('C'));
		compmap.put(Character.valueOf('C'), Character.valueOf('G'));
		compmap.put(Character.valueOf('c'), Character.valueOf('G'));
		compmap.put(Character.valueOf(' '), Character.valueOf(' '));
		compmap.put(Character.valueOf('N'), Character.valueOf('N'));
		compmap.put(Character.valueOf('n'), Character.valueOf('N'));
	}



	/**
	 * ��������������Ϣ��������-���������յ� ��������
	 * 
	 * @param hashSeq
	 *            ���еĹ�ϣ����Ϊ�������ƣ�ֵΪ��������
	 * @param chr
	 *            ���в���֮�������������ڹ�ϣ���в��Ҿ���ĳ������
	 * @param startlocation
	 *            �������
	 * @param endlocation
	 *            �����յ�
	 * @param cisseq����������
	 *            ���������о���true
	 */
	public String getsequence(int startlocation,
			int endlocation, boolean cisseq) {
		String sequence = getsequence(startlocation, endlocation);
		if (cisseq) {
			return sequence;
		} else {
			return reservecomplement(sequence);
		}
	}

	/**
	 * �����������꣬�����յ� ��������
	 */
	public String getsequence(int startlocation, int endlocation) {
		/**
		 * ���λ�㳬���˷�Χ����ô����λ��
		 */
		int length = SeqSequence.length();
		if (startlocation < 1 || startlocation >= length || endlocation < 1
				|| endlocation >= length) {
			logger.error("����������� "+SeqName+" "+startlocation+" "+endlocation);
			return "����������� "+SeqName+" "+startlocation+" "+endlocation;
		}

		if (endlocation <= startlocation) {
			logger.error("����������� "+SeqName+" "+startlocation+" "+endlocation);
			return "����������� "+SeqName+" "+startlocation+" "+endlocation;
		}
		
		if (endlocation - startlocation > 20000) {
			logger.error("�����ȡ20000bp "+SeqName+" "+startlocation+" "+endlocation);
			return "�����ȡ20000bp"+SeqName+" "+startlocation+" "+endlocation;
		}
		return SeqSequence.substring(startlocation - 1, endlocation);// substring���������ҵ�������
	}

	/**
	 * �������У��������ձ� ��÷��򻥲�����
	 */
	private String reservecomplement(String sequence) {
		if (compmap == null) {
			SetCompMap();
		}

		StringBuilder recomseq = new StringBuilder();
		int length = sequence.length();
		Character base;
		for (int i = length - 1; i >= 0; i--) {
			base = compmap.get(sequence.charAt(i));
			if (base != null) {
				recomseq.append(compmap.get(sequence.charAt(i)));
			} else {
				logger.error(SeqName + " ����δ֪��� " + sequence.charAt(i));
				return SeqName + "����δ֪��� " + sequence.charAt(i);
			}
		}
		return recomseq.toString();
	}

}



