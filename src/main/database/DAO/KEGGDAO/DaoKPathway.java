package DAO.KEGGDAO;

import java.util.ArrayList;

import org.apache.ibatis.session.SqlSession;

import entity.kegg.KGpathway;

import util.Util;
public class DaoKPathway {

	
	/**
	 * ��number,pathNam,org���������ȥ����entry��
	 * @param KGpathway
	 * @return
	 */
	public static ArrayList<KGpathway> queryLsKGpathways(KGpathway kGpathway){
		SqlSession session=Util.getSqlSesFactKEGG().openSession();
		ArrayList<KGpathway> lsKGpathways=null;
		try
		{
			lsKGpathways= (ArrayList<KGpathway>)session.selectList("KGMLSingle.selectPathway",kGpathway);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			session.close();
		}
		return lsKGpathways;
	}
	
		/**
		 * ��number,pathNam,org���������ȥ����entry��
		 * @param KGpathway
		 * @return
		 */
		public static KGpathway queryKGpathway(KGpathway kGpathway){
			SqlSession session=Util.getSqlSesFactKEGG().openSession();
			KGpathway kGpathway2=null;
			try
			{
				kGpathway2= (KGpathway)session.selectOne("KGMLSingle.selectPathway",kGpathway);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				session.close();
			}
			return kGpathway2;
		}
		
	
	
	public static void InsertKGpathway(KGpathway kGpathway){
		SqlSession session=Util.getSqlSesFactKEGG().openSession();
 		try
		{
			session.insert("KGMLSingle.insertPathway", kGpathway);
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
	 * Ŀǰ��������ʽ��
		update pathway set
		pathName = #{pathName},
		org = #{org},
		number = #{mapNum},
		title = #{title},
		linkUrl = #{linkUrl}
		<b>where</b> pathName = #{pathName}
	 * @param KGpathway
	 */
	public static void upDateKGpathway(KGpathway kGpathway){
		SqlSession session=Util.getSqlSesFactKEGG().openSession();
 		try
		{
			session.update("KGMLSingle.updatePathway", kGpathway);
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
