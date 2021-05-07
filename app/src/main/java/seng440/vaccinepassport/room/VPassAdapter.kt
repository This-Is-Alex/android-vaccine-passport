//package seng440.vaccinepassport.room
//
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.TextView
//import androidx.recyclerview.widget.RecyclerView
//import seng440.vaccinepassport.R
//
//class VPassAdapter(private var vPasses: List<VPassData>, private val onVPassListener: OnVPassListener)
//    : RecyclerView.Adapter<VPassAdapter.VPassViewHolder>() {
//
//    class VPassViewHolder(itemView: View, val onVPassListener: OnVPassListener)
//        : RecyclerView.ViewHolder(itemView), View.OnClickListener {
//
//        val vaccIdDisplay: TextView
//
//        init {
//            vaccIdDisplay = itemView.findViewById(R.id.vaccIdDisplay)
//
//            itemView.setOnClickListener(this)
//        }
//
//        override fun onClick(view: View?) {
//            OnVPassListener.onVPassClick(adapterPosition)
//        }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VPassViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.vPass_item, parent, false)
//        return VPassViewHolder(view, OnVPassListener)
//    }
//
//    override fun onBindViewHolder(viewHolder: VPassViewHolder, position: Int) {
//        viewHolder.vaccIdDisplay.text = "bsr: ${vPasses[position].vacId}"
//    }
//
//    override fun getItemCount() = vPasses.size
//
//    fun setData(newVPasses: List<VPassData>) {
//        vPasses = newVPasses
//        notifyDataSetChanged()
//    }
//
//
//    interface OnVPassListener {
//        fun onVPassClick(position: Int)
//        fun onVPassDelete(vPass: VPassData)
//        abstract fun getPreferences(modePrivate: Int): Any
//    }
//
////    fun deleteMap(position: Int) {
////        viewModel.deleteMap(viewModel.friends.value!![position])
////    }
//}