package com.example.messengerapp


import android.app.Activity
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.pusher.client.channel.PrivateChannelEventListener
import com.pusher.client.util.HttpAuthorizer
import kotlinx.android.synthetic.main.activity_chat_room.*
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class ChatRoom : AppCompatActivity() {

  companion object {
    const val EXTRA_ID = "id"
    const val EXTRA_NAME = "name"
    const val EXTRA_PUBLICKEY = "publicKey"
  }

  private lateinit var contactName: String
  private lateinit var contactId: String
  private lateinit var contactPublicKey: String
  lateinit var nameOfChannel: String
  val mAdapter = ChatRoomAdapter(ArrayList())

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_chat_room)
    fetchExtras()
    setupRecyclerView()
    subscribeToChannel()
    setupClickListener()
  }


  private fun fetchExtras() {
    contactName = intent.extras?.getString(EXTRA_NAME)!!
    contactId = intent.extras?.getString(EXTRA_ID)!!
    contactPublicKey = intent.extras?.getString(EXTRA_PUBLICKEY)!!
  }


  private fun setupRecyclerView() {
    with(recyclerViewChat) {
      layoutManager = LinearLayoutManager(this@ChatRoom)
      adapter = mAdapter
    }
  }


  private fun subscribeToChannel() {
    val authorizer = HttpAuthorizer("http://138.68.111.102:5000/pusher/auth/private")
    val options = PusherOptions().setAuthorizer(authorizer)
    options.setCluster("eu")

    val pusher = Pusher("0e1770d9090dbea7a1c4", options)
    pusher.connect()

    nameOfChannel = if (Singleton.getInstance().currentUser.id > contactId) {
      "private-" + Singleton.getInstance().currentUser.id + "-" + contactId
    } else {
      "private-" + contactId + "-" + Singleton.getInstance().currentUser.id
    }

    Log.i("ChatRoom", nameOfChannel)

    pusher.subscribePrivate(nameOfChannel, object : PrivateChannelEventListener {
      override fun onEvent(channelName: String?, eventName: String?, data: String?) {

        val jsonObject = JSONObject(data)

        val cipher_text = jsonObject.getString("message")

        var plain_text = ""
        if(jsonObject.getString("sender_id") == contactId)
            plain_text = AES.do_RSADecryption(cipher_text, Singleton.privateKey)
        else
            return

        val messageModel = MessageModel(
            plain_text,
            jsonObject.getString("sender_id"))

        runOnUiThread {
          mAdapter.add(messageModel)
        }

      }

      override fun onAuthenticationFailure(p0: String?, p1: Exception?) {
        Log.e("ChatRoom", p1!!.localizedMessage)
      }

      override fun onSubscriptionSucceeded(p0: String?) {
        Log.i("ChatRoom", "Successful subscription")
      }

    }, "new-message")

  }


  private fun setupClickListener() {
    sendButton.setOnClickListener{
      if (editText.text.isNotEmpty()){
        val jsonObject = JSONObject()

        val plain_text = editText.text.toString()
        val cipher_text = AES.do_RSAEncryption(plain_text, AES.stringToPublicKey(contactPublicKey))
        jsonObject.put("message", cipher_text)


        jsonObject.put("channel_name", nameOfChannel)
        jsonObject.put("sender_id", Singleton.getInstance().currentUser.id)
        val jsonBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
            jsonObject.toString())

        val messageModel = MessageModel(
                plain_text,
                Singleton.getInstance().currentUser.id)
        runOnUiThread {
          mAdapter.add(messageModel)
        }

        RetrofitInstance.retrofit.sendMessage(jsonBody).enqueue(object: Callback<String>{
          override fun onFailure(call: Call<String>?, t: Throwable?) {
            Log.e("ChatRoom",t!!.localizedMessage)

          }

          override fun onResponse(call: Call<String>?, response: Response<String>?) {
            Log.e("ChatRoom",response!!.body())
          }

        })
        editText.text.clear()
        hideKeyBoard()
      }

    }
  }


  private fun hideKeyBoard() {
    val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    var view = currentFocus
    if (view == null) {
      view = View(this)
    }
    imm.hideSoftInputFromWindow(view.windowToken, 0)
  }


}
