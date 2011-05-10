package com.novelbio.database.DAO.FriceDAO;

import java.util.ArrayList;

import org.apache.ibatis.session.SqlSession;
import com.novelbio.database.entity.friceDB.*;
import com.novelbio.database.util.Util;

public class DaoFSGo2Term {

	/**
	 * 用GoIDquery,GoID,GoFunction三个中的任意组合去查找Go2Term表
	 * 主要是来看本列是否已经存在了，返回单个Go2Term
	 * @param Go2Term
	 * @return
	 */
	public static Go2Term queryGo2Term(Go2Term queryGo2Term){
		SqlSession session=Util.getSqlSesFactFriceDB().openSession();
		Go2Term go2Term=null;
		try
		{
			go2Term= (Go2Term)session.selectOne("FriceDBSingle.selectGo2Term",queryGo2Term);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			session.close();
		}
		return go2Term;
	}
	
	/**
	 * 用GoIDquery,GoID,GoFunction三个中的任意组合去查找Go2Term表
	 * 主要是来看本列是否已经存在了，返回单个ArrayList--Go2Term
	 * @param Go2Term
	 * @return
	 */
	public static ArrayList<Go2Term> queryLsGo2Term(Go2Term queryGo2Term){
		SqlSession session=Util.getSqlSesFactFriceDB().openSession();
		ArrayList<Go2Term> lsGo2Term=null;
		try
		{
			lsGo2Term= (ArrayList<Go2Term>)session.selectList("FriceDBSingle.selectGo2Term",queryGo2Term);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			session.close();
		}
		return lsGo2Term;
	}
	
	public static void InsertGo2Term(Go2Term Go2Term){
		SqlSession session=Util.getSqlSesFactFriceDB().openSession();
 		try
		{
			session.insert("FriceDBSingle.insertGo2Term", Go2Term);
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
	 * 目前的升级方式是
	update Go2Term <br>
		set<br>
			if test="GoIDquery !=null"<br>
				GoIDquery = #{GoIDquery},<br>
			/if<br>
			if test="GoID !=null"<br>
				GoID = #{GoID},<br>
			/if<br>
			if test="GoFunction !=null"<br>
				GoFunction = #{GoFunction}<br>
			/if<br>
			if test="GoTerm !=null"<br>
				GoTerm = #{GoTerm}<br>
			/if<br>
		/set<br>
		where<br>
			if test="GoIDquery !=null"<br>
				GoIDquery = #{GoIDquery} <br>
			/if<br>
			if test="GoID !=null"<br>
				and GoID = #{GoID} <br>
			/if<br>
			if test="GoFunction !=null"<br>
				and GoFunction = #{GoFunction} <br>
			/if<br>
	    /where<br>
	 * @param geneInfo
	 */
	public static void upDateGo2Term(Go2Term Go2Term){
		SqlSession session=Util.getSqlSesFactFriceDB().openSession();
 		try
		{
			session.update("FriceDBSingle.updateGo2Term", Go2Term);
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
