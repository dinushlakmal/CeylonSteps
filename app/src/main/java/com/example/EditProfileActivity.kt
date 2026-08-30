package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import coil.load
import coil.transform.CircleCropTransformation
import com.example.data.model.SriLankaDestinations
import com.example.data.repository.UserManager
import com.example.util.GeoDistanceEngine
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.util.Locale

class EditProfileActivity : ComponentActivity() {

    private lateinit var userManager: UserManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var selectedImageUri: Uri? = null
    private var homeLatitude: Double = 6.9271
    private var homeLongitude: Double = 79.8612
    private var homeLocationName: String = "Colombo, Western Province"

    private lateinit var imgAvatar: ImageView
    private lateinit var btnPickPhoto: Button
    private lateinit var etName: EditText
    private lateinit var btnGps: Button
    private lateinit var etManualLocation: EditText
    private lateinit var tvHomeName: TextView
    private lateinit var tvHomeCoords: TextView
    private lateinit var btnSave: Button

    private val photoPickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            imgAvatar.load(uri) {
                crossfade(true)
                transformations(CircleCropTransformation())
            }
        }
    }

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions: Map<String, Boolean> ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            fetchDeviceLocation()
        } else {
            Toast.makeText(this, "Location permission required for GPS auto-detection", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        userManager = UserManager.getInstance(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        initViews()
        loadCurrentProfile()
        setupListeners()
    }

    private fun initViews() {
        imgAvatar = findViewById(R.id.img_edit_profile_avatar)
        btnPickPhoto = findViewById(R.id.btn_edit_pick_photo)
        etName = findViewById(R.id.et_edit_name)
        btnGps = findViewById(R.id.btn_edit_gps_location)
        etManualLocation = findViewById(R.id.et_edit_manual_location)
        tvHomeName = findViewById(R.id.tv_edit_home_name)
        tvHomeCoords = findViewById(R.id.tv_edit_home_coords)
        btnSave = findViewById(R.id.btn_edit_save)
    }

    private fun loadCurrentProfile() {
        val profile = userManager.getUserProfile()
        etName.setText(profile.userName)
        homeLatitude = profile.homeLatitude
        homeLongitude = profile.homeLongitude
        homeLocationName = profile.homeLocationName

        if (profile.profileImageUri != null) {
            selectedImageUri = Uri.parse(profile.profileImageUri)
            imgAvatar.load(selectedImageUri) {
                crossfade(true)
                transformations(CircleCropTransformation())
            }
        }
        updateHomeUI()
    }

    private fun setupListeners() {
        btnPickPhoto.setOnClickListener {
            photoPickerLauncher.launch("image/*")
        }

        btnGps.setOnClickListener {
            val fineGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            val coarseGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            if (fineGranted || coarseGranted) {
                fetchDeviceLocation()
            } else {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }

        etManualLocation.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val input = s?.toString() ?: ""
                val extracted = GeoDistanceEngine.extractCoordinatesFromText(input)
                if (extracted != null) {
                    homeLatitude = extracted.first
                    homeLongitude = extracted.second
                    resolveLocationName(homeLatitude, homeLongitude)
                    updateHomeUI()
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            if (name.isBlank()) {
                etName.error = "Name cannot be empty"
                return@setOnClickListener
            }

            userManager.updateProfile(
                name = name,
                imageUri = selectedImageUri?.toString(),
                homeLocationName = homeLocationName,
                homeLat = homeLatitude,
                homeLng = homeLongitude
            )

            Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    @Suppress("MissingPermission")
    private fun fetchDeviceLocation() {
        Toast.makeText(this, "Updating GPS fix...", Toast.LENGTH_SHORT).show()
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    homeLatitude = location.latitude
                    homeLongitude = location.longitude
                    resolveLocationName(homeLatitude, homeLongitude)
                    updateHomeUI()
                    Toast.makeText(this, "Home Base updated: $homeLocationName", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "GPS error: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun resolveLocationName(lat: Double, lng: Double) {
        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                val city = addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: "Sri Lanka"
                val province = SriLankaDestinations.findMatchingProvince(lat, lng)
                homeLocationName = "$city, $province Province"
                return
            }
        } catch (_: Exception) {}

        val province = SriLankaDestinations.findMatchingProvince(lat, lng)
        homeLocationName = "$province Province, Sri Lanka"
    }

    private fun updateHomeUI() {
        tvHomeName.text = homeLocationName
        tvHomeCoords.text = String.format(Locale.US, "%.5f° N, %.5f° E", homeLatitude, homeLongitude)
    }
}
