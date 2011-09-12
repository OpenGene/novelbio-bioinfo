package com.novelbio.analysis.annotation.pathway.kegg.pathEntity;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.analysis.annotation.copeID.CopedID;
import com.novelbio.database.DAO.KEGGDAO.DaoKIDKeg2Ko;
import com.novelbio.database.entity.kegg.KGIDkeg2Ko;
import com.novelbio.database.entity.kegg.KGentry;
import com.novelbio.database.entity.kegg.KGpathway;

public interface KeggInfoInter {
	
	
	
	
	
	/**
	 * geneID��UniID��AccID
	 * �����AccID����ôһ����û��GeneID��UniID��
	 */
	public String getGenUniID();
	
	public String getKegID();
	
	public int getTaxID();

	/**
	 * ���ظ�geneID����Ӧ��KGentry
	 * @return
	 */
	public ArrayList<KGentry> getKgGentries();
	/////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * ��ø�keggID����Ӧ��KO
	 * ���û�оͷ���null
	 */
	public ArrayList<String> getLsKo();
	
	/**
	 * ��ø�accID��Ӧ�����в��ظ���keggpathway����
	 * <b>������blast</b>
	 * @return
	 */
	public ArrayList<KGpathway> getLsKegPath();
	/**
	 * ����blast����CopedIDs
	 * ���ظ�geneID����Ӧ��KGentry
	 * @return
	 */
	public ArrayList<KGentry> getLsKgGentries(ArrayList<? extends KeggInfoInter> ls_keggInfo);
	/**
	 * ����blast����copedIDs�������Ƕ��
	 * ��������KGentry���������û��blast�Ľ��
	 * @param lscopedIDs
	 * @return
	 */
	public ArrayList<KGpathway> getLsKegPath(ArrayList<? extends KeggInfoInter> ls_keggInfo);
}
