package com.aprz.transformapidemo

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.e("dd", "1")
        super.onCreate(savedInstanceState)
        Log.e("dd", "9")
        setContentView(R.layout.activity_main)
    }
}
