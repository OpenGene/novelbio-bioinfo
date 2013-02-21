package com.novelbio.analysis.annotation.functiontest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.collect.HashMultimap;
import com.novelbio.database.domain.geneanno.AGene2Go;
import com.novelbio.database.domain.geneanno.Go2Term;
import com.novelbio.database.domain.geneanno.GOtype.GORelation;
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
		
		ArrayList<AGene2Go> lsGo = null;
		
		if (blast) {
			lsGo = geneID.getGene2GOBlast(goType);
		} else {
			lsGo = geneID.getGene2GO(goType);
		}
		this.geneID = geneID;
		this.geneUniID = geneID.getGenUniID();
		for (Go2Term go2Term : selectGo(lsGo)) {
			setItemID.add(go2Term.getGoID());
		}
	}

	/**查找N级Go*/
	private Set<Go2Term> selectGo(List<AGene2Go> lsAGene2Gos) {
		Set<Go2Term> setGo2Term = new HashSet<Go2Term>();
		for (AGene2Go aGene2Go : lsAGene2Gos) {
			if (aGene2Go.getGO2Term() != null) {
				Go2Term levelGO = getGOlevel(aGene2Go.getGO2Term(), goLevel);
				if (levelGO != null) {
					addItemID(levelGO.getGoID());
				}
			}
		}
		return setGo2Term;
	}

	/**
	 * 返回第几级GO
	 * @param go2Term
	 * @param level
	 * @return
	 */
	private Go2Term getGOlevel(Go2Term go2Term, int level) {
		Queue<Go2Term> queueGo2Terms = new LinkedList<Go2Term>();
		Go2Term goTermParent = null;
		while ((goTermParent = getOneParentGo2Term(go2Term)) != null) {
			queueGo2Terms.add(goTermParent);
			if (queueGo2Terms.size() > level) {
				queueGo2Terms.poll();
			}
		}
		
		if (queueGo2Terms.size() == level) {
			return queueGo2Terms.peek();
		} else {
			return null;
		}
	}
	
	/**获取一个父级的Go，根据和当前Go的关系选择，优先is，其次REGULATE，再次REGULATE_NEG或者REGULATE_POS，最后PART_OF，没有返回null；*/
	private Go2Term getOneParentGo2Term(Go2Term go2Term) {
		Set<Go2Term> setGo2Terms = go2Term.getParent();
		if (setGo2Terms.size() == 0) {
			return null;
		}
		for (Go2Term go2Term2 : setGo2Terms) {
			if (go2Term2.getRelation() == GORelation.IS) {
				return go2Term2;
			}
		}
		for (Go2Term go2Term2 : setGo2Terms) {
			if (go2Term2.getRelation() == GORelation.REGULATE) {
				return go2Term2;
			}
		}
		for (Go2Term go2Term2 : setGo2Terms) {
			if (go2Term2.getRelation() == GORelation.REGULATE_NEG
					|| go2Term2.getRelation() == GORelation.REGULATE_POS) {
				return go2Term2;
			}
		}
		for (Go2Term go2Term2 : setGo2Terms) {
			if (go2Term2.getRelation() == GORelation.PART_OF) {
				return go2Term2;
			}
		}
		logger.error("该Term的父级出现新的Relation" + go2Term);
		return null;
	}

}