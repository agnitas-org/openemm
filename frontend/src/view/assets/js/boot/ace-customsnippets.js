/**
 * Created with IntelliJ IDEA.
 * User: Igor Gudymenko
 * Date: 7/4/14
 * Time: 4:46 PM
 * To change this template use File | Settings | File Templates.
 */

(function(){

  var snippetManager = ace.require("ace/snippets").snippetManager,
      config = ace.require("ace/config");

  ace.config.loadModule("ace/snippets/html", function(m) {

    if (m && snippetManager.files) {
      snippetManager.files.html = m;
      m.snippets = snippetManager.parseSnippetFile(m.snippetText);

      m.snippets.push({
          content: "[agnALTER column=\"${1}\"]",
          name: "agn:ALTER",
          tabTrigger: "alt"
      }, {
          content: "[agnBANNER banner=\"${1}\" banner=\"${2}\"]",
          name: "agn:BANNER",
          tabTrigger: "bnr"
      }, {
          content: "[agnCALC column=\"${1}\" op=\"${2}\" value=\"${3}\"]",
          name: "agn:CALC",
          tabTrigger: "clc"
      }, {
          content: "[agnCUSTOMDATE offset=\"${1}\" format=\"${2}\"]",
          name: "agn:CUSTOMDATE",
          tabTrigger: "cusd"
      }, {
          content: "[agnCUSTOMDATE_DE offset=\"${1}\" format=\"${2}\"]",
          name: "agn:CUSTOMDATE_DE",
          tabTrigger: "cust"
      }, {
          content: "[agnCUSTOMERID]",
          name: "agn:CUSTOMERID",
          tabTrigger: "cst"
      }, {
          content: "[agnDATE]",
          name: "agn:DATE",
          tabTrigger: "dte"
      }, {
          content: "[agnDATEDB column=\"${1}\" format=\"${2}\"]",
          name: "agn:DATEDB",
          tabTrigger: "datb"
      }, {
          content: "[agnDATEDB_DE column=\"${1}\" format=\"${2}\"]",
          name: "agn:DATEDB_DE",
          tabTrigger: "dadb"
      }, {
          content: "[agnDAYS_UNTIL column=\"${1}\"]",
          name: "agn:DAYS_UNTIL",
          tabTrigger: "daunt"
      }, {
          content: "[agnDAYS_UNTILL column=\"${1}\"]",
          name: "agn:DAYS_UNTILL",
          tabTrigger: "duti"
      }, {
          content: "[agnDB column=\"${1}\"]",
          name: "agn:DB",
          tabTrigger: "db"
      }, {
          content: "[agnDBV]",
          name: "agn:DBV",
          tabTrigger: "dbv"
      }, {
          content: "[agnEMAIL]",
          name: "agn:EMAIL",
          tabTrigger: "ema"
      }, {
          content: "[agnFORM name=\"${1}\"]",
          name: "agn:FORM",
          tabTrigger: "form"
      }, {
          content: "[agnIMAGE name=\"${1}\"]",
          name: "agn:IMAGE",
          tabTrigger: "img"
      }, {
          content: "[agnIMGLINK name=\"${1}\"]",
          name: "agn:IMGLINK",
          tabTrigger: "imgli"
      }, {
          content: "[agnNULL]",
          name: "agn:NULL",
          tabTrigger: "nul"
      }, {
          content: "[agnPROFILE]",
          name: "agn:PROFILE",
          tabTrigger: "prof"
      }, {
          content: "[agnSUBSCRIBERCOUNT]",
          name: "agn:SUBSCRIBERCOUNT",
          tabTrigger: "subs"
      }, {
          content: "[agnSWYN networks=\"${1}\" title=\"${2}\"]",
          name: "agn:SWYN",
          tabTrigger: "swy"
      }, {
          content: "[agnTITLE type=\"${1:2}\"]",
          name: "agn:TITLE",
          tabTrigger: "ti"
      }, {
          content: "[agnTITLEFIRST type=\"${1:2}\"]",
          name: "agn:TITLEFIRST",
          tabTrigger: "tifr"
      }, {
          content: "[agnTITLEFULL type=\"${1:2}\"]",
          name: "agn:TITLEFULL",
          tabTrigger: "tifu"
      }, {
          content: "[agnTITLE_SHORT type=\"${1:2}\"]",
          name: "agn:TITLE_SHORT",
          tabTrigger: "tish"
      }, {
          content: "[agnUNSUBSCRIBE]",
          name: "agn:UNSUBSCRIBE",
          tabTrigger: "unsub"
      }, {
          content: "[agnDYN name=\"${1}\"/]",
          name: "agn:DYN",
          tabTrigger: "dyn"
      }, {
          content: "BIRTHDAY",
          name: "birthday",
          tabTrigger: "h"
      }, {
          content: "CREATION_DATE",
          name: "creation_date",
          tabTrigger: "h"
      }, {
          content: "CUSTOMER_ID",
          name: "customer_id",
          tabTrigger: "h"
      }, {
          content: "DATASOURCE_ID",
          name: "datasource_id",
          tabTrigger: "h"
      }, {
          content: "EMAIL",
          name: "email",
          tabTrigger: "h"
      }, {
          content: "FACEBOOK_STATUS",
          name: "facebook_status",
          tabTrigger: "h"
      }, {
          content: "FIRSTNAME",
          name: "firstname",
          tabTrigger: "h"
      }, {
          content: "FOURSQUARE_STATUS",
          name: "foursquare_status",
          tabTrigger: "h"
      }, {
          content: "GENDER",
          name: "gender",
          tabTrigger: "h"
      }, {
          content: "GOOGLE_STATUS",
          name: "google_status",
          tabTrigger: "h"
      }, {
          content: "LASTNAME",
          name: "lastname",
          tabTrigger: "h"
      }, {
          content: "MAILTYPE",
          name: "mailtype",
          tabTrigger: "h"
      }, {
          content: "ML_FEHLDATEN",
          name: "ml_fehldaten",
          tabTrigger: "h"
      }, {
          content: "TIMESTAMP",
          name: "timestamp",
          tabTrigger: "h"
      }, {
          content: "TITLE",
          name: "title",
          tabTrigger: "h"
      }, {
          content: "TWITTER_STATUS",
          name: "twitter_status",
          tabTrigger: "h"
      }, {
          content: "XING_STATUS",
          name: "xing_status",
          tabTrigger: "h"
      }, {
          content: "HTML-Version",
          name: 'html-version',
          tabTrigger: "h"
      }, {
          content: "Text",
          name: "text",
          tabTrigger: "h"
      });
      snippetManager.register(m.snippets, m.scope);
    }
  });
})();
