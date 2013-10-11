package com.novelbio.analysis.seq.mapping;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.mapping.MapRNA;
import com.novelbio.analysis.seq.mapping.MapSplice;
import com.novelbio.analysis.seq.mapping.MapTophat;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;
import com.novelbio.database.model.species.Species;

public class TestMapRNA {
	MapRNA mapRNA;
	List<FastQ> lsLeftFq = new ArrayList<>();
	List<FastQ> lsRightFq = new ArrayList<>();

	
	public void testRsem() {
		
	}
	@Test
	public void testMapSplice() {
		lsLeftFq.add(new FastQ("/media/hdfs/nbCloud/public/test/RNASeqMap/MouseRNA_KO_L1_1_Small.fq.gz"));
		lsRightFq.add(new FastQ("/media/hdfs/nbCloud/public/test/RNASeqMap/MouseRNA_KO_L1_2_Small.fq.gz"));
		mapRNA = new MapSplice();
		SoftWareInfo softMapSplice = new SoftWareInfo(SoftWare.mapsplice);
		SoftWareInfo softBowtie = new SoftWareInfo(SoftWare.bowtie);
		Species species = new Species(10090);		
		
		mapRNA.setExePath(softMapSplice.getExePath(), softBowtie.getExePath());
		mapRNA.setRefIndex(species.getIndexChr(SoftWare.bowtie));
		mapRNA.setGtf_Gene2Iso(species.getChromSeqSep());
		mapRNA.setLeftFq(lsLeftFq);
		mapRNA.setRightFq(lsRightFq);
		mapRNA.setThreadNum(3);
		mapRNA.setOutPathPrefix("/media/hdfs/nbCloud/public/test/RNASeqMap/mapsplice_");
		mapRNA.mapReads();
		lsLeftFq.clear();
		lsRightFq.clear();
	}
	
//	@Test
	public void testTophat() {
		lsLeftFq.add(new FastQ("/media/hdfs/nbCloud/public/test/RNASeqMap/MouseRNA_KO_L1_1_Small.fq.gz"));
		lsRightFq.add(new FastQ("/media/hdfs/nbCloud/public/test/RNASeqMap/MouseRNA_KO_L1_2_Small.fq.gz"));
		mapRNA = new MapTophat();
		SoftWareInfo softMapSplice = new SoftWareInfo(SoftWare.tophat);
		SoftWareInfo softBowtie = new SoftWareInfo(SoftWare.bowtie2);
		Species species = new Species(9606);
		
		mapRNA.setGffChrAbs(new GffChrAbs(9606));
		mapRNA.setExePath(softMapSplice.getExePath(), softBowtie.getExePath());
		mapRNA.setRefIndex(species.getIndexChr(SoftWare.bowtie2));
		mapRNA.setGtf_Gene2Iso("");
		mapRNA.setOutPathPrefix("/media/hdfs/nbCloud/public/test/RNASeqMap/ddd");
		mapRNA.setLeftFq(lsLeftFq);
		mapRNA.setRightFq(lsRightFq);
		mapRNA.setThreadNum(3);
		mapRNA.mapReads();
		
		lsLeftFq.clear();
		lsRightFq.clear();
	}
}
