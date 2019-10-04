package com.example.nonblockinguiwithcoroutinesample

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

// CoruotineScope interfaceを実装するとActivity内にてコルーチンコンテキストを実装できるようになる
// つまり、GlobalScopeを使わなくてもcoroutineを生成できる
class MainActivity : AppCompatActivity(), CoroutineScope {

    // このActivityのルートJOB
    private val job = Job()

    // UIスレッドのコルーチンコンテキストを提供
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()

        button.setOnClickListener{ onClickButton() }
    }

    private fun onClickButton() {

        // ActivityコンテキストにCoroutineがあるのでどこでもコルーチンを生成できる
        launch { }
        async { }

        // 通常のlaunch生成はUIスレッド上で生成される（コルーチンコンテキストがUIスレッドなので）
//        launch {
//            // UIを3000ミリ秒ブロックする
//            Thread.sleep(3000)
//        }

        // コンテキストに別スレッドを生成しているのでUIスレッドをブロックしない
        // ただし、UIスレッド上ではないので注意
        launch(Dispatchers.Default) {
            // UIをブロックしない
            Thread.sleep(3000)
        }

        // このlaunchはUIスレッド
        launch {
            button.isActivated = false

            // このasyncは別スレッド上に生成しているのでブロックしない
            val msgAsync = async(Dispatchers.Default) {
                Thread.sleep(2000)
                "async"
            }

            // このスコープはUIスレッド上なのでbuttonを参照可能
            button.text = msgAsync.await()

            // withContext{} == async{}.await()
            val msg = withContext(Dispatchers.Default) {
                Thread.sleep(2000)
                "withContext"
            }
            button.text = msg

            // launchはasyncやwithContextと違って値は返せない
            launch(Dispatchers.Default) {
                Thread.sleep(2000)
            }.join()

            button.text = "RUN"
            button.isActivated = true
        }

        // launchはUIスレッド上にコルーチンを生成するがnon blockingなので
        // このトーストはlaunchのスコープの終了を待たずして表示される
        Toast.makeText(this, "pushed", Toast.LENGTH_SHORT).show()
    }
}
