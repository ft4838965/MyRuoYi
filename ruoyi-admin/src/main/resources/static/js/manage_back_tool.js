function guid() {
    var S4=function() {
        return (((1+Math.random())*0x10000)|0).toString(16).substring(1);
    }
    return (S4()+S4()+S4()+S4()+S4()+S4()+S4()+S4());
}
var deleteHTML=function (id) {
    return ('<a class="btn btn-danger btn-xs " style="position: absolute;top: 0;right: 0;" href="javascript:void(0)" onclick="removeMedia(\''+id+'\')"><i class="fa fa-remove"></i>删除</a>');
};
var videoHtml=function (url) {
    return '<video id="modal-video" src="'+url+'" width="100%" data-dismiss="modal"  aria-hidden="true" controls></video>'
}
var liHtml=function(hasDelete,item){
    return ('<li style="float: left;margin: 0 8px 7px 0;position: relative;">\n' +
        '<input type="hidden" value="'+item.mediaId+'" name="mediaId"/>\n' +
        '<input type="hidden" value="'+item.baseSort+'" name="baseSort"/>\n' +
        '<div data-toggle="modal" onclick="' +
        (item.url.split('.')[item.url.split('.').length-1]=='mp4'?
                // ('$(\'#modal-video\').attr(\'src\',\''+item.url+'\').show();$(\'#modal-img\').attr(\'src\',\'\').hide();'):
                ('$(\'#modal-video\').replaceWith(videoHtml(\''+item.url+'\')).show();$(\'#modal-img\').attr(\'src\',\'\').hide();'):
                ('$(\'#modal-img\').attr(\'src\',\''+item.url+'\').show();$(\'#modal-video\').attr(\'src\',\'\').hide();')
        )+
        '" data-target="#myModal5" style="width: 200px;border: 1px solid #009688;border-radius: 5px;height: 100px;line-height: 100px;overflow: hidden;position: relative;">\n' +
        '    <div>' +
        (item.url.split('.')[item.url.split('.').length-1]=='mp4'?
                ('<video src="'+item.url+'" style="width: 100%;position: absolute;bottom: 0;" controls onloadeddata="setMedia(this,'+item.mediaId+')"></video>'):
                ('<img src="'+item.url+'" style="width: 100%;vertical-align: middle;"/>')
        )+
        '</div>\n' +
        '    <div style="clear: both;"></div>\n' +
        '</div>\n' +
        (hasDelete?deleteHTML(item.mediaId):'')+'</li>');
}
function removeMedia(mediaId) {
    var tagObj=$(window.event.target);
    var tagName=$(tagObj).prop("tagName");
    $.operate.saveModal('/yzyx/media/remove',{ids:mediaId},function (r) {
        if(r.code==0){
            switch (tagName) {
                case 'A':$(tagObj).parent().remove();
                case 'I':$(tagObj).parent().parent().remove();
            }
        }
    })
}

function saveSort(type) {
    if($('#'+type+'Item').children().length<1){
        $.modal.msgWarning('没有内容可保存');
        return;
    }
    var mediaItems=[],mediaIds=[],mediaCustoms=[];
    for(var i=0,len=$('#'+type+'Item').children().length;i<len;i++){
        var mediaHtml=$('#'+type+'Item').children()[i];
        mediaIds.push($($(mediaHtml).find('input[name="mediaId"]')[0]).val());
        mediaCustoms.push(parseInt($($(mediaHtml).find('input[name="baseSort"]')[0]).val()));
    }
    mediaCustoms.sort(function(a, b) {return b - a;});
    for(var i=0,len=mediaIds.length;i<len;i++){
        var media={};
        media['mediaId']=mediaIds[i];
        media['baseSort']=mediaCustoms[i];
        mediaItems.push(media);
    }
    $.ajax({
        type:"POST",
        url:"/tool/saveMediaSort",
        dataType:"json",
        contentType:"application/json", // 指定这个协议很重要
        data:JSON.stringify(mediaItems), //只有这一个参数，json格式，后台解析为实体，后台可以直接用
        success:function(data){
            $('#'+type+'Sort').stop(true)
            eval('clearInterval('+type+'Interval)');
            $('#'+type+'Sort').css({'color':'#FFFFFF','background-color':'#1ab394'})
            $.modal.msgSuccess('保存成功!');
        },
        error:function (e) {
            $.modal.msgError('发生错误:'+JSON.stringify(e));
        },
    });
}
