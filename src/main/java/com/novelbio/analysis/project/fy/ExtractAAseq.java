package com.novelbio.analysis.project.fy;

import java.util.ArrayList;

import com.novelbio.analysis.seq.genomeNew.GffChrSeq;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFasta;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.generalConf.NovelBioConst;

/**
 * 周雪霞，鸡的转录本提取aa序列，打算做批量blast
 * @author zong0jie
 *
 */
public class ExtractAAseq {
	public static void main(String[] args) {
		getSeq();
	}
	private static void getSeq() {
		String txtOut = "/media/winE/Bioinformatics/GenomeData/checken/all_AA.seq";
		GffChrSeq gffChrSeq = new GffChrSeq(NovelBioConst.GENOME_GFF_TYPE_UCSC,  
				"/media/winE/Bioinformatics/GenomeData/checken/GeneLoc/chickenEnsemblGenes", "/media/winE/Bioinformatics/GenomeData/checken/chromFa");
		gffChrSeq.loadChrFile();
		ArrayList<SeqFasta> lsSeqFastaCds = gffChrSeq.getSeqCDSAll();
		TxtReadandWrite txtOutAA = new TxtReadandWrite(txtOut, true);
		for (SeqFasta seqFasta : lsSeqFastaCds) {
			txtOutAA.writefileln(seqFasta.toStringAAfasta());
		}
	}
}
