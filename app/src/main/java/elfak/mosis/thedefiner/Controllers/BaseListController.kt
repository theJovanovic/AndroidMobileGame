package elfak.mosis.thedefiner.Controllers

import com.google.firebase.firestore.FirebaseFirestore
import elfak.mosis.thedefiner.Models.BaseListItem

class BaseListController {

    private var firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun getBaselistInfo(callback: (ArrayList<BaseListItem>) -> Unit) {

        var baselistInfo = ArrayList<BaseListItem>()

        this.getUsersMaps { usersMaps ->

            val basesCollection = firestore.collection("bases")
            basesCollection.get()
                .addOnSuccessListener { querySnapshot ->

                    if (querySnapshot != null && !querySnapshot.isEmpty) {

                        for (document in querySnapshot) {
                            val ownerUID = document.getString("owner")

                            val photoURL = usersMaps[ownerUID]!!.photoURL
                            val username = usersMaps[ownerUID]!!.username
                            val points = usersMaps[ownerUID]!!.points

                            val location = document.getGeoPoint("location")
                            val latitude = location!!.latitude.toFloat()
                            val longitude = location.longitude.toFloat()

                            val newBaseListItem = BaseListItem(photoURL, username, points, latitude, longitude)
                            baselistInfo.add(newBaseListItem)
                        }

                    }

                    callback(baselistInfo)
                }
                .addOnFailureListener {
                    callback(baselistInfo)
                }
        }
    }

    private fun getUsersMaps(callback: (HashMap<String, BaseListItem>) -> Unit) {

        val usersHash = HashMap<String, BaseListItem>()

        val usersCollection = firestore.collection("users")
        usersCollection.get()
            .addOnSuccessListener { querySnapshot ->

                if (querySnapshot != null && !querySnapshot.isEmpty) {
                    for (document in querySnapshot) {
                        val userUID = document.id
                        val photoURL = document.getString("profilePictureURL")
                        val username = document.getString("username")
                        val points = document.getLong("points")!!.toInt()
                        val user = BaseListItem(photoURL, username, points, null, null)
                        usersHash[userUID] = user
                    }
                }
                callback(usersHash)
            }
            .addOnFailureListener {
                callback(usersHash)
            }
    }

}