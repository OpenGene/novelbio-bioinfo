

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.SpringLayout;
import javax.swing.JButton;

import com.novelbio.base.plot.BarStyle;
import com.novelbio.base.plot.DotStyle;
import com.novelbio.base.plot.JpanelPlot;
import com.novelbio.base.plot.PlotBar;
import com.novelbio.base.plot.PlotScatter;

import de.erichseifert.gral.data.DataTable;
import de.erichseifert.gral.plots.XYPlot;
import de.erichseifert.gral.util.GraphicsUtils;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class GuiPlotTest {

	private JFrame frame;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GuiPlotTest window = new GuiPlotTest();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public GuiPlotTest() {
		initialize();
	}
	
	JpanelPlot plotJpanel = new JpanelPlot();
	JpanelPlot jPanel = new JpanelPlot();
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 1000, 1000);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		SpringLayout springLayout = new SpringLayout();
		frame.getContentPane().setLayout(springLayout);
		
		JButton btnNewButton = new JButton("New button");
		
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
//				DotStyle dotStyle = new DotStyle();
//				dotStyle.setValueVisible(true);
//				dotStyle.setColor(new Color(0, 255, 0, 255));
//				dotStyle.setStyle(DotStyle.STYLE_LINE);
//				dotStyle.setSize(DotStyle.SIZE_B);
//				dotStyle.setValueVisible(false);
//				dotStyle.setColor(DotStyle.getGridentColor(Color.YELLOW, Color.RED));
//				double[] x = new double[50];
//				double[] y = new double[50];
//				for (int i = 0; i < x.length; i++) {
//					x[i] = random.nextGaussian();
//					y[i] = i;
//				}
//				plotBar.addXY(x, y, dotStyle);
//				plotScatter.removeData(dotStyle);
				plotBar.saveToFile("/media/winF/NBC/Project/Project_Invitrogen/test.jpg", 1000, 1000);
			}
		});
		springLayout.putConstraint(SpringLayout.NORTH, btnNewButton, 45, SpringLayout.NORTH, frame.getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, btnNewButton, 29, SpringLayout.WEST, frame.getContentPane());
		frame.getContentPane().add(btnNewButton);
		getXYplot(100);
		jPanel.setPlotNBCInteractive(getBarplot(100));
