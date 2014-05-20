package com.novelbio.analysis.seq.mirna;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.novelbio.analysis.ExceptionNBCsoft;
import com.novelbio.analysis.seq.AlignRecord;
import com.novelbio.analysis.seq.GeneExpTable.EnumAddAnnoType;
import com.novelbio.analysis.seq.genome.gffOperate.MiRNAList;
import com.novelbio.analysis.seq.genome.gffOperate.MirPre;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.analysis.seq.rnaseq.RPKMcomput.EnumExpression;
import com.novelbio.analysis.seq.sam.AlignSeqReading;
import com.novelbio.analysis.seq.sam.AlignmentRecorder;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.analysis.seq.sam.SamRecord;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.species.Species;
import com.novelbio.generalConf.PathDetailNBC;


/**
 * iso miRNA
 * @author zong0jie
 *
 */
public class MirnaIso implements AlignmentRecorder {
	public static void main(String[] args) {
		String path = "/hdfs:/nbCloud/public/AllProject/project_52cd1450e4b040581d2d572a/task_52cd1464e4b040581d2d572b/MiRNASeqAnalysis_result/TmpMapping/";
		ListMiRNAdat miRNAList = new ListMiRNAdat();
		miRNAList.setSpecies(new Species(9940));
		miRNAList.ReadGffarray(PathDetailNBC.getMiRNADat());
		MirnaIso mirnaIso = new MirnaIso();
		
		SamFile samfile1 = new SamFile(path + "DL4R1_miRNA.bam");
		SamFile samfile2 = new SamFile(path + "DL5R1_miRNA.bam");

//		SamFile samfile3 = new SamFile("/media/winE/OutMrd1.mrd/aaa.sam");

//		SamFile samFileWrite = new SamFile("/media/winE/OutMrd1.mrd/DL4R1_miRNAtest.bam", samfile1.getHeader());
//		int i = 0;
//		for (SamRecord samRecord : samfile1.readLines()) {
//			if (i++ > 100) {
//				break;
//			}
//			samFileWrite.writeSamRecord(samRecord);
//		}
//		samfile1.close();
//		samFileWrite.close();
		
		mirnaIso.setMapMirnaName(miRNAList, samfile1.getMapChrID2Length().keySet());

		AlignSeqReading alignSeqReading = new AlignSeqReading(samfile1);
		alignSeqReading.setLenMin(17);
		alignSeqReading.setLenMax(32);
		alignSeqReading.addAlignmentRecorder(mirnaIso);
		mirnaIso.setCurrentCondition("DL4R1");
		alignSeqReading.run();
		
		alignSeqReading = new AlignSeqReading(samfile2);
		alignSeqReading.addAlignmentRecorder(mirnaIso);
		mirnaIso.setCurrentCondition("DL5R1");
		alignSeqReading.run();
		
		mirnaIso.writeToFile("/media/winE/OutMrd1.mrd/testIsoMir2.txt");
	}
	
	/** key为小写 */
	Map<String, MirIsoUnit> mapMirName2IsoUnit = new LinkedHashMap<>();
	MiRNAList listMiRNAdat;
	
