package com.novelbio.analysis.seq.mapping;

import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo;

public class MapBowtie {
	/** Ĭ����bowtie2 */
	public static final int VERSION_BOWTIE2 = 2;
	public static final int VERSION_BOWTIE1 = 1;
	private static final String BOWTIE1 = "bowtie";
	private static final String BOWTIE2 = "bowtie2";
	/** Ĭ��bowtie2 */
	int bowtieVersion = VERSION_BOWTIE2;
	/** ���ȶԵ�Ⱦɫ�� */
	String chrFile = "";
	/** bowtie����·�� */
	String ExePathBowtie = "";
	public MapBowtie() {
		// TODO Auto-generated constructor stub
	}
	public MapBowtie(int bowtieVersion) {
		this.bowtieVersion = bowtieVersion;
	}
	/**
	 * �趨tophat���ڵ��ļ����Լ����ȶԵ�·��
	 * @param exePath ����ڸ�Ŀ¼��������Ϊ""��null
	 * @param chrFile
	 */
	public void setExePath(String exePathBowtie, String chrFile) {
		if (exePathBowtie == null || exePathBowtie.trim().equals(""))
			this.ExePathBowtie = "";
		else
			this.ExePathBowtie = FileOperate.addSep(exePathBowtie);
		this.chrFile = chrFile;
	}
	/** �趨��bowtie����bowtie2 */
	public void setBowtieVersion(int bowtieVersion) {
		this.bowtieVersion = bowtieVersion;
	}
	/**
	 * ��������
	 * �����¶�����Ǹ�MirDeep�õ�
	 */
	public void IndexMakeBowtie() {
		SoftWareInfo softWareInfo = new SoftWareInfo();
//		linux�������� 
//	 	bwa index -p prefix -a algoType -c  chrFile
//		-c ��solid��
		if (bowtieVersion == VERSION_BOWTIE1) {
			if (FileOperate.isFileExist(chrFile + ".3.ebwt") == true)
				return;
		}
		else if (bowtieVersion == VERSION_BOWTIE2) {
			if (FileOperate.isFileExist(chrFile + ".3.bt2") == true)
				return;
		}

		String cmd = "";
		if (bowtieVersion == VERSION_BOWTIE1) {
			softWareInfo.setName(BOWTIE1);
			cmd = softWareInfo.getExePath() + "bowtie-build ";
		}
		else if (bowtieVersion == VERSION_BOWTIE2) {
			softWareInfo.setName(BOWTIE2);
			cmd = softWareInfo.getExePath() + "bowtie2-build ";
		}
		
		//TODO :�����Ƿ��Զ��ж�Ϊsolid
		cmd = cmd + chrFile + " " + chrFile;
		CmdOperate cmdOperate = new CmdOperate(cmd, "bwaMakeIndex");
		cmdOperate.run();
	}
}
