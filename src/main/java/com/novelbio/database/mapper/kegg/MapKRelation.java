package com.novelbio.database.mapper.kegg;

import java.util.ArrayList;

import org.apache.ibatis.session.SqlSession;

import com.novelbio.database.domain.kegg.KGrelation;
import com.novelbio.database.mapper.MapperSql;
import com.novelbio.database.util.Util;


public interface MapKRelation extends MapperSql {

	
	/**
	 * ��pathname,entry1,entry2,type,sybtypeName���������ȥ����relation��
	 * @param kGrelation
	 * @return
	 */
	public ArrayList<KGrelation> queryLsKGrelations(KGrelation kGrelation);
	
	/**
	 * ��pathname,entry1,entry2,type,sybtypeName���������ȥ����relation��
	 * @param kGrelation
	 * @return
	 */
	public KGrelation queryKGrelation(KGrelation kGrelation);
	
	
	public void insertKGrelation(KGrelation kGrelation);
	
	/**
	 * Ŀǰ��������ʽ��:<br>
		update geneInfo set<br>
		pathname = #{pathname},<br>
		entry1ID = #{entry1},<br>
		entry2ID = #{entry2},<br>
		type = #{type},<br>
		sybtypeName = #{sybtypeName},<br>
		sybtypeValue = #{sybtypeValue}<br>
		where
			if test="pathname !=null"<br>
				 pathname=#{pathname}<br>
			/if<br>
			if test="entry1 !=null and entry1 != 0"<br>
				and entry1ID=#{entry1}<br>
			/if<br>
			if test="entry2 !=null and entry2 !=0"<br>
				and entry2ID=#{entry2}<br>
			/if<br>
			if test="type !=null "<br>
				and type=#{type}<br>
			/if<br>
			if test="sybtypeName !=null "<br>
				and sybtypeName=#{sybtypeName}<br>
			/if<br>
		/where<br>
	 * @param KGrelation
	 */
	public void updateKGrelation(KGrelation kGrelation);



}
