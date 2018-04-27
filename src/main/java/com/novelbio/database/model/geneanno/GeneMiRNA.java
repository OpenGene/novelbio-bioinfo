package com.novelbio.database.model.geneanno;

import java.util.ArrayList;

import com.novelbio.database.domain.modgeneid.GeneID;

/**
 * 本表要导入专门的miRDB数据库中
 * @author zong0jie
 *
 */
public class GeneMiRNA {
	public static void main(String[] args) {
		GeneID copedID = new GeneID("let-7", 0);
		System.out.println(copedID.getTaxID());
	}
	/**
	 * 
	 * miRNA.dat在sanger的miRBase下载，类型如下
	 * 
ID   cel-let-7         standard; RNA; CEL; 99 BP.
XX
AC   MI0000001;
XX
DE   Caenorhabditis elegans let-7 stem-loop
XX
RN   [1]
RX   PUBMED; 11679671.
RA   Lau NC, Lim LP, Weinstein EG, Bartel DP;
RT   "An abundant class of tiny RNAs with probable regulatory roles in
RT   Caenorhabditis elegans";
RL   Science. 294:858-862(2001).
XX
RN   [2]
RX   PUBMED; 12672692.
RA   Lim LP, Lau NC, Weinstein EG, Abdelhakim A, Yekta S, Rhoades MW, Burge CB,
RA   Bartel DP;
RT   "The microRNAs of Caenorhabditis elegans";
RL   Genes Dev. 17:991-1008(2003).
XX
DR   RFAM; RF00027; let-7.
DR   WORMBASE; C05G5/12462-12364; .
XX
CC   let-7 is found on chromosome X in Caenorhabditis elegans [1] and pairs to
CC   sites within the 3' untranslated region (UTR) of target mRNAs, specifying
XX
FH   Key             Location/Qualifiers
FH
FT   miRNA           17..38
FT                   /accession="MIMAT0000001"
FT                   /product="cel-let-7-5p"
FT                   /evidence=experimental
FT                   /experiment="cloned [1-3,5], Northern [1], PCR [4], Solexa
FT                   [6], CLIPseq [7]"
FT   miRNA           56..80
FT                   /accession="MIMAT0015091"
FT                   /product="cel-let-7-3p"
FT                   /evidence=experimental
FT                   /experiment="CLIPseq [7]"
XX
SQ   Sequence 99 BP; 26 A; 19 C; 24 G; 0 T; 30 other;
     uacacugugg auccggugag guaguagguu guauaguuug gaauauuacc accggugaac        60
     uaugcaauuu ucuaccuuac cggagacaga acucuucga                               99
//
	 */
	
	/**
	 * 物种ID，这个在表里面没有
	 * ID   cel-let-7         standard; RNA; CEL; 99 BP.
	 */
	int taxID = 0;
	/**
	 * miRBase里面的ID，编号AC
	 * AC   MI0000001;
	 */
	String miRBaseID = "";
	/**
	 * 官方名，如cel-let-7
	 * ID   cel-let-7         standard; RNA; CEL; 99 BP.
	 */
	String officalName = "";
	/**
	 * NCBI的GeneID或UniProt的GeneID
	 */
	String genUniID = "";
	/**
	 * 实际长度，从1开始记数，在ID里面的
	 * ID   cel-let-7         standard; RNA; CEL; 99 BP.
	 */
	int length = 0;
	/**
	 * pubmedID，编号RX
	 * RX   PUBMED; 19460142.
	 */
	ArrayList<Integer> lsPubmedID = new ArrayList<Integer>();
	String description = "";
	/**
	 * 编号是CC，描述类型的话
	 * CC   The excised miRNA sequence was initially predicted [1], and confirmed
	 * CC   later by sequencing [3].
	 */
	String comment = "";
	/**
	 * 具体序列
	 * SQ   Sequence 94 BP; 17 A; 25 C; 26 G; 0 T; 26 other;
     	augcuuccgg ccuguucccu gagaccucaa gugugagugu acuauugaug cuucacaccu        60
     	gggcucuccg gguaccagga cgguuugagc agau                                    94
	 */
	String seq = "";
	

	
	
	
	
	
	
	
	
	
}
