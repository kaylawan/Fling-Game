package edu.utap.fling

import android.annotation.SuppressLint
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.FlingAnimation
import kotlin.random.Random


class Fling(private val puck: View,
            private val border: Border,
            private val testing: Boolean
)  {
    private val puckMinX = border.minX().toFloat()
    private val puckMaxX = (border.maxX() - puck.width).toFloat()
    private val puckMinY= border.minY().toFloat()
    private val puckMaxY = (border.maxY() - puck.height).toFloat()
    private val friction = 3.0f
    private var goalBorder = Border.Type.T
    private lateinit var flingAnimationX: FlingAnimation
    private lateinit var flingAnimationY: FlingAnimation

    private fun placePuck() {
        if (testing) {
            puck.x = ((border.maxX() - border.minX()) / 2).toFloat()
            puck.y = ((border.maxY() - border.minY()) / 2).toFloat()
        } else {
            // XXX Write me
            puck.x = border.randomX(puck.width)
            puck.y = border.randomY(puck.height)
        }
        puck.visibility = View.VISIBLE
    }

    private fun success(goalAchieved: () -> Unit) {
        flingAnimationX.cancel()
        flingAnimationY.cancel()
        puck.visibility = View.INVISIBLE
        goalAchieved()
    }

    fun makeXFlingAnimation(initVelocity: Float,
                            goalAchieved: () -> Unit): FlingAnimation {
        return FlingAnimation(puck, DynamicAnimation.X)
            .setFriction(friction)
            .setStartVelocity(initVelocity)
            .setMinValue(puckMinX)
            .setMaxValue(puckMaxX)
            .addEndListener { animation, canceled, value, velocity ->
                if (puck.x == puckMinX || puck.x == puckMaxX) {
                    if ((goalBorder == Border.Type.S || goalBorder == Border.Type.E) && goalBorder()) {
                        success(goalAchieved);
                    } else {
                        makeXFlingAnimation(-velocity, goalAchieved).start();
                    }
                }
            }
    }


    fun makeYFlingAnimation(initVelocity: Float,
                            goalAchieved: () -> Unit): FlingAnimation {
        //Log.d("XXX", "Fling Y vel $initVelocity")
        return FlingAnimation(puck, DynamicAnimation.Y)
            .setFriction(friction)
            .setStartVelocity(initVelocity)
            .setMinValue(puckMinY)
            .setMaxValue(puckMaxY)
            .addEndListener { animation, canceled, value, velocity ->
                if (puck.y == puckMaxY || puck.y == puckMinY) {
                    if ((goalBorder == Border.Type.B || goalBorder == Border.Type.T) && goalBorder()) {
                        success(goalAchieved);
                    } else {
                        makeYFlingAnimation(-velocity, goalAchieved).start();
                    }
                }
            }
    }
    private fun goalBorder() : Boolean {
        if (goalBorder == Border.Type.T && puck.y == puckMinY) {
            return true
        } else if (goalBorder == Border.Type.S && puck.x == puckMinX) {
            return true
        } else if (goalBorder == Border.Type.B && puck.y == puckMaxY) {
            return true
        } else if (goalBorder == Border.Type.E && puck.x == puckMaxX) {
            return true
        }
        else{
            return false
        }
    }
    fun isPuckHittingGoalX(value: Float): Boolean {
        // sssuming the goal border's positions are directly at minX and maxX
        return (goalBorder == Border.Type.S && value <= puckMinX) || (goalBorder == Border.Type.E && value + puck.width >= puckMaxX)
    }

    fun isPuckHittingGoalY(value: Float): Boolean {
        // similar logic for the Y axis, considering the top and bottom as goal areas
        return (goalBorder == Border.Type.T && value <= puckMinY) || (goalBorder == Border.Type.B && value + puck.height >= puckMaxY)
    }


    @SuppressLint("ClickableViewAccessibility")
    fun listenPuck(goalAchieved: ()->Unit) {
        // A SimpleOnGestureListener notifies us when the user puts their
        // finger down, and when they edu.utap.edu.utap.fling.
        // Note that here we construct the listener object "on the fly"
        val gestureListener = object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean {
                return true
            }
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (puck.visibility == View.VISIBLE) {
                    flingAnimationX = makeXFlingAnimation(velocityX, goalAchieved)
                    flingAnimationY = makeYFlingAnimation(velocityY, goalAchieved)
                    flingAnimationX.start()
                    flingAnimationY.start()
                }
                return true
            }
        }

        val gestureDetector = GestureDetector(puck.context, gestureListener)
        // When Android senses that the puck is being touched, it will call this code
        // with a motionEvent object that describes the motion.  Our detector
        // will take sequences of motion events and send them to the gesture listener to
        // let us know what the user is doing.
        puck.setOnTouchListener { _, motionEvent ->
            gestureDetector.onTouchEvent(motionEvent)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun deactivatePuck() {
        puck.setOnTouchListener(null)
    }

    fun playRound(goalAchieved: () -> Unit) {
        border.resetBorderColors()
        goalBorder = border.nextGoal()
        placePuck()
        listenPuck(goalAchieved)
    }
}