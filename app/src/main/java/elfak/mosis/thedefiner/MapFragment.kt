package elfak.mosis.thedefiner

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.DisplayMetrics
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import elfak.mosis.thedefiner.Controllers.BaseController
import elfak.mosis.thedefiner.Controllers.PlayerController
import elfak.mosis.thedefiner.Controllers.UserController
import elfak.mosis.thedefiner.Models.Base
import elfak.mosis.thedefiner.Models.BaseLocationSingleton
import org.osmdroid.config.Configuration.*
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.*
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import elfak.mosis.thedefiner.databinding.FragmentMapBinding
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import org.osmdroid.views.overlay.infowindow.InfoWindow
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.cos
import kotlin.math.sin

class MapFragment : Fragment() {

    private lateinit var binding: FragmentMapBinding
    private lateinit var mapView: MapView
    private lateinit var scaleGestureDetector: ScaleGestureDetector
    private lateinit var locationOverlay: MyLocationNewOverlay

    private var polygons: ArrayList<Polygon>? = ArrayList()
    private var bases: ArrayList<Base>? = ArrayList()
    private var baseMarkers: ArrayList<Marker>? = ArrayList()
    private var baseWindows: ArrayList<BaseWindow>? = ArrayList()
    private val baseRadius: Float = 50F
    private var zoomAboveShown = true
    private var zoomBelowShown = false
    private var zoomLevelThreshold = 15

    private lateinit var playerController: PlayerController
    private lateinit var userController: UserController
    private lateinit var baseController: BaseController

    private fun addPolygonsToMap() {
        for (polygon in polygons!!) {
            mapView.overlays.add(polygon)
        }
    }
    private fun removePolygonsFromMap() {
        for (polygon in polygons!!) {
            mapView.overlays.remove(polygon)
        }
    }
    private fun addBaseMarkersToMap() {
        for (baseMarker in baseMarkers!!) {
            mapView.overlays.add(baseMarker)
        }
    }
    private fun removeBaseMarkersFromMap() {
        for (baseMarker in baseMarkers!!) {
            mapView.overlays.remove(baseMarker)
        }
    }

    fun getCurrentLocation(): GeoPoint? {
        return locationOverlay.myLocation
    }
    fun getBaseRadius(): Float {
        return baseRadius
    }
    fun removeBase(base: Base) {
        try {
            mapView.overlays.removeAll { overlay ->
                when (overlay) {
                    is Marker -> overlay.id == base.id
                    is Polygon -> overlay.id == base.id
                    else -> false
                }
            }
        } catch (_: Exception) {}
        this.bases!!.remove(base)
        this.baseMarkers!!.removeIf {it.id == base.id}
        this.polygons!!.removeIf {it.id == base.id}
        mapView.invalidate()
    }
    @SuppressLint("SetTextI18n")
    fun updatePointsTv() {
        playerController.getCurrentPlayerData { playerData ->
            binding.pointsTv.text = "My points: ${playerData.points.toString()}"
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapBinding.inflate(inflater, container, false)
        playerController = PlayerController()
        userController = UserController()
        baseController = BaseController()
        mapView = binding.mapView
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        getInstance().load(requireContext(), PreferenceManager.getDefaultSharedPreferences(requireContext()))
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setBuiltInZoomControls(false)
        mapView.setMultiTouchControls(true)
        mapView.setOnTouchListener { _, event ->
            getScaleGestureDetector().onTouchEvent(event)
            InfoWindow.closeAllInfoWindowsOn(mapView)
            false
        }

        val latitude = BaseLocationSingleton.latitude
        val longitude = BaseLocationSingleton.longitude
        if (latitude != -1f && longitude != -1f) {
            mapView.controller.setZoom(18.0)
            mapView.controller.setCenter(GeoPoint(latitude.toDouble(), longitude.toDouble()))
            BaseLocationSingleton.latitude = -1f
            BaseLocationSingleton.longitude = -1f
        }
        else {
            mapView.controller.setZoom(15.0)
            mapView.controller.setCenter(GeoPoint(43.321202031706285, 21.89567028692961))
        }

        this.addMapScaleOverlay()
        this.addCompassOverlay()

        baseController.getBasesFromServer { bases ->
            this.addBasesToMap(bases)
            this.enableLocationTracking()
            mapView.invalidate()
        }

        this.updatePointsTv()

        binding.btnList.setOnClickListener {
            findNavController().navigate(R.id.action_mapFragment_to_baseListFragment)
        }

        binding.btnReset.setOnClickListener {
            binding.cardViewSearch.visibility = View.GONE
            binding.radiusTv.text = null
            binding.usernameTv.text = null
            binding.maxPointsTv.text = null
            baseController.getBasesFromServer { bases ->
                this.basesFetchedLogic(bases)
            }
        }

        binding.btnFilter.setOnClickListener {

            binding.cardViewSearch.visibility = View.GONE

            val inputedUsername = binding.usernameTv.text.toString()

            val inputedRadiusText = binding.radiusTv.text.toString()
            val inputedRadius = inputedRadiusText.toIntOrNull()

            val inputedMaxPointsText = binding.maxPointsTv.text.toString()
            val inputedMaxPoints = inputedMaxPointsText.toIntOrNull()

            if (inputedRadius != null && locationOverlay.myLocation == null) {
                return@setOnClickListener
            }

            userController.getUserUID(inputedUsername) { userUID ->
                baseController.getBasesFromServer(userUID, locationOverlay.myLocation, inputedRadius) { bases1 ->
                    baseController.filterBases(bases1, inputedMaxPoints) { bases2 ->
                        this.basesFetchedLogic(bases2)
                    }
                }
            }
        }

        binding.floatingButtonSearch.setOnClickListener {
            binding.cardViewAdd.visibility = View.GONE
            if (binding.cardViewSearch.visibility == View.VISIBLE) {
                binding.cardViewSearch.visibility = View.GONE
            }
            else if (binding.cardViewSearch.visibility == View.GONE) {
                binding.cardViewSearch.visibility = View.VISIBLE
            }
        }

        binding.floatingButtonRecenter.setOnClickListener {

            if (locationOverlay.myLocation == null) {
                return@setOnClickListener
            }

            val lastZoomLevel = mapView.zoomLevelDouble
            mapView.controller.setZoom(18.0)
            mapView.controller.animateTo(locationOverlay.myLocation)
            if (lastZoomLevel < zoomLevelThreshold) {
                zoomAboveShown = true
                zoomBelowShown = false
                this.addBaseMarkersToMap()
                this.setMyLocationOverlay()
                mapView.invalidate()
            }
        }

        binding.polygonSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                this.addPolygonsToMap()
                if (mapView.zoomLevelDouble >= zoomLevelThreshold) {
                    this.removeBaseMarkersFromMap()
                    this.addBaseMarkersToMap()
                }
                mapView.overlays.remove(locationOverlay)
                this.setMyLocationOverlay()
                mapView.invalidate()
            } else {
                this.removePolygonsFromMap()
                mapView.invalidate()
            }
        }

