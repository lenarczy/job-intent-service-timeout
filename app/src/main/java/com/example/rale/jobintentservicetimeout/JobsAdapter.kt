package com.example.rale.jobintentservicetimeout

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.job_item.view.*

class JobsAdapter: RecyclerView.Adapter<JobsAdapter.ViewHolder>() {

    private val jobs = ArrayList<Job>()

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.job_item, p0, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = jobs.size

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        val job = jobs[p1]
        p0.bind(job)
    }

    fun addJob(job: Job) {
        jobs.add(job)
        notifyItemInserted(jobs.size - 1)
    }

    fun jobHandled(id: Int, successful: Boolean) {
        jobs.forEachIndexed { index, job -> updateJob(job, id, index, successful) }
    }

    private fun updateJob(job: Job, id: Int, index: Int, successful: Boolean) {
        if (job.id == id) {
            job.completed = if (successful) 1 else -1
            notifyItemChanged(index)
        }
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(job: Job) {
            itemView.idTw.text = job.id.toString()
            itemView.nameTw.text = job.name
            itemView.timeoutTw.text = "${job.timeout} s"
            itemView.statusTw.text = if (job.completed == 1) "Completed" else if (job.completed == 0) "Pending" else "Canceled"
        }
    }
}