
function setMedia(video,imgId) {
    video.currentTime=0;
    video.oncanplay=function(){
        // 拿到图片
        let canvas = document.createElement('canvas');
        canvas.width = video.videoWidth * 0.8;
        canvas.height = video.videoHeight * 0.8;
        console.log(canvas.width+','+canvas.height)
        canvas.getContext('2d').drawImage(video, 0, 0, canvas.width, canvas.height);
        let src = canvas.toDataURL('image/png');
        $('#aa').attr('src',src)
        $.post('/tool/updateMediaBase64',{base64:src,mediaId:imgId})
        // $(video).removeAttr('oncanplay')
    }

}
function STR_IFNULL(str,reStr) {if(!str||str==null||str=='null'||$.trim(str).length==0)return reStr;else return str;}
function ARR_IFNULL(arr,reArr) {if(!arr||arr==null||arr.length==0)return reArr;else return arr;}
function getParam(paramName) {
    var reg = new RegExp("(^|&)" + paramName + "=([^&]*)(&|$)");
    var r = window.location.search.substr(1).match(reg);
    if (r != null) return decodeURI(r[2]);
    return null;
}
function IsPC() {
    var userAgentInfo = navigator.userAgent;
    var Agents = ["Android", "iPhone",
        "SymbianOS", "Windows Phone",
        "iPad", "iPod"];
    var flag = true;
    for (var v = 0; v < Agents.length; v++) {
        if (userAgentInfo.indexOf(Agents[v]) > 0) {
            flag = false;
            break;
        }
    }
    return flag;
}
Date.prototype.format = function(fmt)

{ //author: meizz

    var o = {

        "M+" : this.getMonth()+1,         //月份

        "d+" : this.getDate(),          //日

        "h+" : this.getHours(),          //小时

        "m+" : this.getMinutes(),         //分

        "s+" : this.getSeconds(),         //秒

        "q+" : Math.floor((this.getMonth()+3)/3), //季度

        "S" : this.getMilliseconds()       //毫秒

    };

    if(/(y+)/.test(fmt))

        fmt=fmt.replace(RegExp.$1, (this.getFullYear()+"").substr(4 - RegExp.$1.length));

    for(var k in o)

        if(new RegExp("("+ k +")").test(fmt))

            fmt = fmt.replace(RegExp.$1, (RegExp.$1.length==1) ? (o[k]) : (("00"+ o[k]).substr((""+ o[k]).length)));

    return fmt;

}