package com.novelbio.analysis.seq.mapping;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.novelbio.analysis.IntCmdSoft;
import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.fastq.FastQRecord;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.analysis.seq.sam.SamRGroup;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;

/**
 * 在set里面有很多参数可以设定，不设定就用默认
 * @author zong0jie
 *
 */
@Component
@Scope("prototype")
public class MapBwa extends MapDNA implements IntCmdSoft {
	private static final Logger logger = Logger.getLogger(MapBwa.class);
	/**
	 * 在此大小以下的genome直接读入内存以帮助快速mapping
	 * 单位，KB
	 * 似乎该值双端才有用
	 */
	private static final int GENOME_SIZE_IN_MEMORY = 500000;
	
	/** bwa所在路径 */
	String ExePath = "";
	String[] sampleGroup;
	List<FastQ> lsLeftFq = new ArrayList<>();
	String leftCombFq;
	List<FastQ> lsRightFq = new ArrayList<>();
	String rightCombFq;
	
	MapLibrary mapLibrary = MapLibrary.PairEnd;
	
	/** 含有几个gap */
	int gapNum = 1;
	/** gap的长度 */
	int gapLength = 20;
	/** 线程数量 */
	int threadNum = 4;
	/**
	 * Maximum edit distance if the value is INT, or the fraction of missing alignments given 2% uniform
	 *  base error rate if FLOAT. In the latter case, the maximum edit distance is automatically chosen 
	 *  for different read lengths. [0.04]
	 */
	String mismatch = "0.04";

	/** 是否将index读入内存，仅对双端有效 */
	boolean readInMemory = true;
	
	public void setFqFile(FastQ leftFq, FastQ rightFq) {
		this.lsLeftFq.clear();
		this.lsRightFq.clear();
		if (leftFq != null) {
			lsLeftFq.add(leftFq);
		}
		if (rightFq != null) {
			lsRightFq.add(rightFq);
		}
	}
	
	/**
	 * 设置左端的序列，设置会把以前的清空
	 * @param fqFile
	 */
	@Override
	public void setLeftFq(List<FastQ> lsLeftFastQs) {
		if (lsLeftFastQs == null) return;
		this.lsLeftFq = lsLeftFastQs;
	}
	/**
	 * 设置右端的序列，设置会把以前的清空
	 * @param fqFile
	 */
	@Override
	public void setRightFq(List<FastQ> lsRightFastQs) {
		if (lsRightFastQs == null) return;
		this.lsRightFq = lsRightFastQs;
	}
	
	private void combSeq() {
		boolean singleEnd = (lsLeftFq.size() > 0 && lsRightFq.size() > 0) ? false : true;
		if (lsLeftFq.size() > 0 && lsRightFq.size() > 0) {
			singleEnd = false;
		}
		
		if (lsLeftFq.size() == 1) {
			leftCombFq = lsLeftFq.get(0).getReadFileName();
		} else {
			leftCombFq = combSeq(singleEnd, true, prefix, lsLeftFq);
		}
		if (lsRightFq.size() == 1) {
			rightCombFq = lsRightFq.get(0).getReadFileName();
		} else {
			rightCombFq = combSeq(singleEnd, false, ExePath, lsRightFq);
		}
	}
	
	/** 将输入的fastq文件合并为一个 */
	private static String combSeq(boolean singleEnd, boolean left, String prefix, List<FastQ> lsFastq) {
		if (lsFastq == null || lsFastq.size() == 0) {
			return null;
		}
		String fastqFile = FileOperate.getParentPathName(lsFastq.get(0).getReadFileName()) + prefix;
		if (singleEnd) {
			fastqFile += ".fq.gz";
		} else {
			if (left) {
				fastqFile += "_1.fq.gz";
			} else if (!left) {
				fastqFile += "_2.fq.gz";
			}
		}

		FastQ fastqComb = new FastQ(fastqFile);
		for (FastQ fastQ : lsFastq) {
			for (FastQRecord fastQRecord : fastQ.readlines()) {
				fastqComb.writeFastQRecord(fastQRecord);
			}
			fastQ.close();
		}
		fastqComb.close();
		return fastqComb.getReadFileName();
	}
	
