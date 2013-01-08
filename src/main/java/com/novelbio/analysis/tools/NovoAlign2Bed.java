package com.novelbio.analysis.tools;

import java.io.BufferedReader;
import java.io.IOException;

import com.novelbio.analysis.seq.chipseq.PeakMacs;
import com.novelbio.base.PathDetail;
import com.novelbio.base.dataOperate.TxtReadandWrite;

public class NovoAlign2Bed {
	/**
	 * 将novoalign获得的mapping结果转化为标准bed文件和延长bed文件
	 * @throws Exception 
	 */
	public static void copeNovoAlign2Bed(String novoAlignFile, String bedNorm, String bedLen) throws Exception {
		TxtReadandWrite txtNovo = new TxtReadandWrite(novoAlignFile, false);
		TxtReadandWrite txtBedNorm = new TxtReadandWrite(bedNorm, true);
		TxtReadandWrite txtBedLen = new TxtReadandWrite(bedLen, true);
		
		BufferedReader reader = txtNovo.readfile();
		String content = "";
		while ((content = reader.readLine()) != null) {
			String[] ss = content.split("\t");
			if (ss.length < 10) {
				continue;
			}
			int startcode =  Integer.parseInt(ss[8]) ;
			if (ss[9].equals("R")) {
				startcode = startcode -2;
			}
			else {
				startcode = startcode -1;
			}
			
			int endCodeNorm = startcode + ss[2].length();
			int endCodeLen = startcode + 250;
			String strand = "";
			if (ss[9].equals("R")) {
				strand = "-";
			}
			else {
				strand = "+";
			}
			txtBedNorm.writefileln(ss[7].replace(">", "") + "\t" + startcode + "\t" + endCodeNorm + "\t" + ss[11] + "\t" + ss[12] + "\t" +  strand );
			txtBedLen.writefileln(ss[7].replace(">", "") + "\t" + startcode + "\t" + endCodeLen + "\t" + ss[11] + "\t" + ss[12] + "\t" +  strand );
		}
		
		txtBedLen.close();
		txtNovo.close();
		txtBedNorm.close();
		
		
	}
	
	
	
}
