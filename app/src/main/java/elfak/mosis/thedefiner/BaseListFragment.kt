package elfak.mosis.thedefiner

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import elfak.mosis.thedefiner.Adapters.BaseListAdapter
import elfak.mosis.thedefiner.databinding.FragmentBaseListBinding

class BaseListFragment : Fragment() {

    private var layoutManager: RecyclerView.LayoutManager? = null
    private var adapter: RecyclerView.Adapter<BaseListAdapter.ViewHolder>? = null
    private lateinit var binding: FragmentBaseListBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBaseListBinding.inflate(inflater, container, false)

        layoutManager = LinearLayoutManager(requireContext())
        binding.baselistRv.layoutManager = layoutManager

        adapter = BaseListAdapter(requireContext())
        binding.baselistRv.adapter = adapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }
}