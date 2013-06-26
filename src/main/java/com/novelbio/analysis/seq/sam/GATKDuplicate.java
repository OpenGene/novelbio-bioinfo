package com.novelbio.analysis.seq.sam;


import net.sf.picard.sam.MarkDuplicates;
import net.sf.samtools.SAMFileReader.ValidationStringency;

import org.apache.log4j.Logger;

import com.novelbio.base.fileOperate.FileOperate;

/**
 * <b>务必排序后进行</b>
 * Picard duplicate 去除重复<br>
 * 
 * 这一步是对每个bam文件的一些重复序列进行一些处理，<br>
 * 这些重复的序列可能是因为PCR扩增的时候引入的一些引物序列，<br>
 * 容易干扰下游结果，这个操作可以通过 <br>
 * picard里面的MarkDuplicates工具实现，<br>
 * MarkDuplicates处理完成不会删除这些reads而是加上一个标签，<br>
 * 带有这些标签的reads在后续处理的时候会被跳过。<br>
 * 
 */
public class GATKDuplicate {
	private static final Logger logger = Logger.getLogger(GATKDuplicate.class);
	/**  输入文件路径+bam文件名 */
	private String inputFilePath;
	/** 默认和输入文件同路径包括文件名 */
	private String outputFilePath;
	/** 临时文件路径包括文件名 */
	private String metricsPath;
	private int maxFileHandlesForReadEndsMap = 8000;
	
	private String VALIDATION_STRINGENCY = ValidationStringency.LENIENT.toString();
	
	/**
	 * @param inputFile 输入文件路径+bam文件名
	 * @param outputFile 一般起名为 文件名 + ".dedup.bam";
	 */
	public GATKDuplicate(String inputFile,String outputFile){
		this.inputFilePath = inputFile;
		this.metricsPath = outputFile + ".metrics";
		// 输出文件全路径包括文件名，一般是文件名 + ".dedup.bam";
		this.outputFilePath = outputFile;
	}
	
	/**
	 *  duplicate  去除重复
	 * @return 输出文件路径 + 输入文件名.dedup.bam
	 */
	public boolean removeDuplicate() {
		try {
			String[] params = { "MAX_FILE_HANDLES_FOR_READ_ENDS_MAP="+maxFileHandlesForReadEndsMap, "INPUT=" + inputFilePath, "OUTPUT=" + outputFilePath,
					"METRICS_FILE=" + metricsPath, "VALIDATION_STRINGENCY=" + VALIDATION_STRINGENCY };
			MarkDuplicates.main(params);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("remove duplicate 去除重复 error!!!");
			return false;
		}
	}
	
	/**
	 * 删除校正过程中的临时文件
	 */
	public void delTempFile(){
		FileOperate.delFile(metricsPath);
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
	public void setMetricsPath(String metricsPath) {
		this.metricsPath = metricsPath;
	}
	/** 设置文件处理map的最大数 */
	public void setMaxFileHandlesForReadEndsMap(int maxFileHandlesForReadEndsMap) {
		this.maxFileHandlesForReadEndsMap = maxFileHandlesForReadEndsMap;
	}
	
}
