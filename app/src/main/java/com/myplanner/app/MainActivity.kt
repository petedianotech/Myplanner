package com.myplanner.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.myplanner.app.ui.navigation.MyPlannerNavGraph
import com.myplanner.app.ui.theme.MyPlannerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyPlannerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MyPlannerNavGraph()
                }
            }
        }
    }
}
