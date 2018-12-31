package com.bodybank.ui.camera

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Matrix
import android.graphics.PixelFormat
import android.graphics.SurfaceTexture
import android.hardware.*
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v7.app.AlertDialog
import android.view.*
import com.bodybank.enterprise.type.Gender
import com.bodybank.estimation.EstimationParameter
import com.bodybank.ui.R
import com.bodybank.ui.misc.BaseFragment
import com.bodybank.ui.misc.ViewPressEffectHelper
import com.stfalcon.frescoimageviewer.ImageViewer
import kotlinx.android.synthetic.main.fragment_camera.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

open class CameraFragment : BaseFragment(), SensorEventListener, BasePickerFragment.Delegate {

    public interface Delegate {
        fun onCancelCameraFragment(fragment: CameraFragment)
        fun onFinishCameraFragment(fragment: CameraFragment)
    }

    companion object {
        val REQUEST_CODE_PICK_PHOTO = 100
    }

    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null
    private var rotationSensor: Sensor? = null
    private var gravitySensor: Sensor? = null
    private val mRotationMatrix = FloatArray(16)
    var takingPicture = false
    var dateFormat = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss")
    var delegate: Delegate? = null

    open var estimationParameter: EstimationParameter = EstimationParameter()

    open var capturingFront: Boolean = true
        set(value) {
            field = value
            if (value) {
                imagePreview.visibility = View.INVISIBLE
                estimationParameter.frontImagePath = null
                guideImageView.setImageResource(R.drawable.front)
                imagePreview.setImageURI(null)
            } else {
                imagePreview.visibility = View.VISIBLE
                estimationParameter.sideImagePath = null
                guideImageView.setImageResource(R.drawable.side)
                imagePreview.setImageURI(Uri.fromFile(File(estimationParameter.frontImagePath)))
            }
        }
    open var isParameterAllInput: Boolean = false
        get() = estimationParameter.age > 0 && estimationParameter.heightInCm > 0 && estimationParameter.weightInKg > 0

    internal enum class RotationCalculationType {
        RotationVector, GravityCompass, AccelerometerCompass, Unavailable
    }

    private var rotationCalculationType: RotationCalculationType? = null
    private val magnitudeValues = FloatArray(3)
    private val accelerometerValues = FloatArray(3)
    internal val inclinationValues = FloatArray(16)
    private val gravityValues = FloatArray(3)
    internal var rollingAverage: Array<MutableList<Float>> = arrayOf(mutableListOf(), mutableListOf(), mutableListOf())
    internal val gravity = DoubleArray(3)

    var pictureSize: Camera.Size? = null
    var previewSize: Camera.Size? = null
    var cameraParameter: Camera.Parameters? = null
    var camera: Camera? = null
    var cameraFacing: Int = Camera.CameraInfo.CAMERA_FACING_BACK
    var cameraOrientation: Int = 0