	/** 第一次的时候设定，设定全体miRNA的名字
	 * 同时还要配套相关的{@link MiRNAList}
	 * @param listMiRNAdat
	 * @param colMirName
	 */
	public void setMapMirnaName(MiRNAList listMiRNAdat, Collection<String> colMirName) {
		this.listMiRNAdat = listMiRNAdat;
		for (String mirName : colMirName) {
			MirPre mirPre = listMiRNAdat.getListDetail(mirName);
			if (mirPre == null) {
				//TODO 直接抛出异常是否合适？
				throw new ExceptionNBCsoft(mirName + " cannot find this mirName in mirDat");
			}
			MirIsoUnit mirnaIsoUnit = new MirIsoUnit(mirPre);
			mapMirName2IsoUnit.put(mirName.toLowerCase(), mirnaIsoUnit);
		}
	}
	/** 第一次的时候设定，设定listMiRNAdat，miRNA的名字来源于输入的{@link MiRNAList}
	 * @param listMiRNAdat
	 */
	public void setMapMirnaName(MiRNAList listMiRNAdat) {
		this.listMiRNAdat = listMiRNAdat;
		for (MirPre mirPre : listMiRNAdat.getMapChrID2LsGff().values()) {
			MirIsoUnit mirnaIsoUnit = new MirIsoUnit(mirPre);
			mapMirName2IsoUnit.put(mirPre.getName().toLowerCase(), mirnaIsoUnit);
		}
	}
	/** 设定样本名 */
	public void setCurrentCondition(String sampleName) {
		for (MirIsoUnit mirnaIsoUnit : mapMirName2IsoUnit.values()) {
			mirnaIsoUnit.setCurrentCondition(sampleName);
		}
	}
	
	@Override
	public Align getReadingRegion() {
		return null;
	}

	@Override
	public void addAlignRecord(AlignRecord alignRecord) {
		SamRecord samRecord = (SamRecord)alignRecord;
		if (!alignRecord.isMapped()) {
			return;
		}
		mapMirName2IsoUnit.get(alignRecord.getRefID().toLowerCase()).addMirSamRecord(listMiRNAdat, samRecord);
	}

	@Override
	public void summary() { }
	
	public void writeToFile(String outFileName) {
		TxtReadandWrite txtWrite = new TxtReadandWrite(outFileName, true);
		for (String mirPreName : mapMirName2IsoUnit.keySet()) {
			MirIsoUnit mirIsoUnit = mapMirName2IsoUnit.get(mirPreName);
			List<String[]> lsInfo = mirIsoUnit.getLsAllCountsNum(EnumExpression.Counts);
			txtWrite.writefileln();
			txtWrite.writefileln("#" + mirPreName);
			for (String[] strings : lsInfo) {
				txtWrite.writefileln(strings);
			}
		}
		txtWrite.close();
	}
	
	/**
	 * 如果文件夹不存在，会新建文件夹
	 * @param writeAllCondition
	 * @param fileName
	 * @param expTable
	 * @param enumExpression
	 */
	public void writeFile(boolean writeAllCondition, String fileName) {
		TxtReadandWrite txtWrite = new TxtReadandWrite(fileName, true);
		for (String mirPreName : mapMirName2IsoUnit.keySet()) {
			MirIsoUnit mirIsoUnit = mapMirName2IsoUnit.get(mirPreName);
			List<String[]> lsInfo = null;
			if (writeAllCondition) {
				lsInfo = mirIsoUnit.getLsAllCountsNum(EnumExpression.Counts);
			} else {
				lsInfo = mirIsoUnit.getLsCountsNum(EnumExpression.Counts);
			}
			txtWrite.writefileln();
			txtWrite.writefileln("#" + mirPreName);
			for (String[] strings : lsInfo) {
				txtWrite.writefileln(strings);
			}
		}
		txtWrite.close();
	}
	
	public void read(String existFile) {
		TxtReadandWrite txtRead = new TxtReadandWrite(existFile);
		List<String> lsInfo = new ArrayList<>();
		MirIsoUnit mirIsoUnit = null;
		for (String content : txtRead.readlines()) {
			if (content.startsWith("#")) {
				if (mirIsoUnit != null) {
					mirIsoUnit.read(lsInfo, EnumAddAnnoType.addNew);
				}
				lsInfo = new ArrayList<>();
				String mirPreName = content.split("#")[1];
				mirIsoUnit = mapMirName2IsoUnit.get(mirPreName);
				continue;
			}
			lsInfo.add(content);
		}
		mirIsoUnit.read(lsInfo, EnumAddAnnoType.addNew);
		txtRead.close();
	}
	
	
}
