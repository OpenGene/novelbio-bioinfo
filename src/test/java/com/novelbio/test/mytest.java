package com.novelbio.test;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.math3.stat.inference.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.novelbio.base.StringOperate;
import com.novelbio.base.cmd.CmdMoveFileAli;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.cmd.CmdPathCluster;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.PatternOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.fileOperate.SeekablePathInputStream;
import com.novelbio.base.util.ServiceEnvUtil;
import com.novelbio.bioinfo.base.Align;
import com.novelbio.bioinfo.fasta.ChrDensity;
import com.novelbio.bioinfo.fasta.ChrSeqHash;
import com.novelbio.bioinfo.fasta.SeqHash;
import com.novelbio.bioinfo.fastq.FastQ;
import com.novelbio.bioinfo.fastq.FastQRecord;
import com.novelbio.bioinfo.gff.GffCodGeneDU;
import com.novelbio.bioinfo.gff.GffDetailCG;
import com.novelbio.bioinfo.gff.GffGene;
import com.novelbio.bioinfo.gff.GffHashGene;
import com.novelbio.bioinfo.gff.GffIso;
import com.novelbio.bioinfo.gffchr.GffChrAbs;
import com.novelbio.bioinfo.mappedreads.MapReads;
import com.novelbio.bioinfo.mappedreads.MapReads.ChrMapReadsInfo;
import com.novelbio.bioinfo.sam.AlignSamReading;
import com.novelbio.bioinfo.sam.SamFile;
import com.novelbio.bioinfo.sam.SamRecord;
import com.novelbio.database.domain.modgeneid.GeneID;
import com.novelbio.database.domain.species.IndexMappingMaker;
import com.novelbio.database.domain.species.Species;
import com.novelbio.database.domain.species.IndexMappingMaker.IndexMapSplice;
import com.novelbio.database.model.information.SoftWareInfo.SoftWare;
import com.novelbio.database.model.kegg.KGIDgen2Keg;
import com.novelbio.database.model.kegg.KGentry;
import com.novelbio.database.model.kegg.KGpathway;
import com.novelbio.database.service.servkegg.ServKEntry;
import com.novelbio.database.service.servkegg.ServKIDgen2Keg;
import com.novelbio.database.service.servkegg.ServKPathway;
import com.novelbio.listoperate.HistBin;
import com.novelbio.listoperate.HistList;
import com.novelbio.software.gbas.Allele;
import com.novelbio.software.tssplot.RegionBed;
import com.novelbio.software.tssplot.RegionBed.EnumTssPileUpType;
import com.sun.corba.se.spi.orb.StringPair;

import smile.math.special.Beta;
import smile.stat.hypothesis.ChiSqTest;


public class mytest {
	private static final Logger logger = LoggerFactory.getLogger(mytest.class);
	static boolean is;
	static int taxId;
	public static void main(String[] args) {
		
		System.out.println(taxId);
		
		
//		TxtReadandWrite txtRead = new TxtReadandWrite("/home/novelbio/mywork/nongkeyuan/rice_anno/RAP-MSU_2018-03-29.txt");
//		Set<String> setMsuOnly = new HashSet<>();
//		for (String content : txtRead.readlines()) {
//			String[] ss = content.split("\t");
//			if (ss[0].equals("None")) {
//				setMsuOnly.add(GeneID.removeDot(ss[1]));
//			}
//		}
//		
//		TxtReadandWrite txtReadNcbi2Msu = new TxtReadandWrite("/home/novelbio/mywork/nongkeyuan/rice_anno/ncbi2tigr.txt");
//		TxtReadandWrite txtWrite = new TxtReadandWrite("/home/novelbio/mywork/nongkeyuan/rice_anno/ncbi2tigr.simle.txt", true);
//
//		for (String content : txtReadNcbi2Msu.readlines()) {
//			String[] ss = content.split("\t");
//			if (setMsuOnly.contains(ss[1])) {
//				txtWrite.writefileln(content);
//			}
//		}
//		txtRead.close();
//		txtReadNcbi2Msu.close();
//		txtWrite.close();
		
	}
	
	public static void main2(String[] args) throws IOException {
		TxtReadandWrite txtReadBim = new TxtReadandWrite("/home/novelbio/zongjiework/result-gene-8-NoChange/permutation.bim");
		Set<String> setMarker = new HashSet<>();
		for (String content : txtReadBim.readlines()) {
			String[] ss = content.split("\t");
			if (ss[4].equals("0") || ss[5].equals("0")) {
				setMarker.add(ss[1]);
			}
		}
		txtReadBim.close();
		TxtReadandWrite txtReadMap = new TxtReadandWrite("/home/novelbio/zongjiework/result-gene-8-NoChange/permutation.plink.map");
		TxtReadandWrite txtReadMapNew = new TxtReadandWrite("/home/novelbio/zongjiework/result-gene-8-NoChange/permutation.plink.filter.map", true);
		TxtReadandWrite txtReadPedPre = new TxtReadandWrite("/home/novelbio/zongjiework/result-gene-8-NoChange/permutation.plink.ped.pre");
		Iterator<String> itPedPre = txtReadPedPre.readlines().iterator();
		TxtReadandWrite txtReadPedPreNew = new TxtReadandWrite("/home/novelbio/zongjiework/result-gene-8-NoChange/permutation.plink.filter.ped.pre", true);
		String contentPedPre = itPedPre.next();
		txtReadPedPreNew.writefileln(contentPedPre);
		
		for (String contentMap : txtReadMap.readlines()) {
			contentPedPre = itPedPre.next();
			String[] ss = contentMap.split("\t");
			if (setMarker.contains(ss[1])) {
				continue;
			}
			txtReadMapNew.writefileln(contentMap);
			txtReadPedPreNew.writefileln(contentPedPre);
		}
		txtReadMap.close();
		txtReadMapNew.close();
		txtReadPedPre.close();
		txtReadPedPreNew.close();
		
	}
	
