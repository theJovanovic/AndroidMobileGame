package elfak.mosis.thedefiner.Controllers

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import elfak.mosis.thedefiner.Models.User
import java.util.*
import kotlin.random.Random

class UserController() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun loginUser(
        email: String,
        password: String,
        onSuccess: (user: FirebaseUser?) -> Unit,
        onFailure: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    onSuccess(user)
                } else {
                    onFailure("Authentication failed")
                }
            }
    }

    fun registerUser(user: User, imageURI: Uri, callback: (Boolean, String?) -> Unit) {
        // Create a new user with Firebase Authentication
        auth.createUserWithEmailAndPassword(user.email!!, user.password!!)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val currentUser = auth.currentUser
                    if (currentUser != null) {
                        uploadProfilePicture(currentUser.uid, imageURI) { imageUrl ->
                            if (imageUrl != null) {
                                val userData = hashMapOf(
                                    "username" to user.username,
                                    "email" to user.email,
                                    "firstName" to user.firstName,
                                    "lastName" to user.lastName,
                                    "phoneNumber" to user.phoneNumber,
                                    "profilePictureURL" to imageUrl,
                                    "userColor" to getRandomHexColor(),
                                    "points" to 0,
                                )

                                val userDocument = firestore.collection("users").document(currentUser.uid)

                                userDocument.set(userData)
                                    .addOnSuccessListener {
                                        callback(true, "Verification mail sent")
                                    }
                                    .addOnFailureListener { exception ->
                                        callback(false, "Failed to store user data: ${exception.message}")
                                    }
                            } else {
                                callback(false, "Failed to upload profile picture")
                            }
                        }
                    } else {
                        callback(false, "User not found")
                    }
                } else {
                    callback(false, task.exception?.message ?: "Registration failed")
                }
            }
    }

    fun isValid(user: User): Boolean {
        return (user.username!!.isNotEmpty() && user.firstName!!.isNotEmpty() && user.lastName!!.isNotEmpty()
                && user.phoneNumber!!.isNotEmpty() && user.email!!.isNotEmpty() && user.password!!.isNotEmpty())
    }

    fun sendEmailVerification() {
        auth.currentUser?.sendEmailVerification()
    }

    private fun uploadProfilePicture(userId: String, imageURI: Uri, callback: (String?) -> Unit) {
        val imageRef = storage.reference.child("user_profile_images/$userId-${UUID.randomUUID()}")
        val uploadTask = imageRef.putFile(imageURI)

        uploadTask.addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                callback(uri.toString())
            }.addOnFailureListener {
                callback(null)
            }
        }.addOnFailureListener {
            callback(null)
        }
    }

    fun signOut() {
        auth.signOut()
    }

    fun getUserPhoto(userUID: String, callback: (String?) -> Unit) {
        var photoUrl: String? = null
        val user = auth.currentUser
        if (user == null) {
            callback(photoUrl)
            return
        }
        val userDocument = firestore.collection("users").document(userUID)
        userDocument.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    photoUrl = document.getString("profilePictureURL")
                }
                callback(photoUrl)
            }
            .addOnFailureListener {
                callback(photoUrl)
            }
    }

    fun getCurrentUserPhoto(callback: (String?) -> Unit) {
        var photoUrl: String? = null
        val user = auth.currentUser
        if (user == null) {
            callback(photoUrl)
            return
        }
        val userDocument = firestore.collection("users").document(user!!.uid)
        userDocument.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    photoUrl = document.getString("profilePictureURL")
                }
                callback(photoUrl)
            }
            .addOnFailureListener {
                callback(photoUrl)
            }
    }

    fun getCurrentUserData(callback: (User) -> Unit) {
        var username: String? = null
        var firstName: String? = null
        var lastName: String? = null
        var phoneNumber: String? = null
        var email: String? = null

        val user = auth.currentUser
        if (user == null) {
            callback(User(username, firstName, lastName, phoneNumber, email, null))
            return
        }

        val userDocument = firestore.collection("users").document(user!!.uid)
        userDocument.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    username = document.getString("username")
                    firstName = document.getString("firstName")
                    lastName = document.getString("lastName")
                    phoneNumber = document.getString("phoneNumber")
                    email = document.getString("email")
                }
                callback(User(username, firstName, lastName, phoneNumber, email, null))
            }
            .addOnFailureListener {
                callback(User(username, firstName, lastName, phoneNumber, email, null))
            }
    }

    fun getUserData(userUID: String, callback: (User) -> Unit) {
        var username: String? = null
        var firstName: String? = null
        var lastName: String? = null
        var phoneNumber: String? = null
        var email: String? = null

        val userDocument = firestore.collection("users").document(userUID)
        userDocument.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    username = document.getString("username")
                    firstName = document.getString("firstName")
                    lastName = document.getString("lastName")
                    phoneNumber = document.getString("phoneNumber")
                    email = document.getString("email")
                }
                callback(User(username, firstName, lastName, phoneNumber, email, null))
            }
            .addOnFailureListener {
                callback(User(username, firstName, lastName, phoneNumber, email, null))
            }
    }

    private fun getRandomHexColor(): String {
        val randomColor = Random.nextInt(0x1000000)
        return String.format("#%06X", randomColor)
    }

    fun usernameExists(username: String, callback: (Boolean) -> Unit) {
        val usersCollection = firestore.collection("users")
        usersCollection.get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot != null && !querySnapshot.isEmpty) {
                    for (document in querySnapshot) {
                        val docUsername = document.getString("username")
                        if (docUsername == username) {
                            callback(true)
                            return@addOnSuccessListener
                        }
                    }
                    callback(false)
                }
                else {
                    callback(false)
                }
            }
            .addOnFailureListener {
                callback(true)
            }
    }

    fun getUserUID(username: String, callback: (String?) -> Unit) {
        var userUID: String? = null

        if (username != "") {
            val usersCollection = firestore.collection("users")
            usersCollection.get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot != null && !querySnapshot.isEmpty) {
                        for (document in querySnapshot) {
                            val docUsername = document.getString("username")
                            if (docUsername == username) {
                                userUID = document.id
                                break
                            }
                        }
                        callback(userUID)
                    }
                }
        }
        else {
            callback(null)
        }
    }

}
