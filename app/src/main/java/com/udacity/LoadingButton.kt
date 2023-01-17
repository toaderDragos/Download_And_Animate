package com.udacity

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0

    private var valueAnimator = ValueAnimator()
    private var development = 0             // helps with the progress bar

    private val ovalSpace = RectF()         // for drawing the circle we implement a space in wich it will be defined

    private var animationColorOriginal = 0
    private var animationColorFinal = 0

    private val paintOriginal = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = getResources().getColor(R.color.colorPrimary)
    }

    private val paintProgress = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = getResources().getColor(R.color.colorPrimaryDark)
    }

    private val paintYellowCircle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = getResources().getColor(R.color.colorAccent)

    }

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, oldButtonState, newButtonState ->
        if (ButtonState.Loading == newButtonState){
            valueAnimator = ValueAnimator.ofInt(0, 400)
            // trying to make it fade in as a test
            valueAnimator.addUpdateListener {
//                alpha = it.animatedValue as Float
                development = it.animatedValue as Int
                invalidate()
            }
            valueAnimator.duration = 5000
            valueAnimator.repeatCount = 100
            valueAnimator.repeatMode = ValueAnimator.RESTART
            valueAnimator.start()

            // here I want the slider to progress quickly ( NOT TO JUMP) to the end once the download is finished
        } else if (ButtonState.Completed == newButtonState){
            valueAnimator.duration = 1000
            valueAnimator.repeatCount = 1

            android.os.Handler().postDelayed({
            }, 1000)
            valueAnimator.cancel()
            invalidate()
        }

    }

    // impossible to get the real colors here from attrs - program can't find them
    init {
        isClickable = true
        buttonState = ButtonState.Completed
        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            animationColorOriginal = getColor(R.styleable.ActionBar_height, 0)
            animationColorFinal = getColor(R.styleable.ActionBar_popupTheme, 0)
        }
    }

    // for the animation of the bar
    val rectF = Rect(0, 0, widthSize, heightSize)
    val processing = Rect(0, 0, widthSize * development / 400, heightSize)

    // the animation should accelerate fast when the download it's over ( at this moment it doesn't)
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (canvas != null) {
            canvas.drawRect(rectF, paintOriginal)
        }

        /**
         * value of 400 is trial tested so that it ends in the same time the circle with the progress bar -
         * it's the same value from here valueAnimator = ValueAnimator.ofInt(0, 400) above
         **/
        if (canvas != null && buttonState == ButtonState.Loading){

            canvas.drawRect(processing, paintProgress)
            // for the circle we set it's position and size - it only appears while downloading
            setSpace()
            // development is an animated ever- changing variable that should animate the circle
            canvas.drawArc(ovalSpace, 0f, development.toFloat(), true, paintYellowCircle)
        }
    }

    // this spece is for the Circle animation implementation
    // the width and height belong to the previous drawn rectangle - I centered on it and then moved the circle right.
    private fun setSpace() {
        val horizontalCenter = (width.div(2)).toFloat() + 250
        val verticalCenter = (height.div(2)).toFloat()
        val ovalSize = 60
        ovalSpace.set(
            horizontalCenter - ovalSize,
            verticalCenter - ovalSize,
            horizontalCenter + ovalSize,
            verticalCenter + ovalSize
        )
    }

    // setting the dimensions of the new view - see below for more info
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(MeasureSpec.getSize(w), heightMeasureSpec, 0)
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

    fun changeButtonStateinMAinActivity(currentState: ButtonState) {
        buttonState = currentState
    }

}

//onMeasure() is your opportunity to tell Android how big you want your custom view to be dependent the layout constraints provided by the parent; it is also your custom view's opportunity
// to learn what those layout constraints are (in case you want to behave differently in a match_parent situation than a wrap_content situation).
// These constraints are packaged up into the MeasureSpec values that are passed into the method. Here is a rough correlation of the mode values:
//• EXACTLY means the layout_width or layout_height value was set to a specific value. You should probably make your view this size. This can also get triggered when match_parent is used,
// to set the size exactly to the parent view (this is layout dependent in the framework).
//
//• AT_MOST typically means the layout_width or layout_height value was set to match_parent or wrap_content where a maximum size is needed
// (this is layout dependent in the framework), and the size of the parent dimension is the value. You should not be any larger than this size.
//
//• UNSPECIFIED typically means the layout_width or layout_height value was set to wrap_content with no restrictions. You can be whatever size you would like.
// Some layouts also use this callback to figure out your desired size before determine what specs to actually pass you again in a second measure request.
//
//The contract that exists with onMeasure() is that setMeasuredDimension() MUST be called at the end with the size you would like the view to be. This method is called
// by all the framework implementations, including the default implementation found in View, which is why it is safe to call super instead if that fits your use case.
//
//Granted, because the framework does apply a default implementation, it may not be necessary for you to override this method, but you may see clipping in cases where
// the view space is smaller than your content if you do not, and if you lay out your custom view with wrap_content in both directions, your view may not show up at all
// because the framework doesn't know how large it is!
//
//Generally, if you are overriding View and not another existing widget, it is probably a good idea to provide an implementation, even if it is as simple as something like this:
//
//@Override
//protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//
//    int desiredWidth = 100;
//    int desiredHeight = 100;
//
//    int widthMode = MeasureSpec.getMode(widthMeasureSpec);
//    int widthSize = MeasureSpec.getSize(widthMeasureSpec);
//    int heightMode = MeasureSpec.getMode(heightMeasureSpec);
//    int heightSize = MeasureSpec.getSize(heightMeasureSpec);
//
//    int width;
//    int height;
//
//    //Measure Width
//    if (widthMode == View.MeasureSpec.EXACTLY) {
//        //Must be this size
//        width = widthSize;
//    } else if (widthMode == View.MeasureSpec.AT_MOST) {
//        //Can't be bigger than...
//        width = Math.min(desiredWidth, widthSize);
//    } else {
//        //Be whatever you want
//        width = desiredWidth;
//    }
//
//    //Measure Height
//    if (heightMode == MeasureSpec.EXACTLY) {
//        //Must be this size
//        height = heightSize;
//    } else if (heightMode == MeasureSpec.AT_MOST) {
//        //Can't be bigger than...
//        height = Math.min(desiredHeight, heightSize);
//    } else {
//        //Be whatever you want
//        height = desiredHeight;
//    }
//
//    //MUST CALL THIS
//    setMeasuredDimension(width, height);
//}