package com.novelbio.database.mapper.kegg;

import java.util.ArrayList;

import com.novelbio.database.domain.kegg.KGpathway;
import com.novelbio.database.mapper.MapperSql;

public interface MapKPathway extends MapperSql{
	/**
	 * ��number,pathNam,org���������ȥ����entry��
	 * @param KGpathway
	 * @return
	 */
	public ArrayList<KGpathway> queryLsKGpathways(KGpathway kGpathway);
	
	/**
	 * ��number,pathNam,org���������ȥ����entry��
	 * 
	 * @param KGpathway
	 * @return
	 */
	public KGpathway queryKGpathway(KGpathway kGpathway);

	public void insertKGpathway(KGpathway kGpathway);
	
	/**
	 * Ŀǰ��������ʽ��
		update pathway set
		pathName = #{pathName},
		org = #{org},
		number = #{mapNum},
		title = #{title},
		linkUrl = #{linkUrl}
		<b>where</b> pathName = #{pathName}
	 * @param KGpathway
	 */
	public void updateKGpathway(KGpathway kGpathway);
}
