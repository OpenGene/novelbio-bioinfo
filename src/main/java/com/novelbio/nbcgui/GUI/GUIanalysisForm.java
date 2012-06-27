package com.novelbio.nbcgui.GUI;
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
public class GUIanalysisForm extends javax.swing.JFrame {
	private static final long serialVersionUID = 6809702573230604814L;
	private JTabbedPane jTabbedPane1;
	private GuiGoJPanel guiGoJPanel;
	private GuiPathJpanel guiPathJpanel;
	private GuiBlastJpanel guiBlastJpanel;
	private GuiSrcToTrgJpanel guiSrcToTrg;
	private GuiDegreeAddJpanel guiDegreeAdd;
	private GuiPearsonJpanel guiPearson;
	private GuiToolsJpanel guiTools;
	private GuiFastQJpanel guiFastQ;
	private GuiLimmaJpanel guiLimma;
	private GuiBlast guiBlast; 
	private GuiSeqMiRNA guiSeqMiRNA;
	private GuiMirnaTargetPredict guiMirnaTargetPredict;
	/**
	* Auto-generated main method to display this JFrame
	*/
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				GUIanalysisForm inst = new GUIanalysisForm();
				inst.setLocationRelativeTo(null);
				inst.setVisible(true);
				inst.setTitle("NovelBio Data Analysis Platform");
				Image im = Toolkit.getDefaultToolkit().getImage("/media/winE/NBC/advertise/Ðû´«/LOGO/favicon.png");
				inst.setIconImage(im);
				inst.setResizable(false);
			}
		});
	}
	
	public GUIanalysisForm() {
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
				
				guiSrcToTrg = new GuiSrcToTrgJpanel();
				jTabbedPane1.addTab("Gene-Act Network", null, guiSrcToTrg, null);
				
			
				guiPearson = new GuiPearsonJpanel();
				jTabbedPane1.addTab("Co-Exp Network", null, guiPearson, null);
				guiDegreeAdd = new GuiDegreeAddJpanel();
				jTabbedPane1.addTab("Network Analysis", null, guiDegreeAdd, null);
				
				guiTools = new GuiToolsJpanel();
				jTabbedPane1.addTab("Tools", null, guiTools, null);
				
				guiFastQ = new GuiFastQJpanel();
				jTabbedPane1.addTab("fastQ", null, guiFastQ, null);
				
				guiLimma = new GuiLimmaJpanel();
				jTabbedPane1.addTab("Microarray", null, guiLimma, null);
				
				guiBlast = new GuiBlast();
				jTabbedPane1.addTab("Blast", null, guiBlast, null);
				
				guiSeqMiRNA = new GuiSeqMiRNA();
				jTabbedPane1.addTab("miRNA", null, guiSeqMiRNA, null);
				
				guiMirnaTargetPredict = new GuiMirnaTargetPredict();
				jTabbedPane1.addTab("miRNAtargetPredict", null, guiMirnaTargetPredict, null);
			}
			pack();
			this.setSize(1049, 699);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

}
