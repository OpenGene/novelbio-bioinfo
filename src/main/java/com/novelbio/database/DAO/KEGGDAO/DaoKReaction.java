package com.novelbio.database.DAO.KEGGDAO;

import java.util.ArrayList;
import org.apache.ibatis.session.SqlSession;

import com.novelbio.database.entity.kegg.KGreaction;
import com.novelbio.database.util.Util;

public class DaoKReaction {

	
	/**
	 * 用name,pathNam,ID中任意组合去查找reaction表
	 * @param kGreaction
	 * @return
	 */
	public static ArrayList<KGreaction> querylsKGreactions(KGreaction kGreaction){
		SqlSession session=Util.getSqlSesFactKEGG().openSession();
		ArrayList<KGreaction> lsKGreactions=null;
		try
		{
			lsKGreactions= (ArrayList<KGreaction>)session.selectList("KGMLSingle.selectReaction",kGreaction);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			session.close();
		}
		return lsKGreactions;
	}
	
	/**
	 * 用name,pathNam,ID中任意组合去查找reaction表
	 * @param kGreaction
	 * @return
	 */
	public static KGreaction queryKGreaction(KGreaction kGreaction){
		SqlSession session=Util.getSqlSesFactKEGG().openSession();
		KGreaction kGreaction2=null;
		try
		{
			kGreaction2= (KGreaction)session.selectOne("KGMLSingle.selectReaction",kGreaction);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			session.close();
		}
		return kGreaction2;
	}
	
	
	public static void InsertKGreaction(KGreaction kGreaction){
		SqlSession session=Util.getSqlSesFactKEGG().openSession();
 		try
		{
			session.insert("KGMLSingle.insertReaction", kGreaction);
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
	 * 目前的升级方式是:<br>
		update reaction set<br>
		pathName = #{pathName},<br>
		ID = #{id},<br>
		name = #{name},<br>
		type = #{type},<br>
		altName = #{alt},<br>
		where<br>
			if test="name !=null"<br>
				 name=#{name}<br>
			/if<br>
			if test="pathName !=null"<br>
				and pathName=#{pathName}<br>
			/if<br>
			if test="id !=null and id !=0"<br>
				and ID=#{id}<br>
			/if<br>
		/where<br>
	 * @param KGreaction
	 */
	public static void upDateKGreaction(KGreaction kGreaction){
		SqlSession session=Util.getSqlSesFactKEGG().openSession();
 		try
		{
			session.update("KGMLSingle.updateReaction", kGreaction);
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
