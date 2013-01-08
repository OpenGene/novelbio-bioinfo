package com.novelbio.database.mapper.kegg;

import java.util.ArrayList;

import org.apache.ibatis.session.SqlSession;

import com.novelbio.database.domain.kegg.KGpathRelation;
import com.novelbio.database.mapper.MapperSql;

public interface MapKPathRelation extends MapperSql{
	/**
	 * if test="pathName !=null" <br>
				 pathName=#{pathName}<br>
			/if<br>
			if test="scrPath !=null"<br>
				and scrPath=#{scrPath}<br>
			/if<br>
			if test="trgPath !=null"<br>
				and trgPath=#{trgPath}<br>
			/if<br>
	 * @param kGpathRelation
	 * @return
	 */
	public ArrayList<KGpathRelation> queryLskGpathRelations(KGpathRelation kGpathRelation);
	
	/**
	 * if test="pathName !=null" <br>
				 pathName=#{pathName}<br>
			/if<br>
			if test="scrPath !=null"<br>
				and scrPath=#{scrPath}<br>
			/if<br>
			if test="trgPath !=null"<br>
				and trgPath=#{trgPath}<br>
			/if<br>
	 * @param kGpathRelation
	 * @return
	 */
	public KGpathRelation queryKGpathRelation(KGpathRelation kGpathRelation);
	
	public void insertKGpathRelation(KGpathRelation kGpathRelation);
	
	/**
	 * 目前的升级方式是
		update substrate <br>
		set<br>
			if test="pathName !=null"<br>
				 pathName=#{pathName},<br>
			/if<br>
			if test="scrPath !=null"<br>
				scrPath=#{scrPath},<br>
			/if<br>
			if test="trgPath !=null"<br>
				trgPath=#{trgPath}<br>
			/if<br>
			if test="type !=null"<br>
				type=#{type}<br>
			/if<br>
		/set<br>
		where<br>
			if test="pathName !=null"<br>
				 pathName=#{pathName}<br>
			/if<br>
			if test="scrPath !=null"<br>
				and scrPath=#{scrPath}<br>
			/if<br>
			if test="trgPath !=null"<br>
				and trgPath=#{trgPath}<br>
			/if<br>
		/where<br>
	 * @param kGentry
	 */
	public void updateKGpathRelation(KGpathRelation kGpathRelation);
}
