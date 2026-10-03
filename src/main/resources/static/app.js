const notice = document.querySelector("#notice");
const taskRows = document.querySelector("#task-rows");
let currentTasks = [];
let noticeTimer;
let configurationDirty = false;

async function apiRequest(url, options = {}) {
  const response = await fetch(url, {
    headers: { "Content-Type": "application/json", ...options.headers },
    ...options,
  });

  if (!response.ok) {
    let message = `Request failed (${response.status})`;
    try {
      const error = await response.json();
      message = error.message || message;
    } catch {
      // Keep the HTTP status message when the response is not JSON.
    }
    throw new Error(message);
  }

  if (response.status === 204) {
    return null;
  }
  return response.json();
}

function showNotice(message, isError = false) {
  clearTimeout(noticeTimer);
  notice.textContent = message;
  notice.classList.toggle("notice-error", isError);
  notice.hidden = false;
  noticeTimer = setTimeout(() => {
    notice.hidden = true;
  }, 4500);
}

function formatNumber(value, digits = 2) {
  const number = Number(value);
  return Number.isFinite(number) ? number.toFixed(digits) : "0.00";
}

function formatTime(value) {
  if (!value) {
    return "-";
  }
  const date = new Date(value);
  return Number.isNaN(date.getTime())
    ? "-"
    : date.toLocaleTimeString([], {
        hour: "2-digit",
        minute: "2-digit",
        second: "2-digit",
      });
}

function addCell(row, content, className = "") {
  const cell = document.createElement("td");
  if (className) {
    cell.className = className;
  }
  cell.textContent = content;
  row.append(cell);
  return cell;
}

function renderTasks(tasks) {
  currentTasks = tasks;
  const query = document
    .querySelector("#task-filter")
    .value.trim()
    .toLowerCase();
  const visibleTasks = tasks.filter((task) =>
    task.name.toLowerCase().includes(query),
  );
  document.querySelector("#task-total").textContent = String(
    tasks.length,
  ).padStart(2, "0");
  taskRows.replaceChildren();

  if (visibleTasks.length === 0) {
    const row = document.createElement("tr");
    const cell = addCell(
      row,
      tasks.length
        ? "No tasks match this filter."
        : "No tasks in the queue yet.",
      "empty-state",
    );
    cell.colSpan = 8;
    taskRows.append(row);
    return;
  }

  for (const task of visibleTasks) {
    const row = document.createElement("tr");
    const nameCell = document.createElement("td");
    const name = document.createElement("span");
    name.className = "task-name";
    name.textContent = task.name;
    const id = document.createElement("span");
    id.className = "task-id";
    id.textContent = `JOB-${String(task.id).padStart(4, "0")}`;
    nameCell.append(name, id);
    row.append(nameCell);
    addCell(row, String(task.priority), "numeric-cell");
    addCell(row, String(task.burstTime), "numeric-cell");
    const statusCell = addCell(
      row,
      task.status,
      `status-badge status-${task.status}`,
    );
    statusCell.setAttribute("aria-label", `Status: ${task.status}`);
    addCell(row, task.workerId == null ? "-" : String(task.workerId));
    addCell(row, formatTime(task.startedAt), "numeric-cell");
    addCell(row, formatTime(task.completedAt), "numeric-cell");

    const actionCell = document.createElement("td");
    if (task.status === "WAITING") {
      const cancel = document.createElement("button");
      cancel.className = "cancel-task";
      cancel.type = "button";
      cancel.textContent = "Cancel";
      cancel.setAttribute("aria-label", `Cancel ${task.name}`);
      cancel.addEventListener("click", () => cancelTask(task));
      actionCell.append(cancel);
    }
    row.append(actionCell);
    taskRows.append(row);
  }
}

function renderScheduler(scheduler) {
  const isRunning = scheduler.status === "RUNNING";
  document.querySelector("#scheduler-state").textContent = scheduler.status;
  document
    .querySelector("#state-indicator")
    .classList.toggle("is-running", isRunning);
  document.querySelector("#start-button").disabled = isRunning;
  document.querySelector("#stop-button").disabled = !isRunning;
  if (!configurationDirty) {
    document.querySelector("#algorithm-input").value = scheduler.algorithm;
    document.querySelector("#workers-input").value = scheduler.workerCount;
    document.querySelector("#quantum-input").value = scheduler.timeQuantum;
    updateQuantumVisibility();
  }
}

