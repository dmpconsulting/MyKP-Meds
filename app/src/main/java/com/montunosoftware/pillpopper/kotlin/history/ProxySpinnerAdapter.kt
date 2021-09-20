package com.montunosoftware.pillpopper.kotlin.history

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.montunosoftware.mymeds.R
import org.kp.tpmg.mykpmeds.activation.model.User

class ProxySpinnerAdapter(var context: Context, val userList: List<User>) : BaseAdapter() {

    private val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val holder: ItemHolder
        if (null == convertView) {
            view = inflater.inflate(R.layout.history_users_spinner_listitem, parent, false)
            holder = ItemHolder(view)
            view?.tag = holder
        } else {
            view = convertView
            holder = (view.tag as ItemHolder?)!!
        }
        holder.userName?.text = userList[position].firstName
        return view
    }

    override fun getItem(position: Int): User {
        return userList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return userList.size
    }

    private class ItemHolder(row: View?) {
        val userName: TextView? = row?.findViewById(R.id.history_list_proxy_name_textview)

    }
}
