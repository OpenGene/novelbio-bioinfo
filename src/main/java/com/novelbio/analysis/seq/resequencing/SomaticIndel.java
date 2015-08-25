package com.novelbio.analysis.seq.resequencing;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;

public class SomaticIndel {
	public static void main(String[] args) {
		SomaticIndel somaticIndel = new SomaticIndel();
		somaticIndel.setJarPathAndName("/media/winD/NBCplatform/BioInfomaticsToolsPlatform/bioinfo/GATK/GenomeAnalysisTK.jar");
		somaticIndel.setBamFileNormal("/media/winD/NBC/Project/test/CK_accepted_hits_dedup_rgroup.bam");
		somaticIndel.setBamFileTumor("/media/winD/NBC/Project/test/320_accepted_hits_dedup_rgroup.bam");
		somaticIndel.setRefFastaFile("/media/winD/NBC/Project/test/chrAllOld.fa");
		somaticIndel.setOutIndelsVcfFile("/home/zong0jie/Test/rnaseq/paper/difIndel.vcf");
		somaticIndel.run();
	}
	/** 虚拟机内存 */
	String jvmMemory = "4000m";
	String jarPathAndName = "GenomeAnalysisTK.jar";
	String type = "SomaticIndelDetector";
	
	/** 正常bam文件 */
	String normalBamFile;
	/** 疾病bam文件 */
	String tumorBamFile;
	/** ref的fasta文件 */
	String refFastaFile;
	/** 过滤结果的表达式 */
	String filterExp = "T_COV<6||N_COV<4||T_INDEL_F<0.3||T_INDEL_CF<0.7 ";

	/** Verbose output file in text format.  */
	String outIindelsVerboseFile;
	/** 输出的插入/缺失vcf文件 */
	String outIndelsVcfFile;
	
	public void setJarPathAndName(String jarPathAndName) {
		this.jarPathAndName = jarPathAndName;
	}

	/** 虚拟机内存 */
	public void setJvmMemory(String jvmMemory) {
		this.jvmMemory = jvmMemory;
	}

	/** ref的fasta文件 */
	public void setRefFastaFile(String refFastaFile) {
		this.refFastaFile = refFastaFile;
	}
	
	/** 正常bam文件 */
	public void setBamFileNormal(String normalBamFile) {
		this.normalBamFile = normalBamFile;
	}
	
	/** 疾病bam文件 */
	public void setBamFileTumor(String tumorBamFile) {
		this.tumorBamFile = tumorBamFile;
	}
	
	/** 插入/缺失txt文件 */
	public void setOutIndelsVerboseFile(String indelsTXTFile) {
		this.outIindelsVerboseFile = indelsTXTFile;
	}
	
	/** 插入/缺失cf文件 */
	public void setOutIndelsVcfFile(String indelsVcfFile) {
		this.outIndelsVcfFile = indelsVcfFile;
	}
	
	private String[] getType() {
		return  new String[]{"-T", type};
	}
	private String getJvmMemory() {
		return jvmMemory;
	}
	/** jar包地址 */
	private String getJarPathAndName() {
		return jarPathAndName;
	}
	
	private String[] getTumorBamFile() {
		if (tumorBamFile == null) {
			return null;
		}
		return new String[]{"-I:tumor", tumorBamFile};
	}
	private String[] getNormalBamFile() {
		if (normalBamFile == null) {
			return null;
		}
		return new String[]{"-I:normal", normalBamFile};
	}

	private String[] getOutIndelVerboseFile() {
		if (outIindelsVerboseFile != null && !outIindelsVerboseFile.trim().equals("")) {
			return new String[]{"-verbose", outIindelsVerboseFile};
		}
		return null;
	}
	
	private String[] getOutIndelsVcfFile() {
		if (outIndelsVcfFile == null) {
			return null;
		}
		return new String[]{"-o", outIndelsVcfFile};
	}
	private String[] getRefFastaFile() {
		if (refFastaFile == null || refFastaFile.equals("")) {
			return null;
		}
		return new String[]{"--refseq", refFastaFile};
	}
	
	private String[] getOutBedAndMetrics() {
		String outBed = FileOperate.changeFileSuffix(outIndelsVcfFile, "", "bed");
		String outMetrics = FileOperate.changeFileSuffix(outIndelsVcfFile, "", "matrix");
		return new String[]{"--bedOutput", outBed, "-metrics", outMetrics};
	}
	
	public void run() {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add("java"); lsCmd.add("-Xmx"+getJvmMemory());
		lsCmd.add("-jar"); lsCmd.add(getJarPathAndName());
		ArrayOperate.addArrayToList(lsCmd, getType());
		ArrayOperate.addArrayToList(lsCmd, getNormalBamFile());
		ArrayOperate.addArrayToList(lsCmd, getTumorBamFile());
		ArrayOperate.addArrayToList(lsCmd, getRefFastaFile());
		ArrayOperate.addArrayToList(lsCmd, getOutIndelVerboseFile());
		ArrayOperate.addArrayToList(lsCmd, getOutIndelsVcfFile());
		ArrayOperate.addArrayToList(lsCmd, getOutBedAndMetrics());
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		cmdOperate.runWithExp();
	}
	
}
