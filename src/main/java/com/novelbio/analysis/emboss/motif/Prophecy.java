package com.novelbio.analysis.emboss.motif;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.analysis.IntCmdSoft;
import com.novelbio.analysis.emboss.motif.MotifEmboss.MotifEmbossScanAlgorithm;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.information.SoftWareInfo;
import com.novelbio.database.model.information.SoftWareInfo.SoftWare;

/**
 * emboss将alignment产生权重矩阵，然后给Profit来扫描motif的
 * @author zong0jie
 * <b>默认将序列转换为小写然后扫描<b>
 */
public class Prophecy implements IntCmdSoft {
//	prophecy -sequence "$filename" -datafile Epprofile  -type G -name novelbio -threshold 75 -outfile "$outFileName"
	
	String ExePath = "";
	String inAlignment;
	String profitName = "novelbio";
	String outFile;
	MotifEmbossScanAlgorithm motifEmbossScanAlgorithm = MotifEmbossScanAlgorithm.Gribskov;
	
	boolean isNr;
	/**
	 * @param isNR true 核酸序列，核酸序列默认同时扫描正负链
	 * false 蛋白序列
	 */
	public Prophecy(boolean isNR) {
		this.isNr = isNR;
		SoftWareInfo softWareInfo = new SoftWareInfo(SoftWare.emboss);
		ExePath = softWareInfo.getExePathRun();
	}
	
	public void setOutFile(String outFile) {
		this.outFile = outFile;
	}
	
	/** 输入的连配文件 */
	public void setInAlignment(String inAlignment) {
		this.inAlignment = inAlignment;
	}
	
	public void setMatrixAlgorithm(MotifEmbossScanAlgorithm motifEmbossScanAlgorithm) {
		this.motifEmbossScanAlgorithm = motifEmbossScanAlgorithm;
	}
	
	private List<String> getMatrixAlgorithm() {
		List<String> lsParam = new ArrayList<>();
		lsParam.add("-type");		
		
		if (motifEmbossScanAlgorithm == MotifEmbossScanAlgorithm.Frequency) {
			lsParam.add("F");
		} else if (motifEmbossScanAlgorithm == MotifEmbossScanAlgorithm.Gribskov) {
			lsParam.add("G");
			lsParam.add("-datafile");
			lsParam.add("Epprofile");
			lsParam.add("-open");
			lsParam.add("3.0");
			lsParam.add("-extension");
			lsParam.add("0.3");
		} else if (motifEmbossScanAlgorithm == MotifEmbossScanAlgorithm.Henikoff) {
			lsParam.add("H");
			lsParam.add("-datafile");
			lsParam.add("EBLOSUM62");
			lsParam.add("-open");
			lsParam.add("3.0");
			lsParam.add("-extension");
			lsParam.add("0.3");
		}
		return lsParam;
	}
	
	private String[] getThreshold() {
		return new String[]{"-threshold", "75"};
	}
	
	private String[] getInAlignment() {
		return new String[]{"-sequence", inAlignment};
	}
	
	private String[] getName() {
		return new String[]{"-name", profitName};
	}
	
	/** 是核酸还是蛋白 */
	private String getSeqType() {
		if (isNr) {
			return "-snucleotide1";
		} else {
			return "-sprotein1";
		}
	}
	/** 返回产生的矩阵 */
	public String[] generateProfit() {
		String[] result = new String[1];
		if (isNr) {
			result = new String[2];
		}
		result[0] = outFile;
		List<String> lsCmd = getlsCmd(true, result[0]);
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		cmdOperate.runWithExp("prophecy error:");
		if (isNr) {
			result[1] = FileOperate.changeFileSuffix(outFile, "_reverse", null);
			List<String> lsCmdTrans = getlsCmd(false, result[1]);
			cmdOperate = new CmdOperate(lsCmdTrans);
			cmdOperate.runWithExp("prophecy error:");
		}
		return result;
	}
	
	/** 反方向的cmd命令 */
	private List<String> getlsCmd(boolean isCis, String out) {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add(ExePath + "prophecy");
		lsCmd.add("-slower1"); 
		
		if (!isCis) {
			lsCmd.add("-sreverse1");
		}
		
		ArrayOperate.addArrayToList(lsCmd, getInAlignment());
		lsCmd.addAll(getMatrixAlgorithm());
		lsCmd.add(getSeqType());
		ArrayOperate.addArrayToList(lsCmd, getName());
		ArrayOperate.addArrayToList(lsCmd, getThreshold());
		lsCmd.add("-outfile"); lsCmd.add(out);
		return lsCmd;
	}
	
	@Override
	public List<String> getCmdExeStr() {
		List<String> lsCmdStr = new ArrayList<>();
		List<String> lsCmdCis = getlsCmd(true,outFile);
		CmdOperate cmdOperate = new CmdOperate(lsCmdCis);
		lsCmdCis.add(cmdOperate.getCmdExeStr());
		if (isNr) {
			List<String> lsCmdTrans = getlsCmd(false, FileOperate.changeFileSuffix(outFile, "_reverse", null));
			cmdOperate = new CmdOperate(lsCmdTrans);
			lsCmdCis.add(cmdOperate.getCmdExeStr());
		}
		return lsCmdStr;
	}
	
}
