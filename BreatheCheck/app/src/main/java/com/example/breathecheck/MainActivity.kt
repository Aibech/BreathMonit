package com.example.breathecheck

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private var bluetoothGatt: BluetoothGatt? = null
    private var bluetoothAdapter: BluetoothAdapter? = null

    private lateinit var btnScan: Button
    private lateinit var statusTextView: TextView
    private lateinit var dataTextView: TextView

    private lateinit var btnClear: Button
    private lateinit var chart1: LineChart
    private lateinit var chart2: LineChart

    // Zmienna przechowująca "czas" (oś X) dla nowych punktów
    private var timeIndex = 0f

    // UUIDs z kodu Arduino
    private val SERVICE_UUID = UUID.fromString("19B10000-E8F2-537E-4F6C-D104768A1214")
    private val CHAR_UUID = UUID.fromString("19B10001-E8F2-537E-4F6C-D104768A1214")

    private val PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnScan = findViewById(R.id.btnScan)
        btnClear = findViewById(R.id.btnClear)
        statusTextView = findViewById(R.id.statusTextView)
        dataTextView = findViewById(R.id.dataTextView)
        chart1 = findViewById(R.id.chart1)
        chart2 = findViewById(R.id.chart2)

        setupChart(chart1, Color.BLUE)
        setupChart(chart2, Color.RED)

        btnClear.setOnClickListener {
            chart1.clear()
            chart2.clear()
            timeIndex = 0f
            chart1.invalidate()
            chart2.invalidate()
        }

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        btnScan.setOnClickListener {
            if (checkPermissions()) {
                startScanning()
            } else {
                requestPermissions()
            }
        }
    }

    private fun checkPermissions(): Boolean {
        val hasLocation = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val hasScan = ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
            val hasConnect = ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
            return hasLocation && hasScan && hasConnect
        }
        return hasLocation
    }

    private fun requestPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_SCAN)
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
        }

        ActivityCompat.requestPermissions(
            this,
            permissionsToRequest.toTypedArray(),
            PERMISSION_REQUEST_CODE
        )
    }

    @SuppressLint("MissingPermission")
    private fun startScanning() {
        if (bluetoothAdapter == null || !bluetoothAdapter!!.isEnabled) {
            Toast.makeText(this, "Włącz Bluetooth!", Toast.LENGTH_SHORT).show()
            return
        }

        statusTextView.text = "Status: Skanowanie..."
        val scanner = bluetoothAdapter!!.bluetoothLeScanner

        btnScan.postDelayed({
            scanner?.stopScan(scanCallback)
            if (statusTextView.text == "Status: Skanowanie...") {
                statusTextView.text = "Status: Nie znaleziono urządzenia"
            }
        }, 10000)

        scanner?.startScan(scanCallback)
    }

    // Callback odbierający wyniki skanowania
    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val deviceName = device.name

            if (deviceName != null && deviceName.contains("Tensometr", ignoreCase = true)) {
                Log.i("BLE", "Znaleziono urządzenie: $deviceName")
                bluetoothAdapter?.bluetoothLeScanner?.stopScan(this)

                runOnUiThread { statusTextView.text = "Status: Łączenie z $deviceName..." }
                connectToDevice(device)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.e("BLE", "Błąd skanera Bluetooth: $errorCode")
            runOnUiThread { statusTextView.text = "Błąd skanera: $errorCode" }
        }
    }

    @SuppressLint("MissingPermission")
    private fun connectToDevice(device: BluetoothDevice) {
        bluetoothGatt = device.connectGatt(this, false, gattCallback)
    }

    // Callback obsługujący połączenie i wymianę danych GATT
    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                runOnUiThread { statusTextView.text = "Status: Połączono! Szukam serwisów..." }
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                runOnUiThread { statusTextView.text = "Status: Rozłączono" }
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val service = gatt.getService(SERVICE_UUID)
                val characteristic = service?.getCharacteristic(CHAR_UUID)

                if (characteristic != null) {
                    runOnUiThread { statusTextView.text = "Status: Odbieranie danych (Notify)" }
                    gatt.setCharacteristicNotification(characteristic, true)

                    val descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
                    if (descriptor != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            gatt.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                        } else {
                            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                            gatt.writeDescriptor(descriptor)
                        }
                    }
                }
            }
        }

        @Deprecated("Deprecated in Java for Android 13+")
        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            processData(characteristic)
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            processData(characteristic)
        }

        private fun processData(characteristic: BluetoothGattCharacteristic) {
            if (characteristic.uuid == CHAR_UUID) {
                val receivedData = characteristic.getStringValue(0)

                runOnUiThread {
                    dataTextView.text = "Dane: $receivedData"

                    val parts = receivedData?.split(",")
                    if (parts != null && parts.size == 2) {
                        try {
                            val val1 = parts[0].trim().toFloat()
                            val val2 = parts[1].trim().toFloat()

                            addEntryToChart(chart1, val1)
                            addEntryToChart(chart2, val2)

                            timeIndex += 1f

                        } catch (e: Exception) {
                            Log.e("BLE", "Błąd konwersji danych: ${e.message}")
                        }
                    }
                }
            }
        }
    } // <-- Brakowało tej klamry zamykającej gattCallback!

    private fun setupChart(chart: LineChart, lineColor: Int) {
        chart.description.isEnabled = false
        chart.setTouchEnabled(true)
        chart.isDragEnabled = true
        chart.setScaleEnabled(true)
        chart.setDrawGridBackground(false)

        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)

        val data = LineData()
        chart.data = data

        val createSet = {
            val set = LineDataSet(null, "Wartość")
            set.axisDependency = com.github.mikephil.charting.components.YAxis.AxisDependency.LEFT
            set.color = lineColor
            set.setDrawCircles(false)
            set.lineWidth = 2f
            set.setDrawValues(false)
            set
        }

        chart.tag = createSet
    }

    private fun addEntryToChart(chart: LineChart, value: Float) {
        val data = chart.data ?: return
        var set = data.getDataSetByIndex(0)

        if (set == null) {
            val createSet = chart.tag as () -> LineDataSet
            set = createSet()
            data.addDataSet(set)
        }

        data.addEntry(Entry(timeIndex, value), 0)
        data.notifyDataChanged()
        chart.notifyDataSetChanged()

        chart.setVisibleXRangeMaximum(100f)
        chart.moveViewToX(data.entryCount.toFloat())
    }

    @SuppressLint("MissingPermission")
    override fun onDestroy() {
        super.onDestroy()
        bluetoothGatt?.close()
        bluetoothGatt = null
    }
}