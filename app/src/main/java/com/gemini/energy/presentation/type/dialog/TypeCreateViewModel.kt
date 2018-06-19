package com.gemini.energy.presentation.type.dialog

import android.app.Application
import android.content.Context
import android.util.Log
import com.gemini.energy.R
import com.gemini.energy.domain.entity.Type
import com.gemini.energy.domain.interactor.ZoneTypeSaveUseCase
import com.gemini.energy.internal.util.BaseAndroidViewModel
import com.gemini.energy.internal.util.SingleLiveData
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import java.util.*

class TypeCreateViewModel(context: Context, private val zoneTypeCreateUseCase: ZoneTypeSaveUseCase) :
        BaseAndroidViewModel(context.applicationContext as Application) {

    private val _result = SingleLiveData<Boolean>()
    val result = _result

    private val _error = SingleLiveData<String>()
    val error = _error


    fun createZoneType(zoneId: Int, zoneType: String, zoneSubType: String?, zoneTypeTag: String, auditId: Int) {
        val date = Date()
        addDisposable(save(Type(null, zoneTypeTag, zoneType, zoneSubType, zoneId, auditId, date, date)))
    }

    private fun save(scope: Type): Disposable {
        return zoneTypeCreateUseCase.execute(scope)
                .subscribeWith(object : DisposableObserver<Unit>() {

                    override fun onNext(t: Unit) {
                        Log.d(TAG, "ON-NEXT !!! \\m/")
                        result.value = true
                    }

                    override fun onError(e: Throwable) {
                        error.value = e.localizedMessage ?: e.message ?:
                                context.getString(R.string.unknown_error)
                    }

                    override fun onComplete() {}
                })
    }

    companion object {
        private const val TAG = "ZoneTypeCreateViewModel"
    }

}