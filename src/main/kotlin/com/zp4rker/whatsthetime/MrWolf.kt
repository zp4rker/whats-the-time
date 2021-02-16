package com.zp4rker.whatsthetime

import java.awt.*
import java.awt.event.ComponentEvent
import java.awt.event.ComponentListener
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.Timer
import javax.swing.*
import kotlin.concurrent.schedule

/**
 * @author zp4rker
 */
class MrWolf {

    private val frame = JFrame("What's the time?")
    private val panel = JPanel(BorderLayout())
    private val timeLabel = JLabel("00 00 00", SwingConstants.CENTER)

    init {
        frame.size = Dimension(700, 500)
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.setLocationRelativeTo(null)

        timeLabel.foreground = Color.WHITE
        val customFont = Font.createFont(Font.PLAIN, MrWolf::class.java.getResourceAsStream("/Roboto-Thin.ttf"))
        timeLabel.font = customFont.deriveFont(frame.width / 6F)

        panel.add(timeLabel)
        panel.background = Color.BLACK

        frame.add(panel, BorderLayout.CENTER)
        frame.isVisible = true

        frame.addComponentListener(object : ComponentListener {
            override fun componentResized(e: ComponentEvent?) {
                timeLabel.font = customFont.deriveFont(frame.width / 6F)
            }

            override fun componentMoved(e: ComponentEvent?) {}
            override fun componentShown(e: ComponentEvent?) {}
            override fun componentHidden(e: ComponentEvent?) {}
        })

        setTime()
    }

    private fun setTime() {
        val time = OffsetDateTime.now()
        timeLabel.text = "${time.hour.toString().padStart(2, '0')}  ${time.minute.toString().padStart(2, '0')}  ${time.second.toString().padStart(2, '0')}"
        Timer().schedule(time.until(time.plusSeconds(1), ChronoUnit.MILLIS)) {
            setTime()
        }
    }

    companion object {
        @JvmStatic fun main(args: Array<String>) {
            MrWolf()
        }
    }

}