package com.novelbio.analysis.seq.mapping;

import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.FastQ;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * ׼������mapping��fastQ�ļ�
 * �ǵ��趨chrFile, IndexFile
 * @author zong0jie
 *
 */
public abstract class FastQMapAbs extends FastQ implements FastQMapInt{
	/**
	 * ������bed�ļ��ӳ���240bp
	 */
	int extendTo = 240;
	/**
	 * Ĭ�Ͻ�����bed�ļ��ӳ���240bp
	 * С�ڵ���0Ҳ������bed�ļ��ӳ���240bp
	 */
	public void setExtendTo(int extendTo) {
		if (extendTo > 0) {
			this.extendTo = extendTo;
		}
	}
	/**
	 * ����ļ�·��
	 */
	String outFileName = "";
	/**
	 * soap�����·��
	 */
	String ExePath = "";
	/**
	 * �����ļ��������������ļ���÷���һ���ļ�����
	 */
	String chrFile = "";
	/**
	 * �Ƿ��mapping unique����
	 */
	boolean uniqMapping = true;
	/**
	 * Ĭ����solexa����̲���
	 */
	int minInsert = 0;
	/**
	 * Ĭ����solexa�������
	 */
	int maxInsert = 500;
	/**
	 * ����mapping
	 * @param seqFile1
	 * @param QUALITY
	 */
	public FastQMapAbs(String seqFile1, int QUALITY) {
		super(seqFile1, QUALITY);
		// TODO Auto-generated constructor stub
	}
	/**
	 * ˫��mapping
	 * @param seqFile1
	 * @param seqFile2
	 * @param QUALITY
	 */
	public FastQMapAbs(String seqFile1,String seqFile2, int QUALITY) {
		super(seqFile1, seqFile2, QUALITY);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * ˫��mapping
	 * @param seqFile1
	 * @param seqFile2
	 * @param FastQFormateOffset ����fastQ��ʽ��������FASTQ_SANGER_OFFSET��FASTQ_ILLUMINA_OFFSET���� ��֪����д0���������ļ����ж�
	 * @param QUALITY
	 */
	public FastQMapAbs(String seqFile1,String seqFile2, int FastQFormateOffset,int QUALITY) {
		super(seqFile1, seqFile2, FastQFormateOffset, QUALITY);
		// TODO Auto-generated constructor stub
	}
	/**
	 * �趨����Ƭ�γ��ȣ�Ĭ����solexa�ĳ��ȣ�150-500
	 */
	public void setInsertSize(int minInsertLen, int maxInsertLen)
	{
		this.minInsert = minInsertLen;
		this.maxInsert = maxInsertLen;
	}
	/**
	 * @param exeFile bwa�������ڵ�·����ϵͳ·������""
	 * @param chrFile �����ļ�
	 */
	public void setFilePath(String exeFile, String chrFile) {
		this.chrFile = chrFile;
		if (exeFile.trim().equals("")) {
			this.ExePath = "";
		}
		else {
			this.ExePath = FileOperate.addSep(exeFile);
		}
		
	}
	/**
	 * �����mapping�ļ�·��
	 * @param outFileName
	 */
	public void setOutFileName(String outFileName) {
		this.outFileName = outFileName;
	}
	
	
	
	/**
	 * ����seqFile����mapping��������mapping֮ǰ��Ҫ���й��˴���
	 * @param fileName �����ļ���
	 * ʵ���� fileName+"_Treat_SoapMap";
	 * @return ����reads��������Ҳ���ǲ�������<b>˫�˵Ļ�������2</b>
	 */
	public abstract SAMtools mapReads();
	
	/**
	 * ��ͷ���������
	 */
	protected abstract void IndexMake();
	/**
	 * ����bed�ļ��������˫�˾ͷ���˫�˵�bed�ļ�
	 * ����ǵ��˾ͷ����ӳ���bed�ļ���Ĭ���ӳ���extendTo bp
	 * @return
	 */
	public abstract BedSeq getBedFile(String bedFile);
	/**
	 * ǿ�Ʒ��ص��˵�bed�ļ������ڸ�macs��peak��
	 * @return
	 */
	public abstract BedSeq getBedFileSE(String bedFile);
	/**
	 * ���˵�����reads
	 */
	public FastQMapAbs filterReads(String fileFilterOut)
	{
		FastQ fastQ = null;
		try {
			fastQ = super.filterReads(fileFilterOut);	
		} catch (Exception e) {
			e.printStackTrace();
		}
//		FastQMapBwa fastQSoapMap= new FastQMapBwa(fastQ.getSeqFile(), fastQ.getSeqFile2(), getOffset(), getQuality(), outFileName, uniqMapping);
		FastQMapAbs fastQMapAbs = createFastQMap(fastQ);
		fastQMapAbs.setCompressType(fastQ.getCompressInType(), fastQ.getCompressOutType());

		return fastQMapAbs;
	}

	protected abstract FastQMapAbs createFastQMap(FastQ fastQ);
	/**
	 * �趨mapping���������ݲ�ͬ�Ĳ��򳤶Ƚ���Ĭ��20
	 */
	public abstract void setMapQ(int mapQ);
}
