package com.novelbio.analysis.seq.sam;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.novelbio.base.dataOperate.DateUtil;
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
	private String inBamFile;
	/** 默认和输入文件同路径包括文件名 */
	private String outBamFile;
	/** 输入ref文件路径+fasta文件名 */
	private String referenceFile;
	/** 输入文件路径+vcf文件名 */
	private Set<String> setSnpDBVcfFilePath = new HashSet<String>();
	/** 临时文件路径包括文件名 */
	private String grpPath;
	
	public GATKRecalibrate(String inputBamFile, String refFile){
		this.inBamFile = inputBamFile;
		this.referenceFile = refFile;
	}
	
	/** 设定输出结果 */
	public void setOutBamFile(String outBamFile) {
		// 得到输入的文件名
		// 临时文件路径包括文件名
		this.grpPath = FileOperate.changeFileSuffix(outBamFile, DateUtil.getDateAndRandom(), "grp");
		// 输出文件全路径包括文件名
		this.outBamFile = outBamFile;
	}
	
	/**
	 * @param snpVcfFile 已知的snpdb等文件，用于校正。可以添加多个
	 */
	public void addSnpVcfFile(String snpVcfFile) {
		if (FileOperate.isFileExistAndBigThanSize(snpVcfFile, 0)) {
			setSnpDBVcfFilePath.add(snpVcfFile);
		}
	}
	
	/**
	 * @param snpVcfFile 已知的snpdb等文件，用于校正。可以添加多个
	 */
	public void setSnpVcfFile(Collection<String> colSnpVcfFile) {
		if (colSnpVcfFile == null || colSnpVcfFile.size() == 0) {
			return;
		}
		setSnpDBVcfFilePath.clear();
		for (String snpVcfFile : colSnpVcfFile) {
			if (FileOperate.isFileExistAndBigThanSize(snpVcfFile, 0)) {
				setSnpDBVcfFilePath.add(snpVcfFile);
			}
		}
	}
	
    /** 
	 * recalibrate 重新校正
	 * @return 输出文件路径 + 输入文件名.recal.bam
	 */
	public boolean recalibrate() {
		try {
			// 生成校验文件
			String[] params1 =  getParamBaseRecalibrator();
//			CommandLineGATK.main(params1);

			// 进行校正，生成校正后的文件 *.recal.bam
			String[] params2 = { "-R", referenceFile, "-T", "PrintReads", "-o", outBamFile, "-I", inBamFile, "-BQSR", grpPath };
//			CommandLineGATK.main(params2);
			return true;
		} catch (Exception e) {
			logger.error("recalibrate 重新校正 error!!!");
			return false;
		}
	}
	/**
	 * 生成校验文件的参数
	 * @return
	 */
	private String[] getParamBaseRecalibrator() {
		List<String> lsParam = new ArrayList<String>();
		lsParam.add("-R"); lsParam.add(referenceFile);
		lsParam.add("-T"); lsParam.add("BaseRecalibrator");
		lsParam.add("-o"); lsParam.add(grpPath);
		lsParam.add("-I"); lsParam.add(inBamFile);
		for (String string : setSnpDBVcfFilePath) {
			lsParam.add("-knownSites"); lsParam.add(string);
		}
		return lsParam.toArray(new String[0]);
	}
	
	/**
	 * 删除校正过程中的临时文件
	 */
	public void delTempFile(){
		FileOperate.deleteFileFolder(grpPath);
	}
	
	/** 取得输出路径 */
	public String getOutputFilePath() {
		return outBamFile;
	}
	/** 设置输出路径包括文件名*.bam */
	public void setOutputFilePath(String outputFilePath) {
		this.outBamFile = outputFilePath;
	}
	/** 设置临时文件路径包括文件名 */
	public void setGrpPathh(String grpPath) {
		this.grpPath = grpPath;
	}
	
}
