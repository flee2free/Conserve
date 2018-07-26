package com.gemini.energy.presentation.audit

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.gemini.energy.R
import com.gemini.energy.databinding.ActivityHomeDetailBinding
import com.gemini.energy.presentation.audit.detail.adapter.DetailPagerAdapter
import com.gemini.energy.presentation.audit.detail.preaudit.PreAuditFragment
import com.gemini.energy.presentation.audit.detail.zone.list.ZoneListFragment
import com.gemini.energy.presentation.audit.dialog.AuditDialogFragment
import com.gemini.energy.presentation.audit.list.AuditListFragment
import com.gemini.energy.presentation.audit.list.model.AuditModel
import com.gemini.energy.presentation.base.BaseActivity
import com.gemini.energy.presentation.util.Navigator
import com.gemini.energy.service.EnergyService
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import kotlinx.android.synthetic.main.activity_home_detail.*
import javax.inject.Inject

class AuditActivity : BaseActivity(), AuditListFragment.OnAuditSelectedListener {

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var energyService: EnergyService

    private lateinit var sharedViewModel: SharedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedViewModel = ViewModelProviders.of(this).get(SharedViewModel::class.java)
        sharedViewModel.getAudit().observe(this,
                Observer<AuditModel> { })

        super.binder?.let {
            setupContent(it)
            setupAuditList()
        }
    }


    /*
    * Option Menu Management
    * */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.menu_create_audit -> consume { showCreateAudit() }
        R.id.menu_energy_calculation -> consume {
            linlaHeaderProgress.visibility = View.VISIBLE
            energyService.run(callback = {
                Log.d(TAG, "\\m/ End of Computation \\m/ - (${Thread.currentThread().name})")
                if (it) {
                    linlaHeaderProgress.visibility = View.GONE
                }
            })
        }
        else -> super.onOptionsItemSelected(item)
    }

    private inline fun consume(f: () -> Unit): Boolean {
        f()
        return true
    }


    /*
    * View Pager Main Content
    * */
    private fun setupContent(binder: ActivityHomeDetailBinding) {
        binder.viewPager.adapter = DetailPagerAdapter(supportFragmentManager)
    }


    /*
    * Side Panel Content Setup
    *
    * 1. Audit List
    * 2. Audit Selection Listener - Which gets activated from the [Adapter - Fragment - Activity]
    * 3. Set the Top Right Header to the current Active Audit Id
    * 4. Refresh the Zone View Model - As we need to fetch the Zone for the selected Audit
    * 5. Dispose the Subscribed Get Audit Channel when the Activity dies off
    *
    * */
    private fun setupAuditList() {
        var auditListFragment = AuditListFragment.newInstance()

        supportFragmentManager
                .beginTransaction()
                .add(R.id.side_bar, auditListFragment, FRAG_AUDIT_LIST)
                .commit()
    }

    override fun onAuditSelected(observable: Observable<AuditModel>) {
        disposables.add(observable.subscribeWith(object : DisposableObserver<AuditModel>() {
            override fun onComplete() {}
            override fun onNext(t: AuditModel) {
                setAuditHeader(t)
                refreshZoneViewModel(t)
                refreshPreAudit(t)
            }

            override fun onError(e: Throwable) {}
        }))
    }

    private fun setAuditHeader(audit: AuditModel) {
        findViewById<TextView>(R.id.txt_header_audit).text = "${audit.name}"
    }

    private fun refreshZoneViewModel(auditModel: AuditModel) {
        val tag = "${ANDROID_SWITCHER}:${view_pager.id}:${ZONE_LIST_FRAGMENT_INDEX}"
        val fragment = supportFragmentManager.findFragmentByTag(tag) as ZoneListFragment?
        fragment?.let {
            fragment.setAuditModel(auditModel)
        }
    }

    private fun refreshPreAudit(auditModel: AuditModel) {
        val tag = "${ANDROID_SWITCHER}:${view_pager.id}:${PREAUDIT_FRAGMENT_INDEX}"
        val fragment = supportFragmentManager.findFragmentByTag(tag) as PreAuditFragment?
        fragment?.let {
            fragment.setAuditModel(auditModel)
        }
    }

    override fun onDestroy() {
        disposables.dispose()
        super.onDestroy()
    }

    private fun showCreateAudit() {
        val dialogFragment = AuditDialogFragment()
        dialogFragment.show(supportFragmentManager, FRAG_DIALOG)
    }

    companion object {
        private const val TAG = "AuditActivity"

        private const val FRAG_DIALOG = "AuditDialogFragment"
        private const val FRAG_AUDIT_LIST = "AuditListFragment"

        private const val ANDROID_SWITCHER = "android:switcher"
        private const val ZONE_LIST_FRAGMENT_INDEX = 1
        private const val PREAUDIT_FRAGMENT_INDEX = 0

        private var disposables = CompositeDisposable()
    }

}
