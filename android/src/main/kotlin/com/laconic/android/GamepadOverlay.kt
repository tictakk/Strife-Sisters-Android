package com.laconic.android

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.laconic.strifesdroid.R
import com.laconic.strifesdroid.app.MainActivity
import com.laconic.strifesdroid.databinding.GamepadOverlayBinding
import com.laconic.util.RecyclerAdapter
import io.github.controlwear.virtual.joystick.android.JoystickView

@SuppressLint("ClickableViewAccessibility")
class GamepadOverlay(private val ctx: Context, attrs: AttributeSet) : LinearLayout(ctx,attrs) {
    private val runBtn by lazy { findViewById<AppCompatButton>(R.id.runBtn) }
    private val selBtn by lazy { findViewById<AppCompatButton>(R.id.selBtn) }
    private val iiBtn by lazy { findViewById<FloatingActionButton>(R.id.iiBtn) }
    private val iBtn by lazy { findViewById<FloatingActionButton>(R.id.iBtn) }
    private val joystick by lazy { findViewById<JoystickView>(R.id.dpadBtn) }
    private val settingsBtn by lazy { findViewById<AppCompatImageButton>(R.id.settingsBtn) }
    private val dropDwn by lazy { findViewById<RecyclerView>(R.id.dropDown) }
    lateinit var activity: MainActivity

    external fun pressButton(button: Int)
    external fun releaseButton(button: Int)

    enum class Pad(i: Int){
        BUTTON_UP(0x1), BUTTON_DOWN(0x2), BUTTON_LEFT(0x3), BUTTON_RIGHT(0x4),
        BUTTON_SELECT(0x4), BUTTON_RUN(0x5), BUTTON_I(0x6), BUTTON_II(0x7)
    }

    init{
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.View,
            0, 0).apply {
        }

        inflate(context,R.layout.gamepad_overlay,this)

//        settingsBtn.setOnTouchListener{ v, e ->
//            if(e.action == MotionEvent.ACTION_UP) {
//                activity.toggleConsoleState()
//            }
//            true
//        }
        val onButtonTouched = { b: Pad ->
            View.OnTouchListener { _, e ->
                if (false) {
                    false
                } else {
                    when (e.action) {
                        MotionEvent.ACTION_DOWN -> pressButton(b.ordinal)//director.controller1.onButtonDown(b)
                        MotionEvent.ACTION_UP -> releaseButton(b.ordinal)//director.controller1.onButtonUp(b)
                    }
                    true
                }
            }
        }

        settingsBtn.setOnClickListener{
//            println("drop down: "+dropDwn.visibility)
            activity.toggleConsoleState()
            if(dropDwn.visibility == View.GONE){
                dropDwn.visibility = View.VISIBLE
            }else if(dropDwn.visibility == View.VISIBLE){
                dropDwn.visibility = View.GONE
            }
        }
        dropDwn.layoutManager = LinearLayoutManager(this.ctx)
        dropDwn.adapter = RecyclerAdapter(resources.getStringArray(R.array.settings_options))

        dropDwn.setOnTouchListener{ v, e ->
            if(e.action == MotionEvent.ACTION_UP) {
                v.visibility = View.GONE
                activity.toggleConsoleState()
                //will need to implement the correct functions for each menu option
                //save, load, restart, exit
            }
            true
        }

        joystick.setOnMoveListener(JoystickView.OnMoveListener{ a, s ->
            releaseButton(Pad.BUTTON_LEFT.ordinal);
            releaseButton(Pad.BUTTON_RIGHT.ordinal);
            releaseButton(Pad.BUTTON_UP.ordinal);
            releaseButton(Pad.BUTTON_DOWN.ordinal);
            if(s > 30){//need to find the correct way to set the keystate instead of doing it by every bit every loop
                when(a){
                    in 66..100 -> pressButton(Pad.BUTTON_UP.ordinal)
                    in 25..65 -> {
                        pressButton(Pad.BUTTON_UP.ordinal)
                        pressButton(Pad.BUTTON_RIGHT.ordinal)
                    }
                    in 0..24 -> pressButton(Pad.BUTTON_RIGHT.ordinal)
                    in 345..360 -> pressButton(Pad.BUTTON_RIGHT.ordinal)
                    in 301..345 -> {
                        pressButton(Pad.BUTTON_RIGHT.ordinal)
                        pressButton(Pad.BUTTON_DOWN.ordinal)
                    }
                    in 245..300 -> pressButton(Pad.BUTTON_DOWN.ordinal)
                    in 205..246 -> {
                        pressButton(Pad.BUTTON_DOWN.ordinal)
                        pressButton(Pad.BUTTON_LEFT.ordinal)
                    }
                    in 165..204 -> pressButton(Pad.BUTTON_LEFT.ordinal)
                    in 120..164 -> {
                        pressButton(Pad.BUTTON_LEFT.ordinal)
                        pressButton(Pad.BUTTON_UP.ordinal)
                    }
                    in 99..119 -> pressButton(Pad.BUTTON_UP.ordinal)
                }
            }
        })
        iBtn.setOnTouchListener(onButtonTouched(Pad.BUTTON_I))
        iiBtn.setOnTouchListener(onButtonTouched(Pad.BUTTON_II))
        selBtn.setOnTouchListener(onButtonTouched(Pad.BUTTON_SELECT))
        runBtn.setOnTouchListener(onButtonTouched(Pad.BUTTON_RUN))
        setOverlayAlpha(.65f)
    }

    fun attachActivity(actv: MainActivity){
        activity = actv
    }

    fun setOverlayAlpha(alpha: Float){
        runBtn.alpha = alpha
        iBtn.alpha = alpha
        iiBtn.alpha = alpha
        selBtn.alpha = alpha
        joystick.alpha = alpha
        settingsBtn.alpha = alpha
    }
}