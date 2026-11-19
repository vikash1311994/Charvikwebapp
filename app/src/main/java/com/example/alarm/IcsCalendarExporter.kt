package com.example.alarm

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.model.CareAlarm
import com.example.data.model.CharvikKnowledge
import com.example.data.model.VaccineRecord
import com.example.data.model.VaccineStatus
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object IcsCalendarExporter {

  private fun escapeIcs(raw: String): String = raw
    .replace("\\", "\\\\")
    .replace(";", "\\;")
    .replace(",", "\\,")
    .replace("\n", "\\n")

  fun buildIcsContent(
    vaccines: List<VaccineRecord>,
    alarms: List<CareAlarm>,
  ): String {
    val stampFmt = SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US).apply {
      timeZone = TimeZone.getTimeZone("UTC")
    }
    val dtStamp = stampFmt.format(Date())
    val todayYmd = CharvikKnowledge.todayDateKey().replace("-", "")

    val sb = StringBuilder()
    sb.appendLine("BEGIN:VCALENDAR")
    sb.appendLine("VERSION:2.0")
    sb.appendLine("PRODID:-//Charvik Newborn Care//Android//EN")
    sb.appendLine("CALSCALE:GREGORIAN")
    sb.appendLine("X-WR-CALNAME:Charvik Care & Apollo Vaccines")

    vaccines.filter { it.status != VaccineStatus.DONE.name }.forEach { v ->
      val ymd = v.dueDateIso.replace("-", "")
      val purpose = CharvikKnowledge.apolloVaccineSchedule
        .firstOrNull { it.code == v.vaccineCode }?.clinicalPurpose ?: "Apollo Hospitals IAP vaccine"
      sb.appendLine("BEGIN:VEVENT")
      sb.appendLine("UID:charvik-vax-${v.vaccineCode}@charvik.local")
      sb.appendLine("DTSTAMP:$dtStamp")
      sb.appendLine("DTSTART:${ymd}T090000")
      sb.appendLine("DTEND:${ymd}T093000")
      sb.appendLine("SUMMARY:${escapeIcs("Charvik Vaccine: ${v.name} (${v.stageLabel})")}")
      sb.appendLine("DESCRIPTION:${escapeIcs("Apollo Hospitals / IAP Schedule — $purpose. DOB: Oct 26, 2026.")}")
      sb.appendLine("BEGIN:VALARM")
      sb.appendLine("TRIGGER:-P1D")
      sb.appendLine("ACTION:DISPLAY")
      sb.appendLine("DESCRIPTION:${escapeIcs("Tomorrow: ${v.name} for Charvik at Apollo Hospitals")}")
      sb.appendLine("END:VALARM")
      sb.appendLine("BEGIN:VALARM")
      sb.appendLine("TRIGGER:PT0M")
      sb.appendLine("ACTION:DISPLAY")
      sb.appendLine("DESCRIPTION:${escapeIcs("Today 9 AM: ${v.name} (${v.stageLabel}) for Charvik")}")
      sb.appendLine("END:VALARM")
      sb.appendLine("END:VEVENT")
    }

    alarms.filter { it.enabled }.forEach { a ->
      val hh = String.format(Locale.US, "%02d", a.hour.toInt().coerceIn(0, 23))
      val mm = String.format(Locale.US, "%02d", a.minute.toInt().coerceIn(0, 59))
      val totalEndMin = a.hour.toInt() * 60 + a.minute.toInt() + 15
      val endH = String.format(Locale.US, "%02d", (totalEndMin / 60) % 24)
      val endM = String.format(Locale.US, "%02d", totalEndMin % 60)
      sb.appendLine("BEGIN:VEVENT")
      sb.appendLine("UID:charvik-alarm-${a.id}@charvik.local")
      sb.appendLine("DTSTAMP:$dtStamp")
      sb.appendLine("DTSTART:${todayYmd}T${hh}${mm}00")
      sb.appendLine("DTEND:${todayYmd}T${endH}${endM}00")
      if (a.repeatDaily) {
        sb.appendLine("RRULE:FREQ=DAILY")
      }
      sb.appendLine("SUMMARY:${escapeIcs("Charvik: ${a.label}")}")
      sb.appendLine("DESCRIPTION:${escapeIcs("Care reminder exported from Charvik.")}")
      sb.appendLine("BEGIN:VALARM")
      sb.appendLine("TRIGGER:PT0M")
      sb.appendLine("ACTION:DISPLAY")
      sb.appendLine("DESCRIPTION:${escapeIcs("Charvik Reminder: ${a.label}")}")
      sb.appendLine("END:VALARM")
      sb.appendLine("END:VEVENT")
    }

    sb.appendLine("END:VCALENDAR")
    return sb.toString().replace("\n", "\r\n")
  }

  fun exportAndShare(
    context: Context,
    vaccines: List<VaccineRecord>,
    alarms: List<CareAlarm>,
  ): Result<Unit> = runCatching {
    val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
    val icsFile = File(exportDir, "Charvik-Care-Apollo-Schedule.ics")
    icsFile.writeText(buildIcsContent(vaccines, alarms), Charsets.UTF_8)

    val uri = FileProvider.getUriForFile(
      context,
      "${context.packageName}.fileprovider",
      icsFile,
    )
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
      type = "text/calendar"
      putExtra(Intent.EXTRA_STREAM, uri)
      putExtra(Intent.EXTRA_SUBJECT, "Charvik Apollo Vaccines & Daily Care Schedule (.ics)")
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    val chooser = Intent.createChooser(shareIntent, "Add to Calendar / Share .ics").apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(chooser)
  }
}
