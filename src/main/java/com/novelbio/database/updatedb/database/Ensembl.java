package com.novelbio.database.updatedb.database;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genomeNew.gffOperate.GffCodGeneDU;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashGene;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.modcopeid.CopedID;
import com.novelbio.generalConf.NovelBioConst;

public class Ensembl {
	/**
	 * 存储gffFile和对应的taxID
	 */
	LinkedHashMap<String, Integer> hashEnsemblTaxID = new LinkedHashMap<String, Integer>();
	/**
	 * 存储对应的gff文件，最好是ucsc格式的
	 * 这个的目的是，如果ensemble没找到对应的基因，就到ucsc下面来查找对应的坐标，看该坐标下有没有对应的基因，然后写入数据库
	 */
	ArrayList<String> lsUCSCFile = new ArrayList<String>();
	String taxIDFile = "";
	GffHashGene gffHashGene = null;
	public void setTaxIDFile(String taxIDFile) {
		this.taxIDFile = taxIDFile;
	}
	/**
	 * 
	 * 必须是txt文件
	 * @param fileName 从ensembl下载的gtf文件
	 * @param ucscFile UCSC的坐标文件，不是gtf格式的
	 * @param taxID
	 */
	public void setEnsemblFile(String fileName, String ucscFile, Integer taxID)
	{
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
			ensembleGTF.setGffHashGene(NovelBioConst.GENOME_GFF_TYPE_UCSC, lsUCSCFile.get(i));
			ensembleGTF.setTxtWriteExcep(FileOperate.changeFileSuffix(fileName, "_NotFindInDB", null));
			ensembleGTF.updateFile(fileName, false);
			i ++;
			
		}
	}
}
/**
 * 根据UCSC的坐标文件，将ensembl的gff文件搜索refseqID然后导入ncbi库，找不到的则导入uniID库
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
	 * ensembl的gff的格式
	 * 将指定的文件导入数据库，如果是重复的基因，就不导入了
	 * 如果需要导入多行，譬如amiGO的信息，请覆盖该方法
	 */
	public void updateFile(String gene2AccFile, boolean gzip) {
		setReadFromLine();
		TxtReadandWrite txtGene2Acc;
		if (gzip)
			txtGene2Acc = new TxtReadandWrite(TxtReadandWrite.GZIP, gene2AccFile);
		else 
			txtGene2Acc = new TxtReadandWrite(gene2AccFile, false);
		//从第二行开始读取
		String oldContent = null;
		int num = 0;
		for (String content : txtGene2Acc.readlines(readFromLine)) {
			num++;
			if (num%10000 == 0) {
				logger.info("import line number:" + num);
			}
			if (checkIfSame(oldContent, content)) {
				continue;
			}
			if (!impPerLine(content)) {
				if (txtWriteExcep != null) {
					txtWriteExcep.writefileln(content);
				}
			}
			oldContent = content;
		}
		impEnd();
		txtGene2Acc.close();
		if (txtWriteExcep != null) {
			txtWriteExcep.close();
		}
		logger.info("finished import file " + gene2AccFile);
	}
	/**
	 * 判断两行是不是来自同一个基因
	 * @param oldLine
	 * @param newLine
	 * @return
	 */
	private boolean checkIfSame(String oldLine, String newLine)
	{
		if (oldLine == null) {
			return false;
		}
		String OldInfo = oldLine.split("\t")[8];
		String ThisInfo = newLine.split("\t")[8];
		String[] ssOld = OldInfo.split(";");
		String[] ssThis = ThisInfo.split(";");
		if (ssOld.length < ssThis.length) {
			return false;
		}
		for (int i = 0; i < ssThis.length; i++) {
			if (ssOld[i].equals("") || ssOld[i].contains("exon_number") || ssOld[i].contains("gene_biotype") 
			|| ssOld[i].contains("transcript_name")) {
				continue;
			}
			if (!ssOld[i].equals(ssThis[i])) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * E22C19W28_E50C23	protein_coding	CDS	775083	775229	.	-	0	 gene_id "ENSGALG00000010254"; transcript_id "ENSGALT00000016676"; exon_number "12"; gene_name "FAIM2"; gene_biotype "protein_coding"; protein_id "ENSGALP00000016657";
	 */
	@Override
	public boolean impPerLine(String lineContent) {
		String[] ss = lineContent.split("\t");
		String[] ssID = ss[8].split(";");
		ArrayList<String> lsRefID = new ArrayList<String>();
		for (String string : ssID) {
			if (string.contains("ENSBTAT00000064644")) {
				System.out.println("stop");
			}
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
		
		//本方法效率较低，不过无所谓了
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