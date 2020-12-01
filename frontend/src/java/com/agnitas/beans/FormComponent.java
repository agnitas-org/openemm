/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans;

import java.util.Date;

import org.apache.log4j.Logger;

/**
 * The Class FormComponent.
 */
public class FormComponent {
	/** The Constant logger. */
	@SuppressWarnings("unused")
	private static final transient Logger logger = Logger.getLogger( FormComponent.class);

	/** The company id. */
	protected int companyID = 0;
	
	/** The form id. */
	protected int formID = 0;
	
	/** The id. */
	protected int id = 0;

	/** The name. */
	protected String name;

	/** The description. */
	protected String description;

	/** The mime type. */
	protected String mimeType;

	/** The type. */
	protected FormComponentType type = FormComponentType.IMAGE;

	/** The data. */
	protected byte[] data;
	
	/** The data size. */
	protected int dataSize = 0;

	/** The width. */
	protected int width = 0;
	
	/** The height. */
	protected int height = 0;

	/** The creation date. */
	protected Date creationDate;
	
	/** The change date. */
	protected Date changeDate;

    /**
     * The Enum FormComponentType.
     */
    public enum FormComponentType {
    	
	    /** The image. */
	    IMAGE(1),
	    
    	/** The thumbnail. */
    	THUMBNAIL(2);
    	
    	/** The internal id of FormComponentType. */
	    private final int id;
		
		/**
		 * Instantiates a new form component type.
		 *
		 * @param id the id
		 */
		private FormComponentType(int id) {
			this.id = id;
		}
		
		/**
		 * From string.
		 *
		 * @param value the value
		 * @return the form component type
		 */
		public static FormComponentType fromString(String value) {
			for (FormComponentType formComponentType : FormComponentType.values()) {
        		if (formComponentType.toString().equalsIgnoreCase(value)) {
        			return formComponentType;
        		}
        	}
        	return IMAGE;
		}
		
		/**
		 * From id.
		 *
		 * @param id the id
		 * @return the form component type
		 */
		public static FormComponentType fromId(int id) {
			for (FormComponentType formComponentType : FormComponentType.values()) {
        		if (formComponentType.getId() == id) {
        			return formComponentType;
        		}
        	}
        	return IMAGE;
		}

		/**
		 * Gets the id.
		 *
		 * @return the id
		 */
		public int getId() {
			return id;
		}

		/**
		 * Gets the name.
		 *
		 * @return the name
		 */
		public String getName() {
			return toString();
		}
    }

	
    /**
     * Gets the company id.
     *
     * @return the company id
     */
    public int getCompanyID() {
		return companyID;
	}
    

	/**
	 * Sets the company id.
	 *
	 * @param companyID the new company id
	 */
	public void setCompanyID(int companyID) {
		this.companyID = companyID;
	}
	

	/**
	 * Gets the form id.
	 *
	 * @return the form id
	 */
	public int getFormID() {
		return formID;
	}
	

	/**
	 * Sets the form id.
	 *
	 * @param formID the new form id
	 */
	public void setFormID(int formID) {
		this.formID = formID;
	}
	

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	

	/**
	 * Sets the id.
	 *
	 * @param id the new id
	 */
	public void setId(int id) {
		this.id = id;
	}
	

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	

	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}
	

	/**
	 * Gets the description.
	 *
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	

	/**
	 * Sets the description.
	 *
	 * @param description the new description
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	

	/**
	 * Gets the mime type.
	 *
	 * @return the mime type
	 */
	public String getMimeType() {
		return mimeType;
	}
	

	/**
	 * Sets the mime type.
	 *
	 * @param mimeType the new mime type
	 */
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	

	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public FormComponentType getType() {
		return type;
	}
	

	/**
	 * Sets the type.
	 *
	 * @param type the new type
	 */
	public void setType(FormComponentType type) {
		this.type = type;
	}
	

	/**
	 * Gets the data.
	 *
	 * @return the data
	 */
	public byte[] getData() {
		return data;
	}
	

	/**
	 * Sets the data.
	 *
	 * @param data the new data
	 */
	public void setData(byte[] data) {
		this.data = data;
		this.dataSize = data.length;
	}
	

	/**
	 * Gets the creation date.
	 *
	 * @return the creation date
	 */
	public Date getCreationDate() {
		return creationDate;
	}
	

	/**
	 * Sets the creation date.
	 *
	 * @param creationDate the new creation date
	 */
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	

	/**
	 * Gets the change date.
	 *
	 * @return the change date
	 */
	public Date getChangeDate() {
		return changeDate;
	}
	

	/**
	 * Sets the change date.
	 *
	 * @param changeDate the new change date
	 */
	public void setChangeDate(Date changeDate) {
		this.changeDate = changeDate;
	}

	/**
	 * Gets the data size.
	 *
	 * @return the data size
	 */
	public int getDataSize() {
		return dataSize;
	}

	/**
	 * Sets the data size.
	 *
	 * @param dataSize the new data size
	 */
	public void setDataSize(int dataSize) {
		this.dataSize = dataSize;
	}

	/**
	 * Gets the width.
	 *
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Sets the width.
	 *
	 * @param width the new width
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * Gets the height.
	 *
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Sets the height.
	 *
	 * @param height the new height
	 */
	public void setHeight(int height) {
		this.height = height;
	}
}
