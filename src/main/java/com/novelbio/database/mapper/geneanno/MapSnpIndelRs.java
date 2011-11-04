package com.novelbio.database.mapper.geneanno;

import java.util.ArrayList;
import com.novelbio.database.domain.geneanno.SnpIndelRs;
import com.novelbio.database.mapper.MapperSql;

public interface MapSnpIndelRs extends MapperSql{
	/**
	 * 		select *
		from snpindelrs
		where <br>
			if test="snpRsID != 0 and snpRsID !=null" <br>
				snpRsID = #{snpRsID} <br>
			/if <br>
			if test="taxID != 0" <br>
				and taxID = #{taxID} <br>
			/if <br>
			if test="locStart != 0" <br>
				and locStart = #{locStart} <br>
			/if <br>
			if test="locEnd != 0" <br>
				and locEnd = #{locEnd} <br>
			/if <br>
			if test="strand != null" <br>
				and strand = #{strand} <br>
			/if <br>
			if test="molType !=null and molType != '' " <br>
				and molType = #{molType} <br>
			/if <br>
			if test="type !=null and type != '' " <br>
				and type = #{type} <br>
			/if <br>
			if test="locType !=null and locType != '' " <br>
				and locType = #{locType} <br>
			/if <br>
			if test="weight != 0" <br>
				and weight = #{weight} <br>
			/if <br>
	    /where
	 * @param snpIndelRs
	 * @return
	 */
	public SnpIndelRs querySnpIndelRs(SnpIndelRs snpIndelRs);
	/**
	 * 		select *
		from snpindelrs
		where <br>
			if test="snpRsID != 0 and snpRsID !=null" <br>
				snpRsID = #{snpRsID} <br>
			/if <br>
			if test="taxID != 0" <br>
				and taxID = #{taxID} <br>
			/if <br>
			if test="locStart != 0" <br>
				and locStart = #{locStart} <br>
			/if <br>
			if test="locEnd != 0" <br>
				and locEnd = #{locEnd} <br>
			/if <br>
			if test="strand != null" <br>
				and strand = #{strand} <br>
			/if <br>
			if test="molType !=null and molType != '' " <br>
				and molType = #{molType} <br>
			/if <br>
			if test="type !=null and type != '' " <br>
				and type = #{type} <br>
			/if <br>
			if test="locType !=null and locType != '' " <br>
				and locType = #{locType} <br>
			/if <br>
			if test="weight != 0" <br>
				and weight = #{weight} <br>
			/if <br>
	    /where
	 * @param snpIndelRs
	 * @return
	 */
	public ArrayList<SnpIndelRs> queryLsSnpIndelRs(SnpIndelRs snpIndelRs);
	
//	public void insertSnpIndelRs(SnpIndelRs snpIndelRs);
	
//	public void upDateSnpIndelRs(SnpIndelRs snpIndelRs);

}
