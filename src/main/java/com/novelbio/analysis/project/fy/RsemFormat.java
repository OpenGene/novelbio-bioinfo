package com.novelbio.analysis.project.fy;

import java.util.HashMap;
import java.util.HashSet;

import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genomeNew.listOperate.ListAbs;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.modcopeid.CopedID;
import com.novelbio.generalConf.NovelBioConst;

/**
 * 将UCSC的ensembl中的iso都提取出来，然后做成 geneID \t isoID的式样
 * @author zong0jie
 *
 */
public class RsemFormat {
	
	public static void main(String[] args) {
//		String gtf = "/media/winE/Bioinformatics/GenomeData/mouse/ucsc_mm9/mm9_UCSC";
//		getGene2ID(gtf);
		getGene2IsoFasta();
	}
	
	private static void getGeneID(String gtf, String outGene2Iso, boolean searchDB) {
		TxtReadandWrite txtOut = new TxtReadandWrite(outGene2Iso, true);
		GffHashGene gffHashGene = new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_UCSC, gtf);
		HashMap<String, ListAbs<GffDetailGene>> hashGffGene = gffHashGene.getChrhash();
		for (ListAbs<GffDetailGene> lsGffGene : hashGffGene.values()) {
			for (GffDetailGene gffDetailGene : lsGffGene) {
				gffDetailGene.removeDupliIso();
				for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
					if (searchDB) {
						CopedID copedID = new CopedID(gffGeneIsoInfo.getName(), gffGeneIsoInfo.getTaxID());
						if (!copedID.getIDtype().equals(CopedID.IDTYPE_ACCID)) {
							txtOut.writefileln(copedID.getSymbol() + "\t" + gffGeneIsoInfo.getName());
						}
						else {
							txtOut.writefileln(gffDetailGene.getName().split(GffDetailGene.SEP_GENE_NAME)[0] + "\t" + gffGeneIsoInfo.getName());
						}
					}
					else {
						txtOut.writefileln(gffDetailGene.getName().split(GffDetailGene.SEP_GENE_NAME)[0] + "\t" + gffGeneIsoInfo.getName());
					}
				}
			}
		}
		txtOut.close();
	}
	
	private static void getGene2ID(String ucscTable)
	{
		TxtReadandWrite txtRead = new TxtReadandWrite(ucscTable, false);
		TxtReadandWrite txtOut = new TxtReadandWrite(FileOperate.changeFileSuffix(ucscTable, "_gene2iso", "txt"), true);
		for (String string : txtRead.readlines()) {
			String[] ss = string.split("\t");
			txtOut.writefileln(ss[12] + "\t" + ss[1]);
		}
		txtOut.close();
	}
	
	private static void getGene2IsoUsingGTF()
	{
		String ucscTable = "/media/winE/Bioinformatics/GenomeData/mouse/ucsc_mm9/rsem/mouse_mm9_UCSC.GTF";
		String filepath = "/media/winE/Bioinformatics/GenomeData/mouse/ucsc_mm9/rsem/mm9_UCSC_gene2iso.txt";
		TxtReadandWrite txtRead = new TxtReadandWrite(ucscTable, false);
		TxtReadandWrite txtReadAlready = new TxtReadandWrite(filepath, false);
		TxtReadandWrite txtOut = new TxtReadandWrite(FileOperate.changeFileSuffix(ucscTable, "_gene2isoOut", "txt"), true);
		HashSet<String> hashGeneID = new HashSet<String>();
		for (String string : txtReadAlready.readlines()) {
			hashGeneID.add(string.split("\t")[1]);
		}
		for (String string : txtRead.readlines()) {
			String[] ss = string.split("\t");
			String geneID = ss[8].split(";")[1].replace("transcript_id", "").replace("\"", "").trim();
			if (hashGeneID.contains(geneID)) {
				continue;
			}
			else {
				CopedID copedID = new CopedID(geneID, 10090);
				txtOut.writefileln(copedID.getSymbol() + "\t" + geneID);
				hashGeneID.add(geneID);
			}
		}
		
		txtRead.close();
		txtReadAlready.close();
		txtOut.close();
	}
	
	public static void filterGTF() {
		String ucscTable = "/media/winE/Bioinformatics/GenomeData/mouse/ucsc_mm9/rsem/mouse_mm9_UCSC.GTF";
		TxtReadandWrite txtRead = new TxtReadandWrite(ucscTable, false);
		TxtReadandWrite txtOut = new TxtReadandWrite(FileOperate.changeFileSuffix(ucscTable, "_Filtered2", "txt"), true);
		HashSet<String> hashGeneID = new HashSet<String>();
		String tmpID = ""; boolean con = false;
		for (String string : txtRead.readlines()) {
			String[] ss = string.split("\t");
			String geneID = ss[8].split(";")[1].replace("transcript_id", "").replace("\"", "").trim();
			if (!ss[2].equals("exon")) {
				continue;
			}
			if (!geneID.equals(tmpID) ) {
				if (hashGeneID.contains(geneID)) {
					con = true;
				}
				else {
					con = false;
					hashGeneID.add(geneID);
				}
				tmpID = geneID;
			}
			
			if (!con) {
				txtOut.writefileln(string);
			}
		}
		
		txtRead.close();
		txtOut.close();
	}
	
	private static void getGene2IsoFasta()
	{
		String fasta = "/media/winE/Bioinformatics/GenomeData/mouse/ucsc_mm9/rsem/refMrna.fa";
		String filepath = "/media/winE/Bioinformatics/GenomeData/mouse/ucsc_mm9/rsem/mm9_UCSC_gene2iso.txt";
		TxtReadandWrite txtRead = new TxtReadandWrite(fasta, false);
		TxtReadandWrite txtReadAlready = new TxtReadandWrite(filepath, false);
		TxtReadandWrite txtOut = new TxtReadandWrite(FileOperate.changeFileSuffix(fasta, "_gene2isoOut", "txt"), true);
		HashSet<String> hashGeneID = new HashSet<String>();
		for (String string : txtReadAlready.readlines()) {
			hashGeneID.add(string.split("\t")[1]);
		}
		for (String string : txtRead.readlines()) {
			if (!string.startsWith(">")) {
				continue;
			}
			String geneID = string.replace(">", "");
			if (hashGeneID.contains(geneID)) {
				continue;
			}
			else {
				CopedID copedID = new CopedID(geneID, 10090);
				txtOut.writefileln(copedID.getSymbol() + "\t" + geneID);
				hashGeneID.add(geneID);
			}
		}
		
		txtRead.close();
		txtReadAlready.close();
		txtOut.close();
	}
	
}
