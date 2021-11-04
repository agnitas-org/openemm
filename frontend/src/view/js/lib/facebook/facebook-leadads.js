AGN_FACEBOOK.LeadAds = {};

AGN_FACEBOOK.LeadAds.getTableApi = function (selector) {
  var table = AGN.Lib.Table.get($(selector));
  if (table) {
    return table.api;
  }

  return null;
};

AGN_FACEBOOK.LeadAds.connect_pages = function (userAccessToken, url, selector, mailinglistId, doiMailingId, onSuccess, onError, onPageCompanyConflict) {
  var api = AGN_FACEBOOK.LeadAds.getTableApi(selector);
  var rows = api.getSelectedRows();
  var ids = rows.map(function (row) {
    return {
      facebook_id: row.facebook_id,
      access_token: row.access_token
    }
  });

  var payload = {
    mailinglist_id: mailinglistId,
    doi_mailing_id: doiMailingId,
    user_access_token: userAccessToken,
    pages: ids
  };

  $.ajax({
    url: url,
    type: "POST",
    data: JSON.stringify(payload),
    contentType: "application/json; charset=UTF-8",
    success: function () {
      if (!!onSuccess) {
        onSuccess();
      }
    },
    error: function (xhr, status, error) {
      console.log("status = " + status + ", error = " + error);

      if (!!onError) {
        onError();
      }
    },
    statusCode: {
      409: function (xhr) {
        if (!!onPageCompanyConflict) {
          var jsonPayload = (!!xhr.responseText) ? JSON.parse(xhr.responseText) : null;
          onPageCompanyConflict(jsonPayload);
        }
      },
      500: function () {
        if (!!onError) {
          onError();
        }
      }
    }
  });
};

AGN_FACEBOOK.LeadAds.list_manageable_pages = function (userAccessToken, url, selector, onError) {

  function update_table(jsonData, selector) {
    var table = AGN.Lib.Table.get($(selector));
    if (table) {
      table.api.setRowData(jsonData.pages);
      table.redraw();
    }
  }

  $.ajax({
    url: url,
    type: "GET",
    data: {fbAccessToken: userAccessToken},
    success: function (result) {
      update_table(result, selector);
    },
    error: function (xhr, status, error) {
      console.log("status = " + status + ", error = " + error);

      if (!!onError) {
        onError();
      }
    },
    statusCode: {
      500: function () {
        if (!!onError) {
          onError();
        }
      }
    }
  });

};

AGN_FACEBOOK.LeadAds.renew_page_access_tokens = function (userAccessToken, url, onSuccess) {
  $.ajax({
    url: url,
    type: "POST",
    data: {user_access_token: userAccessToken},
    success: function (result) {
      onSuccess(result);
    }
  })
};
