package com.barakwebsoft.shopzen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.barakwebsoft.shopzen.core.navigation.ShopZenApp
import com.barakwebsoft.shopzen.ui.theme.ShopZenTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShopZenTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ShopZenApp()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ShopZenAppPreview() {
    ShopZenTheme {
        ShopZenApp()
    }
}