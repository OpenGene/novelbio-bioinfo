package com.novelbio.analysis.annotation.GO.goEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.log4j.Logger;

import com.novelbio.analysis.annotation.copeID.CopedID;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.database.DAO.FriceDAO.DaoFSGo2Term;
import com.novelbio.database.entity.friceDB.AGene2Go;
import com.novelbio.database.entity.friceDB.Go2Term;
import com.novelbio.database.entity.kegg.KGentry;

public abstract class GOInfoAbs {
	private static Logger logger = Logger.getLogger(GOInfoAbs.class);
	
	public GOInfoAbs(String genUniAccID, int taxID) {
		this.genUniAccID = genUniAccID;
		this.taxID = taxID;
	}
	
	String genUniAccID = null;
	int taxID = 0;
	
	AGene2Go aGene2Go;

	
	boolean booAGene2Gos = false;
	ArrayList<AGene2Go> lsAGene2Gos = null;
	/**
	 * key：GOID
	 * value：Gene2Go
	 */
	HashMap<String,AGene2Go> hashGene2Gos = null;
	/**
	 * 需要设定lsAGene2Gos和hashGene2Gos，记得每次都要把hashGene2Gos重新设置一遍
	 */
	protected abstract void setGene2Go();
	/**
	 * 获得本GeneID的GO信息
	 * @return
	 */
	public ArrayList<AGene2Go> getLsGen2Go()
	{
		setGene2Go();
		return lsAGene2Gos;
	}
	
	/**
	 * 将多个CopedID的GOInfoAbs放在一起，取并集去冗余
	 * @param copedIDs
	 * @return
	 */
	public ArrayList<AGene2Go> getLsGen2Go(ArrayList<GOInfoAbs> lsGoInfo) {
		setGene2Go();
		for (GOInfoAbs goInfoAbs : lsGoInfo) {
			for (AGene2Go aGene2Go : goInfoAbs.getLsGen2Go()) {
				if (hashGene2Gos.containsKey(aGene2Go.getGOID()))
					continue;
				hashGene2Gos.put(aGene2Go.getGOID(), aGene2Go);
			}
		}
		return ArrayOperate.getArrayListValue(hashGene2Gos);
	}
	
}
