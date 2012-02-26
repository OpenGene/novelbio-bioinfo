package com.novelbio.analysis.seq.genomeNew.getChrSequence;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.ibatis.migration.commands.NewCommand;
import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.reseq.SoapsnpInfo;
import com.novelbio.base.dataStructure.PatternOperate;

/**
 * ����ר������װfasta�ļ��ľ�����Ϣ���ĳ���
 * ������Seqû�й�ϵ
 */
public class SeqFasta {
	private String SeqName;
	private String SeqSequence;
	private static Logger logger = Logger.getLogger(SeqFasta.class);
	private boolean cis5to3 = true;
	/**
	 * ������ļ��Ƿ�ת��Ϊ��Сд True��Сд False����д null������
	 * @return
	 */
	private Boolean TOLOWCASE = null;
	public void setTOLOWCASE(Boolean TOLOWCASE) {
		this.TOLOWCASE = TOLOWCASE;
	}
	/**
	 * nr���еĳ���
	 * @return
	 */
	public int length() {
		return SeqSequence.length();
	}
	
	public void setCis5to3(boolean cis5to3) {
		this.cis5to3 = cis5to3;
	}
	/**
	 * ���򻥲���ϣ��
	 */
	private static HashMap<Character, Character> compmap = null;// ��������ϣ��

