package elfak.mosis.thedefiner

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import elfak.mosis.thedefiner.Controllers.PhotoController
import elfak.mosis.thedefiner.Controllers.UserController
import elfak.mosis.thedefiner.Models.User
import elfak.mosis.thedefiner.databinding.FragmentRegistrationBinding

class RegistrationFragment : Fragment() {

    private lateinit var binding: FragmentRegistrationBinding
    private lateinit var userController: UserController
    private lateinit var photoController: PhotoController
    private var imageURI: Uri? = null

    private val photoResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        photoController.handleActivityResult(result.resultCode, result.data) { uri ->
            imageURI = uri
            binding.ivUserProfile.setImageURI(imageURI)
            binding.ivUserProfile.visibility = View.VISIBLE
            binding.btnRegister.visibility = View.VISIBLE
        }
    }

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            photoController.openCamera()
        } else {
            Toast.makeText(requireContext(), "Camera permission denied.", Toast.LENGTH_SHORT).show()
        }
    }

    private val galleryPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            photoController.openGallery()
        } else {
            Toast.makeText(requireContext(), "Gallery permission denied.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userController = UserController()
        photoController = PhotoController(this, photoResultLauncher)

        binding.btnSelectPhoto.setOnClickListener {
            openPhotoOptions()
        }

        binding.btnRegister.setOnClickListener {

            val username = binding.regUsername.text.toString()
            val firstName = binding.regFirstName.text.toString()
            val lastName = binding.regLastName.text.toString()
            val phoneNumber = binding.regPhoneNumber.text.toString()
            val email = binding.regEmail.text.toString()
            val password = binding.regPassword.text.toString()

            userController.usernameExists(username) { exists ->
                if (exists) {
                    Toast.makeText(requireContext(), "Username already exists.", Toast.LENGTH_SHORT).show()
                }
                else {

                    val newUser = User(username, firstName, lastName, phoneNumber, email, password)

                    if (userController.isValid(newUser) && imageURI != null) {

                        binding.btnRegister.visibility = View.GONE
                        binding.progressBar.visibility = View.VISIBLE

                        userController.registerUser(newUser, imageURI!!) { success, message ->
                            if (success) {
                                userController.sendEmailVerification()
                                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                                findNavController().navigate(R.id.action_registrationFragment_to_loginFragment)
                            } else {
                                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                                binding.btnRegister.visibility = View.VISIBLE
                                binding.progressBar.visibility = View.GONE
                            }
                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Please fill in all fields and select a photo.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun openPhotoOptions() {
        val options = arrayOf("Take a photo", "Select a photo")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Choose an option")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> {
                    if (hasCameraPermission()) {
                        photoController.openCamera()
                    } else {
                        requestCameraPermission()
                    }
                }
                1 -> {
                    photoController.openGallery()
//                    if (hasGalleryPermission()) {
//                        photoController.openGallery()
//                    } else {
//                        requestGalleryPermission()
//                    }
                }
            }
        }
        builder.show()
    }

    private fun hasCameraPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun hasGalleryPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestGalleryPermission() {
        galleryPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
}

