package edu.uw.ee590.sensors

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import java.util.*

class LogDataAdapter constructor(context: Context, listener: LogDataItemClickedListener ) :
    RecyclerView.Adapter<LogDataAdapter.LogViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context);
    private lateinit var listener:LogDataItemClickedListener;

    init {
        this.listener = listener;
    }

    private var logs : ArrayList<LogData> = ArrayList<LogData>(); // Cached copy of words

    inner class LogViewHolder(itemView: View, val clickListener: LogDataItemClickedListener) :
        RecyclerView.ViewHolder(itemView) {

        init {
            itemView.setOnClickListener {
                clickListener.OnLogDataItemClicked(adapterPosition);
            }
        }

        val timeStampArea: TextView = itemView.findViewById(R.id.tvTimestamp);
        val tvTypeArea: TextView = itemView.findViewById(R.id.tvType);
        val typeAreaImg: ImageView = itemView.findViewById(R.id.ivType);
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val itemView = inflater.inflate(R.layout.logdatarecyclerview, parent, false)
        return LogViewHolder(itemView, this.listener);
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        val current : LogData = logs[position];
        val dt: Date = Date(current.timeStamp);
        holder.timeStampArea.text = dt.toString(); // .toString();
        if (current.alertType.toLowerCase() == "critical") {
            holder.typeAreaImg.setImageResource(R.drawable.critical); // current.audioFile.toString();
        } else {
            holder.typeAreaImg.setImageResource(R.drawable.warning); // current.audioFile.toString();
        }

        holder.tvTypeArea.text = current.alertType;

        // holder.timeStampArea.text = current.timeStampMs.toString();
    }

    internal fun setLogs(logs: ArrayList<LogData>) {
        this.logs = logs;
        notifyDataSetChanged();
    }

    override fun getItemCount() = logs.size;
}

interface LogDataItemClickedListener {
    fun OnLogDataItemClicked(position: Int) : Unit;
}
