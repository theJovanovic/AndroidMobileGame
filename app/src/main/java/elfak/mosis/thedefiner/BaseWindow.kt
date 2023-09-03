package elfak.mosis.thedefiner

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import elfak.mosis.thedefiner.Controllers.BaseController
import elfak.mosis.thedefiner.Controllers.PlayerController
import elfak.mosis.thedefiner.Controllers.UserController
import elfak.mosis.thedefiner.Models.Base
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.infowindow.InfoWindow
import kotlin.random.Random

class BaseWindow(
    private val context: Context,
    private val mapFragment: MapFragment,
    private val base: Base,
    private val mapView: MapView) :
    InfoWindow(R.layout.base_window, mapView) {

    private val userController: UserController = UserController()
    private val playerController: PlayerController = PlayerController()
    private val baseController: BaseController = BaseController()
    private var isDestroyed: Boolean = false

    private var winChance: Float = 0.5f

    @SuppressLint("SetTextI18n")
    override fun onOpen(item: Any?) {

        closeAllInfoWindowsOn(mapView)

        val oppUsername = mView.findViewById<TextView>(R.id.oppUsernameTv)
        val myImgView = mView.findViewById<ImageView>(R.id.myProfileImage)
        val oppImgView = mView.findViewById<ImageView>(R.id.opponentProfileImage)
        val myPointsTv = mView.findViewById<TextView>(R.id.myPoints)
        val oppPointsTv = mView.findViewById<TextView>(R.id.opponentPoints)
        val oppPointsLabel = mView.findViewById<TextView>(R.id.oppPointsLabel)
        val winChanceTv = mView.findViewById<TextView>(R.id.winningChanceTv)
        val btnFight = mView.findViewById<Button>(R.id.btnFight)
        val fightInfo = mView.findViewById<TextView>(R.id.fightInfo)

        userController.getCurrentUserPhoto { photoUrl ->
            Glide.with(context)
                .load(photoUrl)
                .into(myImgView)
        }

        userController.getUserPhoto(base.owner!!) { photoUrl ->
            Glide.with(context)
                .load(photoUrl)
                .into(oppImgView)
        }

        userController.getUserData(base.owner) { userData ->
            oppUsername.text =  "${userData.username.toString()} base"
            oppPointsLabel.text = "${userData.username.toString()} Points: "
        }

        playerController.getPoints { myPointsFetched ->
            myPointsTv.text = myPointsFetched.toString()

            playerController.getPoints(base.owner) { oppPointsFetched ->
                oppPointsTv.text = oppPointsFetched.toString()

                val totalPoints = myPointsFetched + oppPointsFetched
                winChance = if (totalPoints != 0) {
                    myPointsFetched.toFloat() / totalPoints.toFloat()
                } else {
                    0.5F
                }

                winChanceTv.text = "Winning chance: ${(winChance * 100F).toInt()}%"

                val distance = mapFragment.getCurrentLocation()?.distanceToAsDouble(base.location)
                val baseRadius = mapFragment.getBaseRadius()
                if (distance != null) {
                    if (distance <= baseRadius) {
                        btnFight.isEnabled = true
                        fightInfo.visibility = View.GONE
                    }
                    else {
                        btnFight.isEnabled = false
                        fightInfo.visibility = View.VISIBLE
                    }
                }

                btnFight.visibility = View.VISIBLE
            }
        }

        btnFight.setOnClickListener {
            this.rotateButton(btnFight)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun fightingLogic() {
        val myPointsTv = mView.findViewById<TextView>(R.id.myPoints)
        val oppPointsTv = mView.findViewById<TextView>(R.id.opponentPoints)
        val myPointsLabel = mView.findViewById<TextView>(R.id.myPointsLabel)
        val oppPointsLabel = mView.findViewById<TextView>(R.id.oppPointsLabel)
        val winChanceTv = mView.findViewById<TextView>(R.id.winningChanceTv)
        val btnFight = mView.findViewById<Button>(R.id.btnFight)
        val fightResultTv = mView.findViewById<TextView>(R.id.fightResult)

        fightResultTv.visibility = View.VISIBLE
        val isFightWinner = fight()
        if (isFightWinner) {
            fightResultTv.text = "YOU WON!"
            myPointsTv.visibility = View.GONE
            oppPointsTv.visibility = View.GONE
            myPointsLabel.visibility = View.GONE
            oppPointsLabel.visibility = View.GONE
            winChanceTv.visibility = View.GONE
            btnFight.visibility = View.GONE
            playerController.addPoints(base.owner!!, -2) { success, _ ->
                if (!success) {
                    Toast.makeText(context, "Error while decrementing opponent's points.", Toast.LENGTH_SHORT).show()
                }
            }
            isDestroyed = true
        }
        else {
            fightResultTv.text = "YOU LOST!"
            Toast.makeText(context, "-2 POINTS", Toast.LENGTH_SHORT).show()
            playerController.addPoints(-2) { success, _ ->
                if (success) {
                    mapFragment.updatePointsTv()

                    playerController.getPoints { myPointsFetched ->
                        myPointsTv.text = myPointsFetched.toString()

                        playerController.getPoints(base.owner!!) { oppPointsFetched ->
                            oppPointsTv.text = oppPointsFetched.toString()

                            val totalPoints = myPointsFetched + oppPointsFetched
                            winChance = if (totalPoints != 0) {
                                myPointsFetched.toFloat() / totalPoints.toFloat()
                            } else {
                                0.5F
                            }

                            winChanceTv.text = "Winning chance: ${(winChance * 100F).toInt()}%"
                        }
                    }
                }
            }
        }
    }

    private fun fight(): Boolean {
        val randomFloat = Random.nextFloat()
        return randomFloat <= winChance
    }

    private fun rotateButton(button: Button) {

        button.animate().apply {
            duration = 2000
            rotationYBy(2520f)
            button.isClickable = false
        }.withEndAction {
            button.isClickable = true
            this.fightingLogic()
        }
    }

    override fun onClose() {
        if (isDestroyed) {
            mapFragment.removeBase(this.base)
            baseController.removeBaseFromServer(this.base) { success ->
                if (!success) {
                    Toast.makeText(context, "Error while deleting the base.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}