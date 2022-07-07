const host = document.location.host

const feedSocket = new WebSocket(`ws://${host}/feed`)
const promptSocket = new WebSocket(`ws://${host}/prompt`)
const patchSocket = new WebSocket(`ws://${host}/patch`)
const lsUsername = localStorage.getItem("username")
const username = lsUsername != null ? lsUsername : getUsername();

function getUsername() {
    const un = prompt("Please enter your username");
    localStorage.setItem("username", un);
    return un;
}

feedSocket.onmessage = (message) => {
    const resp = JSON.parse(message.data)
    document.getElementsByTagName('button')[0].removeAttribute('disabled')
    if(resp.images != null && resp.images.length > 0)
        handleSuccess(resp);
    else
        handleError();
}
patchSocket.onmessage = (message) => {
    const resp = JSON.parse(message.data);
    const oldDiv = document.getElementById(resp.itemTs)
    oldDiv.replaceWith(buildEntryDiv(resp.newValue))
}
function handleError() {
    alert('Error processing your request');
}
function patchLike(itemTs) {
    patchSocket.send(JSON.stringify({itemTs: itemTs, patchAction: 'LIKE'}));
}
function buildEntryDiv(message) {
    const div = document.createElement("div")
    const id = `${message.ts}`
    div.setAttribute("id", id)
    div.classList.add("feed-entry")
    //document.getElementById("feed").prepend(div)
    const h3 = document.createElement("h3")
    const likeCount = message.likes == null ? 0 : message.likes
    h3.innerHTML = `"${message.prompt}" - ${message.username} at ${message.ts} - <span onclick="patchLike('${message.ts}')">&#128077; (${likeCount})</span>`
    div.appendChild(h3);
    message.images.forEach(imageString => {
        const img = document.createElement("img")
        img.setAttribute("src", "data:image/png;base64," + imageString)
        div.appendChild(img)
    });
    return div;
}

function handleSuccess(message) {
    const div = buildEntryDiv(message)
    document.getElementById("feed").prepend(div)
}

function submitPrompt() {
    const prompt = document.getElementById("prompt").value

    promptSocket.send(JSON.stringify({prompt: prompt, username: username}));
    document.getElementsByTagName('button')[0].setAttribute('disabled','true')
}
