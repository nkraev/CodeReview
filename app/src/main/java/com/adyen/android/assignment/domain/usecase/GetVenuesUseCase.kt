package com.adyen.android.assignment.domain.usecase

import android.location.Location
import com.adyen.android.assignment.api.PlacesService
import com.adyen.android.assignment.api.VenueRecommendationsQueryBuilder
import com.adyen.android.assignment.api.model.toVenue
import com.adyen.android.assignment.domain.models.Venue

class GetVenuesUseCase(
    private val api: PlacesService // could be repository with several data sources
) {

    suspend operator fun invoke(location: Location): List<Venue> {
        val query = VenueRecommendationsQueryBuilder()
            .setLatitudeLongitude(location.latitude, location.longitude)
            .build()

        val venuesCall = api.getVenueRecommendations(query).results.map { it.toVenue() }

        return venuesCall
    }
}