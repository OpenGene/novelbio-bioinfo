package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.novelbio.analysis.seq.AlignRecord;
import com.novelbio.analysis.seq.GeneExpTable;
import com.novelbio.analysis.seq.GeneExpTable.EnumAddAnnoType;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.analysis.seq.rnaseq.RPKMcomput.EnumExpression;
import com.novelbio.analysis.seq.sam.AlignmentRecorder;
import com.novelbio.analysis.seq.sam.ExceptionSamError;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.analysis.seq.sam.SamRecord;
import com.novelbio.analysis.seq.sam.SamToBamSort;
import com.novelbio.base.SepSign;
import com.novelbio.base.StringOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.generalConf.TitleFormatNBC;

/**
 * 输入文件必须不能排序
 * @author zong0jie
 */
public class RefSeqCounts implements AlignmentRecorder {
	private static final Logger logger = LoggerFactory.getLogger(RefSeqCounts.class);
	public static void main(String[] args) {
		String parentPath = "/hdfs:/nbCloud/public/AllProject/project_539a72a3e4b0e2f2f738c15b/task_5427ddb0e4b0ece9e25a7396/DNASeqMap_result/";
		String resultPath = parentPath + "expAllId/";
		FileOperate.createFolders(resultPath);
//		FileOperate.createFolders(expPath);
		RefSeqCounts refSeqCounts = new RefSeqCounts();
		
		refSeqCounts.setPairend(false);
		List<AlignmentRecorder> lsRecorders = new ArrayList<>();
		lsRecorders.add(refSeqCounts);
		
		List<String> lsFileRaw = FileOperate.getLsFoldFileName(parentPath, "*", "bam");
		List<String> lsFile = new ArrayList<String>();
		for (String string : lsFileRaw) {
			if (FileOperate.getFileName(string).contains("sort")) {
				continue;
			}
			lsFile.add(string);
		}
		
		SamFile samFile = new SamFile(lsFile.get(0));
		refSeqCounts.readGene2IsoFile("/media/hdfs/nbCloud/staff/bianlianle/Project/RNA_Denovo/Delphacidae_ChenRongZhi/4.Gene_Expression/All-Unigene.gene2Tran.list");
		refSeqCounts.setMapIsoId2Len(samFile.getMapChrID2Length());
		for (String bamFile : lsFile) {
			String prefix = FileOperate.getFileNameSep(bamFile)[0];
			String out = resultPath + prefix + ".xls";
			if (FileOperate.isFileExistAndBigThanSize(out, 0)) {
				refSeqCounts.geneExpTable.read(out, EnumAddAnnoType.notAdd);
				continue;
			}
			refSeqCounts.setCondition(prefix);
			SamToBamSort samToBamSort = new SamToBamSort(null, new SamFile(bamFile));
			samToBamSort.setNeedSort(false);
			samToBamSort.setAddMultiHitFlag(true);
			samToBamSort.setLsAlignmentRecorders(lsRecorders);
			samToBamSort.setWriteToBam(false);
			samToBamSort.convert();
			refSeqCounts.geneExpTable.writeFile(false, resultPath + prefix, EnumExpression.Counts);
		}
		refSeqCounts.geneExpTable.writeFile(true, resultPath + "All" + ".txt", EnumExpression.Counts);
		refSeqCounts.geneExpTable.writeFile(true, resultPath + "All.RPKM" + ".txt", EnumExpression.RPKM);
		refSeqCounts.geneExpTable.writeFile(true, resultPath + "All.TPM" + ".txt", EnumExpression.TPM);
	}
	
	public static void main2(String[] args) {
		String expPath = "/media/hdfs/nbCloud/staff/bianlianle/Project/RNA_Denovo/Paralichthys_olivaceus_LiChao/6.Gene_Expression/zongjieExp/";
		String parentPath = "/media/hdfs/nbCloud/staff/bianlianle/Project/RNA_Denovo/Paralichthys_olivaceus_LiChao/";
		RefSeqCounts refSeqCounts = new RefSeqCounts();
		refSeqCounts.readGene2IsoFile(parentPath + "7.Gene_Expression/RefSeqIndex/All_Trinity.fa.cluster.result.fa.gene_trans_map");
		
		refSeqCounts.setPairend(true);
		SamFile samFile = new SamFile(parentPath + "10.SNP/R2.bam");
		refSeqCounts.setMapIsoId2Len(samFile.getMapChrID2Length());
		refSeqCounts.geneExpTable.read(parentPath + "7.Gene_Expression/zongjieExp/All.txt", EnumAddAnnoType.notAdd);

		refSeqCounts.geneExpTable.writeFile(true, expPath + "All.new" + ".txt", EnumExpression.Counts);
		refSeqCounts.geneExpTable.writeFile(true, expPath + "All.RPKM" + ".txt", EnumExpression.RPKM);
	}
	GeneExpTable geneExpTable = new GeneExpTable(TitleFormatNBC.GeneID);

