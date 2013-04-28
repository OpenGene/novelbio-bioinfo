package com.novelbio.analysis.seq.sam;

import org.apache.log4j.Logger;
import org.broadinstitute.sting.gatk.CommandLineGATK;

import com.novelbio.base.fileOperate.FileOperate;

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
public class GATKCalling {
	private static final Logger logger = Logger.getLogger(GATKCalling.class);
	public static final String SNP = "SNP";
	public static final String INDEL = "INDEL";
	public static final String GENERALPLOIDYSNP = "GENERALPLOIDYSNP";
	public static final String GENERALPLOIDYINDEL = "GENERALPLOIDYINDEL";
	/** 同时查找snp和indel */
	public static final String BOTH = "BOTH";
	/** 输入文件路径+bam文件名 */
	private String inputBam;
	/** 默认和输入文件同路径包括文件名 */
	private String outputFilePath;
	/** 输入ref文件路径+fasta文件名 */
	private String refFile;
	/** 输入文件路径+vcf文件名 */
	private String snpDBVcfFilePath;
	private String glm = GATKCalling.BOTH;
	/** The minimum phred-scaled confidence threshold at which variants should be called */
	private double stand_call_conf = 20.0;
	/** The minimum phred-scaled confidence threshold at which variants should be emitted (and filtered with LowQual if less than the calling threshold) */
	private double stand_emit_conf = 0;
	
	
	public GATKCalling(String inputFilePath, String refFilePath) {
		this.inputBam = inputFilePath;
		this.refFile = refFilePath;
	}

	/** 设置输出路径包括文件名*.bam */
	public void setOutputFilePath(String outputFilePath) {
		this.outputFilePath = outputFilePath;
	}
	
	/** 取得输出路径 */
	public String getOutputFilePath() {
		return outputFilePath;
	}

	/** 设置输入snpDB的文件 */
	public void setSnpDBVcfFilePath(String snpDBVcfFilePath) {
		this.snpDBVcfFilePath = snpDBVcfFilePath;
	}

	/**
	 * Genotype likelihoods calculation model to employ -- SNP is the default
	 * option, while INDEL is also available for calling indels and BOTH is
	 * available for calling both together. The --genotype_likelihoods_model
	 * argument is an enumerated type (Model), which can have one of the
	 * following values:<br>
	 * @param glm 默认 {@link GATKCalling#BOTH} 
	 */
	public void setGlm(String glm) {
		this.glm = glm;
	}
	/** set the minimum phred-scaled confidence threshold at which variants should be called
	 * 默认 20.0
	 *  */
	public void setStand_call_conf(double stand_call_conf) {
		this.stand_call_conf = stand_call_conf;
	}
	/** set the minimum phred-scaled confidence threshold at which variants should be emitted 
	 * (and filtered with LowQual if less than the calling threshold)
	 * 默认 0
	 */
	public void setStand_emit_conf(double stand_emit_conf) {
		this.stand_emit_conf = stand_emit_conf;
	}

	/**
	 * variants calling by GATK<br>
	 * @return 输出文件路径 + 输入文件名.recal.bam
	 */
	public boolean snpCalling() {
		try {
			if (snpDBVcfFilePath == null) {
				String[] params1 = { "-R", refFile, "-T", "UnifiedGenotyper", "-o", outputFilePath, "-I", inputBam, "-stand_call_conf",
						stand_call_conf + "", "-stand_emit_conf", stand_emit_conf + "", "-glm", glm, "-rf", "BadCigar" };
				CommandLineGATK.main(params1);
			} else {
				String[] params1 = { "-R", refFile, "-T", "UnifiedGenotyper", "-o", outputFilePath, "-I", inputBam, "--dbsnp",
						snpDBVcfFilePath, "-stand_call_conf", stand_call_conf + "", "-stand_emit_conf", stand_emit_conf + "", "-glm", glm, "-rf",
						"BadCigar" };
				CommandLineGATK.main(params1);
			}
			return true;
		} catch (Exception e) {
			logger.error("variants calling by GATK error!!!");
			return false;
		}
	}

}
