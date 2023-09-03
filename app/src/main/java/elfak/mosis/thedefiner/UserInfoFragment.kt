package elfak.mosis.thedefiner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import elfak.mosis.thedefiner.Controllers.UserController
import elfak.mosis.thedefiner.Controllers.PlayerController
import elfak.mosis.thedefiner.databinding.FragmentUserInfoBinding

class UserInfoFragment : Fragment() {

    private lateinit var binding: FragmentUserInfoBinding
    private lateinit var userController: UserController
    private lateinit var playerController: PlayerController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserInfoBinding.inflate(inflater, container, false)
        userController = UserController()
        playerController = PlayerController()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLeaderboard.setOnClickListener {
            findNavController().navigate(R.id.action_userInfoFragment_to_leaderboardFragment)
        }

        binding.etUnlogBtn.setOnClickListener {
            userController.signOut()
            findNavController().navigate(R.id.action_userInfoFragment_to_homeFragment)

        }

        binding.btnMap.setOnClickListener {
            findNavController().navigate(R.id.action_userInfoFragment_to_mapFragment)
        }

        if (userController.getCurrentUser() == null) {
            findNavController().popBackStack(R.id.homeFragment, false)
        }

        userController.getCurrentUserData { user ->
            binding.uiUsername.text = user.username
            binding.uiFirstName.text = user.firstName
            binding.uiLastName.text = user.lastName
            binding.uiPhoneNumber.text = user.phoneNumber
            binding.uiEmail.text = user.email
        }

        userController.getCurrentUserPhoto { photoUrl ->
            Glide.with(this)
                .load(photoUrl)
                .into(binding.ivUserProfilePicture)
        }

        playerController.getCurrentPlayerData { player ->
            binding.uiPoints.text = player.points.toString()
        }

    }

}
