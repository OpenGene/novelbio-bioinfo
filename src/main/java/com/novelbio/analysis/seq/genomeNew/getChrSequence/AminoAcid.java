package com.novelbio.analysis.seq.genomeNew.getChrSequence;

import java.util.HashMap;
import org.apache.log4j.Logger;


public class AminoAcid {
	
	private static Logger logger = Logger.getLogger(AminoAcid.class);
	
	public static final String AA3_Ala = "Ala";
	public static final String AA3_Arg = "Arg";
	public static final String AA3_Asp = "Asp";
	public static final String AA3_Cys = "Cys";
	public static final String AA3_Gln = "Gln";
	public static final String AA3_Glu = "Glu";
	public static final String AA3_His = "His";
	public static final String AA3_Ile = "Ile";
	public static final String AA3_Gly = "Gly";
	public static final String AA3_Asn = "Asn";
	public static final String AA3_Leu = "Leu";
	public static final String AA3_Lys = "Lys";
	public static final String AA3_Met = "Met";
	public static final String AA3_Phe = "Phe";
	public static final String AA3_Pro = "Pro";
	public static final String AA3_Ser = "Ser";
	public static final String AA3_Thr = "Thr";
	public static final String AA3_Trp = "Trp";
	public static final String AA3_Tyr = "Tyr";
	public static final String AA3_Val = "Val";
	/**
	 * 用X表示
	 */
	public static final String AA3_STOP = "***";
	
