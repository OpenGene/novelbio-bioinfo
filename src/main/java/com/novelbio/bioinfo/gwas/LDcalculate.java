package com.novelbio.bioinfo.gwas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 计算两个位点之间的ld，包括r^2和d‘
 * @author zongjie
 *
 */
public class LDcalculate {
	String refa;
	String refb;
	
	List<String> lsSite1;
	List<String> lsSite2;
	
	int num;
	double a1b1, a1b2, a2b1, a2b2;
	double p1, p2, q1, q2;
	
	double ddot;
	double r2;
	
	public void setLsSite1(List<String[]> lsRef2AltSite1) {
		this.lsSite1 = new ArrayList<>();
		for (String[] ref2alt : lsRef2AltSite1) {
			this.lsSite1.add(ref2alt[0]);
		}
		this.refa = getMaxRef(lsSite1);
	}
	public void setLsSite1(char[] site1) {
		this.lsSite1 = new ArrayList<>();
		for (char c : site1) {
			lsSite1.add(c+"");
		}
		this.refa = getMaxRef(lsSite1);
	}
	
	public void setLsSite2(List<String[]> lsRef2AltSite2) {
		this.lsSite2 = new ArrayList<>();
		for (String[] ref2alt : lsRef2AltSite2) {
			this.lsSite2.add(ref2alt[0]);
		}
		this.refb = getMaxRef(lsSite2);
	}
	public void setLsSite2(char[] site1) {
		this.lsSite2 = new ArrayList<>();
		for (char c : site1) {
			lsSite2.add(c+"");
		}
		this.refb = getMaxRef(lsSite2);
	}
	
	private String getMaxRef(List<String> lsRef2AltSite) {
		Map<String, int[]> mapAllele2Num = new HashMap<>();
		for (String allele : lsRef2AltSite) {
			int[] num = mapAllele2Num.get(allele);
			if (num == null) {
				num = new int[]{0};
				mapAllele2Num.put(allele, num);
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
		for (int i = 0; i < lsSite1.size(); i++) {
			String aRef2Alt = lsSite1.get(i);
			String bRef2Alt = lsSite2.get(i);
			if (aRef2Alt.equals("N") || bRef2Alt.equals("N")) {
				continue;
			}
			
			num++;
			if (aRef2Alt.equals(refa)) {
				if (bRef2Alt.equals(refb)) {
					ia1b1++;
				} else {
					ia1b2++;
				}
			} else {
				if (bRef2Alt.equals(refb)) {
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
