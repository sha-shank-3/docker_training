from flask import Flask, jsonify, request
from flask_cors import CORS
import psycopg2
import redis
import os
import json
import datetime

app = Flask(__name__)
CORS(app)

# ── Config from environment variables ─────────────────────────────────────────
DB_HOST = os.getenv("DB_HOST", "postgres")
DB_PORT = os.getenv("DB_PORT", "5432")
DB_NAME = os.getenv("DB_NAME", "appdb")
DB_USER = os.getenv("DB_USER", "appuser")
DB_PASS = os.getenv("DB_PASS", "apppass")

REDIS_HOST = os.getenv("REDIS_HOST", "redis")
REDIS_PORT = int(os.getenv("REDIS_PORT", "6379"))

# ── Database connection ───────────────────────────────────────────────────────
def get_db():
    return psycopg2.connect(
        host=DB_HOST, port=DB_PORT,
        dbname=DB_NAME, user=DB_USER, password=DB_PASS
    )

def init_db():
    conn = get_db()
    cur = conn.cursor()
    cur.execute("""
        CREATE TABLE IF NOT EXISTS tasks (
            id SERIAL PRIMARY KEY,
            title VARCHAR(200) NOT NULL,
            completed BOOLEAN DEFAULT FALSE,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
    """)
    conn.commit()
    cur.close()
    conn.close()

# ── Redis cache ───────────────────────────────────────────────────────────────
cache = redis.Redis(host=REDIS_HOST, port=REDIS_PORT, decode_responses=True)

def cache_tasks(tasks):
    cache.setex("tasks", 30, json.dumps(tasks))

def get_cached_tasks():
    data = cache.get("tasks")
    return json.loads(data) if data else None


# ── Routes ────────────────────────────────────────────────────────────────────
@app.route("/")
def home():
    return """
    <!DOCTYPE html>
    <html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Task Manager - Python Backend</title>
        <style>
            * { box-sizing: border-box; margin: 0; padding: 0; }
            body {
                font-family: 'Segoe UI', sans-serif;
                background: linear-gradient(135deg, #1e3c72, #2a5298);
                min-height: 100vh; color: #eee; padding: 30px 20px;
            }
            .container { max-width: 700px; margin: 0 auto; }
            h1 { text-align: center; font-size: 2.2em; margin-bottom: 8px; color: #ffd700; }
            .sub { text-align: center; color: #a4b0be; margin-bottom: 30px; }
            .badge { background: #27ae60; color: #fff; padding: 4px 12px; border-radius: 12px;
                     font-size: 0.8em; display: inline-block; margin-bottom: 20px; }

            .add-form { display: flex; gap: 10px; margin-bottom: 24px; }
            .add-form input {
                flex: 1; padding: 12px 16px; border-radius: 8px; border: none;
                font-size: 1em; background: rgba(255,255,255,0.1); color: #eee;
            }
            .add-form input::placeholder { color: #888; }
            .add-form button {
                padding: 12px 24px; border-radius: 8px; border: none;
                background: #ffd700; color: #1e3c72; font-weight: bold; cursor: pointer;
                font-size: 1em;
            }
            .add-form button:hover { background: #ffed4a; }

            .task-list { list-style: none; }
            .task-item {
                background: rgba(255,255,255,0.08); border-radius: 10px;
                padding: 14px 18px; margin-bottom: 10px; display: flex;
                align-items: center; justify-content: space-between;
                border: 1px solid rgba(255,255,255,0.1);
            }
            .task-item.done .task-title { text-decoration: line-through; color: #666; }
            .task-title { flex: 1; font-size: 1.05em; }
            .task-time { color: #888; font-size: 0.8em; margin-right: 12px; }
            .btn-done, .btn-del {
                padding: 6px 14px; border-radius: 6px; border: none; cursor: pointer;
                font-size: 0.85em; margin-left: 6px;
            }
            .btn-done { background: #27ae60; color: #fff; }
            .btn-del  { background: #e74c3c; color: #fff; }

            .stats { display: flex; gap: 16px; justify-content: center; margin-bottom: 20px; }
            .stat { background: rgba(255,255,255,0.06); padding: 14px 20px; border-radius: 10px;
                    text-align: center; min-width: 120px; }
            .stat .num { font-size: 1.8em; font-weight: bold; color: #ffd700; }
            .stat .lbl { font-size: 0.8em; color: #a4b0be; text-transform: uppercase; }

            .info { text-align: center; margin-top: 30px; }
            .info a { color: #ffd700; text-decoration: none; padding: 8px 18px;
                      border: 1px solid #ffd700; border-radius: 8px; margin: 0 6px; }
            .info a:hover { background: #ffd700; color: #1e3c72; }
            #cache-status { text-align: center; font-size: 0.85em; color: #888; margin-bottom: 16px; }
        </style>
    </head>
    <body>
    <div class="container">
        <h1>📋 Task Manager</h1>
        <p class="sub">Python Flask + PostgreSQL + Redis</p>
        <div style="text-align:center;"><span class="badge">🐍 Python Backend</span></div>

        <div class="stats">
            <div class="stat"><div class="num" id="total">0</div><div class="lbl">Total</div></div>
            <div class="stat"><div class="num" id="done">0</div><div class="lbl">Done</div></div>
            <div class="stat"><div class="num" id="pending">0</div><div class="lbl">Pending</div></div>
        </div>

        <div id="cache-status"></div>

        <div class="add-form">
            <input type="text" id="taskInput" placeholder="Add a new task..." onkeydown="if(event.key==='Enter')addTask()">
            <button onclick="addTask()">Add</button>
        </div>

        <ul class="task-list" id="taskList"></ul>

        <div class="info">
            <a href="/api/health">Health Check</a>
            <a href="/api/tasks">API: Tasks</a>
            <a href="/api/stats">API: Stats</a>
            <a href="http://localhost:8080" target="_blank">Java App</a>
        </div>
    </div>

    <script>
        async function loadTasks() {
            const res = await fetch('/api/tasks');
            const data = await res.json();
            const list = document.getElementById('taskList');
            list.innerHTML = '';
            const tasks = data.tasks;
            document.getElementById('cache-status').textContent =
                data.source === 'cache' ? '⚡ Loaded from Redis cache' : '🗄️ Loaded from PostgreSQL';
            let doneCount = 0;
            tasks.forEach(t => {
                if (t.completed) doneCount++;
                list.innerHTML += `
                    <li class="task-item ${t.completed ? 'done' : ''}">
                        <span class="task-title">${t.title}</span>
                        <span class="task-time">${t.created_at || ''}</span>
                        ${!t.completed ? `<button class="btn-done" onclick="toggleTask(${t.id})">✓</button>` : ''}
                        <button class="btn-del" onclick="deleteTask(${t.id})">✕</button>
                    </li>`;
            });
            document.getElementById('total').textContent = tasks.length;
            document.getElementById('done').textContent = doneCount;
            document.getElementById('pending').textContent = tasks.length - doneCount;
        }
        async function addTask() {
            const input = document.getElementById('taskInput');
            if (!input.value.trim()) return;
            await fetch('/api/tasks', {
                method: 'POST', headers: {'Content-Type':'application/json'},
                body: JSON.stringify({title: input.value})
            });
            input.value = '';
            loadTasks();
        }
        async function toggleTask(id) {
            await fetch(`/api/tasks/${id}`, {method: 'PUT'});
            loadTasks();
        }
        async function deleteTask(id) {
            await fetch(`/api/tasks/${id}`, {method: 'DELETE'});
            loadTasks();
        }
        loadTasks();
    </script>
    </body>
    </html>
    """


