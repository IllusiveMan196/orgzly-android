package com.orgzly.android

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

class OrgzlyTestRunner : AndroidJUnitRunner() {

    @Throws(InstantiationException::class, IllegalAccessException::class, ClassNotFoundException::class)
    override fun newApplication(cl: ClassLoader, className: String, context: Context): Application {
        // TODO: Get rid of this
        App.appContext = context

        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }
}