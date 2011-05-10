package com.novelbio.analysis.guiRun.Degree;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.novelbio.analysis.coexp.simpCoExp.RunCoExp;
import com.novelbio.analysis.coexp.simpCoExp.SimpCoExp;


public class Control {
	/**
	 * 
	 * @param inFile
	 * @param outFile
	 * @param taxID
	 * @return
	 */
	public static boolean getresult(String inFile, String outFile, int taxID) {
		SimpCoExp simpCoExp = new SimpCoExp();
		try {
			simpCoExp.getCoExpDegreeNormal(inFile, taxID, outFile);
			return true;
		} catch (Exception e) {
			return false;
		}
		
	}
	

}
