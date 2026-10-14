package com.example.saneamentopr

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class ResultadoActivity : AppCompatActivity() {

    private lateinit var txtMunicipio: TextView
    private lateinit var txtEndereco: TextView
    private lateinit var txtAgua: TextView
    private lateinit var txtEsgoto: TextView
    private lateinit var chart: HorizontalBarChart
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var btnVoltar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resultado)

        // Inicializa os componentes
        txtMunicipio = findViewById(R.id.txtMunicipio)
        txtEndereco = findViewById(R.id.txtEndereco)
        txtAgua = findViewById(R.id.txtAgua)
        txtEsgoto = findViewById(R.id.txtEsgoto)
        chart = findViewById(R.id.chartCobertura)
        btnVoltar = findViewById(R.id.btnVoltar)
        mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment

        // Recupera os dados da Intent
        val cep = intent.getStringExtra("cep")
        val municipio = intent.getStringExtra("municipio")
        val logradouro = intent.getStringExtra("logradouro")
        val uf = intent.getStringExtra("uf")
        val lat = intent.getDoubleExtra("lat", -25.4284)
        val lon = intent.getDoubleExtra("lon", -49.2733)
        val aguaAtendida = intent.getBooleanExtra("agua_atendida", false)
        val esgotoAtendido = intent.getBooleanExtra("esgoto_atendido", false)
        val aguaPercent = intent.getFloatExtra("agua_percent", 0f)
        val esgotoPercent = intent.getFloatExtra("esgoto_percent", 0f)

        // Exibe os dados básicos
        txtMunicipio.text = "$municipio - $uf"
        txtEndereco.text = logradouro
        txtAgua.text = if (aguaAtendida) "💧 Água: Atendido" else "💧 Água: Não atendido"
        txtEsgoto.text = if (esgotoAtendido) "🚽 Esgoto: Atendido" else "🚽 Esgoto: Não atendido"

        // Configura o mapa
        setupMap(lat, lon, logradouro)

        // Configura o gráfico
        setupChart(aguaPercent, esgotoPercent)

        btnVoltar.setOnClickListener { finish() }
    }

    private fun setupMap(lat: Double, lon: Double, titulo: String?) {
        mapFragment.getMapAsync { googleMap ->
            try {
                val local = LatLng(lat, lon)
                googleMap.clear()
                googleMap.addMarker(MarkerOptions().position(local).title(titulo ?: "Local"))
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(local, 15f))
            } catch (e: Exception) {
                Toast.makeText(this, "Erro ao carregar mapa: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupChart(aguaPercent: Float, esgotoPercent: Float) {
        val entries = listOf(
            BarEntry(0f, aguaPercent),
            BarEntry(1f, esgotoPercent)
        )

        val dataSet = BarDataSet(entries, "Cobertura (%)").apply {
            colors = listOf(Color.parseColor("#0288D1"), Color.parseColor("#43A047"))
            valueTextColor = Color.BLACK
            valueTextSize = 12f
        }

        val barData = BarData(dataSet)
        chart.data = barData
        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.axisRight.isEnabled = false
        chart.axisLeft.axisMinimum = 0f
        chart.axisLeft.axisMaximum = 100f

        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return if (value.toInt() == 0) "Água" else "Esgoto"
            }
        }

        chart.animateY(1000)
        chart.invalidate()
    }

}



