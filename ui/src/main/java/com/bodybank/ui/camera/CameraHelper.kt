package com.bodybank.ui.camera

import android.annotation.TargetApi
import android.content.Context
import android.hardware.Camera
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.view.Surface

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

        @JvmStatic
        fun getFrontCameraId(context: Context): Int {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                return getFrontCameraIdImplV21(context)
            } else {
                return getFrontCameraIdImpl()
            }
        }

        @JvmStatic
        @TargetApi(21)
        fun getFrontCameraIdImplV21(context: Context): Int {
            val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            for (id in manager.cameraIdList) {
                if (manager.getCameraCharacteristics(id).get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
                    return id.toInt()
                }
            }
            return -1
        }

        @JvmStatic
        @TargetApi(9)
        @SuppressWarnings("deprecated")
        fun getFrontCameraIdImpl(): Int {
            val cameraInfo = Camera.CameraInfo()
            for (i in 0..(Camera.getNumberOfCameras() - 1)) {
                Camera.getCameraInfo(i, cameraInfo)
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    return i
                }
            }
            return -1
        }

        fun setCameraDisplayOrientation(displayOrientation: Int, cameraId: Int, camera: Camera) {
            val orientation = getCameraDisplayOrientation(displayOrientation, cameraId)
            camera.setDisplayOrientation(orientation)
        }

        fun getCameraDisplayOrientation(displayOrientation: Int, cameraId: Int): Int {
            var degrees = 0
            when (displayOrientation) {
                Surface.ROTATION_0 -> degrees = 0
                Surface.ROTATION_90 -> degrees = 90
                Surface.ROTATION_180 -> degrees = 180
                Surface.ROTATION_270 -> degrees = 270
            }

            var result: Int
            val info = Camera.CameraInfo()
            Camera.getCameraInfo(cameraId, info)
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                result = (info.orientation + degrees) % 360
                result = (360 - result) % 360
            } else { // back-facing
                result = (info.orientation - degrees + 360) % 360
            }
            return result
        }

        fun getCameraPictureRotation(pictureOrientaiton: Int, cameraId: Int): Int {
            val info = Camera.CameraInfo()
            Camera.getCameraInfo(cameraId, info)
            var rotation = 0
            var orientation = (pictureOrientaiton + 45) / 90 * 90

            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                rotation = (info.orientation - orientation + 360) % 360
            } else {
                rotation = (info.orientation + orientation) % 360
            }

            return rotation
        }

    }
}