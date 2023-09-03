package elfak.mosis.thedefiner.Adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import elfak.mosis.thedefiner.Controllers.LeaderboardController
import elfak.mosis.thedefiner.Models.LeaderboardItem
import elfak.mosis.thedefiner.R


@SuppressLint("NotifyDataSetChanged")
class LeaderboardAdapter(private val context: Context) :
    RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>() {

    private var leaderboardInfo: ArrayList<LeaderboardItem> = ArrayList()
    private var leaderboardController: LeaderboardController = LeaderboardController()

    init {
        leaderboardController.getLeaderboardInfo { fetchedLeaderboardInfo ->
            leaderboardInfo = ArrayList(fetchedLeaderboardInfo)
            leaderboardInfo.sortByDescending { it.points }
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LeaderboardAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.leaderboar_card_layout, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return leaderboardInfo.size
    }

    override fun onBindViewHolder(holder: LeaderboardAdapter.ViewHolder, position: Int) {
        Glide.with(this.context)
            .load(leaderboardInfo[position].photoURL)
            .into(holder.profileImageIv)
        holder.userUsernameTv.text = leaderboardInfo[position].username
        holder.userPointsTv.text = "${leaderboardInfo[position].points.toString()} points"
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var profileImageIv: ImageView
        var userUsernameTv: TextView
        var userPointsTv: TextView

        init {
            profileImageIv = itemView.findViewById(R.id.profileImageIv)
            userUsernameTv = itemView.findViewById(R.id.userUsernameTv)
            userPointsTv = itemView.findViewById(R.id.userPointsTv)
        }
    }

}
