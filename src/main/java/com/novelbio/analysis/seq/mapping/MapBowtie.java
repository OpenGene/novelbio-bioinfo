package com.novelbio.analysis.seq.mapping;

import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;

public class MapBowtie {
	/** Ĭ��bowtie2 */
	SoftWare bowtieVersion = SoftWare.bowtie2;
	/** ���ȶԵ�Ⱦɫ�� */
	String chrFile = "";
	/** bowtie����·�� */
	String ExePathBowtie = "";
	public MapBowtie() {
		// TODO Auto-generated constructor stub
	}
	public MapBowtie(SoftWare bowtieVersion) {
		setBowtieVersion(bowtieVersion);
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
	public void setBowtieVersion(SoftWare bowtieVersion) {
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
		if (bowtieVersion == SoftWare.bowtie) {
			if (FileOperate.isFileExist(chrFile + ".3.ebwt") == true)
				return;
		}
		else if (bowtieVersion == SoftWare.bowtie2) {
			if (FileOperate.isFileExist(chrFile + ".3.bt2") == true)
				return;
		}

		String cmd = "";
		softWareInfo.setName(bowtieVersion);
		
		if (bowtieVersion == SoftWare.bowtie) {
			cmd = softWareInfo.getExePath() + "bowtie-build ";
		}
		else if (bowtieVersion == SoftWare.bowtie2) {
			cmd = softWareInfo.getExePath() + "bowtie2-build ";
		}
		
		//TODO :�����Ƿ��Զ��ж�Ϊsolid
		cmd = cmd + chrFile + " " + chrFile;
		CmdOperate cmdOperate = new CmdOperate(cmd, "bwaMakeIndex");
		cmdOperate.run();
	}
}
