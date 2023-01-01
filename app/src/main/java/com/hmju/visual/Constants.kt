package com.hmju.visual

object Constants {
    const val BASE_URL = "https://raw.githubusercontent.com"
    const val IMG_BASE_URL = "https://raw.githubusercontent.com/sieunju/widget/develop/storage"

    object SelectMenuThumb {
        const val VIEW = IMG_BASE_URL.plus("/example_view.webp")
        const val FLEXIBLE = IMG_BASE_URL.plus("/example_flexible.webp")
        const val PROGRESS = IMG_BASE_URL.plus("/example_progress.webp")
        const val VIEWPAGER = IMG_BASE_URL.plus("/example_viewpager.webp")
        const val TAB_LAYOUT = IMG_BASE_URL.plus("/example_tab_layout.webp")
        const val PARALLAX = IMG_BASE_URL.plus("/example_parallax.webp")
        const val SPECIAL_GRID_DECORATION = IMG_BASE_URL.plus("/example_special_grid_decoration.webp")
        const val TRANSLATION_BEHAVIOR = IMG_BASE_URL.plus("/example_translation_behavior.webp")
        const val RECYCLERVIEW_CUSTOM_SCROLLER = IMG_BASE_URL.plus("/example_recyclerview_scroller.webp")
    }

    object ExampleThumb {
        const val GALAXY = IMG_BASE_URL.plus("/example_galaxy.jpg")
        const val DEEP_LINK_WALLPAPER = IMG_BASE_URL.plus("/example_deep_link_wallpaper.png")
        const val PARALLAX_HEADER = IMG_BASE_URL.plus("/example_parallax_header_thumb.png")
    }
}

// [s] NameSpace

typealias MenuThumb = Constants.SelectMenuThumb

typealias ExampleThumb = Constants.ExampleThumb

// [e] NameSpace
