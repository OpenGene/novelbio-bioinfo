package com.novelbio.analysis.project.kgy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFasta;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFastaHash;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqHash;
import com.novelbio.base.dataOperate.TxtReadandWrite;

/**
 * ���blast������������
 * @author zong0jie
 *
 */
public class GetBlastResult {
	SeqFastaHash seqHash;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		GetBlastResult getBlastResult = new GetBlastResult();
		getBlastResult.getSeq();

	}
	
	
	private void getSeq()
	{
		String parent = "/media/winE/NBC/Project/RNA-Seq_KGY/20110624_SalviaMiltiorrhiza/blast/";
		String txtFile = parent + "AtPAP1blastInfo.txt";
		String fastaFile = parent + "KGYfinal.fa";
		String seqOut = parent + "AtPAP1blastSeq.txt";
		setSeqHash(fastaFile);
		Set<String> lsGeneID = getLsBlastGeneID(txtFile,2);
		writeSeq(lsGeneID, seqOut);
//		seqHash.saveChrLengthToFile(parent + "chrLen");
	}
	
	/**
	 * ����blast���ļ�
	 * @param txtFile
	 * @param colGeneID
	 * @return
	 */
	private Set<String> getLsBlastGeneID(String txtFile, int colGeneID)
	{
		colGeneID --;
		TxtReadandWrite txtRead = new TxtReadandWrite(txtFile, false);
		ArrayList<String[]> lsTmp = txtRead.ExcelRead("\t", 1, 1, -1, -1, 0);
		LinkedHashSet<String> lsResult = new LinkedHashSet<String>();
		for (String[] strings : lsTmp) {
			lsResult.add(strings[colGeneID]);
		}
		return lsResult;
	}
	
	private void setSeqHash(String fastaFile) {
		seqHash = new SeqFastaHash(fastaFile);
	}
	
	private void writeSeq(Collection<String> lsGeneID, String seqOut)
	{
		TxtReadandWrite txtOut = new TxtReadandWrite(seqOut, true);
		for (String string : lsGeneID) {
			SeqFasta seqFasta = seqHash.getSeqFasta(string);
			if (seqFasta == null) {
				System.out.println(string);
				continue;
			}
			txtOut.writefileln(">" + seqFasta.getSeqName());
			txtOut.writefileln(seqFasta.getSeq());
		}
		txtOut.close();
	}
	
}
