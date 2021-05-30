package com.example.easemytrip

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.easemytrip.model.Location
import com.example.easemytrip.model.Trip
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var startTime: String
    private lateinit var endTime: String
    private var locations: ArrayList<Location> = arrayListOf()
    private var fusedLocationClient: FusedLocationProviderClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        start.setOnClickListener {
            startTime = getCurrentTime()

            Log.d("START TIME",startTime)

            end.visibility = VISIBLE
            progress.visibility = VISIBLE
            start.visibility = GONE

            Toast.makeText(applicationContext, R.string.message, Toast.LENGTH_LONG).show()

            if (!checkPermissions()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions()
                }
            }
            else {
                getLastLocation()
            }

        }

        end.setOnClickListener {
            endTime = getCurrentTime()

            Log.d("END TIME",endTime)

            end.visibility = GONE
            progress.visibility = GONE

            getLastLocation()

            loadTripDataFromJson()

            //this function is commented because currently loading data from a dummy json
            //loadTripData()
        }
    }

    private fun loadTripDataFromJson() {
        val trip =
            Gson().fromJson(getJsonDataFromAsset(applicationContext, "trip.json"), Trip::class.java)
        val list: ArrayList<LatLng> = arrayListOf()

        //only send start and end location to map
        val size = trip.locations.size
        val start = LatLng(trip.locations[0].latitude,trip.locations[0].longitude)
        list.add(start)
        val end = LatLng(trip.locations[size-1].latitude,trip.locations[size-1].longitude)
        list.add(end)

        val intent = Intent(this, MapsActivity::class.java)
        intent.putExtra("list",list)
        startActivity(intent)
    }

    //load trip data from json
    private fun getJsonDataFromAsset(context: Context, fileName: String): String? {
        val jsonString: String
        try {
            jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            return null
        }
        return jsonString
    }

    private fun loadTripData() {
        val trip =
            Trip(trip_id = Math.random().toString(),
            start_time = startTime,
            end_time = endTime,
            locations = locations)
        val list: ArrayList<LatLng> = arrayListOf()

        //only send start and end location to map
        val size = trip.locations.size
        val start = LatLng(trip.locations[0].latitude,trip.locations[0].longitude)
        list.add(start)
        val end = LatLng(trip.locations[size-1].latitude,trip.locations[size-1].longitude)
        list.add(end)

        val intent = Intent(this, MapsActivity::class.java)
        intent.putExtra("list",list)
        startActivity(intent)
    }

    private fun getCurrentTime() : String{
        var sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(System.currentTimeMillis())
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        var lastLocation : android.location.Location
        fusedLocationClient?.lastLocation!!.addOnCompleteListener(this) { task ->
            if (task.isSuccessful && task.result != null) {
                lastLocation = task.result!!
                locations.add(Location(latitude = lastLocation.latitude,longitude = lastLocation.longitude,timestamp = getCurrentTime()))
            }
            else {
                Log.w(TAG, "getLastLocation:exception", task.exception)
                showMessage("No location detected. Make sure location is enabled on the device.")
            }
        }
    }
    private fun showMessage(string: String) {
            Toast.makeText(this@MainActivity, string, Toast.LENGTH_LONG).show()
    }
    private fun showSnackbar(
            mainTextStringId: String, actionStringId: String,
            listener: View.OnClickListener
    ) {
        Toast.makeText(this@MainActivity, mainTextStringId, Toast.LENGTH_LONG).show()
    }
    private fun checkPermissions(): Boolean {
        val permissionState = ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
        )
        return permissionState == PackageManager.PERMISSION_GRANTED
    }
    private fun startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                REQUEST_PERMISSIONS_REQUEST_CODE
        )
    }
    private fun requestPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.")
            showSnackbar("Location permission is needed for core functionality", "Okay",
                    View.OnClickListener {
                        startLocationPermissionRequest()
                    })
        }
        else {
            Log.i(TAG, "Requesting permission")
            startLocationPermissionRequest()
        }
    }
    override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<String>,
            grantResults: IntArray
    ) {
        Log.i(TAG, "onRequestPermissionResult")
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            when {
                grantResults.isEmpty() -> {
                    // If user interaction was interrupted, the permission request is cancelled and you
                    // receive empty arrays.
                    Log.i(TAG, "User interaction was cancelled.")
                }
                grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                    // Permission granted.
                    getLastLocation()
                }
                else -> {
                    showSnackbar("Permission was denied", "Settings",
                            View.OnClickListener {
                                // Build intent that displays the App settings screen.
                                val intent = Intent()
                                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                val uri = Uri.fromParts(
                                        "package",
                                        Build.DISPLAY, null
                                )
                                intent.data = uri
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
                            }
                    )

                    //permission denied to ask user
                    showPermissionDialog()
                }
            }
        }
    }

    private fun showPermissionDialog() {
        AlertDialog.Builder(this@MainActivity)
                .setMessage(R.string.permission)
                .setPositiveButton("Allow", DialogInterface.OnClickListener { dialog, which ->
                   //grant permission
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show()
    }

    companion object {
        private val TAG = "LocationProvider"
        private val REQUEST_PERMISSIONS_REQUEST_CODE = 34
    }
}