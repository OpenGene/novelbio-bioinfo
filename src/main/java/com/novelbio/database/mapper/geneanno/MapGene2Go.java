package com.novelbio.database.mapper.geneanno;

import java.util.ArrayList;
import com.novelbio.database.domain.geneanno.Gene2Go;
import com.novelbio.database.mapper.MapperSql;

public interface MapGene2Go extends MapperSql{
	
	/**
	 * ��GeneIDȥ����Gene2Go��
	 * @param GeneID
	 * @return
	 */
	public ArrayList<Gene2Go> queryLsGene2Go(Gene2Go gene2Go);
	
	/**
	 * ��Gene2GoInfo��ȥ����Gene2Go��
	 * ��Ҫ�����������Ƿ��Ѿ�������
	 * ��geneID��goIDȥ�������ݿ�
	 * @return
	 */
	public Gene2Go queryGene2Go(Gene2Go gene2Go);
	
	public void insertGene2Go(Gene2Go gene2Go);
	
	/**
	 * ����evidence��reference����
	 * @param geneInfo
	 */
	public void updateGene2Go(Gene2Go gene2Go);

}
