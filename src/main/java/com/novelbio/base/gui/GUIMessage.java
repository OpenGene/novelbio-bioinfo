package com.novelbio.base.gui;

import javax.swing.JOptionPane;

import com.novelbio.base.fileOperate.FileOperate;

public class GUIMessage {
	public static void main(String[] args) {
		if (!FileOperate.createFolders("/media/winF/NBC/Project/Project_Invitrogen/sRNA/novelMiRNA/sfe/sssdr/")) {
			JOptionPane.showMessageDialog(null, "cannot create fold", "foldcreaterror",JOptionPane.ERROR_MESSAGE);
		}
//		if (!FileOperate.createFolders(folderPath)) {
//			JOptionPane.showMessageDialog(null, "cannot create fold", "foldcreaterror",JOptionPane.ERROR_MESSAGE);
//		}
		FileOperate.DeleteFolder("/media/winF/NBC/Project/Project_Invitrogen/sRNA/novelMiRNA/sfe");
	}
}
