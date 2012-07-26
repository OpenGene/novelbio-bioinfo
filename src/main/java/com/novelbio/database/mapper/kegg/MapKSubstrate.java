package com.novelbio.database.mapper.kegg;

import java.util.ArrayList;

import org.apache.ibatis.session.SqlSession;

import com.novelbio.database.domain.kegg.KGsubstrate;
import com.novelbio.database.mapper.MapperSql;


public interface MapKSubstrate extends MapperSql{
	/**
	 * ��reactionID,pathName,id,name���������ȥ����substrate��
	 * @param kGsubstrate
	 * @return
	 */
	public ArrayList<KGsubstrate> queryLskgKGsubstrates(KGsubstrate kGsubstrate);
	
	/**
	 * ��reactionID,pathName,id,name���������ȥ����substrate��
	 * @param kGsubstrate
	 * @return
	 */
	public KGsubstrate queryKGsubstrate(KGsubstrate kGsubstrate);
	
	
	public void insertKGsubstrate(KGsubstrate kGsubstrate);
	
	/**
	 * Ŀǰ��������ʽ��:<br>
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
