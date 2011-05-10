package com.novelbio.database.DAO.FriceDAO;

import java.util.ArrayList;

import org.apache.ibatis.session.SqlSession;

import com.novelbio.database.entity.friceDB.*;
import com.novelbio.database.util.Util;

public class DaoFSUniGene2Go {

	/**
	 * 用GeneID去查找Gene2Go表
	 * @param GeneID
	 * @return
	 */
	public static ArrayList<UniGene2Go> queryUniGene2Go(String uniProtID){
		SqlSession session=Util.getSqlSesFactFriceDB().openSession();
		ArrayList<UniGene2Go> uniGene2GoInfo=null;
		try
		{
			uniGene2GoInfo= (ArrayList<UniGene2Go>)session.selectList("FriceDBSingle.selectUniGene2GoID",uniProtID);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			session.close();
		}
		return uniGene2GoInfo;
	}
	
	/**
	 * 用Gene2GoInfo类去查找Gene2Go表
	 * 主要是来看本列是否已经存在了
	 * 用geneID和goID去查找数据库
	 * @param GeneID
	 * @return
	 */
	public static UniGene2Go queryUniGene2Go(UniGene2Go QueryUniGene2GoInfo){
		SqlSession session=Util.getSqlSesFactFriceDB().openSession();
		UniGene2Go uniGene2GoInfo=null;
		try
		{
			uniGene2GoInfo= (UniGene2Go)session.selectOne("FriceDBSingle.selectUniGene2Go",QueryUniGene2GoInfo);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			session.close();
		}
		return uniGene2GoInfo;
	}
	
	
	public static void InsertUniGene2Go(UniGene2Go uniGene2GoInfo){
		SqlSession session=Util.getSqlSesFactFriceDB().openSession();
 		try
		{
			session.insert("FriceDBSingle.insertUniGene2Go", uniGene2GoInfo);
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
	 * 升级evidence和reference两项
	 * @param geneInfo
	 */
	public static void upDateUniGene2Go(UniGene2Go uniGene2GoInfoInfo){
		SqlSession session=Util.getSqlSesFactFriceDB().openSession();
 		try
		{
			session.update("FriceDBSingle.updateUniGene2Go", uniGene2GoInfoInfo);
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
