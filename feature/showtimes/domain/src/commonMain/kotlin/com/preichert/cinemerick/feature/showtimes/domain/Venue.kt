package com.preichert.cinemerick.feature.showtimes.domain

data class Venue(
    val chain: Chain,
    val id: String,
    val name: String,
    val region: String? = null,
    val webUrl: String? = null
) {
    val key: String = "${chain.name}:$id"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Venue) return false
        return chain == other.chain && id == other.id
    }

    override fun hashCode(): Int = (chain.name + id).hashCode()
}
