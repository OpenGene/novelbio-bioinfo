package com.novelbio.analysis.seq.genomeNew.getChrSequence;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.reseq.SoapsnpInfo;

/**
 * ����ר������װfasta�ļ��ľ�����Ϣ���ĳ���
 * ������Seqû�й�ϵ
 */
public class SeqFasta {
	private String SeqName;
	private String SeqSequence;
	private static Logger logger = Logger.getLogger(SeqFasta.class);
	private boolean cis5to3 = true;
	
	/**
	 * ����ָ��������������滻�������е�λ��ʱ����������λ�ò����Ǻ�ȷ��
	 * Ʃ�����һ�����е� 10-20��ȥ�������Ƿ�ȷ���뵽10�����������ô�������ټ���һ��XXX���Ա��
	 */
	private static final String SEP_SEQ = "XXXXXXX";
	
	public SeqFasta(String seqName, String SeqSequence)
	{
		getCompMap();
		this.SeqName = seqName;
		this.SeqSequence = SeqSequence;
	}
	public SeqFasta(String seqName, String SeqSequence, boolean cis5to3)
	{
		getCompMap();
		this.SeqName = seqName;
		
		this.cis5to3 = cis5to3;
		if (cis5to3) {
			this.SeqSequence = SeqSequence;
		}
		else {
			this.SeqSequence = reservecom(SeqSequence);
		}
	}
	
	protected SeqFasta() {
		getCompMap();
	}
	/**
	 * �����������ɵ�ʱ���������Ƿ���ʵ��û��Ӱ��
	 * @return
	 */
	public boolean isCis5to3() {
		return cis5to3;
	};
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
		compmap.put(Character.valueOf('a'), Character.valueOf('t'));
		compmap.put(Character.valueOf('T'), Character.valueOf('A'));
		compmap.put(Character.valueOf('t'), Character.valueOf('a'));
		compmap.put(Character.valueOf('G'), Character.valueOf('C'));
		compmap.put(Character.valueOf('g'), Character.valueOf('c'));
		compmap.put(Character.valueOf('C'), Character.valueOf('G'));
		compmap.put(Character.valueOf('c'), Character.valueOf('g'));
		compmap.put(Character.valueOf(' '), Character.valueOf(' '));
		compmap.put(Character.valueOf('N'), Character.valueOf('N'));
		compmap.put(Character.valueOf('n'), Character.valueOf('n'));
		compmap.put(Character.valueOf('-'), Character.valueOf('-'));
		compmap.put(Character.valueOf('\n'), Character.valueOf(' '));
		compmap.put(Character.valueOf('X'), Character.valueOf('X'));
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
	
	/**
	 * ������
	 * ָ����Χ��Ȼ����ָ��������ȥ�滻ԭ��������
	 * @param start Ҫ�滻���е���㣬ʵ��λ��,���Ұ�����λ��
	 * @param end Ҫ�滻���е��յ㣬ʵ��λ��,���Ұ�����λ�㣬<br>
	 * ���end<0��˵���ǲ��������startλ��֮��<br>
	 * ��� start == end ��ô���ǽ��õ��滻��ָ������<br>
	 * ��� start > end && end >0 ˵������
	 * @param seq Ҫ�滻������
	 * @param boostart �滻���е�ǰ���Ƿ�������
	 * @param booend �滻���еĺ��Ƿ�������
	 */
	public void modifySeq(int start, int end, String seq,boolean boostart, boolean booend) {
		String startSeq = "";
		String endSeq = "";
		if (!boostart) {
			startSeq = SEP_SEQ;
		}
		if (!booend) {
			endSeq = SEP_SEQ;
		}
		
		if (start < end) {
			start --;
		}
		else if (start == end) {
			if (seq.length() == 1) {
				modifySeq(start, seq.charAt(0));
				return;
			}
			start --;
			
		}
		else if (end < 0){
			end = start;
		}
		else if (start > end && end > 0) {//��������к���ˣ���������洦��
			logger.error("start < end: "+ start + " "+ end);
		}
		
		String FinalSeq = SeqSequence.substring(0, start) + startSeq + seq.toUpperCase() + endSeq + SeqSequence.substring(end);
		SeqSequence = FinalSeq;
	}
	
