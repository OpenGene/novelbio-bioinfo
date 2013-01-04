package com.novelbio.analysis.seq.fastq;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.blastZJ.BlastSeqFasta;
import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.base.dataOperate.TxtReadandWrite;

public class FastQRecord implements Cloneable {
	private static Logger logger = Logger.getLogger(FastQRecord.class);
	/** ��һfastqû�����֣��͸�������Ӹ����� */
	static String SEQNAME = "Novelbio";
	static long i = 0;
	
	/** fastQ����asc||���ָ������� */
	HashMap<Integer, Integer> mapFastQFilter;
	
	/** ������ı����������ڳ�ʼ�� */
	String fastqStringReadIn;

	private SeqFasta seqFasta = new SeqFasta();
	protected int fastqOffset = FastQ.FASTQ_SANGER_OFFSET;
	protected String seqQuality = "";
	/** �����������ƣ����ڸ�������˵������¼������ */
	protected int quality = FastQ.QUALITY_MIDIAN;
	/** �ü�����ʱ���Ϊ���� */
	private int readsMinLen = 22;
	
	
	
	public FastQRecord() {
		seqFasta = new SeqFasta();
		seqFasta.setTOLOWCASE(null);
	}
	/**
	 * ÿ����һ����¼������������linux�س�������Ȼ������
	 * @param fastqlines
	 */
	public FastQRecord(String fastqlines) {
		this(fastqlines, true);
	}
	/** ����fastq�ļ���������Ҫ���г�ʼ��
	 * ����fastq���˵�ʱ�򣬿����Ȳ���ʼ����Ȼ���ڶ��̵߳�ʱ����г�ʼ��
	 *  */
	protected FastQRecord(String fastqlines, boolean initial) {
		fastqStringReadIn = fastqlines;
		if (initial) {
			initialReadRecord();
		}
	}
	/**��ʼ����������� */
	protected void initialReadRecord() {
		if (seqFasta.getSeqName() != null) {
			return;
		}
		String[] ss = fastqStringReadIn.split(TxtReadandWrite.ENTER_LINUX);
		if (ss.length == 1) {
			ss = fastqStringReadIn.split(TxtReadandWrite.ENTER_WINDOWS);
		}
		String seqName = ss[0].substring(1).trim();
		if (seqName == null || seqName.equals("")) {
			seqName = SEQNAME + i;
			i ++;
		}
		seqFasta.setName(seqName);
		seqFasta.setSeq(ss[1]);
		setFastaQuality(ss[3]);
	}
	
