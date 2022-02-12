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
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finalprojectsocket.R
import com.example.finalprojectsocket.modle.SocketCreate
import com.example.finalprojectsocket.adapter.MessageAdapter
import com.example.finalprojectsocket.modle.message
import com.example.finalprojectsocket.modle.users
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.Socket
import com.vansuita.pickimage.bean.PickResult
import com.vansuita.pickimage.bundle.PickSetup
import com.vansuita.pickimage.dialog.PickImageDialog
import com.vansuita.pickimage.listeners.IPickResult
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity(), IPickResult {

    lateinit var app: SocketCreate
    private var mSocket: Socket? = null
    private var from_user_id: String? = null
    var from_user_Name: String? = null
    var to_user_Name: String? = null
    var to_user_Id: String? = null
    var to_user_image: String? = null


    var onlineUser = ArrayList<users>()
    var messageArray = ArrayList<message>()

    private var time = 2
    private var thread2: Thread? = null
    private var startTyping = false


    private var rvAdapter: RecyclerView.Adapter<*>? = null
    private var layoutManager: LinearLayoutManager? = null
    private var txtToolBar:TextView? = null
    private var image_to:ImageView? = null
    private var stateUser:TextView?=null

    @SuppressLint("HandlerLeak")
    var handler2: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            Log.i(
                "hzm",
                "handleMessage: typing stopped $startTyping"
            )
            if (time == 0) {
                txtToolBar!!.text = to_user_Name
                stateUser!!.text = "Connection"
                Log.i(
                    "hzm",
                    "handleMessage: typing stopped time is $time"
                )
                startTyping = false
                time = 2
            }
        }
    }


    @SuppressLint("CommitPrefEdits")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        image_to = findViewById(R.id.image_to)

        txtToolBar = findViewById(R.id.txtToolBar)
        stateUser = findViewById(R.id.stateUser)

        
        back.setOnClickListener { 
            val intent = Intent(this,
                MainActivity3::class.java)
            startActivity(intent)
        }
        supportActionBar!!.hide()
        val sharedPref = getSharedPreferences("MyPref", Context.MODE_PRIVATE)
        from_user_id = sharedPref.getString("useridAuth", "").toString()
        from_user_Name = sharedPref.getString("usernameAuth", "").toString()
        sharedPref.edit()

      //intent.getStringExtra("name")
        to_user_Name= intent.getStringExtra("name")
        to_user_Id = intent.getStringExtra("id")
        to_user_image= intent.getStringExtra("image")
        Log.e("hzm", "userName : $to_user_Name $$ userId : $to_user_Id")
        txtToolBar!!.text = to_user_Name

        if (from_user_id.equals("")) {

            val intent = Intent(this, Signup::class.java)
            startActivity(intent)
        }

        if (to_user_Id.equals(null)) {

            val intent = Intent(this, MainActivity3::class.java)
            startActivity(intent)
        }

        image_to!!.setImageBitmap(decodeBase64(to_user_image))

