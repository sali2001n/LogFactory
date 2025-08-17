package com.sali.logfactory

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.sali.logfactory.factory.LogFactory
import com.sali.logfactory.models.LogType
import com.sali.logfactory.ui.theme.LogFactoryTheme
import java.io.IOException

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        LogFactory.log(
            logType = LogType.INFO,
            tag = "Log tag",
            message = "Log message"
        )

        try {
            throw IOException("Some message...")
        } catch (ioException: IOException) {
            LogFactory.log(
                logType = LogType.ERROR,
                tag = "Log tag",
                message = "Log message",
                throwable = ioException
            )
        }

        setContent {
            LogFactoryTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    LogFactoryTheme {
        Greeting("Android")
    }
}