package com.novelbio.analysis.project.zhy;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.analysis.seq.genomeNew.GffChrCmpBed;

/**
 * ������贵ķ����Ƚ�TSS����׻���������
 * ���壺��û���TSS��������bp��Χ���Ƚ��������������Χ�ڵ�readsƽ����
 * @author zong0jie
 *
 */
public class CmpTssMethy {
	public static void main(String[] args) {
		CmpTssMethy cmpTssMethy = new CmpTssMethy();
		cmpTssMethy.cmpTssN_2N();
		cmpTssMethy.cmpTssN_3N();
		cmpTssMethy.cmpTss2N_3N();
	}
	
	public void cmpTssN_2N() {
		String parent = "/media/winE/NBC/Project/Project_ZHY_Lab/MeDIP-Seq_20110506/RawData_and_AlignmentResult/mappingFile/";
		String parentOut = "/media/winE/NBC/Project/Project_ZHY_Lab/MeDIP-Seq_20110506/compareTss/";
		
		GffChrCmpBed gffChrCmpBed = new GffChrCmpBed(NovelBioConst.GENOME_GFF_TYPE_TIGR, 
				NovelBioConst.GENOME_PATH_RICE_TIGR_GFF_GENE, NovelBioConst.GENOME_PATH_RICE_TIGR_CHROM);
		
		gffChrCmpBed.setMapReads(parent+"Nextend_sort.bed", 5);
		gffChrCmpBed.loadMapReads();
		gffChrCmpBed.setMapReadsCmp(parent+"2Nextend_sort.bed", 5);
		String txtExcelFile = parentOut + "Methylation_Gene_Tss_Comb.txt";
		String txtOutFile = parentOut + "Nto2N.xls";
		gffChrCmpBed.readGeneList(txtExcelFile, 1, new int[]{-1500,0}, -100, true, txtOutFile);
	}
	public void cmpTssN_3N() {
		String parent = "/media/winE/NBC/Project/Project_ZHY_Lab/MeDIP-Seq_20110506/RawData_and_AlignmentResult/mappingFile/";
		String parentOut = "/media/winE/NBC/Project/Project_ZHY_Lab/MeDIP-Seq_20110506/compareTss/";
		
		GffChrCmpBed gffChrCmpBed = new GffChrCmpBed(NovelBioConst.GENOME_GFF_TYPE_TIGR, 
				NovelBioConst.GENOME_PATH_RICE_TIGR_GFF_GENE, NovelBioConst.GENOME_PATH_RICE_TIGR_CHROM);
		
		gffChrCmpBed.setMapReads(parent+"Nextend_sort.bed", 5);
		gffChrCmpBed.loadMapReads();
		gffChrCmpBed.setMapReadsCmp(parent+"3Nextend_sort.bed", 5);
		String txtExcelFile = parentOut + "Methylation_Gene_Tss_Comb.txt";
		String txtOutFile = parentOut + "Nto3N.xls";
		gffChrCmpBed.readGeneList(txtExcelFile, 1, new int[]{-1500,0}, -100, true, txtOutFile);
	}
	public void cmpTss2N_3N() {
		String parent = "/media/winE/NBC/Project/Project_ZHY_Lab/MeDIP-Seq_20110506/RawData_and_AlignmentResult/mappingFile/";
		String parentOut = "/media/winE/NBC/Project/Project_ZHY_Lab/MeDIP-Seq_20110506/compareTss/";
		
		GffChrCmpBed gffChrCmpBed = new GffChrCmpBed(NovelBioConst.GENOME_GFF_TYPE_TIGR, 
				NovelBioConst.GENOME_PATH_RICE_TIGR_GFF_GENE, NovelBioConst.GENOME_PATH_RICE_TIGR_CHROM);
		
		gffChrCmpBed.setMapReads(parent+"2Nextend_sort.bed", 5);
		gffChrCmpBed.loadMapReads();
		gffChrCmpBed.setMapReadsCmp(parent+"3Nextend_sort.bed", 5);
		String txtExcelFile = parentOut + "Methylation_Gene_Tss_Comb.txt";
		String txtOutFile = parentOut + "2Nto3N.xls";
		gffChrCmpBed.readGeneList(txtExcelFile, 1, new int[]{-1500,0}, -100, true, txtOutFile);
	}
}































