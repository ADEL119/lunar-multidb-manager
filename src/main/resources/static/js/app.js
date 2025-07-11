let currentConfig = {}; // Store full config globally

// Enhanced Toast Notification System
function showToast(message, type = 'info') {
  const toastElement = document.getElementById('backupToast');
  const toastBody = document.getElementById('toastMessage');
  
  // Map types to Bootstrap colors
  const typeColors = {
    success: 'success',
    error: 'danger',
    warning: 'warning',
    info: 'primary'
  };
  
  // Set the appropriate color
  const bgColor = typeColors[type] || 'primary';
  
  // Update toast content and style
  toastElement.className = `toast align-items-center text-white bg-${bgColor} border-0`;
  toastBody.textContent = message;
  
  // Show the toast with options
  const toast = new bootstrap.Toast(toastElement, {
    autohide: true,
    delay: type === 'error' ? 8000 : 5000 // Longer display for errors
  });
  toast.show();
}

// UI Control Functions
function showBackupPanel() {
  const panel = document.getElementById('backupPanel');
  panel.classList.remove('d-none');
  document.getElementById('restorePanel').classList.add('d-none');
  document.getElementById('configPanel').classList.add('d-none');
  panel.scrollIntoView({ behavior: 'smooth' });
  loadBackupReports();
}

function showRestorePanel() {
  const panel = document.getElementById('restorePanel');
  panel.classList.remove('d-none');
  document.getElementById('backupPanel').classList.add('d-none');
  document.getElementById('configPanel').classList.add('d-none');
  panel.scrollIntoView({ behavior: 'smooth' });
  loadRestoreConfigs();
}

