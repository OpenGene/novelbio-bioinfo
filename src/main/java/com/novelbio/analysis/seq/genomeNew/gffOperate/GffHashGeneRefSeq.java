package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;

import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqFastaHash;
import com.novelbio.database.model.modgeneid.GeneID;

/**���������mRNA���к�protein���У�����һ��GffHashGene������ÿ���������һ��������ListGff */
public class GffHashGeneRefSeq extends GffHashGeneAbs{
	
	String proteinSeq = "";
	public void setProteinSeq(String proteinSeq) {
		this.proteinSeq = proteinSeq;
	}
	/** �����GffFfileName����mRNA���� */
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
