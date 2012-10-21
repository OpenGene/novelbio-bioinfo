package com.novelbio.analysis.annotation.blast;

import java.util.HashMap;

import org.apache.ibatis.migration.commands.NewCommand;
import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqFastaHash;
import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.generalConf.NovelBioConst;

/**
 * 自动化进行blast的方法
 * 考虑将路径放入数据库中
 * 调用NCBI的blast程序进行blast分析，考虑将结果自动导入数据库
 * @author zong0jie
 *
 */
public class BlastNBC {
	private static Logger logger = Logger.getLogger(BlastNBC.class);
	String blastAll = "blastall ";
	String formatDB = "formatdb ";
	String blastInputType = "-p ";
	String queryFasta = "";
	/**待比对的数据库，如果是fasta文件，则会自动建索引*/
	String databaseSeq = "";
	BlastType blastType = BlastType.tblastn;

	/**
	 * @return blast输出结果的哈希表
	 * key：说明
	 * value：输出结果的数字，如0为正常，8为简写。可直接用于设置本类的参数
	 */
	public static HashMap<String, Integer> getHashResultType() {
		HashMap<String, Integer> hashBlastType = new HashMap<String, Integer>();
		hashBlastType.put("Normal Type With Alignment", 0);
		hashBlastType.put("Simple Table", 8);
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
	}	
	/**
	 * 待比对的数据库，如果是fasta文件，则会自动建索引
	 * @param databaseSeq
	 */
	public void setDatabaseSeq(String databaseSeq) {
		this.databaseSeq = databaseSeq;
	}
	String resultFile = "";
	/**
	 * 输出文件
	 * @param resultFile
	 */
	public void setResultFile(String resultFile) {
		this.resultFile = resultFile;
	}
	int cpuNum = 2;
	/**
	 * 设定cpu使用数量，感觉设定了没用
	 * 默认为2
	 * @param cpuNum
	 */
	public void setCpuNum(int cpuNum) {
		this.cpuNum = cpuNum;
	}
	int resultType = 8;
	/**
	 * 常规模式为0
	 * 精简模式为8
	 * 具体看文档
	 * @param resultType 默认为8
	 */
	public void setResultType(int resultType) {
		this.resultType = resultType;
	}
	/**
	 * 显示几个结果
	 */
	int resultSeqNum = 2;
	/**
	 * @param resultSeqNum 显示几个结果
	 */
	public void setResultSeqNum(int resultSeqNum) {
		this.resultSeqNum = resultSeqNum;
	}
	/**
	 * 显示几个比对结果
	 */
	int resultAlignNum = 2;
	/**
	 * 输出几个比对结果，当setResultType为8的时候好像不起作用
	 * @param resultAlignNum
	 */
	public void setResultAlignNum(int resultAlignNum) {
		this.resultAlignNum = resultAlignNum;
	}
	/** @param evalue 最低相似度，越小相似度越高。最好是0到1之间，默认0.1。一般不用改  */
	double evalue = 0.1;
	/** @param evalue 最低相似度，越小相似度越高。最好是0到1之间，默认0.1。一般不用改 */
	public void setEvalue(double evalue) {
		this.evalue = evalue;
	}
	/**
	 * 将指定的序列对目标序列进行blast
	 * @return false blast失败
	 * true blast成功
	 */
	public boolean blast() {
		if (indexExisted() == null) {
			return false;
		}
		//索引是否存在
		if (!indexExisted()) {
			//是否成功构建blast数据库
			if (!formatDB()) {
				return false;
			}
		}
		String cmd = "perl " + NovelBioConst.BLAST_NCBI_SCRIPT + blastAll + blastInputType + blastType.toString()
				+ "-i " + CmdOperate.addQuot(queryFasta) + " -d " + CmdOperate.addQuot(databaseSeq) + " -o " + CmdOperate.addQuot(resultFile)
				+ " -a " + cpuNum + getFilter() + " -e " + evalue + " -m " + resultType + " -v "+resultSeqNum + " -b " + resultAlignNum + " --path " + NovelBioConst.BLAST_NCBI_PATH;
		CmdOperate cmdOperate = new CmdOperate(cmd,"blast");
		cmdOperate.run();
		return true;
	}
	/**
	 * database序列是核酸还是蛋白
	 * @return
	 */
	private String getSeqTypePro() {
		String seqTypePro = "";
		int seqTypeFlag = SeqHash.getSeqType(databaseSeq);
		
		if (seqTypeFlag == SeqFasta.SEQ_PRO)
			seqTypePro = "T ";
		else if(seqTypeFlag == SeqFasta.SEQ_DNA)
			seqTypePro = "F ";
		else {
			logger.error("databaseSeq 序列出现未知字符");
		}
		return seqTypePro;
	}
	/**
	 * 对目标序列建索引
	 * 如果索引不存在，一般会自动建索引。
	 * 如果索引建错了，才需要用其重建
	 */
	private boolean formatDB() {
		String seqTypePro = getSeqTypePro();
		if (seqTypePro == null) {
			return false;
		}
		String cmd = "perl " + NovelBioConst.BLAST_NCBI_SCRIPT + formatDB + " -i " + databaseSeq + " -p "+ seqTypePro + " -o T " 
				+ "--path " + NovelBioConst.BLAST_NCBI_PATH;
		CmdOperate cmdOperate = new CmdOperate(cmd,"blastFormatDB");
		cmdOperate.run();
		return true;
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
	}	
	
}
