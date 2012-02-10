package com.novelbio.database.updatedb.database;
/**
 * 
 * 将数据库
 * @author zong0jie
 *
 */
public class ImportDB {
	
	public static void main(String[] args) {
		importData();
	}
	public static void importData()
	{
		String taxIDFile = "/media/winE/Bioinformatics/UpDateDB/常见物种IDKEGGAll.txt";
		String NCBIDBPath = "/media/winE/Bioinformatics/DataBase/";
		String GOPath = "/media/winE/Bioinformatics/DataBase/GO/";
		String gene2Acc = NCBIDBPath + "gene2accession.gz";
		String gene2Ref = NCBIDBPath + "gene2refseq.gz";
		String gene2ensembl = NCBIDBPath + "gene2ensembl.gz";
		String geneRef2UniID = NCBIDBPath + "gene_refseq_uniprotkb_collab.gz";
		String gene2Info = NCBIDBPath + "gene_info.gz";
		String gene2Pub = NCBIDBPath + "gene2pubmed.gz";
		String goExtObo = GOPath + "gene_ontology_ext.obo";
		String gene2GO = NCBIDBPath + "gene2go.gz";
		NCBI ncbi = new NCBI();
		ncbi.setTaxID(taxIDFile);
		ncbi.setGene2AccFile(gene2Acc, gene2Ref);
		ncbi.setGene2Ensembl(gene2ensembl);
		ncbi.setGene2Info(gene2Info);
		ncbi.setGene2Pub(gene2Pub);
		ncbi.setGeneRef2UniID(geneRef2UniID);
		ncbi.setGOExtObo(goExtObo);
		ncbi.setGene2GO(gene2GO);
//		ncbi.importFile();
		
		String outUniIDFile = NCBIDBPath + "outIdmap.txt";
		String idmappingSelectedFile = NCBIDBPath + "idmapping_selected.tab_sub.gz";
		String impgene_associationgoa_uniprotFile = NCBIDBPath + "GO/gene_association.goa_uniprot.gz";
		UniProt uniProt = new UniProt();
		uniProt.setIdmappingSelectedFile(idmappingSelectedFile);
		uniProt.setTaxIDFile(taxIDFile);
		uniProt.setOutUniIDFile(outUniIDFile);
		uniProt.setImpgene_associationgoa_uniprotFile(impgene_associationgoa_uniprotFile);
//		uniProt.update();
		String ensemblFileMouse = "/media/winE/Bioinformatics/DataBase/Mus_musculus.NCBIM37.65.gtf"; 
		String ucscGffFileMouse = "/media/winE/Bioinformatics/GenomeData/mouse/ucsc_mm9/refseqSortUsing.txt";
		int taxIDMouse = 10090;
		
		String ensemblFileChicken = ""; String ucscGffFileChicken = "";
		int taxIDChicken = 9031;
		
		Ensembl ensembl = new Ensembl();
		ensembl.setEnsemblFile(ensemblFileMouse, ucscGffFileMouse, taxIDMouse);
//		ensembl.setEnsemblFile(ensemblFileChicken, ucscGffFileChicken, taxIDChicken);
		ensembl.update();
		
	}
	
	
}