	/** iso到基因的对照表，key为小写 */
	Map<String, String> mapIso2Gene = new HashMap<>();
	List<SamRecord[]> lsSamRecordPair = new ArrayList<>();
	/** key为： samRecord.getName() + SepSign.SEP_ID + samRecord.getMateAlignmentStart() */
	Map<String, SamRecord> mapKey2SamRecord = new HashMap<>();
	SamRecord lastRecord;
	boolean isPairend;
	public void setPairend(boolean isPairend) {
		this.isPairend = isPairend;
	}
	/**
	 * 读取gene2Iso的对照表
	 * 第一列 geneName<br>
	 * 第二列 isoName<br>
	 * @param gene2IsoFile
	 */
	public void readGene2IsoFile(String gene2IsoFile) {
		TxtReadandWrite txtRead = new TxtReadandWrite(gene2IsoFile);
		int i = 0;
		for (String content : txtRead.readlines()) {
			if (i++ == 1&& (content.startsWith("iso") || content.startsWith("#")))	{
				continue;
			}
			if (content.startsWith("#") || StringOperate.isRealNull(content)) {
				continue;
			}
			String[] gene2iso = content.split("\t");
			mapIso2Gene.put(gene2iso[1].toLowerCase(), gene2iso[0]);
		}
		txtRead.close();
		geneExpTable.addLsGeneName(mapIso2Gene.values());
	}
	/**
	 * 根据sam文件的head，获取基因名，这时候认为一个基因就是一个iso
	 * @param samFile
	 */
	public void readGene2IsoFile(SamFile samFile) {
		for (String chrId : samFile.getMapChrID2Length().keySet()) {
			mapIso2Gene.put(chrId.toLowerCase(), chrId);
		}
		geneExpTable.addLsGeneName(mapIso2Gene.values());
	}
	/** 设定转录本名和长度，直接从bam文件获得就行 {@link SamFile#getMapChrID2Length()}<br>
	 * 注意务必先调用{@link #readGene2IsoFile(String)}读取对照表
	 * @param mapIsoId2Len
	 */
	public void setMapIsoId2Len(Map<String, ? extends Number> mapIsoId2Len) {
		Map<String, Integer> mapGeneName2Len = new HashMap<>();
		for (String isoName : mapIsoId2Len.keySet()) {
			int length = mapIsoId2Len.get(isoName).intValue();

			String geneName = mapIso2Gene.get(isoName.toLowerCase());
			if (!mapGeneName2Len.containsKey(geneName) || mapGeneName2Len.get(geneName) < length) {
				mapGeneName2Len.put(geneName, length);
			}
		}
		geneExpTable.setMapGene2Len(mapGeneName2Len);
	}
	
	/** 设定转录本名和长度，直接从bam文件获得就行 {@link SamFile#getMapChrID2Length()}<br>
	 * 注意务必先调用{@link #readGene2IsoFile(String)}读取对照表
	 * @param mapIsoId2Len
	 */
	public void addMapIsoId2Len(Map<String, ? extends Number> mapIsoId2Len) {
		Map<String, Integer> mapGeneName2Len = new HashMap<>();
		for (String isoName : mapIsoId2Len.keySet()) {
			int length = mapIsoId2Len.get(isoName).intValue();

			String geneName = mapIso2Gene.get(isoName.toLowerCase());
			if (!mapGeneName2Len.containsKey(geneName) || mapGeneName2Len.get(geneName) < length) {
				mapGeneName2Len.put(geneName, length);
			}
		}
		geneExpTable.addMapGene2Len(mapGeneName2Len);
	}
	
	public void setCondition(String currentCondition) {
		geneExpTable.setCurrentCondition(currentCondition);
	}
	
