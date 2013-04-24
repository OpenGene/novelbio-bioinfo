package com.novelbio.analysis.seq.sam;

import org.apache.log4j.Logger;

import com.novelbio.base.cmd.CmdOperate;

/**
 * bcftools for pileUp
 */
public class BcfTools {
private static final Logger logger = Logger.getLogger(BcfTools.class);
	String inputFile;
	String outputFile;
	/**
	 * @param inputFile mpileup的文件
	 * @param outputFile .vcf文件
	 * @return
	 */
	public BcfTools(String inputFile, String outputFile) {
		this.inputFile = inputFile;
		this.outputFile = outputFile;
	}
	
	public boolean snpCalling() {
		try {
			CmdOperate cmdOperate = new CmdOperate(getCmd(), "bcfCallSnp");
			cmdOperate.run();
			if (cmdOperate.isFinishedNormal()) {
				return true;
			}
		} catch (Exception e) {
			logger.error("bcftools for pileUp error!!!");
		}
		return false;
	}
	
	private String getCmd() {
		String cmd = "bcftools view -Ncvg ";
		cmd = cmd + CmdOperate.addQuot(inputFile) + " > " + CmdOperate.addQuot(outputFile);
		return cmd;
	}
	
}
