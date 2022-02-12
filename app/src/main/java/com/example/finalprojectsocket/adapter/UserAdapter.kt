package com.example.finalprojectsocket.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.finalprojectsocket.activity.MainActivity
import com.example.finalprojectsocket.modle.users
import com.example.finalprojectsocket.R
import kotlinx.android.synthetic.main.user.view.*

class UserAdapter (val context: Context, val data: ArrayList<users>):RecyclerView.Adapter<UserAdapter.MyViewHolder>(){


    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val name = itemView.username
        val image_user = itemView.imageuser


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {


        val inflate = LayoutInflater.from(context).inflate(R.layout.user, parent, false)
        return MyViewHolder(inflate)


    }

    override fun getItemCount(): Int {
        return data.size

    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        holder.name.text=data[position].username.toString()
       // holder.user_id.text=data[position].userId.toString()
        //image_user
        holder.image_user.setImageBitmap(decodeBase64(data[position].image));

        holder.itemView.setOnClickListener {
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra("name",data[position].username)
            intent.putExtra("id",data[position].userId)
            intent.putExtra("image",data[position].image)

            context.startActivity(intent)


        }

    }
    fun decodeBase64(input: String?): Bitmap? {
        val bytes = Base64.decode(input, 0)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
}