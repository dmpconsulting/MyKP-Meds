package com.montunosoftware.pillpopper.kotlin

import com.montunosoftware.pillpopper.android.util.PillpopperLog

inline fun catchThis(action: () -> Unit, finally: () -> Unit?) {
    try {
        action()
    } catch (t: Throwable) {
        PillpopperLog.exception(t.message)
    } finally {
        finally.invoke()
    }
}


