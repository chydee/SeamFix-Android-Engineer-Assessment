package com.chidi.seamfixchat

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.RelativeLayout
import android.widget.TextView


class ChatScreenAdapter constructor(val context: Context) : BaseAdapter() {

     var messages: ArrayList<Message> = ArrayList()

    inner class MessageViewHolder(view: View) {
        var chatPayload: TextView = view.findViewById(R.id.message_body)
        var time: TextView = view.findViewById(R.id.time)
    }

    override fun getView(
        i: Int, convertView: View?, viewGroup: ViewGroup?
    ): View? {
        var holder: MessageViewHolder
        val message = messages[i]
        var cView = convertView
        val messageInflater =
            context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        if (convertView == null) {
            cView = messageInflater.inflate(R.layout.item_message_layout, null)
            holder = MessageViewHolder(cView)
            cView.tag = holder
        } else {
            holder = convertView.tag as MessageViewHolder
        }
        setLayoutParam(holder, message.belongsToCurrentUser)
        holder.chatPayload.text = message.text
        holder.time.text = message.time
        return cView
    }

    override fun getItem(p0: Int): Any = messages[p0]

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int = messages.size

    fun add(message: Message) {
        this.messages.add(message)
        notifyDataSetChanged()
    }

    fun remove(message: Message) {
        messages.remove(message)
        notifyDataSetChanged()
    }

    private fun setLayoutParam(
        holder: ChatScreenAdapter.MessageViewHolder,
        isCurrentUser: Boolean
    ) {
        if (isCurrentUser) {
            holder.chatPayload.setBackgroundResource(R.drawable.sender_bubble)
            val layoutParams =
                holder.chatPayload.layoutParams as RelativeLayout.LayoutParams
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0)
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
            holder.chatPayload.layoutParams = layoutParams
        } else {
            holder.chatPayload.setBackgroundResource(R.drawable.receiver_bubble)
            val layoutParams =
                holder.chatPayload.layoutParams as RelativeLayout.LayoutParams
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0)
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
            holder.chatPayload.layoutParams = layoutParams
        }

    }


}