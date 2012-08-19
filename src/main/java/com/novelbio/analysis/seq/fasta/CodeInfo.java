package com.novelbio.analysis.seq.fasta;

import java.util.HashMap;

/** 三联密码子对照表 */
class CodeInfo {
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
	/** 用X表示 */
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
	
	/** 用X表示 */
	public static final String AA1_STOP = "*";
	/** 反向互补哈希表 */
	private static HashMap<Character, Character> compmap = null;// 碱基翻译哈希表
	/** 蛋白一位和三位编号转换 */
	static HashMap<String, String> mapAA1toAA3 = null;
	/**  蛋白性质表  */
	static HashMap<String, String[]> mapAA2ChamicalQuality = null;
	/** 三联密码子的配对情况表 */
	static HashMap<String, String> mapDNACodToAA3 = null;
	/** 三联密码子的配对情况表 */
	static HashMap<String, String> mapDNAcodToAA1 = null;

	/**
	 * 获得互补配对hash表<br>
		 * 生物信息学中常用的 18 个碱基字母 字母 碱基 单碱基 A A C C G G I I T T U U<br>
		 *  二碱基 K G/T M A/C R A/G S G/C W A/T Y C/T <br>
		 *  三碱基 B C/G/T D A/G/T H A/C/T V A/C/G<br>
		 *   四碱基  N A/C/G/T X A/C/G/T <br>
		 *   全称 Adenine Cytosine Guanine Isosine Thymine Uracil Keto aMino puRine Strong pair Weak pair pYrimidine Not A Not C Not G Not U (or T) Any Unknown <br>
		 *   说明 腺嘌呤 胞嘧啶 鸟嘌呤 次黄嘌呤 胸腺嘧啶 尿嘧啶 含酮基 含氨基 嘌呤 强配对 弱配对 嘧啶 非A 非C 非G 非 U(T) 任一碱基 未知碱基 <br>
	 */
	public static HashMap<Character, Character> getCompMap() {
		if (compmap != null) {
			return compmap;
		}
		compmap = new HashMap<Character, Character>();// 碱基翻译哈希表
		compmap.put(Character.valueOf('A'), Character.valueOf('T'));
		compmap.put(Character.valueOf('a'), Character.valueOf('t'));
		
		compmap.put(Character.valueOf('T'), Character.valueOf('A'));
		compmap.put(Character.valueOf('t'), Character.valueOf('a'));
		
		compmap.put(Character.valueOf('U'), Character.valueOf('A'));
		compmap.put(Character.valueOf('u'), Character.valueOf('a'));
		
		compmap.put(Character.valueOf('G'), Character.valueOf('C'));
		compmap.put(Character.valueOf('g'), Character.valueOf('c'));
		
		compmap.put(Character.valueOf('C'), Character.valueOf('G'));
		compmap.put(Character.valueOf('c'), Character.valueOf('g'));
		//////////////////////////////////////////////////////////////////////////////////////////////
		compmap.put(Character.valueOf('B'), Character.valueOf('V'));
		compmap.put(Character.valueOf('b'), Character.valueOf('v'));
		
		compmap.put(Character.valueOf('V'), Character.valueOf('B'));
		compmap.put(Character.valueOf('v'), Character.valueOf('b'));
		//////////////////////////////////////////////////////////////////////////////////////////////
		compmap.put(Character.valueOf('D'), Character.valueOf('H'));
		compmap.put(Character.valueOf('d'), Character.valueOf('h'));
		
		compmap.put(Character.valueOf('H'), Character.valueOf('D'));
		compmap.put(Character.valueOf('h'), Character.valueOf('d'));
		/////////////////////////////////////////////////////////////////////////////////////////////
		compmap.put(Character.valueOf('X'), Character.valueOf('X'));
		compmap.put(Character.valueOf('x'), Character.valueOf('x'));
		
		compmap.put(Character.valueOf('N'), Character.valueOf('N'));
		compmap.put(Character.valueOf('n'), Character.valueOf('n'));
		////////////////////////////////////////////////////////////////////////////////////////////
		compmap.put(Character.valueOf('-'), Character.valueOf('-'));
		compmap.put(Character.valueOf('\n'), Character.valueOf(' '));
		compmap.put(Character.valueOf(' '), Character.valueOf(' '));
		//////////////////////////////////////////////////////////////////////////////////////////
		compmap.put(Character.valueOf('Y'), Character.valueOf('R'));
		compmap.put(Character.valueOf('y'), Character.valueOf('r'));
		
		compmap.put(Character.valueOf('R'), Character.valueOf('Y'));
		compmap.put(Character.valueOf('r'), Character.valueOf('y'));
		
		return compmap;
	}
	static HashMap<String, String> getHashCode3() {
		if (mapDNACodToAA3 != null) {
			return mapDNACodToAA3;
		}
		mapDNACodToAA3 =  new HashMap<String, String>();
		mapDNACodToAA3.put("UUU", AA3_Phe); mapDNACodToAA3.put("UCU", AA3_Ser);   mapDNACodToAA3.put("UAU", AA3_Tyr);   mapDNACodToAA3.put("UGU", AA3_Cys);  
		mapDNACodToAA3.put("UUC", AA3_Phe); mapDNACodToAA3.put("UCC", AA3_Ser);   mapDNACodToAA3.put("UAC", AA3_Tyr);   mapDNACodToAA3.put("UGC", AA3_Cys);  
		mapDNACodToAA3.put("UUA", AA3_Leu); mapDNACodToAA3.put("UCA", AA3_Ser);   mapDNACodToAA3.put("UAA", AA3_STOP);  mapDNACodToAA3.put("UGA", AA3_STOP); 
		mapDNACodToAA3.put("UUG", AA3_Leu); mapDNACodToAA3.put("UCG", AA3_Ser);   mapDNACodToAA3.put("UAG", AA3_STOP);  mapDNACodToAA3.put("UGG", AA3_Trp);  
		
		mapDNACodToAA3.put("CUU", AA3_Leu); mapDNACodToAA3.put("CCU", AA3_Pro);  mapDNACodToAA3.put("CAU", AA3_His);   mapDNACodToAA3.put("CGU",AA3_Arg);
		mapDNACodToAA3.put("CUC", AA3_Leu); mapDNACodToAA3.put("CCC", AA3_Pro);  mapDNACodToAA3.put("CAC", AA3_His);   mapDNACodToAA3.put("CGC", AA3_Arg);
		mapDNACodToAA3.put("CUA", AA3_Leu); mapDNACodToAA3.put("CCA", AA3_Pro);  mapDNACodToAA3.put("CAA", AA3_Gln);   mapDNACodToAA3.put("CGA", AA3_Arg);
		mapDNACodToAA3.put("CUG", AA3_Leu); mapDNACodToAA3.put("CCG", AA3_Pro);  mapDNACodToAA3.put("CAG", AA3_Gln);   mapDNACodToAA3.put("CGG", AA3_Arg);
		
		mapDNACodToAA3.put("AUU", AA3_Ile);   mapDNACodToAA3.put("ACU", AA3_Thr); mapDNACodToAA3.put("AAU", AA3_Asn);  mapDNACodToAA3.put("AGU", AA3_Ser);
		mapDNACodToAA3.put("AUC", AA3_Ile);   mapDNACodToAA3.put("ACC", AA3_Thr); mapDNACodToAA3.put("AAC", AA3_Asn);  mapDNACodToAA3.put("AGC", AA3_Ser);
		mapDNACodToAA3.put("AUA", AA3_Ile);   mapDNACodToAA3.put("ACA", AA3_Thr); mapDNACodToAA3.put("AAA", AA3_Lys);  mapDNACodToAA3.put("AGA", AA3_Arg);
		mapDNACodToAA3.put("AUG", AA3_Met); mapDNACodToAA3.put("ACG", AA3_Thr); mapDNACodToAA3.put("AAG", AA3_Lys);  mapDNACodToAA3.put("AGG", AA3_Arg);
		
		mapDNACodToAA3.put("GUU", AA3_Val); mapDNACodToAA3.put("GCU", AA3_Ala); mapDNACodToAA3.put("GAU", AA3_Asp);  mapDNACodToAA3.put("GGU", AA3_Gly);
		mapDNACodToAA3.put("GUC", AA3_Val); mapDNACodToAA3.put("GCC", AA3_Ala); mapDNACodToAA3.put("GAC", AA3_Asp);  mapDNACodToAA3.put("GGC", AA3_Gly);
		mapDNACodToAA3.put("GUA", AA3_Val); mapDNACodToAA3.put("GCA", AA3_Ala); mapDNACodToAA3.put("GAA", AA3_Glu);  mapDNACodToAA3.put("GGA", AA3_Gly);
		mapDNACodToAA3.put("GUG", AA3_Val); mapDNACodToAA3.put("GCG", AA3_Ala); mapDNACodToAA3.put("GAG", AA3_Glu);  mapDNACodToAA3.put("GGG", AA3_Gly);
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		mapDNACodToAA3.put("TTT", AA3_Phe); mapDNACodToAA3.put("TCT", AA3_Ser);   mapDNACodToAA3.put("TAT", AA3_Tyr);   mapDNACodToAA3.put("TGT", AA3_Cys);  
		mapDNACodToAA3.put("TTC", AA3_Phe); mapDNACodToAA3.put("TCC", AA3_Ser);   mapDNACodToAA3.put("TAC", AA3_Tyr);   mapDNACodToAA3.put("TGC", AA3_Cys);  
		mapDNACodToAA3.put("TTA", AA3_Leu); mapDNACodToAA3.put("TCA", AA3_Ser);   mapDNACodToAA3.put("TAA", AA3_STOP);  mapDNACodToAA3.put("TGA", AA3_STOP); 
		mapDNACodToAA3.put("TTG", AA3_Leu); mapDNACodToAA3.put("TCG", AA3_Ser);   mapDNACodToAA3.put("TAG", AA3_STOP);  mapDNACodToAA3.put("TGG", AA3_Trp);  
		                                                         
		mapDNACodToAA3.put("CTT", AA3_Leu); mapDNACodToAA3.put("CCT", AA3_Pro);  mapDNACodToAA3.put("CAT", AA3_His);   mapDNACodToAA3.put("CGT", AA3_Arg);
		mapDNACodToAA3.put("CTC", AA3_Leu);// hashCode.put("CCC", "Pro");  hashCode.put("CAC", "His");   hashCode.put("CGC", "Arg");
		mapDNACodToAA3.put("CTA", AA3_Leu); //hashCode.put("CCA", "Pro");  hashCode.put("CAA", "Gln");   hashCode.put("CGA", "Arg");
		mapDNACodToAA3.put("CTG", AA3_Leu); //hashCode.put("CCG", "Pro");  hashCode.put("CAG", "Gln");   hashCode.put("CGG", "Arg");
		                           
		mapDNACodToAA3.put("ATT", AA3_Ile);   mapDNACodToAA3.put("ACT", AA3_Thr); mapDNACodToAA3.put("AAT", AA3_Asn);  mapDNACodToAA3.put("AGT", AA3_Ser);
		mapDNACodToAA3.put("ATC", AA3_Ile);   //hashCode.put("ACC", "Thr"); hashCode.put("AAC", "Asn");  hashCode.put("AGC", "Ser");
		mapDNACodToAA3.put("ATA", AA3_Ile);   //hashCode.put("ACA", "Thr"); hashCode.put("AAA", "Lys");  hashCode.put("AGA", "Arg");
		mapDNACodToAA3.put("ATG", AA3_Met); //hashCode.put("ACG", "Thr"); hashCode.put("AAG", "Lys");  hashCode.put("AGG", "Arg");
		                           
		mapDNACodToAA3.put("GTT", AA3_Val); mapDNACodToAA3.put("GCT", AA3_Ala); mapDNACodToAA3.put("GAT", AA3_Asp);  mapDNACodToAA3.put("GGT", AA3_Gly);
		mapDNACodToAA3.put("GTC", AA3_Val);// hashCode.put("GCC", "Ala"); hashCode.put("GAC", "Asp");  hashCode.put("GGC", "Gly");
		mapDNACodToAA3.put("GTA", AA3_Val); //hashCode.put("GCA", "Ala"); hashCode.put("GAA", "Glu");  hashCode.put("GGA", "Gly");
		mapDNACodToAA3.put("GTG", AA3_Val); //hashCode.put("GCG", "Ala"); hashCode.put("GAG", "Glu");  hashCode.put("GGG", "Gly");

		return mapDNACodToAA3;
	}

