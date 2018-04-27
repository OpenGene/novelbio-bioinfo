package com.novelbio.analysis.emboss.motif;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.analysis.IntCmdSoft;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.information.SoftWareInfo;
import com.novelbio.database.model.information.SoftWareInfo.SoftWare;

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
public class Profit implements IntCmdSoft {
//	profit  -infile $infile -sequence $sequence -outfile $outfile -sreverse2  -snucleotide2  -sprotein2 -slower2	
	
	String ExePath = "";
	String inProfit;
	String seqFile;
	String outFile;
	/** true:Nr false:AA */
	boolean isNr;
	
	/**
	 * @param isNR true 核酸序列，核酸序列默认同时扫描正负链
	 * false 蛋白序列
	 */
	public Profit(boolean isNR) {
		this.isNr = isNR;
		SoftWareInfo softWareInfo = new SoftWareInfo(SoftWare.emboss);
		ExePath = softWareInfo.getExePathRun();
	}
	
	/** 输入的打分矩阵 */
	public void setInProfit(String inProfit) {
		this.inProfit = inProfit;
	}
	
	public void setSeqFile(String seqFile) {
		this.seqFile = seqFile;
	}
	
	public void setOutFile(String outFile) {
		this.outFile = outFile;
	}
	
	/** 获得打分矩阵的路径 */
	private String[] getInProfit() {
		return new String[]{"-infile", inProfit};
	}
	
	/** 获得要扫描的序列信息 */
	private String[] getSeqFile() {
		return new String[]{"-sequence", seqFile};
	}
	
	/** 是核酸还是蛋白 */
	private String getSeqType() {
		if (isNr) {
			return "-snucleotide2";
		} else {
			return "-sprotein2";
		}
	}
	
	/** 返回cmd命令的命令行 */
	public List<String> getCmdExeStr() {
		List<String> lsCmd = new ArrayList<>();
		CmdOperate cmdOperate = new CmdOperate(getLsCmd());
		lsCmd.add(cmdOperate.getCmdExeStr());
		return lsCmd;
	}
	
	public void scaning() {
		CmdOperate cmdOperate = new CmdOperate(getLsCmd());
		cmdOperate.runWithExp("profit error:");
	}
	
	private List<String> getLsCmd() {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add(ExePath + "profit"); lsCmd.add("-slower2");//变小写
		ArrayOperate.addArrayToList(lsCmd, getInProfit());
		ArrayOperate.addArrayToList(lsCmd, getSeqFile());
		lsCmd.add(getSeqType());
		lsCmd.add("-outfile"); lsCmd.add(outFile);
		return lsCmd;
	}
}
