package com.novelbio.analysis.gwas;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
 * chrId marker	other	position	reference<br>
 * 1	10100001579	0	1579	A<br>
 * 1	10100003044	0	3044	C<br>
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
	
	public void AddAnno(String plinkMapFile, String plinkAddRefFile) {
		ChrBaseIter chrBaseIter = new ChrBaseIter(chrFile);

		TxtReadandWrite txtRead = new TxtReadandWrite(plinkMapFile);
		TxtReadandWrite txtWrite = new TxtReadandWrite(plinkAddRefFile, true);
		
		Allele lastAllel = null;
		Iterator<Base> itBase = null;
		for (String content : txtRead.readlines()) {
			Allele allele = new Allele(content);
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
				txtRead.close();
				txtWrite.close();
				chrBaseIter.close();
				throw new ExceptionNBCChromosome("cannot get reference on position " + allele.getRefID() + " " +allele.getPosition());
			}
			txtWrite.writefileln(allele.toString());
		}
		txtRead.close();
		txtWrite.close();
		chrBaseIter.close();
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

class Allele extends Align {
	String marker;
	String other;
	
	String ref;
	String alt;
	
	
	public Allele() {}
	
	public Allele(String chrInfo) {
		String[] ss = chrInfo.split("\t");
		setChrID(ss[0]);
		marker = ss[1];
		other = ss[2];
		setEndAbs(Integer.parseInt(ss[3]));
		setStartAbs(Integer.parseInt(ss[3]));
		if (ss.length > 6) {
			ref = ss[4];
			alt = ss[5];
		}
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
		return ArrayOperate.cmbString(lsResult, "\t");
	}
}