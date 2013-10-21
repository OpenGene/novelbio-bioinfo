package com.novelbio.database.model.modgeneid;

import java.util.HashMap;

public enum GeneType {
	 mRNA,
	 PSEU, 
	 mRNA_TE,
	 miRNA,  
	 Precursor_RNA, 
	 Precursor_miRNA,
	 tRNA, 
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
			mapMRNA2GeneType.put("mRNA".toLowerCase(),mRNA);
			mapMRNA2GeneType.put("miRNA".toLowerCase(),miRNA);
			mapMRNA2GeneType.put("tRNA".toLowerCase(),tRNA);
			mapMRNA2GeneType.put("rRNA".toLowerCase(), rRNA);
			mapMRNA2GeneType.put("snoRNA".toLowerCase(), snoRNA);
			mapMRNA2GeneType.put("snRNA".toLowerCase(), snRNA);			
			mapMRNA2GeneType.put("ncRNA".toLowerCase(), ncRNA);
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
			
			//TODO
			mapMRNA2GeneType.put("transcript".toLowerCase(),miscRNA);
			
			mapMRNA2GeneType.put("miscRNA".toLowerCase(),miscRNA);
			mapMRNA2GeneType.put("misc_RNA".toLowerCase(),miscRNA);
			
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
