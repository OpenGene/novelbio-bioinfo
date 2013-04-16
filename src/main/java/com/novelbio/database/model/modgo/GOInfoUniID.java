package com.novelbio.database.model.modgo;

import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.novelbio.database.domain.geneanno.AGene2Go;
import com.novelbio.database.domain.geneanno.UniGene2Go;
import com.novelbio.database.service.servgeneanno.ManageUniGene2Go;

public class GOInfoUniID extends GOInfoAbs {
	private static Logger logger = Logger.getLogger(GOInfoUniID.class);
	ManageUniGene2Go servUniGene2Go = new ManageUniGene2Go();
	
	public GOInfoUniID(String genUniAccID, int taxID) {
		super(genUniAccID, taxID);
		setGene2Go();
	}

	@Override
	protected void setGene2Go() {
		if (mapGene2Gos != null) {
			return;
		}
		mapGene2Gos = new HashMap<String, AGene2Go>();
		if (genUniAccID == null || genUniAccID.equals("")) {
			return;
		}
		List<UniGene2Go>  lstmp = servUniGene2Go.queryLsGene2Go(genUniAccID, taxID);
		if (lstmp == null || lstmp.size() == 0) {
			return;
		}
		for (UniGene2Go gene2Go : lstmp) {
			if (gene2Go.getFunction() == null || gene2Go.getGO2Term() == null) {
				if (gene2Go.getFunction() != null || gene2Go.getGO2Term() != null) {
					logger.error("error: Goterm and GoType not all null: " + gene2Go.getGO2Term() + " " + gene2Go.getFunction());
				}
				continue;
			}
			if (mapGene2Gos.containsKey(gene2Go.getGOID())) {
				continue;
			}
			mapGene2Gos.put(gene2Go.getGOID(), gene2Go);
		}
	}

	@Override
	protected AGene2Go createGene2Go() {
		return new UniGene2Go();
	}

	@Override
	protected void save(AGene2Go aGene2Go) {
		servUniGene2Go.saveGene2Go((UniGene2Go)aGene2Go);
	}
}
