package com.novelbio.database.mapper.geneanno;

import java.util.ArrayList;

import org.apache.ibatis.session.SqlSession;

import com.novelbio.database.domain.geneanno.TaxInfo;
import com.novelbio.database.mapper.MapperSql;

public interface MapTaxID  extends MapperSql{
	/**
	 * 		where <br>
			if test="taxID !=null and taxID !=0"<br>
				taxID = #{taxID} <br>
			/if<br>
			if test="chnName !=null"<br>
				and chnName = #{chnName} <br>
			/if<br>
			if test="comName !=null"<br>
				and comName = #{comName} <br>
			/if<br>
			if test="latin !=null"<br>
				and latin = #{latin} <br>
			/if<br>
			if test="abbr !=null"<br>
				and abbr = #{abbr} <br>
			/if<br>
	    /where <br>
	 * 主要是来看本列是否已经存在了，返回单个TaxID
	 * @param TaxInfo
	 * @return
	 */
	public TaxInfo queryTaxInfo(TaxInfo taxInfo);
	/**
	 * 		where <br>
			if test="taxID !=null and taxID !=0"<br>
				taxID = #{taxID} <br>
			/if<br>
			if test="chnName !=null"<br>
				and chnName = #{chnName} <br>
			/if<br>
			if test="comName !=null"<br>
				and comName = #{comName} <br>
			/if<br>
			if test="latin !=null"<br>
				and latin = #{latin} <br>
			/if<br>
			if test="abbr !=null"<br>
				and abbr = #{abbr} <br>
			/if<br>
	    /where <br>
	 * 主要是来看本列是否已经存在了，返回单个TaxID
	 * @param TaxInfo
	 * @return
	 */
	public ArrayList<TaxInfo> queryLsTaxInfo(TaxInfo taxInfo);
	
	public void InsertTaxInfo(TaxInfo taxInfo);
	
	/**
	update taxInfo 
		set
			if test="taxID !=null and taxID !=0"
				taxID = #{taxID} 
			/if
			if test="chnName !=null"
				and chnName = #{chnName} 
			/if
			if test="comName !=null"
				and comName = #{comName} 
			/if
			if test="latin !=null"
				and latin = #{latin} 
			/if
			if test="abbr !=null"
				and abbr = #{abbr} 
			/if
		/set
		where taxID = #{taxID} 
	/update	
	 * @param TaxInfo
	 */
	public  void upDateTaxInfo(TaxInfo taxInfo);
}
