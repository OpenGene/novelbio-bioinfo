package com.novelbio.analysis.tools.repeatdel;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;

import javax.swing.WindowConstants;
import org.jdesktop.application.Application;

import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.base.gui.GUIFileOpen;

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
public class NewJFrame extends javax.swing.JFrame {
	private JButton jBtnOpenFile;
	private JTextField jTxtAccIDCol;
	private JTextArea jTxtResCov;
	private JButton jBtnResCov;
	private JButton jBtnRun;
	private JLabel jLabDataCol;
	private JTextField jTxtDataCol;
	private JLabel jLabAccCol;
	String fileName = "";
	/**
	* Auto-generated main method to display this JFrame
	*/
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				NewJFrame inst = new NewJFrame();
				inst.setLocationRelativeTo(null);
				inst.setVisible(true);
			}
		});
	}
	
	public NewJFrame() {
		super();
		initGUI();
	}
	
	private void initGUI() {
		try {
			GroupLayout thisLayout = new GroupLayout((JComponent)getContentPane());
			getContentPane().setLayout(thisLayout);
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			{
				jBtnOpenFile = new JButton();
				jBtnOpenFile.setName("jBtnOpenFile");
				jBtnOpenFile.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						GUIFileOpen guiFileOpen = new GUIFileOpen();
						fileName = guiFileOpen.openFileName("excel 2003", "xls");
					}
				});
			}
			{
				jTxtAccIDCol = new JTextField();
				jTxtAccIDCol.setName("jTxtAccIDCol");
			}
			{
				jLabAccCol = new JLabel();
				jLabAccCol.setName("jLabAccCol");
			}
			{
				jTxtDataCol = new JTextField();
			}
			{
				jLabDataCol = new JLabel();
				jLabDataCol.setName("jLabDataCol");
			}
			{
				jBtnRun = new JButton();
				jBtnRun.setName("jBtnRun");
				jBtnRun.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						GUIFileOpen guiFileOpen = new GUIFileOpen();
						String fileNameout = guiFileOpen.saveFileName("excel 2003", "xls");
						Repeatdel repeatdel = new Repeatdel();
						int colAccID = Integer.parseInt(jTxtAccIDCol.getText());
						String[] dataCol = jTxtDataCol.getText().split("\\D+");
						int[] data = new int[dataCol.length];
						for (int i = 0; i < data.length; i++) {
							data[i] = Integer.parseInt(dataCol[i]);
						}
						repeatdel.getMedian(fileName,colAccID, fileNameout,data);
					}
				});
			}
			{
				jTxtResCov = new JTextArea();
				jTxtResCov.setName("jTxtResCov");
			}
			{
				jBtnResCov = new JButton();
				jBtnResCov.setName("jBtnResCov");
				jBtnResCov.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						String seq = jTxtResCov.getText();
						String seq2 = SeqFasta.reservecom(seq.replace(" ", ""));
						jTxtResCov.setText(seq2);
					}
				});
			}
				thisLayout.setVerticalGroup(thisLayout.createSequentialGroup()
					.addContainerGap(18, 18)
					.addGroup(thisLayout.createParallelGroup()
					    .addGroup(GroupLayout.Alignment.LEADING, thisLayout.createSequentialGroup()
					        .addComponent(jBtnOpenFile, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
					        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
					        .addGroup(thisLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					            .addComponent(jTxtAccIDCol, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					            .addComponent(jLabAccCol, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
					        .addGap(25)
					        .addGroup(thisLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					            .addComponent(jTxtDataCol, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					            .addComponent(jLabDataCol, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
					        .addGap(31))
					    .addGroup(GroupLayout.Alignment.LEADING, thisLayout.createSequentialGroup()
					        .addGap(25)
					        .addComponent(jTxtResCov, 0, 114, Short.MAX_VALUE)))
					.addGap(24)
					.addGroup(thisLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					    .addComponent(jBtnRun, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, 33, GroupLayout.PREFERRED_SIZE)
					    .addComponent(jBtnResCov, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
					.addContainerGap(161, 161));
				thisLayout.setHorizontalGroup(thisLayout.createSequentialGroup()
					.addContainerGap(52, 52)
					.addGroup(thisLayout.createParallelGroup()
					    .addGroup(thisLayout.createSequentialGroup()
					        .addGroup(thisLayout.createParallelGroup()
					            .addComponent(jBtnRun, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 157, GroupLayout.PREFERRED_SIZE)
					            .addGroup(thisLayout.createSequentialGroup()
					                .addPreferredGap(jBtnRun, jLabDataCol, LayoutStyle.ComponentPlacement.INDENT)
					                .addGroup(thisLayout.createParallelGroup()
					                    .addComponent(jLabDataCol, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 110, GroupLayout.PREFERRED_SIZE)
					                    .addComponent(jLabAccCol, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 110, GroupLayout.PREFERRED_SIZE))
					                .addGap(35)))
					        .addGroup(thisLayout.createParallelGroup()
					            .addComponent(jTxtDataCol, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 72, GroupLayout.PREFERRED_SIZE)
					            .addComponent(jTxtAccIDCol, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 72, GroupLayout.PREFERRED_SIZE))
					        .addGap(20))
					    .addComponent(jBtnOpenFile, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 249, GroupLayout.PREFERRED_SIZE))
					.addGap(88)
					.addGroup(thisLayout.createParallelGroup()
					    .addGroup(thisLayout.createSequentialGroup()
					        .addComponent(jTxtResCov, GroupLayout.PREFERRED_SIZE, 268, GroupLayout.PREFERRED_SIZE)
					        .addGap(0, 0, Short.MAX_VALUE))
					    .addGroup(GroupLayout.Alignment.LEADING, thisLayout.createSequentialGroup()
					        .addGap(103)
					        .addComponent(jBtnResCov, GroupLayout.PREFERRED_SIZE, 158, GroupLayout.PREFERRED_SIZE)
					        .addGap(0, 7, Short.MAX_VALUE)))
					.addContainerGap(74, 74));
			pack();
			this.setSize(741, 405);
			Application.getInstance().getContext().getResourceMap(getClass()).injectComponents(getContentPane());
		} catch (Exception e) {
		    //add your error handling code here
			e.printStackTrace();
		}
	}

}
