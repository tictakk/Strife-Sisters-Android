package com.felipecsl.knes

import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLES30.*
import com.laconic.pcemulator.Console
import com.laconic.pcemulator.pce.PCEngine
import java.lang.annotation.Native
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer

class GLSprite {
  private var context: RenderContext? = null
  private var texture: Int? = null

  var director: Director? = null
  var pce: PCEngine? = null
  var console: Console? = null

//  var screen: IntArray = IntArray(256*240)
  var screen: IntArray = IntArray(IMG_HEIGHT* IMG_WIDTH)
  data class RenderContext(
      val shaderProgram: Int = 0,
      val texSamplerHandle: Int = 0,
      val texCoordHandle: Int = 0,
      val posCoordHandle: Int = 0,
      val texVertices: FloatBuffer? = null,
      val posVertices: FloatBuffer? = null
  )

  fun updateScreen(image: IntArray){
    screen = image;
  }

  fun setTexture(texture: Int) {
    this.texture = texture
  }

  fun draw() {
//    if (director != null) {

    if(console != null) {
//      val image = getVideoBuffer()
      getVideoBuffer()
      createProgramIfNeeded()
      updateTexture()
//      if(screen[7550]!= 0){
//        println(screen[7550])
//      }
//      val image = console!!.getVideoBuffer()
//      println("creating program")
//      getVideoBuffer()
//      val image = director!!.videoBuffer()
//      createProgramIfNeeded()
//      updateTexture(image)
//      }
    }
  }

  external fun getVideoBuffer(): Unit

//  external fun drawScreenOnce(): Unit
//  native fun drawScreenOnce(img: IntArray){
//    updateTexture(img)
//  }

  private fun processImageData(image: IntArray): IntArray{
    return image.map { pixel ->
      (((pixel and 0b111000000) shr 6) shl 13) + (((pixel and 0b111000) shr 3) shl 5) + ((pixel and 0b111) shl 21)
    }.toIntArray()
  }

  private fun createProgramIfNeeded(): Unit {
    if (context == null) {
      val vertexShader = loadShader(GL_VERTEX_SHADER, VERTEX_SHADER)
      if (vertexShader == 0) {
        throw RuntimeException("Failed to create vertex shader")
      }
      val pixelShader = loadShader(GL_FRAGMENT_SHADER, FRAGMENT_SHADER)
      if (pixelShader == 0) {
        throw RuntimeException("Failed to create pixel shader")
      }
      val program = glCreateProgram()
      if (program != 0) {
        glAttachShader(program, vertexShader)
        glAttachShader(program, pixelShader)
        glLinkProgram(program)
        val linkStatus = IntArray(1)
        glGetProgramiv(program, GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] != GL_TRUE) {
          val info = glGetProgramInfoLog(program)
          glDeleteProgram(program)
          throw RuntimeException("Could not link program: $info")
        }
      }
      // Bind attributes and uniforms
      context = RenderContext(
          texSamplerHandle = glGetUniformLocation(program, "tex_sampler"),
          texCoordHandle = glGetAttribLocation(program, "a_texcoord"),
          posCoordHandle = glGetAttribLocation(program, "a_position"),
          texVertices = createVerticesBuffer(TEX_VERTICES),
          posVertices = createVerticesBuffer(POS_VERTICES),
          shaderProgram = program
      )

      val context = context!!
      glBindTexture(GL_TEXTURE_2D, texture!!)
      glTexImage2D(GL_TEXTURE_2D, 0, GLES20.GL_RGBA, IMG_WIDTH, IMG_HEIGHT, 0,
        GLES20.GL_RGBA, GL_UNSIGNED_BYTE, null)

      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)

      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)

      // Use our shader program
      glUseProgram(context.shaderProgram)
      // Disable blending
      glDisable(GL_BLEND)
      // Set the vertex attributes
      glVertexAttribPointer(context.texCoordHandle, 2, GL_FLOAT, false, 0, context.texVertices)
      glEnableVertexAttribArray(context.texCoordHandle)
      glVertexAttribPointer(context.posCoordHandle, 2, GL_FLOAT, false, 0, context.posVertices)
      glEnableVertexAttribArray(context.posCoordHandle)
    }
  }

//  private fun updateTexture(image: IntArray) {
//    glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, IMG_WIDTH, IMG_HEIGHT,
//      GLES11Ext.GL_BGRA, GL_UNSIGNED_BYTE, IntBuffer.wrap(image))
//    // Draw!
//    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4)
//  }

  fun updateTexture() {
    glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, IMG_WIDTH, IMG_HEIGHT,
      GLES20.GL_RGBA, GL_UNSIGNED_BYTE, IntBuffer.wrap(processImageData(screen)))
    // Draw!
    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4)
  }

  private fun createVerticesBuffer(vertices: FloatArray): FloatBuffer {
    if (vertices.size != 8) {
      throw RuntimeException("Number of vertices should be four.")
    }
    val buffer = ByteBuffer.allocateDirect(vertices.size * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
    buffer.put(vertices).position(0)
    return buffer
  }

  private fun loadShader(shaderType: Int, source: String): Int {
    val shader = glCreateShader(shaderType)
    if (shader != 0) {
      glShaderSource(shader, source)
      glCompileShader(shader)
      val compiled = IntArray(1)
      glGetShaderiv(shader, GL_COMPILE_STATUS, compiled, 0)
      if (compiled[0] == 0) {
        val info = glGetShaderInfoLog(shader)
        glDeleteShader(shader)
        throw RuntimeException("Could not compile shader $shaderType:$info")
      }
    }
    return shader
  }

  companion object {
    //This is the input shader. Data goes in here and we assign data places?
    private const val VERTEX_SHADER =
      "attribute vec4 a_position;\n" +
          "attribute vec2 a_texcoord;\n" +
          "varying vec2 v_texcoord;\n" +
          "void main() {\n" +
          "  gl_Position = a_position;\n" +
          "  v_texcoord = a_texcoord;\n" +
          "}\n"
    //This is the output shader where we do stuff with that data
    private const val FRAGMENT_SHADER =
      "precision mediump float;\n" +
          "uniform sampler2D tex_sampler;\n" +
          "varying vec2 v_texcoord;\n" +
          "void main() {\n" +
          "  gl_FragColor = texture2D(tex_sampler, v_texcoord);\n" +
          "}\n"
    private const val IMG_WIDTH = 256
    private const val IMG_HEIGHT = 240
//    private const val IMG_WIDTH = 682
//    private const val IMG_HEIGHT = 242
    private val TEX_VERTICES = floatArrayOf(0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f)
    private val POS_VERTICES = floatArrayOf(-1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f)
  }
}
