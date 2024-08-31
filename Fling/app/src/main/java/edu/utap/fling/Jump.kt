package edu.utap.fling

import android.view.View

class Jump(private val puck: View,
           private val border: Border
) {
    private var currentPosition = 0
    private var isFirstStart = true

    private fun placePuck() {
        when (currentPosition) {
            0 -> {
                puck.x = border.minX().toFloat()
                puck.y = border.minY().toFloat()
            }
            1 -> {
                puck.x = border.maxX().toFloat() - puck.width
                puck.y = border.minY().toFloat()
            }
            2 -> {
                puck.x = border.maxX().toFloat() - puck.width
                puck.y = border.maxY().toFloat() - puck.height
            }
            3 -> {
                puck.x = border.minX().toFloat()
                puck.y = border.maxY().toFloat() - puck.height
            }
        }
    }
    fun start() {
        border.resetBorderColors()
        puck.visibility = View.VISIBLE
        puck.isClickable = true

        if (!isFirstStart) {
            placePuck()
        }
        else {
            placePuck()
            isFirstStart = false
            puck.setOnClickListener {
                currentPosition = (currentPosition + 1) % 4
                placePuck()
            }
        }
    }
    fun finish() {
        puck.visibility = View.INVISIBLE
        puck.isClickable = false
    }
}