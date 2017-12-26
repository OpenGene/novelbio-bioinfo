package com.novelbio.analysis.gwas;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.novelbio.analysis.seq.fasta.Base;
import com.novelbio.analysis.seq.fasta.ChrBaseIter;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.base.StringOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;

/**
 * 给定plink-map文件和chr文件，在map文件最后一列添上碱基
 * 
 * 原来是 
 * chrId marker	other	position<br>
 * 1	10100001579	0	1579<br>
 * 1	10100003044	0	3044<br>
 * 要修改为
 * chrId marker	other	position	reference alt<br>
 * 1	10100001579	0	1579	A G<br>
 * 1	10100003044	0	3044	C A<br>
 * @author zongjie
 *
 */
public class PlinkMapAddBase {
	private static int gapLen = 50000;
	
	/** 染色体文件 */
	String chrFile;
	
	public PlinkMapAddBase(String chrFile) {
		this.chrFile = chrFile;
	}
	
	public void AddAnno(String plinkMapFile, String plinkPed, String plinkAddRefFile) {
		AnnoFromRef annoFromRef = new AnnoFromRef(chrFile);
		TxtReadandWrite txtRead = new TxtReadandWrite(plinkMapFile);
		
		List<Integer> lsRef2This = new ArrayList<>();
		for (String content : txtRead.readlines()) {
			if (content.startsWith("#")) continue;
			
			Allele allele = null;
			try {
				allele = annoFromRef.annotation(content);
			} catch (ExceptionNBCChromosome e) {
				txtRead.close();
				annoFromRef.close();
				throw e;
			}
			AlleleShort alleleShort = new AlleleShort(allele.getRefBase(), "");
			lsRef2This.add(alleleShort.encode());
		}
		annoFromRef.close();
		txtRead.close();
		
		PlinkPedReader pedReader = new PlinkPedReader(plinkPed);
		for (String sample : pedReader.getLsAllSamples()) {
			int i = 0;
			for (Allele allele : pedReader.readAllelsFromSample(sample)) {
				int code = lsRef2This.get(i);
				AlleleShort alleleShort = AlleleShort.decode(code);
				if (!alleleShort.isHaveAlt()) {
					if (!StringOperate.isEqualIgnoreCase(allele.getRefBase(), alleleShort.getRef())) {
						alleleShort.setAlt(allele.getRefBase());
					} else if (!StringOperate.isEqualIgnoreCase(allele.getAltBase(), alleleShort.getRef())) {
						alleleShort.setAlt(allele.getAltBase());
					}
					lsRef2This.set(i, alleleShort.encode());
				}
				i++;
			}
		}
		pedReader.close();
		
		txtRead = new TxtReadandWrite(plinkMapFile);
		TxtReadandWrite txtWrite = new TxtReadandWrite(plinkAddRefFile, true);
		int i = 0;
		for (String content : txtRead.readlines()) {
			if (content.startsWith("#")) continue;
			
			AlleleShort alleleShort = AlleleShort.decode(lsRef2This.get(i));
			Allele allele = new Allele(content);
			allele.setRef(alleleShort.getRef());
			allele.setAlt(alleleShort.getAlt());
			txtWrite.writefileln(allele.toString());
			i++;
		}
		txtRead.close();
		txtWrite.close();
	}
	
	public void addAnnoFromRef(String plinkMapFile, String plinkAddRefFile) {
		AnnoFromRef annoFromRef = new AnnoFromRef(chrFile);

		TxtReadandWrite txtRead = new TxtReadandWrite(plinkMapFile);
		TxtReadandWrite txtWrite = new TxtReadandWrite(plinkAddRefFile, true);
	
		for (String content : txtRead.readlines()) {
			Allele allele = null;
			try {
				allele = annoFromRef.annotation(content);
			} catch (ExceptionNBCChromosome e) {
				txtRead.close();
				txtWrite.close();
				annoFromRef.close();
				throw e;
			}
			txtWrite.writefileln(allele.toString());
		}
		annoFromRef.close();
		txtRead.close();
		txtWrite.close();
	}
	
	static class AnnoFromRef implements Closeable {
		
		ChrBaseIter chrBaseIter;
		
		Iterator<Base> itBase = null;
		Allele lastAllel = null;
		
		public AnnoFromRef(String chrFile) {
			chrBaseIter = new ChrBaseIter(chrFile);
		}
		
		/** 给定plinkmap的一行，用reference进行注释 */
		public Allele annotation(String plinkMapContent) {
			Allele allele = new Allele(plinkMapContent);
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
				allele.setRef(base.getBase());
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
	
	public static class ExceptionNBCChromosome extends RuntimeException {
		private static final long serialVersionUID = -3709544067133262276L;
		
		public ExceptionNBCChromosome() {
			super();
		}
		public ExceptionNBCChromosome(String msg) {
			super(msg);
		}
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
	
	public int encode() {
		int i = 0;
		i += mapBase2Code.get(ref);
		i += 10*mapBase2Code.get(alt);
		return i;
	}
	
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
	
	/** 本allel所在的染色体坐标 */
	public int getPosition() {
		return getStartAbs();
	}
	
	public String toString() {
		List<String> lsResult = new ArrayList<>();
		lsResult.add(getRefID());
		lsResult.add(marker);
		lsResult.add(other);
		lsResult.add(getStartAbs()+"");
		lsResult.add(ref);
		if (!StringOperate.isRealNull(alt)) {
			lsResult.add(alt);
		}
		return ArrayOperate.cmbString(lsResult, "\t");
	}
}