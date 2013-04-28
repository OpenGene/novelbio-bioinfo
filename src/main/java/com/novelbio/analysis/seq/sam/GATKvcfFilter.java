package com.novelbio.analysis.seq.sam;

import org.apache.log4j.Logger;
import org.broadinstitute.sting.gatk.CommandLineGATK;

import com.novelbio.base.cmd.CmdOperate;

/** 用GATK来过滤低质量的vcf文件 */
public class GATKvcfFilter {
	private static final Logger logger = Logger.getLogger(GATKRecalibrate.class);
	
	/** 输入vcf文件名 */
	private String inVcfFile;
	/** 输出过滤好的vcf文件 */
	private String outFilteredVcvFile;
	
	String refFile;
	//////////////// 过滤指标 /////////////////////////
	/** 平均质量 */
	int meanqual_Less = 20;
	/** QD的值 */
	double QD_Less = 20.0;
	double readPosRankSum_Less = -8.0;
	double FS_More = 10.0;
	/** 默认以上指标取或(||)而不是取与(&&)  */
	boolean and = false;
	/////////////////////////////////////////////////////////
	
	public GATKvcfFilter(String inVcfFile, String refFile) {
		this.inVcfFile = inVcfFile;
		this.refFile = refFile;
	}
	
	public void setInVcfFile(String inVcfFile) {
		this.inVcfFile = inVcfFile;
	}
	
	/**
	 * 我们为了校正前做的一次初步的variants calling，<br>
	 * 然后筛选出我们认为最可靠的那些位点作为参考位点，<br>
	 * 用这部分的数据再来进行校正
	 * 
	 * 结果设为此对象的snpDBVcfFilePath
	 */

	public boolean callingBeforeRecalibrate() {
		try {
			// 筛选
			String[] params = { "-R", refFile, "-T", "VariantFiltration", "--filterExpression",
					getFilterExpression(),
					"--missingValuesInExpressionsShouldEvaluateAsFailing", "--variant", inVcfFile, "--logging_level", "ERROR", "-o",
					outFilteredVcvFile};
			CommandLineGATK.main(params);
			return true;
		} catch (Exception e) {
			logger.error("callingForRecalibrate error!!!");
			return false;
		}
	}
	
	private String getFilterExpression() {
		String cmd = "";
		String operate = "";
		if (and) {
			operate = " && ";
		} else {
			operate = " || ";
		}
		cmd = "QD <" + QD_Less + operate + "ReadPosRankSum < " + readPosRankSum_Less 
				+ operate + "FS > " + FS_More + operate + "QUAL < " + meanqual_Less;
		cmd = CmdOperate.addQuot(cmd);
		return cmd;
	}
}