	static HashMap<String, String> getHashCode1() {
		if (mapDNAcodToAA1 != null) {
			return mapDNAcodToAA1;
		}
		mapDNAcodToAA1 =  new HashMap<String, String>();
		mapDNAcodToAA1.put("UUU", AA1_Phe); mapDNAcodToAA1.put("UCU", AA1_Ser);   mapDNAcodToAA1.put("UAU", AA1_Tyr);   mapDNAcodToAA1.put("UGU", AA1_Cys);  
		mapDNAcodToAA1.put("UUC", AA1_Phe); mapDNAcodToAA1.put("UCC", AA1_Ser);   mapDNAcodToAA1.put("UAC", AA1_Tyr);   mapDNAcodToAA1.put("UGC", AA1_Cys);  
		mapDNAcodToAA1.put("UUA", AA1_Leu); mapDNAcodToAA1.put("UCA", AA1_Ser);   mapDNAcodToAA1.put("UAA", AA1_STOP);  mapDNAcodToAA1.put("UGA", AA1_STOP); 
		mapDNAcodToAA1.put("UUG", AA1_Leu); mapDNAcodToAA1.put("UCG", AA1_Ser);   mapDNAcodToAA1.put("UAG", AA1_STOP);  mapDNAcodToAA1.put("UGG", AA1_Trp);  
		
		mapDNAcodToAA1.put("CUU", AA1_Leu); mapDNAcodToAA1.put("CCU", AA1_Pro);  mapDNAcodToAA1.put("CAU", AA1_His);   mapDNAcodToAA1.put("CGU",AA1_Arg);
		mapDNAcodToAA1.put("CUC", AA1_Leu); mapDNAcodToAA1.put("CCC", AA1_Pro);  mapDNAcodToAA1.put("CAC", AA1_His);   mapDNAcodToAA1.put("CGC", AA1_Arg);
		mapDNAcodToAA1.put("CUA", AA1_Leu); mapDNAcodToAA1.put("CCA", AA1_Pro);  mapDNAcodToAA1.put("CAA", AA1_Gln);   mapDNAcodToAA1.put("CGA", AA1_Arg);
		mapDNAcodToAA1.put("CUG", AA1_Leu); mapDNAcodToAA1.put("CCG", AA1_Pro);  mapDNAcodToAA1.put("CAG", AA1_Gln);   mapDNAcodToAA1.put("CGG", AA1_Arg);
		
		mapDNAcodToAA1.put("AUU", AA1_Ile);   mapDNAcodToAA1.put("ACU", AA1_Thr); mapDNAcodToAA1.put("AAU", AA1_Asn);  mapDNAcodToAA1.put("AGU", AA1_Ser);
		mapDNAcodToAA1.put("AUC", AA1_Ile);   mapDNAcodToAA1.put("ACC", AA1_Thr); mapDNAcodToAA1.put("AAC", AA1_Asn);  mapDNAcodToAA1.put("AGC", AA1_Ser);
		mapDNAcodToAA1.put("AUA", AA1_Ile);   mapDNAcodToAA1.put("ACA", AA1_Thr); mapDNAcodToAA1.put("AAA", AA1_Lys);  mapDNAcodToAA1.put("AGA", AA1_Arg);
		mapDNAcodToAA1.put("AUG", AA1_Met); mapDNAcodToAA1.put("ACG", AA1_Thr); mapDNAcodToAA1.put("AAG", AA1_Lys);  mapDNAcodToAA1.put("AGG", AA1_Arg);
		
		mapDNAcodToAA1.put("GUU", AA1_Val); mapDNAcodToAA1.put("GCU", AA1_Ala); mapDNAcodToAA1.put("GAU", AA1_Asp);  mapDNAcodToAA1.put("GGU", AA1_Gly);
		mapDNAcodToAA1.put("GUC", AA1_Val); mapDNAcodToAA1.put("GCC", AA1_Ala); mapDNAcodToAA1.put("GAC", AA1_Asp);  mapDNAcodToAA1.put("GGC", AA1_Gly);
		mapDNAcodToAA1.put("GUA", AA1_Val); mapDNAcodToAA1.put("GCA", AA1_Ala); mapDNAcodToAA1.put("GAA", AA1_Glu);  mapDNAcodToAA1.put("GGA", AA1_Gly);
		mapDNAcodToAA1.put("GUG", AA1_Val); mapDNAcodToAA1.put("GCG", AA1_Ala); mapDNAcodToAA1.put("GAG", AA1_Glu);  mapDNAcodToAA1.put("GGG", AA1_Gly);
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		mapDNAcodToAA1.put("TTT", AA1_Phe); mapDNAcodToAA1.put("TCT", AA1_Ser);   mapDNAcodToAA1.put("TAT", AA1_Tyr);   mapDNAcodToAA1.put("TGT", AA1_Cys);  
		mapDNAcodToAA1.put("TTC", AA1_Phe); mapDNAcodToAA1.put("TCC", AA1_Ser);   mapDNAcodToAA1.put("TAC", AA1_Tyr);   mapDNAcodToAA1.put("TGC", AA1_Cys);  
		mapDNAcodToAA1.put("TTA", AA1_Leu); mapDNAcodToAA1.put("TCA", AA1_Ser);   mapDNAcodToAA1.put("TAA", AA1_STOP);  mapDNAcodToAA1.put("TGA", AA1_STOP); 
		mapDNAcodToAA1.put("TTG", AA1_Leu); mapDNAcodToAA1.put("TCG", AA1_Ser);   mapDNAcodToAA1.put("TAG", AA1_STOP);  mapDNAcodToAA1.put("TGG", AA1_Trp);  
		                                                         
		mapDNAcodToAA1.put("CTT", AA1_Leu); mapDNAcodToAA1.put("CCT", AA1_Pro);  mapDNAcodToAA1.put("CAT", AA1_His);   mapDNAcodToAA1.put("CGT", AA1_Arg);
		mapDNAcodToAA1.put("CTC", AA1_Leu);// hashCode.put("CCC", "Pro");  hashCode.put("CAC", "His");   hashCode.put("CGC", "Arg");
		mapDNAcodToAA1.put("CTA", AA1_Leu); //hashCode.put("CCA", "Pro");  hashCode.put("CAA", "Gln");   hashCode.put("CGA", "Arg");
		mapDNAcodToAA1.put("CTG", AA1_Leu); //hashCode.put("CCG", "Pro");  hashCode.put("CAG", "Gln");   hashCode.put("CGG", "Arg");
		                           
		mapDNAcodToAA1.put("ATT", AA1_Ile);   mapDNAcodToAA1.put("ACT", AA1_Thr); mapDNAcodToAA1.put("AAT", AA1_Asn);  mapDNAcodToAA1.put("AGT", AA1_Ser);
		mapDNAcodToAA1.put("ATC", AA1_Ile);   //hashCode.put("ACC", "Thr"); hashCode.put("AAC", "Asn");  hashCode.put("AGC", "Ser");
		mapDNAcodToAA1.put("ATA", AA1_Ile);   //hashCode.put("ACA", "Thr"); hashCode.put("AAA", "Lys");  hashCode.put("AGA", "Arg");
		mapDNAcodToAA1.put("ATG", AA1_Met); //hashCode.put("ACG", "Thr"); hashCode.put("AAG", "Lys");  hashCode.put("AGG", "Arg");
		                           
		mapDNAcodToAA1.put("GTT", AA1_Val); mapDNAcodToAA1.put("GCT", AA1_Ala); mapDNAcodToAA1.put("GAT", AA1_Asp);  mapDNAcodToAA1.put("GGT", AA1_Gly);
		mapDNAcodToAA1.put("GTC", AA1_Val);// hashCode.put("GCC", "Ala"); hashCode.put("GAC", "Asp");  hashCode.put("GGC", "Gly");
		mapDNAcodToAA1.put("GTA", AA1_Val); //hashCode.put("GCA", "Ala"); hashCode.put("GAA", "Glu");  hashCode.put("GGA", "Gly");
		mapDNAcodToAA1.put("GTG", AA1_Val); //hashCode.put("GCG", "Ala"); hashCode.put("GAG", "Glu");  hashCode.put("GGG", "Gly");

		return mapDNAcodToAA1;
	}
	/**
	 * 蛋白性质表
	 */
	static HashMap<String, String[]> getHashAAquality()
	{
		if (mapAA2ChamicalQuality != null) {
			return mapAA2ChamicalQuality;
		}
		mapAA2ChamicalQuality = new HashMap<String, String[]>();
		mapAA2ChamicalQuality.put(AA3_Asp, new String[]{"polar","charged","negatively"});    mapAA2ChamicalQuality.put(AA1_Asp, new String[]{"polar","charged","negatively"});
		mapAA2ChamicalQuality.put(AA3_Glu, new String[]{"polar","charged","negatively"});     mapAA2ChamicalQuality.put(AA1_Glu, new String[]{"polar","charged","negatively"});
		mapAA2ChamicalQuality.put(AA3_His, new String[]{"polar","charged","positively"});		mapAA2ChamicalQuality.put(AA1_His, new String[]{"polar","charged","positively"});
		mapAA2ChamicalQuality.put(AA3_Lys, new String[]{"polar","charged","positively"});		mapAA2ChamicalQuality.put(AA1_Lys, new String[]{"polar","charged","positively"});
		mapAA2ChamicalQuality.put(AA3_Arg, new String[]{"polar","charged","positively"});		mapAA2ChamicalQuality.put(AA1_Arg, new String[]{"polar","charged","positively"});

		mapAA2ChamicalQuality.put(AA3_Asn, new String[]{"polar","uncharged","amide"});		mapAA2ChamicalQuality.put(AA1_Asn, new String[]{"polar","uncharged","amide"});
		mapAA2ChamicalQuality.put(AA3_Gln, new String[]{"polar","uncharged","amide"});		mapAA2ChamicalQuality.put(AA1_Gln, new String[]{"polar","uncharged","amide"});

 		mapAA2ChamicalQuality.put(AA3_Ser, new String[]{"polar","uncharged","alcohol"}); 		mapAA2ChamicalQuality.put(AA1_Ser, new String[]{"polar","uncharged","alcohol"});
 		mapAA2ChamicalQuality.put(AA3_Thr, new String[]{"polar","uncharged","alcohol"}); 		mapAA2ChamicalQuality.put(AA1_Thr, new String[]{"polar","uncharged","alcohol"});
		
 		mapAA2ChamicalQuality.put(AA3_Leu, new String[]{"nonpolar","hydrophobic","aliphatic"});		mapAA2ChamicalQuality.put(AA1_Leu, new String[]{"nonpolar","hydrophobic","aliphatic"});
		mapAA2ChamicalQuality.put(AA3_Ile, new String[]{"nonpolar","hydrophobic","aliphatic"});		mapAA2ChamicalQuality.put(AA1_Ile, new String[]{"nonpolar","hydrophobic","aliphatic"});
		mapAA2ChamicalQuality.put(AA3_Val, new String[]{"nonpolar","hydrophobic","aliphatic"});		mapAA2ChamicalQuality.put(AA1_Val, new String[]{"nonpolar","hydrophobic","aliphatic"});

		mapAA2ChamicalQuality.put(AA3_Phe, new String[]{"nonpolar","hydrophobic","aromatic"});		mapAA2ChamicalQuality.put(AA1_Phe, new String[]{"nonpolar","hydrophobic","aromatic"});
		mapAA2ChamicalQuality.put(AA3_Tyr, new String[]{"nonpolar","hydrophobic","aromatic"});		mapAA2ChamicalQuality.put(AA1_Tyr, new String[]{"nonpolar","hydrophobic","aromatic"});
		mapAA2ChamicalQuality.put(AA3_Trp, new String[]{"nonpolar","hydrophobic","aromatic"});		mapAA2ChamicalQuality.put(AA1_Trp, new String[]{"nonpolar","hydrophobic","aromatic"});

		mapAA2ChamicalQuality.put(AA3_Ala, new String[]{"nonpolar","small","small"});		mapAA2ChamicalQuality.put(AA1_Ala, new String[]{"nonpolar","small","small"});
		mapAA2ChamicalQuality.put(AA3_Gly, new String[]{"nonpolar","small","small"});		mapAA2ChamicalQuality.put(AA1_Gly, new String[]{"nonpolar","small","small"});

 		mapAA2ChamicalQuality.put(AA3_Met, new String[]{"nonpolar","hydrophobic","sulfur"}); 		mapAA2ChamicalQuality.put(AA1_Met, new String[]{"nonpolar","hydrophobic","sulfur"});
		mapAA2ChamicalQuality.put(AA3_Cys, new String[]{"nonpolar","not_group","sulfur"});		mapAA2ChamicalQuality.put(AA1_Cys, new String[]{"nonpolar","not_group","sulfur"});
		
		mapAA2ChamicalQuality.put(AA3_Pro, new String[]{"nonpolar","not_group","other"});		mapAA2ChamicalQuality.put(AA1_Pro, new String[]{"nonpolar","not_group","other"});
		mapAA2ChamicalQuality.put(AA3_STOP, new String[]{"Stop_Code","Stop_Code","Stop_Code"});		mapAA2ChamicalQuality.put(AA1_STOP, new String[]{"Stop_Code","Stop_Code","Stop_Code"});

		return mapAA2ChamicalQuality;
	}

