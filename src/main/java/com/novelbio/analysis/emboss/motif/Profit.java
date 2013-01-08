package com.novelbio.analysis.emboss.motif;

import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * profit scans one or more sequences with a simple frequency matrix 
 * and writes an output file with any high-scoring matches. All possible 
 * ungapped alignments of each sequence to the matrix are scored and 
 * any matches with a score higher than the specified threshold are 
 * written to the output file. The output file includes the name of any 
 * matching sequence found, the start position in the sequence of the 
 * match and the percentage of the maximum possible score.<br><br>
 * <b>默认将序列转换为小写然后扫描<b>
 * @author zong0jie
 *
 */
public class Profit {
//	profit  -infile $infile -sequence $sequence -outfile $outfile -sreverse2  -snucleotide2  -sprotein2 -slower2	
	
	String ExePath = "";
	String inProfit;
	String seqFile;
	
	/** true:Nr false:AA */
	boolean isNr;
	
	/**
	 * @param isNR true 核酸序列，核酸序列默认同时扫描正负链
	 * false 蛋白序列
	 */
	public Profit(boolean isNR) {
		this.isNr = isNR;
	}
	
	/**
	 * 设定samtools所在的文件夹以及待比对的路径
	 * @param exePath 如果在根目录下则设置为""或null
	 */
	public void setExePath(String exePath) {
		if (exePath == null || exePath.trim().equals("")) {
			this.ExePath = "";
		} else {
			this.ExePath = FileOperate.addSep(exePath);
		}
	}
	
	/** 输入的打分矩阵 */
	public void setInProfit(String inProfit) {
		this.inProfit = inProfit;
	}
	
	public void setSeqFile(String seqFile) {
		this.seqFile = seqFile;
	}
	
	/** 获得打分矩阵的路径 */
	private String getInProfit() {
		return " -infile " + CmdOperate.addQuot(inProfit) + " ";
	}
	
	/** 获得要扫描的序列信息 */
	private String getSeqFile() {
		return " -sequence " + CmdOperate.addQuot(seqFile) + " ";
	}
	
	/** 是核酸还是蛋白 */
	private String getSeqType() {
		if (isNr) {
			return " -snucleotide2 ";
		} else {
			return " -sprotein2 ";
		}
	}
	
	public void scaning(String outFile) {
		String cmd = ExePath + "profit -slower2 " + getInProfit() + getSeqFile() + getSeqType() + " -outfile " + CmdOperate.addQuot(outFile);
		CmdOperate cmdOperate = new CmdOperate(cmd,"emboss_profit");
		cmdOperate.run();
	}

}
