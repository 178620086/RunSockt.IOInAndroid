$(document).ready(function() {
    function getC(name) {
        var strCookie = document.cookie;
        var arrCookie = strCookie.split("; ");
        for (var i = 0; i < arrCookie.length; i++) {
            var arr = arrCookie[i].split("=");
            if (arr[0] == name) return arr[1];
        }
        return null;
    }
    var socket = io.connect(location.host); //加入默认区域。这个区域用于测试
    demo.socket = socket;
    window.demo.isCreate = eval(getC('isC')); //判断是否是创建者
    
    var dial; //弹出框
    if (demo.isCreate) {
        dial = dialog({
            title: '点击开始按钮开始会议！',
            okValue: '会议开始',
            content: '<div class="ma2" width="120" ></div><h5 style="color: burlywood;">请参会人员使用浏览器扫描二维码加入</h5>或输入' + window.location.href,
            ok: function() {
                socket.emit("meetSatrt", {});
            }
        });
        dial.showModal();
    } else {
        dial = dialog({
            title: '请等待会议开始！',
            cancel: false,
            content: '<div class="ma2" width="120"></div><h5 style="color: burlywood;">请参会人员使用浏览器扫描二维码加入</h5>或输入' + window.location.href,
        });
        dial.showModal();

    }
    $(".ma2").qrcode(utf16to8(window.location.href)); //给ma赋值
    $(".ui-dialog-content").css("text-align", 'center');


    $("#conAdd").click(function(){
        socket.emit("meetSatrt", {});
    });
    function utf16to8(str) { //utf16-utf8转码 
        var out, i, len, c;
        out = "";
        len = str.length;
        for (i = 0; i < len; i++) {
            c = str.charCodeAt(i);
            if ((c >= 0x0001) && (c <= 0x007F)) {
                out += str.charAt(i);
            } else if (c > 0x07FF) {
                out += String.fromCharCode(0xE0 | ((c >> 12) & 0x0F));
                out += String.fromCharCode(0x80 | ((c >> 6) & 0x3F));
                out += String.fromCharCode(0x80 | ((c >> 0) & 0x3F));
            } else {
                out += String.fromCharCode(0xC0 | ((c >> 6) & 0x1F));
                out += String.fromCharCode(0x80 | ((c >> 0) & 0x3F));
            }
        }
        return out;
    }

    socket.on("connect", function() {
        console.log("与服务器连接成功！");
    });
    socket.on("connect_error", function() {
        console.log("与服务器端连接错误！！");
    });
    socket.on("disconnect", function() {
        console.log("服务器断开连接");
    });
    socket.on("connect_timeout", function() {
        console.log("与服务器端连接超时！！！");
    });

    socket.on("dwing", function(data) {
        lc.saveShape(LC.JSONToShape(data), false); //保存服务器发过来的图形 并且不再触发saveshape事件
    });

    socket.on('reconnect', function(data) {
        if (data) {
            lc.loadSnapshotJSON(data);
        }
    });
    socket.on('meetSatrt', function() {
        setTimeout(function() {
            dial.close().remove();
        }, 200);
    });
    /**
     **事件类型：type:
     **1:缩放，2:前进redo，3:后退undo，4:清空
     **/
    socket.on('event', function(event) {
        switch (event.type) {
            case 1:
                // lc.zoom(event.data)
                break;
            case 2:
                lc.redo(true); //不触发redo事件
                break;
            case 3:
                lc.undo(true); //不触发undo事件
                break;
            case 4:
                lc.clear(true); //不触发clear事件
                break;
        }
    });

    lc.on("undo", function() {
        var event = {
            type: 3,
            data: null
        };
        socket.emit("event", event);
    });
    lc.on("redo", function() {
        var event = {
            type: 2,
            data: null
        };
        socket.emit("event", event);
    });
    lc.on("clear", function() {
        var event = {
            type: 4,
            data: null
        };
        socket.emit("event", event);
    });
    lc.on("shapeSave", function(shape) {
        socket.emit("dwing", LC.shapeToJSON(shape.shape));
    });
});
