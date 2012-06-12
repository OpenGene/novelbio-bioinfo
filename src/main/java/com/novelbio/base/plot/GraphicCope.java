package com.novelbio.base.plot;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class GraphicCope {
	/** */
	/**
	 * ��תͼƬΪָ���Ƕ�
	 * 
	 * @param bufferedimage
	 *            Ŀ��ͼ��
	 * @param degree
	 *            ��ת�Ƕ� ˳ʱ����ת
	 * @return
	 */
	public static BufferedImage rotateImage(final BufferedImage bufferedimage,
			final int degree) {
		int w = bufferedimage.getWidth();
		int h = bufferedimage.getHeight();
		int type = bufferedimage.getColorModel().getTransparency();
		BufferedImage img;
		Graphics2D graphics2d;
		(graphics2d = (img = new BufferedImage(w, h, type)).createGraphics())
				.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
						RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		graphics2d.rotate(Math.toRadians(degree), w / 2, h / 2);
		graphics2d.drawImage(bufferedimage, 0, 0, null);
		graphics2d.dispose();
		return img;
	}
	/**
	 * ���ͼ��Ϊָ����С
	 * @param bufferedimage  Ŀ��ͼ��
	 * @param w  ��
	 * @param h ��
	 * @return
	 */
	public static BufferedImage resizeImage(final BufferedImage bufferedimage,
			final int w, final int h) {
		return resizeImage(true,bufferedimage, w, h);
	}
	/**
	 * ���ͼ��Ϊָ����С
	 * @param liner �Ƿ���������ͼƬ
	 * @param bufferedimage  Ŀ��ͼ��
	 * @param w  ��
	 * @param h ��
	 * @return
	 */
	public static BufferedImage resizeImage(boolean liner, BufferedImage bufferedimage,final int w, final int h) {
		BufferedImage img;
		if (liner) {
			img = new ImageScale().imageZoomOut(bufferedimage, w, h);
		}
		else {
			int type = bufferedimage.getColorModel().getTransparency();
			Graphics2D graphics2d;
			(graphics2d = (img = new BufferedImage(w, h, type)).createGraphics())
					.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
							RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			graphics2d.drawImage(bufferedimage, 0, 0, w, h, 0, 0,
					bufferedimage.getWidth(), bufferedimage.getHeight(), null);
			graphics2d.dispose();
		}
		return img;
	}
	/** */
	/**
	 * ˮƽ��תͼ��
	 * 
	 * @param bufferedimage
	 *            Ŀ��ͼ��
	 * @return
	 */
	public static BufferedImage flipImage(final BufferedImage bufferedimage) {
		int w = bufferedimage.getWidth();
		int h = bufferedimage.getHeight();
		BufferedImage img;
		Graphics2D graphics2d;
		(graphics2d = (img = new BufferedImage(w, h, bufferedimage
				.getColorModel().getTransparency())).createGraphics())
				.drawImage(bufferedimage, 0, 0, w, h, w, 0, 0, h, null);
		graphics2d.dispose();
		return img;
	}

	/**
	 * ͼƬˮӡ
	 * 
	 * @param pressImg
	 *            ˮӡͼƬ
	 * @param targetImg
	 *            Ŀ��ͼƬ
	 * @param x
	 *            ����ֵ Ĭ�����м�
	 * @param y
	 *            ����ֵ Ĭ�����м�
	 * @param alpha
	 *            ͸����
	 */
	public final static void pressImage(String pressImg, String targetImg, float alpha, String out) {
//			int x, int y, float alpha) {
		try {
			File img = new File(targetImg);
			Image src = ImageIO.read(img);
			int wideth = src.getWidth(null);
			int height = src.getHeight(null);
			BufferedImage image = new BufferedImage(wideth, height,
					BufferedImage.TYPE_INT_RGB);
			Graphics2D g = image.createGraphics();
			//��һ��Ҳ͸����
//			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));
			//
			g.drawImage(src, 0, 0, wideth, height, null);
			// ˮӡ�ļ�
			Image src_biao = ImageIO.read(new File(pressImg));
			int wideth_biao = src_biao.getWidth(null);
			int height_biao = src_biao.getHeight(null);
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP,
					alpha));
			g.drawImage(src_biao, (wideth - wideth_biao) / 2,
					(height - height_biao) / 2, wideth_biao, height_biao, null);
			// ˮӡ�ļ�����
			g.dispose();
			
			File fileOut = new File(out);
			
			ImageIO.write((BufferedImage) image, "jpg", fileOut);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * ����ˮӡ
	 * 
	 * @param pressText
	 *            ˮӡ����
	 * @param targetImg
	 *            Ŀ��ͼƬ
	 * @param fontName
	 *            ��������
	 * @param fontStyle
	 *            ������ʽ
	 * @param color
	 *            ������ɫ
	 * @param fontSize
	 *            �����С
	 * @param x
	 *            ����ֵ
	 * @param y
	 *            ����ֵ
	 * @param alpha
	 *            ͸����
	 */
	public static void pressText(String pressText, String targetImg,
			String fontName, int fontStyle, Color color, int fontSize, int x,
			int y, float alpha) {
		try {
			File img = new File(targetImg);
			Image src = ImageIO.read(img);
			int width = src.getWidth(null);
			int height = src.getHeight(null);
			BufferedImage image = new BufferedImage(width, height,
					BufferedImage.TYPE_INT_RGB);
			Graphics2D g = image.createGraphics();
			g.drawImage(src, 0, 0, width, height, null);
			g.setColor(color);
			g.setFont(new Font(fontName, fontStyle, fontSize));
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP,
					alpha));
			g.drawString(pressText, (width - (getLength(pressText) * fontSize))
					/ 2 + x, (height - fontSize) / 2 + y);
			g.dispose();
			ImageIO.write((BufferedImage) image, "jpg", img);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * ����
	 * 
	 * @param filePath
	 *            ͼƬ·��
	 * @param height
	 *            �߶�
	 * @param width
	 *            ���
	 * @param bb
	 *            ��������ʱ�Ƿ���Ҫ����
	 */
	public static void resize(String filePath, int height, int width, boolean bb) {
		try {
			double ratio = 0.0; // ���ű���
			File f = new File(filePath);
			BufferedImage bi = ImageIO.read(f);
			Image itemp = bi.getScaledInstance(width, height, bi.SCALE_SMOOTH);
			// �������
			if ((bi.getHeight() > height) || (bi.getWidth() > width)) {
				if (bi.getHeight() > bi.getWidth()) {
					ratio = (new Integer(height)).doubleValue()
							/ bi.getHeight();
				} else {
					ratio = (new Integer(width)).doubleValue() / bi.getWidth();
				}
				AffineTransformOp op = new AffineTransformOp(
						AffineTransform.getScaleInstance(ratio, ratio), null);
				itemp = op.filter(bi, null);
			}
			if (bb) {
				BufferedImage image = new BufferedImage(width, height,
						BufferedImage.TYPE_INT_RGB);
				Graphics2D g = image.createGraphics();
				g.setColor(Color.white);
				g.fillRect(0, 0, width, height);
				if (width == itemp.getWidth(null))
					g.drawImage(itemp, 0, (height - itemp.getHeight(null)) / 2,
							itemp.getWidth(null), itemp.getHeight(null),
							Color.white, null);
				else
					g.drawImage(itemp, (width - itemp.getWidth(null)) / 2, 0,
							itemp.getWidth(null), itemp.getHeight(null),
							Color.white, null);
				g.dispose();
				itemp = image;
			}
			ImageIO.write((BufferedImage) itemp, "jpg", f);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static int getLength(String text) {
		int length = 0;
		for (int i = 0; i < text.length(); i++) {
			if (new String(text.charAt(i) + "").getBytes().length > 1) {
				length += 2;
			} else {
				length += 1;
			}
		}
		return length / 2;
	}
}


class ImageScale {
    /**
     * ͼƬ�������� try { File fi = new File("c:/image2.jpg"); // ��ͼ�ļ� File fo = new
     * File("c:/imgTest.jpg"); // ��Ҫת������Сͼ�ļ� BufferedImage bis =
     * ImageIO.read(fi); BufferedImage bid = null; bid = new
     * ImageScale().imageZoomOut(bis, 228, 96); ImageIO.write(bid, "jpeg", fo);
     * } catch (Exception e) { e.printStackTrace(); }
     */
    private int width;
    private int height;
    private int scaleWidth;

    double support = (double) 3.0;
    double PI = (double) 3.14159265358978;
    double[] contrib;
    double[] normContrib;
    double[] tmpContrib;
    int startContrib, stopContrib;
    int nDots;
    int nHalfDots;

    /**
     * Start: Use Lanczos filter to replace the original algorithm for image
     * scaling. Lanczos improves quality of the scaled image modify by :blade
     */
    public BufferedImage imageZoomOut(BufferedImage srcBufferImage, int w, int h) {
        width = srcBufferImage.getWidth();
        height = srcBufferImage.getHeight();
        scaleWidth = w;
        if (DetermineResultSize(w, h) == 1) {
            return srcBufferImage;
        }
        CalContrib();
        BufferedImage pbOut = HorizontalFiltering(srcBufferImage, w);
        BufferedImage pbFinalOut = VerticalFiltering(pbOut, h);
        return pbFinalOut;
    }

    private int DetermineResultSize(int w, int h) {
        double scaleH, scaleV;
        scaleH = (double) w / (double) width;
        scaleV = (double) h / (double) height;

        if (scaleH >= 1.0 && scaleV >= 1.0) {
            return 1;
        }
        return 0;
    }

    private double Lanczos(int i, int inWidth, int outWidth, double Support) {
        double x;
        x = (double) i * (double) outWidth / (double) inWidth;
        return Math.sin(x * PI) / (x * PI) * Math.sin(x * PI / Support)
                / (x * PI / Support);

    } // end of Lanczos()

    private void CalContrib() {
        nHalfDots = (int) ((double) width * support / (double) scaleWidth);
        nDots = nHalfDots * 2 + 1;
        try {
            contrib = new double[nDots];
            normContrib = new double[nDots];
            tmpContrib = new double[nDots];
        } catch (Exception e) {
            System.out.println("init   contrib,normContrib,tmpContrib" + e);
        }

        int center = nHalfDots;
        contrib[center] = 1.0;

        double weight = 0.0;
        int i = 0;
        for (i = 1; i <= center; i++) {
            contrib[center + i] = Lanczos(i, width, scaleWidth, support);
            weight += contrib[center + i];
        }

        for (i = center - 1; i >= 0; i--) {
            contrib[i] = contrib[center * 2 - i];
        }

        weight = weight * 2 + 1.0;

        for (i = 0; i <= center; i++) {
            normContrib[i] = contrib[i] / weight;
        }

        for (i = center + 1; i < nDots; i++) {
            normContrib[i] = normContrib[center * 2 - i];
        }
    }

    private void CalTempContrib(int start, int stop) {
        double weight = 0;

        int i = 0;
        for (i = start; i <= stop; i++) {
            weight += contrib[i];
        }

        for (i = start; i <= stop; i++) {
            tmpContrib[i] = contrib[i] / weight;
        }

    } // end of CalTempContrib()

    private int GetRedValue(int rgbValue) {
        int temp = rgbValue & 0x00ff0000;
        return temp >> 16;
    }

    private int GetGreenValue(int rgbValue) {
        int temp = rgbValue & 0x0000ff00;
        return temp >> 8;
    }

    private int GetBlueValue(int rgbValue) {
        return rgbValue & 0x000000ff;
    }

    private int ComRGB(int redValue, int greenValue, int blueValue) {

        return (redValue << 16) + (greenValue << 8) + blueValue;
    }

    private int HorizontalFilter(BufferedImage bufImg, int startX, int stopX,
            int start, int stop, int y, double[] pContrib) {
        double valueRed = 0.0;
        double valueGreen = 0.0;
        double valueBlue = 0.0;
        int valueRGB = 0;
        int i, j;

        for (i = startX, j = start; i <= stopX; i++, j++) {
            valueRGB = bufImg.getRGB(i, y);

            valueRed += GetRedValue(valueRGB) * pContrib[j];
            valueGreen += GetGreenValue(valueRGB) * pContrib[j];
            valueBlue += GetBlueValue(valueRGB) * pContrib[j];
        }

        valueRGB = ComRGB(Clip((int) valueRed), Clip((int) valueGreen),
                Clip((int) valueBlue));
        return valueRGB;

    } // end of HorizontalFilter()

    private BufferedImage HorizontalFiltering(BufferedImage bufImage, int iOutW) {
        int dwInW = bufImage.getWidth();
        int dwInH = bufImage.getHeight();
        int value = 0;
        BufferedImage pbOut = new BufferedImage(iOutW, dwInH,
                BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < iOutW; x++) {

            int startX;
            int start;
            int X = (int) (((double) x) * ((double) dwInW) / ((double) iOutW) + 0.5);
            int y = 0;

            startX = X - nHalfDots;
            if (startX < 0) {
                startX = 0;
                start = nHalfDots - X;
            } else {
                start = 0;
            }

            int stop;
            int stopX = X + nHalfDots;
            if (stopX > (dwInW - 1)) {
                stopX = dwInW - 1;
                stop = nHalfDots + (dwInW - 1 - X);
            } else {
                stop = nHalfDots * 2;
            }

            if (start > 0 || stop < nDots - 1) {
                CalTempContrib(start, stop);
                for (y = 0; y < dwInH; y++) {
                    value = HorizontalFilter(bufImage, startX, stopX, start,
                            stop, y, tmpContrib);
                    pbOut.setRGB(x, y, value);
                }
            } else {
                for (y = 0; y < dwInH; y++) {
                    value = HorizontalFilter(bufImage, startX, stopX, start,
                            stop, y, normContrib);
                    pbOut.setRGB(x, y, value);
                }
            }
        }

        return pbOut;

    } // end of HorizontalFiltering()

    private int VerticalFilter(BufferedImage pbInImage, int startY, int stopY,
            int start, int stop, int x, double[] pContrib) {
        double valueRed = 0.0;
        double valueGreen = 0.0;
        double valueBlue = 0.0;
        int valueRGB = 0;
        int i, j;

        for (i = startY, j = start; i <= stopY; i++, j++) {
            valueRGB = pbInImage.getRGB(x, i);

            valueRed += GetRedValue(valueRGB) * pContrib[j];
            valueGreen += GetGreenValue(valueRGB) * pContrib[j];
            valueBlue += GetBlueValue(valueRGB) * pContrib[j];
        }

        valueRGB = ComRGB(Clip((int) valueRed), Clip((int) valueGreen),
                Clip((int) valueBlue));
        // System.out.println(valueRGB);
        return valueRGB;

    } // end of VerticalFilter()

    private BufferedImage VerticalFiltering(BufferedImage pbImage, int iOutH) {
        int iW = pbImage.getWidth();
        int iH = pbImage.getHeight();
        int value = 0;
        BufferedImage pbOut = new BufferedImage(iW, iOutH,
                BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < iOutH; y++) {

            int startY;
            int start;
            int Y = (int) (((double) y) * ((double) iH) / ((double) iOutH) + 0.5);

            startY = Y - nHalfDots;
            if (startY < 0) {
                startY = 0;
                start = nHalfDots - Y;
            } else {
                start = 0;
            }

            int stop;
            int stopY = Y + nHalfDots;
            if (stopY > (int) (iH - 1)) {
                stopY = iH - 1;
                stop = nHalfDots + (iH - 1 - Y);
            } else {
                stop = nHalfDots * 2;
            }

            if (start > 0 || stop < nDots - 1) {
                CalTempContrib(start, stop);
                for (int x = 0; x < iW; x++) {
                    value = VerticalFilter(pbImage, startY, stopY, start, stop,
                            x, tmpContrib);
                    pbOut.setRGB(x, y, value);
                }
            } else {
                for (int x = 0; x < iW; x++) {
                    value = VerticalFilter(pbImage, startY, stopY, start, stop,
                            x, normContrib);
                    pbOut.setRGB(x, y, value);
                }
            }

        }
        return pbOut;

    } // end of VerticalFiltering()

    int Clip(int x) {
        if (x < 0)
            return 0;
        if (x > 255)
            return 255;
        return x;
    }
}