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

function sendMessage() {
    $.ajax({
        url: "/messages/send",
        method: "POST",
        data: {author:me, content:document.getElementById("message_prompt").value}
    })
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
        success:function(messageBatch) {
            var allMessages = "";
            messages = messages.concat(messageBatch.messages)
            messages.forEach(function(message) {
                var html = '<div class="message"><span class="message_timestamp">' + message.timestamp.substring(0, message.timestamp.length - 7)
                + '</span><span class="message_author"> ' + message.author
                + '</span><span class="message_content"> > ' + message.content + '</span></div>';
                allMessages = allMessages + html;
            })
            lastMessageId = messageBatch.latestId
            document.getElementById("main").innerHTML = allMessages
            getMessages()
        }
    })
    return false;
}