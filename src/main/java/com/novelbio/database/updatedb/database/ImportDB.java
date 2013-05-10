package com.novelbio.database.updatedb.database;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.ls.LSResourceResolver;

import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffType;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.DBAccIDSource;
import com.novelbio.database.domain.geneanno.AgeneUniID;
import com.novelbio.database.domain.geneanno.DBInfo;
import com.novelbio.database.domain.geneanno.Go2Term;
import com.novelbio.database.domain.geneanno.NCBIID;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.model.species.Species;
import com.novelbio.database.service.servgeneanno.ManageDBInfo;
import com.novelbio.database.service.servgeneanno.ManageGo2Term;
import com.novelbio.database.service.servgeneanno.ManageNCBIUniID;

/**
 * 
 * 将数据库
 * @author zong0jie
 *
 */
public class ImportDB {
	String databasePath2 = "/media/winE/Bioinformatics/DataBaseUpdate/";
	
	String softToolsFile;
	String speciesFile;
	
	/** NCBI等数据库文件下载后存放的路径 */
	String downloadPath = "/media/winE/NBCplatform/genome/";
	
	String taxIDFile = downloadPath + "常见物种IDKEGGAll.txt";
	String GOPath = "/media/winE/Bioinformatics/DataBaseUpdate/";
	
	public static void main(String[] args) {
		String downloadPath = "/media/winE/Bioinformatics/DataBaseUpdate/";
		String softToolsFile = "/media/winE/NBCplatform/BioInfomaticsToolsPlatform/SoftwareInfo.txt";
		String speciesFile = "/media/winE/NBCplatform/genome/SpeciesFileThis.txt";
		String dbInfo = "/media/winE/NBCplatform/DBinfo.txt";
		ImportDB importDB = new ImportDB();
		importDB.setDownloadPath(downloadPath);
		importDB.setSoftToolsFile(softToolsFile);
		importDB.setSpeciesFile(speciesFile);
//		importDB.updateDBinfo(dbInfo);
//		importDB.updateSoftInfo();
//		importDB.updateSpecies();
//		importDB.updateGODB();
		
//		importDB.updateNCBIID();
//		importDB.updateUniprotID();
//		importDB.updateZeaMaize();
		importDB.updateRiceID("/media/winE/Bioinformatics/DataBase/Rice/");//只导了前两个
//		importDB.updateTAIR("/media/winE/Bioinformatics/GenomeData/Arabidopsis/tair10DB/");
//		importDB.updateZB();
//		updateEnsembl();
//		importDB.updateYeast();
//		importDB.updateMicroarray();

//		updateSoyBean();
//		updateZeaMaize();
//		updateBlast();
//		updateAffy();
	}
	
	
	public void setSoftToolsFile(String softToolsFile) {
		this.softToolsFile = softToolsFile;
	}
	public void setTaxIDFile(String taxIDFile) {
		this.taxIDFile = taxIDFile;
	}
	public void setSpeciesFile(String speciesFile) {
		this.speciesFile = speciesFile;
	}
	/** NCBI等数据库文件下载后存放的路径 */
	public void setDownloadPath(String databasePath) {
		this.downloadPath = databasePath;
	}
	
	private void updateSoftInfo() {
		SoftWareInfo.updateInfo(softToolsFile);
	}
	
	private void updateGODB() {
		AmiGO amiGO = new AmiGO();
		amiGO.setGoExtObo(GOPath + "gene_ontology_ext.obo");
		amiGO.setTaxIDfile(taxIDFile);
		amiGO.importFile();
	}
	
	private void updateSpecies() {
		Species species = new Species();
		species.setUpdateSpeciesFile(speciesFile);
		species.setUpdateTaxInfo(taxIDFile);
		species.update();
	}
	
	private void updateDBinfo(String dbInfoFile) {
		DBInfo.updateDBinfo(dbInfoFile);
	}
	
