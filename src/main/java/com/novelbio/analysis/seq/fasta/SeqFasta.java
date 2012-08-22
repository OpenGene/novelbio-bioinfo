package com.novelbio.analysis.seq.fasta;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.reseq.SoapsnpInfo;
import com.novelbio.base.dataOperate.TxtReadandWrite;

/**
 * ����ר������װfasta�ļ��ľ�����Ϣ���ĳ���
 * ������Seqû�й�ϵ
 */
public class SeqFasta implements Cloneable {
	private static Logger logger = Logger.getLogger(SeqFasta.class);

	public static final int SEQ_UNKNOWN = 128;
	public static final int SEQ_PRO = 256;
	public static final int SEQ_DNA = 512;
	public static final int SEQ_RNA = 1024;

	/**
	 * ����ָ��������������滻�������е�λ��ʱ����������λ�ò����Ǻ�ȷ��
	 * Ʃ�����һ�����е� 10-20��ȥ�������Ƿ�ȷ���뵽10�����������ô�������ټ���һ��XXX���Ա��
	 */
	private static final String SEP_SEQ = "XXXXXXX";
	
	protected String SeqName;
	protected String SeqSequence = "";
	/**
	 * ������ļ��Ƿ�ת��Ϊ��Сд True��Сд False����д null������
	 * @return
	 */
	protected Boolean TOLOWCASE = null;
	/** Ĭ�Ϸ�������ĸ���ȵİ����� */
	boolean AA3Len = true;
	
	public SeqFasta() { }
	
	public SeqFasta(String seqName, String SeqSequence) {
		this.SeqName = seqName;
		this.SeqSequence = SeqSequence;
	}
	public SeqFasta(String SeqSequence) {
		this.SeqSequence = SeqSequence;
	}
	/**
	 * @param seqName
	 * @param SeqSequence
	 * @param cis5to3 �������һ�£������ᷴ������
	 */
	public SeqFasta(String seqName, String SeqSequence, boolean cis5to3) {
		this.SeqName = seqName;
		if (cis5to3) {
			this.SeqSequence = SeqSequence;
		}
		else {
			this.SeqSequence = reservecomplement(SeqSequence);
		}
	}
	
	/**
	 * Ĭ�Ϸ�������ĸ���ȵİ�����
	 */
	public void setAA3Len(boolean aA3Len) {
		AA3Len = aA3Len;
	}
	public void setTOLOWCASE(Boolean TOLOWCASE) {
		this.TOLOWCASE = TOLOWCASE;
	}
	/**
	 * ��RNA����ת��ΪDNA��Ҳ���ǽ�U�滻ΪT
	 */
	public void setDNA(boolean isDNAseq) {
		if (isDNAseq) {
			SeqSequence = SeqSequence.replace('u', 't').replace('U', 'T');
		}
	}
	/** �趨������ */
	public void setName(String SeqName) {
		 this.SeqName = SeqName;
	}
	/** ��������� */
	public String getSeqName() {
		return SeqName;
	}
	/** �趨���� */
	public void setSeq(String Seq) {
		 this.SeqSequence = Seq;
	}
	/**
	 * nr���еĳ���
	 * @return
	 */
	public int Length() {
		if (SeqSequence == null) {
			return 0;
		}
		return SeqSequence.length();
	}
	/**
	 * �������ҵ����꣬Ȼ��seqfasta�ض�
	 * @param start ��substringһ�����÷�
	 * @param end ��substringһ�����÷�
	 * @return ���ؽض̺��string
	 */
	public SeqFasta trimSeq(int start, int end) {
		SeqFasta seqFasta = new SeqFasta();
		seqFasta.AA3Len = AA3Len;
		seqFasta.SeqName = SeqName;
		seqFasta.TOLOWCASE = TOLOWCASE;
		if (SeqSequence == null) {
			seqFasta.SeqSequence = SeqSequence;
			return seqFasta;
		}
		seqFasta.SeqSequence = SeqSequence.substring(start, end);
		return seqFasta;
	}
	/**
	 * ��������������Ϣ��������-���������յ� ��������
	 * @param startlocation �������
	 * @param endlocation �����յ�
	 * @param cisseq���������򣬵������о���true
	 */
	public SeqFasta getSubSeq(int startlocation, int endlocation, boolean cisseq) {
		String sequence = getsequence(startlocation, endlocation);
		SeqFasta seqFasta = new SeqFasta(SeqName, sequence, cisseq);
		seqFasta.TOLOWCASE = TOLOWCASE;
		seqFasta.AA3Len = AA3Len;
		return seqFasta;
	}

