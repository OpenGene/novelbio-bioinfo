package com.novelbio.database.updatedb.database;

import java.util.ArrayList;

import com.novelbio.analysis.annotation.blast.BlastNBC;
import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqFastaHash;
import com.novelbio.analysis.seq.genome.GffChrSeq;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.PatternOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.generalConf.NovelBioConst;
/**
 * 常规affy的注释文件，仅导入affy探针，其他注释通通不导入<br>
 * <b>如果导入不进去，考虑将ss[8]放入筛选条件</b><br>
 * 方法：取消那一行的注释
 * <b>导入之前的准备工作</b><br>
 * 1. 将格式调整为tab隔开，去除所有引号<br>
 * 2. 将所有无关ID，以及control探针全部去除
 * 3. 设定从第二列开始导入
 * @author zong0jie
 *
 */
public class NormAffy extends ImportPerLine
{
	public static void main(String[] args) {
		String queryFasta = "/media/winE/Bioinformatics/Affymetrix/rice/Rice_target_modified.fa";
		String subFasta = "/media/winE/Bioinformatics/GenomeData/Rice/TIGRRice/blast/GeneRefSeq.fa";
		String outFile = "/media/winE/Bioinformatics/Affymetrix/rice/Affy2Loc.txt";
		BlastNBC blastNBC = new BlastNBC();
		blastNBC.setBlastType(BlastNBC.BLAST_BLASTN_NR2NR_WITH_NR);
		blastNBC.setDatabaseSeq(subFasta);
		blastNBC.setEvalue(0.001);
		blastNBC.setQueryFastaFile(queryFasta);
		blastNBC.setResultAlignNum(1);
		blastNBC.setResultFile(outFile);
		blastNBC.setResultSeqNum(2);
		blastNBC.setResultType(8);
		blastNBC.blast();
	}
	String dbInfo = "";
	/**
	 * 设定芯片来源，
	 * 如NovelBioConst.DBINFO_ATH_TAIR等
	 * @param dbInfo
	 */
	public void setDbInfo(String dbInfo) {
		this.dbInfo = dbInfo;
	}
	@Override
	boolean impPerLine(String lineContent) {
		if (lineContent.startsWith("#")) {
			return true;
		}
		String[] ss = lineContent.split("\t");
		if (ss[0].startsWith("Probe")) {
			return true;
		}
		GeneID copedID = new GeneID(ss[0], taxID);
		copedID.setUpdateDBinfo(dbInfo, true);
		if (!ss[18].equals("---")) {
			String[] ssGeneID = ss[18].split("///");
			copedID.setUpdateGeneID(ssGeneID[0].trim(), GeneID.IDTYPE_GENEID);
		}
		ArrayList<String> lsRefAccID = new ArrayList<String>();
//		addRefAccID(lsRefAccID, ss[8]);
		addRefAccID(lsRefAccID, ss[10]); addRefAccID(lsRefAccID, ss[14]); addRefAccID(lsRefAccID, ss[17]);
		addRefAccID(lsRefAccID, ss[19]); addRefAccID(lsRefAccID, ss[22]);
		addRefAccID(lsRefAccID, ss[23]); addRefAccID(lsRefAccID, ss[25]);
		copedID.setUpdateRefAccID(lsRefAccID);
		return copedID.update(false);
	}
	
	private void addRefAccID(ArrayList<String> lsRefAccID, String cellInfo) {
		if (cellInfo.equals("---")) {
			return;
		}
		else {
			String[] info = cellInfo.split("///");
			for (String string : info) {
				lsRefAccID.add(string);
			}
		}
	}
	
	/**
	 * 给定target的fasta文件，整理成常规fasta文件，然后去和指定物种的序列做blast
	 * @param fastaFile
	 */
	public void toTargetFastaFile(String fastaFile) {
		String regx = "(?<=target:\\w{0,100}:).+?(?=;)";
		SeqFastaHash seqFastaHash = new SeqFastaHash(fastaFile, regx, false);
		seqFastaHash.writeToFile(FileOperate.changeFileSuffix(fastaFile, "_modified", null));
	}
}