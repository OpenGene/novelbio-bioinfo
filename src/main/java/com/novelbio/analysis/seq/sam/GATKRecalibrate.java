package com.novelbio.analysis.seq.sam;

import java.util.Date;

import org.apache.log4j.Logger;
import org.broadinstitute.sting.gatk.CommandLineGATK;

import com.novelbio.base.fileOperate.FileOperate;

/**
 * recalibrate 重新校正<br>
 * 
 * 这一步是对realign得到的结果进行校正，<br>
 * 因为mapping得到的结果的分值往往不能反应真实情况下的分值,<br>
 * 而只有尽量消除这种偏态才能保证得到的结果的可靠性与正确性，<br>
 * 所以如果情况允许的条件下都需要进行这样的一个校正步骤。<br>
 * 这样最后我们就得到一个最好的处理结果。<br>
 * 校正这一步对低覆盖度的数据比较重要，可以消除很多假阳性；<br>
 * 对于高覆盖度(10x~)的数据可以不做这一步，<br>
 * 这一步最重要的是有已知的可靠位点作为参考，<br>
 * human因为研究的比较多，所以有很多这样的数据(包括dbsnp的数据等等)，<br>
 * 而其他的物种则可能缺少这些数据，<br>
 * 这个时候我们可以考虑先做一次初步的variants calling，<br>
 * 然后筛选出我们认为最可靠的那些位点作为参考位点，<br>
 * 用这部分的数据再来进行校正
 */ 
public class GATKRecalibrate {
	private static final Logger logger = Logger.getLogger(GATKRecalibrate.class);
	
	/** 输入文件路径+bam文件名 */
	private String inputFilePath;
	/** 默认和输入文件同路径包括文件名 */
	private String outputFilePath;
	/** 输入ref文件路径+fasta文件名 */
	private String refFilePath;
	/** 输入文件路径+vcf文件名 */
	private String snpDBVcfFilePath;
	/** 临时文件路径包括文件名 */
	private String grpPath;
	
	public GATKRecalibrate(String inputFilePath,String refFilePath,String snpDBVcfFilePath){
		this.inputFilePath = inputFilePath;
		this.refFilePath = refFilePath;
		this.snpDBVcfFilePath = snpDBVcfFilePath;
		// 得到输入的文件名
		String inputFileName = FileOperate.getFileNameSep(inputFilePath)[0];
		// 临时文件路径包括文件名
		this.grpPath = FileOperate.addSep(outputFilePath) + new Date().getTime() + "_data.grp";
		// 输出文件全路径包括文件名
		this.outputFilePath = FileOperate.addSep(outputFilePath) + inputFileName + ".recal.bam";
	}
    /** 
	 * recalibrate 重新校正
	 * @return 输出文件路径 + 输入文件名.recal.bam
	 */
	public String recalibrate() {
		try {
			// 生成校验文件
			String[] params1 = { "-R", refFilePath, "-T", "BaseRecalibrator", "-o", grpPath, "-I", inputFilePath, "-knownSites", snpDBVcfFilePath };
			CommandLineGATK.main(params1);

			// 进行校正，生成校正后的文件 *.recal.bam
			String[] params2 = { "-R", refFilePath, "-T", "PrintReads", "-o", outputFilePath, "-I", inputFilePath, "-BQSR", grpPath };
			CommandLineGATK.main(params2);
			return outputFilePath;
		} catch (Exception e) {
			logger.error("recalibrate 重新校正 error!!!");
			return null;
		}
	}
	
	/**
	 * 我们为了校正前做的一次初步的variants calling，<br>
	 * 然后筛选出我们认为最可靠的那些位点作为参考位点，<br>
	 * 用这部分的数据再来进行校正
	 * 
	 * 结果设为此对象的snpDBVcfFilePath
	 */

	public String callingBeforeRecalibrate(String inputFilePath, String outputFilePath, String snpDBVcfFilePath, String refFilePath) {
		// variants calling by GATK
		// 这边有一个-rf参数，是用来过滤掉不符合要求的reads，这边是把包含错误的Cigar字符串的reads给排除掉，
		// 关于Cigar字符串可以参考关于sam文件的说明(The SAM Format
		// Speciﬁcation)，sam文件的第六行就是这边的Cigar字符串，
		// -rf的其他参数可以参考GATK网站Read
		// filters下面的条目http://www.broadinstitute.org/gatk/gatkdocs/
		//TODO 最后一个路径需要改一下
		GATKCalling gatkCalling = new GATKCalling(inputFilePath, refFilePath,"/home/novelbio/桌面");
		gatkCalling.setSnpDBVcfFilePath(snpDBVcfFilePath);
		String gatkVcfFilePath = gatkCalling.callingByGATK();

		//TODO variants calling by samtools
		//String samToolsVcfFilePath = callingBySamTools(inputFilePath, outputFilePath, snpDBVcfFilePath, refFilePath);
		String samToolsVcfFilePath = null;
		// 得到输入的文件名
		String inputFileName = FileOperate.getFileNameSep(inputFilePath)[0];
		// 输出文件全路径包括文件名
		String rawOutputFilePath = FileOperate.addSep(outputFilePath) + inputFileName + ".concordance.raw.vcf";
		String fltOutputFilePath = FileOperate.addSep(outputFilePath) + inputFileName + ".concordance.flt.vcf";
		// 选取GATK和samtools一致的结果
		String intersectionOutputFilePath = GATKUtils.selectUnionFrom(gatkVcfFilePath, samToolsVcfFilePath, rawOutputFilePath);
		// TODO 筛选上面得到的结果,计算平均值
		long meanqual = 20;
		try {
			// 筛选
			String[] params = { "-R", refFilePath, "-T", "VariantFiltration", "--filterExpression",
					"\" QD < 20.0 || ReadPosRankSum < -8.0 || FS > 10.0 || QUAL < \"" + meanqual + "\"", "--filterName", "LowQualFilter",
					"--missingValuesInExpressionsShouldEvaluateAsFailing", "--variant", intersectionOutputFilePath, "--logging_level", "ERROR", "-o",
					fltOutputFilePath};
			CommandLineGATK.main(params);
			this.snpDBVcfFilePath = fltOutputFilePath;
			return fltOutputFilePath;
		} catch (Exception e) {
			logger.error("callingForRecalibrate error!!!");
			return null;
		}
	}
	
	/**
	 * 删除校正过程中的临时文件
	 */
	public void delTempFile(){
		FileOperate.delFile(grpPath);
	}
	
	/** 取得输出路径 */
	public String getOutputFilePath() {
		return outputFilePath;
	}
	/** 设置输出路径包括文件名*.bam */
	public void setOutputFilePath(String outputFilePath) {
		this.outputFilePath = outputFilePath;
	}
	/** 设置临时文件路径包括文件名 */
	public void setGrpPathh(String grpPath) {
		this.grpPath = grpPath;
	}
	/** 设置输入文件路径+vcf文件名 */
	public void setSnpDBVcfFilePath(String snpDBVcfFilePath) {
		this.snpDBVcfFilePath = snpDBVcfFilePath;
	}
	
}
