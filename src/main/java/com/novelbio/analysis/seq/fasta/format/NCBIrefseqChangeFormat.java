package com.novelbio.analysis.seq.fasta.format;

import com.novelbio.analysis.seq.fasta.SeqFastaHash;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * 把NCBI的refseq的格式进行修正
 * @author zong0jie
 */
public class NCBIrefseqChangeFormat {
	public static void main(String[] args) {
		NCBIrefseqChangeFormat ncbIrefseqChangeFormat = new NCBIrefseqChangeFormat();
		ncbIrefseqChangeFormat.setTxtRef("/media/winE/Bioinformatics/genome/checken/gal4_NCBI/protein.fa");
		ncbIrefseqChangeFormat.writeOut();
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
}
