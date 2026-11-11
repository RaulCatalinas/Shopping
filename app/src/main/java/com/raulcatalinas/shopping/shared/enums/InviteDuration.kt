package com.raulcatalinas.shopping.shared.enums

import androidx.annotation.StringRes
import com.raulcatalinas.shopping.R

enum class InviteDuration(
    val hours: Int,
    @StringRes val labelRes: Int
) {
    ONE_DAY(24, R.string.invite_duration_one_day),
    ONE_WEEK(168, R.string.invite_duration_one_week),
    ONE_MONTH(720, R.string.invite_duration_one_month)
}