package com.novelbio.analysis.seq.mapping;

import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;

public class MapBowtie {
	/** 默认bowtie2 */
	SoftWare bowtieVersion = SoftWare.bowtie2;
	/** 待比对的染色体 */
	String chrFile = "";
	/** bowtie所在路径 */
	String ExePathBowtie = "";
	public MapBowtie() {
		// TODO Auto-generated constructor stub
	}
	public MapBowtie(SoftWare bowtieVersion) {
		setBowtieVersion(bowtieVersion);
	}
	/**
	 * 设定tophat所在的文件夹以及待比对的路径
	 * @param exePath 如果在根目录下则设置为""或null
	 * @param chrFile
	 */
	public void setExePath(String exePathBowtie, String chrFile) {
		if (exePathBowtie == null || exePathBowtie.trim().equals(""))
			this.ExePathBowtie = "";
		else
			this.ExePathBowtie = FileOperate.addSep(exePathBowtie);
		this.chrFile = chrFile;
	}
	/** 设定是bowtie还是bowtie2 */
	public void setBowtieVersion(SoftWare bowtieVersion) {
		this.bowtieVersion = bowtieVersion;
	}
	/**
	 * 制作索引
	 * 这个暴露出来是给MirDeep用的
	 */
	public void IndexMakeBowtie() {
		SoftWareInfo softWareInfo = new SoftWareInfo();
//		linux命令如下 
//	 	bwa index -p prefix -a algoType -c  chrFile
//		-c 是solid用
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
		
		//TODO :考虑是否自动判断为solid
		cmd = cmd + chrFile + " " + chrFile;
		CmdOperate cmdOperate = new CmdOperate(cmd, "bwaMakeIndex");
		cmdOperate.run();
	}
}
