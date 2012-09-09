package com.novelbio.test;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

public class JiaodianFrame extends javax.swing.JFrame implements ActionListener {
 /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
private JButton tan;
 private JDialog dialog;

 public static void main(String[] args) {
  SwingUtilities.invokeLater(new Runnable() {
   public void run() {
    JiaodianFrame inst = new JiaodianFrame();
    inst.setLocationRelativeTo(null);
    inst.setVisible(true);
   }
  });
 }
 
 public JiaodianFrame() {
  super();
  initGUI();
 }
 
 private void initGUI() {
  try {
   setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
   {
    tan = new JButton();
    getContentPane().add(tan, BorderLayout.CENTER);
    tan.setText("�´���");
    tan.addActionListener(this);
   }
   {
    //������������Ҫ������ģ̬�ģ�trueΪģ̬��falseΪ��ģ̬��
    dialog=new JDialog(new Frame(),"�´���",true);
    JLabel label=new JLabel("�Ҿ����´���");
    dialog.getContentPane().add(label);
    dialog.setDefaultCloseOperation(dialog.DO_NOTHING_ON_CLOSE);
    dialog.addWindowListener(new WindowAdapter(){

     public void windowClosing(WindowEvent e) {
      dialog.setVisible(false);
     }});
    dialog.setSize(200, 150);
    dialog.setLocationRelativeTo(null);
   }
   pack();
   setSize(400, 300);
  } catch (Exception e) {
   e.printStackTrace();
  }
 }

 public void actionPerformed(ActionEvent e) {
  dialog.setVisible(true);
 }
}