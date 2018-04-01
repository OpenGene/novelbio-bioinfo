package com.novelbio.analysis.gwas;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.novelbio.analysis.seq.fasta.Base;
import com.novelbio.analysis.seq.fasta.ChrBaseIter;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.base.StringOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;

/**
 * 给定plink-map文件和chr文件，在map文件最后一列添上碱基
 * 
 * 原来是 
 * chrId marker	other	position Major Minor<br>
 * 1	10100001579	0	1579 A G<br>
 * 1	10100003044	0	3044 G A<br>
 * 要修改为
 * chrId marker	other	position	reference alt<br>
 * 1	10100001579	0	1579	A G<br>
 * 1	10100003044	0	3044	C A<br>
 * @author zongjie
 *
 */
public class PlinkBimChangeBase {
	
	SnpAnno snpAnno = new SnpAnno();
	/** 染色体文件 */
	String chrFile;
	
	public PlinkBimChangeBase(String gffFile, String chrFile) {
		this.chrFile = chrFile;
		GffChrAbs gffChrAbs = new GffChrAbs();
		gffChrAbs.setChrFile(chrFile, null);
		gffChrAbs.setGffHash(new GffHashGene(gffFile));
		snpAnno.setGffChrAbs(gffChrAbs);
	}
	public PlinkBimChangeBase(GffChrAbs gffChrAbs) {
		this.chrFile = gffChrAbs.getSeqHash().getChrFile();
		snpAnno.setGffChrAbs(gffChrAbs);
	}
	public PlinkBimChangeBase(String chrFile) {
		this.chrFile = chrFile;
	}
	
	public void addAnnoFromRef(String plinkBim, String plinkBimAddBase) {
		AnnoFromRef annoFromRef = new AnnoFromRef(chrFile);

		TxtReadandWrite txtRead = new TxtReadandWrite(plinkBim);
		TxtReadandWrite txtWriteBim = new TxtReadandWrite(plinkBimAddBase, true);

		int baseIndex = 1;
		
		for (String content : txtRead.readlines()) {
			Allele allele = null;
			try {
				allele = annoFromRef.annotation(content);
				allele.setIndex(baseIndex++);
				txtWriteBim.writefileln(allele.toString());
			} catch (ExceptionNBCChromosome e) {
				txtRead.close();
				txtWriteBim.close();
				annoFromRef.close();
				throw e;
			}
		}
		annoFromRef.close();
		txtRead.close();
		txtWriteBim.close();
	}
	
	/** 把影响了iso的snp的marker提取出来 */
	public void generateSnpNeedIndex(String plinkBim, String plinkNeedSnpIndex) {
		AnnoFromRef annoFromRef = new AnnoFromRef(chrFile);

		TxtReadandWrite txtRead = new TxtReadandWrite(plinkBim);
		TxtReadandWrite txtWriteSnpIndex = new TxtReadandWrite(plinkNeedSnpIndex, true);

		int baseIndex = 1;
		
		for (String content : txtRead.readlines()) {
			Allele allele = null;
			try {
				allele = annoFromRef.annotation(content);
				allele.setIndex(baseIndex++);
				if (!snpAnno.getSetIsoName(allele).isEmpty()) {
					txtWriteSnpIndex.writefileln(allele.getMarker());
				}
			} catch (ExceptionNBCChromosome e) {
				txtRead.close();
				txtWriteSnpIndex.close();
				annoFromRef.close();
				throw e;
			}
		}
		annoFromRef.close();
		txtRead.close();
		txtWriteSnpIndex.close();
	}
	
