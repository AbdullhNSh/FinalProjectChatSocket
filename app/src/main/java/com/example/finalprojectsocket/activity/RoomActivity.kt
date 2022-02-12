package com.example.finalprojectsocket.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finalprojectsocket.R
import com.example.finalprojectsocket.modle.SocketCreate
import com.example.finalprojectsocket.adapter.RoomAdapter
import com.example.finalprojectsocket.modle.message
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.Socket
import com.vansuita.pickimage.bean.PickResult
import com.vansuita.pickimage.bundle.PickSetup
import com.vansuita.pickimage.dialog.PickImageDialog
import com.vansuita.pickimage.listeners.IPickResult
import kotlinx.android.synthetic.main.activity_room.*
import kotlinx.android.synthetic.main.activity_room.ed_messege
import kotlinx.android.synthetic.main.activity_room.img_send
import kotlinx.android.synthetic.main.activity_room.img_send_img
import kotlinx.android.synthetic.main.activity_room.recyclerMessage
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class RoomActivity : AppCompatActivity() , IPickResult {

    var messageArray = ArrayList<message>()
    lateinit var app: SocketCreate
    private var mSocket: Socket? = null
    private var from_user_id: String? = null
    var from_user_Name: String? =null
        private var time = 2
    private var thread2: Thread? = null
    private var startTyping = false


    private var rvAdapter: RecyclerView.Adapter<*>? = null
    private var layoutManager: LinearLayoutManager? = null


    @SuppressLint("HandlerLeak")
    var handler2: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            Log.i(
                "hzm",
                "handleMessage: typing stopped $startTyping"
            )
            if (time == 0) {
                Appbar.text = "Room"
                Log.i(
                    "hzm",
                    "handleMessage: typing stopped time is $time"
                )
                startTyping = false
                time = 2
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room)

        val sharedPref = getSharedPreferences("MyPref", Context.MODE_PRIVATE)
        from_user_id = sharedPref.getString("useridAuth", "").toString()
        from_user_Name = sharedPref.getString("usernameAuth", "").toString()
        sharedPref.edit()



        if (from_user_id.equals("")) {

            val intent = Intent(this, Signup::class.java)
            startActivity(intent)
        }


        getSupportActionBar()!!.hide();

        app = application as SocketCreate
        mSocket = app.getSocket()

        mSocket!!.on(Socket.EVENT_CONNECT_ERROR) {
            runOnUiThread {
                Log.e("EVENT_CONNECT_ERROR", "EVENT_CONNECT_ERROR: ")//,)
            }
        }
        mSocket!!.on(Socket.EVENT_CONNECT_TIMEOUT, Emitter.Listener {
            runOnUiThread {
                Log.e("EVENT_CONNECT_TIMEOUT", "EVENT_CONNECT_TIMEOUT: ")//,)

            }
        })


        mSocket!!.on(
            Socket.EVENT_CONNECT
        ) { Log.e("onConnect", "Socket Connected!") }
        mSocket!!.on(Socket.EVENT_DISCONNECT, Emitter.Listener {
            runOnUiThread {
                Log.e("onDisconnect", "Socket onDisconnect!")

            }
        })
        //mSocket!!.on("userOnline", useronline)
        //mSocket!!.on("onlineUser", ArrayonlineUser)
        mSocket!!.on("messageRoom", onNewMessage)

        mSocket!!.on("on typing", onTyping)
        mSocket!!.on("image",onNewImage)
        mSocket!!.connect()

        layoutManager = LinearLayoutManager(this)

        recyclerMessage.layoutManager = layoutManager

        rvAdapter = RoomAdapter(this,messageArray, from_user_id!!)
        onTypeButtonEnable()
//        mSocket!!.emit("UsersOnline", "")
  //      mSocket!!.on("UsersOnline", ArrayonlineUser)
        img_send_img.setOnClickListener {
            PickImageDialog.build(PickSetup()).show(this)
        }

        img_send.setOnClickListener {
            sendMessage()
        }

        mSocket!!.connect()
    }



    val onNewImage = Emitter.Listener { args ->

        runOnUiThread {

            Log.e("hzm", args.toString())


            try {
                val message = args[0] as JSONObject

                var textMessage = message.getString("message")
                var fromMessage = message.getString("source_id")
                var time = message.getString("time")
                var image = message.getString("image")

                messageArray.add(message(fromMessage, textMessage, time,image))
                Toast.makeText(this, message.getString("message"), Toast.LENGTH_SHORT)


            } catch (e: Exception) {
                Log.e("hzm", e.toString())
            }
            rvAdapter!!.notifyDataSetChanged()


        }
    }

    val onNewMessage = Emitter.Listener { args ->

        runOnUiThread {

            Log.e("hzm", args.toString())


            try {
                val message = args[0] as JSONObject

                var textMessage = message.getString("message")
                var fromMessage = message.getString("source_id")
                var time = message.getString("time")
                // var image = message.getString("image")
                    Log.e("hzm", message.toString())
                    messageArray.add(message(fromMessage,textMessage,time, ""))


            } catch (e: Exception) {
                Log.e("hzm2020", e.toString())
            }
            rvAdapter!!.notifyDataSetChanged()


        }
    }




    private fun sendMessage() {

        val message = JSONObject()
        message.put("message", ed_messege.text.toString().trim())
        message.put("source_id", from_user_id)
        message.put("time", getCurrentTime())
        message.put("source_name", from_user_Name)


        mSocket!!.emit("message", message)

        ed_messege.setText("")

    }


    fun getCurrentTime(): String {
        //  val dateFormat: DateFormat = SimpleDateFormat("hh:mm a")

        val simpleDateFormat = SimpleDateFormat("h:mm a", Locale.US)
        return simpleDateFormat.format(Date())
    }

    fun onTypeButtonEnable() {
        ed_messege!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                charSequence: CharSequence,
                i: Int,
                i1: Int,
                i2: Int
            ) {
            }

            override fun onTextChanged(
                charSequence: CharSequence,
                i: Int,
                i1: Int,
                i2: Int
            ) {
                val onTyping = JSONObject()
                try {
                    onTyping.put("typing", true)
                    onTyping.put("username", from_user_Name)
                    onTyping.put("uniqueId", from_user_id)

                    mSocket!!.emit("on typing", onTyping)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                img_send!!.isEnabled = charSequence.toString().trim { it <= ' ' }.length > 0
            }

            override fun afterTextChanged(editable: Editable) {}
        })
    }

    var onTyping = Emitter.Listener { args ->
        runOnUiThread {
            val data = args[0] as JSONObject
            Log.i("hzm", "run: " + args[0])
            try {
                var typingOrNot = data.getBoolean("typing")
                val userName =
                    data.getString("username") + " is Typing......"
                val id = data.getString("uniqueId")
                val toId = data.getString("toId")

                    if (id == from_user_id) {
                        typingOrNot = false
                    } else {
                        Appbar.text = userName
                    }
                    if (typingOrNot) {
                        if (!startTyping) {
                            startTyping = true
                            thread2 = Thread(
                                object : Runnable {
                                    override fun run() {
                                        while (time > 0) {
                                            synchronized(this) {
                                                try {
                                                    //wait(1000)
                                                    Thread.sleep(500)
                                                    /*  Handler().postDelayed(
                                                      {
                                                          // This method will be executed once the timer is over
                                                      },
                                                      1000 // value in milliseconds
                                                  )*/

                                                    Log.i(
                                                        "hzm",
                                                        "run: typing $time"
                                                    )
                                                } catch (e: InterruptedException) {
                                                    e.printStackTrace()
                                                }
                                                time--
                                            }
                                            handler2.sendEmptyMessage(0)
                                        }
                                    }
                                }
                            )
                            thread2!!.start()
                        } else {
                            time = 2
                        }
                    }

            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        recyclerMessage.adapter = rvAdapter

    }

    override fun onStart() {
        super.onStart()
        Log.e("getCurrentTime() : ", getCurrentTime())

    }


    override fun onPickResult(r: PickResult?) {
        // img.setImageBitmap(r!!.bitmap)

        val message = JSONObject()
        message.put("message", ed_messege.text.toString().trim())
        message.put("source_id", from_user_id)
        message.put("time", getCurrentTime())
        message.put("image", imageToString(r!!.bitmap))
        mSocket!!.emit("image",message)
    }

    fun decodeBase64(input: String?): Bitmap? {
        val bytes = Base64.decode(input, 0)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    private fun imageToString(bitmap: Bitmap): String? {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val imgBytes = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(imgBytes, Base64.DEFAULT)
    }


}