	/**
	 * �����������꣬�����յ� ��������
	 * ���λ�㳬���˷�Χ����ô����λ��
	 */
	private String getsequence(int startlocation, int endlocation) {
		int length = SeqSequence.length();
		if (startlocation < 1 || startlocation > length || endlocation < 1
				|| endlocation > length) {
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
	 * �����򷵻�null;
	 */
	private String reservecomplement(String sequence) {
		String[] revSeq = CodeInfo.reservecomInfo(sequence);
		if (revSeq[1] != null) {
			logger.error(SeqName + " ����δ֪��� " + revSeq[1]);
		}
		return revSeq[0];
	}
	/** �������У��������ձ� ��÷��򻥲����� */
	public static String reservecom(String sequence) {
		return CodeInfo.reservecom(sequence);
	}
	/**
	 * �������У��������ձ� ��÷��򻥲�����
	 * ����SeqName���䣬cis5to3�������з��򻥲�
	 */
	public SeqFasta reservecom() {
		SeqFasta seqFasta = new SeqFasta();
		seqFasta.TOLOWCASE = TOLOWCASE;
		seqFasta.SeqName = SeqName;
		seqFasta.AA3Len = AA3Len;
		seqFasta.SeqSequence = reservecomplement(SeqSequence);
		return seqFasta;
	}
	/**
	 * ������
	 * ָ����Χ��Ȼ����ָ��������ȥ�滻ԭ��������
	 * @param start Ҫ�滻���е���㣬ʵ��λ��,���Ұ�����λ�� ���start<= 0���򲻿���end��ֱ�ӽ����в嵽��ǰ��
	 * ���start�����г����򲻿���end��ֱ�ӽ����в嵽�����
	 * @param end Ҫ�滻���е��յ㣬ʵ��λ��,���Ұ�����λ�㣬<br>
	 * ��� start == end ��ô���ǽ��õ��滻��ָ������<br>
	 * ��� start > end ˵���ǲ��������startλ��֮��<br>
	 * @param seq Ҫ�滻������
	 * @param boostart �滻���е�ǰ���Ƿ����XXX true������
	 * @param booend �滻���еĺ��Ƿ����XXX true������
	 */
	public void modifySeq(int start, int end, String seq, boolean boostart, boolean booend) {
		String startSeq = "";
		String endSeq = "";
		String FinalSeq = null;
		if (boostart)
			startSeq = SEP_SEQ;
		if (booend)
			endSeq = SEP_SEQ;

		if (start <= 0) {
			SeqSequence = startSeq + seq.toUpperCase() + endSeq + SeqSequence;
			return;
		}
		if (start >= SeqSequence.length() + 1) {
			SeqSequence = SeqSequence + startSeq + seq.toUpperCase() + endSeq;
			return;
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
		else if (start > end){
			end = start;
		}
		FinalSeq = SeqSequence.substring(0, start) + startSeq + seq.toUpperCase() + endSeq + SeqSequence.substring(end);
		SeqSequence = FinalSeq;
	}
	/** ָ��snpλ�㣬ʵ��λ�ã���1��ʼ��Ȼ����ָ��������ȥ�滻ԭ�������� */
	public void modifySeq(int snpSite, char replace) {
		snpSite--;
		char[] chrSeq = SeqSequence.toCharArray();
		chrSeq[snpSite] = replace;
		String FinalSeq = String.copyValueOf(chrSeq);
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
	
	/** ͳ��������Сд���У�N�������Լ�X�������� */
	public StatisticSeqInfo getSeqAssemblyInfo() {
		return new StatisticSeqInfo(this);
	}
	/**@return ��nr����ת��Ϊ����ĸaa���У�����������֮��Ȼ���ո�˳�����orfѡ�� */
	public String toStringAA() {
		return toStringAA(true, 0, true);
	}
	/**
	 * @param AAnum true ����ĸAA��false ����ĸAA
	 * @return ��nr����ת��Ϊaa���У�����������֮��Ȼ���ո�˳�����orfѡ�� 
	 */
	public String toStringAA(boolean AAnum) {
		return toStringAA(true, 0, AAnum);
	}
	/**
	 * ��nr����ת��Ϊ����ĸaa���У�����������֮��Ȼ���ո�˳�����orfѡ��
	 * @param cis ������ false�����򻥲�
	 * @param orf �ڼ���orf��0��1��2
	 * @return
	 */
	public String toStringAA(boolean cis,int orf) {
		return toStringAA(cis, orf, true);
	}
	/**
	 * ��nr����ת��Ϊaa���У�����������֮��Ȼ���ո�˳�����orfѡ��
	 * @param cis ������ false�����򻥲�
	 * @param orf �ڼ���orf��0��1��2
	 * @param AAnum true ����ĸAA��false ����ĸAA
	 * @return
	 */
	public String toStringAA(boolean cis,int orf, boolean AAnum) {
		if (SeqSequence == null) {
			return "";
		}
		char[] nrChar = null;
		if (!cis) {
			nrChar = reservecom(SeqSequence).toCharArray();
		}
		else {
			nrChar = SeqSequence.toCharArray();
		}
		StringBuilder resultAA = new StringBuilder();
		for (int i = orf; i <= nrChar.length - 3; i = i+3) {
			String tmp = String.valueOf(new char[]{nrChar[i],nrChar[i+1],nrChar[i+2]});
			resultAA.append(CodeInfo.convertDNACode2AA(tmp, AAnum));
		}
		return resultAA.toString();
	}
	/**
	 * ����moti����
	 * @return
	 */
	public SeqFastaMotifSearch getMotifScan() {
		return new SeqFastaMotifSearch(this);
	}

	/**
	 * �жϸ�������DNA��RNA�����ǵ��ף�����Ҳ��֪����ʲô
	 * @return
	 * SeqFasta.SEQ_DNA��
	 */
	public int getSeqType() {
		int len = 2000;
		if (len > Length()) {
			len = Length() - 1;
		}
		char[] chr = SeqSequence.substring(0, len).toCharArray();
		int num = 0;
		boolean flagFindU = false;
		for (char c : chr) {
			if (c == 'u' || c == 'U')
				flagFindU = true;
			
			if (CodeInfo.getCompMap().containsKey(c))
				continue;
			else
				num ++ ;
		}
		if (num == 0) {
			if (flagFindU) return SEQ_RNA;
			
			return SEQ_DNA;
		}
		else if ((double)num/Length() < 0.1) {
			return SEQ_UNKNOWN;
		}
		else {
			return SEQ_PRO;
		}
	}
	
	public FastaGetCDSFromProtein getCDSfromProtein(String proteinSeq) {
		return new FastaGetCDSFromProtein(this, proteinSeq);
	}
	/** ����TOLOWCASE�������� */
	public String toString() {
		if (SeqSequence == null) {
			return "";
		}
		if (TOLOWCASE == null) {
			return SeqSequence;
		}
		else {
			return TOLOWCASE.equals(true) ?  SeqSequence.toLowerCase() :  SeqSequence.toUpperCase();
		}
	}
	/** ����AA��fasta���� */
	public String toStringAAfasta() {
		return ">" + SeqName + TxtReadandWrite.ENTER_LINUX + toStringAA(true, 0);
	}
	/** ����Nr��fasta���� */
	public String toStringNRfasta() {
		return ">" + SeqName + TxtReadandWrite.ENTER_LINUX + toString();
	}
	/** ����Nr��fasta���� */
	public String toStringNRfasta(int basePerLine) {
		String result = ">" + SeqName;
		char[] tmpAll = toString().toCharArray();
		char[] tmpLines = new char[basePerLine];
		int m = 0;
		for (int i = 0; i < tmpAll.length; i++) {
			if (m >= basePerLine) {
				String tmpline = String.copyValueOf(tmpLines);
				result = result + TxtReadandWrite.ENTER_LINUX + tmpline;
				tmpLines = new char[basePerLine];
				m = 0;
			}
			tmpLines[m] = tmpAll[i];
			m++;
		}
		String tmpline = String.copyValueOf(tmpLines);
		result = result + TxtReadandWrite.ENTER_LINUX + tmpline;
		return result;
	}
	/** ��¡���� */
	public SeqFasta clone() {
		SeqFasta seqFasta = null;
		try {
			seqFasta = (SeqFasta) super.clone();
			seqFasta.SeqName = SeqName;
			seqFasta.AA3Len = AA3Len;
			seqFasta.SeqSequence = SeqSequence;
			seqFasta.TOLOWCASE = TOLOWCASE;
			return seqFasta;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return seqFasta;
	}
	/** �ҵ�һ�����������ת¼���ȵ� */
	public SeqfastaStatisticsCDS statisticsCDS() {
		SeqfastaStatisticsCDS seqfastaStatisticsCDS = new SeqfastaStatisticsCDS(this);
		return seqfastaStatisticsCDS;
	}
	////////////////////// static ���� //////////////////////////////////////
	/**
	 * ��������motif�󣬻��motif��titile
	 * @return
	 */
	public static String[] getMotifScanTitle() {
		String[] title = new String[]{"SeqName","Strand","MotifSeq","Distance2SeqEnd"};
		return title;
	}
	/**
	 * �Ƚ����������Ƿ�һ�£�������һ�µļ����
	 * ��ͷ��ʼ�Ƚϣ�ͷβ�����пո��м䲻���С�����blastģʽ�ıȽ�
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
	 * ��ð���������ԣ����ԣ���ɵȣ�����genedoc�ķ����׼
	 * @return
	 * string[3]: 0������--�����--����
	 * 1��
	 */
	public static String[] getAAquality(String AA) {
		return CodeInfo.getAAquality(AA);
	}
	/**
	 * ���������ڵ���ĸ������ĸ֮��ת��
	 */
	public static String convertAA(String AA) {
		return CodeInfo.convertAA(AA);
	}
	
	/**
	 * �������DNA����������
	 * �Ƚ�����������Ļ�ѧ���ʣ����ز���㣬����������
	 * Ʃ��������Բ�ͬ�ͷ��ؼ���
	 * ��ʽ polar --> nonpolar��
	 * ��һ���򷵻�"";
	 * @param DNAcode1 ��һ��DNA����
	 * @param DNAcode2 �ڶ���DNA����
	 * @return
	 */
	public static String cmpAAqualityDNA(String DNAcode1, String DNAcode2) {
		return CodeInfo.cmpAAqualityDNA(DNAcode1, DNAcode2);
	}
	
	/**
	 * ������ǰ����ᣬ����ν���ַ����ǵ��ַ�
	 * �Ƚ�����������Ļ�ѧ���ʣ����ز���㣬����������
	 * Ʃ��������Բ�ͬ�ͷ��ؼ���
	 * ��ʽ polar --> nonpolar��
	 * ��һ���򷵻�"";
	 */
	public static String cmpAAquality(String AA1, String AA2) {
		return CodeInfo.cmpAAquality(AA1, AA2);
	}
	
	/** ���ζ�ȡ��� */
	public Iterable<Character> readBase() {
		final char[] seq = toString().toCharArray();
		return new Iterable<Character>() {
			public Iterator<Character> iterator() {
				return new Iterator<Character>() {
					int index = 0;
					Character base = getBase();
					public boolean hasNext() {
						return base != null;
					}
					public Character next() {
						Character retval = base;
						base = getBase();
						return retval;
					}
					public void remove() {
						throw new UnsupportedOperationException();
					}
					Character getBase() {
						if (index >= seq.length) {
							return null;
						}
						Character base = seq[index];
						index++;
						return base;
					}
				};
			}
		};
	}
}
