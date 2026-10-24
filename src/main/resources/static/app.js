"use strict";

const resources = {
    admins: {
        label: "Admins",
        singular: "Admin",
        endpoint: "/api/admins",
        roles: ["ADMIN"],
        fields: [
            { key: "office", label: "Office", required: true }
        ]
    },
    teachers: {
        label: "Teachers",
        singular: "Teacher",
        endpoint: "/api/teachers",
        roles: ["ADMIN", "TEACHER"],
        fields: [
            { key: "department", label: "Department", required: true },
            { key: "designation", label: "Designation", required: true }
        ]
    },
    students: {
        label: "Students",
        singular: "Student",
        endpoint: "/api/students",
        roles: ["ADMIN", "STUDENT"],
        fields: [
            { key: "rollNumber", label: "Roll number", required: true },
            { key: "department", label: "Department", required: true }
        ]
    }
};

const commonFields = [
    { key: "username", label: "Username", required: true },
    { key: "password", label: "Password", type: "password", required: true },
    { key: "fullName", label: "Full name", required: true },
    { key: "email", label: "Email", type: "email", required: true }
];

const state = { user: null, resourceKey: null, rows: [], editingId: null };
const $ = (selector) => document.querySelector(selector);
const elements = {
    loginView: $("#login-view"), appView: $("#app-view"), loginForm: $("#login-form"),
    loginError: $("#login-error"), loginButton: $("#login-button"), tabs: $("#resource-tabs"),
    sectionTitle: $("#section-title"), search: $("#search-input"), table: $("#table-container"),
    modal: $("#modal"), modalTitle: $("#modal-title"), modalError: $("#modal-error"),
    fields: $("#form-fields"), recordForm: $("#record-form"), saveButton: $("#save-button"),
    toast: $("#toast")
};
let toastTimer;

function escapeHtml(value) {
    return String(value ?? "").replace(/[&<>'"]/g, (character) => ({
        "&": "&amp;", "<": "&lt;", ">": "&gt;", "'": "&#39;", '"': "&quot;"
    })[character]);
}

async function readJson(response) {
    const text = await response.text();
    if (!text) return {};
    try { return JSON.parse(text); } catch { return {}; }
}

async function api(url, options = {}, handleExpiredSession = true) {
    const response = await fetch(url, options);
    const body = await readJson(response);
    if (response.status === 401 && handleExpiredSession) {
        showLogin("Your session has ended. Please sign in again.");
        throw new Error("SESSION_ENDED");
    }
    if (!response.ok) {
        const error = new Error(body.message || body.error || `Request failed (${response.status})`);
        error.status = response.status;
        throw error;
    }
    return body;
}

function showLogin(message = "") {
    state.user = null;
    state.rows = [];
    closeModal();
    elements.appView.classList.add("hidden");
    elements.loginView.classList.remove("hidden");
    elements.loginError.textContent = message;
    elements.loginForm.reset();
    $("#login-username").focus();
}

function allowedResources() {
    return Object.entries(resources).filter(([, config]) => config.roles.includes(state.user.role));
}

function showApp(user) {
    state.user = user;
    elements.loginView.classList.add("hidden");
    elements.appView.classList.remove("hidden");
    $("#user-full-name").textContent = user.fullName;
    const badge = $("#role-badge");
    badge.textContent = user.role;
    badge.className = `role-badge role-${user.role.toLowerCase()}`;
    renderTabs();
    const first = allowedResources()[0];
    if (first) selectResource(first[0]);
}

function renderTabs() {
    elements.tabs.innerHTML = allowedResources().map(([key, config]) =>
        `<button class="tab" type="button" data-resource="${escapeHtml(key)}">${escapeHtml(config.label)}</button>`
    ).join("");
}

async function selectResource(key) {
    state.resourceKey = key;
    elements.search.value = "";
    elements.sectionTitle.textContent = resources[key].label;
    elements.tabs.querySelectorAll(".tab").forEach((tab) =>
        tab.classList.toggle("active", tab.dataset.resource === key));
    elements.table.innerHTML = '<div class="loading-state">Loading records...</div>';
    try {
        state.rows = await api(resources[key].endpoint);
        renderTable();
    } catch (error) {
        if (error.message !== "SESSION_ENDED") {
            state.rows = [];
            renderTable();
            showToast(error.message, "error");
        }
    }
}

function visibleFields() {
    return commonFields.filter((field) => field.key !== "password").concat(resources[state.resourceKey].fields);
}

function renderTable() {
    const fields = visibleFields();
    const query = elements.search.value.trim().toLowerCase();
    const rows = state.rows.filter((row) =>
        [row.id, ...fields.map((field) => row[field.key])]
            .some((value) => String(value ?? "").toLowerCase().includes(query)));

    if (!rows.length) {
        elements.table.innerHTML = `<div class="empty-state">${query ? "No matching records found." : "No records yet. Add the first one."}</div>`;
        return;
    }

    const headings = ["ID", ...fields.map((field) => field.label), "Actions"];
    elements.table.innerHTML = `<table>
        <thead><tr>${headings.map((heading) => `<th>${escapeHtml(heading)}</th>`).join("")}</tr></thead>
        <tbody>${rows.map((row) => {
            const isCurrentUser = row.username === state.user.username;
            const cells = fields.map((field) => {
                const tag = field.key === "username" && isCurrentUser ? '<span class="you-tag">you</span>' : "";
                return `<td>${escapeHtml(row[field.key])}${tag}</td>`;
            }).join("");
            return `<tr><td>${escapeHtml(row.id)}</td>${cells}<td><div class="row-actions">
                <button class="action-button" type="button" data-action="edit" data-id="${escapeHtml(row.id)}">Edit</button>
                <button class="action-button danger" type="button" data-action="delete" data-id="${escapeHtml(row.id)}" ${isCurrentUser ? "disabled title=\"You cannot delete your own account\"" : ""}>Delete</button>
            </div></td></tr>`;
        }).join("")}</tbody>
    </table>`;
}

