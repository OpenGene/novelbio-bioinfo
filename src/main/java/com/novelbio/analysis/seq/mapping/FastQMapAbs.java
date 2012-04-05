package com.novelbio.analysis.seq.mapping;

import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.FastQ;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * 准备进行mapping的fastQ文件
 * 记得设定chrFile, IndexFile
 * @author zong0jie
 *
 */
public abstract class FastQMapAbs extends FastQ implements FastQMapInt{
	/**
	 * 将单端bed文件延长至240bp
	 */
	int extendTo = 240;
	/**
	 * 默认将单端bed文件延长至240bp
	 * 小于等于0也将单端bed文件延长至240bp
	 */
	public void setExtendTo(int extendTo) {
		if (extendTo > 0) {
			this.extendTo = extendTo;
		}
	}
	/**
	 * 结果文件路径
	 */
	String outFileName = "";
	/**
	 * soap程序的路径
	 */
	String ExePath = "";
	/**
	 * 序列文件，单独的序列文件最好放在一个文件夹中
	 */
	String chrFile = "";
	/**
	 * 是否仅mapping unique序列
	 */
	boolean uniqMapping = true;
	/**
	 * 默认是solexa的最短插入
	 */
	int minInsert = 0;
	/**
	 * 默认是solexa的最长插入
	 */
	int maxInsert = 500;
	/**
	 * 单端mapping
	 * @param seqFile1
	 * @param QUALITY
	 */
	public FastQMapAbs(String seqFile1, int QUALITY) {
		super(seqFile1, QUALITY);
		// TODO Auto-generated constructor stub
	}
	/**
	 * 双端mapping
	 * @param seqFile1
	 * @param seqFile2
	 * @param QUALITY
	 */
	public FastQMapAbs(String seqFile1,String seqFile2, int QUALITY) {
		super(seqFile1, seqFile2, QUALITY);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * 双端mapping
	 * @param seqFile1
	 * @param seqFile2
	 * @param FastQFormateOffset 哪种fastQ格式，现在有FASTQ_SANGER_OFFSET，FASTQ_ILLUMINA_OFFSET两种 不知道就写0，程序会从文件中判断
	 * @param QUALITY
	 */
	public FastQMapAbs(String seqFile1,String seqFile2, int FastQFormateOffset,int QUALITY) {
		super(seqFile1, seqFile2, FastQFormateOffset, QUALITY);
		// TODO Auto-generated constructor stub
	}
	/**
	 * 设定插入片段长度，默认是solexa的长度，150-500
	 */
	public void setInsertSize(int minInsertLen, int maxInsertLen)
	{
		this.minInsert = minInsertLen;
		this.maxInsert = maxInsertLen;
	}
	/**
	 * @param exeFile bwa程序所在的路径，系统路径则用""
	 * @param chrFile 序列文件
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
	 * 输出的mapping文件路径
	 * @param outFileName
	 */
	public void setOutFileName(String outFileName) {
		this.outFileName = outFileName;
	}
	
	
	
	/**
	 * 将本seqFile进行mapping分析，做mapping之前先要进行过滤处理
	 * @param fileName 最后的文件名
	 * 实验组 fileName+"_Treat_SoapMap";
	 * @return 返回reads的总数，也就是测序量，<b>双端的话不乘以2</b>
	 */
	public abstract SAMtools mapReads();
	
	/**
	 * 回头添加做索引
	 */
	protected abstract void IndexMake();
	/**
	 * 返回bed文件，如果是双端就返回双端的bed文件
	 * 如果是单端就返回延长的bed文件，默认延长至extendTo bp
	 * @return
	 */
	public abstract BedSeq getBedFile(String bedFile);
	/**
	 * 强制返回单端的bed文件，用于给macs找peak用
	 * @return
	 */
	public abstract BedSeq getBedFileSE(String bedFile);
	/**
	 * 过滤低质量reads
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
	 * 设定mapping质量，根据不同的测序长度进行默认20
	 */
	public abstract void setMapQ(int mapQ);
}
