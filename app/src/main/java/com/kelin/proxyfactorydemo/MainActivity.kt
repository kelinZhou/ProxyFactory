package com.kelin.proxyfactorydemo

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.kelin.apiexception.ApiException
import com.kelin.proxyfactory.ProxyFactory
import com.kelin.proxyfactory.Toaster
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.RuntimeException
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private var id = 0

    private val testProxy by lazy {
        ProxyFactory.createIdProxy<Int, String> { id ->
            Observable.just(id).map {
                Thread.sleep(500)
                "我是ID=${it}的结果"
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ProxyFactory.init(application, ToasterImpl())

        testProxy.bind(this)
            .onSuccess { id, data ->
                Log.i("MainActivity=====", "id=$id | data=$data")
            }

        btnTest.setOnClickListener {
            testProxy.request(id++)
        }

        //Use ProxyFactory simple。
        ProxyFactory.createProxy {
            Observable.create<String> {
                it.onNext("正在加载中……")
                Thread.sleep(2000)
//                throw RuntimeException("加载异常")
//                Thread.sleep(2000)
                it.onNext("加载成功！")
                it.onComplete()
            }  // Do something for Observable.
        }.bind(this) // Bind the lifecycle owner.
            .progress(this)
            .onSuccess {
                // Do something when success.
                tvResult.text = it
            }
            .onFailed {
                // Do something when failed.
                tvResult.text = it.displayMessage
            }
            .request()
    }

    private inner class ToasterImpl : Toaster {
        override fun handError(e: Throwable): ApiException {
            return e as? ApiException ?: ApiException(-10, e.message)
        }

        override fun showFailedToast(e: ApiException) {
            Toast.makeText(applicationContext, e.displayMessage, Toast.LENGTH_LONG).show()
        }

        override fun showProgress(context: Context, progressTip: String?) {
            tvStatus.text = "加载中，请稍后"
        }

        override fun hideProgress(context: Context) {
            tvStatus.text = "加载完毕"
        }
    }
}