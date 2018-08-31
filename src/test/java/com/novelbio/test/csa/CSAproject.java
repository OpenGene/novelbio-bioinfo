package com.novelbio.test.csa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.google.common.collect.HashMultimap;
import com.mongodb.util.StringParseUtil;
import com.novelbio.base.StringOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.bioinfo.base.GeneExpTable;
import com.novelbio.bioinfo.base.GeneExpTable.EnumAddAnnoType;
import com.novelbio.software.expression.RPKMcomput.EnumExpression;

public class CSAproject {
	
	
	public static void main(String[] args) {
//		mergeDifGenes("c:/Users/zong0/mywork/csa/difgene/affy-zhanghui-limma/fold-change-4/", "c:/Users/zong0/mywork/csa/difgene/affy-zhanghui-limma/fold-change-4/All_gene.txt");
		
		addTagsOnClusterTxt();
	}
	
	
	/**
	 * 给了一些拟南芥Id，需要比对到水稻上去，每个araId比对到最多10个riceId上去。
	 * 因为两个araId可能比对到同一个riceId上，所以需要去重复。仅保留evalue小的那个
	 */
	public static void filterBlastResult() {
		/** 第一列araId，第二列riceId，第三列evalue */
		String blastFile = "c:/Users/zong0/mywork/csa/compareWtih3rd/yujing-starch/blast-arabidopsis-starch-synthetise.txt";
		String resultFile = FileOperate.changeFileSuffix(blastFile, ".filter", null);
		
		TxtReadandWrite txtRead = new TxtReadandWrite(blastFile);
		String title = txtRead.readFirstLine();
		List<String> lsTmp = new ArrayList<>();
		for (String content : txtRead.readlines(2)) {
			String[] ss = content.split("\t");
			if (Double.parseDouble(ss[2]) > 1e-10) {
				continue;
			}
			lsTmp.add(content);
		}
		txtRead.close();
		
		TxtReadandWrite txtWrite = new TxtReadandWrite(resultFile, true);
		txtWrite.writefileln(title);
		Map<String, String[]> mapRiceId2AraId = new LinkedHashMap<>();
		for (String content : lsTmp) {
			String[] ss = content.split("\t");
			String araId = ss[0], riceId = ss[1];
			double evalue = Double.parseDouble(ss[2]);
			
			if (mapRiceId2AraId.containsKey(riceId)) {
				String[] ara2evalue = mapRiceId2AraId.get(riceId);
				double evalueOld = Double.parseDouble(ara2evalue[1]);
				if (evalueOld <= evalue) {
					System.out.println(content + " " + evalueOld);
					continue;
				}
			}
			mapRiceId2AraId.put(riceId, new String[]{araId, evalue+""});
		}
		for (String riceId : mapRiceId2AraId.keySet()) {
			String[] araId2Evalue = mapRiceId2AraId.get(riceId);
			txtWrite.writefileln(new String[]{araId2Evalue[0], riceId, araId2Evalue[1]});
		}
		txtWrite.close();
	}
	
	/**根据上下调倍数来进行筛选，主要还是因为差异基因太多了 */
	public static void filterDifGeneFolder(String folder, int colLogFc, int cutoff) {
		List<String> lsDifGeneFiles = FileOperate.getLsFoldFileName(folder);
		for (String fileName : lsDifGeneFiles) {
			filterDifGene(fileName, colLogFc, cutoff);
		}
	}
	
