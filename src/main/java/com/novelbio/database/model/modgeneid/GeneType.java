package com.novelbio.database.model.modgeneid;

import java.util.HashMap;

/**
 * 来自ensembl
 * http://asia.ensembl.org/Help/Glossary?id=275
 * 
 * A gene or transcript classification. Transcript types include protein coding, pseudogene, and long non-coding and short non-coding RNAs. For human, mouse and selected other species we incorporate manual annotation from Havana. Where a gene or transcript has been manually annotated, we use the manually assigned biotype. The full list of biotypes used by Havana are here.
 
The biotypes can be grouped into protein coding, pseudogene, long noncoding and short noncoding. Examples of biotypes in each group are as follows:
Protein coding:
IG_C_gene, IG_D_gene, IG_J_gene, IG_LV_gene, IG_M_gene, IG_V_gene, IG_Z_gene, nonsense_mediated_decay, nontranslating_CDS, non_stop_decay, polymorphic_pseudogene, protein_coding, TR_C_gene, TR_D_gene, TR_gene, TR_J_gene, TR_V_gene.
Pseudogene:
disrupted_domain, IG_C_pseudogene, IG_J_pseudogene, IG_pseudogene, IG_V_pseudogene, processed_pseudogene, pseudogene, transcribed_processed_pseudogene, transcribed_unprocessed_pseudogene, translated_processed_pseudogene, translated_unprocessed_pseudogene, TR_J_pseudogene, TR_V_pseudogene, unitary_pseudogene, unprocessed_pseudogene
Long noncoding:
3prime_overlapping_ncrna, ambiguous_orf, antisense, lincRNA, ncrna_host, non_coding, processed_transcript, retained_intron, sense_intronic, sense_overlapping
Short noncoding:
miRNA, miRNA_pseudogene, misc_RNA, misc_RNA_pseudogene, Mt_rRNA, Mt_tRNA, Mt_tRNA_pseudogene, ncRNA, pre_miRNA, RNase_MRP_RNA, RNase_P_RNA, rRNA, rRNA_pseudogene, scRNA_pseudogene, snlRNA, snoRNA, snoRNA_pseudogene, snRNA, snRNA_pseudogene, SRP_RNA, tmRNA,, tRNA, tRNA_pseudogene
Finding biotype groupings

If you see a biotype in Ensembl and are not sure which biotype group it belongs to, you can check this by connecting to the latest ensembl_production database.
For Ensembl release 73, connect to database "ensembl_production_73" eg.
  mysql -uanonymous -P3306 -hensembldb.ensembl.org -Densembl_production_73 -e "select distinct(name),biotype_group from biotype where db_type like '%core%' and is_current=1 order by biotype_group,name;"
  
  
 * @author zong0jie
 * @date 2016年10月17日
 */
public enum GeneType {
	 mRNA,
	 PSEU, 
	 mRNA_TE,
	 miRNA,  
	 Precursor_RNA, 
	 Precursor_miRNA,
	 tRNA,
	 tmRNA,
	 snoRNA, 
	 snRNA,
	 /** small cytoplasmic RNA; any one of several small cytoplasmic RNA molecules present in 
	  * the cytoplasm and (sometimes) nucleus of a eukaryote; */
	 scRNA,
	 rRNA, 
	 /**
	  * 
	  * Miscellaneous RNA或MiscRNA，Misc RNA并不仅指小RNA分子，数据库中也能查到几千个bp的MiscRNA。
	  * MiscRNA含包括编码基因和非编码基因，正如其名Miscellaneous。
	  * 按entrez，是一些尚未进行基因数据分类的RNA，或不确定其编码方式。
	  * <p>
	  * miscrna (misc_rna, miscellaneous RNA) is assigned to any gene that encodes an RNA product not included in the other specifics. 
	  * The genetype other property is applied to loci of known type, but a specific category has not yet been applied in the Entrezgene data model (i.e.named fragile sites). 
	  * The genetype unknown property is applied to probable genes for which the type is still under review. This category is frequently used when the defining sequence has uncertain coding propensity.
	  * http://www.ncbi.nlm.nih.gov/books/NBK3841/
	  */
	 miscRNA,
	 ncRNA, 
	 /** mRNA的反义链上的ncRNA */
	 antisense_RNA, 
	 /** 端粒RNA */
	 telomerase_RNA,
	 /** RNase_P_RNA
	  * RNA component of Ribonuclease P (RNase P), a ubiquitous endoribonuclease.
	  */
	 RNase_P_RNA,
	 
