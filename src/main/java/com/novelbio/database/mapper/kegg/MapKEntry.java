package com.novelbio.database.mapper.kegg;

import java.util.ArrayList;

import com.novelbio.database.domain.kegg.KGentry;
import com.novelbio.database.mapper.MapperSql;

public interface MapKEntry extends MapperSql {

	
	/**
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
			if test="reactionName !=null"<br>
				and reactionName=#{reactionName}<br>
			/if<br>
			if test="taxID !=null or taxID !=0"<br>
				and taxID=#{taxID}<br>
			/if<br>
			if test="parentID !=null or parentID !=0"<br>
				and parentID=#{parentID}<br>
			/if<br>
		/where<br>
	 * @param KGentry
	 * @return
	 */
	public ArrayList<KGentry> queryLsKGentries(KGentry kGentry);
	
	/**
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
			if test="reactionName !=null"<br>
				and reactionName=#{reactionName}<br>
			/if<br>
			if test="taxID !=null or taxID !=0"<br>
				and taxID=#{taxID}<br>
			/if<br>
			if test="parentID !=null or parentID !=0"<br>
				and parentID=#{parentID}<br>
			/if<br>
		/where<br>
	 * @param KGentry
	 * @return
	 */
	public KGentry queryKGentry(KGentry kGentry);
	
	
	public void insertKGentry(KGentry kGentry);
	
	/**
	 * 目前的升级方式是
	    update entry set <br>
		name = #{name},<br>
		pathName = #{pathName},<br>
		ID = #{id},<br>
		type = #{type},<br>
		reactionName = #{reactionName},<br>
		link = #{linkEntry},<br>
		compNum=#{compNum},<br>
		compID=#{compID},<br>
		parentID=#{parentID}<br>
		<b>where</b> name=#{name}<br>
		and pathName=#{pathName}<br>
		and ID=#{id}<br>
	 * @param kGentry
	 */
	public void updateKGentry(KGentry kGentry);

}
