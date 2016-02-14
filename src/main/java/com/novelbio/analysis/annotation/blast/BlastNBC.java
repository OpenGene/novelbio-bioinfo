package com.novelbio.analysis.annotation.blast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.novelbio.analysis.IntCmdSoft;
import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqFastaHash;
import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.analysis.seq.fasta.StrandType;
import com.novelbio.base.PathDetail;
import com.novelbio.base.StringOperate;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.cmd.ExceptionCmd;
import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.geneanno.BlastInfo;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;

/**
 * 自动化进行blast的方法
 * 考虑将路径放入数据库中
 * 调用NCBI的blast程序进行blast分析，考虑将结果自动导入数据库
 * @author zong0jie
 *
 */
public class BlastNBC implements IntCmdSoft {
	private static final Logger logger = Logger.getLogger(BlastNBC.class);
	public static final int ResultType_Simple = 6;
	public static final int ResultType_Normal = 0;
	
	String exePath;
	final String formatDB = "makeblastdb";
	
	String queryFasta = "";
	/** 只有设定queryFasta为seqfasta时才会删除该文件 */
	boolean deleteQueryFasta = false;
	
	/**待比对的数据库，如果是fasta文件，则会自动建索引*/
	String databaseSeq = "";
	/** 数据库是nr还是pro */
	String seqTypePro;
	BlastType blastType = BlastType.tblastn;
	/** query的序列是否很短，一般在探针序列blast的时候才使用，目前只能用于blastp和blastn */
	boolean isShortQuerySeq = false;
	
	String resultFile = "";
	int cpuNum = 2;
	int resultType = ResultType_Simple;
	/** @param evalue 最低相似度，越小相似度越高。最好是0到1之间，默认0.1。一般不用改  */
	double evalue = 0.1;
	/** 显示几个比对结果 */
	int resultAlignNum = 2;
	/**
	 * Query strand(s) to search against database/subject. Choice of both, minus, or plus.
	 * 意思比对到reference的正链还是反链。
	 * 所在的blast type：
	 * blastn, blastx, tblastx
	 */
	StrandType strandType = StrandType.isoForward;
	/**
	 * 显示几个结果
	 */
	int resultSeqNum = 2;
	
	public BlastNBC() {
		SoftWareInfo softWareInfo = new SoftWareInfo(SoftWare.blast);
		exePath = softWareInfo.getExePathRun();
	}
	/**
	 * @return blast输出结果的哈希表
	 * key：说明
	 * value：输出结果的数字，如0为正常，8为简写。可直接用于设置本类的参数
	 */
	public static HashMap<String, Integer> getHashResultType() {
		HashMap<String, Integer> hashBlastType = new LinkedHashMap<String, Integer>();
		hashBlastType.put("Simple Table", BlastNBC.ResultType_Simple);
		hashBlastType.put("Normal Type With Alignment", BlastNBC.ResultType_Normal);
		return hashBlastType;
	}
	
	/**
	 * 设定blast的模式
	 * @param blastType
	 * BLAST_TBLASTN等
	 */
	public void setBlastType(BlastType blastType) {
		this.blastType = blastType;
	}
	/**
	 * 待query的fasta文件
	 * @param queryFasta
	 */
	public void setQueryFastaFile(String queryFasta) {
		this.queryFasta = queryFasta;
		this.deleteQueryFasta = false;
	}
	/** 待比对的序列，和{@link #setQueryFastaFile(String)} 两者取一 */
	public void setQueryFasta(SeqFasta seqFasta) {
		this.queryFasta = PathDetail.getTmpPathWithSep() + "seqToBlast" + DateUtil.getDateAndRandom() + ".fa";
		this.deleteQueryFasta = true;
		TxtReadandWrite txtWrite = new TxtReadandWrite(queryFasta, true);
		txtWrite.writefileln(seqFasta.toStringNRfasta());
		txtWrite.close();
	}
	/**
	 * 待比对的数据库，如果是fasta文件，则会自动建索引
	 * @param databaseSeq
	 */
	public void setSubjectSeq(String databaseSeq) {
		this.databaseSeq = databaseSeq;
		seqTypePro = getSeqTypePro();
	}
	/**
	 * database序列是核酸还是蛋白
	 * @return
	 */
	private String getSeqTypePro() {
		String seqTypePro = null;
		int seqTypeFlag = SeqHash.getSeqType(databaseSeq);
		
		if (seqTypeFlag == SeqFasta.SEQ_PRO)
			seqTypePro = "prot";
		else if(seqTypeFlag == SeqFasta.SEQ_DNA)
			seqTypePro = "nucl";
		else {
			throw new ExceptionCmd("blast error:\n" + "cannot detect the sequence type.");
		}
		return seqTypePro;
	}
	/**
	 * 输出文件
	 * @param resultFile
	 */
	public void setResultFile(String resultFile) {
		this.resultFile = resultFile;
	}
	/**
	 * Query strand(s) to search against database/subject. Choice of both, minus, or plus.
	 * 意思比对到reference的正链还是反链。
	 * 所在的blast type：
	 * blastn, blastx, tblastx
	 */
	 public void setStrandType(StrandType strandType) {
		this.strandType = strandType;
	}
	/**
	 * 设定cpu使用数量，感觉设定了没用
	 * 默认为2
	 * @param cpuNum
	 */
	public void setCpuNum(int cpuNum) {
		this.cpuNum = cpuNum;
	}
	public void setShortQuerySeq(boolean isShortQuerySeq) {
		this.isShortQuerySeq = isShortQuerySeq;
	}
	/**
	 * 常规模式为{@link #ResultType_Normal}
	 * 精简模式为{@link #ResultType_Simple}
	 * 具体看文档
	 * @param resultType 默认为{@link #ResultType_Simple}
	 */
	public void setResultType(int resultType) {
		this.resultType = resultType;
	}

