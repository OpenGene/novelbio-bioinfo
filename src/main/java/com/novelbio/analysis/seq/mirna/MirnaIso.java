package com.novelbio.analysis.seq.mirna;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.novelbio.analysis.ExceptionNBCsoft;
import com.novelbio.analysis.seq.AlignRecord;
import com.novelbio.analysis.seq.genome.gffOperate.MiRNAList;
import com.novelbio.analysis.seq.genome.gffOperate.MirPre;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.analysis.seq.rnaseq.RPKMcomput.EnumExpression;
import com.novelbio.analysis.seq.sam.AlignSeqReading;
import com.novelbio.analysis.seq.sam.AlignmentRecorder;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.analysis.seq.sam.SamRecord;
import com.novelbio.base.dataOperate.TxtReadandWrite;
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
		mirnaIso.setSampleName("DL4R1");
		alignSeqReading.run();
		
		alignSeqReading = new AlignSeqReading(samfile2);
		alignSeqReading.addAlignmentRecorder(mirnaIso);
		mirnaIso.setSampleName("DL5R1");
		alignSeqReading.run();
		
		mirnaIso.writeToFile("/media/winE/OutMrd1.mrd/testIsoMir2.txt");
	}
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
			mapMirName2IsoUnit.put(mirName, mirnaIsoUnit);
		}
	}
	
	/** 设定样本名 */
	public void setSampleName(String sampleName) {
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
		mapMirName2IsoUnit.get(alignRecord.getRefID()).addMirSamRecord(listMiRNAdat, samRecord);
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
	
	
}
