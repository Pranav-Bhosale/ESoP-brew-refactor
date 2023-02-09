package com.esops.exception

class WalletException(val errorList: List<String>) : Throwable() {}