	protected void setMapFastqFilter(HashMap<Integer, Integer> mapFastQFilter) {
		this.mapFastQFilter = mapFastQFilter;
	}
	public void setName(String SeqName) {
		seqFasta.setName(SeqName);
	}
	public void setSeq(String Seq) {
		seqFasta.setSeq(Seq);
	}
	public SeqFasta getSeqFasta() {
		return seqFasta;
	}

	
	/** �ü�����ʱ���Ϊ���٣� Ĭ��Ϊ22
	 */
	public void setTrimMinLen(int trimMinLen) {
		this.readsMinLen = trimMinLen;
	}
	/**
	 * �趨���������ַ�������phred��ʽ�趨
	 * @param fastaQuality
	 */
	public void setFastaQuality(String fastaQuality) {
		this.seqQuality = fastaQuality;
	}
	public String getSeqQuality() {
		return seqQuality;
	}
	public int getLength() {
		return seqFasta.Length();
	}
	/**
	 * �趨ƫ��
	 * FASTQ_SANGER_OFFSET
	 * @param fastqOffset
	 */
	public void setFastqOffset(int fastqOffset) {
		this.fastqOffset = fastqOffset;
	}
	//////////////////////// ���˵����������� ///////////////////////////////////////////
	/**
	 * ע���������µ�adaptor�޷�����
	 * �����Ҳ��ͷ���еķ�������ѭ��������������䣬���ǲ��ܹ����Ǻ���gap��adaptor��
	 * �㷨�������Ҳ����ֻ��һ������ͷ����ô�Ƚ���ͷֱ�ӶԵ��Ҳ���룬Ȼ��ѭ���Ľ���ͷ�Ե�reads��ȥ��
	 * @param seqAdaptorL ��˽�ͷ ����ν��Сд ��ͷ����ֻдһ���� null��""��ʾ���ù��˸ý�ͷ
	 * @param seqAdaptorR �Ҷ˽�ͷ ����ν��Сд ��ͷ����ֻдһ���� null��""��ʾ���ù��˸ý�ͷ
	 * @param mapNumLeft ��һ�ν�ͷ��˻��Ҷ�mapping�����еĵڼ�������ϣ���1��ʼ������-1˵��û�ҵ� �����趨Ϊ��seqIn.length() +1- seqAdaptor.length()
	 * ���mapNum<0, ���Զ��趨ΪseqIn.length() +1- seqAdaptor.length()����ʽ
	 * @param mapNumRight ͬmapNumLeft������Ҷ˽�ͷ
	 * @param numMM ����ݴ���mismatch 2���ȽϺ�
	 * @param conNum ����ݴ���������mismatch��1���ȽϺ�
	 * @param perMm ����ݴ�ٷֱ� �趨Ϊ30�ɣ��������adaptor̫��
	 * @return ���ظ�tag�ĵ�һ������������ϵ�λ�ã���0��ʼ����
	 * Ҳ���Ǹ�adaptorǰ���ж��ٸ����������ֱ����substring(0,return)����ȡ
	 * -1˵��û��adaptor
	 */
	public boolean trimAdaptor(String seqAdaptorL, String seqAdaptorR, int mapNumLeft, int mapNumRight, int numMM, int conNum, int perMm) {
		if ((seqAdaptorL == null || seqAdaptorL.equals("")) && (seqAdaptorR == null || seqAdaptorR.equals(""))) {
			return true;
		}
		int leftNum = 0, rightNum = seqFasta.Length();
		if (seqAdaptorL != null && !seqAdaptorL.equals("")) {
			if (mapNumLeft >= 0)
				leftNum = trimAdaptorL(seqFasta.toString(), seqAdaptorL, seqFasta.Length() - mapNumLeft, numMM,conNum, perMm);
			else
				leftNum = trimAdaptorL(seqFasta.toString(), seqAdaptorL, seqAdaptorL.length(), numMM,conNum, perMm);
		}
		
		if (seqAdaptorR != null && !seqAdaptorR.equals("")) {
			if (mapNumRight >= 0)
				rightNum = trimAdaptorR(seqFasta.toString(), seqAdaptorR, mapNumRight, numMM,conNum, perMm);
			else
				rightNum = trimAdaptorR(seqFasta.toString(), seqAdaptorR, seqFasta.Length() - seqAdaptorR.length(), numMM,conNum, perMm);
		}
		return trimSeq(leftNum, rightNum);
	}
	/**
	 * cutOffѡ��10����Ϊ10������10���µ����ж����ã���Ҫcut��
	 * @param numMM �����õ����У�����˵NNNCNNN���֣������м��һ���õ� һ��Ϊ1
	 * @return
	 */
	public boolean trimNNN( int numMM, int filterNum) {
		int numStart = trimNNNLeft(seqQuality, filterNum, numMM);
		int numEnd = trimNNNRight(seqQuality, filterNum, numMM);
		return trimSeq(numStart, numEnd);
	}
	/**
	 * cutOffѡ��10����Ϊ10������10���µ����ж����ã���Ҫcut��
	 * @param fastQBlock
	 * @param numMM
	 * @return
	 */
	public boolean trimLowCase() {
		char[] info = seqFasta.toString().toCharArray();
		int numStart = 0;
		//��ǰ�������Сд�ͼ���
		for (char c : info) {
			if ((int)c > 90 )
				numStart++;
			else
				break;
		}
		int numEnd = info.length;
		for (int i = info.length - 1; i >= 0; i--) {
			if ((int)info[i] > 90 )
				numEnd--;
			else
				break;
		}
		if (numStart >= numEnd) {
			numStart = numEnd;
		}
//		int numEnd = trimNNNRight(ss, 10, numMM);
		return trimSeq(numStart, numEnd);
	}
	/**
	 * �����Ҳ�polyA
	 * @param block
	 * @param mismatch �����趨����΢��һ��㣬��Ϊ�������趨���������Ϊ1�ˣ��������ｨ��2-3
	 * @return ���ؽض̺��string
	 * һ��������TxtReadandWrite.huiche���У����û��TxtReadandWrite.huiche
	 */
	public boolean trimPolyAR( int mismatch) {
		int num = 	trimPolyA(seqFasta.toString(), mismatch,1);
		return trimSeq(0, num);
	}
	/**
	 * �������polyT
	 * @param block
	 * @param mismatch �����趨����΢��һ��㣬��Ϊ�������趨���������Ϊ1�ˣ��������ｨ��2-3
	 * @return ���ؽض̺��string
	 * һ��������TxtReadandWrite.huiche���У����û��TxtReadandWrite.huiche
	 */
	public boolean trimPolyTL( int mismatch) {
		int num = trimPolyT(seqFasta.toString(), mismatch,1);
		return trimSeq(num, seqFasta.Length());
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
			if (isMatch(pm, mm, seqAdaptor.length(), numMM, perMm)) {
				return i+1;
			}
		}
		int num = blastSeq(false, seqIn, seqAdaptor, numMM, (int) perMm);
		if (num > -1) {
			return num;
		}
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
		if (seqAdaptor == null || seqAdaptor.equals("")) {
			return 0;
		}
		seqIn = seqIn.toUpperCase();
		seqAdaptor = seqAdaptor.toUpperCase();
		
