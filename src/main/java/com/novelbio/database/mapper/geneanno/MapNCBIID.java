package com.novelbio.database.mapper.geneanno;

import java.util.ArrayList;

import org.apache.ibatis.session.SqlSession;

import com.novelbio.database.domain.geneanno.*;
import com.novelbio.database.mapper.MapperSql;

public interface MapNCBIID extends MapperSql{
	

	/**
	 * 用geneID,accessID,TaxID三个中的任意组合去查找NCBIID表
	 * 主要是来看本列是否已经存在了，返回单个NCBIID<br>
	 * if test="geneID !=0 and geneID !=null" <br>
				GeneID = #{geneID} <br>
			/if<br>
			if test="accessID !=null"<br>
				and accessID = #{accessID} <br>
			/if<br>
			if test="taxID !=0 and taxID !=null"<br>
				and TaxID = #{taxID} <br>
			/if<br>
	 * @param NCBIID
	 * @return
	 */
	public NCBIID queryNCBIID(NCBIID QueryNCBIID);
	
	/**
	 * 用geneID,accessID,TaxID三个中的任意组合去查找NCBIID表
	 * 主要是来看本列是否已经存在了，返回ListNCBIID
	 * if test="geneID !=0 and geneID !=null" <br>
				GeneID = #{geneID} <br>
			/if<br>
			if test="accessID !=null"<br>
				and accessID = #{accessID} <br>
			/if<br>
			if test="taxID !=0 and taxID !=null"<br>
				and TaxID = #{taxID} <br>
			/if<br>
	 * @param NCBIID
	 * @return
	 */
	public ArrayList<NCBIID> queryLsNCBIID(NCBIID QueryNCBIID);
	
	public void insertNCBIID(NCBIID nCBIID);
	
	/**
	 * 目前的升级方式是<br>
		update NCBIID<br>
		set<br>
			if test="taxID !=null and taxID !=0"<br>
				TaxID = #{taxID},<br>
			/if<br>
			if test="geneID !=null and geneID !=0"<br>
				GeneID = #{geneID},<br>
			/if<br>
			if test="dbInfo !=null"<br>
				DataBaseInfo = #{dbInfo} <br>
			/if<br>
			if test="accessID !=null"<br>
				and accessID = #{accessID} <br>
			/if<br>
		/set<br>
		where<br>
			if test="geneID !=0 and geneID !=null"<br>
				GeneID = #{geneID} <br>
			/if<br>
			if test="accessID !=null"<br>
				and accessID = #{accessID} <br>
			/if<br>
	    /where <br>
	 * @param NCBIID
	 */
	public void updateNCBIID(NCBIID nCBIID);
}
