package org.arcanaforge.app.domain.correspondence

import kotlinx.serialization.Serializable

@Serializable
data class CardCorrespondences(
    val chakras: List<String> = emptyList(),
    val crystals: List<String> = emptyList(),
    val elements: List<String> = emptyList(),
    val zodiacSigns: List<String> = emptyList(),
    val planets: List<String> = emptyList(),
    val colors: List<String> = emptyList(),
    val herbs: List<String> = emptyList(),
    val custom: Map<String, List<String>> = emptyMap(),
)
