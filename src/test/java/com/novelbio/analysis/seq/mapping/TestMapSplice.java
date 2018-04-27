package com.novelbio.analysis.seq.mapping;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.species.Species;
import com.novelbio.database.model.information.SoftWareInfo.SoftWare;

public class TestMapSplice {
	String fq1;
	String fq2;
	
	MapRNA mapRNA;
	List<FastQ> lsLeftFq = new ArrayList<>();
	List<FastQ> lsRightFq = new ArrayList<>();

	@Before
	public void before() {
		fq1 = "src/test/resources/test_file/fastq/PE/arabidopsis_rna_1.fq.gz";
		fq2 = "src/test/resources/test_file/fastq/PE/arabidopsis_rna_2.fq.gz";
		
		fq1 = getAbsolutePath(fq1);
		fq2 = getAbsolutePath(fq2);
		
		lsLeftFq.add(new FastQ(fq1));
		lsRightFq.add(new FastQ(fq2));
	}
	
	private String getAbsolutePath(String path) {
		File file = new File(path);
		return file.getAbsolutePath();
	}
	
	@Test
	public void testMapSplice() {
		MapSplice mapRNA = (MapSplice)MapRNAfactory.generateMapRNA(SoftWare.mapsplice);
		mapRNA.setThreadNum(5);
		
		Species species = new Species(3702);
		species.setVersion("tair10");
		
		mapRNA.getIndexMappingMaker().setLock(false);
		mapRNA.setRefIndex(species.getIndexChr(SoftWare.mapsplice));
		((MapSplice)mapRNA).setRefIndexFolder(species.getChromSeqSepFolder());
		mapRNA.setLeftFq(lsLeftFq);
		mapRNA.setRightFq(lsRightFq);
		mapRNA.setThreadNum(8);
		mapRNA.setOutPathPrefix("/tmp/mapsplice");
		
		Assert.assertFalse(FileOperate.isFileExistAndBigThan0(mapRNA.getFinishName()));

		mapRNA.mapReads();
		lsLeftFq.clear();
		lsRightFq.clear();
		Assert.assertTrue(FileOperate.isFileExistAndBigThan0(mapRNA.getFinishName()));
		for (String cmd : mapRNA.getCmdExeStr()) {
			System.out.println(cmd);
		}
	}
	
}
