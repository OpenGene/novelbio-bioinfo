package com.novelbio.database.domain.geneanno;

public class GeneMiRNAsub {
	
	/**
	 * 该具体成熟miRNA的accID
	 * FT                   /accession="MIMAT0000002"
	 */
	String accID = "";
	/**
	 * 该miRNA在前体中的起点
	 */
	int start = 0;
	/**
	 * 该miRNA在前体中的终点
	 */
	int end = 0;
	/**
	 * FT                   /product="cel-lin-4-5p"
	 */
	String productName = "";
	/**
	 * FT                   /evidence=experimental
	 */
	String evidence = "";
	/**
	 * 实验手段
	 * 编号的话要回去找GeneMiRNA的文献
	 * FT                   /experiment="cloned [1,3-5], Solexa [6], CLIPseq [7]"
	 */
	String experiment = "";
	
}
