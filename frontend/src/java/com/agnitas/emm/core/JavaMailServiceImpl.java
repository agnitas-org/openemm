/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.mail.util.ByteArrayDataSource;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.util.AgnUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

public class JavaMailServiceImpl implements JavaMailService {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(JavaMailServiceImpl.class);
	
	private ConfigService configService;

	@Required
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	/**
	 * Send an email with error comment and error-Exception and stack trace to the address taken from mailaddress.velocity property.
	 */
	@Override
	public boolean sendVelocityExceptionMail(String formUrl, Exception e) {
		String toAddress = configService.getValue(ConfigValue.Mailaddress_Velocity);
		
		if (StringUtils.isNotBlank(toAddress)) {
			String message = "Velocity error: \n";
			message += e.getMessage().split("\n")[0];
			if (formUrl != null) {
				message += "\n Form URL: \n";
				message += formUrl;
			}
			message += "\n Error details: \n";
			message += AgnUtils.throwableToString(e, -1);
			
			String subjectText = "EMM Fehler";
	
			try {
				return sendEmail(toAddress, subjectText, message, null);
			} catch (Exception me) {
				logger.error("Error sending VelocityExceptionMail with exception: " + e.getMessage() + "\nEmailmessage:\n" + subjectText + "\n" + message, e);
				return false;
			}
		} else {
			return true;
		}
	}

	/**
	 * Send an email with error comment and error-Exception and stack trace to the address taken from mailaddress.error property.
	 *
	 * @param comment
	 *            Text that is added before the exception.
	 * @param e
	 *            The exception to log.
	 * @return true if all went ok.
	 */
	@Override
	public boolean sendExceptionMail(String comment, Exception e) {
		String toAddress = configService.getValue(ConfigValue.Mailaddress_Error);
		if (toAddress != null) {
			if (StringUtils.isNotBlank(toAddress)) {
				StringBuilder messageBuilder = new StringBuilder();
				if (comment != null) {
					messageBuilder.append(comment).append("\n");
				}
				messageBuilder.append("Exception:\n").append(AgnUtils.throwableToString(e, -1));

				String subjectText = "EMM Error (Server: " + AgnUtils.getHostName() + " / Exceptiontype: " + e.getClass().getSimpleName() + ")";
		
				if (logger.isInfoEnabled()) {
					logger.info("Sending error message:\n" + messageBuilder.toString());
				}
	
				try {
					final boolean result = sendEmail(toAddress, subjectText, messageBuilder.toString(), null);
					
					if (!result) {
						logger.error("Could not send exception mail - unreported exception is:", e);
					}
					
					return result;
				} catch (Exception me) {
					logger.error("Error sending email with exception: " + e.getMessage() + "\nEmailmessage:\n" + subjectText + "\n" + messageBuilder.toString(), e);
					return false;
				}
			} else {
				return true;
			}
		} else {
			logger.error("Error sending email with exception (ConfigService not initialized): " + e.getMessage(), e);
			return false;
		}
	}

	/**
	 * Sends an email via Java (not via EMM Backend)
	 * 
	 * For pure textmails leave bodyHtml empty or null
	 */
	@Override
	public boolean sendEmail(String toAddressList, String subject, String bodyText, String bodyHtml, JavaMailAttachment... attachments) {
		return sendEmail(null, null, null, null, null, toAddressList, null, subject, bodyText, bodyHtml, null, attachments);
	}

	@Override
	public boolean sendEmail(String toAddressList, String fromAddress, String replyToAddress, String subject, String bodyText, String bodyHtml, JavaMailAttachment... attachments) {
		return sendEmail(fromAddress, null, replyToAddress, null, null, toAddressList, null, subject, bodyText, bodyHtml, null, attachments);
	}

