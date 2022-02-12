package com.example.finalprojectsocket.activity

import android.app.Dialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NotificationCompat
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finalprojectsocket.R
import com.example.finalprojectsocket.modle.SocketCreate
import com.example.finalprojectsocket.adapter.DialogAdapter
import com.example.finalprojectsocket.adapter.TabPageAdapter
import com.example.finalprojectsocket.modle.dialog
import com.example.finalprojectsocket.modle.users
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.Socket
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_main3.*
import kotlinx.android.synthetic.main.content_tab_layout_demo.*
import org.json.JSONArray
import org.json.JSONObject

class MainActivity3 : AppCompatActivity() , NavigationView.OnNavigationItemSelectedListener  {

    private val dialogUserOnline: ArrayList<dialog> = ArrayList()
    private val userAddGroup: ArrayList<users> = ArrayList()
    private val onlineUser: ArrayList<users> = ArrayList()

    lateinit var app: SocketCreate
    private var mSocket: Socket? = null
    var userName: String? = null
    var userId: String? = null
    var image :String? = null
    val view:View? = null
    private var rvAdapter: RecyclerView.Adapter<*>? = null
    private var layoutManager: LinearLayoutManager? = null
    val arrayid : ArrayList<String>? = null
    var imageNav:ImageView?=null
    var nameNav: TextView?=null
    var uid:TextView?=null


    var recyclerView: RecyclerView? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)

    //    toolbar //= (Toolbar) findViewById(R.id.activity_toolbar);
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        imageNav = findViewById<ImageView>(R.id.imgUser)
        nameNav = findViewById<TextView>(R.id.nameUser)
        uid = findViewById<TextView>(R.id.uidUser)

        setSupportActionBar(toolbar)
