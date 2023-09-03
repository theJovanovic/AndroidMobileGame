package elfak.mosis.thedefiner

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import elfak.mosis.thedefiner.Adapters.LeaderboardAdapter
import elfak.mosis.thedefiner.databinding.FragmentLeaderboardBinding

class LeaderboardFragment : Fragment() {

    private var layoutManager: RecyclerView.LayoutManager? = null
    private var adapter: RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>? = null
    private lateinit var binding: FragmentLeaderboardBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLeaderboardBinding.inflate(inflater, container, false)

        layoutManager = LinearLayoutManager(requireContext())
        binding.leaderboardRv.layoutManager = layoutManager

        adapter = LeaderboardAdapter(requireContext())
        binding.leaderboardRv.adapter = adapter

        return binding.root
    }
}