	public static final String AA1_Ala = "A";
	public static final String AA1_Arg = "R";
	public static final String AA1_Asp = "D";
	public static final String AA1_Cys = "C";
	public static final String AA1_Gln = "Q";
	public static final String AA1_Glu = "E";
	public static final String AA1_His = "H";
	public static final String AA1_Ile = "I";
	public static final String AA1_Gly = "G";
	public static final String AA1_Asn = "N";
	public static final String AA1_Leu = "L";
	public static final String AA1_Lys = "K";
	public static final String AA1_Met = "M";
	public static final String AA1_Phe = "F";
	public static final String AA1_Pro = "P";
	public static final String AA1_Ser = "S";
	public static final String AA1_Thr = "T";
	public static final String AA1_Trp = "W";
	public static final String AA1_Tyr = "Y";
	public static final String AA1_Val = "V";
	/**
	 * 用X表示
	 */
	public static final String AA1_STOP = "*";
	public static void main(String[] args) {
		System.out.println(getAAformate("met "));
	}
	/**
	 * 三联密码子的配对情况表
	 */
	static HashMap<String, String> hashCode3 = null;
	private static HashMap<String, String> getHashCode3()
	{
		if (hashCode3 != null) {
			return hashCode3;
		}
		hashCode3 =  new HashMap<String, String>();
		hashCode3.put("UUU", AA3_Phe); hashCode3.put("UCU", AA3_Ser);   hashCode3.put("UAU", AA3_Tyr);   hashCode3.put("UGU", AA3_Cys);  
		hashCode3.put("UUC", AA3_Phe); hashCode3.put("UCC", AA3_Ser);   hashCode3.put("UAC", AA3_Tyr);   hashCode3.put("UGC", AA3_Cys);  
		hashCode3.put("UUA", AA3_Leu); hashCode3.put("UCA", AA3_Ser);   hashCode3.put("UAA", AA3_STOP);  hashCode3.put("UGA", AA3_STOP); 
		hashCode3.put("UUG", AA3_Leu); hashCode3.put("UCG", AA3_Ser);   hashCode3.put("UAG", AA3_STOP);  hashCode3.put("UGG", AA3_Trp);  
		
		hashCode3.put("CUU", AA3_Leu); hashCode3.put("CCU", AA3_Pro);  hashCode3.put("CAU", AA3_His);   hashCode3.put("CGU",AA3_Arg);
		hashCode3.put("CUC", AA3_Leu); hashCode3.put("CCC", AA3_Pro);  hashCode3.put("CAC", AA3_His);   hashCode3.put("CGC", AA3_Arg);
		hashCode3.put("CUA", AA3_Leu); hashCode3.put("CCA", AA3_Pro);  hashCode3.put("CAA", AA3_Gln);   hashCode3.put("CGA", AA3_Arg);
		hashCode3.put("CUG", AA3_Leu); hashCode3.put("CCG", AA3_Pro);  hashCode3.put("CAG", AA3_Gln);   hashCode3.put("CGG", AA3_Arg);
		
		hashCode3.put("AUU", AA3_Ile);   hashCode3.put("ACU", AA3_Thr); hashCode3.put("AAU", AA3_Asn);  hashCode3.put("AGU", AA3_Ser);
		hashCode3.put("AUC", AA3_Ile);   hashCode3.put("ACC", AA3_Thr); hashCode3.put("AAC", AA3_Asn);  hashCode3.put("AGC", AA3_Ser);
		hashCode3.put("AUA", AA3_Ile);   hashCode3.put("ACA", AA3_Thr); hashCode3.put("AAA", AA3_Lys);  hashCode3.put("AGA", AA3_Arg);
		hashCode3.put("AUG", AA3_Met); hashCode3.put("ACG", AA3_Thr); hashCode3.put("AAG", AA3_Lys);  hashCode3.put("AGG", AA3_Arg);
		
		hashCode3.put("GUU", AA3_Val); hashCode3.put("GCU", AA3_Ala); hashCode3.put("GAU", AA3_Asp);  hashCode3.put("GGU", AA3_Gly);
		hashCode3.put("GUC", AA3_Val); hashCode3.put("GCC", AA3_Ala); hashCode3.put("GAC", AA3_Asp);  hashCode3.put("GGC", AA3_Gly);
		hashCode3.put("GUA", AA3_Val); hashCode3.put("GCA", AA3_Ala); hashCode3.put("GAA", AA3_Glu);  hashCode3.put("GGA", AA3_Gly);
		hashCode3.put("GUG", AA3_Val); hashCode3.put("GCG", AA3_Ala); hashCode3.put("GAG", AA3_Glu);  hashCode3.put("GGG", AA3_Gly);
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		hashCode3.put("TTT", AA3_Phe); hashCode3.put("TCT", AA3_Ser);   hashCode3.put("TAT", AA3_Tyr);   hashCode3.put("TGT", AA3_Cys);  
		hashCode3.put("TTC", AA3_Phe); hashCode3.put("TCC", AA3_Ser);   hashCode3.put("TAC", AA3_Tyr);   hashCode3.put("TGC", AA3_Cys);  
		hashCode3.put("TTA", AA3_Leu); hashCode3.put("TCA", AA3_Ser);   hashCode3.put("TAA", AA3_STOP);  hashCode3.put("TGA", AA3_STOP); 
		hashCode3.put("TTG", AA3_Leu); hashCode3.put("TCG", AA3_Ser);   hashCode3.put("TAG", AA3_STOP);  hashCode3.put("TGG", AA3_Trp);  
		                                                         
		hashCode3.put("CTT", AA3_Leu); hashCode3.put("CCT", AA3_Pro);  hashCode3.put("CAT", AA3_His);   hashCode3.put("CGT", AA3_Arg);
		hashCode3.put("CTC", AA3_Leu);// hashCode.put("CCC", "Pro");  hashCode.put("CAC", "His");   hashCode.put("CGC", "Arg");
		hashCode3.put("CTA", AA3_Leu); //hashCode.put("CCA", "Pro");  hashCode.put("CAA", "Gln");   hashCode.put("CGA", "Arg");
		hashCode3.put("CTG", AA3_Leu); //hashCode.put("CCG", "Pro");  hashCode.put("CAG", "Gln");   hashCode.put("CGG", "Arg");
		                           
		hashCode3.put("ATT", AA3_Ile);   hashCode3.put("ACT", AA3_Thr); hashCode3.put("AAT", AA3_Asn);  hashCode3.put("AGT", AA3_Ser);
		hashCode3.put("ATC", AA3_Ile);   //hashCode.put("ACC", "Thr"); hashCode.put("AAC", "Asn");  hashCode.put("AGC", "Ser");
		hashCode3.put("ATA", AA3_Ile);   //hashCode.put("ACA", "Thr"); hashCode.put("AAA", "Lys");  hashCode.put("AGA", "Arg");
		hashCode3.put("ATG", AA3_Met); //hashCode.put("ACG", "Thr"); hashCode.put("AAG", "Lys");  hashCode.put("AGG", "Arg");
		                           
		hashCode3.put("GTT", AA3_Val); hashCode3.put("GCT", AA3_Ala); hashCode3.put("GAT", AA3_Asp);  hashCode3.put("GGT", AA3_Gly);
		hashCode3.put("GTC", AA3_Val);// hashCode.put("GCC", "Ala"); hashCode.put("GAC", "Asp");  hashCode.put("GGC", "Gly");
		hashCode3.put("GTA", AA3_Val); //hashCode.put("GCA", "Ala"); hashCode.put("GAA", "Glu");  hashCode.put("GGA", "Gly");
		hashCode3.put("GTG", AA3_Val); //hashCode.put("GCG", "Ala"); hashCode.put("GAG", "Glu");  hashCode.put("GGG", "Gly");

		return hashCode3;
	}
	/**
	 * 三联密码子的配对情况表
	 */
	static HashMap<String, String> hashCode1 = null;
	private static HashMap<String, String> getHashCode1()
	{
		if (hashCode1 != null) {
			return hashCode1;
		}
		hashCode1 =  new HashMap<String, String>();
		hashCode1.put("UUU", AA1_Phe); hashCode1.put("UCU", AA1_Ser);   hashCode1.put("UAU", AA1_Tyr);   hashCode1.put("UGU", AA1_Cys);  
		hashCode1.put("UUC", AA1_Phe); hashCode1.put("UCC", AA1_Ser);   hashCode1.put("UAC", AA1_Tyr);   hashCode1.put("UGC", AA1_Cys);  
		hashCode1.put("UUA", AA1_Leu); hashCode1.put("UCA", AA1_Ser);   hashCode1.put("UAA", AA1_STOP);  hashCode1.put("UGA", AA1_STOP); 
		hashCode1.put("UUG", AA1_Leu); hashCode1.put("UCG", AA1_Ser);   hashCode1.put("UAG", AA1_STOP);  hashCode1.put("UGG", AA1_Trp);  
		
		hashCode1.put("CUU", AA1_Leu); hashCode1.put("CCU", AA1_Pro);  hashCode1.put("CAU", AA1_His);   hashCode1.put("CGU",AA1_Arg);
		hashCode1.put("CUC", AA1_Leu); hashCode1.put("CCC", AA1_Pro);  hashCode1.put("CAC", AA1_His);   hashCode1.put("CGC", AA1_Arg);
		hashCode1.put("CUA", AA1_Leu); hashCode1.put("CCA", AA1_Pro);  hashCode1.put("CAA", AA1_Gln);   hashCode1.put("CGA", AA1_Arg);
		hashCode1.put("CUG", AA1_Leu); hashCode1.put("CCG", AA1_Pro);  hashCode1.put("CAG", AA1_Gln);   hashCode1.put("CGG", AA1_Arg);
		
		hashCode1.put("AUU", AA1_Ile);   hashCode1.put("ACU", AA1_Thr); hashCode1.put("AAU", AA1_Asn);  hashCode1.put("AGU", AA1_Ser);
		hashCode1.put("AUC", AA1_Ile);   hashCode1.put("ACC", AA1_Thr); hashCode1.put("AAC", AA1_Asn);  hashCode1.put("AGC", AA1_Ser);
		hashCode1.put("AUA", AA1_Ile);   hashCode1.put("ACA", AA1_Thr); hashCode1.put("AAA", AA1_Lys);  hashCode1.put("AGA", AA1_Arg);
		hashCode1.put("AUG", AA1_Met); hashCode1.put("ACG", AA1_Thr); hashCode1.put("AAG", AA1_Lys);  hashCode1.put("AGG", AA1_Arg);
		
		hashCode1.put("GUU", AA1_Val); hashCode1.put("GCU", AA1_Ala); hashCode1.put("GAU", AA1_Asp);  hashCode1.put("GGU", AA1_Gly);
		hashCode1.put("GUC", AA1_Val); hashCode1.put("GCC", AA1_Ala); hashCode1.put("GAC", AA1_Asp);  hashCode1.put("GGC", AA1_Gly);
		hashCode1.put("GUA", AA1_Val); hashCode1.put("GCA", AA1_Ala); hashCode1.put("GAA", AA1_Glu);  hashCode1.put("GGA", AA1_Gly);
		hashCode1.put("GUG", AA1_Val); hashCode1.put("GCG", AA1_Ala); hashCode1.put("GAG", AA1_Glu);  hashCode1.put("GGG", AA1_Gly);
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		hashCode1.put("TTT", AA1_Phe); hashCode1.put("TCT", AA1_Ser);   hashCode1.put("TAT", AA1_Tyr);   hashCode1.put("TGT", AA1_Cys);  
		hashCode1.put("TTC", AA1_Phe); hashCode1.put("TCC", AA1_Ser);   hashCode1.put("TAC", AA1_Tyr);   hashCode1.put("TGC", AA1_Cys);  
		hashCode1.put("TTA", AA1_Leu); hashCode1.put("TCA", AA1_Ser);   hashCode1.put("TAA", AA1_STOP);  hashCode1.put("TGA", AA1_STOP); 
		hashCode1.put("TTG", AA1_Leu); hashCode1.put("TCG", AA1_Ser);   hashCode1.put("TAG", AA1_STOP);  hashCode1.put("TGG", AA1_Trp);  
		                                                         
		hashCode1.put("CTT", AA1_Leu); hashCode1.put("CCT", AA1_Pro);  hashCode1.put("CAT", AA1_His);   hashCode1.put("CGT", AA1_Arg);
		hashCode1.put("CTC", AA1_Leu);// hashCode.put("CCC", "Pro");  hashCode.put("CAC", "His");   hashCode.put("CGC", "Arg");
		hashCode1.put("CTA", AA1_Leu); //hashCode.put("CCA", "Pro");  hashCode.put("CAA", "Gln");   hashCode.put("CGA", "Arg");
		hashCode1.put("CTG", AA1_Leu); //hashCode.put("CCG", "Pro");  hashCode.put("CAG", "Gln");   hashCode.put("CGG", "Arg");
		                           
		hashCode1.put("ATT", AA1_Ile);   hashCode1.put("ACT", AA1_Thr); hashCode1.put("AAT", AA1_Asn);  hashCode1.put("AGT", AA1_Ser);
		hashCode1.put("ATC", AA1_Ile);   //hashCode.put("ACC", "Thr"); hashCode.put("AAC", "Asn");  hashCode.put("AGC", "Ser");
		hashCode1.put("ATA", AA1_Ile);   //hashCode.put("ACA", "Thr"); hashCode.put("AAA", "Lys");  hashCode.put("AGA", "Arg");
		hashCode1.put("ATG", AA1_Met); //hashCode.put("ACG", "Thr"); hashCode.put("AAG", "Lys");  hashCode.put("AGG", "Arg");
		                           
		hashCode1.put("GTT", AA1_Val); hashCode1.put("GCT", AA1_Ala); hashCode1.put("GAT", AA1_Asp);  hashCode1.put("GGT", AA1_Gly);
		hashCode1.put("GTC", AA1_Val);// hashCode.put("GCC", "Ala"); hashCode.put("GAC", "Asp");  hashCode.put("GGC", "Gly");
		hashCode1.put("GTA", AA1_Val); //hashCode.put("GCA", "Ala"); hashCode.put("GAA", "Glu");  hashCode.put("GGA", "Gly");
		hashCode1.put("GTG", AA1_Val); //hashCode.put("GCG", "Ala"); hashCode.put("GAG", "Glu");  hashCode.put("GGG", "Gly");

		return hashCode1;
	}
	/**
	 * 蛋白性质表
	 */
	static HashMap<String, String[]> hashAAquality = null;
	/**
	 * 蛋白性质表
	 */
	private static HashMap<String, String[]> getHashAAquality()
	{
		if (hashAAquality != null) {
			return hashAAquality;
		}
		hashAAquality = new HashMap<String, String[]>();
		hashAAquality.put(AA3_Asp, new String[]{"polar","charged","negatively"});    hashAAquality.put(AA1_Asp, new String[]{"polar","charged","negatively"});
		hashAAquality.put(AA3_Glu, new String[]{"polar","charged","negatively"});     hashAAquality.put(AA1_Glu, new String[]{"polar","charged","negatively"});
		hashAAquality.put(AA3_His, new String[]{"polar","charged","positively"});		hashAAquality.put(AA1_His, new String[]{"polar","charged","positively"});
		hashAAquality.put(AA3_Lys, new String[]{"polar","charged","positively"});		hashAAquality.put(AA1_Lys, new String[]{"polar","charged","positively"});
		hashAAquality.put(AA3_Arg, new String[]{"polar","charged","positively"});		hashAAquality.put(AA1_Arg, new String[]{"polar","charged","positively"});

		hashAAquality.put(AA3_Asn, new String[]{"polar","uncharged","amide"});		hashAAquality.put(AA1_Asn, new String[]{"polar","uncharged","amide"});
		hashAAquality.put(AA3_Gln, new String[]{"polar","uncharged","amide"});		hashAAquality.put(AA1_Gln, new String[]{"polar","uncharged","amide"});

 		hashAAquality.put(AA3_Ser, new String[]{"polar","uncharged","alcohol"}); 		hashAAquality.put(AA1_Ser, new String[]{"polar","uncharged","alcohol"});
 		hashAAquality.put(AA3_Thr, new String[]{"polar","uncharged","alcohol"}); 		hashAAquality.put(AA1_Thr, new String[]{"polar","uncharged","alcohol"});
		
 		hashAAquality.put(AA3_Leu, new String[]{"nonpolar","hydrophobic","aliphatic"});		hashAAquality.put(AA1_Leu, new String[]{"nonpolar","hydrophobic","aliphatic"});
		hashAAquality.put(AA3_Ile, new String[]{"nonpolar","hydrophobic","aliphatic"});		hashAAquality.put(AA1_Ile, new String[]{"nonpolar","hydrophobic","aliphatic"});
		hashAAquality.put(AA3_Val, new String[]{"nonpolar","hydrophobic","aliphatic"});		hashAAquality.put(AA1_Val, new String[]{"nonpolar","hydrophobic","aliphatic"});

		hashAAquality.put(AA3_Phe, new String[]{"nonpolar","hydrophobic","aromatic"});		hashAAquality.put(AA1_Phe, new String[]{"nonpolar","hydrophobic","aromatic"});
		hashAAquality.put(AA3_Tyr, new String[]{"nonpolar","hydrophobic","aromatic"});		hashAAquality.put(AA1_Tyr, new String[]{"nonpolar","hydrophobic","aromatic"});
		hashAAquality.put(AA3_Trp, new String[]{"nonpolar","hydrophobic","aromatic"});		hashAAquality.put(AA1_Trp, new String[]{"nonpolar","hydrophobic","aromatic"});

		hashAAquality.put(AA3_Ala, new String[]{"nonpolar","small","small"});		hashAAquality.put(AA1_Ala, new String[]{"nonpolar","small","small"});
		hashAAquality.put(AA3_Gly, new String[]{"nonpolar","small","small"});		hashAAquality.put(AA1_Gly, new String[]{"nonpolar","small","small"});

 		hashAAquality.put(AA3_Met, new String[]{"nonpolar","hydrophobic","sulfur"}); 		hashAAquality.put(AA1_Met, new String[]{"nonpolar","hydrophobic","sulfur"});
		hashAAquality.put(AA3_Cys, new String[]{"nonpolar","not_group","sulfur"});		hashAAquality.put(AA1_Cys, new String[]{"nonpolar","not_group","sulfur"});
		
		hashAAquality.put(AA3_Pro, new String[]{"nonpolar","not_group","other"});		hashAAquality.put(AA1_Pro, new String[]{"nonpolar","not_group","other"});
		hashAAquality.put(AA3_STOP, new String[]{"Stop_Code","Stop_Code","Stop_Code"});		hashAAquality.put(AA1_STOP, new String[]{"Stop_Code","Stop_Code","Stop_Code"});

		return hashAAquality;
	}

	
	/**
	 * 蛋白一位和三位编号转换
	 */
	static HashMap<String, String> hashAAchange = null;
	/**
	 * 蛋白一位和三位编号转换
	 */
	private static HashMap<String, String> getHashAAchange()
	{
		if (hashAAchange != null) {
			return hashAAchange;
		}
		hashAAchange = new HashMap<String, String>();
		hashAAchange.put( AA1_Asp, AA3_Asp);     hashAAchange.put( AA3_Asp, AA1_Asp);
		hashAAchange.put( AA1_Arg, AA3_Arg);     hashAAchange.put( AA3_Arg, AA1_Arg);
		hashAAchange.put( AA1_Asp, AA3_Asp);     hashAAchange.put( AA3_Asp, AA1_Asp);
		hashAAchange.put( AA1_Cys, AA3_Cys);     hashAAchange.put( AA3_Cys, AA1_Cys);
		hashAAchange.put( AA1_Gln, AA3_Gln);     hashAAchange.put( AA3_Gln, AA1_Gln);
		hashAAchange.put( AA1_Glu, AA3_Glu);     hashAAchange.put( AA3_Glu, AA1_Glu);
		hashAAchange.put( AA1_His, AA3_His);     hashAAchange.put( AA3_His, AA1_His);
		hashAAchange.put( AA1_Ile, AA3_Ile);     hashAAchange.put( AA3_Ile, AA1_Ile);
		hashAAchange.put( AA1_Gly, AA3_Gly);     hashAAchange.put( AA3_Gly, AA1_Gly);
		hashAAchange.put( AA1_Asn, AA3_Asn);     hashAAchange.put( AA3_Asn, AA1_Asn);
		hashAAchange.put( AA1_Leu, AA3_Leu);     hashAAchange.put( AA3_Leu, AA1_Leu);
		hashAAchange.put( AA1_Lys, AA3_Lys);     hashAAchange.put( AA3_Lys, AA1_Lys);
		hashAAchange.put( AA1_Met, AA3_Met);     hashAAchange.put( AA3_Met, AA1_Met);
		hashAAchange.put( AA1_Phe, AA3_Phe);     hashAAchange.put( AA3_Phe, AA1_Phe);
		hashAAchange.put( AA1_Pro, AA3_Pro);     hashAAchange.put( AA3_Pro, AA1_Pro);
		hashAAchange.put( AA1_Ser, AA3_Ser);     hashAAchange.put( AA3_Ser, AA1_Ser);
		hashAAchange.put( AA1_Thr, AA3_Thr);     hashAAchange.put( AA3_Thr, AA1_Thr);
		hashAAchange.put( AA1_Trp, AA3_Trp);     hashAAchange.put( AA3_Trp, AA1_Trp);
		hashAAchange.put( AA1_Tyr, AA3_Tyr);     hashAAchange.put( AA3_Tyr, AA1_Tyr);
		hashAAchange.put( AA1_Val, AA3_Val);     hashAAchange.put( AA3_Val, AA1_Val);

		return hashAAchange;
	}
	