	/** 蛋白一位和三位编号转换 */
	static HashMap<String, String> setMapAA1toAA3() {
		if (mapAA1toAA3 != null) {
			return mapAA1toAA3;
		}
		mapAA1toAA3 = new HashMap<String, String>();
		mapAA1toAA3.put( AA1_Asp, AA3_Asp);     mapAA1toAA3.put( AA3_Asp, AA1_Asp);
		mapAA1toAA3.put( AA1_Arg, AA3_Arg);     mapAA1toAA3.put( AA3_Arg, AA1_Arg);
		mapAA1toAA3.put( AA1_Asp, AA3_Asp);     mapAA1toAA3.put( AA3_Asp, AA1_Asp);
		mapAA1toAA3.put( AA1_Cys, AA3_Cys);     mapAA1toAA3.put( AA3_Cys, AA1_Cys);
		mapAA1toAA3.put( AA1_Gln, AA3_Gln);     mapAA1toAA3.put( AA3_Gln, AA1_Gln);
		mapAA1toAA3.put( AA1_Glu, AA3_Glu);     mapAA1toAA3.put( AA3_Glu, AA1_Glu);
		mapAA1toAA3.put( AA1_His, AA3_His);     mapAA1toAA3.put( AA3_His, AA1_His);
		mapAA1toAA3.put( AA1_Ile, AA3_Ile);     mapAA1toAA3.put( AA3_Ile, AA1_Ile);
		mapAA1toAA3.put( AA1_Gly, AA3_Gly);     mapAA1toAA3.put( AA3_Gly, AA1_Gly);
		mapAA1toAA3.put( AA1_Asn, AA3_Asn);     mapAA1toAA3.put( AA3_Asn, AA1_Asn);
		mapAA1toAA3.put( AA1_Leu, AA3_Leu);     mapAA1toAA3.put( AA3_Leu, AA1_Leu);
		mapAA1toAA3.put( AA1_Lys, AA3_Lys);     mapAA1toAA3.put( AA3_Lys, AA1_Lys);
		mapAA1toAA3.put( AA1_Met, AA3_Met);     mapAA1toAA3.put( AA3_Met, AA1_Met);
		mapAA1toAA3.put( AA1_Phe, AA3_Phe);     mapAA1toAA3.put( AA3_Phe, AA1_Phe);
		mapAA1toAA3.put( AA1_Pro, AA3_Pro);     mapAA1toAA3.put( AA3_Pro, AA1_Pro);
		mapAA1toAA3.put( AA1_Ser, AA3_Ser);     mapAA1toAA3.put( AA3_Ser, AA1_Ser);
		mapAA1toAA3.put( AA1_Thr, AA3_Thr);     mapAA1toAA3.put( AA3_Thr, AA1_Thr);
		mapAA1toAA3.put( AA1_Trp, AA3_Trp);     mapAA1toAA3.put( AA3_Trp, AA1_Trp);
		mapAA1toAA3.put( AA1_Tyr, AA3_Tyr);     mapAA1toAA3.put( AA3_Tyr, AA1_Tyr);
		mapAA1toAA3.put( AA1_Val, AA3_Val);     mapAA1toAA3.put( AA3_Val, AA1_Val);

		return mapAA1toAA3;
	}
	
