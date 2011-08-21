package com.novelbio.base.cmd;

import java.awt.Dimension;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;

import javax.swing.WindowConstants;
import org.jdesktop.application.Application;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.LayoutStyle;

/**
* This code was edited or generated using CloudGarden's Jigloo
* SWT/Swing GUI Builder, which is free for non-commercial
* use. If Jigloo is being used commercially (ie, by a corporation,
* company or business for any purpose whatever) then you
* should purchase a license for each developer using Jigloo.
* Please visit www.cloudgarden.com for details.
* Use of Jigloo implies acceptance of these licensing terms.
* A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
* THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
* LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
*/
public class GUIcmd extends javax.swing.JPanel {
	private JButton jBtnClose;
	private JScrollPane jSclTxtInfo;
	private JTextArea jTxtInfo;

	/**
	* Auto-generated main method to display this 
	* JPanel inside a new JFrame.
	*/
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.getContentPane().add(new GUIcmd());
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.pack();
		frame.setTitle("System Infomation");
		frame.setVisible(true);
		
	}
	
	public GUIcmd() {
		super();
		initGUI();
	}
	
	private void initGUI() {
		try {
			GroupLayout thisLayout = new GroupLayout((JComponent)this);
			this.setLayout(thisLayout);
			this.setPreferredSize(new java.awt.Dimension(611, 417));
			{
				jBtnClose = new JButton();
				jBtnClose.setName("jBtnClose");
			}
			{
				jSclTxtInfo = new JScrollPane();
				jSclTxtInfo.setName("jSclTxtInfo");
				{
					jTxtInfo = new JTextArea();
					jSclTxtInfo.setViewportView(jTxtInfo);
					jTxtInfo.setPreferredSize(new java.awt.Dimension(607, 374));
					jTxtInfo.setName("jTxtInfo");
				}
			}
			thisLayout.setVerticalGroup(thisLayout.createSequentialGroup()
				.addComponent(jSclTxtInfo, 0, 379, Short.MAX_VALUE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(jBtnClose, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addContainerGap());
			thisLayout.setHorizontalGroup(thisLayout.createParallelGroup()
				.addComponent(jSclTxtInfo, GroupLayout.Alignment.LEADING, 0, 611, Short.MAX_VALUE)
				.addGroup(GroupLayout.Alignment.LEADING, thisLayout.createSequentialGroup()
				    .addGap(234)
				    .addComponent(jBtnClose, GroupLayout.PREFERRED_SIZE, 94, GroupLayout.PREFERRED_SIZE)
				    .addContainerGap(283, Short.MAX_VALUE)));
			Application.getInstance().getContext().getResourceMap(getClass()).injectComponents(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
