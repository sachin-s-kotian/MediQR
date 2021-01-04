package com.example.mediqr

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {

    private val _scan: Int = 2134
    private val _tag = "appx"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        try {
            GoogleSignIn.getLastSignedInAccount(applicationContext)!!.givenName!!.let {
                greeting_text.text = getString(R.string.greet_text).replace("User", it)
            }
        } catch (exception: NullPointerException) {
            Log.e(_tag, exception.toString())
            Toast.makeText(applicationContext, "You must be logged in", Toast.LENGTH_SHORT).show()
            finish()
        }

        scan_button.setOnClickListener {
            startActivityForResult(Intent(applicationContext, ScanActivity::class.java), _scan)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        MenuInflater(this).inflate(R.menu.home_activity_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.settings_option -> {
                startActivity(Intent(applicationContext, SettingsActivity::class.java))
                true
            }
            else -> false
        }
    }
}
