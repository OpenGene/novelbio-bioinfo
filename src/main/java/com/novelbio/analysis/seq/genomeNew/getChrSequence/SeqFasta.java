package com.novelbio.analysis.seq.genomeNew.getChrSequence;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.ibatis.migration.commands.NewCommand;
import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.reseq.SoapsnpInfo;
import com.novelbio.base.dataStructure.PatternOperate;

/**
 * 本类专门用来装fasta文件的具体信息，的超类
 * 本类与Seq没有关系
 */
public class SeqFasta {
	private String SeqName;
	private String SeqSequence;
	private static Logger logger = Logger.getLogger(SeqFasta.class);
	private boolean cis5to3 = true;
	/**
	 * 结果的文件是否转化为大小写 True：小写 False：大写 null：不变
	 * @return
	 */
	private Boolean TOLOWCASE = null;
	public void setTOLOWCASE(Boolean TOLOWCASE) {
		this.TOLOWCASE = TOLOWCASE;
	}
	/**
	 * nr序列的长度
	 * @return
	 */
	public int length() {
		return SeqSequence.length();
	}
	
	public void setCis5to3(boolean cis5to3) {
		this.cis5to3 = cis5to3;
	}
	/**
	 * 反向互补哈希表
	 */
	private static HashMap<Character, Character> compmap = null;// 碱基翻译哈希表

