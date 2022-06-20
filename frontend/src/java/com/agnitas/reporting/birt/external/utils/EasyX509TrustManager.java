/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


/* ====================================================================							
 *							
 * Licensed to the Apache Software Foundation (ASF) under one or more							
 * contributor license agreements. See the NOTICE file distributed with							
 * this work for additional information regarding copyright ownership.							
 * The ASF licenses this file to You under the Apache License, Version 2.0							
 * (the "License"); you may not use this file except in compliance with							
 * the License. You may obtain a copy of the License at							
 *							
 * http://www.apache.org/licenses/LICENSE-2.0							
 *							
 * Unless required by applicable law or agreed to in writing, software							
 * distributed under the License is distributed on an "AS IS" BASIS,							
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.							
 * See the License for the specific language governing permissions and							
 * limitations under the License.							
 * ====================================================================							
 *							
 * This software consists of voluntary contributions made by many							
 * individuals on behalf of the Apache Software Foundation. For more							
 * information on the Apache Software Foundation, please see							
 * <http://www.apache.org/>.							
 *							
 */

package com.agnitas.reporting.birt.external.utils;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * <p>
 * EasyX509TrustManager unlike default {@link X509TrustManager} accepts
 * self-signed certificates.
 * </p>
 * <p>
 * This trust manager SHOULD NOT be used for productive systems due to security
 * reasons, unless it is a concious decision and you are perfectly aware of
 * security implications of accepting self-signed certificates
 * </p>
 * 
 * @author <a href="mailto:adrian.sutton@ephox.com">Adrian Sutton</a>
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 * 
 * <p>
 * DISCLAIMER: HttpClient developers DO NOT actively support this component. The
 * component is provided as a reference material, which may be inappropriate for
 * use without additional customization.
 * </p>
 */

public class EasyX509TrustManager implements X509TrustManager {
	/** The logger. */
	private static final transient Logger logger = LogManager.getLogger(EasyX509TrustManager.class);
	
	private X509TrustManager standardTrustManager = null;

	/**
	 * Constructor for EasyX509TrustManager.
	 */
	public EasyX509TrustManager(KeyStore keystore)
			throws NoSuchAlgorithmException, KeyStoreException {
		super();
		TrustManagerFactory factory = TrustManagerFactory
				.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		factory.init(keystore);
		TrustManager[] trustmanagers = factory.getTrustManagers();
		if (trustmanagers.length == 0) {
			throw new NoSuchAlgorithmException("no trust manager found");
		}
		this.standardTrustManager = (X509TrustManager) trustmanagers[0];
	}

	/**
	 * @see javax.net.ssl.X509TrustManager#checkClientTrusted(X509Certificate[],String
	 *      authType)
	 */
	@Override
	public void checkClientTrusted(X509Certificate[] certificates,
			String authType) throws CertificateException {
		standardTrustManager.checkClientTrusted(certificates, authType);
	}

	/**
	 * @see javax.net.ssl.X509TrustManager#checkServerTrusted(X509Certificate[],String
	 *      authType)
	 */
	@Override
	public void checkServerTrusted(X509Certificate[] certificates,
			String authType) throws CertificateException {
		if ((certificates != null) && logger.isDebugEnabled()) {
			logger.debug("Server certificate chain:");
			for (int i = 0; i < certificates.length; i++) {
				logger.debug("X509Certificate[" + i + "]=" + certificates[i]);
			}
		}
		if ((certificates != null) && (certificates.length == 1)) {
			certificates[0].checkValidity();
		} else {
			standardTrustManager.checkServerTrusted(certificates, authType);
		}
	}

	/**
	 * @see javax.net.ssl.X509TrustManager#getAcceptedIssuers()
	 */
	@Override
	public X509Certificate[] getAcceptedIssuers() {
		return this.standardTrustManager.getAcceptedIssuers();
	}
}