	/**
	 * ��û������hash��
	 * ��������չ�ϣ��ֵ Ŀǰ��A-T�� G-C��N-N �Ķ�Ӧ��ϵ�������˴�Сд�Ķ�Ӧ�� ��������Ҫ����µ�
	 */
	public static HashMap<Character, Character> getCompMap() {
		if (compmap != null) {
			return compmap;
		}
		compmap = new HashMap<Character, Character>();// ��������ϣ��
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
	 * ��X��ʾ
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
	 * ��X��ʾ
	 */
	public static final String AA1_STOP = "*";
 
	/**
	 * ���������ӵ���������
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
	 * ���������ӵ���������
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
	 * �������ʱ�
	 */
	static HashMap<String, String[]> hashAAquality = null;
	/**
	 * �������ʱ�
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
	 * ����һλ����λ���ת��
	 */
	static HashMap<String, String> hashAAchange = null;
	/**
	 * ����һλ����λ���ת��
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
	 * Ĭ�Ϸ�������ĸ���ȵİ�����
	 */
	boolean AA3Len = true;
	/**
	 * Ĭ�Ϸ�������ĸ���ȵİ�����
	 */
	public void setAA3Len(boolean aA3Len) {
		AA3Len = aA3Len;
	}
	/**
	 * ָ�����������ӣ�����ת��Ϊ���ױ���
	 * @param DNAcode
	 * @param AA1 �Ƿ�ת��Ϊ����ĸAA��falseת��Ϊ3��ĸAA
	 * @return
	 * null ��ʾû���ҵ���˵���������������
	 */
	 public static String convertDNACode2AA(String DNAcode, boolean AA1) {
		 DNAcode = DNAcode.trim().toUpperCase();
		if (DNAcode.length() != 3) {
			logger.error("DNA�������볤�Ȳ��ԣ�" + DNAcode);
		}
		if (AA1) {
			return getHashCode1().get(DNAcode);
		}
		else {
			return getHashCode3().get(DNAcode);
		}
	}
	
	
	/**
	 * �����ʽ����׼��AA������ĳɱ�׼��ʽAA
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
	 * ��ð���������ԣ����ԣ���ɵȣ�����genedoc�ķ����׼
	 * @return
	 * string[3]: 0������--�����--����
	 * 1��
	 */
	public static String[] getAAquality(String AA) {
		AA = getAAformate(AA);
		return getHashAAquality().get(AA);
	}
	/**
	 * ���������ڵ���ĸ������ĸ֮��ת��
	 */
	public static String convertAA(String AA) {
		AA = getAAformate(AA);
		return getHashAAchange().get(AA);
	}
	/**
	 * 
	 * �������DNA����������
	 * �Ƚ�����������Ļ�ѧ���ʣ����ز���㣬����������
	 * Ʃ��������Բ�ͬ�ͷ��ؼ���
	 * ��ʽ polar --> nonpolar��
	 * ��һ���򷵻�"";
	 * @param DNAcode1 ��һ��DNA����
	 * @param DNAcode2 �ڶ���DNA����
	 * @return
	 */
	public static String cmpAAqualityDNA(String DNAcode1, String DNAcode2)
	{
		String AA1 = convertDNACode2AA(DNAcode1, true);
		String AA2 = convertDNACode2AA(DNAcode2, true);
		return compareAAquality(AA1, AA2);
	}
	
	/**
	 * ������ǰ����ᣬ����ν���ַ����ǵ��ַ�
	 * �Ƚ�����������Ļ�ѧ���ʣ����ز���㣬����������
	 * Ʃ��������Բ�ͬ�ͷ��ؼ���
	 * ��ʽ polar --> nonpolar��
	 * ��һ���򷵻�"";
	 */
	public static String cmpAAquality(String AA1, String AA2)
	{
		AA1 = getAAformate(AA1);
		AA2 = getAAformate(AA2);
		return compareAAquality(AA1, AA2);
	}
	/**
	 * ������ǰ����ᣬ�����Ǳ�׼��ʽ
	 * �Ƚ�����������Ļ�ѧ���ʣ����ز���㣬����������
	 * Ʃ��������Բ�ͬ�ͷ��ؼ���
	 * ��ʽ polar --> nonpolar��
	 * ��һ���򷵻�"";
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
	 * ����ָ��������������滻�������е�λ��ʱ����������λ�ò����Ǻ�ȷ��
	 * Ʃ�����һ�����е� 10-20��ȥ�������Ƿ�ȷ���뵽10�����������ô�������ټ���һ��XXX���Ա��
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
	 * �����������ɵ�ʱ���������Ƿ���ʵ��û��Ӱ��
	 * @return
	 */
	public boolean isCis5to3() {
		return cis5to3;
	};
	/**
	 * �趨������
	 */
	public void setSeqName(String SeqName) {
		 this.SeqName = SeqName;
	}
	/**
	 * ���������
	 */
	public String getSeqName() {
		return SeqName;
	}
	
	/**
	 * �趨����
	 */
	protected void setSeq(String Seq) {
		 this.SeqSequence = Seq;
	}
	
	/**
	 * ��þ������еķ��򻥲�����
	 */
	public SeqFasta getSeqRC2() {
		String seq = reservecomplement(SeqSequence);
		SeqFasta seqFasta = new SeqFasta(SeqName, seq);
		seqFasta.cis5to3 = !cis5to3;
		return seqFasta;
	}
	/**
	 * ��������������Ϣ��������-���������յ� ��������
	 * 
	 * @param hashSeq
	 *            ���еĹ�ϣ����Ϊ�������ƣ�ֵΪ��������
	 * @param chr
	 *            ���в���֮�������������ڹ�ϣ���в��Ҿ���ĳ������
	 * @param startlocation
	 *            �������
	 * @param endlocation
	 *            �����յ�
	 * @param cisseq����������
	 *            ���������о���true
	 */
	public SeqFasta getSubSeq(int startlocation, int endlocation, boolean cisseq) {
		String sequence = getsequence(startlocation, endlocation);
		SeqFasta seqFasta = new SeqFasta(SeqName, sequence, cisseq);
		seqFasta.TOLOWCASE = TOLOWCASE;
		seqFasta.AA3Len = AA3Len;
		return seqFasta;
	}

	/**
	 * �����������꣬�����յ� ��������
	 */
	private String getsequence(int startlocation, int endlocation) {
		/**
		 * ���λ�㳬���˷�Χ����ô����λ��
		 */
		int length = SeqSequence.length();
		if (startlocation < 1 || startlocation >= length || endlocation < 1
				|| endlocation >= length) {
			logger.error("����������� "+SeqName+" "+startlocation+" "+endlocation);
			return "����������� "+SeqName+" "+startlocation+" "+endlocation;
		}

		if (endlocation <= startlocation) {
			logger.error("����������� "+SeqName+" "+startlocation+" "+endlocation);
			return "����������� "+SeqName+" "+startlocation+" "+endlocation;
		}
		
		if (endlocation - startlocation > 20000) {
			logger.error("�����ȡ20000bp "+SeqName+" "+startlocation+" "+endlocation);
			return "�����ȡ20000bp"+SeqName+" "+startlocation+" "+endlocation;
		}
		return SeqSequence.substring(startlocation - 1, endlocation);// substring���������ҵ�������
	}

	/**
	 * �������У��������ձ� ��÷��򻥲�����
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
				logger.error(SeqName + " ����δ֪��� " + sequence.charAt(i));
				return SeqName + "����δ֪��� " + sequence.charAt(i);
			}
		}
		return recomseq.toString();
	}

	/**
	 * �������У��������ձ� ��÷��򻥲�����
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
				return "��"+ i+ "λ����δ֪���: " + sequence.charAt(i);
			}
		}
		return recomseq.toString();
	}
	
	/**
	 * �������У��������ձ� ��÷��򻥲�����
	 * ����SeqName���䣬cis5to3�������з��򻥲�
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
	 * ������
	 * ָ����Χ��Ȼ����ָ��������ȥ�滻ԭ��������
	 * @param start Ҫ�滻���е���㣬ʵ��λ��,���Ұ�����λ��
	 * @param end Ҫ�滻���е��յ㣬ʵ��λ��,���Ұ�����λ�㣬<br>
	 * ���end<0��˵���ǲ��������startλ��֮��<br>
	 * ��� start == end ��ô���ǽ��õ��滻��ָ������<br>
	 * ��� start > end && end >0 ˵������
	 * @param seq Ҫ�滻������
	 * @param boostart �滻���е�ǰ���Ƿ�������
	 * @param booend �滻���еĺ��Ƿ�������
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
		else if (start > end && end > 0) {//��������к���ˣ���������洦��
			logger.error("start < end: "+ start + " "+ end);
		}
		
		String FinalSeq = SeqSequence.substring(0, start) + startSeq + seq.toUpperCase() + endSeq + SeqSequence.substring(end);
		SeqSequence = FinalSeq;
	}
	
	/**
	 * ָ��snpλ�㣬ʵ��λ�ã���1��ʼ��Ȼ����ָ��������ȥ�滻ԭ��������
	 */
	public void modifySeq(int snpSite, char replace) {
		snpSite--;
		char[] chrSeq = SeqSequence.toCharArray();
		chrSeq[snpSite] = replace;
		String FinalSeq = chrSeq.toString();
		SeqSequence = FinalSeq;
	}
	
	/**
	 * ָ��snpλ�㣬ʵ��λ�ã���1��ʼ��Ȼ����ָ��������ȥ�滻ԭ��������
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
	 * ����TOLOWCASE��������
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
	 * ����AA��fasta����
	 */
	public String toStringAAfasta()
	{
		return ">" + SeqName + "\r\n" + toStringAA(true, 0);
	}
	/**
	 * ����Nr��fasta����
	 */
	public String toStringNRfasta()
	{
		return ">" + SeqName + "\r\n" + toString();
	}
	/**
	 * ͳ��������Сд���У�N�������Լ�X��������
	 */
	public ArrayList<LocInfo> getSeqInfo()
	{
		//string0: flag string1: location string2:endLoc
		ArrayList<LocInfo> lsResult = new ArrayList<LocInfo>();
		
		
		char[] seq = SeqSequence.toCharArray();
		boolean flagBound = false; //�߽�ģ����ǣ�XX
		boolean flagGap = false; //gap��ǣ�Сд
		boolean flagAmbitious = false; //��ȷ�������ǣ�NNN
		int bound = 0; int gap = 0; int ambitious = 0;
		int startBound = 0; int startGap = 0; int startAmbitious = 0;
		for (int i = 0; i < seq.length; i++) {
			if (seq[i] < 'a' && seq[i] != 'X' && seq[i] != 'N') {
				if (flagAmbitious) {
					addList(lsResult, "ambitious", startAmbitious, ambitious);
					flagAmbitious = false; //��ȷ�������ǣ�NNN
				}
				if (flagGap) {
					addList(lsResult, "gap", startGap, gap);
					flagGap = false; //gap��ǣ�Сд
				}
				if (flagBound) {
					addList(lsResult, "bound", startBound, bound);
					flagBound = false; //�߽�ģ����ǣ�XX
				}
			}
			else if (seq[i] == 'X' ) {
				if (flagAmbitious) {
					addList(lsResult, "ambitious", startAmbitious, ambitious);
					flagAmbitious = false; //��ȷ�������ǣ�NNN
				}
				if (flagGap) {
					addList(lsResult, "gap", startGap, gap);
					flagGap = false; //gap��ǣ�Сд
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
					flagGap = false; // gap��ǣ�Сд
				}
				if (flagBound) {
					addList(lsResult, "bound",startBound, bound);
					flagBound = false; // �߽�ģ����ǣ�XX
				}
			}
			else if (seq[i] >= 'a') {
				System.out.println("i");
				if (flagAmbitious) {
					addList(lsResult, "ambitious", startAmbitious, ambitious);
					flagAmbitious = false; //��ȷ�������ǣ�NNN
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
					flagBound = false; //�߽�ģ����ǣ�XX
				}
			}
		}
		return lsResult;
	}
	/**
	 * 
	 * @param lsInfo
	 * @param info
	 * @param start �ڲ������1
	 * @param length
	 */
	private void addList(ArrayList<LocInfo> lsInfo, String info, int start, int length) {
		LocInfo locInfo = new LocInfo(info, "", start, start+length-1, true);
		lsInfo.add(locInfo);
	}
	
	
	/**
	 * �Ƚ����������Ƿ�һ�£�������һ�µļ����
	 * ��ͷ��ʼ�Ƚϣ�ͷβ�����пո��м䲻���С�����blastģʽ�ıȽ�
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
	 * ��nr����ת��Ϊaa���У�����������֮��Ȼ���ո�˳�����orfѡ��
	 * @param cis ������ false�����򻥲�
	 * @param orf �ڼ���orf��0��1��2
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
	 * ����motif���������ϲ�����Ӧ��������ʽ<br>
	 * �����������кͷ������в��ҵĽ��<br>
	 * List-string [4] <br>
	 * 0: seqName<br>
	 * 1: strand : + / -<br>
	 * 2: �����motif����<br>
	 * 3: motif���һ������뱾�����յ�ľ���
	 * @param regex
	 * @return
	 */
	public ArrayList<String[]> getMotifScanResult(String regex) {
		return getMotifScanResult(regex,0);
	}
	
	/**
	 * ���ܲ��ܾ�ȷ�������
	 * ����motif���������ϲ�����Ӧ��������ʽ<br>
	 * �����������кͷ������в��ҵĽ��<br>
	 * List-string [4] <br>
	 * 0: seqName<br>
	 * 1: strand : + / -<br>
	 * 2: �����motif����<br>
	 * 3: motif���һ������뱾����site��ľ���
	 * @param regex
	 * @param site ����������յ��λ�ã�����Ϊ����������Ϊ������Ʃ��tss����seq�յ�500bp����siteΪ-500��
	 * Ҳ��������ȡ��tss����500bp��
	 * ��󷵻�motif��site�㣬��ô<b>����</b>��ʾmotif��site�����Σ�<b>����</b>��ʾmotif��site������
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
