package com.novelbio.analysis.annotation.functiontest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import com.novelbio.database.domain.kegg.KGpathway;
import com.novelbio.database.model.modcopeid.CopedID;
import com.novelbio.database.model.modkegg.KeggInfo;

public class KEGGPathwayFunTest extends AbstFunTest{

	public KEGGPathwayFunTest(ArrayList<CopedID> lsCopedIDsTest,
			ArrayList<CopedID> lsCopedIDsBG, boolean blast) {
		super(lsCopedIDsTest, lsCopedIDsBG, blast);
	}
	
	public KEGGPathwayFunTest(boolean blast, double evalue, int... blastTaxID) {
		setBlast(blast, evalue, blastTaxID);
	}
	
	public KEGGPathwayFunTest() {}
	
	/**
	 * Fisher检验时候用的东西
	 */
	@Override
	public String[] getItemName(String ItemID) {
		String[] KeggTerm = new String[1];
		KeggTerm[0] = KeggInfo.getKGpathway(ItemID).getTitle();
		return KeggTerm;
	}

	@Override
	protected ArrayList<String[]> convert2Item(Collection<CopedID> lsCopedIDs) {
		HashSet<String> hashGenUniID = new HashSet<String>();
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
			for (CopedID copedID : lsCopedIDs) {
				if (hashGenUniID.contains(copedID.getGenUniID())) {
					continue;
				}
				hashGenUniID.add(copedID.getGenUniID());
				ArrayList<KGpathway> lsPath = null;
				lsPath = copedID.getKegPath(blast);
				
				if (lsPath == null || lsPath.size() == 0) {
					continue;
				}
				String[] tmpResult = new String[2];
				tmpResult[0] = copedID.getGenUniID();
				for (KGpathway kGpathway : lsPath) {
					if (tmpResult[1] == null || tmpResult[1].trim().equals("")) {
						tmpResult[1] = kGpathway.getPathName();
					}
					else {
						 tmpResult[1] = tmpResult[1] + "," + kGpathway.getPathName();
					}
				}
				lsResult.add(tmpResult);
			}
			return lsResult;
	}
	/**
	 * Go富集分析的gene2Go表格<br>
	 * blast：<br>
	 * 			title2[0]="QueryID";title2[1]="QuerySymbol";title2[2]="Description";title2[3]="Evalue";title2[4]="subjectSymbol";<br>
			title2[5]="Description";title2[6]="PathID";title2[7]="PathTerm";<br>
			不blast：<br>
						title2[0]="QueryID";title2[1]="QuerySymbol";title2[2]="Description";title2[3]="PathID";<br>
			title2[4]="PathTerm";<br>
	 */
	@Override
	public ArrayList<String[]> setGene2Item() {
		ArrayList<String[]> lsFinal = new ArrayList<String[]>();
		for (CopedID copedID : lsCopedIDsTest) {
			ArrayList<KGpathway> lsPath = null;
			//获得具体的GO信息
			lsPath = copedID.getKegPath(blast);
			if (lsPath == null || lsPath.size() == 0) {
				continue;
			}
			//GO前面的常规信息的填充,Symbol和description等
			String[] tmpresultRaw = copedID.getAnno(blast);
			String[] tmpresult = copyAnno(copedID.getAccID(), tmpresultRaw);
			//GO信息的填充
			for (KGpathway kGpathway : lsPath) {
				String[] result = null;
				if (blast)
					result = Arrays.copyOf(tmpresult, 8);//ArrayOperate.copyArray(tmpresult, 9);
				else
					result = Arrays.copyOf(tmpresult, 5);
				result[result.length -1] = kGpathway.getTitle();
				result[result.length -2] =kGpathway.getPathName();
				lsFinal.add(result);
			}
		}
		return lsFinal;
	}
	/**
	 * 暂时没用
	 */
	@Override
	public void setDetailType(String GOtype) {
		// TODO Auto-generated method stub
		
	}
	/**
	 * 不返回
	 */
	@Override
	public ArrayList<String[]> getItem2GenePvalue() {
		// TODO Auto-generated method stub
		return null;
	}

}
