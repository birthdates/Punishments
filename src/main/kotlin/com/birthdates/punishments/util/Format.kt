package com.birthdates.punishments.util

class Format {
    companion object {
        const val MAX_DURATION_VALUES = 2

        fun parseShortDuration(duration: String): Long {
            // parse time like 1y5m1d3h3s, as 1 year, 5 motnhs, 1 day, 3h, 3s or 3w as weeks
            var time = 0L
            var current = 0L
            var i = 0
            while (i < duration.length) {
                val c = duration[i]
                if (c.isDigit()) {
                    current = current * 10 + (c - '0')
                } else {
                    when (c) {
                        'y' -> time += current * 31556952
                        'M' -> time += current * 2629746
                        'w' -> time += current * 604800
                        'd' -> time += current * 86400
                        'h' -> time += current * 3600
                        'm' -> time += current * 60
                        's' -> time += current
                    }
                    current = 0
                }
                i++
            }
            return time * 1000L
        }

        fun formatDuration(duration: Long): String {
            if (duration < 0) {
                return "permanent"
            }
            val years = (duration / 31556952000)
            val months = (duration % 31556952000) / 2629746000
            val days = (duration % 2629746000) / 86400000
            val hours = (duration % 86400000) / 3600000
            val minutes = (duration % 3600000) / 60000
            val seconds = (duration % 60000) / 1000

            val formatted = StringBuilder()
            var count = 0
            if (years > 0 && count++ < MAX_DURATION_VALUES) {
                formatted.append(years).append(" year").append(if (years > 1) "s" else "").append(", ")
            }
            if (months > 0 && count++ < MAX_DURATION_VALUES) {
                formatted.append(months).append(" month").append(if (months > 1) "s" else "").append(", ")
            }
            if (days > 0 && count++ < MAX_DURATION_VALUES) {
                formatted.append(days).append(" day").append(if (days > 1) "s" else "").append(", ")
            }
            if (hours > 0 && count++ < MAX_DURATION_VALUES) {
                formatted.append(hours).append(" hour").append(if (hours > 1) "s" else "").append(", ")
            }
            if (minutes > 0 && count++ < MAX_DURATION_VALUES) {
                formatted.append(minutes).append(" minute").append(if (minutes > 1) "s" else "").append(", ")
            }
            if (seconds > 0 && count++ < MAX_DURATION_VALUES) {
                formatted.append(seconds).append(" second").append(if (seconds > 1) "s" else "")
            }
            if (formatted.isEmpty()) {
                return "0 seconds"
            }
            return formatted.toString().removeSuffix(", ").trim()
        }
    }
}