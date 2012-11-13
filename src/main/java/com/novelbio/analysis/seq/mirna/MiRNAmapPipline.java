package com.novelbio.analysis.seq.mirna;

import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.mapping.MapBwa;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * СRNA�ȶ���ˮ��
 * @author zong0jie
 *
 */
public class MiRNAmapPipline {
	/** �����ļ� */
	String seqFile = "";
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
	public void setOutPathTmp(String outPathTmpMapping, String outPathTmpBed) {
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
			mapping(fqFile, miRNApreSeq, samFile, bedFileMiRNA,false, true, unMappedFq);
			fqFile = unMappedFq;
		}
	
		if (FileOperate.isFileExist(rfamSeq)) {
			samFile = outputTmpFinal + "rfam.sam";
			unMappedFq = outputTmpFinal + "unMap2rfam.fq";
			mapping(fqFile, rfamSeq, samFile, bedFileRfam,false, true, unMappedFq);
			fqFile = unMappedFq;
		}
		
		if (FileOperate.isFileExist(ncRNAseq)) {
			samFile = outputTmpFinal + "ncRna.sam";
			unMappedFq = outputTmpFinal + "unMap2ncRna.fq";
			mapping(fqFile, ncRNAseq, samFile, bedFileNCRNA,false, true, unMappedFq);
			fqFile = unMappedFq;
		}
		
		if (FileOperate.isFileExist(genome)) {
			samFile = outputTmpFinal + "Genome.sam";
			unMappedFq = outputTmpFinal + "unMapped.fq";
			mapping(fqFile, genome, samFile, bedFileGenome, false, false, unMappedFq);
		}
		
		if (mappingAll2Genome && FileOperate.isFileExist(genome)) {
			fqFile = seqFile;
			samFile = outputTmpFinal + "GenomeAll.sam";
			unMappedFq = outputTmpFinal + "unMapped.fq";
			mapping(fqFile, genome, samFile, bedFileGenomeAll, true, false, unMappedFq);
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
			mapping(fqFile, miRNApreSeq, samFile, bedFileMiRNA, false, false, unMappedFq);
			fqFile = unMappedFq;
		}
	}
	/**
	 * @param fqFile
	 * @param chrFile
	 * @param samFileName ���sam�ļ���
	 * @param bedFile ���bed�ļ���
	 * @param uniqueMapping �Ƿ�ΪuniqueMapping
	 * @param uniqueMappedReadsRandomSelectOne ��unique mapping�������Ƿ�ֻ�����ȡһ��
	 * @param unMappedFq û��mapping�ϵ��ļ����Ϊfq
	 */
	private void mapping(String fqFile, String chrFile, String samFileName, String bedFile, boolean uniqueMapping, boolean uniqueMappedReadsRandomSelectOne,String unMappedFq) {
		MapBwa mapBwa = new MapBwa(fqFile, samFileName);
		mapBwa.setChrFile(chrFile);
		mapBwa.setExePath(exePath);
		SamFile samFile = mapBwa.mapReads();
		samFile.setUniqMapping(uniqueMapping);
		samFile.setUniqueRandomSelectOneRead(uniqueMappedReadsRandomSelectOne);
		try { Thread.sleep(1000); } catch (Exception e) { }
		BedSeq bedSeq = samFile.toBedSingleEnd(TxtReadandWrite.TXT,  bedFile);
		bedSeq.close();
		if (unMappedFq != null && !unMappedFq.equals("")) {
			samFile.getUnMappedReads(false, unMappedFq);
		}
	}
}
