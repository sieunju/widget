package com.hmju.visual

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
	@Test
	fun useAppContext() {
		// Context of the app under test.
		val appContext = InstrumentationRegistry.getInstrumentation().targetContext
		assertEquals("com.hmju.visual", appContext.packageName)
	}

	@Test
	fun testBlur() {
// Blur 처리..
//		if (drawable is BitmapDrawable) {
//			(drawable as BitmapDrawable).run {
//				GlobalScope.launch(Dispatchers.IO) {
//					FlexibleImageView.LogD("Bitmap Size ${bitmap.width}  ${bitmap.height} ${stateItem.startScale}")
//					FlexibleImageView.LogD("Image Width ${stateItem.imgWidth}  ${stateItem.imgHeight}")
//					val resizeWidth = (stateItem.imgWidth * stateItem.startScale).toInt()
//					val resizeHeight = (stateItem.imgHeight * stateItem.startScale).toInt()
//					FlexibleImageView.LogD("Resize Width $resizeWidth  $resizeHeight")
//
//					val bgBitmap = Bitmap.createBitmap(bitmap)
//
//					val rs = RenderScript.create(context)
//					val input = Allocation.createFromBitmap(rs, bgBitmap)
//					val output = Allocation.createTyped(rs, input.type)
//					ScriptIntrinsicBlur.create(rs, Element.U8_4(rs)).apply {
//						setRadius(25F)
//						setInput(input)
//						forEach(output)
//					}
//					output.copyTo(bgBitmap)
//
//					// Callback 은 Ui Thread 상태로 전달
//					withContext(Dispatchers.Main) {
//						callback(bgBitmap)
//					}
//				}
//			}
//		}
	}
}