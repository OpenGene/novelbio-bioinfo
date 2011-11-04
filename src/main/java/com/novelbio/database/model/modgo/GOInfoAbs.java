package com.novelbio.database.model.modgo;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.database.domain.geneanno.AGene2Go;
import com.novelbio.database.domain.geneanno.Go2Term;

public abstract class GOInfoAbs{
	public static final String GO_CC = "cellular component";
	public static final String GO_MF = "molecular function";
	public static final String GO_BP = "biological process";
	public static final String GO_ALL = "all gene ontology";
	
	private static Logger logger = Logger.getLogger(GOInfoAbs.class);
	/**
	 * geneUniID����Ӧ�ľ���GO��Ϣ
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
	 * ȥ����ı���go��Ϣ
	 * key��GOID
	 * value��Gene2Go
	 * �����value�����趨ΪAGene2Go����Ϊ��hash��Ҫ���������Բ�ͬ��CopedID��GO��������CopedID��GO�ܿ����뱾GO��ͬ��
	 * ����UniGO��GeneGO
	 */
	HashMap<String,AGene2Go> hashGene2Gos = null;
	/**
	 * ��Ҫ�趨lsAGene2Gos
	 */
	protected abstract void setGene2Go();
	
	/**
	 * �����CopedID��GOInfoAbs����һ��ȡ����ȥ����
	 * û���򷵻ؿյ�LsResult
	 * @param lsGoInfo ���GOInfoAbs��list
	 * @return
	 */
	public ArrayList<AGene2Go> getLsGen2Go(ArrayList<GOInfoAbs> lsGoInfo, String GOType) {
		setGene2Go();
		hashGene2Gos = new HashMap<String, AGene2Go>();
		//�Ƚ����������Ϣ����Hash��
		for (AGene2Go aGene2Go : getLsGene2Go(GOType)) {
			if (hashGene2Gos.containsKey(aGene2Go.getGOID()))
				continue;
			hashGene2Gos.put(aGene2Go.getGOID(), aGene2Go);
		}
		if (lsGoInfo != null && lsGoInfo.size() > 0) {
			for (GOInfoAbs goInfoAbs : lsGoInfo) {
				for (AGene2Go aGene2Go : goInfoAbs.getLsGene2Go(GOType)) {
					if (hashGene2Gos.containsKey(aGene2Go.getGOID()))
						continue;
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
	 * ���ݾ����GO_TYPE�ı�ǣ���ñ�GeneID��GO��Ϣ
	 * @param GOType �����GO_ALL���򷵻�ȫ����GO��Ϣ
	 * @return
	 * û���򷵻�һ���յ�lsResult
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
			return new ArrayList<AGene2Go>();
		}
	}
	/**
	 * Gotype ������C��F��P
	 * @param GoType
	 * @return
	 * ���û�и���GO���򷵻�һ���յ�lsResult
	 */
	private ArrayList<AGene2Go> getLsGoType(String GoType) {
		ArrayList<AGene2Go> lsResult = new ArrayList<AGene2Go>();
		for (AGene2Go aGene2Go : lsAGene2Gos) {
			if (aGene2Go.getFunction().equals(GoType)) {
				lsResult.add(aGene2Go);
			}
		}
		return lsResult;
	}
	/**
	 * ָ��GOID�����ؾ����GO2Term��Ϣ
	 * @param GOID
	 * @return
	 */
	public static Go2Term getGO2Term(String GOID) {
		return Go2Term.getHashGo2Term().get(GOID);
	}
	
}
