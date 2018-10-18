
String.prototype.trim = function(){
    return this.replace(/^\s+|\s+$/g, "");
};

/**
 * Dash to Camel Case
 * @returns {string}
 */
String.prototype.toCamelCase = function(){
    return this.replace(/(\-|\_[a-z])/g, function($1){return $1.toUpperCase().replace(/\-|\_/,'');});
};

/**
 * Camel Case to Dash
 * @returns {string}
 */
String.prototype.toDash = function(){
    return this.replace(/([A-Z])/g, function($1){return "-"+$1.toLowerCase();});
};

/**
 * Camel Case to Underscore
 * @returns {string}
 */
String.prototype.toUnderscore = function(){
    return this.replace(/([A-Z])/g, function($1){return "_"+$1.toLowerCase();});
};


/**
 * HashToObject
 * @returns {{}}
 */
String.prototype.hashToObject = function(){
    var hash = this.substr(1);
    var result = hash.split('&').reduce(
        function (result, item) {
            var parts = item.split('=');

            if($.isNumeric(parts[1])){
                result[parts[0]] = parseInt(parts[1]);
            }else{
                result[parts[0]] = parts[1];
            }

            return result;
        }, {});
    return result;
};

/**
 * HashString To Object
 * @returns {{}}
 */
String.prototype.hashToObject = function(){
    var hash = this.substr(1);
    var result = hash.split('&').reduce(
        function (result, item) {
            var parts = item.split('=');

            if($.isNumeric(parts[1])){
                result[parts[0]] = parseInt(parts[1]);
            }else{
                result[parts[0]] = parts[1];
            }

            return result;
        }, {});
    return result;
};


/**
 *
 * @param boolean
 * true 일 경우 dot 제거
 * @returns {string}
 */
String.prototype.getExtension = function(t){
    if(!t){
        return this.substring(this.lastIndexOf('.'), this.length).toLowerCase();
    }
    return this.substring(this.lastIndexOf('.')+1, this.length).toLowerCase();
};




var ObjectUtilsInit = {
    extend : function(defaultValue, newValue){
        for(var key in newValue)
            if(newValue.hasOwnProperty(key))
                defaultValue[key] = newValue[key];
        return defaultValue;
    }
};

var ObjectUtils = Object.create(ObjectUtilsInit);

var WebUtilsInit = {
    updateQueryString : function(newOptions) {
        var options = {
            key:null,
            value:null,
            url:null,
            encode:false
        };
        ObjectUtils.extend(options,newOptions);


        var key, value, url;

        key = options.key;
        value = options.value;
        url = options.url;

        if (!url) url = encodeURI(window.location.href);
        var re = new RegExp("([?&])" + key + "=.*?(&|#|$)(.*)", "gi"),
            hash;

        if (re.test(url)) {
            if (typeof value !== 'undefined' && value !== null)
                return url.replace(re, '$1' + key + "=" + value + '$2$3');
            else {
                hash = url.split('#');
                if(options.encode)
                    url = hash[0].replace(re, '$1$3').replace(/(%26|\?)$/, '');
                else
                    url = hash[0].replace(re, '$1$3').replace(/(&|\?)$/, '');
                if (typeof hash[1] !== 'undefined' && hash[1] !== null)
                    url += '#' + hash[1];
                return url;
            }
        }
        else {
            if (typeof value !== 'undefined' && value !== null) {
                var separator;
                if(options.encode)
                    separator = url.indexOf('?') !== -1 ? '%26' : '?';
                else
                    separator = url.indexOf('?') !== -1 ? '&' : '?';

                hash = url.split('#');
                url = hash[0] + separator + key + '=' + value;
                if (typeof hash[1] !== 'undefined' && hash[1] !== null)
                    url += '#' + hash[1];
                return url;
            }
            else
                return url;
        }
    },
    getParameterByName : function (name, url) {
        if (!url) url = window.location.href;
        name = name.replace(/[\[\]]/g, '\\$&');
        var regex = new RegExp('[?&]' + name + '(=([^&#]*)|&|#|$)'),
            results = regex.exec(url);
        if (!results) return null;
        if (!results[2]) return '';
        return decodeURIComponent(results[2].replace(/\+/g, ' '));
    },
    toHashParameters : function(obj,bind){
        var str = '#';
        var cnt = 1;
        if(bind == null){
            bind = true;
        }
        for(var key in obj){
            if(typeof obj[key] != 'function' && obj[key] != 'hash'){
                if(cnt > 1){
                    str+= '&'
                }
                str += (key+'='+obj[key]);
                cnt++;
            }
        }
        if(bind){
            document.location.hash = str;
        }

        return str;
    }
};



var WebUtils = Object.create(WebUtilsInit);

var CheckBoxUtilsInit = {
    checkAll : function (target, parent) {
        if(parent != null)
            $(parent+' input:checkbox').not(target).prop('checked', target.checked);
        else{
            $('input:checkbox').not(target).prop('checked', target.checked);
        }
    },
    checkItem : function (target, parent, allSelector) {
        if(!target.checked){
            $(parent+' '+allSelector).prop('checked', false);
        }else{
            var elems = $(parent+' input:checkbox').not(allSelector).get();
            var result = true;
            elems.forEach(function (elem){
                if(!$(elem).prop('checked')){
                    result = false;
                }
            });
            $(parent+' '+allSelector).prop('checked', result);
        }
    }
};

var CheckBoxUtils = Object.create(CheckBoxUtilsInit);




$.fn.serializeObject = function() {
    var obj = null; try {
        if(this[0].tagName && this[0].tagName.toUpperCase() == "FORM" ) {
            var arr = this.serializeArray(); if(arr){
                obj = {}; jQuery.each(arr, function() {
                    obj[this.name] = this.value;
                });
            }
        }
    }catch(e) { alert(e.message); }
    finally {

    } return obj;
}