	/**
	 * 注释的同时，
	 * 把影响了iso的snp的marker提取出来
	 */
	public void addAnnoFromRef(String plinkBim, String plinkBimAddChr, String plinkNeedSnpIndex) {
		AnnoFromRef annoFromRef = new AnnoFromRef(chrFile);

		TxtReadandWrite txtRead = new TxtReadandWrite(plinkBim);
		TxtReadandWrite txtWriteBim = new TxtReadandWrite(plinkBimAddChr, true);
		TxtReadandWrite txtWriteSnpIndex = new TxtReadandWrite(plinkNeedSnpIndex, true);

		int baseIndex = 1;
		
		for (String content : txtRead.readlines()) {
			Allele allele = null;
			try {
				allele = annoFromRef.annotation(content);
				allele.setIndex(baseIndex++);
				if (!snpAnno.getSetIsoName(allele).isEmpty()) {
					txtWriteSnpIndex.writefileln(allele.getMarker());
					txtWriteBim.writefileln(allele.toString());
				}
			} catch (ExceptionNBCChromosome e) {
				txtRead.close();
				txtWriteBim.close();
				txtWriteSnpIndex.close();
				annoFromRef.close();
				throw e;
			}
		}
		annoFromRef.close();
		txtRead.close();
		txtWriteBim.close();
		txtWriteSnpIndex.close();
	}
	
}

class ExceptionNBCChromosome extends RuntimeException {
	private static final long serialVersionUID = -3709544067133262276L;
	
	public ExceptionNBCChromosome() {
		super();
	}
	public ExceptionNBCChromosome(String msg) {
		super(msg);
	}
}

class AnnoFromRef implements Closeable {
	private static int gapLen = 50000;

	ChrBaseIter chrBaseIter;
	
	Iterator<Base> itBase = null;
	Allele lastAllel = null;
	
	public AnnoFromRef(String chrFile) {
		chrBaseIter = new ChrBaseIter(chrFile);
	}
	
	/** 给定plinkmap的一行，用reference进行注释
	 * 注意 plink.bim 的一行中
	 * 1\t10100001579\t0\t1579\tA\tG
	 * 第六列高频，第五列为低频
	 * 
	 * 输出修改为 第五列 ref 第六列 alt
	 * @param plinkMapContent 
	 * @return
	 */
	public Allele annotation(String plinkBimLine) {
		Allele allele = new Allele(plinkBimLine);
		/**
		 * 如果查找第一个allel，或者两个allel不在一条染色体上/相聚很远
		 * 则重新定位染色体
		 */
		if (lastAllel == null
				|| !StringOperate.isEqualIgnoreCase(lastAllel.getRefID(), allele.getRefID())
				|| allele.getStartAbs() < lastAllel.getStartAbs()
				|| allele.getStartAbs() - lastAllel.getStartAbs() > gapLen
				) {
			itBase = chrBaseIter.readBase(allele.getRefID(), allele.getStartAbs()).iterator();
		}
		Base base = itBase.next();
		while (base != null && base.getPosition() < allele.getStartAbs()) {
			base = itBase.next();
		}
		if (base.getPosition() == allele.getStartAbs()) {
			//本行开始到下面的if--功能是设置高频低频
			allele.setIsRefMajor(!allele.getRefBase().equalsIgnoreCase(base.getBase()+""));
			
			if (!allele.getRefBase().equalsIgnoreCase(base.getBase() + "")) {
				if (!allele.getAltBase().equalsIgnoreCase(base.getBase() + "")) {
					throw new ExceptionNBCPlink("error! " + allele.toString() + " but ref is " + base.getBase());
				}
				allele.changeRefAlt();
			}
			
		} else {
			chrBaseIter.close();
			throw new ExceptionNBCChromosome("cannot get reference on position " + allele.getRefID() + " " +allele.getPosition());
		}
		return allele;
	}
	
	@Override
	public void close() {
		chrBaseIter.close();			
	}

}

/**
 * 把int型转换为本对象
 * 最低位为 isHaveAlt
 * 10位为ref
 * 百位千位为预留的alt
 * @author zong0jie
 * @data 2017年11月23日
 */
