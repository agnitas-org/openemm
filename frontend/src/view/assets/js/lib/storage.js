(function($){

  var get,
      set,
      deleteByKey,
      deleteByNamespace,
      getFromRealStorage,
      saveChosenFields,
      restoreChosenFields,
      currentStorage,
      Storage;

  Storage = function (type) {
    function createCookie(name, value, days) {
      var date, expires;

      if (days) {
        date = new Date();
        date.setTime(date.getTime()+(days*24*60*60*1000));
        expires = "; expires="+date.toGMTString();
      } else {
        expires = "";
      }
      document.cookie = name+"="+value+expires+"; path=/";
    }

    function readCookie(name) {
      var nameEQ = name + "=",
          ca = document.cookie.split(';'),
          i, c;

      for (i=0; i < ca.length; i++) {
        c = ca[i];
        while (c.charAt(0)==' ') {
          c = c.substring(1,c.length);
        }

        if (c.indexOf(nameEQ) == 0) {
          return c.substring(nameEQ.length,c.length);
        }
      }
      return null;
    }

    function setData(data) {
      data = JSON.stringify(data);
      if (type == 'session') {
        window.name = data;
      } else {
        createCookie('localStorage', data, 365);
      }
    }

    function clearData() {
      if (type == 'session') {
        window.name = '';
      } else {
        createCookie('localStorage', '', 365);
      }
    }

    function getData() {
      var data = type == 'session' ? window.name : readCookie('localStorage');
      try {
        return JSON.parse(data);
      } catch (e) {
        console.warn("Could not parse storage data: " + data + " cause: " + e);
      }
      return {};
    }


    // initialise if there's already data
    var data = getData();

    return {
      length: 0,
      clear: function () {
        data = {};
        this.length = 0;
        clearData();
      },
      getItem: function (key, ignoreCache) {
        if (ignoreCache === true) {
          var realData = getData();
          return realData[key] === undefined ? null : realData[key];
        } else {
          return data[key] === undefined ? null : data[key];
        }
      },
      key: function (i) {
        // not perfect, but works
        var ctr = 0;
        for (var k in data) {
          if (ctr == i) return k;
          else ctr++;
        }
        return null;
      },
      removeItem: function (key) {
        delete data[key];
        this.length--;
        setData(data);
      },
      setItem: function (key, value) {
        data[key] = value+''; // forces the value to a string
        this.length++;
        setData(data);
      }
    };
  };

  try {
    if (!window.localStorage) {
      currentStorage = new Storage('local');
    } else {
      currentStorage = window.localStorage;
    }
  } catch(e) {
    currentStorage = new Storage('local');
  }

  get = function(key) {
    try {
      var result = currentStorage.getItem(key);
      if (result) {
        return JSON.parse(result);
      }
    } catch (e) {
      console.warn("Could not get Storage key: " + key + " cause: " + e);
    }
    return undefined;
  };

  set = function(key, value) {
    try {
      value = JSON.stringify(value);
      currentStorage.setItem(key, value);
    } catch (e) {
      console.warn("Could not set Storage key:value " + key + ":" + value + " cause: " + e)
    }
  };

  deleteByKey = function(key) {
    try {
      currentStorage.removeItem(key);
    } catch (e) {
      console.warn("Could not remove Storage key:" + key + " cause: " + e);
    }
  };

  deleteByNamespace = function(namespace) {
    var namespaceLength = namespace.length;

    _.each(currentStorage, function(value, key) {
      if ( key.substring(0, namespaceLength) == namespace ) {
        deleteByKey(key);
      }
    })
  };

  getFromRealStorage = function(key) {
    var result;
    try {
      if (!window.localStorage) {
        result = currentStorage.getItem(key, true);
      } else {
        result = currentStorage.getItem(key);
      }
    } catch(e) {
      result = currentStorage.getItem(key, true);
    }

    try {
      if (result) {
        return JSON.parse(result);
      }
    } catch (e) {
      console.warn("Could not parse (key:value) " + key + ': ' + result, e);
    }
    return undefined;
  };

  /**
   * Stores fields values in locale storage (supports select-one/select-multiple/text/textarea/radio/checkbox).
   * Fields should have "data-stored-field" with optionally scope "data-stored-field="scope"".
   * @param selector JQuery selector on field with data "data-stored-field" or container with fields.
   */
  saveChosenFields = function (selector) {
    selector.each(function () {
      var $e = $(this);
      if ($e.is("[data-stored-field]") && ($e.is("input") || $e.is("select"))){
        //Simple field
        saveStoredField($e);
      }else{
        $e.find("[data-stored-field]").each(function () {
          saveStoredField($(this));
        })
      }
    });
  };

  /**
   * Restores fields values from locale storage by name.
   */
  restoreChosenFields = function (selector) {
    var wasChanged = false;
    selector.each(function () {
      var $e = $(this);

      if ($e.is("[data-stored-field]") && ($e.is("input") || $e.is("select"))){
        //Simple field
        if (restoreStoredField($e)){
          wasChanged = true;
        }
      }else{
        $e.find("[data-stored-field]").each(function () {
          if (restoreStoredField($(this))){
            wasChanged = true;
          }
        })
      }
    });
    return wasChanged;
  }
  
  function saveStoredField(field){
    var scope = field.data("stored-field") | "";
    var property = field.prop("name");

    if (property != undefined){
      var value = undefined;
      if (field.prop("type") == "checkbox") {
        value = field.is(":checked");
      } else if (field.is("input") || field.is("select")){
        value = field.val();
      }
      if (value != undefined){
        set(createStoredFieldKey(property, scope),  value);
      }
    }
  }

  function restoreStoredField(field) {
    var scope = field.data("stored-field") | "";
    var wasChanged = false;
    var property = field.prop("name");

    if (property != undefined){
      var type = field.prop("type") || field.prop("type");
      var value = get(createStoredFieldKey(property, scope));

      if (value === undefined){
        return false;
      }

      if(type == "radio"){
        if (field.val() !== value && field.is(":checked")){
          field.prop("checked", false);
          return true;
        } else if (field.val() == value && !field.is(":checked")){
          field.prop("checked", true);
          return true;
        }
      } else if (type == "checkbox"){
        if (field.prop("checked") !== value){
          field.prop("checked", value);
          wasChanged = true;
        }
      } else if (type == "select-one"){
        var exist = false;
        field.find("option").each(function () {
          if ($(this).val() == value){
            exist = true;
            return false;
          }
        });
        if (exist && field.val() !== value){
          field.val(value).change();
          wasChanged = true;
        }
      } else if(type == "select-multiple"){
        var existed = [];
        var currentValue = field.value();
        field.find("option").each(function () {
          var optionSelector = $(this);
          $.each(value,  function (i, e) {
            if (optionSelector.val() == e){
              existed.push(e);
            }
          });
        });
        existed.sort();
        currentValue.sort();
        var equals = true;
        if (existed.length != currentValue.length) equals = false;
        for (var i = 0; i < existed.length; ++i) {
          if (existed[i] !== currentValue[i]) equals = false;
        }
        if (!equals){
          field.val(existed).change();
          wasChanged = true;
        }
      } else if (type == "textarea" || type =="text"){
        if (field.val() !== value){
          field.val(value);
          wasChanged = true;
        }
      }
    }
    return wasChanged;
  }

  function createStoredFieldKey(property, scope) {
    var key = "stored_field_";
    if (property){
      key = key + property;
    }
    if (scope){
      key = key + "_" + scope;
      return key;
    }
    return undefined;
  }

  AGN.Lib.Storage = {
    get: get,
    set: set,
    delete: deleteByKey,
    deleteByNamespace: deleteByNamespace,
    getFromRealStorage: getFromRealStorage,
    saveChosenFields: saveChosenFields,
    restoreChosenFields: restoreChosenFields
  }

})(jQuery);
