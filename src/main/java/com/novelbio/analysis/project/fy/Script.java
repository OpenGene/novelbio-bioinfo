package com.novelbio.analysis.project.fy;

import java.awt.image.TileObserver;
import java.util.ArrayList;
import java.util.HashMap;

import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * ɾ��gff�е�CDS��Ŀ
 * @author zong0jie
 *
 */
public class Script {
	public static void main(String[] args) {
//		removeCDS("/media/winE/bioinformaticsTools/RNA-Seq/mm9/Mus_musculus.NCBIM37.65.gff", "/media/winE/bioinformaticsTools/RNA-Seq/mm9/aaas");
		addEmblInfo();
	}
	/**
	 * @param inFile
	 * @param outFile
	 */
	private static void removeCDS(String inFile, String outFile) {
		TxtReadandWrite txtRead = new TxtReadandWrite(inFile, false);
		TxtReadandWrite txtOut = new TxtReadandWrite(outFile, true);
		for (String string : txtRead.readlines()) {
			if (string.startsWith("#") || string.trim().equals("")) {
				continue;
			}
			String[] ss = string.split("\t");
			if (ss[2].equals("CDS")) {
				continue;
			}
			txtOut.writefileln(string);
		}
		txtRead.close();
		txtOut.close();
	}
	/**
	 * ��ѩϼ˵symbolû����igv���������embl��ID
	 * ���embl�ı��
	 */
	private static void addEmblInfo()
	{
		String txtAnno = "/media/winF/NBC/Project/Project_FY/chicken/Result/rsem/DEGseq/WT0vsWT5_anno.xls";
		//��ȡ�ֵ�
		String idTable = "/media/winE/Bioinformatics/GenomeData/checken/rsem/all_Gene2Iso.txt";
		//���
		String txtOut = FileOperate.changeFileSuffix(txtAnno, "_ensembl", null);
		TxtReadandWrite txtResult = new TxtReadandWrite(txtOut, true);
		ArrayList<String[]> lsGene2Ensembl = ExcelTxtRead.readLsExcelTxt(idTable, 2);
		HashMap<String, String> hashID2embl = new HashMap<String, String>();
		for (String[] strings : lsGene2Ensembl) {
			hashID2embl.put(strings[0], strings[1]);
		}
		ArrayList<String[]> lsInfo = ExcelTxtRead.readLsExcelTxt(txtAnno, 1);
		String title[] = ArrayOperate.copyArray(lsInfo.get(0), lsInfo.get(0).length + 1);
		title[title.length - 1] = "EnsemblID";
		txtResult.writefileln(title);
		title[title.length - 1] = "EnsemblID";
		for (int j = 1; j < lsInfo.size(); j++) {
			String[] strings = lsInfo.get(j);
			String[] ss = ArrayOperate.copyArray(strings, title.length);
			for (int i = 0; i < ss.length; i++) {
				if (ss[i] == null) {
					ss[i] = "";
				}
			}
			ss[ss.length - 1] = hashID2embl.get(strings[0]);
			txtResult.writefileln(ss);
		}
		txtResult.close();
	}
}
