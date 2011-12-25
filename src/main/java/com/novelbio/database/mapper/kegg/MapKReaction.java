package com.novelbio.database.mapper.kegg;

import java.util.ArrayList;

import com.novelbio.database.domain.kegg.KGreaction;
import com.novelbio.database.mapper.MapperSql;

public interface MapKReaction extends MapperSql {

	
	/**
	 * ��name,pathNam,ID���������ȥ����reaction��
	 * @param kGreaction
	 * @return
	 */
	public ArrayList<KGreaction> queryLsKGreactions(KGreaction kGreaction);
	
	/**
	 * ��name,pathNam,ID���������ȥ����reaction��
	 * @param kGreaction
	 * @return
	 */
	public KGreaction queryKGreaction(KGreaction kGreaction);
	
	
	public void insertKGreaction(KGreaction kGreaction);
	
	/**
	 * Ŀǰ��������ʽ��:<br>
		update reaction set<br>
		pathName = #{pathName},<br>
		ID = #{id},<br>
		name = #{name},<br>
		type = #{type},<br>
		altName = #{alt},<br>
		where<br>
			if test="name !=null"<br>
				 name=#{name}<br>
			/if<br>
			if test="pathName !=null"<br>
				and pathName=#{pathName}<br>
			/if<br>
			if test="id !=null and id !=0"<br>
				and ID=#{id}<br>
			/if<br>
		/where<br>
	 * @param KGreaction
	 */
	public void updateKGreaction(KGreaction kGreaction);

}
