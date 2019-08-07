/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.throttling.impl;


public class SlidingAverageRateMeter {

	private long lastUpdatedTime;
	private long period;
	private long threshold;
	private double averagedCounter = .0;
	
	public SlidingAverageRateMeter(long period, long threshold) {
		this.period = period;
		this.threshold = threshold;
		lastUpdatedTime = System.currentTimeMillis();
	}
	
	public void tick() {
		tick(1);
	}
		
	public void tick(long ticks) {
		update();
		averagedCounter += ticks;
	}
	
	public double getRate() {
		update();
		return averagedCounter / period * 1000;
	}
	
	public double getAveragedCounter() {
		return averagedCounter;
	}
	
	private void update() {
		long currentTime = System.currentTimeMillis();
		
		long delta = currentTime - lastUpdatedTime;
		
		if ( delta > period) {
			averagedCounter = 0.;
			lastUpdatedTime = currentTime;
		} else if ( delta > threshold ) {
			averagedCounter *= 1. - (double) delta / period;
			lastUpdatedTime = currentTime;
		}
	}
}
