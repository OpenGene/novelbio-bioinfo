package com.novelbio.database.mapper.geneanno;

import java.util.ArrayList;

import org.apache.ibatis.session.SqlSession;

import com.novelbio.database.entity.friceDB.*;
import com.novelbio.database.util.Util;

public class MapNCBIID {
	

	/**
	 * 用geneID,accessID,TaxID三个中的任意组合去查找NCBIID表
	 * 主要是来看本列是否已经存在了，返回单个NCBIID<br>
	 * if test="geneID !=0 and geneID !=null" <br>
				GeneID = #{geneID} <br>
			/if<br>
			if test="accessID !=null"<br>
				and accessID = #{accessID} <br>
			/if<br>
			if test="taxID !=0 and taxID !=null"<br>
				and TaxID = #{taxID} <br>
			/if<br>
	 * @param NCBIID
	 * @return
	 */
	public static NCBIID queryNCBIID(NCBIID QueryNCBIID){
		SqlSession session=Util.getSqlSesFactFriceDB().openSession();
		NCBIID nCBIID=null;
		try
		{
			nCBIID= (NCBIID)session.selectOne("FriceDBSingle.selectNCBIID",QueryNCBIID);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			session.close();
		}
		return nCBIID;
	}
	
	/**
	 * 用geneID,accessID,TaxID三个中的任意组合去查找NCBIID表
	 * 主要是来看本列是否已经存在了，返回ListNCBIID
	 * if test="geneID !=0 and geneID !=null" <br>
				GeneID = #{geneID} <br>
			/if<br>
			if test="accessID !=null"<br>
				and accessID = #{accessID} <br>
			/if<br>
			if test="taxID !=0 and taxID !=null"<br>
				and TaxID = #{taxID} <br>
			/if<br>
	 * @param NCBIID
	 * @return
	 */
	public static ArrayList<NCBIID> queryLsNCBIID(NCBIID QueryNCBIID){
		SqlSession session=Util.getSqlSesFactFriceDB().openSession();
		ArrayList<NCBIID> lsNCBIID=null;
		try
		{
			lsNCBIID= (ArrayList<NCBIID>)session.selectList("FriceDBSingle.selectNCBIID",QueryNCBIID);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			session.close();
		}
		return lsNCBIID;
	}
	
	public static void InsertNCBIID(NCBIID nCBIID){
		SqlSession session=Util.getSqlSesFactFriceDB().openSession();
 		try
		{
			session.insert("FriceDBSingle.insertNCBIID", nCBIID);
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
		update NCBIID<br>
		set<br>
			if test="taxID !=null and taxID !=0"<br>
				TaxID = #{taxID},<br>
			/if<br>
			if test="geneID !=null and geneID !=0"<br>
				GeneID = #{geneID},<br>
			/if<br>
			if test="dbInfo !=null"<br>
				DataBaseInfo = #{dbInfo} <br>
			/if<br>
			if test="accessID !=null"<br>
				and accessID = #{accessID} <br>
			/if<br>
		/set<br>
		where<br>
			if test="geneID !=0 and geneID !=null"<br>
				GeneID = #{geneID} <br>
			/if<br>
			if test="accessID !=null"<br>
				and accessID = #{accessID} <br>
			/if<br>
			if test="taxID !=0 and taxID !=null"<br>
				and TaxID = #{taxID} <br>
			/if<br>
	    /where <br>
	 * @param NCBIID
	 */
	public static void upDateNCBIID(NCBIID nCBIID){
		SqlSession session=Util.getSqlSesFactFriceDB().openSession();
 		try
		{
			session.update("FriceDBSingle.updateNCBIID", nCBIID);
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
