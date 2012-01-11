package com.novelbio.database.updatedb.idconvert;

import java.util.ArrayList;

import org.apache.ibatis.annotations.Update;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFasta;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFastaHash;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.model.modcopeid.CopedID;

public class ZebraFish {
	public static void main(String[] args) {
		ZebraFish zebraFish = new ZebraFish();
//		zebraFish.updateZbGeneID("/home/zong0jie/桌面/danio_rerio/gene2geneID.txt");
//		zebraFish.updateZbGO("/home/zong0jie/桌面/danio_rerio/gene_association.zfin");
//		zebraFish.updateZbRefSeqID("/home/zong0jie/桌面/danio_rerio/gene2refseq.txt", NovelBioConst.DBINFO_NCBI_ACC_REFSEQ);
//		zebraFish.updateZbRefSeqID("/home/zong0jie/桌面/danio_rerio/ensembl_1_to_1.txt", NovelBioConst.DBINFO_ENSEMBL);
		zebraFish.getSeq("/home/zong0jie/桌面/danio_rerio/sequence/rna.fa", "/home/zong0jie/桌面/danio_rerio/sequence/rnaCope.fa");
	}
	private void updateZbGeneID(String zbID2geneIDFile) {
		TxtReadandWrite txtRead = new TxtReadandWrite(zbID2geneIDFile, false);
		for (String content : txtRead.readlines()) {
			String[] ss = content.split("\t");
			CopedID copedID = new CopedID(ss[0], 7955);
			copedID.setUpdateGeneID(ss[2], CopedID.IDTYPE_GENEID);
			copedID.setUpdateDBinfo(NovelBioConst.DBINFO_DRE_ZFIN, true);
			copedID.update(true);
			
			copedID = new CopedID(ss[1], 7955);
			copedID.setUpdateGeneID(ss[2], CopedID.IDTYPE_GENEID);
			copedID.setUpdateDBinfo(NovelBioConst.DBINFO_SYMBOL, true);
			copedID.update(true);
		}
	}
	private void updateZbRefSeqID(String zbID2RefSeqIDFile, String dbInfo) {
		TxtReadandWrite txtRead = new TxtReadandWrite(zbID2RefSeqIDFile, false);
		for (String content : txtRead.readlines()) {
			String[] ss = content.split("\t");
			CopedID copedID = new CopedID(ss[0], 7955);
			copedID.setUpdateRefAccID(ss[2]);
			copedID.setUpdateDBinfo(NovelBioConst.DBINFO_DRE_ZFIN, true);
			copedID.update(true);
			
			copedID = new CopedID(ss[1], 7955);
			copedID.setUpdateRefAccID(ss[2]);
			copedID.setUpdateDBinfo(NovelBioConst.DBINFO_SYMBOL, true);
			copedID.update(true);
			
			copedID = new CopedID(ss[2], 7955);
			copedID.setUpdateRefAccID(ss[0]);
			copedID.setUpdateDBinfo(dbInfo, true);
			copedID.update(true);
		}
	}
	private void updateZbGO(String goFile) {
		TxtReadandWrite txtRead = new TxtReadandWrite(goFile, false);
		for (String content : txtRead.readlines()) {
			if (content.startsWith("!")) {
				continue;
			}
			String[] ss = content.split("\t");
			CopedID copedID = new CopedID(ss[1], 7955);
			copedID.setUpdateGO(ss[3], NovelBioConst.DBINFO_DRE_ZFIN, ss[5], ss[4], "");
			copedID.update(true);
		}
	}
	
	private void getSeq(String seqIn, String seqOut)
	{
		SeqFastaHash seqFastaHash = new SeqFastaHash(seqIn, "\\w{2}_\\d+", false, false);
		ArrayList<SeqFasta> lsFasta = seqFastaHash.getSeqFastaAll();
		TxtReadandWrite txtOut = new TxtReadandWrite(seqOut, true);
		for (SeqFasta seqFasta : lsFasta) {
			txtOut.writefileln(seqFasta.toStringNRfasta());
		}
	}
}
