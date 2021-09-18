package com.ivydad.module.hookdatasize

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tv_click.setOnClickListener {

            val intent = Intent(this,SecondActivity::class.java)
            intent.putExtra("data", ByteArray(600*1024))
            startActivity(intent)
        }
    }
}