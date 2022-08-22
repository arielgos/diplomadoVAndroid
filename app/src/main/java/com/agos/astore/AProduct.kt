package com.agos.astore

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity


class AProduct : AppCompatActivity() {

    override fun onBackPressed() {
        setResult(RESULT_CANCELED, Intent())
        finish()
    }
}