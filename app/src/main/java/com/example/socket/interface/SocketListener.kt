package com.example.socket.`interface`

interface SocketListener {
    fun onSocketConnected()
    fun onSocketDisconnected()
    fun onNewMessageReceived(username: String?, message: String?)
}