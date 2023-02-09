package com.esops.exception

class InventoryException(val errorList: List<String>) : Throwable() {}