	/** 升级从NCBI下载的信息 */
	private void updateNCBIID() {
		String gene2Acc = downloadPath + "gene2accession.gz";
		String gene2Ref = downloadPath + "gene2refseq.gz";
		String gene2ensembl = downloadPath + "gene2ensembl.gz";
		String geneRef2UniID = downloadPath + "gene_refseq_uniprotkb_collab.gz";
		String gene2Info = downloadPath + "gene_info.gz";
		String gene2Pub = downloadPath + "gene2pubmed.gz";
		String goExtObo = GOPath + "gene_ontology_ext.obo";
		String gene2GO = downloadPath + "gene2go.gz";
		
		NCBI ncbi = new NCBI();
		ncbi.setTaxID(taxIDFile);
		ncbi.setGene2AccFile(gene2Acc, gene2Ref);
		ncbi.setGene2Ensembl(gene2ensembl);
		ncbi.setGene2Info(gene2Info);
		ncbi.setGene2Pub(gene2Pub);
		ncbi.setGeneRef2UniID(geneRef2UniID);
		ncbi.setGene2GO(gene2GO);
		ncbi.importFile();
	}
	
	/**
	 * 升级从UniProt下载的信息
	 */
	private void updateUniprotID() {
		String idmappingSelectedFile = downloadPath + "idmapping_selected.tab.gz";
		String impgene_associationgoa_uniprotFile = GOPath + "gene_association.goa_uniprot.gz";
		String outUniIDFile = downloadPath + "outIdmap.txt";
		UniProt uniProt = new UniProt();
		uniProt.setIdmappingSelectedFile(idmappingSelectedFile);
		uniProt.setTaxIDFile(taxIDFile);
		uniProt.setOutUniIDFile(outUniIDFile);
		uniProt.setImpgene_associationgoa_uniprotFile(impgene_associationgoa_uniprotFile);
		uniProt.update();
	}
	
	private void updateEnsembl() {
		Species species;
		species.getGffFile();
//		String ensemblFileMouse = "/media/winE/Bioinformatics/DataBase/Mus_musculus.NCBIM37.65.gtf"; 
//		String ucscGffFileMouse = "/media/winE/Bioinformatics/GenomeData/mouse/ucsc_mm9/refseqSortUsing.txt";
//		int taxIDMouse = 10090;
		IDconvertEnsembl2NCBI ensembl = new IDconvertEnsembl2NCBI();
//		ensembl.setEnsemblFile(ensemblFileMouse, ucscGffFileMouse, taxIDMouse);
		
//		String ensemblFileChicken = "/media/winE/Bioinformatics/GenomeData/checken/GeneLoc/Gallus_gallus.WASHUC2.65.gtf";
//		String ucscGffFileChicken = "/media/winE/Bioinformatics/GenomeData/checken/GeneLoc/chicken_Refseq_UCSCGFF";
//		ensembl.setEnsemblFile(ensemblFileChicken, ucscGffFileChicken, taxIDChicken);
//		ensembl.update();
		
//		String ensemblFileCow = "/media/winE/Bioinformatics/GenomeData/Cow/cow_bta6_Ensembl.GTF";
//		String ucscFileCow = "/media/winE/Bioinformatics/GenomeData/Cow/cow_bta6_UCSC";
//		int taxIDCow = 9913;
//		ensembl.setEnsemblFile(ensemblFileCow, ucscFileCow, taxIDCow);
//		ensembl.update();
		
		String ensemblFilePig = "/media/winE/Bioinformatics/GenomeData/pig/gff/Sus_scrofa.Sscrofa10.2.67.gtf";
		String ncbiFilePig = "/media/winE/Bioinformatics/GenomeData/pig/gff/ref_Sscrofa10.2_top_level_modify.gff3";
		int taxIDpig = 9823;
		ensembl.setEnsemblFile(ensemblFilePig, ncbiFilePig, taxIDpig);
		ensembl.update();
	}
	
	private void updateRiceID(String riceParentPath) {
		String gffRapDB = riceParentPath + "RAP_genes.gff3";
		String gffTIGR =  riceParentPath + "Tigr_all.gff3";
		String rap2MSU =  riceParentPath + "RAP-MSU.txt";
		String goFile = riceParentPath + "all.GOSlim_assignment";
		String rapDBoutID = FileOperate.changeFileSuffix(gffRapDB, "_IDout", "txt");
		String tigrDBoutID = FileOperate.changeFileSuffix(gffTIGR, "_IDout", "txt");
		RiceID riceID = new RiceID();
		riceID.setGffRapDB(gffRapDB);
		riceID.setGffTIGR(gffTIGR);
		riceID.setRapDBoutID(rapDBoutID);
		riceID.setRiceRap2MSU(rap2MSU);
		riceID.setTigrDBoutID(tigrDBoutID);
		riceID.setTigrGoSlim(goFile);
		riceID.update();
	}
	
