package com.novelbio.analysis.seq.resequencing;

import java.util.ArrayList;
import java.util.Collection;

/** ���ڲ��Ҷ�����SnpSomatic��service�� */
public class SnpSomaticFinder {
	public static void main(String[] args) {
		
		
	}
	
	SNPGATKcope snpgatKcope;
	GeneFilter geneFilter;
	
	public void setSnpgatKcope(SNPGATKcope snpgatKcope) {
		this.snpgatKcope = snpgatKcope;
	}
	public void setGeneFilter(GeneFilter geneFilter) {
		this.geneFilter = geneFilter;
	}
	
	/**
	 * ���Ҫ�����������
	 * @param colTreatName
	 */
	public void addTreatName(Collection<String> colTreatName) {
		geneFilter.addTreatName(colTreatName);
	}
	
	public void setSnpLevel() {
		geneFilter.
	}
	
	public void running() {
		ArrayList<RefSiteSnpIndel> lsFilteredRefSnp = snpgatKcope.getLsFilteredSnp();
		geneFilter.addLsRefSiteSnpIndel(lsFilteredRefSnp);
		geneFilter.set
	}
	
	
}
