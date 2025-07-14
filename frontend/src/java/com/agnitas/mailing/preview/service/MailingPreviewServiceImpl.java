/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.mailing.preview.service;

import java.util.Objects;

import com.agnitas.preview.Page;
import com.agnitas.preview.Preview;
import com.agnitas.preview.PreviewFactory;
import com.agnitas.util.SafeString;
public final class MailingPreviewServiceImpl implements MailingPreviewService {
	
	private PreviewFactory previewFactory;
	
	@Override
	public Page renderPreview(int mailingID, int customerID) {
		final Preview preview = previewFactory.createPreview();
		final Page page = preview.makePreview(mailingID, customerID, false);
		preview.done();
		
		return page;
	}
	
	@Override
	public String renderHtmlPreview(int mailingID, int customerID) {
		return renderHtmlPreview(mailingID, customerID, HtmlPreviewOptions.createDefault());
	}

	@Override
	public String renderHtmlPreview(int mailingID, int customerID, HtmlPreviewOptions options) {
		return postProcessHtmlPreview(renderPreview(mailingID, customerID).getHTML(), options);
	}

	@Override
	public String renderTextPreview(int mailingID, int customerID) {
		return renderPreview(mailingID, customerID).getText();
	}
	
	@Override
	public String renderSmsPreview(int mailingID, int customerID) {
		return renderPreview(mailingID, customerID).getSMS();
	}

	@Override
	public String renderPreviewFor(int mailingID, int customerID, String fragment) {
		final Preview preview = previewFactory.createPreview();
		final String page = preview.makePreview(mailingID, customerID, fragment);
		preview.done();
		
		return page;
	}

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