function loadRestoreConfigs() {
  fetch('/api/config/getAll')
    .then(res => res.json())
    .then(data => {
      const tbody = document.getElementById('restoreTableBody');
      tbody.innerHTML = '';

      // Map lowercase types to icons
      const typeIcons = {
        "mongo": '<img src="img/mongodb.png" alt="MongoDB" width="100" title="MongoDB">',
        "mysql": '<img src="img/mysql.png" alt="MySQL" width="100" title="MySQL">',
        "mariadb": '<img src="img/mariadb.png" alt="MariaDB" width="100" title="MariaDB">',
        "postgres": '<img src="img/postgresql.png" alt="PostgreSQL" width="100" title="PostgreSQL">'
      };

      data.forEach((cfg, index) => {
        const row = document.createElement('tr');
        const inputId = `dataSource-${index}`;

        const dbType = (cfg.type || '').toLowerCase();
        const typeIcon = typeIcons[dbType] || cfg.type || '-';

        row.innerHTML = `
          <td class="text-center">${typeIcon}</td>
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
      showToast('Failed to load restore configurations: ' + error.message, 'error');
    });
}

function restoreDatabase(dbName, dbType, inputId) {
  const dataSource = document.getElementById(inputId).value.trim();

  if (!dataSource) {
    showToast("Please enter a backup file path.", 'warning');
    return;
  }

  const encodedDataSource = encodeURIComponent(dataSource);
  const url = `/api/restore/${dbName}/${dbType}?dataSource=${encodedDataSource}`;
  
  const restoreBtn = document.querySelector(`button[onclick="restoreDatabase('${dbName}', '${dbType}', '${inputId}')"]`);
  const originalBtnText = restoreBtn.innerHTML;
  restoreBtn.disabled = true;
  restoreBtn.innerHTML = `<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Restoring...`;

  fetch(url, { method: 'GET' })
    .then(async (res) => {
      const msg = await res.text();
      if (!res.ok) {
        throw new Error(msg); // Treat non-2xx responses as errors
      }
      return msg;
    })
    .then(msg => {
      showToast(`Restore successful: ${msg}`, 'success');
    })
    .catch(error => {
      console.error('Restore error:', error);
      showToast(`Restore failed: ${error.message}`, 'error');
    })
    .finally(() => {
      restoreBtn.disabled = false;
      restoreBtn.innerHTML = originalBtnText;
    });
}

function toggleReportTable() {
  const reportSection = document.getElementById('reportSection');
  reportSection.classList.toggle('d-none');
  if (!reportSection.classList.contains('d-none')) {
    loadBackupReports();
  }
}

function triggerBackupNow() {
  const btn = document.getElementById('backupNowBtn');
  const originalHTML = btn.innerHTML;

  btn.disabled = true;
  btn.innerHTML = `<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Processing...`;

  fetch('/api/scheduler/backup-now', { method: 'POST' })
    .then(res => res.text())
    .then(msg => {
      showToast(`Backup initiated successfully: ${msg}`, 'success');
      loadBackupReports();
    })
    .catch(error => {
      console.error('Backup error:', error);
      showToast(`Backup failed: ${error.message}`, 'error');
    })
    .finally(() => {
      btn.disabled = false;
      btn.innerHTML = originalHTML;
    });
}

function scheduleBackup(event) {
  event.preventDefault();
  const cron = document.getElementById('cronExpression').value;
  const label = document.getElementById('frequencyLabel').value;

  // Show processing state
  const submitBtn = event.target.querySelector('button[type="submit"]');
  const originalBtnText = submitBtn.innerHTML;
  submitBtn.disabled = true;
  submitBtn.innerHTML = `<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Scheduling...`;

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
    showToast(`Backup scheduled: ${msg}`, 'success');
    document.getElementById('cronExpression').value = '';
    document.getElementById('frequencyLabel').value = '';
  })
  .catch(error => {
    console.error('Scheduling error:', error);
    showToast(`Scheduling failed: ${error.message}`, 'error');
  })
  .finally(() => {
    submitBtn.disabled = false;
    submitBtn.innerHTML = originalBtnText;
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
      showToast('Failed to load reports: ' + error.message, 'error');
    });
}

function showConfigPanel() {
  document.getElementById('backupPanel').classList.add('d-none');
  document.getElementById('restorePanel').classList.add('d-none');
  const panel = document.getElementById('configPanel');
  panel.classList.remove('d-none');
  panel.scrollIntoView({ behavior: 'smooth' });
  loadGlobalConfig();
}

function loadGlobalConfig() {
  fetch('/api/config/update')
    .then(res => res.json())
    .then(config => {
      currentConfig = config;

      document.getElementById('pathDirectory').value = config.pathDirectory || '';
      document.getElementById('notificationEmailFrom').value = config.notificationConfig?.notificationEmailFrom || '';
      document.getElementById('notificationEmailPassword').value = config.notificationConfig?.notificationEmailPassword || '';
      document.getElementById('notificationSmtpHost').value = config.notificationConfig?.notificationSmtpHost || '';
      document.getElementById('notificationSmtpPort').value = config.notificationConfig?.notificationSmtpPort || '';
      document.getElementById('notificationSmtpAuth').value = config.notificationConfig?.notificationSmtpAuth || 'false';
      document.getElementById('notificationStartTlsEnable').value = config.notificationConfig?.notificationStartTlsEnable || 'false';
      document.getElementById('notificationSummaryEmailToList').value = (config.notificationConfig?.notificationSummaryEmailToList || []).join(',');

      const dbContainer = document.getElementById('dbConfigList');
      dbContainer.innerHTML = '';

      (config.databaseConfigList || []).forEach((db, index) => {
        const dbCard = document.createElement('div');
        dbCard.className = 'card mb-3 shadow-sm';
        dbCard.innerHTML = `
          <div class="card-body">
            <h5 class="card-title text-primary">Database #${index + 1}</h5>
            <div class="row g-2">
              <div class="col-md-6">
                <label class="form-label">Type</label>
                <input type="text" class="form-control" value="${db.type || ''}" onchange="currentConfig.databaseConfigList[${index}].type = this.value">
              </div>
              <div class="col-md-6">
                <label class="form-label">Database</label>
                <input type="text" class="form-control" value="${db.database || ''}" onchange="currentConfig.databaseConfigList[${index}].database = this.value">
              </div>
              <div class="col-md-6">
                <label class="form-label">Host</label>
                <input type="text" class="form-control" value="${db.host || ''}" onchange="currentConfig.databaseConfigList[${index}].host = this.value">
              </div>
              <div class="col-md-6">
                <label class="form-label">Port</label>
                <input type="number" class="form-control" value="${db.port || ''}" onchange="currentConfig.databaseConfigList[${index}].port = parseInt(this.value)">
              </div>
              <div class="col-md-6">
                <label class="form-label">Username</label>
                <input type="text" class="form-control" value="${db.username || ''}" onchange="currentConfig.databaseConfigList[${index}].username = this.value">
              </div>
              <div class="col-md-6">
                <label class="form-label">Password</label>
                <input type="text" class="form-control" value="${db.password || ''}" onchange="currentConfig.databaseConfigList[${index}].password = this.value">
              </div>
              <div class="col-md-6">
                <label class="form-label">Authentication DB</label>
                <input type="text" class="form-control" value="${db.authenticationDatabase || ''}" onchange="currentConfig.databaseConfigList[${index}].authenticationDatabase = this.value">
              </div>
              <div class="col-md-6">
                <label class="form-label">Short Name</label>
                <input type="text" class="form-control" value="${db.shortName || ''}" onchange="currentConfig.databaseConfigList[${index}].shortName = this.value">
              </div>
              <div class="col-md-12">
                <label class="form-label">Email List (comma-separated)</label>
                <input type="text" class="form-control" value="${(db.emailList || []).join(',')}" onchange="currentConfig.databaseConfigList[${index}].emailList = this.value.split(',').map(e => e.trim())">
              </div>
              <div class="col-md-4">
                <label class="form-label">Daily</label>
                <select class="form-select" onchange="currentConfig.databaseConfigList[${index}].daily = this.value === 'true'">
                  <option value="true" ${db.daily ? 'selected' : ''}>True</option>
                  <option value="false" ${!db.daily ? 'selected' : ''}>False</option>
                </select>
              </div>
              <div class="col-md-4">
                <label class="form-label">Weekly</label>
                <select class="form-select" onchange="currentConfig.databaseConfigList[${index}].weekly = this.value === 'true'">
                  <option value="true" ${db.weekly ? 'selected' : ''}>True</option>
                  <option value="false" ${!db.weekly ? 'selected' : ''}>False</option>
                </select>
              </div>
              <div class="col-md-4">
                <label class="form-label">Monthly</label>
                <select class="form-select" onchange="currentConfig.databaseConfigList[${index}].monthly = this.value === 'true'">
                  <option value="true" ${db.monthly ? 'selected' : ''}>True</option>
                  <option value="false" ${!db.monthly ? 'selected' : ''}>False</option>
                </select>
              </div>
              <div class="col-md-12">
                <label class="form-label">Large Collections (comma-separated)</label>
                <input type="text" class="form-control" value="${(db.largeCollections || []).join(',')}" onchange="currentConfig.databaseConfigList[${index}].largeCollections = this.value.split(',').map(e => e.trim())">
              </div>
              <div class="col-md-6">
                <label class="form-label">Backup Large Collections</label>
                <select class="form-select" onchange="currentConfig.databaseConfigList[${index}].backupLargeCollections = this.value === 'true'">
                  <option value="true" ${db.backupLargeCollections ? 'selected' : ''}>True</option>
                  <option value="false" ${!db.backupLargeCollections ? 'selected' : ''}>False</option>
                </select>
              </div>
              <div class="col-md-6 d-flex align-items-end justify-content-end">
                <button class="btn btn-outline-danger w-100" onclick="deleteDatabase(${index})">
                  <i class="bi bi-trash"></i> Delete
                </button>
              </div>
            </div>
          </div>
        `;
        dbContainer.appendChild(dbCard);
      });
    })
    .catch(err => showToast("Failed to load config: " + err.message, 'error'));
}

function submitGlobalConfig(event) {
  event.preventDefault();
  
  const submitBtn = event.target.querySelector('button[type="submit"]');
  const originalBtnText = submitBtn.innerHTML;
  submitBtn.disabled = true;
  submitBtn.innerHTML = `<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Saving...`;

  const updatedConfig = {
    pathDirectory: document.getElementById('pathDirectory').value.trim(),
    notificationConfig: {
      notificationEmailFrom: document.getElementById('notificationEmailFrom').value.trim(),
      notificationEmailPassword: document.getElementById('notificationEmailPassword').value.trim(),
      notificationSmtpHost: document.getElementById('notificationSmtpHost').value.trim(),
      notificationSmtpPort: parseInt(document.getElementById('notificationSmtpPort').value),
      notificationSmtpAuth: document.getElementById('notificationSmtpAuth').value === 'true',
      notificationStartTlsEnable: document.getElementById('notificationStartTlsEnable').value === 'true',
      notificationSummaryEmailToList: document.getElementById('notificationSummaryEmailToList').value
        .split(',').map(e => e.trim()).filter(e => e.length > 0)
    },
    databaseConfigList: currentConfig.databaseConfigList || []
  };

  fetch('/api/config/update', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(updatedConfig)
  })
    .then(() => showToast('Configuration saved successfully!', 'success'))
    .catch(err => showToast('Save failed: ' + err.message, 'error'))
    .finally(() => {
      submitBtn.disabled = false;
      submitBtn.innerHTML = originalBtnText;
    });
}

function deleteDatabase(index) {
  // Confirm deletion with user
  if (!confirm('Are you sure you want to delete this database configuration?')) {
    return;
  }

  // Remove from local config
  const deletedDb = currentConfig.databaseConfigList.splice(index, 1)[0];

  // Update UI immediately
  renderDatabaseConfigCards();

  // Show processing state
  showToast('Deleting database configuration...', 'info');

  // Send the update to the server
  fetch('/api/config/update', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(currentConfig)
  })
  .then(() => {
    showToast('Database configuration deleted successfully!', 'success');
    // Reload the config from server to ensure sync
    loadGlobalConfig();
  })
  .catch(err => {
    console.error('Delete error:', err);
    // Revert the change if server update fails
    currentConfig.databaseConfigList.splice(index, 0, deletedDb);
    renderDatabaseConfigCards();
    showToast('Failed to delete database: ' + err.message, 'error');
  });
}

function addNewDatabase() {
  const modal = new bootstrap.Modal(document.getElementById('newDatabaseModal'));
  modal.show();
}

function handleNewDatabaseSubmit(event) {
  event.preventDefault();

  const newDb = {
    type: document.getElementById('dbType').value.trim(),
    host: document.getElementById('dbHost').value.trim(),
    port: parseInt(document.getElementById('dbPort').value),
    username: document.getElementById('dbUsername').value.trim(),
    password: document.getElementById('dbPassword').value.trim(),
    authenticationDatabase: document.getElementById('dbAuthDB').value.trim(),
    database: document.getElementById('dbName').value.trim(),
    shortName: document.getElementById('dbShortName').value.trim(),
    emailList: document.getElementById('dbEmailList').value.split(',').map(e => e.trim()).filter(Boolean),
    daily: document.getElementById('dbDaily').value === 'true',
    weekly: document.getElementById('dbWeekly').value === 'true',
    monthly: document.getElementById('dbMonthly').value === 'true',
    largeCollections: document.getElementById('dbLargeCollections').value.split(',').map(e => e.trim()).filter(Boolean),
    backupLargeCollections: document.getElementById('dbBackupLargeCollections').value === 'true'
  };

  const submitBtn = event.target.querySelector('button[type="submit"]');
  const originalBtnText = submitBtn.innerHTML;
  submitBtn.disabled = true;
  submitBtn.innerHTML = `<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Adding...`;

  fetch('/api/config/update/add-database', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(newDb)
  })
    .then(res => {
      if (!res.ok) throw new Error('Failed to add database');
      return res.json();
    })
    .then(updatedConfig => {
      currentConfig = updatedConfig;
      renderDatabaseConfigCards();
      bootstrap.Modal.getInstance(document.getElementById('newDatabaseModal')).hide();
      showToast('Database added successfully!', 'success');
    })
    .catch(err => showToast('Error: ' + err.message, 'error'))
    .finally(() => {
      submitBtn.disabled = false;
      submitBtn.innerHTML = originalBtnText;
    });
}

function renderDatabaseConfigCards() {
  const dbContainer = document.getElementById('dbConfigList');
  dbContainer.innerHTML = '';

  (currentConfig.databaseConfigList || []).forEach((db, index) => {
    const dbCard = document.createElement('div');
    dbCard.className = 'card mb-3 shadow-sm';
    dbCard.innerHTML = `
      <div class="card-body">
        <h5 class="card-title text-primary">Database #${index + 1}</h5>
        <div class="row g-2">
          <div class="col-md-6">
            <label class="form-label">Type</label>
            <input type="text" class="form-control" value="${db.type || ''}" onchange="currentConfig.databaseConfigList[${index}].type = this.value">
          </div>
          <div class="col-md-6">
            <label class="form-label">Database</label>
            <input type="text" class="form-control" value="${db.database || ''}" onchange="currentConfig.databaseConfigList[${index}].database = this.value">
          </div>
          <div class="col-md-6">
            <label class="form-label">Host</label>
            <input type="text" class="form-control" value="${db.host || ''}" onchange="currentConfig.databaseConfigList[${index}].host = this.value">
          </div>
          <div class="col-md-6">
            <label class="form-label">Port</label>
            <input type="number" class="form-control" value="${db.port || 0}" onchange="currentConfig.databaseConfigList[${index}].port = parseInt(this.value)">
          </div>
          <div class="col-md-6">
            <label class="form-label">Username</label>
            <input type="text" class="form-control" value="${db.username || ''}" onchange="currentConfig.databaseConfigList[${index}].username = this.value">
          </div>
          <div class="col-md-6">
            <label class="form-label">Password</label>
            <input type="text" class="form-control" value="${db.password || ''}" onchange="currentConfig.databaseConfigList[${index}].password = this.value">
          </div>
          <div class="col-md-6">
            <label class="form-label">Authentication DB</label>
            <input type="text" class="form-control" value="${db.authenticationDatabase || ''}" onchange="currentConfig.databaseConfigList[${index}].authenticationDatabase = this.value">
          </div>
          <div class="col-md-6">
            <label class="form-label">Short Name</label>
            <input type="text" class="form-control" value="${db.shortName || ''}" onchange="currentConfig.databaseConfigList[${index}].shortName = this.value">
          </div>
          <div class="col-12">
            <label class="form-label">Email List (comma-separated)</label>
            <input type="text" class="form-control" value="${(db.emailList || []).join(',')}" onchange="currentConfig.databaseConfigList[${index}].emailList = this.value.split(',').map(e => e.trim())">
          </div>
          <div class="col-md-4">
            <label class="form-label">Daily</label>
            <select class="form-select" onchange="currentConfig.databaseConfigList[${index}].daily = this.value === 'true'">
              <option value="true" ${db.daily ? 'selected' : ''}>True</option>
              <option value="false" ${!db.daily ? 'selected' : ''}>False</option>
            </select>
          </div>
          <div class="col-md-4">
            <label class="form-label">Weekly</label>
            <select class="form-select" onchange="currentConfig.databaseConfigList[${index}].weekly = this.value === 'true'">
              <option value="true" ${db.weekly ? 'selected' : ''}>True</option>
              <option value="false" ${!db.weekly ? 'selected' : ''}>False</option>
            </select>
          </div>
          <div class="col-md-4">
            <label class="form-label">Monthly</label>
            <select class="form-select" onchange="currentConfig.databaseConfigList[${index}].monthly = this.value === 'true'">
              <option value="true" ${db.monthly ? 'selected' : ''}>True</option>
              <option value="false" ${!db.monthly ? 'selected' : ''}>False</option>
            </select>
          </div>
          <div class="col-12">
            <label class="form-label">Large Collections (comma-separated)</label>
            <input type="text" class="form-control" value="${(db.largeCollections || []).join(',')}" onchange="currentConfig.databaseConfigList[${index}].largeCollections = this.value.split(',').map(e => e.trim())">
          </div>
          <div class="col-md-6">
            <label class="form-label">Backup Large Collections</label>
            <select class="form-select" onchange="currentConfig.databaseConfigList[${index}].backupLargeCollections = this.value === 'true'">
              <option value="true" ${db.backupLargeCollections ? 'selected' : ''}>True</option>
              <option value="false" ${!db.backupLargeCollections ? 'selected' : ''}>False</option>
            </select>
          </div>
          <div class="col-md-6 d-flex align-items-end justify-content-end">
            <button class="btn btn-outline-danger w-100" onclick="deleteDatabase(${index})">
              <i class="bi bi-trash"></i> Delete
            </button>
          </div>
        </div>
      </div>
    `;
    dbContainer.appendChild(dbCard);
  });
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
  showBackupPanel();
});

window.addEventListener('load', loadBackupReports);