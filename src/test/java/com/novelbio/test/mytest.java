package com.novelbio.test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqFastaHash;
import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffType;
import com.novelbio.analysis.seq.mapping.StrandSpecific;
import com.novelbio.base.dataOperate.HttpFetch;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.plot.GraphicCope;
import com.novelbio.database.service.SpringFactory;
import com.novelbio.nbcgui.controlseq.CtrlRNAmap;
import com.novelbio.nbcgui.controltest.CtrlTestGOInt;


public class mytest {
	private static Logger logger = Logger.getLogger(mytest.class);
	
	public static void main(String[] args) throws IOException, URISyntaxException {
		GffHashGene gffHashGene = new GffHashGene(GffType.NCBI, "/home/zong0jie/desktop/chr.gff");
		SeqHash seqHash = new SeqHash("/home/zong0jie/desktop/goatChrome");
		GffChrAbs gffChrAbs = new GffChrAbs();
		gffChrAbs.setSeqHash(seqHash);
		gffChrAbs.setGffHash(gffHashGene);
		int num = 0;
		TxtReadandWrite txtWrite = new TxtReadandWrite("/home/zong0jie/desktop/goatChrome/3utr", true);
		for (GffDetailGene gffDetailGene : gffHashGene.getGffDetailAll()) {
			GffGeneIsoInfo gffGeneIsoInfo = gffDetailGene.getLongestSplitMrna();
			SeqFasta seqFasta;
			if (gffGeneIsoInfo.isCis5to3()) {
				seqFasta = gffChrAbs.getSeqHash().getSeq(gffGeneIsoInfo.getRefID(), gffGeneIsoInfo.getEnd(), gffGeneIsoInfo.getEnd() + 500);
			} else {
				seqFasta = gffChrAbs.getSeqHash().getSeq(gffGeneIsoInfo.getRefID(), gffGeneIsoInfo.getEnd() - 500, gffGeneIsoInfo.getEnd());
				if (seqFasta != null) {
					seqFasta = seqFasta.reservecom();
				}
			}
			if (seqFasta != null) {
				seqFasta.setName(gffGeneIsoInfo.getName());
				txtWrite.writefileln(seqFasta.toStringNRfasta());
			}
		
			num++;
			System.out.println(num);
			if (num == 3830) {
				System.out.println();
			}
		}
		txtWrite.close();
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