        binding.floatingActionButton.setOnClickListener {
            binding.cardViewSearch.visibility = View.GONE
            if (binding.cardViewAdd.visibility == View.VISIBLE) {
                binding.cardViewAdd.visibility = View.GONE
            }
            else if (binding.cardViewAdd.visibility == View.GONE) {
                binding.cardViewAdd.visibility = View.VISIBLE
            }
        }

        binding.btnAddBase.setOnClickListener {

            if (locationOverlay.myLocation == null) {
                return@setOnClickListener
            }

            // provera da li je u radijusu druge baze
            if (!baseController.isLocationValid(locationOverlay.myLocation, bases, baseRadius)) {
                Toast.makeText(requireContext(), "Can't add new base too close to the existing one.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.polygonSwitch.isChecked = false

            // kreiranje Base objekta
            val base: Base = baseController.createBase(locationOverlay.myLocation)!!
            this.bases!!.add(base)

            // dadavanje poligona
            val polygon = this.getPolygon(base)
            polygons!!.add(polygon)

            // dodavanje baze na mapu
            val baseMarker: Marker = this.getBaseMarker(locationOverlay.myLocation, 0, base)
            baseMarkers!!.add(baseMarker)
            if (mapView.zoomLevelDouble >= zoomLevelThreshold) {
                mapView.overlays.add(baseMarker)
                mapView.invalidate()
            }

            // dodavanje baze na server
            baseController.addBaseToServer(base) { success ->
                if (!success) {
                    Toast.makeText(requireContext(), "Error while adding a base", Toast.LENGTH_SHORT).show()
                }
            }
            binding.cardViewAdd.visibility = View.GONE

            // dodavanje poena
            playerController.addPoints(3) { success, newPoints ->
                if (success) {
                    binding.pointsTv.text = "My points: $newPoints"
                    Toast.makeText(requireContext(), "+3 POINTS", Toast.LENGTH_SHORT).show()
                }
                else {
                    Toast.makeText(requireContext(), "Error while updating points", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    private fun addBasesToMap(fetchedBases: ArrayList<Base>) {
        val userUID = userController.getCurrentUser()!!.uid
        for (base in fetchedBases) {
            this.bases!!.add(base)

            val baseMarker: Marker = if (userUID == base.owner) {
                this.getBaseMarker(base.location!!, 0, base)
            } else {
                this.getBaseMarker(base.location!!, 1, base)
            }
            this.baseMarkers!!.add(baseMarker)
            mapView.overlays.add(baseMarker)

            val polygon = this.getPolygon(base)
            this.polygons!!.add(polygon)

            mapView.invalidate()
        }
    }

    private fun getBaseMarker(baseLocation: GeoPoint, userType: Int, base: Base): Marker {
        // userType: 0 - current user, 1 - other users
        val baseMarker = Marker(mapView)
        baseMarker.id = base.id
        baseMarker.position = baseLocation
        if (userType == 0) {
            baseMarker.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_base_green)
            baseMarker.title = "MY BASE"
        }
        else {
            baseMarker.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_base)
            val baseWindow = BaseWindow(requireContext(), this, base, mapView)
            baseWindows!!.add(baseWindow)
            baseMarker.infoWindow = baseWindow
        }
        baseMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        return baseMarker
    }

    private fun getPolygon(base: Base): Polygon {
        val geoPoints = ArrayList<GeoPoint>()
        val polygon = Polygon()
        val numVertices = 16
        val angleIncrement = 2 * Math.PI / numVertices
        for (i in 0 until numVertices) {
            val angle = angleIncrement * i
            val lat = base.location!!.latitude + (baseRadius / 111000.0) * cos(angle)
            val lon = base.location.longitude + (baseRadius / (111000.0 * cos(Math.toRadians(base.location.latitude)))) * sin(angle)
            geoPoints.add(GeoPoint(lat, lon))
        }
        polygon.id = base.id
        polygon.points = geoPoints
        polygon.strokeWidth = 0F
        playerController.getPlayerData(base.owner!!) { player ->
            val colorInt = Color.parseColor(player.playerColor)
            polygon.fillPaint.color = Color.argb(96, Color.red(colorInt),
                Color.green(colorInt),
                Color.blue(colorInt))
        }
        return polygon
    }

    private fun enableLocationTracking() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            this.setMyLocationOverlay()
            this.showComponentsOnLoad()
        }
    }

    private fun setMyLocationOverlay() {
        locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(requireContext()), mapView)
        locationOverlay.enableMyLocation()
        locationOverlay.isDrawAccuracyEnabled = false
        mapView.overlays.add(locationOverlay)
    }

    private fun addMapScaleOverlay() {
        val dm : DisplayMetrics = requireContext().resources.displayMetrics
        val scaleBarOverlay = ScaleBarOverlay(mapView)
        scaleBarOverlay.setCentred(true)
        scaleBarOverlay.setScaleBarOffset(dm.widthPixels / 3 + 40, 10)
        mapView.overlays.add(scaleBarOverlay)
    }

    private fun addCompassOverlay() {
        val compassOverlay = CompassOverlay(requireContext(), InternalCompassOrientationProvider(requireContext()), mapView)
        compassOverlay.enableCompass()
        mapView.overlays.add(compassOverlay)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                this.setMyLocationOverlay()
                this.showComponentsOnLoad()
            }
        }

