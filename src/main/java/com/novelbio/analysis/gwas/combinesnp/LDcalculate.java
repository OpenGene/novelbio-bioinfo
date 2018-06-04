package com.novelbio.analysis.gwas.combinesnp;

import java.util.ArrayList;
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
	
	
	public void setRefa(String refa) {
		this.refa = refa;
	}
	public void setRefb(String refb) {
		this.refb = refb;
	}
	public void setLsRef2AltSite1(List<String[]> lsRef2AltSite1) {
		this.lsRef2AltSite1 = lsRef2AltSite1;
	}
	public void setLsRef2AltSite2(List<String[]> lsRef2AltSite2) {
		this.lsRef2AltSite2 = lsRef2AltSite2;
	}
	
	public void calculate()  {
		prepare();
		calculateDetail();
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
		
		a1b1 = (double)ia1b1/num;
		a1b2 = (double)ia1b2/num;
		a2b1 = (double)ia2b1/num;
		a2b2 = (double)ia2b2/num;
		
		p1 = a1b1+a1b2;
		p2 = a2b1+a2b2;
		q1 = a1b1+a2b1;
		q2 = a1b2+a2b2;
	}
	
	private void calculateDetail() {
		double d = a1b1 - p1*q1;
		r2 = d*d/(p1*p2*q1*q2);
		if (Double.isInfinite(r2)) {
			System.err.println("d " + d);
			System.err.println("a1b1 " + a1b1);
			System.err.println("a1b2 " + a1b2);
			System.err.println("a2b1 " + a2b1);
			System.err.println("a2b2 " + a2b2);
			System.err.println("p1 " + p1);
			System.err.println("p2 " + p2);
			System.err.println("q1 " + q1);
			System.err.println("q2 " + q2);
		}
		double dmax = 0;
		if (d<= 0) {
			dmax = Math.max(-p1*q1, -p2*q2);
		} else {
			dmax = Math.min(p1*q2, p2*q1);
		}
		ddot = d/dmax;
	}
	
}
