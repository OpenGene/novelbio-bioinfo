package com.novelbio.database.updatedb.database;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * 
 * 将数据库
 * @author zong0jie
 *
 */
public class ImportDB {

	public static void main(String[] args) {
		
//		updateNCBIID();
//		updateUniprotID();
//		updateRiceID();//只导了前两个
//		updateEnsembl();
		updateBlast();
	}
	/**
	 * 升级从NCBI下载的信息
	 */
	private static void updateNCBIID()
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
		ncbi.importFile();
	}
	/**
	 * 升级从UniProt下载的信息
	 */
	private static void updateUniprotID() {
		String taxIDFile = "/media/winE/Bioinformatics/UpDateDB/常见物种IDKEGGAll.txt";
		String NCBIDBPath = "/media/winE/Bioinformatics/DataBase/";
		String outUniIDFile = NCBIDBPath + "outIdmap.txt";
		String idmappingSelectedFile = NCBIDBPath + "idmapping_selected.tab_sub.gz";
		String impgene_associationgoa_uniprotFile = NCBIDBPath + "GO/gene_association.goa_uniprot.gz";
		UniProt uniProt = new UniProt();
		uniProt.setIdmappingSelectedFile(idmappingSelectedFile);
		uniProt.setTaxIDFile(taxIDFile);
		uniProt.setOutUniIDFile(outUniIDFile);
		uniProt.setImpgene_associationgoa_uniprotFile(impgene_associationgoa_uniprotFile);
		uniProt.update();
		
		
		
	}
	private static void updateEnsembl()
	{
		String ensemblFileMouse = "/media/winE/Bioinformatics/DataBase/Mus_musculus.NCBIM37.65.gtf"; 
		String ucscGffFileMouse = "/media/winE/Bioinformatics/GenomeData/mouse/ucsc_mm9/refseqSortUsing.txt";
		int taxIDMouse = 10090;
		Ensembl ensembl = new Ensembl();
//		ensembl.setEnsemblFile(ensemblFileMouse, ucscGffFileMouse, taxIDMouse);
		
		String ensemblFileChicken = "/media/winE/Bioinformatics/GenomeData/checken/GeneLoc/Gallus_gallus.WASHUC2.65.gtf";
		String ucscGffFileChicken = "/media/winE/Bioinformatics/GenomeData/checken/GeneLoc/chicken_Refseq_UCSCGFF";
		int taxIDChicken = 9031;
		ensembl.setEnsemblFile(ensemblFileChicken, ucscGffFileChicken, taxIDChicken);
		ensembl.update();
	}
	private static void updateRiceID()
	{
		String riceParentPath = "/media/winE/Bioinformatics/DataBase/Rice/";
		String gffRapDB = riceParentPath + "RAP_genes.gff3";
		String gffTIGR =  riceParentPath + "Tigr_all.gff3";
		String rap2MSU =  riceParentPath + "RAP-MSU.txt";
		String rapDBoutID = FileOperate.changeFileSuffix(gffRapDB, "_IDout", "txt");
		String tigrDBoutID = FileOperate.changeFileSuffix(gffTIGR, "_IDout", "txt");
		RiceID riceID = new RiceID();
		riceID.setGffRapDB(gffRapDB);
		riceID.setGffTIGR(gffTIGR);
		riceID.setRapDBoutID(rapDBoutID);
		riceID.setRiceRap2MSU(rap2MSU);
		riceID.setTigrDBoutID(tigrDBoutID);
		riceID.update();
	}
	
	private static void updateBlast()
	{
		String blastFile = "/media/winE/Bioinformatics/BLAST/result/chicken/ensemblNr2HumAA";
		String outFIle = "/media/winE/Bioinformatics/BLAST/result/chicken/ensemblNr2HumAA_out";
		int blastTaxID = 0;
		Blast blast = new Blast();
		blast.setAccIDIsGeneID(false);
		blast.setBlastIDisGeneID(true);
		blast.setQueryDBinfo(NovelBioConst.DBINFO_ENSEMBL);
		blast.setBlastDBinfo(NovelBioConst.DBINFO_NCBI_ACC_REFSEQ);
		blast.setSubTaxID(9606);
		blast.setTaxID(blastTaxID);
		blast.setTxtWriteExcep(outFIle);
		blast.updateFile(blastFile, false);
	}
}
