package DAO.FriceDAO;

import java.util.ArrayList;

import org.apache.ibatis.session.SqlSession;

import util.Util;
import entity.friceDB.GeneInfo;

public class DaoFSGeneInfo {
	
	/**
	 * ��GeneIDȥ����GeneInfo��
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
	 * ��GeneInfo��ȥ����GeneInfo��
	 * ��Ҫ�����������Ƿ��Ѿ�������
	 * ��geneIDȥ�������ݿ�
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
	 * ��GeneInfo��ȥ����GeneInfo��
	 * ��Ҫ�����������Ƿ��Ѿ�������
	 * ��geneIDȥ�������ݿ�
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
	 * ��geneID���ң�����ȫ����Ŀ��
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