function openModal(row = null) {
    state.editingId = row?.id ?? null;
    const config = resources[state.resourceKey];
    elements.modalTitle.textContent = `${row ? "Edit" : "Add"} ${config.singular}`;
    elements.modalError.textContent = "";
    const fields = commonFields.concat(config.fields);
    elements.fields.innerHTML = fields.map((field) => {
        const editingPassword = row && field.key === "password";
        const label = editingPassword ? `${field.label} (leave blank to keep current)` : field.label;
        const value = field.key === "password" ? "" : row?.[field.key] ?? "";
        const required = field.required && !editingPassword ? "required" : "";
        const fullWidth = ["fullName", "email"].includes(field.key) ? " full-width" : "";
        return `<div class="field${fullWidth}">
            <label for="field-${escapeHtml(field.key)}">${escapeHtml(label)}</label>
            <input id="field-${escapeHtml(field.key)}" name="${escapeHtml(field.key)}" type="${escapeHtml(field.type || "text")}" value="${escapeHtml(value)}" ${required}>
        </div>`;
    }).join("");
    elements.modal.classList.remove("hidden");
    elements.fields.querySelector("input")?.focus();
}

function closeModal() {
    elements.modal.classList.add("hidden");
    state.editingId = null;
    elements.recordForm.reset();
    elements.modalError.textContent = "";
}

async function saveRecord(event) {
    event.preventDefault();
    if (!elements.recordForm.reportValidity()) return;
    const config = resources[state.resourceKey];
    const formData = new FormData(elements.recordForm);
    const body = {};
    commonFields.concat(config.fields).forEach((field) => {
        const value = String(formData.get(field.key) ?? "").trim();
        if (!(state.editingId && field.key === "password" && !value)) body[field.key] = value;
    });
    elements.modalError.textContent = "";
    elements.saveButton.disabled = true;
    try {
        const editing = state.editingId !== null;
        await api(`${config.endpoint}${editing ? `/${state.editingId}` : ""}`, {
            method: editing ? "PUT" : "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(body)
        });
        closeModal();
        showToast(editing ? "Updated successfully" : "Created successfully", "success");
        await selectResource(state.resourceKey);
    } catch (error) {
        if (error.message !== "SESSION_ENDED") elements.modalError.textContent = error.message;
    } finally {
        elements.saveButton.disabled = false;
    }
}

async function deleteRecord(id) {
    if (!confirm("Delete this record? This action cannot be undone.")) return;
    try {
        await api(`${resources[state.resourceKey].endpoint}/${id}`, { method: "DELETE" });
        showToast("Deleted successfully", "success");
        await selectResource(state.resourceKey);
    } catch (error) {
        if (error.message !== "SESSION_ENDED") showToast(error.message, "error");
    }
}

function showToast(message, type = "success") {
    clearTimeout(toastTimer);
    elements.toast.textContent = message;
    elements.toast.className = `toast show ${type}`;
    toastTimer = setTimeout(() => { elements.toast.className = "toast"; }, 3200);
}

elements.loginForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    elements.loginError.textContent = "";
    elements.loginButton.disabled = true;
    const data = new URLSearchParams(new FormData(elements.loginForm));
    try {
        await api("/api/auth/login", { method: "POST", body: data }, false);
        const user = await api("/api/auth/me");
        showApp(user);
    } catch (error) {
        if (error.status === 401) elements.loginError.textContent = "Invalid username or password";
        else if (error.message !== "SESSION_ENDED") elements.loginError.textContent = error.message;
    } finally {
        elements.loginButton.disabled = false;
    }
});

$("#logout-button").addEventListener("click", async () => {
    try { await api("/api/auth/logout", { method: "POST" }, false); }
    catch (error) { showToast(error.message, "error"); }
    showLogin();
});

elements.tabs.addEventListener("click", (event) => {
    const tab = event.target.closest("[data-resource]");
    if (tab) selectResource(tab.dataset.resource);
});
elements.search.addEventListener("input", renderTable);
$("#add-button").addEventListener("click", () => openModal());
elements.table.addEventListener("click", (event) => {
    const button = event.target.closest("[data-action]");
    if (!button) return;
    const id = Number(button.dataset.id);
    const row = state.rows.find((item) => item.id === id);
    if (button.dataset.action === "edit" && row) openModal(row);
    if (button.dataset.action === "delete") deleteRecord(id);
});
elements.recordForm.addEventListener("submit", saveRecord);
$("#modal-close").addEventListener("click", closeModal);
$("#modal-cancel").addEventListener("click", closeModal);
elements.modal.addEventListener("click", (event) => { if (event.target === elements.modal) closeModal(); });
document.addEventListener("keydown", (event) => {
    if (event.key === "Escape" && !elements.modal.classList.contains("hidden")) closeModal();
});

(async function initialize() {
    try { showApp(await api("/api/auth/me", {}, false)); }
    catch { showLogin(); }
})();
