package com.gemini.energy.service.device

import com.gemini.energy.service.IComputable
import com.gemini.energy.service.OutgoingRows

class Hvac : IComputable {

    override fun compute(): List<List<OutgoingRows>> {
        return listOf()
    }
}
