package com.novelbio.analysis.seq.sam;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.regexp.recompile;
import org.springframework.data.repository.NoRepositoryBean;

import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * GATK realign 重排<br>
 * 
 * 这一步是本地重排，因为在indel周围出现mapping错误的概率很高，<br>
 * 而通过在indel周围进行一些重排可以大大减少由于indel而导致的很多SNP的假阳性，<br>
 * 如果已经有一些可靠位点的信息的话 (例如dbsnp的数据)，<br>
 * 这一步可以只在一些已知的可靠位点周围进行，这样得到的结果比较可靠也比较节省时间；<br>
 * 而缺少这些数据的情况下则需要对所有位点都进行这样一个操作。<br>
 * realign这一步用的是GATK里面的RealignerTargetCreator和IndelRealigner 工具，<br>
 * 如果已经有可靠的INDEL位点(如dbsnp数据等等，需要是VCF格式的)信息，<br>
 * 可以通过-known参数来进行设置，让realign操作主要集中在这些位点附近，<br>
 * 实际情况下可能很多物种并没有这样的参考数据，<br>
 * 所以需要对所有INDEL位点进行这样的一个操作
 * 
 */
public class GATKRealign {
	public static void main(String[] args) {
		String[] params2 = { "-R", "/media/winD/Bioinfor/genome/rice/tigr7/ChromFa/all/chrAll.fa", "-T", "IndelRealigner", 
				"-targetIntervals", "/media/winE/NBC/Project/Project_ZDB_Lab/QXL/Project_ZDB/NewCombine/realignTest/9522_realign2013-05-23083530445.intervals",
				"-o", "/media/winE/NBC/Project/Project_ZDB_Lab/QXL/Project_ZDB/NewCombine/realignTest/AF18-1_sorted_subNew.bam", 
				"--consensusDeterminationModel","USE_SW", //表示用'Smith-Waterman'的比对方法来产生最佳名参数有：USE_READS
				"-I", "/media/winE/NBC/Project/Project_ZDB_Lab/QXL/Project_ZDB/NewCombine/realignTest/AF18-1_sorted_sub.bam", "--unsafe", ALL }; //, "--filter_mismatching_base_and_quals"
//		CommandLineGATK.main(params2);
	}
	private static final Logger logger = Logger.getLogger(GATKRealign.class);
	
	public static final String ALLOW_UNINDEXED_BAM = "ALLOW_UNINDEXED_BAM";
	public static final String ALLOW_UNSET_BAM_SORT_ORDER = "ALLOW_UNSET_BAM_SORT_ORDER";
	public static final String NO_READ_ORDER_VERIFICATION = "NO_READ_ORDER_VERIFICATION";
	public static final String ALLOW_SEQ_DICT_INCOMPATIBILITY = "ALLOW_SEQ_DICT_INCOMPATIBILITY";
	public static final String LENIENT_VCF_PROCESSING = "LENIENT_VCF_PROCESSING";
	public static final String ALL = "ALL";
	public static final String SAFE = "SAFE";
	
	/** 输入文件路径+bam文件名 */
	private String inputFilePath;
	/** 默认和输入文件同路径包括文件名 */
	private String outputFilePath;
	/** 输入ref文件路径+fasta文件名 */
	private String refFilePath;
	/** 临时文件路径包括文件名 */
	private String intervalsPath;
	/** 可通过安全验证 默认ALL还有({@link#ALLOW_UNINDEXED_BAM}) */
	private String unsafe = GATKRealign.ALL;
	
	/**
	 * @param inputFilePath 输入文件路径+bam文件名
	 * @param refFilePath　输入ref文件路径+fasta文件名
	 * @param outputPath　输出路径
	 */
	public GATKRealign(String inputFileName, String refFilePath,String outputFile){
		this.inputFilePath = inputFileName;
		this.refFilePath = refFilePath;
		this.intervalsPath = FileOperate.changeFileSuffix(outputFile, DateUtil.getDateAndRandom(), "intervals");
		this.outputFilePath = outputFile;
	}
	/**
	 * realign 重排
	 * @return 输出文件路径 + 输入文件名.realn.bam
	 */
	public boolean realign() {
		try {
			
//			intervalsPath = "/media/winE/NBC/Project/Project_ZDB_Lab/QXL/Project_ZDB/NewCombine/realignTest/AF18_realign2013-05-23013331150.intervals";
			String[] params1 = { "-I", inputFilePath, "-R", refFilePath, "-T", "RealignerTargetCreator", "-o", intervalsPath ,"--unsafe", unsafe};
//			CommandLineGATK.main(params1);
			String[] params2 = { "-R", refFilePath, "-T", "IndelRealigner", "-targetIntervals", intervalsPath, "-o", outputFilePath, 
					"--consensusDeterminationModel","USE_READS", //表示用'Smith-Waterman'的比对方法来产生最佳名参数有：USE_READS
					"-I", inputFilePath, "--unsafe", unsafe }; //, "--filter_mismatching_base_and_quals"
//			CommandLineGATK.main(params2);
			return true;
		} catch (Exception e) {
			logger.error("realign 本地重排 error!!!");
			return false;
		}
	}
	
	/**
	 * 删除重排过程中的临时文件
	 */
	public void delTempFile(){
		FileOperate.delFile(intervalsPath);
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
	public void setIntervalsPath(String intervalsPath) {
		this.intervalsPath = intervalsPath;
	}
	/** 取得ref文件路径+fasta文件名 */
	public String getRefFilePath() {
		return refFilePath;
	}
	/** 设置ref文件路径+fasta文件名 */
	public void setRefFilePath(String refFilePath) {
		this.refFilePath = refFilePath;
	}
	/** 设置哪些可通过安全验证 默认ALL */
	public void setUnsafe(String unsafe) {
		this.unsafe = unsafe;
	}
	
	
	public static Map<String, String> getMapGatkSafe2Value() {
		Map<String, String> mapGatkSafe2Value = new LinkedHashMap<>();
		mapGatkSafe2Value.put(SAFE, SAFE);
		mapGatkSafe2Value.put(ALLOW_SEQ_DICT_INCOMPATIBILITY, ALLOW_SEQ_DICT_INCOMPATIBILITY);
		mapGatkSafe2Value.put(ALLOW_UNINDEXED_BAM, ALLOW_UNINDEXED_BAM);
		mapGatkSafe2Value.put(ALLOW_UNSET_BAM_SORT_ORDER, ALLOW_UNSET_BAM_SORT_ORDER);
		mapGatkSafe2Value.put(LENIENT_VCF_PROCESSING, LENIENT_VCF_PROCESSING);
		mapGatkSafe2Value.put(NO_READ_ORDER_VERIFICATION, NO_READ_ORDER_VERIFICATION);
		mapGatkSafe2Value.put(ALL, ALL);
		return mapGatkSafe2Value;
	}
}
