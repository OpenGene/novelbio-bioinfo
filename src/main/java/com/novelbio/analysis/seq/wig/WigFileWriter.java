package com.novelbio.analysis.seq.wig;

import java.io.Closeable;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import org.apache.log4j.Logger;

import com.novelbio.base.dataOperate.TxtReadandWrite;


/**
 * A class for writing data to Wiggle files in either fixedStep or variableStep format
 * 
 * @author timpalpant
 *
 */
public class WigFileWriter implements Closeable {
	
	private static final Logger log = Logger.getLogger(WigFileWriter.class);
		private final TxtReadandWrite writer;
	
	/**
	 * Construct a new Wiggle file with the given header
	 * @param p the Path to the Wig file
	 * @param header the header for the new Wig file
	 * @throws IOException if a disk write error occurs
	 */
	public WigFileWriter(String path, TrackHeader header) throws IOException {
		writer = new TxtReadandWrite(path, true);
		// Write the header to the output file
		if (header.getType() != TrackHeader.Type.WIGGLE) {
			log.error("Refusing to write track header with type="+header.getType().getId()+" to Wig file");
		} else {
			log.debug("Writing Wig file header: "+header);
			writer.writefileln(header.toString());
		}
	}

	@Override
	public final void close() throws IOException {
		writer.close();
	}
	
	/**
	 * Formats values for writing into Wig files
	 */
	public static DecimalFormat newFormatter() {
		DecimalFormat formatter = new DecimalFormat();
		formatter.setMaximumFractionDigits(8);
		formatter.setGroupingUsed(false);
		DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();
		symbols.setInfinity("Inf");
		symbols.setNaN("NaN");
		formatter.setDecimalFormatSymbols(symbols);
		return formatter;
	}
	
	/**
	 * Add a new Contig of values to this Wig file
	 * The most compact format will automatically be chosen (fixedStep/variableStep) based on sparsity and layout
	 * and it will be written with the largest resolution that still resolves all features in the data
	 * @param contig the Contig of values to write to this Wig file
	 */
	public final void write(final Contig contig) {
		if (contig.coverage() == 0) {
		  log.debug("Not writing empty contig with no data values");
		} else {
			float sparsity = ((float) contig.coverage()) / contig.length();
			if (sparsity < 0.55 || contig.getVariableStepSpan() > contig.getMinStep()) {
				writeVariableStepContig(contig);
			} else {
				writeFixedStepContig(contig);
			}
		}
	}
	
	/**
	 * Add a new Contig of values to this Wig file in fixedStep format
	 * Resolution (step/span) will be automatically chosen so that the output is
	 * as compact as possible while still resolving all features in the data.
	 * @param contig the Contig of values to write to this Wig file
	 * @return the Future corresponding to this Contig's write job
	 */
	public final void writeFixedStepContig(final Contig contig) {
		log.debug("Writing contig: "+contig.getFixedStepHeader());
		DecimalFormat formatter = newFormatter();
		int step = contig.getMinStep();
		synchronized (writer) {
			writer.writefileln(contig.getFixedStepHeader());
			for (int bp = contig.getFirstBaseWithData(); bp <= contig.high(); bp += step) {
				writer.writefileln(formatter.format(contig.get(bp)));
			}
		}
	}
	
	/**
	 * Add a new Contig of values to this Wig file in variableStep format
	 * Resolution (span) will be automatically chosen so that the output is
	 * as compact as possible while still resolving all features in the data.
	 * @param contig the Contig of values to write to this Wig file
	 * @return 
	 */
	public final void writeVariableStepContig(final Contig contig) {
		log.debug("Writing contig: "+contig.getVariableStepHeader());
		DecimalFormat formatter = newFormatter();
		int bp = contig.getFirstBaseWithData();
		int span = contig.getVariableStepSpan();
		synchronized (writer) {
			writer.writefileln(contig.getVariableStepHeader());
			while (bp <= contig.high()) {
				float value = contig.get(bp);
				// Write the value and skip the span size
				if (!Float.isNaN(value)) {
					writer.writefileln(bp+"\t"+formatter.format(value));
					bp += span;
				} else {
					bp++;
				}
			}
		}
	}

}
