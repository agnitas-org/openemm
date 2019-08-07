/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.beans;

import java.io.Serializable;

/**
 * Class for storing values sent by clicking an image button.
 * 
 * Using the ImageButton class is really simple:
 * 
 * <ol>
 *   <li>Add ImageButton field to ActionForm and instantiate it (either in constructor or in reset())</li>
 *   <li>Create a setter with parameter of type String. This will be used for the URL parameter containing the button label. Delegate that parameter to the setLabel() method of the ImageButton instance.</li>
 *   <li>Create a getter returning the instance of ImageButton. This will be used by Struts to set the x- and y-coordinates.</li>
 *   <li>To check, if the button was clicked, use isSelected()</li>
 *   <li>If you do not instantiate the ImageButton in reset(), call clearButton() here to reset the button's state to "unclicked".</li>
 * </ol>
 */
public class ImageButton implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3287437050371352848L;

	

	/**
	 * x-coord in image button (if sent).
	 */
	private int x;

	/**
	 * y-coord in image button (if sent).
	 */
	private int y;
	
	/**
	 * Button label, if sent.
	 */
	private String label;
	
	/**
	 * Creates new ImageButton.
	 */
	public ImageButton() {
		clearButton();
	}
	
	/**
	 * Resets button properties to default values.
	 */
	public void clearButton() {
		this.x = -1;
		this.y = -1;
		this.label = "";
	}
	
	/**
	 * Returns x-coordinate. If not sent, -1 is returned. 
	 * @return x-coordinate
	 */
	public int getX() {
		return x;
	}

	/**
	 * Set x-coordinate of click.
	 * @param x x-coordinate of click
	 */
	public void setX(int x) {
		this.x = x;
	}

	/**
	 * Returns y-coordinate. If not sent, -1 is returned. 
	 * @return y-coordinate
	 */
	public int getY() {
		return y;
	}

	/**
	 * Set y-coordinate of click.
	 * @param y y-coordinate of click
	 */
	public void setY(int y) {
		this.y = y;
	}

	/**
	 * Returns button label. If not sent, an empty string is returned. 
	 * @return button label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Sets button label
	 * @param label button label
	 */
	public void setLabel(String label) {
		this.label = label;
	}
	
	/**
	 * Checks, if button was clicked. An image button is clicked, if and x- or y-coordinate was sent or the button label was sent (or all of them).
	 * @return true, if button was clicked
	 */
	public boolean isSelected() {
		return x != -1 || y != -1 || (label != null && !label.equals(""));
	}
}
