package com.novelbio.database.mapper.geneanno;

import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
 

import com.novelbio.database.domain.geneanno.*;
import com.novelbio.database.util.Util;

public class MapGene2Go {

	
	/**
	 * ��GeneIDȥ����Gene2Go��
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
	 * ��Gene2GoInfo��ȥ����Gene2Go��
	 * ��Ҫ�����������Ƿ��Ѿ�������
	 * ��geneID��goIDȥ�������ݿ�
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
	 * ����evidence��reference����
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
