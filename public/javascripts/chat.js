/**
 * Code for chat app
 * leovegas 2020
 **/

const inputField = document.getElementById("chat-input");
const outputArea = document.getElementById("chat-area");
const infoArea = document.getElementById("chat-info");
const activeUsers = document.getElementById("active-users");
const usersInfo = document.getElementById("users-info");
const listRoute = document.getElementById("list-route").value;

usersInfo.style.cursor = "pointer";


outputArea.scrollTop = outputArea.scrollHeight;
outputArea.readOnly = "true";
infoArea.readOnly = "true";

const socketRoute = document.getElementById("ws-route").value;
const socket = new WebSocket(socketRoute.replace("http", "ws"));

const button = document.getElementById("sendButton");
button.onclick = (event) => {
    socket.send(inputField.value);
    inputField.value = '';
    outputArea.scrollTop = outputArea.scrollHeight
};

inputField.onkeydown = (event) => {
    if (event.key === 'Enter') {
        socket.send(inputField.value);
        inputField.value = '';
        outputArea.scrollTop = outputArea.scrollHeight;
    }
};

socket.onopen = () => {
    socket.send(" logged in");
    outputArea.scrollTop = outputArea.scrollHeight;
    window.setTimeout(removeLine, 5000);
}


socket.onmessage = (event) => {
    console.log("Got "+event.data)
    if (!event.data.endsWith('tick009')){
        if (event.data.endsWith(' logged in')||event.data.endsWith(' logged out')){
            loadList();
            activeUsers.innerText = event.data;
        }else {
            outputArea.value += '\n' + event.data;
            outputArea.scrollTop = outputArea.scrollHeight;
        }
        window.setTimeout(removeLine, 10000);
    } else {
        activeUsers.innerText = "Active users: "+event.data.split(" ")[1];
    }

    };

function loadList() {
    const ul = document.getElementById("userlist");
    $("#userlist").empty();
    fetch(listRoute).then(res => res.json()).then(users => {
        for (let i = 0; i<users.length; ++i) {
            const li = document.createElement("li");
            const text = document.createTextNode(users[i]);
            li.appendChild(text);
                                                                                // li.onclick = e => {
                                                                                //     ul.innerHTML="";
                                                                                //     fetch(deleteRoute, {
                                                                                //         method: 'POST',
                                                                                //         headers: {'Content-Type':'application/json', 'Csrf-Token': csrfToken},
                                                                                //         body: JSON.stringify(i)
                                                                                //     }).then (res => res.json())
                                                                                //         .then (data => {
                                                                                //             if (data) {
                                                                                //                 loadList()
                                                                                //                 document.getElementById("task-message").innerHTML = "";
                                                                                //             }else {
                                                                                //                 document.getElementById("task-message").innerHTML = "Failed to delete";
                                                                                //             }
                                                                                //         })
                                                                                // }
            ul.appendChild(li);
        }
    })
}

function removeLine()
{
    // var removeval = outputArea.value;
    // removeval = removeval.trim();
    // var n = removeval.split("\n");
    // n.forEach(el => {
    //     if (el.endsWith(' logged in')||el.endsWith(' logged out')) {
    //         delete n[n.indexOf(el)]
    //     }
    // });
    // outputArea.value = n.join('\n');
    // n.forEach(e => {
    //
    // })
    usersInfo.innerText="";
}

/////////////////////////////////pop up js ///////////////////////////////

/////////////////////////////////pop up js ///////////////////////////////


// function sendMessge()
// {
//     socket.send("tick009");
// }
//
// function runAgain()
// {
//     window.setInterval(sendMessge, 10000);
// }
//
// runAgain();