    var currentPickerFramgnet: BasePickerFragment? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sensorManager = activity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        if (rotationSensor == null) {
            rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)
        }
        if (rotationSensor == null) {
            gravitySensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GRAVITY)
            rotationCalculationType = RotationCalculationType.AccelerometerCompass
            if (gravitySensor == null) {
                rotationCalculationType = RotationCalculationType.AccelerometerCompass
                accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            }
            magnetometer = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        } else {
            rotationCalculationType = RotationCalculationType.RotationVector
        }
        if (magnetometer == null && rotationSensor == null) {
            rotationCalculationType = RotationCalculationType.Unavailable
        }
        if (CameraHelper.getFrontCameraId(activity!!) == -1) {
            switchCameraButton?.isEnabled = false
        }
        imagePreview?.setOnClickListener {
            ImageViewer.Builder(context, listOf(estimationParameter.frontImagePath!!)).show()
        }
        timerContainer.setOnClickListener { }
        pickerContainer.setOnClickListener { }
        heightValueLabel?.setOnClickListener { showHeightPicker() }
        weightValueLabel?.setOnClickListener { showWeightPicker() }
        ageValueLabel?.setOnClickListener { showAgePicker() }
        timerButton?.setOnClickListener { onClickTimerButton() }
        switchCameraButton?.setOnClickListener { switchCamera() }
        pickImageButton?.setOnClickListener { onPickImageButtonClick() }
        genderSegmentedControl?.setOnCheckedChangeListener { group, index ->
            if (index == 0) {
                estimationParameter.gender = Gender.male
            } else {
                estimationParameter.gender = Gender.female
            }
        }
        captureButton?.setOnClickListener { onClickCaptureButton() }
        listOf<View?>(
            heightValueLabel,
            weightValueLabel,
            ageValueLabel,
            timerButton,
            pickImageButton,
            captureButton,
            switchCameraButton
        ).forEach { view ->
            view?.let {
                ViewPressEffectHelper.attach(it)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        initializeViewComponents()
    }

    fun initializeViewComponents() {
        textureView?.keepScreenOn = true
        textureView?.surfaceTextureListener = SurfaceCallback()
    }


    override fun onSensorChanged(event: SensorEvent) {
        // It is good practice to check that we received the proper sensor event
        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            // Convert the rotation-vector to a 4x4 matrix.
            SensorManager.getRotationMatrixFromVector(
                mRotationMatrix,
                event.values
            )
        } else {
            if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                System.arraycopy(event.values, 0, magnitudeValues, 0, magnitudeValues.size)
            } else if (event.sensor.type == Sensor.TYPE_GRAVITY) {
                System.arraycopy(event.values, 0, gravityValues, 0, gravityValues.size)
            } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                System.arraycopy(event.values, 0, accelerometerValues, 0, accelerometerValues.size)
            }
            if (magnetometer != null) {
                if (rotationCalculationType == RotationCalculationType.AccelerometerCompass) {
                    SensorManager.getRotationMatrix(
                        mRotationMatrix,
                        inclinationValues,
                        accelerometerValues,
                        magnitudeValues
                    )
                } else {
                    SensorManager.getRotationMatrix(mRotationMatrix, inclinationValues, gravityValues, magnitudeValues)
                }
            } else {
                if (rotationCalculationType == RotationCalculationType.AccelerometerCompass) {
                    roll(rollingAverage[0], event.values[0])
                    roll(rollingAverage[1], event.values[1])
                    roll(rollingAverage[2], -event.values[2])

                    gravity[0] = averageList(rollingAverage[0]).toDouble()
                    gravity[1] = averageList(rollingAverage[1]).toDouble()
                    gravity[2] = averageList(rollingAverage[2]).toDouble()
                } else {
                    SensorManager.getRotationMatrix(mRotationMatrix, inclinationValues, gravityValues, magnitudeValues)
                }
            }
        }

        val rotation = activity?.windowManager?.defaultDisplay?.rotation
        var axisX = SensorManager.AXIS_X
        var axisY = SensorManager.AXIS_Z
        when (rotation) {
            Surface.ROTATION_0 -> {
                axisX = SensorManager.AXIS_X
                axisY = SensorManager.AXIS_Z
            }

            Surface.ROTATION_90 -> {
                axisX = SensorManager.AXIS_Z
                axisY = SensorManager.AXIS_MINUS_X
            }

            Surface.ROTATION_180 -> {
                axisX = SensorManager.AXIS_MINUS_X
                axisY = SensorManager.AXIS_MINUS_Z
            }

            Surface.ROTATION_270 -> {
                axisX = SensorManager.AXIS_MINUS_Z
                axisY = SensorManager.AXIS_X
            }

            else -> {
            }
        }

        val orientationVals = FloatArray(3)
        if (rotationCalculationType == RotationCalculationType.RotationVector || magnetometer != null) {
            SensorManager
                .remapCoordinateSystem(
                    mRotationMatrix,
                    axisX, axisY,
                    mRotationMatrix
                )

            SensorManager.getOrientation(mRotationMatrix, orientationVals)

            // Optionally convert the result from radians to degrees
            orientationVals[0] = Math.toDegrees(orientationVals[0].toDouble()).toFloat()
            orientationVals[1] = Math.toDegrees(orientationVals[1].toDouble()).toFloat()
            orientationVals[2] = Math.toDegrees(orientationVals[2].toDouble()).toFloat()
        } else {
            orientationVals[2] = Math.toDegrees(Math.atan2(gravity[1], gravity[2])).toFloat()
        }
    }

    private val MAX_SAMPLE_SIZE = 5

    fun roll(list: MutableList<Float>, newMember: Float) {
        if (list.size == MAX_SAMPLE_SIZE) {
            list.removeAt(0)
        }
        list.add(newMember)
    }

    fun averageList(tallyUp: List<Float>): Float {

        var total = 0f
        for (item in tallyUp) {
            total += item
        }
        total = total / tallyUp.size

        return total
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {


    }

    private fun calculateZAngle(x: Int, z: Int): Int {
        var zAngle = 0
        // there are 4 cases
        if (x < 0 && (z > 0 || z < 0)) {
            zAngle = Math.abs(z)
        } else if (x > 0 && (z < 0 || z > 0)) {
            zAngle = 180 - Math.abs(z)
        } else if (z == 90 || z == -90) {
            zAngle = 90
        } else if (x == -90 || z == 0 && x < 0) {
            zAngle = 0
        } else if (x == 90 || z == 0 && x > 0) {
            zAngle = 180
        }
        return zAngle
    }

    fun tempImageFileName() = "bodybank-image-cache" + dateFormat.format(Date()) + ".jpg"

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (resultCode == RESULT_OK) {
                if (requestCode == REQUEST_CODE_PICK_PHOTO && null != data) {
                    data.data?.let { uri ->
                        context?.let { context ->
                            val fileName = tempImageFileName()
                            context.openFileOutput(fileName, Context.MODE_PRIVATE)?.let { output ->
                                context.contentResolver?.openInputStream(uri)?.let { input ->
                                    input.copyTo(output)
                                }
                                if (capturingFront) {
                                    estimationParameter.frontImagePath = context.getFileStreamPath(fileName).path
                                    capturingFront = false
                                } else {
                                    estimationParameter.sideImagePath = context.getFileStreamPath(fileName).path
                                    delegate?.onFinishCameraFragment(this)
                                }
                            }

                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun openCamera() {
        if (camera == null) {
            activity?.let {
                if (cameraFacing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    camera = Camera.open()
                } else {
                    camera = Camera.open(CameraHelper.getFrontCameraId(it))
                }
            }
        }
    }

    private inner class SurfaceCallback : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
            if (null == camera) {
                try {
                    openCamera()
                    camera?.setPreviewTexture(surfaceTexture)
                    initializeCamera()
                    configurePreviewTransform(width, height)
                } catch (e: Throwable) {
                    e.printStackTrace()
                }

            }
        }

        override fun onSurfaceTextureSizeChanged(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
            configurePreviewTransform(width, height)
        }

        override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
            if (camera != null) {
                camera?.stopPreview()
                camera?.release()
                camera = null
            }
            return false
        }

        override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {
            if (camera != null) {
                try {
                    camera?.setPreviewTexture(surfaceTexture)
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }

    }


    private fun getPreviewTransformMatrix(viewWidth: Int, viewHeight: Int): Matrix {
        val matrix = Matrix()
        var width = 0
        var height = 0
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            previewSize?.let {
                width = it.width
                height = it.height
            }
        } else {
            previewSize?.let {
                width = it.height
                height = it.width
            }
        }
        val horizontalScale = viewWidth.toFloat() / width
        val verticalScale = viewHeight.toFloat() / height
        if (horizontalScale > verticalScale) {
            matrix.postScale(1f, horizontalScale / verticalScale, 0f, 0f)
            val scaledHeight = viewHeight * horizontalScale / verticalScale
            matrix.postTranslate(0f, -(scaledHeight - viewHeight) / 2f)
        } else {
            matrix.postScale(verticalScale / horizontalScale, 1f, 0f, 0f)
            val scaledWidth = viewWidth * verticalScale / horizontalScale
            matrix.postTranslate(-(scaledWidth - viewWidth) / 2f, 0f)
        }
        return matrix
    }

    private fun configurePreviewTransform(viewWidth: Int, viewHeight: Int) {
        textureView.setTransform(getPreviewTransformMatrix(viewWidth, viewHeight))
    }

    private fun initializeCamera() {
        cameraParameter = camera?.parameters
        cameraParameter?.pictureFormat = PixelFormat.JPEG

        pictureSize = CameraHelper.getMaximumPictureSize(cameraParameter!!, 3840 * 2160)
        previewSize = CameraHelper.getMinimumPictureSize(cameraParameter!!, 320 * 480)

        cameraParameter?.setPictureSize(pictureSize!!.width, pictureSize!!.height)
        cameraParameter?.setPreviewSize(previewSize!!.width, previewSize!!.height)

        // do not set this when you use ffc camera
        if (cameraParameter?.supportedFocusModes?.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE) == true) {
            cameraParameter?.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
        }
        camera?.setDisplayOrientation(
            CameraHelper.getCameraDisplayOrientation(
                activity!!.windowManager.defaultDisplay.orientation,
                cameraFacing
            )
        )
        val outputOrienataion =
            CameraHelper.getCameraPictureRotation(activity!!.windowManager!!.defaultDisplay!!.rotation, cameraFacing)
        cameraParameter?.setRotation(outputOrienataion)
        determineCameraOrientation()
        try {
            camera?.parameters = cameraParameter
        } catch (e: Exception) {
            e.printStackTrace()
        }

        CameraHelper.setCameraDisplayOrientation(
            activity!!.windowManager!!.defaultDisplay!!.rotation,
            cameraFacing,
            camera!!
        )
        camera?.startPreview()
        camera?.cancelAutoFocus()
    }

    private fun determineCameraOrientation() {
        val info = Camera.CameraInfo()
        Camera.getCameraInfo(cameraFacing, info)
        cameraOrientation = info.orientation
    }

    fun takePicture() {
        takingPicture = true
        camera?.takePicture({

        }, { byteArray, _ ->

        }, { byteArray, _ ->
            val fileName = tempImageFileName()
            context?.let {
                it.openFileOutput(fileName, Context.MODE_PRIVATE)?.use {
                    it.write(byteArray)
                }
                if (capturingFront) {
                    estimationParameter.frontImagePath = it.getFileStreamPath(fileName).path
                    capturingFront = false
                } else {
                    estimationParameter.sideImagePath = it.getFileStreamPath(fileName).path
                    delegate?.onFinishCameraFragment(this)
                }
            }
        })
    }

    fun switchCamera() {
        if (cameraFacing == Camera.CameraInfo.CAMERA_FACING_BACK) {
            cameraFacing = Camera.CameraInfo.CAMERA_FACING_FRONT
        } else {
            cameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK
        }
        camera?.stopPreview()
        camera?.release()
        camera = null
        if (textureView.isAvailable) {
            openCamera()
            camera?.setPreviewTexture(textureView.surfaceTexture)
            initializeCamera()
        } else {
            textureView.surfaceTextureListener = SurfaceCallback()
        }
    }

    fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_PICK_PHOTO)
    }

    fun onPickImageButtonClick() {
        if (isParameterAllInput) {
            if (takingPicture) {
                return
            }
            pickImage()
        } else {
            activity?.let {
                it.runOnUiThread {
                    AlertDialog.Builder(it).setMessage("Input height, weight, age and gender.")
                        .setPositiveButton("OK", { _, _ -> }).show()
                }
            }
        }
    }

    fun showPicker(fragment: BasePickerFragment) {
        pickerContainer.visibility = View.VISIBLE
        childFragmentManager.beginTransaction().replace(R.id.pickerContainer, fragment).setCustomAnimations(
            android.R.anim.fade_in,
            android.R.anim.fade_out,
            android.R.anim.fade_in,
            android.R.anim.fade_out
        ).addToBackStack("pick").commit()
        fragment.delegate = this
        currentPickerFramgnet = fragment
    }

    fun showHeightPicker() {
        val framgent = HeightPickerFramgent()
        framgent.setInitialValue(heightValueLabel!!.text.toString(), heightUnitLabel!!.text.toString())
        showPicker(framgent)
    }

    fun showWeightPicker() {
        val framgent = WeightPickerFragment()
        framgent.setInitialValue(weightValueLabel!!.text.toString(), weightUnitLabel!!.text.toString())
        showPicker(framgent)
    }

    fun showAgePicker() {
        val framgent = AgePickerFragment()
        framgent.setInitialValue(ageValueLabel!!.text.toString(), "")
        showPicker(framgent)
    }

    fun onClickCaptureButton() {

        if (isParameterAllInput) {
            if (takingPicture) {
                return
            }
            takePicture()
        } else {

            activity?.let {
                it.runOnUiThread {
                    AlertDialog.Builder(it).setMessage("Input height, weight, age and gender.")
                        .setPositiveButton("OK", { _, _ -> }).show()
                }
            }
        }
    }


    override fun onFinishPickerFragment(fragment: BasePickerFragment) {
        if (fragment is HeightPickerFramgent) {
            estimationParameter.heightInCm = fragment.heightInCm
            heightValueLabel.text = String.format("%.2f", fragment.currentValue)
            heightUnitLabel.text = fragment.unit
        } else if (fragment is WeightPickerFragment) {
            estimationParameter.weightInKg = fragment.weightInKg
            weightValueLabel.text = String.format("%.2f", fragment.currentValue)
            weightUnitLabel.text = fragment.unit
        } else if (fragment is AgePickerFragment) {
            estimationParameter.age = fragment.age
            ageValueLabel.text = String.format("%.0f", fragment.currentValue)
        }
        removePickerFragment()
    }

    fun removePickerFragment() {
        currentPickerFramgnet?.let {
            childFragmentManager.popBackStack()
        }
        pickerContainer?.postDelayed({
            pickerContainer?.visibility = View.GONE
        }, 500)
        currentPickerFramgnet = null
    }

    override fun onCancelPickerFragment(fragment: BasePickerFragment) {
        removePickerFragment()
    }

    fun onClickTimerButton() {
        if (isParameterAllInput) {
            if (takingPicture) {
                return
            }
            beginTimerCount()
        } else {
            activity?.let {
                it.runOnUiThread {
                    AlertDialog.Builder(it).setMessage("Input height, weight, age and gender.")
                        .setPositiveButton("OK", { _, _ -> }).show()
                }
            }
        }
    }

    fun beginTimerCount() {
        timerContainer?.visibility = View.VISIBLE
        val timer = PhotoTimer(10 * 1000)
        timer.start()
    }

    inner class PhotoTimer(val count: Long) : CountDownTimer(count, 1000) {
        override fun onFinish() {
            takePicture()
            timerContainer?.visibility = View.GONE
        }

        override fun onTick(p0: Long) {
            activity?.runOnUiThread {
                timerCountLabel.text = String.format("%d", (count - p0) / 1000)
            }
        }
    }
}

