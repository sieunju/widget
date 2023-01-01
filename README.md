<p align="center">
  <img src="https://raw.githubusercontent.com/sieunju/widget/develop/storage/widget_logo.png" alt="widgetLogo" width="300" />
</p>

> [![](https://jitpack.io/v/sieunju/widget.svg)](https://jitpack.io/#sieunju/widget)   
> Custom Compoent Ui Library. 😎

![AndroidMinSdkVersion](https://img.shields.io/badge/minSdkVersion-21-green.svg) ![AndroidTargetSdkVersion](https://img.shields.io/badge/targetSdkVersion-30-brightgreen.svg)

## Concept

- 외부 라이브러리를 사용하지 않고 최소한으로 구현   
(Minimize deployment without using external libraries)

## HowTo

- Project Gradle

```groovy
allprojects {
	    repositories {
		    ...
		    maven { url 'https://jitpack.io' }
	    }
}
```

- App Module Gradle

```groovy
dependencies {
    	implementation 'com.github.sieunju:widget:$latestVersion'
}
```
## Example Screen
|CustomView|FlexibleImageEidtView|ProgressView|
|---|---|---|
|![](https://raw.githubusercontent.com/sieunju/widget/develop/storage/example_view.webp)|![](https://raw.githubusercontent.com/sieunju/widget/develop/storage/example_flexible.webp)|![](https://raw.githubusercontent.com/sieunju/widget/develop/storage/example_progress.webp)
|LineIndicator|TabLayout|ParallaxViewHolder|
|![](https://raw.githubusercontent.com/sieunju/widget/develop/storage/example_viewpager.webp)|![](https://raw.githubusercontent.com/sieunju/widget/develop/storage/example_tab_layout.webp)|![](https://raw.githubusercontent.com/sieunju/widget/develop/storage/example_parallax.webp)|
|SpecialGrid|Scroller|TranslationBehavior|
|![](https://raw.githubusercontent.com/sieunju/widget/develop/storage/example_special_grid_decoration.webp)|![](https://raw.githubusercontent.com/sieunju/widget/develop/storage/example_recyclerview_scroller.webp)|![](https://raw.githubusercontent.com/sieunju/widget/develop/storage/example_translation_behavior.webp)|


## 이슈 사항 (Material Libraries Conflict??)
- 혹시나 머티리얼을 사용하시거나 프로젝트에 사용중인 라이브러리랑 충돌이 일어나는 경우에는 아래와 같이 사용해주시면 됩니다. 🙇‍♂️
(this is soluction 😎)

- A.K.A exclude
```groovy

implementation("com.github.sieunju:widget:$lateversion") {
        exclude("com.google.android.material")
        exclude("androidx.appcompat:appcompat")
        exclude("androidx.constraintlayout")
    }
```

# Index

<details>
<summary><strong>View</strong></summary>

- [ProgressView](https://github.com/sieunju/widget/wiki/ProgressView)
- [CustomLayout](https://github.com/sieunju/widget/wiki/CustomLayout)
- [CustomTextView](https://github.com/sieunju/widget/wiki/CustomTextView)
- [FlexibleImageView](https://github.com/sieunju/widget/wiki/FlexibleImageView)
- [LinePagerTabLayout](https://github.com/sieunju/widget/wiki/LinePagerTabLayout)

</details>

<details>

<summary><strong>Coordinator.Behavior</strong></summary>

- [TranslationBehavior](https://github.com/sieunju/widget/wiki/TranslationBehavior)

</details>

<details>

<summary><strong>RecyclerView</strong></summary>

- [ParallaxView](https://github.com/sieunju/widget/wiki/ParallaxView)
- [CustomLinearScroller](https://github.com/sieunju/widget/wiki/CustomLinearScroller)

</details>

<details>

<summary><strong>ViewPager2</strong></summary>

- [LineIndicator](https://github.com/sieunju/widget/wiki/LineIndicator)
- [AutoScrollMediator](https://github.com/sieunju/widget/wiki/AutoScrollMediator)

</details>
