package com.example.rale.jobintentservicetimeout

import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import com.firebase.jobdispatcher.JobParameters
import com.firebase.jobdispatcher.JobService
import java.util.concurrent.TimeUnit

private const val TAG = "MainServiceFirebase"
class MainServiceFirebase: JobService() {

    private var isStopped = false

    override fun onStopJob(job: JobParameters?): Boolean {
        Log.d(TAG, "onStopJob called")
        isStopped = true
        return false
    }

    override fun onStartJob(jobParameters: JobParameters?): Boolean {
        Log.d(TAG, "onStartJob called $jobParameters")
        jobParameters?.let {
            val job = MainService.convertWorkToJob(it.extras)
            val msg = "Work for $job"
            Log.d(TAG, msg)
            try {
                TimeUnit.SECONDS.sleep(job.timeout.toLong())
                if (isStopped) {
                    Log.d(TAG, "$msg is stopped")
                    return false
                }
                Log.d(TAG, "$msg is finished")
                val intent = Intent(MainService.JOB_FINISHED_ACTION).apply {
                    putExtra(MainService.WORK_ID_KEY, job.id)
                    putExtra(MainService.JOB_FINISHED_KEY, true)
                }
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
            } catch (e: InterruptedException) {
                Log.d(TAG, "$msg was interrupted")
                Thread.currentThread().interrupt()
            }
        }
        return false
    }
}