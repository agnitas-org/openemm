/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.predelivery.litmus.helper;

/**
 * This is a helper class for a litmus item that state = LITMUS_SENDEMAIL_TEST_STATUS_COMPLETE
 */
public class CompleteLitmusItem {
	private int id;
	private String applicationName;
	private String applicationLongName;
	
	private boolean spamTest = false;
	private boolean spam = false;
	
	private String windowImageContentBlocking;
	private String windowImageThumbContentBlocking;
	
	private String fullpageImage;
	private String fullpageImageThumb;
	
	private String windowImageNoContentBlocking;
	private String windowImageThumbNoContentBlocking;
	
	private String windowImage;
	private String windowImageThumb;
	
	private String fullpageImageContentBlocking;
	private String fullpageImageThumbContentBlocking;
	
	private String fullpageImageNoContentBlocking;
	private String fullpageImageThumbNoContentBlocking;
		
	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	/**
	 * @param applicationName the applicationName to set
	 */
	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}
	/**
	 * @return the applicationName
	 */
	public String getApplicationName() {
		return applicationName;
	}
	/**
	 * @param windowImageContentBlocking the windowImageContentBlocking to set
	 */
	public void setWindowImageContentBlocking(String windowImageContentBlocking) {
		this.windowImageContentBlocking = windowImageContentBlocking;
	}
	/**
	 * @return the windowImageContentBlocking
	 */
	public String getWindowImageContentBlocking() {
		return windowImageContentBlocking;
	}
	/**
	 * @param fullpageImageThumb the fullpageImageThumb to set
	 */
	public void setFullpageImageThumb(String fullpageImageThumb) {
		this.fullpageImageThumb = fullpageImageThumb;
	}
	/**
	 * @return the fullpageImageThumb
	 */
	public String getFullpageImageThumb() {
		return fullpageImageThumb;
	}
	/**
	 * @param windowImageNoContentBlocking the windowImageNoContentBlocking to set
	 */
	public void setWindowImageNoContentBlocking(
			String windowImageNoContentBlocking) {
		this.windowImageNoContentBlocking = windowImageNoContentBlocking;
	}
	/**
	 * @return the windowImageNoContentBlocking
	 */
	public String getWindowImageNoContentBlocking() {
		return windowImageNoContentBlocking;
	}
	/**
	 * @param windowImage the windowImage to set
	 */
	public void setWindowImage(String windowImage) {
		this.windowImage = windowImage;
	}
	/**
	 * @return the windowImage
	 */
	public String getWindowImage() {
		return windowImage;
	}
	/**
	 * @param windowImageThumbContentBlocking the windowImageThumbContentBlocking to set
	 */
	public void setWindowImageThumbContentBlocking(
			String windowImageThumbContentBlocking) {
		this.windowImageThumbContentBlocking = windowImageThumbContentBlocking;
	}
	/**
	 * @return the windowImageThumbContentBlocking
	 */
	public String getWindowImageThumbContentBlocking() {
		return windowImageThumbContentBlocking;
	}
	/**
	 * @param windowImageThumbNoContentBlocking the windowImageThumbNoContentBlocking to set
	 */
	public void setWindowImageThumbNoContentBlocking(
			String windowImageThumbNoContentBlocking) {
		this.windowImageThumbNoContentBlocking = windowImageThumbNoContentBlocking;
	}
	/**
	 * @return the windowImageThumbNoContentBlocking
	 */
	public String getWindowImageThumbNoContentBlocking() {
		return windowImageThumbNoContentBlocking;
	}
	/**
	 * @param windowImageThumb the windowImageThumb to set
	 */
	public void setWindowImageThumb(String windowImageThumb) {
		this.windowImageThumb = windowImageThumb;
	}
	/**
	 * @return the windowImageThumb
	 */
	public String getWindowImageThumb() {
		return windowImageThumb;
	}
	/**
	 * @param fullpageImageContentBlocking the fullpageImageContentBlocking to set
	 */
	public void setFullpageImageContentBlocking(
			String fullpageImageContentBlocking) {
		this.fullpageImageContentBlocking = fullpageImageContentBlocking;
	}
	/**
	 * @return the fullpageImageContentBlocking
	 */
	public String getFullpageImageContentBlocking() {
		return fullpageImageContentBlocking;
	}
	/**
	 * @param fullpageImageNoContentBlocking the fullpageImageNoContentBlocking to set
	 */
	public void setFullpageImageNoContentBlocking(
			String fullpageImageNoContentBlocking) {
		this.fullpageImageNoContentBlocking = fullpageImageNoContentBlocking;
	}
	/**
	 * @return the fullpageImageNoContentBlocking
	 */
	public String getFullpageImageNoContentBlocking() {
		return fullpageImageNoContentBlocking;
	}
	/**
	 * @param fullpageImage the fullpageImage to set
	 */
	public void setFullpageImage(String fullpageImage) {
		this.fullpageImage = fullpageImage;
	}
	/**
	 * @return the fullpageImage
	 */
	public String getFullpageImage() {
		return fullpageImage;
	}
	/**
	 * @param fullpageImageThumbContentBlocking the fullpageImageThumbContentBlocking to set
	 */
	public void setFullpageImageThumbContentBlocking(
			String fullpageImageThumbContentBlocking) {
		this.fullpageImageThumbContentBlocking = fullpageImageThumbContentBlocking;
	}
	/**
	 * @return the fullpageImageThumbContentBlocking
	 */
	public String getFullpageImageThumbContentBlocking() {
		return fullpageImageThumbContentBlocking;
	}
	/**
	 * @param fullpageImageThumbNoContentBlocking the fullpageImageThumbNoContentBlocking to set
	 */
	public void setFullpageImageThumbNoContentBlocking(
			String fullpageImageThumbNoContentBlocking) {
		this.fullpageImageThumbNoContentBlocking = fullpageImageThumbNoContentBlocking;
	}
	/**
	 * @return the fullpageImageThumbNoContentBlocking
	 */
	public String getFullpageImageThumbNoContentBlocking() {
		return fullpageImageThumbNoContentBlocking;
	}
	
	/**
	 * @param applicationLongName the applicationLongName to set
	 */
	public void setApplicationLongName(String applicationLongName) {
		this.applicationLongName = applicationLongName;
	}
	/**
	 * @return the applicationLongName
	 */
	public String getApplicationLongName() {
		return applicationLongName;
	}
	/**
	 * @param spamTest the spamTest to set
	 */
	public void setSpamTest(boolean spamTest) {
		this.spamTest = spamTest;
	}
	/**
	 * @return the spamTest
	 */
	public boolean isSpamTest() {
		return spamTest;
	}
	/**
	 * @param spam the spam to set
	 */
	public void setSpam(boolean spam) {
		this.spam = spam;
	}
	/**
	 * @return the spam
	 */
	public boolean isSpam() {
		return spam;
	}
	
}
