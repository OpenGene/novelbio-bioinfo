package DAO.KEGGDAO;

import java.util.ArrayList;

import org.apache.ibatis.session.SqlSession;

import util.Util;
import entity.kegg.KGsubstrate;

public class DaoKSubstrate {


	
	/**
	 * 用reactionID,pathName,id,name中任意组合去查找substrate表
	 * @param kGsubstrate
	 * @return
	 */
	public static ArrayList<KGsubstrate> queryLskgKGsubstrates(KGsubstrate kGsubstrate){
		SqlSession session=Util.getSqlSesFactKEGG().openSession();
		ArrayList<KGsubstrate> lsKGsubstrates=null;
		try
		{
			lsKGsubstrates= (ArrayList<KGsubstrate>)session.selectList("KGMLSingle.selectSubstrate",kGsubstrate);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			session.close();
		}
		return lsKGsubstrates;
	}
	
	/**
	 * 用reactionID,pathName,id,name中任意组合去查找substrate表
	 * @param kGsubstrate
	 * @return
	 */
	public static KGsubstrate queryKGsubstrate(KGsubstrate kGsubstrate){
		SqlSession session=Util.getSqlSesFactKEGG().openSession();
		KGsubstrate kGsubstrate2=null;
		try
		{
			kGsubstrate2= (KGsubstrate)session.selectOne("KGMLSingle.selectSubstrate",kGsubstrate);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			session.close();
		}
		return kGsubstrate2;
	}
	
	
	public static void InsertKGsubstrate(KGsubstrate kGsubstrate){
		SqlSession session=Util.getSqlSesFactKEGG().openSession();
 		try
		{
			session.insert("KGMLSingle.insertSubstrate", kGsubstrate);
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
		update substrate set<br>
		pathName = #{pathName},<br>
		reactionID = #{reactionID},<br>
		ID = #{id},<br>
		name = #{name},<br>
		type = #{type}<br>
		where<br>
			if test="reactionID !=null and reactionID !=0"<br>
				 reactionID=#{reactionID}<br>
			/if<br>
			if test="pathName !=null"<br>
				and pathName=#{pathName}<br>
			/if<br>
			if test="id !=null and id !=0"<br>
				and ID=#{id}<br>
			/if<br>
			if test="name !=null"<br>
				and name=#{name}<br>
			/if<br>
		/where<br>
	 * @param kGsubstrate
	 */
	public static void upDateKGsubstrate(KGsubstrate kGsubstrate){
		SqlSession session=Util.getSqlSesFactKEGG().openSession();
 		try
		{
			session.update("KGMLSingle.updateSubstrate", kGsubstrate);
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
