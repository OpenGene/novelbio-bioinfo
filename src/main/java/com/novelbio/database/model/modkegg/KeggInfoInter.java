package com.novelbio.database.model.modkegg;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.database.domain.kegg.KGIDkeg2Ko;
import com.novelbio.database.domain.kegg.KGentry;
import com.novelbio.database.domain.kegg.KGpathway;
import com.novelbio.database.model.modgeneid.GeneID;

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
	/////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
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
	/**
	 * 输入blast到的CopedIDs
	 * 返回该geneID所对应的KGentry
	 * 没有则返回一个空的arraylist
	 * @return
	 */
	public ArrayList<KGentry> getLsKgGentries(ArrayList<? extends KeggInfoInter> ls_keggInfo);
	/**
	 * 输入blast到的copedIDs，可以是多个
	 * 返回最后的KGentry结果，包括没有blast的结果
	 * @param lscopedIDs
	 * @return
	 */
	public ArrayList<KGpathway> getLsKegPath(ArrayList<? extends KeggInfoInter> ls_keggInfo);
}
