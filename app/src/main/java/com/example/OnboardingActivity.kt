package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import com.lankafootprints.travelapp.auth.UserManager as AuthUserManager
import java.util.Locale

class OnboardingActivity : ComponentActivity() {

    private lateinit var userManager: UserManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var selectedImageUri: Uri? = null
    private var homeLatitude: Double = 6.9271
    private var homeLongitude: Double = 79.8612
    private var homeLocationName: String = "Colombo, Western Province"

    private lateinit var imgProfilePreview: ImageView
    private lateinit var tvAvatarInitials: TextView
    private lateinit var etUserName: EditText
    private lateinit var etManualLocation: EditText
    private lateinit var tvDetectedHomeName: TextView
    private lateinit var tvDetectedCoordinates: TextView
    private lateinit var btnGpsLocation: Button
    private lateinit var btnPickPhoto: Button
    private lateinit var btnSaveStart: Button
    private lateinit var btnGoogleSignIn: Button

    private lateinit var googleSignInClient: GoogleSignInClient

    private val photoPickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            tvAvatarInitials.visibility = View.GONE
            imgProfilePreview.visibility = View.VISIBLE
            imgProfilePreview.load(uri) {
                crossfade(true)
                transformations(CircleCropTransformation())
            }
        }
    }

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                // Auto register the user with our new AuthUserManager
                AuthUserManager.autoRegisterFromGoogle(this, account)
                
                // Update local Onboarding UI state
                etUserName.setText(account.displayName)
                if (account.photoUrl != null) {
                    selectedImageUri = account.photoUrl
                    tvAvatarInitials.visibility = View.GONE
                    imgProfilePreview.visibility = View.VISIBLE
                    imgProfilePreview.load(account.photoUrl) {
                        crossfade(true)
                        transformations(CircleCropTransformation())
                    }
                }
                Toast.makeText(this, "Signed in as ${account.email}", Toast.LENGTH_SHORT).show()
            }
        } catch (e: ApiException) {
            Toast.makeText(this, "Google Sign-In failed: ${e.statusCode}", Toast.LENGTH_SHORT).show()
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
        setContentView(R.layout.activity_onboarding)

        userManager = UserManager.getInstance(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        initViews()
        setupListeners()
        updateDetectedHomeUI()
    }

    private fun initViews() {
        imgProfilePreview = findViewById(R.id.img_profile_preview)
        tvAvatarInitials = findViewById(R.id.tv_avatar_initials)
        etUserName = findViewById(R.id.et_user_name)
        etManualLocation = findViewById(R.id.et_manual_location)
        tvDetectedHomeName = findViewById(R.id.tv_detected_home_name)
        tvDetectedCoordinates = findViewById(R.id.tv_detected_coordinates)
        btnGpsLocation = findViewById(R.id.btn_gps_location)
        btnPickPhoto = findViewById(R.id.btn_pick_photo)
        btnSaveStart = findViewById(R.id.btn_save_start)
        btnGoogleSignIn = findViewById(R.id.btn_google_signin)
    }

    private fun setupListeners() {
        btnGoogleSignIn.setOnClickListener {
            googleSignInLauncher.launch(googleSignInClient.signInIntent)
        }

        btnPickPhoto.setOnClickListener {
            photoPickerLauncher.launch("image/*")
        }

        btnGpsLocation.setOnClickListener {
            requestLocationAndFetch()
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
                    updateDetectedHomeUI()
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnSaveStart.setOnClickListener {
            val name = etUserName.text.toString().trim()
            if (name.isBlank()) {
                etUserName.error = "Please enter your name"
                return@setOnClickListener
            }

            userManager.updateProfile(
                name = name,
                imageUri = selectedImageUri?.toString(),
                homeLocationName = homeLocationName,
                homeLat = homeLatitude,
                homeLng = homeLongitude
            )

            Toast.makeText(this, "Welcome to LankaFootprints, $name!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun requestLocationAndFetch() {
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

    @Suppress("MissingPermission")
    private fun fetchDeviceLocation() {
        Toast.makeText(this, "Acquiring GPS fix...", Toast.LENGTH_SHORT).show()
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    homeLatitude = location.latitude
                    homeLongitude = location.longitude
                    resolveLocationName(homeLatitude, homeLongitude)
                    updateDetectedHomeUI()
                    Toast.makeText(this, "Home Base set to: $homeLocationName", Toast.LENGTH_SHORT).show()
                } else {
                    fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                        if (lastLoc != null) {
                            homeLatitude = lastLoc.latitude
                            homeLongitude = lastLoc.longitude
                            resolveLocationName(homeLatitude, homeLongitude)
                            updateDetectedHomeUI()
                        } else {
                            Toast.makeText(this, "Could not fetch GPS coordinates. Please paste a map link or coordinates.", Toast.LENGTH_LONG).show()
                        }
                    }
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
        } catch (_: Exception) {
            // Fallback to Province lookup
        }

        val province = SriLankaDestinations.findMatchingProvince(lat, lng)
        homeLocationName = "$province Province, Sri Lanka"
    }

    private fun updateDetectedHomeUI() {
        tvDetectedHomeName.text = homeLocationName
        tvDetectedCoordinates.text = String.format(Locale.US, "%.5f° N, %.5f° E", homeLatitude, homeLongitude)
    }
}
