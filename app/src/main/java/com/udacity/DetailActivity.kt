package com.udacity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_detail.*
import android.content.Intent
import android.widget.TextView

/** This class takes arguments from the Notification's intent and displays them here ( if they were sent)
 * */

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)

        val urlTitle: TextView = findViewById(R.id.url)
        val successState: TextView = findViewById(R.id.success)

        if (intent.hasExtra("success or failure")){
            successState.setText(intent.getStringExtra("success or failure"))

            // changing the color of the response if negative
            if (intent.getStringExtra("success or failure") == "Failed"){
                successState.setTextColor(getResources().getColor(R.color.red))
                }
        }

        if (intent.hasExtra("chosen title")) {
            urlTitle.setText(intent.getStringExtra("chosen title"))
        }

    }



}
