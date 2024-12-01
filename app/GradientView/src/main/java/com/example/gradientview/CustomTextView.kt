package com.example.customtextview

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import androidx.appcompat.widget.AppCompatTextView


@SuppressLint("Recycle")
class CustomTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private var gradientBgColor: IntArray = intArrayOf(Color.BLACK, Color.WHITE)
    private var gradientTextColor: IntArray = intArrayOf(Color.BLACK, Color.WHITE)
    private var isBgVertical: Boolean = false
    private var isTextVertical: Boolean = false
    private var isBorderVertical: Boolean = false
    private var textShader: Shader? = null
    private var borderRadius: Float = 0f
    private var borderColor: IntArray = intArrayOf(Color.BLACK, Color.WHITE)
    private var borderWidth: Float = 0f
    private var dashWidth: Float = 0f
    private var dashGap: Float = 0F
    private var dashPhase: Float = 0f

    init {
        val typedArray =
            context.obtainStyledAttributes(attrs, R.styleable.CustomTextView, defStyleAttr, 0)

        //Orientation
        val bgOrientation = typedArray.getInt(R.styleable.CustomTextView_bgOrientation, 0)
        val textOrientation = typedArray.getInt(R.styleable.CustomTextView_textOrientation, 0)
        val borderOrientation = typedArray.getInt(R.styleable.CustomTextView_borderOrientation, 0)

        isBgVertical = bgOrientation == 1
        isTextVertical = textOrientation == 1
        isBorderVertical = borderOrientation == 1

        //Background
        val gradientColorResId =
            typedArray.getResourceId(R.styleable.CustomTextView_gradientBgColors, 0)
        if (gradientColorResId != 0) {
            gradientBgColor = context.resources.getIntArray(gradientColorResId)
        }

        //Text
        val gradientTextColorResId =
            typedArray.getResourceId(R.styleable.CustomTextView_gradientTextColors, 0)
        if (gradientTextColorResId != 0) {
            gradientTextColor = context.resources.getIntArray(gradientTextColorResId)
        }

        //Border
        borderRadius = typedArray.getDimension(R.styleable.CustomTextView_cornerRadius, 0f)
        borderWidth = typedArray.getDimension(R.styleable.CustomTextView_strokeWidth, 0f)
        dashWidth = typedArray.getDimension(R.styleable.CustomTextView_dashWidth, 0f)
        dashGap = typedArray.getDimension(R.styleable.CustomTextView_dashGap, 0f)

        val borderColorResId =
            typedArray.getResourceId(R.styleable.CustomTextView_strokeColors, 0)
        if (borderColorResId != 0) {
            borderColor = context.resources.getIntArray(borderColorResId)
        }

        typedArray.recycle()

        applyBackground()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        applyTextGradient()
    }

    //Set TextView gradient background
    private fun applyBackground() {
        val gradientBackground = GradientDrawable().apply {
            orientation = if (isBgVertical) {
                GradientDrawable.Orientation.BOTTOM_TOP
            } else {
                GradientDrawable.Orientation.LEFT_RIGHT
            }
            colors = gradientBgColor
            cornerRadius = borderRadius - borderWidth
        }

        val transparentBg = GradientDrawable().apply {
            colors = intArrayOf(Color.WHITE, Color.WHITE)
            cornerRadius = borderRadius - borderWidth
        }

        val gradientBorder = object : GradientDrawable() {
            override fun draw(canvas: Canvas) {
                val paint = Paint().apply {
                    style = Paint.Style.STROKE
                    strokeWidth = borderWidth
                    isAntiAlias = true
                    pathEffect = DashPathEffect(floatArrayOf(dashWidth, dashGap), dashPhase)

                    // Apply gradient shader to the border
                    shader = LinearGradient(
                        0f, 0f,
                        if (isBorderVertical) 0f else bounds.width().toFloat(),
                        if (isBorderVertical) bounds.height().toFloat() else 0f,
                        borderColor, null, Shader.TileMode.CLAMP
                    )
                }

                val rect = RectF(
                    borderWidth / 2, borderWidth / 2,
                    bounds.width() - borderWidth / 2, bounds.height() - borderWidth / 2
                )

                canvas.drawRoundRect(rect, borderRadius, borderRadius, paint)
            }
        }

        val layerDrawable = LayerDrawable(
            arrayOf(gradientBorder, transparentBg, gradientBackground)
        )

        layerDrawable.setLayerInset(
            1,
            borderWidth.toInt(),
            borderWidth.toInt(),
            borderWidth.toInt(),
            borderWidth.toInt()
        )
        layerDrawable.setLayerInset(
            2,
            borderWidth.toInt(),
            borderWidth.toInt(),
            borderWidth.toInt(),
            borderWidth.toInt()
        )

        this.background = layerDrawable
    }

    //Prepare gradient shader for text
    private fun applyTextGradient() {
        if (width == 0 || height == 0) return

        textShader = if (isTextVertical) {
            LinearGradient(
                0f, 0f, 0f, height.toFloat(), // Vertical gradient
                gradientTextColor, null, Shader.TileMode.CLAMP
            )
        } else {
            LinearGradient(
                0f, 0f, width.toFloat(), 0f, // Horizontal gradient
                gradientTextColor, null, Shader.TileMode.CLAMP
            )
        }
    }

    override fun onDraw(canvas: Canvas) {
        textShader?.let { paint.shader = it }
        super.onDraw(canvas)
    }

    enum class Direction {
        FORWARD,
        BACKWARD
    }

    inner class Animation {
        private var animDuration: Long = 1000
        private var animationDirection: Int = 0

        fun setDirection(direction: Direction) {
            animationDirection = when (direction) {
                Direction.FORWARD -> 0
                else -> 1
            }
        }

        fun speed(duration: Long): Animation {
            animDuration = duration
            return this
        }

        fun setDashAnimation(): Animation {
            val animator: ValueAnimator? = if (animationDirection == 0) {
                ValueAnimator.ofFloat(dashWidth + dashGap, 0f)
            } else {
                ValueAnimator.ofFloat(0f, dashWidth + dashGap)
            }

            animator?.apply {
                duration = animDuration
                repeatCount = ValueAnimator.INFINITE
                interpolator = LinearInterpolator()

                addUpdateListener { animation ->
                    dashPhase = animation.animatedValue as Float
                    invalidate()
                }
            }
            animator?.start()
            return this
        }
    }
}