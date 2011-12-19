package com.novelbio.analysis.project.zhy;

import java.util.ArrayList;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.analysis.seq.genomeNew.GffChrCmpBed;
import com.novelbio.base.dataOperate.ExcelTxtRead;

/**
 * ������贵ķ����Ƚ�TSS����׻���������
 * ���壺��û���TSS��������bp��Χ���Ƚ��������������Χ�ڵ�readsƽ����
 * @author zong0jie
 *
 */
public class CmpTssMethy {
	public static void main(String[] args) {
		CmpTssMethy cmpTssMethy = new CmpTssMethy();
		cmpTssMethy.cmpTssNvs2N();
//		cmpTssMethy.cmpTssK27K0_K4();
//		cmpTssMethy.cmpTssK27W0_W4();
//		cmpTssMethy.cmpTssK4K0_K4();
//		cmpTssMethy.cmpTssK4W0_W4();
	}
	GffChrCmpBed gffChrCmpBed = new GffChrCmpBed(NovelBioConst.GENOME_GFF_TYPE_TIGR, NovelBioConst.GENOME_PATH_RICE_TIGR_GFF_GENE, 
			NovelBioConst.GENOME_PATH_RICE_TIGR_CHROM);

	public void cmpTssNvs2N() {
		String parent = "/media/winE/NBC/Project/Project_ZHY_Lab/MeDIP-Seq_20110506/RawData_and_AlignmentResult/mappingFile/";
		String parentOut = "/media/winE/NBC/Project/Project_ZHY_Lab/��ũ-�ź���������/Annotation/";
		String txtExcelFile = parentOut + "N-Peak_anno.txt";
		String txtOutFile = parentOut + "Nvs2N-MethyChange.xls";

		gffChrCmpBed.setMapReads(parent+"Nextend_sort.bed", 5);
		gffChrCmpBed.loadMapReads();
		gffChrCmpBed.setMapReadsCmp(parent+"2Nextend_sort.bed", 5);
	
		gffChrCmpBed.readGeneList(txtExcelFile, 5, new int[]{-2000,0}, -100, true, txtOutFile);
	}
}































