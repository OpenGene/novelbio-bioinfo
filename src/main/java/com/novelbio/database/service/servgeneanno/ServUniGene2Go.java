package com.novelbio.database.service.servgeneanno;

import java.util.ArrayList;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.novelbio.database.domain.geneanno.AGene2Go;
import com.novelbio.database.domain.geneanno.UniGene2Go;
import com.novelbio.database.mapper.geneanno.MapUniGene2Go;
import com.novelbio.database.service.SpringFactory;

@Service
public class ServUniGene2Go implements MapUniGene2Go{
	private static Logger logger = Logger.getLogger(ServUniGene2Go.class);
	@Inject
	private MapUniGene2Go mapUniGene2Go;
	public ServUniGene2Go()  
	{
		mapUniGene2Go = (MapUniGene2Go)SpringFactory.getFactory().getBean("mapUniGene2Go");
	}
	
	
	public ArrayList<UniGene2Go> queryLsUniGene2Go(String uniprotID, int taxID) {
		UniGene2Go uniGene2Go = new UniGene2Go();
		uniGene2Go.setGeneUniID(uniprotID);
		uniGene2Go.setTaxID(taxID);
		return mapUniGene2Go.queryLsUniGene2Go(uniGene2Go);
	}

	public UniGene2Go queryUniGene2Go(String uniprotID, int taxID, String GOID) {
		UniGene2Go uniGene2Go = new UniGene2Go();
		uniGene2Go.setGeneUniID(uniprotID);
		uniGene2Go.setGOID(GOID);
		uniGene2Go.setTaxID(taxID);
		try {
			return mapUniGene2Go.queryUniGene2Go(uniGene2Go);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(uniGene2Go.getGOID());
			return null;
		}
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
	public boolean updateUniGene2Go(String genUniID, int taxID, AGene2Go gene2Go) {
		UniGene2Go uniGene2GoOld = queryUniGene2Go(genUniID, taxID, gene2Go.getGOID());
		if (uniGene2GoOld != null) {
			if (uniGene2GoOld.addInfo(gene2Go)) {
				updateUniGene2Go(uniGene2GoOld);
			}
		}
		else {
			UniGene2Go uniGene2GoNew = new UniGene2Go();
			uniGene2GoNew.copyInfo(gene2Go);
			if (uniGene2GoNew.getGOID() == null) {
				logger.error("出现未知GOID：" + gene2Go.getGOID());
				return false;
			}
			uniGene2GoNew.setGeneUniID(genUniID);
			try {
				insertUniGene2Go(uniGene2GoNew);
			} catch (Exception e) {//出错原因可能是有两个连续一样的goID，然后连续插入的时候第一个goID还没来得及建索引，导致第二个直接就插入了然后出错
				System.out.println(gene2Go.getGOID() + " " + uniGene2GoNew.getGeneUniId() + " " + gene2Go.getTaxID());
				return false;
			}
		}
		return true;
	}
}
