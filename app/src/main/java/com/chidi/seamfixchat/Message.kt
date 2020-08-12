package com.chidi.seamfixchat

class Message (
    val text: String,
    val time: String,
    var belongsToCurrentUser: Boolean = false
)