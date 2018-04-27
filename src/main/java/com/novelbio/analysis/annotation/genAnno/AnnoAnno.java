package com.novelbio.analysis.annotation.genAnno;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.base.SepSign;
import com.novelbio.base.StringOperate;
import com.novelbio.database.domain.modgeneid.GeneID;
import com.novelbio.database.domain.modkegg.KeggInfoAbs;
import com.novelbio.generalConf.TitleFormatNBC;

public class AnnoAnno extends AnnoAbs {
	GffChrAbs gffChrAbs;
	boolean addLocInfo;
	
	/** 是否添加坐标信息 */
	public void setAddLocInfo(boolean addLocInfo) {
		this.addLocInfo = addLocInfo;
	}
	/** 设定GffChrAbs，当需要设定基因坐标时设定 */
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	/**
	 * 注释数据，不需要blast
	 * @param info 给定一行信息
	 * @param taxID 物种
	 * @param accColNum 具体该info的哪个column，实际column
	 * @return
	 */
	protected List<String[]> getInfo(int taxID, String accID) {
		if (accID.toLowerCase().equals("frem1")) {
			System.out.println();
		}
		List<String[]> lsResult = new ArrayList<>();
		List<String> resultTmp = new ArrayList<>();
		GeneID copedID = new GeneID(accID, taxID);
		if (copedID.getIDtype() != GeneID.IDTYPE_ACCID) {
			resultTmp.add(copedID.getAccID_With_DefaultDB().getAccID());
			resultTmp.add(copedID.getSymbol());
			if (addLocInfo) {
				resultTmp.addAll(getLocInfo(accID));
			}
			String keggId = copedID.getKeggInfo().getKegID();
			if (StringOperate.isRealNull(keggId)) {
				keggId = "";
			}
			resultTmp.add(keggId);
			resultTmp.add(copedID.getDescription());
		} else {
			resultTmp.add("");
			resultTmp.add("");
			if (addLocInfo) {
				resultTmp.add("");
				resultTmp.add("");
			}
			resultTmp.add("");
			resultTmp.add("");
		}
	
		lsResult.add(resultTmp.toArray(new String[0]));
		return lsResult;
	}
	
	/** 获得坐标位点信息 */
	private List<String> getLocInfo(String accID) {
		List<String> lsResult = new ArrayList<>();
		GffGeneIsoInfo gffGeneIsoInfo = gffChrAbs.getGffHashGene().searchISO(accID);
		if (gffGeneIsoInfo != null) {
			lsResult.add(gffGeneIsoInfo.getGeneType().toString());
			lsResult.add(gffGeneIsoInfo.getRefIDlowcase() + ":" + gffGeneIsoInfo.getStartAbs() + "-" + gffGeneIsoInfo.getEndAbs());
			String strand = gffGeneIsoInfo.isCis5to3() ? "+" : "-";
			lsResult.add(strand);
		} else {
			lsResult.add("");
			lsResult.add("");
		}
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
		geneID.setBlastInfo(evalue, subTaxID);
		lsResultTmp.add(geneID.getAccID_With_DefaultDB().getAccID());
		String[] anno = geneID.getAnno(true);
		String blastAccId = "";
		List<GeneID> lsGeneIDs = geneID.getLsBlastGeneID();
		int i = 0;
		for (GeneID geneID2 : lsGeneIDs) {
			try {
				if (i++ == 0) {
					blastAccId = geneID2.getAccID_With_DefaultDB().getAccID();
				} else {
					blastAccId = blastAccId + "//" + geneID2.getAccID_With_DefaultDB().getAccID();
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		if (blastAccId.endsWith("//")) {
			blastAccId.substring(0, blastAccId.length() - 2);
		}
		
		lsResultTmp.add(anno[0]);
		if (addLocInfo) {
			lsResultTmp.addAll(getLocInfo(accID));
		}
		String keggId = geneID.getKeggInfo().getKegID();
		if (StringOperate.isRealNull(keggId)) {
			keggId = "";
		}
		lsResultTmp.add(keggId);
		
		lsResultTmp.add(anno[1]);
		lsResultTmp.add(anno[3]);
		lsResultTmp.add(blastAccId);
		lsResultTmp.add(anno[4]);
		lsResultTmp.add(anno[5]);
		lsResult.add(lsResultTmp.toArray(new String[0]));
		return lsResult;
	}
	
	protected String[] getTitle() {
		List<String> lsTitle = new ArrayList<>();
		lsTitle.add(TitleFormatNBC.AccID.toString());
		lsTitle.add(TitleFormatNBC.Symbol.toString());
		if (addLocInfo) {
			lsTitle.add(TitleFormatNBC.GeneType.toString());
			lsTitle.add(TitleFormatNBC.Location.toString());
			lsTitle.add(TitleFormatNBC.Strand.toString());
		}
		lsTitle.add(TitleFormatNBC.KEGGID.toString());
		lsTitle.add(TitleFormatNBC.Description.toString());
		if (blast) {
			lsTitle.add(TitleFormatNBC.Evalue.toString());
			lsTitle.add("Blast_AccID");
			lsTitle.add("Blast_Symbol");
			lsTitle.add("Blast_Description");
		}
		return lsTitle.toArray(new String[0]);
	}
}
