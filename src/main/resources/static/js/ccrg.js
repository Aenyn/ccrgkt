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
    var toggleScrollExp = /\/togglescroll/
    var noScrollExp = /\/noscroll/
    var scrollExp = /\/scroll/
    var empty = /^\s*$/
    if(toggleScrollExp.test(message)) {
        scrollMode = !scrollMode
        return false
    } if(noScrollExp.test(message)) {
        scrollMode = false
        return false
    } if(scrollExp.test(message)) {
        scrollMode = true
        return false
    } if(empty.test(message)) {
        var date = new Date()
        addMessages([{"author":"Local announcement", "content":"Vous ne pouvez pas envoyer de message vide", "timestamp":{"hour":date.getHours(),"minute":date.getMinutes()}}])
        return false
     } else {
        return true
    }
}

function sendMessage() {
    var content = document.getElementById("message_prompt").value
    if(checkCommands(content)) {
        $.ajax({
            url: "/messages/send",
            method: "POST",
            data: {author:me, content:content}
        })
    }
    document.getElementById("message_prompt").value = "";
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
    var url = "/messages/longPoll?latestId=" + lastMessageId;
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
            unreadMessages += messageBatch.messages.length
            document.title = "(" + unreadMessages + ") CCRG";
        }
        messages = messages.concat(messageBatch.messages)
        messages.forEach(function(message) {
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
        scrollToBottom()
    }

function getActiveUsers() {
    var url = "/users/get";
    $.ajax({
        url: url,
        method: "GET",
        success:function(userList) {
            var allUsers = "";
            var userNames = userList.map(function(user) {
                return user.name
            });
            userNames.forEach(function(username) {
                var html = '<div class="user"><span class="user_name">' + username + '</span>';
                allUsers = allUsers + html;
            })
            document.getElementById("online").innerHTML = allUsers
        }
    })
    return false;
}

function scrollToBottom() {
    if(scrollMode) {
        var objDiv = document.getElementById("main");
        objDiv.scrollTop = objDiv.scrollHeight;
    }
}