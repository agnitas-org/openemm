/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.agnitas.emm.core.linkcheck.beans.LinkReachability;
import org.agnitas.util.NetworkUtil;
import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;

/**
 * Working for cheking availability of link URL.
 */
public class LinkcheckWorker implements Runnable {

	/** The logger. */
	private static final transient Logger logger = Logger.getLogger( LinkcheckWorker.class);
	
	/** Connection time out. 0 = no timeout. */
	private final int timeout;
	
	/** URL to check. */
	private final String linkToCheck;
	
	/** List to add result of check. */ 
	private final List<LinkReachability> resultList;
	
	/** 
	 * @param timeout timeout value for connection
	 * @param linkToCheck URL to check
	 * @param resultList list to add result of link check 
	 */
	public LinkcheckWorker(int timeout, String linkToCheck, List<LinkReachability> resultList) {
		this.timeout = timeout;
		this.linkToCheck = linkToCheck;
		this.resultList = resultList;
	}
	
	
	@Override
	public void run() {
		// check if the link has dynamic content.
		boolean dynamic = dynamicLinkCheck();
		
		if (dynamic) {
			if( logger.isInfoEnabled()) {
				logger.info( "Link is dynamic - no checking for: " + linkToCheck);
			}
			
			resultList.add(new LinkReachability(this.linkToCheck, LinkReachability.Reachability.OK));
		} else {
			LinkReachability availability = netBasedTest();
			
			if(logger.isInfoEnabled()) {
				logger.info("Result of checking link '" + this.linkToCheck + "': " + availability.getReachability());
			}
			
			this.resultList.add(availability);
		}
	}
	
	/**
	 * this method checks, if the given link contains dynamic content like ##AGNUID##
	 * if thats the case, we wont check the link anymore.

	 * @return true, the link is dynamic
	 */
	private boolean dynamicLinkCheck() {
		boolean dynamic = false;
		Pattern pattern = Pattern.compile ("##([^#]+)##");
		Matcher aMatch = pattern.matcher(linkToCheck);
		if (aMatch.find() ) {
			// found dynamic content
			return true;
		} 
		return dynamic;		
	}
	
	/**
	 * this method checks, if the given link works. It gets a real connection
	 * to the given server and tries to fetch some answers.

	 * @return availability of checked link
	 */
	private LinkReachability netBasedTest() {
		URL url;
		try {						
			if( logger.isInfoEnabled()) {
				logger.info( "Checking link: " + linkToCheck);
			}
			
			url = new URL(linkToCheck);	// just for checking, we could use the plain String...
			HttpClient httpClient = new HttpClient();
			NetworkUtil.setHttpClientProxyFromSystem(httpClient, linkToCheck);
			httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(timeout);
			httpClient.getHttpConnectionManager().getParams().setSoTimeout(timeout);
			
			// create get-method.
			GetMethod get = new GetMethod(url.toString());
			
//			get.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, new Integer(timeout));
			
			// Set useragent, because some servers don't respond without a mandatory set useragent and return http-error 404 or so
			get.addRequestHeader("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2 (.NET CLR 3.5.30729)");
			
			// lets start working...
			httpClient.executeMethod(get);
			
			// check response code
			if (get.getStatusCode() == HttpURLConnection.HTTP_NOT_FOUND)  {
				return new LinkReachability(this.linkToCheck, LinkReachability.Reachability.NOT_FOUND);
			}			
			
			return new LinkReachability(this.linkToCheck, LinkReachability.Reachability.OK);
		}
		catch (MalformedURLException e) {
			if( logger.isInfoEnabled()) {
				logger.info( "Link URL malformed: " + linkToCheck);				// This is no "real error", this is a test result for the link. So we can log this at INFO leven
			}
			
			return new LinkReachability(this.linkToCheck, LinkReachability.Reachability.NOT_FOUND);
		}
		catch (UnknownHostException e) {
			if( logger.isInfoEnabled()) {
				logger.info( "Unknown host: " + linkToCheck);					// This is no "real error", this is a test result for the link. So we can log this at INFO leven
			}
			
			return new LinkReachability(this.linkToCheck, LinkReachability.Reachability.NOT_FOUND);
		}
		catch (SocketTimeoutException | ConnectTimeoutException e) {
			if(logger.isInfoEnabled()) {
				logger.info("Timed out", e);
			}
			
			return new LinkReachability(this.linkToCheck, LinkReachability.Reachability.TIMED_OUT);
		}
		catch (IOException e1) {
			logger.warn( "I/O error testing URL: " + linkToCheck, e1); 			// This is no "real error", this is a test result for the link. Since this could be any IO problem, let us report this at WARN level
			
			// some other connection problem, but link was found, so don't add it to invalid links.
			// invalidlinks.add(fullUrl);			
			return new LinkReachability(this.linkToCheck, LinkReachability.Reachability.NOT_FOUND);
		}
	}	
	

}
