package com.novelbio.analysis.annotation.pathway.kegg.pathEntity;

import java.util.ArrayList;
import java.util.List;

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
	 * ��ͨ��blast��õ�KO list���룬��ñ�������Ӧ��KGentry list
	 * ���û�оͷ���null
	 */
	public ArrayList<KGentry> getBlastQInfo(List<String> lsKO);
	/**
	 * ��ø�accID��Ӧ�����в��ظ���keggpathway����
	 * <b>���Ҫ��blast�Ľ������Ҫ��ִ��getBlastQInfo����</b>��Ҳ��������һ��copedid�ķ�����ö�Ӧ��lsQKegEntities��Ϣ<br>
	 * <b>��������������Ѿ�����pathway��Ϣ����ô�Ͳ�����blast</b>
	 * @return
	 */
	public ArrayList<KGpathway> getLsKegPath();
	
}
