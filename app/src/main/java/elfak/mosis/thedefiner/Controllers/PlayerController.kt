package elfak.mosis.thedefiner.Controllers

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import elfak.mosis.thedefiner.Models.Player
import kotlin.math.max

class PlayerController {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun getCurrentPlayerData(callback: (Player) -> Unit) {
        // Retrieve player data from Firestore
        var userColor: String? = null
        var points: Long? = null

        val currUser = auth.currentUser
        if (currUser == null) {
            callback(Player(null, null))
            return
        }

        val userDocument = firestore.collection("users").document(currUser!!.uid)
        userDocument.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    userColor = document.getString("userColor")
                    points = document.getLong("points")
                }
                callback(Player(userColor, points!!.toInt()))
            }
            .addOnFailureListener {
                // Failed to retrieve user data from Firestore
                callback(Player(null, null))
            }
    }

    fun getPlayerData(uid: String, callback: (Player) -> Unit) {
        // Retrieve player data from Firestore
        var userColor: String? = null
        var points: Long? = null

        val userDocument = firestore.collection("users").document(uid)
        userDocument.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    userColor = document.getString("userColor")
                    points = document.getLong("points")
                }
                callback(Player(userColor, points!!.toInt()))
            }
            .addOnFailureListener {
                // Failed to retrieve user data from Firestore
                callback(Player(null, null))
            }
    }

    fun addPoints(i: Int, callback: (Boolean, Int) -> Unit) {
        val user = auth.currentUser
        val userDocument = firestore.collection("users").document(user!!.uid)
        var newPointsValue = 0

        firestore.runTransaction { transaction ->
            val documentSnapshot = transaction.get(userDocument)
            val currentPoints = documentSnapshot.getLong("points")!!.toInt()
            newPointsValue = max(currentPoints + i, 0)

            transaction.update(userDocument, "points", newPointsValue)
        }
            .addOnSuccessListener {
                callback(true, newPointsValue)
            }
            .addOnFailureListener {
                callback(false, newPointsValue)
            }
    }

    fun addPoints(userUID: String, i: Int, callback: (Boolean, Int) -> Unit) {
        val userDocument = firestore.collection("users").document(userUID)
        var newPointsValue = 0

        firestore.runTransaction { transaction ->
            val documentSnapshot = transaction.get(userDocument)
            val currentPoints = documentSnapshot.getLong("points")!!.toInt()
            newPointsValue = max(currentPoints + i, 0)

            transaction.update(userDocument, "points", newPointsValue)
        }
            .addOnSuccessListener {
                callback(true, newPointsValue)
            }
            .addOnFailureListener {
                callback(false, newPointsValue)
            }
    }

    fun getPoints(callback: (Int) -> Unit) {

        val user = auth.currentUser
        var points: Int? = null

        val userDocument = firestore.collection("users").document(user!!.uid)
        userDocument.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    points = document.getLong("points")!!.toInt()
                }
                callback(points!!)
            }
            .addOnFailureListener {
                callback(0)
            }
    }

    fun getPoints(userUID: String, callback: (Int) -> Unit) {

        var points: Int? = null

        val userDocument = firestore.collection("users").document(userUID)
        userDocument.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    points = document.getLong("points")!!.toInt()
                }
                callback(points!!)
            }
            .addOnFailureListener {
                callback(0)
            }
    }

}