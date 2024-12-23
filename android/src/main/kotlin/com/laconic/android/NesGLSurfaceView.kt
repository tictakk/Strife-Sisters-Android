package com.laconic.android

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import com.laconic.strifesdroid.GLSprite

class NesGLSurfaceView(
    context: Context,
    attributeSet: AttributeSet
) : GLSurfaceView(context, attributeSet) {
    lateinit var renderer: NesGLRenderer

  fun setSprite(sprite: GLSprite) {
      setEGLContextClientVersion(3)

    // Set the Renderer for drawing on the GLSurfaceView
    renderer = NesGLRenderer(sprite)
    setRenderer(renderer)
  }
}