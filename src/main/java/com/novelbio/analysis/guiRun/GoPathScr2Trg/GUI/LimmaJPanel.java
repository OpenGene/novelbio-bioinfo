package com.novelbio.analysis.guiRun.GoPathScr2Trg.GUI;

import java.awt.Dimension;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;

import javax.swing.WindowConstants;
import org.jdesktop.application.Application;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
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
public class LimmaJPanel extends javax.swing.JPanel {
	private JScrollPane jScrollPaneRawCelData;
	private JRadioButton jRadGCRMA;
	private JRadioButton jRadRMA;
	private JLabel jLabNormData;
	private JLabel RawData;
	private JButton jBtnNorm;
	private JCheckBox jChkLog;
	private JLabel jLabResult;
	private JLabel jLabelCompare;
	private JButton jBtnOpenNormData;
	private JButton jBtnOpenRawData;
	private JButton jBtnSave;
	private JButton jBtnRun;
	private JScrollPane jScrollPaneResult;
	private JScrollPane jScrollPaneCompare;
	private JScrollPane jScrollPanePreView;

	/**
	* Auto-generated main method to display this 
	* JPanel inside a new JFrame.
	*/
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.getContentPane().add(new LimmaJPanel());
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
	
	public LimmaJPanel() {
		super();
		initGUI();
	}
	
