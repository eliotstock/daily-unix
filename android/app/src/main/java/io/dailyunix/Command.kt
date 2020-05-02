package io.dailyunix

data class Command(val name: String,
        var whatIs: String?,
        var providerPackage: String?,
        var tldr: String?,
        var man: String?)
