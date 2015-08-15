package com.novelbio.test;

import htsjdk.samtools.CigarOperator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqFastaHash;
import com.novelbio.analysis.seq.fastq.FQrecordFilter;
import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.fastq.FastQRecord;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffOperate.GffCodGeneDU;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffType;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.analysis.seq.sam.SamRecord;
import com.novelbio.base.dataOperate.HttpFetch;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.model.modgeneid.GeneType;
import com.novelbio.database.model.species.Species;
import com.novelbio.database.mongorepo.kegg.RepoKEntry;
import com.novelbio.database.mongorepo.kegg.RepoKIDKeg2Ko;
import com.novelbio.database.mongorepo.kegg.RepoKIDgen2Keg;
import com.novelbio.database.mongorepo.kegg.RepoKNCompInfo;
import com.novelbio.database.mongorepo.kegg.RepoKNIdKeg;
import com.novelbio.database.mongorepo.kegg.RepoKPathRelation;
import com.novelbio.database.mongorepo.kegg.RepoKPathway;
import com.novelbio.database.mongorepo.kegg.RepoKReaction;
import com.novelbio.database.mongorepo.kegg.RepoKRelation;
import com.novelbio.database.mongorepo.kegg.RepoKSubstrate;
import com.novelbio.database.service.SpringFactoryBioinfo;


public class mytest {
//	private static final Logger logger = Logger.getLogger(mytest.class);
	static boolean is;

	public static void main(String[] args) throws Exception {
//		SamFile samFile = new SamFile("/home/novelbio/下载/test.sam");
//		for (SamRecord samRecord : samFile.readLines()) {
//		
//			
//			List<Align> lsaAligns = mergeNearbyAlign(samRecord.getAlignmentBlocks());
//			for (Align align : lsaAligns) {
//				List<String> lsGff = new ArrayList<>();
//				lsGff.add(samRecord.getRefID());
//				lsGff.add("novelbio");
//				lsGff.add("exon");
//				lsGff.add(align.getStartAbs() + "");
//				lsGff.add(align.getEndAbs() + "");
//            }
//		}
//		GeneID geneID = new GeneID("tp53", 9606);
//		System.out.println(geneID.getGeneUniID());
		
		GffHashGene gffHashGene = new GffHashGene("/home/novelbio/NBCresource/www/genes.gtf");
		gffHashGene.writeToGTF("/home/novelbio/NBCresource/www/genes_modify.gtf");
	}
	
	private static List<Align> mergeNearbyAlign(List<Align> lsAligns) {
		List<Align> lsAlignsResult = new ArrayList<>();
		Align alignLast = null;
		for (Align align : lsAligns) {
			if (alignLast == null) {
				alignLast = align;
				continue;
			}
			int distance = align.getStartAbs() - alignLast.getEndAbs();
			if (distance < 0) {
				throw new RuntimeException("distance less than 0");
			} else if (distance < 20) {
				System.out.println("merge");
				align.setStart(alignLast.getStartAbs());
			} else {
				lsAlignsResult.add(alignLast);
			}
			alignLast = align;
		}
		lsAlignsResult.add(alignLast);
		return lsAlignsResult;
    }
	
	
}
