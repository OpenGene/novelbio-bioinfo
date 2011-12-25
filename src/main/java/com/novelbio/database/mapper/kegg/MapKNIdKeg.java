package com.novelbio.database.mapper.kegg;

import java.util.ArrayList;

import com.novelbio.database.domain.kegg.noGene.KGNIdKeg;
import com.novelbio.database.mapper.MapperSql;

public interface MapKNIdKeg extends MapperSql{


	/**
      where <br>
			if test="kegID != null and kegID != '' "<br>
				 kegID = #{kegID}<br>
			/if<br>
			if test="usualName !=null and usualName != '' "<br>
				and usualName = #{usualName}<br>
			/if<br>
		/where<br>
	 * @param KGNIdKeg<br>
	 * @return
	 */
	public ArrayList<KGNIdKeg> queryLsKGNIdKeg(KGNIdKeg kgnIdKeg);
	
	/**
    where <br>
			if test="kegID != null and kegID != '' "<br>
				 kegID = #{kegID}<br>
			/if<br>
			if test="usualName !=null and usualName != '' "<br>
				and usualName = #{usualName}<br>
			/if<br>
		/where<br>
	 * @param KGNIdKeg<br>
	 * @return
	 */
	public KGNIdKeg queryKGNIdKeg(KGNIdKeg kgnIdKeg);
	
	
	public void insertKGNIdKeg(KGNIdKeg kgnIdKeg);
	
	/**
	 * 目前的升级方式是<br>
		update idKeg set<br>
		set<br>
			if test="kegID !=null"<br>
				 kegID = #{kegID},<br>
			/if<br>
			if test="usualName !=null"<br>
				usualName = #{usualName},<br>
			/if<br>
			if test="attribute !=null"<br>
				attribute = #{attribute}<br>
			/if<br>
		/set<br>
		where<br>
			if test="kegID != null and kegID != '' "<br>
				 kegID = #{kegID}<br>
			/if<br>
			if test="usualName !=null and usualName != '' "<br>
				and usualName = #{usualName}<br>
			/if<br>
		/where<br>
	 * @param kgnIdKeg
	 */
	public void updateKGNIdKeg(KGNIdKeg kgnIdKeg);


}
