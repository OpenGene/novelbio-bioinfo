package com.novelbio.analysis.gwas.combinesnp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.novelbio.analysis.gwas.Allele;

import smile.clustering.HierarchicalClustering;
import smile.clustering.linkage.UPGMALinkage;

/** ld计算 */
public class LDcalculate {
	String refa, alta;
	String refb, altb;
	
	List<String[]> lsRef2AltSite1;
	List<String[]> lsRef2AltSite2;
	
	int num;
	double a1b1, a1b2, a2b1, a2b2;
	double p1, p2, q1, q2;
	
	double ddot;
	double r2;
	
	public void setLsRef2AltSite1(List<String[]> lsRef2AltSite1) {
		this.lsRef2AltSite1 = lsRef2AltSite1;
		this.refa = getMaxRef(lsRef2AltSite1);
	}
	
	private String getMaxRef(List<String[]> lsRef2AltSite) {
		Map<String, int[]> mapAllele2Num = new HashMap<>();
		for (String[] alleles : lsRef2AltSite) {
			int[] num = mapAllele2Num.get(alleles[0]);
			if (num == null) {
				num = new int[]{0};
				mapAllele2Num.put(alleles[0], num);
			}
			num[0]++;
		}
		List<String[]> lsSite2Num = new ArrayList<>();
		for (String alleleStr : mapAllele2Num.keySet()) {
			lsSite2Num.add(new String[] {alleleStr, mapAllele2Num.get(alleleStr)[0]+""});
		}
		Collections.sort(lsSite2Num, (site2Num1, site2Num2) ->{
			Integer site1 = Integer.parseInt(site2Num1[1]);
			Integer site2 = Integer.parseInt(site2Num2[1]);
			return -site1.compareTo(site2);
		} );
		return lsSite2Num.get(0)[0];
	}
	
	public void setLsRef2AltSite2(List<String[]> lsRef2AltSite2) {
		this.lsRef2AltSite2 = lsRef2AltSite2;
		this.refb = getMaxRef(lsRef2AltSite2);
	}
	
	public void calculate()  {
		prepare();
		calculateDetail();
	}
	public double getMaxR2Ddot() {
		return Math.max(r2, ddot);
	}
	public double getR2() {
		return r2;
	}
	public double getDdot() {
		return ddot;
	}
	private void prepare() {
		num = 0;
		int ia1b1 = 0, ia1b2 = 0, ia2b1 = 0, ia2b2 = 0;
		for (int i = 0; i < lsRef2AltSite1.size(); i++) {
			String[] aRef2Alt = lsRef2AltSite1.get(i);
			String[] bRef2Alt = lsRef2AltSite2.get(i);
			if (aRef2Alt[0].equals("N") || bRef2Alt[0].equals("N")) {
				continue;
			}
			
			num++;
			if (aRef2Alt[0].equals(refa)) {
				if (bRef2Alt[0].equals(refb)) {
					ia1b1++;
				} else {
					ia1b2++;
				}
			} else {
				if (bRef2Alt[0].equals(refb)) {
					ia2b1++;
				} else {
					ia2b2++;
				}
			}
		}
		if (ia2b1+ia2b2==0 && ia1b2+ia2b2==0) {
			ia1b1--;
			ia2b2++;
		}
		
		if (ia2b1+ia2b2 == 0) {
			if (ia1b1 > ia1b2) {
				ia1b2--;
				ia2b2++;
			} else {
				ia1b1--;
				ia2b2++;
			}
		}
		if (ia1b2+ia2b2 == 0) {
			if (ia1b1>ia2b1) {
				ia2b1--;
				ia2b2++;
			} else {
				ia1b1--;
				ia2b2++;
			}
		}
		
		a1b1 = (double)ia1b1/num;
		a1b2 = (double)ia1b2/num;
		a2b1 = (double)ia2b1/num;
		a2b2 = (double)ia2b2/num;
		
//		if (a1b1 == 0) {
//			a1b1 = (double)1/num;
//		}
//		if (a1b2 == 0) {
//			a1b2 = (double)1/num;
//		}
//		if (a2b1 == 0) {
//			a2b1 = (double)1/num;
//		}
//		if (a2b2 == 0) {
//			a2b2 = (double)1/num;
//		}
		p1 = a1b1+a1b2;
		p2 = a2b1+a2b2;
		q1 = a1b1+a2b1;
		q2 = a1b2+a2b2;
		
//		if (p1 == 0) {
//			a1b1 = (double)1/num;
//			a1b2 = (double)1/num;
//		}
//		if (p2 == 0) {
//			a2b1 = (double)1/num;
//			a2b2 = (double)1/num;
//		}
//		if (q1 == 0) {
//			a1b1 = (double)1/num;
//			a2b1 = (double)1/num;
//		}
//		if (q2 == 0) {
//			a1b2 = (double)1/num;
//			a2b2 = (double)1/num;
//		}
//		p1 = a1b1+a1b2;
//		p2 = a2b1+a2b2;
//		q1 = a1b1+a2b1;
//		q2 = a1b2+a2b2;
	}
	
	private void calculateDetail() {
		double d = a1b1 - p1*q1;
		r2 = d*d/(p1*p2*q1*q2);
		double dmax = 0;
		if (d<= 0) {
			dmax = Math.max(-p1*q1, -p2*q2);
		} else {
			dmax = Math.min(p1*q2, p2*q1);
		}
		ddot = d/dmax;
	}
	
}
