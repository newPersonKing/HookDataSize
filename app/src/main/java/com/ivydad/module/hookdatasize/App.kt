package com.ivydad.module.hookdatasize

import android.app.Application

class App : Application() {


    override fun onCreate() {
        super.onCreate()
        HookUtil.hook()
    }

}