package com.novelbio.analysis.seq.fasta.format;

import java.util.ArrayList;
import java.util.HashMap;

import javax.print.DocFlavor.STRING;

import com.novelbio.analysis.seq.fasta.SeqFastaHash;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
/** 
 * NCBI的repeat有问题
 * 染色体的NCBIID和chrID之间的转换表 */
public class NCBIrepeatChangeFormat {

	public static void main(String[] args) {
		NCBIrepeatChangeFormat ncbIrepeatChangeFormat = new NCBIrepeatChangeFormat();
		ncbIrepeatChangeFormat.setTxtRef("/media/winE/Bioinformatics/genome/human/hg19_GRCh37/gff/masking_coordinates", 
				"/media/winE/Bioinformatics/genome/human/hg19_GRCh37/gff/ref_GRCh37.p9_top_level_modify_ChrID_Tab.gff3");
		ncbIrepeatChangeFormat.writeOut("/media/winE/Bioinformatics/genome/human/hg19_GRCh37/gff/masking_coordinates_modified");
	}
	String txtRepeat = "";
	HashMap<String, String> mapAccID2ChrID = new HashMap<String, String>();
	/**
	 * @param txtRepeat
	 * @param accIDconvertTab 染色体的NCBIID和chrID之间的转换表
	 */
	public void setTxtRef(String txtRepeat, String accIDconvertTab) {
		this.txtRepeat = txtRepeat;
		ArrayList<String[]> lsTab = ExcelTxtRead.readLsExcelTxt(accIDconvertTab, 1);
		for (String[] string : lsTab) {
			mapAccID2ChrID.put(string[0], string[1]);
		}
	}
	public void writeOut(String txtOut) {
		TxtReadandWrite txtRead = new TxtReadandWrite(txtRepeat, false);
		TxtReadandWrite txtWrite = new TxtReadandWrite(txtOut, true);
		for (String string : txtRead.readlines()) {
			String[] tmp = string.split("\t");
			tmp[0] = mapAccID2ChrID.get(tmp[0]);
			if (tmp[0] == null) {
				continue;
			}
			txtWrite.writefileln(tmp);
		}
		txtRead.close();
		txtWrite.close();
	}

}
