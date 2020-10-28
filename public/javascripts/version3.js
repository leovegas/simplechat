"use strict"

const validateRoute = document.getElementById("validateRoute").value;
const createRoute = document.getElementById("createRoute").value;
const deleteRoute = document.getElementById("deleteRoute").value;
const addRoute = document.getElementById("addRoute").value;
const csrfToken = document.getElementById("csrfToken").value;
const tasksRoute = document.getElementById("tasksRoute").value;
const restorePassRoute = document.getElementById("restoreRoute").value;
const newPassRoute = document.getElementById("newPassRoute").value;


const passwordField = document.getElementById("loginPass");

function onEnter() {
    passwordField.onkeydown = (e) => {
        if (e.key === 'Enter') {
            login();
        }
    };
    document.getElementById("loginName").onclick = e => {
        document.getElementById("loginName").value = "";
    }
    document.getElementById("email").onclick = e => {
        document.getElementById("email").value = "";
        document.getElementById("email-alert").hidden = true;
    }
}

onEnter();

function login() {
    const email = document.getElementById("email").value;
    const username = document.getElementById("loginName").value;
    const password = document.getElementById("loginPass").value;
    const status = true;

    fetch(validateRoute, {
        method: 'POST',
        headers: {'Content-Type': 'application/json', 'Csrf-Token': csrfToken},
        body: JSON.stringify({username, password, status, email})
    }).then(res => res.json())
        .then(data => {
            if (data) {
                window.location = "/chat";
                document.getElementById("loginName").value = "";
            } else {
                document.getElementById("loginName").value = "Login Failed";
                document.getElementById("loginPass").value = "";
            }
        })

}


function loadTasks() {
    const ul = document.getElementById("task-list");
    fetch(tasksRoute).then(res => res.json()).then(tasks => {
        for (let i = 0; i < tasks.length; ++i) {
            const li = document.createElement("li");
            const text = document.createTextNode(tasks[i]);
            li.appendChild(text);
            li.onclick = e => {
                ul.innerHTML = "";
                fetch(deleteRoute, {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json', 'Csrf-Token': csrfToken},
                    body: JSON.stringify(i)
                }).then(res => res.json())
                    .then(data => {
                        if (data) {
                            loadTasks()
                            document.getElementById("task-message").innerHTML = "";
                        } else {
                            document.getElementById("task-message").innerHTML = "Failed to delete";
                        }
                    })
            }
            ul.appendChild(li);
        }
    })
}

function addTask() {
    const task = document.getElementById("newTask").value;
    const ul = document.getElementById("task-list");
    fetch(addRoute, {
        method: 'POST',
        headers: {'Content-Type': 'application/json', 'Csrf-Token': csrfToken},
        body: JSON.stringify(task)
    }).then(res => res.json())
        .then(data => {
            if (data) {
                const li = document.createElement("li");
                const text = document.createTextNode(task);
                li.appendChild(text);
                ul.appendChild(li);
                document.getElementById("newTask").value = "";
                document.getElementById("task-message").innerHTML = "";

            } else {
                document.getElementById("task-message").innerHTML = "Failed to add";
            }
        })
}

function visibleRestorePass() {
    document.getElementById("restore-section").hidden = false;
    document.getElementById("login-section").hidden = true;
    document.getElementById("register-section").hidden = true;
    document.getElementById("newpass-section").hidden = true;
}

function visibleCreateUser() {
    document.getElementById("login-section").hidden = true;
    document.getElementById("register-section").hidden = false;
    document.getElementById("email").innerText = "";
    document.getElementById("createName").innerText = "";
    document.getElementById("createPass").innerText = "";
}

function visibleLogin() {
    document.getElementById("login-section").hidden = false;
    document.getElementById("register-section").hidden = true;
    document.getElementById("restore-section").hidden = true;
    document.getElementById("newpass-section").hidden = true;

}

function visibleNewpass() {
    document.getElementById("login-section").hidden = true;
    document.getElementById("register-section").hidden = true;
    document.getElementById("restore-section").hidden = true;
    document.getElementById("newpass-section").hidden = false;
}


