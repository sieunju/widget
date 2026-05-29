# CustomSwitchView

### 요약 (Summary)

- 간단한 속성값으로 Apple(iOS) 스타일의 토글 스위치를 구현할 수 있는 ViewClass 입니다.  
(A toggle switch View that replicates the iOS UISwitch style using simple attribute values.)

### 지원하는 기능 (Supported Features)

|Attribute|Type|Description|
|---|---|---|
|switchChecked|Boolean|Sets the initial checked state of the switch.|
|switchTrackColorOn|Color|Track color when the switch is ON.|
|switchTrackColorOff|Color|Track color when the switch is OFF.|
|switchThumbColor|Color|Color of the thumb (circle).|
|switchThumbSize|Dimension|Diameter of the thumb. If not set, auto-calculated from track height.|
|switchThumbMargin|Dimension|Margin between the thumb and the track edge. Default is 2dp.|
|switchAnimDuration|Integer|Duration of the toggle animation in milliseconds. Default is 250ms.|
|switchTouchPadding|Dimension|Expands the touch area via TouchDelegate without changing the visual size.|

### 유의 사항 (Notice)

- `switchTouchPadding` 은 부모 View 에 `TouchDelegate` 를 설정하는 방식으로 동작합니다.  
부모 View 가 이미 `TouchDelegate` 를 사용하고 있는 경우 덮어쓰기가 발생할 수 있습니다.  
(switchTouchPadding works by setting a TouchDelegate on the parent View. If the parent already uses a TouchDelegate, it may be overwritten.)

- `switchThumbSize` 를 지정하지 않으면 track 높이 기준으로 thumb 크기가 자동 계산됩니다.  
(If switchThumbSize is not set, the thumb size is automatically calculated based on the track height.)

### 사용법 (How To)

- xml

```xml
<hmju.widget.view.CustomSwitchView
    android:id="@+id/vSwitch"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:switchChecked="false"
    app:switchTrackColorOn="#00A18E"
    app:switchTrackColorOff="#8E9292"
    app:switchThumbColor="#FFFFFF"
    app:switchThumbSize="12dp"
    app:switchThumbMargin="4dp"
    app:switchAnimDuration="400"
    app:switchTouchPadding="16dp" />
```

- kotlin

```kotlin
// 상태 변경 콜백
vSwitch.setOnCheckedChangeListener { isChecked ->
    if (isChecked) {
        // ON 처리
    } else {
        // OFF 처리
    }
}

// 코드에서 상태 변경
vSwitch.isChecked = true
```

### Gradle

```groovy
implementation 'com.github.sieunju.widget:view:$latestVersion'
```
