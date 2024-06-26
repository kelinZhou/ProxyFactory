# ProxyFactory
[![](https://jitpack.io/v/kelinZhou/ProxyFactory.svg)](https://jitpack.io/#kelinZhou/ProxyFactory)

###### 基于RxJava封装的异步任务代理框架，可以快速、方便的使用其开发异步相关的业务需求。
* * *

## 下载
###### 第一步：添加 JitPack 仓库到你项目根目录的 gradle 文件中。
```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```
###### 第二步：添加这个依赖。
```
dependencies {
    implementation "com.github.kelinZhou:ProxyFactory:${LastVersion}"
}
```

## 使用
在使用之前需要先对其进行初始化。
```kotlin
ProxyFactory.init(application, ToasterImpl(), isDebug, !isDebug)

class ProxyEventHandlerImpl : ProxyEventHandler {
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
        showProgressDialog(context, progressTip)
    }

    /**
     * 隐藏加载中的样式。
     */
    override fun hideProgress(context: Context) {
        dismissProgressDialog(context)
    }
}
```
### Proxy
ProxyFactory提供了以下几类的Proxy用于处理不同业务场景。

#### 1. DataProxy 只关心返回数据的代理。
使用[DataProxy](/ProxyFactory/src/main/java/com/kelin/proxyfactory/DataProxy.kt)您可以方便的执行异步任务。
```kotlin
ProxyFactory.createProxy { Observable.just("I'm Result!") }
    .onSuccess { data ->
        // Do something with data.
    }
    .onFailed { e ->
        // Do something with Exception.
    }
    .request()  //请求启动任务。
```

#### 2.ActionDataProxy 带有动作的代理。
使用[ActionDataProxy](/ProxyFactory/src/main/java/com/kelin/proxyfactory/ActionDataProxy.kt)您可以方便的执行异步任务并关系其动作。
```kotlin
ProxyFactory.createActionProxy { Observable.just("I'm Result for.") }
    .onSuccess { data, action ->
        // Do something with data & action.
    }
    .onFailed { e, action ->
        // Do something with data & action.
    }
    .request(LoadAction.LOAD)
```
不同于`DataProxy`，`ActionDataProxy`必须在发起请求时传入动作(`action`)[LoadAction](/ProxyFactory/src/main/java/com/kelin/proxyfactory/LoadAction.kt)或[ActionParameter](/ProxyFactory/src/main/java/com/kelin/proxyfactory/ActionParameter.kt)，然后可以在回调中获取到action。

#### 3.IdDataProxy 支持请求参数的代理。
使用[IdDataProxy](/ProxyFactory/src/main/java/com/kelin/proxyfactory/IdDataProxy.kt)您可以方便的执行异步任务并传递参数。
```kotlin
ProxyFactory.createIdProxy<String, String> { id -> Observable.just("I'm Result for $id.") }
    .onSuccess { id, data -> 
        // Do something with data & id.
    }
    .onFailed { id, e -> 
        // Do something with Exception & id.
    }
    .request("Kelin")
```
不同于`DataProxy`，`IdDataProxy`可以在回调中获取到发起任务时传入的参数。

#### 4.IdActionDataProxy 同时支持请求参和动作的代理。
```kotlin
ProxyFactory.createIdActionProxy<String, String> { id -> Observable.just("I'm Result for $id.") }
    .bind(this, object :IdActionDataProxy.IdActionDataCallback<String, ActionParameter, String>{
        override fun onSuccess(id: String, action: ActionParameter, data: String) {
            // Do something with data & id & action.
        }

        override fun onFailed(id: String, action: ActionParameter, e: ApiException) {
            // Do something with data & id & action.
        }
    })
    .request(ActionParameter.createInstance(), "Kelin")
```
需要注意的是[IdActionDataProxy](/ProxyFactory/src/main/java/com/kelin/proxyfactory/IdActionDataProxy.kt)不再支持`onSuccess`和`onFailed`的方式设置回调。

### ActionParameter
动作及功能参数，[ActionParameter](/ProxyFactory/src/main/java/com/kelin/proxyfactory/ActionParameter.kt)的核心就是[LoadAction](/ProxyFactory/src/main/java/com/kelin/proxyfactory/LoadAction.kt)。而`LoadAction`是一个枚举类，主要成员如下：
    
* LOAD ：没有数据load。对于分页的，load总是第一页的数据。
* RETRY ：load失败，retry（这个不叫refresh！！！！）。对于分页的，load总是第一页的数据。
* REFRESH ： 已经load成功，再次load。对于分页的，load总是第一页的数据。
* AUTO_REFRESH ： 已经load成功，再次load。对于分页的，load总是已经加载过的所有页。
* LOAD_MORE ： 加载更多(分页加载)。

说到分页`ActionParameter`还有个子类[PageActionParameter](/ProxyFactory/src/main/java/com/kelin/proxyfactory/PageActionParameter.kt)，`PageActionParameter`可以用来处理分页逻辑。

如果要使用分页加载则需要用到`ProxyFactory`的`createPageActionProxy`方法或`createPageIdActionProxy`方法，下面以`createPageIdActionProxy`方法举例：
```kotlin
ProxyFactory.createPageIdActionProxy<String, String> { id, pages ->  Observable.just("I'm Result for $id. Pages(page:${pages.page}, size:${pages.size}).") }
    .bind(this, object :IdActionDataProxy.IdActionDataCallback<String, ActionParameter, String>{
        override fun onSuccess(id: String, action: ActionParameter, data: String) {
            // Do something with data & id & action.
        }

        override fun onFailed(id: String, action: ActionParameter, e: ApiException) {
            // Do something with data & id & action.
        }
    })
    .request(PageActionParameter.createInstance(true, 20), "Kelin")
```
上面的栗子中`PageActionParameter.createInstance(true, 20)`参数`true`表示启用分页加载，`20`表示每页的数量。

## 注意事项

### bind方法
所有的Proxy都是支持通过调用其`bind`方法为其绑定到生命周期组件`LifecycleOwner`的，是为了防止页面销毁后任务没有销毁而回调又是内部类从而可能导致内存泄露的问题。
当然，`bind`方法也不是强制要求调用的，没有调用过`bind`方法的Proxy的回调均为一次性回调，即无论是`onSuccess`还是`onFailed`被回调过一次后，回调就会立即从Proxy内部被移除引用。也就意味着没有调用过`bind`方法的Proxy不能被重复使用。

*下面列举一个Proxy使用不当的栗子：*
```kotlin
val proxy = ProxyFactory.createProxy { Observable.just("I'm Result!") }
    .onSuccess { data ->
        // Do something with data.
    }
    .onFailed { e ->
        // Do something with Exception.
    }
btnTest.setOnClickListener{
    proxy.request()  //第二次请求启动任务会得不到回调。
}
```
在这个栗子中，只有`btnTest`按钮被第一次点击的时候会执行`onSuccess`还是`onFailed`回调，而从第二次开始以及之后的所有点击都不会再执行`onSuccess`还是`onFailed`回调了。

*如果想要重复使用一个Proxy则需要对上面的代码进行改造：
```kotlin
val proxy = ProxyFactory.createProxy { Observable.just("I'm Result!") }
    .bind(activity) //如果是在Activity中使用则直接传入`this`，如果是在Fragment中使用则最好传入`viewLifecycleOwner`。
    .onSuccess { data ->
        // Do something with data.
    }
    .onFailed { e ->
        // Do something with Exception.
    }
btnTest.setOnClickListener{
    proxy.request()  //请求启动任务。
}
```

### progress方法
所有Proxy都支持在调用`request`方法时自动弹窗loading对话框。loading对话框的弹出和隐藏能力由[Toaster](/ProxyFactory/src/main/java/com/kelin/proxyfactory/Toaster.kt)接口中的`showProgress`和`hideProgress`方法提供。
*下面举个栗子来演示progress方法的用法：*
```kotlin
ProxyFactory.createProxy { Observable.just("I'm Result!") }
    .progress(context, "Please Wait...") //第一个参数为弹出对话框时所需要的上下文，第二个参数为loading对话框中需要显示的文字。
    .onSuccess { data ->
        // Do something with data.
    }
    .onFailed { e ->
        // Do something with Exception.
    }
    .request()  //请求启动任务。
```

### setNotToast方法
为了方便调用也为了回调更加灵活，所以`onSuccess`还是`onFailed`回调并不是都必须同时设置的，考虑到通常情况下`onFailed`回调只是为了提示用户，所以如果没有设置`onFailed`回调则默认会弹出Toast提示用户。弹出Toast的能力由[Toaster](/ProxyFactory/src/main/java/com/kelin/proxyfactory/Toaster.kt)接口中的`showFailedToast`方法提供。如果你不希望在失败时弹出Toast提示也不想设置`onFailed`回调则可以使用该方法。
*下面举个栗子来演示setNotToast方法的用法：*
```kotlin
ProxyFactory.createProxy { Observable.just("I'm Result!") }
    .setNotToast() //禁止在失败时自动弹出Toast。
    .onSuccess { data ->
        // Do something with data.
    }
    .request()  //请求启动任务。
```

### withoutNetWork方法
所有Proxy在执行request方法时都会默认检测网络状态，如果当前无网络则可能会请求失败。所以该方法是用来关闭网络状态检测的。
```kotlin
ProxyFactory.createProxy { Observable.just("I'm Result!") }
    .withoutNetWork() //禁用网络状态检测。
    .onSuccess { data ->
        // Do something with data.
    }
    .request()  //请求启动任务。
```

* * *
### License
```
Copyright 2023 kelin410@163.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```