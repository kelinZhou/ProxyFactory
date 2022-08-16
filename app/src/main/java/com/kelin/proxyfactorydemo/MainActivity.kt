package com.kelin.proxyfactorydemo

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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

    private class ToasterImpl:Toaster {
        override fun handError(e: Throwable): ApiException? {
            TODO("Not yet implemented")
        }

        override fun showFailedToast(e: ApiException) {
            TODO("Not yet implemented")
        }

        override fun showProgress(context: Context, progressTip: String?) {
            TODO("Not yet implemented")
        }

        override fun hideProgress(context: Context) {
            TODO("Not yet implemented")
        }
    }
}