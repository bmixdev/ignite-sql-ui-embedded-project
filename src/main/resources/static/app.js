function applyTheme(theme) {
  document.documentElement.setAttribute("data-theme", theme);

  const btn = document.getElementById("themeBtn");
  if (btn) {
    btn.textContent = (theme === "dark") ? "☀️ Светлая" : "🌙 Тёмная";
  }
}

function initTheme() {
  const saved = localStorage.getItem("theme");
  if (saved === "light" || saved === "dark") {
    applyTheme(saved);
    return;
  }

  const prefersDark = window.matchMedia &&
      window.matchMedia("(prefers-color-scheme: dark)").matches;

  applyTheme(prefersDark ? "dark" : "light");
}

function toggleTheme() {
  const current = document.documentElement.getAttribute("data-theme");
  const next = (current === "dark") ? "light" : "dark";
  localStorage.setItem("theme", next);
  applyTheme(next);
}

function escapeHtml(v) {
  const s = String(v ?? "");
  return s.replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;")
      .replace(/'/g, "&#039;");
}

function renderTable(columns, rows) {
  if (!columns || columns.length === 0) return `<div class="empty">Нет колонок</div>`;
  if (!rows || rows.length === 0) return `<div class="empty">Нет строк</div>`;

  let html = `<table class="grid"><thead><tr>`;
  for (const c of columns) html += `<th>${escapeHtml(c)}</th>`;
  html += `</tr></thead><tbody>`;

  for (const r of rows) {
    html += `<tr>`;
    for (const c of columns) html += `<td>${escapeHtml(r[c])}</td>`;
    html += `</tr>`;
  }

  html += `</tbody></table>`;
  return html;
}

async function runSql() {
  const sqlEl = document.getElementById("sql");
  const hint = document.getElementById("hint");
  const meta = document.getElementById("meta");
  const out  = document.getElementById("out");
  const tableWrap = document.getElementById("tableWrap");

  const sql = sqlEl.value.replace(/;\s*$/, "");

  hint.textContent = "Выполняю…";
  meta.textContent = "";
  out.textContent = "";
  tableWrap.innerHTML = "";

  const t0 = performance.now();

  try {
    const r = await fetch("api/sql", {
      method: "POST",
      headers: {"Content-Type": "application/json"},
      body: JSON.stringify({sql})
    });

    const data = await r.json();
    out.textContent = JSON.stringify(data, null, 2);

    if (!data.ok) {
      hint.textContent = "Ошибка";
      meta.textContent = data.error ?? "Unknown error";
      return;
    }

    const elapsedMs = data.elapsedMs ?? Math.round(performance.now() - t0);
    const isSelect = Array.isArray(data.columns) && Array.isArray(data.rows);

    if (isSelect) {
      meta.textContent = `OK • rows=${data.rows.length} • ${elapsedMs} ms`;
      tableWrap.innerHTML = renderTable(data.columns, data.rows);
    } else {
      meta.textContent = `OK • updateCount=${data.updateCount ?? 0} • ${elapsedMs} ms`;
      tableWrap.innerHTML = `<div class="empty">Команда выполнена (не SELECT)</div>`;
    }

    hint.textContent = "Готово";
  } catch (e) {
    hint.textContent = "Ошибка";
    meta.textContent = "Network/JS error";
    out.textContent = String(e);
  }
}

function pickCountFromQueryResult(qr) {
  // qr = {ok, columns, rows, ...}
  if (!qr || !qr.ok || !Array.isArray(qr.rows)) return 0;
  return qr.rows.length;
}

function shortRows(rows, max = 5) {
  if (!Array.isArray(rows)) return [];
  return rows.slice(0, max);
}

function renderDiagBlock(title, qr, hintCol) {
  if (!qr || !qr.ok) {
    return `<div class="diag-card"><div class="diag-title">${escapeHtml(title)}</div><div class="diag-bad">Нет данных</div></div>`;
  }
  const count = pickCountFromQueryResult(qr);
  const rows = shortRows(qr.rows, 4);
  const cols = qr.columns || [];
  const hintText = hintCol && rows[0] && rows[0][hintCol] ? String(rows[0][hintCol]) : "";

  return `
    <div class="diag-card">
      <div class="diag-title">${escapeHtml(title)}</div>
      <div class="diag-stat">${count} rows</div>
      ${hintText ? `<div class="diag-hint">${escapeHtml(hintText)}</div>` : ""}
      <div class="diag-mini">
        ${rows.length ? renderTable(cols.slice(0, Math.min(cols.length, 6)), rows.map(r => {
    const out = {};
    for (const c of cols.slice(0, Math.min(cols.length, 6))) out[c] = r[c];
    return out;
  })) : `<div class="empty">—</div>`}
      </div>
    </div>
  `;
}

async function refreshDiagnostics() {
  const diagMeta = document.getElementById("diagMeta");
  const diagOut = document.getElementById("diagOut");

  diagMeta.textContent = "Загружаю…";
  diagOut.innerHTML = "";

  const t0 = performance.now();

  try {
    const r = await fetch("api/ignite/overview");
    const payload = await r.json();

    if (!payload.ok) {
      diagMeta.textContent = "Ошибка";
      diagOut.innerHTML = `<div class="empty">${escapeHtml(payload.error ?? "Unknown error")}</div>`;
      return;
    }

    const elapsed = Math.round(performance.now() - t0);
    diagMeta.textContent = `OK • ${elapsed} ms`;

    const data = payload.data || {};
    const nodes = data.nodes;
    const caches = data.caches;
    const schemas = data.schemas;
    const active = data.activeQueries;

    diagOut.innerHTML =
        renderDiagBlock("SYS.NODES", nodes, "CONSISTENT_ID") +
        renderDiagBlock("SYS.CACHES", caches, "NAME") +
        renderDiagBlock("SYS.SCHEMAS", schemas, "SCHEMA_NAME") +
        renderDiagBlock("SYS.SQL_QUERIES", active, "SQL");
  } catch (e) {
    diagMeta.textContent = "Ошибка";
    diagOut.innerHTML = `<div class="empty">${escapeHtml(String(e))}</div>`;
  }
}

document.getElementById("run").addEventListener("click", runSql);
document.getElementById("sql").addEventListener("keydown", (e) => {
  if ((e.ctrlKey || e.metaKey) && e.key === "Enter") runSql();
});

document.getElementById("refreshDiag").addEventListener("click", refreshDiagnostics);

// auto-load diagnostics on page open
refreshDiagnostics();

// theme init + handler
initTheme();
const themeBtn = document.getElementById("themeBtn");
if (themeBtn) themeBtn.addEventListener("click", toggleTheme);