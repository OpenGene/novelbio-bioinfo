package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.ibatis.migration.commands.NewCommand;

import com.novelbio.base.cmd.cmdOperate2;

public class GtfDetailCufIso extends GffDetailAbs{
	
	public GtfDetailCufIso(String chrID, String locString, boolean cis5to3) {
		super(chrID, locString, cis5to3);
		// TODO Auto-generated constructor stub
	}

	/**
	 * 存储每个符号所代表的含义<br>
  	=  Complete match of intron chain <br>
	c  Contained 	<br>
	j 	Potentially novel isoform (fragment): at least one splice junction is shared with a reference transcript 	<br>
	e 	Single exon transfrag overlapping a reference exon and at least 10 bp of a reference intron, indicating a possible pre-mRNA fragment. 	<br>
	i 	A transfrag falling entirely within a reference intron 	<br>
	o 	Generic exonic overlap with a reference transcript 	<br>
	p 	Possible polymerase run-on fragment (within 2Kbases of a reference transcript) <br>	
	r 	Repeat. Currently determined by looking at the soft-masked reference sequence and applied to transcripts where at least 50% of the bases are lower case <br>	
	u 	Unknown, intergenic transcript 	<br>
	x 	Exonic overlap with reference on the opposite strand 	<br>
	s 	An intron of the transfrag overlaps a reference intron on the opposite strand (likely due to read mapping errors) 	<br>
	. 	(.tracking file only, indicates multiple classifications)<br>
	 */
	private static HashMap<String, String> hashIsoInfo = new HashMap<String, String>();
	
	/**
	 * 放入所有剪接的名字，以及该剪接对应的时期，和相应的表达量，并排序
	 */
	private TreeMap<String,ArrayList<Double>> treeIso2Exp = new TreeMap<String, ArrayList<Double>>();
	/**
	 * 依次放入每个时期的名称，名称和treeIso2Exp里面的名称一致.
	 * 因为一个gtf文件中所有基因的时期都一致，所以用static
	 */
	private static ArrayList<String> lsCodName = new ArrayList<String>();
	/**
	 * 添加处理条件，这个在第一次就要完成
	 */
	public static void addCodName(String codName) {
		lsCodName.add(codName);
	}
	/**
	 * 添加某个转录本的表达量，按照时期依次添加，可以添加数组也可以一个一个添加
	 */
	public void addIsoExp(String class_code,String IsoName,double...exp) {
		ArrayList<Double> lsExp = new ArrayList<Double>();
		for (double d : exp) {
			lsExp.add(d);
		}
		treeIso2Exp.put(changeName(IsoName, class_code), lsExp);
	}
	/**
	 * 给定基因名和该基因的情况，将其合并为完整基因名
	 * @return
	 */
	public static String changeName(String IsoName, String class_code) {
		setHashIsoInfo();
		String thisCode = hashIsoInfo.get(class_code.trim());
		if (thisCode.equals("")) {
			return IsoName;
		}
		return IsoName+"_"+thisCode;
	}
	/**
	 * 获得每个时期所包含的可变剪接以及该剪接所对应的表达量
	 * arraylist-不同的时期
	 * string[]-每个时期中不同转录本的表达情况
	 * @return
	 */
	public ArrayList<String[]> getIsoExp() {
		ArrayList<String[]> lsIsoExp = new ArrayList<String[]>();
		int m = 0;// 计数器
		for (Entry<String, ArrayList<Double>> entry : treeIso2Exp.entrySet()) {
			String isoName = entry.getKey();
			ArrayList<Double> lsexp = entry.getValue();
			for (int i = 0; i < lsexp.size(); i++) {
				String[] cod = null;
				if (i<lsIsoExp.size()) {
					cod = lsIsoExp.get(i);
				}
				else
				{
					cod = new String[treeIso2Exp.size()];
					lsIsoExp.add(cod);
				}
				cod[m] = isoName+":"+lsexp.get(i) + "";
			}
			m++;
		}
		return lsIsoExp;
	}
	/**
	 * 获得做了几个时期的实验，分别是什么标题
	 * 这个仅针对当前GtfHash
	 */
	public static ArrayList<String> getExp() {
		return lsCodName;
	}
	
	
	
	/**
	 * 初始化基因异构
	 */
	private static void setHashIsoInfo() {
		if (!hashIsoInfo.isEmpty()) {
			return;
		}
		hashIsoInfo.put("=", "");
		hashIsoInfo.put("c", "c");
		hashIsoInfo.put("j", "j");
		hashIsoInfo.put("e", "e");
		hashIsoInfo.put("i", "i");
		hashIsoInfo.put("o", "o");
		hashIsoInfo.put("p", "p");
		hashIsoInfo.put("r", "r");
		hashIsoInfo.put("u", "u");
		hashIsoInfo.put("x", "x");
		hashIsoInfo.put("s", "s");
		hashIsoInfo.put(".", ".");
	}
}
