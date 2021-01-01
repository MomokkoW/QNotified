package me.kyuubiran.util

import android.os.Handler
import android.os.Looper
import com.topjohnwu.superuser.internal.UiThreadHandler.handler
import nil.nadph.qnotified.script.QNClient
import nil.nadph.qnotified.util.Utils
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread


object AutoRenewFireMgr {
    const val ENABLE = "kr_auto_renew_fire"
    const val LIST = "kr_auto_renew_fire_list"
    const val MESSAGE = "kr_auto_renew_fire_message"
    const val TIME = "kr_auto_renew_fire_time"
    private val mHandler = Handler(Looper.getMainLooper())
    private val mRunnable = object : Runnable {
        override fun run() {
            if (autoRenewList.isEmpty()) return
            if (needSend()) {
                thread {
                    Utils.getApplication().applicationContext.showToastBySystem("好耶 开始自动续火了 请不要关闭QQ哦")
                    for (u in autoRenewList) {
                        if (u.isGlobalMode) QNClient.send(u.uin, autoRenewMsg, 0)
                        else QNClient.send(u.uin, u.msg, 0)
                        Thread.sleep(5000)
                    }
                    Utils.getApplication().applicationContext.showToastBySystem("好耶 续火完毕了")
                }
            }
            handler.postDelayed(this, 600000L)
        }
    }

    private val str: String
        get() {
            return getExFriendCfg().getStringOrDefault(LIST, "")
        }

    private val tempList: ArrayList<AutoRenewFireItem> = arrayListOf()
    private val autoRenewList: ArrayList<AutoRenewFireItem>
        get() {
            return strToArr()
        }
    private var autoRenewMsg: String = "火"
        set(value) {
            field = value
            val cfg = getExFriendCfg()
            cfg.putString(MESSAGE, value)
            cfg.save()
        }
        get() {
            return getExFriendCfg().getStringOrDefault(MESSAGE, "火")
        }

    private fun strToArr(): ArrayList<AutoRenewFireItem> {
        if (str.isEmpty()) return arrayListOf()
        val strList = ArrayList(str.split("[||]"))
        val arfItemList = ArrayList<AutoRenewFireItem>()
        for (item in strList) {
            arfItemList.add(AutoRenewFireItem.parse(item))
        }
        return arfItemList
    }

    @Suppress("SameParameterValue")
    private fun save(list: ArrayList<AutoRenewFireItem>) {
        val cfg = getExFriendCfg()
        if (list.isEmpty()) {
            cfg.putString(LIST, "")
            cfg.save()
            return
        }
        val sb = StringBuilder()
        for (s in list.withIndex()) {
            if (s.index != list.size - 1) {
                sb.append(s.value).append("[||]")
            } else {
                sb.append(s.value)
            }
        }
        cfg.putString(LIST, sb.toString())
        cfg.save()
    }

    fun add(uin: String?, msg: String = "") {
        if (uin == null) return
        tempList.clear()
        tempList.addAll(autoRenewList)
        tempList.add(AutoRenewFireItem(uin, msg))
        save(tempList)
    }

    fun add(uin: Long) {
        add(uin.toString())
    }

    fun setMsg(uin: String, msg: String) {
        tempList.clear()
        tempList.addAll(autoRenewList)
        for (u in tempList) {
            if (u.uin == uin) {
                u.msg = msg
            }
        }
        save(tempList)
    }

    fun setMsg(uin: Long, msg: String) {
        setMsg(uin.toString(), msg)
    }

    private fun getUser(uin: String?): AutoRenewFireItem? {
        if (uin == null || uin.isEmpty()) return null
        for (u in autoRenewList) {
            if (uin == u.uin) return u
        }
        return null
    }

    fun getMsg(uin: String?): String {
        val u = getUser(uin)
        return u?.msg ?: ""
    }

    fun remove(uin: String?) {
        if (uin == null) return
        tempList.clear()
        tempList.addAll(autoRenewList)
        val removeItemList = ArrayList<AutoRenewFireItem>()
        for (u in tempList) {
            if (u.uin == uin) removeItemList.add(u)
        }
        tempList.removeAll(removeItemList)
        save(tempList)
    }

    fun remove(uin: Long) {
        remove(uin.toString())
    }

    fun hasEnabled(uin: String?): Boolean {
        for (u in autoRenewList) {
            if (u.uin == uin) return true
        }
        return false
    }

    fun hasEnabled(uin: Long): Boolean {
        return hasEnabled(uin.toString())
    }

    private fun needSend(): Boolean {
        val cfg = getExFriendCfg()
        val nextTime = cfg.getLongOrDefault(TIME, 0L)
        if (nextTime - System.currentTimeMillis() < 0) {
            val cal = Calendar.getInstance(Locale.CHINA)
            cal.add(Calendar.DATE, 1)
            cal.set(Calendar.HOUR, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 5)
            cal.set(Calendar.MILLISECOND, 0)
            cfg.putLong(TIME, cal.timeInMillis)
            cfg.save()
            return true
        }
        return false
    }

    fun doAutoSend() {
        mHandler.post(mRunnable)
    }

    fun resetTime() {
        val cfg = getExFriendCfg()
        cfg.putLong(TIME, 0L)
        cfg.save()
    }

    fun resetList() {
        val cfg = getExFriendCfg()
        autoRenewList.clear()
        cfg.putString(LIST, "")
        cfg.save()
    }
}

class AutoRenewFireItem(var uin: String, var msg: String = "") {
    val isGlobalMode: Boolean
        get() {
            return msg.isEmpty()
        }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append(uin)
        if (msg.isNotEmpty()) sb.append("[--]").append(msg)
        return sb.toString()
    }

    companion object {
        fun parse(string: String): AutoRenewFireItem {
            val arr = string.split("[--]")
            return if (arr.size == 2) {
                AutoRenewFireItem(arr[0], arr[1])
            } else {
                AutoRenewFireItem(arr[0])
            }
        }
    }
}