	public static void filterDifGene(String diffGene, int colLogFc, int cutoff) {
		TxtReadandWrite txtRead = new TxtReadandWrite(diffGene);
		TxtReadandWrite txtWrite = new TxtReadandWrite(FileOperate.changeFileSuffix(diffGene, ".filter", null), true);
		txtWrite.writefileln(txtRead.readFirstLine());
		for (String content : txtRead.readlines(2)) {
			String[] ss = content.split("\t");
			double folder = Double.parseDouble(ss[colLogFc-1]);
			if (Math.abs(folder) < Math.abs(cutoff)) {
				continue;
			}
			txtWrite.writefileln(content);
		}
		txtRead.close();
		txtWrite.close();
	}
	
	
	/**
	 * 给余婧画折线图用的
	 * 把基因的表达量按照时间分开然后堆叠。
	 * 譬如 gene1 有wt-Ld 5个时间点 csa-Ld 5个时间点
	 * wt-Sd 5个时间点 csa-Sd 5个时间点
	 * 那么就可以把这个合并起来，一行一个状态
	 * 得到如下表格
	 * 	gene1	time1 time2 time3 time4 time5
	 * wt-Ld
	 * wt-Sd
	 * csa-Ld
	 * csa-Sd
	 */
	public static void makeGeneTimePileup() {
		String geneTable1 = "c:/Users/zong0/mywork/csa/compareWtih3rd/yujing-genes-flower-rpkm-time.avg.txt";
		String geneTable2 = "c:/Users/zong0/mywork/csa/compareWtih3rd/yujing-genes-leaf-rpkm-time.avg.txt";

		String outTable = "c:/Users/zong0/mywork/csa/compareWtih3rd/yujing-genes-all-rpkm-time.pileup.txt";
		
		TxtReadandWrite txtRead1 = new TxtReadandWrite(geneTable1);
		TxtReadandWrite txtRead2 = new TxtReadandWrite(geneTable2);
		TxtReadandWrite txtWrite = new TxtReadandWrite(outTable, true);
		String[] title1 = txtRead1.readFirstLine().split("\t");
		String[] title2 = txtRead2.readFirstLine().split("\t");
		title2 = ArrayOperate.deletElement(title2, new int[]{0});
		Iterator<String> itRead2 = txtRead2.readlines(2).iterator();

		String[] title = mergeValues(title1, title2);
		
		for (String content1 : txtRead1.readlines(2)) {
			String content2 = itRead2.next();
			String[] ss1 = content1.split("\t");
			String[] ss2 = content2.split("\t");
			
			if (!StringOperate.isEqual(ss1[0], ss2[0])) {
				txtRead1.close();
				txtRead2.close();
				txtWrite.close();
				
				throw new RuntimeException("gene name is not same");
			}
			ss2 = ArrayOperate.deletElement(ss2, new int[]{0});
			
			String[] ss = mergeValues(ss1, ss2);
			
			String geneName = ss[0];
			txtWrite.writefileln(getTitle(geneName, title));
			for (String[] pileupInfo : getPileupValue(ss, title)) {
				txtWrite.writefileln(pileupInfo);
			}
			txtWrite.writefileln();
		}
		txtRead1.close();
		txtRead2.close();
		txtWrite.close();
	}
	
	private static String[] mergeValues(String[] value1, String[] value2) {
		List<String> lsResult = ArrayOperate.converArray2List(value1);
		lsResult.addAll(ArrayOperate.converArray2List(value2));
		String[] result = lsResult.toArray(new String[0]);
		return result;
	}
	
	/**
	 * 给定基因名和 F_csa_Ld_8	F_csa_Ld_12	F_csa_Ld_16	F_csa_Ld_20	F_csa_Ld_24	F_csa_Ld_4	F_csa_Sd_8	F_csa_Sd_12
	 * 返回 geneName 8 12 16 20 24 4
	 * @param geneName
	 * @param title
	 * @return
	 */
	private static String[] getTitle(String geneName, String[] title) {
		List<String> lsTitle = new ArrayList<>();
		lsTitle.add(geneName);
		for (int i = 1; i < title.length; i++) {
			String sub = title[i];
			String[] ss = sub.split("_");
			String subTime = ss[ss.length - 1];
			if (lsTitle.contains(ss[ss.length - 1])) {
				break;
			}
			lsTitle.add(subTime);
		}
		return lsTitle.toArray(new String[0]);
	}
	
	/**
	 * 给定
	 * LOC_Os08g33150	3.8	8.4	5.4	1.8	0.5	...
	 * 返回 
	 * 3.8	8.4 ...
	 * 5.4	1.8 ...
	 * 这种堆叠好的
	 */
	private static List<String[]> getPileupValue(String[] values, String[] title) {
		List<String[]> lsResult = new ArrayList<>();
		List<String> lsSub = new ArrayList<>();
		String lineName = "";
		for (int i = 1; i < title.length; i++) {
			//给定 title[i]= F_csa_Ld_12 获得 name=F_csa_Ld
			List<String> lsInfo = ArrayOperate.converArray2List(title[i].split("_"));
			String name = ArrayOperate.cmbString(lsInfo.subList(0, lsInfo.size() - 1), "_");
			
			if (!StringOperate.isEqual(lineName, name)) {
				if (!lsSub.isEmpty()) {
					lsResult.add(lsSub.toArray(new String[0]));
					lsSub.clear();
				}
				lineName = name;
				lsSub.add(name);
			}
			lsSub.add(values[i]);
		}
		lsResult.add(lsSub.toArray(new String[0]));
		return lsResult;
	}
	