	private void updateBlast() {
		String blastFile = "/media/winE/Bioinformatics/BLAST/result/chicken/ensemblNr2HumAA";
		String outFIle = "/media/winE/Bioinformatics/BLAST/result/chicken/ensemblNr2HumAA_out";
		int queryTaxID = 0;
		BlastUp2DB blast = new BlastUp2DB();
		blast.setQueryID(GeneID.IDTYPE_ACCID);
		blast.setBlastID(GeneID.IDTYPE_GENEID);
		blast.setQueryDBinfo(NovelBioConst.DBINFO_ENSEMBL);
		blast.setBlastDBinfo(NovelBioConst.DBINFO_NCBI_ACC_REFSEQ);
		blast.setSubTaxID(9606);
		blast.setTaxID(queryTaxID);
		blast.setTxtWriteExcep(outFIle);
//		blast.updateFile(blastFile, false);
		/////////////////  arabidopsis   //////////////////////////////////
		blastFile = "/media/winE/Bioinformatics/BLAST/result/rice/tigrrice2tairath";
		outFIle = FileOperate.changeFileSuffix(blastFile, "_out", null);
		queryTaxID = 39947;
		blast = new BlastUp2DB();
		blast.setQueryID(GeneID.IDTYPE_ACCID);
		blast.setBlastID(GeneID.IDTYPE_ACCID);
		blast.setQueryDBinfo(NovelBioConst.DBINFO_RICE_TIGR);
		blast.setBlastDBinfo(NovelBioConst.DBINFO_ATH_TAIR);
		blast.setSubTaxID(3702);
		blast.setTaxID(queryTaxID);
		blast.setTxtWriteExcep(outFIle);
//		blast.updateFile(blastFile, false);
		/////////////////   zebrafish   /////////////////////////
		blastFile = "/media/winE/Bioinformatics/BLAST/result/zebrafish/dre_nr2hsa_refseq";
		outFIle = FileOperate.changeFileSuffix(blastFile, "_out", null);
		queryTaxID = 7955;
		blast = new BlastUp2DB();
		blast.setQueryID(GeneID.IDTYPE_GENEID);
		blast.setBlastID(GeneID.IDTYPE_GENEID);
		blast.setQueryDBinfo(NovelBioConst.DBINFO_NCBI);
		blast.setBlastDBinfo(NovelBioConst.DBINFO_NCBI_ACC_REFSEQ);
		blast.setSubTaxID(9606);
		blast.setTaxID(queryTaxID);
		blast.setTxtWriteExcep(outFIle);
//		blast.updateFile(blastFile, false);		
		/////////////////   pichia   /////////////////////////
		blastFile = "/media/winE/Bioinformatics/BLAST/result/Pichia/pichia2SSC.txt";
		outFIle = FileOperate.changeFileSuffix(blastFile, "_out", null);
		queryTaxID = 4922;
		blast = new BlastUp2DB();
		blast.setQueryID(GeneID.IDTYPE_ACCID);
		blast.setBlastID(GeneID.IDTYPE_ACCID);
		blast.setQueryDBinfo(NovelBioConst.DBINFO_PPA_ID);
		blast.setBlastDBinfo(NovelBioConst.DBINFO_SSC_ID);
		blast.setSubTaxID(4932);
		blast.setTaxID(queryTaxID);
		blast.setTxtWriteExcep(outFIle);
//		blast.updateFile(blastFile, false);
		/////////////////   zeamaize   /////////////////////////
		blastFile = "/media/winE/Bioinformatics/BLAST/result/maize/maize2AthFinal5b.txt";
		outFIle = FileOperate.changeFileSuffix(blastFile, "_out", null);
		queryTaxID = 4577;
		blast = new BlastUp2DB();
		blast.setQueryID(GeneID.IDTYPE_ACCID);
		blast.setBlastID(GeneID.IDTYPE_ACCID);
		blast.setQueryDBinfo(NovelBioConst.DBINFO_MAIZE_MGDB);
		blast.setBlastDBinfo(NovelBioConst.DBINFO_ATH_TAIR);
		blast.setSubTaxID(3702);
		blast.setTaxID(queryTaxID);
		blast.setTxtWriteExcep(outFIle);
//		blast.updateFile(blastFile, false);
		/////////////////   soybean   /////////////////////////
		blastFile = "/media/winE/Bioinformatics/BLAST/result/soybean/soybean2Ath.xls";
		outFIle = FileOperate.changeFileSuffix(blastFile, "_out", null);
		queryTaxID = 3847;
		blast = new BlastUp2DB();
		blast.setQueryID(GeneID.IDTYPE_ACCID);
		blast.setBlastID(GeneID.IDTYPE_ACCID);
		blast.setQueryDBinfo(NovelBioConst.DBINFO_GLYMAX_SOYBASE);
		blast.setBlastDBinfo(NovelBioConst.DBINFO_ATH_TAIR);
		blast.setSubTaxID(3702);
		blast.setTaxID(queryTaxID);
		blast.setTxtWriteExcep(outFIle);
//		blast.updateFile(blastFile, false);
		
		/////////////////   pig   /////////////////////////
		blastFile = "/media/winE/Bioinformatics/GenomeData/pig/ncbiRef2Human";
		outFIle = FileOperate.changeFileSuffix(blastFile, "_out", null);
		queryTaxID = 9823;
		blast = new BlastUp2DB();
		blast.setQueryID(GeneID.IDTYPE_ACCID);
		blast.setBlastID(GeneID.IDTYPE_ACCID);
		blast.setQueryDBinfo(NovelBioConst.DBINFO_NCBI);
		blast.setBlastDBinfo(NovelBioConst.DBINFO_NCBI);
		blast.setSubTaxID(9606);
		blast.setTaxID(queryTaxID);
		blast.setTxtWriteExcep(outFIle);
		blast.setIDisBlastType(true);
//		blast.updateFile(blastFile, false);
		
		/////////////////   cow   /////////////////////////
		blastFile = "/media/winE/Bioinformatics/BLAST/result/cow/cowRefPro2humRefPro";
		outFIle = FileOperate.changeFileSuffix(blastFile, "_out", null);
		queryTaxID = 9913;
		blast = new BlastUp2DB();
		blast.setQueryID(GeneID.IDTYPE_GENEID);
		blast.setBlastID(GeneID.IDTYPE_GENEID);
		blast.setQueryDBinfo(NovelBioConst.DBINFO_NCBI);
		blast.setBlastDBinfo(NovelBioConst.DBINFO_NCBI);
		blast.setSubTaxID(9606);
		blast.setTaxID(queryTaxID);
		blast.setTxtWriteExcep(outFIle);
		blast.setIDisBlastType(true);
//		blast.updateFile(blastFile, false);
		
		////////////////////  Maize ////////////////////////////////////
		blastFile = "/media/winE/NBCplatform/genome/maize/blast/zmb73_p_To_athTair10_p";
		outFIle = FileOperate.changeFileSuffix(blastFile, "_out", null);
		queryTaxID = 4577;
		blast = new BlastUp2DB();
		blast.setQueryID(GeneID.IDTYPE_ACCID);
		blast.setBlastID(GeneID.IDTYPE_ACCID);
		blast.setQueryDBinfo(NovelBioConst.DBINFO_MAIZE_MGDB);
		blast.setBlastDBinfo(NovelBioConst.DBINFO_ATH_TAIR);
		blast.setSubTaxID(3702);
		blast.setTaxID(queryTaxID);
		blast.setTxtWriteExcep(outFIle);
		blast.setIDisBlastType(false);
		blast.updateFile(blastFile);
	}
	
