package com.novelbio.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqFastaHash;
import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.fastq.FastQRecord;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffOperate.GffCodGeneDU;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffType;
import com.novelbio.analysis.seq.mapping.MapBwaAln;
import com.novelbio.analysis.seq.mapping.StrandSpecific;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.analysis.seq.sam.SamRecord;
import com.novelbio.base.dataOperate.HttpFetch;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.fileOperate.FileOperate;


public class mytest {
	private static final Logger logger = Logger.getLogger(mytest.class);
	static boolean is;
	public static void main(String[] args) throws Exception {
		String name = "/hdfs:/nbCloud/public/nbcplatform/genome/Chrom_Sep/9606/testsss";
		TxtReadandWrite txtRead = new TxtReadandWrite(name);
		for (String string : txtRead.readlines()) {
			System.out.println(string);
		}
		txtRead.close();
		
		name = "/hdfs:/nbCloud/public/nbcplatform/genome/Chrom_Sep/9606/hg19_GRCh37/chr4.fa";
		txtRead = new TxtReadandWrite(name);
		for (String string : txtRead.readlines()) {
			System.out.println(string);
		}
		txtRead.close();
	}
 
	private static void copeGffWangxia2() {
		GffHashGene gffHashGeneOur = new GffHashGene(GffType.NCBI, "/media/winE/OutMrd1.mrd/ARZ_v2.gff3.gz");
		for (GffDetailGene gffDetailGeneOur : gffHashGeneOur.getGffDetailAll()) {
			List<GffGeneIsoInfo> lsIso = gffDetailGeneOur.getLsCodSplit();
			GffGeneIsoInfo isoLong = gffDetailGeneOur.getLongestSplitMrna();
			for (GffGeneIsoInfo isoShort : lsIso) {
				if (isoLong.equals(isoShort) || isoLong.isCis5to3() == isoShort.isCis5to3()) {
					continue;
				}
				double[] longInt = new double[]{isoLong.getStartAbs(), isoLong.getEndAbs()};
				double[] shortInt = new double[]{isoShort.getStartAbs(), isoShort.getEndAbs()};
				if (ArrayOperate.cmpArray(longInt, shortInt)[3] > 0.7) {
					double midShort = MathComput.mean(shortInt);
					if (isoLong.getATGsite() > midShort && isoLong.getUAGsite() > midShort) {
						isoLong = isoLong.subGffGeneIso(isoShort.getEndAbs(), isoLong.getEndAbs());
					} else if (isoLong.getATGsite() < midShort && isoLong.getUAGsite() < midShort) {
						isoLong = isoLong.subGffGeneIso(isoLong.getStartAbs(), isoShort.getStartAbs());
					}
				}
			}
			gffDetailGeneOur.removeIso(isoLong.getName());
			gffDetailGeneOur.addIso(isoLong);
		}
		gffHashGeneOur.writeToGTF("/media/winE/OutMrd1.mrd/ARZ_v2_coped.gtf");
	}
	
	private void copeGffWangxia() {
		GffHashGene gffHashGeneOther = new GffHashGene(GffType.NCBI, "");
		GffHashGene gffHashGeneOur = new GffHashGene(GffType.GTF, "");
		Set<GffDetailGene> addOther = new HashSet<>();
		Set<GffDetailGene> removeOur = new HashSet<>();
		for (GffDetailGene gffDetailGeneOur : gffHashGeneOur.getGffDetailAll()) {
			GffCodGeneDU gffCodGeneDU = gffHashGeneOther.searchLocation(gffDetailGeneOur.getRefID(), gffDetailGeneOur.getStartAbs(), gffDetailGeneOur.getEndAbs());
			Set<GffDetailGene> setGffDetailGenes = gffCodGeneDU.getCoveredOverlapGffGene();
			if (setGffDetailGenes.size() <= 1) {
				continue;
			}
			double[] regionThis = new double[]{gffDetailGeneOur.getStartAbs(), gffDetailGeneOur.getEndAbs()};
			Set<GffDetailGene> setThisOther = new HashSet<>();
			for (GffDetailGene gffDetailGeneOther : setGffDetailGenes) {
				double[] regionOther = new double[]{gffDetailGeneOther.getStartAbs(), gffDetailGeneOther.getEndAbs()};
				if (ArrayOperate.cmpArray(regionOther, regionThis)[2] >= 0.6) {
					setThisOther.add(gffDetailGeneOther);
				}
			}
			if (setThisOther.size() > 1) {
				addOther.addAll(setThisOther);
				removeOur.add(gffDetailGeneOur);
			}
		}
	}
	
	
	/** 将有问题的fastq文件整理为正常的 */
	public static void makeFastqFile() {
	
	}
	
	public static void fanweiCope() {
		List<String> lsList =FileOperate.getFoldFileNameLs("/media/hdfs/nbCloud/staff/fanwei/9311tmp/", "*", "bam");
		for (String samName : lsList) {
			SamFile samFile = new SamFile(samName);
			String samNameNew = FileOperate.changeFileSuffix(samName, "_unique", null);
			SamFile samFileUnique = new SamFile(samNameNew, samFile.getHeader());
			for (SamRecord smRecord : samFile.readLines()) {
				if (smRecord.isUniqueMapping()) {
					samFileUnique.writeSamRecord(smRecord);
				}
			}
			samFile.close();
			samFileUnique.close();
		}
	}
	
