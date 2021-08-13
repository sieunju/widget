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
    	Latest Version: 0.0.3
        implementation 'com.github.sieunju:widget:$version'
    }
    ~~~

4. #### 사용 예
    * ### ProgressView
        - *간단 설명*
            - Ui 에서 실시간으로 진행률을 나타내야 할때 UiThread 로 처리하기에는 어려움이 있습니다.  
            그래서 주로 카메라 프리뷰에서 사용되는 SurfaceView 기반의 ProgressView 를 만들었습니다.  
            (UiThread 가 아닌 Worker Thread 에서 사용하셔도 Ui 표현이 가능합니다.)
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
    
    * ### TranslationBehavior
        - *간단 설명*
            - CoordinatorLayout 기반으로 스크롤에 따라서 x, y, scale, alpha  
            UI 업데이트 처리 할수 있는 Behavior Class 입니다.
        - *xml*
        ~~~
        <androidx.cardview.widget.CardView
            android:layout_width="80dp"
            android:layout_height="80dp"
            app:layout_behavior="hmju.widget.behavior.TranslationBehavior"
            app:behaviorDependId="@id/abl_header"
            app:behaviorDependPin="50dp"
            app:behaviorEndX="e,10"
            app:behaviorEndY="14"
            app:behaviorEndWidth="30dp"
            android:layout_marginTop="30dp"
            android:layout_marginRight="10dp"
            app:behaviorEndHeight="30dp"
            android:layout_gravity="right|top"
            app:cardCornerRadius="40dp"
            app:cardElevation="5dp">

        <ImageView
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:adjustViewBounds="true"
            android:scaleType="centerInside"
            android:src="@drawable/ic_launcher_foreground" />
        </androidx.cardview.widget.CardView>
        ~~~
        - *attrs*
        ~~~
        <declare-styleable name="TranslationBehavior">
            <attr name="behaviorDependId" format="reference"/> <!-- Dependency View Id -->
            <attr name="behaviorDependType" format="enum"> <!-- Dependency View Type -->
                <enum name="horizontal" value="0"/>
                <enum name="vertical" value="1"/>
            </attr>
            <attr name="behaviorDependPin" format="dimension" /> <!-- Dependency Pin Height or Width-->
            <attr name="behaviorDependRange" format="dimension"/> <!-- Dependency Action Range -->

            <!-- Child View End X Location
                Format {$Standard , End Location X}
                @param Standard -> s or e (없어도 됨.)
                @param End Location X -> 33(dp) s or e 기준으로 Child View 를 마지막에 위치 하고 싶은 Y 좌표.
                ex.) 23 or s,33
            -->
            <attr name="behaviorEndX" format="string"/>
            <!-- Child View End Y Location
                Format {$Standard , End Location Y}
                @param Standard -> s or e (없어도 됨.)
                @param End Location Y -> 33(dp) s or e 기준으로 Child View 를 마지막에 위치 하고 싶은 Y 좌표.
                ex.) 23 or s,33
            -->
            <attr name="behaviorEndY" format="string"/>
            <attr name="behaviorEndAlpha" format="float"/> <!-- Child View End Alpha -->

            <attr name="behaviorEndWidth" format="dimension"/>   <!-- Child View End Width -->
            <attr name="behaviorEndHeight" format="dimension"/> <!-- Child View End Height -->
        </declare-styleable>
        ~~~
        - *영상*
        <img width="30%" src="https://user-images.githubusercontent.com/33802191/128978844-eebdc119-d27e-41d6-a0ce-b042f1f24e97.gif" />
        
    * ### CustomLayout, CustomTextView
        - *간단 설명*
            - View 에서 Background 처리 할때 Drawable.xml 추가합니다. 그렇게 되면 관리하기도 쉽지 않고,  
            각 Background 들이 제각각 이면 처리하기가 쉽지 않습니다.  
            또한 비/선택, 비/활성화 상태에 따라 처리하는 것도 쉽지 않습니다.
            - 간단한 속성값으로 Corner, Border, Selected, Enable 처리 할수 있는 View Class 입니다.
            - ___CustomLayout 안에 ImageView 가 있는 경우 CustomLayout Corner 값에 따라 Crop 될수 있습니다.___
        - *xml*
        ~~~
        <hmju.widget.view.CustomTextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:padding="15dp"
            android:text="텍스트!"
            app:textViewBgColor="#673AB7"
            app:textViewCorner="5dp"
            app:textViewTxtColor="#FFFFFF" />
        
        <hmju.widget.view.CustomLayout
            android:layout_width="match_parent"
            android:layout_height="200dp"
            android:layout_margin="15dp"
            app:layoutBgColor="@color/purple_700"
            app:layoutCorner="20dp">

            <androidx.appcompat.widget.AppCompatImageView
                android:id="@+id/imgThumb"
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:scaleType="centerCrop" />

        </hmju.widget.view.CustomLayout>
        ~~~
        - *attrs*
        ~~~
        <declare-styleable name="CustomTextView">
            <attr name="textViewIsAuto" format="boolean" /> <!-- 속성으로 표현할건지 코드로 표현 할건지 Default true -->
            <attr name="textViewDefState" format="boolean" /> <!-- Default 상태 -->
            <attr name="textViewCorner" format="dimension" /> <!-- Corner Radius -->
            <attr name="textViewTxtColor" format="color" /> <!-- Enable or Default TextColor -->
            <attr name="textViewBgColor" format="color" /> <!-- Enable or Default Bg Color -->
            <attr name="textViewBorder" format="dimension" /> <!-- Enable or Default Border Width -->
            <attr name="textViewBorderColor" format="color" /> <!--Enable or Default Border Color  -->
            <attr name="textViewDisableTxtColor" format="color" /> <!-- Disable or Not Selected Text Color-->
            <attr name="textViewDisableBgColor" format="color" /> <!-- Disable or Not Selected Bg Color -->
            <attr name="textViewDisableBorder" format="dimension" /> <!-- Disable or Not Selected Border Width -->
            <attr name="textViewDisableBorderColor" format="color" /> <!-- Disable Or Not Selected Border Color -->
            <attr name="textViewAutoMaxSize" format="integer"/> <!-- Auto Sizing Max Size -->
            <attr name="textViewAutoMinSize" format="integer"/> <!-- Auto Sizing Min Size -->
        </declare-styleable>

        <declare-styleable name="CustomLayout">
            <attr name="layoutDefState" format="boolean" />
            <attr name="layoutCorner" format="dimension" />
            <attr name="layoutBgColor" format="color" />
            <attr name="layoutBorder" format="dimension" />
            <attr name="layoutBorderColor" format="color" />
            <attr name="layoutDisableBgColor" format="color" />
            <attr name="layoutDisableBorder" format="dimension" />
            <attr name="layoutDisableBorderColor" format="color" />
        </declare-styleable>
        ~~~
        - *예시 화면*  
        <img width="40%" src="https://user-images.githubusercontent.com/33802191/128981833-26fdf4a3-fb58-4f2c-b028-f9b0f392af54.jpg"/>  
        
#### ps. ___현재 버전에서는 autoTextSize 구현이 안되어 있습니다. 향후 버전에 업데이트 할 예정입니다.___
