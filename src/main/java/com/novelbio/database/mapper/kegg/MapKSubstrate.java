package com.novelbio.database.mapper.kegg;

import java.util.ArrayList;

import org.apache.ibatis.session.SqlSession;

import com.novelbio.database.domain.kegg.KGsubstrate;
import com.novelbio.database.mapper.MapperSql;


public interface MapKSubstrate extends MapperSql{
	/**
	 * 用reactionID,pathName,id,name中任意组合去查找substrate表
	 * @param kGsubstrate
	 * @return
	 */
	public ArrayList<KGsubstrate> queryLskgKGsubstrates(KGsubstrate kGsubstrate);
	
	/**
	 * 用reactionID,pathName,id,name中任意组合去查找substrate表
	 * @param kGsubstrate
	 * @return
	 */
	public KGsubstrate queryKGsubstrate(KGsubstrate kGsubstrate);
	
	
	public void insertKGsubstrate(KGsubstrate kGsubstrate);
	
	/**
	 * 目前的升级方式是:<br>
		update substrate set<br>
		pathName = #{pathName},<br>
		reactionID = #{reactionID},<br>
		ID = #{id},<br>
		name = #{name},<br>
		type = #{type}<br>
		where<br>
			if test="reactionID !=null and reactionID !=0"<br>
				 reactionID=#{reactionID}<br>
			/if<br>
			if test="pathName !=null"<br>
				and pathName=#{pathName}<br>
			/if<br>
			if test="id !=null and id !=0"<br>
				and ID=#{id}<br>
			/if<br>
			if test="name !=null"<br>
				and name=#{name}<br>
			/if<br>
		/where<br>
	 * @param kGsubstrate
	 */
	public void updateKGsubstrate(KGsubstrate kGsubstrate);




}
