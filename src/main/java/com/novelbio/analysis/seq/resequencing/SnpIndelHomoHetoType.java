package com.novelbio.analysis.seq.resequencing;

/** 该样本中SnpIndel的杂合情况，未知，snp杂合，snp纯合，indel杂合，indel纯合 这几种*/
public enum SnpIndelHomoHetoType {
		SnpHomo, SnpHetoMore, SnpHetoMid, SnpHetoLess, IndelHomo, IndelHetoMore, IndelHetoMid, IndelHetoLess, RefHomo, UnKnown;
	}
