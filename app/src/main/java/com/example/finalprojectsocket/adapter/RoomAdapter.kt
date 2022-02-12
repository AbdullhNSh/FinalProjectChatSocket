package com.example.finalprojectsocket.adapter

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.finalprojectsocket.R
import com.example.finalprojectsocket.modle.message

class RoomAdapter( val context: Context, var data :ArrayList<message>, val fromUid: String):RecyclerView.Adapter<RoomAdapter.MyViewHolder>(){


    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name = itemView.findViewById<TextView>(R.id.text_view)
        var image = itemView.findViewById<ImageView>(R.id.image_message)
        val time = itemView.findViewById<TextView>(R.id.time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        return if (viewType == R.layout.item_room_to) {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_room_to, parent, false)
            MyViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_room_from, parent, false)
            MyViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        holder.name.text = data[position].text
        holder.time.text = data[position].time
        //holder.image = data[position].image

        if(data[position].image !="") {
            //Glide.with(context).load(data[position].image).into(holder.image)
            //  val c =   decodeBase64(data[position].image)
            //holder.image =   c.
            holder.image.setImageBitmap(decodeBase64(data[position].image));

        }

    }

    override fun getItemViewType(position: Int): Int {
        return if (fromUid != data[position].name) {
            R.layout.item_message_to
        } else {
            R.layout.item_message_from
        }
    }

    fun decodeBase64(input: String?): Bitmap? {
        val bytes = Base64.decode(input, 0)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

}



/*

class MessageAdapter(val context: Context, val data: ArrayList<message>, val fromUid: String) :
    RecyclerView.Adapter<MessageAdapter.MyViewHolder>() {


    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        // val name = itemView.from_message
        // val message = itemView.text_view
        //internal fun setMessage(message: message) {
        val name = itemView.findViewById<TextView>(R.id.text_view)
        var image = itemView.findViewById<ImageView>(R.id.image_message)
        val time = itemView.findViewById<TextView>(R.id.time)
        //textView.text = message.text


    }

    override fun getItemViewType(position: Int): Int {
        return if (fromUid != data[position].name) {
            R.layout.item_message_to
        } else {
            R.layout.item_message_from
        }
    }

    /* override fun onDataChanged() {
         recycler_view.layoutManager!!.scrollToPosition(itemCount - 1)
     }*/
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        return if (viewType == R.layout.item_message_to) {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_message_to, parent, false)
            MyViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_message_from, parent, false)
            MyViewHolder(view)
        }
        /*  var layout = -1
          when (viewType) {
              Message.TYPE_MESSAGE -> layout =
                  R.layout.item_message
              Message.TYPE_LOG -> layout = R.layout.item_log
              Message.TYPE_ACTION -> layout =
                  R.layout.item_action
          }

           val v = LayoutInflater.from(parent.context).inflate(layout, parent, false)
          return MyViewHolder(v)
          */

        /*val inflate = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false)
        return MyViewHolder(inflate)*/


    }

    override fun getItemCount(): Int {
        return data.size

    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.name.text = data[position].text
        holder.time.text = data[position].time
        //holder.image = data[position].image

        if(data[position].image !="") {
            //Glide.with(context).load(data[position].image).into(holder.image)
            //  val c =   decodeBase64(data[position].image)
            //holder.image =   c.
            holder.image.setImageBitmap(decodeBase64(data[position].image));

        }




    }

    fun decodeBase64(input: String?): Bitmap? {
        val bytes = Base64.decode(input, 0)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

}
*/