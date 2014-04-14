package com.novelbio.listOperate;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarPainter;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RectangleInsets;

import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.plot.BarStyle;
import com.novelbio.base.plot.DotStyle;
import com.novelbio.base.plot.PlotBar;
import com.novelbio.base.plot.PlotScatter;
import com.novelbio.base.plot.PlotBox.BoxInfo;

public abstract class HistList extends ListAbsSearch<HistBin, ListCodAbs<HistBin>, ListCodAbsDu<HistBin,ListCodAbs<HistBin>>> {
	private static final Logger logger = Logger.getLogger(HistList.class);
	private static final long serialVersionUID = 1481673037539688125L;
	
	/** 总共多少数字 */
	long allNum = 0;
	HistBinType histBinType = HistBinType.LopenRclose;
	
	PlotScatter plotScatter;
	
	/**
	 * 默认是左开右闭
	 * @param histBinType
	 */
	public void setHistBinType(HistBinType histBinType) {
		this.histBinType = histBinType;
	}
	
	@Override
	protected ListCodAbs<HistBin> creatGffCod(String listName, int Coordinate) {
		ListCodAbs<HistBin> lsAbs = new ListCodAbs<HistBin>(listName, Coordinate);
		return lsAbs;
	}

	@Override
	protected ListCodAbsDu<HistBin, ListCodAbs<HistBin>> creatGffCodDu(
			ListCodAbs<HistBin> gffCod1, ListCodAbs<HistBin> gffCod2) {
		ListCodAbsDu<HistBin, ListCodAbs<HistBin>> lsResult= new ListCodAbsDu<HistBin, ListCodAbs<HistBin>>(gffCod1, gffCod2);
		return lsResult;
	}
	
	/**
	 * 获得的每一个信息都是实际的而没有clone
	 * 输入PeakNum，和单条Chr的list信息 返回该PeakNum的所在LOCID，和具体位置
	 * 采用clone的方法获得信息
	 * 没找到就返回null
	 */
	@Deprecated
	public ListCodAbs<HistBin> searchLocation(int Coordinate) {
		return super.searchLocation(Coordinate);
	}
	@Deprecated
	public boolean add(HistBin e) {
		// TODO Auto-generated method stub
		return super.add(e);
	}
	/**
	 * 返回双坐标查询的结果，内部自动判断 cod1 和 cod2的大小
	 * 如果cod1 和cod2 有一个小于0，那么坐标不存在，则返回null
	 * @param chrID 内部自动小写
	 * @param cod1 必须大于0
	 * @param cod2 必须大于0
	 * @return
	 */
	@Deprecated
	public ListCodAbsDu<HistBin, ListCodAbs<HistBin>> searchLocationDu(int cod1, int cod2) {
		return super.searchLocationDu(cod1, cod2);
	}
	
	public void setPlotScatter(PlotScatter plotScatter) {
		this.plotScatter = plotScatter;
	}
	
	/**
	 * 自动设置histlist的bin，从0开始，每隔interval设置一位，名字就起interval
	 * @param histList
	 * @param binNum bin的个数
	 * @param interval 间隔
	 * @param maxSize 最大值，如果最后一位bin都没到最大值，接下来一个bin就和最大值合并
	 */
	public void setBinAndInterval(int binNum, int interval,int maxSize) {
		clear();
		setStartBin(interval, interval + "", 0, interval);
		int binNext = interval*2;
		for (int i = 1; i < binNum; i++) {
			addHistBin(binNext, binNext + "", binNext);
			binNext = binNext + interval;
		}
		if (binNext < maxSize) {
			addHistBin(binNext, binNext + "", maxSize);
		}
	}
	
	/**
	 * 设置起点
	 * @param number 本bin所代表的数值，null就用终点和起点的平均值
	 * @param name
	 * @param start
	 * @param end
	 */
	public void setStartBin(Integer number, String name, int start, int end) {
		setStartBin(number.doubleValue(), name, start, end);
	}
	/**
	 * 设置起点
	 * @param number 本bin所代表的数值，null就用终点和起点的平均值
	 * @param name
	 * @param start
	 * @param end
	 */
	public void setStartBin(Double number, String name, int start, int end) {
		HistBin histBinThis = new HistBin(number);
		histBinThis.setStartCis(start);
		histBinThis.setEndCis(end);
		histBinThis.addItemName(name);
		add(histBinThis);
	}

