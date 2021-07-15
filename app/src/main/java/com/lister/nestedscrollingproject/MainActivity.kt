
package com.lister.nestedscrollingproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.lister.nestedscrolltest.R
import com.lister.nestedscrollingproject.bounce.BounceLayoutActivity
import com.lister.nestedscrollingproject.coordinator.CoordinatorActivity
import com.lister.nestedscrollingproject.mixed.MixedLayoutActivity
import com.lister.nestedscrollingproject.simple.SimpleNestedActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.coordinator_layout_1).setOnClickListener {
            startActivity(Intent(this, CoordinatorActivity::class.java))
        }

        findViewById<Button>(R.id.nested_scroll_simple).setOnClickListener {
            startActivity(Intent(this, SimpleNestedActivity::class.java))
        }

        findViewById<Button>(R.id.nested_scroll_mixed_layout).setOnClickListener {
            startActivity(Intent(this, MixedLayoutActivity::class.java))
        }

        findViewById<Button>(R.id.nested_scroll_bounce).setOnClickListener {
            startActivity(Intent(this, BounceLayoutActivity::class.java))
        }
    }
}