@app.route("/api/health")
def health():
    status = {"app": "UP", "timestamp": str(datetime.datetime.now())}
    try:
        conn = get_db()
        conn.close()
        status["database"] = "UP"
    except Exception as e:
        status["database"] = f"DOWN: {e}"
    try:
        cache.ping()
        status["cache"] = "UP"
    except Exception as e:
        status["cache"] = f"DOWN: {e}"
    return jsonify(status)


@app.route("/api/tasks", methods=["GET"])
def get_tasks():
    cached = get_cached_tasks()
    if cached:
        return jsonify({"tasks": cached, "source": "cache"})
    conn = get_db()
    cur = conn.cursor()
    cur.execute("SELECT id, title, completed, created_at FROM tasks ORDER BY created_at DESC")
    rows = cur.fetchall()
    tasks = [{"id": r[0], "title": r[1], "completed": r[2], "created_at": str(r[3])} for r in rows]
    cur.close()
    conn.close()
    cache_tasks(tasks)
    return jsonify({"tasks": tasks, "source": "database"})


@app.route("/api/tasks", methods=["POST"])
def create_task():
    data = request.get_json()
    title = data.get("title", "").strip()
    if not title:
        return jsonify({"error": "title is required"}), 400
    conn = get_db()
    cur = conn.cursor()
    cur.execute("INSERT INTO tasks (title) VALUES (%s) RETURNING id", (title,))
    task_id = cur.fetchone()[0]
    conn.commit()
    cur.close()
    conn.close()
    cache.delete("tasks")
    return jsonify({"id": task_id, "title": title}), 201


@app.route("/api/tasks/<int:task_id>", methods=["PUT"])
def toggle_task(task_id):
    conn = get_db()
    cur = conn.cursor()
    cur.execute("UPDATE tasks SET completed = NOT completed WHERE id = %s", (task_id,))
    conn.commit()
    cur.close()
    conn.close()
    cache.delete("tasks")
    return jsonify({"status": "updated"})


@app.route("/api/tasks/<int:task_id>", methods=["DELETE"])
def delete_task(task_id):
    conn = get_db()
    cur = conn.cursor()
    cur.execute("DELETE FROM tasks WHERE id = %s", (task_id,))
    conn.commit()
    cur.close()
    conn.close()
    cache.delete("tasks")
    return jsonify({"status": "deleted"})


@app.route("/api/stats")
def stats():
    conn = get_db()
    cur = conn.cursor()
    cur.execute("SELECT COUNT(*) FROM tasks")
    total = cur.fetchone()[0]
    cur.execute("SELECT COUNT(*) FROM tasks WHERE completed = TRUE")
    done = cur.fetchone()[0]
    cur.close()
    conn.close()
    return jsonify({"total": total, "completed": done, "pending": total - done})


# Initialize DB on first request (works with gunicorn)
_db_initialized = False

@app.before_request
def ensure_db():
    global _db_initialized
    if not _db_initialized:
        try:
            init_db()
            _db_initialized = True
        except Exception:
            pass


if __name__ == "__main__":
    init_db()
    app.run(host="0.0.0.0", port=5000, debug=False)
