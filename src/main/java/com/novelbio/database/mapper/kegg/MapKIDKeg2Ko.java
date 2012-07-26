package com.novelbio.database.mapper.kegg;

import java.util.ArrayList;

import org.apache.ibatis.session.SqlSession;

import com.novelbio.database.domain.kegg.KGIDkeg2Ko;
import com.novelbio.database.mapper.MapperSql;

public interface MapKIDKeg2Ko extends MapperSql {

	/**
		where <br>
			if test="keggID !=null" <br>
				keggID = #{keggID} <br>
			/if<br>
			if test="KO !=null"<br>
				and KO = #{KO} <br>
			/if<br>
			if test="taxID !=null and taxID !=0"<br>
				and taxID = #{taxID} <br>
			/if<br>
	    /where<br>
	 * @param KGIDkeg2Ko
	 * @return
	 */
	public ArrayList<KGIDkeg2Ko> queryLsKGIDkeg2Ko(KGIDkeg2Ko kgiDkeg2Ko);
	
	/**
		where<br>
			if test="keggID !=null"<br>
				keggID = #{keggID} <br>
			/if<br>
			if test="KO !=null"<br>
				and KO = #{KO} <br>
			/if<br>
			if test="taxID !=null and taxID !=0"<br>
				and taxID = #{taxID} <br>
			/if<br>
	    /where<br>
	 * @param KGIDkeg2Ko
	 * @return
	 */
	public KGIDkeg2Ko queryKGIDkeg2Ko(KGIDkeg2Ko kGIDkeg2Ko);
	
	
	public void insertKGIDkeg2Ko(KGIDkeg2Ko kGIDkeg2Ko);
	
	/**
	 * 目前的升级方式是<br>
		update IDKeg2Ko set<br>
		keggID = #{keggID},<br>
		KO = #{KO},<br>
		taxID = #{taxID},<br>
 		where<br>
			if test="keggID !=null"<br>
				keggID = #{keggID} <br>
			/if<br>
			if test="KO !=null"<br>
				and KO = #{KO} <br>
			/if<br>
			if test="taxID !=null and taxID !=0"<br>
				and taxID = #{taxID} <br>
			/if<br>
	    /where<br>
	 * @param kGIDkeg2Ko
	 */
	public void updateKGIDkeg2Ko(KGIDkeg2Ko kGIDkeg2Ko);

}
