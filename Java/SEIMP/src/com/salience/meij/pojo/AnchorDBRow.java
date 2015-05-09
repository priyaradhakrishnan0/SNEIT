package com.salience.meij.pojo;

import java.util.List;

public class AnchorDBRow {

	public String anchor;
	public int anchor_freq;
	public int total_freq = 0;
	public List<AnchorPageRow> pages = null;

	public String getAnchor() {
		return anchor;
	}

	public void setAnchor(String anchor) {
		this.anchor = anchor;
	}

	public int getAnchor_freq() {
		return anchor_freq;
	}

	public void setAnchor_freq(int anchor_freq) {
		this.anchor_freq = anchor_freq;
	}

	public int getTotal_freq() {
		return total_freq;
	}

	public void setTotal_freq(int total_freq) {
		this.total_freq = total_freq;
	}

	public List<AnchorPageRow> getPages() {
		return pages;
	}

	public void setPages(List<AnchorPageRow> pages) {
		this.pages = pages;
	}

}
