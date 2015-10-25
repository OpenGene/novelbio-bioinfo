package com.novelbio.analysis.tools.snvintersection;

import java.util.Comparator;

public interface SortStrategy extends Comparator<String> {

	public class DefaultSortStrategy implements SortStrategy {

		@Override
		public int compare(String o1, String o2) {
			if (o1 == null && o2 == null) {
				return 0;
			} else if (o1 == null) {
				return -1;
			} else if (o2 == null) {
				return 1;
			} else {
				return o1.compareTo(o2);
			}
		}
	}
}
