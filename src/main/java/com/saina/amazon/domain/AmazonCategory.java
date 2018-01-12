package com.saina.amazon.domain;

public class AmazonCategory {

	private String categoryId;
	private String categoryName;
	private String aliasName;
	private int level;
	private String parentCategoryId;
	private String parentCategoryName;
	private String site;
	private String fullPath;

	public String getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public String getAliasName() {
		return aliasName;
	}

	public void setAliasName(String aliasName) {
		this.aliasName = aliasName;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public String getParentCategoryId() {
		return parentCategoryId;
	}

	public void setParentCategoryId(String parentCategoryId) {
		this.parentCategoryId = parentCategoryId;
	}

	public String getParentCategoryName() {
		return parentCategoryName;
	}

	public void setParentCategoryName(String parentCategoryName) {
		this.parentCategoryName = parentCategoryName;
	}

	public String getSite() {
		return site;
	}

	public void setSite(String site) {
		this.site = site;
	}

	public String getFullPath() {
		return fullPath;
	}

	public void setFullPath(String fullPath) {
		this.fullPath = fullPath;
	}

	public AmazonCategory(String categoryId, String categoryName, String aliasName, int level, String parentCategoryId, String parentCategoryName, String site, String fullPath) {
		super();
		this.categoryId = categoryId;
		this.categoryName = categoryName;
		this.aliasName = aliasName;
		this.level = level;
		this.parentCategoryId = parentCategoryId;
		this.parentCategoryName = parentCategoryName;
		this.site = site;
		this.fullPath = fullPath;
	}

}
