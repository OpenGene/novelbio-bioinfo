package com.novelbio.analysis.comparegenomics;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.novelbio.analysis.IntCmdSoft;
import com.novelbio.analysis.annotation.blast.BlastNBC;
import com.novelbio.analysis.annotation.blast.BlastType;
import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.GffChrSeq;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene.GeneStructure;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.geneanno.BlastInfo;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.model.species.Species;

/**
 *  指定一条序列，将该序列比对到指定物种的dna或rna上，并获得序列 
 * @author zong0jie
 * 
 */
public class BlastGetSeq implements IntCmdSoft {
	private static final Logger logger = Logger.getLogger(BlastGetSeq.class);
	
	public static void main(String[] args) {
		BlastGetSeq blastGetSeq = new BlastGetSeq();
		blastGetSeq.setBlastType(BlastType.blastp);
		blastGetSeq.setResultFile("/media/winE/NBC/Project/liufei/");
		List<Species> lsSpecies = new ArrayList<>();
		lsSpecies.add(new Species(9606));
		lsSpecies.add(new Species(10090));
		lsSpecies.add(new Species(7955));
		lsSpecies.add(new Species(7227));
		lsSpecies.add(new Species(6239));
		lsSpecies.add(new Species(3702));
		lsSpecies.add(new Species(39947));
		blastGetSeq.setLsSpeciesBlastTo(lsSpecies);
		SeqFasta seqFasta = new SeqFasta();
		seqFasta.setName("GRLH1");
		seqFasta.setSeq("MSQEHENKRAVLVLPNDPAYNQRRPYTSEDEAWKSFLENPLTAATKAMMSINGDEDSAAALGLLYDYYKVPRDKRTISQQKTDVLGSDVDPNKRNMLTPLQETSMQLGDNRIQVLKGVPLNIVLPGNQHVQDKRGLFPSPDTTVTVSIAPVASNSVKTEGPSHGFSVTVPNPHCAEPDSHTVVFDRQLPHNQFSPNTQPRTPDSTFPENPDVFSFPGDLQLRMGPITQDDYGTFDTVSGNNFEYILEASKSLRQKSGDGTMTYLNKGQFYPITLRETDNGKLLQGPICKVRSVVMVVFGEEKSRDDQLKHWKYWHSRQHTAKQRCIDIADYKESFNTISNIEEISYNAISFTWDISEEAKIFISVNCLSTDFSSQKGVKGLPLNIQIDTYSYNNRSNKPIHRAYCQIKVFCDKGAERKIRDEERKQSRRKVGADVKVPLLHKRTDMTVFRTLTDFETQPVLFIPDIHFSTFQRHAFTAEDSEEGSAMKRLPYTEEEFGSPPNKLARMDEPKRVLLYVRRETEEVFDALMLKTPTLKGLVEAISEKYEVSLEKIGKVYKKCKKGILVNMDDNIIKHYSNEDTFQIQMEEMGGMIKLTLTEIE");
		blastGetSeq.setQueryFasta(seqFasta);
		blastGetSeq.blastAndGetSeq();
	}
	
	
	/** 输入的序列 */
	SeqFasta seqFasta;
	
	/** 需要比对到的物种 */
	List<Species> lsSpeciesBlastTo;
	/** 是否提取所有转录本做blast，默认只提取最长转录本 */
	boolean isAllIso = true;
	
	double evalue = 1e-5;
	/** 最短长度比例，比对结果小于该长度比例就不认为是同源的基因 */
	double lengthMinProp = 0.2;
	/** 最短长度Bp，比对结果小于该长度(Bp)就不认为是同源的基因 */
	int lengthMinBp = 40;
	boolean bothCondition = false;
	
	BlastType blastType = BlastType.blastp;
	String resultFile;
	/** 输出文件是protein还是nr */
	boolean isGetProtein = true;
	
	List<String> lsCmd = new ArrayList<>();
	