function renderStatistics(statistics) {
  document.querySelector("#completed-count").textContent =
    statistics.completedTasks;
  document.querySelector("#failed-count").textContent = statistics.failedTasks;
  document.querySelector("#throughput-value").textContent = formatNumber(
    statistics.throughput,
    2,
  );
  document.querySelector("#waiting-average").textContent = formatNumber(
    statistics.averageWaitingTime,
  );
  document.querySelector("#turnaround-average").textContent = formatNumber(
    statistics.averageTurnaroundTime,
  );
  document.querySelector("#response-average").textContent = formatNumber(
    statistics.averageResponseTime,
  );
}

async function refreshDashboard(showError = true) {
  try {
    const [tasks, scheduler, statistics] = await Promise.all([
      apiRequest("/api/tasks"),
      apiRequest("/api/scheduler/status"),
      apiRequest("/api/statistics"),
    ]);
    renderTasks(tasks);
    renderScheduler(scheduler);
    renderStatistics(statistics);
    document.querySelector("#last-updated").textContent =
      `Synced ${new Date().toLocaleTimeString()}`;
  } catch (error) {
    if (showError) {
      showNotice(error.message, true);
    }
  }
}

async function runScheduler(action) {
  try {
    await apiRequest(`/api/scheduler/${action}`, { method: "POST" });
    await refreshDashboard(false);
    showNotice(
      action === "start" ? "Scheduler started." : "Scheduler stopped.",
    );
  } catch (error) {
    showNotice(error.message, true);
  }
}

async function saveConfiguration(event) {
  event.preventDefault();
  const algorithm = document.querySelector("#algorithm-input").value;
  const workerCount = Number(document.querySelector("#workers-input").value);
  const timeQuantum = Number(document.querySelector("#quantum-input").value);
  if (
    !Number.isInteger(workerCount) ||
    workerCount < 1 ||
    !Number.isInteger(timeQuantum) ||
    timeQuantum < 1
  ) {
    showNotice(
      "Workers and time quantum must be positive whole numbers.",
      true,
    );
    return;
  }

  try {
    await apiRequest("/api/scheduler/algorithm", {
      method: "PUT",
      body: JSON.stringify({ algorithm }),
    });
    await apiRequest("/api/scheduler/workers", {
      method: "PUT",
      body: JSON.stringify({ workerCount }),
    });
    await apiRequest("/api/scheduler/time-quantum", {
      method: "PUT",
      body: JSON.stringify({ timeQuantum }),
    });
    configurationDirty = false;
    await refreshDashboard(false);
    showNotice("Scheduler configuration updated.");
  } catch (error) {
    showNotice(error.message, true);
  }
}

async function createTask(event) {
  event.preventDefault();
  const form = event.currentTarget;
  const formData = new FormData(form);
  const task = {
    name: String(formData.get("name")).trim(),
    priority: Number(formData.get("priority")),
    burstTime: Number(formData.get("burstTime")),
  };

  try {
    await apiRequest("/api/tasks", {
      method: "POST",
      body: JSON.stringify(task),
    });
    form.reset();
    document.querySelector("#task-priority").value = "5";
    document.querySelector("#task-burst").value = "1";
    await refreshDashboard(false);
    showNotice("Task added to the queue.");
  } catch (error) {
    showNotice(error.message, true);
  }
}

async function cancelTask(task) {
  try {
    await apiRequest(`/api/tasks/${task.id}/cancel`, { method: "POST" });
    await refreshDashboard(false);
    showNotice(`Cancelled ${task.name}.`);
  } catch (error) {
    showNotice(error.message, true);
  }
}

function updateQuantumVisibility() {
  document.querySelector("#quantum-field").hidden =
    document.querySelector("#algorithm-input").value !== "ROUND_ROBIN";
}

document
  .querySelector("#refresh-button")
  .addEventListener("click", () => refreshDashboard());
document
  .querySelector("#start-button")
  .addEventListener("click", () => runScheduler("start"));
document
  .querySelector("#stop-button")
  .addEventListener("click", () => runScheduler("stop"));
document
  .querySelector("#configuration-form")
  .addEventListener("submit", saveConfiguration);
document.querySelector("#configuration-form").addEventListener("input", () => {
  configurationDirty = true;
});
document.querySelector("#configuration-form").addEventListener("change", () => {
  configurationDirty = true;
});
document
  .querySelector("#create-task-form")
  .addEventListener("submit", createTask);
document
  .querySelector("#task-filter")
  .addEventListener("input", () => renderTasks(currentTasks));
document
  .querySelector("#algorithm-input")
  .addEventListener("change", updateQuantumVisibility);

refreshDashboard();
setInterval(() => refreshDashboard(false), 5000);
