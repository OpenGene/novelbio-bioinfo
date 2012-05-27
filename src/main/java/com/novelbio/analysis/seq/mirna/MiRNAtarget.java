package com.novelbio.analysis.seq.mirna;

import java.util.ArrayList;

import com.novelbio.analysis.seq.genomeNew.GffChrSeq;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFasta;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffCodGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashGene;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.generalConf.NovelBioConst;

/**
 * 预测miRNA的靶基因
 * @author zong0jie
 *
 */
public class MiRNAtarget {
//	GffHashGene gffHashGene = new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_UCSC, NovelBioConst.GENOME_PATH_UCSC_HG19_GFF_REFSEQ);
	GffChrSeq gffChrSeq = new GffChrSeq(NovelBioConst.GENOME_GFF_TYPE_NCBI,
			"/media/winE/Bioinformatics/GenomeData/pig/gff/ref_Sscrofa10.2_top_level_modify.gff3", "/media/winE/Bioinformatics/GenomeData/pig/chromFa");
	public static void main(String[] args) {
		MiRNAtarget miRNAtarget = new MiRNAtarget();
		miRNAtarget.getMir2Target("/media/winF/NBC/Project/Project_XSQ_Lab/miRNA/novelbio/sRNAall/noveltarget/predict", "/media/winF/NBC/Project/Project_XSQ_Lab/miRNA/novelbio/sRNAall/noveltarget/predictOutput");
	}
	/**
	 * 查找miRanda网站上下载的人类miRNA的靶基因，看落到什么区域的
	 */
	public void getGeneUTR(String testFile, String outFile) {
		TxtReadandWrite txtOut = new TxtReadandWrite(outFile, true);
		TxtReadandWrite txtRead = new TxtReadandWrite(testFile, false);
		for (String string : txtRead.readlines(2)) {
			String[] ss = string.split("\t");
			String[] locationInfo = ss[13].split(",")[0].split(":");
			String chrID = "chr" + locationInfo[1];
			int start = Integer.parseInt(locationInfo[2].split("-")[0]);
			int end = Integer.parseInt(locationInfo[2].split("-")[1]);
			GffCodGene gffCodGene = gffHashGene.searchLocation(chrID, (start + end)/2);
			if (gffCodGene == null) {
				continue;
			}
			if (!gffCodGene.isInsideLoc()) {
				continue;
			}
			GffGeneIsoInfo gffGeneIsoInfo = gffCodGene.getGffDetailThis().getIsolist(ss[5]);
			if (gffGeneIsoInfo == null) {
				continue;
			}
			if (gffGeneIsoInfo.getCodLocUTR(gffCodGene.getCoord()) == GffGeneIsoInfo.COD_LOCUTR_5UTR) {
				txtOut.writefileln(ss[0] + "\t" + ss[13] + "\t" + "5UTR");
			}
			else if (gffGeneIsoInfo.getCodLocUTR(gffCodGene.getCoord()) == GffGeneIsoInfo.COD_LOCUTR_3UTR) {
				txtOut.writefileln(ss[0] + "\t" + ss[13] + "\t" + "3UTR");
			}
			else {
				txtOut.writefileln(ss[0] + "\t" + ss[13] + "\t" + "Other");
			}
		}
		txtOut.close();
		txtRead.close();
	}
	/**
	 * 将所有含有3UTR的基因的3UTR序列写入文本
	 * @param outFile
	 */
	public void get3UTRseq(String outFile) {
		TxtReadandWrite txtOut = new TxtReadandWrite(outFile, true);
		ArrayList<SeqFasta> ls3UTR = gffChrSeq.getSeq3UTRAll();
		for (SeqFasta seqFasta : ls3UTR) {
			txtOut.writefileln(seqFasta.toStringNRfasta());
		}
		txtOut.close();
	}
	/**
	 * 给定miRanda的输出文件，将其整理成需要的格式
	 * @param miRandaOut
	 */
	public void getMir2Target(String miRandaOut, String outFile) {
		TxtReadandWrite txtRead = new TxtReadandWrite(miRandaOut, false);
		TxtReadandWrite txtOut = new TxtReadandWrite(outFile, true);
		boolean start = true;//标记扫描的起点
		String[] pair = null;
		for (String string : txtRead.readlines()) {
			if (start) {
				pair = new String[4];//0: mir 1:Target 2: energy
			}
			if (string.contains("Performing Scan")) {
				string = string.replace("Performing Scan:", "");
				String[] tmp = string.split("vs");
				pair[0] = tmp[0].trim(); pair[1] = tmp[1].trim();
				start = false;
			}
			//标记一个写完
			if (string.contains("Complete")) {
				if (pair[0] != null) {
					txtOut.writefileln(pair);
				}
				start = true;
			}
			//没找到就查找下一条
			if (string.contains("No Hits Found above Threshold")) {
				start = true;
				continue;
			}
			if (string.startsWith(">") && !string.startsWith(">>")) {
				String[] tmpScore = string.split("\t");
				if (pair[2] == null) {
					pair[2] = tmpScore[2]; pair[3] = tmpScore[3];
				}
				else {
					double score = Double.parseDouble(tmpScore[2]);
					if (score > Double.parseDouble(pair[2])) {
						pair[2] = tmpScore[2]; pair[3] = tmpScore[3];
					}
				}
			}
		}
		txtRead.close();
		txtOut.close();
	}
}
