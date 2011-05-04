package com.novelBio.upDateDB.dataBase;

import entity.friceDB.Gene2GoInfo;
import entity.friceDB.NCBIID;
import DAO.FriceDAO.DaoFCGene2GoInfo;
 


 


public class UserTest {
                     
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DaoFCGene2GoInfo userDao=new DaoFCGene2GoInfo();
		NCBIID ncbiid=new NCBIID();
		ncbiid.setAccID("119592981");//ncbiid.setTaxID(9606);
		Gene2GoInfo gene2GoInfo=userDao.queryGeneDetail(ncbiid);
 
		System.out.println("test");
	}

}
