package com.novelbio.database.execute;

import java.util.ArrayList;

import com.novelBio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.entity.friceDB.NCBIID;




public class UserTest {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		TxtReadandWrite txtTmp = new TxtReadandWrite();
		txtTmp.setParameter("/home/zong0jie/×ÀÃæ/control.txt", false, true);
		String[][] test = txtTmp.ExcelRead("/t", 1, 1, txtTmp.ExcelRows(), 1);
		for (int i = 0; i < test.length; i++) {
			NCBIID ncbiid = new NCBIID();
			ncbiid.setAccID(test[i][0]);
			
		} 
		
		 
	}

}
