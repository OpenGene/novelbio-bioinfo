package DAO.FriceDAO;

import java.util.ArrayList;

import org.apache.ibatis.session.SqlSession;

import util.Util;
import entity.friceDB.GeneInfo;

public class DaoFSGeneInfo {
	
	/**
	 * 用GeneID去查找GeneInfo表
	 * @param GeneID
	 * @return
	 */
	public static ArrayList<GeneInfo> queryLsGeneInfo(long GeneID){
		SqlSession session=Util.getSqlSesFactFriceDB().openSession();
		ArrayList<GeneInfo> lsGeneInfo=null;
		try
		{
			lsGeneInfo= (ArrayList<GeneInfo>)session.selectList("FriceDBSingle.selectGeneInfoID",GeneID);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			session.close();
		}
		return lsGeneInfo;
	}
	
	/**
	 * 用GeneInfo类去查找GeneInfo表
	 * 主要是来看本列是否已经存在了
	 * 用geneID去查找数据库
	 * 	@param geneInfo
	 * @return
	 */
	public static ArrayList<GeneInfo> queryLsGeneInfo(GeneInfo geneInfo){
		SqlSession session=Util.getSqlSesFactFriceDB().openSession();
		ArrayList<GeneInfo> lsGeneInfo=null;
		try
		{
			lsGeneInfo= (ArrayList<GeneInfo>)session.selectList("FriceDBSingle.selectGeneInfo",geneInfo);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			session.close();
		}
		return lsGeneInfo;
	}
	/**
	 * 用GeneInfo类去查找GeneInfo表
	 * 主要是来看本列是否已经存在了
	 * 用geneID去查找数据库
	 * @param geneInfo
	 * @return
	 */
	public static GeneInfo queryGeneInfo(GeneInfo geneInfo){
		SqlSession session=Util.getSqlSesFactFriceDB().openSession();
		GeneInfo geneInfo2=null;
		try
		{
			geneInfo2= (GeneInfo)session.selectOne("FriceDBSingle.selectGeneInfo",geneInfo);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			session.close();
		}
		return geneInfo2;
	}
	
	
	public static void InsertGeneInfo(GeneInfo geneInfo){
		SqlSession session=Util.getSqlSesFactFriceDB().openSession();
 		try
		{
			session.insert("FriceDBSingle.insertGeneInfo", geneInfo);
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
	public static void upDateGeneInfo(GeneInfo geneInfo){
		SqlSession session=Util.getSqlSesFactFriceDB().openSession();
 		try
		{
			session.update("FriceDBSingle.updateGeneInfo", geneInfo);
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
