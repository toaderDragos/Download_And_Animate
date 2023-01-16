package com.udacity

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import java.util.logging.Handler
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0

    private var valueAnimator = ValueAnimator()
    private val pointPosition: PointF = PointF(0.0f, 0.0f)
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
        context.withStyledAttributes(attrs, R.styleable.LoadingButton){
            animationColorOriginal = getColor(R.styleable.ActionBar_height, 0)
            animationColorFinal = getColor(R.styleable.ActionBar_popupTheme, 0)
        }
    }

    // the animation should accelerate fast when the download it's over ( at this moment it doesn't)
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        // for the animation of the bar
        val rectF = Rect(0,0, widthSize, heightSize)
        if (canvas != null) {
            canvas.drawRect(rectF, paintOriginal)
        }

        /**
         * value of 400 is trial tested so that it ends in the same time the circle with the progress bar -
         * it's the same value from here valueAnimator = ValueAnimator.ofInt(0, 400) above
         **/
        if (canvas != null && buttonState == ButtonState.Loading){
            val processing = Rect(0,0, widthSize * development/ 400, heightSize)
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