		mapNum--;
		if (mapNum >= seqIn.length() || mapNum < 0) {
			mapNum = seqIn.length() - 1;
		}
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
				if (chrAdaptor[j] == 'N' || chrIn[i+j-lenA+1] == chrAdaptor[j] || chrIn[i+j-lenA+1] == 'N') {
					pm++; con = 0;
				}
				else {
					con ++ ;
					mm++;
					if (mm > numMM || con > conNum)
						break;
				}
			}
			if (isMatch(pm, mm, seqAdaptor.length(), numMM, perMm)) {
				return i+1;
			}
		}
		int num = blastSeq(true, seqIn, seqAdaptor, numMM, perMm);
		if (num > -1) {
			return num;
		}
		return 0;
	}
	/** �ж��Ƿ�ͨ���ʼ� */
	private static boolean isMatch(int pm, int mm, int seqAdaptorLen,int maxMMnum, float perMm) {
		int lenAdaptor = pm + mm;
		if (pm >= ((double)seqAdaptorLen * (1 - perMm/100)) 
				&&  mm <= maxMMnum && ((float)mm/seqAdaptorLen) <= perMm/100 ) 
		{
			return true;
		}
		return false;
	}
	/** ��blast�ķ������ҽ�ͷ */
	private static int blastSeq(boolean leftAdaptor, String seqSeq, String seqAdaptor, int numMM, float perMm) {
		BlastSeqFasta blastSeqFasta = new BlastSeqFasta(seqSeq, seqAdaptor);
		blastSeqFasta.setSpaceScore(-2);
		blastSeqFasta.blast();
		blastSeqFasta.blast();
		if ((double)blastSeqFasta.getMatchNum()/seqAdaptor.length() < (1-((double)perMm/100)) || blastSeqFasta.getGapNumQuery() + blastSeqFasta.getGapNumSubject() > numMM
			|| blastSeqFasta.getMisMathchNum() > numMM 
			|| (float)(blastSeqFasta.getGapNumQuery() + blastSeqFasta.getGapNumSubject() + blastSeqFasta.getMisMathchNum())/seqAdaptor.length() > perMm/100
				) 
		{
			return -1;
		}
		if (leftAdaptor) {
			return blastSeqFasta.getEndQuery();
		}
		else {
			return blastSeqFasta.getStartQuery() + 1;
		}
	}
	/**
	 * �����Ҳ�polyA����ΪAAANNNAAANANAAʱ������N��������
	 * @param seqIn
	 * @param numMM �������� һ��Ϊ1
	 * @param maxConteniunNoneA ���������
	 * @return
	 * ���ظ�Seq�ĵ�һ��A�������ϵ�λ�ã���0��ʼ����
	 * ���û��A������ֵ == Seq.length()
	 * Ҳ���Ǹ�polyAǰ���ж��ٸ����������ֱ����substring(0,return)����ȡ
	 */
	private int trimPolyA(String seqIn, int numMM, int maxConteniunNoneA) {
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
		return 0;
	}
	/**
	 * �������polyT����ΪTTTNNNTTTNTNTTʱ������N��������
	 * @param seqIn
	 * @param numMM �������� һ��Ϊ1
	 * @param maxConteniunNoneA ���������
	 * @return
	 * ���ظ�tag�����һ������������ϵ�λ�ã���1��ʼ����
	 * Ҳ���Ǹ�polyT�ж��ٸ����������ֱ����substring(return)����ȡ
	 */
	private int trimPolyT(String seqIn, int numMM, int maxConteniunNoneT) {
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
		return lenIn;
	}
	/**
	 * ������˵��������У�Q10��Q13����Ϊ���������У�һ·����ֱ��ȫ���й�Ϊֹ
	 * @param seqIn ������
	 * @param cutOff ���������е�cutOff, С�ڵ������ͻᱻcut
	 * @param numMM �����õ����У�����˵NNNCNNN���֣������м��һ���õ� һ��Ϊ1
	 * @return
	 * 	 * ���ظ�NNN�ĵ����һ������������ϵ�λ�ã���1��ʼ����
	 * Ҳ���Ǹ�NNN�ж��ٸ����������ֱ����substring(return)����ȡ
	 * ����-1��ʾ����
	 */
	private int trimNNNLeft(String seqIn,int cutOff, int numMM) {
		char[] chrSeq = this.seqFasta.toString().toCharArray();
		char[] chrIn = seqIn.toCharArray();
		int numMismatch = 0;
		int con = -1;//��¼�����ĵ��������ַ��м���
		for (int i = 0; i < chrIn.length; i++) {
			if ((int)chrIn[i] - fastqOffset > cutOff && chrSeq[i] != 'N' && chrSeq[i] != 'n') {
				numMismatch++;
				con++;
			} else {
				con = -1;
			}
			if (numMismatch > numMM) {
				return i - con;//�������a�Ļ��ļӻ�ȥ
			}
		}
		return seqIn.length();
	}
	/**
	 * �����Ҷ˵��������У�Q10��Q13����Ϊ���������У�һ·����ֱ��ȫ���й�Ϊֹ
	 * @param seqIn ������
	 * @param cutOff ���������е�cutOff, С�ڵ������ͻᱻcut
	 * @param numMM �����õ����У�����˵NNNCNNN���֣������м��һ���õ� һ��Ϊ1
	 * @return
	 * 	 * ���ظ�NNN�ĵ�һ������������ϵ�λ�ã���0��ʼ����
	 * Ҳ���Ǹ�NNNǰ���ж��ٸ����������ֱ����substring(0,return)����ȡ
	 * ����-1��ʾ����
	 */
	private int trimNNNRight(String seqIn,int cutOff, int numMM) {
		char[] chrSeq = this.seqFasta.toString().toCharArray();
		char[] chrIn = seqIn.toCharArray(); int lenIn = seqIn.length();
		int numMismatch = 0;
		int con = 0;//��¼�����ĵ��������ַ��м���
		for (int i = lenIn-1; i >= 0; i--) {
			if ((int)chrIn[i] - fastqOffset > cutOff && chrSeq[i] != 'N' && chrSeq[i] != 'n') {
				numMismatch++;
				con++;
			}
			else {
				con = 0;
			}
			if (numMismatch > numMM) {
				return i+con;
			}
		}
		return 0;
	}
	/**
	 * �������ҵ����꣬Ȼ��seqfasta�ض�
	 * @param start ��substringһ�����÷�
	 * @param end ��substringһ�����÷�
	 * @return ���ؽض̺��string
	 * ����ض̺�ĳ���С���趨�����reads���ȣ���ô�ͷ���null
	 */
	private boolean trimSeq(int start, int end) {
		if (end - start < readsMinLen) {
			return false;
		}
		if (start == 0 && end == seqQuality.length()) {
			return true;
		}
		seqFasta = seqFasta.trimSeq(start, end);
		try {
			seqQuality = seqQuality.substring(start, end);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("stop");
			return false;
		}
		
		return true;
	}
	/**
	 * ����fastq��ʽ���ı�
	 * @return
	 */
	public String toString() {
		if (seqQuality.length() != seqFasta.Length()) {
			char[] quality = new char[seqFasta.Length()];
			if (fastqOffset == FastQ.FASTQ_ILLUMINA_OFFSET) {
				for (int i = 0; i < quality.length; i++) {
					quality[i] = 'f';
				}
			}
			else {
				for (int i = 0; i < quality.length; i++) {
					quality[i] = 'A';
				}
			}
			seqQuality = String.copyValueOf(quality);
		}
		return "@" + seqFasta.getSeqName() + TxtReadandWrite.ENTER_LINUX + seqFasta.toString() + TxtReadandWrite.ENTER_LINUX + "+" + TxtReadandWrite.ENTER_LINUX + seqQuality;
	}
	/**
	 * ��¡����
	 */
	public FastQRecord clone() {
		FastQRecord seqFasta = null;
		try {
			seqFasta = (FastQRecord) super.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		seqFasta.seqQuality = seqQuality;
		seqFasta.fastqOffset = fastqOffset;
		seqFasta.readsMinLen = readsMinLen;
		seqFasta.mapFastQFilter = mapFastQFilter;
		seqFasta.quality = quality;
		return seqFasta;
	}

	/////////////////////////////// �����������ƣ�����fastq�ļ� //////////////////////////////
	/**
	 * �ʼҪ�趨���е͵�
	 * �������е������Ƿ����Ҫ�� ���Ȼ��ж������Ƿ���BBBBB��β���ǵĻ�ֱ������ 
	 * @return
	 */
	public boolean QC() {
		if (seqFasta.toString().length() < readsMinLen) {
			return false;
		}
		if (this.fastqOffset == FastQ.FASTQ_ILLUMINA_OFFSET && seqQuality.endsWith("BBBBBBBBBB") ) {
			return false;
		}
		/** �Ϳ�Q10��Q13��Q20������ */
		int[][] seqQC1 = copeFastQ(2, 10, 13, 20);
		return filterFastQ(seqQC1);
	}
	/**
	 * ��mismatich�ȶ�ָ���ļ������Ƿ����
	 * @param thisFastQ
	 * @return
	 */
	private boolean filterFastQ(int[][] thisFastQ) {
		for (int[] is : thisFastQ) {
			Integer Num = mapFastQFilter.get(is[0]);
			if (Num == null) {
				continue;
			} else if (Num < is[1]) {
				return false;
			}
		}
		return true;
	}
	/**
	 * ����һ��fastQ��ascII�룬ͬʱָ��һϵ�е�Qֵ������asc||С�ڸ�Qֵ��char�ж���
	 * ����Qvalue�����˳�����������Ӧ��int[]
	 * @param Qvalue Qvalue����ֵ������ָ�����<b>�����С��������</b>��һ��ΪQ13����ʱΪQ10�������ά���ٿƵ�FASTQ format
	 * @return int ����˳��С�ڵ���ÿ��Qvalue������
	 */
	private int[][] copeFastQ(int... Qvalue) {
		if (fastqOffset == 0) {
			System.out.println("FastQ.copeFastQ ,û��ָ��offset");
		}
		int[][] qNum = new int[Qvalue.length][2];
		for (int i = 0; i < qNum.length; i++) {
			qNum[i][0] = Qvalue[i];
		}
		char[] fastq = seqQuality.toCharArray();
		for (int m = 0; m < fastq.length; m++) {
			char c = fastq[m];
			int qualityScore = (int) c - fastqOffset;
			/////////////////////////����������ÿ������������ֲ�ͳ��/////////////////////////////////////////////////
			//////////////////////////////////////////////////////////////////////////
			for (int i = Qvalue.length - 1; i >= 0; i--) {
				if (qualityScore <= Qvalue[i]) {//ע����С�ڵ���
					qNum[i][1]++;
					continue;
				} else {
					break;
				}
			}
		}
		return qNum;
	}
}
