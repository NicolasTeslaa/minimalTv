package com.minimaltv.launcher.data

import com.minimaltv.launcher.model.TvApp
import java.text.Collator
import java.util.Locale

object AppOrder {
    fun sort(apps: List<TvApp>, favorites: Set<String>, launchCounts: Map<String, Int>): List<TvApp> {
        val collator = Collator.getInstance(Locale.forLanguageTag("pt-BR"))
        collator.strength = Collator.PRIMARY
        return apps.sortedWith { left, right ->
            val leftFavorite = left.packageName in favorites
            val rightFavorite = right.packageName in favorites
            when {
                leftFavorite != rightFavorite -> if (leftFavorite) -1 else 1
                leftFavorite && launchCounts[left.packageName] != launchCounts[right.packageName] ->
                    (launchCounts[right.packageName] ?: 0).compareTo(launchCounts[left.packageName] ?: 0)
                else -> collator.compare(left.name, right.name).takeIf { it != 0 }
                    ?: left.packageName.compareTo(right.packageName)
            }
        }
    }
}
