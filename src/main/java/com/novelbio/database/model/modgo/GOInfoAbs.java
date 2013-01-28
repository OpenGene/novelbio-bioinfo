package com.novelbio.database.model.modgo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.log4j.Logger;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.database.domain.geneanno.AGene2Go;
import com.novelbio.database.domain.geneanno.GOtype;
import com.novelbio.database.domain.geneanno.Go2Term;
import com.novelbio.database.service.servgeneanno.ServGo2Term;

public abstract class GOInfoAbs{

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
	 * 没有则返回空的LsResult
	 * @param lsGoInfo 多个GOInfoAbs的list
	 * @return
	 */
	public ArrayList<AGene2Go> getLsGen2Go(ArrayList<GOInfoAbs> lsGoInfo, GOtype GOType) {
		setGene2Go();
		hashGene2Gos = new HashMap<String, AGene2Go>();
		//先将本基因的信息加入Hash表
		for (AGene2Go aGene2Go : getLsGene2Go(GOType)) {
			if (hashGene2Gos.containsKey(aGene2Go.getGOID()))
				continue;
			hashGene2Gos.put(aGene2Go.getGOID(), aGene2Go);
		}
		if (lsGoInfo != null && lsGoInfo.size() > 0) {
			for (GOInfoAbs goInfoAbs : lsGoInfo) {
				for (AGene2Go aGene2Go : goInfoAbs.getLsGene2Go(GOType)) {
					if (hashGene2Gos.containsKey(aGene2Go.getGOID())) {
						continue;
					}
					hashGene2Gos.put(aGene2Go.getGOID(), aGene2Go);
				}
			}
		}
		if (hashGene2Gos == null || hashGene2Gos.size() == 0) {
			return new ArrayList<AGene2Go>();
		}
		return ArrayOperate.getArrayListValue(hashGene2Gos);
	}
	/**
	 * 根据具体的GO_TYPE的标记，获得本GeneID的GO信息
	 * <br>
	 * GO_BP<br>
	 * GO_CC<br>
	 * GO_MF<br>
	 * GO_ALL<br>
	 * @param GOType Go2Term.GO_BP等，如果是Go2Term.GO_ALL，则返回全部的GO信息
	 * @return
	 * 没有则返回一个空的lsResult
	 */
	public ArrayList<AGene2Go> getLsGene2Go(GOtype GOType) {
		setGene2Go();
		
		if (GOType != GOtype.ALL) {
			return getLsGoType(GOType);
		} else {
			return lsAGene2Gos;
		}
	}
	/**
	 * Gotype 必须是：<br>
	 * FUN_SHORT_BIO_P<br>
	 * FUN_SHORT_CEL_C<br>
	 * FUN_SHORT_MOL_F<br>
	 * @param GoType
	 * @return
	 * 如果没有该项GO，则返回一个空的lsResult
	 */
	private ArrayList<AGene2Go> getLsGoType(GOtype goType) {
		ArrayList<AGene2Go> lsResult = new ArrayList<AGene2Go>();
		HashSet<String> setGOID = new HashSet<String>();
		if (lsAGene2Gos == null) {
			return new ArrayList<AGene2Go>();
		}
		for (AGene2Go aGene2Go : lsAGene2Gos) {
			if (aGene2Go.getFunction() != goType || setGOID.contains(aGene2Go.getGOID())) {
				continue;
			}
			setGOID.add(aGene2Go.getGOID());
			lsResult.add(aGene2Go);
		}
		return lsResult;
	}
	/**
	 * 指定GOID，返回具体的GO2Term信息
	 * @param GOID
	 * @return
	 */
	public static Go2Term getGO2Term(String GOID) {
		ServGo2Term servGo2Term = new ServGo2Term();
		return servGo2Term.queryGo2Term(GOID);
	}
	
}
