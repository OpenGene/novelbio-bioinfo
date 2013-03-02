package com.novelbio.database.model.modgeneid;

import java.util.HashMap;

import org.springframework.context.expression.MapAccessor;

import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;

public enum GeneType {
	 mRNA, miRNA, PSEU, mRNA_TE,
	 tRNA, snoRNA, snRNA,
	 rRNA, ncRNA, miscRNA;
	 
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
			
			mapMRNA2GeneType.put("pseudogene".toLowerCase(),PSEU);
			mapMRNA2GeneType.put("pseudogenic_transcript".toLowerCase(), PSEU);
			
			mapMRNA2GeneType.put("transcript".toLowerCase(),miscRNA);
			mapMRNA2GeneType.put("miscRNA".toLowerCase(),miscRNA);
			
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
