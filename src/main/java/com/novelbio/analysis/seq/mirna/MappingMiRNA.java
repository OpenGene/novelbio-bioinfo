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
public class MappingMiRNA {
	/** �����ļ� */
	String seqFile = "";
	/** ���·����ǰ׺ */
	String outPath = "";
	
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
	/** ȫ��reads mapping��ȫ�������� */
	String bedFileGenomeAll = null;
	
	/** �Ƿ�ȫ��mapping��genome�ϣ�Ĭ��Ϊtrue */
	boolean mapping2Genome = true;
	/** �Ƿ�ȫ��mapping��genome�ϣ�Ĭ��Ϊtrue */
	public void setMapping2Genome(boolean mapping2Genome) {
		this.mapping2Genome = mapping2Genome;
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
	 * @param seqFile ����
	 * @param outPath ����ļ���
	 * @param prix �ļ�ǰ׺
	 */
	public void setSample(String seqFile) {
		this.seqFile = seqFile;
	}
	/** �趨����ļ��к�ǰ׺ */
	public void setOutPath(String outPathPrefix) {
		this.outPath = outPathPrefix;
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
	/**
	 * mapping����ˮ��
	 */
	public void mappingPipeline() {
		bedFileMiRNA = outPath + "_miRNA.bed";
		bedFileRfam = outPath + "_rfam.bed";
		bedFileNCRNA = outPath + "_ncRna.bed";
		bedFileGenome = outPath + "_Genome.bed";
		/** ȫ��reads mapping��ȫ�������� */
		bedFileGenomeAll = outPath + "_GenomeAll.bed";
		
		
		String fqFile = seqFile;
		String samFile = "";
		String unMappedFq = "";
		if (FileOperate.isFileExist(miRNApreSeq)) {
			samFile = outPath + "_miRNA.sam";
			unMappedFq = outPath + "_unMap2miRNA.fq";
			mapping(fqFile, miRNApreSeq, samFile, bedFileMiRNA, unMappedFq);
			fqFile = unMappedFq;
		}
	
		if (FileOperate.isFileExist(rfamSeq)) {
			samFile = outPath + "_rfam.sam";
			unMappedFq = outPath + "_unMap2rfam.fq";
			mapping(fqFile, rfamSeq, samFile, bedFileRfam, unMappedFq);
			fqFile = unMappedFq;
		}
		
		if (FileOperate.isFileExist(ncRNAseq)) {
			samFile = outPath + "_ncRna.sam";
			unMappedFq = outPath + "_unMap2ncRna.fq";
			mapping(fqFile, ncRNAseq, samFile, bedFileNCRNA, unMappedFq);
			fqFile = unMappedFq;
		}
		
		if (FileOperate.isFileExist(genome)) {
			samFile = outPath + "_Genome.sam";
			unMappedFq = outPath + "_unMapped.fq";
			mapping(fqFile, genome, samFile, bedFileGenome, unMappedFq);
		}
		
		if (mapping2Genome && FileOperate.isFileExist(genome)) {
			fqFile = seqFile;
			samFile = outPath + "_GenomeAll.sam";
			unMappedFq = outPath + "_unMapped.fq";
			mapping(fqFile, genome, samFile, bedFileGenomeAll, unMappedFq);
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
//		outPath = FileOperate.addSep(outPath);
		MapBwa mapBwa = new MapBwa(fqFile, samFileName, false);
		mapBwa.setExePath(exePath, chrFile);
		SamFile samFile = mapBwa.mapReads();
		samFile.toBedSingleEnd(TxtReadandWrite.TXT,  bedFile, false);
		if (unMappedFq != null && !unMappedFq.equals("")) {
			samFile.getUnMappedReads(false, unMappedFq);
		}
	}
}