	public void setQueryFasta(SeqFasta seqFasta) {
		this.seqFasta = seqFasta;
	}
	public void setLsSpeciesBlastTo(List<Species> lsSpeciesBlastTo) {
		this.lsSpeciesBlastTo = lsSpeciesBlastTo;
	}
	/** 输出文件 */
	public void setResultFile(String resultFile) {
		this.resultFile = resultFile;
	}
	/** 默认 1e-5 */
	public void setEvalue(double evalue) {
		this.evalue = evalue;
	}
	/** 输出结果是protein还是dna，默认protein */
	public void setOutputType(boolean isGetProtein) {
		this.isGetProtein = isGetProtein;
	}
	/** 最短长度Bp，比对结果小于该长度(Bp)就不认为是同源的基因
	 * 
	 * @param lengthMinBp 最短长度Bp，比对结果小于该长度(Bp)就不认为是同源的基因 默认50
	 * @param lengthMinProp 最短长度比例，比对结果小于该长度比例就不认为是同源的基因 默认0.3
	 * @param both 默认false <br> 
	 * true：两个条件都满足<br> 
	 * false：两个条件满足一个即可
	 */
	public void setLengthMin(int lengthMinBp, double lengthMinProp, boolean bothCondition) {
		this.lengthMinBp = lengthMinBp;
		this.lengthMinProp = lengthMinProp;
		this.bothCondition = bothCondition;
	}
	/** 务必在序列输入后设定 */
	public void setBlastType(BlastType blastType) {
		this.blastType = blastType;
	}
	public void blastAndGetSeq() {
		//物种与找到的基因数量
		String outNum = FileOperate.getPathName(resultFile) + "blastSpeciesNum";
		TxtReadandWrite txtWriteNum = new TxtReadandWrite(outNum, true);
		//具体的序列
		String outSeq = resultFile;
		if (resultFile.endsWith("/") || resultFile.endsWith("\\")) {
			outSeq = outSeq + "blastSpeciesSeq.fa";
		}
		TxtReadandWrite txtWriteSeq = new TxtReadandWrite(outSeq, true);
		txtWriteNum.writefileln("SpeciesName\tSeqNum");
		for (Species species : lsSpeciesBlastTo) {
			if (!checkSpecies(species)) {
				logger.info("no species: " + species.getNameLatin());
				continue;
			}
			String seqBlastTo = getBlastToSeq(species);
			BlastNBC blastNBC = new BlastNBC();
			blastNBC.setBlastType(blastType);
			blastNBC.setQueryFasta(seqFasta);
			blastNBC.setSubjectSeq(seqBlastTo);
			if (seqFasta.Length() < 50) {
				blastNBC.setShortQuerySeq(true);
			}
			blastNBC.setEvalue(evalue);
			blastNBC.setResultSeqNum(1000);
			blastNBC.setResultType(BlastNBC.ResultType_Simple);
			String resultBlast = FileOperate.getPathName(resultFile) + seqFasta.getSeqName().trim().split(" ")[0] + "_blastto_"  + species.getNameLatin().replace(" ", "_");
			blastNBC.setResultFile(resultBlast);
			blastNBC.blast();
			lsCmd.addAll(blastNBC.getCmdExeStr());
			List<SeqFasta> lsSeqFastas = getSeq(blastNBC, species);
			txtWriteNum.writefileln(species.getNameLatin() + "\t" + lsSeqFastas.size());
			writeToFile(species, lsSeqFastas, txtWriteSeq);
			logger.info("finish get species: " + species.getNameLatin());
		}
		
		txtWriteNum.close();
		txtWriteSeq.close();
	}
	
	private boolean checkSpecies(Species species) {
		if (species == null || species.getTaxID() == 0) {
			return false;
		}
		String fileName = getBlastToSeq(species);
		if (!FileOperate.isFileExistAndBigThanSize(fileName, 0)) {
			return false;
		}
		return true;
	}
	
	private String getBlastToSeq(Species species) {
		if (blastType == BlastType.blastn || blastType == BlastType.tblastn || blastType == BlastType.tblastx) {
			return species.getRefseqFile(isAllIso);
		} else {
			return species.getRefseqProFile(isAllIso);
		}
	}
	
	/** 根据blast以及相应的参数，返回提取到的序列 */
	private List<SeqFasta> getSeq(BlastNBC blastNBC, Species species) {
		List<BlastInfo> lsBlastInfos = BlastInfo.readBlastFile(blastNBC.getResultFile());
		if (lsBlastInfos.size() == 0) {
			return new ArrayList<>();
		}
		int seqQueryLen = seqFasta.Length();
		lsBlastInfos = BlastInfo.removeDuplicate(lsBlastInfos);
		List<String> lsGeneName = new ArrayList<>();
		for (BlastInfo blastInfo : lsBlastInfos) {
			//根据阈值做筛选，将名字保存到lsGeneName
			if (blastInfo.getEvalue() <= evalue && 
					(   ( bothCondition && blastInfo.getAlignLen() >= lengthMinBp && blastInfo.getAlignLen()/seqQueryLen >= lengthMinProp
						)
					|| 
						( !bothCondition && (blastInfo.getAlignLen() >= lengthMinBp || blastInfo.getAlignLen()/seqQueryLen >= lengthMinProp)
						)
					)
				) {
				lsGeneName.add(blastInfo.getSubjectID());
			}
		}
		if (lsGeneName.size() == 0) {
			return new ArrayList<>();
		}
		
		GffChrAbs gffChrAbs = new GffChrAbs(species, true);
		GffChrSeq gffChrSeq = new GffChrSeq(gffChrAbs);
		if (isGetProtein) {
			gffChrSeq.setGeneStructure(GeneStructure.CDS);
		} else {
			gffChrSeq.setGeneStructure(GeneStructure.ALLLENGTH);
		}
		gffChrSeq.setGetReplicateIso(false);
		gffChrSeq.setGetAllIso(false);
		gffChrSeq.setGetIntron(false);
		gffChrSeq.setGetSeqIsoRemoveSamGene(lsGeneName);
		gffChrSeq.setIsSaveToFile(false);
		gffChrSeq.run();
		List<SeqFasta> lsSeqfasta = gffChrSeq.getLsResult();
		return lsSeqfasta;
	}
		
	private void writeToFile(Species species, List<SeqFasta> lsSeqFastas, TxtReadandWrite txtWrite) {
		for (SeqFasta seqFasta : lsSeqFastas) {
			GeneID geneID = new GeneID(seqFasta.getSeqName(), species.getTaxID());
			String seqfastaNameNew = seqFasta.getSeqName();
			if (!geneID.getSymbol().toLowerCase().equals(seqfastaNameNew.toLowerCase())) {
				seqfastaNameNew = geneID.getSymbol() + "_" + seqfastaNameNew;
			}
			seqFasta.setName(species.getCommonName() + "_" + seqfastaNameNew);
			String result = null;
			if (isGetProtein) {
				result = seqFasta.toStringAAfasta();
			} else {
				result = seqFasta.toStringNRfasta();
			}
			txtWrite.writefileln(result);
		}
	}
	
	@Override
	public List<String> getCmdExeStr() {
		return lsCmd;
	}
	
}
