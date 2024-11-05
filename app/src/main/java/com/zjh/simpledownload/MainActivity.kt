package com.zjh.simpledownload

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.zjh.download.SimpleDownload
import com.zjh.download.core.DownloadTask
import com.zjh.download.helper.State
import com.zjh.download.utils.download
import com.zjh.download.utils.logD
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MainActivity : AppCompatActivity() {

    private lateinit var downloadTask1: DownloadTask
    private lateinit var downloadTask2: DownloadTask
    private lateinit var downloadTask3: DownloadTask
    private val nameLit = listOf("新浪微博", "腾讯手机管家", "腾讯浏览器")
    private lateinit var pro1: TextView
    private lateinit var pro2: TextView
    private lateinit var pro3: TextView

    private val urlList = listOf(
        "https://imtt.dd.qq.com/16891/apk/96881CC7639E84F35E86421691CBBA5D.apk?fsname=com.sina.weibo_11.1.3_4842.apk&csr=3554",
        "https://imtt.dd.qq.com/16891/apk/DE071539CCD23453643F24779B052788.apk?fsname=com.tencent.qqpimsecure_8.10.0_1417.apk&csr=3554",
        "https://imtt.dd.qq.com/16891/apk/0F9F42DB8B17D9BA27A60D8D556F936C.apk?fsname=com.tencent.mtt_11.2.1.1506_11211500.apk&csr=3554"
    )

    private val scope by lazy {
        CoroutineScope(Dispatchers.IO + Job())
    }

    private val savePath by lazy {
        SimpleDownload.instance.context.filesDir.path + "/apks"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        pro1 = findViewById(R.id.pro1)
        pro2 = findViewById(R.id.pro2)
        pro3 = findViewById(R.id.pro3)
        downloadTask1 = scope.download(urlList[0], nameLit[0])
        download(downloadTask1)
        downloadTask2 = scope.download(urlList[1], nameLit[1])
        download(downloadTask2)
        downloadTask3 = scope.download(urlList[2], nameLit[2])
        download(downloadTask3)


        pro1.setOnClickListener {
            if (downloadTask1.isStarted()) {
                logD("任务1暂停")
                downloadTask1.stop()
            } else {
                logD("任务1开始")
                downloadTask1.start()
            }
        }
    }

    private fun download(downloadTask: DownloadTask) {
        //状态监听
        downloadTask.state().onEach {
            when (it) {
                is State.None -> logD("未开始任务")
                is State.Waiting -> logD("等待中")
                is State.Downloading -> logD("下载中")
                is State.Failed -> logD("下载失败")
                is State.Stopped -> logD("下载已暂停")
                is State.Succeed -> logD("下载成功")
            }
            logD("state : $it")
        }.launchIn(scope)

        //进度监听
        downloadTask.progress().onEach {
            logD("Thread Name : ${Thread.currentThread().name}")
            if (downloadTask.param.url.contains("11.1.3_4842")) {
                pro1.text = it.percentStr()
            } else if (downloadTask.param.url.contains("8.10.0_1417")) {
                pro2.text = it.percentStr()
            } else if (downloadTask.param.url.contains("11.2.1.1506_11211500")) {
                pro3.text = it.percentStr()
            }
            logD("name : ${downloadTask.param.saveName} , progress : ${it.percentStr()}")
        }.launchIn(MainScope())

        //开始下载任务
        downloadTask.start()
    }
}