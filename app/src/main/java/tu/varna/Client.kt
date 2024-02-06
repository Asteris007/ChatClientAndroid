package tu.varna

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Scanner

internal class Client(private val socket: Socket, private val username: String) {
    private val bufferedReader: BufferedReader
    private val bufferedWriter: BufferedWriter
    private val sdf = SimpleDateFormat("HH:mm:ss")

    init {
        bufferedReader = BufferedReader(InputStreamReader(socket.getInputStream()))
        bufferedWriter = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))
    }

    fun sendMessage() {
        try {
            bufferedWriter.write(username)
            bufferedWriter.newLine()
            bufferedWriter.flush()
            val scanner = Scanner(System.`in`)
            while (socket.isConnected) {
                val messageToSend = scanner.nextLine()
                bufferedWriter.write(username + " [" + sdf.format(Date()) + "]: " + messageToSend)
                bufferedWriter.newLine()
                bufferedWriter.flush()
            }
        } catch (e: IOException) {
            closeEverything()
        }
    }

    fun listenForMessage() {
        Thread {
            while (socket.isConnected) {
                try {
                    val messageFromGroupChat = bufferedReader.readLine()
                    if (messageFromGroupChat.startsWith(username)) {
                        println("%-30s %s".format(" ", messageFromGroupChat))
                    } else {
                        println("%-30s %s".format(messageFromGroupChat, " "))
                    }
                } catch (e: IOException) {
                    closeEverything()
                }
            }
        }.start()
    }

    private fun closeEverything() {
        try {
            bufferedReader.close()
            bufferedWriter.close()
            socket.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }



    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            try {
                val scanner = Scanner(System.`in`)
                println("Enter your username for the group chat: ")
                val username = scanner.nextLine()
                val socket = Socket("172.16.1.125", 2727)
                val client = Client(socket, username)
                client.listenForMessage()
                client.sendMessage()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

}
