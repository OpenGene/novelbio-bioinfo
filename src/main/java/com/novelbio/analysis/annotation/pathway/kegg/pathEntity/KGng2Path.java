package com.novelbio.analysis.annotation.pathway.kegg.pathEntity;

import java.util.ArrayList;

import com.novelbio.database.domain.kegg.KGentry;
import com.novelbio.database.domain.kegg.noGene.KGNCompInfo;
import com.novelbio.database.domain.kegg.noGene.KGNIdKeg;



/**
 * ����compound�ȵ�pathway����Ϣ
 * @author zong0jie
 *
 */
public class KGng2Path 
{
	KGNIdKeg kgnIdKeg;
	/**
	 * ���滯�����usualName������(�� drug��compound��)��KeggID
	 * @param kgnIdKeg
	 */
	public void setKGNIdKeg(KGNIdKeg kgnIdKeg) {
		this.kgnIdKeg = kgnIdKeg;
	}
	/**
	 * ���滯�����usualName������(�� drug��compound��)��KeggID
	 * @param kgnIdKeg
	 */
	public KGNIdKeg getKGNIDKeg() {
		return this.kgnIdKeg;
	}
	
	KGNCompInfo kgnCompInfo;
	/**
	 * ���滯����ľ�����Ϣ
	 * @param kgnCompInfo
	 */
	public void setKgnCompInfo(KGNCompInfo kgnCompInfo) {
		this.kgnCompInfo = kgnCompInfo;
	}
	/**
	 * ���滯����ľ�����Ϣ
	 * @param kgnCompInfo
	 */
	public KGNCompInfo getKgnCompInfo() {
		return this.kgnCompInfo;
	}
	/**
	 * keggEntry��Ϣ
	 */
	ArrayList<KGentry> lsKGentries;
	/**
	 * keggEntry��Ϣ
	 * @param kGentry
	 */
	public void setLsKGentry(ArrayList<KGentry> lsKGentries) {
		this.lsKGentries = lsKGentries;
	}
	/**
	 * keggEntry��Ϣ
	 * @param kGentry
	 */
	public ArrayList<KGentry> getLsKGentry() {
		return this.lsKGentries;
	}
}
