package com.novelbio.analysis.seq.genomeNew.getChrSequence;

import java.util.HashMap;

import org.apache.log4j.Logger;

/**
 * ����ר������װfasta�ļ��ľ�����Ϣ���ĳ���
 * ������Seqû�й�ϵ
 */
public class SeqFasta {
	private String SeqName;
	private String SeqSequence;
	private static Logger logger = Logger.getLogger(SeqFasta.class);  
	protected SeqFasta() {
		getCompMap();
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
	 * ��û������hash��
	 * ��������չ�ϣ��ֵ Ŀǰ��A-T�� G-C��N-N �Ķ�Ӧ��ϵ�������˴�Сд�Ķ�Ӧ�� ��������Ҫ����µ�
	 */
	public static HashMap<Character, Character> getCompMap() {
		if (compmap != null) {
			return compmap;
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
		compmap.put(Character.valueOf('-'), Character.valueOf('-'));
		compmap.put(Character.valueOf('\n'), Character.valueOf(' '));
		return compmap;
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
			getCompMap();
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

	/**
	 * �������У��������ձ� ��÷��򻥲�����
	 */
	public static String reservecom(String sequence) {
		if (compmap == null) {
			getCompMap();
		}
		StringBuilder recomseq = new StringBuilder();
		int length = sequence.length();
		Character base;
		for (int i = length - 1; i >= 0; i--) {
			base = compmap.get(sequence.charAt(i));
			if (base != null) {
				recomseq.append(compmap.get(sequence.charAt(i)));
			} else {
				return "��"+ i+ "λ����δ֪���: " + sequence.charAt(i);
			}
		}
		return recomseq.toString();
	}
	
}
