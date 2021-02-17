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
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
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

    private val salahLabel = JLabel("Fajr is in 5 hours", SwingConstants.CENTER)
    private val dateLabel = JLabel("Date", SwingConstants.CENTER)
    private val battLabel = JLabel("Battery is at XX%", SwingConstants.CENTER)

    private var ipLocation: IPLocation? = null
    private var adjustment: Int = -1

    init {
        val regularFont = Font.createFont(Font.PLAIN, MrWolf::class.java.getResourceAsStream("/Roboto-Regular.ttf"))
        val thinFont = Font.createFont(Font.PLAIN, MrWolf::class.java.getResourceAsStream("/Roboto-Thin.ttf"))

        frame.size = Dimension(700, 500)
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.setLocationRelativeTo(null)

        timeLabel.foreground = Color.WHITE
        timeLabel.font = thinFont.deriveFont(frame.width / 6F)

        arrayOf(salahLabel, dateLabel, battLabel).forEach {
            it.foreground = Color.WHITE
            it.font = regularFont.deriveFont(timeLabel.font.size2D / 7)
        }

        val box = Box.createHorizontalBox()
        box.add(Box.createHorizontalGlue())
        box.add(Box.createVerticalBox().apply {
            add(Box.createVerticalGlue())
            add(Box.createVerticalGlue())
            add(Box.createHorizontalBox().also { box ->
                box.add(Box.createHorizontalGlue())
                box.add(timeLabel)
                box.add(Box.createHorizontalGlue())
            })
            arrayOf(salahLabel, dateLabel, battLabel).forEach {
                add(Box.createHorizontalBox().also { box ->
                    box.add(Box.createHorizontalGlue())
                    box.add(it)
                    box.add(Box.createHorizontalGlue())
                })
            }
            add(Box.createVerticalGlue())
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

                arrayOf(salahLabel, dateLabel, battLabel).forEach {
                    it.font = regularFont.deriveFont(timeLabel.font.size2D / 7)
                }
            }

            override fun componentMoved(e: ComponentEvent?) {}
            override fun componentShown(e: ComponentEvent?) {}
            override fun componentHidden(e: ComponentEvent?) {}
        })

        dateLabel.addMouseListener(object : MouseListener {
            override fun mouseClicked(e: MouseEvent?) {
                adjustment = if (adjustment == 0) -1 else 0
                updateDate()
            }

            override fun mousePressed(e: MouseEvent?) {}
            override fun mouseReleased(e: MouseEvent?) {}
            override fun mouseEntered(e: MouseEvent?) {}
            override fun mouseExited(e: MouseEvent?) {}
        })

        updateTime()
        updateSalah()
        updateDate()
        updateBattery()
    }

    private fun updateTime() {
        val time = getTime()
        timeLabel.text = "${time.hour.toString().padStart(2, '0')}  ${time.minute.toString().padStart(2, '0')}  ${time.second.toString().padStart(2, '0')}"
        Timer().schedule(time.until(time.plusSeconds(1), ChronoUnit.MILLIS)) {
            getTime().let {
                if (it.second % 15 == 0) {
                    updateSalah()
                    updateBattery()
                } else if (it.hour == 0 && it.minute == 0) {
                    updateDate()
                }
            }
            updateTime()
        }
    }

    private fun updateSalah() {
        val now = getTime()

        if (SalahTime.Schedule.isEmpty() || now.dayOfMonth > SalahTime.Schedule.first().time.dayOfMonth) {
            SalahTime.Schedule.clear()

            val timings = getHijriData().getJSONObject("timings")
            for (key in timings.keySet().filter { !SalahTime.Ignored.contains(it) }) {
                val timeComponents = timings.getString(key).split(":")
                val time = getTime().withHour(timeComponents[0].toInt()).withMinute(timeComponents[1].toInt()).withSecond(0)
                SalahTime.Schedule.add(SalahTime(key, time))
            }

            SalahTime.Schedule.sortBy { it.time }
        }

        val text = if (SalahTime.Schedule.none { it.time < now }) {
            val nextSalah = SalahTime.Schedule.find { it.time > now } ?: return
            val nextDiff = now.until(nextSalah.time, ChronoUnit.MINUTES)
            "It's ${nextSalah.name} time in $nextDiff minute${if (nextDiff != 1L) "s" else ""}."
        } else if (SalahTime.Schedule.none { it.time > now }) {
            "It's Isha time."
        } else {
            val lastSalah = SalahTime.Schedule.last { it.time < now }
            val prevDiff = lastSalah.time.until(now, ChronoUnit.MINUTES)

            val nextSalah = SalahTime.Schedule.find { it.time > now } ?: return
            val nextDiff = now.until(nextSalah.time, ChronoUnit.MINUTES)

            if (prevDiff > SalahTime.Duration.valueOf(lastSalah.name).value) {
                "It's ${nextSalah.name} time in $nextDiff minute${if (nextDiff != 1L) "s" else ""}."
            } else {
                "It's ${lastSalah.name} time."
            }
        }

        if (salahLabel.text != text) salahLabel.text = text
    }

    private fun updateDate() {
        val data = getHijriData()

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
        val percentage = runCatching {
            Runtime.getRuntime().exec("pmset -g batt").inputStream.reader().readText().let {
                Regex("(\\d+)%").find(it)?.groupValues?.get(0) ?: "unknown"
            }
        }.getOrNull() ?: "unknown"

        val fullText = if (percentage != "unknown") "Battery is at ${percentage.padStart(2, '0')}" else ""

        if (battLabel.text != fullText) battLabel.text = fullText
    }

    private fun getHijriData(date: OffsetDateTime = getTime()): JSONObject {
        if (ipLocation == null) updateLocation()

        val dateFormat = DateTimeFormatter.ofPattern("dd-MM-YYYY")
        val url = "http://api.aladhan.com/v1/timingsByCity/${date.format(dateFormat)}"
        return JSONObject(request("GET", url, mapOf(
            "city" to ipLocation?.city,
            "country" to ipLocation?.country,
            "method" to "4",
            "adjustment" to "$adjustment"
        ))).getJSONObject("data")
    }

    private fun updateLocation() {
        ipLocation = IPLocation.search()
    }

    private fun getTime(): OffsetDateTime {
        if (ipLocation == null) updateLocation()

        return OffsetDateTime.now(ZoneId.of(ipLocation?.timezone))
    }

    companion object {
        @JvmStatic fun main(args: Array<String>) {
            MrWolf()
        }
    }

}