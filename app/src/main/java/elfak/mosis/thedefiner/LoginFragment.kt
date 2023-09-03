package elfak.mosis.thedefiner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import elfak.mosis.thedefiner.Controllers.UserController
import elfak.mosis.thedefiner.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var userController: UserController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userController = UserController()

        binding.etLoginBtn.setOnClickListener {

            val email = binding.loginEmail.text.toString()
            val password = binding.loginPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {

                userController.loginUser(email, password,
                    onSuccess = { user ->
                        if (user != null && user.isEmailVerified) {
                            findNavController().navigate(R.id.action_loginFragment_to_userInfoFragment)
                        } else {
                            // Email is not verified
                            Toast.makeText(
                                requireContext(),
                                "Please verify your email address",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    },
                    onFailure = { errorMessage ->
                        // Login failed
                        Toast.makeText(
                            requireContext(),
                            errorMessage,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please enter email and password",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

}
