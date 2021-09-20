package com.montunosoftware.pillpopper.kotlin.history.horizontalRecyclerHistory

import com.montunosoftware.pillpopper.database.model.HistoryEvent

data class HorizontalSectionDataModel(
        var headerTime: String = "",
        var horizontalHistoryEventList: ArrayList<HistoryEvent> = arrayListOf()
)
