package com.jstar.eclipse.processing;

import javax.lang.model.element.Element;

public class ClassInformation {

	private Element className;
	
	private String sourceFileName;
	
	public ClassInformation(final Element className, final String sourceFileName) {
		this.className = className;
		this.sourceFileName = sourceFileName;
	}

	public void setClassName(final Element className) {
		this.className = className;
	}

	public Element getClassName() {
		return className;
	}

	public void setSourceFileName(final String sourceFileName) {
		this.sourceFileName = sourceFileName;
	}

	public String getSourceFileName() {
		return sourceFileName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((className == null) ? 0 : className.hashCode());
		result = prime * result
				+ ((sourceFileName == null) ? 0 : sourceFileName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClassInformation other = (ClassInformation) obj;
		if (className == null) {
			if (other.className != null)
				return false;
		} else if (!className.equals(other.className))
			return false;
		if (sourceFileName == null) {
			if (other.sourceFileName != null)
				return false;
		} else if (!sourceFileName.equals(other.sourceFileName))
			return false;
		return true;
	}

}
