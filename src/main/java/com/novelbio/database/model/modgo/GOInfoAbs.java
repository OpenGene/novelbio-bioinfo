package com.novelbio.database.model.modgo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.database.DBAccIDSource;
import com.novelbio.database.domain.geneanno.AGene2Go;
import com.novelbio.database.domain.geneanno.DBInfo;
import com.novelbio.database.domain.geneanno.GOtype;
import com.novelbio.database.domain.geneanno.Go2Term;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.service.servgeneanno.ManageGo2Term;

/** 该类内部没有进行延迟初始，所以需要在外部延迟初始该类 */
public abstract class GOInfoAbs {
	private static final Logger logger = Logger.getLogger(GOInfoAbs.class);

	String genUniAccID = null;
	int taxID = 0;
		
	/**
	 * 去冗余的保存go信息
	 * key：GOID
	 * value：Gene2Go
	 * 这里的value必须设定为AGene2Go，因为该hash还要容纳其来自不同的CopedID的GO，而其他CopedID的GO很可能与本GO不同，
	 * 来自UniGO或GeneGO
	 */
	Map<String, AGene2Go> mapGene2Gos = null;
	
	Set<AGene2Go> setUpdate = new HashSet<AGene2Go>();
	/**
	 * geneUniID所对应的具体GO信息
	 * @param genUniAccID
	 * @param taxID
	 */
	public GOInfoAbs(String genUniAccID, int taxID) {
		this.genUniAccID = genUniAccID;
		this.taxID = taxID;
	}
	
	public String getGenUniAccID() {
		return genUniAccID;
	}
	
	public void addGOinfo(GOInfoAbs goInfoAbs) {
		for (String goID : goInfoAbs.mapGene2Gos.keySet()) {
			AGene2Go aGene2Go = mapGene2Gos.get(goID);
			AGene2Go aGene2GoOther = goInfoAbs.mapGene2Gos.get(goID);
			if (aGene2Go == null) {
				aGene2Go = createGene2Go();
				aGene2Go.copyInfo(aGene2GoOther);
				aGene2Go.setGeneUniID(genUniAccID);
				aGene2Go.setTaxID(taxID);
				
				mapGene2Gos.put(goID, aGene2Go);
				setUpdate.add(aGene2Go);
			} else {
				if (aGene2Go.addInfo(aGene2GoOther)) {
					setUpdate.add(aGene2Go);
				}
			}
		}
	}
	