	/**
	 * hierarchical cluster出来的结果没有切分成类似kmean一样的类。
	 * 我是手工做的，把一个文件拿到后，在treeview里面人工看，找到某个cluster中最底部的基因
	 * 然后在文本里面把这个基因与下一个cluster用空行隔开。
	 * 
	 * 现在我需要在文本中把cluster的id加上，从1开始计算。
	 * 
	 * 譬如原来是
	 * gene1
	 * gene2
	 * 
	 * gene3
	 * gene4
	 * gene5
	 * 
	 * gene6
	 * 
	 * 改为
	 * gene1	1
	 * gene2	1
	 * gene3	2
	 * gene4	2
	 * gene5	2
	 * gene6	3
	 * 
	 * 这种形式
	 */
	public static void addTagsOnClusterTxt() {
		String clusterFile = "c:/Users/zong0/mywork/csa/cluster/affy-zhanghui//cluster.txt";
		String outFile = FileOperate.changeFileSuffix(clusterFile, ".addcluster", null);
		TxtReadandWrite txtRead = new TxtReadandWrite(clusterFile);
		TxtReadandWrite txtWrite = new TxtReadandWrite(outFile, true);
		
		int i = 1;
		boolean readGene = false;
		for (String content : txtRead.readlines()) {
			if (!StringOperate.isRealNull(content)) {
				readGene = true;
			} else {
				if (readGene) {
					i++;
				}
				readGene = false;
				continue;
			}
			content = content.trim() + "\t" + i;
			txtWrite.writefileln(content);
		}
		txtRead.close();
		txtWrite.close();
	}
	
	/**
	 * 给定选中的表，把选中表中的基因与表达谱的值对应起来。
	 * 默认第一列是基因名。
	 * 譬如我挑出了200个基因，现在想知道这200个基因的表达值是多少
	 */
	public static void overlapToExpTable() {
		int geneIdCol = 0;//如果是cluster的信息，则geneName在第二列

	
		
//		String geneTable = "c:/Users/zong0/mywork/csa/cluster/flower-cluster-all/flower-all-diff-cluster.txt";
//		String geneExpTable = "c:/Users/zong0/mywork/csa/expression/All_RPKM-Flower.modify.reorder-time.avg.txt";
//		String outputFile = FileOperate.changeFileSuffix(geneTable, "-rpkm-time.avg", null);
		
//		String geneTable = "c:/Users/zong0/mywork/csa/compareWtih3rd/yujing-starch/blast-arabidopsis-starch-synthetise.filter.anno.gene.txt";
//
		String geneTable = "c:/Users/zong0/mywork/csa/difgene/affy-zhanghui-limma/fold-change-2/flower-all-affy.txt";
		String geneExpTable = "c:/Users/zong0/mywork/csa/expression/affy-zhanghui-logvalue-mean.avg.txt";
		String outputFile = FileOperate.changeFileSuffix(geneTable, "-All-limma-time.avg", null);
		
//		String geneExpTable = "c:/Users/zong0/mywork/csa/cluster/leaf-cluster-all/leaf-diffgene-all-cluster-rpkm-time.avg.txt";
//		String outputFile = FileOperate.changeFileSuffix(geneTable, "-leafcluster", null);
		boolean unknownGeneThrowError = false;
		
		TxtReadandWrite txtReadGeneTable = new TxtReadandWrite(geneTable);
		Set<String> setGeneName = new LinkedHashSet<>();
		for (String content : txtReadGeneTable.readlines()) {
			String geneName = content.split("\t")[geneIdCol];
			if (StringOperate.isRealNull(geneName)) {
				continue;
			}
			setGeneName.add(geneName);
		}
		txtReadGeneTable.close();
		
		Map<String, String> mapGene2Exp = new HashMap<>();
		TxtReadandWrite txtReadGeneExpTable = new TxtReadandWrite(geneExpTable);
		String title = txtReadGeneExpTable.readFirstLine();
		for (String content : txtReadGeneExpTable.readlines()) {
			String[] ss = content.split("\t");
			mapGene2Exp.put(ss[0], content);
		}
		txtReadGeneExpTable.close();
		
		
		TxtReadandWrite txtResult = new TxtReadandWrite(outputFile, true);
		txtResult.writefileln(title);
		for (String geneName : setGeneName) {
			if (!mapGene2Exp.containsKey(geneName)) {
				if (geneName.equalsIgnoreCase("AccID") || geneName.startsWith("#")) {
					continue;
				}
				if (unknownGeneThrowError) {
					txtResult.close();
					throw new RuntimeException("cannot file gene " + geneName + " in file " + geneExpTable);
				} else {
					txtResult.writefileln(geneName);
					continue;
				}
			}
			String content = mapGene2Exp.get(geneName);
			txtResult.writefileln(content);
		}
		txtResult.close();
		
		System.out.println("finish");
	}
	