	/**
	 * Sends an email via Java (not via EMM Backend)
	 * 
	 * For default senderaddress (from address) leave fromAddress empty or null
	 * 
	 * For pure textmails leave bodyHtml empty or null
	 */
	@Override
	public boolean sendEmail(String fromAddress, String fromName, String replyToAddress, String replyToName, String bounceAddress, String toAddressList, String ccAddressList, String subject, String bodyText, String bodyHtml, String charset, JavaMailAttachment... attachments) {
		String smtpMailRelayHostname = configService.getValue(ConfigValue.SmtpMailRelayHostname);
		if (StringUtils.isBlank(smtpMailRelayHostname)) {
			logger.error("smtpMailRelayHostname is missing, so no mail was sent. emailSubject: " + subject);
			return false;
		}
		
		if (StringUtils.isBlank(smtpMailRelayHostname)) {
			logger.warn("STMP mail relay hostname is not set, using localhost as fallback");
			smtpMailRelayHostname = "localhost";
		}
		
		if (StringUtils.isBlank(fromAddress)) {
			fromAddress = configService.getValue(ConfigValue.Mailaddress_Sender);
			if (StringUtils.isNotBlank(configService.getValue(ConfigValue.Mailaddress_SenderName))) {
				fromName = configService.getValue(ConfigValue.Mailaddress_SenderName);
			}
			if (StringUtils.isBlank(fromAddress)) {
				fromAddress = System.getProperty("user.name") + "@" + AgnUtils.getHostName();
				fromName = "EMM";
			}
		}
		
		if (StringUtils.isBlank(charset)) {
			charset = "UTF-8";
		}
		
		try {
			// create some properties and get the default Session
			Properties props = new Properties();
			props.put("system.mail.host", smtpMailRelayHostname);
			props.put("mail.smtp.host", smtpMailRelayHostname);
			props.put("mail.host", smtpMailRelayHostname);
			
			// Set bounce address
			if (StringUtils.isNotBlank(bounceAddress)) {
				props.put("mail.smtp.from", bounceAddress);
			} else if (StringUtils.isNotBlank(configService.getValue(ConfigValue.Mailaddress_Bounce))) {
				props.put("mail.smtp.from", configService.getValue(ConfigValue.Mailaddress_Bounce));
			} else {
				// Use fromAddress as fallback
				props.put("mail.smtp.from", fromAddress);
			}
			
			Session session = Session.getDefaultInstance(props, null);

			// create a message
			MimeMessage msg = new MimeMessage(session);
			if (StringUtils.isBlank(fromName)) {
				msg.setFrom(new InternetAddress(fromAddress));
			} else {
				msg.setFrom(new InternetAddress(fromAddress, fromName));
			}
			msg.setSubject(subject, charset);
			msg.setSentDate(new Date());
			
			// Set reply-to address
			if (StringUtils.isNotBlank(replyToAddress)) {
				if (StringUtils.isBlank(replyToName)) {
					msg.setReplyTo(new Address[] { new InternetAddress(replyToAddress) });
				} else {
					msg.setReplyTo(new Address[] { new InternetAddress(replyToAddress, replyToName) });
				}
			} else if (StringUtils.isNotBlank(configService.getValue(ConfigValue.Mailaddress_ReplyTo))) {
				if (StringUtils.isBlank(configService.getValue(ConfigValue.Mailaddress_ReplyToName))) {
					msg.setReplyTo(new Address[] { new InternetAddress(configService.getValue(ConfigValue.Mailaddress_ReplyTo)) });
				} else {
					msg.setReplyTo(new Address[] { new InternetAddress(configService.getValue(ConfigValue.Mailaddress_ReplyTo), configService.getValue(ConfigValue.Mailaddress_ReplyToName)) });
				}
			} else {
				// Use fromAddress as fallback
				if (StringUtils.isBlank(fromName)) {
					msg.setReplyTo(new Address[] { new InternetAddress(fromAddress) });
				} else {
					msg.setReplyTo(new Address[] { new InternetAddress(fromAddress, fromName) });
				}
			}

			// Set to-recipient email addresses
			InternetAddress[] toAddresses = getEmailAddressesFromList(toAddressList);
			if (toAddresses.length > 0) {
				msg.setRecipients(Message.RecipientType.TO, toAddresses);
			}

			// Set cc-recipient email addresses
			InternetAddress[] ccAddresses = getEmailAddressesFromList(ccAddressList);
			if (ccAddresses.length > 0) {
				msg.setRecipients(Message.RecipientType.CC, ccAddresses);
			}

			if (attachments == null || attachments.length <= 0) {
				if (StringUtils.isBlank(bodyText) && StringUtils.isBlank(bodyHtml)) {
					// Use a simple text email with only text content
					// Set to a single space. If body is totally empty, Commons Emails rejects sending email with exception "Invalid message supplied" (-> EMM-4133)
					msg.setText(" ", charset);
				} else if (StringUtils.isBlank(bodyHtml)) {
					// Use a simple text email with only text content
					msg.setText(bodyText, charset);
				} else if (StringUtils.isBlank(bodyText)) {
					// Use a simple html email with only html content
					msg.setContent(bodyHtml, "text/html; charset=" + charset);
				} else {
					// Use a multipart email with text and html content
					Multipart multipart = new MimeMultipart("alternative");
					
					MimeBodyPart textMimeBodyPart = new MimeBodyPart();
					textMimeBodyPart.setText(bodyText, charset);
					multipart.addBodyPart(textMimeBodyPart);
					
					MimeBodyPart htmlMimeBodyPart = new MimeBodyPart();
					htmlMimeBodyPart.setContent(bodyHtml, "text/html; charset=" + charset);
					multipart.addBodyPart(htmlMimeBodyPart);
					
					msg.setContent(multipart);
				}
			} else {
				Multipart multipartMixed = new MimeMultipart("mixed");
				
				if (StringUtils.isBlank(bodyText) && StringUtils.isBlank(bodyHtml)) {
					// Use a multipart text email with only text content and attachements
					MimeBodyPart textMimeBodyPart = new MimeBodyPart();
					// Set to a single space. If body is totally empty, Commons Emails rejects sending email with exception "Invalid message supplied" (-> EMM-4133)
					textMimeBodyPart.setText(" ", charset);
					multipartMixed.addBodyPart(textMimeBodyPart);
				} else if (StringUtils.isBlank(bodyHtml)) {
					MimeBodyPart textMimeBodyPart = new MimeBodyPart();
					// Use a multipart text email with only text content and attachements
					textMimeBodyPart.setText(bodyText, charset);
					multipartMixed.addBodyPart(textMimeBodyPart);
				} else if (StringUtils.isBlank(bodyText)) {
					MimeBodyPart textMimeBodyPart = new MimeBodyPart();
					// Use a multipart html email with only html content and attachements
					textMimeBodyPart.setContent(bodyHtml, "text/html; charset=" + charset);
					multipartMixed.addBodyPart(textMimeBodyPart);
				} else {
					// Use a multipart html email with text and html content and attachements
					Multipart multipartTextContent = new MimeMultipart("alternative");
					
					MimeBodyPart textMimeBodyPart = new MimeBodyPart();
					textMimeBodyPart.setText(bodyText, charset);
					multipartTextContent.addBodyPart(textMimeBodyPart);
					
					MimeBodyPart htmlMimeBodyPart = new MimeBodyPart();
					htmlMimeBodyPart.setContent(bodyHtml, "text/html; charset=" + charset);
					multipartTextContent.addBodyPart(htmlMimeBodyPart);
					
			        MimeBodyPart alternativeMimeBodyPart = new MimeBodyPart();
			        multipartMixed.addBodyPart(alternativeMimeBodyPart);
			        alternativeMimeBodyPart.setContent(multipartTextContent);
				}
				
				for (JavaMailAttachment attachment : attachments) {
					MimeBodyPart attachmentMimeBodyPart = new MimeBodyPart();
					attachmentMimeBodyPart.setFileName(MimeUtility.encodeText(attachment.getName(), "UTF-8", null));
					ByteArrayDataSource bds = new ByteArrayDataSource(attachment.getData(), attachment.getMimeType());
					attachmentMimeBodyPart.setDataHandler(new DataHandler(bds));
					multipartMixed.addBodyPart(attachmentMimeBodyPart);
				}
				
				msg.setContent(multipartMixed);
			}
			
			Transport.send(msg);
			
			if (logger.isDebugEnabled()) {
				logger.debug("Sending java email:\nfrom: " + fromAddress + "\nto: " + toAddressList + "\nsubject: " + subject);
			}
			
			return true;
		} catch (Exception e) {
			logger.error("Error sending email via " + smtpMailRelayHostname + ": " + e.getMessage() + " \nemailSubject: " + subject + " \nemailContent: " + (StringUtils.isBlank(bodyText) ? bodyHtml : bodyText), e);
			return false;
		}
	}

	private static InternetAddress[] getEmailAddressesFromList(String listString) {
		List<InternetAddress> emailAddresses = new ArrayList<>();
		for (String address : AgnUtils.splitAndNormalizeEmails(listString)) {
			address = StringUtils.trimToEmpty(address);
			if(AgnUtils.isEmailValid(address)) {
				try {
					InternetAddress nextAddress = new InternetAddress(address);
					nextAddress.validate();
					emailAddresses.add(nextAddress);
				} catch (AddressException e) {
					logger.error("Invalid Emailaddress found: " + address);
				}
			}
		}

		return emailAddresses.toArray(new InternetAddress[0]);
	}
}