	String DNAseq = "";
	/**
	 * 默认返回三字母长度的氨基酸
	 */
	boolean AA3Len = true;
	
	public AminoAcid(String DNAseq) {
		this.DNAseq = DNAseq;
	}
	public AminoAcid(String DNAseq, boolean AA3Len) {
		this.DNAseq = DNAseq;
		this.AA3Len = AA3Len;
	}
	
	
	public void setAA3Len(boolean AA3Len) {
		this.AA3Len = AA3Len;
	}
	
	
	/**
	 * 指定序列，将其转换为蛋白编码
	 * @param DNAcode
	 * @param AA1 是否转化为单字母AA，false转化为3字母AA
	 * @return
	 * null 表示没有找到，说明输入的序列有误
	 */
	public String convertDNA2AA()
	{
		DNAseq = DNAseq.trim().toUpperCase();
		if (DNAseq.length() %3 != 0) {
			logger.error("DNA三联密码长度不对：" + DNAseq);
		}
		char[] DNAseqChar = DNAseq.toCharArray();
		String resultAAseq = "";
		for (int i = 2; i < DNAseqChar.length; i = i + 3) {
			String tmpDNAcode = "";
			tmpDNAcode = ""+ DNAseqChar[i - 2] + DNAseqChar[i - 1] + DNAseqChar[i];
			if (AA3Len) {
				resultAAseq = resultAAseq + " " + convertDNACode2AA(tmpDNAcode, !AA3Len);
			}
			else {
				resultAAseq = resultAAseq + convertDNACode2AA(tmpDNAcode, !AA3Len);
			}
		}
		return resultAAseq.trim();
	}
	
