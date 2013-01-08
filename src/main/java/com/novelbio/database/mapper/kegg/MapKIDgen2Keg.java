package com.novelbio.database.mapper.kegg;

import java.util.ArrayList;
import com.novelbio.database.domain.kegg.KGIDgen2Keg;
import com.novelbio.database.mapper.MapperSql;

public interface MapKIDgen2Keg extends MapperSql {


	/**
		where
			if test="geneID !=null and geneID !=0"
				 geneID=#{geneID}
			/if
			if test="keggID !=null"
				and keggID=#{keggID}
			/if
			if test="taxID !=null and taxID !=0"
				and taxID=#{taxID}
			/if
		/where
	 * @param KGIDgen2Keg
	 * @return
	 */
	public ArrayList<KGIDgen2Keg> queryLsKGIDgen2Keg(KGIDgen2Keg kgIDgen2Keg);
	
	/**
		where
			if test="geneID !=null and geneID !=0"
				 geneID=#{geneID}
			/if
			if test="keggID !=null"
				and keggID=#{keggID}
			/if
			if test="taxID !=null and id !=0"
				and taxID=#{taxID}
			/if
		/where
	 * @param KGIDgen2Keg
	 * @return
	 */
	public KGIDgen2Keg queryKGIDgen2Keg(KGIDgen2Keg kGIDgen2Keg);
	
	
	public void insertKGIDgen2Keg(KGIDgen2Keg kGIDgen2Keg);
	
	/**
	 * 目前的升级方式是
		update IDgen2Keg set
		geneID = #{geneID},
		keggID = #{keggID},
		taxID = #{taxID},
		where
			if test="geneID !=null and geneID !=0"
				 geneID=#{geneID}
			/if
			if test="keggID !=null"
				and keggID=#{keggID}
			/if
			if test="taxID !=null and id !=0"
				and taxID=#{taxID}
			/if
		/where
	 * @param kGIDgen2Keg
	 */
	public void updateKGIDgen2Keg(KGIDgen2Keg kGIDgen2Keg);
}
