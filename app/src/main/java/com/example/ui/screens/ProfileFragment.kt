package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import coil.load
import coil.transform.CircleCropTransformation
import com.example.EditProfileActivity
import com.example.LankaFootprintsApp
import com.example.R
import com.example.data.repository.UserManager
import com.example.util.GeoDistanceEngine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

class ProfileFragment : Fragment() {

    private lateinit var userManager: UserManager

    private lateinit var imgAvatar: ImageView
    private lateinit var tvInitials: TextView
    private lateinit var tvName: TextView
    private lateinit var tvHomeBase: TextView
    private lateinit var tvHomeCoords: TextView
    private lateinit var btnEditProfile: Button
    private lateinit var tvTotalKm: TextView
    private lateinit var tvTotalTrips: TextView
    private lateinit var tvRoundTripKm: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        userManager = UserManager.getInstance(requireContext())

        imgAvatar = view.findViewById(R.id.img_profile_avatar)
        tvInitials = view.findViewById(R.id.tv_profile_initials)
        tvName = view.findViewById(R.id.tv_profile_name)
        tvHomeBase = view.findViewById(R.id.tv_profile_home_base)
        tvHomeCoords = view.findViewById(R.id.tv_profile_home_coords)
        btnEditProfile = view.findViewById(R.id.btn_edit_profile)
        tvTotalKm = view.findViewById(R.id.tv_profile_total_km)
        tvTotalTrips = view.findViewById(R.id.tv_profile_total_trips)
        tvRoundTripKm = view.findViewById(R.id.tv_profile_roundtrip_km)

        btnEditProfile.setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }

        observeProfileAndTrips()
        return view
    }

    override fun onResume() {
        super.onResume()
        updateProfileUI()
    }

    private fun observeProfileAndTrips() {
        viewLifecycleOwner.lifecycleScope.launch {
            userManager.userProfile.collectLatest {
                updateProfileUI()
            }
        }

        val app = requireActivity().application as LankaFootprintsApp
        viewLifecycleOwner.lifecycleScope.launch {
            app.repository.allTrips.collectLatest { trips ->
                val pastTrips = trips.filter { !it.isUpcoming }
                val profile = userManager.getUserProfile()

                val totalRouteKm = com.example.data.repository.TripRepository.calculateTotalRouteDistance(pastTrips)
                val roundTripKm = GeoDistanceEngine.calculateRoundTripDistanceKm(pastTrips, profile)

                tvTotalKm.text = "${String.format(Locale.US, "%,.0f", totalRouteKm)} km"
                tvTotalTrips.text = "${pastTrips.size}"
                tvRoundTripKm.text = "${String.format(Locale.US, "%,.0f", roundTripKm)} km Round-Trip"
            }
        }
    }

    private fun updateProfileUI() {
        val profile = userManager.getUserProfile()
        tvName.text = profile.userName
        tvHomeBase.text = profile.homeLocationName
        tvHomeCoords.text = String.format(Locale.US, "%.5f° N, %.5f° E", profile.homeLatitude, profile.homeLongitude)

        if (!profile.profileImageUri.isNullOrBlank()) {
            tvInitials.visibility = View.GONE
            imgAvatar.visibility = View.VISIBLE
            imgAvatar.load(Uri.parse(profile.profileImageUri)) {
                crossfade(true)
                transformations(CircleCropTransformation())
            }
        } else {
            val initials = profile.userName.split(" ")
                .filter { it.isNotBlank() }
                .take(2)
                .map { it.first().uppercaseChar() }
                .joinToString("")
                .ifBlank { "LF" }

            tvInitials.text = initials
            tvInitials.visibility = View.VISIBLE
        }
    }
}