	/**
	 * 把一个文件夹中的所有差异基因合并，并且去重复
	 * 就是把所有的差异基因放一起，方便后面做聚类等分析
	 */
	public static void mergeDifGenes(String folderIn, String result) {
//		String folder = "c:/Users/zong0/mywork/csa/difgene/EBSeq/leaf-sd-csavswt-EBSeq/";
//		String result = "c:/Users/zong0/mywork/csa/difgene/EBSeq/Leaf-Sd-DifGene-All.txt";
//		
//		String folder = "c:/Users/zong0/mywork/csa/difgene/EBSeq/leaf-ld-csavswt-EBSeq/";
//		String result = "c:/Users/zong0/mywork/csa/difgene/EBSeq/Leaf-Ld-DifGene-All.txt";
//		
//		String folder = "c:/Users/zong0/mywork/csa/tmp/";
//		String result = "c:/Users/zong0/mywork/csa/difgene/EBSeq/diffgene-merge/leaf/Leaf-wt-DifGene-All.txt";
//		
//		String folder = "c:/Users/zong0/mywork/csa/difgene/EBSeq/";
//		String result = "c:/Users/zong0/mywork/csa/difgene/EBSeq/Leaf-DifGene-All.txt";
		
		List<String> lsDifGeneFiles = FileOperate.getLsFoldFileName(folderIn, "*", "txt");
		Set<String> setGeneName = new LinkedHashSet<>();
		for (String fileName : lsDifGeneFiles) {
			TxtReadandWrite txtRead = new TxtReadandWrite(fileName);
			for (String content : txtRead.readlines()) {
				String geneName = content.split("\t")[0];
				if (StringOperate.isRealNull(geneName)) {
					continue;
				}
				setGeneName.add(geneName);
			}
			txtRead.close();
		}
		
		TxtReadandWrite txtWrite = new TxtReadandWrite(result, true);
		for (String geneName : setGeneName) {
			txtWrite.writefileln(geneName);
		}
		txtWrite.close();
		
		System.out.println("finish");
	}
	
	
	/**
	 * 给定一个样本的排序表，把样本按照排序表里面的顺序进行排序
	 * 目的是因为长光照和短光照下，同一时间可能有的是白天有的是晚上
	 */
	public static void reorderByIndexFile() {
		String indexFile = "c:/Users/zong0/mywork/csa/expression/csa组排序表-按照白天黑夜排序.txt";
		String inFile = "c:/Users/zong0/mywork/csa/difgene/EBSeq/Leaf-DifGene-All-rpkm.avg.txt";
		
		Map<String, Integer> mapSample2Index = new HashMap<>();
		TxtReadandWrite txtRead = new TxtReadandWrite(indexFile);
		int i = 0;
		for (String content : txtRead.readlines()) {
			if (StringOperate.isRealNull(content)) {
				continue;
			}
			mapSample2Index.put(content, i++);
		}
		txtRead.close();
		TxtReadandWrite txtReadInFile = new TxtReadandWrite(inFile);
		List<List<String>> lsCols = new ArrayList<>();
		//按列提取出来
		int m = 0;
		for (String content : txtReadInFile.readlines()) {
			String[] ss = content.split("\t");
			for (int j = 0; j < ss.length; j++) {
				String cell = ss[j];
				List<String> lsCol = null;
				if (m == 0) {
					lsCol = new ArrayList<>();
					lsCols.add(lsCol);
				} else {
					lsCol = lsCols.get(j);
				}
				lsCol.add(cell);
			}
			m++;
		}
		txtReadInFile.close();
		
		List<List<String>> lsResult = new ArrayList<>();
		Map<Integer, List<String>> mapIndex2LsCol = new TreeMap<>();
		for (List<String> list : lsCols) {
			if (mapSample2Index.containsKey(list.get(0))) {
				int index = mapSample2Index.get(list.get(0));
				mapIndex2LsCol.put(index, list);
			} else {
				lsResult.add(list);
			}
		}
		
		for (List<String> list : mapIndex2LsCol.values()) {
			lsResult.add(list);
		}
		
		TxtReadandWrite txtWrite = new TxtReadandWrite(FileOperate.changeFileSuffix(inFile, ".reorder", null), true);
		int k = 0;
		for (int j = 0; j < lsResult.get(0).size(); j++) {
			List<String> lsRow = new ArrayList<>();
			for (List<String> list : lsResult) {
				lsRow.add(list.get(k));
			}
			txtWrite.writefileln(lsRow);
			k++;
		}
		txtWrite.close();
		
		System.out.println("finish");
	}
	
