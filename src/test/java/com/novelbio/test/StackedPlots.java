package com.novelbio.test;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.util.Random;

import de.erichseifert.gral.data.DataTable;
import de.erichseifert.gral.graphics.DrawableContainer;
import de.erichseifert.gral.graphics.TableLayout;
import de.erichseifert.gral.plots.XYPlot;
import de.erichseifert.gral.plots.areas.AreaRenderer;
import de.erichseifert.gral.plots.areas.DefaultAreaRenderer2D;
import de.erichseifert.gral.plots.lines.DefaultLineRenderer2D;
import de.erichseifert.gral.plots.lines.LineRenderer;
import de.erichseifert.gral.plots.points.PointRenderer;
import de.erichseifert.gral.ui.InteractivePanel;
import de.erichseifert.gral.util.GraphicsUtils;
import de.erichseifert.gral.util.Insets2D;

public class StackedPlots extends ExamplePanel {
        /** Version id for serialization. */
        private static final long serialVersionUID = 6832343098989019088L;

        /** Instance to generate random data values. */
        private static final Random random = new Random();

        @SuppressWarnings("unchecked")
        public StackedPlots() {
                // Generate data
                DataTable data = new DataTable(Double.class, Double.class);
                double x=0.0, y=0.0;
                for (x=0.0; x<100.0; x+=2.0) {
                        y += 10.0*random.nextGaussian();
                        data.add(x, Math.abs(y));
                }

                // Create and format upper plot
                XYPlot plotUpper = new XYPlot(data);
                Color colorUpper = COLOR1;
                plotUpper.setPointRenderer(data, null);
//                LineRenderer lineUpper = new DefaultLineRenderer2D();
//                lineUpper.setSetting(LineRenderer.COLOR, colorUpper);
//                plotUpper.setLineRenderer(data, lineUpper);
                AreaRenderer areaUpper = new DefaultAreaRenderer2D();
//                areaUpper.setSetting(AreaRenderer.COLOR, GraphicsUtils.deriveWithAlpha(colorUpper, 64));
                areaUpper.setSetting(AreaRenderer.COLOR, Color.blue);

                plotUpper.setAreaRenderer(data, areaUpper);
                plotUpper.setInsets(new Insets2D.Double(20.0, 50.0, 40.0, 20.0));
                plotUpper.setAxisNotMove(XYPlot.AXIS_Y);
                plotUpper.setAxisNotZoom(XYPlot.AXIS_Y);
                // Create and format lower plot
                XYPlot plotLower = new XYPlot(data);
                Color colorLower = COLOR1;
                PointRenderer pointsLower = plotLower.getPointRenderer(data);
                pointsLower.setSetting(PointRenderer.COLOR, colorLower);
                pointsLower.setSetting(PointRenderer.SHAPE, new Ellipse2D.Double(-3, -3, 6, 6));
                LineRenderer lineLower = new DefaultLineRenderer2D();
                lineLower.setSetting(LineRenderer.STROKE, new BasicStroke(2f));
                lineLower.setSetting(LineRenderer.GAP, 1.0);
                lineLower.setSetting(LineRenderer.COLOR, colorLower);
                plotLower.setLineRenderer(data, lineLower);
                plotLower.setInsets(new Insets2D.Double(20.0, 50.0, 40.0, 20.0));
                plotLower.setAxisNotMove(XYPlot.AXIS_Y);
                plotLower.setAxisNotZoom(XYPlot.AXIS_Y);
                DrawableContainer plots = new DrawableContainer(new TableLayout(1));
                plots.add(plotUpper);
                plots.add(plotLower);

//                 Connect the two plots, i.e. user (mouse) actions affect both plots
                plotUpper.getNavigator().connect(plotLower.getNavigator());

                InteractivePanel panel = new InteractivePanel(plots);
                add(panel);
        }

        @Override
        public String getTitle() {
                return "Stacked plots";
        }
	
	        @Override
	        public String getDescription() {
	                return "An area and a line plot with synchronized actions.";
	        }
	
	        public static void main(String[] args) {
	                new StackedPlots().showInFrame();
	        }
	}