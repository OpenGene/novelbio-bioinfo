package com.novelbio.nbcgui.GUI;

import javax.swing.JPanel;
/**
 * �׻�����ChIP-Seq��RNA-Seq�����Ϸ����Ķ���
 * @author zong0jie
 *
 */
public class GuiReadsAndExpInfo extends JPanel {

	/**
	 * Create the panel.
	 */
	public GuiReadsAndExpInfo() {
		setLayout(null);
		
		GuiLayeredPaneSpeciesVersionGff layeredPane = new GuiLayeredPaneSpeciesVersionGff();
		layeredPane.setBounds(39, 25, 274, 176);
		add(layeredPane);
	}
}