	public int getOrfShitf()
	{
		return DNAseq.length()%3;
	}
	
	
	/**
	 * 指定三联密码子，将其转换为蛋白编码
	 * @param DNAcode
	 * @param AA1 是否转化为单字母AA，false转化为3字母AA
	 * @return
	 * null 表示没有找到，说明输入的序列有误
	 */
	 public static String convertDNACode2AA(String DNAcode, boolean AA1) {
		 DNAcode = DNAcode.trim().toUpperCase();
		if (DNAcode.length() != 3) {
			logger.error("DNA三联密码长度不对：" + DNAcode);
		}
		if (AA1) {
			return getHashCode1().get(DNAcode);
		}
		else {
			return getHashCode3().get(DNAcode);
		}
	}
	
	
	/**
	 * 输入格式不标准的AA，将其改成标准格式AA
	 * @param AA
	 * @return
	 */
	private static String getAAformate(String AA)
	{
		
		if (AA.trim().equals(AA1_STOP) || AA.trim().equals(AA3_STOP)) {
			return AA.trim();
		}
		
		
		AA = AA.trim();
		if (AA.length() == 1) {
			return AA.toUpperCase();
		}
		else if (AA.length() == 3) {
			AA = AA.toLowerCase();
			char[] aa = AA.toCharArray();
			aa[0] = (char)((int)aa[0] - 32);
			return String.valueOf(aa);
		}
		else {
			logger.error("input error AA: "+AA);
			return null;
		}
	}
	
	
	/**
	 * 获得氨基酸的特性，极性，电荷等，按照genedoc的分类标准
	 * @return
	 * string[3]: 0：极性--带电荷--负电
	 * 1：
	 */
	public static String[] getAAquality(String AA) {
		AA = getAAformate(AA);
		return getHashAAquality().get(AA);
	}
	/**
	 * 将氨基酸在单字母和三字母之间转换
	 */
	public static String convertAA(String AA) {
		AA = getAAformate(AA);
		return getHashAAchange().get(AA);
	}
	/**
	 * 
	 * 输入的是DNA三联密码字
	 * 比较两个氨基酸的化学性质，返回差异点，返回最大差异
	 * 譬如如果极性不同就返回极性
	 * 格式 polar --> nonpolar等
	 * 都一样则返回"";
	 * @param DNAcode1 第一个DNA编码
	 * @param DNAcode2 第二个DNA编码
	 * @return
	 */
	public static String cmpAAqualityDNA(String DNAcode1, String DNAcode2)
	{
		String AA1 = convertDNACode2AA(DNAcode1, true);
		String AA2 = convertDNACode2AA(DNAcode2, true);
		return compareAAquality(AA1, AA2);
	}
	
