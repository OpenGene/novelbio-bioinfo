package com.novelbio.analysis.seq.fasta.format;

import com.novelbio.analysis.seq.fasta.SeqFastaHash;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.modgeneid.GeneID;

/**
 * 把NCBI的refseq的格式进行修正
 * @author zong0jie
 */
public class NCBIrefseqChangeFormat {
	public static void main(String[] args) {
		NCBIrefseqChangeFormat ncbIrefseqChangeFormat = new NCBIrefseqChangeFormat();
		ncbIrefseqChangeFormat.setTxtRef("/media/winE/Bioinformatics/GenomeData/maize/ZmB73_5a_WGS_translations.fasta/ZmB73_5a_WGS_translations.fasta");
		ncbIrefseqChangeFormat.writeOutZeaMaize();
	}
	String txtRef = "";
	public void setTxtRef(String txtRef) {
		this.txtRef = txtRef;
	}
	public void writeOut() {
		String txtOut = FileOperate.changeFileSuffix(txtRef, "_modify", "fa");
		SeqFastaHash seqFastaHash = new SeqFastaHash(txtRef, "\\w{2}_\\w+", true);
		seqFastaHash.writeToFile(txtOut);
	}
	
	public void writeOutZeaMaize() {
		TxtReadandWrite txtRead = new TxtReadandWrite(txtRef, false);
		TxtReadandWrite txtWrite = new TxtReadandWrite(FileOperate.changeFileSuffix(txtRef, "_modify", "fa"), true);
		for (String content : txtRead.readlines()) {
			if (content.contains(">")) {
				content = getGeneNameModifyMaize(content);
			}
			txtWrite.writefileln(content);
		}
		
		txtRead.close();
		txtWrite.close();
	}
	
	private static String getGeneNameModifyMaize(String geneNameRaw) {
		String geneName = geneNameRaw.replace(">", "").split(" ")[0];
		if (!geneName.startsWith("AC")) {
			geneName = geneName.split("_")[0];
		}
		geneName = GeneID.removeDot(geneName);
		return ">" + geneName;
	}
}