function logout() {
    const username = document.getElementById("createName").value;
    const password = document.getElementById("createPass").value;
    const status = false;

    fetch(createRoute, {
        method: 'POST',
        headers: {'Content-Type': 'application/json', 'Csrf-Token': csrfToken},
        body: JSON.stringify({username, password, status})
    }).then(res => res.json())
        .then(data => {
            if (data) {
                document.getElementById("login-section").hidden = false;
                document.getElementById("register-section").hidden = true;
                document.getElementById("create-message").innerHTML = "";
                loadTasks();
            } else {
                document.getElementById("create-message").innerHTML = "Creating Failed";
            }
        })
}

function createUser() {
    const email = document.getElementById("email").value;
    const username = document.getElementById("createName").value;
    const password = document.getElementById("createPass").value;
    const status = false;

    if (isEmailOkForRegister()) {
        fetch(createRoute, {
            method: 'POST',
            headers: {'Content-Type': 'application/json', 'Csrf-Token': csrfToken},
            body: JSON.stringify({username, password, status, email})
        }).then(res => res.json())
            .then(data => {
                if (data) {
                    visibleLogin();
                } else {
                    document.getElementById("email-alert").innerText = "Email already exists"
                    document.getElementById("email-alert").hidden = false;
                }
            })
    } else {
        document.getElementById("email-alert").innerText = "Email is required"
        document.getElementById("email-alert").hidden = false;
    }

}

function isEmailOkForRecovery() {
    var isValid = false;
    if (document.getElementById("email-recovery").getAttribute('type') === 'email' || document.getElementById("email-recovery").getAttribute('name') === 'email') {
        if (document.getElementById("email-recovery").value.trim().match(/^([a-zA-Z0-9_\-\.]+)@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.)|(([a-zA-Z0-9\-]+\.)+))([a-zA-Z]{1,5}|[0-9]{1,3})(\]?)$/) == null) {
            isValid = false;
        } else isValid = true;
    } else {
        if (document.getElementById("email-recovery").value.trim() === '') {
            isValid = false;
        } else isValid = true;
    }

    return isValid
}

function isEmailOkForRegister() {
    var isValid = false;
    if (document.getElementById("email").getAttribute('type') === 'email' || document.getElementById("email").getAttribute('name') === 'email') {
        if (document.getElementById("email").value.trim().match(/^([a-zA-Z0-9_\-\.]+)@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.)|(([a-zA-Z0-9\-]+\.)+))([a-zA-Z]{1,5}|[0-9]{1,3})(\]?)$/) == null) {
            isValid = false;
        } else isValid = true;
    } else {
        if (document.getElementById("email").value.trim() === '') {
            isValid = false;
        } else isValid = true;
    }

    return isValid
}

function restorePassword() {
    const email = document.getElementById("email-recovery").value;

    if (isEmailOkForRecovery()) {
        fetch(restorePassRoute, {
            method: 'POST',
            headers: {'Content-Type': 'application/json', 'Csrf-Token': csrfToken},
            body: JSON.stringify(email)
        }).then(res => res.json())
            .then(data => {
                if (data) {
                    visibleNewpass()
                } else {
                    document.getElementById("email-alert-recovery").innerText = "Email not found"
                    document.getElementById("email-alert-recovery").hidden = false;
                }
            })
    } else {
        document.getElementById("email-alert-recovery").innerText = "Email is required"
        document.getElementById("email-alert-recovery").hidden = false;
    }
}


function newPass() {
    const newpass = document.getElementById("newpass").value;

    fetch(newPassRoute, {
        method: 'POST',
        headers: {'Content-Type': 'application/json', 'Csrf-Token': csrfToken},
        body: JSON.stringify(newpass)
    }).then(res => res.json())
        .then(data => {
            if (data) {
                visibleLogin()
            } else {
                document.getElementById("email-alert-recovery").innerText = "Email not found"
                document.getElementById("email-alert-recovery").hidden = false;
            }
        })

}