/**
 * Copyright Copyright 2010-12 Simon Andrews
 *
 *    This file is part of FastQC.
 *
 *    FastQC is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    FastQC is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with FastQC; if not, write to the Free Software
 *    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package uk.ac.babraham.FastQC.Modules;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;


import uk.ac.babraham.FastQC.Sequence.Sequence;

public interface QCModule {

	public void processSequence(Sequence sequence);

	public String name ();
	
	public String description ();
	
	public void reset ();
	
	public boolean raisesError();
	
	public boolean raisesWarning();
	
	public boolean ignoreFilteredSequences();
	
	/**
	 * 指定宽高画图，拿到bufferedImage<br>
	 * 如果返回为null,可能是需要生成结果表格，请使用getResult()方法
	 * @param width
	 * @param heigth
	 * @return
	 * @throws IOException
	 */
	public BufferedImage getBufferedImage(int width,int heigth);
	
	/**
	 * 返回结果集，一般是生成表格用的<br>
	 * @return
	 */
	public Map<String, String> getResult();
}
