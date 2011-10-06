package com.novelbio.analysis.annotation.GO.goEntity;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.novelbio.database.DAO.FriceDAO.DaoFSUniGene2Go;
import com.novelbio.database.entity.friceDB.AGene2Go;
import com.novelbio.database.entity.friceDB.UniGene2Go;

public class GOInfoUniID extends GOInfoAbs{
	private static Logger logger = Logger.getLogger(GOInfoUniID.class);
	public GOInfoUniID(String genUniAccID, int taxID) {
		super(genUniAccID, taxID);
	}

	@Override
	protected void setGene2Go() {
		
		if (lsAGene2Gos != null) {
			return;
		}
		lsAGene2Gos = new ArrayList<AGene2Go>();
		ArrayList<UniGene2Go>  lstmp = DaoFSUniGene2Go.queryUniGene2Go(genUniAccID);
		if (lstmp == null || lstmp.size() == 0) {
			return;
		}
		lsAGene2Gos = new ArrayList<AGene2Go>();
		for (UniGene2Go uniGene2Go : lstmp) {
			if (uniGene2Go.getFunction() == null || uniGene2Go.getGOTerm() == null) {
				if (uniGene2Go.getFunction() != null || uniGene2Go.getGOTerm() != null) {
					logger.error("error: Goterm and GoType not all null: " + uniGene2Go.getGOTerm() + " " + uniGene2Go.getFunction());
				}
				continue;
			}
			lsAGene2Gos.add(uniGene2Go);
		}
	}
	
}
