package elfak.mosis.thedefiner.Controllers

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import java.io.ByteArrayOutputStream

class PhotoController(private val fragment: Fragment,
                      private val photoResultLauncher: ActivityResultLauncher<Intent>) {

    fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        photoResultLauncher.launch(intent)
    }

    fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        photoResultLauncher.launch(intent)
    }

    fun handleActivityResult(resultCode: Int, data: Intent?, onSuccess: (Uri) -> Unit) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            val imageUri = if (data.data != null) {
                // Photo from Gallery
                data.data
            } else {
                // Photo from Camera
                val imageBitmap = data.extras?.get("data") as? Bitmap
                if (imageBitmap != null) {
                    getImageUriFromBitmap(imageBitmap)
                } else {
                    null
                }
            }

            if (imageUri != null) {
                onSuccess(imageUri)
            } else {
                Toast.makeText(fragment.requireContext(), "Failed to get image.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getImageUriFromBitmap(bitmap: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(
            fragment.requireContext().contentResolver,
            bitmap,
            "Title",
            null
        )
        return Uri.parse(path)
    }

}
