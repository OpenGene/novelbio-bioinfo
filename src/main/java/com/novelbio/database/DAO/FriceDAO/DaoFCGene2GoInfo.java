package com.novelbio.database.DAO.FriceDAO;

import java.util.ArrayList;

import org.apache.ibatis.session.SqlSession;

import com.novelbio.database.domain.geneanno.*;
import com.novelbio.database.util.Util;

@Deprecated
public class DaoFCGene2GoInfo {
	/**
	 * 给定NCBIID对象，返回NCBIID、gene2Go、Gene2Info几个表的信息
	 * @param accessID
	 * @return
	 */
	public static Gene2GoInfo queryGeneDetail(NCBIID ncbiid){
		SqlSession session=Util.getSqlSesFactFriceDB().openSession();
		Gene2GoInfo gene2GoInfo=null;
		try
		{
			gene2GoInfo=(Gene2GoInfo) session.selectOne("GeneID2GoInfo.selectGeneDetail",ncbiid);
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
	 * 给定NCBIID对象，返回NCBIID、gene2Go、Gene2Info几个表的信息
	 * @param accessID
	 * @return
	 */
	public static ArrayList<Gene2GoInfo> queryLsGeneDetail(NCBIID ncbiid){
		SqlSession session=Util.getSqlSesFactFriceDB().openSession();
		ArrayList<Gene2GoInfo> lsGene2GoInfo=null;
		try
		{
			lsGene2GoInfo=(ArrayList<Gene2GoInfo>) session.selectList("GeneID2GoInfo.selectGeneDetail",ncbiid);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			session.close();
		}
		return lsGene2GoInfo;
	}
	/**
	 * 给定uniProtID对象，返回UniID、UniGene2Go、UniGene2Info几个表的信息<br>
	 * 		where<br>
			if test="accessID !=null"<br>
				and accessID=#{accessID}<br>
			/if<br>
			if test="taxID !=null and taxID !=0"<br>
				and TaxID=#{taxID}<br>
			/if<br>
			if test="UniID!=null"><br>
				and UniID=#{UniID}<br>
			/if<br>
		/where<br>
	 * @param accessID
	 * @return
	 */
	public static Uni2GoInfo queryUniDetail(UniProtID uniProtID){
		SqlSession session=Util.getSqlSesFactFriceDB().openSession();
		Uni2GoInfo uni2GoInfo=null;
		try
		{
			uni2GoInfo=(Uni2GoInfo) session.selectOne("GeneID2GoInfo.selectUniGeneDetail",uniProtID);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			session.close();
		}
		return uni2GoInfo;
	}
	/**
	 * 给定uniProtID对象，返回UniID、UniGene2Go、UniGene2Info几个表的信息<br>
	 * 		where<br>
			if test="accessID !=null"<br>
				and accessID=#{accessID}<br>
			/if<br>
			if test="taxID !=null and taxID !=0"<br>
				and TaxID=#{taxID}<br>
			/if<br>
			if test="UniID!=null"><br>
				and UniID=#{UniID}<br>
			/if<br>
		/where<br>
	 * @param accessID
	 * @return
	 */
	public static ArrayList<Uni2GoInfo> queryLsUniDetail(UniProtID uniProtID){
		SqlSession session=Util.getSqlSesFactFriceDB().openSession();
		ArrayList<Uni2GoInfo> lsUni2GoInfo=null;
		try
		{
			lsUni2GoInfo=(ArrayList<Uni2GoInfo>) session.selectList("GeneID2GoInfo.selectUniGeneDetail",uniProtID);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			session.close();
		}
		return lsUni2GoInfo;
	}
	
}
