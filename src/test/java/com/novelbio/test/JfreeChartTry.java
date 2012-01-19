package com.novelbio.test;

import java.awt.Font;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

public class JfreeChartTry extends ApplicationFrame {
	   //���캯��
	   public JfreeChartTry(String s){
	     super(s);
	     setContentPane(createDemoPanel());
	   }
	  
	   public static void main(String[] args){
	     JfreeChartTry fjc = new JfreeChartTry("CityInfoPort��˾��֯�ܹ�ͼ");
	     fjc.pack();
	     RefineryUtilities.centerFrameOnScreen(fjc);
	     fjc.setVisible(true);
	   }
	  
	   //���ɱ�ͼ���ݼ�����
	   public static PieDataset createDataset(){
	     DefaultPieDataset defaultpiedataset = new DefaultPieDataset();
	     defaultpiedataset.setValue("������Ա",10.02D);
	     defaultpiedataset.setValue("�г���Ա",20.23D);
	     defaultpiedataset.setValue("������Ա",60.02D);
	     defaultpiedataset.setValue("OEM��Ա",10.02D);
	     defaultpiedataset.setValue("������Ա",5.11D);
	    
	     return defaultpiedataset;
	   }
	  
	   //����ͼ��������JFreeChart
	   public static JFreeChart createChart(PieDataset piedataset){
	     //����ͼ�����
	     JFreeChart jfreechart = ChartFactory.createPieChart("CityInfoPort��˾��֯�ܹ�ͼ",piedataset,true,true,false);
	     //���ͼ����ʾ����
	     PiePlot pieplot = (PiePlot)jfreechart.getPlot();
	     //����ͼ���ǩ����
	     pieplot.setLabelFont(new Font("SansSerif",Font.BOLD,12));
	     pieplot.setNoDataMessage("No data available");
	     pieplot.setCircular(true);
	     pieplot.setLabelGap(0.01D);//���
	     return jfreechart;
	   }
	  
	   //������ʾͼ������
	   public static JPanel createDemoPanel(){
	     JFreeChart jfreechart = createChart(createDataset());
	     return new ChartPanel(jfreechart);
	   }
	  
	}