	private void updateAffy() {
		String affyFile = "";
		String outFile = "";
		int taxID = 0;
		NormAffy normAffy = null;
		/////////   arabidopsis  //////////////////////////////////////////////;
//		affyFile = "/media/winE/Bioinformatics/Affymetrix/Arabidopsis/ATH1-121501.na31.annot.csv/ATH1-121501.na31.annot_modify.csv";
//		outFile = FileOperate.changeFileSuffix(affyFile, "_Out", "txt");
//		taxID = 3702;
//		normAffy = new NormAffy();
//		normAffy.setTaxID(taxID);
//		normAffy.setTxtWriteExcep(outFile);
//		normAffy.setDbInfo(NovelBioConst.DBINFO_AFFY_ATH);
//		normAffy.updateFile(affyFile, false);
		/////////   human  //////////////////////////////////////////////;
//		affyFile = "/media/winE/Bioinformatics/Affymetrix/Human/Human Genome U133 Plus 2.0/HG-U133_Plus_2.na31.annot.csv/HG-U133_Plus_2.na31.annot.csv";
//		outFile = FileOperate.changeFileSuffix(affyFile, "_Out", "txt");
//		taxID = 9606;
//		normAffy = new NormAffy();
//		normAffy.setTaxID(taxID);
//		normAffy.setTxtWriteExcep(outFile);
//		normAffy.setDbInfo(NovelBioConst.DBINFO_AFFY_HUMAN_U133_PLUS2);
//		normAffy.updateFile(affyFile, false);

		/////////   Zebrafish  //////////////////////////////////////////////;
//		affyFile = "/media/winE/Bioinformatics/Affymetrix/";
//		outFile = FileOperate.changeFileSuffix(affyFile, "_Out", "txt");
//		taxID = 0
//		normAffy = new NormAffy();
//		normAffy.setTaxID(taxID);
//		normAffy.setTxtWriteExcep(outFile);
//		normAffy.setDbInfo(NovelBioConst.DBINFO_AFFY_ZEBRAFISH);
//		normAffy.updateFile(affyFile, false);

		/////////   soybean  //////////////////////////////////////////////;
//		affyFile = "/media/winE/Bioinformatics/Affymetrix/";
//		outFile = FileOperate.changeFileSuffix(affyFile, "_Out", "txt");
//		taxID = 0
//		normAffy = new NormAffy();
//		normAffy.setTaxID(taxID);
//		normAffy.setTxtWriteExcep(outFile);
//		normAffy.updateFile(affyFile, false);

		/////////   rice  //////////////////////////////////////////////;
		affyFile = "/media/winE/Bioinformatics/Affymetrix/rice/Rice.na31.annot.csv/Rice.na31.annot.csv";
		outFile = FileOperate.changeFileSuffix(affyFile, "_Out", "txt");
		taxID = 39947;
		normAffy = new NormAffy();
		normAffy.setTaxID(taxID);
		normAffy.setTxtWriteExcep(outFile);
		normAffy.setDataBaseInfo(NovelBioConst.DBINFO_AFFY_RICE_31);
		normAffy.updateFile(affyFile);

		/////////   Pig Porcine  //////////////////////////////////////////////;
//		affyFile = "";
//		outFile = FileOperate.changeFileSuffix(affyFile, "_Out", "txt");
//		taxID = 0;
//		normAffy = new NormAffy();
//		normAffy.setTaxID(taxID);
//		normAffy.setTxtWriteExcep(outFile);
//		normAffy.setDbInfo(NovelBioConst.DBINFO_AFFY_PIG);
//		normAffy.updateFile(affyFile, false);

		/////////   Mouse  //////////////////////////////////////////////;
//		affyFile = "/media/winE/Bioinformatics/Affymetrix/Mouse/Mouse Genome 430 2.0 Array/Mouse430_2.na31.annot.csvTT/Mouse430_2.na31.annot.csv";
//		outFile = FileOperate.changeFileSuffix(affyFile, "_Out", "txt");
//		taxID = 10090;
//		normAffy = new NormAffy();
//		normAffy.setTaxID(taxID);
//		normAffy.setTxtWriteExcep(outFile);
//		normAffy.setDbInfo(NovelBioConst.DBINFO_AFFY_MOUSE_430_2);
//		normAffy.updateFile(affyFile, false);
		
		/////////   Rat  //////////////////////////////////////////////;
//		affyFile = "/media/winE/Bioinformatics/Affymetrix/Mouse/Mouse Genome 430 2.0 Array/Mouse430_2.na31.annot.csvTT/Mouse430_2.na31.annot.csv";
//		outFile = FileOperate.changeFileSuffix(affyFile, "_Out", "txt");
//		taxID = 10090;
//		normAffy = new NormAffy();
//		normAffy.setTaxID(taxID);
//		normAffy.setTxtWriteExcep(outFile);
//		normAffy.setDbInfo(NovelBioConst.DBINFO_AFFY_MOUSE_430_2);
//		normAffy.updateFile(affyFile, false);

		/////////   Bovine  //////////////////////////////////////////////;
//		affyFile = "/media/winE/Bioinformatics/Affymetrix/";
//		outFile = FileOperate.changeFileSuffix(affyFile, "_Out", "txt");
//		taxID = 0;
//		normAffy = new NormAffy();
//		normAffy.setTaxID(taxID);
//		normAffy.setTxtWriteExcep(outFile);
//		normAffy.setDbInfo(NovelBioConst.DBINFO_AFFY_COW);
//		normAffy.updateFile(affyFile, false);
		affyFile = "/media/winE/Bioinformatics/Affymetrix/rat/Rat230_2.na32.annot.csv/Rat230_2.na32.annot.csv";
		outFile = FileOperate.changeFileSuffix(affyFile, "_Out", "txt");
		taxID = 10116;
		normAffy = new NormAffy();
		normAffy.setTaxID(taxID);
		normAffy.setTxtWriteExcep(outFile);
		normAffy.setDataBaseInfo(NovelBioConst.DBINFO_AFFY_MOUSE_430_2);
		normAffy.updateFile(affyFile);
	}
	
