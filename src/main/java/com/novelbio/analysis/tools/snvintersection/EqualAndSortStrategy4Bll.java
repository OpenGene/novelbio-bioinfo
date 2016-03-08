package com.novelbio.analysis.tools.snvintersection;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;


public class EqualAndSortStrategy4Bll implements SortStrategy, EqualStrategy {

	static private Map<String, Integer> comareMap;
	static {
		comareMap = new HashMap<>();
		String[] keys = new String[] { "chr1", "chr2", "chr3", "chr4", "chr5", "chr6", "chr7", "chr8", "chr9", "chr10",
				"chr11", "chr22", "chr13", "chr14", "chr15", "chr16", "chr17", "chr18", "chr19", "chr20", "chr21",
				"chr22", "chr23", "chr24", "chr25", "chr26", "chr27", "chr28", "chr29", "chr30", "chrMT", "NW_" };
		Integer[] values = new Integer[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21,
				22, 23, 24, 25, 26, 27, 28, 29, 30, 0, 32 };
		for (int i = 0; i < keys.length; i++) {
			comareMap.put(keys[i], values[i]);
		}
	}

	@Override
	public int compare(String o1, String o2) {
		if (StringUtils.isBlank(o1) && StringUtils.isBlank(o2)) {
			return 0;
		} else if (StringUtils.isNotBlank(o1) && StringUtils.isBlank(o2)) {
			return 1;
		} else if (StringUtils.isBlank(o1) && StringUtils.isNotBlank(o2)) {
			return -1;
		}
		String[] split1 = StringUtils.split(o1);
		String[] split2 = StringUtils.split(o2);
		if (split1.length < 6 && split2.length < 6) {
			return 0;
		} else if (split1.length >= 6 && split2.length < 6) {
			return 1;
		} else if (split1.length < 6 && split2.length >= 6) {
			return -1;
		}
		double result;
		try {
			if (comareMap.containsKey(split1[1]) && comareMap.containsKey(split2[1])) {
				result = comareMap.get(split1[1]) - comareMap.get(split2[1]);
				if (result != 0) {
					return (int) Math.signum(result);
				} else {
					result = Double.valueOf(split1[2]) - Double.valueOf(split2[2]);
					if (result != 0) {
						return (int) Math.signum(result);
					} else {
						result = split1[4].compareTo(split2[4]);
						if (result != 0) {
							return (int) Math.signum(result);
						} else {
							result = split1[5].compareTo(split2[5]);
							if (result != 0) {
								return (int) Math.signum(result);
							} else {
								return 0;
							}
						}
					}
				}

			} else if (!comareMap.containsKey(split1[1]) && !comareMap.containsKey(split2[1])) {
				result = Double.valueOf(StringUtils.substring(split1[1], 3))
						- Double.valueOf(StringUtils.substring(split2[1], 3));
				if (result != 0) {
					return (int) Math.signum(result);
				} else {
					result = Double.valueOf(split1[2]) - Double.valueOf(split2[2]);
					if (result != 0) {
						return (int) Math.signum(result);
					} else {
						result = split1[4].compareTo(split2[4]);
						if (result != 0) {
							return (int) Math.signum(result);
						} else {
							result = split1[5].compareTo(split2[5]);
							if (result != 0) {
								return (int) Math.signum(result);
							} else {
								return 0;
							}
						}
					}
				}
			} else {
				if (!comareMap.containsKey(split1[1]) && comareMap.containsKey(split2[1])) {
					return 1;
				} else if (comareMap.containsKey(split1[1]) && !comareMap.containsKey(split2[1])) {
					return -1;
				}
			}
			return 0;
		} catch (Exception e) {
			System.out.println(o1 + "\n" + o2);
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean equals(String o1, String o2) {
		try {
			String[] split1 = StringUtils.split(o1);
			String[] split2 = StringUtils.split(o2);
			return StringUtils.equals(split1[2], split2[2]) && StringUtils.equals(split1[1], split2[1])
					&& StringUtils.equals(split1[4], split2[4]) && StringUtils.equals(split1[5], split2[5]);
		} catch (Exception e) {
			return false;
		}
	}
}