package com.novelbio.analysis.seq.resequencing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.base.PathDetail;
import com.novelbio.base.StringOperate;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;

public class MuTect {
	public static void main(String[] args) {
		MuTect muTect = new MuTect();
		muTect.setJarFile("/media/winD/NBCplatform/BioInfomaticsToolsPlatform/bioinfo/GATK/muTect-1.1.4.jar");
		SamFile samFile9A = new SamFile("/media/winD/NBC/Project/test/CK_accepted_hits_dedup_rgroup.bam");
//		samFile9A = samFile9A.addGroup("9A", "9A", "9A", null);
		SamFile samFileCKP = new SamFile("/media/winD/NBC/Project/test/320_accepted_hits_dedup_rgroup.bam");
		muTect.setInputNormalFile(samFile9A.getFileName());
		muTect.setInputTumorFile(samFileCKP.getFileName());
		muTect.setOutFile("/home/zong0jie/Test/rnaseq/paper/difSnp.txt");
		muTect.setReferenceSequence("/media/winD/NBC/Project/test/chrAllOld.fa");
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
	 * cosmic数据库，应该是vcf格式
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
	private String[] getReferenceSequence() {
		if (referenceSequence == null) {
			return null;
		}
		return new String[]{"--reference_sequence", referenceSequence};
	}
	/**
	 * 参考基因组
	 */
	public void setReferenceSequence(String referenceSequence) {
		this.referenceSequence = referenceSequence;
	}
	private String[] getCosmic() {
		if (cosmic == null || cosmic.equals("")) {
			return null;
		}
		return new String[]{"--cosmic", cosmic};
	}
	
	/** cosmic数据库，应该是vcf格式 */
	public void setCosmic(String cosmic) {
		this.cosmic = cosmic;
	}
	
	private String[] getDbsnp() {
		if (dbsnp == null || dbsnp.equals("")) {
			return null;
		}
		return new String[]{"--dbsnp", dbsnp};
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
	private String[] getAnalysisType() {
		return new String[]{"--analysis_type", analysisType};
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
	private String[] getInputNormalFile() {
		if (inputNormalFile == null) {
			return null;
		}
		return new String[]{"--input_file:normal", inputNormalFile};
	}

	/**
	 * 输入疾病bam文件
	 */
	private String[] getInputTumorFile() {
		if (inputTumorFile == null) {
			return null;
		}
		return new String[]{"--input_file:tumor", inputTumorFile};
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
	private String[] getOutFile() {
		return new String[]{"--out", outFile};
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
	private String[] getCoverageFile() {
		if (coverageFile == null || coverageFile.equals("")) {
			return null;
		}
		return new String[]{"--coverage_file", coverageFile};
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
	private String[] getIntervals() {
		if (intervals == null) {
			intervals = getIntervalsFromChr();
		}
		if (StringOperate.isRealNull(intervals)) {
			return null;
        }
		return new String[]{"--intervals", intervals};
	}
	
	/** 根据chrID产生的intervals */
	private String getIntervalsFromChr() {
		String outFile = PathDetail.getTmpPathWithSep() + "snp_intervals" + DateUtil.getDateAndRandom() + ".list";
		TxtReadandWrite txtWrite = new TxtReadandWrite(outFile, true);
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
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add("java"); lsCmd.add("-Xmx" + getJvmRunMemory());
		lsCmd.add("-jar"); lsCmd.add(getJarPathAndName());
		ArrayOperate.addArrayToList(lsCmd, getAnalysisType());
		ArrayOperate.addArrayToList(lsCmd, getCosmic());
		ArrayOperate.addArrayToList(lsCmd, getDbsnp());
		ArrayOperate.addArrayToList(lsCmd, getInputNormalFile());
		ArrayOperate.addArrayToList(lsCmd, getInputTumorFile());
		ArrayOperate.addArrayToList(lsCmd, getIntervals());
		ArrayOperate.addArrayToList(lsCmd, getReferenceSequence());
		ArrayOperate.addArrayToList(lsCmd, getOutFile());
		ArrayOperate.addArrayToList(lsCmd, getCoverageFile());
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		cmdOperate.run();
	}
	
}
