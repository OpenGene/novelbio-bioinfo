package com.novelbio.nbcgui.GUI;

import javax.swing.JButton;
import javax.swing.JProgressBar;

import com.novelbio.base.gui.JScrollPaneData;

/** �н���������ʾ�ı���Ľӿ� */
public interface GuiRunningBarAbs {
	
	public JProgressBar getProcessBar();
	
	public JScrollPaneData getScrollPaneData();
	/** �����ļ� */
	public JButton getBtnOpen();
	/** ���� */
	public JButton getBtnRun();
	/** ���� */
	public JButton getBtnSave();

}
