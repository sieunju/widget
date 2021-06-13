> Visual 적인 View 들을 모아놓은 라이브러리입니다.

---

1. #### 컨셉
    - 외부 라이브러리를 사용하지 않고 최소한으로 구현
2. #### 사양
    - Min SDK Version 21
    - Max SDK Version 30

3. #### 라이브러리 추가 하는 방법
    - *Project Gradle*
    ~~~
    allprojects {
	    repositories {
		    ...
		    maven { url 'https://jitpack.io' }
	    }
    }
    ~~~
    - *App Module Gradle*
    ~~~
    dependencies {
        implementation 'com.github.sieunju:widget:$version'
    }
    ~~~

4. #### 사용 예
    1. ProgressView
        - *간단 설명*
            - Ui 에서 실시간으로 진행률을 나타내야 할때 UiThread 로 처리하기에는 어려움이 있습니다.<br>그래서 주로 카메라 프리뷰에서 사용되는 SurfaceView 기반의 ProgressView 를 만들었습니다. <br>(UiThread 가 아닌 Worker Thread 에서 사용하셔도 Ui 표현이 가능합니다.)
            - 함수
                - incrementProgressBy(diff: Int)
                    - diff -> 증가율
                - currentProgress : Int
                    - 현재 진행률
        - *xml*
        ~~~
        <hmju.widget.progress.ProgressView
            android:id="@+id/progressView"
            android:layout_width="match_parent"
            android:layout_height="30dp"
            app:progressBgColor="@color/white"
            android:layout_marginLeft="20dp"
            android:layout_marginRight="20dp"
            app:progressRadius="15dp"
            app:progressEndColor="#29359C"
            app:progressMax="100"
            app:progressStartColor="#29359C"
            app:progressType="horizontal" />
        ~~~
        - *attrs*
        ~~~
        <declare-styleable name="ProgressView">
            <attr name="progressType" format="enum"> <!-- 수평 or 수직 상태 -->
                <enum name="horizontal" value="0" />
                <enum name="vertical" value="1" />
            </attr>
            <attr name="progressRadius" format="dimension" /> <!-- Radius 값 -->
            <attr name="progressStartColor" format="color" /> <!-- 프로그래스 뷰 시작 색상 -->
            <attr name="progressCenterColor" format="color" /> <!-- 프로그래스 뷰 중간 색상 -->
            <attr name="progressEndColor" format="color" /> <!-- 프로그래스 뷰 마지막 색상 -->
            <attr name="progressCenterXY" format="float" /> <!-- 색상 시작과 끝 중간 지점 비율에 대한 좌표(%) -->
            <attr name="progressBgColor" format="color" />   <!-- Background Color -->
            <attr name="progressMax" format="integer" /> <!-- 최대 진행값 -->
            <attr name="progressMin" format="integer" /> <!-- 최소 진행값 -->
        </declare-styleable>
        ~~~

*ps.추후 behavior, parallaxViewHolder 업로드할 예정.*