	private static double calculPIC(double[] allen) {
		List<Double> lsAllen = new ArrayList<>();
		for (Double double1 : allen) {
			lsAllen.add(double1);
		}
		return calculPIC(lsAllen);
	}
	private static double calculPICSimple(double[] allen) {
		List<Double> lsAllen = new ArrayList<>();
		for (Double double1 : allen) {
			lsAllen.add(double1);
		}
		return calculPICSimple(lsAllen);
	}
	
	private static double calculPIC(List<Double> lsAllen) {
		double value = 0;
		for (Double double1 : lsAllen) {
			value+=double1;
		}
		if (value != 1) {
			throw new RuntimeException("error");
		}
		
		double first = 0;
		for (Double double1 : lsAllen) {
			first += Math.pow(double1,2);
		}
		
		double second = 0;
		for (int i = 0; i < lsAllen.size()-1; i++) {
			for (int j = i+1; j < lsAllen.size(); j++) {
				double pi = lsAllen.get(i);
				double pj = lsAllen.get(j);
				second += 2*Math.pow(pi, 2)*Math.pow(pj, 2);
			}
		}
		double result = 1 - first - second;
		return result;
		
	}
	
	private static double calculPICSimple(List<Double> lsAllen) {
		double value = 0;
		for (Double double1 : lsAllen) {
			value+=double1;
		}
		if (value != 1) {
			throw new RuntimeException("error");
		}
		
		double first = 0;
		for (Double double1 : lsAllen) {
			first += Math.pow(double1,2);
		}
		double second = Math.pow(first, 2);
		double third = 0;
		for (Double double1 : lsAllen) {
			third += Math.pow(double1,4);
		}
		double result = 1 - first - second + third;
		return result;
		
	}
	
	
	private static SamRecord[] get1and2(SamRecord samRecord1, SamRecord samRecord2) {
		if (samRecord1.isFirstRead()) {
			return new SamRecord[]{samRecord1, samRecord2};
		} else {
			return new SamRecord[]{samRecord2, samRecord1};
		}
	}
	
	
	private static int compare(String[] s1, String[] s2) {
		Double scoreThis = Double.parseDouble(s1[11]);
		Double scoreO = Double.parseDouble(s2[11]);
		Double identityThis = Double.parseDouble(s1[2]);
		Double identityO = Double.parseDouble(s2[2]);
		Double evalueThis = Double.parseDouble(s1[10]);
		Double evalueO = Double.parseDouble(s2[10]);
		
		int result = -scoreThis.compareTo(scoreO);
		if (result == 0) {
			result = -identityThis.compareTo(identityO);
		}
		if (result == 0) {
			result = evalueThis.compareTo(evalueO);
		}
		return result;
	}
	
	
	private void wzfBlast() {
		TxtReadandWrite txtRead = new TxtReadandWrite("/media/winF/NBC/Project/Project_WZF/compareGenomic/blast/result", false);
		Set<String> setGeneName = new HashSet<String>();
		for (String string : txtRead.readlines()) {
			String[] ss = string.split("\t");
			if (Double.parseDouble(ss[10]) < 1e-40) {
				setGeneName.add(ss[0].toLowerCase());
			}
		}
		List<String> lsGeneNameNoHomo = new ArrayList<String>();
		SeqFastaHash seqFastaHash = new SeqFastaHash("/media/winF/NBC/Project/Project_WZF/compareGenomic/blast/ss070731_nr.txt", null, true);
		for (String seqName : seqFastaHash.getLsSeqName()) {
			if (setGeneName.contains(seqName.toLowerCase())) {
				continue;
			}
			lsGeneNameNoHomo.add(seqName);
		}
		
		TxtReadandWrite txtOut = new TxtReadandWrite("/media/winF/NBC/Project/Project_WZF/compareGenomic/blast/ss070731_no_homo.txt", true);
		for (String seqName : lsGeneNameNoHomo) {
			SeqFasta seqFasta = seqFastaHash.getSeqFasta(seqName);
			txtOut.writefileln(seqFasta.toStringNRfasta());
		}
		txtOut.close();
		for (String string : lsGeneNameNoHomo) {
			System.out.println(string);
		}
	}
	
	/**
	 * 获得pixiv的cookies
	 */
    public static void getcookies(HttpFetch webFetch) {
    	if (webFetch == null) {
			webFetch = HttpFetch.getInstance();
		}
    	if (webFetch.getCookies() != null) {
    		return;
    	}
    	Map<String, String> mapPostKey2Value = new HashMap<String, String>();
    	mapPostKey2Value.put("mode", "login");
    	mapPostKey2Value.put("pixiv_id", "facemun");
    	mapPostKey2Value.put("pass", "f12344321n");
    	webFetch.setPostParam(mapPostKey2Value);
    	webFetch.setUri("http://www.pixiv.net/index.php");
    	if (!webFetch.query()) {
			getcookies(webFetch);
		}
   }
    
    private static String changeFileSuffix(String fileName, String append, String suffix) {
		int endDot = fileName.lastIndexOf(".");
		int indexSep = Math.max(fileName.indexOf("/"), fileName.indexOf("\\"));
		String result;
		if (endDot > indexSep) {
			result = fileName.substring(0, endDot);
		} else {
			result = fileName;
		}
		suffix = suffix.trim();
		if (!suffix.equals("")) {
			suffix = "." + suffix;
		}
		return result + append + suffix;
    }

}
