package com.novelbio.test;


import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.novelbio.base.StringOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.PatternOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.bioinfo.fasta.SeqHash;
import com.novelbio.bioinfo.gff.GffCodGeneDU;
import com.novelbio.bioinfo.gff.GffGene;
import com.novelbio.bioinfo.gff.GffHashGene;
import com.novelbio.software.snpanno.SnpInfo;
import com.sun.jndi.toolkit.url.Uri;

import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;


public class mytest {
	private static final Logger logger = LoggerFactory.getLogger(mytest.class);
	static boolean is;
	static int taxId;
	
	public static void main(String[] args) throws IOException, URISyntaxException {
		Boolean b = new Boolean("true");
		Boolean c = new Boolean("true");
		System.out.println(b == true);
		System.out.println(c == true);
		System.out.println(b == c);

		
	}
	
	public static void mergeVcf() {
		TxtReadandWrite txtRead = new TxtReadandWrite("/home/novelbio/mywork/nongkeyuan/3k-rice/NB_final_snp.bim");
		TxtReadandWrite txtRead2 = new TxtReadandWrite("/home/novelbio/mywork/nongkeyuan/3k-rice/Nipponbare_indel.bim");
		TxtReadandWrite txtOut = new TxtReadandWrite("/home/novelbio/mywork/nongkeyuan/3k-rice/NB_final_all.bim", true);
		for (String string : txtRead.readlines()) {
			if (StringOperate.isRealNull(string)) {
				continue;
			}
			txtOut.writefileln(string);
		}
		for (String string : txtRead2.readlines()) {
			if (StringOperate.isRealNull(string)) {
				continue;
			}
			txtOut.writefileln(string);
		}
		txtOut.close();
		txtRead.close();
		txtRead2.close();
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
				String geneName = gffDetailGene.getName();
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
