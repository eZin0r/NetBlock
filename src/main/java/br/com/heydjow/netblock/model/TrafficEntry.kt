package br.com.heydjow.netblock.model
data class TrafficEntry(
    val uid:Int,
    val host:String?,
    val sent:Int,
    val received:Int,
    val time:Long
)