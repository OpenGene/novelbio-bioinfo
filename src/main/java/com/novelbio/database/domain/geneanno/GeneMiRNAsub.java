package com.novelbio.database.domain.geneanno;

public class GeneMiRNAsub {
	
	/**
	 * �þ������miRNA��accID
	 * FT                   /accession="MIMAT0000002"
	 */
	String accID = "";
	/**
	 * ��miRNA��ǰ���е����
	 */
	int start = 0;
	/**
	 * ��miRNA��ǰ���е��յ�
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
	 * ʵ���ֶ�
	 * ��ŵĻ�Ҫ��ȥ��GeneMiRNA������
	 * FT                   /experiment="cloned [1,3-5], Solexa [6], CLIPseq [7]"
	 */
	String experiment = "";
	
}
