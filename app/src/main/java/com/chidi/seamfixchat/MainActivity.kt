package com.chidi.seamfixchat

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.chidi.seamfixchat.databinding.ActivityMainBinding
import com.chidi.seamfixchat.mqtt.MqttClientHelper
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import org.eclipse.paho.client.mqttv3.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.schedule


class MainActivity : AppCompatActivity() {

    private val mqttClient by lazy {
        MqttClientHelper(this)
    }

    private val adapter by lazy {
        ChatScreenAdapter(this)
    }

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // binding.textViewMsgPayload.movementMethod = ScrollingMovementMethod()

        setMqttCallBack()

        binding.textViewNumMsgs.text = showToday()

        binding.messagesListView.adapter = adapter

        // pub button
        binding.sendBtn.setOnClickListener { view ->
            var snackbarMsg: String
            val topic = binding.inputeUserId.text.toString().trim()
            snackbarMsg = "Cannot send empty message!"
            if (topic.isNotEmpty()) {
                snackbarMsg = try {
                    mqttClient.publish(topic, binding.chatPayload.text.toString().trim())
                    val msg = Message(binding.chatPayload.text.toString(), currentDateTime(), true)
                    displayChat(msg)
                    binding.chatPayload.text.clear()
                    "Delivered to user '$topic'"
                } catch (ex: MqttException) {
                    "Error sending message to user: $topic"
                }
            }
            Snackbar.make(view, snackbarMsg, 300)
                .setAction("Action", null).show()

        }

        // Subscribe to an event
        binding.subscribeBtn.setOnClickListener { view ->
            var snackbarMsg: String
            val topic = binding.inputeUserId.text.toString().trim()
            snackbarMsg = "Enter User ID to connect"
            if (topic.isNotEmpty()) {
                snackbarMsg = try {
                    mqttClient.subscribe(topic)
                    "Connected to user '$topic'"
                } catch (ex: MqttException) {
                    "Error Connecting to user: $topic"
                }
            }
            Snackbar.make(view, snackbarMsg, Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show()
        }

        Timer("CheckMqttConnection", false).schedule(3000) {
            if (!mqttClient.isConnected()) {
                Snackbar.make(
                    textViewNumMsgs,
                    "Failed to connect to: '$SOLACE_MQTT_HOST' within 3 seconds",
                    Snackbar.LENGTH_SHORT
                )
                    .setAction("Action", null).show()
            }
        }

        binding.messagesListView.onItemLongClickListener =
            OnItemLongClickListener { adapterView, view, i, l ->
                Log.d("debug", "onLongClick = " + i + "  " + view.tag)
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Delete message")
                    .setMessage("Are you sure you want to delete this message?")
                    .setPositiveButton(
                        "Yes"
                    ) { _, _ ->
                        adapter.remove(adapter.messages[i])
                    }
                    .setNegativeButton("No", null)
                    .create()
                    .show()


                true
            }

    }

    private fun setMqttCallBack() {
        mqttClient.setCallback(object : MqttCallback, MqttCallbackExtended {
            override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                val toast = "Connected to host:\n'$SOLACE_MQTT_HOST'."
                Log.w("Debug", toast)
                Toast.makeText(applicationContext, toast, Toast.LENGTH_LONG).show()
            }

            override fun messageArrived(topic: String?, message: MqttMessage?) {
                Log.w("Debug", "Message received from host '$SOLACE_MQTT_HOST': $message")
                /* binding.textViewNumMsgs.text =
                     ("${binding.textViewNumMsgs.text.toString().toInt() + 1}")*/
                /*val str: String =
                    "------------" + Calendar.getInstance().time + "-------------\n$message\n${binding.textViewMsgPayload.text}"
                binding.textViewMsgPayload.text = str*/
                val msg = Message(message!!.toString(), currentDateTime(), false)
                displayChat(msg)
            }

            override fun connectionLost(cause: Throwable?) {
                val toast = "Connection to host lost:\n'$SOLACE_MQTT_HOST'"
                Log.w("Debug", toast)
                Toast.makeText(applicationContext, toast, Toast.LENGTH_LONG).show()
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                Log.w("Debug", "Message published to host '$SOLACE_MQTT_HOST'")
            }
        })
    }

    private fun displayChat(message: Message) {
        adapter.add(message)
        scroll()
    }

    private fun scroll() {
        binding.messagesListView.setSelection(messagesListView.count - 1)
    }

    private fun currentDateTime(): String {
        val c = Calendar.getInstance()
        val dateformat = SimpleDateFormat("hh:mm aa", Locale.ROOT)
        return dateformat.format(c.time)
    }

    private fun showToday(): String {
        val c = Calendar.getInstance()
        val dateformat = SimpleDateFormat("dd MMM yyyy", Locale.ROOT)

        return dateformat.format(c.time)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        mqttClient.destroy()
        super.onDestroy()
    }

}