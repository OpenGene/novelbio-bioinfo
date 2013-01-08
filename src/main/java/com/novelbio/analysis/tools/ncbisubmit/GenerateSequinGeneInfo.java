package com.novelbio.analysis.tools.ncbisubmit;

import java.util.ArrayList;
import java.util.HashSet;

import org.apache.poi.ss.util.SSCellRange;

import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqFastaHash;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.sun.tools.javah.resources.l10n;

/**
 * 将gff文件整理成为Sequin识别的格式
 * 如下：
 * 1830	2966	gene
			gene	dnaN
			locus_tag     OBB_0002
1830	2966	CDS
			product	DNA-directed DNA polymerase III beta chain
			EC_number	2.7.7.7
			protein_id	gnl|ncbi|OBB_0002
3219	3440	gene
			locus_tag     OBB_0003
3219	3440	CDS
			product	hypothetical protein
			protein_id	gnl|ncbi|OBB_0003
3443	4552	gene
			gene	recF
			locus_tag     OBB_0004
3443	4552	CDS
			product	RecF
			function	DNA repair and genetic recombination
			protein_id	gnl|ncbi|OBB_0004

 * @author zong0jie
 *
 */
public class GenerateSequinGeneInfo {	
	String gffBactriumFile;
	String outFile;
	
	public static void main(String[] args) {
		String gffBactriaFile = "/media/winF/NBC/Project/Project_WZF/annotation/finalBacterium.Gene.Prediction.anno_out_All_final_2.gff";
		GenerateSequinGeneInfo generateSequinGeneInfo = new GenerateSequinGeneInfo();
		generateSequinGeneInfo.setGffBactriumFile(gffBactriaFile);
		generateSequinGeneInfo.copeFile();
		
//		String gffBactriaFile = "/media/winF/NBC/Project/Project_WZF/annotation/finalBacterium.Gene.Prediction.anno_out_All.gff";
//		TxtReadandWrite txtRead = new TxtReadandWrite(gffBactriaFile, false);
//		TxtReadandWrite txtWriteNull = new TxtReadandWrite(gffBactriaFile + "tmpNull", true);
//		TxtReadandWrite txtWriteNorm = new TxtReadandWrite(gffBactriaFile + "tmpNorm", true);
//		TxtReadandWrite txtWriteNoProduct = new TxtReadandWrite(gffBactriaFile + "tmpNoProduct", true);
//		for (String string : txtRead.readlines()) {
//			if (string.contains("Product=")) {
//				txtWriteNorm.writefileln(string);
//			} else if (string.contains("NULL")) {
//				txtWriteNull.writefileln(string);
//			} else {
//				txtWriteNoProduct.writefileln(string);
//			}
//		
//		}
//		txtRead.close();
//		txtWriteNull.close();
//		txtWriteNorm.close();
//		txtWriteNoProduct.close();
		
		
		
//		TxtReadandWrite txtReadandWrite = new TxtReadandWrite("/media/winF/NBC/Project/Project_WZF/annotation/finalBacterium.Gene.Prediction.anno_out_All.gfftmpNoProduct", false);
//		TxtReadandWrite txtWrite = new TxtReadandWrite("/media/winF/NBC/Project/Project_WZF/annotation/finalBacterium.Gene.Prediction.anno_out_All.gfftmpNoProductModify", true);
//		for (String string : txtReadandWrite.readlines()) {
//			String[] ss = string.split("\t");
//			ss[8] = ss[8].split("OS=")[0];
//			txtWrite.writefileln(ss);
//		}
//		txtReadandWrite.close();
//		txtWrite.close();
		
		
		
		
	}
	
	public void setGffBactriumFile(String gffBactriumFile) {
		this.gffBactriumFile = gffBactriumFile;
		if (outFile == null) {
			outFile = FileOperate.changeFileSuffix(gffBactriumFile, "_Sequin", null);
		}
	}
	public void setOutFile(String outFile) {
		this.outFile = outFile;
	}
	
	public void copeFile() {
		TxtReadandWrite txtGffFile = new TxtReadandWrite(gffBactriumFile, false);
		TxtReadandWrite txtOut = new TxtReadandWrite(outFile, true);
		for (String content : txtGffFile.readlines()) {
			SequinGene sequinGene = new SequinGene(content);
			txtOut.writefileln(sequinGene.toString());
		}
		txtOut.close();
	}
}
/** 将单个基因整理成指定格式
 * 
 * 1830	2966	gene
			gene	dnaN
			locus_tag     OBB_0002
1830	2966	CDS
			product	DNA-directed DNA polymerase III beta chain
			EC_number	2.7.7.7
			protein_id	gnl|ncbi|OBB_0002
 *  
 */
