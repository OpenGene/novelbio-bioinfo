package com.novelbio.database.DAO.FriceDAO;

import java.util.ArrayList;

import org.apache.ibatis.session.SqlSession;
import com.novelbio.database.entity.friceDB.*;
import com.novelbio.database.util.Util;

public class DaoFSUniGeneInfo {
	/**
	 * ��GeneIDȥ����UniGeneInfo��
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
	 * ��Gene2GoInfo��ȥ����UniGeneInfo��
	 * ��Ҫ�����������Ƿ��Ѿ�������
	 * ��geneIDȥ�������ݿ�
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
	 * ��geneID���ң�����ȫ����Ŀ��
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