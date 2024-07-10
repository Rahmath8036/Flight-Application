package com.example.flightapp.api

import com.google.gson.annotations.SerializedName

data class FlightSearchResponse(
    @SerializedName("Quotes") val quotes: List<Quote>,
    @SerializedName("Places") val places: List<Place>,
    @SerializedName("Carriers") val carriers: List<Carrier>
)

data class Quote(
    @SerializedName("QuoteId") val quoteId: Int,
    @SerializedName("MinPrice") val minPrice: Double,
    @SerializedName("Direct") val direct: Boolean,
    @SerializedName("OutboundLeg") val outboundLeg: OutboundLeg,
    @SerializedName("InboundLeg") val inboundLeg: OutboundLeg?
)

data class Place(
    @SerializedName("PlaceId") val placeId: Int,
    @SerializedName("Name") val name: String,
    @SerializedName("Type") val type: String,
    @SerializedName("SkyscannerCode") val skyscannerCode: String
)

data class Carrier(
    @SerializedName("CarrierId") val carrierId: Int,
    @SerializedName("Name") val name: String
)

data class OutboundLeg(
    @SerializedName("CarrierIds") val carrierIds: List<Int>,
    @SerializedName("OriginId") val originId: Int,
    @SerializedName("DestinationId") val destinationId: Int,
    @SerializedName("DepartureDate") val departureDate: String
)
