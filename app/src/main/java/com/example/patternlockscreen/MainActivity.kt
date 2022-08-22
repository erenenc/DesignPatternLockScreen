package com.example.patternlockscreen

import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.core.content.getSystemService
import com.example.patternlockscreen.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    private val patternView by lazy { binding.patternView }

    private val testPattern1 = "abcdefghi"
    private val testPattern2 = "cfie"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // todo dont forgett to request vibration in manifest

        patternView.onPatternChanged = {
            binding.textViewPattern.text = it
            binding.textViewPattern.setTextColor(Color.BLACK)

            vibrate()
        }

        patternView.onCheckPattern = {
            binding.textViewPattern.text = it
            binding.textViewPattern.setTextColor(
                when(it) {
                    testPattern1, testPattern2 -> {
                        Toast.makeText(this, "success", Toast.LENGTH_LONG).show()
                        Color.GREEN
                    }
                    else -> {
                        Toast.makeText(this, "try again", Toast.LENGTH_LONG).show()
                        Color.RED
                    }
                }
            )
        }
    }

    private fun vibrate() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            vibrator.let {
                if (it.hasVibrator()) {
                    it.vibrate(70)

                }
            }
        } else {
            val effect = VibrationEffect.createOneShot(70, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.vibrate(effect)
        }
    }
}












