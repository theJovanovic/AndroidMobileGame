package elfak.mosis.thedefiner.Controllers

import com.google.firebase.firestore.FirebaseFirestore
import elfak.mosis.thedefiner.Models.Base
import org.osmdroid.util.GeoPoint

class BaseController {

    private val userController: UserController = UserController()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private fun getRandomString(length: Int): String {
        val charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..length)
            .map { charset.random() }
            .joinToString("")
    }

    fun createBase(currentLocation: GeoPoint): Base {
        return Base(getRandomString(10), userController.getCurrentUser()!!.uid, currentLocation)
    }

    fun addBaseToServer(base: Base?, callback: (Boolean) -> Unit) {

        val user = userController.getCurrentUser()

        val location = com.google.firebase.firestore.GeoPoint(
            base!!.location!!.latitude,
            base.location!!.longitude
        )

        val baseData = hashMapOf(
            "id" to base.id,
            "owner" to user!!.uid,
            "location" to location
        )

        val baseDocument = firestore.collection("bases").document(base.id!!)
        baseDocument.set(baseData)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener {
                callback(false)
            }

    }

    fun isLocationValid(currentLocation: GeoPoint?, bases: ArrayList<Base>?, minDistance: Float): Boolean {
        for (base in bases!!) {
            if (currentLocation!!.distanceToAsDouble(base.location) <= minDistance) {
                return false
            }
        }
        return true
    }

    fun removeBaseFromServer(base: Base, callback: (Boolean) -> Unit) {
        val baseDocument = firestore.collection("bases").document(base.id!!)
        baseDocument.delete()
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    fun getBasesFromServer(callback: (ArrayList<Base>) -> Unit) {

        val bases = ArrayList<Base>()

        val basesCollection = firestore.collection("bases")
        basesCollection.get()
            .addOnSuccessListener { querySnapshot ->

                if (querySnapshot != null && !querySnapshot.isEmpty) {
                    for (document in querySnapshot) {
                        val id = document.getString("id")
                        val owner = document.getString("owner")
                        val geoPoint = document.getGeoPoint("location")
                        val latitude = geoPoint!!.latitude
                        val longitude = geoPoint.longitude
                        val location = GeoPoint(latitude, longitude)
                        val base = Base(id, owner, location)
                        bases.add(base)
                    }
                }
                callback(bases)
            }
            .addOnFailureListener {
                callback(bases)
            }
    }

    fun getBasesFromServer(userUID: String?, myLocation: GeoPoint?, inputedRadius: Int?, callback: (ArrayList<Base>) -> Unit) {
        val bases = ArrayList<Base>()

        val basesCollection = firestore.collection("bases")
        basesCollection.get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot != null && !querySnapshot.isEmpty) {
                    for (document in querySnapshot) {
                        val baseId = document.getString("id")
                        val baseOwner = document.getString("owner")
                        if (userUID != null && userUID != baseOwner) {
                            continue
                        }
                        val baseGeoPoint = document.getGeoPoint("location")
                        val baseLatitude = baseGeoPoint!!.latitude
                        val baseLongitude = baseGeoPoint.longitude
                        val baseLocation = GeoPoint(baseLatitude, baseLongitude)
                        if (inputedRadius != null && baseLocation.distanceToAsDouble(myLocation) > inputedRadius) {
                            continue
                        }
                        val base = Base(baseId, baseOwner, baseLocation)
                        bases.add(base)
                    }
                }
                callback(bases)
            }
            .addOnFailureListener {
                callback(bases)
            }
    }

    fun filterBases(bases: ArrayList<Base>, inputedMaxPoints: Int?, callback: (ArrayList<Base>) -> Unit) {
        if (inputedMaxPoints == null) {
            callback(bases)
            return
        }

        val filteredBases = ArrayList<Base>()

        val userDocuments = firestore.collection("users")
        userDocuments.get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot != null && !querySnapshot.isEmpty) {
                    for (document in querySnapshot) {
                        val userUID = document.id
                        val userPoints = document.getLong("points")!!.toInt()
                        if (userPoints > inputedMaxPoints) {
                            continue
                        }
                        val userBasesArray = ArrayList(bases.filter { it.owner == userUID })
                        filteredBases.addAll(userBasesArray)
                    }
                }
                callback(filteredBases)
            }
            .addOnFailureListener {
                callback(filteredBases)
            }
    }


}