package com.novelbio.analysis.gwas;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.tools.FileObject;

import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

public class TestGwas {
	static PlinkPedReader plinkPedReader;

	public static void main(String[] args) {
		TxtReadandWrite txtWrite = new TxtReadandWrite("/home/novelbio/test/plink/result.txt", true);
		
		String plinkBim = "/home/novelbio/test/plink/619-40maf.addchr.bim";
		String plinkPed =  "/home/novelbio/test/plink/plink.ped";
		String plinkBimCorrect = "/home/novelbio/test/plink/plink.map.anno";
	
		String chrFile = "/home/novelbio/test/plink/chrAll.fa";
		String gffFile = "/home/novelbio/test/plink/all.gff3";
	
		PlinkPedReader.createPlinkPedIndex(plinkPed);
		plinkPedReader = new PlinkPedReader(plinkPed);
		List<String> lsSamples = plinkPedReader.getLsAllSamples();
		if (!FileOperate.isFileExistAndBigThan0(plinkBimCorrect)) {
			PlinkMapAddBase plinkMapAddBase = new PlinkMapAddBase(chrFile);
			plinkMapAddBase.addAnnoFromRef(plinkBim, plinkBimCorrect);

		}
	
		System.out.println("finish anno");
		PlinkMapReader plinkMapReader = new PlinkMapReader();
		plinkMapReader.setGffChrAbs(chrFile, gffFile);
		plinkMapReader.setPlinkMap(plinkBimCorrect);
		plinkMapReader.initial();
		
		DateUtil dateUtil = new DateUtil();
		dateUtil.setStartTime();
		
		while (!plinkMapReader.isFinish()) {
			List<Allele> lsAllele = plinkMapReader.readLsAlleles();
			if (lsAllele == null) {
				break;
			}
			System.out.println(plinkMapReader.getGene().getNameSingle());
			for (Allele allele : lsAllele) {
				System.out.println(allele.toString());
			}
			if (lsAllele.isEmpty()) {
				continue;
			}
			for (String sample : lsSamples) {
				List<Allele> lsAlleleSample = getLsAlleleFromSample(sample, lsAllele);
//				System.out.println("#########");
				txtWrite.writefileln("#########");
				txtWrite.writefileln(sample);

//				System.out.println(sample);
				for (Allele allele : lsAlleleSample) {
					txtWrite.writefileln(allele.toStringAlleleGwas());
//					System.out.println(allele.toStringAlleleGwas());
				}
				txtWrite.writefileln("#########");
//				System.out.println("#########");
			}
			System.out.println("==========================");

		}
		System.out.println(dateUtil.getElapseTime());
	}
	
	private static List<Allele> getLsAlleleFromSample(String sample, List<Allele> lsAlleles) {
		if (lsAlleles.isEmpty()) {
			return new ArrayList<>();
		}
		
		List<Allele> lsAlleleResult = new ArrayList<>();

		
		int start = lsAlleles.get(0).getIndex();

		Iterator<Allele> itAllelesRef = lsAlleles.iterator();
		Iterator<Allele> itAllelesSample = plinkPedReader.readAllelsFromSample(sample, start).iterator();
		
		Allele alleleRef = itAllelesRef.next();
		Allele alleleSample = itAllelesSample.next();
		while (true) {
			if (alleleRef.getIndex() == alleleSample.getIndex() ) {
				alleleSample.setRef(alleleRef);
				lsAlleleResult.add(alleleSample);
				if (!itAllelesRef.hasNext()) {
					break;
				}
				alleleRef = itAllelesRef.next();
				alleleSample = itAllelesSample.next();
				continue;
			} else if (alleleRef.getIndex() > alleleSample.getIndex()) {
				if (!itAllelesSample.hasNext()) {
					throw new ExceptionNBCPlink("error sample " + sample + " doesnot have " + alleleRef.toString());
				}
				alleleSample = itAllelesSample.next();
				continue;
			} else if (alleleRef.getIndex() < alleleSample.getIndex()) {
				throw new ExceptionNBCPlink("error sample " + sample + " doesnot have " + alleleRef.toString());
			}
		}
		
		return lsAlleleResult;
	}
}
