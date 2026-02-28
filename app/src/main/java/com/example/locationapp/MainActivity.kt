package com.example.locationapp

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.URL
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var tvAddress: TextView
    private lateinit var tvLatLng: TextView
    private lateinit var tvAltitude: TextView
    private lateinit var btnRefresh: Button
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvAddress = findViewById(R.id.tvAddress)
        tvLatLng = findViewById(R.id.tvLatLng)
        tvAltitude = findViewById(R.id.tvAltitude)
        btnRefresh = findViewById(R.id.btnRefresh)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        btnRefresh.setOnClickListener {
            checkPermissionAndGetLocation()
        }

        checkPermissionAndGetLocation()
    }

    private fun checkPermissionAndGetLocation() {
        val fineLocation = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        )
        val coarseLocation = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (fineLocation == PackageManager.PERMISSION_GRANTED ||
            coarseLocation == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun getCurrentLocation() {
        tvAddress.text = "位置情報を取得中..."
        tvLatLng.text = ""
        tvAltitude.text = "標高を取得中..."

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            return
        }

        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            null
        ).addOnSuccessListener { location: Location? ->
            if (location != null) {
                val lat = location.latitude
                val lng = location.longitude
                tvLatLng.text = "緯度: %.6f　経度: %.6f".format(lat, lng)
                getAddressFromLocation(lat, lng)
                getAltitudeFromGSI(lat, lng)
            } else {
                tvAddress.text = "位置情報を取得できませんでした\nGPSを有効にしてください"
                tvAltitude.text = ""
            }
        }.addOnFailureListener {
            tvAddress.text = "エラーが発生しました: ${it.message}"
            tvAltitude.text = ""
        }
    }

    private fun getAltitudeFromGSI(lat: Double, lng: Double) {
        scope.launch {
            try {
                val altitude = withContext(Dispatchers.IO) {
                    val url = "https://cyberjapandata2.gsi.go.jp/general/dem/scripts/getelevation.php?lon=$lng&lat=$lat&outtype=JSON"
                    val response = URL(url).readText()
                    val json = JSONObject(response)
                    json.getDouble("elevation")
                }
                tvAltitude.text = "標高: %.1f m（国土地理院）".format(altitude)
            } catch (e: Exception) {
                tvAltitude.text = "標高を取得できませんでした"
            }
        }
    }

    private fun getAddressFromLocation(lat: Double, lng: Double) {
        try {
            val geocoder = Geocoder(this, Locale.JAPANESE)
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val parts = listOf(
                    address.adminArea,
                    address.locality,
                    address.subLocality,
                    address.thoroughfare,
                    address.subThoroughfare
                ).filterNotNull()

                val addressText = if (parts.isNotEmpty()) {
                    parts.joinToString("")
                } else {
                    address.getAddressLine(0) ?: "住所を取得できませんでした"
                }
                tvAddress.text = addressText
            } else {
                tvAddress.text = "住所が見つかりませんでした"
            }
        } catch (e: Exception) {
            tvAddress.text = "住所の取得に失敗しました"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                Toast.makeText(this, "位置情報の許可が必要です", Toast.LENGTH_LONG).show()
                tvAddress.text = "位置情報の許可が必要です"
            }
        }
    }
}
