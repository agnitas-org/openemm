(function () {
  AGN.Lib.DomInitializer.new('mailing-parameter-list-search-initializer', function ($element) {
    var config = AGN.Lib.Helpers.objFromString($element.data('config'));
    var searchLink = config.searchLink;
    var viewLink = config.viewLink;
    var mailingSearchQuery = config.mailingSearchQuery;
    var parameterSearchQuery = config.parameterSearchQuery;
    var $parameterSearch = $('#parameter_search_field');
    var $mailingSearch = $('#mailing_search_field');

    // init select 2 for parameter search field
    new initSelect2Field($parameterSearch, searchLink, 'parameterSearchQuery', parameterSearchQuery, false, viewLink);

    // init select 2 for mailing search field
    new initSelect2Field($mailingSearch, searchLink, 'mailingSearchQuery', mailingSearchQuery, true);
  });

  function initSelect2Field($field, searchLink, searchFieldName, oldValue, isSelectable, viewLink) {
    var lastSearchQuery = oldValue;
    var lastResults = {results: []};
    var $fieldCopy = $field;

    // configures select2 for field
    $fieldCopy.select2({
      openOnEnter: true,

      /**
       * Min length of search query for start searching.
       */
      minimumInputLength: 1,

      /**
       * Allow to initialize the selection based on the value of the element select2 is attached to.
       *
       * Necessary for allowing using of following functions:
       * 1) $searchField.select2("search", searchQuery);
       * 2) $searchField.select2("data", results);
       *
       * @param element
       * @param callback
       */
      initSelection: function (element, callback) {
      },

      /**
       * Used to get the id from the choice object or a string representing the key under which the id is stored.
       *
       * @param {object} dataEntry a choice object.
       * @returns {string} the id of the object which will apear as test of search input field.
       */
      id: function (dataEntry) {
        if (isSelectable) {
          return dataEntry.mailingID;
        }
        return lastSearchQuery;
      },

      /**
       * Determinants appearance of entry search on UI.
       *
       * @param {object} resultEntry
       * @returns {string} - html structure for each result entry
       */
      formatResult: function (resultEntry) {
        return '<div style="box-shadow: 0 4px 8px 0 rgba(0,0,0,0.2);">' +
            '<div class="row">' +
            '<div class="col-sm-6">' + t('fields.mailing.parameter') + '</div>' +
            '<div class="col-sm-6">' + resultEntry.name + '</div>' +
            '</div>' +

            '<div class="row">' +
            '<div class="col-sm-6">' + t('fields.mailing.description') + '</div>' +
            '<div class="col-sm-6">' + resultEntry.description + '</div>' +
            '</div>' +

            '<div class="row">' +
            '<div class="col-sm-6">' + t('fields.mailing.for_mailing') + '</div>' +
            '<div class="col-sm-6">' + resultEntry.mailingID + '</div>' +
            '</div>' +

            '<div class="row">' +
            '<div class="col-sm-6">' + t('fields.mailing.change_date') + '</div>' +
            '<div class="col-sm-6  ">' + new Date(resultEntry.changeDate).toLocaleDateString() + '</div>' +
            '</div>' +
            '</div>';
      },

      /**
       * Determinants text of selected entry;
       *
       * @param {object} searchEntry
       * @returns {string} - text of selected entry.
       */
      formatSelection: function (searchEntry) {
        // change search query to mailingID of selected item
        if (isSelectable) {
          return searchEntry.mailingID;
        }

        // open view page of selected item
        AGN.Lib.Page.reload(viewLink.replace('{mailing-info-id}', searchEntry.mailingInfoID), true);
      },

      ajax: {
        url: searchLink,
        data: function (searchQuery) {
          var searchRequest = {};
          lastSearchQuery = searchQuery;
          setInputText(lastSearchQuery, $fieldCopy);
          searchRequest[searchFieldName] = searchQuery;
          return searchRequest;
        },
        results: function (searchResponse) {
          var id = 0;
          // adding id to each entry
          searchResponse.forEach(function (entry) {
            entry.id = id++;
          });
          // data converting to Select2 form
          lastResults = {
            results: searchResponse
          };
          return lastResults;
        },
        cache: true
      }
    });

    // init last data
    $fieldCopy.on('select2-open', function () {
      setSearchText(lastSearchQuery, $fieldCopy);
    });

    // transfer data FROM input field TO fake field of select2
    function setSearchText(searchText, $field) {
      var text = searchText || '';
      $field.select2("search", text);
    }

    // transfer data TO input field FROM fake field of select2
    function setInputText(searchText, $field) {
      var text = searchText || '';
      var $fakeSearchField = $field.select2('container').find('.select2-chosen');
      $field.select2("val", text);
      $fakeSearchField.text(text);
    }

    setInputText(oldValue, $fieldCopy);
  }
})();