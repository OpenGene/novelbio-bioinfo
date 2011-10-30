package com.novelbio.database.mapper.geneanno;

import java.util.ArrayList;

import org.apache.ibatis.session.SqlSession;

import com.novelbio.database.entity.friceDB.*;
import com.novelbio.database.util.Util;

public class MapFSTaxID {

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
	public static TaxInfo queryTaxInfo(TaxInfo taxInfo){
		SqlSession session=Util.getSqlSesFactFriceDB().openSession();
		TaxInfo taxInfo2=null;
		try
		{
			taxInfo2= (TaxInfo)session.selectOne("FriceDBSingle.selectTaxInfo",taxInfo);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			session.close();
		}
		return taxInfo2;
	}
	
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
	public static ArrayList<TaxInfo> queryLsTaxInfo(TaxInfo taxInfo){
		SqlSession session=Util.getSqlSesFactFriceDB().openSession();
		ArrayList<TaxInfo> lsTaxIDs = null;
		try
		{
			lsTaxIDs= (ArrayList<TaxInfo>)session.selectList("FriceDBSingle.selectTaxInfo",taxInfo);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			session.close();
		}
		return lsTaxIDs;
	}
	
	public static void InsertTaxInfo(TaxInfo taxInfo){
		SqlSession session=Util.getSqlSesFactFriceDB().openSession();
 		try
		{
			session.insert("FriceDBSingle.insertTaxInfo", taxInfo);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			session.close();
		}
	}
	
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
	public static void upDateTaxInfo(TaxInfo taxInfo){
		SqlSession session=Util.getSqlSesFactFriceDB().openSession();
 		try
		{
			session.update("FriceDBSingle.updateTaxInfo", taxInfo);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			session.close();
		}
	}
}
