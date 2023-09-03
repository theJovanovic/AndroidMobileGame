package elfak.mosis.thedefiner.Adapters

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import elfak.mosis.thedefiner.Controllers.BaseListController
import elfak.mosis.thedefiner.Models.BaseListItem
import elfak.mosis.thedefiner.Models.BaseLocationSingleton
import elfak.mosis.thedefiner.R
import java.util.*
import kotlin.collections.ArrayList


@SuppressLint("NotifyDataSetChanged")
class BaseListAdapter(private val context: Context) :
    RecyclerView.Adapter<BaseListAdapter.ViewHolder>() {

    private var baselistInfo: ArrayList<BaseListItem> = ArrayList()
    private var baselistController: BaseListController = BaseListController()

    init {
        baselistController.getBaselistInfo { fetchedBaselistInfo ->
            baselistInfo = ArrayList(fetchedBaselistInfo)
            baselistInfo.sortByDescending { it.username }
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseListAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.base_list_card_layout, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return baselistInfo.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: BaseListAdapter.ViewHolder, position: Int) {
        Glide.with(this.context)
            .load(baselistInfo[position].photoURL)
            .into(holder.profileImageIv)
        val addressList = this.getAddressFromLocation(baselistInfo[position].latitude!!.toDouble(), baselistInfo[position].longitude!!.toDouble(), this.context)?.split(", ")
        if (addressList != null) {
            holder.addressTv.text = "Street: ${addressList[0]}\nCity: ${addressList[1]}\nCountry: ${addressList[2]}"
        }
        else {
            holder.addressTv.text = "Street: ?\nCity: ?\nCountry: ?"
        }
        holder.userIfnoTv.text = "${baselistInfo[position].username} (${baselistInfo[position].points.toString()} points)"
        holder.baseLocationTv.text = "Latitude: ${baselistInfo[position].latitude}\nLongitude: ${baselistInfo[position].longitude}"
        holder.showButton.setOnClickListener {
            BaseLocationSingleton.latitude = baselistInfo[position].latitude!!
            BaseLocationSingleton.longitude = baselistInfo[position].longitude!!
            Navigation.findNavController(it).popBackStack()
        }
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var profileImageIv: ImageView
        var addressTv: TextView
        var userIfnoTv: TextView
        var baseLocationTv: TextView
        var showButton: Button

        init {
            profileImageIv = itemView.findViewById(R.id.profileImageIv)
            addressTv = itemView.findViewById(R.id.addressTv)
            userIfnoTv = itemView.findViewById(R.id.userInfoTv)
            baseLocationTv = itemView.findViewById(R.id.baseLocationTv)
            showButton = itemView.findViewById(R.id.btnShow)
        }
    }

    fun getAddressFromLocation(latitude: Double, longitude: Double, context: Context): String? {
        val geocoder = Geocoder(context, Locale.getDefault())
        return try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]
                // Here, you can get more detailed address components if needed
                "${address.getAddressLine(0)}"
            } else {
                "No address found for the provided latitude and longitude"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "Cannot fetch address"
        }
    }

}
