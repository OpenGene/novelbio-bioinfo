package com.novelbio.analysis.seq.genome;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.ListGff;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.PatternOperate;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.model.species.Species;

public class GffSpeciesInfo {
	private static Logger logger = Logger.getLogger(GffSpeciesInfo.class);
	GffChrAbs gffChrAbs = new GffChrAbs();
	private String[] totals = new String[2];
	public static void main(String[] args) {
		Species species = new Species(9606);
		String outpath = "/home/ywd/cs/cs";
		GffSpeciesInfo specieInformation = new GffSpeciesInfo();
		specieInformation.writeGeneDescription(species, outpath);
	}

	public ArrayList<String[]> getChrLength(Species species) {
		/** 无序的表格 */
		ArrayList<String[]> lsChrLehgth = new ArrayList<String[]>();
		long total = 0;
		/** 第一列按照Chr1和Chr2，Chr3排序的表 */
		for (String string : species.getMapChromInfo().keySet()) {
			System.out.println(string);
			System.out.println(species.getMapChromInfo().get(string));
			String[] chrAndLength = new String[2];
			chrAndLength[0] = string;
			chrAndLength[1] =Long.toString(species.getMapChromInfo().get(string)) ;
			total = total + species.getMapChromInfo().get(string);
 			lsChrLehgth.add(chrAndLength);
		}
		totals[0] = "chrTotalLength";
		totals[1] = Long.toString(total);

		return lsChrLehgth;
	}
	
	/**
	 *把 lsChrLehgth第一列按照Chr1和Chr2，Chr3排序
	 * @return 
	 */
	public ArrayList<String[]> sortLsChrInfo(ArrayList<String[]> lsChrID2Length) {
		final PatternOperate patternChrNum = new PatternOperate("\\d+", false);
		Collections.sort(lsChrID2Length, new Comparator<String[]>() {
			@Override
			public int compare(String[] ChrID2Length1, String[] ChrID2Length2) {
				String num1Str = patternChrNum.getPatFirst(ChrID2Length1[0]);
				String num2Str = patternChrNum.getPatFirst(ChrID2Length2[0]);
				if (num1Str != null && num2Str != null) {
					Integer num1 = Integer.parseInt(num1Str);
					Integer num2 = Integer.parseInt(num2Str);
					return num1.compareTo(num2);
					} else {		
						return ChrID2Length1[0].compareTo(ChrID2Length2[0]);					
					}
				}
		});
		lsChrID2Length.add(totals);
		return lsChrID2Length;
	}

	/**
	 * 获取物种的所有的基因
	 */
	private HashSet<String> getSpeciesGene() {
		HashMap<String, ListGff> mapChrID2LsGff = new HashMap<String, ListGff>();
		HashSet<String> setGeneID = new HashSet<String>();
		mapChrID2LsGff = gffChrAbs.getGffHashGene().getMapChrID2LsGff();
		for (ListGff listGff : mapChrID2LsGff.values()) {
			for (GffDetailGene gffDetailGene : listGff) {
				for (GffGeneIsoInfo geneIsoInfo : gffDetailGene.getLsCodSplit()) {
					setGeneID.add(GeneID.removeDot(geneIsoInfo.getName()));
					System.out.println(GeneID.removeDot(geneIsoInfo.getName()));
				}
			}
		}
		return setGeneID;
	}
	/**
	 * 写所有的物种所有基因
	 */
	public void writeGeneBG(Species species,String outPath) {
		gffChrAbs.setSpecies(species);
		HashSet<String> setGeneID = getSpeciesGene();
		TxtReadandWrite txtReadandWrite = new TxtReadandWrite(outPath + "geneBG.txt", true);
		txtReadandWrite.writefileln("GeneSymbol");
		for (String string : setGeneID) {
			txtReadandWrite.writefileln(string);
		}
		txtReadandWrite.close();
	}
	/**
	 * 获取指定基因的各个外显子长度
	 * @param geneSymbol 指定基因
	 * @return
	 */
	private ArrayList<Integer>	getExonLength(String geneSymbol) {
		ArrayList<Integer> lsExonLength = new ArrayList<Integer>();
		GffGeneIsoInfo gffGeneIsoInfo = gffChrAbs.getGffHashGene().searchISO(geneSymbol);
		int ExonNum = gffGeneIsoInfo.getExonNum();
		for (int i = 1; i < ExonNum + 1; i++) {
			lsExonLength.add(gffGeneIsoInfo.getLenExon(i));
		}
		return lsExonLength;
	}
	/**
	 * 获取指定基因的各个内含子长度
	 * @param geneSymbol 指定基因
	 * @return
	 */
	private ArrayList<Integer> getIntronLength(String geneSymbol) {
		ArrayList<Integer> lsIntron = new ArrayList<Integer>();
		GffGeneIsoInfo gffGeneIsoInfo = gffChrAbs.getGffHashGene().searchISO(geneSymbol);
		int Intron = gffGeneIsoInfo.getLsIntron().size();
		for (int i = 1; i < Intron + 1; i++) {
			lsIntron.add(gffGeneIsoInfo.getLenIntron(i));
		}
		return lsIntron;
	}
	
	/**
	 *  写指定物种的所有基因的内含子和外显子信息
	 * @param species  物种	
	 * @param outpath  输出文件路径
	 */
	public void writeGeneDescription(Species species, String outpath) {
		gffChrAbs.setSpecies(species);
		String speciseName = species.getAbbrName();
		HashSet<String> setGeneID = getSpeciesGene();
		TxtReadandWrite txtWrite = new TxtReadandWrite(outpath + speciseName + "ExonAndIntronLehgth.txt", true);
		for (String geneID : setGeneID) {
			String lines;
			lines = geneID + "\t" + "ExonLength";
			for (int exonLength : getExonLength( geneID)) {
				lines = lines + "\t" + exonLength;
			} 
			lines = lines + "\t" + "InonLength";
			for (int intronLength : getIntronLength(geneID)) {
				lines = lines + "\t" + intronLength;
			}
			txtWrite.writefileln(lines);
		}
		txtWrite.close();
	}
	
/**
 * 指定物种写所有外显子的长度
 * @param species 指定物种
 * @param outpath 输出路径
 */
	public void writeExonLength(Species species, String outpath) {
		gffChrAbs.setSpecies(species);
		TxtReadandWrite txtWrite = new TxtReadandWrite(outpath + "ExonLength.txt", true);
		txtWrite.writefileln("ExonLength");
		Set<String> setGeneID = getSpeciesGene();
		for (String geneSymbol : setGeneID) {
			ArrayList<Integer> lsExon = getExonLength(geneSymbol);
			for (Integer exonLength : lsExon) {
				txtWrite.writefileln(Integer.toString(exonLength));
			}		
		}
		txtWrite.close();
	}
	/**
	 * 指定物种写所有内含子的长度
	 * @param species 指定物种
	 * @param outpath 输出路径
	 */
	public void writeIntronLength(Species species, String outpath) {
		gffChrAbs.setSpecies(species);
		TxtReadandWrite txtWrite  = new TxtReadandWrite(outpath + "IntronLength.txt", true);
		txtWrite.writefileln("IntronLength");
		Set<String> setGeneID = getSpeciesGene();
		for (String geneSymbol : setGeneID) {
			for (Integer lntron : getIntronLength(geneSymbol)) {
				txtWrite.writefileln(Integer.toString(lntron));
			}
		}
		txtWrite.close();
	}
}
