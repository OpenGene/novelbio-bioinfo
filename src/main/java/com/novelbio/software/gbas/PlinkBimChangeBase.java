package com.novelbio.software.gbas;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.novelbio.base.StringOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.bioinfo.base.Align;
import com.novelbio.bioinfo.fasta.Base;
import com.novelbio.bioinfo.fasta.ChrBaseIter;
import com.novelbio.bioinfo.gff.GffHashGene;
import com.novelbio.bioinfo.gffchr.GffChrAbs;

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
				|| !StringOperate.isEqualIgnoreCase(lastAllel.getChrId(), allele.getChrId())
				|| allele.getStartAbs() < lastAllel.getStartAbs()
				|| allele.getStartAbs() - lastAllel.getStartAbs() > gapLen
				) {
			itBase = chrBaseIter.readBase(allele.getChrId(), allele.getStartAbs()).iterator();
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
			throw new ExceptionNBCChromosome("cannot get reference on position " + allele.getChrId() + " " +allele.getPosition());
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

