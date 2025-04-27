// 🔁 Trigger a manual backup
function triggerBackupNow() {
  fetch('/api/scheduler/backup-now', { method: 'POST' })
    .then(res => res.text())
    .then(msg => {
      alert(msg);
      loadBackupReports();
    });
}

// 🗓️ Schedule a dynamic backup with custom cron
function scheduleBackup(event) {
  event.preventDefault();
  const cron = document.getElementById('cronExpression').value;
  const label = document.getElementById('frequencyLabel').value;

  fetch('/api/scheduler/schedule-dynamic', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      cronExpression: cron,
      frequencyLabel: label
    })
  })
  .then(res => res.text())
  .then(msg => {
    alert(msg);
    document.getElementById('cronExpression').value = '';
    document.getElementById('frequencyLabel').value = '';
  });
}

// 📄 Load and display backup reports using DataTables
function loadBackupReports() {
  fetch('/api/reports')
    .then(res => res.json())
    .then(data => {
      const table = $('#reportTable').DataTable();
      table.clear(); // Clear existing rows

      data.reverse().forEach(r => {
        const statusBadge = '<span class="badge ' + (r.status === 'SUCCESS' ? 'bg-success' : 'bg-danger') + '">' + r.status + '</span>';
        const timestamp = new Date(r.timestamp).toLocaleString();

        table.row.add([
          r.databaseName,
          r.type,
          r.frequency,
          statusBadge,
          r.filePath,
          timestamp
        ]);
      });

      table.draw(); // Refresh with new data
    });
}

// 👀 Show the backup section after login
function showBackupPanel() {
  const panel = document.getElementById('backupPanel');
  panel.classList.remove('d-none');
  panel.scrollIntoView({ behavior: 'smooth' });
  loadBackupReports();
}

// 🧑‍💻 Fake login for demo
function handleLogin(event) {
  event.preventDefault();
  const form = event.target;

  if (!form.checkValidity()) {
    form.classList.add('was-validated');
    return;
  }

  const username = document.getElementById('username').value;
  const password = document.getElementById('password').value;
  if (username === 'admin' && password === 'admin') {
    alert('Login successful!');
    showBackupPanel();
  } else {
    alert('Invalid credentials');
  }
}

// 🚪 Fake logout for demo
function handleLogout() {
  alert("You're logged out (demo only)");
  location.reload(); // Simple refresh
}

// 🔄 Load backup reports on page load
window.onload = loadBackupReports;

// 🧾 Show the report table section
function toggleReportTable() {
  const reportSection = document.getElementById('reportSection');
  reportSection.classList.remove('d-none');
  loadBackupReports();
}

// 🧠 Initialize DataTables once the DOM is ready
document.addEventListener('DOMContentLoaded', () => {
  $('#reportTable').DataTable({
    responsive: true,
    autoWidth: false,
    order: [[5, 'desc']]
  });
});
