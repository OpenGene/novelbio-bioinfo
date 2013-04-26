package com.novelbio.analysis.annotation.functiontest;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.collect.HashMultimap;
import com.novelbio.database.domain.geneanno.AGene2Go;
import com.novelbio.database.domain.geneanno.GOtype.GORelation;
import com.novelbio.database.domain.geneanno.Go2Term;
import com.novelbio.database.model.modgeneid.GeneID;

public class GeneID2LsGoLevel extends GeneID2LsGo {
	private final static Logger logger = Logger.getLogger(GeneID2LsGoLevel.class);
	
	int goLevel = 2;
	
	HashMultimap<GeneID, Go2Term> mapGeneID2LsGO = HashMultimap.create();
	
	public GeneID2LsGoLevel() {
		super();
	}
	
	public void setGoLevel(int goLevel) {
		this.goLevel = goLevel;
	}
	
	@Override
	public void setGeneID(GeneID geneID, boolean blast) {
		List<AGene2Go> lsGo = null;
		
		if (blast) {
			lsGo = geneID.getGene2GOBlast(goType);
		} else {
			lsGo = geneID.getGene2GO(goType);
		}
		this.geneID = geneID;
		this.geneUniID = geneID.getGeneUniID();
		for (Go2Term go2Term : selectGo(lsGo)) {
			setItemID.add(go2Term.getGoID());
		}
	}

	/**查找N级Go*/
	private Set<Go2Term> selectGo(List<AGene2Go> lsAGene2Gos) {
		Set<Go2Term> setGo2Term = new HashSet<Go2Term>();
		for (AGene2Go aGene2Go : lsAGene2Gos) {
			if (aGene2Go.getGO2Term() != null) {
				Go2Term levelGO = aGene2Go.getGO2Term().getGOlevel(goLevel);
				if (levelGO != null) {
					addItemID(levelGO.getGoID());
				}
			}
		}
		return setGo2Term;
	}


}