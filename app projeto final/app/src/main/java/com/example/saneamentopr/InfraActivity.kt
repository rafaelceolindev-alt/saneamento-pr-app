package com.example.saneamentopr

import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class InfraActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var txtEndereco: TextView
    private lateinit var txtMunicipio: TextView
    private lateinit var txtAgua: TextView
    private lateinit var txtEsgoto: TextView
    private lateinit var iconAgua: ImageView
    private lateinit var iconEsgoto: ImageView
    private lateinit var chartCobertura: HorizontalBarChart

    private var lat: Double = 0.0
    private var lon: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_infra)

        // Inicializa as views
        txtEndereco = findViewById(R.id.txtEndereco)
        txtMunicipio = findViewById(R.id.txtMunicipio)
        txtAgua = findViewById(R.id.txtAgua)
        txtEsgoto = findViewById(R.id.txtEsgoto)
        iconAgua = findViewById(R.id.iconAgua)
        iconEsgoto = findViewById(R.id.iconEsgoto)
        chartCobertura = findViewById(R.id.chartCobertura)

        // Recebe os dados via Intent
        val endereco = intent.getStringExtra("logradouro") ?: "Endereço não informado"
        val municipio = intent.getStringExtra("municipio") ?: "Município não informado"
        val aguaAtendida = intent.getBooleanExtra("agua_atendida", false)
        val esgotoAtendido = intent.getBooleanExtra("esgoto_atendido", false)
        val aguaPercent = intent.getFloatExtra("agua_percentual", 0f)
        val esgotoPercent = intent.getFloatExtra("esgoto_percentual", 0f)
        lat = intent.getDoubleExtra("lat", 0.0)
        lon = intent.getDoubleExtra("lon", 0.0)

        // Mostra dados textuais
        txtEndereco.text = endereco
        txtMunicipio.text = municipio
        txtAgua.text = if (aguaAtendida) "✅ Água Atendida" else "🚫 Sem Água"
        txtEsgoto.text = if (esgotoAtendido) "✅ Esgoto Atendido" else "🚫 Sem Esgoto"

        // Define ícones visuais
        iconAgua.setImageResource(if (aguaAtendida) R.drawable.ic_water_drop else R.drawable.ic_water_drop)
        iconEsgoto.setImageResource(if (esgotoAtendido) R.drawable.ic_pipe else R.drawable.ic_pipe)

        // Exibe o gráfico
        exibirGrafico(aguaPercent, esgotoPercent)

        // Inicia o mapa
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun exibirGrafico(aguaPercent: Float, esgotoPercent: Float) {
        val entries = listOf(
            BarEntry(0f, aguaPercent),
            BarEntry(1f, esgotoPercent)
        )

        val dataSet = BarDataSet(entries, "Cobertura (%)")
        dataSet.colors = listOf(Color.parseColor("#2196F3"), Color.parseColor("#4CAF50"))
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 12f

        val barData = BarData(dataSet)
        barData.barWidth = 0.6f

        chartCobertura.data = barData
        chartCobertura.description.isEnabled = false
        chartCobertura.legend.isEnabled = false

        val xAxis = chartCobertura.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(listOf("Água", "Esgoto"))
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.textColor = Color.BLACK
        xAxis.textSize = 12f

        chartCobertura.axisLeft.axisMinimum = 0f
        chartCobertura.axisLeft.axisMaximum = 100f
        chartCobertura.axisLeft.textColor = Color.BLACK
        chartCobertura.axisRight.isEnabled = false

        chartCobertura.invalidate()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val ponto = LatLng(lat, lon)
        googleMap.addMarker(
            MarkerOptions()
                .position(ponto)
                .title("Local consultado")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
        )
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ponto, 15f))
    }
}

