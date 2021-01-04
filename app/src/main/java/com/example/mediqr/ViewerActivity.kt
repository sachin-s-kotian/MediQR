package com.example.mediqr

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.example.mediqr.helpers.DrugRecyclerAdapter
import com.example.mediqr.helpers.ErrorRecyclerAdapter
import com.example.mediqr.helpers.MRQ
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_viewer.*
import org.json.JSONObject
import java.nio.charset.Charset

class ViewerActivity : AppCompatActivity() {

    val _tag = "appx"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_viewer)
    }

    override fun onStart() {
        super.onStart()
        try {
            val token = intent.data!!.path!!.removePrefix("/")
            val request: JsonObjectRequest = object : JsonObjectRequest(
                Method.POST,
                "${getString(R.string.service_url)}/prescriptions",
                JSONObject().apply {
                    put(
                        "query",
                        """mutation{ scan(token: "$token") { title prescriber data { drug notes } } }"""
                    )
                },
                Response.Listener {
                    Log.d(_tag, it.toString())
                    if (it.has("errors")) {
                        viewer_error_recycler.layoutManager =
                            LinearLayoutManager(applicationContext)
                        viewer_error_recycler.adapter =
                            ErrorRecyclerAdapter(it.getJSONArray("errors"))
                    }
                    it.getJSONObject("data").let { data ->
                        if (data.isNull("scan")) {

                        } else {
                            val scanData = data.getJSONObject("scan")
                            viewer_title.text = scanData.getString("title")
                            viewer_prescriber.text = scanData.getString("prescriber")
                            viewer_rows.layoutManager = LinearLayoutManager(applicationContext)
                            viewer_rows.adapter = DrugRecyclerAdapter(scanData.getJSONArray("data"))
                        }
                    }
                },
                Response.ErrorListener {
                    Log.e(_tag, it.toString())
                    it.networkResponse?.data?.let { t ->
                        Log.e(
                            _tag,
                            t.toString(Charset.forName("utf-8"))
                        )
                    }
                }
            ) {
                override fun getHeaders(): MutableMap<String, String> {
                    return mutableMapOf<String, String>().apply {
                        put(
                            "access_token",
                            GoogleSignIn.getLastSignedInAccount(applicationContext)!!.idToken!!
                        )
                    }
                }
            }
            MRQ.getInstance(applicationContext).addToRequestQueue(request)
        } catch (exception: Exception) {
            MaterialAlertDialogBuilder(this).apply {
                setTitle("Error")
                setMessage(exception.message!!)
                create()
            }.show()
        }
    }

}
