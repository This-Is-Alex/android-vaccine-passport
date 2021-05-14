package seng440.vaccinepassport.room

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import seng440.vaccinepassport.R
import seng440.vaccinepassport.VaccineType

class VPassAdapter(private var vPasses: List<VPassData>, private val onVPassListener: OnVPassListener)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private inner class VPassViewHolder internal constructor(itemView: View/*, val onVPassListener: OnVPassListener*/)
        : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        internal val vaccIdDisplay: TextView
        internal val drsName: TextView
        internal val dosageNum : TextView
        internal val vaccineDate : TextView
        internal val country : TextView
        internal val name : TextView
        internal val passportNum : TextView

        init {
            vaccIdDisplay = itemView.findViewById(R.id.txt_vaccine_name)
            drsName = itemView.findViewById(R.id.txt_dr_name)
            dosageNum = itemView.findViewById(R.id.txt_dose_num)
            vaccineDate = itemView.findViewById(R.id.txt_vaccine_date)
            country = itemView.findViewById(R.id.txt_country)
            name = itemView.findViewById(R.id.txt_person_name)
            passportNum = itemView.findViewById(R.id.txt_passport_number)
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            //OnVPassListener.onVPassClick(adapterPosition)
        }

        internal fun bind(position: Int) {
            val rawData = vPasses[position]
            val vaccine = rawData.vacId
            vaccIdDisplay.text = VaccineType.fromId(vaccine).toString()
            drsName.text = rawData.drAdministered
            dosageNum.text = rawData.dosageNum.toString()
            vaccineDate.text = rawData.date.toString()
            country.text = rawData.country
            name.text = rawData.name
            passportNum.text = rawData.passportNum
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.vaccine_list_item, parent, false)
        return VPassViewHolder(view/*, OnVPassListener*/)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        (viewHolder as VPassViewHolder).bind(position)
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

//    fun deleteMap(position: Int) {
//        viewModel.deleteMap(viewModel.friends.value!![position])
//    }
}