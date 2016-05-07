package com.novelbio.analysis.seq.sam;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.novelbio.analysis.IntCmdSoft;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;

/**
 * variants calling by GATK<br>
 * 
 * 这边有一个-rf参数，是用来过滤掉不符合要求的reads，<br>
 * 这边是把包含错误的Cigar字符串的reads给排除掉，<br>
 * 关于Cigar字符串可以参考关于sam文件的说明(The SAM Format Speciﬁcation)，<br>
 * sam文件的第六行就是这边的Cigar字符串，<br>
 * -rf的其他参数可以参考GATK网站Read filters下面的条目<br>
 * http://www.broadinstitute.org/gatk/gatkdocs/<br>
 */
public class GATKCalling implements IntCmdSoft {
	private static final Logger logger = Logger.getLogger(GATKCalling.class);
	
	public static final String SNP = "SNP";
	public static final String INDEL = "INDEL";
	public static final String GENERALPLOIDYSNP = "GENERALPLOIDYSNP";
	public static final String GENERALPLOIDYINDEL = "GENERALPLOIDYINDEL";
	public static final String BOTH = "BOTH";
	String exePath = "";
	/** 输入文件路径+bam文件名 */
	private List<String> lsInputFilePath = new ArrayList<>();
	/** 默认和输入文件同路径包括文件名 */
	private String outVcf;
	/** 输入ref文件路径+fasta文件名 */
	private String refFilePath;
	/** 输入文件路径+vcf文件名 */
	private String snpDBVcfFilePath;
//	private String glm = GATKCalling.BOTH;
	/** The minimum phred-scaled confidence threshold at which variants should be called */
	private double stand_call_conf = 30.0;
	/** The minimum phred-scaled confidence threshold at which variants should be emitted (and filtered with LowQual if less than the calling threshold) */
	private double stand_emit_conf = 10.0;
	
	/** 会过滤掉mapquality小于20的reads */
	private int mmq = -1;
	/** 会过滤掉basequality小于10的位点 */
	private int mbq = -1;
	
	public GATKCalling(String refFilePath) {
		this.refFilePath = refFilePath;
		this.snpDBVcfFilePath = null;
		SoftWareInfo softWareInfo = new SoftWareInfo(SoftWare.GATK);
		 this.exePath = softWareInfo.getExePathRun();
	}
	
	public void addBamFile(String inputFile) {
		lsInputFilePath.add(inputFile);
	}
	
	public void setMbq(int mbq) {
		this.mbq = mbq;
	}
	public void setMmq(int mmq) {
		this.mmq = mmq;
	}
	/**
	 * variants calling by GATK<br>
	 * @return 输出文件路径 + 输入文件名.recal.bam
	 */
	public String callingByGATK() {
		List<String> lsCmd = getLsCmd();
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		cmdOperate.setRedirectOutToTmp(true);
		cmdOperate.addCmdParamOutput(outVcf);
		cmdOperate.runWithExp("GATK error");
		return outVcf;
	}
	
	private List<String> getLsCmd() {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add("java"); lsCmd.add("-Xmx4g"); lsCmd.add("-jar");
		lsCmd.add(exePath + "GenomeAnalysisTK.jar");
		lsCmd.add("-T"); lsCmd.add("HaplotypeCaller");
		ArrayOperate.addArrayToList(lsCmd, getRefFilePath());
		ArrayOperate.addArrayToList(lsCmd, getOutPutPath());
		lsCmd.addAll(getLsInputPath());
		ArrayOperate.addArrayToList(lsCmd, getMbp());
		ArrayOperate.addArrayToList(lsCmd, getMmq());
		ArrayOperate.addArrayToList(lsCmd, getStandCallConf());
		ArrayOperate.addArrayToList(lsCmd, getStandEmitConf());
		ArrayOperate.addArrayToList(lsCmd, getCigar());
		ArrayOperate.addArrayToList(lsCmd, getDBsnpVcf());
		return lsCmd;
	}
	
	private String[] getRefFilePath() {
		return new String[]{"-R", refFilePath};
	}
	
	private String[] getOutPutPath() {
		return new String[]{"-o", outVcf};
	}
	
	private List<String> getLsInputPath() {
		List<String> lsInputCmd = new ArrayList<>();
		for (String bamFile : lsInputFilePath) {
			lsInputCmd.add("-I");
			lsInputCmd.add(bamFile);
		}
		return lsInputCmd;
	}
	
	private String[] getMmq() {
		if (mmq < 0) return null;
		return new String[]{"-mmq", mmq + ""};
	}
	private String[] getMbp() {
		if (mbq < 0) return null;
		return new String[]{"-mbq", mbq + ""};
	}
	
	private String[] getStandCallConf() {
		return new String[]{"-stand_call_conf", stand_call_conf + ""};
	}
	
	private String[] getStandEmitConf() {
		return new String[]{"-stand_emit_conf", stand_emit_conf + ""};
	}
	
	private String[] getCigar() {
		return new String[]{"-rf", "BadCigar"};
	}
	
	private String[] getDBsnpVcf() {
		if (FileOperate.isFileExist(snpDBVcfFilePath)) {
			return new String[]{"--dbsnp", snpDBVcfFilePath};
		}
		return null;
	}
	
	/** 取得输出路径 */
	public String getOutputFilePath() {
		return outVcf;
	}

	/** 设置输出路径包括文件名*.vcf */
	public void setOutVcf(String outVcf) {
		this.outVcf = outVcf;
	}

	/** 设置输入文件路径+vcf文件名 可以设为null,默认为null，只是降低准确度 */
	public void setSnpDBVcfFilePath(String snpDBVcfFilePath) {
		this.snpDBVcfFilePath = snpDBVcfFilePath;
	}

//	/**
//	 * 这个不用改，意思同时找snp和indel<br>
//	 * Genotype likelihoods calculation model to employ -- SNP is the default
//	 * option, while INDEL is also available for calling indels and BOTH is
//	 * available for calling both together. The --genotype_likelihoods_model
//	 * argument is an enumerated type (Model), which can have one of the
//	 * following values:<br>
//	 * {@link #setGlm(GATKCalling.BOTH)} <br>
//	 * BOTH (default)
//	 */
//	public void setGlm(String glm) {
//		this.glm = glm;
//	}
	/** set the minimum phred-scaled confidence threshold at which variants should be called */
	public void setStand_call_conf(double stand_call_conf) {
		this.stand_call_conf = stand_call_conf;
	}
	/** set the minimum phred-scaled confidence threshold at which variants should be emitted (and filtered with LowQual if less than the calling threshold) */
	public void setStand_emit_conf(double stand_emit_conf) {
		this.stand_emit_conf = stand_emit_conf;
	}

	@Override
	public List<String> getCmdExeStr() {
		List<String> lsResult = new ArrayList<>();
		List<String> lsCmd = getLsCmd();
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		lsResult.add(cmdOperate.getCmdExeStr());
		return lsResult;
	}

	
}
