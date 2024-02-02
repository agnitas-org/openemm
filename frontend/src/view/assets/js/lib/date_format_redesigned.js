(function(){
    let DateFormat;
    const dateFormats={"ar-SA":"dd/MM/yy","bg-BG":"dd.M.yyyy","ca-ES":"dd/MM/yyyy","zh-TW":"yyyy/M/d","cs-CZ":"d.M.yyyy","da-DK":"dd-MM-yyyy","de-DE":"dd.MM.yyyy","el-GR":"d/M/yyyy","en-US":"M/d/yyyy","fi-FI":"d.M.yyyy","fr-FR":"dd/MM/yyyy","he-IL":"dd/MM/yyyy","hu-HU":"yyyy. MM. dd.","is-IS":"d.M.yyyy","it-IT":"dd/MM/yyyy","ja-JP":"yyyy/MM/dd","ko-KR":"yyyy-MM-dd","nl-NL":"d-M-yyyy","nb-NO":"dd.MM.yyyy","pl-PL":"yyyy-MM-dd","pt-BR":"d/M/yyyy","ro-RO":"dd.MM.yyyy","ru-RU":"dd.MM.yyyy","hr-HR":"d.M.yyyy","sk-SK":"d. M. yyyy","sq-AL":"yyyy-MM-dd","sv-SE":"yyyy-MM-dd","th-TH":"d/M/yyyy","tr-TR":"dd.MM.yyyy","ur-PK":"dd/MM/yyyy","id-ID":"dd/MM/yyyy","uk-UA":"dd.MM.yyyy","be-BY":"dd.MM.yyyy","sl-SI":"d.M.yyyy","et-EE":"d.MM.yyyy","lv-LV":"yyyy.MM.dd.","lt-LT":"yyyy.MM.dd","fa-IR":"MM/dd/yyyy","vi-VN":"dd/MM/yyyy","hy-AM":"dd.MM.yyyy","az-Latn-AZ":"dd.MM.yyyy","eu-ES":"yyyy/MM/dd","mk-MK":"dd.MM.yyyy","af-ZA":"yyyy/MM/dd","ka-GE":"dd.MM.yyyy","fo-FO":"dd-MM-yyyy","hi-IN":"dd-MM-yyyy","ms-MY":"dd/MM/yyyy","kk-KZ":"dd.MM.yyyy","ky-KG":"dd.MM.yy","sw-KE":"M/d/yyyy","uz-Latn-UZ":"dd/MM yyyy","tt-RU":"dd.MM.yyyy","pa-IN":"dd-MM-yy","gu-IN":"dd-MM-yy","ta-IN":"dd-MM-yyyy","te-IN":"dd-MM-yy","kn-IN":"dd-MM-yy","mr-IN":"dd-MM-yyyy","sa-IN":"dd-MM-yyyy","mn-MN":"yy.MM.dd","gl-ES":"dd/MM/yy","kok-IN":"dd-MM-yyyy","syr-SY":"dd/MM/yyyy","dv-MV":"dd/MM/yy","ar-IQ":"dd/MM/yyyy","zh-CN":"yyyy/M/d","de-CH":"dd.MM.yyyy","en-GB":"dd/MM/yyyy","es-MX":"dd/MM/yyyy","fr-BE":"d/MM/yyyy","it-CH":"dd.MM.yyyy","nl-BE":"d/MM/yyyy","nn-NO":"dd.MM.yyyy","pt-PT":"dd-MM-yyyy","sr-Latn-CS":"d.M.yyyy","sv-FI":"d.M.yyyy","az-Cyrl-AZ":"dd.MM.yyyy","ms-BN":"dd/MM/yyyy","uz-Cyrl-UZ":"dd.MM.yyyy","ar-EG":"dd/MM/yyyy","zh-HK":"d/M/yyyy","de-AT":"dd.MM.yyyy","en-AU":"d/MM/yyyy","es-ES":"dd/MM/yyyy","fr-CA":"yyyy-MM-dd","sr-Cyrl-CS":"d.M.yyyy","ar-LY":"dd/MM/yyyy","zh-SG":"d/M/yyyy","de-LU":"dd.MM.yyyy","en-CA":"dd/MM/yyyy","es-GT":"dd/MM/yyyy","fr-CH":"dd.MM.yyyy","ar-DZ":"dd-MM-yyyy","zh-MO":"d/M/yyyy","de-LI":"dd.MM.yyyy","en-NZ":"d/MM/yyyy","es-CR":"dd/MM/yyyy","fr-LU":"dd/MM/yyyy","ar-MA":"dd-MM-yyyy","en-IE":"dd/MM/yyyy","es-PA":"MM/dd/yyyy","fr-MC":"dd/MM/yyyy","ar-TN":"dd-MM-yyyy","en-ZA":"yyyy/MM/dd","es-DO":"dd/MM/yyyy","ar-OM":"dd/MM/yyyy","en-JM":"dd/MM/yyyy","es-VE":"dd/MM/yyyy","ar-YE":"dd/MM/yyyy","en-029":"MM/dd/yyyy","es-CO":"dd/MM/yyyy","ar-SY":"dd/MM/yyyy","en-BZ":"dd/MM/yyyy","es-PE":"dd/MM/yyyy","ar-JO":"dd/MM/yyyy","en-TT":"dd/MM/yyyy","es-AR":"dd/MM/yyyy","ar-LB":"dd/MM/yyyy","en-ZW":"M/d/yyyy","es-EC":"dd/MM/yyyy","ar-KW":"dd/MM/yyyy","en-PH":"M/d/yyyy","es-CL":"dd-MM-yyyy","ar-AE":"dd/MM/yyyy","es-UY":"dd/MM/yyyy","ar-BH":"dd/MM/yyyy","es-PY":"dd/MM/yyyy","ar-QA":"dd/MM/yyyy","es-BO":"dd/MM/yyyy","es-SV":"dd/MM/yyyy","es-HN":"dd/MM/yyyy","es-NI":"dd/MM/yyyy","es-PR":"dd/MM/yyyy","am-ET":"d/M/yyyy","tzm-Latn-DZ":"dd-MM-yyyy","iu-Latn-CA":"d/MM/yyyy","sma-NO":"dd.MM.yyyy","mn-Mong-CN":"yyyy/M/d","gd-GB":"dd/MM/yyyy","en-MY":"d/M/yyyy","prs-AF":"dd/MM/yy","bn-BD":"dd-MM-yy","wo-SN":"dd/MM/yyyy","rw-RW":"M/d/yyyy","qut-GT":"dd/MM/yyyy","sah-RU":"MM.dd.yyyy","gsw-FR":"dd/MM/yyyy","co-FR":"dd/MM/yyyy","oc-FR":"dd/MM/yyyy","mi-NZ":"dd/MM/yyyy","ga-IE":"dd/MM/yyyy","se-SE":"yyyy-MM-dd","br-FR":"dd/MM/yyyy","smn-FI":"d.M.yyyy","moh-CA":"M/d/yyyy","arn-CL":"dd-MM-yyyy","ii-CN":"yyyy/M/d","dsb-DE":"d. M. yyyy","ig-NG":"d/M/yyyy","kl-GL":"dd-MM-yyyy","lb-LU":"dd/MM/yyyy","ba-RU":"dd.MM.yy","nso-ZA":"yyyy/MM/dd","quz-BO":"dd/MM/yyyy","yo-NG":"d/M/yyyy","ha-Latn-NG":"d/M/yyyy","fil-PH":"M/d/yyyy","ps-AF":"dd/MM/yy","fy-NL":"d-M-yyyy","ne-NP":"M/d/yyyy","se-NO":"dd.MM.yyyy","iu-Cans-CA":"d/M/yyyy","sr-Latn-RS":"d.M.yyyy","si-LK":"yyyy-MM-dd","sr-Cyrl-RS":"d.M.yyyy","lo-LA":"dd/MM/yyyy","km-KH":"yyyy-MM-dd","cy-GB":"dd/MM/yyyy","bo-CN":"yyyy/M/d","sms-FI":"d.M.yyyy","as-IN":"dd-MM-yyyy","ml-IN":"dd-MM-yy","en-IN":"dd-MM-yyyy","or-IN":"dd-MM-yy","bn-IN":"dd-MM-yy","tk-TM":"dd.MM.yy","bs-Latn-BA":"d.M.yyyy","mt-MT":"dd/MM/yyyy","sr-Cyrl-ME":"d.M.yyyy","se-FI":"d.M.yyyy","zu-ZA":"yyyy/MM/dd","xh-ZA":"yyyy/MM/dd","tn-ZA":"yyyy/MM/dd","hsb-DE":"d. M. yyyy","bs-Cyrl-BA":"d.M.yyyy","tg-Cyrl-TJ":"dd.MM.yy","sr-Latn-BA":"d.M.yyyy","smj-NO":"dd.MM.yyyy","rm-CH":"dd/MM/yyyy","smj-SE":"yyyy-MM-dd","quz-EC":"dd/MM/yyyy","quz-PE":"dd/MM/yyyy","hr-BA":"d.M.yyyy.","sr-Latn-ME":"d.M.yyyy","sma-SE":"yyyy-MM-dd","en-SG":"d/M/yyyy","ug-CN":"yyyy-M-d","sr-Cyrl-BA":"d.M.yyyy","es-US":"M/d/yyyy"};
    

    DateFormat = {
        MONTH_NAMES: ['January','February','March','April','May','June','July','August','September','October','November','December','Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'],
        DAY_NAMES: ['Sunday','Monday','Tuesday','Wednesday','Thursday','Friday','Saturday','Sun','Mon','Tue','Wed','Thu','Fri','Sat'],
        LZ: function(x) {return(x<0||x>9?"":"0")+x},
        compareDates: function(date1,dateformat1,date2,dateformat2) {
            var d1=DateFormat.parseFormat(date1,dateformat1);
            var d2=DateFormat.parseFormat(date2,dateformat2);
            if (d1==0 || d2==0) return -1;
            else if (d1 > d2) return 1;
            return 0;
        },
        format: function(date,format) {
            format=format+"";
            var result="";
            var i_format=0;
            var c="";
            var token="";
            var y=date.getYear()+"";
            var M=date.getMonth()+1;
            var d=date.getDate();
            var E=date.getDay();
            var H=date.getHours();
            var m=date.getMinutes();
            var s=date.getSeconds();
            var yyyy,yy,MMM,MM,dd,hh,h,mm,ss,ampm,HH,H,KK,K,kk,k;
            // Convert real date parts into formatted versions
            var value=new Object();
            if (y.length < 4) {y=""+(y-0+1900);}
            value["y"]=""+y;
            value["yyyy"]=y;
            value["yy"]=y.substring(2,4);
            value["M"]=M;
            value["MM"]=DateFormat.LZ(M);
            value["MMM"]=DateFormat.MONTH_NAMES[M-1];
            value["NNN"]=DateFormat.MONTH_NAMES[M+11];
            value["d"]=d;
            value["dd"]=DateFormat.LZ(d);
            value["E"]=DateFormat.DAY_NAMES[E+7];
            value["EE"]=DateFormat.DAY_NAMES[E];
            value["H"]=H;
            value["HH"]=DateFormat.LZ(H);
            if (H==0){value["h"]=12;}
            else if (H>12){value["h"]=H-12;}
            else {value["h"]=H;}
            value["hh"]=DateFormat.LZ(value["h"]);
            if (H>11){value["K"]=H-12;} else {value["K"]=H;}
            value["k"]=H+1;
            value["KK"]=DateFormat.LZ(value["K"]);
            value["kk"]=DateFormat.LZ(value["k"]);
            if (H > 11) { value["a"]="PM"; }
            else { value["a"]="AM"; }
            value["m"]=m;
            value["mm"]=DateFormat.LZ(m);
            value["s"]=s;
            value["ss"]=DateFormat.LZ(s);
            while (i_format < format.length) {
                c=format.charAt(i_format);
                token="";
                while ((format.charAt(i_format)==c) && (i_format < format.length))
                    token += format.charAt(i_format++);
                if (value[token] != null) result += value[token];
                else result += token;
            }
            return result;
        },
        _isInteger: function(val) {
            var digits="1234567890";
            for (var i=0; i < val.length; i++)
                if (digits.indexOf(val.charAt(i))==-1) return false;
            return true;
        },
        _getInt: function(str,i,minlength,maxlength) {
            for (var x=maxlength; x>=minlength; x--) {
                var token=str.substring(i,i+x);
                if (token.length < minlength) return null;
                if (DateFormat._isInteger(token)) return token;
            }
            return null;
        },
        parseFormat: function(val,format) {
            val=val+"";
            format=format+"";
            var i_val=0;
            var i_format=0;
            var c="";
            var token="";
            var token2="";
            var x,y;
            var now=new Date();
            var year=now.getYear();
            var month=now.getMonth()+1;
            var date=1;
            var hh=now.getHours();
            var mm=now.getMinutes();
            var ss=now.getSeconds();
            var ampm="";

            while (i_format < format.length) {
                // Get next token from format string
                c=format.charAt(i_format);
                token="";
                while ((format.charAt(i_format)==c) && (i_format < format.length))
                    token += format.charAt(i_format++);
                // Extract contents of value based on format token
                if (token=="yyyy" || token=="yy" || token=="y") {
                    if (token=="yyyy") x=4;y=4;
                    if (token=="yy") x=2;y=2;
                    if (token=="y") x=2;y=4;
                    year=DateFormat._getInt(val,i_val,x,y);
                    if (year==null) return 0;
                    i_val += year.length;
                    if (year.length==2) {
                        if (year > 70) year=1900+(year-0);
                        else year=2000+(year-0);
                    }
                } else if (token=="MMM"||token=="NNN") {
                    month=0;
                    for (var i=0; i<DateFormat.MONTH_NAMES.length; i++) {
                        var month_name=DateFormat.MONTH_NAMES[i];
                        if (val.substring(i_val,i_val+month_name.length).toLowerCase()==month_name.toLowerCase()) {
                            if (token=="MMM"||(token=="NNN"&&i>11)) {
                                month=i+1;
                                if (month>12) month -= 12;
                                i_val += month_name.length;
                                break;
                            }
                        }
                    }
                    if ((month < 1)||(month>12)) return 0;
                } else if (token=="EE"||token=="E") {
                    for (var i=0; i<DateFormat.DAY_NAMES.length; i++) {
                        var day_name=DateFormat.DAY_NAMES[i];
                        if (val.substring(i_val,i_val+day_name.length).toLowerCase()==day_name.toLowerCase()) {
                            i_val += day_name.length;
                            break;
                        }
                    }
                } else if (token=="MM"||token=="M") {
                    month=DateFormat._getInt(val,i_val,token.length,2);
                    if(month==null||(month<1)||(month>12)) return 0;
                    i_val+=month.length;
                } else if (token=="dd"||token=="d") {
                    date=DateFormat._getInt(val,i_val,token.length,2);
                    if(date==null||(date<1)||(date>31)) return 0;
                    i_val+=date.length;
                } else if (token=="hh"||token=="h") {
                    hh=DateFormat._getInt(val,i_val,token.length,2);
                    if(hh==null||(hh<1)||(hh>12)) return 0;
                    i_val+=hh.length;
                } else if (token=="HH"||token=="H") {
                    hh=DateFormat._getInt(val,i_val,token.length,2);
                    if(hh==null||(hh<0)||(hh>23)) return 0;
                    i_val+=hh.length;
                } else if (token=="KK"||token=="K") {
                    hh=DateFormat._getInt(val,i_val,token.length,2);
                    if(hh==null||(hh<0)||(hh>11)) return 0;
                    i_val+=hh.length;
                } else if (token=="kk"||token=="k") {
                    hh=DateFormat._getInt(val,i_val,token.length,2);
                    if(hh==null||(hh<1)||(hh>24)) return 0;
                    i_val+=hh.length;hh--;
                } else if (token=="mm"||token=="m") {
                    mm=DateFormat._getInt(val,i_val,token.length,2);
                    if(mm==null||(mm<0)||(mm>59)) return 0;
                    i_val+=mm.length;
                } else if (token=="ss"||token=="s") {
                    ss=DateFormat._getInt(val,i_val,token.length,2);
                    if(ss==null||(ss<0)||(ss>59)) return 0;
                    i_val+=ss.length;
                } else if (token=="a") {
                    if (val.substring(i_val,i_val+2).toLowerCase()=="am") ampm="AM";
                    else if (val.substring(i_val,i_val+2).toLowerCase()=="pm") ampm="PM";
                    else return 0;
                    i_val+=2;
                } else {
                    if (val.substring(i_val,i_val+token.length)!=token) return 0;
                    else i_val+=token.length;
                }
            }
            // If there are any trailing characters left in the value, it doesn't match
            if (i_val != val.length) return 0;
            // Is date valid for month?
            if (month==2) {
                // Check for leap year
                if (((year%4==0)&&(year%100 != 0)) || (year%400==0)) { // leap year
                    if (date > 29) return 0;
                } else if (date > 28) {
                    return 0;
                }
            }
            if ((month==4)||(month==6)||(month==9)||(month==11))
                if (date > 30) return 0;
            // Correct hours value
            if (hh<12 && ampm=="PM") hh=hh-0+12;
            else if (hh>11 && ampm=="AM") hh-=12;
            var newdate=new Date(year,month-1,date,hh,mm,ss);
            return newdate;
        },
        parse: function(val, format) {
            if (format) {
                return DateFormat.parseFormat(val, format);
            } else {
                var preferEuro=(arguments.length==2)?arguments[1]:false;
                var generalFormats=new Array('y-M-d','MMM d, y','MMM d,y','y-MMM-d','d-MMM-y','MMM d');
                var monthFirst=new Array('M/d/y','M-d-y','M.d.y','MMM-d','M/d','M-d');
                var dateFirst =new Array('d/M/y','d-M-y','d.M.y','d-MMM','d/M','d-M');
                var checkList=[generalFormats,preferEuro?dateFirst:monthFirst,preferEuro?monthFirst:dateFirst];
                var d=null;
                for (var i=0; i<checkList.length; i++) {
                    var l=checkList[i];
                    for (var j=0; j<l.length; j++) {
                        d=DateFormat.parseFormat(val,l[j]);
                        if (d!=0) return new Date(d);
                    }
                }
                return null;
            }
        },
        toArray: function(date, stub) {
            if (date && date.getMonth) {
                return [date.getFullYear(), date.getMonth(), date.getDate()];
            } else {
                return stub;
            }
        },
        getLocaleShortDateString: function(d) {
          var l=navigator.language?navigator.language:navigator['userLanguage'],y=d.getFullYear(),m=d.getMonth()+1,d=d.getDate();
          var f=(l in dateFormats)?dateFormats[l]:"MM/dd/yyyy";
          function z(s){s=''+s;return s.length>1?s:'0'+s;}
          f=f.replace(/yyyy/,y);f=f.replace(/yy/,String(y).substr(2));
          f=f.replace(/MM/,z(m));f=f.replace(/M/,m);
          f=f.replace(/dd/,z(d));f=f.replace(/d/,d);
          return f;
      },
      getLocaleDateFormat: function() {
          var l=navigator.language?navigator.language:navigator['userLanguage'];
          var f=(l in dateFormats)?dateFormats[l]:"MM/dd/yyyy";
          return f;
      },
      getLocalizedShortWeekdays: function (locale, startDayIndex) {
        const weekdays = [];
        const date = new Date();
        const options = { weekday: 'short' };
      
        date.setDate(date.getDate() - date.getDay() + startDayIndex); // Set the starting day
      
        for (let i = 0; i < 7; i++) {
          const formattedWeekday = date.toLocaleDateString(locale, options);
          weekdays.push(formattedWeekday);
          date.setDate(date.getDate() + 1); // Move to the next day
        }
      
        return weekdays;
      }
    }

    AGN.Lib.DateFormat = DateFormat;

})();