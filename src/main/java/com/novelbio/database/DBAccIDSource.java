package com.novelbio.database;

/**
 * NCBI和UniProt表的ID的来源
 * @author zong0jie
 *
 */
public enum DBAccIDSource {
	//////////公共数据库//////////////////////////
	/** 来自NCBI，不代表来自RefSeq、RefProtein等 */
	NCBI("NCBI"),
	RefSeqPro("RefSeq_Protein"), RefSeqRNA("RefSeq_RNA"), RefSeqDNA("RefSeq_DNA"),
	ProteinGI("ProteinGI"), ProteinAC("ProteinAC"), RNAAC("rnaAC"),
	GeneAC("GeneAC"),
	
	/** 来自UniProt，不代表来自UniProt的子数据库 */
	Uniprot("UniProt"),
	UniprotKB_ID("UniprotKB_ID"), UniprotPARC("UniProt_PARX"), UniprotUniGene("UniProt_UniGene"),
	
	EMBL("EMBL"),
	EMBL_CDS("EMBL_CDS"),
	Ensembl("Ensembl"),
	Ensembl_TRS("Ensembl_TRS"), Ensembl_RNA("Ensembl_RNA"), Ensembl_Pro("Ensembl_Pro"), Ensembl_Gene("Ensembl_Gene"),
	KEGG("KEGG"),
	
	PIR("PIR"),

	IPI("IPI"),
	
	//////////专业数据库/////////////////
	PlantGDB("PlantGDB"),
	/** 毕赤酵母的PPA数据库 */
	PPA_PichiaID("PPAPichia"),
	SSC_ScerID("SSCScer"),
	TAIR_ATH("TairAth"),
	ZFIN_DRE("ZFINdre"),
	SOYBASE("SoyBase"),
	MaizeGDB("MaizeGDB"),
	TIGR_rice("TigrRice"),
	RapDB_rice("RapDBrice"),
	IRGSP_rice("IRGSPrice"),
	NIAS_FLcDNA("NIAS_FLcDNA"),
	
	////////公司和芯片ID///////////////
	Array_Nemblgen("Nemblgen"),
	Array_Affymetrix("Affymetrix"),
	NovelBio("NovelBio"),
	
	Symbol("Symbol"), Synonyms("Synonyms");
	
	String dbName;
	DBAccIDSource(String dbName) {
		this.dbName = dbName;
	}
	
	@Override
	public String toString() {
		return dbName;
	}
	
}
