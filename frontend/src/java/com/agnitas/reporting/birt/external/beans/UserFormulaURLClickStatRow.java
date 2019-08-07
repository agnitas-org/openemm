/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.beans;

/**
 * POJO holding a row in the formula click statistic
 */
public class UserFormulaURLClickStatRow {
	private String url;
	private int url_id;
	private int clicks_gros;
	private int clicks_net;
    private int clicks_gross_mobile;
	private int clicks_net_mobile;
	private int clicks_unique;
    private int clicks_anonymous;
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
	 * @param url_id the url_id to set
	 */
	public void setUrl_id(int url_id) {
		this.url_id = url_id;
	}
	/**
	 * @return the url_id
	 */
	public int getUrl_id() {
		return url_id;
	}
	/**
	 * @param clicks_gros the clicks_gros to set
	 */
	public void setClicks_gros(int clicks_gros) {
		this.clicks_gros = clicks_gros;
	}
	/**
	 * @return the clicks_gros
	 */
	public int getClicks_gros() {
		return clicks_gros;
	}
	/**
	 * @param clicks_net the clicks_net to set
	 */
	public void setClicks_net(int clicks_net) {
		this.clicks_net = clicks_net;
	}
	/**
	 * @return the clicks_net
	 */
	public int getClicks_net() {
		return clicks_net;
	}
	/**
	 * @param clicks_unique the clicks_unique to set
	 */
	public void setClicks_unique(int clicks_unique) {
		this.clicks_unique = clicks_unique;
	}
	/**
	 * @return the clicks_unique
	 */
	public int getClicks_unique() {
		return clicks_unique;
	}

    public int getClicks_gross_mobile() {
        return clicks_gross_mobile;
    }

    public void setClicks_gross_mobile(int clicks_gross_mobile) {
        this.clicks_gross_mobile = clicks_gross_mobile;
    }

    public int getClicks_net_mobile() {
        return clicks_net_mobile;
    }

    public void setClicks_net_mobile(int clicks_net_mobile) {
        this.clicks_net_mobile = clicks_net_mobile;
    }

    public int getClicks_anonymous() {
        return clicks_anonymous;
    }

    public void setClicks_anonymous(int clicks_anonymous) {
        this.clicks_anonymous = clicks_anonymous;
    }
}
