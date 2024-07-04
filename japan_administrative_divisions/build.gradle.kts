plugins {
    alias(libs.plugins.androidAssetPack)
}

assetPack {
    packName.set("japan_administrative_divisions")
    dynamicDelivery {
        deliveryType = "install-time"
    }
}
