package io.dailyunix

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CommandViewModel : ViewModel() {

    var command: MutableLiveData<Command> = MutableLiveData<Command>()

}