	/**
	 * 在此之前必须先设定起点{@link #setStartBin}
	 * 添加hist区间，必须是紧挨着设定，
	 * 意思本区间为上一个num和本num之间
	 * @param number 本bin所代表的数值，null就用终点和起点的平均值
	 * @param name 填写的话，就用该名字做坐标名字
	 * @param thisNum
	 */
	public void addHistBin(Integer number, String name, int thisNum) {
		addHistBin(number.doubleValue(), name, thisNum);
	}
	/**
	 * 在此之前必须先设定起点{@link #setStartBin}
	 * 添加hist区间，必须是紧挨着设定，
	 * 意思本区间为上一个num和本num之间
	 * @param number 本bin所代表的数值，null就用终点和起点的平均值
	 * @param name
	 * @param thisNum 本bin的终点
	 */
	public void addHistBin(Double number, String name, int thisNum) {
		HistBin histBinLast = get(size() - 1);
		histBinLast.getEndCis();
		HistBin histBinThis = new HistBin(number);
		histBinThis.addItemName(name);
		histBinThis.setStartCis(histBinLast.getEndCis());
		histBinThis.setEndCis(thisNum);
		histBinThis.setParentName(getName());
		add(histBinThis);
	}
	
	/**
	 * 查找 coordinate，根据 HistBinType 返回相应的histbin
	 * @param coordinate
	 * @return
	 */
	public abstract HistBin searchHistBin(int coordinate);
	/**
	 * 给定number，把相应的hist加上1
	 * @param coordinate
	 */
	public void addNum(int coordinate) {
		addNum(coordinate, 1);
	}
	/**
	 * 给定number，把相应的hist加上addNumber的数量
	 * @param coordinate
	 */
	public void addNum(int coordinate, int addNumber) {
		HistBin histBin = searchHistBin(coordinate);
		histBin.addNumber(addNumber);
		allNum = allNum + addNumber;
	}
	
	/**
	 * 返回BoxInfo<br>
	 * @return
	 */
	public BoxInfo getBoxInfo() {
		BoxInfo boxInfo = new BoxInfo(getName());
		boxInfo.setInfo25And75(getPercentInfo(25).getThisNumber(), getPercentInfo(75).getThisNumber());
		boxInfo.setInfoMedian(getPercentInfo(50).getThisNumber());
		boxInfo.setInfoMinAndMax(getPercentInfo(1).getThisNumber(), getPercentInfo(99).getThisNumber());
		boxInfo.setInfo5And95(getPercentInfo(5).getThisNumber(), getPercentInfo(95).getThisNumber());
		return boxInfo;
	}
	/** 指定percentage乘以100
	 * 返回该比例所对应的值
	 */
	private HistBin getPercentInfo(int percentage) {
		long thisNumThreshold = (long) ((double)percentage/100 * allNum);
		long thisNum = 0;
		
		for (HistBin histBin : this) {
			thisNum = thisNum + histBin.getCountNumber();
			if (thisNum >= thisNumThreshold) {
				return histBin;
			}
		}
		//全找了一遍没找到么说明数字太大了那就返回最后一位的HistBin吧
		return get(size() - 1);		
	}
	
