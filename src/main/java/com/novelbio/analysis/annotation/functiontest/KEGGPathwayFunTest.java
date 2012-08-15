package com.novelbio.analysis.annotation.functiontest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import com.novelbio.database.domain.kegg.KGpathway;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.model.modkegg.KeggInfo;

public class KEGGPathwayFunTest extends AbstFunTest{

	public KEGGPathwayFunTest(ArrayList<GeneID> lsCopedIDsTest,
			ArrayList<GeneID> lsCopedIDsBG, boolean blast) {
		super(lsCopedIDsTest, lsCopedIDsBG, blast);
	}
	
	public KEGGPathwayFunTest(boolean blast, double evalue, int... blastTaxID) {
		setBlast(blast, evalue, blastTaxID);
	}
	
	public KEGGPathwayFunTest() {}
	
	/**
	 * Fisher����ʱ���õĶ���
	 */
	@Override
	public String[] getItemName(String ItemID) {
		String[] KeggTerm = new String[1];
		KeggTerm[0] = KeggInfo.getKGpathway(ItemID).getTitle();
		return KeggTerm;
	}

	@Override
	protected ArrayList<String[]> convert2Item(Collection<GeneID> lsGeneIDs) {
		HashSet<String> hashGenUniID = new HashSet<String>();
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
			for (GeneID geneID : lsGeneIDs) {
				if (hashGenUniID.contains(geneID.getGenUniID())) {
					continue;
				}
				hashGenUniID.add(geneID.getGenUniID());
				ArrayList<KGpathway> lsPath = null;
				lsPath = geneID.getKegPath(blast);
				
				if (lsPath == null || lsPath.size() == 0) {
					continue;
				}
				String[] tmpResult = new String[2];
				tmpResult[0] = geneID.getGenUniID();
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
	 * Go����������gene2Go���<br>
	 * blast��<br>
	 * 			title2[0]="QueryID";title2[1]="QuerySymbol";title2[2]="Description";title2[3]="Evalue";title2[4]="subjectSymbol";<br>
			title2[5]="Description";title2[6]="PathID";title2[7]="PathTerm";<br>
			��blast��<br>
						title2[0]="QueryID";title2[1]="QuerySymbol";title2[2]="Description";title2[3]="PathID";<br>
			title2[4]="PathTerm";<br>
	 */
	@Override
	public ArrayList<String[]> setGene2Item() {
		ArrayList<String[]> lsFinal = new ArrayList<String[]>();
		for (GeneID copedID : lsCopedIDsTest) {
			ArrayList<KGpathway> lsPath = null;
			//��þ����GO��Ϣ
			lsPath = copedID.getKegPath(blast);
			if (lsPath == null || lsPath.size() == 0) {
				continue;
			}
			//GOǰ��ĳ�����Ϣ�����,Symbol��description��
			String[] tmpresultRaw = copedID.getAnno(blast);
			String[] tmpresult = copyAnno(copedID.getAccID(), tmpresultRaw);
			//GO��Ϣ�����
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
		String[] title;
		if (blast) {
			title = new String[8];
			title[0]="QueryID";title[1]="QuerySymbol";title[2]="Description";
			title[3]="Evalue"; title[4]="subjectSymbol"; title[5]="Description";
			title[6]="PathID"; title[7]="PathTerm";
		}
		else {
			title = new String[5];
			title[0]="QueryID";title[1]="QuerySymbol";title[2]="Description";
			title[3]="PathID"; title[4]="PathTerm";
		}
		lsFinal.add(0,title);
		
		return lsFinal;
	}
	/**
	 * ��ʱû��
	 */
	@Override
	public void setDetailType(String GOtype) {
		// TODO Auto-generated method stub
		
	}
	/**
	 * ������
	 */
	@Override
	public ArrayList<String[]> getItem2GenePvalue() {
		// TODO Auto-generated method stub
		return null;
	}

}
