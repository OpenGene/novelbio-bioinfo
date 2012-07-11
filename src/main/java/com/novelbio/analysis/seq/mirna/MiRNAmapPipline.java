package com.novelbio.analysis.seq.mirna;

import com.novelbio.analysis.seq.BedRecord;
import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.FastQOld;
import com.novelbio.analysis.seq.chipseq.pipeline.Pipline;
import com.novelbio.analysis.seq.mapping.MapBwa;
import com.novelbio.analysis.seq.mapping.SamFile;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.generalConf.NovelBioConst;

/**
 * СRNA�ȶ���ˮ��
 * @author zong0jie
 *
 */
public class MiRNAmapPipline {
	public static void main(String[] args) {
		String fqFile = "/media/winF/NBC/Project/Project_CRZ_Lab/result/tmpMapping/Y1A_filtered_unMap2miRNA.fq";
		String rfamSeq = "/media/winE/Bioinformatics/GenomeData/Rice/sRNA/osa_rfam.fa";
		String samFile = "/media/winF/NBC/Project/Project_CRZ_Lab/result/tmpMapping/Y1A_filtered_rfam.sam";
		String bedFile = "/media/winF/NBC/Project/Project_CRZ_Lab/result/tmpBed/Y1A_filtered_rfam.bed";
		String unMappedFq = "/media/winF/NBC/Project/Project_CRZ_Lab/result/tmpMapping/Y1A_filtered_unMap2rfam.fq";
		MiRNAmapPipline mAmapPipline = new MiRNAmapPipline();
		mAmapPipline.mapping(fqFile, rfamSeq, samFile, bedFile, unMappedFq);
	}
	/** �����ļ� */
	String seqFile = "";
	/** ����ļ��У���Ҫ��bed�ļ� */
	String outPath;
	/** �������ʱ�ļ��У���Ҫ����mapping���м��ļ� */
	String outPathTmpMapping;
	/** �������ʱ�ļ��У���Ҫ����mapping���м��ļ� */
	String outPathTmpBed;
	String outputPrefix = "";

	/** rfam���ݿ��е����� */
	String rfamSeq = "";
	/** miRNA���� */
	String miRNApreSeq = "";
	/** ncRNA���� */
	String ncRNAseq = "";
	/** ���������� */
	String genome = "";
	/** bwa���ڵ�·�� */
	String exePath = "";
	
	////////////////////// ����ļ��� /////////////////////////////
	String bedFileMiRNA = null;
	String bedFileRfam = null;
	String bedFileNCRNA = null;
	String bedFileGenome = null;
	
