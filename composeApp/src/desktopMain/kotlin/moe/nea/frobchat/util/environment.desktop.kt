package moe.nea.frobchat.util

import java.net.InetAddress

actual fun getHostname(): String {
	return InetAddress.getLocalHost().hostName
}
