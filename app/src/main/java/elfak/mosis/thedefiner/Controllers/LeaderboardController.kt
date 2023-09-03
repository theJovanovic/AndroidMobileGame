package elfak.mosis.thedefiner.Controllers

import com.google.firebase.firestore.FirebaseFirestore
import elfak.mosis.thedefiner.Models.LeaderboardItem

class LeaderboardController {

    private var firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun getLeaderboardInfo(callback: (ArrayList<LeaderboardItem>) -> Unit) {
        val usersCollection = firestore.collection("users")
        usersCollection.get()
            .addOnSuccessListener { querySnapshot ->

                if (querySnapshot != null && !querySnapshot.isEmpty) {

                    val leaderboardInfo = ArrayList<LeaderboardItem>()

                    for (document in querySnapshot) {
                        val photoURL = document.getString("profilePictureURL")
                        val username = document.getString("username")
                        val points = document.getLong("points")!!.toInt()
                        val newLeaderboardInfo = LeaderboardItem(photoURL, username, points)
                        leaderboardInfo.add(newLeaderboardInfo)
                    }

                    callback(leaderboardInfo)

                }
                else {
                    callback(ArrayList())
                }
            }
            .addOnFailureListener {
                callback(ArrayList())
            }
    }
}