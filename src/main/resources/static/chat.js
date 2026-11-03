"use strict";

const form = document.querySelector("#chat-form");
const input = document.querySelector("#content");
const send = document.querySelector("#send");
const newChat = document.querySelector("#new-chat");
const messages = document.querySelector("#messages");
const welcome = document.querySelector("#welcome");
const status = document.querySelector("#status");
const errorBox = document.querySelector("#error");
const conversation = document.querySelector(".conversation");
const storageKey = "java-study-chat-id";
let chatId = null;
let busy = false;

function storedChatId() {
    try {
        const value = localStorage.getItem(storageKey);
        return /^[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}$/i.test(value || "") ? value : null;
    } catch {
        return null;
    }
}

function rememberChat(id) {
    try {
        localStorage.setItem(storageKey, id);
    } catch {
        // The chat also works when browser storage is disabled.
    }
}

function setBusy(value, message) {
    busy = value;
    newChat.disabled = value;
    input.disabled = value || !chatId;
    send.disabled = value || !chatId;
    messages.setAttribute("aria-busy", String(value));
    status.textContent = message || "Готов к твоему вопросу";
}

function showError(error) {
    errorBox.textContent = error.message;
    errorBox.hidden = false;
}

function clearError() {
    errorBox.hidden = true;
    errorBox.textContent = "";
}

async function request(path, options = {}) {
    let response;
    try {
        response = await fetch(path, {
            ...options,
            headers: { "Accept": "application/json", ...options.headers }
        });
    } catch {
        throw new Error("Could not connect to the application. Check that Spring Boot is running and try again.");
    }
    const body = await response.json().catch(() => null);
    if (!response.ok) {
        const error = new Error(body?.detail || `Request failed (${response.status}). Please try again.`);
        error.status = response.status;
        throw error;
    }
    if (body === null) {
        throw new Error("The application returned an unexpected response. Refresh the page and try again.");
    }
    return body;
}

function renderMessage(message) {
    const item = document.createElement("article");
    item.className = message.role === "USER" ? "message user" : "message assistant";
    const label = document.createElement("div");
    label.className = "message-label";
    label.textContent = message.role === "USER" ? "ТЫ" : "STUDY CHAT";
    const text = document.createElement("p");
    text.textContent = message.content;
    item.append(label, text);
    messages.append(item);
    welcome.hidden = true;
}

function scrollToLatest() {
    conversation.scrollTop = conversation.scrollHeight;
}

async function createChat() {
    const chat = await request("/api/chats", { method: "POST" });
    chatId = chat.id;
    rememberChat(chatId);
    messages.replaceChildren();
    welcome.hidden = false;
}

async function initialize() {
    setBusy(true, "Открываем диалог…");
    clearError();
    try {
        const savedId = storedChatId();
        if (savedId) {
            try {
                const history = await request(`/api/chats/${savedId}/chatMessages`);
                chatId = savedId;
                history.forEach(renderMessage);
            } catch (error) {
                if (error.status !== 404) throw error;
                await createChat();
            }
        } else {
            await createChat();
        }
    } catch (error) {
        showError(error);
    } finally {
        setBusy(false, chatId ? undefined : "Нажми «Новый диалог», чтобы повторить подключение");
        scrollToLatest();
    }
}

newChat.addEventListener("click", async () => {
    if (busy) return;
    clearError();
    setBusy(true, "Создаём диалог…");
    try {
        await createChat();
        input.value = "";
    } catch (error) {
        showError(error);
    } finally {
        setBusy(false, chatId ? undefined : "Could not create a chat");
        if (chatId) input.focus();
    }
});

form.addEventListener("submit", async (event) => {
    event.preventDefault();
    if (busy || !chatId) return;
    const content = input.value.trim();
    if (!content) return;
    clearError();
    setBusy(true, "Ollama готовит ответ… Первый запрос может занять больше времени.");
    try {
        const reply = await request(`/api/chats/${chatId}/chatMessages`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ content })
        });
        renderMessage({ role: "USER", content });
        renderMessage(reply);
        input.value = "";
        scrollToLatest();
    } catch (error) {
        showError(error);
    } finally {
        setBusy(false);
        input.focus();
    }
});

input.addEventListener("keydown", (event) => {
    if (event.key === "Enter" && !event.shiftKey && !event.isComposing) {
        event.preventDefault();
        form.requestSubmit();
    }
});

document.querySelectorAll("[data-prompt]").forEach(button => {
    button.addEventListener("click", () => {
        if (busy || !chatId) return;
        input.value = button.dataset.prompt;
        input.focus();
    });
});

initialize();
