package com.novelbio.analysis.guiRun.GoPathScr2Trg.GUI;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;

import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;

import javax.swing.WindowConstants;
import javax.swing.SwingUtilities;



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
public class GUIanalysis extends javax.swing.JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6809702573230604814L;
	private JTabbedPane jTabbedPane1;
	private GuiGoJPanel guiGoJPanel;
	private GuiPathJpanel guiPathJpanel;
	private GuiBlastJpanel guiBlastJpanel;
	private GuiSrcToTrg guiSrcToTrg;
	private GuiDegreeAdd guiDegreeAdd;
	private GuiPearson guiPearson;
	/**
	* Auto-generated main method to display this JFrame
	*/
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				GUIanalysis inst = new GUIanalysis();
				inst.setLocationRelativeTo(null);
				inst.setVisible(true);
				inst.setTitle("NovelBio Data Analysis Platform");
				Image im = Toolkit.getDefaultToolkit().getImage("/media/winE/NBC/advertise/Ðû´«/LOGO/favicon.png");
				inst.setIconImage(im);
				inst.setResizable(false);
			}
		});
	}
	
	public GUIanalysis() {
		super();
		initGUI();
	}
	
	private void initGUI() {
		try {
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			{
				jTabbedPane1 = new JTabbedPane();
				getContentPane().add(jTabbedPane1, BorderLayout.CENTER);
				jTabbedPane1.setPreferredSize(new java.awt.Dimension(1035, 682));
				guiBlastJpanel = new GuiBlastJpanel();
				jTabbedPane1.addTab("Gene Query", null, guiBlastJpanel, null);
				
				guiGoJPanel= new GuiGoJPanel();
				jTabbedPane1.addTab("GO Analysis", null, guiGoJPanel, null);
				
				guiPathJpanel = new GuiPathJpanel();
				jTabbedPane1.addTab("Pathway Analysis", null, guiPathJpanel, null);
				
				guiSrcToTrg = new GuiSrcToTrg();
				jTabbedPane1.addTab("Gene-Act Network", null, guiSrcToTrg, null);
				
			
				guiPearson = new GuiPearson();
				jTabbedPane1.addTab("Co-Exp Network", null, guiPearson, null);
				guiDegreeAdd = new GuiDegreeAdd();
				jTabbedPane1.addTab("Network Analysis", null, guiDegreeAdd, null);
				
//				{
//					jPanCoExp = new JPanel();
//					jTabbedPane1.addTab("CoExpression", null, jPanCoExp, null);
//				}
//				{
//					jPanSrctrg = new JPanel();
//					jTabbedPane1.addTab("Signal Network", null, jPanSrctrg, null);
//				}
			}
			pack();
			this.setSize(1049, 699);
		} catch (Exception e) {
		    //add your error handling code here
			e.printStackTrace();
		}
	}
	

}
