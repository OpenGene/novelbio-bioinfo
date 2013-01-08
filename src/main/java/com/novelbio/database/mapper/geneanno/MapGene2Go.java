package com.novelbio.database.mapper.geneanno;

import java.util.ArrayList;
import com.novelbio.database.domain.geneanno.Gene2Go;
import com.novelbio.database.mapper.MapperSql;

public interface MapGene2Go extends MapperSql{
	
	/**
	 * 用GeneID去查找Gene2Go表
	 * @param GeneID
	 * @return
	 */
	public ArrayList<Gene2Go> queryLsGene2Go(Gene2Go gene2Go);
	
	/**
	 * 用Gene2GoInfo类去查找Gene2Go表
	 * 主要是来看本列是否已经存在了
	 * 用geneID和goID去查找数据库
	 * @return
	 */
	public Gene2Go queryGene2Go(Gene2Go gene2Go);
	
	public void insertGene2Go(Gene2Go gene2Go);
	
	/**
	 * 升级evidence和reference两项
	 * @param geneInfo
	 */
	public void updateGene2Go(Gene2Go gene2Go);

}
