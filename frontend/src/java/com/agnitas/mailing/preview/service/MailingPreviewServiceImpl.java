/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.mailing.preview.service;

import java.util.Objects;

import org.agnitas.preview.Page;
import org.agnitas.preview.Preview;
import org.agnitas.preview.PreviewFactory;
import org.agnitas.util.SafeString;
import org.springframework.beans.factory.annotation.Required;

public final class MailingPreviewServiceImpl implements MailingPreviewService {
	
	private PreviewFactory previewFactory;
	
	@Override
	public final Page renderPreview(final int mailingID, final int customerID) throws Exception {
		final Preview preview = previewFactory.createPreview();
		final Page page = preview.makePreview(mailingID, customerID, false);
		preview.done();
		
		return page;
	}
	
	@Override
	public final String renderHtmlPreview(final int mailingID, final int customerID) throws Exception {
		return renderHtmlPreview(mailingID, customerID, HtmlPreviewOptions.createDefault());
	}

	@Override
	public final String renderHtmlPreview(final int mailingID, final int customerID, final HtmlPreviewOptions options) throws Exception {
		return postProcessHtmlPreview(renderPreview(mailingID, customerID).getHTML(), options);
	}

	@Override
	public final String renderTextPreview(final int mailingID, final int customerID) throws Exception {
		return renderPreview(mailingID, customerID).getText();
	}
	
	@Override
	public final String renderPreviewFor(final int mailingID, final int customerID, final String fragment) throws Exception {
		final Preview preview = previewFactory.createPreview();
		final String page = preview.makePreview(mailingID, customerID, fragment);
		preview.done();
		
		return page;
	}

	@Required
	public final void setPreviewFactory(final PreviewFactory factory) {
		this.previewFactory = Objects.requireNonNull(factory,"Preview factory is null");
	}

	private final String postProcessHtmlPreview(String preview, final HtmlPreviewOptions options) {
		if(options.isRemoveHtmlTags()) {
			preview = SafeString.removeHTMLTags(preview);
		}
		
		if(options.getLineFeed() > 0) {
			preview = SafeString.cutLineLength(preview, options.getLineFeed());
		}
		
		return preview;
	}
	
}
