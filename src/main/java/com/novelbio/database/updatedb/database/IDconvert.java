package com.novelbio.database.updatedb.database;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genomeNew.gffOperate.GffCodGeneDU;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashGene;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.PatternOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.modcopeid.CopedID;
import com.novelbio.generalConf.NovelBioConst;
/**
 * IDת������ensembl�ı�ת��ΪNCBI�ı����Լ����ƹ���
 * @author zong0jie
 *
 */
public class IDconvert {
	/**
	 * �洢gffFile�Ͷ�Ӧ��taxID
	 */
	LinkedHashMap<String, Integer> hashEnsemblTaxID = new LinkedHashMap<String, Integer>();
	/**
	 * �洢��Ӧ��gff�ļ���������NCBI���ƺ������ʣ��������ucsc��ʽ��,���������
	 * �����Ŀ���ǣ����ensembleû�ҵ���Ӧ�Ļ��򣬾͵�ucsc���������Ҷ�Ӧ�����꣬������������û�ж�Ӧ�Ļ���Ȼ��д�����ݿ�
	 */
	ArrayList<String> lsUCSCFile = new ArrayList<String>();
	String taxIDFile = "";
	GffHashGene gffHashGene = null;
	public void setTaxIDFile(String taxIDFile) {
		this.taxIDFile = taxIDFile;
	}
	/**
	 * ������txt�ļ�
	 * @param fileName ��ensembl���ص�gtf�ļ�
	 * @param ucscFile UCSC�������ļ�������gtf��ʽ��
	 * @param taxID
	 */
	public void setEnsemblFile(String fileName, String ucscFile, Integer taxID) {
		hashEnsemblTaxID.put(fileName, taxID);
		lsUCSCFile.add(ucscFile);
	}
	public void update() {
		EnsembleGTF ensembleGTF = new EnsembleGTF();
		ensembleGTF.setTaxIDFile(taxIDFile);
		int i = 0;
		for (Entry<String, Integer> entry : hashEnsemblTaxID.entrySet()) {
			String fileName = entry.getKey();
			int taxID = entry.getValue();
			ensembleGTF.setTaxID(taxID);
			ensembleGTF.setGffHashGene(NovelBioConst.GENOME_GFF_TYPE_NCBI, lsUCSCFile.get(i));
			ensembleGTF.setTxtWriteExcep(FileOperate.changeFileSuffix(fileName, "_NotFindInDB", null));
			ensembleGTF.updateFile(fileName, false);
			i ++;
		}
	}
}
/**
 * ����UCSC�������ļ�����ensembl��gff�ļ�����refseqIDȻ����ncbi�⣬�Ҳ���������uniID��
 * @author zong0jie
 *
 */
class EnsembleGTF extends ImportPerLine
{
	private static Logger logger = Logger.getLogger(EnsembleGTF.class);
	GffHashGene gffHashGene;
	public void setGffHashGene(String geneType, String gffFile) {
		gffHashGene =  new GffHashGene(geneType, gffFile);
	}
	/**
	 * E22C19W28_E50C23	protein_coding	CDS	775083	775229	.	-	0	 gene_id "ENSGALG00000010254"; transcript_id "ENSGALT00000016676"; exon_number "12"; gene_name "FAIM2"; gene_biotype "protein_coding"; protein_id "ENSGALP00000016657";
	 * ensembl��gff�ĸ�ʽ
	 * ��ָ�����ļ��������ݿ⣬������ظ��Ļ��򣬾Ͳ�������
	 * �����Ҫ������У�Ʃ��amiGO����Ϣ���븲�Ǹ÷���
	 */
	public void updateFile(String gene2AccFile, boolean gzip) {
		setReadFromLine();
		TxtReadandWrite txtGene2Acc;
		if (gzip)
			txtGene2Acc = new TxtReadandWrite(TxtReadandWrite.GZIP, gene2AccFile);
		else 
			txtGene2Acc = new TxtReadandWrite(gene2AccFile, false);
		//�ӵڶ��п�ʼ��ȡ
		String oldContent = null;
		int num = 0;
		for (String content : txtGene2Acc.readlines(readFromLine)) {
			num++;
			if (num%10000 == 0) {
				logger.info("import line number:" + num);
			}
			String tmpString = checkIfSame(oldContent, content);
			if (tmpString == null) {//�����µ����ݣ����ϵ����ݵ���
				if (!impPerLine(oldContent)) {
					if (txtWriteExcep != null) {
						txtWriteExcep.writefileln(oldContent);
					}
				}
				oldContent = content;
			}
			else {
				oldContent = tmpString;
			}
		}
		//����ѭ�����ٵ������һ��oldContent
		if (!impPerLine(oldContent)) {
			if (txtWriteExcep != null) {
				txtWriteExcep.writefileln(oldContent);
			}
		}
		impEnd();
		txtGene2Acc.close();
		if (txtWriteExcep != null) {
			txtWriteExcep.close();
		}
		logger.info("finished import file " + gene2AccFile);
	}
	
