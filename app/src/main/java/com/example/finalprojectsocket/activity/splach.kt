package com.example.finalprojectsocket.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.example.finalprojectsocket.R

class splach : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splach)

        supportActionBar!!.hide()


        /* val prefs = context?.getSharedPreferences("SharedPref_Name", Context.MODE_PRIVATE)
         if(prefs?.contains("Key")!!){
             val deviceToken = prefs.getString("Key", null);
         }*/
      //  val sharedPref1 = getSharedPreferences("MyPref", Context.MODE_PRIVATE)
     //   sharedPref1.edit().clear().apply()
        val sharedPref = getSharedPreferences("MyPref", Context.MODE_PRIVATE)
       var  userId = sharedPref.getString("useridAuth", null).toString()
        var  id = sharedPref.getInt("id", -1)

        var  userName = sharedPref.getString("usernameAuth", "").toString()

        sharedPref.edit()
        Log.e("MyPref",id.toString())
        Handler().postDelayed({
            if(id == -1){
                Log.e("id",id.toString())

                val intent = Intent(this,
                    Signup::class.java)
               startActivity(intent)
            }else if(id == 1){
                Log.e("id",id.toString())

                val intent = Intent(this,
                    MainActivity3::class.java)
               startActivity(intent)
            }

        }, 1000)
    }
}