package com.hmju.visual.ui.select.models

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.hmju.visual.ui.blur.BlurHeaderActivity
import com.hmju.visual.ui.coordinator.DynamicCoordinatorActivity
import com.hmju.visual.ui.coordinator.TranslationBehaviorActivity
import com.hmju.visual.ui.snackbar.SnackBarActivity
import com.hmju.visual.ui.gesture.FlexibleImageViewFragment
import com.hmju.visual.ui.progress.ProgressFragment
import com.hmju.visual.ui.recyclerview.ParallaxViewHolderFragment
import com.hmju.visual.ui.recyclerview.RecyclerViewScrollerFragment
import com.hmju.visual.ui.recyclerview.SpecialGridDecorationFragment
import com.hmju.visual.ui.tablayout.CustomTabLayoutFragment
import com.hmju.visual.ui.view.CustomViewFragment
import com.hmju.visual.ui.view.PullToRefreshFragment
import com.hmju.visual.ui.view.StackCardViewFragment
import com.hmju.visual.ui.viewpager.ViewPagerFragment
import kotlin.reflect.KClass

/**
 * Description :
 *
 * Created by juhongmin on 2025. 10. 4.
 */
sealed interface Card {
    sealed interface ContentsType {
        data class WebP(
            val name: String,
            val bytes: ByteArray
        ) : ContentsType {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as WebP

                if (name != other.name) return false
                if (!bytes.contentEquals(other.bytes)) return false

                return true
            }

            override fun hashCode(): Int {
                var result = name.hashCode()
                result = 31 * result + bytes.contentHashCode()
                return result
            }

        }

        data class Images(
            val name: String,
            val bytes: ByteArray
        ) : ContentsType {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as Images

                if (name != other.name) return false
                if (!bytes.contentEquals(other.bytes)) return false

                return true
            }

            override fun hashCode(): Int {
                var result = name.hashCode()
                result = 31 * result + bytes.contentHashCode()
                return result
            }

        }
    }

    data class FragmentCard(
        val title: String,
        val contents: ContentsType,
        val router: String
    ) : Card {
        fun getTargetFragment(): KClass<out Fragment>? {
            return when (router) {
                "CustomViewFragment" -> CustomViewFragment::class
                "FlexibleImageViewFragment" -> FlexibleImageViewFragment::class
                "ProgressFragment" -> ProgressFragment::class
                "ViewPagerFragment" -> ViewPagerFragment::class
                "CustomTabLayoutFragment" -> CustomTabLayoutFragment::class
                "ParallaxViewHolderFragment" -> ParallaxViewHolderFragment::class
                "SpecialGridDecorationFragment" -> SpecialGridDecorationFragment::class
                "RecyclerViewScrollerFragment" -> RecyclerViewScrollerFragment::class
                "StackCardViewFragment" -> StackCardViewFragment::class
                "PullToRefreshFragment" -> PullToRefreshFragment::class
                else -> null
            }
        }
    }

    data class ActivityCard(
        val title: String,
        val contents: ContentsType,
        val router: String
    ) : Card {
        fun getTargetActivity(): KClass<out FragmentActivity>? {
            return when (router) {
                "TranslationBehaviorActivity" -> TranslationBehaviorActivity::class
                "DynamicCoordinatorActivity" -> DynamicCoordinatorActivity::class
                "SnackBarActivity" -> SnackBarActivity::class
                "BlurHeaderActivity" -> BlurHeaderActivity::class
                else -> null
            }
        }
    }

    companion object {
        fun Card.getThumbContents(): ContentsType {
            return when (this) {
                is FragmentCard -> contents
                is ActivityCard -> contents
            }
        }
    }
}