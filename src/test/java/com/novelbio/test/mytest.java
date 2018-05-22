package com.novelbio.test;


import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.math3.stat.inference.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.novelbio.analysis.seq.chipseq.RegionBed;
import com.novelbio.analysis.seq.chipseq.RegionBed.EnumTssPileUpType;
import com.novelbio.analysis.seq.fasta.ChrDensity;
import com.novelbio.analysis.seq.fasta.ChrSeqHash;
import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.fastq.FastQRecord;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailCG;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReads;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReads.ChrMapReadsInfo;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.analysis.seq.mapping.IndexMappingMaker;
import com.novelbio.analysis.seq.mapping.IndexMappingMaker.IndexMapSplice;
import com.novelbio.analysis.seq.sam.AlignSamReading;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.analysis.seq.sam.SamRecord;
import com.novelbio.base.StringOperate;
import com.novelbio.base.cmd.CmdMoveFileAli;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.cmd.CmdPathCluster;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.PatternOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.util.ServiceEnvUtil;
import com.novelbio.database.domain.modgeneid.GeneID;
import com.novelbio.database.domain.species.Species;
import com.novelbio.database.model.information.SoftWareInfo.SoftWare;
import com.novelbio.database.model.kegg.KGIDgen2Keg;
import com.novelbio.database.model.kegg.KGentry;
import com.novelbio.database.model.kegg.KGpathway;
import com.novelbio.database.service.servkegg.ServKEntry;
import com.novelbio.database.service.servkegg.ServKIDgen2Keg;
import com.novelbio.database.service.servkegg.ServKPathway;
import com.novelbio.listoperate.HistBin;
import com.novelbio.listoperate.HistList;

import smile.math.special.Beta;
import smile.stat.hypothesis.ChiSqTest;


public class mytest {
	private static final Logger logger = LoggerFactory.getLogger(mytest.class);
	static boolean is;
	
	public static void main(String[] args) throws IOException {
//		PatternOperate patternOperate = new PatternOperate("/media/nbfs/nbCloud/[\\w/\\._-]+");
//		System.out.println(patternOperate.getPatFirst("<script param=\"/media/nbfs/nbCloud/public/task/java/bio-info.jar\"/>"));
		
		
		changeScriptsToBin();
		
		
//		changePathToBin("/media/nbfs/nbCloud/public/taskdatabase", "${database_path}");

//		changePathToBin("/media/nbfs/nbCloud/public/task/scriptmodule/\\w+/scripts|/media/nbfs/nbCloud/public/task/scriptmodule/\\w+/software", "${task_bin_path}");
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