	/**
	 * 根据统计画直方图
	 * @param dotStyle
	 * @param fontSize 字体大小
	 * @return
	 */
	public PlotScatter getPlotHistBar(BarStyle dotStyle) {
		double[] Ycount = getYnumber(0);
		double[] Xrange = getX();
		String[] xName = getRangeX();
		HashMap<Double, String> mapX2Name = new HashMap<Double, String>();
		for (int i = 0; i < xName.length; i++) {
			HistBin histBin = get(i);
			if (histBin.getNameSingle() == null || histBin.getNameSingle().trim().equals("")) {
				mapX2Name.put(Xrange[i], xName[i]);
			} else {
				mapX2Name.put(Xrange[i], histBin.getNameSingle());
			}
		}
		if (plotScatter == null) {
			plotScatter = new PlotScatter(PlotScatter.PLOT_TYPE_BARPLOT);
		}
		double minY = MathComput.min(Ycount)/3;
		double maxY = MathComput.max(Ycount);

		if (dotStyle.getBarWidth() == 0 && Xrange.length > 1) {
			dotStyle.setBarAndStrokeWidth(Xrange[1] - Xrange[0]);
		}
		
		plotScatter.setAxisX(Xrange[0] - 1, Xrange[Xrange.length - 1] + 1);
		plotScatter.setAxisY(minY, maxY * 1.2);
		plotScatter.addXY(Xrange, Ycount, dotStyle);
		plotScatter.setAxisTicksXMap(mapX2Name);
//		plotScatter.setAxisTicksXMap(mapX2Name);
		return plotScatter;
	}
	
	
	public JFreeChart getPlotHistBar(String title,String xTitle,String yTitle){
		double[] Ycount = getYnumber(0);
		String[] xName = getRangeX();
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		try {
			for (int i = 0; i < Ycount.length; i++) {
				dataset.addValue(Ycount[i], null,xName[i]);
			}
		} catch (Exception e) {
		}
		JFreeChart chart = ChartFactory.createBarChart(title, xTitle, yTitle, dataset, PlotOrientation.HORIZONTAL, false, false, false);
		// chart.getRenderingHints().put(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		// 设置图标题的字体
		chart.getTitle().setFont(new Font("黑体", Font.BOLD, 30));
		/** title永远是居中的，但是我们想要让title靠上或者靠边怎么办呢，
		 * 就要将title包装成一个矩形，然后jfreechart会将这个矩形居中
		 * 所以第一个就是矩形的上边，这样上边设置越大，title与上边框的距离就越大
		 * 第二个是左边，左边设置越大，title与左边界的距离也就越大
		 * 第三个是下边，下边越大，title与下边图片的距离也越大
		 */
		chart.getTitle().setPadding(20,0,20,0);
		// TextTitle title = new TextTitle("直方图测试");
		// 设置图例中的字体
		// LegendTitle legend = chart.getLegend();
		// legend.setItemFont(new Font("宋体", Font.BOLD, 16));
		// chart.setBorderPaint(Color.white);
		chart.setBorderVisible(true);
		// chart.setBackgroundPaint(Color.WHITE);
		CategoryPlot plot = (CategoryPlot) chart.getPlot();
		plot.setBackgroundPaint(Color.white);
		plot.setOutlinePaint(Color.WHITE); // 设置绘图面板外边的填充颜色
		// CategoryPlot plot = (CategoryPlot) chart.getCategoryPlot();
		// plot.setRenderer(render);//使用我们设计的效果
		BarRenderer renderer = new BarRenderer();
		renderer.setBaseFillPaint(Color.pink);
		plot.setRenderer(renderer);
		
		// 设置柱子宽度
		renderer.setMaximumBarWidth(0.03);
		renderer.setMinimumBarLength(0.01000000000000001D); // 宽度
		// 设置柱子高度
		renderer.setMinimumBarLength(0.1);
		// 设置柱子类型
		BarPainter barPainter = new StandardBarPainter();
		renderer.setBarPainter(barPainter);
//		renderer.setSeriesPaint(0, new Color(51, 102, 153));
		// 是否显示阴影
		renderer.setShadowVisible(false);
		// 阴影颜色
		// renderer1.setShadowPaint(Color.white);
		// 设置柱子边框的渐变色
		// renderer1.setBarPainter(new GradientBarPainter(1,1,1));
		// 设置柱子边框颜色
		// renderer1.setBaseOutlinePaint(Color.BLACK);
		// 设置柱子边框可见
		// renderer1.setDrawBarOutline(true);
		// 设置每个地区所包含的平行柱的之间距离，数值越大则间隔越大，图片大小一定的情况下会影响柱子的宽度，可以为负数
		renderer.setItemMargin(0.4);

		plot.setRenderer(renderer);

		// 设置横轴的标题
		// cateaxis.setLabelFont(new Font("粗体", Font.BOLD, 16));
		// 设置横轴的标尺
		CategoryAxis cateaxis = (CategoryAxis) plot.getDomainAxis();
		cateaxis.setTickLabelFont(new Font(Font.SERIF, Font.BOLD, 22));
		cateaxis.setMaximumCategoryLabelWidthRatio(0.45f);
		// 让标尺以30度倾斜
//		cateaxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 3.0));
		// 纵轴
		NumberAxis numaxis = (NumberAxis) plot.getRangeAxis();
		numaxis.setTickUnit(new NumberTickUnit(PlotBar.getSpace(numaxis.getRange().getUpperBound(), 10)));
		numaxis.setLabelFont(new Font("宋体", Font.BOLD, 25));
		numaxis.setLabelInsets(new RectangleInsets(0, 500, 10, 0));
		return chart;
	}
	/**
	 * 返回x的数值，从0开始
	 * @return
	 */
	private double[] getX() {
		double[] lengthX = new double[size()];
		for (int j = 0; j < lengthX.length; j++) {
			lengthX[j] = j;
		}
		return lengthX;
	}
	/**
	 * 返回y的数值，注意初始的HistBin必须为等分，否则会出错
	 * @binNum 分割的份数，小于等于0表示分割为histlist的份数
	 * @return
	 */
	private double[] getYnumber(int binNum) {
		if (binNum <= 0) {
			binNum = size();
		}
		
		double[] numberY = new double[size()];
		int i = 0;
		for (HistBin histBin : this) {
			numberY[i] = histBin.getCountNumber();;
			i++;
		}
		
		if (binNum != size()) {
			numberY = MathComput.mySpline(numberY, binNum, 0, 0, 0);
		}
		
		return numberY;
	}
	/**
	 * 返回x的区间的名字
	 * @return
	 */
	private String[] getRangeX() {
		String[] rangeX = new String[size()];
		int i = 0;
		for (HistBin histBin : this) {
			rangeX[i] = histBin.getStartCis() + "_" + histBin.getEndCis();
			i++;
		}
		return rangeX;
	}
	
