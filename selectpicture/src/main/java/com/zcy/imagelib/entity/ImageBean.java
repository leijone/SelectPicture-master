package com.zcy.imagelib.entity;

import java.io.Serializable;

public class ImageBean implements Serializable {

	private static final long serialVersionUID = 1L;
	public String parentName;
	public long size;
	public String displayName;
	public String path;
	public boolean isChecked;

	public ImageBean() {
		super();
	}

	public ImageBean(String path) {
		super();
		this.path = path;
	}

	public ImageBean(String parentName, long size, String displayName,
			String path, boolean isChecked) {
		super();
		this.parentName = parentName;
		this.size = size;
		this.displayName = displayName;
		this.path = path;
		this.isChecked = isChecked;
	}

	@Override
	public String toString() {
		return "ImageBean [parentName=" + parentName + ", size=" + size
				+ ", displayName=" + displayName + ", path=" + path
				+ ", isChecked=" + isChecked + "]";
	}
	@Override
	public boolean equals(Object o) {
		try {
			ImageBean obj = (ImageBean)o;
			return (this.isChecked==obj.isChecked)
					&&(this.displayName.equals(obj.displayName))
					&&(this.parentName.equals(obj.parentName))
					&&(this.path.equals(obj.path))
					&&(this.size==obj.size);
		} catch (Exception e) {
			return false;
		}
	}
}