	private void updateTAIR(String parentPath) {
		Arabidopsis arabidopsis = new Arabidopsis();
		String athGO = parentPath + "ATH_GO_GOSLIM.txt/ATH_GO_GOSLIM2.txt";
		String tAIR_functional_descriptions = parentPath + "TAIR10_functional_descriptions";
		String tAIRModelcDNAAssociations = parentPath + "idconvert/TAIR10_Model_cDNA_associations";
		String tAIRNCBIGeneIDmapping = parentPath + "TAIR10_NCBI_GENEID_mapping";
		String tAIRNCBIRefSeqMappingPROT = parentPath + "TAIR10_NCBI_REFSEQ_mapping_PROT";
		String tAIRNCBIRefSeqMappingRNA = parentPath + "TAIR10_NCBI_REFSEQ_mapping_RNA";
		String uniprot2agi = parentPath + "idconvert/Uniprot2AGI";
		arabidopsis.setAthGO(athGO);
		arabidopsis.setTAIR_functional_descriptions(tAIR_functional_descriptions);
		arabidopsis.setTAIRModelcDNAAssociations(tAIRModelcDNAAssociations);
		arabidopsis.setTAIRNCBIGeneIDmapping(tAIRNCBIGeneIDmapping);
		arabidopsis.setTAIRNCBIRefSeqMappingPROT(tAIRNCBIRefSeqMappingPROT);
		arabidopsis.setTAIRNCBIRefSeqMappingRNA(tAIRNCBIRefSeqMappingRNA);
		arabidopsis.setUniprot2AGI(uniprot2agi);
		arabidopsis.update();
	}
	
