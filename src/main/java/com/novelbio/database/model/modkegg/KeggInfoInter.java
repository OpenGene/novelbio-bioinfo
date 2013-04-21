package com.novelbio.database.model.modkegg;

import java.util.ArrayList;

import com.novelbio.database.domain.kegg.KGentry;
import com.novelbio.database.domain.kegg.KGpathway;

public interface KeggInfoInter {
	/**
	 * geneID或UniID或AccID
	 * 如果是AccID，那么一定是没有GeneID和UniID的
	 */
	public String getGenUniID();
	
	public String getKegID();
	
	public int getTaxID();

	/**
	 * 返回该geneID所对应的KGentry
	 * @return
	 */
	public ArrayList<KGentry> getKgGentries();
	/**
	 * 获得该keggID所对应的KO
	 * 如果没有就返回null
	 */
	public ArrayList<String> getLsKo();
	
	/**
	 * 获得该accID对应的所有不重复的keggpathway对象
	 * <b>不进行blast</b>
	 * @return
	 */
	public ArrayList<KGpathway> getLsKegPath();
	
}
