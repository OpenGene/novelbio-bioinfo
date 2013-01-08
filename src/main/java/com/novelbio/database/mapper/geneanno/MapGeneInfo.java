package com.novelbio.database.mapper.geneanno;

import java.util.ArrayList;

import org.apache.ibatis.session.SqlSession;

import com.novelbio.database.domain.geneanno.GeneInfo;
import com.novelbio.database.mapper.MapperSql;

public interface MapGeneInfo extends MapperSql{
	/**
	 * 用GeneInfo类去查找GeneInfo表
	 * 主要是来看本列是否已经存在了
	 * 用geneID去查找数据库
	 * @param geneInfo
	 * @return
	 */
	public GeneInfo queryGeneInfo(GeneInfo geneInfo);
	
	/**
	 * 用GeneInfo类去查找GeneInfo表
	 * 主要是来看本列是否已经存在了
	 * 用geneID去查找数据库
	 * 	@param geneInfo
	 * @return
	 */
	public ArrayList<GeneInfo> queryLsGeneInfo(GeneInfo geneInfo);
	
	public void insertGeneInfo(GeneInfo geneInfo);
	
	/**
	 * 用geneID查找，升级全部项目，
	 * @param geneInfo
	 */
	public void updateGeneInfo(GeneInfo geneInfo);

}
