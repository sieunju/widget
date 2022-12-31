package com.hmju.visual

object Constants {
    const val IMG_BASE_URL = "https://raw.githubusercontent.com/sieunju/widget/develop/stroage"
    object SelectMenuThumb {
        const val VIEW = "/example_view.gif"
    }

    object ExampleThumb {
        const val GALAXY = IMG_BASE_URL.plus("/example_galaxy.jpg")
    }
}

typealias MenuThumb = Constants.SelectMenuThumb