	/**
	 * 输入的是氨基酸，无所谓三字符还是单字符
	 * 比较两个氨基酸的化学性质，返回差异点，返回最大差异
	 * 譬如如果极性不同就返回极性
	 * 格式 polar --> nonpolar等
	 * 都一样则返回"";
	 */
	public static String cmpAAquality(String AA1, String AA2)
	{
		AA1 = getAAformate(AA1);
		AA2 = getAAformate(AA2);
		return compareAAquality(AA1, AA2);
	}
	/**
	 * 输入的是氨基酸，必须是标准格式
	 * 比较两个氨基酸的化学性质，返回差异点，返回最大差异
	 * 譬如如果极性不同就返回极性
	 * 格式 polar --> nonpolar等
	 * 都一样则返回"";
	 */
	private static String compareAAquality(String AA1, String AA2)
	{
		if (AA1.equals(AA2)) {
			return "same Amio Acid";
		}
		String[] aaInfo1 = getHashAAquality().get(AA1);
		String[] aaInfo2 = getHashAAquality().get(AA2);
		if (aaInfo1 == null || aaInfo2 ==null) {
			return "";
		}
		for (int i = 0; i < aaInfo1.length; i++) {
			if (!aaInfo1[i].equals(aaInfo2[i])) {
				return aaInfo1[i] + " --> " + aaInfo2[i];
			}
		}
		return "same chemical property";	
	}
	
}
