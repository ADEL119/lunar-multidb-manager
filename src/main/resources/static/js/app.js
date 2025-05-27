// Constants
const ADMIN_CREDENTIALS = {
  username: 'admin',
  password: 'admin'
};

// UI Control Functions
function showBackupPanel() {
  const panel = document.getElementById('backupPanel');
  panel.classList.remove('d-none');
  document.getElementById('restorePanel').classList.add('d-none');
  panel.scrollIntoView({ behavior: 'smooth' });
  loadBackupReports();
}

function showRestorePanel() {
  const panel = document.getElementById('restorePanel');
  panel.classList.remove('d-none');
  document.getElementById('backupPanel').classList.add('d-none');
  panel.scrollIntoView({ behavior: 'smooth' });
}

function toggleReportTable() {
  const reportSection = document.getElementById('reportSection');
  reportSection.classList.toggle('d-none');
  if (!reportSection.classList.contains('d-none')) {
    loadBackupReports();
  }
}

// Backup Functions
function triggerBackupNow() {
  fetch('/api/scheduler/backup-now', { method: 'POST' })
    .then(res => res.text())
    .then(msg => {
      alert(msg);
      loadBackupReports();
    })
    .catch(error => {
      console.error('Backup error:', error);
      alert('Backup failed: ' + error.message);
    });
}

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
  })
  .catch(error => {
    console.error('Scheduling error:', error);
    alert('Scheduling failed: ' + error.message);
  });
}

function loadBackupReports() {
  fetch('/api/reports')
    .then(res => res.json())
    .then(data => {
      const table = $('#reportTable').DataTable();
      table.clear();

      data.reverse().forEach(r => {
        const statusBadge = `<span class="badge ${r.status === 'SUCCESS' ? 'bg-success' : 'bg-danger'}">${r.status}</span>`;
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

      table.draw();
    })
    .catch(error => {
      console.error('Report loading error:', error);
      alert('Failed to load reports: ' + error.message);
    });
}

// Auth Functions
function handleLogin(event) {
  event.preventDefault();
  const form = event.target;

  if (!form.checkValidity()) {
    form.classList.add('was-validated');
    return;
  }

  const username = document.getElementById('username').value;
  const password = document.getElementById('password').value;
  if (username === ADMIN_CREDENTIALS.username && password === ADMIN_CREDENTIALS.password) {
    alert('Login successful!');
    showBackupPanel();
  } else {
    alert('Invalid credentials');
  }
}

function handleLogout() {
  alert("You're logged out (demo only)");
  location.reload();
}

// Initialization
document.addEventListener('DOMContentLoaded', () => {
  $('#reportTable').DataTable({
    responsive: true,
    autoWidth: false,
    order: [[5, 'desc']],
    language: {
      emptyTable: 'No backup reports available'
    }
  });
});

window.addEventListener('load', loadBackupReports);