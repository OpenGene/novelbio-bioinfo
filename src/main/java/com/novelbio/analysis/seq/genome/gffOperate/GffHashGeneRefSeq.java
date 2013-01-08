package com.novelbio.analysis.seq.genome.gffOperate;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.velocity.app.event.ReferenceInsertionEventHandler.referenceInsertExecutor;
import org.hamcrest.core.Is;

import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqFastaHash;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.modgeneid.GeneID;

/**给定基因的mRNA序列和protein序列，生成一个GffHashGene，其中每个基因就是一个独立的ListGff */
public class GffHashGeneRefSeq extends GffHashGeneAbs{
	public static void main(String[] args) {
		String proteinSeq = "/media/winE/Bioinformatics/GenomeData/CriGri/protein_Cope.fa";
		String gfffilename = "/media/winE/Bioinformatics/GenomeData/CriGri/rna_Cope.fa";
		GffHashGeneRefSeq gffHashGeneRefSeq = new GffHashGeneRefSeq();
		gffHashGeneRefSeq.setProteinSeq(proteinSeq);
		gffHashGeneRefSeq.ReadGffarray(gfffilename);
		GffCodGene gffCodGene = gffHashGeneRefSeq.searchLocation("XM_003494920", 73);
		GffDetailGene gffDetailGene = gffCodGene.getGffDetailThis();
		System.out.println(gffDetailGene.getLongestSplitMrna().getATGsite());
		System.out.println(gffDetailGene.getLongestSplitMrna().getCod2ATG(74));
		
		
		gffCodGene = gffHashGeneRefSeq.searchLocation("xm_003494920", 74);
		gffDetailGene = gffCodGene.getGffDetailThis();
		System.out.println(gffDetailGene.getLongestSplitMrna().getATGsite());
		System.out.println(gffDetailGene.getLongestSplitMrna().getCod2ATG(74));
	}
	
	
	String proteinSeq = "";
	HashMap<String, String> mapRNAID2GeneID = new HashMap<String, String>();
	HashMap<String, String> mapGeneID2ProteinID = new HashMap<String, String>();

	public void setProteinSeq(String proteinSeq) {
		this.proteinSeq = proteinSeq;
	}
	/** 这里的GffFfileName就是mRNA序列 */
	@Override
	protected void ReadGffarrayExcepTmp(String gfffilename) throws Exception {
		SeqFastaHash seqHashMRNA = new SeqFastaHash(gfffilename, null, false);
		SeqFastaHash seqHashProtein = new SeqFastaHash(proteinSeq);

		setTaxID(seqHashMRNA);
		seqHashMRNA.setDNAseq(true);
		fillID(seqHashMRNA, seqHashProtein);
		ArrayList<String> lsGeneID = seqHashMRNA.getLsSeqName();
		for (String seqName : lsGeneID) {
			SeqFasta seqFasta = seqHashMRNA.getSeqFasta(seqName);
			String proteinSeq = getProteinSeq(seqName, seqHashProtein);
			ListGff listGff = seqFasta.getCDSfromProtein(proteinSeq).getGffDetailGene();
		
			mapChrID2ListGff.put(listGff.getName().toString().toLowerCase(), listGff);
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
	private void fillID(SeqFastaHash seqHashMRNA, SeqFastaHash seqHashProtein) {
		fillMapRNAID2GeneID(seqHashMRNA);
		fillMapGeneID2ProteinID(seqHashProtein);
	}
	private void fillMapRNAID2GeneID(SeqFastaHash seqHashMRNA ) {
		String fileRNAID2GeneID = FileOperate.changeFileSuffix(seqHashMRNA.getChrFile(), "_RNAID2GeneID", "txt");
		if (FileOperate.isFileExist(fileRNAID2GeneID)) {
			TxtReadandWrite txtReadMapRNAID2GeneID = new TxtReadandWrite(fileRNAID2GeneID, false);
			for (String string : txtReadMapRNAID2GeneID.readlines()) {
				String[] tmp = string.split("\t");
				mapRNAID2GeneID.put(tmp[0].toLowerCase(), tmp[1]);
			}
			return;
		}
		
		TxtReadandWrite txtOutMapRNAID2GeneID = new TxtReadandWrite(fileRNAID2GeneID, true);
		for (String seqName : seqHashMRNA.getLsSeqName()) {
			GeneID geneID = new GeneID(seqName, taxID);
			String symbol = geneID.getSymbol();
			mapRNAID2GeneID.put(seqName.toLowerCase(), symbol);
			txtOutMapRNAID2GeneID.writefileln(seqName + "\t" + symbol);
		}
		txtOutMapRNAID2GeneID.close();
	}
	private void fillMapGeneID2ProteinID(SeqFastaHash seqHashProtein ) {
		String fileGeneID2ProteinID = FileOperate.changeFileSuffix(seqHashProtein.getChrFile(), "_GeneID2ProteinID", "txt");
		if (FileOperate.isFileExist(fileGeneID2ProteinID)) {
			TxtReadandWrite txtReadGeneID2ProteinID = new TxtReadandWrite(fileGeneID2ProteinID, false);
			for (String string : txtReadGeneID2ProteinID.readlines()) {
				String[] tmp = string.split("\t");
				mapGeneID2ProteinID.put(tmp[0].toLowerCase(), tmp[1]);
			}
			return;
		}
		
		TxtReadandWrite txtOutMapGeneID2ProteinID = new TxtReadandWrite(fileGeneID2ProteinID, true);
		for (String seqName : seqHashProtein.getLsSeqName()) {
			GeneID geneID = new GeneID(seqName, taxID);
			String symbol = geneID.getSymbol();
			//如果重复的symobl，则选择长的那条序列的名字装入hash表
			if (mapGeneID2ProteinID.containsKey(symbol.toLowerCase())) {
				String seqNameOld = mapGeneID2ProteinID.get(symbol.toLowerCase());
				SeqFasta seqFastaOld = seqHashProtein.getSeqFasta(seqNameOld);
				SeqFasta seqFastaNew = seqHashProtein.getSeqFasta(seqName);
				if (seqFastaNew.Length() > seqFastaOld.Length()) {
					mapGeneID2ProteinID.put(symbol.toLowerCase(), seqName);
					txtOutMapGeneID2ProteinID.writefileln(symbol + "\t" + seqName);
				}
			}
			else {
				mapGeneID2ProteinID.put(symbol.toLowerCase(), seqName);
				txtOutMapGeneID2ProteinID.writefileln(symbol + "\t" + seqName);
			}
		}
		txtOutMapGeneID2ProteinID.close();
	}
	
	private String getProteinSeq(String rnaLoc, SeqFastaHash seqHashProtein) {
		String geneID = mapRNAID2GeneID.get(rnaLoc.toLowerCase());
		String proteinID = mapGeneID2ProteinID.get(geneID.toLowerCase());
		if (proteinID == null) {
			return null;
		}
		SeqFasta seqFasta = seqHashProtein.getSeqFasta(proteinID);
		if (seqFasta == null) {
			return null;
		}
		return seqFasta.toString();
	}
}
