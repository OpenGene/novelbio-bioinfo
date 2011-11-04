package com.novelbio.database.model.modgo;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.novelbio.database.domain.geneanno.AGene2Go;
import com.novelbio.database.domain.geneanno.Gene2Go;
import com.novelbio.database.domain.geneanno.UniGene2Go;
import com.novelbio.database.mapper.geneanno.MapGene2Go;
import com.novelbio.database.mapper.geneanno.MapUniGene2Go;

public class GOInfoGenID extends GOInfoAbs{
	private static Logger logger = Logger.getLogger(GOInfoGenID.class);

	public GOInfoGenID(String genUniAccID, int taxID) {
		super(genUniAccID, taxID);
	}

	@Override
	protected void setGene2Go() {
		if (lsAGene2Gos != null) {
			return;
		}
		lsAGene2Gos = new ArrayList<AGene2Go>();
		ArrayList<Gene2Go>  lstmp = MapGene2Go.queryGene2Go(Integer.parseInt(genUniAccID));
		if (lstmp == null || lstmp.size() == 0) {
			return;
		}
		lsAGene2Gos = new ArrayList<AGene2Go>();
		for (Gene2Go gene2Go : lstmp) {
			if (gene2Go.getFunction() == null || gene2Go.getGOTerm() == null) {
				if (gene2Go.getFunction() != null || gene2Go.getGOTerm() != null) {
					logger.error("error: Goterm and GoType not all null: " + gene2Go.getGOTerm() + " " + gene2Go.getFunction());
				}
				continue;
			}
			lsAGene2Gos.add(gene2Go);
		}
	}

}