	public PlotScatter getIntegralPlot(boolean cis, DotStyle dotStyle) {
		ArrayList<double[]> lsXY = getIntegral(cis);
		PlotScatter plotScatter = null;
		if (dotStyle.getStyle() == DotStyle.STYLE_BAR || dotStyle.getStyle() == DotStyle.STYLE_BOX) {
			plotScatter = new PlotScatter(PlotScatter.PLOT_TYPE_BARPLOT);
		} else {
			plotScatter = new PlotScatter(PlotScatter.PLOT_TYPE_SCATTERPLOT);
		}
		
		plotScatter.addXY(lsXY, dotStyle);
		plotScatter.setAxisX(get(0).getStartAbs(), get(size() - 1).getStartAbs());
		plotScatter.setAxisY(0, 1);
		return plotScatter;
	}
	
	/**
	 * 积分图
	 * @param cis true：从前往后，就是最前面是10%，越往后越高
	 * false：从后往前，就是最前面是100%，越往后越低
	 */
	public ArrayList<double[]> getIntegral(boolean cis) {
		ArrayList<double[]> lsXY = new ArrayList<double[]>();
		double thisNum = 0;
		double[] x = new double[size()];
		double[] y = new double[size()];
		if (cis) {
			for (int count = 0; count < size(); count++) {
				HistBin histBin = get(count);
				thisNum = thisNum + histBin.getCountNumber();
				x[count] = histBin.getThisNumber();
				y[count] = thisNum/allNum;
			}
		} else {
			for (int count = size() - 1; count >= 0; count--) {
				HistBin histBin = get(count);
				thisNum = thisNum + histBin.getCountNumber();
				x[count] = histBin.getThisNumber();
				y[count] = thisNum/allNum;
			}
		}
		for (int i = 0; i < x.length; i++) {
			double[] xy = new double[2];
			xy[0] = x[i];
			xy[1] = y[i];
			lsXY.add(xy);
		}
 		return lsXY;
	}

	/**
	 * 积分值
	 * @param cis true：从前往后，就是最前面是10%，越往后越高
	 * false：从后往前，就是最前面是100%，越往后越低
	 * @reture 0: 求和 1:积分的prop
	 */
	public double[] getIntegral(int Num, boolean cis) {
		double thisNum = 0;
		double thisIntergralProp = 0;
		if (cis) {
			for (int count = 0; count < size(); count++) {
				HistBin histBin = get(count);
				//TODO 没有考虑左开右闭和左闭右开
				if (histBin.getStartAbs() >= Num) {
					break;
				}
				thisNum = thisNum + histBin.getCountNumber();
			}
			thisIntergralProp = thisNum/allNum;
		} else {
			for (int count = size() - 1; count >= 0; count--) {
				HistBin histBin = get(count);
				if (histBin.getEndAbs() < Num) {
					break;
				}
				thisNum = thisNum + histBin.getCountNumber();
				thisIntergralProp = thisNum/allNum;
			}
		}
 		return new double[]{thisNum, thisIntergralProp};
	}
	
