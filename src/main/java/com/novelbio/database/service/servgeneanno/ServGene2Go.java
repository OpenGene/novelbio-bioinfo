package com.novelbio.database.service.servgeneanno;

import java.util.ArrayList;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.novelbio.database.domain.geneanno.AGene2Go;
import com.novelbio.database.domain.geneanno.Gene2Go;
import com.novelbio.database.mapper.geneanno.MapGene2Go;
import com.novelbio.database.mapper.geneanno.MapGeneInfo;
import com.novelbio.database.mapper.geneanno.MapGo2Term;
import com.novelbio.database.service.AbsGetSpring;
@Service
public class ServGene2Go extends AbsGetSpring implements MapGene2Go {
	@Inject
	private MapGene2Go mapGene2Go;
	public ServGene2Go()  
	{
		mapGene2Go = (MapGene2Go) factory.getBean("mapGene2Go");
	}

	public ArrayList<Gene2Go> queryLsGene2Go(int geneID) {
		Gene2Go gene2Go = new Gene2Go();
		gene2Go.setGeneUniID(geneID + "");
		return mapGene2Go.queryLsGene2Go(gene2Go);
	}
	public Gene2Go queryGene2Go(String geneID, String GOID) {
		Gene2Go gene2Go = new Gene2Go();
		gene2Go.setGeneUniID(geneID);
		gene2Go.setGOID(GOID);
		return mapGene2Go.queryGene2Go(gene2Go);
	}
	@Override
	public ArrayList<Gene2Go> queryLsGene2Go(Gene2Go gene2Go) {
		return mapGene2Go.queryLsGene2Go(gene2Go);
	}

	@Override
	public Gene2Go queryGene2Go(Gene2Go gene2Go) {
		return mapGene2Go.queryGene2Go(gene2Go);
	}

	@Override
	public void insertGene2Go(Gene2Go gene2Go) {
		// TODO Auto-generated method stub
		mapGene2Go.insertGene2Go(gene2Go);
	}

	@Override
	public void updateGene2Go(Gene2Go gene2Go) {
		// TODO Auto-generated method stub
		mapGene2Go.updateGene2Go(gene2Go);
	}
	/**
	 * 输入geneUniID以及具体的内容，看是否需要升级
	 * 能插入就插入，已经有了就判端与数据库中是否一致，不一致就升级
	 * @param genUniID
	 * @param gene2Go
	 */
	public void updateGene2Go(String genUniID,AGene2Go gene2Go) {
		Gene2Go gene2GoOld = queryGene2Go(genUniID, gene2Go.getGOID());
		if (gene2GoOld != null) {
			if (gene2GoOld.addInfo(gene2Go)) {
				updateGene2Go(gene2GoOld);
			}
		}
		else {
			Gene2Go gene2GoNew = new Gene2Go();
			gene2GoNew.copyInfo(gene2Go);
			gene2GoNew.setGeneUniID(genUniID);
			insertGene2Go(gene2GoNew);
		}
	}
	
}