	private static void addGeneToMap(Map<String, String[]> mapGene2Info, String[] info) {
		if (!mapGene2Info.containsKey(info[0])) {
			mapGene2Info.put(info[0], info);
		} else {
			String[] infoOld = mapGene2Info.get(info[0]);
			if (Double.parseDouble(infoOld[3]) > Double.parseDouble(info[3])) {
				mapGene2Info.put(info[0], info);
			}
		}
	}
	
	private static void gwasAnno() {
		GffHashGene gffHashGene = new GffHashGene();
		gffHashGene.addGeneNameFlag("ID=gene:");
		gffHashGene.addTranscriptNameFlag("ID=transcript:");
		gffHashGene.readGffFile("/home/novelbio/zongjiework/reference/Oryza_sativa.IRGSP-1.0.39.gff3");
		TxtReadandWrite txtRead = new TxtReadandWrite("/home/novelbio/zongjiework/zongjie-agorithm/PHHN2015-619-650-LMM.normal.head3000.txt");
		TxtReadandWrite txtWrite = new TxtReadandWrite("/home/novelbio/zongjiework/zongjie-agorithm/PHHN2015-619-650-LMM.normal.head3000.anno-200kb.txt", true);

		String title = txtRead.readFirstLine();
		title = "GeneId\t"+title;
		txtWrite.writefileln(title);
		for (String content : txtRead.readlines(2)) {
			String[] ss = content.split("\t");
			String chrId = ss[1];
			int position = Integer.parseInt(ss[3]);
			int posStart = position-200_000;
			int posEnd = position+200_000;
			GffCodGeneDU gffCodGeneDU = gffHashGene.searchLocation(chrId, posStart, posEnd);
			Set<GffGene> lsGenes = gffCodGeneDU.getCoveredOverlapGffGene();
			for (GffGene gffDetailGene : lsGenes) {
				String geneName = gffDetailGene.getNameSingle();
				txtWrite.writefileln(geneName + "\t" + content);
			}
		}
		txtRead.close();
		txtWrite.close();
	}
	
	public static void changeScriptsToBin() {
		Set<String> setPath = new HashSet<>();
		
		PatternOperate patternOperate = new PatternOperate("/media/nbfs/nbCloud/[\\w/\\._-]+");
		List<String> lsTasks = FileOperate.getLsFoldFileName("hdfs:/nbCloud/public/task/scriptmodule/");
		List<String> lsFiles = new ArrayList<>();
		for (String task : lsTasks) {
			lsFiles.addAll(FileOperate.getLsFoldFileName(task+"/Prepare"));
			lsFiles.addAll(FileOperate.getLsFoldFileName(task+"/Run"));
			lsFiles.addAll(FileOperate.getLsFoldFileName(task+"/Summary"));
		}
		List<String> lsNew = new ArrayList<>();
		
		for (String file : lsFiles) {
			if (!file.endsWith("xml")) {
				continue;
			}
			TxtReadandWrite txtRead = new TxtReadandWrite(file);
			for (String content : txtRead.readlines()) {
//				String info = patternOperate.getPatFirst(content);
//				if (!StringOperate.isRealNull(info)) {
//					setPath.add(FileOperate.getParentPathNameWithSep(info));
//				}
				if (content.contains("${database_path}")) {
					System.out.println(file);
					break;
				}
			}
			txtRead.close();
		}
//		for (String string : setPath) {
//			System.out.println(string);
//		}
	}
	
	public static void changePathToBin(String pattern, String replace) {
		PatternOperate patternOperate = new PatternOperate(pattern);		
		List<String> lsTasks = FileOperate.getLsFoldFileName("hdfs:/nbCloud/public/task/scriptmodule/");
		List<String> lsFiles = new ArrayList<>();
		for (String task : lsTasks) {
			lsFiles.addAll(FileOperate.getLsFoldFileName(task+"/Prepare"));
			lsFiles.addAll(FileOperate.getLsFoldFileName(task+"/Run"));
			lsFiles.addAll(FileOperate.getLsFoldFileName(task+"/Summary"));
		}
		List<String> lsNew = new ArrayList<>();
		
		for (String file : lsFiles) {
			if (!file.endsWith("xml")) {
				continue;
			}
			TxtReadandWrite txtRead = new TxtReadandWrite(file);
			for (String content : txtRead.readlines()) {
				if (!StringOperate.isRealNull(patternOperate.getPatFirst(content))) {
					lsNew.add(file);
					break;
				}
			}
			txtRead.close();
		}
		
		Set<String> setChanged = new HashSet<>();
		for (String file : lsNew) {
			String parentPath = FileOperate.getParentPathNameWithSep(file);
			parentPath = FileOperate.getParentPathNameWithSep(parentPath);
			if (!setChanged.contains(parentPath)) {
				FileOperate.moveFile(true, parentPath + "scripts", parentPath + "bin");
				FileOperate.moveFile(true, parentPath + "software", parentPath + "bin");
				setChanged.add(parentPath);
			}
			TxtReadandWrite txtRead = new TxtReadandWrite(file);
			TxtReadandWrite txtWrite = new TxtReadandWrite(file+".new", true);
			for (String content : txtRead.readlines()) {
				String pat = patternOperate.getPatFirst(content);
				if (!StringOperate.isRealNull(pat)) {
					content = content.replace(pat, replace);
				}
				txtWrite.writefileln(content);
			}
			txtRead.close();
			txtWrite.close();
			System.out.println("change file: " + file);
			FileOperate.moveFile(true, file+".new", file);
		}
	}
	
}