	/**
	 * @param name hist的名字，务必不能重复，否则hash表会有冲突
	 * @param cisList true 从小到大排序的list。 false 从大到小排序的list
	 * @return
	 */
	public static HistList creatHistList(String name, boolean cisList){
		if (cisList) {
			return new HistListCis(name);
		} else {
			return new HistListTrans(name);
		}
	}
	
	public static enum HistBinType {
		LcloseRopen, LopenRclose
	}
	
}

class HistListCis extends HistList {
	private static final Logger logger = Logger.getLogger(HistListCis.class);
	private static final long serialVersionUID = -4966352009491903291L;
	
	public HistListCis(String histName) {
		setName(histName);
	}
	
	/**
	 * 查找 coordinate，根据 HistBinType 返回相应的histbin
	 * @param coordinate
	 * @return
	 */
	public HistBin searchHistBin(int coordinate) {
		ListCodAbs<HistBin> lsHistBin = searchLocation(coordinate);
		HistBin histThis = lsHistBin.getGffDetailThis();
		HistBin histLast = lsHistBin.getGffDetailUp();
		HistBin histNext = lsHistBin.getGffDetailDown();
		
		HistBin resultBin = histThis;
		
		if (histThis == null) {
			HistBin histbin = null;
			if (histLast != null) {
				histbin = histLast;
			} else if (histNext != null) {
				histbin = histNext;
			}
			return histbin;
		}
		
		if (histBinType == HistBinType.LcloseRopen) {
			if ((coordinate >= histThis.getStartCis() && coordinate < histThis.getEndCis())
					||
				(histLast == null && coordinate < histThis.getStartCis() )
					||
				(histNext == null && coordinate >= histThis.getEndCis() )
			) {
				resultBin = histThis;
			} else if (coordinate < histThis.getStartCis() && coordinate >= histLast.getEndCis()) {
				resultBin = histLast;
			} else if (coordinate >= histThis.getEndCis() && coordinate <= histNext.getStartCis()) {
				resultBin = histNext;
			}
		} else if (histBinType == HistBinType.LopenRclose) {
			if ((coordinate > histThis.getStartCis() && coordinate <= histThis.getEndCis())
				||
			(histLast == null && coordinate <= histThis.getStartCis())
				||
			(histNext == null && coordinate > histThis.getEndCis())	
					) {
				resultBin = histThis;
			} else if (coordinate <= histThis.getStartCis() && coordinate >= histLast.getEndCis()) {
				resultBin = histLast;
			} else if (coordinate > histThis.getEndCis() && coordinate <= histNext.getStartCis()) {
				resultBin = histNext;
			}
		}
		return resultBin;
	}

}

class HistListTrans extends HistList {
	private static final Logger logger = Logger.getLogger(HistListTrans.class);
	private static final long serialVersionUID = -5310222125261004172L;
	
	public HistListTrans(String name) {
		setName(name);
	}
	/**
	 * 查找 coordinate，根据 HistBinType 返回相应的histbin
	 * @param coordinate
	 * @return
	 */
	public HistBin searchHistBin(int coordinate) {
		ListCodAbs<HistBin> lsHistBin = searchLocation(coordinate);
		HistBin histThis = lsHistBin.getGffDetailThis();
		HistBin histLast = lsHistBin.getGffDetailUp();
		HistBin histNext = lsHistBin.getGffDetailDown();
		
		HistBin resultBin = histThis;
		if (histBinType == HistBinType.LcloseRopen) {
			if (coordinate <= histThis.getStartCis() && coordinate > histThis.getEndCis()) {
				resultBin = histThis;
			} else if (coordinate > histThis.getStartCis() && coordinate <= histLast.getEndCis()) {
				resultBin = histLast;
			} else if (coordinate <= histThis.getEndCis() && coordinate >= histNext.getStartCis()) {
				resultBin = histNext;
			}
		} else if (histBinType == HistBinType.LopenRclose) {
			if (coordinate > histThis.getStartCis() && coordinate <= histThis.getEndCis()) {
				resultBin = histThis;
			} else if (coordinate <= histThis.getStartCis() && coordinate >= histLast.getEndCis()) {
				resultBin = histLast;
			} else if (coordinate > histThis.getEndCis() && coordinate <= histNext.getStartCis()) {
				resultBin = histNext;
			}
		}
		return resultBin;
	}
	
}
