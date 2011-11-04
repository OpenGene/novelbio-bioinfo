package com.novelbio.database.mapper.geneanno;

import java.util.ArrayList;

import org.apache.ibatis.session.SqlSession;

import com.novelbio.database.domain.geneanno.*;
import com.novelbio.database.util.Util;

public class MapUniGeneInfo {
	/**
	 * 用GeneID去查找UniGeneInfo表
	 * @param uniProtID
	 * @return
	 */
	public static ArrayList<UniGeneInfo> queryUniGeneInfo(String uniProtID){
		SqlSession session=Util.getSqlSesFactFriceDB().openSession();
		ArrayList<UniGeneInfo> lsUniGeneInfo=null;
		try
		{
			lsUniGeneInfo= (ArrayList<UniGeneInfo>)session.selectList("FriceDBSingle.selectUniGeneInfoID",uniProtID);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			session.close();
		}
		return lsUniGeneInfo;
	}
	
	/**
	 * 用Gene2GoInfo类去查找UniGeneInfo表
	 * 主要是来看本列是否已经存在了
	 * 用geneID去查找数据库
	 * @param GeneID
	 * @return
	 */
	public static UniGeneInfo queryUniGeneInfo(UniGeneInfo uniGeneInfo){
		SqlSession session=Util.getSqlSesFactFriceDB().openSession();
		UniGeneInfo uniGeneInfo2=null;
		try
		{
			uniGeneInfo2= (UniGeneInfo)session.selectOne("FriceDBSingle.selectUniGeneInfo",uniGeneInfo);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			session.close();
		}
		return uniGeneInfo2;
	}
	
	
	public static void InsertUniGeneInfo(UniGeneInfo uniGeneInfo){
		SqlSession session=Util.getSqlSesFactFriceDB().openSession();
 		try
		{
			session.insert("FriceDBSingle.insertUniGeneInfo", uniGeneInfo);
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
	 * 用geneID查找，升级全部项目，
	 * @param geneInfo
	 */
	public static void upDateUniGeneInfo(UniGeneInfo uniGeneInfo){
		SqlSession session=Util.getSqlSesFactFriceDB().openSession();
 		try
		{
			session.update("FriceDBSingle.updateUniGeneInfo", uniGeneInfo);
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