	/**
	 * 获得互补配对hash表
	 * 给碱基对照哈希表赋值 目前有A-T， G-C，N-N 的对应关系（包括了大小写的对应） 将来可能要添加新的
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
		compmap.put(Character.valueOf('G'), Character.valueOf('C'));
		compmap.put(Character.valueOf('g'), Character.valueOf('c'));
		compmap.put(Character.valueOf('C'), Character.valueOf('G'));
		compmap.put(Character.valueOf('c'), Character.valueOf('g'));
		compmap.put(Character.valueOf(' '), Character.valueOf(' '));
		compmap.put(Character.valueOf('N'), Character.valueOf('N'));
		compmap.put(Character.valueOf('n'), Character.valueOf('n'));
		compmap.put(Character.valueOf('-'), Character.valueOf('-'));
		compmap.put(Character.valueOf('\n'), Character.valueOf(' '));
		compmap.put(Character.valueOf('X'), Character.valueOf('X'));
		return compmap;
	}
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
	/**
	 * 默认返回三字母长度的氨基酸
	 */
	boolean AA3Len = true;
	/**
	 * 默认返回三字母长度的氨基酸
	 */
	public void setAA3Len(boolean aA3Len) {
		AA3Len = aA3Len;
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
	/**
	 * 当用指定序列来插入或替换本序列中的位置时，如果插入的位置并不是很确定
	 * 譬如插入一段序列到 10-20上去，但是是否精确插入到10并不清楚，那么该区域再加上一段XXX用以标记
	 */
	private static final String SEP_SEQ = "XXXXXXX";
	
	public SeqFasta(String seqName, String SeqSequence)
	{
		getCompMap();
		this.SeqName = seqName;
		this.SeqSequence = SeqSequence;
	}
	public SeqFasta(String seqName, String SeqSequence, boolean cis5to3)
	{
		getCompMap();
		this.SeqName = seqName;
		
		this.cis5to3 = cis5to3;
		if (cis5to3) {
			this.SeqSequence = SeqSequence;
		}
		else {
			this.SeqSequence = reservecom(SeqSequence);
		}
	}
	
	protected SeqFasta() {
		getCompMap();
	}
	/**
	 * 本序列在生成的时候是正向还是反向，实际没有影响
	 * @return
	 */
	public boolean isCis5to3() {
		return cis5to3;
	};
	/**
	 * 设定序列名
	 */
	public void setSeqName(String SeqName) {
		 this.SeqName = SeqName;
	}
	/**
	 * 获得序列名
	 */
	public String getSeqName() {
		return SeqName;
	}
	
	/**
	 * 设定序列
	 */
	protected void setSeq(String Seq) {
		 this.SeqSequence = Seq;
	}
	
	/**
	 * 获得具体序列的反向互补序列
	 */
	public SeqFasta getSeqRC2() {
		String seq = reservecomplement(SeqSequence);
		SeqFasta seqFasta = new SeqFasta(SeqName, seq);
		seqFasta.cis5to3 = !cis5to3;
		return seqFasta;
	}
	/**
	 * 输入序列坐标信息：序列名-序列起点和终点 返回序列
	 * 
	 * @param hashSeq
	 *            序列的哈希表，键为序列名称，值为具体序列
	 * @param chr
	 *            序列参数之序列名，用来在哈希表中查找具体某条序列
	 * @param startlocation
	 *            序列起点
	 * @param endlocation
	 *            序列终点
	 * @param cisseq序列正反向
	 *            ，蛋白序列就输true
	 */
	public SeqFasta getSubSeq(int startlocation, int endlocation, boolean cisseq) {
		String sequence = getsequence(startlocation, endlocation);
		SeqFasta seqFasta = new SeqFasta(SeqName, sequence, cisseq);
		seqFasta.TOLOWCASE = TOLOWCASE;
		seqFasta.AA3Len = AA3Len;
		return seqFasta;
	}

	/**
	 * 输入序列坐标，起点和终点 返回序列
	 */
	private String getsequence(int startlocation, int endlocation) {
		/**
		 * 如果位点超过了范围，那么修正位点
		 */
		int length = SeqSequence.length();
		if (startlocation < 1 || startlocation >= length || endlocation < 1
				|| endlocation >= length) {
			logger.error("序列坐标错误 "+SeqName+" "+startlocation+" "+endlocation);
			return "序列坐标错误 "+SeqName+" "+startlocation+" "+endlocation;
		}

		if (endlocation <= startlocation) {
			logger.error("序列坐标错误 "+SeqName+" "+startlocation+" "+endlocation);
			return "序列坐标错误 "+SeqName+" "+startlocation+" "+endlocation;
		}
		
		if (endlocation - startlocation > 20000) {
			logger.error("最多提取20000bp "+SeqName+" "+startlocation+" "+endlocation);
			return "最多提取20000bp"+SeqName+" "+startlocation+" "+endlocation;
		}
		return SeqSequence.substring(startlocation - 1, endlocation);// substring方法返回找到的序列
	}

	/**
	 * 输入序列，互补对照表 获得反向互补序列
	 */
	private String reservecomplement(String sequence) {
		if (compmap == null) {
			getCompMap();
		}
		StringBuilder recomseq = new StringBuilder();
		int length = sequence.length();
		Character base;
		for (int i = length - 1; i >= 0; i--) {
			base = compmap.get(sequence.charAt(i));
			if (base != null) {
				recomseq.append(compmap.get(sequence.charAt(i)));
			} else {
				logger.error(SeqName + " 含有未知碱基 " + sequence.charAt(i));
				return SeqName + "含有未知碱基 " + sequence.charAt(i);
			}
		}
		return recomseq.toString();
	}

	/**
	 * 输入序列，互补对照表 获得反向互补序列
	 */
	public static String reservecom(String sequence) {
		if (compmap == null) {
			getCompMap();
		}
		StringBuilder recomseq = new StringBuilder();
		int length = sequence.length();
		Character base;
		for (int i = length - 1; i >= 0; i--) {
			base = compmap.get(sequence.charAt(i));
			if (base != null) {
				recomseq.append(compmap.get(sequence.charAt(i)));
			} else {
				return "第"+ i+ "位含有未知碱基: " + sequence.charAt(i);
			}
		}
		return recomseq.toString();
	}
	
	/**
	 * 输入序列，互补对照表 获得反向互补序列
	 * 其中SeqName不变，cis5to3反向，序列反向互补
	 */
	public SeqFasta reservecom() {
		SeqFasta seqFasta = new SeqFasta();
		seqFasta.TOLOWCASE = TOLOWCASE;
		seqFasta.SeqName = SeqName;
		seqFasta.cis5to3 = !cis5to3;
		seqFasta.AA3Len = AA3Len;
		seqFasta.SeqSequence = reservecom(SeqSequence);
		return seqFasta;
	}
	
	/**
	 * 待测试
	 * 指定范围，然后用指定的序列去替换原来的序列
	 * @param start 要替换序列的起点，实际位点,并且包含该位点
	 * @param end 要替换序列的终点，实际位点,并且包含该位点，<br>
	 * 如果end<0，说明是插入紧挨着start位点之后<br>
	 * 如果 start == end 那么就是将该点替换成指定序列<br>
	 * 如果 start > end && end >0 说明出错
	 * @param seq 要替换的序列
	 * @param boostart 替换序列的前部是否有问题
	 * @param booend 替换序列的后部是否有问题
	 */
	public void modifySeq(int start, int end, String seq,boolean boostart, boolean booend) {
		String startSeq = "";
		String endSeq = "";
		if (!boostart) {
			startSeq = SEP_SEQ;
		}
		if (!booend) {
			endSeq = SEP_SEQ;
		}
		
		if (start < end) {
			start --;
		}
		else if (start == end) {
			if (seq.length() == 1) {
				modifySeq(start, seq.charAt(0));
				return;
			}
			start --;
			
		}
		else if (end < 0){
			end = start;
		}
		else if (start > end && end > 0) {//插入的序列横跨了，这个在外面处理
			logger.error("start < end: "+ start + " "+ end);
		}
		
		String FinalSeq = SeqSequence.substring(0, start) + startSeq + seq.toUpperCase() + endSeq + SeqSequence.substring(end);
		SeqSequence = FinalSeq;
	}
	
	/**
	 * 指定snp位点，实际位置，从1开始，然后用指定的序列去替换原来的序列
	 */
	public void modifySeq(int snpSite, char replace) {
		snpSite--;
		char[] chrSeq = SeqSequence.toCharArray();
		chrSeq[snpSite] = replace;
		String FinalSeq = chrSeq.toString();
		SeqSequence = FinalSeq;
	}
	
	/**
	 * 指定snp位点，实际位置，从1开始，然后用指定的序列去替换原来的序列
	 */
	public void modifySeq(ArrayList<SoapsnpInfo> lsSoapsnpInfos) {
		char[] chrSeq = SeqSequence.toCharArray();
		for (SoapsnpInfo soapsnpInfo : lsSoapsnpInfos) {
			chrSeq[soapsnpInfo.getStart()-1] = soapsnpInfo.getBestBase();
		}
		String FinalSeq = String.copyValueOf(chrSeq);
		SeqSequence = FinalSeq;
	}
	/**
	 * 根据TOLOWCASE返回序列
	 */
	public String toString()
	{
		if (SeqSequence == null) {
			return null;
		}
		if (TOLOWCASE == null) {
			return SeqSequence;
		}
		else {
			return TOLOWCASE.equals(true) ?  SeqSequence.toLowerCase() :  SeqSequence.toUpperCase();
		}
	}
	/**
	 * 返回AA的fasta序列
	 */
	public String toStringAAfasta()
	{
		return ">" + SeqName + "\r\n" + toStringAA(true, 0);
	}
	/**
	 * 返回Nr的fasta序列
	 */
	public String toStringNRfasta()
	{
		return ">" + SeqName + "\r\n" + toString();
	}
	/**
	 * 统计序列中小写序列，N的数量以及X的数量等
	 */
	public ArrayList<LocInfo> getSeqInfo()
	{
		//string0: flag string1: location string2:endLoc
		ArrayList<LocInfo> lsResult = new ArrayList<LocInfo>();
		
		
		char[] seq = SeqSequence.toCharArray();
		boolean flagBound = false; //边界模糊标记，XX
		boolean flagGap = false; //gap标记，小写
		boolean flagAmbitious = false; //不确定碱基标记，NNN
		int bound = 0; int gap = 0; int ambitious = 0;
		int startBound = 0; int startGap = 0; int startAmbitious = 0;
		for (int i = 0; i < seq.length; i++) {
			if (seq[i] < 'a' && seq[i] != 'X' && seq[i] != 'N') {
				if (flagAmbitious) {
					addList(lsResult, "ambitious", startAmbitious, ambitious);
					flagAmbitious = false; //不确定碱基标记，NNN
				}
				if (flagGap) {
					addList(lsResult, "gap", startGap, gap);
					flagGap = false; //gap标记，小写
				}
				if (flagBound) {
					addList(lsResult, "bound", startBound, bound);
					flagBound = false; //边界模糊标记，XX
				}
			}
			else if (seq[i] == 'X' ) {
				if (flagAmbitious) {
					addList(lsResult, "ambitious", startAmbitious, ambitious);
					flagAmbitious = false; //不确定碱基标记，NNN
				}
				if (flagGap) {
					addList(lsResult, "gap", startGap, gap);
					flagGap = false; //gap标记，小写
				}
				if (flagBound) {
					bound ++;
				}
				else {
					flagBound = true;
					bound = 0;
					startBound = i;
				}
			} 
			else if (seq[i] == 'N') {
				if (flagAmbitious) {
					ambitious ++;
				}
				else {
					flagAmbitious = true;
					ambitious = 0;
					startAmbitious = i;
				}
				if (flagGap) {
					addList(lsResult, "gap", startGap, gap);
					flagGap = false; // gap标记，小写
				}
				if (flagBound) {
					addList(lsResult, "bound",startBound, bound);
					flagBound = false; // 边界模糊标记，XX
				}
			}
			else if (seq[i] >= 'a') {
				System.out.println("i");
				if (flagAmbitious) {
					addList(lsResult, "ambitious", startAmbitious, ambitious);
					flagAmbitious = false; //不确定碱基标记，NNN
				}
				if (flagGap) {
					gap ++;
				}
				else {
					flagGap = true;
					gap = 0;
					startGap = i;
				}
				if (flagBound) {
					addList(lsResult, "bound", startBound, bound);
					flagBound = false; //边界模糊标记，XX
				}
			}
		}
		return lsResult;
	}
	/**
	 * 
	 * @param lsInfo
	 * @param info
	 * @param start 内部会加上1
	 * @param length
	 */
	private void addList(ArrayList<LocInfo> lsInfo, String info, int start, int length) {
		LocInfo locInfo = new LocInfo(info, "", start, start+length-1, true);
		lsInfo.add(locInfo);
	}
	
	
	/**
	 * 比较两个序列是否一致，计数不一致的碱基数
	 * 从头开始比较，头尾可以有空格，中间不能有。不是blast模式的比较
	 */
	public static int compare2Seq(String seq1, String seq2) {
		char[] chrSeq1 = seq1.trim().toLowerCase().toCharArray();
		char[] chrSeq2 = seq2.trim().toLowerCase().toCharArray();
		int result = 0;
		int i = Math.min(chrSeq1.length, chrSeq2.length);
		for (int j = 0; j < i; j++) {
			if (chrSeq1[j] != chrSeq2[j]) {
				result ++ ;
			}
		}
		result = result + Math.max(chrSeq1.length, chrSeq2.length) - i;
		return result;
	}
	/**
	 * 将nr序列转变为aa序列，首先正反向之后，然后按照该顺序进行orf选择
	 * @param cis 是正向 false：反向互补
	 * @param orf 第几个orf，0，1，2
	 * @return
	 */
	public String toStringAA(boolean cis,int orf) {
		String seq = "";
		if (!cis) {
			seq = reservecom(SeqSequence);
		}
		else {
			seq = SeqSequence;
		}
		char[] nrChar = seq.toCharArray();
		StringBuilder resultAA = new StringBuilder();
		for (int i = orf; i < nrChar.length - 3; i = i+3) {
			String tmp = String.valueOf(new char[]{nrChar[i],nrChar[i+1],nrChar[i+2]});
			resultAA.append(AminoAcid.convertDNACode2AA(tmp, true));
		}
		return resultAA.toString();
	}
	/**
	 * 给定motif，在序列上查找相应的正则表达式<br>
	 * 返回正向序列和反向序列查找的结果<br>
	 * List-string [4] <br>
	 * 0: seqName<br>
	 * 1: strand : + / -<br>
	 * 2: 具体的motif序列<br>
	 * 3: motif最后一个碱基与本序列终点的距离
	 * @param regex
	 * @return
	 */
	public ArrayList<String[]> getMotifScanResult(String regex) {
		return getMotifScanResult(regex,0);
	}
	
	/**
	 * 可能不能精确到单碱基
	 * 给定motif，在序列上查找相应的正则表达式<br>
	 * 返回正向序列和反向序列查找的结果<br>
	 * List-string [4] <br>
	 * 0: seqName<br>
	 * 1: strand : + / -<br>
	 * 2: 具体的motif序列<br>
	 * 3: motif最后一个碱基与本序列site点的距离
	 * @param regex
	 * @param site 距离该序列终点的位置，上游为负数，下游为正数。譬如tss距离seq终点500bp，则site为-500。
	 * 也就是序列取到tss下游500bp。
	 * 最后返回motif到site点，那么<b>负数</b>表示motif在site的上游，<b>正数</b>表示motif在site的下游
	 * @return
	 */
	public ArrayList<String[]> getMotifScanResult(String regex, int site) {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		ArrayList<String[]> lsTmpResultFor = PatternOperate.getPatLoc(toString(), regex, false);
		if (lsTmpResultFor != null && lsTmpResultFor.size() > 0) {
			for (String[] strings : lsTmpResultFor) {
				String[] tmpResult = new String[4];
				tmpResult[0] = getSeqName();
				tmpResult[1] = "+";
				tmpResult[2] = strings[0];
				if (site != 0)
					tmpResult[3] = (Integer.parseInt(strings[2]) + site) * -1 + "";
				else 
					tmpResult[3] = strings[2];
				
				lsResult.add(tmpResult);
			}
		}
		ArrayList<String[]> lsTmpResultRev = PatternOperate.getPatLoc(reservecom().toString(), regex, false);
		if (lsTmpResultRev != null && lsTmpResultRev.size() > 0) {
			for (String[] strings : lsTmpResultRev) {
				String[] tmpResult = new String[4];
				tmpResult[0] = getSeqName();
				tmpResult[1] = "-";
				tmpResult[2] = strings[0];
				if (site != 0)
					tmpResult[3] = (Integer.parseInt(strings[1]) + site) * -1 + "";
				else
					tmpResult[3] = strings[1];
				
				lsResult.add(tmpResult);
			}
		}
		return lsResult;
	}
	
	public static String[] getMotifScanTitle()
	{
		String[] title = new String[]{"SeqName","Strand","MotifSeq","Distance2SeqEnd"};
		return title;
	}
	
	public SeqFasta clone() {
		SeqFasta seqFasta = new SeqFasta(SeqName, SeqSequence);
		seqFasta.cis5to3 = cis5to3;
		return seqFasta;
	}
}
