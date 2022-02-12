package com.example.finalprojectsocket.modle

import android.app.Application
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import java.net.URISyntaxException

class SocketCreate : Application() {

    private var mSocket: Socket? = IO.socket("http://192.168.1.116:8080")//192.168.43.161//192.168.1.116

  //  http://localhost:8080//4000

    fun getSocket(): Socket? {
        return mSocket
    }
}