class AlleleShort {
	static Map<Integer, String> mapCode2Base = new HashMap<>();
	static Map<String, Integer> mapBase2Code = new HashMap<>();
	static {
		mapCode2Base.put(0, "");
		mapCode2Base.put(1, "A");
		mapCode2Base.put(2, "T");
		mapCode2Base.put(3, "C");
		mapCode2Base.put(4, "G");
		
		mapBase2Code.put("", 0);
		mapBase2Code.put("A", 1);
		mapBase2Code.put("T", 2);
		mapBase2Code.put("C", 3);
		mapBase2Code.put("G", 4);
	}
	private String ref;
	private String alt;
		
	private AlleleShort() {};
	
	public AlleleShort(String ref, String alt) {
		if (StringOperate.isEqualIgnoreCase(ref, alt)) {
			alt = "";
		}
		this.alt = alt.toUpperCase();
		this.ref = ref.toUpperCase();
		if (StringOperate.isRealNull(alt)) {
			alt = "";
		}
	}
	
	public String getRef() {
		return ref;
	}
	public void setAlt(String alt) {
		this.alt = alt.toUpperCase();
	}
	public String getAlt() {
		return alt;
	}
	public boolean isHaveAlt() {
		return !StringOperate.isRealNull(alt);
	}
	
	//========================================
	//暂时不用
	@Deprecated
	public int encode() {
		int i = 0;
		i += mapBase2Code.get(ref);
		i += 10*mapBase2Code.get(alt);
		return i;
	}
	@Deprecated
	public static AlleleShort decode(int codeAllele) {
		AlleleShort alleleShort = new AlleleShort();
		int refInt = (codeAllele) % 10;
		alleleShort.ref = mapCode2Base.get(refInt);
		int altInt = (codeAllele/10) % 10;
		alleleShort.alt = mapCode2Base.get(altInt);
		return alleleShort;
	}
	
}

class Allele extends Align {
	String marker;
	String other;
	
	String ref;
	String alt;
	
	/** 该snp的序号 */
	int index;
	/**
	 * ref 位点是否为高频位点
	 * 对于plink.bim 来说，ref位点不一定是高频位点
	 * 很可能alt是高频
	 */
	Boolean isRefMajor;
	
	String allele1;
	String allele2;
	
	public Allele() {}
	
	/**
	 * 在读取plinkMap的时候，初始化本方法不会设定{@link #setIndex(int)}
	 * 所以需要手工设定
	 * @param chrInfo
	 */
	public Allele(String chrInfo) {
		String[] ss = chrInfo.split("\t");
		setChrID(ss[0]);
		marker = ss[1];
		other = ss[2];
		setEndAbs(Integer.parseInt(ss[3]));
		setStartAbs(Integer.parseInt(ss[3]));
		if (ss.length >= 5) {
			ref = ss[4];
		}
		if (ss.length >= 6) {
			alt = ss[5];
		}
		if (ss.length >= 7) {
			if (StringOperate.isEqual(ss[6], "1")) {
				isRefMajor = true;
			} else if (StringOperate.isEqual(ss[6], "-1")) {
				isRefMajor = false;
			}
		}
	}
	public String getMarker() {
		return marker;
	}
	/**
	 * ref 位点是否为高频位点
	 * 对于plink.bim 来说，ref位点不一定是高频位点
	 * 很可能alt是高频
	 */
	public void setIsRefMajor(boolean isRefMajor) {
		this.isRefMajor = isRefMajor;
	}
	public Boolean isRefMajor() {
		return isRefMajor;
	}
	/** ref和alt交换 */
	public void changeRefAlt() {
		String tmp = ref;
		ref = alt;
		alt = tmp;
	}
	/** 该snp的序号，在plinkPed中，只能知道allel的序号而不知道snp的具体position<br>
	 * 必须从plinkMap相同序号的allel中获取position<br>
	 * <br>
	 * 从1开始计数
	 * @param index
	 */
	public void setIndex(int index) {
		this.index = index;
	}
	/** 该snp的序号，在plinkPed中，只能知道allel的序号而不知道snp的具体position<br>
	 * 必须从plinkMap相同序号的allel中获取position<br>
	 * <br>
	 * 从1开始计数
	 * @param index
	 */
	public int getIndex() {
		return index;
	}
	public void setAlt(String alt) {
		this.alt = alt;
	}
	public void setAlt(Character alt) {
		this.alt = alt+"";
	}
	public void setRef(String ref) {
		this.ref = ref;
	}
	public void setRef(Character ref) {
		this.ref = ref+"";
	}
	public String getRefBase() {
		return ref;
	}
	public String getAltBase() {
		return alt;
	}
	public void setAllele1(Character allele1) {
		this.allele1 = allele1+"";
	}
	public void setAllele2(Character allele2) {
		this.allele2 = allele2+"";
	}
	public void setAllele1(String allele1) {
		this.allele1 = allele1;
	}
	public void setAllele2(String allele2) {
		this.allele2 = allele2;
	}
	public String getAllele1() {
		return allele1;
	}
	public String getAllele2() {
		return allele2;
	}
	/** 本allel所在的染色体坐标 */
	public int getPosition() {
		return getStartAbs();
	}
	