/*
        to_user_Name = intent.getStringExtra("name")
        to_user_Id = intent.getStringExtra("id")
        title = to_user_Name

        Log.e("hzm", "userName : $to_user_Name $$ userId : $to_user_Id")*/

        //userName!!,userId!!

        app = application as SocketCreate
        mSocket = app.getSocket()
        //joinNewUser(userName!!, userId!!)


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
        //mSocket!!.on("joinUser", onNewUser)
        // mSocket!!.on("onlineUser", ArrayonlineUser)

        // mSocket!!.on("joinUser",joinUser)onlineUser
        mSocket!!.on("message", onNewMessage)
        mSocket!!.on("on typing", onTyping)
        mSocket!!.on("image",onNewImage)
        mSocket!!.connect()

        layoutManager = LinearLayoutManager(this)
        
        recyclerMessage.layoutManager = layoutManager
        rvAdapter = MessageAdapter(this, messageArray, from_user_id.toString())
        onTypeButtonEnable()

        img_send.setOnClickListener {
            sendMessage()
        }
        img_send_img.setOnClickListener {
            PickImageDialog.build(PickSetup()).show(this)
        }
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



                Log.e("hzm", message.toString())
                var des_id = message.getString("des_id")
                Log.e(
                    "if(fromMessage=",
                    "${fromMessage + " " + from_user_id + " " + des_id + " " + to_user_Id}"
                )

                if (fromMessage == from_user_id && des_id == to_user_Id) {
                    Log.e(
                        "if(fromMessage=",
                        "${fromMessage + " " + from_user_id + " " + des_id + " " + to_user_Id}"
                    )

                    messageArray.add(message(fromMessage, textMessage, time,image))
                    if (to_user_Id.equals(des_id)) {
                        Toast.makeText(this, message.getString("message"), Toast.LENGTH_SHORT)
                            .show()

                    }
                } else if (des_id == from_user_id && fromMessage == to_user_Id) {
                    Log.e(
                        "if(fromMessage=",
                        "${fromMessage + " " + from_user_id + " " + des_id + " " + to_user_Id}"
                    )

                    messageArray.add(message(fromMessage, textMessage, time,image))
                    if (to_user_Id.equals(des_id)) {
                        Toast.makeText(this, message.getString("message"), Toast.LENGTH_SHORT)
                            .show()
                    }
                }
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
                var des_id = message.getString("des_id")
                Log.e(
                    "if(fromMessage=",
                    "${fromMessage + " " + from_user_id + " " + des_id + " " + to_user_Id}"
                )

                if (fromMessage == from_user_id && des_id == to_user_Id) {
                    Log.e(
                        "if(fromMessage=",
                        "${fromMessage + " " + from_user_id + " " + des_id + " " + to_user_Id}"
                    )

                    messageArray.add(message(fromMessage, textMessage, time,""))
                   /* if (to_user_Id.equals(des_id)) {
                        Toast.makeText(this, message.getString("message"), Toast.LENGTH_SHORT)
                            .show()

                    }*/
                } else if (des_id == from_user_id && fromMessage == to_user_Id) {
                    Log.e(
                        "if(fromMessage=",
                        "${fromMessage + " " + from_user_id + " " + des_id + " " + to_user_Id}"
                    )

                    messageArray.add(message(fromMessage, textMessage, time,""))
                   /* if (to_user_Id.equals(des_id)) {
                        Toast.makeText(this, message.getString("message"), Toast.LENGTH_SHORT)
                            .show()
                    }*/
                }
            } catch (e: Exception) {
                Log.e("hzm2020", e.toString())
            }
            rvAdapter!!.notifyDataSetChanged()


        }
    }

    /*private fun joinNewUser(username: String, userId: String) {

        val user = JSONObject()
        user.put("username", username)
        user.put("userId", userId)
        mSocket!!.emit("joinUser", user)
    }*/

    override fun onPickResult(r: PickResult?) {
        // img.setImageBitmap(r!!.bitmap)

        val message = JSONObject()
        message.put("message", ed_messege.text.toString().trim())
        message.put("source_id", from_user_id)
        message.put("des_id", to_user_Id)
        message.put("time", getCurrentTime())
        message.put("name_des", to_user_Name)
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

    private fun sendMessage() {

        val message = JSONObject()
        message.put("message", ed_messege.text.toString().trim())
        message.put("source_id", from_user_id)
        message.put("des_id", to_user_Id)
        message.put("time", getCurrentTime())
        message.put("name_des", to_user_Name)
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
                    onTyping.put("toId", to_user_Id)

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
                if ((id == from_user_id && toId == to_user_Id)||(toId == from_user_id && id == to_user_Id)) {

                    if (id == from_user_id && toId == to_user_Id) {
                      //  typingOrNot = false
//                        stateUser!!.setText("Connection")

                       stateUser!!.text = "Connection"

                    } else {
                        stateUser!!.text = "is Typing......"
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
                                                    Thread.sleep(500)
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
    /* override fun onDestroy() {
         super.onDestroy()
         mSocket!!.disconnect()
         mSocket!!.off("new message", onNewMessage)
     }*/


    /*  mSocket!!.on(Socket.EVENT_CONNECT)
      { Log.e("onConnect", "Socket Connected!") };*/
}


/*lateinit var app: SocketCreate
    private var mSocket: Socket? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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
        ) { Log.e("onConnect", "Socket Connected!") };
        mSocket!!.on(Socket.EVENT_DISCONNECT, Emitter.Listener {
            runOnUiThread {
                Log.e("onDisconnect", "Socket onDisconnect!")

            }
        })


        mSocket!!.connect()




        img_send.setOnClickListener {
            sendMessage()
        }


    }

    private fun sendMessage() {
        mSocket!!.emit("message", ed_messege.text.toString())

    }




imgProfile1.setOnClickListener {

    PickImageDialog.build(PickSetup()).show(this)
}

override fun onPickResult(r: PickResult?) {
        imgProfile1.setImageBitmap(r!!.bitmap)
        uploadImage(r!!.uri)
    }




    *//*val joinUser = Emitter.Listener { args ->

        runOnUiThread {
            val newUser = args[0] as JSONObject

            try {
                Log.e("hzm", newUser.toString())
                var des_id = newUser.getString("des_id")

                if (user_id.equals(des_id)) {
                    Toast.makeText(this, message.getString("message"), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("hzm", e.toString())
            }

        }
    }*/

/* val ArrayonlineUser = Emitter.Listener { args ->

     runOnUiThread {
       //  Log.e("hzm", args[0].toString())

         var user = args[0] as JSONArray

         for (i in 0 until user.length()) {
             Log.e("hzm", user.getJSONObject(i).toString())
            val userToJsonObject = user.getJSONObject(i)
           //  Log.e("hzm", user[i].toString())
           /* val name =  user.getString(i) as JSONObject*/
             var nameuser = userToJsonObject.getString("username")
             var iduser = userToJsonObject.getString("userId")

            /* Log.e("hzm10", name.toString()+ ""+ i)*/
             Log.e("hzm  x", nameuser   + " "+iduser )


             // Your code here
         }

         try {
             Log.e("hzm111111111", user[0].toString())
             //var usernameJoin = user.getString("username")
             //var userIdJion = user.getString("userId")
             //onlineUser.add(users(usernameJoin,userIdJion))

         } catch (e: Exception) {
             Log.e("hzm", e.toString())
         }

     }
 }*/
/* val onNewUser = Emitter.Listener { args ->

     runOnUiThread {
         Log.e("hzm", args[0].toString())

         val user = args[0] as JSONObject

         try {
             Log.e("hzm", user.toString())
             var usernameJoin = user.getString("username")
             var userIdJion = user.getString("userId")
             onlineUser.add(users(usernameJoin,userIdJion))

         } catch (e: Exception) {
             Log.e("hzm", e.toString())
         }

     }
 }*/