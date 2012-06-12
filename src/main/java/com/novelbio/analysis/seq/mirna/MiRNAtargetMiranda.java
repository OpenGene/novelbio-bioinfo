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
 * Ԥ��miRNA�İл���miranda
 * @author zong0jie
 *
 */
public class MiRNAtargetMiranda {
//	GffHashGene gffHashGene = new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_UCSC, NovelBioConst.GENOME_PATH_UCSC_HG19_GFF_REFSEQ);
	GffChrSeq gffChrSeq = null;
	public static void main(String[] args) {
		MiRNAtargetMiranda miRNAtarget = new MiRNAtargetMiranda();
		miRNAtarget.getMir2Target("/media/winF/NBC/Project/Project_XSQ_Lab/miRNA/novelbio/sRNAall/noveltarget/predict", "/media/winF/NBC/Project/Project_XSQ_Lab/miRNA/novelbio/sRNAall/noveltarget/predictOutput");
	}
	public void setGffChrSeq(GffChrSeq gffChrSeq) {
		this.gffChrSeq = gffChrSeq;
	}
	/**
	 * �����к���3UTR�Ļ����3UTR����д���ı�
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
	 * ����miRanda������ļ��������������Ҫ�ĸ�ʽ
	 * @param miRandaOut
	 */
	public void getMir2Target(String miRandaOut, String outFile) {
		TxtReadandWrite txtRead = new TxtReadandWrite(miRandaOut, false);
		TxtReadandWrite txtOut = new TxtReadandWrite(outFile, true);
		boolean start = true;//���ɨ������
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
			//���һ��д��
			if (string.contains("Complete")) {
				if (pair[0] != null) {
					txtOut.writefileln(pair);
				}
				start = true;
			}
			//û�ҵ��Ͳ�����һ��
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
