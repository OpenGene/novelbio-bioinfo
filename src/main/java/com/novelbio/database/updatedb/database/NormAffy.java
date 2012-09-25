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
 * ����affy��ע���ļ���������affy̽�룬����ע��ͨͨ������<br>
 * <b>������벻��ȥ�����ǽ�ss[8]����ɸѡ����</b><br>
 * ������ȡ����һ�е�ע��
 * <b>����֮ǰ��׼������</b><br>
 * 1. ����ʽ����Ϊtab������ȥ����������<br>
 * 2. �������޹�ID���Լ�control̽��ȫ��ȥ��
 * 3. �趨�ӵڶ��п�ʼ����
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
	 * �趨оƬ��Դ��
	 * ��NovelBioConst.DBINFO_ATH_TAIR��
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
	 * ����target��fasta�ļ�������ɳ���fasta�ļ���Ȼ��ȥ��ָ�����ֵ�������blast
	 * @param fastaFile
	 */
	public void toTargetFastaFile(String fastaFile) {
		String regx = "(?<=target:\\w{0,100}:).+?(?=;)";
		SeqFastaHash seqFastaHash = new SeqFastaHash(fastaFile, regx, false);
		seqFastaHash.writeToFile(FileOperate.changeFileSuffix(fastaFile, "_modified", null));
	}
}