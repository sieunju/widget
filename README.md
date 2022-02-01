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

## Index





    * ### FlexibleImageView
        - *간단 설명*
            - 사진을 Scale 및 Move를 이용해서 저장하고 싶은 이미지를 편집 할수 있는 ImageView 기반 클래스입니다.   
            참고: 인스타 피드 추가
        - *xml*
            - FlexibleImageView는 절대 단독적으로 있으면 안됩니다. FrameLayout 이나 ConstraintLayout 어떤 레이아웃에 감싸야 합니다.   
            궁금 하시다면 단독적으로 해보시길.. :)
        ~~~
        <androidx.constraintlayout.widget.ConstraintLayout
            android:id="@+id/clFlexible"
            android:layout_width="match_parent"
            android:layout_height="0dp"
            android:layout_marginTop="50dp"
            android:background="#FFFFFF"
            app:layout_constraintDimensionRatio="1:1"
            app:layout_constraintTop_toTopOf="parent">

            <hmju.widget.view.FlexibleImageView
                android:id="@+id/imgThumb"
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:adjustViewBounds="true" />

        </androidx.constraintlayout.widget.ConstraintLayout>
        ~~~
        - *Public Function*
            - loadUrl(url : String)
                - 외부 이미지 주소나 갤러리 이미지 URL을 호출하면 해당 이미지 뷰에 렌더링 처리하는 함수입니다.
            - centerCrop
                - 이미지 리소스를 뷰 너비 와 높이를 비율에 맞게 꽉채우는 함수입니다. 
                - Like ImageView ScaleType: CenterCrop 과 동일 합니다. 
                - 단, Gesture 를 통해 이미지를 다시 확대하거나 축소 할 수 있습니다.
            - fitCenter
                - 이미지 리소스를 뷰 너비 or 높이 둘중하나에 맞춰서 확대 처리하는 함수입니다.
                - Like ImageView ScaleType: FitCenter 와 동일 합니다.
                - 단, Gesture 를 통해 이미지를 다시 확대하거나 축소 할 수 있습니다.
             
        - *Extension Function*
            - captureBitmap(callback : (Bitmap) -> Unit) (Deprecated)
                - FlexibleImage 를 감싸고 있는 뷰를 기준으로 캡처하여 Bitmap 으로 치환하여 콜백 처리하는 확장 함수입니다.  
            - captureBitmap() : Bitmap
                - FlexibleImage 를 감싸고 있는 뷰를 기준으로 캡처하여 Bitmap 으로 치환하여 리턴하는 확장 함수입니다.
            - backgroundCaptureBitmap() : Bitmap
                - FlexibleImage 를 통해 움직인 이미지 좌표를 기준 및 이미지에 사용한 Bitmap 을 가지고 백그라운드 상태로 캡처 할수 있는 확장 함수
                - Glide ImageLoader 를 이용한 예시입니다. 참고 하시면 되겠습니다 :)
                    ~~~
                    Glide.with(requireContext())
                        .asDrawable()
                        .load(imageUrl)
                        .into(object : CustomTarget<Drawable?>() {

                            override fun onResourceReady(
                                resource: Drawable,
                                transition: Transition<in Drawable?>?
                            ) {
                                val bitmapDrawable = resource as BitmapDrawable
                                GlobalScope.launch {
                                    val bitmap = withContext(Dispatchers.IO) {
                                        backgroundCaptureBitmap(
                                            bitmapDrawable.bitmap,
                                            flexibleImage.getStateItem(),
                                            flexibleCaptureView.width,
                                            flexibleCaptureView.height
                                        )
                                }
                                withContext(Dispatchers.Main) {
                                    view.findViewById<AppCompatImageView>(R.id.imgCapture)
                                        .setImageBitmap(bitmap)
                                }
                            }
                        }
                    })
                    ~~~
        - *영상*   
        
         ![flexibleimageview-test](https://user-images.githubusercontent.com/33802191/143772342-892044a6-eee7-4e12-aecf-28799496ad38.gif)
