package com.paduvi.model;

import java.util.List;

import org.springframework.data.domain.Page;

public class LogPage {

	private List<HealthJob> data;
	private boolean hasMore;
	private int page;
	
	public LogPage(Page<HealthJob> p) {
		this.data = p.getContent();
		this.hasMore = !p.isLast();
		this.page = p.getNumber();
	}

	public List<HealthJob> getData() {
		return data;
	}

	public void setData(List<HealthJob> data) {
		this.data = data;
	}

	public boolean isHasMore() {
		return hasMore;
	}

	public void setHasMore(boolean hasMore) {
		this.hasMore = hasMore;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}
}
