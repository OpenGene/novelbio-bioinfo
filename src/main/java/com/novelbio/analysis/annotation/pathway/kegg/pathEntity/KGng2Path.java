package com.novelbio.analysis.annotation.pathway.kegg.pathEntity;

import java.util.ArrayList;

import com.novelbio.database.domain.kegg.KGentry;
import com.novelbio.database.domain.kegg.noGene.KGNCompInfo;
import com.novelbio.database.domain.kegg.noGene.KGNIdKeg;



/**
 * 保存compound等到pathway的信息
 * @author zong0jie
 *
 */
public class KGng2Path 
{
	KGNIdKeg kgnIdKeg;
	/**
	 * 保存化合物的usualName，属性(如 drug、compound等)、KeggID
	 * @param kgnIdKeg
	 */
	public void setKGNIdKeg(KGNIdKeg kgnIdKeg) {
		this.kgnIdKeg = kgnIdKeg;
	}
	/**
	 * 保存化合物的usualName，属性(如 drug、compound等)、KeggID
	 * @param kgnIdKeg
	 */
	public KGNIdKeg getKGNIDKeg() {
		return this.kgnIdKeg;
	}
	
	KGNCompInfo kgnCompInfo;
	/**
	 * 保存化合物的具体信息
	 * @param kgnCompInfo
	 */
	public void setKgnCompInfo(KGNCompInfo kgnCompInfo) {
		this.kgnCompInfo = kgnCompInfo;
	}
	/**
	 * 保存化合物的具体信息
	 * @param kgnCompInfo
	 */
	public KGNCompInfo getKgnCompInfo() {
		return this.kgnCompInfo;
	}
	/**
	 * keggEntry信息
	 */
	ArrayList<KGentry> lsKGentries;
	/**
	 * keggEntry信息
	 * @param kGentry
	 */
	public void setLsKGentry(ArrayList<KGentry> lsKGentries) {
		this.lsKGentries = lsKGentries;
	}
	/**
	 * keggEntry信息
	 * @param kGentry
	 */
	public ArrayList<KGentry> getLsKGentry() {
		return this.lsKGentries;
	}
}
