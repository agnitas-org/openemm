/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.beans;

public class TrackingPointStatRow {

	private String trackingPoint;
	private String pagetag;
	private String targetGroup;
	private int clicks_gros;
	private int clicks_net;
	private int clicks_gros_percent;
	private int clicks_net_percent;
	private double num_value;
	private String currency;	
	private String alphaParameter;
	private int column_index;
	
	
	public String getTrackingPoint() {
		return trackingPoint;
	}
	public void setTrackingPoint(String trackingPoint) {
		this.trackingPoint = trackingPoint;
	}
	public String getPagetag() {
		return pagetag;
	}
	public void setPagetag(String pagetag) {
		this.pagetag = pagetag;
	}
	public String getTargetGroup() {
		return targetGroup;
	}
	public void setTargetGroup(String targetGroup) {
		this.targetGroup = targetGroup;
	}
	public int getClicks_gros() {
		return clicks_gros;
	}
	public void setClicks_gros(int clicks_gros) {
		this.clicks_gros = clicks_gros;
	}
	public int getClicks_net() {
		return clicks_net;
	}
	public void setClicks_net(int clicks_net) {
		this.clicks_net = clicks_net;
	}
	public int getClicks_gros_percent() {
		return clicks_gros_percent;
	}
	public void setClicks_gros_percent(int clicks_gros_percent) {
		this.clicks_gros_percent = clicks_gros_percent;
	}
	public int getClicks_net_percent() {
		return clicks_net_percent;
	}
	public void setClicks_net_percent(int clicks_net_percent) {
		this.clicks_net_percent = clicks_net_percent;
	}
	public int getColumn_index() {
		return column_index;
	}
	public void setColumn_index(int column_index) {
		this.column_index = column_index;
	}
	public double getNum_value() {
		return num_value;
	}
	public void setNum_value(double num_value) {
		this.num_value = num_value;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public String getAlphaParameter() {
		return alphaParameter;
	}
	public void setAlphaParameter(String alphaParameter) {
		this.alphaParameter = alphaParameter;
	}
	

}
