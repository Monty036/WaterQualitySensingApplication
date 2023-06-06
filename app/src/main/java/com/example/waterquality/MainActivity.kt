package com.example.waterquality

import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class MainActivity : AppCompatActivity() {


    private val tdsTextView: TextView by lazy { findViewById(R.id.TDS) }
    private val phTextView: TextView by lazy { findViewById(R.id.PH) }
    private val tempTextView: TextView by lazy { findViewById(R.id.Status) }
    private val alertView: TextView by lazy { findViewById(R.id.alert) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // This executes the code in it in every 4 seconds
        Timer().scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                // Replace "http://192.168.4.1" with the IP address of your ESP8266 server which you can find just by printing its IP address
                val url = URL("http://192.168.4.1")
                AsyncTaskHandleGet().execute(url) { response ->
                    // Parse the response as an array to take multiple inputs
                    val dataArray = response.split(",").map { it.trim().toDouble() }

                    // Display the values in the TextViews
                    val tds = dataArray[0].toString()
                    val ph = dataArray[1].toString()
                    val Sta = dataArray[2].toString()
                    //Here I have taken the value of pH ,TDS and status which I get the data from nodemcu or esp8266
                    runOnUiThread {
                        tdsTextView.text = "TDS: $tds"
                        phTextView.text = "pH: $ph"
                        tempTextView.text = "Status: $Sta"
                        if (dataArray[2].toInt() == 2) {
                            alertView.setBackgroundResource(R.color.green)
                        }
                        if (dataArray[2].toInt() == 1) {
                            alertView.setBackgroundResource(R.color.yellow)
                        }
                        if (dataArray[2].toInt() == 0) {
                            alertView.setBackgroundResource(R.color.red)
                        }
                    }
                }
            }
        }, 0, 4000)
    }



    // AsyncTask for handling the GET request to the ESP8266 server
    inner class AsyncTaskHandleGet : AsyncTask<URL, Void, String>() {
        private var callback: ((String) -> Unit)? = null

        override fun doInBackground(vararg urls: URL): String {
            val url = urls[0]
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            val inputStream = connection.inputStream
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            val response = StringBuilder()

            bufferedReader.use { reader ->
                var line = reader.readLine()
                while (line != null) {
                    response.append(line)
                    line = reader.readLine()
                }
            }

            return response.toString()
        }


        fun execute(url: URL, callback: (String) -> Unit) {
            this.callback = callback
            super.execute(url)
        }


        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            callback?.let { it(result) }
        }

    }
}