	/**
	 * 百分之多少的mismatch，或者几个mismatch
	 * @param mismatch
	 */
	public void setMismatch(double mismatch) {
		if (mismatch >= 1 || mismatch == 0) {
			this.mismatch = (int)mismatch+"";
		}
		else {
			this.mismatch = mismatch + "";
		}
	}
	/**
	 * 百分之多少的mismatch，或者几个mismatch
	 * @param mismatchScore
	 */
	private String[] getMismatch() {
		return new String[]{"-n", mismatch + ""};
	}
	
	/**
	 * 设定bwa所在的文件夹以及待比对的路径
	 * @param exePath 如果在根目录下则设置为""或null
	 * @param chrFile
	 */
	public void setExePath(String exePath) {
		if (exePath == null || exePath.trim().equals("")) {
			this.ExePath = "";
		} else {
			this.ExePath = FileOperate.addSep(exePath);
		}
	}
	/** 线程数量，默认4线程 */
	public void setThreadNum(int threadNum) {
		this.threadNum = threadNum;
	}
	private String[] getThreadNum() {
		return new String[]{"-t", threadNum + ""};
	}
	/**
	 * 是否将index读入内存，仅对双端测序有用
	 */
	public void setReadInMemory(boolean readInMemory) {
		this.readInMemory = readInMemory;
	}
	
	public void setMapLibrary(MapLibrary mapLibrary) {
		this.mapLibrary = mapLibrary;
	}
	/**
	 * 本次mapping的组，所有参数都不能有空格
	 * @param sampleID 
	 * @param LibraryName
	 * @param SampleName
	 * @param Platform
	 */
	public void setSampleGroup(String sampleID, String LibraryName, String SampleName, String Platform) {
		SamRGroup samRGroup = new SamRGroup(sampleID, LibraryName, SampleName, Platform);
		sampleGroup = new String[]{"-r", samRGroup.toString()};
	}
	/**
	 * 默认gap为4，如果是indel查找的话，设置到5或者6比较合适
	 * @param gapLength
	 */
	public void setGapLength(int gapLength) {
		this.gapLength = gapLength;
	}
	/**
	 * 默认gap为4，如果是indel查找的话，设置到5或者6比较合适
	 * @param gapLength
	 */
	private String[] getGapLen() {
		return new String[]{"-e", gapLength + ""};
	}
	/** 比对的时候容忍最多几个gap 默认为1，1个就够了，除非长度特别长或者是454*/
	public void setGapNum(int gapnum) {
		this.gapNum = gapnum;
	}
	/** 比对的时候容忍最多几个gap 默认为1，1个就够了，除非长度特别长或者是454*/
	private String[] getGapNum() {
		return new String[]{"-o", gapNum + ""};
	}
	private String[] getInsertSize() {
		int insertMax = 500;
		if (isPairEnd()) {
			if (mapLibrary == MapLibrary.SingleEnd || mapLibrary == MapLibrary.PairEnd) {
				insertMax = 500;
			} else if (mapLibrary == MapLibrary.MatePair) {
				insertMax = 10000;
			} else if (mapLibrary == MapLibrary.MatePairLong) {
				insertMax = 25000;
			}
			return new String[]{"-a", insertMax + ""};
		}
		return null;
	}
	protected boolean isPairEnd() {
		if (leftCombFq == null || rightCombFq == null) {
			return false;
		}
		return true;
	}
	/** 种子长度 */
	private String[] getSeedSize() {
		return new String[]{"-l", 25 + ""};
	}
	/**
	 * gap罚分
	 * @return
	 */
	private String[] getOpenPanalty() {
		return new String[]{"-O", 10 +""};
	}
	/**
	 * 是illumina32标准还是64标准
	 * @return 64标准返回"-l", 32标准返回null
	 */
	private String getFastQoffset() {
		FastQ fastQ = new FastQ(leftCombFq);
		int offset = fastQ.getOffset();
		if (offset == FastQ.FASTQ_ILLUMINA_OFFSET) {
			return  "-I";
		}
		return null;
	}
	/**
	 * 返回sai的信息, <b>不加引号</b>
	 * @param Sai1orSai2 双端的话，sai1就输入1，sai2就输入2。单端sai也输入1
	 * @return
	 */
	private String getSai(int Sai1orSai2) {
		String sai = FileOperate.getParentPathName(outFileName) + FileOperate.getFileNameSep(outFileName)[0];
		if (Sai1orSai2 == 1) {
			if (isPairEnd()) {
				sai = sai + "_1.sai"; 
			} else {
				sai = sai + ".sai";
			}
		} else if (Sai1orSai2 ==2) {
			sai = sai + "_2.sai"; 
		}
		return sai;
	}
	
