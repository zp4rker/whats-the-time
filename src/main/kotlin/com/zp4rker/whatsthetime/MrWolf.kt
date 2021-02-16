package com.zp4rker.whatsthetime

import java.awt.*
import java.awt.event.ComponentEvent
import java.awt.event.ComponentListener
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.image.BufferedImage
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import java.util.Timer
import java.util.concurrent.TimeUnit
import javax.swing.*
import kotlin.concurrent.schedule

/**
 * @author zp4rker
 */
class MrWolf {

    private val frame = JFrame("What's the time?")
    private val panel = JPanel(BorderLayout())
    private val timeLabel = JLabel("00 00 00", SwingConstants.CENTER)
    private val battLabel = JLabel("Battery is at XX%", SwingConstants.CENTER)

    init {
        frame.size = Dimension(700, 500)
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.setLocationRelativeTo(null)

        timeLabel.foreground = Color.WHITE
        val thinFont = Font.createFont(Font.PLAIN, MrWolf::class.java.getResourceAsStream("/Roboto-Thin.ttf"))
        timeLabel.font = thinFont.deriveFont(frame.width / 6F)

        battLabel.foreground = Color.WHITE
        val regularFont = Font.createFont(Font.PLAIN, MrWolf::class.java.getResourceAsStream("/Roboto-Regular.ttf"))
        battLabel.font = regularFont.deriveFont(timeLabel.font.size2D / 7)

        val box = Box.createHorizontalBox()
        box.add(Box.createHorizontalGlue())
        box.add(Box.createVerticalBox().apply {
            add(Box.createVerticalGlue())
            add(timeLabel)
            add(Box.createHorizontalBox().also {
                it.add(Box.createHorizontalGlue())
                it.add(battLabel)
                it.add(Box.createHorizontalGlue())
            })
            add(Box.createVerticalGlue())
        })
        box.add(Box.createHorizontalGlue())

        panel.add(box)

        panel.background = Color.black

        frame.add(panel, BorderLayout.CENTER)
        frame.isVisible = true

        val blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB), Point(0, 0), "blank cursor")
        frame.cursor = blankCursor

        frame.addComponentListener(object : ComponentListener {
            override fun componentResized(e: ComponentEvent?) {
                timeLabel.font = thinFont.deriveFont(frame.width / 6F)

                battLabel.font = regularFont.deriveFont(timeLabel.font.size2D / 7)
            }

            override fun componentMoved(e: ComponentEvent?) {}
            override fun componentShown(e: ComponentEvent?) {}
            override fun componentHidden(e: ComponentEvent?) {}
        })

        frame.addMouseListener(object : MouseListener {
            override fun mouseClicked(e: MouseEvent?) {
                updateTime()
                updateBattery()
            }

            override fun mousePressed(e: MouseEvent?) {}
            override fun mouseReleased(e: MouseEvent?) {}
            override fun mouseEntered(e: MouseEvent?) {}
            override fun mouseExited(e: MouseEvent?) {}
        })

        updateTime()
        updateBattery()
    }

    private fun updateTime() {
        val time = OffsetDateTime.now()
        timeLabel.text = "${time.hour.toString().padStart(2, '0')}  ${time.minute.toString().padStart(2, '0')}  ${time.second.toString().padStart(2, '0')}"
        Timer().schedule(time.until(time.plusSeconds(1), ChronoUnit.MILLIS)) {
            updateTime()
        }
    }

    private fun updateBattery() {
        val battRaw = Runtime.getRuntime().exec("pmset -g batt").inputStream.reader().readText()
        val percentage = Regex("(\\d+)%").find(battRaw)?.groupValues?.get(0) ?: "unknown"

        val fullText = "Battery is at ${percentage.padStart(2, '0')}"

        if (battLabel.text != fullText) battLabel.text = fullText

        Timer().schedule(TimeUnit.SECONDS.toMillis(30)) {
            updateBattery()
        }
    }

    companion object {
        @JvmStatic fun main(args: Array<String>) {
            MrWolf()
        }
    }

}