	/**
	 * 需要设定lsAGene2Gos
	 */
	protected abstract void setGene2Go();

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
	public List<AGene2Go> getLsGene2Go(GOtype GOType) {		
		if (GOType != GOtype.ALL) {
			return getLsGoType(GOType);
		} else {
			return new ArrayList<AGene2Go>(mapGene2Gos.values());
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
	private List<AGene2Go> getLsGoType(GOtype goType) {
		ArrayList<AGene2Go> lsResult = new ArrayList<AGene2Go>();
		HashSet<String> setGOID = new HashSet<String>();
		if (mapGene2Gos == null) {
			return new ArrayList<AGene2Go>();
		}
		for (AGene2Go aGene2Go : mapGene2Gos.values()) {
			if (aGene2Go.getFunction() != goType || setGOID.contains(aGene2Go.getGOID())) {
				continue;
			}
			setGOID.add(aGene2Go.getGOID());
			lsResult.add(aGene2Go);
		}
		return lsResult;
	}
	
	/**
	 * 不需要设定 aGene2GoIn 的geneUniID
	 * @param aGene2GoIn
	 */
	public void addGene2GO(AGene2Go aGene2GoIn) {
		if (aGene2GoIn.getGOID() == null || aGene2GoIn.getGOID().equals("")) {
			return;
		}
		String GOID = aGene2GoIn.getGOID().trim();
		
		AGene2Go gene2Go = createGene2Go();
		gene2Go.setGOID(aGene2GoIn.getGOID());
		gene2Go.setTaxID(taxID);
		for (String string : aGene2GoIn.getEvidence()) {
			gene2Go.addEvidence(string);
		}
		for (DBInfo dbInfo : aGene2GoIn.getDataBase()) {
			gene2Go.addDBID(dbInfo);
		}
		gene2Go.addReference(aGene2GoIn.getReference());
		
		if (mapGene2Gos.containsKey(GOID)) {
			AGene2Go aGene2Go = mapGene2Gos.get(GOID);
			if (aGene2Go.addInfo(gene2Go)) {
				setUpdate.add(aGene2Go);
			}
		} else {
			gene2Go.setGeneUniID(getGenUniAccID());
			setUpdate.add(gene2Go);
			mapGene2Gos.put(GOID, gene2Go);
		}
	}
	
	public void addGOid(int taxID, String GOID, DBAccIDSource GOdatabase, String GOevidence,
			List<String> lsGORef, String goQualifiy) {
		if (GOID == null) {
			return;
		}
		GOID = GOID.trim();
		if (GOID.equals("")) {
			return;	
		}
		AGene2Go gene2Go = createGene2Go();
		gene2Go.setGOID(GOID);
		gene2Go.setTaxID(taxID);
		gene2Go.addEvidence(GOevidence);
		gene2Go.addDBName(GOdatabase.toString());
		gene2Go.setQualifier(goQualifiy);
		gene2Go.addReference(lsGORef);
		if (mapGene2Gos.containsKey(GOID)) {
			AGene2Go aGene2Go = mapGene2Gos.get(GOID);
			if (aGene2Go.addInfo(gene2Go)) {
				setUpdate.add(aGene2Go);
			}
		} else {
			gene2Go.setGeneUniID(getGenUniAccID());
			setUpdate.add(gene2Go);
			mapGene2Gos.put(GOID, gene2Go);
		}
	}
	protected abstract AGene2Go createGene2Go();
	
	public void update() {
		for (AGene2Go aGene2Go : setUpdate) {
			save(aGene2Go);
		}
	}
	
	protected abstract void save(AGene2Go aGene2Go);
	
	/**
	 * 指定GOID，返回具体的GO2Term信息
	 * @param GOID
	 * @return
	 */
	public static Go2Term getGO2Term(String GOID) {
		ManageGo2Term servGo2Term = new ManageGo2Term();
		return servGo2Term.queryGo2Term(GOID);
	}
	
	public static GOInfoAbs createGOInfoAbs(int idtype, String genUniID, int taxID) {
		GOInfoAbs goInfoAbs = null;
		if (idtype == GeneID.IDTYPE_GENEID) {
			goInfoAbs = new GOInfoGenID(genUniID, taxID);
		} else if (idtype== GeneID.IDTYPE_UNIID) {
			goInfoAbs = new GOInfoUniID(genUniID, taxID);
		} else {
			goInfoAbs = new GOInfoUniID(genUniID, taxID);
		}
		return goInfoAbs;
	}
	
	/**
	 * 将多个CopedID的GOInfoAbs放在一起，取并集去冗余
	 * 没有则返回空的LsResult
	 * @param lsGoInfo 多个GOInfoAbs的list
	 * @return
	 */
	public static List<AGene2Go> getLsGen2Go(Collection<GOInfoAbs> lsGoInfo, GOtype GOType) {
		Map<String, AGene2Go> mapGene2Gos = new HashMap<String, AGene2Go>();
		if (lsGoInfo == null || lsGoInfo.size() == 0) {
			return new ArrayList<AGene2Go>();
		}
		for (GOInfoAbs goInfoAbs : lsGoInfo) {
			for (AGene2Go aGene2Go : goInfoAbs.getLsGene2Go(GOType)) {
				if (mapGene2Gos.containsKey(aGene2Go.getGOID())) {
					continue;
				}
				mapGene2Gos.put(aGene2Go.getGOID(), aGene2Go);
			}
		}
		if (mapGene2Gos == null || mapGene2Gos.size() == 0) {
			return new ArrayList<AGene2Go>();
		}
		return ArrayOperate.getArrayListValue(mapGene2Gos);
	}

}
