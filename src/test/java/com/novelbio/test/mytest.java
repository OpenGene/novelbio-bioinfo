package com.novelbio.test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.fs.FileUtil;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqFastaHash;
import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.analysis.seq.fastq.FQrecordFilter;
import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.fastq.FastQRecord;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.GffChrSeq;
import com.novelbio.analysis.seq.genome.gffOperate.GffCodGeneDU;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffFile;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffType;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene.GeneStructure;
import com.novelbio.analysis.seq.sam.BamRemoveDuplicate;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.analysis.seq.sam.SamRecord;
import com.novelbio.base.dataOperate.HttpFetch;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.geneanno.NCBIID;
import com.novelbio.database.domain.geneanno.TaxInfo;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.kegg.KGIDgen2Keg;
import com.novelbio.database.domain.kegg.KGentry;
import com.novelbio.database.domain.kegg.KGpathway;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.model.modgeneid.GeneType;
import com.novelbio.database.model.species.Species;
import com.novelbio.database.mongorepo.geneanno.RepoNCBIID;
import com.novelbio.database.mongorepo.kegg.RepoKEntry;
import com.novelbio.database.mongorepo.kegg.RepoKIDKeg2Ko;
import com.novelbio.database.mongorepo.kegg.RepoKIDgen2Keg;
import com.novelbio.database.mongorepo.kegg.RepoKNCompInfo;
import com.novelbio.database.mongorepo.kegg.RepoKNIdKeg;
import com.novelbio.database.mongorepo.kegg.RepoKPathRelation;
import com.novelbio.database.mongorepo.kegg.RepoKPathway;
import com.novelbio.database.mongorepo.kegg.RepoKReaction;
import com.novelbio.database.mongorepo.kegg.RepoKRelation;
import com.novelbio.database.mongorepo.kegg.RepoKSubstrate;
import com.novelbio.database.service.SpringFactoryBioinfo;


public class mytest {
//	private static final Logger logger = Logger.getLogger(mytest.class);
	static boolean is;

	public static void main(String[] args) throws Exception {
		
//		GeneID geneID = new GeneID("HIST1H2BL", 9606);
//		RepoKIDgen2Keg repoKIDgen2Keg = SpringFactoryBioinfo.getBean(RepoKIDgen2Keg.class);
//		System.out.println(geneID.getGeneUniID());
//		KGIDgen2Keg kgiDgen2Keg = repoKIDgen2Keg.findByGeneId(Long.parseLong(geneID.getGeneUniID()));
//		System.out.println(kgiDgen2Keg.getKeggID());
		
		
//		MongoTemplate mongoTemplate = (MongoTemplate)SpringFactoryBioinfo.getFactory().getBean("mongoTemplate");
//		Query query = new Query( Criteria.where("taxID").is(10090));
//		List<KGentry> lskGentries = mongoTemplate.find(query, KGentry.class);
//		Map<String, KGentry> mapKey2Entry = new HashMap<>();
//		for (KGentry kGentry : lskGentries) {
//			mapKey2Entry.put(kGentry.getPathName() + "_" + kGentry.getEntryId() + "_" + kGentry.getEntryName(), kGentry);
//		}
//		System.out.println(lskGentries.size());
//		
		
//		RepoKEntry repoKEntry = SpringFactoryBioinfo.getBean(RepoKEntry.class);
//		List<KGentry> lskGentries = repoKEntry.findByNamePath("hsa:8340", "path:hsa05034");
//		for (KGentry kGentry : lskGentries) {
//			System.out.println(kGentry.getEntryId());
//		}
		GffHashGene gffHashGene = new GffHashGene("/media/nbfs/nbCloud/public/nbcplatform/genome/species/9606/hg19_GRCh37/gff/ref_GRCh37.p13_top_level.gff3.gz");
		gffHashGene.writeToGTF("/home/novelbio/hg19_p13.gtf");
		
//		for (String key : mapKey2Entry.keySet()) {
//			if (!mapKey2EntryKeg.containsKey(key)) {
//				System.out.println(key);
//			}
//		}
		
		
		System.out.println();
	}
	
	private static String getSeq(byte[] readInfo) {
		StringBuilder sequence = new StringBuilder();
		for (byte b : readInfo) {
			char seq = (char)b;
			sequence.append(seq);
		}
		return sequence.toString();
	}
	
