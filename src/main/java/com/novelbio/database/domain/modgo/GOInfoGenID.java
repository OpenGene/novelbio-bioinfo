package com.novelbio.database.domain.modgo;

import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.novelbio.database.model.geneanno.AGene2Go;
import com.novelbio.database.model.geneanno.Gene2Go;
import com.novelbio.database.service.servgeneanno.ManageGene2Go;

public class GOInfoGenID extends GOInfoAbs {
	private static Logger logger = Logger.getLogger(GOInfoGenID.class);
	static ManageGene2Go servGene2Go = new ManageGene2Go();
	public GOInfoGenID(String genUniAccID, int taxID) {
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
		List<Gene2Go>  lstmp = servGene2Go.queryLsGene2Go(Integer.parseInt(genUniAccID), taxID);
		if (lstmp == null || lstmp.size() == 0) {
			return;
		}
		for (Gene2Go gene2Go : lstmp) {
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
		return new Gene2Go();
	}

	@Override
	protected void save(AGene2Go aGene2Go) {
		servGene2Go.saveGene2Go((Gene2Go)aGene2Go);
	}
	
	
}