	private void updateZB() {
		String zbEnsembl = "/media/winE/Bioinformatics/GenomeData/danio_rerio/ensembl_1_to_1.txt";
		String zbGeneIDFile = "/media/winE/Bioinformatics/GenomeData/danio_rerio/gene2geneID.txt";
		String zbGOFile = "/media/winE/Bioinformatics/GenomeData/danio_rerio/gene_association.zfin";
		String zbRefSeqFile = "/media/winE/Bioinformatics/GenomeData/danio_rerio/gene2refseq.txt";
		ZebraFish zebraFish = new ZebraFish();
		zebraFish.setZbEnsembl(zbEnsembl);
		zebraFish.setZbGeneIDFile(zbGeneIDFile);
		zebraFish.setZbGOFile(zbGOFile);
		zebraFish.setZbRefSeqFile(zbRefSeqFile);
		zebraFish.update();
	}
	
	private void updateMicroarray() {
		String zerbfishFile = "/media/winE/Bioinformatics/Affymetrix/rice/Affy2Loc.txt";
		String zerbfishFile2 = "/media/winE/Bioinformatics/BLAST/result/zebrafish/affy2zerbfish_coped.xls";
		MicroArrayBlast microArrayBlast = null;
		int taxID = 0;
		////////////////////  斑马鱼  /////////////////////////////
		zerbfishFile = "/media/winE/Bioinformatics/BLAST/result/zebrafish/affy2zerbfishRefSeq.xls";
		zerbfishFile2 = "/media/winE/Bioinformatics/BLAST/result/zebrafish/affy2zerbfish_coped.xls";
		microArrayBlast = new MicroArrayBlast();
		microArrayBlast.setDataBaseInfo(NovelBioConst.DBINFO_AFFY_ZEBRAFISH);
		microArrayBlast.setGeneID(GeneID.IDTYPE_ACCID);
//		microArrayBlast.updateFile(zerbfishFile, false);
//		microArrayBlast.updateFile(zerbfishFile2, false);
		////////////////////  水稻  /////////////////////////////
		zerbfishFile = "/media/winE/Bioinformatics/Affymetrix/rice/Affy2Loc.txt";
		microArrayBlast = new MicroArrayBlast();
		microArrayBlast.setDataBaseInfo(NovelBioConst.DBINFO_AFFY_RICE_31);
//		microArrayBlast.setGeneID(CopedID.IDTYPE_ACCID);
//		microArrayBlast.updateFile(zerbfishFile, false);
		////////////////////  大豆  /////////////////////////////
		zerbfishFile = "/media/winE/Bioinformatics/Affymetrix/soybean/affy2Gly_nr.txt";
		microArrayBlast = new MicroArrayBlast();
		microArrayBlast.setDataBaseInfo(NovelBioConst.DBINFO_AFFY_GLMAX);
		microArrayBlast.setGeneID(GeneID.IDTYPE_ACCID);
		microArrayBlast.setTaxID(3847);
		microArrayBlast.updateFile(zerbfishFile, false);
	}
	