	/**
	 * 根据基因组大小，考虑将基因组读入内存
	 * @return 没有则返回null
	 */
	private String readInMemory() {
		if (FileOperate.getFileSize(chrFile) < GENOME_SIZE_IN_MEMORY || readInMemory) {
			return "-P";
		}
		return null;
	}
	
	@Override
	protected SamFile mapping() {
		combSeq();
		if (!bwaAln()) {
			return null;
		}
		return bwaSamPeSe();
	}
	/**
	 * linux命令如下<br>
	 * bwa aln -n 4 -o 1 -e 5 -t 4 -o 10 -I -l 18 /media/winE/Bioinformatics/GenomeData/Streptococcus_suis/98HAH33/BWAindex/NC_009443.fna barcod_TGACT.fastq > TGACT.sai<br>
	 * bwa aln -n 4 -o 1 -e 5 -t 4 -o 10 -I -l 18 /media/winE/Bioinformatics/GenomeData/Streptococcus_suis/98HAH33/BWAindex/NC_009443.fna barcod_TGACT2.fastq > TGACT2.sai<br>
	 * bwa sampe -P -n 4 /media/winE/Bioinformatics/GenomeData/Streptococcus_suis/98HAH33/BWAindex/NC_009443.fna TGACT.sai TGACT2.sai barcod_TGACT.fastq
	 *
	 * @return 是否成功运行
	 */
	private boolean bwaAln() {
		List<String> lsCmdLeft = getLsCmdAln(true);
		CmdOperate cmdOperate = new CmdOperate(lsCmdLeft);
		cmdOperate.run();
		
		if (isPairEnd()) {
			List<String> lsCmdRight = getLsCmdAln(false);
			cmdOperate = new CmdOperate(lsCmdRight);
			cmdOperate.run();
		}
		
		if (cmdOperate.isFinishedNormal() || cmdOperate.getRunTime() > overTime) {
			return true;
		} else {
			return false;
		}
	}
	
	private List<String> getLsCmdAln(boolean firstOrSecond) {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add(ExePath + "bwa");
		lsCmd.add("aln");
		ArrayOperate.addArrayToList(lsCmd, getMismatch());
		ArrayOperate.addArrayToList(lsCmd, getGapNum());
		ArrayOperate.addArrayToList(lsCmd, getGapLen());
		ArrayOperate.addArrayToList(lsCmd, getThreadNum());
		ArrayOperate.addArrayToList(lsCmd, getSeedSize());
		ArrayOperate.addArrayToList(lsCmd, getOpenPanalty());
		addLsCmd(lsCmd, getFastQoffset());
		lsCmd.add(chrFile);
		if (firstOrSecond) {
			lsCmd.add(leftCombFq);
			lsCmd.add(">");
			lsCmd.add(getSai(1));
		} else {
			lsCmd.add(rightCombFq);
			lsCmd.add(">");
			lsCmd.add(getSai(2));	
		}
		return lsCmd;
	}
	
	/**
	 * @param lsCmd
	 * @param param null则不添加入lsCmd
	 */
	private void addLsCmd(List<String> lsCmd, String param) {
		if (param == null) return;
		lsCmd.add(param);
	}
	
	/**
	 * 这里设定了将基因组读入内存的限制
	 * bwa sampe -P -n 4 /media/winE/Bioinformatics/GenomeData/Streptococcus_suis/98HAH33/BWAindex/NC_009443.fna TGACT.sai 
	 * TGACT2.sai barcod_TGACT.fastq barcod_TGACT2.fastq > TGACT.sam
	 */
	private SamFile bwaSamPeSe() {
		List<String> lsCmd = getLsCmdSam();
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		cmdOperate.setGetCmdInStdStream(true);
		Thread thread = new Thread(cmdOperate);
		thread.start();
		InputStream inputStream = cmdOperate.getStdStream();
		SamFile samResult = copeSamStream(false, inputStream, isNeedSort);
		if (samResult != null && !cmdOperate.isRunning() && cmdOperate.isFinishedNormal()) {
			deleteFile();
			return samResult;
		} else {
			deleteFailFile();
			return null;
		}
	}
	
