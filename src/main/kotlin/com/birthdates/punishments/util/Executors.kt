package com.birthdates.punishments.util

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class Executors {
    companion object {
        val IO: ExecutorService = Executors.newCachedThreadPool()
    }
}