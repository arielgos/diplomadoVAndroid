package com.example.diplomadov

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.postDelayed
import com.example.diplomadov.databinding.ActivitySplashBinding

class ASplash : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySplashBinding.inflate(layoutInflater)

        binding.root.postDelayed(2500) {
            startActivity(Intent(this@ASplash, AMain::class.java))
            finish()
        }

        setContentView(binding.root)

    }

    override fun onBackPressed() {
        //evitamos la accion del boton atras
    }
}