package com.novelbio.analysis.diffexpress;
/**
 * 调用DEGseq算法，适用于RPKM的试验，譬如mRNAseq
 * @author zong0jie
 *
 */
public class DiffExpDEGseq {
	library('DEGseq')
	geneExpMatrix1 = readGeneExp('heartKO.genes.results.xls', header=T, sep='\t', geneCol=1, valCol=4)
	geneExpMatrix2 = readGeneExp('heartWT.genes.results.xls', header=T, sep='\t', geneCol=1, valCol=4)
	DEGexp(geneExpMatrix1 = geneExpMatrix1, geneCol1 = 1, expCol1 = 2, groupLabel1 = 'KO', 
	geneExpMatrix2 = geneExpMatrix2, geneCol2 = 1, expCol2 = 2, groupLabel2 = 'WT', method = 'MARS', outputDir='./HeartKOvsWT')

	geneExpMatrix1 = readGeneExp('MEFKO0d.genes.xls', header=T, sep='\t', geneCol=1, valCol=3)
	geneExpMatrix2 = readGeneExp('MEFWT0d.genes.xls', header=T, sep='\t', geneCol=1, valCol=3)
	DEGexp(geneExpMatrix1 = geneExpMatrix1, geneCol1 = 1, expCol1 = 2, groupLabel1 = 'MEFKO0d', 
	geneExpMatrix2 = geneExpMatrix2, geneCol2 = 1, expCol2 = 2, groupLabel2 = 'MEFWT0d', method = 'MARS', outputDir='./MEFKO0dvsWT0d')

	geneExpMatrix1 = readGeneExp('MEFWT0d.genes.xls', header=T, sep='\t', geneCol=1, valCol=3)
	geneExpMatrix2 = readGeneExp('MEFWT2d.genes.xls', header=T, sep='\t', geneCol=1, valCol=3)
	DEGexp(geneExpMatrix1 = geneExpMatrix1, geneCol1 = 1, expCol1 = 2, groupLabel1 = 'MEFWT0d', 
	geneExpMatrix2 = geneExpMatrix2, geneCol2 = 1, expCol2 = 2, groupLabel2 = 'MEFWT2d', method = 'MARS', outputDir='./MEFWT0dvsWT2d')

	geneExpMatrix1 = readGeneExp('MEFKO2d.genes.xls', header=T, sep='\t', geneCol=1, valCol=3)
	geneExpMatrix2 = readGeneExp('MEFWT2d.genes.xls', header=T, sep='\t', geneCol=1, valCol=3)
	DEGexp(geneExpMatrix1 = geneExpMatrix1, geneCol1 = 1, expCol1 = 2, groupLabel1 = 'MEFKO2d', 
	geneExpMatrix2 = geneExpMatrix2, geneCol2 = 1, expCol2 = 2, groupLabel2 = 'MEFWT2d', method = 'MARS', outputDir='./MEFKO2dvsWT2d')

}