	/** �Ƿ�ȫ��mapping��genome�ϣ�Ĭ��Ϊtrue */
	boolean mappingAll2Genome = true;
	/** ȫ��reads mapping��ȫ�������Ϻ������bed�ļ� */
	String bedFileGenomeAll = null;

	
	/** �Ƿ�ȫ��mapping��genome�ϣ�Ĭ��Ϊtrue */
	public void setMappingAll2Genome(boolean mappingAll2Genome) {
		this.mappingAll2Genome = mappingAll2Genome;
	}
	public void setRfamSeq(String rfamSeq) {
		this.rfamSeq = rfamSeq;
	}
	/** �趨ǰ������ */
	public void setMiRNApreSeq(String miRNAseq) {
		this.miRNApreSeq = miRNAseq;
	}
	public void setNcRNAseq(String ncRNAseq) {
		this.ncRNAseq = ncRNAseq;
	}
	public void setGenome(String genome) {
		this.genome = genome;
	}
	/** bwa���ڵ�·����Ĭ��Ϊ""��Ҳ������ϵͳ·���� */
	public void setExePath(String exePath) {
		this.exePath = exePath;
	}
	/**
	 * @param outputPrefix ���ǰ׺
	 * @param seqFile �����fastq�ļ�
	 */
	public void setSample(String outputPrefix, String fastqFile) {
		if (outputPrefix != null && !outputPrefix.trim().equals("")) {
			if (!outputPrefix.endsWith("_")) {
				this.outputPrefix = outputPrefix.trim() + "_";
			}
		}
		this.seqFile = fastqFile;
	}
	/** �趨�����ʱ�ļ��У��������ļ��� */
	public void setOutPath(String outPath, String outPathTmpMapping, String outPathTmpBed) {
		this.outPath = FileOperate.addSep(outPath);
		this.outPathTmpMapping = FileOperate.addSep(outPathTmpMapping);
		this.outPathTmpBed = FileOperate.addSep(outPathTmpBed);
	}
	/** �ȶ�miRNA��bed�ļ���� */
	public String getOutMiRNAbed() {
		return bedFileMiRNA;
	}
	/** �ȶ�rfam��bed�ļ���� */
	public String getOutRfambed() {
		return bedFileRfam;
	}
	/** �ȶ�refseq�е�ncRNA��bed�ļ���� */
	public String getOutNCRNAbed() {
		return bedFileNCRNA;
	}
	/** �ȶԻ������bed�ļ���� */
	public String getOutGenomebed() {
		return bedFileGenome;
	}
	/** �ȶ�miRNA��bed�ļ���� */
	public String getOutGenomeAllbed() {
		return bedFileGenomeAll;
	}
	/** mapping����ˮ�� */
	public void mappingPipeline() {
		String outputBed = outPathTmpBed + outputPrefix;
		bedFileMiRNA = outputBed +  "miRNA.bed";
		bedFileRfam = outputBed + "rfam.bed";
		bedFileNCRNA = outputBed + "ncRna.bed";
		bedFileGenome = outputBed + "Genome.bed";
		/** ȫ��reads mapping��ȫ�������� */
		bedFileGenomeAll = outputBed + "GenomeAll.bed";
		
		String outputTmpFinal = outPathTmpMapping + outputPrefix;
		String fqFile = seqFile;
		String samFile = "";
		String unMappedFq = "";
		if (FileOperate.isFileExist(miRNApreSeq)) {
			samFile = outputTmpFinal + "miRNA.sam";
			unMappedFq = outputTmpFinal + "unMap2miRNA.fq";
			mapping(fqFile, miRNApreSeq, samFile, bedFileMiRNA, unMappedFq);
			fqFile = unMappedFq;
		}
	
		if (FileOperate.isFileExist(rfamSeq)) {
			samFile = outputTmpFinal + "rfam.sam";
			unMappedFq = outputTmpFinal + "unMap2rfam.fq";
			mapping(fqFile, rfamSeq, samFile, bedFileRfam, unMappedFq);
			fqFile = unMappedFq;
		}
		
		if (FileOperate.isFileExist(ncRNAseq)) {
			samFile = outputTmpFinal + "ncRna.sam";
			unMappedFq = outputTmpFinal + "unMap2ncRna.fq";
			mapping(fqFile, ncRNAseq, samFile, bedFileNCRNA, unMappedFq);
			fqFile = unMappedFq;
		}
		
		if (FileOperate.isFileExist(genome)) {
			samFile = outputTmpFinal + "Genome.sam";
			unMappedFq = outputTmpFinal + "unMapped.fq";
			mapping(fqFile, genome, samFile, bedFileGenome, unMappedFq);
		}
		
		if (mappingAll2Genome && FileOperate.isFileExist(genome)) {
			fqFile = seqFile;
			samFile = outputTmpFinal + "GenomeAll.sam";
			unMappedFq = outputTmpFinal + "unMapped.fq";
			mapping(fqFile, genome, samFile, bedFileGenomeAll, unMappedFq);
		}
	}
	/** ��mapping��MiRNA�� */
	public void mappingMiRNA() {
		FileOperate.createFolders(outPathTmpBed);
		FileOperate.createFolders(outPathTmpMapping);
		
		String outputBed = outPathTmpBed + outputPrefix;
		bedFileMiRNA = outputBed +  "miRNA.bed";
		/** ȫ��reads mapping��ȫ�������� */
		bedFileGenomeAll = outputBed + "GenomeAll.bed";
		
		String outputTmpFinal = outPathTmpMapping + outputPrefix;
		String fqFile = seqFile;
		String samFile = "";
		String unMappedFq = "";
		if (FileOperate.isFileExist(miRNApreSeq)) {
			samFile = outputTmpFinal + "miRNA.sam";
			unMappedFq = outputTmpFinal + "unMap2miRNA.fq";
			mapping(fqFile, miRNApreSeq, samFile, bedFileMiRNA, unMappedFq);
			fqFile = unMappedFq;
		}
	}
	/**
	 * ��СRNAmapping��������ȥ
	 * @param fqFile
	 * @param chrFile
	 * @param samFileName
	 * @param bedFile
	 * @param unMappedFq
	 */
	private void mapping(String fqFile, String chrFile, String samFileName, String bedFile, String unMappedFq) {
		MapBwa mapBwa = new MapBwa(fqFile, samFileName, false);
		mapBwa.setExePath(exePath, chrFile);
		SamFile samFile = mapBwa.mapReads();
		try { Thread.sleep(1000); } catch (Exception e) { }
		samFile.toBedSingleEnd(TxtReadandWrite.TXT,  bedFile, false);
		if (unMappedFq != null && !unMappedFq.equals("")) {
			samFile.getUnMappedReads(false, unMappedFq);
		}
	}
}
