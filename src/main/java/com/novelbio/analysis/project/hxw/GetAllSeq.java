package com.novelbio.analysis.project.hxw;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.analysis.seq.genomeNew.GffChrSeq;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFasta;
import com.novelbio.base.dataOperate.TxtReadandWrite;

/**
 * 获得全部人类refseq的蛋白序列
 * @author zong0jie
 *
 */
public class GetAllSeq {

	public static void main(String[] args) {
		getSeq();
	}
	public static void getSeq() {
		TxtReadandWrite txtOut = new TxtReadandWrite("/media/winE/NBC/Project/Project_HXW_Lab/aa.txt",true);
		GffChrSeq gffChrSeq = new GffChrSeq(NovelBioConst.GENOME_GFF_TYPE_UCSC,  
				NovelBioConst.GENOME_PATH_UCSC_HG19_GFF_REFSEQ, NovelBioConst.GENOME_PATH_UCSC_HG19_CHROM);
//		gffChrSeq.setGffFile(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF, "/media/winE/NBC/Project/Project_FY_Lab/Result/cufflinkAll/cufcompare/cmpAll.combined_cope.gtf");
		gffChrSeq.loadChrFile();
		ArrayList<SeqFasta> lsSeqFastas = gffChrSeq.getSeqProteinAll();
		HashSet<String> hash = new HashSet<String>();
		for (SeqFasta seqFasta : lsSeqFastas) {
			if (seqFasta.toStringAAfasta().contains("*")) {
				continue;
			}
			if (hash.contains(seqFasta.getSeqName())) {
				continue;
			}
			hash.add(seqFasta.getSeqName());
			txtOut.writefileln(seqFasta.toStringAAfasta());
		}
		txtOut.close();
	}
	

}
