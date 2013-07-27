package com.novelbio.analysis.annotation.genAnno;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.generalConf.TitleFormatNBC;

public class AnnoAnno extends AnnoAbs {
	
	protected String[] getTitle() {
		List<String> lsTitle = new ArrayList<>();
		lsTitle.add(TitleFormatNBC.Symbol.toString());
		lsTitle.add(TitleFormatNBC.Description.toString());
		if (blast) {
			lsTitle.add(TitleFormatNBC.Evalue.toString());
			lsTitle.add("Blast_Symbol");
			lsTitle.add("Blast_Description");
		}
		return lsTitle.toArray(new String[0]);
	}

	/**
	 * 注释数据，不需要blast
	 * @param info 给定一行信息
	 * @param taxID 物种
	 * @param accColNum 具体该info的哪个column，实际column
	 * @return
	 */
	protected List<String[]> getInfo(int taxID, String accID) {
		List<String[]> lsResult = new ArrayList<>();
		String[] resultTmp = new String[2];
		GeneID copedID = new GeneID(accID, taxID);
		if (copedID.getIDtype() != GeneID.IDTYPE_ACCID) {
			resultTmp[0] = copedID.getSymbol();
			resultTmp[1] = copedID.getDescription();
		}
		lsResult.add(resultTmp);
		return lsResult;
	}
	/**
	 * 注释数据，需要blast
	 * @param info 给定一行信息
	 * @param taxID 物种
	 * @param accColNum 具体该info的哪个column，实际column
	 * @return
	 */
	protected List<String[]> getInfoBlast(int taxID, int subTaxID, double evalue, String accID) {
		List<String[]> lsResult = new ArrayList<>();
		List<String> lsResultTmp = new ArrayList<>();
		GeneID geneID = new GeneID(accID, taxID);
		if (geneID.getIDtype() != GeneID.IDTYPE_ACCID) {
			geneID.setBlastInfo(evalue, subTaxID);
			String[] anno = geneID.getAnno(true);
			lsResultTmp.add(anno[0]);
			lsResultTmp.add(anno[1]);
			lsResultTmp.add(anno[3]);
			lsResultTmp.add(anno[4]);
			lsResultTmp.add(anno[5]);
		} else {
			for (int i = 0; i < 5; i++) {
				lsResultTmp.add("");
			}
		}
		lsResult.add(lsResultTmp.toArray(new String[0]));
		return lsResult;
	}

}
