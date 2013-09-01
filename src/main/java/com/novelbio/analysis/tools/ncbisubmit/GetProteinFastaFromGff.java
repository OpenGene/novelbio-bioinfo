package com.novelbio.analysis.tools.ncbisubmit;

import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqFastaHash;
import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

/** 从最后的gff文件中将蛋白序列提取出来 */
public class GetProteinFastaFromGff {
	public static void main(String[] args) {
		GetProteinFastaFromGff getProteinFastaFromGff = new GetProteinFastaFromGff();
		getProteinFastaFromGff.gffFile = "/media/winF/NBC/Project/Project_WZF/annotation/finalBacterium.Gene.Prediction.anno_out_All_final.gff";
		getProteinFastaFromGff.seqFile = "/media/winF/NBC/Project/Project_WZF/annotation/finalBacterium.txt";
		getProteinFastaFromGff.extractFile();
		
	}
	String gffFile;
	String seqFile;
	public void extractFile() {
		SeqHash seqFastaHash = new SeqHash(seqFile);
		TxtReadandWrite txtReadGff = new TxtReadandWrite(gffFile, false);
		TxtReadandWrite txtWritePro = new TxtReadandWrite(FileOperate.changeFileSuffix(seqFile, "_Pro", null), true);
		
		for (String content : txtReadGff.readlines()) {
			String[] ss = content.split("\t");
			if (!ss[2].equals("gene")) {
				SequinGene sequinGene = new SequinGene(content);
				continue;
			}
			SequinGene sequinGene = new SequinGene(content);
			boolean cis5to3 = ss[6].equals("+");
			SeqFasta seqFasta = seqFastaHash.getSeq("bacterium", Integer.parseInt(ss[3]), Integer.parseInt(ss[4]));
			if (!cis5to3) {
				seqFasta = seqFasta.reservecom();
			}
			seqFasta.setName(sequinGene.toTitle());
			txtWritePro.writefileln(seqFasta.toStringAAfasta());
		}
		seqFastaHash.close();
		txtReadGff.close();
		txtWritePro.close();
	}
}
