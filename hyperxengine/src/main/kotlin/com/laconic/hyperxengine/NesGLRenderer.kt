package com.laconic.hyperxengine

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class NesGLRenderer(private val sprite: GLSprite) : GLSurfaceView.Renderer {
  override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
    // Set the background frame color
    GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
    val textureHandle = IntArray(1)
    GLES20.glGenTextures(1, textureHandle, 0)
    if (textureHandle[0] != 0) {
      sprite.setTexture(textureHandle[0])
    } else {
      throw RuntimeException("Cannot create GL texture")
    }
  }

  override fun onDrawFrame(unused: GL10) {
    sprite.draw()
  }

  override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
    // Adjust the viewport based on geometry changes,
    // such as screen rotation

    println("width: $width") //2103
    println("height: $height") //904
    //desired -> .9375
    //1580
    val newWidth = (height/.9375)//attempt to recreate proper pce aspect ratio
    GLES20.glViewport((width - newWidth.toInt())/2, 0, newWidth.toInt(), height)
  }
}