package com.zp4rker.whatsthetime

import com.zp4rker.whatsthetime.http.request
import org.json.JSONObject
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.event.ComponentEvent
import java.awt.event.ComponentListener
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
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
    private val dateLabel = JLabel("Date", SwingConstants.CENTER)
    private val battLabel = JLabel("Battery is at XX%", SwingConstants.CENTER)

    init {
        val regularFont = Font.createFont(Font.PLAIN, MrWolf::class.java.getResourceAsStream("/Roboto-Regular.ttf"))
        val thinFont = Font.createFont(Font.PLAIN, MrWolf::class.java.getResourceAsStream("/Roboto-Thin.ttf"))

        frame.size = Dimension(700, 500)
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.setLocationRelativeTo(null)

        timeLabel.foreground = Color.WHITE
        timeLabel.font = thinFont.deriveFont(frame.width / 6F)

        arrayOf(dateLabel, battLabel).forEach {
            it.foreground = Color.WHITE
            it.font = regularFont.deriveFont(timeLabel.font.size2D / 7)
        }

        val box = Box.createHorizontalBox()
        box.add(Box.createHorizontalGlue())
        box.add(Box.createVerticalBox().apply {
            add(Box.createVerticalGlue())
            arrayOf(timeLabel, dateLabel, battLabel).forEach {
                add(Box.createHorizontalBox().also { box ->
                    box.add(Box.createHorizontalGlue())
                    box.add(it)
                    box.add(Box.createHorizontalGlue())
                })
            }
            add(Box.createVerticalGlue())
        })
        box.add(Box.createHorizontalGlue())

        panel.add(box)

        panel.background = Color.black

        frame.add(panel, BorderLayout.CENTER)
        frame.isVisible = true

        frame.addComponentListener(object : ComponentListener {
            override fun componentResized(e: ComponentEvent?) {
                timeLabel.font = thinFont.deriveFont(frame.width / 6F)

                arrayOf(dateLabel, battLabel).forEach {
                    it.font = regularFont.deriveFont(timeLabel.font.size2D / 7)
                }
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
        updateDate()
        updateBattery()
    }

    private fun updateTime() {
        val time = OffsetDateTime.now()
        timeLabel.text = "${time.hour.toString().padStart(2, '0')}  ${time.minute.toString().padStart(2, '0')}  ${time.second.toString().padStart(2, '0')}"
        Timer().schedule(time.until(time.plusSeconds(1), ChronoUnit.MILLIS)) {
            updateTime()
        }
    }

    private fun updateDate() {
        val url = "http://api.aladhan.com/v1/timingsByCity?city=Canberra&country=Australia&method=4&adjustment=-1"
        val data = JSONObject(request("GET", url)).getJSONObject("data")

        val hijri = data.getJSONObject("date").getJSONObject("hijri")
        val month = hijri.getJSONObject("month").getString("en")
        val day = hijri.getString("day").let {
            val n = it.toInt()
            "$n" + when {
                n.toString().takeLast(1) == "1" -> "st"
                n.toString().takeLast(1) == "2" -> "nd"
                n.toString().takeLast(1) == "3" -> "rd"
                else -> "th"
            }
        }
        val year = hijri.getString("year")

        val gregorian = data.getJSONObject("date").getJSONObject("gregorian")
        val weekdayEn = gregorian.getJSONObject("weekday").getString("en")

        val date = "$weekdayEn the $day of $month, $year"

        if (dateLabel.text != date) dateLabel.text = date
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