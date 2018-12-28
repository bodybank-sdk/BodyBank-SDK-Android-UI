package com.bodybank.ui.camera

import android.hardware.Camera

class CameraHelper {
    companion object {
        @JvmStatic
        public fun getMaximumPictureSize(params: Camera.Parameters, maxPixelArea: Int): Camera.Size {
            var sizes = params.supportedPictureSizes.toMutableList()
            if (maxPixelArea > 0) {
                sizes.filter { it.width * it.height <= maxPixelArea }
            }
            sizes.sortBy { it.width * it.height }
            return if (!sizes.isEmpty()) {
                sizes.last()
            } else {
                params.pictureSize
            }

        }

        @JvmStatic
        public fun getMinimumPictureSize(params: Camera.Parameters, minPixelArea: Int): Camera.Size {
            var sizes = params.supportedPictureSizes.toMutableList()
            if (minPixelArea > 0) {
                sizes.filter { it.width * it.height >= minPixelArea }
            }
            sizes.sortBy { it.width * it.height }
            return if (!sizes.isEmpty()) {
                sizes.first()
            } else {
                params.pictureSize
            }
        }
    }
}