package com.novelbio.analysis.coexp.simpCoExp;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.generalConf.TitleFormatNBC;

/**
 * 加上degree并且注释
 * @author zomg0jie
 *
 */
public class DegreeAnnotation {
	int taxID = 0;
	/** 是否读取共表达的组 */
	boolean readCoexpDegree;
	List<String[]> lsPairInfo;
	List<CoexPair> lsCoexPairs;
	/** key小写 */
	Map<String, GeneDegreeUnit> mapAccID2Degree = new LinkedHashMap<>();
	
	public void readData(String infile) {
		lsPairInfo = ExcelTxtRead.readLsExcelTxt(infile, 1);
		String[] title = lsPairInfo.get(0);
		if (title[2].equals("pearson") && title[3].equals(TitleFormatNBC.Pvalue.toString()) 
				&& title[4].equals(TitleFormatNBC.FDR.toString())) {
			readCoexpDegree = true;
		} else {
			readCoexpDegree = false;
		}
		lsPairInfo = lsPairInfo.subList(1, lsPairInfo.size());
		initial();
	}
	
	public void setTaxID(int taxID) {
		this.taxID = taxID;
	}

	public void writeToFile(String outFile) {
		ExcelOperate excelCoExp = new ExcelOperate();
		excelCoExp.newExcelOpen(outFile);
		String sheet1 = "GeneInteraction";
		excelCoExp.WriteExcel(sheet1, 1, 1, getLsPairAnno());
		String sheet2 = "Attribute";
		excelCoExp.WriteExcel(sheet2, 1, 1, getLsDegree());
	}
	
	public void initial() {
		lsCoexPairs = new ArrayList<>();
		for (String[] pairInfo : lsPairInfo) {
			CoexPair coexPair = null;
			if (readCoexpDegree) {
				coexPair = new CoexPair(taxID, pairInfo, true);
			} else {
				coexPair = new CoexPair();
				CoexpGenInfo coexpGenInfo1 = new CoexpGenInfo(pairInfo[0], taxID, null);
				coexpGenInfo1.initialGeneInfo();
				CoexpGenInfo coexpGenInfo2 = new CoexpGenInfo(pairInfo[1], taxID, null);
				coexpGenInfo2.initialGeneInfo();
				coexPair.setCoexpPair(coexpGenInfo1, coexpGenInfo1);
			}
			lsCoexPairs.add(coexPair);
			
			GeneDegreeUnit geneDegreeUnit1 = new GeneDegreeUnit();
			geneDegreeUnit1.setGeneID(coexPair.getCoexpGenInfo1().getGeneID());
			GeneDegreeUnit geneDegreeUnit2 = new GeneDegreeUnit();
			geneDegreeUnit2.setGeneID(coexPair.getCoexpGenInfo2().getGeneID());
			addUnit(true, pairInfo[0], geneDegreeUnit1);
			addUnit(false, pairInfo[1], geneDegreeUnit2);
		}
	}
	
	private void addUnit(boolean out, String accID, GeneDegreeUnit geneDegreeUnit) {
		accID = accID.toLowerCase();
		if (mapAccID2Degree.containsKey(accID)) {
			geneDegreeUnit = mapAccID2Degree.get(accID);
		} else {
			mapAccID2Degree.put(accID, geneDegreeUnit);
		}
		if (out) {
			geneDegreeUnit.addOutDegree();
		} else {
			geneDegreeUnit.addInDegree();
		}
	}
	
	public List<String[]> getLsPairAnno() {
		List<String[]> lsPairAnno = new ArrayList<>();
		for (CoexPair coexPair : lsCoexPairs) {
			String[] tmp = coexPair.toStringArrayAnno();
			if (tmp != null) {
				lsPairAnno.add(tmp);
			}
		}
		if (readCoexpDegree) {
			lsPairAnno.add(0, CoexPair.getTitleAnnoCoexp());
		} else {
			lsPairAnno.add(0, CoexPair.getTitleAnno());
		}
		return lsPairAnno;
	}
	
	public List<String[]> getLsDegree() {
		List<String[]> lsDegree = new ArrayList<>();
		for (GeneDegreeUnit geneDegreeUnit : mapAccID2Degree.values()) {
			lsDegree.add(geneDegreeUnit.toStringArray());
		}
		lsDegree.add(0, GeneDegreeUnit.getTitle());
		return lsDegree;
	}
}

class GeneDegreeUnit {
	GeneID geneID;
	int inDegree;
	int outDegree;
	
	public void setGeneID(GeneID geneID) {
		this.geneID = geneID;
	}
	
	public void addInDegree() {
		inDegree++;
	}
	
	public void addOutDegree() {
		outDegree++;
	}
	
	public int getInDegree() {
		return inDegree;
	}
	public int getOutDegree() {
		return outDegree;
	}
	
	public int getDegree() {
		return inDegree+outDegree;
	}
	
	public String[] toStringArray() {
		List<String> lsResult = new ArrayList<>();
		lsResult.add(geneID.getAccID());
		lsResult.add(geneID.getSymbol());
		lsResult.add(geneID.getDescription());
		lsResult.add(inDegree + "");
		lsResult.add(outDegree + "");
		lsResult.add(getDegree() + "");
		return lsResult.toArray(new String[0]);
	}
	
	public static String[] getTitle() {
		List<String> lsTitle = new ArrayList<>();
		lsTitle.add(TitleFormatNBC.AccID.toString());
		lsTitle.add(TitleFormatNBC.Symbol.toString());
		lsTitle.add(TitleFormatNBC.Description.toString());
		lsTitle.add("InDegree");
		lsTitle.add("OutDegree");
		lsTitle.add("Degree");
		return lsTitle.toArray(new String[0]);
	}
}
