package seng440.vaccinepassport.room

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import seng440.vaccinepassport.MainActivity
import seng440.vaccinepassport.R
import seng440.vaccinepassport.VaccineType
import java.text.SimpleDateFormat

class VPassAdapter(private var vPasses: List<VPassData>, private val onVPassListener: OnVPassListener, private val context: Context)
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
        internal val approvalTxt : TextView
        internal val approvalTitle : TextView
        private val itemCard : MaterialCardView
        internal val trashButton: ImageButton


        init {
            vaccIdDisplay = itemView.findViewById(R.id.txt_vaccine_name)
            drsName = itemView.findViewById(R.id.txt_dr_name)
            dosageNum = itemView.findViewById(R.id.txt_dose_num)
            vaccineDate = itemView.findViewById(R.id.txt_vaccine_date)
            country = itemView.findViewById(R.id.txt_country)
            name = itemView.findViewById(R.id.txt_person_name)
            approvalTxt = itemView.findViewById(R.id.txt_approval_status)
            approvalTitle = itemView.findViewById(R.id.lbl_approval_status)

            itemCard = itemView.findViewById(R.id.vpass_item_card)
            trashButton = itemView.findViewById(R.id.trash_btn)

            itemCard.setOnClickListener { onClick(itemView) }
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
        val vaccineTypeData = VaccineType.fromId(vaccine)
        if (vaccineTypeData != null) {
            viewHolder.vaccIdDisplay.text = vaccineTypeData.fullName
        } else {
            viewHolder.vaccIdDisplay.text = vaccineTypeData.toString()
        }
        viewHolder.drsName.text = rawData.drAdministered
        viewHolder.dosageNum.text = rawData.dosageNum.toString()
        Log.d("Data", "Date : " + rawData.date.toString())
        viewHolder.vaccineDate.text = dateFormatter.format(MainActivity.timestampToDate(rawData.date))
        viewHolder.country.text = rawData.country
        viewHolder.name.text = rawData.name

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val isBorderMode: Boolean = sharedPreferences.getBoolean("border_mode", false)
        val isLogging: Boolean = sharedPreferences.getBoolean("logging_mode", false) && isBorderMode


        viewHolder.approvalTxt.visibility = if (isLogging) View.VISIBLE else View.GONE
        viewHolder.approvalTitle.visibility = if (isLogging) View.VISIBLE else View.GONE
        viewHolder.approvalTxt.text = if (rawData.approved) context.getString(R.string.vpass_approved) else context.getString(R.string.vpass_rejected)

        viewHolder.trashButton.setOnClickListener {
            onVPassListener.onVPassDelete(vPasses[position])
        }
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