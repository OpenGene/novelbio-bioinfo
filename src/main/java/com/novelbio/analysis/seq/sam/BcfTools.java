package com.novelbio.analysis.seq.sam;

import java.util.ArrayList;
import java.util.List;

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
			CmdOperate cmdOperate = new CmdOperate(getCmd());
			cmdOperate.run();
			if (cmdOperate.isFinishedNormal()) {
				return true;
			}
		} catch (Exception e) {
			logger.error("bcftools for pileUp error!!!");
		}
		return false;
	}
	
	private List<String> getCmd() {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add("bcftools"); lsCmd.add("view");
		lsCmd.add("-Ncvg"); lsCmd.add(inputFile);
		lsCmd.add(">"); lsCmd.add(outputFile);
		return lsCmd;
	}
	
}
