package com.example.rale.jobintentservicetimeout

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.firebase.jobdispatcher.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlin.properties.Delegates


class MainActivity : AppCompatActivity() {

    private var counter = 0
    private var jobsAdapter by Delegates.notNull<JobsAdapter>()
    private val receiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                if (it.action == MainService.JOB_FINISHED_ACTION) {
                    val id = it.extras.getInt(MainService.WORK_ID_KEY)
                    val successful = it.extras.getBoolean(MainService.JOB_FINISHED_KEY)
                    jobsAdapter.jobHandled(id, successful)
                }
            }
        }

    }
    private var dispatcher by Delegates.notNull<FirebaseJobDispatcher>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        jobsAdapter = JobsAdapter()
        jobs.adapter = jobsAdapter
        dispatcher = FirebaseJobDispatcher(GooglePlayDriver(applicationContext))
        fab.setOnClickListener { view ->
            counter++
            val job = Job(counter, "Worker $counter", timeout.text.toString().toInt())
            jobsAdapter.addJob(job)
            dispatchJob(job.id, job.name, job.timeout)
//            enqueueWork(job.id, job.name, job.timeout)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(receiver, IntentFilter(MainService.JOB_FINISHED_ACTION))
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(receiver)
        Log.d("MainActivity", "onStop")
//        MainService.shouldStop.set(true)

    }

    private fun enqueueWork(id: Int, name: String, timeout: Int) {
        intent = Intent(applicationContext, MainService::class.java).apply {
            putExtra(MainService.WORK_ID_KEY, id)
            putExtra(MainService.WORK_NAME_KEY, name)
            putExtra(MainService.WORK_TIMEOUT_KEY, timeout)
        }
        MainService.enqueueWork(applicationContext, intent)
    }

    private fun dispatchJob(id: Int, name: String, timeout: Int) {
        val extra = Bundle().apply {
            putInt(MainService.WORK_ID_KEY, id)
            putString(MainService.WORK_NAME_KEY, name)
            putInt(MainService.WORK_TIMEOUT_KEY, timeout)
        }
        val job = dispatcher.newJobBuilder()
                // the JobService that will be called
                .setService(MainServiceFirebase::class.java)
                // uniquely identifies the job
                .setTag("$id")
                // one-off job
                .setRecurring(false)
                // don't persist past a device reboot
                .setLifetime(Lifetime.UNTIL_NEXT_BOOT)
                // start between 0 and 60 seconds from now
                .setTrigger(Trigger.executionWindow(0, 1))
                // don't overwrite an existing job with the same tag
                .setReplaceCurrent(false)
                // retry with exponential backoff
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                // constraints that need to be satisfied for the job to run
//                .setConstraints(
//                        // only run on an unmetered network
//                        Constraint.ON_UNMETERED_NETWORK,
//                        // only run when the device is charging
//                        Constraint.DEVICE_CHARGING
//                )
                .setExtras(extra)
                .build()
        dispatcher.mustSchedule(job)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MainActivity", "onDestroy")
//        stopService(Intent(applicationContext, MainService::class.java))
//        MainService.shouldStop.set(true)
        dispatcher.cancelAll()
    }
}
