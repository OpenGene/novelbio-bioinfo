package com.novelbio.analysis.seq.wig;

import java.io.Serializable;

public class Interval implements Serializable {
	
	private static final long serialVersionUID = 7515817773660876485L;
	
	private String chr;
	private int start;
	private int stop;
	private String id;
	
	public Interval(String chr, int start, int stop) {
		this(chr, start, stop, null);
	}
	
	public Interval(String chr, int start, int stop, String id) {
		this.chr = chr;
		this.start = start;
		this.stop = stop;
		this.id = id;
	}
	
	/**
	 * Parse an Interval from a UCSC-like string of the form "chrV:12-14"
	 * @param s an interval String to parse
	 * @return an Interval object parsed from s
	 * @throws IntervalException if s is not a valid interval string
	 */
	public static Interval parse(String s) throws IntervalException {
		int colon = s.indexOf(':');
		if (colon == -1) {
			throw new IntervalException("Cannot parse invalid interval string " + s);
		}
		
		int dash = s.indexOf('-');
		if (dash == -1) {
			throw new IntervalException("Cannot parse invalid interval string " + s);
		}
		
		try {
			String chr = s.substring(0, colon);
			int start = Integer.parseInt(s.substring(colon+1, dash).replaceAll(",", ""));
			int stop = Integer.parseInt(s.substring(dash+1).replaceAll(",", ""));
			return new Interval(chr, start, stop);
		} catch (NumberFormatException e) {
			throw new IntervalException("Cannot parse invalid interval string " + s);
		}
	}
	
	/**
	 * Return an Interval in Bed format
	 * @return an Interval in Bed format
	 */
	public String toBed() {
		String idStr = (id == null) ? "." : id;
		return chr + "\t" + (low()-1) + "\t" + high() + "\t" + idStr + "\t.\t" + strand();
	}
	
	/**
	 * Return an Interval in BedGraph format
	 * @return an Interval in BedGraph format
	 */
	public String toBedGraph() {
		return chr + "\t" + (low()-1) + "\t" + high();
	}
	
	/**
	 * Return an Interval in GFF format
	 * @return an Interval in GFF format
	 */
	public String toGFF() {
		String idStr = (id == null) ? "no_id" : id;
		return chr + "\tSpotArray\tfeature\t" + low() + "\t" + high() + "\t.\t" + strand() + "\t.\tprobe_id=" + idStr + ";count=1";
	}
	
	/**
	 * Return this Interval as an entry in a file
	 * This method should be overridden by subclasses to produce filetype-specific formats
	 * @return this Interval in an output format
	 */
	public String toOutput() {
		return toString();
	}
	
	/**
	 * The center of this interval, equal to (start+stop)/2
	 * If the interval does not have a perfect center (even length intervals)
	 * then the center is rounded down (floor)
	 * @return the center base pair of this interval
	 */
	public final int center() {
		return (start + stop) / 2;
	}
	
	/**
	 * The length of this interval, equal to high-low+1
	 * @return
	 */
	public final int length() {
		return Math.abs(stop-start) + 1;
	}
	
	/**
	 * If this interval is on the specified chromosome and includes the base pair
	 * @param chr
	 * @param bp
	 * @return
	 */
	public final boolean includes(final String chr, final int bp) {
		return this.chr.equals(chr) && includes(bp);
	}
	
	/**
	 * If this interval includes the given base pair (assumes that it is on the correct chromosome)
	 * @param bp
	 * @return
	 */
	public final boolean includes(final int bp) {
		return low() <= bp && bp <= high();
	}
	
	/**
	 * The lowest genomic coordinate, i.e min { start, stop }
	 * @return start or stop, whichever is lower
	 */
	public final int low() {
		return Math.min(start, stop);
	}
	
	/**
	 * The highest genomic coordinate, i.e max { start, stop }
	 * @return start or stop, whichever is higher
	 */
	public final int high() {
		return Math.max(start, stop);
	}

	/**
	 * If this interval is on the + strand, i.e. stop >= start
	 * @return true if this interval is on the + strand, false otherwise
	 */
	public final boolean isWatson() {
		return stop >= start;
	}
	
	/**
	 * If this interval is on the - strand, i.e. stop < start
	 * @return true if this interval is on the - strand, false otherwise
	 */
	public final boolean isCrick() {
		return !isWatson();
	}
	
	/**
	 * The strand of this Interval, either "+" or "-"
	 * @return "+" if this Interval is Watson, "-" if this Inteval is Crick
	 */
	public final Strand strand() {
		return isWatson() ? Strand.WATSON : Strand.CRICK;
	}
	
	/**
	 * Calculate the intersection of this interval and another interval
	 * Returns null if the chromosome of other is different
	 * The returned interval is always Watson oriented (start <= stop)
	 * @param other an Interval to intersect with this one
	 * @return a new Interval which is contained in this and other
	 */
	public final Interval intersection(final Interval other) {
	  if (other == null || !chr.equals(other.chr)) {
	    // Return an empty interval with our chromosome
	    return null;
	  }
	  
	  int low = Math.max(low(), other.low());
	  int high = Math.min(high(), other.high());
	  // If there is no overlap, return null
	  if (low > high) return null;
	  return new Interval(chr, low, high);
	}
	
	/**
   * Calculate the union of this interval and another interval
   * Union is defined as an interval which covers both intervals
   * The returned interval is always Watson oriented (start <= stop)
   * An error is raised if the other interval does not have the same chromosome
   * @param other an interval to intersect with this one
   * @return a new interval which spans this and other
	 * @throws IntervalException 
   */
  public final Interval union(final Interval other) throws IntervalException {
    if (other == null) {
      return new Interval(chr, low(), high());
    } else if (!chr.equals(other.chr)) {
      throw new IntervalException("Cannot union intervals with different chromosomes"
          +" ("+chr+", "+other.chr+")");
    }
    
    int low = Math.min(low(), other.low());
    int high = Math.max(high(), other.high());
    return new Interval(chr, low, high);
  }
	
	@Override
	public final String toString() {
		return chr + ":" + start + "-" + stop;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((chr == null) ? 0 : chr.hashCode());
		result = prime * result + start;
		result = prime * result + stop;
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Interval))
			return false;
		Interval other = (Interval) obj;
		if (chr == null) {
			if (other.chr != null)
				return false;
		} else if (!chr.equals(other.chr))
			return false;
		if (start != other.start)
			return false;
		if (stop != other.stop)
			return false;
		return true;
	}
	
	public final String getChr() {
		return chr;
	}
	
	public final void setChr(final String chr) {
		this.chr = chr;
	}
	
	public final int getStart() {
		return start;
	}
	
	public final void setStart(final int start) {
		this.start = start;
	}
	
	public final int getStop() {
		return stop;
	}
	
	public final void setStop(final int stop) {
		this.stop = stop;
	}
	
	public final String getId() {
		return id;
	}
	
	public final void setId(final String id) {
		this.id = id;
	}
}
