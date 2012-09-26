package com.novelbio.nbcgui.controlseq;

import java.util.ArrayList;

import com.novelbio.analysis.seq.Align2DGEvalue;
import com.novelbio.analysis.seq.AlignSeq;
import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.FormatSeq;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.species.Species;

public class CtrlDGEgetvalue {
	Align2DGEvalue align2dgEvalue = new Align2DGEvalue();
	
	public void setSpecies(Species species) {
		align2dgEvalue.setSpecies(species);
	}
	/** �趨sam��bam������bed�ļ�
	 * ��Щ�ļ��������Ź����
	 * */
	public void setLsAlignSeq(ArrayList<String[]> lsFile2Prefix, FormatSeq formatSeq, String outFile) {
		ArrayList<AlignSeq> lsAlignSeqs = new ArrayList<AlignSeq>();
		ArrayList<String> lsTitles = new ArrayList<String>();
		for (String[] strings : lsFile2Prefix) {
			if (formatSeq == FormatSeq.BED) {
				lsAlignSeqs.add(new BedSeq(strings[0]));
			}
			else if (formatSeq == FormatSeq.SAM || formatSeq == formatSeq.BAM) {
				lsAlignSeqs.add(new SamFile(strings[0]));
			}
			lsTitles.add(strings[1]);
		}
		if (FileOperate.isFileDirectory(outFile)) {
			outFile = FileOperate.addSep(outFile) + "result.xls";
		}
		align2dgEvalue.setLsAlignSeq(lsAlignSeqs, lsTitles, outFile);
	}
	/** ��������ļ����� */
	public void sort() {
		align2dgEvalue.sort();
	}
	/**
	 * һ��������ж��λ����reads���ǣ�����˵DGE������������һ������Ķ��λ����reads����
	 * ��ѡ��ȫ��reads����ѡ����ߵ��reads
	 * @param allTags
	 */
	public void setAllTags(boolean allTags) {
		align2dgEvalue.setAllTags(allTags);
	}
	/**
	 * �޷��趨compressType
	 * ��bed�ļ�ת����DGE�������Ϣ��ֱ�ӿ�����DEseq������
	 * ���ػ�õĽ���ļ���
	 */
	public String dgeCal() {
		return align2dgEvalue.dgeCal();
	}

}
