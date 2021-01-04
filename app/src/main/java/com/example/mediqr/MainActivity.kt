package com.example.mediqr

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.example.mediqr.helpers.ErrorRecyclerAdapter
import com.example.mediqr.helpers.GSignin
import com.example.mediqr.helpers.MRQ
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private val _login = 100
    private val _tag = "appx"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        main_error_recycler.layoutManager = LinearLayoutManager(applicationContext)
        main_error_recycler.layoutAnimation =
            AnimationUtils.loadLayoutAnimation(applicationContext, R.anim.left_to_right_animation)
    }

    override fun onStart() {
        super.onStart()
        try {
            val account = GoogleSignIn.getLastSignedInAccount(applicationContext)!!
            if (account.isExpired) throw Exception("Auth Expired")
            mediQRGoogleAuth(account.idToken!!)
        } catch (exception: Exception) {
            Log.e(_tag, exception.toString())
            startActivityForResult(
                GSignin.getInstance(applicationContext).signinClient.signInIntent,
                _login
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            _login -> {
                val signinTask = GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    mediQRGoogleAuth(signinTask.getResult(ApiException::class.java)!!.idToken!!)
                } catch (exception: ApiException) {
                    Log.e(_tag, exception.toString())
                    MaterialAlertDialogBuilder(this).apply {
                        setTitle("Error")
                        setMessage("Couldn't sign-in. Please retry.")
                        setCancelable(false)
                        create()
                    }.show()
                }
            }
        }
    }

    private fun mediQRGoogleAuth(idToken: String) {
        MRQ.getInstance(applicationContext).addToRequestQueue(object : JsonObjectRequest(
            Method.POST,
            "${getString(R.string.service_url)}/accounts",
            JSONObject("""{ query: "{ account { domain authorized } }" }"""),
            Response.Listener {
                Log.d(_tag, it.toString())
                if (it.has("errors")) {
                    main_error_recycler.adapter = ErrorRecyclerAdapter(it.getJSONArray("errors"))
                }
                it.getJSONObject("data").let { data ->
                    if (data.isNull("account")) {
                        MaterialAlertDialogBuilder(this).apply {
                            setTitle("Register")
                            setMessage("By pressing continue, you'll be registering for MediQR as a Pharmacist.")
                            setCancelable(false)
                            setPositiveButton("Continue") { dialog, _ ->
                                mediQRGoogleRegister(idToken)
                                dialog.dismiss()
                            }
                            setNegativeButton("Cancel") { dialog, _ ->
                                dialog.dismiss()
                                finish()
                            }
                            create()
                        }.show()
                    } else {
                        startActivity(Intent(applicationContext, HomeActivity::class.java))
                        finish()
                    }
                }
            },
            Response.ErrorListener {
                Log.e(_tag, it.toString())
                MaterialAlertDialogBuilder(this).apply {
                    setTitle("Oops!")
                    setMessage("MediQR Server didn't respond, please try again.")
                    setCancelable(false)
                    setNegativeButton("Exit") { dialog, _ ->
                        dialog.dismiss()
                        finish()
                    }
                    create()
                }.show()
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> =
                mutableMapOf<String, String>().apply {
                    this["access_token"] = idToken
                }
        })
    }

    private fun mediQRGoogleRegister(idToken: String) {
        MRQ.getInstance(applicationContext).addToRequestQueue(object : JsonObjectRequest(
            Method.POST,
            "${getString(R.string.service_url)}/accounts",
            JSONObject("""{query:"mutation{create(domain: PHARMA){_id}}"}"""),
            Response.Listener {
                Log.d(_tag, it.toString())
                if (it.has("errors")) {
                    main_error_recycler.adapter = ErrorRecyclerAdapter(it.getJSONArray("errors"))
                }
                it.getJSONObject("data").let { data ->
                    if (data.isNull("create")) {
                        Toast.makeText(applicationContext, "Failed to register", Toast.LENGTH_LONG)
                            .show()
                    } else {
                        onStart()
                    }
                }
            },
            Response.ErrorListener {
                Log.e(_tag, it.toString())
                MaterialAlertDialogBuilder(this).apply {
                    setTitle("Oops!")
                    setMessage("MediQR Server didn't respond, please try again.")
                    setCancelable(false)
                    setNegativeButton("Exit") { dialog, _ ->
                        dialog.dismiss()
                        finish()
                    }
                    create()
                }.show()
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> =
                mutableMapOf<String, String>().apply {
                    this["access_token"] = idToken
                }
        })
    }
}
