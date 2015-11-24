package com.novelbio.analysis.seq.denovo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import picard.annotation.Gene;

import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqFastaReader;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

public class TestCap3Cluster {
	@Test
	public void testGenerateCompareTab() {
		String cap3ResultFile = "src/test/resources/test_file/denovo/cap3ClusterResult.txt";
		String geneId2TransIdFile = "src/test/resources/test_file/denovo/cap3GeneId2TransId.txt";
		
		ContigId2TranId contigId2TranId = new ContigId2TranId();
		contigId2TranId.setCAP3File(cap3ResultFile, null);
		contigId2TranId.setOutContigIDToTranIDFile(geneId2TransIdFile);
		contigId2TranId.generateCompareTab();
		
		Set<String> setTransId = contigId2TranId.getSetTransId();
		Assert.assertEquals(30, setTransId.size());
		
		TxtReadandWrite txtRead = new TxtReadandWrite(geneId2TransIdFile);
		
		List<String> lsGeneId2TransId = txtRead.readfileLs();
		txtRead.close();
		Assert.assertEquals(30, lsGeneId2TransId.size());
		Assert.assertEquals("Contig1\tTRINITY_DN28859_c0_g1_i1@@32R_24h", lsGeneId2TransId.get(0));
		
		FileOperate.DeleteFileFolder(geneId2TransIdFile);
	}
	
	@Test
	public void testGenerateCompareTabWithGeneId() {
		String cap3ResultFile = "src/test/resources/test_file/denovo/cap3ClusterResult.txt";
		String geneId2TransIdFile = "src/test/resources/test_file/denovo/cap3GeneId2TransId.txt";
		
		Set<String> setGeneBeFiltere = new HashSet<>();
		setGeneBeFiltere.add("Contig2"); setGeneBeFiltere.add("Contig3"); setGeneBeFiltere.add("Contig4");
		
		ContigId2TranId contigId2TranId = new ContigId2TranId();
		contigId2TranId.setCAP3File(cap3ResultFile, setGeneBeFiltere);
		contigId2TranId.setOutContigIDToTranIDFile(geneId2TransIdFile);
		contigId2TranId.generateCompareTab();
		
		Set<String> setTransId = contigId2TranId.getSetTransId();
		Assert.assertEquals(7, setTransId.size());
		
		TxtReadandWrite txtRead = new TxtReadandWrite(geneId2TransIdFile);
		
		List<String> lsGeneId2TransId = txtRead.readfileLs();
		txtRead.close();
		Assert.assertEquals(7, lsGeneId2TransId.size());
		Assert.assertEquals("Contig2\tTRINITY_DN28868_c0_g1_i1@@32R_24h", lsGeneId2TransId.get(0));
		
		FileOperate.DeleteFileFolder(geneId2TransIdFile);
	}
	
	@Test
	public void testGenerateClusterFa() {
		ClusterCAP3 cap3Cluster = new ClusterCAP3();
		cap3Cluster.outMergedFile = "src/test/resources/test_file/denovo/merged";
		cap3Cluster.finalClusterResult = "src/test/resources/test_file/denovo/final.fa";
		cap3Cluster.setOutDir("src/test/resources/test_file/denovo");
		Set<String> setGeneId = cap3Cluster.generateResultClusterFaAndGetFilteredGeneName();
		Assert.assertEquals(14, setGeneId.size());
		
		Set<String> setGeneId2 = new HashSet<>();
		SeqFastaReader seqFastaReader = new SeqFastaReader(cap3Cluster.finalClusterResult);
		for (SeqFasta seqFasta : seqFastaReader.readlines()) {
			setGeneId2.add(seqFasta.getSeqName());
        }
		Assert.assertEquals(setGeneId, setGeneId2);
		FileOperate.DeleteFileFolder(cap3Cluster.finalClusterResult);
	}
	
}
