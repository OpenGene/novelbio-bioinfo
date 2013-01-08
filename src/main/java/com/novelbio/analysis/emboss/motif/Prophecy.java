package com.novelbio.analysis.emboss.motif;

import com.novelbio.analysis.emboss.motif.MotifEmboss.MotifEmbossScanAlgorithm;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * emboss将alignment产生权重矩阵，然后给Profit来扫描motif的
 * @author zong0jie
 * <b>默认将序列转换为小写然后扫描<b>
 */
public class Prophecy {
//	prophecy -sequence "$filename" -datafile Epprofile  -type G -name novelbio -threshold 75 -outfile "$outFileName"
	
	String ExePath = "";
	String inAlignment;
	String profitName = "novelbio";
	MotifEmbossScanAlgorithm motifEmbossScanAlgorithm = MotifEmbossScanAlgorithm.Gribskov;
	
	boolean isNr;
	/**
	 * @param isNR true 核酸序列，核酸序列默认同时扫描正负链
	 * false 蛋白序列
	 */
	public Prophecy(boolean isNR) {
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
	
	/** 输入的连配文件 */
	public void setInAlignment(String inAlignment) {
		this.inAlignment = inAlignment;
	}
	
	public void setMatrixAlgorithm(MotifEmbossScanAlgorithm motifEmbossScanAlgorithm) {
		this.motifEmbossScanAlgorithm = motifEmbossScanAlgorithm;
	}
	
	private String getMatrixAlgorithm() {
		String result = " -type ";
		if (motifEmbossScanAlgorithm == MotifEmbossScanAlgorithm.Frequency) {
			result = result + "F ";
		} else if (motifEmbossScanAlgorithm == MotifEmbossScanAlgorithm.Gribskov) {
			result = result + "G -datafile Epprofile -open 3.0 -extension 0.3 ";
		} else if (motifEmbossScanAlgorithm == MotifEmbossScanAlgorithm.Henikoff) {
			result = result + "H -datafile EBLOSUM62 -open 3.0 -extension 0.3 ";
		}
		return result;
	}
	
	private String getThreshold() {
		return " -threshold 75 ";
	}
	
	private String getInAlignment() {
		return " -sequence " + CmdOperate.addQuot(inAlignment);
	}
	
	private String getName() {
		return " -name " + profitName + " ";
	}
	
	/** 是核酸还是蛋白 */
	private String getSeqType() {
		if (isNr) {
			return " -snucleotide1 ";
		} else {
			return " -sprotein1 ";
		}
	}
	/** 返回产生的矩阵 */
	public String[] generateProfit(String outFile) {
		String[] result = new String[1];
		if (isNr) {
			result = new String[2];
		}
		result[0] = outFile;
		String cmd = ExePath + "prophecy -slower1 " + getInAlignment() + getMatrixAlgorithm()
				+ getSeqType() + getName() + getThreshold() + " -outfile " + CmdOperate.addQuot(outFile);
		CmdOperate cmdOperate = new CmdOperate(cmd,"emboss_profit");
		cmdOperate.run();
		if (isNr) {
			result[1] = FileOperate.changeFileSuffix(outFile, "_reverse", null);
			cmd = ExePath + "prophecy -slower1 -sreverse1 " + getInAlignment() + getMatrixAlgorithm() + getSeqType()
					+ getName() + getThreshold() + " -outfile " + CmdOperate.addQuot(result[1]);
			cmdOperate = new CmdOperate(cmd,"emboss_profit_reverce");
			cmdOperate.run();
		}
	
		
		return result;
	}


}
