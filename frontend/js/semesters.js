/**
 * CampusTrack — Phase 1: Semester Management Controller
 * Handles loading, creating, editing, status toggling, and deleting semesters.
 * Strictly communicates with Java Servlets REST API backed by MySQL.
 */
document.addEventListener('DOMContentLoaded', () => {
    // Require student authentication
    Auth.requireAuth();
    Auth.initNavbar();

    let allSemesters = [];
    let pendingStatusChange = null;
    let pendingDeleteSemester = null;

    // DOM Elements
    const semestersGrid = document.getElementById('semestersGrid');
    const semestersEmptyState = document.getElementById('semestersEmptyState');
    const semestersLoadingState = document.getElementById('semestersLoadingState');
    const backendErrorContainer = document.getElementById('backendErrorContainer');
    const backendErrorMessage = document.getElementById('backendErrorMessage');
    const retryConnectionBtn = document.getElementById('retryConnectionBtn');

    // KPI Elements
    const kpiTotalSemesters = document.getElementById('kpiTotalSemesters');
    const kpiActiveSemesters = document.getElementById('kpiActiveSemesters');
    const kpiCompletedSemesters = document.getElementById('kpiCompletedSemesters');

    // API Status Elements
    const apiStatusDot = document.getElementById('apiStatusDot');
    const apiStatusText = document.getElementById('apiStatusText');

    // Student Switcher Elements
    const currentStudentLabel = document.getElementById('currentStudentLabel');
    const activeStudentBadge = document.getElementById('activeStudentBadge');
    const quickToggleStudentBtn = document.getElementById('quickToggleStudentBtn');

    // Modals
    const addSemesterModalEl = document.getElementById('addSemesterModal');
    const addSemesterModal = addSemesterModalEl ? new bootstrap.Modal(addSemesterModalEl) : null;
    const addSemesterForm = document.getElementById('addSemesterForm');
    const addSemesterErrorAlert = document.getElementById('addSemesterErrorAlert');
    const addSemesterSubmitBtn = document.getElementById('addSemesterSubmitBtn');
    const addSemesterSpinner = document.getElementById('addSemesterSpinner');
    const addSemesterNumber = document.getElementById('addSemesterNumber');
    const addAcademicYear = document.getElementById('addAcademicYear');
    const addSemesterStatus = document.getElementById('addSemesterStatus');

    const editSemesterModalEl = document.getElementById('editSemesterModal');
    const editSemesterModal = editSemesterModalEl ? new bootstrap.Modal(editSemesterModalEl) : null;
    const editSemesterForm = document.getElementById('editSemesterForm');
    const editSemesterErrorAlert = document.getElementById('editSemesterErrorAlert');
    const editSemesterSubmitBtn = document.getElementById('editSemesterSubmitBtn');
    const editSemesterSpinner = document.getElementById('editSemesterSpinner');
    const editSemesterId = document.getElementById('editSemesterId');
    const editSemesterNumber = document.getElementById('editSemesterNumber');
    const editAcademicYear = document.getElementById('editAcademicYear');
    const editSemesterStatus = document.getElementById('editSemesterStatus');

    const statusConfirmModalEl = document.getElementById('statusConfirmModal');
    const statusConfirmModal = statusConfirmModalEl ? new bootstrap.Modal(statusConfirmModalEl) : null;
    const statusConfirmHeading = document.getElementById('statusConfirmHeading');
    const statusConfirmMessage = document.getElementById('statusConfirmMessage');
    const confirmStatusChangeBtn = document.getElementById('confirmStatusChangeBtn');

    const deleteConfirmModalEl = document.getElementById('deleteConfirmModal');
    const deleteConfirmModal = deleteConfirmModalEl ? new bootstrap.Modal(deleteConfirmModalEl) : null;
    const deleteConfirmHeading = document.getElementById('deleteConfirmHeading');
    const deleteConfirmMessage = document.getElementById('deleteConfirmMessage');
    const confirmDeleteSemesterBtn = document.getElementById('confirmDeleteSemesterBtn');

    // -------------------------------------------------------------------------
    // Initial Setup
    // -------------------------------------------------------------------------
    updateStudentIdentityUI();
    checkHealth();
    loadSemesters();

    // -------------------------------------------------------------------------
    // Backend Health Check
    // -------------------------------------------------------------------------
    async function checkHealth() {
        if (!apiStatusDot || !apiStatusText) return;
        try {
            const health = await AppConfig.checkHealth();
            if (health.online) {
                apiStatusDot.className = 'status-dot-pulse bg-success';
                apiStatusText.textContent = 'API Live (Tomcat)';
            } else {
                apiStatusDot.className = 'status-dot-pulse bg-warning';
                apiStatusText.textContent = 'Connecting...';
            }
        } catch (e) {
            apiStatusDot.className = 'status-dot-pulse bg-danger';
            apiStatusText.textContent = 'API Offline';
        }
    }

    // -------------------------------------------------------------------------
    // Load Semesters from Java REST API
    // -------------------------------------------------------------------------
    async function loadSemesters() {
        // UI State: Loading
        semestersLoadingState.classList.remove('d-none');
        semestersGrid.classList.add('d-none');
        semestersEmptyState.classList.add('d-none');
        backendErrorContainer.classList.add('d-none');

        try {
            const data = await ApiClient.get('/semesters');
            allSemesters = Array.isArray(data) ? data : [];

            // Update KPI summary cards
            updateSummaryKpis(allSemesters);

            semestersLoadingState.classList.add('d-none');

            if (allSemesters.length === 0) {
                semestersEmptyState.classList.remove('d-none');
                semestersGrid.classList.add('d-none');
            } else {
                renderSemesterCards(allSemesters);
                semestersGrid.classList.remove('d-none');
                semestersEmptyState.classList.add('d-none');
            }

            // Successfully connected, update status pill
            if (apiStatusDot && apiStatusText) {
                apiStatusDot.className = 'status-dot-pulse bg-success';
                apiStatusText.textContent = 'API Live (Tomcat)';
            }

        } catch (err) {
            console.error('[SemesterModule] Error fetching semesters:', err);
            semestersLoadingState.classList.add('d-none');
            semestersGrid.classList.add('d-none');
            semestersEmptyState.classList.add('d-none');
            backendErrorContainer.classList.remove('d-none');
            backendErrorMessage.textContent = err.message || 'Unable to communicate with the Java REST API. Please ensure Apache Tomcat is running.';

            if (apiStatusDot && apiStatusText) {
                apiStatusDot.className = 'status-dot-pulse bg-danger';
                apiStatusText.textContent = 'API Offline';
            }
        }
    }

    // -------------------------------------------------------------------------
    // Update KPI Summary Cards
    // -------------------------------------------------------------------------
    function updateSummaryKpis(semesters) {
        const total = semesters.length;
        const active = semesters.filter(s => (s.status || '').toLowerCase() === 'active').length;
        const completed = semesters.filter(s => (s.status || '').toLowerCase() === 'completed').length;

        if (kpiTotalSemesters) kpiTotalSemesters.textContent = total;
        if (kpiActiveSemesters) kpiActiveSemesters.textContent = active;
        if (kpiCompletedSemesters) kpiCompletedSemesters.textContent = completed;
    }

    // -------------------------------------------------------------------------
    // Render Semester Cards
    // -------------------------------------------------------------------------
    function renderSemesterCards(semesters) {
        if (!semestersGrid) return;

        // Sort by semester number ascending
        const sorted = [...semesters].sort((a, b) => a.semesterNumber - b.semesterNumber);

        semestersGrid.innerHTML = sorted.map(sem => {
            const isCompleted = (sem.status || '').toLowerCase() === 'completed';
            const statusClass = isCompleted ? 'Completed' : 'Active';
            const cardThemeClass = isCompleted ? 'completed-sem' : 'active-sem';
            const semName = sem.semesterName || `Semester ${sem.semesterNumber}`;
            const semId = sem.id || sem.semesterId;

            // Route to dedicated semester details page
            const detailUrl = `semester-detail.html?id=${semId}`;

            return `
                <div class="academic-semester-card ${cardThemeClass}" data-sem-id="${semId}">
                    <div class="sem-card-header">
                        <div class="d-flex align-items-center gap-3">
                            <div class="sem-number-pill">
                                ${sem.semesterNumber}
                            </div>
                            <div class="sem-title-box">
                                <h3>${escapeHtml(semName)}</h3>
                                <div class="sem-academic-year">
                                    <i class="bi bi-calendar3"></i> ${escapeHtml(sem.academicYear)}
                                </div>
                            </div>
                        </div>
                        <span class="badge-sem-status ${statusClass}">
                            ${!isCompleted ? '<span class="status-dot-pulse"></span>' : '<i class="bi bi-check-circle-fill"></i>'}
                            ${statusClass}
                        </span>
                    </div>

                    <div class="text-muted small mb-3">
                        <span class="d-inline-flex align-items-center gap-1">
                            <i class="bi bi-mortarboard text-secondary"></i> Program Progress: Term ${sem.semesterNumber} of 8
                        </span>
                    </div>

                    <div class="sem-card-actions">
                        <a href="${detailUrl}" class="btn-open-semester" title="Open ${escapeHtml(semName)} Details">
                            <span>Open Semester</span>
                            <i class="bi bi-arrow-right-short fs-5"></i>
                        </a>

                        <button type="button" class="btn-icon-academic edit-semester-btn" 
                                data-id="${semId}" 
                                data-number="${sem.semesterNumber}" 
                                data-year="${escapeHtml(sem.academicYear)}" 
                                data-status="${statusClass}"
                                title="Edit Semester Details">
                            <i class="bi bi-pencil"></i>
                        </button>

                        <button type="button" class="btn-icon-academic toggle-status toggle-status-btn" 
                                data-id="${semId}" 
                                data-number="${sem.semesterNumber}"
                                data-name="${escapeHtml(semName)}"
                                data-current-status="${statusClass}"
                                title="${isCompleted ? 'Reactivate Semester' : 'Mark as Completed'}">
                            <i class="bi ${isCompleted ? 'bi-arrow-clockwise text-teal-custom' : 'bi-check2-circle text-success'}"></i>
                        </button>

                        <button type="button" class="btn-icon-academic delete-sem delete-semester-btn" 
                                data-id="${semId}" 
                                data-name="${escapeHtml(semName)}"
                                title="Delete Semester">
                            <i class="bi bi-trash"></i>
                        </button>
                    </div>
                </div>
            `;
        }).join('');

        // Attach action listeners
        attachCardListeners();
    }

    // -------------------------------------------------------------------------
    // Event Listeners for Card Actions
    // -------------------------------------------------------------------------
    function attachCardListeners() {
        // Edit Semester Click
        document.querySelectorAll('.edit-semester-btn').forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.stopPropagation();
                const semId = btn.dataset.id;
                const semNumber = btn.dataset.number;
                const academicYear = btn.dataset.year;
                const status = btn.dataset.status;

                editSemesterId.value = semId;
                editSemesterNumber.value = semNumber;
                editAcademicYear.value = academicYear;
                editSemesterStatus.value = status;

                editSemesterErrorAlert.classList.add('d-none');
                editSemesterModal.show();
            });
        });

        // Status Toggle Click
        document.querySelectorAll('.toggle-status-btn').forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.stopPropagation();
                const semId = parseInt(btn.dataset.id);
                const semNumber = btn.dataset.number;
                const semName = btn.dataset.name;
                const currentStatus = btn.dataset.currentStatus;
                const targetStatus = currentStatus === 'Active' ? 'Completed' : 'Active';

                pendingStatusChange = { semId, semNumber, semName, currentStatus, targetStatus };

                if (targetStatus === 'Completed') {
                    statusConfirmHeading.textContent = `Mark ${semName} as Completed?`;
                    statusConfirmMessage.textContent = `Are you sure you want to mark ${semName} as Completed? You can reactivate it at any time.`;
                    confirmStatusChangeBtn.className = 'btn btn-success rounded-pill px-4';
                    confirmStatusChangeBtn.textContent = 'Mark Completed';
                } else {
                    statusConfirmHeading.textContent = `Reactivate ${semName}?`;
                    statusConfirmMessage.textContent = `Are you sure you want to reactivate ${semName} back to Active status?`;
                    confirmStatusChangeBtn.className = 'btn btn-academic-teal rounded-pill px-4';
                    confirmStatusChangeBtn.textContent = 'Reactivate Semester';
                }

                statusConfirmModal.show();
            });
        });

        // Delete Semester Click
        document.querySelectorAll('.delete-semester-btn').forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.stopPropagation();
                const semId = parseInt(btn.dataset.id);
                const semName = btn.dataset.name;

                pendingDeleteSemester = { semId, semName };
                deleteConfirmHeading.textContent = `Delete ${semName}?`;
                deleteConfirmMessage.textContent = `Are you sure you want to delete ${semName}? All associated semester data will be permanently removed.`;
                deleteConfirmModal.show();
            });
        });
    }

    // -------------------------------------------------------------------------
    // Confirm Status Change Execution
    // -------------------------------------------------------------------------
    if (confirmStatusChangeBtn) {
        confirmStatusChangeBtn.addEventListener('click', async () => {
            if (!pendingStatusChange) return;

            confirmStatusChangeBtn.disabled = true;
            confirmStatusChangeBtn.textContent = 'Updating...';

            try {
                await ApiClient.patch(`/semesters/${pendingStatusChange.semId}/status`, {
                    status: pendingStatusChange.targetStatus
                });

                statusConfirmModal.hide();
                ApiClient.showToast(`${pendingStatusChange.semName} marked as ${pendingStatusChange.targetStatus}!`, 'success');
                loadSemesters();
            } catch (err) {
                console.error('[SemesterModule] Status update error:', err);
                ApiClient.showToast(err.message || 'Failed to update semester status', 'danger');
            } finally {
                confirmStatusChangeBtn.disabled = false;
                confirmStatusChangeBtn.textContent = 'Confirm';
                pendingStatusChange = null;
            }
        });
    }

    // -------------------------------------------------------------------------
    // Confirm Delete Semester Execution
    // -------------------------------------------------------------------------
    if (confirmDeleteSemesterBtn) {
        confirmDeleteSemesterBtn.addEventListener('click', async () => {
            if (!pendingDeleteSemester) return;

            confirmDeleteSemesterBtn.disabled = true;
            confirmDeleteSemesterBtn.textContent = 'Deleting...';

            try {
                await ApiClient.delete(`/semesters/${pendingDeleteSemester.semId}`);
                deleteConfirmModal.hide();
                ApiClient.showToast(`${pendingDeleteSemester.semName} deleted successfully.`, 'info');
                loadSemesters();
            } catch (err) {
                console.error('[SemesterModule] Delete error:', err);
                ApiClient.showToast(err.message || 'Failed to delete semester', 'danger');
            } finally {
                confirmDeleteSemesterBtn.disabled = false;
                confirmDeleteSemesterBtn.textContent = 'Delete';
                pendingDeleteSemester = null;
            }
        });
    }

    // -------------------------------------------------------------------------
    // Add Semester Form Submission
    // -------------------------------------------------------------------------
    if (addSemesterForm) {
        addSemesterForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            addSemesterErrorAlert.classList.add('d-none');

            const semNumber = parseInt(addSemesterNumber.value);
            const academicYear = (addAcademicYear.value || '').trim();
            const status = addSemesterStatus.value || 'Active';

            // 1. Frontend Validation: Semester Number 1 to 8
            if (isNaN(semNumber) || semNumber < 1 || semNumber > 8) {
                showAddError('Please select a valid semester number between 1 and 8.');
                return;
            }

            // 2. Frontend Validation: Academic Year required
            if (!academicYear) {
                showAddError('Academic year is required (e.g. 2024-2025).');
                return;
            }

            // 3. Frontend Validation: Check duplicate semester number for same student
            const duplicate = allSemesters.find(s => parseInt(s.semesterNumber) === semNumber);
            if (duplicate) {
                showAddError(`Semester ${semNumber} already exists for this student. Please choose a different semester number.`);
                return;
            }

            // Submit to REST API
            setAddLoading(true);

            try {
                const newSem = await ApiClient.post('/semesters', {
                    semesterNumber: semNumber,
                    academicYear: academicYear,
                    status: status
                });

                setAddLoading(false);
                addSemesterModal.hide();
                addSemesterForm.reset();
                ApiClient.showToast(`Semester ${newSem.semesterNumber || semNumber} created successfully!`, 'success');
                loadSemesters();

            } catch (err) {
                setAddLoading(false);
                console.error('[SemesterModule] Create semester error:', err);
                showAddError(err.message || 'Failed to create semester. Please verify server connection.');
            }
        });
    }

    function showAddError(msg) {
        addSemesterErrorAlert.textContent = msg;
        addSemesterErrorAlert.classList.remove('d-none');
    }

    function setAddLoading(isLoading) {
        addSemesterSubmitBtn.disabled = isLoading;
        if (isLoading) {
            addSemesterSpinner.classList.remove('d-none');
        } else {
            addSemesterSpinner.classList.add('d-none');
        }
    }

    // Auto pre-select next semester number when opening Add modal
    const openAddSemesterBtn = document.getElementById('openAddSemesterBtn');
    if (openAddSemesterBtn) {
        openAddSemesterBtn.addEventListener('click', () => {
            addSemesterErrorAlert.classList.add('d-none');
            // Find lowest unused semester number 1..8
            const existingNums = new Set(allSemesters.map(s => parseInt(s.semesterNumber)));
            let nextNum = 1;
            for (let i = 1; i <= 8; i++) {
                if (!existingNums.has(i)) {
                    nextNum = i;
                    break;
                }
            }
            if (addSemesterNumber) addSemesterNumber.value = String(nextNum);
        });
    }

    // Quick Year Suggestion Buttons
    document.querySelectorAll('.year-suggestion').forEach(btn => {
        btn.addEventListener('click', () => {
            if (addAcademicYear) addAcademicYear.value = btn.dataset.year;
        });
    });

    // -------------------------------------------------------------------------
    // Edit Semester Form Submission
    // -------------------------------------------------------------------------
    if (editSemesterForm) {
        editSemesterForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            editSemesterErrorAlert.classList.add('d-none');

            const semId = parseInt(editSemesterId.value);
            const semNumber = parseInt(editSemesterNumber.value);
            const academicYear = (editAcademicYear.value || '').trim();
            const status = editSemesterStatus.value || 'Active';

            if (isNaN(semNumber) || semNumber < 1 || semNumber > 8) {
                showEditError('Please select a valid semester number between 1 and 8.');
                return;
            }

            if (!academicYear) {
                showEditError('Academic year is required.');
                return;
            }

            // Check duplicate semester number on other semesters
            const duplicate = allSemesters.find(s => parseInt(s.semesterNumber) === semNumber && (s.id || s.semesterId) !== semId);
            if (duplicate) {
                showEditError(`Semester ${semNumber} already exists for this student.`);
                return;
            }

            setEditLoading(true);

            try {
                await ApiClient.put(`/semesters/${semId}`, {
                    semesterNumber: semNumber,
                    academicYear: academicYear,
                    status: status
                });

                setEditLoading(false);
                editSemesterModal.hide();
                ApiClient.showToast(`Semester ${semNumber} updated successfully!`, 'success');
                loadSemesters();

            } catch (err) {
                setEditLoading(false);
                console.error('[SemesterModule] Edit error:', err);
                showEditError(err.message || 'Failed to update semester.');
            }
        });
    }

    function showEditError(msg) {
        editSemesterErrorAlert.textContent = msg;
        editSemesterErrorAlert.classList.remove('d-none');
    }

    function setEditLoading(isLoading) {
        editSemesterSubmitBtn.disabled = isLoading;
        if (isLoading) {
            editSemesterSpinner.classList.remove('d-none');
        } else {
            editSemesterSpinner.classList.add('d-none');
        }
    }

    // -------------------------------------------------------------------------
    // Student Identity UI
    // -------------------------------------------------------------------------
    function updateStudentIdentityUI() {
        const user = ApiClient.getUser();
        const studentName = user ? (user.name || user.fullName || 'Student') : 'Student';
        const regNo = user ? (user.registrationNumber || '') : '';

        if (currentStudentLabel) currentStudentLabel.textContent = regNo ? `${studentName} (${regNo})` : studentName;
        if (activeStudentBadge) activeStudentBadge.textContent = regNo ? `${studentName} (${regNo})` : studentName;
    }

    // Retry connection button
    if (retryConnectionBtn) {
        retryConnectionBtn.addEventListener('click', () => {
            checkHealth();
            loadSemesters();
        });
    }

    const reloadApiBtn = document.getElementById('reloadApiBtn');
    if (reloadApiBtn) {
        reloadApiBtn.addEventListener('click', (e) => {
            e.preventDefault();
            checkHealth();
            loadSemesters();
        });
    }

    // Helper: Escape HTML string
    function escapeHtml(str) {
        if (!str) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }
});
