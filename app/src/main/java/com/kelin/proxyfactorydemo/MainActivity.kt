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

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ProxyFactory.init(application, ToasterImpl())


        //Use ProxyFactory simple。
        ProxyFactory.createProxy {
            Observable.just(2)  // Do something for Observable.
        }.bind(this) // Bind the lifecycle owner.
            .onSuccess {
                // Do something when success.
            }
            .onFailed {
                // Do something when failed.
            }
            .request()
    }

    private inner class ToasterImpl : Toaster {
        override fun handError(e: Throwable): ApiException? {
            return e as? ApiException
        }

        override fun showFailedToast(e: ApiException) {
            Toast.makeText(applicationContext, e.displayMessage, Toast.LENGTH_LONG).show()
        }

        override fun showProgress(context: Context, progressTip: String?) {
            Log.i("Toaster", "正在加载中...")
        }

        override fun hideProgress(context: Context) {
            Log.i("Toaster", "加载完毕")
        }
    }
}