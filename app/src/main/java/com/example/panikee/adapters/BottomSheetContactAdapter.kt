package com.example.panikee.adapters

import android.app.Activity
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.panikee.R
import com.example.panikee.data.vo.Contact
import com.google.gson.Gson

class BottomSheetContactAdapter(ctv : Activity?) : RecyclerView.Adapter<BottomSheetContactAdapter.ViewHolder>(){

    private var act : Activity? = ctv
    private lateinit var sampleData : MutableList<Contact>

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BottomSheetContactAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(
            R.layout.fragment_contact_list_item, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: BottomSheetContactAdapter.ViewHolder, position: Int) {
        holder.contactName.text = sampleData[position].contactName
        holder.contactNumber.text = sampleData[position].contactNumber
        holder.contactBtnRemove.setOnClickListener {
            deleteFriend(position)
        }
    }

    /** Get Size of Item */
    override fun getItemCount(): Int {
        return sampleData.size
    }

    /** Set Content Data from External */
    fun setContentData(mutableList: MutableList<Contact>){
        sampleData = mutableList
    }

    /** Delete and Remove Friend from Friends */
    private fun deleteFriend(position : Int){
        sampleData.removeAt(position)
        FriendsPreferencesAdapter().update(act, "friends", sampleData)
        Toast.makeText(act, "Success removing friend", Toast.LENGTH_SHORT).show()
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var contactName   : TextView = itemView.findViewById(R.id.fragment_contact_list_item_contact_name)
        var contactNumber : TextView = itemView.findViewById(R.id.fragment_contact_list_item_contact_number)
        var contactBtnRemove : Button = itemView.findViewById(R.id.fragment_contact_list_item_btn_remove)
    }

}