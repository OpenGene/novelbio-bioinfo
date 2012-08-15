package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;

import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqFastaHash;
import com.novelbio.database.model.modgeneid.GeneID;

/**给定基因的mRNA序列和protein序列，生成一个GffHashGene，其中每个基因就是一个独立的ListGff */
public class GffHashGeneRefSeq extends GffHashGeneAbs{
	
	String proteinSeq = "";
	public void setProteinSeq(String proteinSeq) {
		this.proteinSeq = proteinSeq;
	}
	/** 这里的GffFfileName就是mRNA序列 */
	@Override
	protected void ReadGffarrayExcep(String gfffilename) throws Exception {
		SeqFastaHash seqHashMRNA = new SeqFastaHash(gfffilename);
		setTaxID(seqHashMRNA);
		seqHashMRNA.setDNAseq(true);
		SeqFastaHash seqHashProtein = new SeqFastaHash(proteinSeq);
		for (String seqName : seqHashMRNA.getLsSeqName()) {
			SeqFasta seqFasta = seqHashMRNA.getSeqFasta(seqName);
			String proteinSeq = seqHashProtein.getSeqFasta(seqName).toString();
			ListGff listGff = seqFasta.getCDSfromProtein(proteinSeq).getGffDetailGene();
			mapChrID2ListGff.put(listGff.getName().toString(), listGff);
		}
	}
	private void setTaxID(SeqFastaHash seqHashMRNA) {
		ArrayList<String> lsGeneName = seqHashMRNA.getLsSeqName();
		int queryNum = 0;
		for (String geneName : lsGeneName) {
			ArrayList<GeneID> lsCopedIDs = GeneID.createLsCopedID(geneName, 0, false);
			if (lsCopedIDs.size() == 1 && lsCopedIDs.get(0).getIDtype() != GeneID.IDTYPE_ACCID) {
				taxID = lsCopedIDs.get(0).getTaxID();
				break;
			}
			if (queryNum > 100) {
				break;
			}
			queryNum++;
		}
	}
}
