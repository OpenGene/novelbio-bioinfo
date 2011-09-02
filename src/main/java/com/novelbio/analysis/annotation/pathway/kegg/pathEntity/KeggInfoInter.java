package com.novelbio.analysis.annotation.pathway.kegg.pathEntity;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.database.DAO.KEGGDAO.DaoKIDKeg2Ko;
import com.novelbio.database.entity.kegg.KGIDkeg2Ko;
import com.novelbio.database.entity.kegg.KGentry;
import com.novelbio.database.entity.kegg.KGpathway;

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
	 * 将通过blast获得的KO list放入，获得本物种相应的KGentry list
	 * 如果没有就返回null
	 */
	public ArrayList<KGentry> getBlastQInfo(List<String> lsKO);
	/**
	 * 获得该accID对应的所有不重复的keggpathway对象
	 * <b>如果要用blast的结果，需要先执行getBlastQInfo方法</b>，也就是用另一个copedid的方法获得对应的lsQKegEntities信息<br>
	 * <b>但是如果本基因已经有了pathway信息，那么就不进行blast</b>
	 * @return
	 */
	public ArrayList<KGpathway> getLsKegPath();
	
}
