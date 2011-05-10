package com.novelbio.annotation.pathway.kegg;

import java.util.ArrayList;

import com.novelBio.base.dataOperate.TxtReadandWrite;
import com.novelbio.annotation.pathway.kegg.kGpath.QKegPath;
import com.novelbio.annotation.pathway.kegg.kGpath.Scr2Target;
import com.novelbio.annotation.pathway.kegg.prepare.KGprepare;

/**
 * ����geneID�����ظ�gene��keggID
 * @author zong0jie
 *
 */
public class keggID {
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String parentFIle = "/media/winE/NBC/Project/Microarray_QY110318/";

		int QtaxID = 0;
		try {
			String readtxt = parentFIle + "QY-DIFGENEUp.txt";
			String outtxt = parentFIle + "QY-DIFGENEUpKegID.txt";
			getKegID(readtxt, 0, outtxt);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			String readtxt = parentFIle + "QY-DIFGENEDown.txt";
			String outtxt = parentFIle + "QY-DIFGENEDownKegID.txt";
			getKegID(readtxt, 0, outtxt);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	
	
	
	
	
	
	public static void getKegID(String txtAccID,int taxID,String outFile) throws Exception {

		String[] accID = KGprepare.getAccID(txtAccID, 1, 1);
		ArrayList<String> lsKegID = QKegPath.getGeneKegID(accID, taxID);
		TxtReadandWrite txtOut = new TxtReadandWrite();
		txtOut.setParameter(outFile, true, false);
		for (String string : lsKegID) {
			txtOut.writefile(string+"\n", false);
		}
		txtOut.writefile("", true);
	}
}
