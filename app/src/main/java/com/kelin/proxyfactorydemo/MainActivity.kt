package com.kelin.proxyfactorydemo

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.kelin.apiexception.ApiException
import com.kelin.proxyfactory.*
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

ProxyFactory.createPageIdActionProxy<String, String> { id, pages ->  Observable.just("I'm Result for $id. Pages(page:${pages.page}, size:${pages.size}).") }
    .bind(this, object :IdActionDataProxy.IdActionDataCallback<String, ActionParameter, String>{
        override fun onSuccess(id: String, action: ActionParameter, data: String) {
            TODO("Not yet implemented")
        }

        override fun onFailed(id: String, action: ActionParameter, e: ApiException) {
            TODO("Not yet implemented")
        }
    })
    .request(PageActionParameter.createInstance(true, 20), "Kelin")

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
        }.progress(this)
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

        /**
         * 处理异步任务中捕获的异常，如果你希望自己处理改异常则需要返回null，返回null之后Proxy的onFailed方法将不会被回调，否则会将你返回的ApiException回调给Proxy的onFailed方法。
         */
        override fun handError(e: Throwable): ApiException {
            return e as? ApiException ?: ApiException(-10, e.message)
        }

        /**
         * 显示调用异步任务失败时的提示。
         */
        override fun showFailedToast(e: ApiException) {
            Toast.makeText(applicationContext, e.displayMessage, Toast.LENGTH_LONG).show()
        }

        /**
         * 显示加载中的样式。
         */
        override fun showProgress(context: Context, progressTip: String?) {
            tvStatus.text = "加载中，请稍后"
        }

        /**
         * 隐藏加载中的样式。
         */
        override fun hideProgress(context: Context) {
            tvStatus.text = "加载完毕"
        }
    }
}