package nz.eloque.foss_wallet.ui.screens.create

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import nz.eloque.foss_wallet.persistence.PassStore


@HiltViewModel
class CreateViewModel @Inject constructor(
    application: Application,
    private val passStore: PassStore,
) : AndroidViewModel(application)