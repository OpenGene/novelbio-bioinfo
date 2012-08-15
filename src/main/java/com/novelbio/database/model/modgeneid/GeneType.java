package com.novelbio.database.model.modgeneid;

import java.util.HashMap;

import org.springframework.context.expression.MapAccessor;

import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;

public enum GeneType {
	 mRNA, miRNA, PSEU_TRANSCRIPT, mRNA_TE,
	 tRNA, snoRNA, snRNA,
	 rRNA, ncRNA, miscRNA;
	 
	static HashMap<String, GeneType> mapMRNA2GeneType = new HashMap<String, GeneType>();
	/**
	 * 设定mRNA和gene的类似名，在gff文件里面出现的
	 */
	private static void setMapName2GeneType() {
		if (mapMRNA2GeneType.isEmpty()) {
			mapMRNA2GeneType.put("mRNA_TE_gene",mRNA_TE);
			mapMRNA2GeneType.put("mRNA",mRNA);
			mapMRNA2GeneType.put("miRNA",miRNA);
			mapMRNA2GeneType.put("tRNA",GeneType.tRNA);
			mapMRNA2GeneType.put("pseudogenic_transcript", PSEU_TRANSCRIPT);
			mapMRNA2GeneType.put("snoRNA", snoRNA);
			mapMRNA2GeneType.put("snRNA", snRNA);
			mapMRNA2GeneType.put("rRNA", rRNA);
			mapMRNA2GeneType.put("ncRNA", ncRNA);
			mapMRNA2GeneType.put("transcript",miscRNA);
			mapMRNA2GeneType.put("miscRNA",miscRNA);
		}
	}
	 
}
