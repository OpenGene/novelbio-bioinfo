package com.novelbio.database.DAO.FriceDAO;

import java.util.ArrayList;

import org.apache.ibatis.session.SqlSession;

import com.novelbio.database.entity.friceDB.BlastInfo;
import com.novelbio.database.util.Util;




public class DaoFSBlastInfo {

	/**
		where<br>
			if test="queryID !=null"<br>
				queryID = #{queryID} <br>
			/if<br>
			if test="queryTax !=null or queryTax!=0"<br>
				and queryTax = #{queryTax} <br>
			/if<br>
			if test="queryDB !=null"<br>
				and queryDB = #{queryDB} <br>
			/if<br>
			if test="subjectID !=null"<br>
				and subjectID = #{subjectID} <br>
			/if<br>
			if test="subjectTax !=null or subjectTax!=0"<br>
				and subjectTax = #{subjectTax} <br>
			/if<br>
			if test="subjectDB !=null"<br>
				and subjectDB = #{subjectDB} <br>
			/if<br>
	    /where <br>
	 * @param BlastInfo
	 * @return
	 */
	public static BlastInfo queryBlastInfo(BlastInfo qBlastInfo){
		SqlSession session=Util.getSqlSesFactFriceDB().openSession();
		BlastInfo blastInfo=null;
		try
		{
			blastInfo= (BlastInfo)session.selectOne("FriceDBSingle.selectBlastInfo",qBlastInfo);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			session.close();
		}
		return blastInfo;
	}
	
	/**
		where<br>
			if test="queryID !=null"<br>
				queryID = #{queryID} <br>
			/if<br>
			if test="queryTax !=null or queryTax!=0"<br>
				and queryTax = #{queryTax} <br>
			/if<br>
			if test="queryDB !=null"<br>
				and queryDB = #{queryDB} <br>
			/if<br>
			if test="subjectID !=null"<br>
				and subjectID = #{subjectID} <br>
			/if<br>
			if test="subjectTax !=null or subjectTax!=0"<br>
				and subjectTax = #{subjectTax} <br>
			/if<br>
			if test="subjectDB !=null"<br>
				and subjectDB = #{subjectDB} <br>
			/if<br>
	    /where <br>
	 * @param BlastInfo
	 * @return
	 */
	public static  ArrayList<BlastInfo> queryLsBlastInfo(BlastInfo qBlastInfo){
		SqlSession session=Util.getSqlSesFactFriceDB().openSession();
		ArrayList<BlastInfo> lsBlastInfos=null;
		try
		{
			lsBlastInfos= (ArrayList<BlastInfo>)session.selectList("FriceDBSingle.selectBlastInfo",qBlastInfo);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			session.close();
		}
		return lsBlastInfos;
	}
	
	public static void InsertBlastInfo(BlastInfo blastInfo){
		SqlSession session=Util.getSqlSesFactFriceDB().openSession();
 		try
		{
			session.insert("FriceDBSingle.insertBlastInfo", blastInfo);
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
update BlastInfo <br>
		set<br>
		/set<br>
		where<br>
			if test="queryID !=null"<br>
				queryID = #{queryID} <br>
			/if<br>
			if test="queryTax !=null or queryTax!=0"<br>
				and queryTax = #{queryTax} <br>
			/if<br>
			if test="subjectTax !=null or subjectTax!=0"<br>
				and subjectTax = #{subjectTax} <br>
			/if<br>
	    /where <br>
	 */
	public static void upDateBlastInfo(BlastInfo blastInfo){
		SqlSession session=Util.getSqlSesFactFriceDB().openSession();
 		try
		{
			session.update("FriceDBSingle.updateBlastInfo", blastInfo);
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
