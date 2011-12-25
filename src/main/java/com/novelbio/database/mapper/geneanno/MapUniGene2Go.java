package com.novelbio.database.mapper.geneanno;

import java.util.ArrayList;

import com.novelbio.database.domain.geneanno.UniGene2Go;
import com.novelbio.database.mapper.MapperSql;

public interface MapUniGene2Go extends MapperSql{

 
	
	/**
	 * ��Gene2GoInfo��ȥ����Gene2Go��
	 * ��Ҫ�����������Ƿ��Ѿ�������
	 * ��uniID��goIDȥ�������ݿ�
	 * @param GeneID
	 * @return
	 */
	public UniGene2Go queryUniGene2Go(UniGene2Go uniGene2Go);
	/**
	 * ��Gene2GoInfo��ȥ����Gene2Go��
	 * ��Ҫ�����������Ƿ��Ѿ�������
	 * ��uniID��goIDȥ�������ݿ�
	 * @param GeneID
	 * @return
	 */
	public ArrayList<UniGene2Go> queryLsUniGene2Go(UniGene2Go uniGene2Go);
	
	public void insertUniGene2Go(UniGene2Go uniGene2Go);
	
	/**
	 * ����evidence��reference����
	 * @param geneInfo
	 */
	public void updateUniGene2Go(UniGene2Go uniGene2Go);

}
