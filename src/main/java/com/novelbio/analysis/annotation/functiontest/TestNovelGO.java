package com.novelbio.analysis.annotation.functiontest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import com.novelbio.analysis.annotation.GO.goEntity.GOInfoAbs;
import com.novelbio.analysis.annotation.copeID.CopedID;
import com.novelbio.database.entity.friceDB.AGene2Go;
import com.novelbio.database.entity.friceDB.Go2Term;

public class TestNovelGO extends TestAbs{
	String GoType = GOInfoAbs.GO_BP;
	public TestNovelGO(ArrayList<CopedID> lsCopedIDsTest,
			ArrayList<CopedID> lsCopedIDsBG, boolean blast, String GoType) {
		super(lsCopedIDsTest, lsCopedIDsBG, blast);
		this.GoType = GoType;
	}
	public TestNovelGO(boolean blast, String GoType) {
		super(blast);
		this.GoType = GoType;
	}
	
	@Override
	protected ArrayList<String[]> convert2Item(ArrayList<CopedID> lsCopedIDs) {
		HashSet<String> hashGenUniID = new HashSet<String>();
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
			for (CopedID copedID : lsCopedIDs) {
				if (hashGenUniID.contains(copedID.getGenUniID())) {
					continue;
				}
				hashGenUniID.add(copedID.getGenUniID());
				ArrayList<AGene2Go> lsGO = null;
				if (blast) 
					lsGO = copedID.getGene2GOBlast(GoType);
				else 
					lsGO = copedID.getGene2GO(GoType);
				
				if (lsGO == null || lsGO.size() == 0) {
					continue;
				}
				String[] tmpResult = new String[2];
				tmpResult[0] = copedID.getGenUniID();
				for (AGene2Go aGene2Go : lsGO) {
					if (tmpResult[1] == null || tmpResult[1].trim().equals("")) {
						tmpResult[1] = aGene2Go.getGOID();
					}
					else {
						 tmpResult[1] = tmpResult[1] + "," + aGene2Go.getGOID();
					}
				}
				lsResult.add(tmpResult);
			}
			return lsResult;
	}
	/**
	 * 富集分析的gene2Item表格，带标题<br>
	 * blast：<br>
	 * 			title2[0]="QueryID";title2[1]="QuerySymbol";title2[2]="Description";title2[3]="Evalue";title2[4]="subjectSymbol";<br>
			title2[5]="Description";title2[6]="GOID";title2[7]="GOTerm";title2[8]="Evidence"<br>
			不blast：<br>
						title2[0]="QueryID";title2[1]="QuerySymbol";title2[2]="Description";title2[3]="GOID";<br>
			title2[4]="GOTerm";title2[5]="Evidence"<br>
	 */
	@Override
	public ArrayList<String[]> setGene2Item() {
		ArrayList<String[]> lsFinal = new ArrayList<String[]>();
		for (CopedID copedID : lsCopedIDsTest) {
			ArrayList<AGene2Go> lsGen2Go = null;
			//获得具体的GO信息
			if (blast)
				lsGen2Go = copedID.getGene2GOBlast(GoType);
			else
				lsGen2Go = copedID.getGene2GO(GoType);
			if (lsGen2Go == null || lsGen2Go.size() == 0) {
				continue;
			}
			//GO前面的常规信息的填充,Symbol和description等
			String[] tmpresult = copedID.getAnnoInfo(blast);
			//GO信息的填充
			for (AGene2Go aGene2Go : lsGen2Go) {
				String[] result = null;
				if (blast)
					result = Arrays.copyOf(tmpresult, 9);//ArrayOperate.copyArray(tmpresult, 9);
				else
					result = Arrays.copyOf(tmpresult, 6);
				result[result.length -1] = aGene2Go.getEvidence();
				result[result.length -2] = aGene2Go.getGOTerm();
				result[result.length -3] =aGene2Go.getGOID();
				lsFinal.add(result);
			}
		}
		return lsFinal;
	}
	/**
	 * Fisher检验时候用的东西
	 */
	@Override
	public String[] getItemName(String ItemID) {
		String[] GoTerm = new String[1];
		GoTerm[0] = Go2Term.getHashGo2Term().get(ItemID).getGoTerm();
		return GoTerm;
	}

}
