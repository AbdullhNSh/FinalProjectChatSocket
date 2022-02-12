package com.example.finalprojectsocket.activity

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.finalprojectsocket.R
import com.example.finalprojectsocket.modle.SocketCreate
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.Socket
import com.vansuita.pickimage.bean.PickResult
import com.vansuita.pickimage.bundle.PickSetup
import com.vansuita.pickimage.dialog.PickImageDialog
import com.vansuita.pickimage.listeners.IPickResult
import kotlinx.android.synthetic.main.activity_signup.btnGoTo
import kotlinx.android.synthetic.main.activity_signup.edtUsername
import kotlinx.android.synthetic.main.activity_signup.img
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.util.*

class Signup : AppCompatActivity() , IPickResult {
    lateinit var app: SocketCreate
            var path: String? =  null
    var userName: String? = null
    private var mSocket: Socket? = null
    private var user_id: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)


        val sharedPref1 = getSharedPreferences("MyPref", Context.MODE_PRIVATE)
        sharedPref1.edit().clear().apply()
        user_id = UUID.randomUUID().toString()

        img.setOnClickListener {

            PickImageDialog.build(PickSetup()).show(this)
        }

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


        mSocket!!.connect()


        btnGoTo.setOnClickListener {
            if(path==null){
                    Toast.makeText(this,"ادخل صورة",Toast.LENGTH_SHORT).show()
            }else {
                userName = edtUsername.text.toString()

               /* if (userName.equals("")) {
                    Log.e("hzm", "userName.equals()")
                    val intent = Intent(this, usersOline::class.java)
                    startActivity(intent)

                } else {*/
                    val intent = Intent(this, MainActivity3::class.java)//usersOline
                    joinNewUser(userName!!, user_id!!, path!!)
                    intent.putExtra("username", userName)
                    intent.putExtra("userId", user_id)
                    val sharedPref = getSharedPreferences("MyPref", Context.MODE_PRIVATE)
                    val editor = sharedPref.edit()
                    editor.putInt("id", 1)
                    editor.putString("usernameAuth", userName)
                    editor.putString("useridAuth", user_id)
                    editor.putString("imageuserAuth",path)

                editor.apply()
                Log.e("id", userName.toString())
                Log.e("id", user_id.toString())

                startActivity(intent)
              //  }

            }
        }


    }

    private fun joinNewUser(username: String, userId: String,image:String) {

        val user = JSONObject()
        user.put("username", username)
        user.put("image",image)
        user.put("userId", userId)
        user.put("connect",true)
        mSocket!!.emit("joinUser", user)
    }

    override fun onPickResult(r: PickResult?) {
        img.setImageBitmap(r!!.bitmap)
        path = imageToString(r.bitmap)
      //  mSocket!!.emit("image", imageToString(r.bitmap))
    }

   /* fun sendImage(path: String) {
        val sendData = JSONObject()
        try {
            sendData.put("imageData", encodeImage(path))
            mSocket!!.emit("image", sendData)
        } catch (e: JSONException) {
        }
    }*/

    val onNewMessage = Emitter.Listener { args ->

        runOnUiThread {

            Log.e("hzm", args[0].toString())
            val image = args[0].toString()

            decodeBase64(image)
            img.setImageBitmap(decodeBase64(image))

            try {


                // Log.e("hzm", message.toString())

            } catch (e: Exception) {
                Log.e("hzm", e.toString())
            }
            //   rvAdapter!!.notifyDataSetChanged()


        }
    }

    private fun encodeImage(path: String): String? {
        val imagefile = File(path)
        var fis: FileInputStream? = null
        try {
            fis = FileInputStream(imagefile)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        val bm = BitmapFactory.decodeStream(fis)
        val baos = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val b = baos.toByteArray()
        //Base64.de
        return Base64.encodeToString(b, Base64.DEFAULT)
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