	/**
	 * 根据样本名称对照表，把csa的样本替换为实际的样本名
	 * 因为当初送样的时候，样本名是S1-1这种，需要替换为实际有意义的名字
	 */
	public static void convertSampleName() {
		String convertTab = "c:/Users/zong0/mywork/csa/csa样本信息整理表.txt";
		String fileIn = "c:/Users/zong0/mywork/csa/叶片/All_Counts-Leaf.txt";

		TxtReadandWrite txtread = new TxtReadandWrite(convertTab);
		Map<String, String> mapId2Detail = new HashMap<>();
		for (String content : txtread.readlines()) {
			String[] ss = content.split("\t");
			mapId2Detail.put(ss[0], ss[1]);
		}
		txtread.close();
		
		String fileOut = FileOperate.changeFileSuffix(fileIn, ".modify", null);
		TxtReadandWrite txtReadReal = new TxtReadandWrite(fileIn);
		TxtReadandWrite txtWrite = new TxtReadandWrite(fileOut, true);
		int i = 0;
		for (String content : txtReadReal.readlines()) {
			if (i++ == 0) {
				String[] ss = content.split("\t");
				for (int j = 0; j < ss.length; j++) {
					String ssunit = ss[j];
					if (ssunit.startsWith("S")) ssunit = ssunit.substring(1);
					ssunit = ssunit.replace("-", "_");
					if (mapId2Detail.containsKey(ssunit)) {
						ss[j] = mapId2Detail.get(ssunit);
					}
				}
				content = ArrayOperate.cmbString(ss, "\t");
			}
			txtWrite.writefileln(content);
		}
		
		txtread.close();
		txtWrite.close();
		
		System.out.println("finish");
	}
	
	/** 整理出来的表格中，基因不是平均数，现在需要把基因弄成平均数 */
	public static void getAvgExpGroup() {
		String inFile = "c:/Users/zong0/mywork/csa/expression/affy-zhanghui-logvalue-mean.txt";
		String outFile = FileOperate.changeFileSuffix(inFile, ".avg", null);
		String title = TxtReadandWrite.readFirstLine(inFile);
		HashMultimap<String, String> mapGroup2Samples = HashMultimap.create();
		String[] ss = title.split("\t");
		for (int i = 1; i < ss.length; i++) {
			String sample = ss[i];
			String group = sample.substring(0, sample.length() - 2);
			mapGroup2Samples.put(group, sample);
		}
		GeneExpTable geneExpTable = new GeneExpTable();
		geneExpTable.read(inFile, EnumAddAnnoType.notAdd);
		geneExpTable = geneExpTable.getConditionAvg(mapGroup2Samples);
		geneExpTable.writeFile(true, outFile, EnumExpression.RawValue);
	}
	
}