class SequinGene {
	static int geneNum = 1;
	static String prefix = "NJAUSS";
	static String labName = "LuLabNJAU";
	int start;
	int end;

	/** gene名 */
	String geneName;
	/** locus名 */
	String locus_tag;
	/** CDS，rrna等等 */
	String type;
	
	String product;
	String note;
	boolean isPseudo = false;
	boolean isMisc = false;
	public SequinGene(String gffLines) {
		String[] ss = gffLines.split("\t");
		if (ss[2].equalsIgnoreCase("tRNA")) {
			type = "tRNA";
		} else if (ss[2].equalsIgnoreCase("rRNA")) {
			type = "rRNA";
		} else if (ss[2].equalsIgnoreCase("pseudo")) {
			setIsPseudo(true);
			type = "CDS";
		} else if (ss[2].equalsIgnoreCase("misc_feature")) {
			isMisc = true;
			type = "misc_feature";
		} else {
			type = "CDS";
		}
		setStartEnd(Integer.parseInt(ss[3]), Integer.parseInt(ss[4]), ss[6].equals("+"));
		if (ss[8].contains("Product=")) {
			String product = ss[8].split(";")[1].split("Product=")[1].trim();
			setProduct(product);
		}
		if (ss[8].contains("Note=")) {
			String note = ss[8].split("Note=")[1].trim();
			setNote(note);
		}
		setLocus_tag(getLOC(geneNum));
		geneNum ++;
	}
	private void setIsPseudo(boolean pseudo) {
		this.isPseudo = pseudo;
	}
	/**
	 * 默认补齐到4位
	 * 输入10，100等
	 * 返回 0010,0100等
	 * @param num
	 * @return
	 */
	private String getLOC(int num) {
		String tmpOut = num + "";
		int length = tmpOut.length();
		int remainLength = 4 - length;
		for (int i = 0; i < remainLength; i++) {
			tmpOut = "0" + tmpOut;
		}
		return prefix + "_" + tmpOut;
	}
	
	private void setStartEnd(int start, int end, boolean cis5to3) {
		if (cis5to3) {
			this.start = Math.min(start, end);
			this.end = Math.max(start, end);
		}
		else {
			this.end = Math.min(start, end);
			this.start = Math.max(start, end);
		}
	}
	public void setGeneName(String geneName) {
		this.geneName = geneName;
	}
	public void setProduct(String product) {
		this.product = product;
	}
	public void setNote(String note) {
		this.note = note;
	}
	public void setType(String type) {
		this.type = type;
	}
	public void setLocus_tag(String locus_tag) {
		this.locus_tag = locus_tag;
	}
	
	/** 写入文本，不包含换行 */
	public String toString() {
		String locationLine = start + "\t" + end + "\t" + "gene";
		if (geneName != null && !geneName.equals("")) {
			locationLine = locationLine + TxtReadandWrite.ENTER_LINUX + "\t\t\tgene\t" + geneName; 
		}
		locationLine = locationLine + TxtReadandWrite.ENTER_LINUX + "\t\t\tlocus_tag\t" + locus_tag;
		if (isPseudo) {
			locationLine = locationLine + TxtReadandWrite.ENTER_LINUX + "\t\t\tpseudo";
			if (product != null && !product.equals("")) {
				locationLine = locationLine + TxtReadandWrite.ENTER_LINUX + "\t\t\tproduct\t" + product;
			}
			if (note != null && !note.equals("")) {
				locationLine = locationLine + TxtReadandWrite.ENTER_LINUX + "\t\t\tproduct\t" + note;
			}
			return locationLine;
		}
		locationLine = locationLine + TxtReadandWrite.ENTER_LINUX + start + "\t" + end + "\t" + type;
		if (product != null && !product.equals("")) {
			locationLine = locationLine + TxtReadandWrite.ENTER_LINUX + "\t\t\tproduct\t" + product;
		}
		if (note != null && !note.equals("")) {
			locationLine = locationLine + TxtReadandWrite.ENTER_LINUX + "\t\t\tproduct\t" + note;
		}
		if (type.equals("CDS")) {
			locationLine = locationLine + TxtReadandWrite.ENTER_LINUX + "\t\t\tprotein_id\t" + "gnl|" +labName+"|" + locus_tag;
		}
		if (isPseudo) {
			locationLine = locationLine + TxtReadandWrite.ENTER_LINUX + "\t\t\tpseudo";
		}
		return locationLine;
	}
	
	/** 写入文本，不包含换行 */
	public String toTitle() {
		String locationLine = locus_tag + " " +  product;
		return locationLine;
	}
	
	
	
}