    private fun showComponentsOnLoad() {
        binding.floatingActionButton.visibility = View.VISIBLE
        binding.floatingButtonRecenter.visibility = View.VISIBLE
        binding.floatingButtonSearch.visibility = View.VISIBLE
        binding.linearLayoutList.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
    }

    private fun getScaleGestureDetector(): ScaleGestureDetector {
        scaleGestureDetector = ScaleGestureDetector(requireContext(), object : ScaleGestureDetector.OnScaleGestureListener {
            override fun onScale(detector: ScaleGestureDetector): Boolean {

                val currentZoomLevel = mapView.zoomLevelDouble

                if (currentZoomLevel < zoomLevelThreshold) {
                    if (!zoomBelowShown) {
                        zoomBelowShown = true
                        zoomAboveShown = false
                        removeBaseMarkersFromMap()
                        mapView.overlays.remove(locationOverlay)
                        mapView.invalidate()
                    }
                } else if (currentZoomLevel >= zoomLevelThreshold) {
                    if (!zoomAboveShown) {
                        zoomAboveShown = true
                        zoomBelowShown = false
                        addBaseMarkersToMap()
                        setMyLocationOverlay()
                        mapView.invalidate()
                    }
                }

                return true
            }

            override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                return true
            }

            override fun onScaleEnd(detector: ScaleGestureDetector) {
            }
        })

        return scaleGestureDetector
    }

    private fun basesFetchedLogic(bases: ArrayList<Base>) {
        InfoWindow.closeAllInfoWindowsOn(mapView)
        val lastZoomLevel = mapView.zoomLevelDouble
        mapView.controller.setZoom(15.0)
        mapView.controller.animateTo(locationOverlay.myLocation)
        if (lastZoomLevel < zoomLevelThreshold) {
            zoomAboveShown = true
            zoomBelowShown = false
            this.addBaseMarkersToMap()
        }
        this.removeBaseMarkersFromMap()
        this.removePolygonsFromMap()
        this.baseMarkers = ArrayList()
        this.polygons = ArrayList()
        this.bases = ArrayList()
        mapView.overlays.remove(locationOverlay)
        this.addBasesToMap(bases)
        this.setMyLocationOverlay()
        mapView.invalidate()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

}


