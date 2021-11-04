AGN.Lib.Controller.new('facebook-leadads', function() {

  var Login = AGN_FACEBOOK.Login,
        LeadAds = AGN_FACEBOOK.LeadAds,
        SDK = AGN_FACEBOOK.SDK,
        Select = AGN.Lib.Select;

  var config = {};

  this.addDomInitializer('facebook-leadads', function(){
    config = this.config;
    // necessary to get real status if user logged out from facebook
    AGN.Lib.Storage.delete('fblst_' + config.FACEBOOK_LEADADS_APP_ID);
    SDK.load_sdk(config.FACEBOOK_LEADADS_APP_ID, afterInit);
  });

  var enableMailinglist = function(enable) {
    $('#mailinglistId').prop("disabled", !enable);
  };
  var enableDOIMailinglist = function(enable) {
    $('#doiMailingId').prop("disabled", !enable);
  };
  var enablePageConnection = function(enable) {
    $('[data-action="connect-page"]').prop("disabled", !enable);
  };

  var hideInfoForLogged = function() {
    $('[data-action="authorized-info"]:not(.hidden)').addClass('hidden');
  };

  var showInfoForLogged = function() {
    $('[data-action="authorized-info"].hidden').removeClass('hidden');
  };

  var afterInit = function() {
    var loginCallback = function(accessToken) {
      var onError = function() {
        AGN.Lib.Messages(t('defaults.error'), t('messages.error.reload'), 'alert');
      };
      setGuiAccordingLoginState();

      LeadAds.list_manageable_pages(
        accessToken,
        config.URL.LIST_FACEBOOK_PAGE_BINDINGS_AJAX_URL,
        '#facebook-pages-table',
        onError);
    };
    Login.attach_login_function(".facebook-login-btn", loginCallback, 'pages_read_engagement, leads_retrieval');

    setGuiAccordingLoginState();
  };

  var setGuiAccordingLoginState = function() {
    //Currently, the login button is always shown. jQuery is not able to show the button, if it is hidden by class or style...
    Login.doByLoginStatus(null, function() {$(".facebook-login-btn").show()});
    Login.doByLoginStatus(onLoggedIn, onNotLoggedIn);
  };

  var onNotLoggedIn = function() {
    Select.get($('#mailinglistId')).selectFirstValue();
    enableMailinglist(false);
    enableDOIMailinglist(false);
    enablePageConnection(false);

    hideInfoForLogged();
  };

  var onLoggedIn = function(accessToken) {
    showInfoForLogged();

    enableMailinglist(true);

    LeadAds.list_manageable_pages(accessToken, config.URL.LIST_FACEBOOK_PAGE_BINDINGS_AJAX_URL, '#facebook-pages-table');

    $('[data-action="connect-page"]').data("fbAccessToken", accessToken);

    LeadAds.renew_page_access_tokens(
      accessToken,config.URL.RENEW_FACEBOOK_PAGE_ACCESS_TOKENS_AJAX_URL,
          function(result) {
            var json = JSON.parse(result);

            if(json.count > 0) {
              AGN.Lib.Messages(t('defaults.success'), t('facebook.leadAds.renewedPageAccessTokens', json.count), 'success');
            }
          }
        );
  };

  this.addAction({change: 'mailinglist-change'}, function() {
    enablePageConnection(false);
    enableDOIMailinglist(false);

    var $el = $(this.el);
    var selectedMailinglist = Select.get($el).getSelectedValue();
    updateDOIMailings(selectedMailinglist);
    enableDOIMailinglist(selectedMailinglist !== 0);
  });

  var updateDOIMailings = function(mailinglistId) {
    var doiMailingsSelect = Select.get($('[data-action="doi-mailing-change"]'));
    doiMailingsSelect.resetOptions();
    doiMailingsSelect.setOptions([{id: 0, text:'---'}]);
    doiMailingsSelect.selectFirstValue();

    if (mailinglistId !== 0) {
      enableDOIMailinglist(false);
      $.ajax({
        url: config.URL.LIST_ACTIONBASED_AJAX_URL,
        type: "GET",
        data: {mailinglist: mailinglistId},
        success: function(result) {
          if (result) {
            var options = _.map(result.mailings, function(mailing) {
              console.log("mailing " + mailing.id + ": " + mailing.shortname);
              return {id: mailing.id, text: mailing.shortname};
            });

            options.unshift({id: 0, text:'---'});
            doiMailingsSelect.setOptions(options);

            doiMailingsSelect.selectFirstValue();
          }
        },
        error: function (xhr, status, error) {
          console.log("status = " + status + ", error = " + error);

          AGN.Lib.Messages(t('defaults.error'), t('messages.error.reload'), 'alert');
        },
        statusCode: {
          500: function() {
            if(!!onError) {
              onError();
            }
          }
        }
      });
    }
  };

  this.addAction({change: 'doi-mailing-change'}, function() {
    var $el = $(this.el);
    var selectedValue = Select.get($el).getSelectedValue();
    enablePageConnection(selectedValue !== 0);
  });

  this.addAction({click: 'connect-page'}, function() {
    var $el = $(this.el);
    var accessToken = $el.data("fbAccessToken");

    var onSuccess = function() {
      // Reload table and show user feedback
      LeadAds.list_manageable_pages(accessToken, config.URL.LIST_FACEBOOK_PAGE_BINDINGS_AJAX_URL, '#facebook-pages-table');
      AGN.Lib.Messages(t('defaults.success'), t('defaults.saved'), 'success');
    };

    var mailinglistId = Select.get($('[data-action="mailinglist-change"]')).getSelectedValue();
    var doiMailingId = Select.get($('[data-action="doi-mailing-change"]')).getSelectedValue();
    LeadAds.connect_pages(accessToken,
      config.URL.CONNECT_FACEBOOK_PAGE_BINDINGS_AJAX_URL,
      '#facebook-pages-table',
      mailinglistId,
      doiMailingId,
      onSuccess,
      null,
      function(json) {
        if(!!json) {
          json.page_names.forEach(function(name) { AGN.Lib.Messages(t('facebook.leadAds.error.already_bound_to_other_company'), name, 'alert')});
        }
      }
    );
  });
});
