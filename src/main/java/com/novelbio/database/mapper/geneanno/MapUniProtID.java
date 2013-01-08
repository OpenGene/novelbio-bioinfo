package com.novelbio.database.mapper.geneanno;

import java.util.ArrayList;

import org.apache.ibatis.session.SqlSession;

import com.novelbio.database.domain.geneanno.UniProtID;
import com.novelbio.database.mapper.MapperSql;

public interface MapUniProtID extends MapperSql{

	
	/**
	 * 	if test="uniID !=null"<br>
				UniID = #{uniID} <br>
			/if<br>
			if test="accessID !=null"<br>
				and accessID = #{accessID} <br>
		/if<br>
			if test="taxID !=null and taxID !=0"<br>
				and TaxID = #{taxID} <br>
			/if<br>
	 * 主要是来看本列是否已经存在了<br>
	 * @param QueryUniProtID
	 * @return
	 */
	public UniProtID queryUniProtID(UniProtID QueryUniProtID);
	
	/**
	 * 	if test="uniID !=null"<br>
				UniID = #{uniID} <br>
			/if<br>
			if test="accessID !=null"<br>
				and accessID = #{accessID} <br>
		/if<br>
			if test="taxID !=null and taxID !=0"<br>
				and TaxID = #{taxID} <br>
			/if<br>
	 * 主要是来看本列是否已经存在了<br>
	 * @param QueryUniProtID
	 * @return
	 */
	public ArrayList<UniProtID> queryLsUniProtID(UniProtID QueryUniProtID);
	
	public void insertUniProtID(UniProtID UniProtID);
	
	/**
	 * 目前的升级方式是<br>
		update UniProtID <br>
		set<br>
			if test="taxID !=null and taxID !=0"<br>
				TaxID = #{taxID},<br>
			/if<br>
			if test="uniID !=null"<br>
				UniID = #{uniID},<br>
			/if<br>
			if test="dbInfo !=null"<br>
				DataBaseInfo = #{dbInfo} <br>
			/if<br>
			if test="accessID !=null"<br>
				and accessID = #{accessID} <br>
			/if<br>
		/set<br>
		where<br>
			if test="taxID !=null and taxID !=0"<br>
				TaxID = #{taxID}<br>
			/if<br>
			if test="uniID !=null"<br>
				and UniID = #{uniID} <br>
			/if<br>
			if test="accessID !=null"<br>
				and accessID = #{accessID} <br>
			/if<br>
		/where<br>
	 * @param geneInfo
	 */
	public void updateUniProtID(UniProtID UniProtID);

}
