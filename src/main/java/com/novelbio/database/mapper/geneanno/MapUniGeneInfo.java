package com.novelbio.database.mapper.geneanno;

import java.util.ArrayList;

import org.apache.ibatis.session.SqlSession;

import com.novelbio.database.domain.geneanno.UniGeneInfo;
import com.novelbio.database.mapper.MapperSql;

public interface MapUniGeneInfo extends MapperSql {
	
	/**
	 * 用Gene2GoInfo类去查找UniGeneInfo表
	 * 主要是来看本列是否已经存在了
	 * 用geneID去查找数据库
	 * @param GeneID
	 * @return
	 */
	public UniGeneInfo queryUniGeneInfo(UniGeneInfo uniGeneInfo);
	
	
	public void insertUniGeneInfo(UniGeneInfo uniGeneInfo);
	
	/**
	 * 用geneID查找，升级全部项目，
	 * @param geneInfo
	 */
	public void updateUniGeneInfo(UniGeneInfo uniGeneInfo);

}