	/**
	 * @param resultSeqNum 显示几个结果
	 */
	public void setResultSeqNum(int resultSeqNum) {
		this.resultSeqNum = resultSeqNum;
	}

	/**
	 * 输出几个比对结果，当setResultType为8的时候好像不起作用
	 * @param resultAlignNum
	 */
	public void setResultAlignNum(int resultAlignNum) {
		this.resultAlignNum = resultAlignNum;
	}

	/** @param evalue 最低相似度，越小相似度越高。最好是0到1之间，默认0.1。一般不用改 */
	public void setEvalue(double evalue) {
		this.evalue = evalue;
	}
	/** 返回blast得到的结果文件 */
	public String getResultFile() {
		return resultFile;
	}
	/** 返回blast得到的结果信息 */
	public List<BlastInfo> getResultInfo() {
		return BlastInfo.readBlastFile(resultFile);
	}
	public int getResultType() {
		return resultType;
	}
	/**
	 * 将指定的序列对目标序列进行blast
	 * @return false blast失败
	 * true blast成功
	 */
	public boolean blast() {
		Boolean isIndexExist = indexExisted();
		if (isIndexExist == null) {
			return false;
		}
		//索引是否存在
		if (!isIndexExist) {
			//是否成功构建blast数据库
			if (!formatDB()) {
				return false;
			}
		}
		CmdOperate cmdOperate = new CmdOperate(getLsCmdBlast());
		cmdOperate.setInputFile(queryFasta);
		cmdOperate.run();
		if (deleteQueryFasta) {
			FileOperate.DeleteFileFolder(queryFasta);
		}
		if (!cmdOperate.isFinishedNormal()) {
			throw new ExceptionCmd("blast error:\n" + cmdOperate.getCmdExeStrReal() + "\n" + cmdOperate.getErrOut());
		}
		return true;
	}
	
	private List<String> getLsCmdBlast() {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add(exePath + blastType.toString());
		ArrayOperate.addArrayToList(lsCmd, getDB());
		ArrayOperate.addArrayToList(lsCmd, getBlastStrand());
		ArrayOperate.addArrayToList(lsCmd, getQuery());
		ArrayOperate.addArrayToList(lsCmd, getThread());
		ArrayOperate.addArrayToList(lsCmd, getEvalue());
		ArrayOperate.addArrayToList(lsCmd, getBlastTask());
		ArrayOperate.addArrayToList(lsCmd, getResultTypeCmd());
		ArrayOperate.addArrayToList(lsCmd, getBlastNum());
		lsCmd.add(">");
		lsCmd.add(resultFile);
		return lsCmd;
	}
	
	/** 只有当blastp和blastn时才有用，用于特别短序列的blast
	 * @param nr 是否为blastn
	 * @return
	 */
	private String[] getBlastTask() {
		if (blastType != BlastType.blastn && blastType != BlastType.blastp) {
			return new String[0];
		}
		
		if (isShortQuerySeq) {
			if (blastType == BlastType.blastn) {
				return new String[]{"-task", "blastn-short"};
			} else {
				return new String[]{"-task", "blastp-short"};
			}
		}
		return new String[0];
	}
	
	private String[] getBlastStrand() {
		if (strandType == null) return null;
		
		String strand = null;
		if (blastType == BlastType.blastn || blastType == BlastType.blastx || blastType == BlastType.blastx) {
			if (strandType == StrandType.cis) {
				strand = "plus";
			} else if (strandType == StrandType.trans) {
				strand = "minus";
			}
		}
		if (!StringOperate.isRealNull(strand)) {
			return new String[]{"-strand", strand};
		}
		return null;
	}
	
	private String[] getDB() {
		return new String[]{ "-db", databaseSeq};
	}
	private String[] getQuery() {
		return new String[]{"-query", "-"};
	}

