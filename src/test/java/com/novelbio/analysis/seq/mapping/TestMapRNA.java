package com.novelbio.analysis.seq.mapping;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffOperate.GffType;
import com.novelbio.analysis.seq.mapping.MapRNA;
import com.novelbio.analysis.seq.mapping.MapSplice;
import com.novelbio.analysis.seq.mapping.MapTophat;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;
import com.novelbio.database.model.species.Species;

public class TestMapRNA {
	MapRNA mapRNA;
	List<FastQ> lsLeftFq = new ArrayList<>();
	List<FastQ> lsRightFq = new ArrayList<>();

	
	public void testRsem() {
		
	}
//	@Test
//	public void testMapSplice() {
//		lsLeftFq.add(new FastQ("/media/hdfs/nbCloud/public/test/RNASeqMap/MouseRNA_KO_L1_1_Small.fq.gz"));
//		lsRightFq.add(new FastQ("/media/hdfs/nbCloud/public/test/RNASeqMap/MouseRNA_KO_L1_2_Small.fq.gz"));
//		mapRNA = new MapSplice();
//		Species species = new Species(10090);		
//		
//		mapRNA.setRefIndex(species.getIndexChr(SoftWare.bowtie));
//		mapRNA.setGtf_Gene2Iso(species.getChromSeqSep());
//		mapRNA.setLeftFq(lsLeftFq);
//		mapRNA.setRightFq(lsRightFq);
//		mapRNA.setThreadNum(3);
//		mapRNA.setOutPathPrefix("/media/hdfs/nbCloud/public/test/RNASeqMap/mapsplice_");
//		mapRNA.mapReads();
//		lsLeftFq.clear();
//		lsRightFq.clear();
//	}
	
//	@Test
//	public void testTophat() {
//		CmdOperate.setTmpPath("/home/novelbio/tmp/indexTophat");
//		lsLeftFq.add(new FastQ("/hdfs:/nbCloud/public/test/RNASeqMap/MouseRNA_KO_L1_1_Small.fq.gz"));
//		lsRightFq.add(new FastQ("/hdfs:/nbCloud/public/test/RNASeqMap/MouseRNA_KO_L1_2_Small.fq.gz"));
//		mapRNA = new MapTophat();
//
//		Species species = new Species(3702);
//		species.setVersion("tair10");
//	
//		mapRNA.setGffChrAbs(new GffChrAbs(species));
//		mapRNA.setRefIndex(species.getIndexChr(SoftWare.bowtie2));
//		mapRNA.setGtf_Gene2Iso("");
//		mapRNA.setOutPathPrefix("/hdfs:/nbCloud/public/test/RNASeqMap/ddd");
//		mapRNA.setLeftFq(lsLeftFq);
//		mapRNA.setRightFq(lsRightFq);
//		mapRNA.setThreadNum(3);
//		mapRNA.mapReads();
//		
//		lsLeftFq.clear();
//		lsRightFq.clear();
//	}
	@Test
	public void testTophat2() {
		CmdOperate.setTmpPath("/home/novelbio/tmp/indexTophat");
		lsLeftFq.add(new FastQ("/hdfs:/nbCloud/public/test/RNASeqMap/MouseRNA_KO_L1_1_Small.fq.gz"));
		lsRightFq.add(new FastQ("/hdfs:/nbCloud/public/test/RNASeqMap/MouseRNA_KO_L1_2_Small.fq.gz"));
		Species species = new Species(9606);
		mapRNA = new MapTophat(new GffChrAbs(species));
	
		mapRNA.setRefIndex(species.getIndexChr(SoftWare.bowtie2));
		mapRNA.setGtf_Gene2Iso("");
		mapRNA.setOutPathPrefix("/hdfs:/nbCloud/public/test/RNASeqMap/human");
		mapRNA.setLeftFq(lsLeftFq);
		mapRNA.setRightFq(lsRightFq);
		mapRNA.setThreadNum(3);
		mapRNA.mapReads();
		
		lsLeftFq.clear();
		lsRightFq.clear();
	}
//	@Test
//	public void testTophatIndex() {
//		CmdOperate.setTmpPath("/home/novelbio/tmp/indexTophat");
//		MapTophat mapRNA = new MapTophat();
//		Species species = new Species(3702);
//		species.setVersion("tair10");
//		mapRNA.setGffChrAbs(new GffChrAbs(species));
//		mapRNA.setRefIndex(species.getIndexChr(SoftWare.bowtie2));
//		mapRNA.setGtf_Gene2Iso("");
//		mapRNA.IndexGffMake();
//	}
}
