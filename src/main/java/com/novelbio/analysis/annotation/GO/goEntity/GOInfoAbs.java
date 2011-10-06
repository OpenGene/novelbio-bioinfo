package com.novelbio.analysis.annotation.GO.goEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.log4j.Logger;

import com.novelbio.analysis.annotation.copeID.CopedID;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.database.DAO.FriceDAO.DaoFSGo2Term;
import com.novelbio.database.entity.friceDB.AGene2Go;
import com.novelbio.database.entity.friceDB.AGeneInfo;
import com.novelbio.database.entity.friceDB.Go2Term;
import com.novelbio.database.entity.kegg.KGentry;

public abstract class GOInfoAbs{
	public static final String GO_CC = "cellular component";
	public static final String GO_MF = "molecular function";
	public static final String GO_BP = "biological process";
	public static final String GO_ALL = "all gene ontology";
	
	private static Logger logger = Logger.getLogger(GOInfoAbs.class);
	/**
	 * geneUniID所对应的具体GO信息
	 * @param genUniAccID
	 * @param taxID
	 */
	public GOInfoAbs(String genUniAccID, int taxID) {
		this.genUniAccID = genUniAccID;
		this.taxID = taxID;
	}
	
	String genUniAccID = null;
	int taxID = 0;
	
	boolean booAGene2Gos = false;
	
	ArrayList<AGene2Go> lsAGene2Gos = null;
	/**
	 * 去冗余的保存go信息
	 * key：GOID
	 * value：Gene2Go
	 * 这里的value必须设定为AGene2Go，因为该hash还要容纳其来自不同的CopedID的GO，而其他CopedID的GO很可能与本GO不同，
	 * 来自UniGO或GeneGO
	 */
	HashMap<String,AGene2Go> hashGene2Gos = null;
	/**
	 * 需要设定lsAGene2Gos
	 */
	protected abstract void setGene2Go();
	
	/**
	 * 将多个CopedID的GOInfoAbs放在一起，取并集去冗余
	 * 没有则返回null
	 * @param lsGoInfo 多个GOInfoAbs的list
	 * @return
	 */
	public ArrayList<AGene2Go> getLsGen2Go(ArrayList<GOInfoAbs> lsGoInfo, String GOType) {
		setGene2Go();
		//先将本基因的信息加入Hash表
		for (AGene2Go aGene2Go : getLsGene2Go(GOType)) {
			if (hashGene2Gos.containsKey(aGene2Go.getGOID()))
				continue;
			hashGene2Gos.put(aGene2Go.getGOID(), aGene2Go);
		}
		
		for (GOInfoAbs goInfoAbs : lsGoInfo) {
			for (AGene2Go aGene2Go :  goInfoAbs.getLsGene2Go(GOType)) {
				if (hashGene2Gos.containsKey(aGene2Go.getGOID()))
					continue;
				hashGene2Gos.put(aGene2Go.getGOID(), aGene2Go);
			}
		}
		if (hashGene2Gos == null || hashGene2Gos.size() == 0) {
			return null;
		}
		return ArrayOperate.getArrayListValue(hashGene2Gos);
	}
	/**
	 * 根据具体的GO_TYPE的标记，获得本GeneID的GO信息
	 * @param GOType 如果是GO_ALL，则返回全部的GO信息
	 * @return
	 * 没有则返回null
	 */
	public ArrayList<AGene2Go> getLsGene2Go(String GOType)
	{
		setGene2Go();
		
		if (GOType.equals(GO_BP)) {
			return getLsGoType("P");
		}
		else if (GOType.equals(GO_CC)) {
			return getLsGoType("C");
		}
		else if (GOType.equals(GO_MF)) {
			return getLsGoType("F");
		}
		else if (GOType.equals(GO_ALL)) {
			return lsAGene2Gos;
		}
		else {
			logger.error("UnKnown GOType: "+ GOType );
			return null;
		}
	}
	/**
	 * Gotype 必须是C，F，P
	 * @param GoType
	 * @return
	 * 如果没有该项GO，则返回null
	 */
	private ArrayList<AGene2Go> getLsGoType(String GoType) {
		ArrayList<AGene2Go> lsResult = new ArrayList<AGene2Go>();
		for (AGene2Go aGene2Go : lsAGene2Gos) {
			if (aGene2Go.getFunction().equals(GoType)) {
				lsResult.add(aGene2Go);
			}
		}
		if (lsResult.size() == 0) {
			return null;
		}
		return lsResult;
	}
	/**
	 * 指定GOID，返回具体的GO2Term信息
	 * @param GOID
	 * @return
	 */
	public static Go2Term getGO2Term(String GOID) {
		return Go2Term.getHashGo2Term().get(GOID);
	}
	
}
