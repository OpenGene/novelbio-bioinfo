package com.novelbio.analysis.seq.resequencing;

import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.fileOperate.FileOperate;

public class SomaticIndel {
	public static void main(String[] args) {
		SomaticIndel somaticIndel = new SomaticIndel();
		somaticIndel.setBamFileNormal("");
		somaticIndel.setBamFileTumor("");
		somaticIndel.setOutIndelsVcfFile("");
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
	
	private String getType() {
		return  " -T " + type;
	}
	private String getJvmMemory() {
		return jvmMemory;
	}
	/** jar包地址 */
	private String getJarPathAndName() {
		return jarPathAndName;
	}
	
	private String getTumorBamFile() {
		if (tumorBamFile == null) {
			return "";
		}
		return " -I:tumor " + tumorBamFile;
	}
	private String getNormalBamFile() {
		if (normalBamFile == null) {
			return "";
		}
		return  " -I:normal " + normalBamFile;
	}

	private String getOutIndelVerboseFile() {
		if (outIindelsVerboseFile != null && !outIindelsVerboseFile.trim().equals("")) {
			return " -verbose " + outIindelsVerboseFile;
		}
		return "";
	}
	
	private String getOutIndelsVcfFile() {
		if (outIndelsVcfFile == null) {
			return "";
		}
		return " -o " + outIndelsVcfFile;
	}
	private String getRefFastaFile() {
		if (refFastaFile == null || refFastaFile.equals("")) {
			return "";
		}
		return " --refseq " + refFastaFile;
	}
	
	private String getOutBedAndMetrics() {
		String outBed = FileOperate.changeFileSuffix(outIndelsVcfFile, "", "bed");
		String outMetrics = FileOperate.changeFileSuffix(outIndelsVcfFile, "", "matrix");
		return " --bedOutput " + outBed + " " + " -metrics " + outMetrics + " ";
	}
	
	public void run() {
		String cmdScript = "java -Xmx" + getJvmMemory() + " -jar " + getJarPathAndName() +  getType() 
				+ getNormalBamFile() + getTumorBamFile() + getRefFastaFile() + getOutIndelVerboseFile()
				+ getOutIndelsVcfFile() + getOutBedAndMetrics();
		CmdOperate cmdOperate = new CmdOperate(cmdScript);
		cmdOperate.run();
	}
	
}
