package com.novelbio.analysis.seq.resequencing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqFastaHash;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.analysis.seq.sam.SamRecord;
import com.novelbio.base.PathDetail;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.model.species.Species;
import com.novelbio.generalConf.PathNBCDetail;

public class MuTect {
	public static void main(String[] args) {
		MuTect muTect = new MuTect();
		muTect.setJarFile("/media/winD/NBCplatform/BioInfomaticsToolsPlatform/bioinfo/GATK/muTect.jar");
		SamFile samFile9A = new SamFile("/media/winD/NBC/Project/test/CK_accepted_hits_dedup_rgroup.bam");
//		samFile9A = samFile9A.addGroup("9A", "9A", "9A", null);
		Species species = new Species(39947);
		SamFile samFileCKP = new SamFile("/media/winD/NBC/Project/test/320_accepted_hits_dedup_rgroup.bam");
		muTect.setInputNormalFile(samFile9A.getFileName());
		muTect.setInputTumorFile(samFileCKP.getFileName());
		muTect.setOutFile("/home/zong0jie/Test/rnaseq/paper/difSnp.txt");
		muTect.setReferenceSequence(species.getChromSeq());
		muTect.run();
//		SamFile samFile = new SamFile("/home/zong0jie/Test/rnaseq/paper/CKP_accepted_hits.bam");
//		SamFile samFileWrite = new SamFile("/home/zong0jie/Test/rnaseq/paper/CKP_accepted_hits.bam", samFile.getHeader());
//		for (SamRecord samRecord : samFile.readLines()) {
//			System.out.println(samRecord.toString());
//		}
	}
	//TODO 软件jar包路径和名字，宗博指定
	String jarPathAndName = "muTect.jar";
	
	String jvmRunMemory = "4000m";
	
	/**
	 * 固定是MuTect
	 */
	String analysisType = "MuTect";
	/**
	 * 参考基因组
	 */
	String referenceSequence;
	/**
	 * 不明
	 */
	String cosmic;
	
	/** dbSnp的vcf文件 */
	String dbsnp;
	
	/** 染色体信息 */
	String intervals;
	
	/** 输入的bam正常文件 */
	String inputNormalFile;
	
	/** 输入疾病bam文件 */
	String inputTumorFile;
	
	/** 输出结果 */
	String outFile;
	
	/** 输出结果2 */
	String coverageFile;
	
	public void setJarFile(String jarPathAndName) {
		this.jarPathAndName = jarPathAndName;
	}
	/**
	 * 参考基因组
	 */
	private String getReferenceSequence() {
		if (referenceSequence == null) {
			return "";
		}
		return " --reference_sequence " +  referenceSequence;
	}
	/**
	 * 参考基因组
	 */
	public void setReferenceSequence(String referenceSequence) {
		this.referenceSequence = referenceSequence;
	}
	private String getCosmic() {
		if (cosmic == null || cosmic.equals("")) {
			return "";
		}
		return "--cosmic " + cosmic;
	}
	public void setCosmic(String cosmic) {
		this.cosmic = cosmic;
	}
	private String getDbsnp() {
		if (dbsnp == null || dbsnp.equals("")) {
			return "";
		}
		return "--dbsnp " + dbsnp;
	}
	public void setDbsnp(String dbsnp) {
		this.dbsnp = dbsnp;
	}

	/**
	 * 染色体信息
	 * @return
	 */
	public void setIntervals(List<Align> lsAligns) {
		String chromosomeInfo = null;
		for (Align align : lsAligns) {
			String chr = align.getRefID();
			String start = align.getStartAbs() + "";
			String end = align.getEndAbs() + "";
			String oneChrchromo = chr + ":"  + start + "-" + end;
			if (chromosomeInfo == null) {
				chromosomeInfo =  oneChrchromo;
			}else {
				chromosomeInfo = chromosomeInfo + ";"+ oneChrchromo;
			}

		}
		this.intervals = chromosomeInfo;
	}
	
	/**
	 * 固定是MuTect
	 */
	private String getAnalysisType() {
		return " --analysis_type " + analysisType;
	}
	
	/**
	 * 输入的正常文件
	 */
	public void setInputNormalFile(String inputNormalFile) {
		this.inputNormalFile = inputNormalFile;
	}
	
	/**
	 * 获得正常组的bam文件
	 */
	private String getInputNormalFile() {
		if (inputNormalFile == null) {
			return "";
		}
		return"--input_file:normal " +  inputNormalFile;
	}

	/**
	 * 输入疾病bam文件
	 */
	private String getInputTumorFile() {
		if (inputTumorFile == null) {
			return "";
		}
		return "--input_file:tumor " + inputTumorFile;
	}
	/**
	 * 输入疾病文件
	 */
	public void setInputTumorFile(String inputTumorFile) {
		this.inputTumorFile = inputTumorFile;
	}
	/**
	 * 输出结果2
	 */
	private String getOutFile() {
		return "--out " + outFile;
	}
	/**
	 * 输出结果
	 */
	public void setOutFile(String outFile) {
		this.outFile = outFile;
	}
	/**
	 * 输出结果2
	 */
	private String getCoverageFile() {
		if (coverageFile == null || coverageFile.equals("")) {
			return "";
		}
		return "--coverage_file " + coverageFile;
	}
	/**
	 * 输出结果2
	 */
	public void setCoverageFile(String coverageFile) {
		this.coverageFile = coverageFile;
	}
	
	/**
	 * 设置虚拟机内存大小
	 * @param jvmRunMemory
	 */
	public void setJvmRunMemory(String jvmRunMemory) {
		this.jvmRunMemory = jvmRunMemory;
	}
	/**
	 * 获取虚拟机内存大小
	 * @param jvmRunMemory
	 */
	private String getJvmRunMemory() {
		return jvmRunMemory;
	}
	
	/**
	 * jar包所在路径
	 * @return
	 */
	private String getJarPathAndName() {
		return jarPathAndName;
	}
	
	/**
	 * 染色体信息
	 * @return
	 */
	private String getIntervals() {
		if (intervals == null) {
			intervals = getIntervalsFromChr();
		}
		return "--intervals " + CmdOperate.addQuot(intervals);
	}
	
	/** 根据chrID产生的intervals */
	private String getIntervalsFromChr() {
		String outFile = PathNBCDetail.getTmpPath() + "snp_intervals" + DateUtil.getDateAndRandom();
		TxtReadandWrite txtWrite = new TxtReadandWrite(outFile);
		SamFile samFile = new SamFile(inputNormalFile);
		Map<String, Long> mapChrID2Long = samFile.getMapChrID2Length();
		for (String chrID : mapChrID2Long.keySet()) {
			txtWrite.writefileln(chrID + ":" + 10 + "-" + (mapChrID2Long.get(chrID) - 10));
		}
		txtWrite.close();
		return outFile;
	}
	
	/**
	 * 主运行方法
	 */
	public void run() {
		String  cmdScript = "java -Xmx" + getJvmRunMemory() + " -jar " + getJarPathAndName() + " " + getAnalysisType() + " "
											 + getCosmic() + " " + getDbsnp() + " "  + getInputNormalFile() + " " + getInputTumorFile() + " " + getIntervals() + " "
											 + getReferenceSequence() + " " + getOutFile() + " " +getCoverageFile();
		CmdOperate cmdOperate = new CmdOperate(cmdScript);
		cmdOperate.run();

	}
	
}
