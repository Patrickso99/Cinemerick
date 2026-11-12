package com.preichert.cinemerick.feature.showtimes.domain

/** How a cinema the user named matches the venues on offer. */
sealed interface VenueMatch {
    data class Certain(val venue: Venue) : VenueMatch

    /** Probably [best], but the words differ ("Notorius Fiume"): the user confirms or picks one of [candidates]. */
    data class Close(val best: Venue, val candidates: List<Venue>) : VenueMatch

    /** Nothing near enough: the cinema is skipped. */
    data object NotFound : VenueMatch
}

private const val MAX_CANDIDATES = 3
private const val EXACT = 1.0
private const val NEAR = 0.6

/** The words each chain is called by, normalized. */
private val chainAliases: Map<Chain, List<String>> = mapOf(
    Chain.THE_SPACE to listOf("the space", "space"),
    Chain.UCI to listOf("uci", "uci cinemas"),
    Chain.NOTORIOUS to listOf("notorious"),
    Chain.CINERGIA to listOf("cinergia"),
    Chain.CRISTALLO to listOf("cristallo"),
    Chain.DON_BOSCO to listOf("don bosco"),
    Chain.CINEMAZERO to listOf("cinemazero", "cinema zero"),
    Chain.ARCADIA to listOf("arcadia"),
)

// Words that say nothing about which venue it is.
private val fillerWords = setOf("cinema", "cinemas", "multisala", "di", "a", "al", "the", "luxe")

/**
 * Matches [mention] against the venues of every chain. A venue is certain only when the chain was said as it is
 * spelled and every place word is in the venue name; a chain or place said slightly differently makes it a close
 * match the user confirms. [preferred] (the venues already selected) wins when only a chain was named.
 */
fun matchVenue(mention: VenueMention, catalog: Map<Chain, List<Venue>>, preferred: Set<Venue> = emptySet()): VenueMatch {
    val spoken = normalize(mention.spoken)
    // The user's words win over the model's reading of them: a small model attaches the wrong chain and place to a name.
    val spokenChain = chainIn(spoken)
    val modelContradicts = spokenChain != null && mention.chain != null && spokenChain != mention.chain
    val chain = spokenChain ?: mention.chain
    val chainSaidExactly = chain != null && chainAliases.getValue(chain).any { containsWords(spoken, it) }
    val pool = if (chain != null) catalog[chain].orEmpty() else catalog.values.flatten()
    if (pool.isEmpty()) return VenueMatch.NotFound

    val placeWords = words(mention.place.takeUnless { modelContradicts } ?: placeIn(spoken, chain))
    if (placeWords.isEmpty()) {
        if (chain == null) return VenueMatch.NotFound
        val best = pool.firstOrNull { it in preferred } ?: pool.first()
        return if (pool.size == 1 && chainSaidExactly) VenueMatch.Certain(best)
        else VenueMatch.Close(best, (listOf(best) + pool).distinct().take(MAX_CANDIDATES))
    }

    // Words no venue has at all are probably a chain said wrong ("Ucy Marcon"): they are left out of the score, but
    // the match can then only be close.
    val knownWords = placeWords.filter { word -> pool.any { placeScore(listOf(word), it) > 0.0 } }
    val unknownWords = knownWords.size < placeWords.size
    val scored = pool.map { it to placeScore(knownWords.ifEmpty { placeWords }, it) }.filter { it.second > 0.0 }.sortedByDescending { it.second }
    val (best, score) = scored.firstOrNull() ?: return VenueMatch.NotFound
    val tiedExact = scored.count { it.second == EXACT } > 1
    return when {
        score == EXACT && !tiedExact && !unknownWords && (chainSaidExactly || chain == null) -> VenueMatch.Certain(best)
        score >= NEAR -> VenueMatch.Close(best, scored.map { it.first }.take(MAX_CANDIDATES))
        else -> VenueMatch.NotFound
    }
}

