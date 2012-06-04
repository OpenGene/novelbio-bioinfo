package com.novelbio.analysis.seq.genomeNew.getChrSequence;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.novelbio.base.dataOperate.TxtReadandWrite;

public class FastQRecord extends SeqFasta implements Cloneable {
	private static Logger logger = Logger.getLogger(FastQRecord.class);
	protected int fastqOffset = FASTQ_SANGER_OFFSET;
	protected String seqQuality = "";
	/** �����������ƣ����ڸ�������˵������¼������ */
	protected int quality = QUALITY_MIDIAN;
	/** �ü�����ʱ���Ϊ���� */
	private int trimMinLen = 22;
	public FastQRecord() {
		// TODO Auto-generated constructor stub
	}
	/**
	 * ÿ����һ����¼������������linux�س�������Ȼ������
	 * @param fastqlines
	 */
	public FastQRecord(String fastqlines) {
		String[] ss = fastqlines.split(TxtReadandWrite.ENTER_LINUX);
		if (ss.length == 1) {
			ss = fastqlines.split(TxtReadandWrite.ENTER_WINDOWS);
		}
		setSeqName(ss[0].substring(1));
		setSeq(ss[1]);
		setFastaQuality(ss[3]);
	}
	
	/** �ü�����ʱ���Ϊ���٣� Ĭ��Ϊ22
	 */
	public void setTrimMinLen(int trimMinLen) {
		this.trimMinLen = trimMinLen;
	}
	/**
	 * �趨������������phred��ʽ�趨
	 * @param fastaQuality
	 */
	public void setFastaQuality(String fastaQuality) {
		this.seqQuality = fastaQuality;
	}
	public String getSeqQuality() {
		return seqQuality;
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
	 * �����Ҳ��ͷ���еķ�������ѭ���������������䣬���ǲ��ܹ����Ǻ���gap��adaptor��
	 * �㷨�������Ҳ����ֻ��һ������ͷ����ô�Ƚ���ͷֱ�ӶԵ��Ҳ���룬Ȼ��ѭ���Ľ���ͷ�Ե�reads��ȥ��
	 * @param minLen ��̶��٣�С�ڸó��Ⱦͷ��� null
	 * @param seqAdaptorL ��˽�ͷ ����ν��Сд ��ͷ����ֻдһ���� null��""��ʾ���ù��˸ý�ͷ
	 * @paran seqAdaptorR �Ҷ˽�ͷ ����ν��Сд ��ͷ����ֻдһ���� null��""��ʾ���ù��˸ý�ͷ
	 * @param mapNumLeft ��һ�ν�ͷ��˻��Ҷ�mapping�����еĵڼ�������ϣ���1��ʼ������-1˵��û�ҵ� �����趨Ϊ��seqIn.length() +1- seqAdaptor.length()
	 * ���mapNum<0, ���Զ��趨ΪseqIn.length() +1- seqAdaptor.length()����ʽ
	 * @param mapNumRight ͬmapNumLeft������Ҷ˽�ͷ
	 * @param numMM ����ݴ�����mismatch 2���ȽϺ�
	 * @param conNum ����ݴ���������mismatch��1���ȽϺ�
	 * @param perMm ����ݴ��ٷֱ� �趨Ϊ30�ɣ��������adaptor̫��
	 * @return ���ظ�tag�ĵ�һ������������ϵ�λ�ã���0��ʼ����
	 * Ҳ���Ǹ�adaptorǰ���ж��ٸ����������ֱ����substring(0,return)����ȡ
	 * -1˵��û��adaptor
	 */
	public FastQRecord trimAdaptor(String seqAdaptorL, String seqAdaptorR, int mapNumLeft, int mapNumRight, int numMM, int conNum, int perMm) {
		if ((seqAdaptorL == null || seqAdaptorL.equals("")) && (seqAdaptorR == null || seqAdaptorR.equals(""))) {
			return this;
		}
		int leftNum = 0, rightNum = super.SeqSequence.length();
		if (seqAdaptorL != null && !seqAdaptorL.equals("")) {
			if (mapNumLeft >= 0)
				leftNum = 	trimAdaptorL(SeqSequence, seqAdaptorL, SeqSequence.length() - mapNumLeft, numMM,conNum, perMm);
			else
				leftNum = 	trimAdaptorL(SeqSequence, seqAdaptorL, seqAdaptorL.length(), numMM,conNum, perMm);
		}
		
		if (seqAdaptorR != null && !seqAdaptorR.equals("")) {
			if (mapNumRight >= 0)
				rightNum = 	trimAdaptorR(SeqSequence, seqAdaptorR, mapNumRight, numMM,conNum, perMm);
			else//TODO ȷ�������趨���ٺ��ʣ�SeqSequence.length() - seqAdaptorL.length()
				rightNum = 	trimAdaptorR(SeqSequence, seqAdaptorR, SeqSequence.length() - seqAdaptorR.length(), numMM,conNum, perMm);
		}
		return trimSeq(leftNum, rightNum);
	}
	/**
	 * cutOffѡ��10����Ϊ10������10���µ����ж����ã���Ҫcut��
	 * @param numMM �����õ����У�����˵NNNCNNN���֣������м��һ���õ� һ��Ϊ1
	 * @return
	 */
	public FastQRecord trimNNN( int numMM) {
		int numStart = trimNNNLeft(seqQuality, 10, numMM);
		int numEnd = trimNNNRight(seqQuality, 10, numMM);
		return trimSeq(numStart, numEnd);
	}
	/**
	 * cutOffѡ��10����Ϊ10������10���µ����ж����ã���Ҫcut��
	 * @param fastQBlock
	 * @param numMM
	 * @return
	 */
	public FastQRecord trimLowCase() {
		char[] info = SeqSequence.toCharArray();
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
	public FastQRecord trimPolyAR( int mismatch) {
		int num = 	trimPolyA(SeqSequence, mismatch,1);
		return trimSeq(0, num);
	}
	/**
	 * �������polyT
	 * @param block
	 * @param mismatch �����趨����΢��һ��㣬��Ϊ�������趨���������Ϊ1�ˣ��������ｨ��2-3
	 * @return ���ؽض̺��string
	 * һ��������TxtReadandWrite.huiche���У����û��TxtReadandWrite.huiche
	 */
	public FastQRecord trimPolyTL( int mismatch) {
		int num = trimPolyT(SeqSequence, mismatch,1);
		return trimSeq(num, SeqSequence.length());
	}
	/**
	 * ע���������µ�adaptor�޷�����
	 * �����Ҳ��ͷ���еķ�������ѭ���������������䣬���ǲ��ܹ����Ǻ���gap��adaptor��
	 * �㷨�������Ҳ����ֻ��һ������ͷ����ô�Ƚ���ͷֱ�ӶԵ��Ҳ���룬Ȼ��ѭ���Ľ���ͷ�Ե�reads��ȥ��
	 * @param seqIn �������� ����ν��Сд
	 * @param seqAdaptor ��ͷ ����ν��Сд ��ͷ����ֻдһ����
	 * @param mapNum ��һ�ν�ͷ���mapping�����еĵڼ�������ϣ���1��ʼ������-1˵��û�ҵ� �����趨Ϊ��seqIn.length() +1- seqAdaptor.length()
	 * @param numMM ����ݴ�����mismatch 2���ȽϺ�
	 * @param conNum ����ݴ���������mismatch��1���ȽϺ�
	 * @param perMm ����ݴ��ٷֱ� �趨Ϊ30�ɣ��������adaptor̫��
	 * @return ���ظ�tag�ĵ�һ������������ϵ�λ�ã���0��ʼ����
	 * Ҳ���Ǹ�adaptorǰ���ж��ٸ����������ֱ����substring(0,return)����ȡ
	 * -1˵��û��adaptor
	 */
	private int trimAdaptorR(String seqIn, String seqAdaptor, int mapNum, int numMM, int conNum, float perMm) {
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
	 * ��������ͷ���еķ�������ѭ���������������䣬���ǲ��ܹ����Ǻ���gap��adaptor��
	 * �㷨������������ֻ��һ������ͷ����ô�Ƚ���ͷֱ�ӶԵ������룬Ȼ��ѭ���Ľ���ͷ�Ե�reads��ȥ��
	 * @param seqIn �������� ����ν��Сд
	 * @param seqAdaptor ��ͷ ����ν��Сд
	 * @param mapNum ��һ�ν�ͷ�Ҷ�mapping�����еĵڼ�������ϣ���1��ʼ������-1˵��û�ҵ� �����趨Ϊ��adaptorLeft.length()
	 * @param numMM ����ݴ�����mismatch 1���ȽϺ�
	 * @param conNum ����ݴ���������mismatch��1���ȽϺ�
	 * @param perMm ����ݴ��ٷֱ�,100���ƣ��趨Ϊ30�ɣ��������adaptor̫��
	 * @return ���ظ�tag����һ������������ϵ�λ�ã���1��ʼ����
	 * Ҳ���Ǹ�adaptorǰ���ж��ٸ����������ֱ����substring(return)����ȡ
	 * -1˵��û��adaptor
	 */
	private int trimAdaptorL(String seqIn, String seqAdaptor, int mapNum, int conNum, int numMM, int perMm) {
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
			if (mm <= numMM && ((float)(mm/lenAdaptor)) <= perMm/100 && lenAdaptor > 4) {
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
//		System.out.println(seqIn);
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
//		System.out.println(seqIn);
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
		char[] chrIn = seqIn.toCharArray();
		int numMismatch = 0;
		int con = -1;//��¼�����ĵ��������ַ��м���
		for (int i = 0; i < chrIn.length; i++) {
			if ((int)chrIn[i] - fastqOffset > cutOff) {
				numMismatch++;
				con++;
			}
			else {
				con = -1;
			}
			if (numMismatch > numMM) {
				return i - con;//�������a�Ļ��ļӻ�ȥ
			}
		}
//		logger.info("no useful seq: "+ seqIn);
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
		char[] chrIn = seqIn.toCharArray(); int lenIn = seqIn.length();
		int numMismatch = 0;
		int con = 0;//��¼�����ĵ��������ַ��м���
		for (int i = lenIn-1; i >= 0; i--) {
			if ((int)chrIn[i] - fastqOffset > cutOff) {
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
//		logger.info("no useful seq: "+ seqIn);
		return 0;
	}
	/**
	 * �������ҵ����꣬Ȼ��seqfasta�ض�
	 * @param start ��substringһ�����÷�
	 * @param end ��substringһ�����÷�
	 * @return ���ؽض̺��string
	 * ����ض̺�ĳ���С���趨�����reads���ȣ���ô�ͷ���null
	 */
	private FastQRecord trimSeq(int start, int end) {
		if (end - start < trimMinLen) {
			return null;
		}
		FastQRecord result = new FastQRecord();
		if (start == 0 && end == seqQuality.length()) {
			return clone();
		}
		result.SeqName = SeqName;
		result.AA3Len = AA3Len;
		result.TOLOWCASE = TOLOWCASE;
		result.fastqOffset= fastqOffset;
		result.trimMinLen = trimMinLen;
		result.seqQuality = seqQuality.substring(start, end);
		result.SeqSequence = SeqSequence.substring(start, end);
		return result;
	}
	/**
	 * ����fastq��ʽ���ı�
	 * @return
	 */
	public String toString() {
		if (seqQuality.length() != SeqSequence.length()) {
			char[] quality = new char[SeqSequence.length()];
			if (fastqOffset == FASTQ_ILLUMINA_OFFSET) {
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
		return "@" + SeqName + TxtReadandWrite.ENTER_LINUX + SeqSequence + TxtReadandWrite.ENTER_LINUX + "+" + TxtReadandWrite.ENTER_LINUX + seqQuality;
	}
	/**
	 * ��¡����
	 */
	public FastQRecord clone() {
		FastQRecord seqFasta = null;
		seqFasta = (FastQRecord) super.clone();
		seqFasta.seqQuality = seqQuality;
		seqFasta.fastqOffset = fastqOffset;
		seqFasta.trimMinLen = trimMinLen;
		return seqFasta;
	}

	/////////////////////////////// �����������ƣ�����fastq�ļ� //////////////////////////////
	/**
	 * ����һ��fastQ��ascII�룬ͬʱָ��һϵ�е�Qֵ������asc||С�ڸ�Qֵ��char�ж���
	 * ����Qvalue�����˳�����������Ӧ��int[]
	 * 
	 * @param FASTQ_FORMAT_OFFSET
	 *            offset�Ƕ��٣�FASTQ_SANGER_OFFSET��
	 * @param fastQSeq
	 *            �����fastQ�ַ���
	 * @param Qvalue
	 *            Qvalue����ֵ������ָ�����<b>�����С��������</b>��һ��ΪQ13����ʱΪQ10�������ά���ٿƵ�FASTQ
	 *            format
	 * @return int ����˳��С�ڵ���ÿ��Qvalue������
	 */
	public int[][] copeFastQ(int... Qvalue) {
		if (fastqOffset == 0) {
			System.out.println("FastQ.copeFastQ ,û��ָ��offset");
		}
		int[][] qNum = new int[Qvalue.length][2];
		for (int i = 0; i < qNum.length; i++) {
			qNum[i][0] = Qvalue[i];
		}
		char[] fastq = seqQuality.toCharArray();
		//reads���ȷֲ���һ������454
//		gffHashBin.addNumber(gffreadsLen, fastq.length);
		for (int m = 0; m < fastq.length; m++) {
			char c = fastq[m];
			int qualityScore = (int) c - fastqOffset;
			/////////////////////////����������ÿ������������ֲ�ͳ��/////////////////////////////////////////////////
//			gffHashBin.addNumber(m+gffbpName, qualityScore);
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

	/**
	 * �ʼҪ�趨���е͵�
	 * �������е������Ƿ����Ҫ�� ���Ȼ��ж������Ƿ���BBBBB��β���ǵĻ�ֱ������ 
	 * @return
	 */
	public boolean QC() {
		if (SeqSequence == null) {
			return false;
		}
		if (seqQuality.endsWith("BBBBBBB") ) {
			return false;
		}
		/** �Ϳ�Q10��Q13��Q20������ */
		int[][] seqQC1 = copeFastQ(2, 10, 13, 20);
		return filterFastQ(seqQC1);
	}
	///////////////////  fastq������  /////////////////////////////////////////////////////////////
	public static int QUALITY_LOW = 10;
	public static int QUALITY_MIDIAN = 20;
	/**
	 * ˫�˵�ʱ��ֻ���������ж��ǺõĲű���
	 */
	public static int QUALITY_MIDIAN_PAIREND = 40;
	public static int QUALITY_HIGM = 50;
	public static int QUALITY_LOW_454 = 10454;
	/**
	 * fastQ����asc||���ָ�������
	 */
	static HashMap<Integer, Integer> hashFastQFilter = new HashMap<Integer, Integer>();
	/**
	 * ��mismatich�ȶ�ָ���ļ������Ƿ����
	 * @param thisFastQ
	 * @return
	 */
	private static boolean filterFastQ(int[][] thisFastQ) {
		for (int[] is : thisFastQ) {
			Integer Num = hashFastQFilter.get(is[0]);
			if (Num == null) {
				continue;
			} else if (Num < is[1]) {
				return false;
			}
		}
		return true;
	}
	/**
	 * �趨ȫ�ֹ���ָ��
	 * @param QUALITY
	 */
	public static void setHashFastQFilter(int QUALITY) {
		if (QUALITY == QUALITY_HIGM) {
			hashFastQFilter.put(10, 2);
			hashFastQFilter.put(13, 3);
			hashFastQFilter.put(20, 7);
		} else if (QUALITY == QUALITY_LOW) {
//			hashFastQFilter.put(2, 1);
			hashFastQFilter.put(10, 4);
			hashFastQFilter.put(13, 10);
			hashFastQFilter.put(20, 20);
		} else if (QUALITY == QUALITY_MIDIAN
				|| QUALITY == QUALITY_MIDIAN_PAIREND) {
//			hashFastQFilter.put(2, 1);
			hashFastQFilter.put(10, 2);
			hashFastQFilter.put(13, 6);
			hashFastQFilter.put(20, 10);
		} else if (QUALITY == QUALITY_LOW_454) {
//			hashFastQFilter.put(2, 1);
			hashFastQFilter.put(10, 6);
			hashFastQFilter.put(13, 15);
			hashFastQFilter.put(20, 50);
		}
		else {
//			hashFastQFilter.put(2, 1);
			hashFastQFilter.put(10, 2);
			hashFastQFilter.put(13, 6);
			hashFastQFilter.put(20, 10);
		}
	}
	
}