	@Override
	public Align getReadingRegion() {
		return null;
	}
	
	/** 读取以前计算好的counts文件
	 * 会从title中获取具体的sample名
	 * @param outTmpFile 必须是counts文件
	 */
	public void readExistExpFile(String outTmpFile) {
		this.geneExpTable.read(outTmpFile, EnumAddAnnoType.notAdd);
	}
	
	public void writeFile(boolean writeAllCondition, String fileName, EnumExpression enumExpression) {
		this.geneExpTable.writeFile(writeAllCondition, fileName, enumExpression);
	}
	
	@Override
	public void addAlignRecord(AlignRecord alignRecord) {
		SamRecord samRecord = (SamRecord)alignRecord;
		if (!isPairend) {
			addSErecord(samRecord);
		} else {
			addPErecord(samRecord);
		}
	}
	
	private void addSErecord(SamRecord samRecord) {
		if (lastRecord == null || !samRecord.getName().equals(lastRecord.getName())) {
			addSamRecord(lsSamRecordPair);
			lsSamRecordPair.clear();
		}
		lsSamRecordPair.add(new SamRecord[]{samRecord});
		lastRecord = samRecord;
	}
	
	private void addPErecord(SamRecord samRecord) {
		if (samRecord.isFirstRead()) {
			if (lastRecord == null || !samRecord.getName().equals(lastRecord.getName())) {
				addSamRecord(lsSamRecordPair);

				mapKey2SamRecord.clear();
				lsSamRecordPair.clear();
			}
			mapKey2SamRecord.put(getKey(samRecord), samRecord);
			lastRecord = samRecord;
		} else {
			SamRecord samRecord1 = mapKey2SamRecord.get(getKey(samRecord));
			SamRecord[] samRecords = new SamRecord[]{samRecord1, samRecord};
			lsSamRecordPair.add(samRecords);
		}
	}
	
	/** 计算表达 */
	private void addSamRecord(List<SamRecord[]> lsSamRecordPair) {
		Set<String> setGeneName = new HashSet<>();
		for (SamRecord[] samRecords : lsSamRecordPair) {
			if (!isPairend) {
				if (!samRecords[0].isMapped()) continue;
				setGeneName.add(getGeneName(samRecords[0]));
			} else {
				SamRecord record1 = samRecords[0];
				SamRecord record2 = samRecords[1];
				if (record1 == null) {
					if (record2 != null && record2.isMapped()) {
						setGeneName.add(getGeneName(record2));
					}
				} else if (record2 == null) {
					if (record1.isMapped()) {
						setGeneName.add(getGeneName(record1));
					}
				} else if (!record1.isMapped() && !record2.isMapped()) {
					continue;
				} else if (record1.isMapped() ^ record2.isMapped()) {
					String geneName = record1.isMapped()? getGeneName(record1) : getGeneName(record2);
					setGeneName.add(geneName);
				} else if (record1.getRefID().equals(record2.getRefID())) {
					setGeneName.add(getGeneName(record1));
				} else {
					setGeneName.add(getGeneName(record1));
					setGeneName.add(getGeneName(record2));
				}
			}
		}
		addGeneExp(setGeneName);
	}
	
	private String getGeneName(SamRecord samRecord) {
		if (!samRecord.isMapped()) {
			return null;
		}
		String geneName = mapIso2Gene.get(samRecord.getRefID().toLowerCase());
		if (geneName == null) {
			
			throw new ExceptionSamError("cannot find iso in gene: " + samRecord.getRefID());
		}
		return geneName;
	}
	
	private void addGeneExp(Set<String> setGeneName) {
		for (String geneName : setGeneName) {
			if (StringOperate.isRealNull(geneName)) {
				continue;
			}
			geneExpTable.addGeneExp(geneName, (double)1/setGeneName.size());
		}
	}
	
	private static String getKey(SamRecord samRecord) {
		if (samRecord.isFirstRead()) {
			return samRecord.getName() + SepSign.SEP_ID + samRecord.getStartAbs();
		} else {
			return samRecord.getName() + SepSign.SEP_ID + samRecord.getMateAlignmentStart();
		}
	}
	
	@Override
	public void summary() {
		addSamRecord(lsSamRecordPair);
		lsSamRecordPair.clear();
		mapKey2SamRecord.clear();
	}

}
