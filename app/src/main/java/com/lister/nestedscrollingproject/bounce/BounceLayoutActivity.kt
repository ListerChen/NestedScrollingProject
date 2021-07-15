package com.lister.nestedscrollingproject.bounce

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lister.nestedscrolltest.R
import com.lister.nestedscrollingproject.util.CustomAdapter

class BounceLayoutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bounce_layout)

        val rv: RecyclerView = findViewById(R.id.bounce_rv)
        val adapter = CustomAdapter()
        adapter.setData(getDataList())
        rv.adapter = adapter
        rv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }

    private fun getDataList(): List<String> {
        val dataList: MutableList<String> = mutableListOf()
        for (index in 1..30) {
            dataList.add(index.toString())
        }
        return dataList
    }
}