/**
 * A small model sometimes puts several cinemas in one mention ("the space silea uci marcon notorius fiume"). A new
 * cinema starts at each chain name (or a near spelling of it); a mention with one chain at most is left as it is. The
 * pieces keep the words as said and have no chain or place of their own: those are read from the words when matching.
 */
fun splitVenueMention(mention: VenueMention): List<VenueMention> {
    val words = mention.spoken.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    val chains = words.map { chainOfWord(normalize(it)) }
    val starts = words.indices.filter { index ->
        // A chain name of two words ("uci cinemas") starts once.
        chains[index] != null && !(index > 0 && chains[index - 1] == chains[index])
    }
    if (starts.size < 2) return listOf(mention)
    return starts.mapIndexed { i, start ->
        // "the" belongs to the name that follows ("the space").
        val from = if (start > 0 && normalize(words[start - 1]) == "the" && i > 0) start - 1 else start
        val to = starts.getOrNull(i + 1)?.let { next -> if (next > 0 && normalize(words[next - 1]) == "the") next - 1 else next } ?: words.size
        VenueMention(spoken = words.subList(if (i == 0) 0 else from, to).joinToString(" "))
    }
}

// The chain a single word names, exactly or nearly; words that are not part of a name never do.
private fun chainOfWord(key: String): Chain? {
    if (key in fillerWords) return null
    return chainAliases.entries.firstOrNull { (_, aliases) ->
        aliases.flatMap { it.split(' ') }.filter { it !in fillerWords }.any { alias -> key == alias || isNear(key, alias) }
    }?.key
}

// The share of place words found in the venue's name or region: exact or a prefix counts fully, a near spelling less.
private fun placeScore(placeWords: List<String>, venue: Venue): Double {
    val venueWords = words(venue.name) + words(venue.region.orEmpty())
    val total = placeWords.sumOf { word ->
        when {
            venueWords.any { it == word || (word.length >= 4 && it.startsWith(word)) } -> EXACT
            venueWords.any { isNear(word, it) } -> NEAR
            else -> 0.0
        }
    }
    return total / placeWords.size
}

private fun chainIn(spoken: String): Chain? =
    chainAliases.entries.firstOrNull { (_, aliases) -> aliases.any { containsWords(spoken, it) } }?.key
        ?: chainAliases.entries.firstOrNull { (_, aliases) ->
            aliases.any { alias -> words(spoken).any { isNear(it, alias.replace(" ", "")) } }
        }?.key

// What is left of the spoken words once the chain's name (or a near spelling of it) is taken out.
private fun placeIn(spoken: String, chain: Chain?): String {
    val aliasWords = chain?.let { chainAliases.getValue(it).flatMap(::words) }.orEmpty()
    return words(spoken).filterNot { word -> aliasWords.any { it == word || isNear(word, it) } }.joinToString(" ")
}

private fun containsWords(text: String, phrase: String): Boolean = " $text ".contains(" $phrase ")

private fun words(text: String): List<String> =
    normalize(text).split(' ').filter { it.isNotEmpty() && it !in fillerWords }

private fun normalize(text: String): String = titleKey(text)

// Near spellings: one edit for words of 4+ letters, two for 7+.
private fun isNear(a: String, b: String): Boolean {
    val allowed = when {
        minOf(a.length, b.length) >= 7 -> 2
        minOf(a.length, b.length) >= 4 -> 1
        else -> 0
    }
    return allowed > 0 && a != b && editDistance(a, b) <= allowed
}

private fun editDistance(a: String, b: String): Int {
    var previous = IntArray(b.length + 1) { it }
    for (i in 1..a.length) {
        val current = IntArray(b.length + 1)
        current[0] = i
        for (j in 1..b.length) {
            val cost = if (a[i - 1] == b[j - 1]) 0 else 1
            current[j] = minOf(previous[j] + 1, current[j - 1] + 1, previous[j - 1] + cost)
        }
        previous = current
    }
    return previous[b.length]
}