	private List<String> getLsCmdSam() {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add(this.ExePath + "bwa");
		if (isPairEnd()) {
			lsCmd.add("sampe");
			ArrayOperate.addArrayToList(lsCmd, sampleGroup);
			ArrayOperate.addArrayToList(lsCmd, getInsertSize());
			addLsCmd(lsCmd, readInMemory());
			lsCmd.add("-n"); lsCmd.add(10+"");
			lsCmd.add("-N"); lsCmd.add(10+"");
			lsCmd.add(chrFile);
			lsCmd.add(getSai(1));
			lsCmd.add(getSai(2));
			lsCmd.add(leftCombFq);
			lsCmd.add(rightCombFq);
		} else {
			lsCmd.add("samse");
			ArrayOperate.addArrayToList(lsCmd, sampleGroup);
			lsCmd.add("-n"); lsCmd.add(50+"");
			lsCmd.add(chrFile);
			lsCmd.add(getSai(1));
			lsCmd.add(leftCombFq);
		}
		lsCmd.add(">");
		lsCmd.add(outFileName);
		return lsCmd;
	}
	
	/**
	 * 删除sai文件
	 * @param samFileName
	 */
	private void deleteFile() {
		FileOperate.DeleteFileFolder(getSai(1));
		if (isPairEnd()) {
			FileOperate.DeleteFileFolder(getSai(2));
		}
	}
	
	/**
	 * @param force 是否强制建索引，只有当mapping出错的时候才会强制建索引，但是也只会建一次
	 * @return true仅表示是否运行了建索引程序，不代表建索引成功
	 */
	@Override
	protected void makeIndex() {
		List<String> lsCmd = getLsCmdIndex();
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		cmdOperate.run();
	}

	@Override
	protected boolean isIndexExist() {
		return FileOperate.isFileExist(chrFile + ".bwt");
	}

	private List<String> getLsCmdIndex() {
//		linux命令如下 
//	 	bwa index -p prefix -a algoType -c  chrFile
//		-c 是solid用

		List<String> lsCmd = new ArrayList<>();
		lsCmd.add(this.ExePath + "bwa");
		lsCmd.add("index");
		ArrayOperate.addArrayToList(lsCmd, getChrLen());
		lsCmd.add(chrFile);
		return lsCmd;
	}
	
	/**
	 * 根据基因组大小判断采用哪种编码方式
	 * @return 已经在前后预留空格，直接添加上idex就好
	 * 小于500MB的用 -a is
	 * 大于500MB的用 -a bwtsw
	 */
	private String[] getChrLen() {
		long size = (long) FileOperate.getFileSize(chrFile);
		if (size/1024 > 500) {
			return new String[]{"-a", "bwtsw"};
		} else {
			return new String[]{"-a", "is"};
		}
	}

	@Override
	public void setSubVersion(SoftWare bowtieVersion) {
		// TODO Auto-generated method stub
		
	}
	
	public String getVersion() {
		CmdOperate cmdOperate = new CmdOperate(this.ExePath + "bwa");
		cmdOperate.setGetLsErrOut();
		
		List<String> lsInfo = cmdOperate.getLsErrOut();
		String version = lsInfo.get(2).toLowerCase().replace("version:", "").trim();
		return version;
	}
	@Override
	public List<String> getCmdExeStr() {
		List<String> lsCmdResult = new ArrayList<>();
		List<String> lsCmd = getLsCmdAln(true);
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		lsCmdResult.add(cmdOperate.getCmdExeStr());
		if (isPairEnd()) {
			lsCmd = getLsCmdAln(false);
			cmdOperate = new CmdOperate(lsCmd);
			lsCmdResult.add(cmdOperate.getCmdExeStr());
		}
		lsCmd = getLsCmdSam();
		cmdOperate = new CmdOperate(lsCmd);
		lsCmdResult.add(cmdOperate.getCmdExeStr());
		return lsCmdResult;
	}

}
