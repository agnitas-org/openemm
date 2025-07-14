/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.beans;

/**
 * POJO holding a row in the form click statistic
 */
public class UserFormLinkClicksStatisticRow {
	private String url;
	private int urlId;
	private int clicksGross;
	private int clicksNet;
    private int clicksGrossMobile;
	private int clicksNetMobile;
	private int clicksUnique;
    private int clicksAnonymous;
	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}
	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}
	/**
	 * @param urlId the url_id to set
	 */
	public void setUrlId(int urlId) {
		this.urlId = urlId;
	}
	/**
	 * @return the url_id
	 */
	public int getUrlId() {
		return urlId;
	}
	/**
	 * @param clicksGross the clicks_gros to set
	 */
	public void setClicksGross(int clicksGross) {
		this.clicksGross = clicksGross;
	}
	/**
	 * @return the clicks_gros
	 */
	public int getClicksGross() {
		return clicksGross;
	}
	/**
	 * @param clicksNet the clicks_net to set
	 */
	public void setClicksNet(int clicksNet) {
		this.clicksNet = clicksNet;
	}
	/**
	 * @return the clicks_net
	 */
	public int getClicksNet() {
		return clicksNet;
	}
	/**
	 * @param clicksUnique the clicks_unique to set
	 */
	public void setClicksUnique(int clicksUnique) {
		this.clicksUnique = clicksUnique;
	}
	/**
	 * @return the clicks_unique
	 */
	public int getClicksUnique() {
		return clicksUnique;
	}

    public int getClicksGrossMobile() {
        return clicksGrossMobile;
    }

    public void setClicksGrossMobile(int clicksGrossMobile) {
        this.clicksGrossMobile = clicksGrossMobile;
    }

    public int getClicksNetMobile() {
        return clicksNetMobile;
    }

    public void setClicksNetMobile(int clicksNetMobile) {
        this.clicksNetMobile = clicksNetMobile;
    }

    public int getClicksAnonymous() {
        return clicksAnonymous;
    }

    public void setClicksAnonymous(int clicksAnonymous) {
        this.clicksAnonymous = clicksAnonymous;
    }
}
