package seng440.vaccinepassport.room

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import seng440.vaccinepassport.R
import seng440.vaccinepassport.VaccineType
import java.text.SimpleDateFormat

class VPassAdapter(private var vPasses: List<VPassData>, private val onVPassListener: OnVPassListener)
    : RecyclerView.Adapter<VPassAdapter.VPassViewHolder>() {

    private val dateFormatter = SimpleDateFormat("dd-MMM-yyyy")

    class VPassViewHolder (itemView: View, val onVPassListener: OnVPassListener)
        : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        internal val vaccIdDisplay: TextView
        internal val drsName: TextView
        internal val dosageNum : TextView
        internal val vaccineDate : TextView
        internal val country : TextView
        internal val name : TextView


        init {
            vaccIdDisplay = itemView.findViewById(R.id.txt_vaccine_name)
            drsName = itemView.findViewById(R.id.txt_dr_name)
            dosageNum = itemView.findViewById(R.id.txt_dose_num)
            vaccineDate = itemView.findViewById(R.id.txt_vaccine_date)
            country = itemView.findViewById(R.id.txt_country)
            name = itemView.findViewById(R.id.txt_person_name)
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            onVPassListener.onVPassClick(adapterPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VPassViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.vaccine_list_item, parent, false)
        return VPassViewHolder(view, onVPassListener)
    }

    override fun onBindViewHolder(viewHolder: VPassViewHolder, position: Int) {
        val rawData = vPasses[position]
        val vaccine = rawData.vacId
        viewHolder.vaccIdDisplay.text = VaccineType.fromId(vaccine).toString()
        viewHolder.drsName.text = rawData.drAdministered
        viewHolder.dosageNum.text = rawData.dosageNum.toString()
        viewHolder.vaccineDate.text = dateFormatter.format(rawData.date)
        viewHolder.country.text = rawData.country
        viewHolder.name.text = rawData.name
    }

    override fun getItemCount() = vPasses.size

    fun setData(newVPasses: List<VPassData>) {
        vPasses = newVPasses
        notifyDataSetChanged()
    }


    interface OnVPassListener {
        fun onVPassClick(position: Int)
        fun onVPassDelete(vPass: VPassData)
        abstract fun getPreferences(modePrivate: Int): Any
    }
}