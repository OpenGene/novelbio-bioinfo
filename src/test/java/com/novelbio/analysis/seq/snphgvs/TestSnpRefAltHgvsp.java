package com.novelbio.analysis.seq.snphgvs;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.analysis.seq.snphgvs.SnpRefAltInfo;
import com.novelbio.analysis.seq.snphgvs.SnpRefAltHgvsc;
import com.novelbio.database.model.modgeneid.GeneType;

import EDU.oswego.cs.dl.util.concurrent.FJTask.Seq;

public class TestSnpRefAltHgvsp {
	GffHashGene gffHashGene = new GffHashGene("/home/novelbio/NBCresource/genome/species/9606/hg19_GRCh37/gff/ref_GRCh37.p13_top_level.gff3.gz");
	SeqHash seqHash = new SeqHash("/home/novelbio/NBCresource/genome/species/9606/hg19_GRCh37/ChromFa/chrAll.fa");
	
	@Test
	public void testAnno() {
			
		SnpRefAltInfo snpRefAltInfo = new SnpRefAltInfo("chr1", 173010498, "ATCAAGTCTCTA", "A");
		snpRefAltInfo.setSeqHash(seqHash);
		snpRefAltInfo.copeInputVar();
		snpRefAltInfo.setVarHgvsType();
		SnpRefAltHgvsp snpRefAltHgvsp = SnpRefAltHgvsp.generateSnpRefAltHgvsp(snpRefAltInfo,
				gffHashGene.searchISO("NM_005092"));
		String value = snpRefAltHgvsp.getHgvsp(seqHash);
		System.out.println(value);
		
		snpRefAltInfo = new SnpRefAltInfo("chr1", 173470228, "AGTTTTCAACTATAA", "A");
		snpRefAltInfo.setSeqHash(seqHash);
		snpRefAltInfo.copeInputVar();
		snpRefAltInfo.setVarHgvsType();
		snpRefAltHgvsp = SnpRefAltHgvsp.generateSnpRefAltHgvsp(snpRefAltInfo,
				gffHashGene.searchISO("NM_178527"));
		value = snpRefAltHgvsp.getHgvsp(seqHash);
		System.out.println(value);
	}
	
}
