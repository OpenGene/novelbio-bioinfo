package com.novelbio.analysis.seq.genome.gffOperate;

import java.util.List;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

public class GffHashGeneGBK extends GffHashGeneAbs {

	
	/**
	 * 这里输入的应该是一个文件夹，包含了所有GBK的文件
	 */
	@Override
	protected void ReadGffarrayExcepTmp(String gfffilename) throws Exception {
		List<String> lsGBKfile = FileOperate.getFoldFileNameLs(gfffilename, "*", "*");
		for (String string : lsGBKfile) {
			
			
		}
	}
	
	private void readGBKfile(String gbkFile) {
		TxtReadandWrite	txtRead = new TxtReadandWrite(gbkFile);
		for (String content : txtRead.readlines()) {
			
		}
		
		
		
		
		txtRead.close();
	}
	
}
