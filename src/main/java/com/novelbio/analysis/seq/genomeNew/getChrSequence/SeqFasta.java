package com.novelbio.analysis.seq.genomeNew.getChrSequence;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.apache.velocity.app.event.ReferenceInsertionEventHandler.referenceInsertExecutor;

import com.novelbio.analysis.seq.reseq.SoapsnpInfo;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.PatternOperate;

/**
 * 本类专门用来装fasta文件的具体信息，的超类
 * 本类与Seq没有关系
 */
public class SeqFasta implements Cloneable {
	private static Logger logger = Logger.getLogger(SeqFasta.class);

	public static final int SEQ_UNKNOWN = 128;
	public static final int SEQ_PRO = 256;
	public static final int SEQ_DNA = 512;
	public static final int SEQ_RNA = 1024;

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
	 * 当用指定序列来插入或替换本序列中的位置时，如果插入的位置并不是很确定
	 * 譬如插入一段序列到 10-20上去，但是是否精确插入到10并不清楚，那么该区域再加上一段XXX用以标记
	 */
	private static final String SEP_SEQ = "XXXXXXX";
	
	
	protected String SeqName;
	protected String SeqSequence = "";
	/**
	 * 结果的文件是否转化为大小写 True：小写 False：大写 null：不变
	 * @return
	 */
	protected Boolean TOLOWCASE = null;