	private String[] getThread() {
		return new String[]{"-num_threads", cpuNum+""};
	}
	private String[] getEvalue() {
		return new String[]{"-evalue", evalue+""};
	}
	private String[] getResultTypeCmd() {
		String resultFormat = "0";
		if (resultType == ResultType_Normal) {
			resultFormat = "0";
		} else if (resultType == ResultType_Simple) {
			resultFormat = "6";
		}
		return new String[]{"-outfmt", resultFormat};
	}
	private String[] getBlastNum() {
		return new String[]{"-num_descriptions", resultSeqNum+"", "-num_alignments", resultAlignNum+""};
	}

	/**
	 * 对目标序列建索引
	 * 如果索引不存在，一般会自动建索引。
	 * 如果索引建错了，才需要用其重建
	 */
	private boolean formatDB() {
		if (seqTypePro == null) {
			return false;
		}
		CmdOperate cmdOperate = new CmdOperate(getLsCmdFormatDB());
		cmdOperate.runWithExp("make database error:");
		return true;
	}
	
	private List<String> getLsCmdFormatDB() {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add(exePath + formatDB);
		lsCmd.add("-in"); lsCmd.add(databaseSeq);
		lsCmd.add("-dbtype"); lsCmd.add(seqTypePro);
		lsCmd.add("-parse_seqids");
		return lsCmd;
	}
	
	/**
	 * 看索引是否存在
	 * @return true 存在， false 不存在 null 序列有问题
	 */
	private Boolean indexExisted() {
		boolean indexExist = false;
		int seqType = SeqHash.getSeqType(databaseSeq);
		if (seqType == SeqFasta.SEQ_DNA || seqType == SeqFasta.SEQ_RNA) {
			indexExist = FileOperate.isFileExist(databaseSeq + ".nhr") && FileOperate.isFileExist(databaseSeq + ".nin") 
					&& FileOperate.isFileExist(databaseSeq + ".nog") && FileOperate.isFileExist(databaseSeq + ".nsd")
					 && FileOperate.isFileExist(databaseSeq + ".nsi") && FileOperate.isFileExist(databaseSeq + ".nsq");
		}
		else if (seqType == SeqFasta.SEQ_PRO) {
			indexExist = FileOperate.isFileExist(databaseSeq + ".phr") && FileOperate.isFileExist(databaseSeq + ".pin") 
					&& FileOperate.isFileExist(databaseSeq + ".pog") && FileOperate.isFileExist(databaseSeq + ".psd")
					 && FileOperate.isFileExist(databaseSeq + ".psi") && FileOperate.isFileExist(databaseSeq + ".psq");
		}
		else {
			logger.error("序列出现未知字符");
			return null;
		}
		return indexExist;
	}
	/**
	 * 用来屏蔽简单重复和低复杂度序列的参数，有T和F两个选项，选择“T”，则
		程序在比对过程中会屏蔽掉query序列中的简单重复和低复杂度序列；选择
		“F”则不会屏蔽。NCBI的blast程序默认值是“T”，我们默认是F，也就是返回 -F F
	 *但是RNA的tblastx走NCBI默认，也就是返回"",否则会报错
	 */
	private String getFilter() {
		if (this.blastType == BlastType.tblastx) {
			return " ";
		}
		return " -F F ";
	}
	/**
	 * 给定fasta格式的文件，设定NCBI的ID为正则表达式，将Fasta格式的ID挑选出来
	 * 返回一个新的fasta文件
	 */
	public static void getFasta(String fastaFile) {
		SeqFastaHash seqFastaHash = new SeqFastaHash(fastaFile, "\\w+_\\d+", false, false, false);
		seqFastaHash.writeToFile(FileOperate.changeFileSuffix(fastaFile, "_cleanID", null));
		seqFastaHash.close();
	}
	
	@Override
	public List<String> getCmdExeStr() {
		List<String> lsCmds = new ArrayList<>();
		//索引是否存在
		if (!indexExisted()) {
			CmdOperate cmdOperate = new CmdOperate(getLsCmdFormatDB());
			lsCmds.add(cmdOperate.getCmdExeStr());
		}
		CmdOperate cmdOperate = new CmdOperate(getLsCmdBlast());
		lsCmds.add(cmdOperate.getCmdExeStr());
		// TODO Auto-generated method stub
		return lsCmds;
	}
	
	/** 返回是否用链特异性的blast */
	public static Map<String, StrandType> getMapStrandType2Value() {
		Map<String, StrandType> mapKey2StrandType = new LinkedHashMap<>();
		mapKey2StrandType.put("Both", StrandType.isoForward);
		mapKey2StrandType.put("plus", StrandType.cis);
		mapKey2StrandType.put("minus", StrandType.trans);
		return mapKey2StrandType;
	}
	
}
