package com.novelbio.analysis.seq.mapping;

public enum MappingReadsType {
		allReads, 
		allMappedReads, unMapped, uniqueMapping, 
		/** 非unique Mapping */
		repeatMapping,
		junctionUniqueMapping, junctionAllMappedReads
}