	/** 默认返回三字母长度的氨基酸 */
	boolean AA3Len = true;
	
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
	private static HashMap<String, String> getHashCode3() {
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

	private static HashMap<String, String> getHashCode1() {
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
	private static HashMap<String, String[]> getHashAAquality()
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
	private static HashMap<String, String> setMapAA1toAA3() {
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

	
	public SeqFasta(String seqName, String SeqSequence) {
		getCompMap();
		this.SeqName = seqName;
		this.SeqSequence = SeqSequence;
	}
	public SeqFasta(String SeqSequence) {
		getCompMap();
		this.SeqSequence = SeqSequence;
	}
	/**
	 * @param seqName
	 * @param SeqSequence
	 * @param cis5to3 仅仅标记一下，并不会反向序列
	 */
	public SeqFasta(String seqName, String SeqSequence, boolean cis5to3) {
		getCompMap();
		this.SeqName = seqName;
		if (cis5to3) {
			this.SeqSequence = SeqSequence;
		}
		else {
			this.SeqSequence = reservecomplement(SeqSequence);
		}
	}
	public SeqFasta() {
		getCompMap();
	}
	
	/**
	 * 默认返回三字母长度的氨基酸
	 */
	public void setAA3Len(boolean aA3Len) {
		AA3Len = aA3Len;
	}
	public void setTOLOWCASE(Boolean TOLOWCASE) {
		this.TOLOWCASE = TOLOWCASE;
	}
	/**
	 * 将RNA序列转化为DNA，也就是将U替换为T
	 */
	public void setDNA(boolean isDNAseq) {
		if (isDNAseq) {
			SeqSequence = SeqSequence.replace('u', 't').replace('U', 'T');
		}
	}
	/** 设定序列名 */
	public void setName(String SeqName) {
		 this.SeqName = SeqName;
	}
	/** 获得序列名 */
	public String getSeqName() {
		return SeqName;
	}
	/** 设定序列 */
	public void setSeq(String Seq) {
		 this.SeqSequence = Seq;
	}
	/**
	 * nr序列的长度
	 * @return
	 */
	public int getLength() {
		if (SeqSequence == null) {
			return 0;
		}
		return SeqSequence.length();
	}
	/**
	 * 给定左右的坐标，然后将seqfasta截短
	 * @param start 和substring一样的用法
	 * @param end 和substring一样的用法
	 * @return 返回截短后的string
	 */
	public SeqFasta trimSeq(int start, int end) {
		SeqFasta seqFasta = new SeqFasta();
		seqFasta.AA3Len = AA3Len;
		seqFasta.SeqName = SeqName;
		seqFasta.TOLOWCASE = TOLOWCASE;
		if (SeqSequence == null) {
			seqFasta.SeqSequence = SeqSequence;
			return seqFasta;
		}
		seqFasta.SeqSequence = SeqSequence.substring(start, end);
		return seqFasta;
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
	 * 输入序列坐标信息：序列名-序列起点和终点 返回序列
	 * @param startlocation 序列起点
	 * @param endlocation 序列终点
	 * @param cisseq序列正反向，蛋白序列就输true
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
	 * 如果位点超过了范围，那么修正位点
	 */
	private String getsequence(int startlocation, int endlocation) {
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
	 * 出错则返回null;
	 */
	private String reservecomplement(String sequence) {
		String[] revSeq = reservecomInfo(sequence);
		if (revSeq[1] != null) {
			logger.error(SeqName + " 含有未知碱基 " + revSeq[1]);
		}
		return revSeq[0];
	}
	/**
	 * 输入序列，互补对照表 获得反向互补序列
	 */
	public static String reservecom(String sequence) {
		String[] revSeq = reservecomInfo(sequence);
		return revSeq[0];
	}
	/**
	 * @param sequence
	 * @return
	 * 0：seq 出错则返回null
	 * 1：如果出错，记录出错位置
	 */
	private static String[] reservecomInfo(String sequence) {
		String[] result = new String[2];
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
				result[1] = i + "";
				break;
			}
		}
		//出错了result[1] 才会有记录
		if (result[1] == null) {
			result[0] = recomseq.toString();
		}
		return result;
	}
	/**
	 * 输入序列，互补对照表 获得反向互补序列
	 * 其中SeqName不变，cis5to3反向，序列反向互补
	 */
	public SeqFasta reservecom() {
		SeqFasta seqFasta = new SeqFasta();
		seqFasta.TOLOWCASE = TOLOWCASE;
		seqFasta.SeqName = SeqName;
		seqFasta.AA3Len = AA3Len;
		seqFasta.SeqSequence = reservecomplement(SeqSequence);
		return seqFasta;
	}
	
	/**
	 * 待测试
	 * 指定范围，然后用指定的序列去替换原来的序列
	 * @param start 要替换序列的起点，实际位点,并且包含该位点 如果start<= 0，则不考虑end，直接将序列插到最前面
	 * 如果start比序列长，则不考虑end，直接将序列插到最后面
	 * @param end 要替换序列的终点，实际位点,并且包含该位点，<br>
	 * 如果 start == end 那么就是将该点替换成指定序列<br>
	 * 如果 start > end 说明是插入紧挨着start位点之后<br>
	 * @param seq 要替换的序列
	 * @param boostart 替换序列的前部是否插入XXX true：插入
	 * @param booend 替换序列的后部是否插入XXX true：插入
	 */
	public void modifySeq(int start, int end, String seq, boolean boostart, boolean booend) {
		String startSeq = "";
		String endSeq = "";
		String FinalSeq = null;
		if (boostart)
			startSeq = SEP_SEQ;
		if (booend)
			endSeq = SEP_SEQ;

		if (start <= 0) {
			SeqSequence = startSeq + seq.toUpperCase() + endSeq + SeqSequence;
			return;
		}
		if (start >= SeqSequence.length() + 1) {
			SeqSequence = SeqSequence + startSeq + seq.toUpperCase() + endSeq;
			return;
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
		else if (start > end){
			end = start;
		}
		FinalSeq = SeqSequence.substring(0, start) + startSeq + seq.toUpperCase() + endSeq + SeqSequence.substring(end);
		SeqSequence = FinalSeq;
	}
	
	/**
	 * 指定snp位点，实际位置，从1开始，然后用指定的序列去替换原来的序列
	 */
	public void modifySeq(int snpSite, char replace) {
		snpSite--;
		char[] chrSeq = SeqSequence.toCharArray();
		chrSeq[snpSite] = replace;
		String FinalSeq = String.copyValueOf(chrSeq);
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
	 * 统计序列中小写序列，N的数量以及X的数量等
	 */
	public ArrayList<LocInfo> getSeqInfo() {
		StatisticSeqInfo statisticSeqInfo = new StatisticSeqInfo();
		statisticSeqInfo.setSeq(SeqSequence);
		statisticSeqInfo.statistics();
		return statisticSeqInfo.getLsSeqInfo();
	}
	/**@return 将nr序列转变为单字母aa序列，首先正反向之后，然后按照该顺序进行orf选择 */
	public String toStringAA() {
		return toStringAA(true, 0, true);
	}
	/**
	 * @param AAnum true 单字母AA，false 三字母AA
	 * @return 将nr序列转变为aa序列，首先正反向之后，然后按照该顺序进行orf选择 
	 */
	public String toStringAA(boolean AAnum) {
		return toStringAA(true, 0, AAnum);
	}
	/**
	 * 将nr序列转变为单字母aa序列，首先正反向之后，然后按照该顺序进行orf选择
	 * @param cis 是正向 false：反向互补
	 * @param orf 第几个orf，0，1，2
	 * @return
	 */
	public String toStringAA(boolean cis,int orf) {
		return toStringAA(true, 0, true);
	}
	/**
	 * 将nr序列转变为aa序列，首先正反向之后，然后按照该顺序进行orf选择
	 * @param cis 是正向 false：反向互补
	 * @param orf 第几个orf，0，1，2
	 * @param AAnum true 单字母AA，false 三字母AA
	 * @return
	 */
	public String toStringAA(boolean cis,int orf, boolean AAnum) {
		if (SeqSequence == null) {
			return "";
		}
		char[] nrChar = null;
		if (!cis) {
			nrChar = reservecom(SeqSequence).toCharArray();
		}
		else {
			nrChar = SeqSequence.toCharArray();
		}
		StringBuilder resultAA = new StringBuilder();
		for (int i = orf; i <= nrChar.length - 3; i = i+3) {
			String tmp = String.valueOf(new char[]{nrChar[i],nrChar[i+1],nrChar[i+2]});
			resultAA.append(convertDNACode2AA(tmp, AAnum));
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
	 * 可能不能精确到单碱基<b>同时搜索正反向</b>
	 * 给定motif，在序列上查找相应的正则表达式<br>
	 * 返回正向序列和反向序列查找的结果<br>
	 * List-string [4] <br>
	 * 0: seqName<br>
	 * 1: strand : + / -<br>
	 * 2: 具体的motif序列<br>
	 * 3: motif最后一个碱基与本序列site点的距离
	 * @param regex
	 * @param site 距离该序列终点的位置，上游为负数，下游为正数。譬如该位点为tss，并且tss距离seq终点500bp，则site为-500。
	 * 也就是序列取到tss下游500bp。
	 * 最后返回motif到site点，那么<b>负数</b>表示motif在site的上游，<b>正数</b>表示motif在site的下游
	 * @return
	 */
	public ArrayList<String[]> getMotifScanResult(String regex, int site) {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		ArrayList<String[]> lsTmpResultFor = PatternOperate.getPatLoc(toString(), regex, false);
		ArrayList<String[]> lsTmpResultRev = PatternOperate.getPatLoc(reservecom().toString(), regex, false);

		copeMotifResultToList(true, lsTmpResultFor, site, lsResult);
		copeMotifResultToList(false, lsTmpResultRev, site, lsResult);
		
		return lsResult;
	}
	/**
	 * @param strand 序列方向
	 * @param lsTmpResult 找到的motif信息，来自PatternOperate.getPatLoc()方法
	 * 最后返回motif到site点，那么<b>负数</b>表示motif在site的上游，<b>正数</b>表示motif在site的下游
	 * @param site 距离该序列终点的位置，上游为负数，下游为正数。譬如tss距离seq终点500bp，则site为-500。
	 * 也就是序列取到tss下游500bp。
	 * @param lsResult 返回的list
	 */
	private void copeMotifResultToList(boolean strand, ArrayList<String[]> lsTmpResultRev, int site, ArrayList<String[]> lsResult) {
		if (lsTmpResultRev != null && lsTmpResultRev.size() > 0) {
			for (String[] strings : lsTmpResultRev) {
				String[] tmpResult = new String[4];
				tmpResult[0] = getSeqName();
				if (strand)
					tmpResult[1] = "+";
				else
					tmpResult[1] = "-";

				tmpResult[2] = strings[0];//具体的motif序列
				
				if (site != 0)
					tmpResult[3] = (Integer.parseInt(strings[1]) + site) * -1 + "";
				else
					tmpResult[3] = strings[1];
				
				lsResult.add(tmpResult);
			}
		}
	}
	/**
	 * 判断该序列是DNA，RNA，还是蛋白，或者也不知道是什么
	 * @return
	 * SeqFasta.SEQ_DNA等
	 */
	public int getSeqType() {
		int len = 2000;
		if (len > getLength()) {
			len = getLength() - 1;
		}
		char[] chr = SeqSequence.substring(0, len).toCharArray();
		int num = 0;
		boolean flagFindU = false;
		for (char c : chr) {
			if (c == 'u' || c == 'U')
				flagFindU = true;
			
			if (getCompMap().containsKey(c))
				continue;
			else
				num ++ ;
		}
		if (num == 0) {
			if (flagFindU) return SEQ_RNA;
			
			return SEQ_DNA;
		}
		else if ((double)num/getLength() < 0.1) {
			return SEQ_UNKNOWN;
		}
		else {
			return SEQ_PRO;
		}
	}
	/** 根据TOLOWCASE返回序列 */
	public String toString() {
		if (SeqSequence == null) {
			return "";
		}
		if (TOLOWCASE == null) {
			return SeqSequence;
		}
		else {
			return TOLOWCASE.equals(true) ?  SeqSequence.toLowerCase() :  SeqSequence.toUpperCase();
		}
	}
	/** 返回AA的fasta序列 */
	public String toStringAAfasta() {
		return ">" + SeqName + TxtReadandWrite.ENTER_LINUX + toStringAA(true, 0);
	}
	/** 返回Nr的fasta序列 */
	public String toStringNRfasta() {
		return ">" + SeqName + TxtReadandWrite.ENTER_LINUX + toString();
	}
	/** 克隆序列 */
	public SeqFasta clone() {
		SeqFasta seqFasta = null;
		try {
			seqFasta = (SeqFasta) super.clone();
			seqFasta.SeqName = SeqName;
			seqFasta.AA3Len = AA3Len;
			seqFasta.SeqSequence = SeqSequence;
			seqFasta.TOLOWCASE = TOLOWCASE;
			return seqFasta;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return seqFasta;
	}
	////////////////////// static 方法 //////////////////////////////////////
	/**
	 * 当查找完motif后，获得motif的titile
	 * @return
	 */
	public static String[] getMotifScanTitle() {
		String[] title = new String[]{"SeqName","Strand","MotifSeq","Distance2SeqEnd"};
		return title;
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
	 * 输入格式不标准的AA，将其改成标准格式AA
	 * @param AA
	 * @return
	 */
	private static String getAAformate(String AA) {
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
		return setMapAA1toAA3().get(AA);
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
	public static String cmpAAquality(String AA1, String AA2) {
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
	private static String compareAAquality(String AA1, String AA2) {
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
/**
 * 统计序列中有多少是NN，有多少是可能的gap，有多少是边界不清楚
 * 有多少大写，多少小写
 * @author zong0jie
 *
 */
class StatisticSeqInfo {
	//string0: flag string1: location string2:endLoc
	ArrayList<LocInfo> lsResult = new ArrayList<LocInfo>();
	char[] seq;
	boolean flagBound = false; //边界模糊标记，XX
	boolean flagGap = false; //gap标记，小写
	boolean flagAmbitious = false; //不确定碱基标记，NNN
	int bound = 0; int gap = 0; int ambitious = 0;
	int startBound = 0; int startGap = 0; int startAmbitious = 0;
	
	/**
	 * 设定要统计的序列
	 * @param seq
	 */
	public void setSeq(char[] seq) {
		this.seq = seq;
	}
	/**
	 * 设定要统计的序列
	 * @param seq
	 */
	public void setSeq(String seq) {
		this.seq = seq.toCharArray();
	}
	/**
	 * 获得结果
	 * @return
	 */
	public ArrayList<LocInfo> getLsSeqInfo() {
		return lsResult;
	}
	public void statistics() {
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
	}
	/**
	 * @param lsInfo
	 * @param info
	 * @param start 内部会加上1
	 * @param length
	 */
	private void addList(ArrayList<LocInfo> lsInfo, String info, int start, int length) {
		LocInfo locInfo = new LocInfo(info, "", start, start+length-1, true);
		lsInfo.add(locInfo);
	}
}
