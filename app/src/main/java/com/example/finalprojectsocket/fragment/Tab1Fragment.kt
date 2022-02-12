package com.example.finalprojectsocket.fragment

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finalprojectsocket.activity.MainActivity
import com.example.finalprojectsocket.R
import com.example.finalprojectsocket.modle.SocketCreate
import com.example.finalprojectsocket.adapter.UserAdapter
import com.example.finalprojectsocket.modle.users
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.Socket
import kotlinx.android.synthetic.main.fragment_tab1.view.*
import org.json.JSONArray
import org.json.JSONObject

class Tab1Fragment :Fragment(){

    private val onlineUser: ArrayList<users> = ArrayList()


    private var user_id = "2"
    var userName: String? = null
    var userId: String? = null
    private var rvAdapter: RecyclerView.Adapter<*>? = null
    private var layoutManager: LinearLayoutManager? = null
    lateinit var app: SocketCreate
    private var mSocket: Socket? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root =  inflater.inflate(R.layout.fragment_tab1, container, false)
        app = activity!!.application as SocketCreate
        mSocket = app.getSocket()

        val sharedPref = activity!!.getSharedPreferences("MyPref", Context.MODE_PRIVATE)
        userName = activity!!.intent.getStringExtra("username")
        userId = activity!!.intent.getStringExtra("userId")
        //image = sharedPref.getString("imageuserAuth", "").toString()

        if (userId.equals(null)) {

            userId = sharedPref.getString("useridAuth", "").toString()
            userName = sharedPref.getString("usernameAuth", "").toString()
            sharedPref.edit()
            //sharedPref.clear//
        }

        mSocket!!.on(Socket.EVENT_CONNECT_ERROR) {
            getActivity()!!.runOnUiThread {
                Log.e("EVENT_CONNECT_ERROR", "EVENT_CONNECT_ERROR: ")//,)
            }
        }
        mSocket!!.on(Socket.EVENT_CONNECT_TIMEOUT, Emitter.Listener {
            getActivity()!!.runOnUiThread {
                Log.e("EVENT_CONNECT_TIMEOUT", "EVENT_CONNECT_TIMEOUT: ")//,)

            }
        })


        mSocket!!.on(
            Socket.EVENT_CONNECT
        ) { Log.e("onConnect", "Socket Connected!") }
        mSocket!!.on(Socket.EVENT_DISCONNECT, Emitter.Listener {
            getActivity()!!.runOnUiThread {
                Log.e("onDisconnect", "Socket onDisconnect!")

            }
        })
        //mSocket!!.on("userOnline", useronline)
        mSocket!!.on("onlineUser", ArrayonlineUser)//
         mSocket!!.on("message", onNewMessage)


        mSocket!!.emit("UsersOnline", "")
        mSocket!!.on("UsersOnline", ArrayonlineUser)


        mSocket!!.connect()

        layoutManager = LinearLayoutManager(requireContext())
        root.recyclerUserOline.layoutManager = layoutManager
        rvAdapter = UserAdapter(requireContext(), onlineUser)
        root.recyclerUserOline.adapter = rvAdapter

        return root

    }
   val onNewMessage = Emitter.Listener { args ->

        getActivity()!!.runOnUiThread {

            Log.e("hzm", args.toString())


            try {

                val message = args[0] as JSONObject


                var textMessage = message.getString("message")
                var from_id_Message = message.getString("source_id")
                var des_id = message.getString("des_id")
                var des_name = message.getString("name_des")


                Log.e("hzm", message.toString())
                //  messageArray.add(com.app.socketchatdemo.modle.message(fromMessage, textMessage))
                if (userId.equals(des_id)) {
                    createNotificationChannel(textMessage,from_id_Message,des_name)
                    Toast.makeText(requireContext(), message.getString("message"), Toast.LENGTH_SHORT).show()

                }
            } catch (e: Exception) {
                Log.e("hzm", e.toString())
            }

        }
    }

    private fun createNotificationChannel(message:String,id:String,name:String) {//,username:String
        val CHANNEL_ID = "ServiceChannelExample"
        var manager: NotificationManager?=null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(CHANNEL_ID, "Example Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT)
            manager = getActivity()!!.getSystemService(NotificationManager::class.java)
            manager!!.createNotificationChannel(serviceChannel)
        } else {
            manager = getActivity()!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        }
        val notificationIntent = Intent(requireContext(), MainActivity::class.java)
        /* notificationIntent.putExtra("id2",id)
         notificationIntent.putExtra("name2",name)*/

        Log.e("notificationIntent", "userName : $name $$ userId : $id")

        val pendingIntent = PendingIntent.getActivity(requireContext(), 0, notificationIntent, 0)

        val notification = NotificationCompat.Builder(requireContext(), CHANNEL_ID)
            .setContentTitle(name)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_send_black_24dp)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_launcher_background))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        manager!!.notify(1,notification)
    }
    val ArrayonlineUser = Emitter.Listener { args ->
        onlineUser.clear()
        getActivity()!!.runOnUiThread {
            try {
                var user = args[0] as JSONArray
             //   Log.e("hzm",user.toString())

                for (i in 0 until user.length()) {
                    val userToJsonObject = user.getJSONObject(i)

                    var nameuser = userToJsonObject.getString("username")
                    var iduser = userToJsonObject.getString("userId")
                    var imageUser = userToJsonObject.getString("image")


                    if (iduser != userId) {

                        onlineUser.add(users(nameuser, iduser,imageUser))
                        //dialogUserOnline.add(dialog(nameuser,false))

                    }
                }


                 for (i in 0 until onlineUser.size) {
                     Log.e("forloop12020",onlineUser[i].username)
                      for (j in i+1  until onlineUser.size) {
                          if (onlineUser[i] === onlineUser[j]) {
                              Log.e("username",onlineUser[i].username)
                              Log.e("username",onlineUser[j].username)

                              onlineUser[j] = users("","","")
                          }
                      }
                  }

                for (i in 0 until onlineUser.size) {

               /*     val nameuser1 = onlineUser[i].username
                    val id = onlineUser[i].userId
                    val image = onlineUser[i].image
                    Log.d("forr loop", "$nameuser1 $id")
*/
                    //   if(nameuser1 != "" && id !=""){
                    // }
                    if (onlineUser[i] ==users("","","")){
                        onlineUser.remove(onlineUser[i])
                    }




                }

                // Log.e("hzm",onlineUser.toString())
                /*for (i in 0 until onlineUser.size) {

                    val nameuser1 = onlineUser[i].username
                 Log.e("hzm",nameuser1)


                }*/
                rvAdapter!!.notifyDataSetChanged()


            } catch (e: Exception) {
                Log.e("hzm", e.toString())
            }

        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

}