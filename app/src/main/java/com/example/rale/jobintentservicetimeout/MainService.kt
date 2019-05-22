package com.example.rale.jobintentservicetimeout

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.JobIntentService
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean


class MainService: JobIntentService() {

    private var handledJob = -1

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
    }

    override fun onHandleWork(p0: Intent) {
        if (shouldStop.get()) {
            setInterruptIfStopped(true)
            stopSelf()
        }
        SingletonExample.getBusySingleton()
        val job = convertWorkToJob(p0)
        handledJob = job.id
        val msg = "Work for $job"
        Log.d(TAG, msg)
        try {
            TimeUnit.SECONDS.sleep(job.timeout.toLong())
            if (isStopped) {
                Log.d(TAG, "$msg is stopped")
                return
            }
            Log.d(TAG, "$msg is finished")
            val intent = Intent(JOB_FINISHED_ACTION).apply {
                putExtra(WORK_ID_KEY, job.id)
                putExtra(JOB_FINISHED_KEY, true)
            }
            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
        } catch (e: InterruptedException) {
            Log.d(TAG, "$msg was interrupted")
            Thread.currentThread().interrupt()
        }
    }

    override fun onStopCurrentWork(): Boolean {
        val result = super.onStopCurrentWork()
        Log.d(TAG, "onStopCurrentWork $result for job=$handledJob")
        val intent = Intent(JOB_FINISHED_ACTION).apply {
            putExtra(WORK_ID_KEY, handledJob)
            putExtra(JOB_FINISHED_KEY, false)
        }
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
        return result
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }

    companion object {
        private const val TAG = "MainService"
        private const val JOB_ID = 1234
        const val JOB_FINISHED_ACTION = "jobFinishedAction"
        const val JOB_FINISHED_KEY = "isJobFinished"
        const val WORK_ID_KEY = "workIdKey"
        const val WORK_NAME_KEY = "workNameKey"
        const val WORK_TIMEOUT_KEY = "workTimeoutKey"
        val shouldStop = AtomicBoolean(true)

        fun enqueueWork(ctx: Context, work: Intent) {
            val job = convertWorkToJob(work)
            Log.d(TAG, "Enqueued work for $job")
            enqueueWork(ctx, MainService::class.java, JOB_ID, work)
        }

        fun convertWorkToJob(work: Intent): Job = convertWorkToJob(work.extras)

        fun convertWorkToJob(extras: Bundle?): Job {
            extras?.let {
                val id = it.getInt(WORK_ID_KEY)
                val timeOut = it.getInt(WORK_TIMEOUT_KEY)
                val name = it.getString(WORK_NAME_KEY)
                return Job(id, name, timeOut)
            }
            return Job(-1, "default", 1)
        }
    }
}