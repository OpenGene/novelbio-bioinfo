package com.novelbio.database.mapper.geneanno;

import java.util.ArrayList;

import org.apache.ibatis.session.SqlSession;

import com.novelbio.database.domain.geneanno.*;
import com.novelbio.database.util.Util;

public class MapUniProtID {
	
	/**
	 * 	if test="uniID !=null"<br>
				UniID = #{uniID} <br>
			/if<br>
			if test="accessID !=null"<br>
				and accessID = #{accessID} <br>
		/if<br>
			if test="taxID !=null and taxID !=0"<br>
				and TaxID = #{taxID} <br>
			/if<br>
	 * 主要是来看本列是否已经存在了<br>
	 * @param QueryUniProtID
	 * @return
	 */
	public static UniProtID queryUniProtID(UniProtID QueryUniProtID){
		SqlSession session=Util.getSqlSesFactFriceDB().openSession();
		UniProtID UniProtID=null;
		try
		{
			UniProtID= (UniProtID)session.selectOne("FriceDBSingle.selectUniProtID",QueryUniProtID);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			session.close();
		}
		return UniProtID;
	}
	
	/**
	 * 	if test="uniID !=null"<br>
				UniID = #{uniID} <br>
			/if<br>
			if test="accessID !=null"<br>
				and accessID = #{accessID} <br>
		/if<br>
			if test="taxID !=null and taxID !=0"<br>
				and TaxID = #{taxID} <br>
			/if<br>
	 * 主要是来看本列是否已经存在了<br>
	 * @param QueryUniProtID
	 * @return
	 */
	public static ArrayList<UniProtID> queryLsUniProtID(UniProtID QueryUniProtID){
		SqlSession session=Util.getSqlSesFactFriceDB().openSession();
		ArrayList<UniProtID> lsUniProtID=null;
		try
		{
			lsUniProtID= (ArrayList<UniProtID>)session.selectList("FriceDBSingle.selectUniProtID",QueryUniProtID);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			session.close();
		}
		return lsUniProtID;
	}
	
	public static void InsertUniProtID(UniProtID UniProtID){
		SqlSession session=Util.getSqlSesFactFriceDB().openSession();
 		try
		{
			session.insert("FriceDBSingle.insertUniProtID", UniProtID);
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
	 * 目前的升级方式是<br>
		update UniProtID <br>
		set<br>
			if test="taxID !=null and taxID !=0"<br>
				TaxID = #{taxID},<br>
			/if<br>
			if test="uniID !=null"<br>
				UniID = #{uniID},<br>
			/if<br>
			if test="dbInfo !=null"<br>
				DataBaseInfo = #{dbInfo} <br>
			/if<br>
			if test="accessID !=null"<br>
				and accessID = #{accessID} <br>
			/if<br>
		/set<br>
		where<br>
			if test="taxID !=null and taxID !=0"<br>
				TaxID = #{taxID}<br>
			/if<br>
			if test="uniID !=null"<br>
				and UniID = #{uniID} <br>
			/if<br>
			if test="accessID !=null"<br>
				and accessID = #{accessID} <br>
			/if<br>
		/where<br>
	 * @param geneInfo
	 */
	public static void upDateUniProt(UniProtID UniProtID){
		SqlSession session=Util.getSqlSesFactFriceDB().openSession();
 		try
		{
			session.update("FriceDBSingle.updateUniProtID", UniProtID);
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
