package com.example.finalprojectsocket.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.finalprojectsocket.R
import com.example.finalprojectsocket.modle.dialog
import com.example.finalprojectsocket.modle.users
import kotlinx.android.synthetic.main.item_dailog_users.view.*

class DialogAdapter (val context: Context, val data: ArrayList<dialog>,val usersOnGroup:ArrayList<users>): RecyclerView.Adapter<DialogAdapter.MyViewHolder>(){


    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var name = itemView.checkBox
       // val user_id = itemView.user_id


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {


        val inflate = LayoutInflater.from(context).inflate(R.layout.item_dailog_users, parent, false)
        return MyViewHolder(inflate)


    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        holder.name.text=data[position].name.toString()
        /*holder.name =
            data[position].isAdd*/

        holder.name.setOnClickListener {
            if (holder.name.isChecked) {
                holder.name.isSelected = true
                usersOnGroup.add(users(data[position].name,data[position].id,data[position].image))
                Toast.makeText(context,"${holder.name.isChecked}",Toast.LENGTH_SHORT).show()
            } else if (!holder.name.isChecked) {
                holder.name.isChecked = false
                Toast.makeText(context,"${holder.name.isChecked}",Toast.LENGTH_SHORT).show()

            }
        }
        /*holder.itemView.setOnClickListener {
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra("name",data[position].username)
            intent.putExtra("id",data[position].userId)

            context.startActivity(intent)


        }*/

    }
}