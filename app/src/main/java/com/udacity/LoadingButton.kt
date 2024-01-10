package com.udacity

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import androidx.core.animation.doOnEnd
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var buttonColor = Color.BLUE
    private var loadingColor = Color.GRAY
    private var circleColor = Color.YELLOW
    private var textColor = Color.WHITE

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = resources.getDimension(R.dimen.default_text_size)
        typeface = Typeface.create("", Typeface.BOLD)
    }

    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL_AND_STROKE
        color = circleColor
    }

    private var widthSize = 0
    private var heightSize = 0
    private var progress: Float = 0f
    private var arcProgress: Float = 0f

    var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { _, _, new ->
        when (new) {
            ButtonState.Loading -> {
                valueAnimator.start()
            }

            ButtonState.Completed -> {
                valueAnimator.cancel()
            }

            else -> Unit
        }
    }

    private val valueAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 3000
        addUpdateListener { animation ->
            progress = widthSize * animation.animatedValue as Float
            arcProgress = 360 * animation.animatedValue as Float
            invalidate()
        }
        doOnEnd {
            buttonState = ButtonState.Completed
            progress = 0f
            arcProgress = 0f
            invalidate()
        }
        repeatMode = ValueAnimator.RESTART
        repeatCount = ValueAnimator.INFINITE
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // TODO @DrStart:    Draw the button with the base color
        paint.color = buttonColor
        canvas.drawRect(0f, 0f, widthSize.toFloat(), heightSize.toFloat(), paint)

        // Draw the button with the loading color
        paint.color = loadingColor
        canvas.drawRect(0f, 0f, progress, heightSize.toFloat(), paint)

        // TODO @DrStart:    Draw the text
        paint.color = textColor
        val buttonText = when (buttonState) {
            ButtonState.Completed -> resources.getString(R.string.download)
            ButtonState.Loading -> resources.getString(R.string.loading_text)
            else -> ""
        }
        canvas.drawText(
            buttonText,
            widthSize / 2f,
            (heightSize / 2f) - (paint.descent() + paint.ascent()) / 2f,
            paint
        )


        // TODO @DrStart:    Draw the circle
        if (buttonState == ButtonState.Loading) {
            val left =
                widthSize / 2f + resources.getDimension(R.dimen.default_text_size) + 60f // TODO @DrStart:   Adjust the value to position the circle further from the text
            val top = heightSize / 2f - 15f
            val diameter = 30f
            val oval = RectF(left, top, left + diameter, top + diameter)
            canvas.drawArc(oval, 0f, arcProgress, true, circlePaint)
        }

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        widthSize = w
        heightSize = h
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minWidth: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minWidth, widthMeasureSpec, 1)

        val minHeight: Int = paddingTop + paddingBottom + suggestedMinimumHeight
        val h: Int = resolveSizeAndState(minHeight, heightMeasureSpec, 0)

        setMeasuredDimension(w, h)
    }

    //    TODO @DrStart:   method to handle state of our customButton
    fun setLoading(isLoading: Boolean) {
        buttonState = if (isLoading) ButtonState.Loading else ButtonState.Completed
    }
}