	private void initGUI() {
		try {
			GroupLayout thisLayout = new GroupLayout((JComponent)this);
			this.setLayout(thisLayout);
			this.setPreferredSize(new java.awt.Dimension(1039, 664));
			{
				jScrollPaneRawCelData = new JScrollPane();
			}
			{
				jScrollPanePreView = new JScrollPane();
			}
			{
				jScrollPaneCompare = new JScrollPane();
			}
			{
				jScrollPaneResult = new JScrollPane();
			}
			{
				jBtnRun = new JButton();
				jBtnRun.setName("jBtnRun");
				jBtnRun.setMargin(new java.awt.Insets(1, 1, 1, 1));
			}
			{
				jBtnSave = new JButton();
				jBtnSave.setName("jBtnSave");
				jBtnSave.setMargin(new java.awt.Insets(1, 1, 1, 1));
			}
			{
				jBtnOpenRawData = new JButton();
				jBtnOpenRawData.setName("jBtnOpenRawData");
				jBtnOpenRawData.setMargin(new java.awt.Insets(1, 1, 1, 1));
			}
			{
				jBtnOpenNormData = new JButton();
				jBtnOpenNormData.setName("jBtnOpenNormData");
				jBtnOpenNormData.setMargin(new java.awt.Insets(1, 1, 1, 1));
			}
			{
				jLabNormData = new JLabel();
				jLabNormData.setName("jLabNormData");
			}
			{
				jLabResult = new JLabel();
				jLabResult.setName("jLabResult");
			}
			{
				jChkLog = new JCheckBox();
				jChkLog.setName("jChkLog");
			}
			{
				jRadRMA = new JRadioButton();
				jRadRMA.setName("jRadRMA");
			}
			{
				jRadGCRMA = new JRadioButton();
				jRadGCRMA.setName("jRadGCRMA");
			}
			{
				jLabelCompare = new JLabel();
				jLabelCompare.setName("jLabelCompare");
			}
			{
				jBtnNorm = new JButton();
				jBtnNorm.setName("jBtnNorm");
				jBtnNorm.setMargin(new java.awt.Insets(1, 1, 1, 1));
			}
			{
				RawData = new JLabel();
				RawData.setName("RawData");
			}
			thisLayout.setVerticalGroup(thisLayout.createSequentialGroup()
				.addGap(6)
				.addGroup(thisLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				    .addComponent(jBtnOpenRawData, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				    .addComponent(RawData, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, 17, GroupLayout.PREFERRED_SIZE)
				    .addComponent(jBtnOpenNormData, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				    .addComponent(jLabNormData, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(thisLayout.createParallelGroup()
				    .addGroup(GroupLayout.Alignment.LEADING, thisLayout.createSequentialGroup()
				        .addComponent(jScrollPanePreView, GroupLayout.PREFERRED_SIZE, 227, GroupLayout.PREFERRED_SIZE)
				        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				        .addComponent(jLabResult, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				        .addComponent(jScrollPaneResult, GroupLayout.PREFERRED_SIZE, 341, GroupLayout.PREFERRED_SIZE))
				    .addGroup(GroupLayout.Alignment.LEADING, thisLayout.createSequentialGroup()
				        .addComponent(jScrollPaneRawCelData, GroupLayout.PREFERRED_SIZE, 208, GroupLayout.PREFERRED_SIZE)
				        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				        .addGroup(thisLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				            .addComponent(jBtnNorm, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				            .addComponent(jRadRMA, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				            .addComponent(jRadGCRMA, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
				        .addGroup(thisLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				            .addComponent(jChkLog, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				            .addComponent(jLabelCompare, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				        .addComponent(jScrollPaneCompare, GroupLayout.PREFERRED_SIZE, 328, GroupLayout.PREFERRED_SIZE)))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(thisLayout.createParallelGroup()
				    .addGroup(GroupLayout.Alignment.LEADING, thisLayout.createSequentialGroup()
				        .addComponent(jBtnSave, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				        .addGap(0, 6, Short.MAX_VALUE))
				    .addGroup(GroupLayout.Alignment.LEADING, thisLayout.createSequentialGroup()
				        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				        .addComponent(jBtnRun, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)))
				.addContainerGap());
			thisLayout.setHorizontalGroup(thisLayout.createSequentialGroup()
				.addContainerGap()
				.addGroup(thisLayout.createParallelGroup()
				    .addGroup(GroupLayout.Alignment.LEADING, thisLayout.createSequentialGroup()
				        .addGroup(thisLayout.createParallelGroup()
				            .addComponent(jLabelCompare, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 124, GroupLayout.PREFERRED_SIZE)
				            .addGroup(GroupLayout.Alignment.LEADING, thisLayout.createSequentialGroup()
				                .addComponent(jBtnRun, GroupLayout.PREFERRED_SIZE, 105, GroupLayout.PREFERRED_SIZE)
				                .addGap(19))
				            .addGroup(GroupLayout.Alignment.LEADING, thisLayout.createSequentialGroup()
				                .addComponent(RawData, GroupLayout.PREFERRED_SIZE, 114, GroupLayout.PREFERRED_SIZE)
				                .addGap(10)))
				        .addGap(42)
				        .addComponent(jChkLog, GroupLayout.PREFERRED_SIZE, 131, GroupLayout.PREFERRED_SIZE)
				        .addGap(0, 56, Short.MAX_VALUE))
				    .addGroup(thisLayout.createSequentialGroup()
				        .addComponent(jScrollPaneCompare, GroupLayout.PREFERRED_SIZE, 352, GroupLayout.PREFERRED_SIZE)
				        .addGap(0, 0, Short.MAX_VALUE))
				    .addGroup(GroupLayout.Alignment.LEADING, thisLayout.createSequentialGroup()
				        .addComponent(jRadRMA, GroupLayout.PREFERRED_SIZE, 64, GroupLayout.PREFERRED_SIZE)
				        .addGap(23)
				        .addComponent(jRadGCRMA, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE)
				        .addGap(39)
				        .addGroup(thisLayout.createParallelGroup()
				            .addGroup(thisLayout.createSequentialGroup()
				                .addComponent(jBtnNorm, GroupLayout.PREFERRED_SIZE, 127, GroupLayout.PREFERRED_SIZE)
				                .addGap(0, 0, Short.MAX_VALUE))
				            .addGroup(GroupLayout.Alignment.LEADING, thisLayout.createSequentialGroup()
				                .addPreferredGap(jBtnNorm, jBtnOpenRawData, LayoutStyle.ComponentPlacement.INDENT)
				                .addComponent(jBtnOpenRawData, 0, 115, Short.MAX_VALUE))))
				    .addGroup(thisLayout.createSequentialGroup()
				        .addComponent(jScrollPaneRawCelData, GroupLayout.PREFERRED_SIZE, 352, GroupLayout.PREFERRED_SIZE)
				        .addGap(0, 0, Short.MAX_VALUE)))
				.addGap(17)
				.addGroup(thisLayout.createParallelGroup()
				    .addComponent(jScrollPaneResult, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 650, GroupLayout.PREFERRED_SIZE)
				    .addComponent(jScrollPanePreView, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 650, GroupLayout.PREFERRED_SIZE)
				    .addGroup(thisLayout.createSequentialGroup()
				        .addGroup(thisLayout.createParallelGroup()
				            .addComponent(jLabNormData, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 147, GroupLayout.PREFERRED_SIZE)
				            .addGroup(GroupLayout.Alignment.LEADING, thisLayout.createSequentialGroup()
				                .addComponent(jLabResult, GroupLayout.PREFERRED_SIZE, 103, GroupLayout.PREFERRED_SIZE)
				                .addGap(44)))
				        .addGap(345)
				        .addGroup(thisLayout.createParallelGroup()
				            .addComponent(jBtnOpenNormData, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 158, GroupLayout.PREFERRED_SIZE)
				            .addGroup(GroupLayout.Alignment.LEADING, thisLayout.createSequentialGroup()
				                .addGap(59)
				                .addComponent(jBtnSave, GroupLayout.PREFERRED_SIZE, 95, GroupLayout.PREFERRED_SIZE)
				                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)))))
				.addGap(7));

			Application.getInstance().getContext().getResourceMap(getClass()).injectComponents(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