	private void updateYeast() {
		String path = "/media/winE/NBCplatform/genome/yeast/db/";
		String yeastDBxrefFile = path + "dbxref.tab";
		String SGD_featuresFile = path + "SGD_features.tab";
		String Gene_AssociationFile = path + "gene_association.sgd.gz";
//		String Pipas_FunFile = "/media/winE/Bioinformatics/GenomeData/yeast/Pichia/pipas_function-1009.txt";
//		String Pipas_GO_SlimFile = "/media/winE/Bioinformatics/GenomeData/yeast/Pichia/Pichia_GS115.GO_0509.gz";
//		String ppa_ncbi_geneidFile = "/media/winE/Bioinformatics/GenomeData/yeast/Pichia/ppa_ncbi-geneid.list";
		Yeast yeast = new Yeast();
		yeast.setGene_AssociationFile(Gene_AssociationFile);
//		yeast.setPipas_Fun(Pipas_FunFile);
//		yeast.setPipas_GO_Slim(Pipas_GO_SlimFile);
		yeast.setSGD_featuresFile(SGD_featuresFile);
		yeast.setYeastDBxrefFile(yeastDBxrefFile);
//		yeast.setPpa_ncbi_geneidFile(ppa_ncbi_geneidFile);
		yeast.update();
	}

	private void updateSoyBean() {
		String soyDbxref = "/media/winE/Bioinformatics/GenomeData/soybean/dbxref";
		String soyAnno = "/media/winE/Bioinformatics/GenomeData/soybean/Gmax_109_annotation_info.txt";
		SoyBean soyBean = new SoyBean();
		soyBean.setSoyDbxref(soyDbxref);
		soyBean.setSoyGeneInfo(soyAnno);
		soyBean.update();
	}
	
	private void updateZeaMaize() {
		String zeamaizeDbxref = "/media/winE/Bioinformatics/GenomeData/maize/ZmB73_5a_xref.txt";
		String maizeGeneInfo =
				"/media/winE/Bioinformatics/GenomeData/maize/ZmB73_5a_gene_descriptors.txt/ZmB73_5a_gene_descriptors.txt";
		MaizeGDB maizeGDB = new MaizeGDB();
		maizeGDB.setMaizeDbxref(zeamaizeDbxref);
		maizeGDB.setMaizeGeneInfo(maizeGeneInfo);
		maizeGDB.update();
	}

}
