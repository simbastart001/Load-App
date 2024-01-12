package com.udacity

/**     @DrStart:   */

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.animation.doOnEnd
import androidx.core.content.withStyledAttributes
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var buttonColor = 0
    private var loadingColor = 0
    private var circleColor = 0
    private var textColor = 0

    init {
        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            buttonColor = getColor(R.styleable.LoadingButton_myBackgroundColor, 0)
            loadingColor = getColor(R.styleable.LoadingButton_myLoadingColor, 0)
            circleColor = getColor(R.styleable.LoadingButton_myCirleColor, 0)
            textColor = getColor(R.styleable.LoadingButton_myTextColor, 0)
        }
    }

    /** colors*/
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = resources.getDimension(R.dimen.default_text_size)
        typeface = Typeface.create("", Typeface.BOLD)
    }

    private var paintButton = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = resources.getDimension(R.dimen.default_text_size)
        typeface = Typeface.create("", Typeface.BOLD)
        color = context.getColor(R.color.buttonColor)
    }

    private var paintLoadingButton = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = resources.getDimension(R.dimen.default_text_size)
        typeface = Typeface.create("", Typeface.BOLD)
        color = context.getColor(R.color.loadingColor)
    }

    private var paintCircleButton = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL_AND_STROKE
        color = context.getColor(R.color.circleColor)
    }

    private var paintTextColor = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = resources.getDimension(R.dimen.default_text_size)
        typeface = Typeface.create("", Typeface.BOLD)
        color = context.getColor(R.color.white)
    }

    private var widthSize = 0
    private var heightSize = 0
    private var progress: Float = 0f
    private var arcProgress: Float = 0f

    /**     @DrStart:  Ensure button animation must continue while the file is downloading */
    private val valueAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 6000
        // Set the animator to repeat indefinitely
        repeatCount = ValueAnimator.INFINITE
        addUpdateListener { animation ->
            progress = widthSize * animation.animatedValue as Float
            arcProgress = 360 * animation.animatedValue as Float
            invalidate()
        }
        doOnEnd {
            progress = 0f
            arcProgress = 0f
            invalidate()
        }
    }

    var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { _, _, new ->
        when (new) {
            ButtonState.Loading -> {
                valueAnimator.start()
            }

            ButtonState.Completed -> {
                valueAnimator.cancel()
                progress = 0f
                arcProgress = 0f
            }

            else -> Unit
        }
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // TODO @DrStart:      Draw the button with the base color
        canvas.drawRect(0f, 0f, widthSize.toFloat(), heightSize.toFloat(), paintButton)

        // TODO @DrStart:      Draw the loading portion
        if (buttonState == ButtonState.Loading) {
            canvas.drawRect(0f, 0f, progress, heightSize.toFloat(), paintLoadingButton)
        }

        // TODO @DrStart:      Draw the text
        val buttonText =
            resources.getString(if (buttonState == ButtonState.Completed) R.string.download else R.string.loading_text)
        canvas.drawText(
            buttonText,
            widthSize / 2f,
            (heightSize / 2f) - (paint.descent() + paint.ascent()) / 2f,
            paintTextColor
        )

        // TODO @DrStart:      Draw the circle
        if (buttonState == ButtonState.Loading) {
            val left = widthSize - 75f // TODO @DrStart:      Adjusted to place circle to the right
            val top = (heightSize - 50f) / 2f // TODO @DrStart:      Center in the button vertically
            val diameter = 50f
            val oval = RectF(left, top, left + diameter, top + diameter)
            canvas.drawArc(oval, 0f, arcProgress, true, paintCircleButton)
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

    fun setLoading(isLoading: Boolean) {
        buttonState = if (isLoading) ButtonState.Loading else ButtonState.Completed
    }

    /**     @DrStart:   Method to reset button state and progress on download failure */
    fun onDownloadFail() {
        buttonState = ButtonState.Completed
        progress = 0f
        arcProgress = 0f
        valueAnimator.cancel()
        invalidate()
    }
}
