> [![](https://jitpack.io/v/sieunju/widget.svg)](https://jitpack.io/#sieunju/widget)   
> Visual 적인 View 들을 모아놓은 라이브러리입니다.

## 컨셉

- 외부 라이브러리를 사용하지 않고 최소한으로 구현

## 사양

- Min SDK Version 21
- Max SDK Version 30

## 라이브러리 추가 하는 방법

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

# Index

<details>
<summary><strong>View</strong></summary>

- [ProgressView](https://github.com/sieunju/widget/wiki/ProgressView)
- [CustomLayout](https://github.com/sieunju/widget/wiki/CustomLayout)
- [CustomTextView](https://github.com/sieunju/widget/wiki/CustomTextView)
- [FlexibleImageView](https://github.com/sieunju/widget/wiki/FlexibleImageView)

</details>

<details>

<summary><strong>Coordinator.Behavior</strong></summary>

- [TranslationBehavior](https://github.com/sieunju/widget/wiki/TranslationBehavior)

</details>

<details>

<summary><strong>RecyclerView</strong></summary>

- [ParallaxView](https://github.com/sieunju/widget/wiki/ParallaxView)

</details>
