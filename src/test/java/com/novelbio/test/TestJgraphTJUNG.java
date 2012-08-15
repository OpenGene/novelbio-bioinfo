package com.novelbio.test;

import java.util.ArrayList;

import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.model.modgeneid.GeneID;

public class TestJgraphTJUNG {
	
	
	
	
	
	
	private ArrayList<GeneID> getCopedID()
	{
		ArrayList<GeneID> lsResult = new ArrayList<GeneID>();
		String excelFile = "";
		ArrayList<String[]> lsAccID = ExcelTxtRead.readLsExcelTxtFile(excelFile, 1, 1, -1, -1);
		for (String[] strings : lsAccID) {
			GeneID copedID = new GeneID(strings[0], 0);
			if (!copedID.getIDtype().equals(GeneID.IDTYPE_ACCID)) {
				lsResult.add(copedID);
			}
		}
		return lsResult;
	}
}