	 /**
	  * RNA molecule essential for the catalytic activity of RNase MRP, an enzymatically
	  *  active ribonucleoprotein with two distinct roles in eukaryotes. In mitochondria 
	  *  it plays a direct role in the initiation of mitochondrial DNA replication, while in 
	  *  the nucleus it is involved in precursor rRNA processing.
	  */
	 RNase_MRP_RNA
	 ;
	 static HashMap<String, GeneType> mapMRNA2GeneType = new HashMap<String, GeneType>();
	/**
	 * 设定mRNA和gene的类似名，在gff文件里面出现的
	 * key为小写
	 */
	private static void setMapName2GeneType() {
		if (mapMRNA2GeneType.isEmpty()) {
			mapMRNA2GeneType.put("transcript", mRNA);
			
			mapMRNA2GeneType.put("nontranslating_CDS".toLowerCase(), mRNA);

			
			mapMRNA2GeneType.put("mRNA".toLowerCase(),mRNA);
			mapMRNA2GeneType.put("protein_coding".toLowerCase(),mRNA);
			mapMRNA2GeneType.put("miRNA".toLowerCase(),miRNA);
			mapMRNA2GeneType.put("tRNA".toLowerCase(),tRNA);
			mapMRNA2GeneType.put("tRNA_pseudogene".toLowerCase(),tRNA);
			mapMRNA2GeneType.put("rRNA".toLowerCase(), rRNA);
			mapMRNA2GeneType.put("snoRNA".toLowerCase(), snoRNA);
			mapMRNA2GeneType.put("snRNA".toLowerCase(), snRNA);			
			mapMRNA2GeneType.put("ncRNA".toLowerCase(), ncRNA);
			mapMRNA2GeneType.put("lncRNA".toLowerCase(), ncRNA);
			mapMRNA2GeneType.put("antisense".toLowerCase(), ncRNA);
			mapMRNA2GeneType.put("lincRNA".toLowerCase(), ncRNA);
			mapMRNA2GeneType.put("ncrna_host".toLowerCase(), ncRNA);
			mapMRNA2GeneType.put("non_coding".toLowerCase(), ncRNA);
			mapMRNA2GeneType.put("processed_transcript".toLowerCase(), ncRNA);
			mapMRNA2GeneType.put("retained_intron".toLowerCase(), ncRNA);
			mapMRNA2GeneType.put("sense_intronic".toLowerCase(), ncRNA);
			mapMRNA2GeneType.put("sense_overlapping".toLowerCase(), ncRNA);
			mapMRNA2GeneType.put("3prime_overlapping_ncrna".toLowerCase(), ncRNA);
			mapMRNA2GeneType.put("ambiguous_orf".toLowerCase(), ncRNA);
			mapMRNA2GeneType.put("SRP_RNA".toLowerCase(), ncRNA);

			
			
			mapMRNA2GeneType.put("primary_transcript".toLowerCase(), ncRNA);
			mapMRNA2GeneType.put("precursor_RNA".toLowerCase(), Precursor_RNA);
			mapMRNA2GeneType.put("Precursor_miRNA".toLowerCase(), Precursor_miRNA);
			mapMRNA2GeneType.put("antisense_RNA".toLowerCase(), antisense_RNA);
			mapMRNA2GeneType.put("telomerase_RNA".toLowerCase(), telomerase_RNA);
			mapMRNA2GeneType.put("RNase_P_RNA".toLowerCase(), RNase_P_RNA);
			mapMRNA2GeneType.put("RNase_MRP_RNA".toLowerCase(), RNase_MRP_RNA);
			mapMRNA2GeneType.put("scRNA".toLowerCase(), scRNA);
			mapMRNA2GeneType.put("pseudogene".toLowerCase(),PSEU);
			mapMRNA2GeneType.put("pseudogenic_transcript".toLowerCase(), PSEU);
			mapMRNA2GeneType.put("tmRNA".toLowerCase(), tmRNA);
			//TODO
			mapMRNA2GeneType.put("transcript".toLowerCase(),miscRNA);
			
			mapMRNA2GeneType.put("miscRNA".toLowerCase(),miscRNA);
			mapMRNA2GeneType.put("misc_RNA".toLowerCase(),miscRNA);
			mapMRNA2GeneType.put("misc_RNA_pseudogene".toLowerCase(),miscRNA);

			mapMRNA2GeneType.put("mRNA_TE_gene".toLowerCase(),mRNA_TE);
			mapMRNA2GeneType.put("transposon_fragment".toLowerCase(),mRNA_TE);
			mapMRNA2GeneType.put("transposable_element_gene".toLowerCase(),mRNA_TE);
		}
	}
	
	/**
	 * 暂时认为mRNA、mRNA_TE、PSEU
	 * 都可以含有UTR
	 * @return
	 */
	public static boolean isMRNA_CanHaveUTR(GeneType geneType) {
		if (geneType == mRNA || geneType == mRNA_TE || geneType == PSEU) {
			return true;
		}
		return false;
	}
	
	/**
	 * key为小写
	 * @return
	 */
	public static HashMap<String, GeneType> getMapMRNA2GeneType() {
		if (mapMRNA2GeneType.size() == 0) {
			setMapName2GeneType();
		}
		return mapMRNA2GeneType;
	}
	/**
	 * @param geneType 输入会自动转化为小写
	 * @return
	 */
	public static GeneType getGeneType(String geneType) {
		return getMapMRNA2GeneType().get(geneType.toLowerCase());
	}
}
