package com.raulcatalinas.shopping.shared.utils

import android.content.Context
import android.widget.Toast

fun showToast(context: Context, msg: String, duration: Int = Toast.LENGTH_SHORT) {

    Toast.makeText(context, msg, duration).show()
}