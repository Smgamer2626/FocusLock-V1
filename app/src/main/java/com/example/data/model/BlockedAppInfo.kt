package com.example.data.model

data class BlockedAppInfo(
    val id: String,
    val name: String,
    val packageName: String,
    val category: String,
    val iconKey: String, // e.g. "youtube", "instagram", "tiktok", "gaming", "social", "generic"
    val isDistractionPreset: Boolean = true
)

object BlockedAppsCatalog {
    val defaultCatalog = listOf(
        BlockedAppInfo(
            id = "yt",
            name = "YouTube",
            packageName = "com.google.android.youtube",
            category = "Video & Streaming",
            iconKey = "youtube"
        ),
        BlockedAppInfo(
            id = "ig",
            name = "Instagram",
            packageName = "com.instagram.android",
            category = "Social Media",
            iconKey = "instagram"
        ),
        BlockedAppInfo(
            id = "tt",
            name = "TikTok",
            packageName = "com.zhiliaoapp.musically",
            category = "Social Media",
            iconKey = "tiktok"
        ),
        BlockedAppInfo(
            id = "sc",
            name = "Snapchat",
            packageName = "com.snapchat.android",
            category = "Social Media",
            iconKey = "snapchat"
        ),
        BlockedAppInfo(
            id = "nflx",
            name = "Netflix",
            packageName = "com.netflix.mediaclient",
            category = "Video & Streaming",
            iconKey = "netflix"
        ),
        BlockedAppInfo(
            id = "disc",
            name = "Discord",
            packageName = "com.discord",
            category = "Social & Messaging",
            iconKey = "discord"
        ),
        BlockedAppInfo(
            id = "rdt",
            name = "Reddit",
            packageName = "com.reddit.frontpage",
            category = "Social Media",
            iconKey = "reddit"
        ),
        BlockedAppInfo(
            id = "rblx",
            name = "Roblox",
            packageName = "com.roblox.client",
            category = "Games",
            iconKey = "gaming"
        ),
        BlockedAppInfo(
            id = "twch",
            name = "Twitch",
            packageName = "tv.twitch.android.app",
            category = "Video & Streaming",
            iconKey = "twitch"
        ),
        BlockedAppInfo(
            id = "twtr",
            name = "X / Twitter",
            packageName = "com.twitter.android",
            category = "Social Media",
            iconKey = "twitter"
        ),
        BlockedAppInfo(
            id = "spot",
            name = "Spotify",
            packageName = "com.spotify.music",
            category = "Music & Podcasts",
            iconKey = "spotify"
        ),
        BlockedAppInfo(
            id = "gms",
            name = "Mobile Games & Arcade",
            packageName = "com.google.android.play.games",
            category = "Games",
            iconKey = "gaming"
        )
    )

    fun getAppByName(name: String): BlockedAppInfo? {
        return defaultCatalog.find { it.name.equals(name, ignoreCase = true) || it.packageName.equals(name, ignoreCase = true) }
    }
}
