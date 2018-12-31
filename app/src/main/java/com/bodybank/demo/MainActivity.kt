package com.bodybank.demo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.bodybank.ui.history.EstimationHistoryListFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        supportActionBar?.hide()
        supportFragmentManager?.beginTransaction()?.replace(R.id.fragmentContainer, EstimationHistoryListFragment())
            ?.commit()
    }
}