	/**
	 * ָ��snpλ�㣬ʵ��λ�ã���1��ʼ��Ȼ����ָ��������ȥ�滻ԭ��������
	 */
	public void modifySeq(int snpSite, char replace) {
		snpSite--;
		char[] chrSeq = SeqSequence.toCharArray();
		chrSeq[snpSite] = replace;
		String FinalSeq = chrSeq.toString();
		SeqSequence = FinalSeq;
	}
	
	/**
	 * ָ��snpλ�㣬ʵ��λ�ã���1��ʼ��Ȼ����ָ��������ȥ�滻ԭ��������
	 */
	public void modifySeq(ArrayList<SoapsnpInfo> lsSoapsnpInfos) {
		char[] chrSeq = SeqSequence.toCharArray();
		for (SoapsnpInfo soapsnpInfo : lsSoapsnpInfos) {
			chrSeq[soapsnpInfo.getStart()-1] = soapsnpInfo.getBestBase();
		}
		String FinalSeq = String.copyValueOf(chrSeq);
		SeqSequence = FinalSeq;
	}
	/**
	 * ֱ�ӷ�������
	 */
	public String toString()
	{
		return SeqSequence;
		
	}
	/**
	 * ͳ��������Сд���У�N�������Լ�X��������
	 */
	public ArrayList<LocInfo> getSeqInfo()
	{
		//string0: flag string1: location string2:endLoc
		ArrayList<LocInfo> lsResult = new ArrayList<LocInfo>();
		
		
		char[] seq = SeqSequence.toCharArray();
		boolean flagBound = false; //�߽�ģ����ǣ�XX
		boolean flagGap = false; //gap��ǣ�Сд
		boolean flagAmbitious = false; //��ȷ�������ǣ�NNN
		int bound = 0; int gap = 0; int ambitious = 0;
		int startBound = 0; int startGap = 0; int startAmbitious = 0;
		for (int i = 0; i < seq.length; i++) {
			if (seq[i] < 'a' && seq[i] != 'X' && seq[i] != 'N') {
				if (flagAmbitious) {
					addList(lsResult, "ambitious", startAmbitious, ambitious);
					flagAmbitious = false; //��ȷ�������ǣ�NNN
				}
				if (flagGap) {
					addList(lsResult, "gap", startGap, gap);
					flagGap = false; //gap��ǣ�Сд
				}
				if (flagBound) {
					addList(lsResult, "bound", startBound, bound);
					flagBound = false; //�߽�ģ����ǣ�XX
				}
			}
			else if (seq[i] == 'X' ) {
				if (flagAmbitious) {
					addList(lsResult, "ambitious", startAmbitious, ambitious);
					flagAmbitious = false; //��ȷ�������ǣ�NNN
				}
				if (flagGap) {
					addList(lsResult, "gap", startGap, gap);
					flagGap = false; //gap��ǣ�Сд
				}
				if (flagBound) {
					bound ++;
				}
				else {
					flagBound = true;
					bound = 0;
					startBound = i;
				}
			} 
			else if (seq[i] == 'N') {
				if (flagAmbitious) {
					ambitious ++;
				}
				else {
					flagAmbitious = true;
					ambitious = 0;
					startAmbitious = i;
				}
				if (flagGap) {
					addList(lsResult, "gap", startGap, gap);
					flagGap = false; // gap��ǣ�Сд
				}
				if (flagBound) {
					addList(lsResult, "bound",startBound, bound);
					flagBound = false; // �߽�ģ����ǣ�XX
				}
			}
			else if (seq[i] >= 'a') {
				System.out.println("i");
				if (flagAmbitious) {
					addList(lsResult, "ambitious", startAmbitious, ambitious);
					flagAmbitious = false; //��ȷ�������ǣ�NNN
				}
				if (flagGap) {
					gap ++;
				}
				else {
					flagGap = true;
					gap = 0;
					startGap = i;
				}
				if (flagBound) {
					addList(lsResult, "bound", startBound, bound);
					flagBound = false; //�߽�ģ����ǣ�XX
				}
			}
		}
		return lsResult;
	}
	/**
	 * 
	 * @param lsInfo
	 * @param info
	 * @param start �ڲ������1
	 * @param length
	 */
	private void addList(ArrayList<LocInfo> lsInfo, String info, int start, int length) {
		LocInfo locInfo = new LocInfo(info, "", start, start+length-1, true);
		lsInfo.add(locInfo);
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
	
	
	
	
	
	
}
