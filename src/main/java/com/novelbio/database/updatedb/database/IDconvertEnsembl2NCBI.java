package com.novelbio.database.updatedb.database;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genome.gffOperate.GffCodGeneDU;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffType;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.PatternOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.generalConf.NovelBioConst;
/**
 * ID转换，将ensembl的表转化为NCBI的表，以及类似功能
 * @author zong0jie
 *
 */
public class IDconvertEnsembl2NCBI {
	/**
	 * 存储gffFile和对应的taxID
	 */
	LinkedHashMap<String, Integer> hashEnsemblTaxID = new LinkedHashMap<String, Integer>();
	/**
	 * 存储对应的gff文件，现在用NCBI的似乎更合适，如果享用ucsc格式的,就在下面改
	 * 这个的目的是，如果ensemble没找到对应的基因，就到ucsc下面来查找对应的坐标，看该坐标下有没有对应的基因，然后写入数据库
	 */
	ArrayList<String> lsUCSCFile = new ArrayList<String>();
	String taxIDFile = "";
	GffHashGene gffHashGene = null;
	public void setTaxIDFile(String taxIDFile) {
		this.taxIDFile = taxIDFile;
	}
	/**
	 * 必须是txt文件
	 * @param fileName 从ensembl下载的gtf文件
	 * @param ucscFile UCSC的坐标文件，不是gtf格式的
	 * @param taxID
	 */
	public void setEnsemblFile(String fileName, String ucscFile, Integer taxID) {
		hashEnsemblTaxID.put(fileName, taxID);
		lsUCSCFile.add(ucscFile);
	}
	public void update() {
		EnsembleGTF ensembleGTF = new EnsembleGTF();
		EnsembleGTF.setTaxIDFile(taxIDFile);
		int i = 0;
		for (Entry<String, Integer> entry : hashEnsemblTaxID.entrySet()) {
			String fileName = entry.getKey();
			int taxID = entry.getValue();
			ensembleGTF.setTaxID(taxID);
			ensembleGTF.setGffHashGene(GffType.NCBI, lsUCSCFile.get(i));
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
class EnsembleGTF extends ImportPerLine {
	private static Logger logger = Logger.getLogger(EnsembleGTF.class);
	GffHashGene gffHashGene;
	
	PatternOperate patTranscript = new PatternOperate("(?<=transcript_id \")\\w+", false);
	
	public void setGffHashGene(GffType geneType, String gffFile) {
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
			String tmpString = checkIfSame(oldContent, content);
			if (tmpString == null) {//发现新的内容，则将老的内容导入
				if (!impPerLine(oldContent)) {
					if (txtWriteExcep != null) {
						txtWriteExcep.writefileln(oldContent);
					}
				}
				oldContent = content;
			} else {
				oldContent = tmpString;
			}
		}
		//跳出循环后再导入最后一个oldContent
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
	
	/**
	 * 判断两行是不是来自同一个基因，如果来自同一个基因，就将新基因的坐标和老基因的坐标合并
	 * @param oldLine
	 * @param newLine
	 * @return null 表示是一个全新的line
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
	 * E22C19W28_E50C23	protein_coding	CDS	775083	775229	.	-	0	
	 *  gene_id "ENSGALG00000010254"; transcript_id "ENSGALT00000016676"; exon_number "12"; gene_name "FAIM2"; gene_biotype "protein_coding"; protein_id "ENSGALP00000016657";
	 *  
	 *  先在数据库中找geneName等，找不到再找gff文件
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
			else if (string.contains("transcript_name")) {
				lsRefID.add(string.replace("transcript_name", "").replace("\"", "").trim());
			}
			else if (string.contains("gene_name")) {
				lsRefID.add(string.replace("gene_name", "").replace("\"", "").trim());
			}
			else if (string.contains("protein_id")) {
				lsRefID.add(string.replace("protein_id", "").replace("\"", "").trim());
			}
		}
		GeneID geneID = new GeneID("", taxID);
		geneID.setUpdateRefAccID(lsRefID);
		geneID.setUpdateRefAccIDClear(true);
		if (geneID.getIDtype().equals(GeneID.IDTYPE_ACCID)) {
			GffCodGeneDU gffCodGeneDu = gffHashGene.searchLocation("chr"+ss[0].toLowerCase().replace("chr", ""), Integer.parseInt(ss[3]),  Integer.parseInt(ss[4]));
			if (gffCodGeneDu == null || gffCodGeneDu.getAllGffDetail().size() <= 0) {
				return false;
			}
			int geneNum = gffCodGeneDu.getCoveredGffGene().size()/2;
			List<GffDetailGene> lsGenes = new ArrayList<GffDetailGene>(gffCodGeneDu.getCoveredGffGene());
			geneID = lsGenes.get(geneNum).getLongestSplitMrna().getGeneID();
			if (geneID.getIDtype().equals(GeneID.IDTYPE_ACCID)) {
				return false;
			}
		}
		
		//本方法效率较低，不过无所谓了
		for (String string : ssID) {
			if (string.contains("gene_id")) {
				geneID.setUpdateAccID(string.replace("gene_id", "").replace("\"", "").trim());
				geneID.setUpdateDBinfo(NovelBioConst.DBINFO_ENSEMBL_GENE, false);
				geneID.update(true);
			}
			else if (string.contains("transcript_id")) {
				geneID.setUpdateAccID(string.replace("transcript_id", "").replace("\"", "").trim());
				geneID.setUpdateDBinfo(NovelBioConst.DBINFO_ENSEMBL_TRS, false);
				geneID.update(true);
			}
			else if (string.contains("gene_name")) {
				geneID.setUpdateAccID(string.replace("gene_name", "").replace("\"", "").trim());
				geneID.setUpdateDBinfo(NovelBioConst.DBINFO_SYMBOL, false);
				geneID.update(true);
			}
			else if (string.contains("protein_id")) {
				geneID.setUpdateAccID(string.replace("protein_id", "").replace("\"", "").trim());
				geneID.setUpdateDBinfo(NovelBioConst.DBINFO_ENSEMBL_PRO, false);
				geneID.update(true);
			}
		}
		return true;
	}
	
}
