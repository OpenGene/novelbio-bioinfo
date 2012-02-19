package com.novelbio.test;

import java.util.ArrayList;

import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.model.modcopeid.CopedID;

public class TestJgraphTJUNG {
	
	
	
	
	
	
	private ArrayList<CopedID> getCopedID()
	{
		ArrayList<CopedID> lsResult = new ArrayList<CopedID>();
		String excelFile = "";
		ArrayList<String[]> lsAccID = ExcelTxtRead.readLsExcelTxtFile(excelFile, 1, 1, -1, -1);
		for (String[] strings : lsAccID) {
			CopedID copedID = new CopedID(strings[0], 0);
			if (!copedID.getIDtype().equals(CopedID.IDTYPE_ACCID)) {
				lsResult.add(copedID);
			}
		}
		return lsResult;
	}
}
