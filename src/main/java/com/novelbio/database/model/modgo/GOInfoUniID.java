package com.novelbio.database.model.modgo;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.novelbio.database.domain.geneanno.AGene2Go;
import com.novelbio.database.domain.geneanno.UniGene2Go;
import com.novelbio.database.service.servgeneanno.ServUniGene2Go;

public class GOInfoUniID extends GOInfoAbs{
	private static Logger logger = Logger.getLogger(GOInfoUniID.class);
	ServUniGene2Go servUniGene2Go = new ServUniGene2Go();
	
	public GOInfoUniID(String genUniAccID, int taxID) {
		super(genUniAccID, taxID);
	}

	@Override
	protected void setGene2Go() {
		
		if (lsAGene2Gos != null) {
			return;
		}
		lsAGene2Gos = new ArrayList<AGene2Go>();
		ArrayList<UniGene2Go>  lstmp = servUniGene2Go.queryLsUniGene2Go(genUniAccID);
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