	/**
	 * @param sequence
	 * @return
	 * 0：seq 出错则返回null
	 * 1：如果出错，记录出错位置
	 */
	static String[] reservecomInfo(String sequence) {
		String[] result = new String[2];
		CodeInfo.getCompMap();
		StringBuilder recomseq = new StringBuilder();
		int length = sequence.length();
		Character base;
		for (int i = length - 1; i >= 0; i--) {
			base = compmap.get(sequence.charAt(i));
			if (base == null) {
				result[1] = i + "";
				base = sequence.charAt(i);
			}
			recomseq.append(compmap.get(sequence.charAt(i)));
		}
		result[0] = recomseq.toString();
		return result;
	}
	
	/**
	 * 输入序列，互补对照表 获得反向互补序列
	 */
	public static String reservecom(String sequence) {
		String[] revSeq = reservecomInfo(sequence);
		return revSeq[0];
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
			 return null;
		 }
		 String aa;
		 if (AA1) {
			 aa = CodeInfo.getHashCode1().get(DNAcode);
			 if (aa == null) {
				 aa = "X";
			 }
		 }
		 else {
			 aa = CodeInfo.getHashCode3().get(DNAcode);
			 if (aa == null) {
				 aa = "Xxx";
			 }
		 }
		 return aa;
	}
	 /**
	  * 输入的是氨基酸，无所谓三字符还是单字符
	  * 比较两个氨基酸的化学性质，返回差异点，返回最大差异
	  * 譬如如果极性不同就返回极性
	  * 格式 polar --> nonpolar等
	  * 都一样则返回"";
	  */
	public static String cmpAAquality(String AA1, String AA2) {
		AA1 = getAAformate(AA1);
		AA2 = getAAformate(AA2);
		return compareAAquality(AA1, AA2);
	}

	/**
	 * 获得氨基酸的特性，极性，电荷等，按照genedoc的分类标准
	 * @return string[3]: 0：极性--带电荷--负电 1：
	 */
	public static String[] getAAquality(String AA) {
		AA = getAAformate(AA);
		return getHashAAquality().get(AA);
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
	public static String cmpAAqualityDNA(String DNAcode1, String DNAcode2) {
		String AA1 = CodeInfo.convertDNACode2AA(DNAcode1, true);
		String AA2 = CodeInfo.convertDNACode2AA(DNAcode2, true);
		return compareAAquality(AA1, AA2);
	}
	/**
	 * 将氨基酸在单字母和三字母之间转换
	 */
	public static String convertAA(String AA) {
		AA = getAAformate(AA);
		return setMapAA1toAA3().get(AA);
	}
	/**
	 * 输入格式不标准的AA，将其改成标准格式AA
	 * @param AA
	 * @return
	 */
	 private static String getAAformate(String AA) {
		 if (AA.trim().equals(CodeInfo.AA1_STOP) || AA.trim().equals(CodeInfo.AA3_STOP)) {
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
			 return null;
		 }
	 }
	 
	/**
	 * 输入的是氨基酸，必须是标准格式 比较两个氨基酸的化学性质，返回差异点，返回最大差异 譬如如果极性不同就返回极性<br>
	 * 格式 polar --> nonpolar等 都一样则返回"";
	 */
	private static String compareAAquality(String AA1, String AA2) {
		if (AA1.equals(AA2)) {
			return "same Amio Acid";
		}
		String[] aaInfo1 = CodeInfo.getHashAAquality().get(AA1);
		String[] aaInfo2 = CodeInfo.getHashAAquality().get(AA2);
		if (aaInfo1 == null || aaInfo2 == null) {
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
