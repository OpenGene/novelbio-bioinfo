package com.novelbio.bioinfo.mirna;

import com.novelbio.bioinfo.mirna.ListMiRNAdat;
import com.novelbio.bioinfo.mirna.MirMature;
import com.novelbio.database.domain.species.Species;
import com.novelbio.generalconf.PathDetailNBC;

import junit.framework.TestCase;

public class TestListMiRNAdat extends TestCase {
	public static void main(String[] args) {
		ListMiRNAdat listMiRNAdat = new ListMiRNAdat();
		listMiRNAdat.setSpecies(new Species(9606));
		listMiRNAdat.ReadGffarray(PathDetailNBC.getMiRNADat());
		MirMature mirMature = listMiRNAdat.searchLOC("hsa-miR-6724-5p");
		System.out.println(mirMature.getName() + " " + mirMature.getMirAccID());
	}
}