	PatternOperate patTranscript = new PatternOperate("(?<=transcript_id \")\\w+", false);
	
	/**
	 * �ж������ǲ�������ͬһ�������������ͬһ�����򣬾ͽ��»����������ϻ��������ϲ�
	 * @param oldLine
	 * @param newLine
	 * @return null ��ʾ��һ��ȫ�µ�line
	 */
	private String checkIfSame(String oldLine, String newLine) {
		if (oldLine == null) {
			return null;
		}
		String[] OldInfo = oldLine.split("\t");
		String[] ThisInfo = newLine.split("\t");
		String transIDold = patTranscript.getPatFirst(OldInfo[8]);
		String transIDnew = patTranscript.getPatFirst(ThisInfo[8]);
		if (!transIDold.equals(transIDnew)) {
			return null;
		}
		String[] tmpResult = null;
		if (OldInfo[8].length() > ThisInfo[8].length()) {
			tmpResult = OldInfo;
		}
		else {
			tmpResult = ThisInfo;
		}
		tmpResult[3] = Math.min(Integer.parseInt(OldInfo[3]), Integer.parseInt(ThisInfo[3])) + "";
		tmpResult[4] = Math.max(Integer.parseInt(OldInfo[4]), Integer.parseInt(ThisInfo[4])) + "";
		return ArrayOperate.cmbString(tmpResult, "\t");
	}
	
	/**
	 * E22C19W28_E50C23	protein_coding	CDS	775083	775229	.	-	0	 gene_id "ENSGALG00000010254"; transcript_id "ENSGALT00000016676"; exon_number "12"; gene_name "FAIM2"; gene_biotype "protein_coding"; protein_id "ENSGALP00000016657";
	 */
	@Override
	public boolean impPerLine(String lineContent) {
		if (lineContent == null) {
			return true;
		}
		
		String[] ss = lineContent.split("\t");
		String[] ssID = ss[8].split(";");
		ArrayList<String> lsRefID = new ArrayList<String>();
		for (String string : ssID) {
			if (string.contains("gene_id")) {
				lsRefID.add(string.replace("gene_id", "").replace("\"", "").trim());
			}
			else if (string.contains("transcript_id")) {
				lsRefID.add(string.replace("transcript_id", "").replace("\"", "").trim());
			}
			else if (string.contains("gene_name")) {
				lsRefID.add(string.replace("gene_name", "").replace("\"", "").trim());
			}
			else if (string.contains("protein_id")) {
				lsRefID.add(string.replace("protein_id", "").replace("\"", "").trim());
			}
		}
		CopedID copedID = new CopedID("", taxID);
		copedID.setUpdateRefAccID(lsRefID);
		copedID.setUpdateRefAccIDClear(true);
		if (copedID.getIDtype().equals(CopedID.IDTYPE_ACCID)) {
			GffCodGeneDU gffCodGeneDu = gffHashGene.searchLocation("chr"+ss[0].toLowerCase().replace("chr", ""), Integer.parseInt(ss[3]),  Integer.parseInt(ss[4]));
			if (gffCodGeneDu == null || gffCodGeneDu.getAllGffDetail().size() <= 0) {
//				copedID.update(false);
				return false;
			}
			int geneNum = gffCodGeneDu.getAllGffDetail().size()/2;
			copedID = gffCodGeneDu.getAllGffDetail().get(geneNum).getLongestSplit().getCopedID();
			if (copedID.getIDtype().equals(CopedID.IDTYPE_ACCID)) {
//				copedID.update(false);
				return false;
			}
		}
		
		//������Ч�ʽϵͣ���������ν��
		for (String string : ssID) {
			if (string.contains("gene_id")) {
				copedID.setUpdateAccID(string.replace("gene_id", "").replace("\"", "").trim());
				copedID.setUpdateDBinfo(NovelBioConst.DBINFO_ENSEMBL_GENE, false);
				copedID.update(true);
			}
			else if (string.contains("transcript_id")) {
				copedID.setUpdateAccID(string.replace("transcript_id", "").replace("\"", "").trim());
				copedID.setUpdateDBinfo(NovelBioConst.DBINFO_ENSEMBL_TRS, false);
				copedID.update(true);
			}
			else if (string.contains("gene_name")) {
				copedID.setUpdateAccID(string.replace("gene_name", "").replace("\"", "").trim());
				copedID.setUpdateDBinfo(NovelBioConst.DBINFO_SYMBOL, false);
				copedID.update(true);
			}
			else if (string.contains("protein_id")) {
				copedID.setUpdateAccID(string.replace("protein_id", "").replace("\"", "").trim());
				copedID.setUpdateDBinfo(NovelBioConst.DBINFO_ENSEMBL_PRO, false);
				copedID.update(true);
			}
		}
		return true;
	}
	
}