title="Chats"


        val sharedPref = getSharedPreferences("MyPref", Context.MODE_PRIVATE)

        userName = intent.getStringExtra("username")
        userId = intent.getStringExtra("userId")
        image = sharedPref.getString("imageuserAuth", "").toString()



        if (userId.equals(null)) {

            userId = sharedPref.getString("useridAuth", "").toString()
            userName = sharedPref.getString("usernameAuth", "").toString()
            sharedPref.edit()
            //sharedPref.clear//
        }
        Log.e("hzm",userId.toString())
        Log.e("hzm",userName.toString())

        if(userId==""||userName==""){
            val intent = Intent(this, Signup::class.java)

            startActivity(intent)
        }
        getProfileData()
        val toggle = ActionBarDrawerToggle(
            this,
            drawer_layout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        nav_view.setNavigationItemSelectedListener(this)


        app = application as SocketCreate
        mSocket = app.getSocket()

        mSocket!!.on(Socket.EVENT_CONNECT_ERROR) {
            runOnUiThread {
                Log.e("EVENT_CONNECT_ERROR", "EVENT_CONNECT_ERROR: ")//,)
                Toast.makeText(this,"EVENT_CONNECT_ERROR",Toast.LENGTH_SHORT).show()
              //  Snackbar.make(view!!,"Repalce action", Snackbar.LENGTH_LONG).show()
            }
        }
        mSocket!!.on(Socket.EVENT_CONNECT_TIMEOUT, Emitter.Listener {
            runOnUiThread {
                Log.e("EVENT_CONNECT_TIMEOUT", "EVENT_CONNECT_TIMEOUT: ")//,)
                Toast.makeText(this,"EVENT_CONNECT_TIMEOUT",Toast.LENGTH_SHORT).show()

                // Snackbar.make(view!!,"Repalce action", Snackbar.LENGTH_LONG).show()

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
        mSocket!!.on("onlineUser", ArrayonlineUser)
        mSocket!!.on("message", onNewMessage)
        mSocket!!.on("group", group)


        mSocket!!.emit("UsersOnline", "")
        mSocket!!.on("UsersOnline", ArrayonlineUser)


        mSocket!!.connect()
       /* fab.setOnClickListener {view ->
            Snackbar.make(view,"Repalce action", Snackbar.LENGTH_LONG).show()

        }*/
        configureTabLayout()
    }
    private fun configureTabLayout(){
        tab_layout.addTab(tab_layout.newTab().setText("Private"))
        tab_layout.addTab(tab_layout.newTab().setText("Group"))


        val adapter = TabPageAdapter(supportFragmentManager,tab_layout.tabCount)
        pager.adapter=adapter
        pager.addOnPageChangeListener(
            TabLayout.TabLayoutOnPageChangeListener(tab_layout))

        tab_layout.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
                pager.currentItem = tab!!.position

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
            }

        })

    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.AddGroup -> {

                showDialog()
                // Toast.makeText(applicationContext, "click on setting", Toast.LENGTH_LONG).show()
                true
            }
            R.id.itemLogout ->{
                val sharedPref1 = getSharedPreferences("MyPref", Context.MODE_PRIVATE)
                sharedPref1.edit().clear().apply()
                val intent = Intent(this, Signup::class.java)
                startActivity(intent)
                //Toast.makeText(applicationContext, "click on share", Toast.LENGTH_LONG).show()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    val group = Emitter.Listener { args ->
        runOnUiThread {
            Log.e("hzm", args.toString())
            try {
                val newgroup = args[0] as JSONArray

                // var name = newgroup.getString(0)

                Log.e("grouuuuuuup",newgroup.toString())

                // var array = newgroup.getString("arrayUsers")
                // array.toArray
/*     group.put("adminname", users(userName!!,userId!!,image!!))


                group.put("arrayUsers", userAddGroup.toString())*/

                /*    Log.e("hzm", message.toString())
                    //  messageArray.add(com.app.socketchatdemo.modle.message(fromMessage, textMessage))
                    if (userId.equals(des_id)) {
                        createNotificationChannel(textMessage, from_id_Message, source_name)
                        Toast.makeText(this, message.getString("message"), Toast.LENGTH_SHORT).show()

                    }*/



           } catch (e: Exception) {
                Log.e("hzm", e.toString())
            }

        }
    }

    val ArrayonlineUser = Emitter.Listener { args ->
        onlineUser.clear()
        dialogUserOnline.clear()
        runOnUiThread {
            try {
                var user = args[0] as JSONArray
                Log.e("hzm",user.toString())

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
                Log.e("honlineUser1",onlineUser.toString())


                /*  for (i in 0 until onlineUser.size) {
                      for (j in i+1  until onlineUser.size) {
                          if (onlineUser[i] === onlineUser[j]) {
                              onlineUser[j] = users("","","")
                          }
                      }
                  }
  */
                for (i in 0 until onlineUser.size) {

                    val nameuser1 = onlineUser[i].username
                    val id = onlineUser[i].userId
                    val image = onlineUser[i].image
                    Log.d("forr loop", "$nameuser1 $id")

                    //   if(nameuser1 != "" && id !=""){
                    // }
                    if (onlineUser[i] ==users("","","")){
                        onlineUser.remove(onlineUser[i])
                    }

                    dialogUserOnline.add(dialog(id,nameuser1,false,image))



                }

                for (i in 0 until dialogUserOnline.size) {
                    for (j in i+1  until dialogUserOnline.size) {
                        if (dialogUserOnline[i]  === dialogUserOnline[j]) {
                            dialogUserOnline.remove(dialogUserOnline[j])
                        }
                    }
                }
                // Log.e("hzm",onlineUser.toString())

                rvAdapter!!.notifyDataSetChanged()

                Log.e("onlineUser", onlineUser.toString())
                Log.e("dialogUserOnline",dialogUserOnline.toString())

            } catch (e: Exception) {
                Log.e("hzm", e.toString())
            }

        }
    }


    val onNewMessage = Emitter.Listener { args ->
        runOnUiThread {
            Log.e("hzm", args.toString())
            try {
                val message = args[0] as JSONObject

                var textMessage = message.getString("message")
                var from_id_Message = message.getString("source_id")
                var des_id = message.getString("des_id")
                var des_name = message.getString("name_des")
                var source_name = message.getString("source_name")


                Log.e("hzm", message.toString())
                //  messageArray.add(com.app.socketchatdemo.modle.message(fromMessage, textMessage))
                if (userId.equals(des_id)) {
                    createNotificationChannel(textMessage, from_id_Message, source_name)
                    Toast.makeText(this, message.getString("message"), Toast.LENGTH_SHORT).show()

                }
            } catch (e: Exception) {
                Log.e("hzm", e.toString())
            }

        }
    }
    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun createNotificationChannel(
        message: String,
        id: String,
        name: String
    ) {//,username:String
        val CHANNEL_ID = "ServiceChannelExample"
        var manager: NotificationManager? =null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID, "Example Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        } else {
            manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        }
        val notificationIntent = Intent(this, MainActivity::class.java)
        /* notificationIntent.putExtra("id2",id)
         notificationIntent.putExtra("name2",name)*/

        Log.e("notificationIntent", "userName : $name $$ userId : $id")

        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(name)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_send_black_24dp)
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    resources,
                    R.drawable.ic_launcher_background
                )
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        manager!!.notify(1, notification)
    }
    fun getProfileData() {
        Log.e("image",image.toString())
       /* if(image ==""){
            val intent = Intent(this,Signup::class.java)
            startActivity(intent)
        }*/
       // imageNav!!.setImageBitmap(decodeBase64(image.toString()))
       //  nameNav!!.setText(userName)
      //  uid!!.setText(userId)

        //image_user.setImageBitmap(decodeBase64(data[position].image))
    }
    fun decodeBase64(input: String?): Bitmap? {
        val bytes = Base64.decode(input, 0)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
    private fun showDialog() {


        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_user)

        dialog.setTitle("Add User")
        val editText: EditText = dialog.findViewById(R.id.edtnameGroup)
        val btnFinish: Button = dialog.findViewById(R.id.btnFinish)
        val btnCreate: Button = dialog.findViewById(R.id.btnCreate)
        btnFinish.setOnClickListener {
            dialog.dismiss()
        }
        btnCreate.setOnClickListener {

            for (i in 0 until dialogUserOnline.size) {


                if (dialogUserOnline[i].isAdd) {
                    Log.e("group", dialogUserOnline[i].toString())
                }


            }
        }
        val recyclerView: RecyclerView = dialog.findViewById(R.id.RecyclerAddUser)
        val adapterRe = DialogAdapter(this, dialogUserOnline, userAddGroup)
        recyclerView.adapter = adapterRe
        recyclerView.layoutManager = LinearLayoutManager(
            applicationContext,
            LinearLayoutManager.VERTICAL,
            false
        )

        recyclerView.setOnClickListener { }
        dialog.show()

        btnCreate.setOnClickListener {
            if (userAddGroup != null) {
                //Log.e("group", userAddGroup.toString())

                var group = JSONArray()
                group.put(userAddGroup.toString())

                //userAddGroup

                for (i in 0 until userAddGroup.size) {


                    Log.e("group", userAddGroup[i].username.toString())
                    Log.e("group", userAddGroup[i].userId.toString())
                    //  var id1=   userAddGroup[i].userId
                    // arrayid!!.add(id1)
                    //arrayid!!.add(userAddGroup[i].userId)


                }

                //   group=users(userName!!,userId!!,image!!)

                /*for (i in 0 until 1) {
                   var obj = JSONObject()
                    try {
                       // obj.put("adminname", users(userName!!,userId!!,image!!))
                     //   obj.put("arrayUsers", userAddGroup)

                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
                group.put(userAddGroup)
*/

                // group.put("adminname", users(userName!!,userId!!,image!!))


                //     group.put("arrayUsers", userAddGroup)//.toString())
                //  group.put("arrayUsers", userAddGroup)


                mSocket!!.emit("group", group)
                /*var obj: JSONObject? = null
                             val jsonArrayLatLng = JSONArray()
                             for (i in 0 until list!!.size) {
                                 obj = JSONObject()
                                 try {
                                     obj.put("lat", list.get(i).lat)
                                     obj.put("long", list.get(i).lng)

                                 } catch (e: JSONException) {
                                     e.printStackTrace()
                                 }
                                 jsonArrayLatLng.put(obj)
                             }
                             val jsonObject = JSONObject()
                             jsonObject.put("list_key_name_here", jsonArrayLatLng)
                             socket?.emit(EVENT, jsonObject)
             */



            } else {
                Toast.makeText(this, "group", Toast.LENGTH_SHORT).show()
            }

        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        TODO("Not yet implemented")
    }
}