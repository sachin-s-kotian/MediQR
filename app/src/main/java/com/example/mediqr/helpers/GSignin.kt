package com.example.mediqr.helpers

import android.content.Context
import com.example.mediqr.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

class GSignin constructor(context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: GSignin? = null

        fun getInstance(context: Context) =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: GSignin(context).also {
                    INSTANCE = it
                }
            }
    }

    private val signinOptions: GoogleSignInOptions by lazy {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).apply {
            requestEmail()
            requestId()
            requestProfile()
            requestIdToken(context.getString(R.string.client_id))
        }.build()
    }

    val signinClient: GoogleSignInClient by lazy {
        GoogleSignIn.getClient(context, signinOptions)
    }
}