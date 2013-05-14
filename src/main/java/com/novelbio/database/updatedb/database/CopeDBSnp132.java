package com.novelbio.database.updatedb.database;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;

/**
 * 将dbsnp的数据修正，然后用txxt的方法 shujuku 
 * @author zong0jie
 *
 */
public class CopeDBSnp132 {
	public static void main(String[] args) {
		String pareString = "/media/winE/Bioinformatics/snp/snp/";
		String filePathIn = pareString + "snp132.txt.gz";
		String filePathOut = pareString + "snp132out.txt";
		int taxID = 9606;
		
		
		CopeDBSnp132 copeDBSnp132 = new CopeDBSnp132();
		copeDBSnp132.readSnp132(filePathIn, filePathOut, taxID);
	}
	
	
	public void readSnp132(String filePathIn, String filePathOut, int taxID) {
		TxtReadandWrite txtRead = new TxtReadandWrite(filePathIn);
		TxtReadandWrite txtOut = new TxtReadandWrite(filePathOut, true);
		for (String string : txtRead.readlines()) {
			String[] ssout = string.split("\t");
			String[] ss = ArrayOperate.copyArray(ssout, 26);
			for (int i = 0; i < ss.length; i++) {
				if (ss[i] == null) {
					ss[i] = "";
				}
			}
			String out = ss[4] + "\t" + ss[1] + "\t" +ss[2] + "\t" + ss[3] + "\t" + ss[5] + "\t" + ss[6] + "\t" + ss[7] + "\t"
			+ ss[9] + "\t" + ss[10] + "\t" + ss[11] + "\t" + ss[12] + "\t" +ss[13] + "\t" + ss[14] + 
					"\t" + ss[15] + "\t" + ss[16] + "\t" + ss[17] + "\t" + ss[18] + "\t" + ss[19] + "\t" + ss[20]
				+ "\t" + ss[21] + "\t" + ss[22] + "\t" + ss[23] + "\t" + ss[24] + "\t" + ss[25] +"\t" +taxID;
			txtOut.writefileln(out);
		}
		txtRead.close();
		txtOut.close();
	}
}
