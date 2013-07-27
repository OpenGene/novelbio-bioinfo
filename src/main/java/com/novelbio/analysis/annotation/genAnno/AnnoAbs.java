package com.novelbio.analysis.annotation.genAnno;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.base.dataStructure.ArrayOperate;

public abstract class AnnoAbs {
	public static final int ANNOTATION = 1234;
	public static final int GO = 2345;
	public static final int PATH = 3456;
	
	int taxIDthis;
	int taxIDBlast;
	double evalueBlast;
	boolean blast;
	
	public void setTaxIDquery(int taxIDquery) {
		this.taxIDthis = taxIDquery;
	}
	public void setBlast(boolean blast) {
		this.blast = blast;
	}
	public void setBlastToTaxID(int taxID, double evalue) {
		this.taxIDBlast = taxID;
		this.evalueBlast = evalue;
	}

	public String[] getTitle(String[] titleOld) {
		return ArrayOperate.combArray(titleOld, getTitle(), 0);
	}
	
	protected abstract String[] getTitle();
	
	public List<String[]> getInfo(String[] info, String accID) {
		List<String[]> lsResultFinal = new ArrayList<>();
		List<String[]> lsResult = new ArrayList<>();
		if (!blast) {
			lsResult = getInfo(taxIDthis, accID);
		} else {
			lsResult = getInfoBlast(taxIDthis, taxIDBlast, evalueBlast, accID);
		}
		try {
			for (String[] strings : lsResult) {
				String[] info2 = ArrayOperate.copyArray(info);
				lsResultFinal.add(ArrayOperate.combArray(info2, strings, 0));
			}
		} catch (Exception e) {
			if (!blast) {
				lsResult = getInfo(taxIDthis, accID);
			} else {
				lsResult = getInfoBlast(taxIDthis, taxIDBlast, evalueBlast, accID);
			}
		}

		return lsResultFinal;
	}

	/**
	 * 注释数据，不需要blast
	 * @param info 给定一行信息
	 * @param taxID 物种
	 * @param accColNum 具体该info的哪个column，实际column
	 * @return
	 */
	protected abstract List<String[]> getInfo(int taxID, String accID);
	/**
	 * 注释数据，需要blast
	 * @param info 给定一行信息
	 * @param taxID 物种
	 * @param accColNum 具体该info的哪个column，实际column
	 * @return
	 */
	protected abstract List<String[]> getInfoBlast(int taxID, int subTaxID, double evalue, String accID);
	
	public static AnnoAbs createAnnoAbs(int annoType) {
		if (annoType == AnnoAbs.ANNOTATION) {
			return new AnnoAnno();
		} else if (annoType == AnnoAbs.GO) {
			return new AnnoGO();
		} else if (annoType == AnnoAbs.PATH) {
			return new AnnoPath();
		}
		return null;
	}
}
