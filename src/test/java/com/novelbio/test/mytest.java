package com.novelbio.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.picard.fastq.FastqRecord;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.GeneExpTable;
import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqFastaHash;
import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.fastq.FastQRecord;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffOperate.GffCodGeneDU;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffType;
import com.novelbio.analysis.seq.mapping.StrandSpecific;
import com.novelbio.analysis.seq.mirna.MiRNACount;
import com.novelbio.analysis.seq.rnaseq.RPKMcomput.EnumExpression;
import com.novelbio.analysis.seq.sam.AlignSeqReading;
import com.novelbio.analysis.seq.sam.BamIndex;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.analysis.seq.sam.SamRecord;
import com.novelbio.base.dataOperate.HttpFetch;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.species.Species;
import com.novelbio.generalConf.PathDetailNBC;
import com.novelbio.generalConf.TitleFormatNBC;


public class mytest {
	private static final Logger logger = Logger.getLogger(mytest.class);
	static boolean is;
	public static void main(String[] args) {
//		SamFile samFile = new SamFile("/media/winE/tmp/test.sam");
//		SamFile samFile2 = new SamFile("/media/winE/tmp/testOrder.bam", samFile.getHeader());
//		int i = 0;
//		for (SamRecord samRecord : samFile.readLines()) {
//			if (i++ > 50000) {
//				break;
//			}
//			samFile2.writeSamRecord(samRecord);
//		}
//		samFile.close();
//		samFile2.close();
		
//		SamFile samFile = new SamFile("/media/winE/tmp/testOrder.bam");
//		SAMFileHeader header = samFile.getHeader();
		
//		TxtReadandWrite txtRead = new TxtReadandWrite("/media/hdfs/nbCloud/public/test/fastq/798B_CGATGT_L004_R2_001.fastq.gz");
//		int i = 0;
//		for (String string : txtRead.readlines()) {
//			if (i++ > 10) {
//				break;
//			}
//			System.out.println(string);
//		}
//		txtRead.close();
		
		SamFile samFile = new SamFile("/media/hdfs/nbCloud/public/test/RNASeqMap/R001_mapsplice.sort.bam");
//		samFile.indexMake();
//		BamIndex bamIndex = new BamIndex(samFile);
//		bamIndex.indexC();
//		int i = 0;
		for (SamRecord samRecord : samFile.readLinesContained("chr3", 23456789, 33456789)) {
//			if (i++ > 10) {
//				break;
//			}
			System.out.println(samRecord.toString());
		}
		
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
	
	private void testMapTophat() {
		Map<String, ArrayList<ArrayList<FastQ>>> mapPrefix2LsFQ = new HashMap<String, ArrayList<ArrayList<FastQ>>>();
		ArrayList<ArrayList<FastQ>> lsFQ = new ArrayList<ArrayList<FastQ>>();
		mapPrefix2LsFQ.put("aaa", lsFQ);
		ArrayList<FastQ> lsLeft = new ArrayList<FastQ>();
		ArrayList<FastQ> lsRight = new ArrayList<FastQ>();
		lsLeft.add(new FastQ("/media/winE/NBC/Project/Project_QZL/QZL-Filted/359_filtered.fastq"));
		lsRight.add(new FastQ("/media/winE/NBC/Project/Project_QZL/QZL-Filted/363_filtered.fastq"));
		lsFQ.add(lsLeft); lsFQ.add(lsRight);
		
		
		GffChrAbs gffChrAbs = new GffChrAbs(690566);
//		gffChrAbs.setChrFile("/home/zong0jie/Desktop/test/Schl_L-1/ChromFa", "NC");
//		gffChrAbs.setGffFile(0, GffType.NCBI, "/home/zong0jie/Desktop/test/Schl_L-1/gff/Schl_L-1.gff");
		CtrlRNAmap ctrlRNAmap = new CtrlRNAmap(CtrlRNAmap.TOP_HAT);
		ctrlRNAmap.setGffChrAbs(gffChrAbs);
		ctrlRNAmap.setGtfAndGene2Iso("");
		ctrlRNAmap.setMapPrefix2LsFastq(mapPrefix2LsFQ);
		ctrlRNAmap.setIndexFile("/home/zong0jie/Desktop/test/Schl_L-1/ChromFa/All/all.fa");
		ctrlRNAmap.setIsUseGTF(true);
		
		ctrlRNAmap.setOutPathPrefix("/home/zong0jie/Desktop/test/Schl_L-1/test");
		ctrlRNAmap.setStrandSpecifictype(StrandSpecific.NONE);
		ctrlRNAmap.setThreadNum(4);
		ctrlRNAmap.mapping();
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
