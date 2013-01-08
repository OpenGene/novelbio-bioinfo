package com.novelbio.database.mapper.geneanno;

import java.util.ArrayList;

import com.novelbio.database.domain.geneanno.UniGene2Go;
import com.novelbio.database.mapper.MapperSql;

public interface MapUniGene2Go extends MapperSql{

 
	
	/**
	 * 用Gene2GoInfo类去查找Gene2Go表
	 * 主要是来看本列是否已经存在了
	 * 用uniID和goID去查找数据库
	 * @param GeneID
	 * @return
	 */
	public UniGene2Go queryUniGene2Go(UniGene2Go uniGene2Go);
	/**
	 * 用Gene2GoInfo类去查找Gene2Go表
	 * 主要是来看本列是否已经存在了
	 * 用uniID和goID去查找数据库
	 * @param GeneID
	 * @return
	 */
	public ArrayList<UniGene2Go> queryLsUniGene2Go(UniGene2Go uniGene2Go);
	
	public void insertUniGene2Go(UniGene2Go uniGene2Go);
	
	/**
	 * 升级evidence和reference两项
	 * @param geneInfo
	 */
	public void updateUniGene2Go(UniGene2Go uniGene2Go);

}
