package com.hmju.visual

object Constants {
    const val IMG_BASE_URL = "https://raw.githubusercontent.com/sieunju/widget/develop/stroage"

    object SelectMenuThumb {
        const val VIEW = IMG_BASE_URL.plus("/example_view.gif")
        const val FLEXIBLE = IMG_BASE_URL.plus("/example_flexible.gif")
        const val PROGRESS = IMG_BASE_URL.plus("/example_progress.gif")
        const val VIEWPAGER = IMG_BASE_URL.plus("/example_viewpager.gif")
        const val TAB_LAYOUT = IMG_BASE_URL.plus("/example_tab_layout.gif")
    }

    object ExampleThumb {
        const val GALAXY = IMG_BASE_URL.plus("/example_galaxy.jpg")
        const val DEEP_LINK_WALLPAPER = IMG_BASE_URL.plus("/example_deep_link_wallpaper.png")
    }
}

// [s] NameSpace

typealias MenuThumb = Constants.SelectMenuThumb

typealias ExampleThumb = Constants.ExampleThumb

// [e] NameSpace
