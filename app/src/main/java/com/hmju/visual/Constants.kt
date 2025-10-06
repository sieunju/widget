package com.hmju.visual

object Constants {
    const val IMG_BASE_URL = "https://raw.githubusercontent.com/sieunju/widget/develop/assets"

    object ExampleThumb {
        const val GALAXY = IMG_BASE_URL.plus("/example_galaxy.jpg")
        const val DEEP_LINK_WALLPAPER = IMG_BASE_URL.plus("/example_deep_link_wallpaper.png")
        const val PARALLAX_HEADER = IMG_BASE_URL.plus("/example_parallax_header_thumb.png")
    }

    object LogoThumb {
        const val LOGO = IMG_BASE_URL.plus("/widget_logo.png")
        const val LOGO_PURPLE = IMG_BASE_URL.plus("/widget_logo_purple.png")
    }
}

// [s] NameSpace

typealias ExampleThumb = Constants.ExampleThumb

typealias LogoThumb = Constants.LogoThumb

// [e] NameSpace
