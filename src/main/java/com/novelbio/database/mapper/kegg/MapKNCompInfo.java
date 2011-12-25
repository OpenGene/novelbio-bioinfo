package com.novelbio.database.mapper.kegg;

import java.util.ArrayList;

import com.novelbio.database.domain.kegg.noGene.KGNCompInfo;
import com.novelbio.database.domain.kegg.noGene.KGNIdKeg;
import com.novelbio.database.mapper.MapperSql;
/**
 * kegg的化合物表
 * @author zong0jie
 *
 */
public interface MapKNCompInfo extends MapperSql {

	/**
		select *
		from kgComp
		where kegID = #{kegID}
	 * @param KGNIdKeg<br>
	 * @return
	 */
	public ArrayList<KGNCompInfo> queryLsKGNCompInfo(KGNIdKeg kgnIdKeg);
	
	/**
	select *
	from kgComp
	where kegID = #{kegID}
 * @param KGNIdKeg<br>
 * @return
 */
	public KGNCompInfo queryKGNCompInfo(KGNIdKeg kgnIdKeg);
	
	
	public void insertKGNCompInfo(KGNCompInfo kgnCompInfo);
	
	/**
	 * 目前的升级方式是<br>
	update kgComp set<br>
		set<br>
			if test="kegID !=null"<br>
				 kegID=#{kegID},<br>
			/if<br>
			if test="usualName !=null"<br>
				usualName = #{usualName},<br>
			/if<br>
			if test="formula !=null"<br>
				formula=#{formula},<br>
			/if<br>
			if test="mass !=null and mass != 0"<br>
				mass = #{mass},<br>
			/if<br>
			if test="remark !=null "<br>
				remark = #{remark},<br>
			/if<br>
			if test="comment !=null "<br>
				comment = #{comment}<br>
			/if<br>
		/set<br>
		where kegID = #{kegID}<br>
	 * @param kgnCompInfo
	 */
	public void updateKGNCompInfo(KGNCompInfo kgnCompInfo);
}
