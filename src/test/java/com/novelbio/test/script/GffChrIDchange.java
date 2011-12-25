package com.novelbio.test.script;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

public class GffChrIDchange {
	public static void main(String[] args) {
		changeChrID("/media/winE/Bioinformatics/GenomeData/maize/ZmB73_5a.59_WGS.gff3");
	}
	public static void changeChrID(String txtFile) {
		TxtReadandWrite txtOut = new TxtReadandWrite(FileOperate.changeFileSuffix(txtFile, "_changeChrID", null), true);
		
		TxtReadandWrite txtRead = new TxtReadandWrite(txtFile, false);
		for (String string : txtRead.readlines()) {
			String[] ss = string.split("\t");
			if (ss[0].contains("Chr")) {
				ss[0]=ss[0].replace("Chr", "chr");
			}
			else if (ss[0].contains("Mt")) {
				ss[0] = ss[0].replace("Mt", "chrmt");
			}
			else if (ss[0].contains("Pt")) {
				ss[0] = ss[0].replace("Mt", "chrmt");
			}
			txtOut.writefileln(ss);
		}
		txtOut.close();
	}
}
