import kotlinx.browser.document
import kotlinx.html.js.*
import kotlinx.html.dom.*
import kotlinx.browser.window
import kotlinx.html.*

import kotlinx.serialization.decodeFromString
import org.w3c.dom.*
import org.w3c.dom.events.Event
import kotlin.js.json

val promptSocket = WebSocket("ws://localhost:8088/prompt")


val username = getUsername()

fun getUsername(): String {
    fun promptAndValidate(prefix: String = ""): String {
        val un = window.prompt("${prefix}Please enter your username")
        return if (un == null || un == "null" || un.trim().length < 2)
            promptAndValidate()
        else un
    }

    val storedUn = window.localStorage["username"]
    return if (storedUn == null) {
        val un = promptAndValidate("Please try again. ")
        window.localStorage["username"] = un
        un
    } else
        storedUn
}
 fun submitPrompt(e: Event) {
    e.preventDefault()
     val promptText = (document.getElementById("prompt")!! as HTMLInputElement).value
    console.log(promptText)
     promptSocket.send(JSON.stringify(json( "prompt" to  promptText, "username" to username)))
 }

fun buildEntryDiv(entry: FeedEntry): HTMLElement {
    return document.create.div {
        id = entry.ts!!
        h3 {
            this.text("\"${entry.prompt}\" - ${entry.username} at ${entry.ts}")
        }
        entry.images?.map { image ->
            img {
                src="data:image/png;base64,$image"
            }
        }
    }
}

fun main() {
    val feedSocket = WebSocket("ws://localhost:8088/feed")

    feedSocket.onmessage = {
        val r = kotlinx.serialization.json.Json.decodeFromString<FeedEntry>(it.data.toString())
        val newDiv = buildEntryDiv(r)
        document.getElementById("feed")!!.prepend(newDiv)

    }
    document.body!!.append {
        form {
            onSubmitFunction = { e -> submitPrompt(e)}
            label {
                htmlFor = "prompt"
                +"Prompt"
            }
            input {
                name = "prompt"
                id = "prompt"
                type = InputType.text
            }
            button {
                type = ButtonType.submit
                +"Submit"
            }

        }
        div {
            id = "feed"
        }
    }

}
