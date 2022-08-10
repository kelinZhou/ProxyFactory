package com.kelin.proxyfactorydemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.kelin.proxyfactory.ProxyFactory
import com.kelin.proxyfactory.R
import io.reactivex.Observable

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


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
}