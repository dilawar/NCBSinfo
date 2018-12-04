package com.rohitsuratekar.NCBSinfo

import android.app.AlertDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.setupWithNavController
import com.rohitsuratekar.NCBSinfo.common.Constants
import com.rohitsuratekar.NCBSinfo.common.MainCallbacks
import com.rohitsuratekar.NCBSinfo.common.hideMe
import com.rohitsuratekar.NCBSinfo.common.showMe
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), MainCallbacks {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        navController = Navigation.findNavController(this, R.id.nav_host)
        bottom_navigation.setupWithNavController(navController)

        for (item in bottom_navigation.menu.children) {
            item.isEnabled = false
        }

    }

    private fun gotoHome() {
        for (item in bottom_navigation.menu.children) {
            item.isEnabled = true
        }
        navController.popBackStack()
        navController.navigate(R.id.homeFragment)
    }

    override fun showProgress() {
        progress_frame.showMe()
    }

    override fun hideProgress() {
        progress_frame.hideMe()
    }

    override fun showError(message: String) {
        hideProgress()
        AlertDialog.Builder(this@MainActivity)
            .setTitle(R.string.oops)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok) { _, _ -> }
            .show()
    }

    override fun navigate(option: Int) {
        when (option) {
            Constants.NAVIGATE_HOME -> gotoHome()
        }
    }
}