//		plotJpanel.setPlotNBC(plotScatter);
//		springLayout.putConstraint(SpringLayout.NORTH, plotJpanel, 25, SpringLayout.NORTH, frame.getContentPane());
//		springLayout.putConstraint(SpringLayout.WEST, plotJpanel, 6, SpringLayout.EAST, btnNewButton);
//		springLayout.putConstraint(SpringLayout.SOUTH, plotJpanel, 325, SpringLayout.NORTH, frame.getContentPane());
//		springLayout.putConstraint(SpringLayout.EAST, plotJpanel, 348, SpringLayout.EAST, btnNewButton);
		
		springLayout.putConstraint(SpringLayout.NORTH, jPanel, 25, SpringLayout.NORTH, frame.getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, jPanel, 6, SpringLayout.EAST, btnNewButton);
		springLayout.putConstraint(SpringLayout.SOUTH, jPanel, 800, SpringLayout.NORTH, frame.getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, jPanel, 800, SpringLayout.EAST, btnNewButton);
		
		
		frame.getContentPane().add(jPanel,BorderLayout.CENTER);
	}
	PlotBar plotBar = new PlotBar();
	PlotScatter plotHist = new PlotScatter();
	PlotScatter plotScatter = new PlotScatter();
    /** Instance to generate random data values. */
    private static final Random random = new Random();
	private PlotScatter getXYplot(int num) {
		double[] x = new double[num];
		double[] y = new double[num];
		for (int i = 0; i < x.length; i++) {
			x[i] = random.nextGaussian();
			y[i] = i;
		}
        // Create example data
        Random random = new Random();
        ArrayList<Double> data = new  ArrayList<Double>();
        for (int i = 0; i < 100; i++) {
                data.add(random.nextGaussian());
        }
        
        plotScatter = new PlotScatter();
		plotScatter.setAxisX(-5, 5);
		plotScatter.setAxisY(0, 0.3);
		BarStyle dotStyle2 = new BarStyle();
//		dotStyle2.setBasicStroke(new BasicStroke(2f));
//		dotStyle2.setColor(DotStyle.getGridentColor(Color.red, GraphicsUtils.deriveBrighter(Color.red)));
//		dotStyle2.setEdgeColor(DotStyle.getGridentColor( GraphicsUtils.deriveBrighter(Color.red),Color.red));
//		dotStyle2.setValueVisible(true);
//		dotStyle2.setBarWidth(10);
//		plotScatter.addHistData(data, 15, dotStyle2);
//		plotScatter.mapNum2ChangeX(0, 0, resolution.length, chrLength, interval);
		DotStyle dotStyle = new DotStyle();
		dotStyle.setValueVisible(true);
		dotStyle.setColor(new Color(0, 0, 255, 255));
		dotStyle.setStyle(DotStyle.STYLE_AREA);
		plotScatter.addXY(x, y, dotStyle);
		plotScatter.setAxisX(-10, 10);
		plotScatter.setAxisY(-10, 10);
		plotScatter.setBg(Color.WHITE);
		plotScatter.setTitle( " Reads Density", null);
		plotScatter.setTitleX("Chromosome Length", null, 2);
		plotScatter.setTitleY("Normalized Reads Counts", null, 0.2);
		plotScatter.setPlotareaAll(false);
//		plotScatter.setAxisNotMove(XYPlot.AXIS_Y);
		
//		plotScatter.setInsets(PlotScatter.INSETS_SIZE_ML);
		return plotScatter;
	}
	
	private PlotScatter getHistplot(int num) {
		double[] x = new double[num];
		double[] y = new double[num];
		for (int i = 0; i < x.length; i++) {
			x[i] = random.nextGaussian();
			y[i] = i;
		}
        // Create example data
        Random random = new Random();
        ArrayList<Double> data = new  ArrayList<Double>();
        for (int i = 0; i < 100; i++) {
                data.add(random.nextGaussian());
        }
        
        plotHist = new PlotScatter();
        plotHist.setAxisX(-5, 5);
        plotHist.setAxisY(0, 0.3);
		BarStyle dotStyle2 = new BarStyle();
		dotStyle2.setBasicStroke(new BasicStroke(2f));
		dotStyle2.setColor(DotStyle.getGridentColor(Color.red, GraphicsUtils.deriveBrighter(Color.red)));
		dotStyle2.setColorEdge(DotStyle.getGridentColor( GraphicsUtils.deriveBrighter(Color.red),Color.red));
		dotStyle2.setValueVisible(true);
		dotStyle2.setBarWidth(0.2);
		plotHist.addHistData(data, 15, dotStyle2);
//		plotScatter.mapNum2ChangeX(0, 0, resolution.length, chrLength, interval);
//		plotScatter.setAxisX(-10, 10);
//		plotScatter.setAxisY(-10, 10);
		plotHist.setBg(Color.WHITE);
		plotHist.setTitle( " Reads Density", null);
		plotHist.setTitleX("Chromosome Length", null, 2);
		plotHist.setTitleY("Normalized Reads Counts", null, 0.2);
		plotHist.setPlotareaAll(false);
//		plotScatter.setAxisNotMove(XYPlot.AXIS_Y);
		
		plotHist.setInsets(PlotScatter.INSETS_SIZE_ML);
		return plotHist;
	}
	
	
	private PlotBar getBarplot(int num) {
		plotBar = new PlotBar();
		BarInfo barInfo = new BarInfo(1, 10, "aaa");
		BarInfo barInfo2 = new BarInfo(2, 12, "bbb");
//		PlotBar plotBar = new PlotBar();
		ArrayList<BarInfo> lsBarInfos = new ArrayList<BarInfo>();
		lsBarInfos.add(barInfo); lsBarInfos.add(barInfo2);
		BarStyle barStyle = new BarStyle();
		barStyle.setBarWidth(0.5);
		barStyle.setBasicStroke(new BasicStroke(0.5f));
		barStyle.setColor(BarStyle.getGridentColorBrighter(Color.BLUE));
		barStyle.setColorEdge(BarStyle.getGridentColorDarker(Color.BLUE));
		plotBar.addBarPlot(lsBarInfos, barStyle);
		plotBar.setAxisX(0, 3);
		plotBar.setAxisY(0, 20);
		plotBar.setBg(Color.WHITE);
		plotBar.setInsets(PlotScatter.INSETS_SIZE_ML);
		plotBar.setBg(Color.WHITE);
		return plotBar;
	}
	
}
