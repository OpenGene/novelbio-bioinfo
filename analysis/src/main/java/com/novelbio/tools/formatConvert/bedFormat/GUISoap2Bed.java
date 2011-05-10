package com.novelbio.tools.formatConvert.bedFormat;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.LayoutStyle;

import javax.swing.WindowConstants;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;


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
public class GUISoap2Bed extends javax.swing.JFrame {
	private JButton jBtnOpenFIle;
	private JButton jbtnGetTmpFile;
	private JButton jBtnSavenoFR;
	private JButton jBtnSave;

	/**
	* Auto-generated main method to display this JFrame
	*/
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				GUISoap2Bed inst = new GUISoap2Bed();
				inst.setLocationRelativeTo(null);
				inst.setVisible(true);
			}
		});
	}
	
	public GUISoap2Bed() {
		super();
		initGUI();
	}
	String soap;
	String out;
	private void initGUI() {
		try {
			GroupLayout thisLayout = new GroupLayout((JComponent)getContentPane());
			getContentPane().setLayout(thisLayout);
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			{
				jBtnOpenFIle = new JButton();
				jBtnOpenFIle.setText("SelectSoap");
				jBtnOpenFIle.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						soap = getFileName("soap mapped or bed", "mapped","bed");
				
					}
				});
			}
			{
				jBtnSave = new JButton();
				jBtnSave.setText("Save\u6b63\u8d1f\u94fe");
				jBtnSave.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						out = saveFileName("save bed file","bed");
						String outF = out+"F.bed";
						String outR = out +"R.bed";
						String outError = out + "error.mapping";
						try {
							Soap2Bed.getBed2Macs(soap, outF, outR,outError);
							JOptionPane.showMessageDialog(null, "Finished", "Finished", JOptionPane.INFORMATION_MESSAGE);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							JOptionPane.showMessageDialog(null, "e.printStackTrace()", "error", JOptionPane.INFORMATION_MESSAGE);
							e.printStackTrace();
						}
						
					}
				});
			}
			{
				jBtnSavenoFR = new JButton();
				jBtnSavenoFR.setText("save\u666e\u901a");
				jBtnSavenoFR.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						out = saveFileName("save bed file","bed");
						String outF = out+".bed";
						try {
							Soap2Bed.getBed2Macs(soap, outF);
							JOptionPane.showMessageDialog(null, "Finished", "Finished", JOptionPane.INFORMATION_MESSAGE);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							JOptionPane.showMessageDialog(null, "e.printStackTrace()", "error", JOptionPane.INFORMATION_MESSAGE);
							e.printStackTrace();
						}
					}
				});
			}
			{
				jbtnGetTmpFile = new JButton();
				jbtnGetTmpFile.setText("\u68af\u5ea6\u6587\u672c");
				jbtnGetTmpFile.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						out = saveFileName("BedFile","bed");
						int[] mm = new int[4];
						mm[0] = 60;mm[1] = 70;mm[2] = 80;mm[3] = 90;
						try {
							Soap2Bed.getTiduTxt(soap, mm, out);
							JOptionPane.showMessageDialog(null, "Finished","Finished", JOptionPane.INFORMATION_MESSAGE);
						} catch (Exception e) {
							JOptionPane.showMessageDialog(null, "error", "error", JOptionPane.INFORMATION_MESSAGE);
							e.printStackTrace();
						}
					}
				});
			}
			thisLayout.setVerticalGroup(thisLayout.createSequentialGroup()
				.addContainerGap(40, 40)
				.addGroup(thisLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				    .addComponent(jBtnOpenFIle, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				    .addComponent(jBtnSave, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGap(60)
				.addGroup(thisLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				    .addComponent(jBtnSavenoFR, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				    .addComponent(jbtnGetTmpFile, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
				.addContainerGap(134, 134));
			thisLayout.setHorizontalGroup(thisLayout.createSequentialGroup()
				.addContainerGap(47, 47)
				.addGroup(thisLayout.createParallelGroup()
				    .addGroup(GroupLayout.Alignment.LEADING, thisLayout.createSequentialGroup()
				        .addComponent(jBtnOpenFIle, GroupLayout.PREFERRED_SIZE, 97, GroupLayout.PREFERRED_SIZE)
				        .addGap(20))
				    .addComponent(jBtnSavenoFR, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 117, GroupLayout.PREFERRED_SIZE))
				.addGap(52)
				.addGroup(thisLayout.createParallelGroup()
				    .addComponent(jBtnSave, GroupLayout.Alignment.LEADING, 0, 173, Short.MAX_VALUE)
				    .addGroup(GroupLayout.Alignment.LEADING, thisLayout.createSequentialGroup()
				        .addComponent(jbtnGetTmpFile, GroupLayout.PREFERRED_SIZE, 119, GroupLayout.PREFERRED_SIZE)
				        .addGap(0, 54, Short.MAX_VALUE)))
				.addContainerGap());
			pack();
			setSize(400, 300);
		} catch (Exception e) {
		    //add your error handling code here
			e.printStackTrace();
		}
	}
	
	
	//打开文本选择器
	private String getFileName(String title, String... arg1){
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter(title,arg1);
		chooser.setFileFilter(filter);
		int returnVal = chooser.showOpenDialog(getParent());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			return chooser.getSelectedFile().getAbsolutePath();
		}
		return null;
	}
	//打开文本选择器
	private String saveFileName(String title,String... arg1) {
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("bedFIle", "bed");
		chooser.setFileFilter(filter);
		int returnVal = chooser.showSaveDialog(getParent());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			return chooser.getSelectedFile().getAbsolutePath();
		}
		return null;
	}

}
