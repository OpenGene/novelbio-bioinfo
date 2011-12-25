package com.novelbio.database.service.servgeneanno;

import java.util.ArrayList;
import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.novelbio.database.domain.geneanno.AGene2Go;
import com.novelbio.database.domain.geneanno.UniGene2Go;
import com.novelbio.database.mapper.geneanno.MapUniGene2Go;
import com.novelbio.database.service.AbsGetSpring;

@Service
public class ServUniGene2Go extends AbsGetSpring implements MapUniGene2Go{

	@Inject
	private MapUniGene2Go mapUniGene2Go;
	public ServUniGene2Go()  
	{
		mapUniGene2Go = (MapUniGene2Go) factory.getBean("mapUniGene2Go");
	}
	
	
	public ArrayList<UniGene2Go> queryLsUniGene2Go(String uniprotID) {
		UniGene2Go uniGene2Go = new UniGene2Go();
		uniGene2Go.setGeneUniID(uniprotID);
		return mapUniGene2Go.queryLsUniGene2Go(uniGene2Go);
	}

	public UniGene2Go queryUniGene2Go(String uniprotID, String GOID) {
		UniGene2Go uniGene2Go = new UniGene2Go();
		uniGene2Go.setGeneUniID(uniprotID);
		uniGene2Go.setGOID(GOID);
		return mapUniGene2Go.queryUniGene2Go(uniGene2Go);
	}

	
	@Override
	public ArrayList<UniGene2Go> queryLsUniGene2Go(UniGene2Go uniGene2Go) {
		return mapUniGene2Go.queryLsUniGene2Go(uniGene2Go);
	}

	@Override
	public UniGene2Go queryUniGene2Go(UniGene2Go uniGene2Go) {
		return mapUniGene2Go.queryUniGene2Go(uniGene2Go);
	}

	@Override
	public void insertUniGene2Go(UniGene2Go uniGene2Go) {
		// TODO Auto-generated method stub
		mapUniGene2Go.insertUniGene2Go(uniGene2Go);
	}

	@Override
	public void updateUniGene2Go(UniGene2Go uniGene2Go) {
		// TODO Auto-generated method stub
		mapUniGene2Go.updateUniGene2Go(uniGene2Go);
	}
	/**
	 * 输入geneUniID以及具体的内容，看是否需要升级
	 * 能插入就插入，已经有了就判端与数据库中是否一致，不一致就升级
	 * @param genUniID
	 * @param gene2Go
	 */
	public void updateUniGene2Go(String genUniID, AGene2Go gene2Go) {
		UniGene2Go uniGene2GoOld = queryUniGene2Go(genUniID, gene2Go.getGOID());
		if (uniGene2GoOld != null) {
			if (uniGene2GoOld.addInfo(gene2Go)) {
				updateUniGene2Go(uniGene2GoOld);
			}
		}
		else {
			UniGene2Go uniGene2GoNew = new UniGene2Go();
			uniGene2GoNew.copyInfo(gene2Go);
			uniGene2GoNew.setGeneUniID(genUniID);
			insertUniGene2Go(uniGene2GoNew);
		}
	}
}
