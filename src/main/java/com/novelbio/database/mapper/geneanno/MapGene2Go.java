package com.novelbio.database.mapper.geneanno;

import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
 

import com.novelbio.database.domain.geneanno.*;
import com.novelbio.database.util.Util;

public class MapGene2Go {

	
	/**
	 * 用GeneID去查找Gene2Go表
	 * @param GeneID
	 * @return
	 */
	public static ArrayList<Gene2Go> queryGene2Go(long GeneID){
		SqlSession session=Util.getSqlSesFactFriceDB().openSession();
		ArrayList<Gene2Go> gene2GoInfo=null;
		try
		{
			gene2GoInfo= (ArrayList<Gene2Go>)session.selectList("FriceDBSingle.selectGene2GoID",GeneID);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			session.close();
		}
		return gene2GoInfo;
	}
	
	/**
	 * 用Gene2GoInfo类去查找Gene2Go表
	 * 主要是来看本列是否已经存在了
	 * 用geneID和goID去查找数据库
	 * @return
	 */
	public static Gene2Go queryGene2Go(Gene2Go Querygene2GoInfo){
		SqlSession session=Util.getSqlSesFactFriceDB().openSession();
		Gene2Go gene2GoInfo=null;
		try
		{
			gene2GoInfo= (Gene2Go)session.selectOne("FriceDBSingle.selectGene2Go",Querygene2GoInfo);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			session.close();
		}
		return gene2GoInfo;
	}
	
	
	public static void InsertGene2Go(Gene2Go gene2GoInfo){
		SqlSession session=Util.getSqlSesFactFriceDB().openSession();
 		try
		{
			session.insert("FriceDBSingle.insertGene2Go", gene2GoInfo);
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
	public static void upDateGene2Go(Gene2Go gene2GoInfoInfo){
		SqlSession session=Util.getSqlSesFactFriceDB().openSession();
 		try
		{
			session.update("FriceDBSingle.updateGene2Go", gene2GoInfoInfo);
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
