//
//import kotlinx.serialization.decodeFromString
//import kotlinx.serialization.json.Json
//import org.w3c.dom.HTMLFormElement
//import org.w3c.dom.HTMLInputElement
//import org.w3c.dom.WebSocket
//
//
//
//val FeedComponent = FC<FeedComponentProps> { props ->
//
//    val (feed, feedState) = useState(mutableListOf<FeedEntry>())
//    useEffect {
//    props.socket.onmessage = {
//            val newState = mutableListOf<FeedEntry>()
//            newState.addAll(feed)
//            newState.add(Json.decodeFromString(it.data as String))
//            feedState(newState)
//        }
//    }
//
//
//    div {
//        feed.forEach { feedItem ->
//            div {
//                h3 {
//                    +""""${feedItem.prompt}" - ${feedItem.username} - ${feedItem.ts}"""
//                }
//                feedItem.images?.forEach { image ->
//                    img {
//                        key = image
//                        src = "data:image/png;base64,$image"
//                    }
//
//                }
//            }
//        }
//    }
//}
//
//
//val PromptForm = FC<Props> {
//    val socket = WebSocket("ws://localhost:8080/prompt")
//    val (promptText, promptTextChange) = useState("")
//    val submitHandler: FormEventHandler<HTMLFormElement> = {
//        it.preventDefault()
//        console.log(promptText)
//        socket.send("""{"prompt":"$promptText","username":"tester"}""")
//
//    }
//    val changeHandler: ChangeEventHandler<HTMLInputElement> = {
//        promptTextChange(it.target.value)
//    }
//    form {
//        onSubmit = submitHandler
//        label {
//            input {
//                type = InputType.text
//                maxLength = 64
//                minLength = 2
//                name = "prompt"
//                id = "prompt"
//                value = promptText
//                onChange = changeHandler
//
//            }
//        }
//        button {
//            type = ButtonType.submit
//            +"Submit"
//        }
//    }
//}
//
//val Welcome = FC<Props> {
//    PromptForm {
//
//    }
//    FeedComponent {
//        socket = WebSocket("ws://localhost:8080/feed")
//    }
//}
