
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.LinearGradientPaint;

import com.novelbio.test.ExamplePanel;

import de.erichseifert.gral.data.DataTable;
import de.erichseifert.gral.plots.BarPlot;
import de.erichseifert.gral.plots.points.PointRenderer;
import de.erichseifert.gral.ui.InteractivePanel;
import de.erichseifert.gral.util.GraphicsUtils;
import de.erichseifert.gral.util.Insets2D;
import de.erichseifert.gral.util.Location;



public class SimpleBarPlot extends ExamplePanel {
        /** Version id for serialization. */
        private static final long serialVersionUID = -2793954497895054530L;

        @SuppressWarnings("unchecked")
        public SimpleBarPlot() {
                // Create example data
                DataTable data = new DataTable(Double.class, Integer.class, String.class);
                data.add(0.1,  1, "January");
                data.add(0.2,  3, "February");
                data.add(0.3, -2, "March");
                data.add(0.4,  6, "April");
                data.add(0.5, -4, "May");
                data.add(0.6,  8, "June");
                data.add(0.7,  9, "July");
                data.add(0.8, 11, "August");

                // Create new bar plot
                BarPlot plot = new BarPlot(data);

                // Format plot
                plot.setInsets(new Insets2D.Double(40.0, 40.0, 40.0, 40.0));
                plot.setSetting(BarPlot.BAR_WIDTH, 0.075);

                // Format bars
                PointRenderer pointRenderer = plot.getPointRenderer(data);
                pointRenderer.setSetting(PointRenderer.COLOR,
                        new LinearGradientPaint(0f,0f, 0f,1f,
                                        new float[] { 0.0f, 1.0f },
                                        new Color[] { COLOR1, GraphicsUtils.deriveBrighter(COLOR1) }
                        )
                );
                pointRenderer.setSetting(BarPlot.BarRenderer.STROKE, new BasicStroke(3f));
                pointRenderer.setSetting(BarPlot.BarRenderer.STROKE_COLOR,
                        new LinearGradientPaint(0f,0f, 0f,1f,
                                        new float[] { 0.0f, 1.0f },
                                        new Color[] { GraphicsUtils.deriveBrighter(COLOR1), COLOR1 }
                        )
                );
                pointRenderer.setSetting(PointRenderer.VALUE_DISPLAYED, true);
                pointRenderer.setSetting(PointRenderer.VALUE_COLUMN, 2);
                pointRenderer.setSetting(PointRenderer.VALUE_LOCATION, Location.CENTER);
                pointRenderer.setSetting(PointRenderer.VALUE_COLOR, GraphicsUtils.deriveDarker(COLOR1));
                pointRenderer.setSetting(PointRenderer.VALUE_FONT,
                        Font.decode(null).deriveFont(Font.BOLD));

                // Add plot to Swing component
                add(new InteractivePanel(plot));
        }

        @Override
        public String getTitle() {
                return "Bar plot";
        }

        @Override
        public String getDescription() {
                return "Bar plot with example data and color gradients";
        }

        public static void main(String[] args) {
	                new SimpleBarPlot().showInFrame();
	        }
	}