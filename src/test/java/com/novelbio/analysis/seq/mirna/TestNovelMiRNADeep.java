package com.novelbio.analysis.seq.mirna;

import java.io.File;
import java.util.List;

import org.junit.Test;

import com.novelbio.analysis.seq.bed.BedSeq;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;
import com.novelbio.database.model.species.Species;

public class TestNovelMiRNADeep {
	NovelMiRNADeep novelMiRNADeep = new NovelMiRNADeep();

	public void prepare() {
		Species species = new Species(9940);
		novelMiRNADeep.setExePath("", species.getIndexChr(SoftWare.bowtie));
		novelMiRNADeep.setGffChrAbs(new GffChrAbs(species));
		novelMiRNADeep.setMiRNASeq(species.getMiRNAmatureFile(), null, species.getMiRNAhairpinFile());
		novelMiRNADeep.setOutPath("/media/hdfs/nbCloud/public/test/miRNApredict/mirDeep");
		novelMiRNADeep.setSeqInput(new BedSeq("/media/hdfs/nbCloud/public/test/miRNApredict/sheepAlign.bed.gz"));
		novelMiRNADeep.setSpecies(species.getCommonName());
	}
	@Test
	public void test() {
		prepare();
		novelMiRNADeep.predict();
	}
}