	public void setRef(Allele alleleRef) {
		this.ref = alleleRef.getRefBase();
		this.alt = alleleRef.getAltBase();
		this.setChrID(alleleRef.getRefID());
		this.setStartAbs(alleleRef.getStartAbs());
		this.setEndAbs(alleleRef.getEndAbs());
		this.setCis5to3(alleleRef.isCis5to3());
		this.marker = alleleRef.marker;
		this.isRefMajor = alleleRef.isRefMajor();
		
		if (!allele1.equals("0") && !ref.equalsIgnoreCase(allele1) && !alt.equalsIgnoreCase(allele1)
				|| !allele2.equals("0") && !ref.equalsIgnoreCase(allele2) && !alt.equalsIgnoreCase(allele2)
				) {
			throw new ExceptionNBCPlink("Error! allele ref " + alleleRef.toString() + " is not correspond with " + allele1 + " " + allele2);
		}
	}
	
	public String toString() {
		List<String> lsResult = new ArrayList<>();
		lsResult.add(getRefID());
		lsResult.add(marker);
		lsResult.add(other);
		lsResult.add(getStartAbs()+"");
		lsResult.add(ref);
		lsResult.add(alt);
		if (isRefMajor != null) {
			String isRefMajorStr = isRefMajor ? "1" : "-1";
			lsResult.add(isRefMajorStr);
		}
		return ArrayOperate.cmbString(lsResult, "\t");
	}
	
	/**
	 * 根据碱基频率来获取值
	 * 譬如高频位点为A，低频位点为T
	 * 则 AA = 1
	 * AT = 0
	 * TT = -1
	 * @return
	 */
	public int getFrq() {
		boolean isAllele1SameToRef = StringOperate.isEqual(allele1, ref);
		boolean isAllele2SameToRef = StringOperate.isEqual(allele2, ref);
		
		if (StringOperate.isEqual(allele1, "0") || StringOperate.isEqual(allele2, "0")) {
			return isRefMajor ? 1 : -1;
//			return -9;
		}
		
		int result = 0;
		if (isAllele1SameToRef && isAllele2SameToRef) {
			result = 1;
		} else if (isAllele1SameToRef || isAllele2SameToRef) {
			result = 0;
		} else {
			result = -1;
		}
		result = isRefMajor ? result : -result;
		return result;
		
	}
	public String toStringAlleleGwas() {
		List<String> lsResult = new ArrayList<>();
		lsResult.add(getRefID());
		lsResult.add(marker);
		lsResult.add(getStartAbs()+"");
		lsResult.add(ref);
		if (!StringOperate.isRealNull(alt)) {
			lsResult.add(alt);
		}
		if (!StringOperate.isRealNull(allele1)) {
			lsResult.add(allele1);
			lsResult.add(allele2);
		}
		lsResult.add(getFrq()+"");
		return ArrayOperate.cmbString(lsResult, "\t");
	}
}