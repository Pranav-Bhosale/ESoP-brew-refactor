package com.esops.model

import com.esops.configuration.InventoryLimitConfiguration
import com.esops.configuration.PlatformFeesConfiguration
import com.esops.configuration.WalletLimitConfiguration

data class PlatformConfigurationResponse(
    val inventoryLimitConfiguration: InventoryLimitConfiguration,
    val platformFeesConfiguration: PlatformFeesConfiguration,
    val walletLimitConfiguration: WalletLimitConfiguration
)
