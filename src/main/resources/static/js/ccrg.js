function getMyName() {
    $.ajax({
        url: "/users/me",
        method: "GET",
        success:function(id) {
            me = id.name;
            document.getElementById("user_name").innerHTML = me;
            announceMyArrival();
        }
    })
}

function checkCommands(message) {
    var toggleScrollExp = /^\/togglescroll/
    var noScrollExp = /^\/noscroll/
    var scrollExp = /^\/scroll/
    var empty = /^\s*$/
    var afk = /^\/afk/
    var kick = /^\/kick (.*)/
    if (kick.test(message)) {
	    var match = kick.exec(message);
        kickUser(match[1])
        return false
    } if(toggleScrollExp.test(message)) {
        scrollMode = !scrollMode
        return false
    } if(noScrollExp.test(message)) {
        scrollMode = false
        return false
    } if(scrollExp.test(message)) {
        scrollMode = true
        return false
    } if(afk.test(message)) {
        setAfk()
        return false
    } if(empty.test(message)) {
        var date = new Date()
        addMessages({messages:[{"author":"Local announcement", "content":"Vous ne pouvez pas envoyer de message vide", "timestamp":{"hour":date.getHours(),"minute":date.getMinutes()}}]})
        return false
     } else {
        return true
    }
}

function sendMessage() {
    var content = document.getElementById("message_prompt").value
    if(checkCommands(content)) {
        unsetAfk();
        $.ajax({
            url: "/messages/send",
            method: "POST",
            data: {author:me, content:content}
        })
    }
    document.getElementById("message_prompt").value = "";
}

function kickUser(user) {
    $.ajax({
        url: "/users/kick",
        method: "POST",
        data: {userToKick:user, kickingUser:me}
    })
}

function announceMyArrival() {
    $.ajax({
        url: "/users/announce",
        method: "POST",
        data: {user:me, isArrival:true}
    })
}

function announceMyDeparture() {
    $.ajax({
        url: "/users/announce",
        method: "POST",
        data: {user:me, isArrival:false},
        async: false
    })
    return null;
}

function getMessages() {
    var url = "/messages/longPoll?latestId=" + lastMessageId + "&user=" + me;
    $.ajax({
        url: url,
        method: "GET",
        success:addMessages,
        complete:getMessages
    })
    return false;
}

function addMessages(messageBatch) {
    var allMessages = "";
    if(!isActive) {
        unreadMessages += messageBatch.messages.filter(message => !message.author.toUpperCase().endsWith("BOT") && message.author !== "System announcement").length
        if(unreadMessages > 0) {
            document.title = "(" + unreadMessages + ") CCRG";
        }
    }
    messages = messages.concat(messageBatch.messages)
    messages.forEach(function(message) {
        if(message.specialType === 'kick') {
            kicked = true
            if(!window.alert(message.content)) {
                window.location.replace('http://www.staggeringbeauty.com/');
            };
        }
        var hourTemp = (message.timestamp.hour + 1) % 24
        var hour = hourTemp < 10 ? "0" + hourTemp : "" + hourTemp
        var minute = message.timestamp.minute < 10 ? "0" + message.timestamp.minute : "" + message.timestamp.minute
        var html = '<div class="message"><span class="message_timestamp">' + hour + ":" + minute
        + '</span><span class="message_author"> ' + message.author
        + '</span><span class="message_content">> ' + message.content + '</span></div>';
        allMessages = allMessages + html;
    })
    lastMessageId = messageBatch.latestId
    document.getElementById("main").innerHTML = allMessages
    if(messageBatch.messages.length > 0) {
        scrollToBottom()
    }
}

function getActiveUsers() {
    var url = "/users/get";
    $.ajax({
        url: url,
        method: "GET",
        success:function(userList) {
            var allUsers = "";
            userList.forEach(function(user) {
                var name = user.name;
                var htmlClass = 'user_name';
                if (user.afk) {
                    htmlClass = htmlClass + ' afk';
                    name = name + ' (afk)';
                }
                var html = '<div class="user"><span class="' + htmlClass + '">' + name + '</span>';
                allUsers = allUsers + html;
            })
            document.getElementById("online").innerHTML = allUsers
        }
    })
    return false;
}

function keepAlive() {
    var url = "/users/keepalive";
    $.ajax({
        url: url,
        method: "GET",
        data: {user:me},
    });
}

function setAfk() {
    var url = "/users/setafk";
    $.ajax({
        url: url,
        method: "GET",
        data: {user:me},
    });
}

function unsetAfk() {
    var url = "/users/unsetafk";
    $.ajax({
        url: url,
        method: "GET",
        data: {user:me},
    });
}

function scrollToBottom() {
    if(scrollMode) {
        var objDiv = document.getElementById("main");
        objDiv.scrollTop = objDiv.scrollHeight;
    }
}