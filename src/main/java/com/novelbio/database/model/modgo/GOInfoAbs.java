package com.novelbio.database.model.modgo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.database.DBAccIDSource;
import com.novelbio.database.domain.geneanno.AGene2Go;
import com.novelbio.database.domain.geneanno.GOtype;
import com.novelbio.database.domain.geneanno.Gene2Go;
import com.novelbio.database.domain.geneanno.Go2Term;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.service.servgeneanno.ManageGo2Term;

public abstract class GOInfoAbs{

	private static Logger logger = Logger.getLogger(GOInfoAbs.class);

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
	
	List<AGene2Go> lsUpdate = new ArrayList<AGene2Go>();
	/**
	 * geneUniID所对应的具体GO信息
	 * @param genUniAccID
	 * @param taxID
	 */
	public GOInfoAbs(String genUniAccID, int taxID) {
		this.genUniAccID = genUniAccID;
		this.taxID = taxID;
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
	public Collection<AGene2Go> getLsGene2Go(GOtype GOType) {
		setGene2Go();
		
		if (GOType != GOtype.ALL) {
			return getLsGoType(GOType);
		} else {
			return mapGene2Gos.values();
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
	
	public void addGOid(int taxID, String GOID, DBAccIDSource GOdatabase, String GOevidence,
			String GORef, String gOQualifiy) {
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
		gene2Go.setQualifier(gOQualifiy);
		gene2Go.addReference(GORef);
		if (mapGene2Gos.containsKey(GOID)) {
			AGene2Go aGene2Go = mapGene2Gos.get(GOID);
			if (aGene2Go.addInfo(gene2Go)) {
				lsUpdate.add(aGene2Go);
			}
		} else {
			lsUpdate.add(gene2Go);
		}
	}
	protected abstract AGene2Go createGene2Go();
	
	public void update() {
		for (AGene2Go aGene2Go : lsUpdate) {
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
