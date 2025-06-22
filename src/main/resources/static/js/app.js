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
  console.log("Showing restore panel...");
  const panel = document.getElementById('restorePanel');
  panel.classList.remove('d-none');
  document.getElementById('backupPanel').classList.add('d-none');
  panel.scrollIntoView({ behavior: 'smooth' });
  loadRestoreConfigs();
}

function loadRestoreConfigs() {
  console.log("Fetching restore configs...");
  fetch('/api/config/getAll')
    .then(res => {
      console.log('Response received:', res);
      return res.json();
    })
    .then(data => {
      console.log('Parsed data:', data);
      const tbody = document.getElementById('restoreTableBody');
      tbody.innerHTML = ''; // Clear previous

      data.forEach((cfg, index) => {
        const row = document.createElement('tr');

        // Give each input a unique ID based on index
        const inputId = `dataSource-${index}`;

        row.innerHTML = `
          <td>${cfg.type || '-'}</td>
          <td>${cfg.database || '-'}</td>
          <td>${cfg.host || '-'}</td>
          <td>${cfg.port || '-'}</td>
          <td>${cfg.username || '-'}</td>
          <td>${cfg.password || '-'}</td>
          <td>
            <input type="text" id="${inputId}" class="form-control form-control-sm" placeholder="Enter file path">
          </td>
          <td>
            <button class="btn btn-sm btn-warning" onclick="restoreDatabase('${cfg.database}', '${cfg.type}', '${inputId}')">
              <i class="bi bi-arrow-clockwise"></i> Restore
            </button>
          </td>
        `;
        tbody.appendChild(row);
      });
    })
    .catch(error => {
      console.error('Restore config loading error:', error);
      alert('Failed to load restore configurations: ' + error.message);
    });
}

function restoreDatabase(dbName, dbType, inputId) {
  const dataSource = document.getElementById(inputId).value.trim();

  if (!dataSource) {
    alert("Please enter a backup file path.");
    return;
  }

  const encodedDataSource = encodeURIComponent(dataSource); // Important to safely pass file path in URL

  const url = `/api/restore/${dbName}/${dbType}?dataSource=${encodedDataSource}`;
  console.log('Calling restore endpoint:', url);

  fetch(url, { method: 'GET' })
    .then(res => res.text())
    .then(msg => {
      alert(msg);
    })
    .catch(error => {
      console.error('Restore error:', error);
      alert('Failed to restore: ' + error.message);
    });
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