	private static void filter(String fastq1, String fastq2) {
		FastQ fastQ1 = new FastQ(fastq1);
		FastQ fastQ2 = new FastQ(fastq2);
		int subNum = 1;
		long num = 0;
		FastQ fastqWrite1 = null;
		FastQ fastqWrite2 = null; 
		for (FastQRecord[] fastQRecords : fastQ1.readlinesPE(fastQ2)) {
			if (num % 7500_0000 == 0) {
				if (fastqWrite1 != null) {
					fastqWrite1.close();
					fastqWrite2.close();
				}
				fastqWrite1 = new FastQ(FileOperate.changeFileSuffix(fastq1, "_"+subNum, null), true);
				fastqWrite2 = new FastQ(FileOperate.changeFileSuffix(fastq2, "_"+subNum, null), true);
				subNum++;
				num = 0;
			}
			
			FastQRecord q1 = fastQRecords[0];
			FastQRecord q2 = fastQRecords[1];
			setName(q1);
			setName(q2);
			fastqWrite1.writeFastQRecord(q1);
			fastqWrite2.writeFastQRecord(q2);
			num++;
		}
		
		fastqWrite1.close();
		fastqWrite2.close();
	}
	
	private static void setName(FastQRecord fastQRecord) {
		String name = fastQRecord.getName();
		String[] ss = name.split(":");
		String[] ss2 = new String[ss.length - 2];
		for (int i = 2; i < ss.length; i++) {
			ss2[i-2] = ss[i];
		}
		fastQRecord.setName(ArrayOperate.cmbString(ss2, ":"));
		FQrecordFilter.trimSeq(fastQRecord, 0, 0, 0, 125);
	}
	
	private void extractMiRNASeq() {
		TxtReadandWrite txtWrite = new TxtReadandWrite("/home/novelbio/NBCsource/miRNApromoter.fa", true);
		int upstream = 1500;
		Species species = new Species(9606);
		species.setVersion("hg19_GRCh37");
		GffChrAbs gffChrAbs = new GffChrAbs(species);
		List<GffDetailGene> lsDetailGenes = gffChrAbs.getGffHashGene().getLsGffDetailGenes();
		for (GffDetailGene gffDetailGene : lsDetailGenes) {
			GffGeneIsoInfo isoMirna = null;
			for (GffGeneIsoInfo iso : gffDetailGene.getLsCodSplit()) {
				if (iso.getGeneType() == GeneType.miRNA) {
					isoMirna = iso;
					break;
				}
			}
			
			if (isoMirna != null) {
				int tss = isoMirna.getParentGffDetailGene().getStartCis();
				int up = isoMirna.isCis5to3()?  tss - 1500 : tss + 1500;
				SeqFasta seqUp = gffChrAbs.getSeqHash().getSeq(isoMirna.getRefID(), Math.min(tss, up), Math.max(tss, up));
				if (seqUp == null) {
					continue;
				}
				if (!isoMirna.isCis5to3()) {
					seqUp = seqUp.reservecom();
				}
				seqUp.setName(isoMirna.getName());
				txtWrite.writefileln(seqUp.toStringNRfasta());
			}
		}
		txtWrite.close();
	}
	
	private void deletdb() {
		RepoKEntry a = SpringFactoryBioinfo.getFactory().getBean(RepoKEntry.class);
		RepoKPathRelation b = SpringFactoryBioinfo.getFactory().getBean(RepoKPathRelation.class);
		RepoKReaction c = SpringFactoryBioinfo.getFactory().getBean(RepoKReaction.class);
		RepoKIDgen2Keg d = SpringFactoryBioinfo.getFactory().getBean(RepoKIDgen2Keg.class);
		RepoKIDKeg2Ko e = SpringFactoryBioinfo.getFactory().getBean(RepoKIDKeg2Ko.class);
		RepoKNCompInfo f = SpringFactoryBioinfo.getFactory().getBean(RepoKNCompInfo.class);
		RepoKNIdKeg g = SpringFactoryBioinfo.getFactory().getBean(RepoKNIdKeg.class);
		RepoKPathway h = SpringFactoryBioinfo.getFactory().getBean(RepoKPathway.class);
		RepoKSubstrate i = SpringFactoryBioinfo.getFactory().getBean(RepoKSubstrate.class);
		RepoKRelation j = SpringFactoryBioinfo.getFactory().getBean(RepoKRelation.class);
		
		try {
			a.deleteAll();
		} catch (Exception e2) {
			// TODO: handle exception
		}
		try {
			b.deleteAll(); 
		} catch (Exception e2) {
			// TODO: handle exception
		}
		try {
			c.deleteAll(); 
		} catch (Exception e2) {
			// TODO: handle exception
		}  
		try {
			d.deleteAll(); 
		} catch (Exception e2) {
			// TODO: handle exception
		}
		try {
			e.deleteAll();
		} catch (Exception e2) {
			// TODO: handle exception
		}
		try {
			f.deleteAll();
		} catch (Exception e2) {
			// TODO: handle exception
		}
		try {
			 g.deleteAll();
		} catch (Exception e2) {
			// TODO: handle exception
		}
		try {
			h.deleteAll();
		} catch (Exception e2) {
			// TODO: handle exception
		}
		try {
			 i.deleteAll(); 
		} catch (Exception e2) {
			// TODO: handle exception
		}
		try {
			j.deleteAll();
		} catch (Exception e2) {
			// TODO: handle exception
		}
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
